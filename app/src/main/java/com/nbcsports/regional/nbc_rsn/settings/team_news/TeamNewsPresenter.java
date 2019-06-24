package com.nbcsports.regional.nbc_rsn.settings.team_news;

import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsActionableView;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;

import java.util.ArrayList;
import java.util.List;

public class TeamNewsPresenter implements TeamNewsContract.Presenter {

    private TeamNewsContract.View view;

    TeamNewsPresenter(TeamNewsContract.View view) {
        this.view = view;
    }

    protected ArrayList<TeamNewsOptStatus> getTeamNewsOptStatuses(List<Team> teams) {
        ArrayList<TeamNewsOptStatus> optStatuses = new ArrayList<>();
        for (Team team : teams) {
            String teamId = team.getTeamId();
            optStatuses.add(new TeamNewsOptStatus(
                    teamId,
                    NotificationsManagerKt.INSTANCE.isOptedInTeamNews(teamId),
                    NotificationsManagerKt.INSTANCE.isOptedInGameStart(teamId),
                    NotificationsManagerKt.INSTANCE.isOptedInFinalScore(teamId)
            ));
        }
        return optStatuses;
    }

    @Override
    public void setAllTeamNotifications(Team team, boolean enabled) {
        NotificationsManagerKt.INSTANCE.setTeamNotificationsOptIn(team.getTeamId(), enabled);
    }

    @Override
    public void setTeamNewsNotifications(Team team, boolean enabled) {
        NotificationsManagerKt.INSTANCE.setTeamNewsOptIn(team.getTeamId(), enabled);
    }

    @Override
    public void setGameStartNotifications(Team team, boolean enabled) {
        NotificationsManagerKt.INSTANCE.setGameStartOptIn(team.getTeamId(), enabled);
    }

    @Override
    public void setFinalScoreNotifications(Team team, boolean enabled) {
        NotificationsManagerKt.INSTANCE.setFinalScoreOptIn(team.getTeamId(), enabled);
    }

    @Override
    public void setUpTextViewWithLocalizedText(android.view.View... views) {
        for (android.view.View itemView : views){
            switch (itemView.getId()) {
                case R.id.team_news_text:
                    ((TextView)itemView).setText(LocalizationManager.TeamNews.TeamNews);
                    break;
                case R.id.edit_notification_settings_text_view:
                    ((TextView)itemView).setText(LocalizationManager.TeamNews.EditNotificationSettings);
                    break;
                case R.id.all_changed_saved_text_view:
                    ((TextView)itemView).setText(LocalizationManager.TeamNews.AllChangedSaved);
                    break;
                case R.id.all_option:
                    ((SettingsActionableView)itemView).getTextView().setText(LocalizationManager.TeamNews.All);
                    break;
                case R.id.team_news_option:
                    ((SettingsActionableView)itemView).getTextView().setText(LocalizationManager.TeamNews.TeamNews);
                    break;
                case R.id.game_start_option:
                    ((SettingsActionableView)itemView).getTextView().setText(LocalizationManager.TeamNews.GameStart);
                    break;
                case R.id.final_score_option:
                    ((SettingsActionableView)itemView).getTextView().setText(LocalizationManager.TeamNews.FinalScore);
                    break;
            }
        }
    }
}
