package io.github.rosemoe.sora.event;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.lang.styling.line.LineSideIcon;
import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Called when side icon is clicked.
 * If you would like to avoid [ClickEvent] to be triggered, you are expected to intercept editor by
 * calling [SideIconClickEvent.intercept]
 */
public class SideIconClickEvent extends Event {
    private final LineSideIcon clickedIcon;

    public SideIconClickEvent(@NonNull CodeEditor editor, LineSideIcon clickedIcon) {
        super(editor);
        this.clickedIcon = clickedIcon;
    }

    public LineSideIcon getClickedIcon() {
        return clickedIcon;
    }
}
