package net.fred.lua.ui.view;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;

public class TouchNavigation extends GestureDetector.SimpleOnGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
    private final FreeScrollView freeSv;
    private boolean touchedVerticalScrollBar;

    public TouchNavigation(FreeScrollView freeScrollView) {
        this.freeSv = freeScrollView;
        this.touchedVerticalScrollBar = false;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        if (touchedVerticalScrollBar) {
            float scrollPos = (float) freeSv.computeVerticalScrollRange() / freeSv.getContentHeight();
            freeSv.smoothScrollBy(0, (int) -(distanceY * scrollPos));
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
        if (onVerticalScrollbarTrack) {
            touchedVerticalScrollBar = true;
            if (freeSv.isOnVerticalScrollbar(x, y)) {
                freeSv.extendScrollbar(true);
                return false;//User may scroll bar.
            } else {
                freeSv.extendScrollbar(false);
                float scrollPos = (float) freeSv.computeVerticalScrollRange() / freeSv.getContentHeight();
                freeSv.smoothScrollTo(0, (int) (scrollPos * e.getY()));
            }
            return true;
        }
        if (touchedVerticalScrollBar) {
            touchedVerticalScrollBar = false;
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
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        if (freeSv.isScrolling()) {
            freeSv.stopScroll();
        }
        return true;
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
