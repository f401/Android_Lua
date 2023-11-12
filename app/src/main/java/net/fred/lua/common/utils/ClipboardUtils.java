package net.fred.lua.common.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;

public class ClipboardUtils {

    /**
     * Obtain clipboard manager based on @{code ctx}
     *
     * @param ctx Context required.
     * @return Clipboard manager.
     */
    public static ClipboardManager getClipBoardManager(@NonNull Context ctx) {
        return (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    /**
     * Copy `text` to the clipboard.
     */
    public static void copy(@NonNull Context ctx, @NonNull String text) {
        ClipboardManager manager = getClipBoardManager(ctx);
        manager.setPrimaryClip(ClipData.newPlainText(ctx.getPackageName(), text));
    }
}
