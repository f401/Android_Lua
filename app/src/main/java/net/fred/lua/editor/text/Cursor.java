package net.fred.lua.editor.text;

import androidx.annotation.NonNull;

import com.google.common.collect.Lists;

import java.util.List;

public class Cursor {

    private final List<TextRange> mRanges;

    public Cursor() {
        this.mRanges = Lists.newArrayList();
    }

    public TextRange[] getRanges() {
        return mRanges.toArray(new TextRange[0]);
    }

    public void newRange(@NonNull CharPosition left, @NonNull CharPosition right) {
        mRanges.add(new TextRange(left, right));
    }
}
