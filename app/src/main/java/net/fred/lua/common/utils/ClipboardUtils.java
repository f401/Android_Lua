package net.fred.lua.common.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;
import com.google.common.base.Preconditions;

public class ClipboardUtils {

    /**
     * Obtain clipboard manager based on @{code ctx}
     *
     * @param ctx Context required.
     * @return Clipboard manager.
     */
    @NonNull
    public static ClipboardManager getClipBoardManager(@NonNull Context ctx) {
        Preconditions.checkNotNull(ctx, "Context can not be null");
        return (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    /**
     * Copy `text` to the clipboard.
     */
    public static void copy(@NonNull Context ctx, @NonNull String text) {
        Preconditions.checkNotNull(ctx, "Context can not be null");
        Preconditions.checkNotNull(text, "Text can not be null");
        ClipboardManager manager = getClipBoardManager(ctx);
        manager.setPrimaryClip(ClipData.newPlainText(ctx.getPackageName(), text));
    }
}
