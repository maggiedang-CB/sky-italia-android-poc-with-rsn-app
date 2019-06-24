package com.nbcsports.regional.nbc_rsn.settings.media_settings;

public interface MediaSettingsContract {

    interface View {

        void setSettingsPresenter(Presenter presenter);

        void setArrivingFromSettingsFragment(boolean isArrivingFromSettingsFragment);

        void showBackImageView();

        void showExitImageView();

        void setUpCellularDataForLiveStreamDefaultValue(boolean defaultValue);

        void setUpAutoPlayLiveGamesDefaultValue(boolean defaultValue);

        void setUpCellularDataForMediaStreamDefaultValue(boolean defaultValue);

        void setUpAutoPlayVideosDefaultValue(boolean defaultValue);

    }

    interface Presenter {

        void setUpBackAndExitImageView(boolean isArrivingFromSettingsFragment);

        void setUpTextViewWithLocalizedText(android.view.View... views);

        void setUpCellularDataForLiveStreamDefaultValue();

        void setUpAutoPlayLiveGamesDefaultValue();

        void setUpCellularDataForMediaStreamDefaultValue();

        void setUpAutoPlayVideosDefaultValue();

        void enableCellularDataForLiveStream();

        void disableCellularDataForLiveStream();

        void enableAutoPlayLiveGames();

        void disableAutoPlayLiveGames();

        void enableCellularDataForMediaStream();

        void disableCellularDataForMediaStream();

        void enableAutoPlayVideos();

        void disableAutoPlayVideos();

    }

}
