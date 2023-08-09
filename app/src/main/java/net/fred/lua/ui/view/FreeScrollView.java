package net.fred.lua.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FreeScrollView extends View {
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private Scroller mScroller;
    private Matrix mMatrix;

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
        return new TouchNavigation();
    }

    private Paint mPaint;

    protected void init(Context context) {
        TouchNavigation touchNavigation = constructTouchNavigation();
        mGestureDetector = new GestureDetector(context, touchNavigation);
        mScaleGestureDetector = new ScaleGestureDetector(context, touchNavigation);

        mScroller = new Scroller(context);
        mMatrix = new Matrix();

        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
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

    public boolean isScrolling() {
        return !mScroller.isFinished();
    }

    public void stopScroll() {
        mScroller.forceFinished(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.concat(mMatrix);
        super.onDraw(canvas);
    }

    protected void onUp() {
    }

    protected int getMaxScrollX() {
        return Integer.MAX_VALUE;
    }

    protected int getMaxScrollY() {
        return Integer.MAX_VALUE;
    }

    public class TouchNavigation extends GestureDetector.SimpleOnGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            mMatrix.postTranslate(-distanceX, -distanceY);
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            mScroller.fling(getScrollX(), getScrollY(),
                    (int) -velocityX,
                    (int) -velocityY,
                    0, getMaxScrollX(),
                    0, getMaxScrollY());
            postInvalidate();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            if (isScrolling()) {
                stopScroll();
            }
            return true;
        }

        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            mMatrix.postScale(scale, scale, detector.getFocusX(), detector.getFocusY());
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {

        }
    }
}
