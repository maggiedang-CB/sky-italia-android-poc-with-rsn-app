package com.nbcsports.regional.nbc_rsn.faboutro;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Handler;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil;

public class FabOutroPresenter implements FabOutroContract.Presenter {

    private FabOutroContract.View foView;

    private ConstraintLayout rootConstraintLayout;
    private Guideline rootGuidelineV03;
    private Guideline rootGuidelineV97;
    private Guideline rootGuidelineH03;
    private Guideline rootGuidelineH97;
    private ImageView backgroundImageView;
    private ConstraintLayout topSuccessRootConstraintLayout;
    private Guideline topSuccessGuidelineH30;
    private ImageView topSuccessLogoImageView;
    private TextView topSuccessLabelTextView;
    private RelativeLayout rootVirtualBottomMovingGuidelineRelativeLayout;
    private ConstraintLayout middleContentRootConstraintLayout;
    private ConstraintLayout bottomContentRootConstraintLayout;

    private float tsrclInitHeight, tsrclInitWidth, drawingAreaHeight, mcrclInitialY, bcrclInitialY;
    private float tsrclPivotPercent = FtueUtil.PIVOT_PERCENT_TYPE_1;
    private float mcrclYOffset      = FtueUtil.XY_OFFSET_TYPE_1;
    private float bcrclInitAlpha    = FtueUtil.ALPHA_TYPE_1;
    private float bcrclYOffset      = FtueUtil.XY_OFFSET_TYPE_2;
    private float bivInitScaleX     = FtueUtil.SCALE_ONE;
    private float bivInitScaleY     = FtueUtil.SCALE_ONE;
    private float bivScaleXOffset   = FtueUtil.SCALE_OFFSET_TYPE_1;
    private float bivScaleYOffset   = FtueUtil.SCALE_OFFSET_TYPE_1;
    private float bivPivotY         = FtueUtil.PIVOT_ZERO;
    private AnimatorSet animatorSet;

    public FabOutroPresenter(FabOutroContract.View foView) {
        this.foView = foView;
        this.foView.setFabOutroPresenter(this);
        this.foView.requestAllViews();
    }

    @Override
    public void setUpAppropriateViews(android.view.View... views) {
        for (android.view.View itemView : views){
            switch (itemView.getId()) {
                case R.id.fab_outro_root_constraint_layout:
                    rootConstraintLayout = (ConstraintLayout)itemView;
                    break;
                case R.id.root_guideline_v03:
                    rootGuidelineV03 = (Guideline)itemView;
                    break;
                case R.id.root_guideline_v97:
                    rootGuidelineV97 = (Guideline)itemView;
                    break;
                case R.id.root_guideline_h03:
                    rootGuidelineH03 = (Guideline)itemView;
                    break;
                case R.id.root_guideline_h97:
                    rootGuidelineH97 = (Guideline)itemView;
                    break;
                case R.id.fab_outro_background_image_view:
                    backgroundImageView = (ImageView)itemView;
                    break;
                case R.id.fab_outro_top_success_root_constraint_layout:
                    topSuccessRootConstraintLayout = (ConstraintLayout)itemView;
                    break;
                case R.id.fab_outro_top_success_guideline_h30:
                    topSuccessGuidelineH30 = (Guideline)itemView;
                    break;
                case R.id.fab_outro_top_success_logo_image_view:
                    topSuccessLogoImageView = (ImageView)itemView;
                    break;
                case R.id.fab_outro_top_success_label_text_view:
                    topSuccessLabelTextView = (TextView)itemView;
                    break;
                case R.id.root_virtual_bottom_moving_guideline_relative_layout:
                    rootVirtualBottomMovingGuidelineRelativeLayout = (RelativeLayout)itemView;
                    break;
                case R.id.fab_outro_middle_content_root_constraint_layout:
                    middleContentRootConstraintLayout = (ConstraintLayout)itemView;
                    break;
                case R.id.fab_outro_bottom_content_root_constraint_layout:
                    bottomContentRootConstraintLayout = (ConstraintLayout)itemView;
                    break;
            }
        }
    }

    @Override
    public void setUpTextViewWithLocalizedText(android.view.View... views) {
        for (android.view.View itemView : views){
            switch (itemView.getId()) {
                case R.id.fab_outro_middle_content_left_text_view:
                    ((TextView)itemView).setText(LocalizationManager.TeamSelectorSuccess.RotatedText);
                    break;
                case R.id.fab_outro_middle_content_right_text_view:
                    ((TextView)itemView).setText(LocalizationManager.TeamSelectorSuccess.TitleText);
                    break;
                case R.id.fab_outro_bottom_content_text_view:
                    ((TextView)itemView).setText(LocalizationManager.TeamSelectorSuccess.SubtitleText);
                    break;
            }
        }
    }

