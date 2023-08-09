package net.fred.lua.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class NonColoredView extends FreeScrollView {
    private Paint rowPaint;
    private int xExtend;

    public NonColoredView(Context context) {
        super(context);
    }

    public NonColoredView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NonColoredView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        rowPaint = new Paint();
        rowPaint.setTextSize(50f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        super.onDraw(canvas);
        realDraw(canvas);
        canvas.restore();
    }

    private void realDraw(Canvas canvas) {

    }
}
