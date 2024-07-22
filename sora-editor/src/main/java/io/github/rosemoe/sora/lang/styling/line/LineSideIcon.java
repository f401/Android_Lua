package io.github.rosemoe.sora.lang.styling.line;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class LineSideIcon extends LineAnchorStyle {
    @NonNull
    private Drawable drawable;

    public LineSideIcon(int line, @NonNull Drawable drawable) {
        super(line);
        this.drawable = drawable;
    }

    @NonNull
    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(@NonNull Drawable drawable) {
        this.drawable = drawable;
    }
}
