package com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.mini;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import androidx.fragment.app.Fragment;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Mini;

import lombok.Setter;
import timber.log.Timber;

public class MiniPlayerGestureListener extends GestureDetector.SimpleOnGestureListener
        implements OnUpListener {

    public boolean allowAlphaAnimation;

    enum ScrollDirection {
        VERTICAL, HORIZONTAL;
    }

    @Setter
    private View view;

    private Fragment fragment;

    final private MiniPlayerCloseListener miniPlayerCloseListener;

    float dX, dY, scrollDestination;
    private float originalY;
    private float originalX;
    private ScrollDirection scrollDirection;

    public MiniPlayerGestureListener(Fragment fragment, MiniPlayerCloseListener closeListener) {
        this.fragment = fragment;
        miniPlayerCloseListener = closeListener;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        Timber.d("onScroll distanceX: %s distanceY: %s e1.getAction: %s e2.getAction: %s", distanceX, distanceY,
                e1.getAction(), e2.getAction());

        if (scrollDirection == null) {

            float absY = Math.abs(distanceY);
            float absX = Math.abs(distanceX);

            if (e2.getAction() == MotionEvent.ACTION_MOVE && absY > 0
                    && absY > absX ) {
                scrollDirection = ScrollDirection.VERTICAL;
            } else if (e2.getAction() == MotionEvent.ACTION_MOVE && absY > 0
                    && absX > absY) {
                scrollDirection = ScrollDirection.HORIZONTAL;
            }
        }

        if (scrollDirection == ScrollDirection.VERTICAL){
            // drag up
            scrollDestination = e2.getRawY() + dY;
            float alpha = 1 - (Math.abs(originalY - scrollDestination) / 300f);

            ObjectAnimator animationY = ObjectAnimator.ofFloat(view, "Y", scrollDestination);
            ObjectAnimator animationAlpha = ObjectAnimator.ofFloat(view, "Alpha", alpha);
            AnimatorSet animation = new AnimatorSet();
            if (allowAlphaAnimation) {
                animation.playTogether(animationY, animationAlpha);
            } else {
                animation.play(animationY);
            }
            animation.setDuration(0);
            animation.start();

        } else if (scrollDirection == ScrollDirection.HORIZONTAL){
            // drag up
            scrollDestination = e2.getRawX() + dX;
            float alpha = 1 - (Math.abs(originalX - scrollDestination) / 300f);

            ObjectAnimator animationY = ObjectAnimator.ofFloat(view, "X", scrollDestination);
            ObjectAnimator animationAlpha = ObjectAnimator.ofFloat(view, "Alpha", alpha);
            AnimatorSet animation = new AnimatorSet();
            if (allowAlphaAnimation) {
                animation.playTogether(animationY, animationAlpha);
            } else {
                animation.play(animationY);
            }
            animation.setDuration(0);
            animation.start();
        }

        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Timber.d("onDown %s", e.getAction());

        scrollDestination = 0;

        if (fragment instanceof Mini){
            originalY = ((Mini) fragment).getViewPositionY();
            originalX = ((Mini) fragment).getViewPositionX();
            dY = originalY - e.getRawY();
            dX = originalX - e.getRawX();
        } else {
            originalY = view.getY();
            originalX = view.getX();
            dY = view.getY() - e.getRawY();
            dX = view.getX() - e.getRawX();
        }
        return super.onDown(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Timber.d("onFling velocityX: %s velocityY: %s e1.getAction: %s e2.getAction: %s", velocityX, velocityY,
                e1.getAction(), e2.getAction());

        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public void onUp(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP){

            if (scrollDirection == ScrollDirection.VERTICAL) {

                Timber.d("onUp VERTICAL scrollDistance: %s", originalY - scrollDestination);
                if ( Math.abs(originalY - scrollDestination) > 300) {
                    Timber.d("onUp vertical close");
                    animate(view, "Y", originalY);
                } else {
                    animateWithAlpha(view, "Y", originalY, 1);
                }

            } else if (scrollDirection == ScrollDirection.HORIZONTAL) {

                Timber.d("onUp HORIZONTAL scrollDistance: %s", originalX - scrollDestination);
                if ( Math.abs(originalX - scrollDestination) > 300) {
                    Timber.d("onUp horizontal close");
                    animate(view, "X", originalX);
                } else {
                    animateWithAlpha(view, "X", originalX, 1);
                }
            }
            scrollDirection = null;
        }
    }

    private void animate(View view, String propertyName, float floatValue){
        ObjectAnimator animation = ObjectAnimator.ofFloat(view, propertyName, floatValue);
        animation.setDuration(0);
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (fragment instanceof Mini){
                    ((Mini) fragment).setUpMiniOnTouchListener(false);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
                miniPlayerCloseListener.doClose();
                animation.removeAllListeners();
                animation.cancel();
            }
        });
        animation.start();
    }

    private void animateWithAlpha(View view, String propertyName, float floatValue, float alphaValue){
        ObjectAnimator animation = ObjectAnimator.ofFloat(view, propertyName, floatValue);
        ObjectAnimator animationAlpha = ObjectAnimator.ofFloat(view, "Alpha", alphaValue);
        AnimatorSet set = new AnimatorSet();
        if (allowAlphaAnimation) {
            set.playTogether(animation, animationAlpha);
        } else {
            set.play(animation);
        }
        set.setDuration(500);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (fragment instanceof Mini){
                    ((Mini) fragment).setUpMiniOnTouchListener(false);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (fragment instanceof Mini){
                    ((Mini) fragment).setUpMiniOnTouchListener(true);
                }
                animation.removeAllListeners();
                animation.cancel();
            }
        });
        set.start();
    }
}
