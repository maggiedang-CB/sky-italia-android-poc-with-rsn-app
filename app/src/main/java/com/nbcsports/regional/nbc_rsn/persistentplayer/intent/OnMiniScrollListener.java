package com.nbcsports.regional.nbc_rsn.persistentplayer.intent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import androidx.fragment.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;

import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.layouts.Mini;
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils;

public class OnMiniScrollListener {
    private final Mini mini;
    private final PersistentPlayer persistentPlayer;
    private AnimatorSet animationShow;
    private ObjectAnimator animationHide;

    public OnMiniScrollListener(Mini mini) {
        this.mini = mini;
        persistentPlayer = mini.getPersistentPlayer();
    }

    public void show(FragmentManager supportFragmentManager) {
        persistentPlayer.log(this, "mini.show()");
        mini.setShown(true);

        ActivityUtils.showFragment(supportFragmentManager, mini);

        // Animation for show
        ObjectAnimator animationY = ObjectAnimator.ofFloat((ViewGroup) mini.getView().getParent(), "Y", mini.getViewPositionY());
        animationY.setDuration(PlayerConstants.MINI_PLAYER_ANIMATION_DURATION);
        ObjectAnimator animationAlpha = ObjectAnimator.ofFloat((ViewGroup) mini.getView().getParent(), "alpha", 1);
        animationShow = new AnimatorSet();
        animationShow.playTogether(animationY, animationAlpha);
        animationShow.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                persistentPlayer.log(this, "mini.show() successful");
                // Enable close gesture listener
                mini.setUpMiniOnTouchListener(true);
                // fix an issue
                if (persistentPlayer.getType() == PlayerConstants.Type.MEDIUM){
                    hide(supportFragmentManager);
                }
                animationShow.removeAllListeners();
                animationShow.cancel();
            }
        });
        animationShow.start();

        // Additional UI treatment for mini
        mini.getExoPlayerView().setUseController(false);
        mini.getPersistentPlayerView().getSwitchScreenView().setVisibility(View.GONE);
        // flicking the mini away will set the view to INVISIBLE, so this is the place to set it back
        // to visible
        ((ViewGroup)mini.getView().getParent()).setVisibility(View.VISIBLE);

        // Ensure mini continue to play
        if (persistentPlayer.getPlayerEngine() != null) {
            persistentPlayer.getPlayerEngine().setPlayWhenReady(persistentPlayer.getPlayerEngine().getPlayWhenReady());
        }

        if (persistentPlayer.getPlayerEngine() != null)
            persistentPlayer.getPlayerEngine().addListener(mini);
    }

    public void hide(FragmentManager supportFragmentManager) {
        persistentPlayer.log(this, "mini.hide()");
        mini.setShown(false);

        // Animation for hide
        animationHide = ObjectAnimator.ofFloat((ViewGroup) mini.getView().getParent(), "Y", mini.getScreenHeight());
        animationHide.setDuration(PlayerConstants.MINI_PLAYER_ANIMATION_DURATION);
        animationHide.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ActivityUtils.hideFragment(supportFragmentManager, mini);
                animationHide.removeAllListeners();
                animationHide.cancel();
                persistentPlayer.log(this, "mini.hide() successful");
            }
        });
        // prevent weird up then down animation
        if (animationShow != null && !animationShow.isRunning()) {
            animationHide.start();
        }

        // Disable close gesture listener
        mini.setUpMiniOnTouchListener(false);

        if (persistentPlayer.getPlayerEngine() != null)
            persistentPlayer.getPlayerEngine().removeListener(mini);
    }
}
