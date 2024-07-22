package io.github.rosemoe.sora.lang.completion.snippet;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PlaceholderDefinition {
    private int id;
    @Nullable
    private List<String> choices;
    private List<PlaceHolderElement> elements;
    @Nullable
    private Transform transform;
    @Nullable
    private String text;


    public PlaceholderDefinition(int id, @Nullable List<String> choices, List<PlaceHolderElement> elements, @Nullable Transform transform) {
        this.id = id;
        this.choices = choices;
        this.elements = elements;
        this.transform = transform;
        this.text = null;
    }

    public PlaceholderDefinition(int id) {
        this(id, null, new ArrayList<PlaceHolderElement>(), null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Nullable
    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(@Nullable List<String> choices) {
        this.choices = choices;
    }

    public List<PlaceHolderElement> getElements() {
        return elements;
    }

    public void setElements(List<PlaceHolderElement> elements) {
        this.elements = elements;
    }

    @Nullable
    public String getText() {
        return text;
    }

    public void setText(@Nullable String text) {
        this.text = text;
    }

    @Nullable
    public Transform getTransform() {
        return transform;
    }

    public void setTransform(@Nullable Transform transform) {
        this.transform = transform;
    }
}
