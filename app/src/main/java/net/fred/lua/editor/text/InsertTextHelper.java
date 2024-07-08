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

class InsertTextHelper {
    private static final InsertTextHelper[] sCached = new InsertTextHelper[8];
    static int TYPE_LINE_CONTENT = 0;
    static int TYPE_NEWLINE = 1;
    static int TYPE_EOF = 2;
    private CharSequence mText;
    private int mIndex, mIndexNext, mLength;

    private synchronized static InsertTextHelper obtain() {
        for (int i = 0; i < sCached.length; i++) {
            if (sCached[i] != null) {
                InsertTextHelper cache = sCached[i];
                sCached[i] = null;
                return cache;
            }
        }
        return new InsertTextHelper();
    }

    public static InsertTextHelper forInsertion(@NonNull CharSequence text) {
        InsertTextHelper o = obtain();
        o.init(text);
        return o;
    }

    public void recycle() {
        synchronized (InsertTextHelper.class) {
            for (int i = 0; i < sCached.length; i++) {
                if (sCached[i] == null) {
                    sCached[i] = this;
                    reset();
                    break;
                }
            }
        }
    }

    private void init(@NonNull CharSequence text) {
        this.mText = text;
        mIndex = -1;
        mIndexNext = 0;
        mLength = text.length();
    }

    public int getIndex() {
        return mIndex;
    }

    public int getIndexNext() {
        return mIndexNext;
    }

    public int forward() {
        mIndex = mIndexNext;
        if (mIndex == mLength) {
            return TYPE_EOF;
        }
        char ch = mText.charAt(mIndex);
        switch (ch) {
            case '\n':
                mIndexNext = mIndex + 1;
                return TYPE_NEWLINE;
            case '\r':
                if (mIndex + 1 < mLength && mText.charAt(mIndex + 1) == '\n') {
                    mIndexNext = mIndex + 2;
                } else {
                    mIndexNext = mIndex + 1;
                }
                return TYPE_NEWLINE;
            default:
                mIndexNext = mIndex + 1;
                while (mIndexNext < mLength) {
                    ch = mText.charAt(mIndexNext);
                    if (ch == '\n' || ch == '\r') {
                        break;
                    }
                    mIndexNext++;
                }
                return TYPE_LINE_CONTENT;
        }
    }

    public void reset() {
        mText = null;
        mLength = mIndex = 0;
    }
}
