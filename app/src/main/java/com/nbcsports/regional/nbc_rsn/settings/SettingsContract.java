package com.nbcsports.regional.nbc_rsn.settings;

import com.nbcsports.regional.nbc_rsn.common.Team;

import java.util.List;

public interface SettingsContract {

    interface View {

        void initRecyclerView();

        void setPresenter(SettingsPresenter presenter);

        void showRegionBackgroundImage();

        void showSettingsLabel();

        void showUpdatedTeamList();

        void updateNotificationViews();

        void setToggleMoreTeamsOptionVisibility(boolean visible);

        void setBreakingNewsOptionVisibility(boolean visible);

        void setTeamNewsOptionVisibility(boolean visible);
    }

    interface Presenter {

        Team getTeamWithClosestLocation(List<Team> masterTeamList);

        void setNotifications(boolean visible);

        void setBreakingNewsNotifications(boolean visible);

        void setUpTextViewWithLocalizedText(android.view.View... views);

    }

}
