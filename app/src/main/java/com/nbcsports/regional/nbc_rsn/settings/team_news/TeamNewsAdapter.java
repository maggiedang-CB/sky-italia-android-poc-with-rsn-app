package com.nbcsports.regional.nbc_rsn.settings.team_news;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsActionableView;
import com.nbcsports.regional.nbc_rsn.settings.settings_views.SettingsToggleButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TeamNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private TeamNewsContract.Presenter presenter;

    private static final int NO_TEAMS_WITH_OPTIONS_OPEN = -1;
    private int teamWithOptionsOpenIndex = NO_TEAMS_WITH_OPTIONS_OPEN;

    private static final float BUTTON_POINTING_DOWN = 180f;
    private static final float BUTTON_POINTING_UP = 0f;

    private ArrayList<Team> teams = new ArrayList<>();
    private boolean[] areOptionsShowingAtTeamIndex;
    private ArrayList<TeamNewsOptStatus> teamNewsOptStatuses = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TeamNewsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_team_news, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TeamNewsViewHolder) holder).bindTo(teams.get(position), areOptionsShowingAtTeamIndex[position], teamNewsOptStatuses.get(position));
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    public void setTeamData(List<Team> teamData, List<TeamNewsOptStatus> statuses) {
        teams = (ArrayList<Team>) teamData;
        teamNewsOptStatuses = new ArrayList<>(statuses);
        areOptionsShowingAtTeamIndex = new boolean[teams.size()];
        notifyDataSetChanged();
    }

    public ArrayList<Team> getTeamData() {
        return teams;
    }

    public ArrayList<TeamNewsOptStatus> getTeamNewsOptStatuses() {
        return teamNewsOptStatuses;
    }

    public void setTeamNewsPresenter(TeamNewsContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public class TeamNewsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.team_logo)
        ImageView teamLogo;

        @BindView(R.id.team_name)
        TextView teamName;

        @BindView(R.id.team_city)
        TextView teamCity;

        @BindView(R.id.open_options_button)
        ImageView openOptionsButton;

        @BindView(R.id.options)
        ConstraintLayout optionsContainer;

        @BindView(R.id.all_option)
        SettingsActionableView allOption;

        @BindView(R.id.team_news_option)
        SettingsActionableView teamNewsOption;

        @BindView(R.id.game_start_option)
        SettingsActionableView gameStartOption;

        @BindView(R.id.final_score_option)
        SettingsActionableView finalScoreOption;

        TeamNewsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindTo(Team team, boolean optionsVisibility, TeamNewsOptStatus teamOptedStatus) {
            if (team == null) {
                return;
            }

            if (!team.getLogoUrl().isEmpty()) {
                Picasso.get().load(team.getLogoUrl()).into(teamLogo);
            }

            if (!team.getDisplayName().isEmpty()) {
                teamName.setText(team.getDisplayName());
            }

            if (!team.getCityName().isEmpty()) {
                teamCity.setText(team.getCityName());
            }

            if (LocalizationManager.isInitialized()){
                presenter.setUpTextViewWithLocalizedText(allOption, teamNewsOption, gameStartOption,
                        finalScoreOption);
            }

            if (optionsVisibility) {
                setDefaultTogglePositions(teamOptedStatus);
                optionsContainer.setVisibility(View.VISIBLE);

            } else {
                optionsContainer.setVisibility(View.GONE);

            }

            openOptionsButton.animate()
                    .rotation(optionsVisibility ? BUTTON_POINTING_DOWN : BUTTON_POINTING_UP)
                    .setDuration(0)
                    .start();
        }

        private void setDefaultTogglePositions(TeamNewsOptStatus optStatus) {
            teamNewsOption.getToggleButton().setToggleDefault(optStatus.getTeamNewsOptStatus());
            finalScoreOption.getToggleButton().setToggleDefault(optStatus.getFinalScoreOptStatus());
            gameStartOption.getToggleButton().setToggleDefault(optStatus.getGameStartOptStatus());
            allOption.getToggleButton().setToggleDefault(isAllToggledOn());
        }

        @OnClick(R.id.team_news_option)
        public void toggleTeamNewsOption() {
            int position = getAdapterPosition();
            if (position == -1
                    || presenter == null
                    || teams.get(position) == null
                    || teamNewsOptStatuses.get(position) == null) return;

            SettingsToggleButton teamNewsToggleButton = teamNewsOption.getToggleButton();
            boolean toggledOn = teamNewsToggleButton.isToggledOn();

            presenter.setTeamNewsNotifications(teams.get(position), !toggledOn);
            teamNewsToggleButton.animateToggle(!toggledOn);
            teamNewsOptStatuses.get(position).setTeamNewsOptStatus(!toggledOn);
            allOption.getToggleButton().animateToggle(isAllToggledOn());
        }

        @OnClick(R.id.game_start_option)
        public void toggleGameStartOption() {
            int position = getAdapterPosition();
            if (position == -1
                    || presenter == null
                    || teams.get(position) == null
                    || teamNewsOptStatuses.get(position) == null) return;

            SettingsToggleButton gameStartToggleButton = gameStartOption.getToggleButton();
            boolean toggledOn = gameStartToggleButton.isToggledOn();

            presenter.setGameStartNotifications(teams.get(position), !toggledOn);
            gameStartToggleButton.animateToggle(!toggledOn);
            teamNewsOptStatuses.get(position).setGameStartOptStatus(!toggledOn);
            allOption.getToggleButton().animateToggle(isAllToggledOn());
        }

        @OnClick(R.id.final_score_option)
        public void toggleFinalScoreOption() {
            int position = getAdapterPosition();
            if (position == -1
                    || presenter == null
                    || teams.get(position) == null
                    || teamNewsOptStatuses.get(position) == null) return;

            SettingsToggleButton finalScoreToggleButton = finalScoreOption.getToggleButton();
            boolean toggledOn = finalScoreToggleButton.isToggledOn();

            presenter.setFinalScoreNotifications(teams.get(position), !toggledOn);
            finalScoreToggleButton.animateToggle(!toggledOn);
            teamNewsOptStatuses.get(position).setFinalScoreOptStatus(!toggledOn);
            allOption.getToggleButton().animateToggle(isAllToggledOn());
        }

        @OnClick(R.id.all_option)
        public void toggleAllOptions() {
            int position = getAdapterPosition();
            if (position == -1
                    || presenter == null
                    || teams.get(position) == null
                    || teamNewsOptStatuses.get(position) == null) return;

            SettingsToggleButton allOptionToggleButton = allOption.getToggleButton();
            boolean toggledOn = allOptionToggleButton.isToggledOn();

            presenter.setAllTeamNotifications(teams.get(position), !toggledOn);
            allOptionToggleButton.animateToggle(!toggledOn);
            teamNewsOption.getToggleButton().animateToggle(!toggledOn);
            gameStartOption.getToggleButton().animateToggle(!toggledOn);
            finalScoreOption.getToggleButton().animateToggle(!toggledOn);

            TeamNewsOptStatus teamNewsOptStatus = teamNewsOptStatuses.get(position);
            teamNewsOptStatus.setTeamNewsOptStatus(!toggledOn);
            teamNewsOptStatus.setGameStartOptStatus(!toggledOn);
            teamNewsOptStatus.setFinalScoreOptStatus(!toggledOn);

            allOptionToggleButton.animateToggle(!toggledOn);
        }

        @OnClick(R.id.team_info)
        public void toggleOptionsVisibility() { //show or hide all the different options
            int newTeamIndexWithOptionsOpenPosition = getAdapterPosition();
            if (newTeamIndexWithOptionsOpenPosition == -1) return;

            areOptionsShowingAtTeamIndex[newTeamIndexWithOptionsOpenPosition] = true;

            if (teamWithOptionsOpenIndex == NO_TEAMS_WITH_OPTIONS_OPEN) { // No drawers open
                areOptionsShowingAtTeamIndex[newTeamIndexWithOptionsOpenPosition] = true;
                teamWithOptionsOpenIndex = newTeamIndexWithOptionsOpenPosition;

            } else { //One drawer is already open
                int previousTeamIndexWithOptionsOpen = teamWithOptionsOpenIndex;

                if (newTeamIndexWithOptionsOpenPosition == previousTeamIndexWithOptionsOpen) {
                    //Closing the drawer
                    areOptionsShowingAtTeamIndex[newTeamIndexWithOptionsOpenPosition] = false;
                    teamWithOptionsOpenIndex = NO_TEAMS_WITH_OPTIONS_OPEN;

                } else {
                    //Opening a new drawer
                    areOptionsShowingAtTeamIndex[previousTeamIndexWithOptionsOpen] = false;
                    areOptionsShowingAtTeamIndex[newTeamIndexWithOptionsOpenPosition] = true;
                    teamWithOptionsOpenIndex = newTeamIndexWithOptionsOpenPosition;
                    notifyItemChanged(previousTeamIndexWithOptionsOpen);
                }
            }
            notifyItemChanged(newTeamIndexWithOptionsOpenPosition);
        }

        boolean isAllToggledOn() {
            return teamNewsOption.getToggleButton().isToggledOn()
                    && gameStartOption.getToggleButton().isToggledOn()
                    && finalScoreOption.getToggleButton().isToggledOn();
        }
    }
}
