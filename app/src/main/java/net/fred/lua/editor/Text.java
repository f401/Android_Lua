package net.fred.lua.editor;

import androidx.annotation.Nullable;

public interface Text {
    void insert(char[] src, int off);

    void replace(int from, int to, @Nullable char[] replacement);
}
