package io.github.rosemoe.sora.widget.snippet;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.concurrent.LazyInit;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import io.github.rosemoe.sora.event.EventReceiver;
import io.github.rosemoe.sora.event.InterceptTarget;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.event.SnippetEvent;
import io.github.rosemoe.sora.event.Unsubscribe;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.InterpolatedShellItem;
import io.github.rosemoe.sora.lang.completion.snippet.PlaceHolderElement;
import io.github.rosemoe.sora.lang.completion.snippet.PlaceholderDefinition;
import io.github.rosemoe.sora.lang.completion.snippet.PlaceholderItem;
import io.github.rosemoe.sora.lang.completion.snippet.PlainPlaceholderElement;
import io.github.rosemoe.sora.lang.completion.snippet.PlainTextItem;
import io.github.rosemoe.sora.lang.completion.snippet.SnippetItem;
import io.github.rosemoe.sora.lang.completion.snippet.VariableItem;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentLine;
import io.github.rosemoe.sora.text.Indexer;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.snippet.variable.ClipboardBasedSnippetVariableResolver;
import io.github.rosemoe.sora.widget.snippet.variable.CommentBasedSnippetVariableResolver;
import io.github.rosemoe.sora.widget.snippet.variable.CompositeSnippetVariableResolver;
import io.github.rosemoe.sora.widget.snippet.variable.EditorBasedSnippetVariableResolver;
import io.github.rosemoe.sora.widget.snippet.variable.RandomBasedSnippetVariableResolver;
import io.github.rosemoe.sora.widget.snippet.variable.TimeBasedSnippetVariableResolver;
import io.github.rosemoe.sora.widget.snippet.variable.WorkspaceBasedSnippetVariableResolver;


/**
 * Manage snippet editing in editor
 *
 * @author Rosemoe
 */
public final class SnippetController {
    private static final Pattern lineSeparatorRegex = Pattern.compile("\\r|\\n|\\r\\n");

    @NonNull
    private final CodeEditor editor;
    private final CompositeSnippetVariableResolver variableResolver;
    /**
     * Workspace based variable resolver. User should implement this class and set it when workspace is updated
     */
    @Nullable
    private WorkspaceBasedSnippetVariableResolver workspaceVariableResolver;
    @LazyInit
    private CommentBasedSnippetVariableResolver commentVariableResolver;
    @Nullable
    private CodeSnippet currentSnippet;
    private int snippetIndex;
    private List<PlaceholderItem> tabStops;
    private int currentTabStopIndex;
    private boolean inSequenceEdits;

    public SnippetController(@NonNull CodeEditor editor) {
        this.editor = editor;

        this.variableResolver = new CompositeSnippetVariableResolver();
        variableResolver.addResolver(new ClipboardBasedSnippetVariableResolver(editor.getClipboardManager()));
        variableResolver.addResolver(new EditorBasedSnippetVariableResolver(editor));
        variableResolver.addResolver(new RandomBasedSnippetVariableResolver());
        variableResolver.addResolver(new TimeBasedSnippetVariableResolver());
        variableResolver.addResolver(getCommentVariableResolver());

        editor.subscribeEvent(SelectionChangeEvent.class, new EventReceiver<SelectionChangeEvent>() {
            @Override
            public void onReceive(@NonNull SelectionChangeEvent event, @NonNull Unsubscribe unsubscribe) {
                if (isInSnippet()) {
                    if (!checkIndex(event.getLeft().index) || !checkIndex(event.getRight().index)) {
                        stopSnippet();
                    }
                }
            }
        });
    }

    private boolean checkIndex(int index) {
        PlaceholderItem editing = getEditingTabStop();
        return index >= editing.getStartIndex() && index <= editing.getEndIndex();
    }