    @Override
    public void setUpBackgroundAnimationsAndPlay() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getDefaultBackgroundAnimationsAndPlay();
            }
        }, 500L);
    }

    private void getDefaultBackgroundAnimationsAndPlay() {
        tsrclInitHeight   = topSuccessRootConstraintLayout.getHeight();
        tsrclInitWidth    = topSuccessRootConstraintLayout.getWidth();
        drawingAreaHeight = rootGuidelineH97.getY() - rootGuidelineH03.getY();

        // Move topSuccessRootConstraintLayout to match the topSuccessRootConstraintLayout pivot
        // With drawing area pivot
        float tsrclAnimateStartPointY = (rootGuidelineH97.getY()-tsrclInitHeight)*tsrclPivotPercent;
        topSuccessRootConstraintLayout.setY(tsrclAnimateStartPointY);
        topSuccessRootConstraintLayout.setAlpha(0.0f);
        topSuccessRootConstraintLayout.setVisibility(View.VISIBLE);

        ObjectAnimator tsrclAlphaObjectAnimator = ObjectAnimator.ofFloat(topSuccessRootConstraintLayout, "alpha", 1.0f);
        tsrclAlphaObjectAnimator.setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f));
        tsrclAlphaObjectAnimator.setDuration(500L);

        ObjectAnimator tsrclExpendObjectAnimator = ObjectAnimator.ofFloat(topSuccessRootConstraintLayout, "y", rootGuidelineH03.getY());
        tsrclExpendObjectAnimator.setInterpolator(new PathInterpolator(0.8f, 0.0f, 0.2f, 1.0f));
        tsrclExpendObjectAnimator.setDuration(1000L);
        tsrclExpendObjectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewGroup.LayoutParams params = topSuccessRootConstraintLayout.getLayoutParams();
                float newTsrclY = (float)(valueAnimator.getAnimatedValue("y"));
                float newTsrclHeight = tsrclInitHeight+(tsrclAnimateStartPointY-newTsrclY)/tsrclPivotPercent;
                params.height = (int)newTsrclHeight;
                params.width  = (int)tsrclInitWidth;
                topSuccessRootConstraintLayout.setLayoutParams(params);
            }
        });
        tsrclExpendObjectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ViewGroup.LayoutParams params = topSuccessRootConstraintLayout.getLayoutParams();
                params.height = (int)drawingAreaHeight;
                params.width  = (int)tsrclInitWidth;
                topSuccessRootConstraintLayout.setLayoutParams(params);
            }
        });

        ObjectAnimator tsrclCollapseObjectAnimator = ObjectAnimator.ofFloat(rootVirtualBottomMovingGuidelineRelativeLayout, "y", rootGuidelineH03.getY()+tsrclInitHeight);
        tsrclCollapseObjectAnimator.setInterpolator(new PathInterpolator(0.8f, 0.0f, 0.2f, 1.0f));
        tsrclCollapseObjectAnimator.setDuration(750L);
        tsrclCollapseObjectAnimator.setStartDelay(1750L);
        tsrclCollapseObjectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewGroup.LayoutParams params = topSuccessRootConstraintLayout.getLayoutParams();
                float newRvbmgY = (float)(valueAnimator.getAnimatedValue("y"));
                float newTsrclHeight = newRvbmgY-rootGuidelineH03.getY();
                params.height = (int)newTsrclHeight;
                params.width  = (int)tsrclInitWidth;
                topSuccessRootConstraintLayout.setLayoutParams(params);
            }
        });
        tsrclCollapseObjectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ViewGroup.LayoutParams params = topSuccessRootConstraintLayout.getLayoutParams();
                params.height = (int)tsrclInitHeight;
                params.width  = (int)tsrclInitWidth;
                topSuccessRootConstraintLayout.setLayoutParams(params);
            }
        });

        // Scale backgroundImageView with bivScaleXOffset and bivScaleYOffset
        backgroundImageView.setPivotX((float)(backgroundImageView.getWidth()/2.0f));
        backgroundImageView.setPivotY(bivPivotY);
        backgroundImageView.setScaleX(bivInitScaleX+bivScaleXOffset);
        backgroundImageView.setScaleY(bivInitScaleY+bivScaleYOffset);
        backgroundImageView.setAlpha(0.0f);
        backgroundImageView.setVisibility(View.VISIBLE);

        ObjectAnimator bivScaleDownXObjectAnimator = ObjectAnimator.ofFloat(backgroundImageView, "scaleX", bivInitScaleX);
        bivScaleDownXObjectAnimator.setInterpolator(new PathInterpolator(0.8f, 0.0f, 0.2f, 1.0f));
        bivScaleDownXObjectAnimator.setDuration(750L);
        bivScaleDownXObjectAnimator.setStartDelay(1750L);

        ObjectAnimator bivScaleDownYObjectAnimator = ObjectAnimator.ofFloat(backgroundImageView, "scaleY", bivInitScaleY);
        bivScaleDownYObjectAnimator.setInterpolator(new PathInterpolator(0.8f, 0.0f, 0.2f, 1.0f));
        bivScaleDownYObjectAnimator.setDuration(750L);
        bivScaleDownYObjectAnimator.setStartDelay(1750L);

        ObjectAnimator bivAlphaObjectAnimator = ObjectAnimator.ofFloat(backgroundImageView, "alpha", 1.0f);
        bivAlphaObjectAnimator.setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f));
        bivAlphaObjectAnimator.setDuration(750L);
        bivAlphaObjectAnimator.setStartDelay(1750L);

        // Move middleContentRootConstraintLayout to new starting point with mcrclYOffset
        mcrclInitialY = middleContentRootConstraintLayout.getY();
        middleContentRootConstraintLayout.setY(mcrclInitialY+mcrclYOffset);
        middleContentRootConstraintLayout.setAlpha(0.0f);
        middleContentRootConstraintLayout.setVisibility(View.VISIBLE);

        ObjectAnimator mcrclMoveUpObjectAnimator = ObjectAnimator.ofFloat(middleContentRootConstraintLayout, "y", mcrclInitialY);
        mcrclMoveUpObjectAnimator.setInterpolator(new PathInterpolator(0.0f, 0.25f, 0.25f, 1.0f));
        mcrclMoveUpObjectAnimator.setDuration(900L);
        mcrclMoveUpObjectAnimator.setStartDelay(2300L);

        ObjectAnimator mcrclAlphaObjectAnimator = ObjectAnimator.ofFloat(middleContentRootConstraintLayout, "alpha", 1.0f);
        mcrclAlphaObjectAnimator.setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f));
        mcrclAlphaObjectAnimator.setDuration(900L);
        mcrclAlphaObjectAnimator.setStartDelay(2300L);

        // Move bottomContentRootConstraintLayout to new starting point with bcrclYOffset
        bcrclInitialY = bottomContentRootConstraintLayout.getY();
        bottomContentRootConstraintLayout.setY(bcrclInitialY+bcrclYOffset);
        bottomContentRootConstraintLayout.setAlpha(0.0f);
        bottomContentRootConstraintLayout.setVisibility(View.VISIBLE);

        ObjectAnimator bcrclMoveUpObjectAnimator = ObjectAnimator.ofFloat(bottomContentRootConstraintLayout, "y", bcrclInitialY);
        bcrclMoveUpObjectAnimator.setInterpolator(new PathInterpolator(0.0f, 0.25f, 0.25f, 1.0f));
        bcrclMoveUpObjectAnimator.setDuration(900L);
        bcrclMoveUpObjectAnimator.setStartDelay(2400L);

        ObjectAnimator bcrclAlphaObjectAnimator = ObjectAnimator.ofFloat(bottomContentRootConstraintLayout, "alpha", bcrclInitAlpha);
        bcrclAlphaObjectAnimator.setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f));
        bcrclAlphaObjectAnimator.setDuration(900L);
        bcrclAlphaObjectAnimator.setStartDelay(2400L);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(tsrclAlphaObjectAnimator, tsrclExpendObjectAnimator,
                tsrclCollapseObjectAnimator, bivScaleDownXObjectAnimator,
                bivScaleDownYObjectAnimator, bivAlphaObjectAnimator,
                mcrclMoveUpObjectAnimator, mcrclAlphaObjectAnimator,
                bcrclMoveUpObjectAnimator, bcrclAlphaObjectAnimator);
        animatorSet.start();
    }

    @Override
    public void clearAllViewsAnimations() {
        if (rootGuidelineV03 != null){
            rootGuidelineV03.clearAnimation();
        }
        if (rootGuidelineV97 != null){
            rootGuidelineV97.clearAnimation();
        }
        if (rootGuidelineH03 != null){
            rootGuidelineH03.clearAnimation();
        }
        if (rootGuidelineH97 != null){
            rootGuidelineH97.clearAnimation();
        }
        if (backgroundImageView != null){
            backgroundImageView.clearAnimation();
        }
        if (topSuccessRootConstraintLayout != null){
            topSuccessRootConstraintLayout.clearAnimation();
        }
        if (topSuccessGuidelineH30 != null){
            topSuccessGuidelineH30.clearAnimation();
        }
        if (topSuccessLogoImageView != null){
            topSuccessLogoImageView.clearAnimation();
        }
        if (topSuccessLabelTextView != null){
            topSuccessLabelTextView.clearAnimation();
        }
        if (rootVirtualBottomMovingGuidelineRelativeLayout != null){
            rootVirtualBottomMovingGuidelineRelativeLayout.clearAnimation();
        }
        if (middleContentRootConstraintLayout != null){
            middleContentRootConstraintLayout.clearAnimation();
        }
        if (bottomContentRootConstraintLayout != null){
            bottomContentRootConstraintLayout.clearAnimation();
        }
        if (animatorSet != null){
            animatorSet.removeAllListeners();
            animatorSet.end();
            animatorSet.cancel();
            animatorSet = null;
        }
    }

}
