package io.github.rosemoe.sora.lang.completion.snippet;

public class PlainPlaceholderElement implements PlaceHolderElement {
    private String text;

    public PlainPlaceholderElement(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
