package net.fred.lua.ui.view;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;

public class TouchNavigation extends GestureDetector.SimpleOnGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
    public static final int SCROLLBAR_HORIZONTAL = 1;
    public static final int SCROLLBAR_VERTICAL = 2;

    private final FreeScrollView freeSv;
    private int touchedScrollBar;

    public TouchNavigation(FreeScrollView freeScrollView) {
        this.freeSv = freeScrollView;
        this.touchedScrollBar = 0;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        if (touchedScrollBar == SCROLLBAR_VERTICAL) {
            float scrollPos = (float) freeSv.computeVerticalScrollRange() / freeSv.getContentHeight();
            freeSv.smoothScrollBy(0, (int) -(distanceY * scrollPos));
        } else if (touchedScrollBar == SCROLLBAR_HORIZONTAL) {
            float scrollPos = (float) freeSv.computeHorizontalScrollRange() / freeSv.getContentWidth();
            freeSv.smoothScrollBy((int) -(distanceX * scrollPos), 0);
        } else {
            freeSv.scrollBy((int) distanceX, (int) distanceY);
        }
        return true;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        int x = freeSv.screenToViewX((int) e.getX()),
                y = freeSv.screenToViewY((int) e.getY());
        boolean onVerticalScrollbarTrack = freeSv.isOnVerticalScrollBarTrack(x, y);
        boolean onHorizontalScrollbarTrack = freeSv.isOnHorizontalScrollbarTrack(x, y);
        if (onVerticalScrollbarTrack || onHorizontalScrollbarTrack) {
            if (onVerticalScrollbarTrack) {
                touchedScrollBar = SCROLLBAR_VERTICAL;
            } else {
                touchedScrollBar = SCROLLBAR_HORIZONTAL;
            }
            freeSv.extendScrollbar(false);
            return false;
        }
        if (touchedScrollBar > 0) {//has touched
            touchedScrollBar = 0;
            freeSv.shrinkScrollbar(false);
        }
        return false;
    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        freeSv.fling((int) -velocityX, (int) -velocityY);
        return true;
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        if (touchedScrollBar > 0) {
            if (touchedScrollBar == SCROLLBAR_VERTICAL) {
                float scrollPos = (float) freeSv.computeVerticalScrollRange() / freeSv.getContentHeight();
                freeSv.smoothScrollTo(freeSv.getScrollX(), (int) (scrollPos * e.getY()));
            } else if (touchedScrollBar == SCROLLBAR_HORIZONTAL) {
                float scrollPos = (float) freeSv.computeHorizontalScrollRange() / freeSv.getContentWidth();
                freeSv.smoothScrollTo((int) (scrollPos * e.getX()), freeSv.getScrollY());
            }
            return true;
        }
        if (freeSv.isScrolling()) {
            freeSv.stopScroll();
            return true;
        }
        return false;
    }

    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
        float scaleFactor = freeSv.getScaleFactor() * detector.getScaleFactor();
        if (scaleFactor > FreeScrollView.SCALE_MAX) {
            scaleFactor = FreeScrollView.SCALE_MAX;
        } else if (scaleFactor < FreeScrollView.SCALE_MIN) {
            scaleFactor = FreeScrollView.SCALE_MIN;
        }

        freeSv.invalidateScaleFactor(scaleFactor);
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
