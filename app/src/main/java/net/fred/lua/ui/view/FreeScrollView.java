package net.fred.lua.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FreeScrollView extends View {
    private GestureDetector mGestureDetector;
    private Scroller mScroller;
    private Paint mPaint;

    private int mMaxFlingVelocity;

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

    private void init(Context context) {
        mGestureDetector = new GestureDetector(context, constructTouchNavigation());
        mScroller = new Scroller(context);
        mPaint = new Paint();
        mPaint.setTextSize(30f);
        mPaint.setColor(Color.RED);

        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mMaxFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
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
        boolean consume = mGestureDetector.onTouchEvent(event);
        Log.i("View", consume + "");
        return consume;
    }

    public boolean isScrolling() {
        return !mScroller.isFinished();
    }

    public void stopScroll() {
        mScroller.abortAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.YELLOW);
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, 200, mPaint);
    }

    public class TouchNavigation extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            scrollBy((int) distanceX, (int) distanceY);
            Log.i("Touch", "Catch scroll");
            return true;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            Log.i("Nav", "on down");
            return true;
        }

        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            mScroller.fling(getScrollX(), getScrollY(),
                    (int) -velocityX, (int) -velocityY,
                    0, getWidth(),
                    0, getHeight());
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
    }
}
