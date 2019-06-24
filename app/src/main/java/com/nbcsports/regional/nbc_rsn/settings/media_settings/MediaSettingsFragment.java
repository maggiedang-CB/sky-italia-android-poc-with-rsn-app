package com.nbcsports.regional.nbc_rsn.settings.media_settings;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo;
import com.nbcsports.regional.nbc_rsn.common.BaseFragment;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsActionableView;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsHeadingView;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsToggleButton;

import butterknife.BindView;
import butterknife.OnClick;

public class MediaSettingsFragment extends BaseFragment implements MediaSettingsContract.View {

    private MediaSettingsContract.Presenter msPresenter;

    @BindView(R.id.root_nested_scroll_view)
    NestedScrollView rootNestedScrollView;

    @BindView(R.id.root_linear_layout)
    LinearLayout rootLinearLayout;

    @BindView(R.id.back_image_view)
    ImageView backImageView;

    @BindView(R.id.exit_image_view)
    ImageView exitImageView;

    @BindView(R.id.media_settings_header_text_view)
    TextView mediaSettingsHeaderTextView;

    @BindView(R.id.cellular_data_for_live_stream_heading_view)
    SettingsHeadingView cdlsHeadingView;

    @BindView(R.id.cellular_data_for_live_stream_actionable_view)
    SettingsActionableView cdlsActionableView;

    @BindView(R.id.allow_cellular_data_for_live_stream_message_text_view)
    TextView acdlsMessageTextView;

    @BindView(R.id.auto_play_live_games_actionable_view)
    SettingsActionableView aplgActionableView;

    @BindView(R.id.cellular_data_for_media_steam_heading_view)
    SettingsHeadingView cdmsHeadingView;

    @BindView(R.id.cellular_data_for_media_steam_actionable_view)
    SettingsActionableView cdmsActionableView;

    @BindView(R.id.allow_cellular_data_for_media_steam_message_text_view)
    TextView acdmsMessageTextView;

    @BindView(R.id.auto_play_videos_actionable_view)
    SettingsActionableView apvActionableView;

    @Override
    public int getLayout() {
        return R.layout.fragment_media_settings;
    }

    public MediaSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new MediaSettingsPresenter(this);
        initTogglesWithDefaultValue();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            // User arrives at the top of the fragment.
            rootNestedScrollView.scrollTo(0, 0);
        }
    }

    @Override
    public void setSettingsPresenter(MediaSettingsContract.Presenter presenter) {
        msPresenter = presenter;
    }

    @Override
    public void setArrivingFromSettingsFragment(boolean isArrivingFromSettingsFragment) {
        msPresenter.setUpBackAndExitImageView(isArrivingFromSettingsFragment);
    }

    @Override
    public void showBackImageView() {
        if (backImageView != null && exitImageView != null){
            backImageView.setVisibility(View.VISIBLE);
            exitImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showExitImageView() {
        if (backImageView != null && exitImageView != null){
            backImageView.setVisibility(View.GONE);
            exitImageView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.back_image_view)
    public void goBackToSettings() {
        NavigationManager.getInstance().closeAndRemoveFragmentFromBackStack();
    }

    @OnClick(R.id.exit_image_view)
    public void exitMediaSettingsFragment() {
        NavigationManager.getInstance().closeAndRemoveFragmentFromBackStack();
    }

    @Override
    public void setUpCellularDataForLiveStreamDefaultValue(boolean defaultValue) {
        cdlsActionableView.getToggleButton().setToggleDefault(defaultValue);
    }

    @Override
    public  void setUpAutoPlayLiveGamesDefaultValue(boolean defaultValue) {
        aplgActionableView.getToggleButton().setToggleDefault(defaultValue);
    }

    @Override
    public void setUpCellularDataForMediaStreamDefaultValue(boolean defaultValue) {
        cdmsActionableView.getToggleButton().setToggleDefault(defaultValue);
    }

    @Override
    public void setUpAutoPlayVideosDefaultValue(boolean defaultValue) {
        apvActionableView.getToggleButton().setToggleDefault(defaultValue);
    }

    @Override
    public void onLocalizationManagerInitialized() {
        msPresenter.setUpTextViewWithLocalizedText(mediaSettingsHeaderTextView, cdlsHeadingView,
                cdlsActionableView, acdlsMessageTextView, aplgActionableView, cdmsHeadingView,
                cdmsActionableView, acdmsMessageTextView, apvActionableView);
    }

    private void initTogglesWithDefaultValue() {
        msPresenter.setUpCellularDataForLiveStreamDefaultValue();
        msPresenter.setUpAutoPlayLiveGamesDefaultValue();
        msPresenter.setUpCellularDataForMediaStreamDefaultValue();
        msPresenter.setUpAutoPlayVideosDefaultValue();
    }

    @OnClick(R.id.cellular_data_for_live_stream_actionable_view)
    public void toggleCellularDataLiveStream(){
        SettingsToggleButton toggleButton = cdlsActionableView.getToggleButton();

        if (toggleButton.isToggledOn()) {
            toggleButton.animateToggleOff();
            msPresenter.disableCellularDataForLiveStream();

        } else {
            toggleButton.animateToggleOn();
            msPresenter.enableCellularDataForLiveStream();
        }
    }

    @OnClick(R.id.auto_play_live_games_actionable_view)
    public void toggleAutoPlayLiveGames(){
        SettingsToggleButton toggleButton = aplgActionableView.getToggleButton();

        if (toggleButton.isToggledOn()) {
            toggleButton.animateToggleOff();
            msPresenter.disableAutoPlayLiveGames();

        } else {
            toggleButton.animateToggleOn();
            msPresenter.enableAutoPlayLiveGames();
        }
    }

    @OnClick(R.id.cellular_data_for_media_steam_actionable_view)
    public void toggleCellularDataForMediaStream(){
        SettingsToggleButton toggleButton = cdmsActionableView.getToggleButton();

        if (toggleButton.isToggledOn()) {
            toggleButton.animateToggleOff();
            msPresenter.disableCellularDataForMediaStream();

        } else {
            toggleButton.animateToggleOn();
            msPresenter.enableCellularDataForMediaStream();
        }
    }

    @OnClick(R.id.auto_play_videos_actionable_view)
    public void toggleAutoPlayVideos(){
        SettingsToggleButton toggleButton = apvActionableView.getToggleButton();

        if (toggleButton.isToggledOn()) {
            toggleButton.animateToggleOff();
            msPresenter.disableAutoPlayVideos();

        } else {
            toggleButton.animateToggleOn();
            msPresenter.enableAutoPlayVideos();
        }
    }

    @Override
    public PageInfo getPageInfo() {
        return new PageInfo(false, "", "", "Settings", "", "", "", "", "", "", "");
    }
}
