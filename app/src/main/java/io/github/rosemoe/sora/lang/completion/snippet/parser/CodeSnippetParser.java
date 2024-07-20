/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 */
package io.github.rosemoe.sora.lang.completion.snippet.parser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.ConditionalFormat;
import io.github.rosemoe.sora.lang.completion.snippet.FormatString;
import io.github.rosemoe.sora.lang.completion.snippet.NextUpperCaseFormat;
import io.github.rosemoe.sora.lang.completion.snippet.NoFormat;
import io.github.rosemoe.sora.lang.completion.snippet.PlaceHolderElement;
import io.github.rosemoe.sora.lang.completion.snippet.PlaceholderDefinition;
import io.github.rosemoe.sora.lang.completion.snippet.PlainPlaceholderElement;
import io.github.rosemoe.sora.lang.completion.snippet.Transform;
import io.github.rosemoe.sora.lang.completion.snippet.VariableItem;

public class CodeSnippetParser {

    private final String src;
    private final CodeSnippet.Builder builder;
    private final CodeSnippetTokenizer tokenizer;
    private Token token;

    private CodeSnippetParser(String snippet, List<PlaceholderDefinition> definitions) {
        src = snippet;
        builder = new CodeSnippet.Builder(definitions);
        tokenizer = new CodeSnippetTokenizer(snippet);
    }

    private static void appendPlaceholderElement(@NonNull ArrayList<PlaceHolderElement> elements, @NonNull String t) {
        if (!elements.isEmpty()) {
            if (elements.get(elements.size() - 1) instanceof PlainPlaceholderElement) {
                // merge with the last plain placeholder element
                PlainPlaceholderElement plain = (PlainPlaceholderElement) elements.get(elements.size() - 1);
                plain.setText(plain.getText() + t);
                return;
            }
        }
        elements.add(new PlainPlaceholderElement(t));
    }

    public static CodeSnippet parse(@NonNull String snippet) {
        return parse(snippet, new ArrayList<>());
    }

    public static CodeSnippet parse(@NonNull String snippet, @NonNull List<PlaceholderDefinition> definitions) {
        CodeSnippetParser parser = new CodeSnippetParser(snippet, definitions);
        parser.parse();
        return parser.builder.build();
    }

    private void next() {
        if (token.type == TokenType.EOF) {
            return;
        }
        token = tokenizer.nextToken();
    }

    private boolean accept(TokenType type) {
        if (token.type == type) {
            next();
            return true;
        }
        return false;
    }

    private String _accept(TokenType type) {
        if (token.type == type) {
            String text = src.substring(token.index, token.index + token.length);
            next();
            return text;
        }
        return null;
    }

    private boolean accept(TokenType... types) {
        for (TokenType type : types) {
            if (token.type == type) {
                next();
                return true;
            }
        }
        return false;
    }

    private String _accept(TokenType... types) {
        if (types.length == 0) {
            String text = src.substring(token.index, token.index + token.length);
            next();
            return text;
        }
        for (TokenType type : types) {
            if (token.type == type) {
                String text = src.substring(token.index, token.index + token.length);
                next();
                return text;
            }
        }
        return null;
    }

    private void backTo(Token token) {
        tokenizer.moveTo(token.index + token.length);
        this.token = token;
    }

    private void parse() {
        token = tokenizer.nextToken();
        while (parseInternal()) {
            //empty
        }
    }

    private boolean parseInternal() {
        return parseEscaped() ||
                parseTabStopOrVariableName() ||
                parseComplexVariable() ||
                parseComplexPlaceholder() ||
                parseInterpolatedShell() ||
                parseAnything();
    }

    private boolean parseEscaped() {
        if (accept(TokenType.Backslash)) {
            String escaped = _accept(TokenType.CurlyClose, TokenType.Dollar, TokenType.Backslash, TokenType.Backtick);
            if (escaped == null) {
                escaped = "\\";
            }
            builder.addPlainText(escaped);

            return true;
        }
        return false;
    }

