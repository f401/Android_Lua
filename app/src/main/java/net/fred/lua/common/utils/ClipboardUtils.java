package net.fred.lua.common.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;

public class ClipboardUtils {

    public static ClipboardManager getClipBoardManager(@NonNull Context ctx) {
        return (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public static void copy(@NonNull Context ctx, @NonNull String text) {
        ClipboardManager manager = getClipBoardManager(ctx);
        manager.setPrimaryClip(ClipData.newPlainText(ctx.getPackageName(), text));
    }
}
