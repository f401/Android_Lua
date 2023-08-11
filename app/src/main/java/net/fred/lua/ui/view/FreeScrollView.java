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
    public static final int DEFAULT_BOTTOM_GAP_SIZE = 400;
    public static final int BOUNDARY_PROMPT_MAX_TOP = 30;

    public static final int TOP_BOUNDARY_PROMPT = 1;
    public static final int LEFT_BOUNDARY_PROMPT = 2;
    public static final int RIGHT_BOUNDARY_PROMPT = 4;
    public static final int BOTTOM_BOUNDARY_PROMPT = 8;

    public static final float SCALE_MAX = 2.0f;
    public static final float SCALE_MIN = 0.5f;

    public static final float TEXT_MIN_SIZE = 8f;
    public static final float TEXT_MAX_SIZE = 80f;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private OverScroller mScroller;
    private float mScaleFactor;
    private float mScaleFocusX, mScaleFocusY;
    private Paint mLineBrush, mBoundaryPaint;
    private int mDrawBoundaryPrompt;


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

    /**
     * Set a circle O with a radius of r and a chord length of l.
     * The distance from the chord to the arc is m (@{link #BOUNDARY_PROMPT_MAX_TOP}).
     * Given l and t, find r.
     * <p>
     * According to the Pythagorean theorem, the following formulas can be listed:
     * r ^ 2=(r - m) ^ 2+(l/2) ^ 2
     * According to the complete square formula, it can be concluded that:
     * r ^ 2=r ^ 2-2rm+m ^ 2+(l/2) ^ 2
     * Transfer and merge to obtain:
     * 2rm=m ^ 2+(l/2) ^ 2
     * Ultimately available:
     * r=(m ^ 2 + (l/2) ^ 2)/2m
     * </p>
     */
    protected static float evalCircleRadius(float l) {
        return (MathUtils.square(BOUNDARY_PROMPT_MAX_TOP) +
                MathUtils.square(l / 2)) / (2 * BOUNDARY_PROMPT_MAX_TOP);
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

    // ----------------------------------------- boundary prompt ------------------------------//

    protected void init(Context context) {
        TouchNavigation touchNavigation = constructTouchNavigation();
        mGestureDetector = new GestureDetector(context, touchNavigation);
        mScaleGestureDetector = new ScaleGestureDetector(context, touchNavigation);

        mScroller = new OverScroller(context);

        mLineBrush = new Paint();
        mLineBrush.setColor(Color.BLACK);
        mLineBrush.setTextSize(50f);

        mBoundaryPaint = new Paint();
        mBoundaryPaint.setColor(Color.GRAY);

        mScaleFactor = 1.0f;
        mScaleFocusX = mScaleFocusY = 0.0f;
        mDrawBoundaryPrompt = 0;

        setFocusable(true);
    }

    /**
     * direction is defined as 'TOP_BOUNDARY_PROMPT, LEFT_BOUNDARY_PROMPT, RIGHT_BOUNDARY_PROMPT
     * and BOTTOM_BOUNDARY_PROMPT'.
     */
    public void drawBoundaryPrompt(int direction) {
        mDrawBoundaryPrompt |= direction;
    }

    /**
     * Called by @{link #onDraw}
     */
    private void realDrawBoundaryPrompt(Canvas canvas) {
        mDrawBoundaryPrompt &= 15;//Mask

        if ((mDrawBoundaryPrompt & TOP_BOUNDARY_PROMPT) > 0) {
            float r = evalCircleRadius(getWidth());
            canvas.drawCircle((float) getWidth() / 2,
                    BOUNDARY_PROMPT_MAX_TOP - r, r, mBoundaryPaint);
        }

        mDrawBoundaryPrompt = 0; //reset
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        realDrawBoundaryPrompt(canvas);
        canvas.drawText("1", getWidth() / 2, getHeight() / 2, mLineBrush);

        //canvas.scale(mScaleFactor, mScaleFactor, mScaleFocusX, mScaleFocusY);
        super.onDraw(canvas);
        canvas.restore();
    }

    public int rowHeight() {
        Paint.FontMetricsInt fontMetrics = mLineBrush.getFontMetricsInt();
        return fontMetrics.descent - fontMetrics.ascent;
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
            drawBoundaryPrompt(LEFT_BOUNDARY_PROMPT);
        } else if (x > maxX) {
            x = maxX;
            drawBoundaryPrompt(RIGHT_BOUNDARY_PROMPT);
        }

        final float maxY = Math.max(getScrollY(), getMaxScrollY());
        if (y < 0) {
            y = 0;
            drawBoundaryPrompt(TOP_BOUNDARY_PROMPT);
        } else if (y > maxY) {
            y = (int) maxY;
            drawBoundaryPrompt(BOTTOM_BOUNDARY_PROMPT);
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
        float newTextSize = newScaleFactor * mLineBrush.getTextSize();
        newTextSize = Math.min(TEXT_MAX_SIZE, newTextSize);
        newTextSize = Math.max(TEXT_MIN_SIZE, newTextSize);
        mLineBrush.setTextSize(newTextSize);
        postInvalidate();
    }



}
