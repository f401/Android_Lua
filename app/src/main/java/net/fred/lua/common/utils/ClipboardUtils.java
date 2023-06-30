package net.fred.lua.common.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtils {

    public static ClipboardManager getClipBoardManager(Context ctx) {
        return (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public static void copy(Context ctx, String text) {
        ClipboardManager manager = getClipBoardManager(ctx);
        manager.setPrimaryClip(ClipData.newPlainText(ctx.getPackageName(), text));
    }
}