    private boolean parseInterpolatedShell() {
        Token backup = token;
        if (accept(TokenType.Backtick)) {
            StringBuilder sb = new StringBuilder();
            while (!accept(TokenType.Backtick)) {
                if (accept(TokenType.Backslash)) {
                    if (accept(TokenType.Backtick)) {
                        sb.append('`');
                    } else {
                        sb.append('\\');
                    }
                } else if (token.type == TokenType.EOF) {
                    backTo(backup);
                    return false;
                } else {
                    sb.append(_accept());
                }
            }
            builder.addInterpolatedShell(sb.toString());
            return true;
        }
        backTo(token);
        return false;
    }

    private boolean parseTabStopOrVariableName() {
        Token backup = token;
        if (accept(TokenType.Dollar)) {
            String text;
            if ((text = _accept(TokenType.Int)) != null) {
                builder.addPlaceholder(Integer.parseInt(text));
            } else if ((text = _accept(TokenType.VariableName)) != null) {
                builder.addVariable(text, (String) null);
            } else {
                backTo(backup);
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean parseComplexVariable() {
        final VariableItem variable = _parseComplexVariable();
        if (variable != null) {
            builder.addVariable(variable);
        }
        return variable != null;
    }

    @Nullable
    private VariableItem _parseComplexVariable() {
        Token backup = token;
        String text;
        if (accept(TokenType.Dollar) && accept(TokenType.CurlyOpen) && (text = _accept(TokenType.VariableName)) != null) {
            String variableName = text;
            String defaultValue = null;
            if (accept(TokenType.Colon)) {
                // ${name:xxx}
                StringBuilder sb = new StringBuilder();
                while (!accept(TokenType.CurlyClose)) {
                    if (accept(TokenType.Backslash)) {
                        if ((text = _accept(TokenType.Backslash, TokenType.Dollar, TokenType.CurlyClose)) != null) {
                            sb.append(text);
                        } else {
                            sb.append('\\');
                        }
                    } else if (token.type == TokenType.EOF) {
                        backTo(backup);
                        return null;
                    } else {
                        sb.append(src, token.index, token.index + token.length);
                        next();
                    }
                }
                return new VariableItem(-1, variableName, sb.toString());
            } else if (accept(TokenType.Forwardslash)) {
                // ${name/regexp/format/options}
                Transform transform = new Transform();
                if (parseTransform(transform)) {
                    return new VariableItem(-1, variableName, null, transform);
                }
                backTo(backup);
                return null;
            } else if (accept(TokenType.CurlyClose)) {
                // ${name}
                return new VariableItem(-1, variableName, "");
            } else {
                // missing token
                backTo(backup);
                return null;
            }
        }
        backTo(backup);
        return null;
    }

    private boolean parseComplexPlaceholder() {
        Token backup = token;
        String text;
        if (accept(TokenType.Dollar) && accept(TokenType.CurlyOpen) && (text = _accept(TokenType.Int)) != null) {
            String idText = text;
            if (accept(TokenType.Colon)) {
                // ${1:xxx}
                final ArrayList<PlaceHolderElement> elements = new ArrayList<PlaceHolderElement>();
                while (!accept(TokenType.CurlyClose)) {
                    if (accept(TokenType.Backslash)) {
                        String t;
                        if ((text = _accept(TokenType.Backslash, TokenType.Dollar, TokenType.CurlyClose)) != null) {
                            t = text;
                        } else {
                            t = "\\";
                        }

                        appendPlaceholderElement(elements, t);
                    } else if (token.type == TokenType.EOF) {
                        backTo(backup);
                        return false;
                    } else {
                        String v = parseSimpleVariableName();
                        if (v != null) {
                            elements.add(new VariableItem(token.index, v, ""));
                            continue;
                        }

                        VariableItem vi = _parseComplexVariable();
                        if (vi != null) {
                            vi.setIndex(token.index);
                            elements.add(vi);
                            continue;
                        }

                        String t = src.substring(token.index, token.index + token.length);
                        appendPlaceholderElement(elements, t);
                        next();
                    }
                }
                final int id = Integer.parseInt(idText);
                builder.addComplexPlaceholder(id, elements);
            } else if (accept(TokenType.Pipe)) {
                // ${1|one,two,three|}
                ArrayList<String> choices = new ArrayList<String>();
                while (true) {
                    if (parseChoiceElement(choices)) {
                        if (accept(TokenType.Comma)) {
                            continue;
                        }
                        if (accept(TokenType.Pipe) && accept(TokenType.CurlyClose)) {
                            builder.addPlaceholder(Integer.parseInt(idText), choices);
                            return true;
                        }
                    }

                    backTo(backup);
                    return false;
                }
            } else if (accept(TokenType.Forwardslash)) {
                // ${1/regexp/format/options}
                Transform transform = new Transform();
                if (parseTransform(transform)) {
                    builder.addPlaceholder(Integer.parseInt(idText), transform);
                    return true;
                }
                backTo(backup);
                return false;
            } else if (accept(TokenType.CurlyClose)) {
                // ${1}
                builder.addPlaceholder(Integer.parseInt(idText));
            } else {
                // missing token
                backTo(backup);
                return false;
            }
            return true;
        }
        backTo(backup);
        return false;
    }

    @Nullable
    private String parseSimpleVariableName() {
        Token backup = token;
        if (accept(TokenType.Dollar)) {
            // Check for : $VARIABLE_NAME
            String v = _accept(TokenType.VariableName);
            if (v != null) {
                return v;
            }
        }
        backTo(backup);
        return null;
    }

    private boolean parseChoiceElement(List<String> choices) {
        Token backup = token;
        StringBuilder sb = new StringBuilder();
        String text;
        while (token.type != TokenType.Comma && token.type != TokenType.Pipe) {
            if (accept(TokenType.Backslash)) {
                if ((text = _accept(TokenType.Pipe, TokenType.Comma, TokenType.Backslash)) != null) {
                    sb.append(text);
                } else {
                    sb.append('\\');
                }
            } else if (token.type != TokenType.EOF) {
                sb.append(_accept());
            } else {
                backTo(backup);
                return false;
            }
        }
        if (sb.length() == 0) {
            backTo(backup);
            return false;
        }
        choices.add(sb.toString());
        return true;
    }

    private boolean parseTransform(Transform transform) {
        // ...<regex>/<format>/<options>}
        Token backup = token;

        // (1) /regex
        StringBuilder regexValue = new StringBuilder();
        while (!accept(TokenType.Forwardslash)) {
            if (accept(TokenType.Backslash)) {
                if (accept(TokenType.Forwardslash)) {
                    regexValue.append('/');
                } else {
                    regexValue.append('\\');
                }
                continue;
            }

            if (token.type != TokenType.EOF) {
                regexValue.append(_accept());
                continue;
            }

            return false;
        }

        // (2) /format
        ArrayList<FormatString> list = new ArrayList<FormatString>();
        while (!accept(TokenType.Forwardslash)) {
            if (accept(TokenType.Backslash)) {
                String escaped;
                if ((escaped = _accept(TokenType.Backslash, TokenType.Forwardslash)) != null) {
                    list.add(new NoFormat(escaped));
                } else if ((escaped = _accept(TokenType.VariableName)) != null) {
                    if ("u".equals(escaped)) {
                        list.add(new NextUpperCaseFormat());
                    } else {
                        list.add(new NoFormat("\\" + escaped));
                    }
                } else {
                    list.add(new NoFormat("\\"));
                }
                continue;
            }

            if (parseFormatString(list) || parseAnything(list)) {
                continue;
            }
            return false;
        }

        // (3) /option
        StringBuilder regexOptions = new StringBuilder();
        while (!accept(TokenType.CurlyClose)) {
            if (token.type != TokenType.EOF) {
                regexOptions.append(_accept());
                continue;
            }
            return false;
        }

        try {
            int options = 0;
            if (regexOptions.indexOf("i") != -1) {
                options |= Pattern.CASE_INSENSITIVE;
            }
            if (regexOptions.indexOf("m") != -1) {
                options |= Pattern.MULTILINE;
            }
            transform.globalMode = (regexOptions.indexOf("g") != -1);
            transform.regexp = Pattern.compile(regexValue.toString(), options);
        } catch (PatternSyntaxException e) {
            backTo(backup);
            return false;
        }
        transform.format = list;
        return true;
    }

    private boolean parseFormatString(List<FormatString> formatStrings) {
        Token backup = token;
        if (!accept(TokenType.Dollar)) {
            return false;
        }
        boolean complex = accept(TokenType.CurlyOpen);
        String text;
        if ((text = _accept(TokenType.Int)) == null) {
            backTo(backup);
            return false;
        }
        int group = Integer.parseInt(text);
        ConditionalFormat format = new ConditionalFormat();
        format.setGroup(group);
        if (complex) {
            if (accept(TokenType.Colon)) {
                if (accept(TokenType.Forwardslash)) {
                    // ${1:/upcase}
                    if ((text = _accept(TokenType.VariableName)) != null && accept(TokenType.CurlyClose)) {
                        format.setShorthand(text);
                        formatStrings.add(format);
                        return true;
                    }
                } else if (accept(TokenType.Plus)) {
                    // ${1:+<if>}
                    String ifValue = until(TokenType.CurlyClose);
                    if (ifValue != null) {
                        accept(TokenType.CurlyClose);
                        format.setIfValue(ifValue);
                        formatStrings.add(format);
                        return true;
                    }
                } else if (accept(TokenType.Dash)) {
                    String elseValue = until(TokenType.CurlyClose);
                    if (elseValue != null) {
                        accept(TokenType.CurlyClose);
                        format.setElseValue(elseValue);
                        formatStrings.add(format);
                        return true;
                    }
                } else if (accept(TokenType.QuestionMark)) {
                    String ifValue = until(TokenType.Colon);
                    accept(TokenType.Colon);
                    String elseValue = until(TokenType.CurlyClose);
                    if (ifValue != null && elseValue != null) {
                        accept(TokenType.CurlyClose);
                        format.setIfValue(ifValue);
                        format.setElseValue(elseValue);
                        formatStrings.add(format);
                        return true;
                    }
                } else {
                    String elseValue = until(TokenType.CurlyClose);
                    if (elseValue != null) {
                        accept(TokenType.CurlyClose);
                        format.setElseValue(elseValue);
                        formatStrings.add(format);
                        return true;
                    }
                }
            }
            backTo(backup);
            return false;
        } else {
            // $1
            formatStrings.add(format);
            return true;
        }
    }

    private boolean parseAnything(List<FormatString> formatStrings) {
        if (token.type == TokenType.EOF) {
            return false;
        } else {
            formatStrings.add(new NoFormat(_accept()));
        }
        return true;
    }

    private boolean parseAnything() {
        if (token.type == TokenType.EOF) {
            return false;
        } else {
            builder.addPlainText(_accept());
        }
        return true;
    }

    private String until(TokenType type) {
        Token backup = token;
        StringBuilder sb = new StringBuilder();
        while (token.type != type) {
            if (token.type == TokenType.EOF) {
                backTo(backup);
                return null;
            } else {
                String text;
                if (accept(TokenType.Backslash)) {
                    if ((text = _accept(TokenType.Backslash, TokenType.Dollar, TokenType.CurlyClose)) != null) {
                        sb.append(text);
                    } else {
                        backTo(backup);
                        return null;
                    }
                } else {
                    sb.append(_accept());
                }
            }
        }
        return sb.toString();
    }

}
