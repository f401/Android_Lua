package net.fred.lua.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.Nullable;

import net.fred.lua.common.Logger;
import net.fred.lua.common.utils.MathUtils;

// Based on https://github.com/TIIEHenry/CodeEditor/blob/master/CodeEditor/src/main/java/tiiehenry/code/view/TouchNavigationMethod.java
public class FreeScrollView extends View {
    public static final int DEFAULT_BOTTOM_GAP_SIZE = MathUtils.dp2px(400);

    public static final float SCALE_MAX = 2.0f;
    public static final float SCALE_MIN = 0.5f;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private OverScroller mScroller;
    private float mScaleFactor;
    private float mScaleFocusX, mScaleFocusY;

    public FreeScrollView(Context context) {
        super(context);
        init(context);
    }

    public FreeScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FreeScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected TouchNavigation constructTouchNavigation() {
        return new TouchNavigation(this);
    }

    private Paint mPaint;

    protected void init(Context context) {
        TouchNavigation touchNavigation = constructTouchNavigation();
        mGestureDetector = new GestureDetector(context, touchNavigation);
        mScaleGestureDetector = new ScaleGestureDetector(context, touchNavigation);

        mScroller = new OverScroller(context);

        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);

        mScaleFactor = 1.0f;
        mScaleFocusX = mScaleFocusY = 0.0f;

        setFocusable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(useAllDimensions(widthMeasureSpec),
                useAllDimensions(heightMeasureSpec));
    }

    private int useAllDimensions(int spec) {
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);

        if (MeasureSpec.UNSPECIFIED == mode) {
            size = Integer.MAX_VALUE;
            Logger.w("MeasureSpec cannot be UNSPECIFIED. Setting dimensions to max.");
        }

        return size;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        if (!mGestureDetector.onTouchEvent(event)) {
            onUp();
        }
        return true;
    }

    protected void onUp() {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor, mScaleFocusX, mScaleFocusY);
        super.onDraw(canvas);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, 200, mPaint);
        canvas.restore();
    }

    // --------------------------------------------------------- Scroll And Scale ----------------------------------------//

    public boolean isScrolling() {
        return !mScroller.isFinished();
    }

    public void stopScroll() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }


    /**
     * This includes rolling boundary check.
     * When exceeding the boundary. Will use maximum(minimum) values.
     */
    @Override
    public void scrollTo(int x, int y) {
        final int maxX = Math.max(getScrollX(), getMaxScrollX());
        if (x < 0) {
            x = 0;
        } else if (x > maxX) {
            x = maxX;
        }

        final int maxY = Math.max(getScrollY(), getMaxScrollY());
        if (y < 0) {
            y = 0;
        } else if (y > maxY) {
            y = maxY;
        }
        super.scrollTo(x, y);
    }

    protected int getMaxScrollX() {
        return Integer.MAX_VALUE;
    }

    protected int getMaxScrollY() {
        return Integer.MAX_VALUE;
    }

    /**
     * Converts a x-coordinate from screen coordinates to local coordinates,
     * excluding padding
     */
    public int screenToViewX(int x) {
        return x - getPaddingLeft() + getScrollX();
    }

    /**
     * Converts a y-coordinate from screen coordinates to local coordinates,
     * excluding padding
     */
    public int screenToViewY(int y) {
        return y - getPaddingTop() + getScrollY();
    }

    protected void fling(int velocityX, int velocityY) {
        mScroller.fling(getScrollX(), getScrollY(),
                velocityX, velocityY,
                0, getMaxScrollX(),
                0, getMaxScrollY());
        postInvalidate();
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public void invalidateScaleFactor(float newScaleFactor, float focusX, float focusY) {
        this.mScaleFactor = newScaleFactor;
        this.mScaleFocusX = focusX;
        this.mScaleFocusY = focusY;
        postInvalidate();
    }

}
