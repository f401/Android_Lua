package io.github.rosemoe.sora.lang.styling.line;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class LineStyles extends LineAnchorStyle {
    private final List<LineAnchorStyle> styles;

    public LineStyles(int line) {
        super(line);
        this.styles = Lists.newArrayList();
    }

    /**
     * Add a new style object. Note that style object of a given class is allowed to add once.
     * eg. You can not add two [LineBackground] objects even when they are exactly the same
     */
    public int addStyle(@NonNull LineAnchorStyle style) {
        Preconditions.checkArgument(!(styles instanceof LineStyles), "Can not add LineStyles object");
        Preconditions.checkArgument(style.getLine() == getLine(), "target line differs from this object");
        int result = 1;
        if (findOne(style.getClass()) != null) {
            eraseStyle(style.getClass());
            result = 0;
        }
        styles.add(style);
        return result;
    }

    public void updateElements() {
        for (LineAnchorStyle lineAnchorStyle : styles) {
            lineAnchorStyle.setLine(getLine());
        }
    }

    public <T extends LineAnchorStyle> int eraseStyle(Class<T> clazz) {
        List<T> all = findAll(clazz);
        styles.removeAll(all);
        return all.size();
    }

    @Nullable
    public <T extends LineAnchorStyle> T findOne(Class<T> clazz) {
        for (LineAnchorStyle style : styles) {
            if (clazz.isInstance(style)) {
                return (T) style;
            }
        }
        return null;
    }

    @NonNull
    public <T extends LineAnchorStyle> List<T> findAll(Class<T> clazz) {
        ArrayList<T> result = Lists.newArrayList();
        for (LineAnchorStyle style : styles) {
            if (clazz.isInstance(style)) {
                result.add((T) style);
            }
        }
        return result;
    }
}