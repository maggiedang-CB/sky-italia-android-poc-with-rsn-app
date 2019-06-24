package com.nbcsports.regional.nbc_rsn.settings.media_settings;

import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsActionableView;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsHeadingView;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;

public class MediaSettingsPresenter implements MediaSettingsContract.Presenter {

    private MediaSettingsContract.View msView;

    private boolean isArrivingFromSettingsFragment;

    public static final String CELLULAR_DATA_FOR_LIVE_STREAM  = "cellular_data_for_live_stream";
    public static final String AUTO_PLAY_LIVE_GAMES           = "auto_play_live_games";
    public static final String CELLULAR_DATA_FOR_MEDIA_STREAM = "cellular_data_for_media_stream";
    public static final String AUTO_PLAY_VIDEOS               = "auto_play_videos";

    public MediaSettingsPresenter(MediaSettingsContract.View msView) {
        this.msView = msView;
        this.msView.setSettingsPresenter(this);
    }

    @Override
    public void setUpBackAndExitImageView(boolean isArrivingFromSettingsFragment) {
        this.isArrivingFromSettingsFragment = isArrivingFromSettingsFragment;
        if (isArrivingFromSettingsFragment) {
            msView.showBackImageView();
        }
        else {
            msView.showExitImageView();
        }
    }

    @Override
    public void setUpTextViewWithLocalizedText(android.view.View... views) {
        for (android.view.View itemView : views){
            switch (itemView.getId()){
                case R.id.media_settings_header_text_view:
                    ((TextView)itemView).setText(LocalizationManager.MediaSettings.MediaSettings);
                    break;
                case R.id.cellular_data_for_live_stream_heading_view:
                    ((SettingsHeadingView)itemView).getTextView().setText(LocalizationManager.MediaSettings.LiveVideoPreferences);
                    break;
                case R.id.cellular_data_for_live_stream_actionable_view:
                    ((SettingsActionableView)itemView).getTextView().setText(LocalizationManager.MediaSettings.CellularDataForLiveStream);
                    break;
                case R.id.allow_cellular_data_for_live_stream_message_text_view:
                    ((TextView)itemView).setText(LocalizationManager.MediaSettings.AllowTheUseOfCellularDataForLiveStreaming);
                    break;
                case R.id.auto_play_live_games_actionable_view:
                    ((SettingsActionableView)itemView).getTextView().setText(LocalizationManager.MediaSettings.AutoPlayLiveGames);
                    break;
                case R.id.cellular_data_for_media_steam_heading_view:
                    ((SettingsHeadingView)itemView).getTextView().setText(LocalizationManager.MediaSettings.MediaStreamPreferences);
                    break;
                case R.id.cellular_data_for_media_steam_actionable_view:
                    ((SettingsActionableView)itemView).getTextView().setText(LocalizationManager.MediaSettings.CellularDataForMediaStream);
                    break;
                case R.id.allow_cellular_data_for_media_steam_message_text_view:
                    ((TextView)itemView).setText(LocalizationManager.MediaSettings.AllowTheUseOfCellularDataForNonLiveVideoAndAudioStreaming);
                    break;
                case R.id.auto_play_videos_actionable_view:
                    ((SettingsActionableView)itemView).getTextView().setText(LocalizationManager.MediaSettings.AutoPlayVideos);
                    break;
            }
        }
    }

    @Override
    public void setUpCellularDataForLiveStreamDefaultValue() {
        boolean settingValue = PreferenceUtils.INSTANCE.getBoolean(CELLULAR_DATA_FOR_LIVE_STREAM, true);
        msView.setUpCellularDataForLiveStreamDefaultValue(settingValue);
    }

    @Override
    public void setUpAutoPlayLiveGamesDefaultValue() {
        boolean settingValue = PreferenceUtils.INSTANCE.getBoolean(AUTO_PLAY_LIVE_GAMES, true);
        msView.setUpAutoPlayLiveGamesDefaultValue(settingValue);
    }

    @Override
    public void setUpCellularDataForMediaStreamDefaultValue() {
        boolean settingValue = PreferenceUtils.INSTANCE.getBoolean(CELLULAR_DATA_FOR_MEDIA_STREAM, true);
        msView.setUpCellularDataForMediaStreamDefaultValue(settingValue);
    }

    @Override
    public void setUpAutoPlayVideosDefaultValue() {
        boolean settingValue = PreferenceUtils.INSTANCE.getBoolean(AUTO_PLAY_VIDEOS, true);
        msView.setUpAutoPlayVideosDefaultValue(settingValue);
    }

    @Override
    public void enableCellularDataForLiveStream() {
        PreferenceUtils.INSTANCE.setBoolean(CELLULAR_DATA_FOR_LIVE_STREAM, true);
    }

    @Override
    public void disableCellularDataForLiveStream() {
        PreferenceUtils.INSTANCE.setBoolean(CELLULAR_DATA_FOR_LIVE_STREAM, false);
    }

    @Override
    public void enableAutoPlayLiveGames() {
        PreferenceUtils.INSTANCE.setBoolean(AUTO_PLAY_LIVE_GAMES, true);
    }

    @Override
    public void disableAutoPlayLiveGames() {
        PreferenceUtils.INSTANCE.setBoolean(AUTO_PLAY_LIVE_GAMES, false);
    }

    @Override
    public void enableCellularDataForMediaStream() {
        PreferenceUtils.INSTANCE.setBoolean(CELLULAR_DATA_FOR_MEDIA_STREAM, true);
    }

    @Override
    public void disableCellularDataForMediaStream() {
        PreferenceUtils.INSTANCE.setBoolean(CELLULAR_DATA_FOR_MEDIA_STREAM, false);
    }

    @Override
    public void enableAutoPlayVideos() {
        PreferenceUtils.INSTANCE.setBoolean(AUTO_PLAY_VIDEOS, true);
    }

    @Override
    public void disableAutoPlayVideos() {
        PreferenceUtils.INSTANCE.setBoolean(AUTO_PLAY_VIDEOS, false);
    }

}
