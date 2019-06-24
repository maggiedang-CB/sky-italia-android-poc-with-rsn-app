package com.nbcsports.regional.nbc_rsn.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.fabigation.FabMenu;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;

import org.joda.time.DateTime;

public class FtueUtil {

    private static final String FAB_MENU_USAGE_COUNT = "_prefFabMenuUsageCount";
    private static final String IS_APP_FIRST_LAUNCH  = "_prefIsAppFirstLaunch";
    private static final String FAB_FLUNG            = "_prefFabBeenFlung";
    private static final String FAB_FLICK_MSG_VIEWED = "_prefFabFlickNotificationViewed";
    private static final String ARTICLE_OPENED_COUNT = "_prefArticleOpenedCount";
    private static final String FAB_TAPPED           = "_prefFabBeenTapped";
    private static final String DATA_MENU_OPENED     = "_prefDataMenuOpened";
    private static final String DATA_MENU_FTUE_DONE  = "_prefDataMenuFtueDone";

    // Ftue success view
    public static final float PIVOT_PERCENT_TYPE_1 = 0.95f;
    public static final float PIVOT_ZERO           = 0.0f;
    public static final float XY_OFFSET_TYPE_1     = 30.0f;
    public static final float XY_OFFSET_TYPE_2     = 110.0f;
    public static final float ALPHA_TYPE_1         = 0.65f;
    public static final float SCALE_ONE            = 1.0f;
    public static final float SCALE_OFFSET_TYPE_1  = 0.3f;

    public static final long DATA_MENU_MSG_MINIMUM_DURATION = 3000L;

    private static boolean isFabTapMsgViewed = false;
    private static boolean isDataMenuMsgViewed = false;

    private static long dataMenuMsgShowUpInitTime = 0L;

    private static AnimatorSet fabAnimatorSet, fabAnimatorAnimatorSet, slideIndicatorAnimatorSet;

    public static boolean hasViewedFabFlickMsg(){
        return PreferenceUtils.INSTANCE.getBoolean(FAB_FLICK_MSG_VIEWED, false);
    }

    public static void recordFabFlickMsgViewed(){
        PreferenceUtils.INSTANCE.setBoolean(FAB_FLICK_MSG_VIEWED, true);
    }

    public static boolean hasUsedFabAtMostOnce(){
        return PreferenceUtils.INSTANCE.getInt(FAB_MENU_USAGE_COUNT, 0) <= 1;
    }

    public static void recordFabUse(){
        int fabUsage = PreferenceUtils.INSTANCE.getInt(FAB_MENU_USAGE_COUNT, 0);
        PreferenceUtils.INSTANCE.setInt(FAB_MENU_USAGE_COUNT, fabUsage + 1); // increment by one
    }

    public static void recordArticleOpened() {
        int articleOpened = PreferenceUtils.INSTANCE.getInt(ARTICLE_OPENED_COUNT, 0);
        PreferenceUtils.INSTANCE.setInt(ARTICLE_OPENED_COUNT, articleOpened + 1); // increment by one
    }

    public static boolean hasOpenedArticleAtMostFourTimes() {
        return PreferenceUtils.INSTANCE.getInt(ARTICLE_OPENED_COUNT, 0) <= 4;
    }

    public static void recordFabTapToClose() {
        int fabUsage = PreferenceUtils.INSTANCE.getInt(FAB_TAPPED, 0);
        PreferenceUtils.INSTANCE.setInt(FAB_TAPPED, fabUsage + 1); // increment by one
    }

    public static int getFabTapToClose() {
        return PreferenceUtils.INSTANCE.getInt(FAB_TAPPED, 0);
    }

    public static void setFabTapMsgViewed(boolean isFabTapMsgViewed) {
        FtueUtil.isFabTapMsgViewed = isFabTapMsgViewed;
    }

    public static boolean hasViewedFabTapMsg() {
        return FtueUtil.isFabTapMsgViewed;
    }

    public static boolean hasOpenedDataMenu() {
        return PreferenceUtils.INSTANCE.getBoolean(DATA_MENU_OPENED, false);
    }

