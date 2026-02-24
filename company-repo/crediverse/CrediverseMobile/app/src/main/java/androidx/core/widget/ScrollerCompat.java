package androidx.core.widget;

import android.content.Context;
import android.widget.OverScroller;

/**
 * Shim for the removed ScrollerCompat class.
 * Jetifier remaps android.support.v4.widget.ScrollerCompat -> androidx.core.widget.ScrollerCompat,
 * but this class was removed from AndroidX. This shim delegates to android.widget.OverScroller.
 * Required by hellocharts-library 1.5.8.
 */
public class ScrollerCompat {

    private final OverScroller mScroller;

    private ScrollerCompat(Context context) {
        mScroller = new OverScroller(context);
    }

    public static ScrollerCompat create(Context context) {
        return new ScrollerCompat(context);
    }

    public boolean computeScrollOffset() {
        return mScroller.computeScrollOffset();
    }

    public int getCurrX() {
        return mScroller.getCurrX();
    }

    public int getCurrY() {
        return mScroller.getCurrY();
    }

    public int getFinalX() {
        return mScroller.getFinalX();
    }

    public int getFinalY() {
        return mScroller.getFinalY();
    }

    public void fling(int startX, int startY, int velocityX, int velocityY,
                      int minX, int maxX, int minY, int maxY) {
        mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mScroller.startScroll(startX, startY, dx, dy, duration);
    }

    public void abortAnimation() {
        mScroller.abortAnimation();
    }

    public boolean isFinished() {
        return mScroller.isFinished();
    }

    public void forceFinished(boolean finished) {
        mScroller.forceFinished(finished);
    }
}