    /**
     * Start a new snippet editing. The given [CodeSnippet] must pass the checks in [CodeSnippet.checkContent].
     * Otherwise, the snippet editing will not be started.
     * No matter whether a new snippet editing is started, the existing snippet editing will get cancelled after
     * calling this method.
     */
    public void startSnippet(int index, @NonNull CodeSnippet snippet, @NonNull String selectedText) {
        if (snippetIndex != -1) {
            stopSnippet();
        }
        // Stage 1: verify the snippet structure
        if (!snippet.checkContent() || snippet.getItems().isEmpty()) {
            Log.e("SnippetController", "invalid code snippet");
            return;
        }
        CodeSnippet clonedSnippet = snippet.clone();
        currentSnippet = clonedSnippet;
        currentTabStopIndex = -1;
        snippetIndex = index;
        // Stage 2: resolve the variables and execute shell codes
        List<SnippetItem> elements = clonedSnippet.getItems();
        HashMap<String, PlaceholderDefinition> variableItemMapping = Maps.newHashMap();
        int maxTabStop = 0;
        for (SnippetItem it : elements) {
            if (it instanceof PlaceholderItem && ((PlaceholderItem) it).getDefinition().getId() > maxTabStop) {
                maxTabStop = ((PlaceholderItem) it).getDefinition().getId();
            }
        }
        for (int i = 0; i < elements.size(); ++i) {
            SnippetItem item = elements.get(i);
            if (item instanceof VariableItem) {
                String value = null;
                VariableItem vi = (VariableItem) item;
                if (variableResolver.canResolve(vi.getName())) {
                    value = variableResolver.resolve(vi.getName());
                } else if ("selection".equals(vi.getName())) {
                    value = selectedText;
                } else if (vi.getDefaultValue() != null) {
                    value = vi.getDefaultValue();
                }

                if (value != null) {
                    // Resolved variable value
                    value = TransformApplier.doTransform(value, vi.getTransform());
                    int deltaIndex = value.length() - (item.getEndIndex() - item.getStartIndex());
                    elements.set(i, new PlainTextItem(
                            value,
                            item.getStartIndex(),
                            item.getStartIndex() + value.length()
                    ));
                    shiftItemsFrom(i + 1, deltaIndex);
                } else {
                    // Convert to placeholder
                    PlaceholderDefinition def;
                    if (variableItemMapping.containsKey(vi.getName())) {
                        def = variableItemMapping.get(vi.getName());
                    } else {
                        def = new PlaceholderDefinition(++maxTabStop);
                        def.setText(vi.getName());
                        variableItemMapping.put(vi.getName(), def);
                    }
                    elements.set(i, new PlaceholderItem(def, item.getStartIndex()));
                    int deltaIndex = vi.getName().length() - (vi.getEndIndex() - vi.getStartIndex());
                    shiftItemsFrom(i + 1, deltaIndex);
                }
            } else if (item instanceof InterpolatedShellItem) {
                InterpolatedShellItem isi = (InterpolatedShellItem) item;
                String value = "";
                try {
                    Process proc = Runtime.getRuntime().exec("sh");
                    OutputStream os = proc.getOutputStream();
                    os.write(isi.getShellCode().getBytes(StandardCharsets.UTF_8));
                    os.write("\nexit\n".getBytes(StandardCharsets.UTF_8));
                    os.flush();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = proc.getInputStream().read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    value = baos.toString("UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int lastIndex = value.length() - 1;
                char lastChar = value.charAt(lastIndex);
                if (!value.isEmpty() && lastChar == '\n' || lastChar == '\r') {
                    if (lastChar == '\r' || (lastIndex > 0 && value.charAt(lastIndex - 1) != '\r')
                            || lastIndex == 0) {
                        value = value.substring(0, lastIndex);
                    } else {
                        value = value.substring(0, lastIndex - 1);
                    }
                }
                int deltaIndex = value.length() - (item.getEndIndex() - item.getStartIndex());
                elements.set(i, new PlainTextItem(
                        value,
                        item.getStartIndex(),
                        item.getStartIndex() + value.length()
                ));
                shiftItemsFrom(i + 1, deltaIndex);
            }
        }
        // Stage 3: clean useless items and shift all items to editor index
        Iterators.removeIf(clonedSnippet.getItems().iterator(), new Predicate<SnippetItem>() {
            @Override
            public boolean apply(@Nullable SnippetItem item) {
                return item instanceof PlainTextItem && ((PlainTextItem) item).getText().isEmpty();
            }
        });
        shiftItemsFrom(0, index);
        // Stage 4: make correct indentation
        Content text = editor.getText();
        CharPosition pos = text.getIndexer().getCharPosition(index);
        ContentLine line = text.getLine(pos.line);
        int indentEnd = 0;
        for (int i = 0; i < pos.column; ++i) {
            char c = line.charAt(i);
            if (c == ' ' || c == '\t') {
                indentEnd++;
            } else {
                break;
            }
        }
        ContentLine indentText = line.subSequence(0, indentEnd);
        List<SnippetItem> items = clonedSnippet.getItems();
        for (int i = 0; i < items.size(); ++i) {
            SnippetItem snippetItem = items.get(i);
            if (snippetItem instanceof PlainTextItem &&
                    lineSeparatorRegex.matcher(((PlainTextItem) snippetItem).getText()).find()) {
                PlainTextItem pti = (PlainTextItem) snippetItem;
                boolean first = true;
                StringBuilder sb = new StringBuilder();

                for (String it : pti.getText().split(lineSeparatorRegex.pattern())) {
                    if (first) {
                        sb.append(it);
                        first = false;
                    } else {
                        sb.append(editor.getLineSeparator().getContent())
                                .append(indentText)
                                .append(it);
                    }
                }
                int deltaIndex = sb.length() - pti.getText().length();
                pti.setText(sb.toString());
                snippetItem.setIndex(snippetItem.getStartIndex(), snippetItem.getEndIndex() + deltaIndex);
                shiftItemsFrom(i + 1, deltaIndex);
            } else if (snippetItem instanceof PlaceholderItem) {
                PlaceholderDefinition definition = ((PlaceholderItem) snippetItem).getDefinition();
                if (definition.getElements().isEmpty()) {
                    continue;
                }

                StringBuilder sb = new StringBuilder();
                int deltaIndex = 0;
                for (PlaceHolderElement element : definition.getElements()) {
                    if (element instanceof PlainPlaceholderElement) {
                        PlainPlaceholderElement ppe = (PlainPlaceholderElement) element;
                        sb.append(ppe.getText());
                        deltaIndex += ppe.getText().length();
                    } else if (element instanceof VariableItem) {
                        VariableItem vi = (VariableItem) element;
                        /*var value = when {
                            variableResolver.canResolve(element.name) -> variableResolver.resolve(
                                    element.name
                            )

                            element.name == "selection" -> selectedText
                            element.defaultValue != null -> element.defaultValue
                            else -> null
                        }*/
                        String value = null;
                        if (variableResolver.canResolve(vi.getName())) {
                            value = variableResolver.resolve(vi.getName());
                        } else if ("selection".equals(vi.getName())) {
                            value = selectedText;
                        } else if (vi.getDefaultValue() != null) {
                            value = vi.getDefaultValue();
                        }

                        if (value != null) {
                            value = TransformApplier.doTransform(value, vi.getTransform());
                            sb.append(value);
                            deltaIndex += value.length();
                        } else {
                            sb.append(vi.getName());
                            deltaIndex += vi.getName().length();
                        }
                    }
                }

                definition.setText(sb.toString());
                snippetItem.setIndex(snippetItem.getStartIndex(), snippetItem.getEndIndex() + deltaIndex);
                shiftItemsFrom(i + 1, deltaIndex);
            }
        }
        // Stage 5: collect tab stops and placeholders
        List<PlaceholderItem> tabStops = Lists.newArrayList();
        for (SnippetItem item : clonedSnippet.getItems()) {
            if (item instanceof PlaceholderItem) {
                final PlaceholderItem pi = (PlaceholderItem) item;
                if (pi.getDefinition().getId() != 0
                        && Iterators.find(tabStops.iterator(), new Predicate<PlaceholderItem>() {
                    @Override
                    public boolean apply(PlaceholderItem input) {
                        return input.getDefinition().equals(pi.getDefinition());
                    }
                }) == null) {
                    tabStops.add(pi);
                }
            }
        }
        Collections.sort(tabStops, new Comparator<PlaceholderItem>() {
            @Override
            public int compare(PlaceholderItem o1, PlaceholderItem o2) {
                return Ints.compare(o1.getDefinition().getId(), o2.getDefinition().getId());
            }
        });
        PlaceholderItem end = (PlaceholderItem) Iterators.find(clonedSnippet.getItems().iterator(), new Predicate<SnippetItem>() {
            @Override
            public boolean apply(@Nullable SnippetItem input) {
                return input instanceof PlaceholderItem && ((PlaceholderItem) input).getDefinition().getId() == 0;
            }
        });
        if (end == null) {
            end = new PlaceholderItem(new PlaceholderDefinition(0),
                    elements.get(elements.size() - 1).getEndIndex()
            );
            clonedSnippet.getItems().add(end);
        }
        tabStops.add(end);
        this.tabStops = tabStops;
        // Stage 6: insert the text
        StringBuilder sb = new StringBuilder();
        for (SnippetItem it : clonedSnippet.getItems()) {
            if (it instanceof PlainTextItem) {
                sb.append(((PlainTextItem) it).getText());
            } else if (it instanceof PlaceholderItem) {
                PlaceholderDefinition definition = ((PlaceholderItem) it).getDefinition();
                if (!TextUtils.isEmpty(definition.getText())) {
                    sb.append(definition.getText());
                }
            }
        }
        text.insert(pos.line, pos.column, sb);
        // Stage 7: shift to the first tab stop
        if ((editor.dispatchEvent(
                new SnippetEvent(
                        editor,
                        SnippetEvent.ACTION_START,
                        currentTabStopIndex,
                        tabStops.size()
                )) & InterceptTarget.TARGET_EDITOR) != 0) {
            stopSnippet();
            return;
        }
        shiftToTabStop(0);
    }

    /**
     * Check whether the editor in snippet editing
     */
    public boolean isInSnippet() {
        return snippetIndex != -1 && currentTabStopIndex != -1;
    }

    @Nullable
    public PlaceholderItem getEditingTabStop() {
        if (snippetIndex == -1)
            return null;
        else
            return tabStops.get(currentTabStopIndex);
    }

    @Nullable
    public PlaceholderItem getTabStopAt(int index) {
        return tabStops == null ? null : tabStops.get(index);
    }

    public int getTabStopCount() {
        return tabStops == null ? 0 : tabStops.size();
    }

    @NonNull
    public List<SnippetItem> getEditingRelatedTabStops() {
        PlaceholderItem editing = getEditingTabStop();
        if (editing != null) {
            return Lists.newArrayList(Collections2.filter(currentSnippet.getItems(), new Predicate<SnippetItem>() {
                @Override
                public boolean apply(SnippetItem it) {
                    return it instanceof PlaceholderItem
                            && ((PlaceholderItem) it).getDefinition()
                            == editing.getDefinition() && !it.equals(editing);
                }
            }));
        }
        return Lists.newArrayList();
    }

    @NonNull
    public List<SnippetItem> getInactiveTabStops() {
        PlaceholderItem editing = getEditingTabStop();
        if (editing != null) {
            return Lists.newArrayList(Collections2.filter(currentSnippet.getItems(), new Predicate<SnippetItem>() {
                @Override
                public boolean apply(SnippetItem it) {
                    return it instanceof PlaceholderItem
                            && ((PlaceholderItem) it).getDefinition()
                            != editing.getDefinition();
                }
            }));
        }
        return Lists.newArrayList();

    }

    public boolean isEditingRelated(@NonNull SnippetItem it) {
        PlaceholderItem editing = getEditingTabStop();
        if (editing != null) {
            return it instanceof PlaceholderItem &&
                    ((PlaceholderItem) it).getDefinition() == editing.getDefinition() && it != editing;
        }
        return false;
    }

    public void shiftToPreviousTabStop() {
        if (snippetIndex != -1 && currentTabStopIndex > 0) {
            shiftToTabStop(currentTabStopIndex - 1);
        }
    }

    public void shiftToNextTabStop() {
        if (snippetIndex != -1 && currentTabStopIndex < tabStops.size() - 1) {
            shiftToTabStop(currentTabStopIndex + 1);
        }
    }

    private void shiftToTabStop(int index) {
        if (snippetIndex == -1) {
            return;
        }
        if (index != currentTabStopIndex && currentTabStopIndex != -1) {
            // apply transform
            PlaceholderItem tabStop = tabStops.get(currentTabStopIndex);
            if (tabStop.getDefinition().getTransform() != null) {
                editor.getText().replace(
                        tabStop.getStartIndex(),
                        tabStop.getEndIndex(),
                        TransformApplier.doTransform(
                                editor.getText().substring(
                                        tabStop.getStartIndex(),
                                        tabStop.getEndIndex()
                                ), tabStop.getDefinition().getTransform()
                        )
                );
            }
        }
        PlaceholderItem tabStop = tabStops.get(currentTabStopIndex);
        Indexer indexer = editor.getText().getIndexer();
        CharPosition left = indexer.getCharPosition(tabStop.getStartIndex());
        CharPosition right = indexer.getCharPosition(tabStop.getEndIndex());
        currentTabStopIndex = index;
        editor.setSelectionRegion(left.line, left.column, right.line, right.column);
        editor.dispatchEvent(
                new SnippetEvent(
                        editor,
                        SnippetEvent.ACTION_SHIFT,
                        currentTabStopIndex,
                        tabStops.size()
                )
        );
        if (index == tabStops.size() - 1) {
            stopSnippet();
        }
    }

    private void shiftItemsFrom(int itemIndex, int deltaIndex) {
        if (deltaIndex == 0) {
            return;
        }
        List<SnippetItem> items = currentSnippet.getItems();
        for (SnippetItem item : items) {
            item.shiftIndex(deltaIndex);
        }
    }

    /**
     * Stop snippet editing
     */
    public void stopSnippet() {
        if (!isInSnippet()) {
            return;
        }
        currentSnippet = null;
        snippetIndex = -1;
        tabStops = null;
        currentTabStopIndex = -1;
        editor.dispatchEvent(new SnippetEvent(editor, SnippetEvent.ACTION_STOP, currentTabStopIndex, 0));
        editor.invalidate();
    }

    /**
     * Language based variable resolver. User should set valid values when change language.
     */
    public CommentBasedSnippetVariableResolver getCommentVariableResolver() {
        if (commentVariableResolver == null) {
            commentVariableResolver = new CommentBasedSnippetVariableResolver(null);
        }
        return commentVariableResolver;
    }

    @Nullable
    public WorkspaceBasedSnippetVariableResolver getWorkspaceVariableResolver() {
        return workspaceVariableResolver;
    }

    public void setWorkspaceVariableResolver(@Nullable WorkspaceBasedSnippetVariableResolver workspaceVariableResolver) {
        if (workspaceVariableResolver != null) {
            variableResolver.removeResolver(workspaceVariableResolver);
        }
        this.workspaceVariableResolver = workspaceVariableResolver;
        if (workspaceVariableResolver != null) {
            variableResolver.addResolver(workspaceVariableResolver);
        }
    }

    public int getSnippetIndex() {
        return snippetIndex;
    }

    public void setSnippetIndex(int snippetIndex) {
        this.snippetIndex = snippetIndex;
    }

}