    public static void setHasOpenedDataMenu(boolean opened) {
        PreferenceUtils.INSTANCE.setBoolean(DATA_MENU_OPENED, opened);
    }

    public static boolean hasDoneDataMenuFtue() {
        return PreferenceUtils.INSTANCE.getBoolean(DATA_MENU_FTUE_DONE, false);
    }

    public static void setHasDoneDataMenuFtue(boolean isDataMenuFtueDone) {
        PreferenceUtils.INSTANCE.setBoolean(DATA_MENU_FTUE_DONE, isDataMenuFtueDone);
    }

    public static void setDataMenuMsgViewed(boolean isDataMenuMsgViewed) {
        FtueUtil.isDataMenuMsgViewed = isDataMenuMsgViewed;
    }

    public static boolean hasViewedDataMenuMsg() {
        return FtueUtil.isDataMenuMsgViewed;
    }

    public static void setDataMenuMsgShowUpInitTime() {
        dataMenuMsgShowUpInitTime = new DateTime().getMillis();
    }

    public static boolean isDataMenuMsgShownLongEnough() {
        long currentTimeMillis = new DateTime().getMillis();
        return (currentTimeMillis - dataMenuMsgShowUpInitTime) > DATA_MENU_MSG_MINIMUM_DURATION;
    }

    public static boolean isAppFirstLaunch() {
        return PreferenceUtils.INSTANCE.getBoolean(IS_APP_FIRST_LAUNCH, true);
    }

    public static void setIsAppFirstLaunch(boolean isAppFirstLaunch) {
        PreferenceUtils.INSTANCE.setBoolean(IS_APP_FIRST_LAUNCH, isAppFirstLaunch);
    }

    public static void showFabWithAnimation(long startDelay, boolean enableCustomOnTouch, View... views) {
        clearFabAnimatorSet();
        clearFabAnimatorAnimatorSet();

        FabMenu fab = null;
        ImageView fabLogo = null;
        ImageView fabAnimator = null;
        for (View itemView : views){
            switch (itemView.getId()){
                case R.id.fab:
                    fab = (FabMenu)itemView;
                    break;
                case R.id.fab_logo_container:
                    fabLogo = (ImageView)itemView;
                    break;
                case R.id.fab_logo_animator:
                    fabAnimator = (ImageView)itemView;
                    break;
                default:
                    break;
            }
        }
        if (fab != null && fabLogo != null && fabAnimator != null){
            fab.setAlpha(0.0f);
            fab.setScaleX(0.8f);
            fab.setScaleY(0.8f);
            fab.setVisibility(View.VISIBLE);
            fab.setUpOnTouchListener(enableCustomOnTouch);

            ObjectAnimator fabScaleXObjectAnimator = ObjectAnimator.ofFloat(fab, "scaleX", 1.0f);
            fabScaleXObjectAnimator.setInterpolator(new OvershootInterpolator(3.8147f));
            fabScaleXObjectAnimator.setDuration(300L);
            fabScaleXObjectAnimator.setStartDelay(startDelay);

            ObjectAnimator fabScaleYObjectAnimator = ObjectAnimator.ofFloat(fab, "scaleY", 1.0f);
            fabScaleYObjectAnimator.setInterpolator(new OvershootInterpolator(3.8147f));
            fabScaleYObjectAnimator.setDuration(300L);
            fabScaleYObjectAnimator.setStartDelay(startDelay);

            ObjectAnimator fabAlphaObjectAnimator = ObjectAnimator.ofFloat(fab, "alpha", 1.0f);
            fabAlphaObjectAnimator.setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f));
            fabAlphaObjectAnimator.setDuration(500L);
            fabAlphaObjectAnimator.setStartDelay(startDelay);

            fabAnimatorSet = new AnimatorSet();
            fabAnimatorSet.playTogether(fabScaleXObjectAnimator, fabScaleYObjectAnimator, fabAlphaObjectAnimator);
            fabAnimatorSet.start();

            fabLogo.setBackgroundResource(R.drawable.circle_no_border_sky_blue_filled);
            fabLogo.setScaleX(0.5f);
            fabLogo.setScaleY(0.5f);

