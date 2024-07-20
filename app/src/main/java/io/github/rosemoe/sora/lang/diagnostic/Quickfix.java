package io.github.rosemoe.sora.lang.diagnostic;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.common.base.MoreObjects;

public class Quickfix {
    @Nullable
    private final CharSequence title;
    private final long documentVersion;
    @Nullable
    private final Runnable action;

    @StringRes
    private int resourceId;

    public Quickfix(@Nullable CharSequence title, long documentVersion, @Nullable Runnable action) {
        this.title = title;
        this.documentVersion = documentVersion;
        this.action = action;
    }

    public Quickfix(@StringRes int resourceId, long documentVersion, @Nullable Runnable action) {
        this(null, documentVersion, action);
        this.resourceId = resourceId;
    }

    @NonNull
    public CharSequence resolveTitle(@NonNull Context context) {
        return MoreObjects.firstNonNull(title, context.getString(resourceId));
    }

    public void executeQuickfix() {
        if (action != null) {
            action.run();
        }
    }
}