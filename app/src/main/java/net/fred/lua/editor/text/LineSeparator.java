package net.fred.lua.editor.text;

import androidx.annotation.NonNull;

public enum LineSeparator {
    LF("\n"), CRLF("\r\n"), CR("\r");

    private final String _char;

    LineSeparator(String c) {
        this._char = c;
    }

    @NonNull
    public String getChar() {
        return _char;
    }

    public int length() {
        return _char.length();
    }
}
