package io.github.rosemoe.sora.util;

import android.os.Handler;
import android.os.Looper;

public class EditorHandler extends Handler {
    public static EditorHandler INSTANCE = new EditorHandler();

    public EditorHandler() {
        super(Looper.getMainLooper());
    }
}
