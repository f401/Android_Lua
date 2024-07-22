package io.github.rosemoe.sora.lang.completion;

public enum CompletionItemKind {
    Identifier("I", 0, 0xffabb6bdL),
    Text("T", 0, 0xffabb6bdL),
    Method("M", 1, 0xfff4b2beL),
    Function("F", 2, 0xfff4b2beL),
    Constructor("C", 3, 0xfff4b2beL),
    Field("F", 4, 0xfff1c883L),
    Variable("V", 5, 0xfff1c883L),
    Class("C", 6, 0xff85cce5L),
    Interface("I", 7, 0xff99cb87L),
    Module("M", 8, 0xff85cce5L),
    Property("P", 9, 0xffcebcf4L),
    Unit("U", 10),
    Value("V", 11, 0xfff1c883L),
    Enum("E", 12, 0xff85cce5L),
    Keyword("K", 13, 0xffcc7832L),
    Snippet("S", 14),
    Color("C", 15, 0xfff4b2beL),
    Reference("R", 17),
    File("F", 16),
    Folder("F", 18),
    EnumMember("E", 19),
    Constant("C", 20, 0xfff1c883L),
    Struct("S", 21, 0xffcebcf4L),
    Event("E", 22),
    Operator("O", 23, 0xffeaabb6L),
    TypeParameter("T", 24, 0xfff1c883L),
    User("U", 25),
    Issue("I", 26);

    private final int value;
    private final long defaultDisplayBackgroundColor;
    private final String displayChar;

    CompletionItemKind(String displayChar, int value, long defaultDisplayBackgroundColor) {
        this.value = value;
        this.defaultDisplayBackgroundColor = defaultDisplayBackgroundColor;
        this.displayChar = displayChar;
    }

    CompletionItemKind(String displayChar, int value) {
        this(displayChar, value, 0);
    }

    public int getValue() {
        return value;
    }

    public long getDefaultDisplayBackgroundColor() {
        return defaultDisplayBackgroundColor;
    }

    public String getDisplayChar() {
        return displayChar;
    }
}