/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 */
package net.fred.lua.editor.text;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import net.fred.lua.common.utils.StringUtils;

public enum LineSeparator {
    /** Used for Linux. */
    LF("\n"),
    /** Used for Windows. DOS format.*/
    CRLF("\r\n"),
    /** Used for Classic Mac Os. */
    CR("\r");

    /**
     * Get target line separator from a line separator string.
     *
     * @param src line separator string
     * @throws IllegalArgumentException if the given str is not a line separator
     */
    public static LineSeparator fromString(String src) {
        Preconditions.checkArgument(!StringUtils.isEmpty(src));
        switch (src) {
            case "\n":
                return LF;
            case "\r":
                return CR;
            case "\r\n":
                return CRLF;
            default:
                return null;
        }
    }

    /**
     * Get target line separator from a line separator string.
     *
     * @param text  the whole text
     * @param start start index of the line separator
     * @param end   end index of the line separator
     * @throws IllegalArgumentException if the given str is not a line separator
     */
    public static LineSeparator fromString(@NonNull CharSequence text, int start, int end) {
        Preconditions.checkNotNull(text, "text must not be null");
        if (end == start) {
            return null;
        }
        if (end - start == 1) {
            char ch = text.charAt(start);
            if (ch == '\r') return CR;
            if (ch == '\n') return LF;
        }
        if (end - start == 2 && text.charAt(start) == '\r' && text.charAt(start + 1) == '\n') {
            return CRLF;
        }
        throw new IllegalArgumentException("unknown line separator type");
    }

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
