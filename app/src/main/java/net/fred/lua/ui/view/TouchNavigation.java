package net.fred.lua.ui.view;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;

public class TouchNavigation extends GestureDetector.SimpleOnGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
    private final FreeScrollView freeScrollView;

    public TouchNavigation(FreeScrollView freeScrollView) {
        this.freeScrollView = freeScrollView;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        freeScrollView.scrollBy((int) distanceX, (int) distanceY);
        return true;
    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        freeScrollView.fling((int) -velocityX, (int) -velocityY);
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        if (freeScrollView.isScrolling()) {
            freeScrollView.stopScroll();
        }
        return true;
    }

    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
        float scaleFactor = freeScrollView.getScaleFactor() * detector.getScaleFactor();
        if (scaleFactor > FreeScrollView.SCALE_MAX) {
            scaleFactor = FreeScrollView.SCALE_MAX;
        } else if (scaleFactor < FreeScrollView.SCALE_MIN) {
            scaleFactor = FreeScrollView.SCALE_MIN;
        }

        freeScrollView.invalidateScaleFactor(scaleFactor);
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
