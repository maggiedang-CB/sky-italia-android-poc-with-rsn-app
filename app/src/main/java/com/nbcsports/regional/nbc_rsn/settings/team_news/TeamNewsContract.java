package com.nbcsports.regional.nbc_rsn.settings.team_news;

import com.nbcsports.regional.nbc_rsn.common.Team;

public interface TeamNewsContract {

    interface View {

        void updateViewsBasedOnPreviousFragment();

        void exitTeamNewsFragment();
    }

    interface Presenter {

        void setAllTeamNotifications(Team team, boolean enabled);

        void setTeamNewsNotifications(Team team, boolean enabled);

        void setGameStartNotifications(Team team, boolean enabled);

        void setFinalScoreNotifications(Team team, boolean enabled);

        void setUpTextViewWithLocalizedText(android.view.View... views);

    }
}