            fabAnimator.setImageResource(R.drawable.circle_no_border_sky_blue_filled);
            fabAnimator.setAlpha(1.0f);
            fabAnimator.setScaleX(0.5f);
            fabAnimator.setScaleY(0.5f);
            fabAnimator.setVisibility(View.VISIBLE);

            ObjectAnimator fabAnimatorScaleObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(fabAnimator,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f));
            fabAnimatorScaleObjectAnimator.setInterpolator(new LinearInterpolator());
            fabAnimatorScaleObjectAnimator.setDuration(800L);
            fabAnimatorScaleObjectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            fabAnimatorScaleObjectAnimator.setRepeatMode(ObjectAnimator.RESTART);

            ObjectAnimator fabAnimatorAlphaObjectAnimator = ObjectAnimator.ofFloat(fabAnimator, "alpha", 0.1f);
            fabAnimatorAlphaObjectAnimator.setInterpolator(new PathInterpolator(0.73f, 0.0f, 0.91f, 0.44f));
            fabAnimatorAlphaObjectAnimator.setDuration(800L);
            fabAnimatorAlphaObjectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            fabAnimatorAlphaObjectAnimator.setRepeatMode(ObjectAnimator.RESTART);

            fabAnimatorAnimatorSet = new AnimatorSet();
            fabAnimatorAnimatorSet.playTogether(fabAnimatorScaleObjectAnimator, fabAnimatorAlphaObjectAnimator);
            fabAnimatorAnimatorSet.setStartDelay(startDelay);
            fabAnimatorAnimatorSet.start();
        }
    }

    public static void setSlideIndicatorVisibleWithAnimation(boolean show, View... views) {
        clearSlideIndicatorAnimatorSet();

        RelativeLayout slideIndicatorBackgroundRelativeLayoutOriginal = null;
        TextView slideIndicatorTextViewOriginal                       = null;
        ImageView slideIndicatorImageViewOriginal                     = null;
        for (View itemView : views){
            switch (itemView.getId()){
                case R.id.slide_indicator_background_relative_layout:
                    slideIndicatorBackgroundRelativeLayoutOriginal = (RelativeLayout)itemView;
                    break;
                case R.id.slide_indicator_text_view:
                    slideIndicatorTextViewOriginal = (TextView)itemView;
                    break;
                case R.id.slide_indicator_image_view:
                    slideIndicatorImageViewOriginal = (ImageView)itemView;
                    break;
                default:
                    break;
            }
        }
        final RelativeLayout slideIndicatorBackgroundRelativeLayout = slideIndicatorBackgroundRelativeLayoutOriginal;
        final TextView slideIndicatorTextView                       = slideIndicatorTextViewOriginal;
        final ImageView slideIndicatorImageView                     = slideIndicatorImageViewOriginal;
        if (slideIndicatorBackgroundRelativeLayout != null && slideIndicatorTextView != null && slideIndicatorImageView != null){
            PathInterpolator commonPathInterpolator = new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f);
            if (show){
                slideIndicatorBackgroundRelativeLayout.setAlpha(0.0f);
                slideIndicatorTextView.setAlpha(0.0f);
                slideIndicatorImageView.setAlpha(0.0f);
                slideIndicatorBackgroundRelativeLayout.setVisibility(View.VISIBLE);
                slideIndicatorTextView.setVisibility(View.VISIBLE);
                slideIndicatorImageView.setVisibility(View.VISIBLE);

                ObjectAnimator slideIndicatorBackgroundObjectAnimator = ObjectAnimator.ofFloat(slideIndicatorBackgroundRelativeLayout, "alpha", 1.0f);
                ObjectAnimator slideIndicatorTextViewObjectAnimator   = ObjectAnimator.ofFloat(slideIndicatorTextView, "alpha", 1.0f);
                ObjectAnimator slideIndicatorImageViewObjectAnimator  = ObjectAnimator.ofFloat(slideIndicatorImageView, "alpha", 1.0f);
                slideIndicatorAnimatorSet                             = new AnimatorSet();
                slideIndicatorAnimatorSet.playTogether(slideIndicatorBackgroundObjectAnimator, slideIndicatorTextViewObjectAnimator, slideIndicatorImageViewObjectAnimator);
                slideIndicatorAnimatorSet.setDuration(200L);
                slideIndicatorAnimatorSet.setInterpolator(commonPathInterpolator);
                slideIndicatorAnimatorSet.start();
            } else {
                ObjectAnimator slideIndicatorBackgroundObjectAnimator = ObjectAnimator.ofFloat(slideIndicatorBackgroundRelativeLayout, "alpha", 0.0f);
                ObjectAnimator slideIndicatorTextViewObjectAnimator   = ObjectAnimator.ofFloat(slideIndicatorTextView, "alpha", 0.0f);
                ObjectAnimator slideIndicatorImageViewObjectAnimator  = ObjectAnimator.ofFloat(slideIndicatorImageView, "alpha", 0.0f);
                slideIndicatorAnimatorSet                             = new AnimatorSet();
                slideIndicatorAnimatorSet.playTogether(slideIndicatorBackgroundObjectAnimator, slideIndicatorTextViewObjectAnimator, slideIndicatorImageViewObjectAnimator);
                slideIndicatorAnimatorSet.setDuration(200L);
                slideIndicatorAnimatorSet.setInterpolator(commonPathInterpolator);
                slideIndicatorAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        slideIndicatorBackgroundRelativeLayout.setVisibility(View.INVISIBLE);
                        slideIndicatorTextView.setVisibility(View.INVISIBLE);
                        slideIndicatorImageView.setVisibility(View.INVISIBLE);
                        clearSlideIndicatorAnimatorSet();
                    }
                });
                slideIndicatorAnimatorSet.start();
            }
        }
    }

    public static void setFabFlung(MainActivity mainActivity){
        if (!PreferenceUtils.INSTANCE.getBoolean(FAB_FLUNG, false)) {
            PreferenceUtils.INSTANCE.setBoolean(FAB_FLUNG, true);
        }
        if (NotificationsManagerKt.INSTANCE.isFtueFabFlickBannerShowing()){
            mainActivity.getNotificationBanner().hideBanner();
        }
    }

    public static boolean fabHasNeverBeenFlicked(){
        return !PreferenceUtils.INSTANCE.getBoolean(FAB_FLUNG, false);
    }

    public static void clearFabAnimations(View... views) {
        ImageView fabLogo = null;
        ImageView fabAnimator = null;
        for (View itemView : views){
            switch (itemView.getId()){
                case R.id.fab_logo_container:
                    fabLogo = (ImageView)itemView;
                    break;
                case R.id.fab_logo_animator:
                    fabAnimator = (ImageView)itemView;
                    break;
                default:
                    break;
            }
        }
        if (fabLogo != null && fabAnimator != null){
            fabLogo.setBackgroundResource(R.color.dark_grey);
            fabLogo.setScaleX(1.0f);
            fabLogo.setScaleY(1.0f);
            fabAnimator.clearAnimation();
            fabAnimator.setImageResource(0);
            fabAnimator.setScaleX(1.0f);
            fabAnimator.setScaleY(1.0f);
        }
        clearFabAnimatorSet();
        clearFabAnimatorAnimatorSet();
    }

    public static void clearFabAnimatorSet() {
        if (fabAnimatorSet != null){
            fabAnimatorSet.removeAllListeners();
            fabAnimatorSet.end();
            fabAnimatorSet.cancel();
            fabAnimatorSet = null;
        }
    }

    public static void clearFabAnimatorAnimatorSet() {
        if (fabAnimatorAnimatorSet != null){
            fabAnimatorAnimatorSet.removeAllListeners();
            fabAnimatorAnimatorSet.end();
            fabAnimatorAnimatorSet.cancel();
            fabAnimatorAnimatorSet = null;
        }
    }

    public static void clearSlideIndicatorAnimatorSet() {
        if (slideIndicatorAnimatorSet != null){
            slideIndicatorAnimatorSet.removeAllListeners();
            slideIndicatorAnimatorSet.end();
            slideIndicatorAnimatorSet.cancel();
            slideIndicatorAnimatorSet = null;
        }
    }
}
