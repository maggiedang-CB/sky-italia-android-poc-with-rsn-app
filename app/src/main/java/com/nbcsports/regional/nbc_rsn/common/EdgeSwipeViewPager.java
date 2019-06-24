package com.nbcsports.regional.nbc_rsn.common;

/**
 * Created by Arkady Koplyarov on 2018-04-06.
 */

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.nbcsports.regional.nbc_rsn.utils.DataMenuUtils;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * A ViewPager providing horizontal edge swipe behaviour.
 */
public class EdgeSwipeViewPager extends ViewPager {
    private boolean enabled = true;
    private int fragmentOrientation;
    private boolean centerTouched = false;

    //Remember start points
    float mInitX;
    float mInitY;
    boolean mCatched = false;
    int mThreshold = 80; //30; //10; // For easier swiping, have the edge wider...

    public EdgeSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((getCurrentItem() == 0 && getChildCount() == 0)
                || DataMenuUtils.INSTANCE.getDATA_MENU_IS_OPENED()) {
            // this is to handle an IndexOutOfBoundsException
            return false;
        }

        return this.enabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if ((getCurrentItem() == 0 && getChildCount() == 0)
                || DataMenuUtils.INSTANCE.getDATA_MENU_IS_OPENED()) {
            // this handles an IndexOutOfBoundsException
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                // On attempting to start team view edge-swiping (in portrait mode only),
                // enable it, because it might be disabled on exiting Fab Menu (by MainActivity.exitMenu)
                setPagingEnabled(fragmentOrientation != ORIENTATION_LANDSCAPE);

                mInitX = event.getX();
                mInitY = event.getY();
                break;

            default:
                break;
        }
        mCatched = (mInitX < mThreshold || mInitX > (this.getWidth() - mThreshold));
        centerTouched = mCatched;

        return this.enabled && centerTouched && super.onInterceptTouchEvent(event);

    }

    /**
     * To disable the swiping, call the setPagingEnabled method with false
     * and users won't be able to switch the views
     *
     * @param enabled
     */
    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * It's to allow users switching the views in portrait mode only,
     * supposing that if switched to landscape mode user has persistent player in landscape (in full screen).
     *
     * @param orientation
     */
    public void setFragmentOrientation(int orientation) {
        fragmentOrientation = orientation;
    }


}
