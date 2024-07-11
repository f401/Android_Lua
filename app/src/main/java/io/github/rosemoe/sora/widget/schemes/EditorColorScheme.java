package io.github.rosemoe.sora.widget.schemes;

public class EditorColorScheme {
    //----------------Issue colors----------------
    public static final int PROBLEM_TYPO = 37;
    public static final int PROBLEM_WARNING = 36;
    public static final int PROBLEM_ERROR = 35;
    //-----------------Highlight colors-----------
    public static final int ATTRIBUTE_VALUE = 34;
    public static final int ATTRIBUTE_NAME = 33;
    public static final int HTML_TAG = 32;
    public static final int ANNOTATION = 28;
    public static final int FUNCTION_NAME = 27;
    public static final int IDENTIFIER_NAME = 26;
    public static final int IDENTIFIER_VAR = 25;
    public static final int LITERAL = 24;
    public static final int OPERATOR = 23;
    public static final int COMMENT = 22;
    public static final int KEYWORD = 21;
    //-------------View colors---------------------
    public static final int STICKY_SCROLL_DIVIDER = 62;
    /**
     * Color for text strikethrough. If value is 0, text color of that region will be used.
     */
    public static final int STRIKETHROUGH = 57;
    /**
     * Alias for {@link #STRIKETHROUGH}
     */
    public static final int STRIKE_THROUGH = STRIKETHROUGH;
    public static final int DIAGNOSTIC_TOOLTIP_ACTION = 56;
    public static final int DIAGNOSTIC_TOOLTIP_DETAILED_MSG = 55;
    public static final int DIAGNOSTIC_TOOLTIP_BRIEF_MSG = 54;
    public static final int DIAGNOSTIC_TOOLTIP_BACKGROUND = 53;
    public static final int FUNCTION_CHAR_BACKGROUND_STROKE = 52;
    public static final int HARD_WRAP_MARKER = 51;
    public static final int TEXT_INLAY_HINT_FOREGROUND = 50;
    public static final int TEXT_INLAY_HINT_BACKGROUND = 49;
    public static final int SNIPPET_BACKGROUND_EDITING = 48;
    public static final int SNIPPET_BACKGROUND_RELATED = 47;
    public static final int SNIPPET_BACKGROUND_INACTIVE = 46;
    public static final int SIDE_BLOCK_LINE = 38;
    public static final int NON_PRINTABLE_CHAR = 31;

    /**
     * Use zero if the text color should not be changed
     */
    public static final int TEXT_SELECTED = 30;
    public static final int MATCHED_TEXT_BACKGROUND = 29;
    public static final int COMPLETION_WND_CORNER = 20;
    public static final int COMPLETION_WND_BACKGROUND = 19;
    public static final int COMPLETION_WND_TEXT_PRIMARY = 42;
    public static final int COMPLETION_WND_TEXT_SECONDARY = 43;
    public static final int COMPLETION_WND_ITEM_CURRENT = 44;

    /**
     * No longer supported
     */
    public static final int LINE_BLOCK_LABEL = 18;

    public static final int HIGHLIGHTED_DELIMITERS_BACKGROUND = 41;
    public static final int HIGHLIGHTED_DELIMITERS_UNDERLINE = 40;
    public static final int HIGHLIGHTED_DELIMITERS_FOREGROUND = 39;
    public static final int LINE_NUMBER_PANEL_TEXT = 17;
    public static final int LINE_NUMBER_PANEL = 16;
    public static final int BLOCK_LINE_CURRENT = 15;
    public static final int BLOCK_LINE = 14;
    public static final int SCROLL_BAR_TRACK = 13;
    public static final int SCROLL_BAR_THUMB_PRESSED = 12;
    public static final int SCROLL_BAR_THUMB = 11;
    public static final int UNDERLINE = 10;
    public static final int CURRENT_LINE = 9;
    public static final int SELECTION_HANDLE = 8;
    public static final int SELECTION_INSERT = 7;
    public static final int SELECTED_TEXT_BACKGROUND = 6;
    public static final int TEXT_NORMAL = 5;
    public static final int WHOLE_BACKGROUND = 4;
    public static final int LINE_NUMBER_BACKGROUND = 3;
    public static final int LINE_NUMBER_CURRENT = 45;
    public static final int LINE_NUMBER = 2;
    public static final int LINE_DIVIDER = 1;
    public static final int SIGNATURE_TEXT_NORMAL = 58;
    public static final int SIGNATURE_TEXT_HIGHLIGHTED_PARAMETER = 59;
    public static final int STATIC_SPAN_BACKGROUND = 63;
    public static final int STATIC_SPAN_FOREGROUND = 64;
    public static final int SIGNATURE_BACKGROUND = 60;
    /**
     * Min pre-defined color id
     */
    protected static final int START_COLOR_ID = 1;
}
