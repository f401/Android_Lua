package io.github.rosemoe.sora.lang.completion;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class SimpleCompletionIconDrawer {

    public static Drawable draw(CompletionItemKind kind, boolean circle) {
        return new CircleDrawable(kind, circle);
    }

    public static Drawable draw(CompletionItemKind kind) {
        return draw(kind, true);
    }

    private static class CircleDrawable extends Drawable {

        private final CompletionItemKind kind;
        private final boolean circle;
        private final Paint mPaint;
        private final Paint mTextPaint;

        private CircleDrawable(CompletionItemKind kind, boolean circle) {
            this.kind = kind;
            this.circle = circle;
            this.mPaint = new Paint();
            this.mPaint.setAntiAlias(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.mPaint.setColor(kind.getDefaultDisplayBackgroundColor());
            } else {
                this.mPaint.setColor((int) kind.getDefaultDisplayBackgroundColor());
            }

            this.mTextPaint = new Paint();
            this.mTextPaint.setColor(-0x1);
            this.mTextPaint.setAntiAlias(true);
            this.mTextPaint.setTextSize(14 * Resources.getSystem().getDisplayMetrics().densityDpi);
            this.mTextPaint.setTextAlign(Paint.Align.CENTER);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            final float width = getBounds().right;
            final float height = getBounds().bottom;
            if (circle) {
                canvas.drawCircle(width / 2, height / 2, width / 2, mPaint);
            } else {
                canvas.drawRect(0f, 0f, width, height, mPaint);
            }
            canvas.save();
            canvas.translate(width / 2, height / 2);
            float textCenter = -(mTextPaint.descent() + mTextPaint.ascent()) / 2f;
            canvas.drawText(kind.getDisplayChar(), 0f, textCenter, mTextPaint);
            canvas.restore();
        }

        @Override
        public void setAlpha(int alpha) {
            this.mPaint.setAlpha(alpha);
            this.mTextPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            this.mTextPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }
}