package com.nbcsports.regional.nbc_rsn.settings;

import android.location.Location;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsActionableView;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsHeadingView;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsToggleTeamsView;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;
import com.nbcsports.regional.nbc_rsn.utils.LocationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsPresenter implements SettingsContract.Presenter {

    private SettingsContract.View view;
    private Location userLocation;

    SettingsPresenter(SettingsContract.View settingsFragment) {
        view = settingsFragment;
        view.setPresenter(this);
        view.initRecyclerView();
    }

    @Override
    public Team getTeamWithClosestLocation(List<Team> masterTeamList) {
        userLocation = LocationUtils.getUserLocation();

        if (masterTeamList == null || userLocation == null) return null;

        ArrayList<Team> sortedTeams = new ArrayList<>(masterTeamList);

        Collections.sort(sortedTeams, this::compareTeams);

        if (sortedTeams.isEmpty()) {
            return null;
        }
        return sortedTeams.get(0);
    }

    private int compareTeams(Team team1, Team team2) {
        Location teamLocation1 = new Location("");
        teamLocation1.setLatitude(team1.getGeolocation().getLatitude());
        teamLocation1.setLongitude(team1.getGeolocation().getLongitude());

        Location teamLocation2 = new Location("");
        teamLocation2.setLatitude(team2.getGeolocation().getLatitude());
        teamLocation2.setLongitude(team2.getGeolocation().getLongitude());

        return Double.compare(userLocation.distanceTo(teamLocation1), userLocation.distanceTo(teamLocation2));
    }

    @Override
    public void setNotifications(boolean enabled) {
        NotificationsManagerKt.INSTANCE.setAllowNotificationsOptIn(enabled);
    }

    @Override
    public void setBreakingNewsNotifications(boolean enabled) {
        NotificationsManagerKt.INSTANCE.setBreakingNewsOptIn(enabled);
    }

    @Override
    public void setUpTextViewWithLocalizedText(android.view.View... views) {
        for (android.view.View itemView : views) {
            switch (itemView.getId()) {
                case R.id.my_teams_settings_heading_view:
                    ((SettingsHeadingView) itemView).getTextView().setText(LocalizationManager.Settings.MyTeams);
                    break;
                case R.id.toggle_teams:
                    ((SettingsToggleTeamsView) itemView).getViewMoreText().setText(LocalizationManager.Settings.ViewMore);
                    break;
                case R.id.edit_reorder_teams:
                    ((SettingsActionableView) itemView).getTextView().setText(LocalizationManager.Settings.EditReorder);
                    break;
                case R.id.notification_settings_heading_view:
                    ((SettingsHeadingView) itemView).getTextView().setText(LocalizationManager.Settings.Notifications);
                    break;
                case R.id.allow_notifications:
                    ((SettingsActionableView) itemView).getTextView().setText(LocalizationManager.Settings.AllowNotifications);
                    break;
                case R.id.breaking_news:
                    ((SettingsActionableView) itemView).getTextView().setText(LocalizationManager.Settings.BreakingNews);
                    break;
                case R.id.team_news:
                    ((SettingsActionableView) itemView).getTextView().setText(LocalizationManager.Settings.TeamNews);
                    break;
                case R.id.data_settings_heading_view:
                    ((SettingsHeadingView) itemView).getTextView().setText(LocalizationManager.Settings.Data);
                    break;
                case R.id.media_settings:
                    ((SettingsActionableView) itemView).getTextView().setText(LocalizationManager.Settings.MediaSettings);
                    break;
                case R.id.provider_settings_heading_view:
                    // No text from localization json yet
                    break;
                case R.id.support:
                    ((SettingsHeadingView) itemView).getTextView().setText(LocalizationManager.Settings.Support);
                    break;
                case R.id.support_faq:
                    ((SettingsActionableView) itemView).getTextView().setText(LocalizationManager.Settings.FAQ);
                    break;
                case R.id.support_feedback:
                    ((SettingsActionableView) itemView).getTextView().setText(LocalizationManager.Settings.Feedback);
                    break;
                case R.id.support_privacy:
                    ((SettingsActionableView) itemView).getTextView().setText(LocalizationManager.Settings.Privacy);
                    break;
                case R.id.support_share:
                    ((SettingsActionableView) itemView).getTextView().setText(LocalizationManager.Settings.Share);
                    break;
                case R.id.support_term_of_use:
                    ((SettingsActionableView) itemView).getTextView().setText(LocalizationManager.Settings.TermsUse);
                    break;
                case R.id.support_update_app:
                    ((SettingsActionableView) itemView).getTextView().setText(LocalizationManager.Settings.UpdateApp);
                    break;
                case R.id.logout:
                    ((SettingsActionableView)itemView).getTextView().setText(LocalizationManager.Settings.LogOut);
            }
        }
    }


}
