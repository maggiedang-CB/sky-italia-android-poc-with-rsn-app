package com.nbcsports.regional.nbc_rsn.faboutro;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;

import butterknife.BindView;

public class FabOutroFragment extends BaseFragment implements FabOutroContract.View {

    private FabOutroContract.Presenter foPresenter;

    private boolean skipBackgroundAnimation = false;

    @BindView(R.id.fab_outro_root_constraint_layout)
    ConstraintLayout rootConstraintLayout;

    @BindView(R.id.root_guideline_v03)
    Guideline rootGuidelineV03;

    @BindView(R.id.root_guideline_v97)
    Guideline rootGuidelineV97;

    @BindView(R.id.root_guideline_h03)
    Guideline rootGuidelineH03;

    @BindView(R.id.root_guideline_h97)
    Guideline rootGuidelineH97;

    @BindView(R.id.fab_outro_background_image_view)
    ImageView backgroundImageView;

    @BindView(R.id.fab_outro_top_success_root_constraint_layout)
    ConstraintLayout topSuccessRootConstraintLayout;

    @BindView(R.id.fab_outro_top_success_guideline_h30)
    Guideline topSuccessGuidelineH30;

    @BindView(R.id.fab_outro_top_success_logo_image_view)
    ImageView topSuccessLogoImageView;

    @BindView(R.id.fab_outro_top_success_label_text_view)
    TextView topSuccessLabelTextView;

    @BindView(R.id.root_virtual_bottom_moving_guideline_relative_layout)
    RelativeLayout rootVirtualBottomMovingGuidelineRelativeLayout;

    @BindView(R.id.fab_outro_middle_content_root_constraint_layout)
    ConstraintLayout middleContentRootConstraintLayout;

    @BindView(R.id.fab_outro_middle_content_left_text_view)
    TextView middleContentLeftTextView;

    @BindView(R.id.fab_outro_middle_content_right_text_view)
    TextView middleContentRightTextView;

    @BindView(R.id.fab_outro_bottom_content_root_constraint_layout)
    ConstraintLayout bottomContentRootConstraintLayout;

    @BindView(R.id.fab_outro_bottom_content_text_view)
    TextView bottomContentTextView;

    @Override
    public int getLayout() {
        return R.layout.fragment_fab_outro;
    }

    public FabOutroFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.trackingPageName = "FabOutroFragment";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new FabOutroPresenter(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        foPresenter.clearAllViewsAnimations();
        if (!hidden){
            if (skipBackgroundAnimation){
                ((MainActivity) getActivity()).showFabWithAnimation(100L, false);
            } else {
                foPresenter.setUpBackgroundAnimationsAndPlay();
                ((MainActivity) getActivity()).showFabWithAnimation(3800L, false);
            }
        }
    }

    @Override
    public void setFabOutroPresenter(FabOutroContract.Presenter presenter) {
        foPresenter = presenter;
    }

    @Override
    public void requestAllViews() {
        foPresenter.setUpAppropriateViews(rootConstraintLayout, rootGuidelineV03,
                rootGuidelineV97, rootGuidelineH03, rootGuidelineH97, backgroundImageView,
                topSuccessRootConstraintLayout, topSuccessGuidelineH30, topSuccessLogoImageView,
                topSuccessLabelTextView, rootVirtualBottomMovingGuidelineRelativeLayout,
                middleContentRootConstraintLayout, bottomContentRootConstraintLayout);
    }

    @Override
    public void onFabOutroShownAlready() {
        skipBackgroundAnimation = true;
    }

    @Override
    public void onLocalizationManagerInitialized() {
        foPresenter.setUpTextViewWithLocalizedText(middleContentLeftTextView,
                middleContentRightTextView, bottomContentTextView);
    }

    @Override
    public PageInfo getPageInfo() {
        return new PageInfo(false, "", "", "", "", "", "", "", "", "", "");
    }
}
