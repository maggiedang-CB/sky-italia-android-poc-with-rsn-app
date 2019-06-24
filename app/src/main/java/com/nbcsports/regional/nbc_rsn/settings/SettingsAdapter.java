package com.nbcsports.regional.nbc_rsn.settings;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Team> selectedTeams = new ArrayList<>();

    //Maximum number that will be visible when user has not tapped "View More"
    private final int MAX_NUM_DEFAULT_VISIBLE_TEAMS = 7;

    //If number selectedTeams is greater than DEFAULT_VISIBLE_TEAM_COUNT, this is used as a flag to
    // indicate if user has chosen they want to view more or view less.
    private boolean viewingMoreTeams = false;

    private TeamCountingListener teamCountingListener;

    SettingsAdapter(TeamCountingListener listener) {
        teamCountingListener = listener;
    }

    public void setSelectedTeams(List<Team> newSelectedTeams) {
        if (newSelectedTeams != null) {
            selectedTeams.clear();
            selectedTeams.addAll(newSelectedTeams);
            //Lets the listener know whether the number of teams is greater than the MAX_NUM_DEFAULT_VISIBLE_TEAMS.
            //  This in turn will show or hide the toggleTeams button.
            teamCountingListener.onNumberOfTeamsCounted(selectedTeams.size() > MAX_NUM_DEFAULT_VISIBLE_TEAMS);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolderSettingTeam(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_team, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolderSettingTeam) holder).bindTo(selectedTeams.get(position));
    }

    //User will not be able to toggleVisible teams if the number of teams is less than MAX_NUM_DEFAULT_VISIBLE_TEAMS.
    // -- Button will be hidden.
    void toggleVisibleTeamsCount() {
        viewingMoreTeams = !isViewingMoreTeams();
        notifyItemRangeChanged(MAX_NUM_DEFAULT_VISIBLE_TEAMS, selectedTeams.size() - MAX_NUM_DEFAULT_VISIBLE_TEAMS);
    }

    public boolean isViewingMoreTeams() {
        return viewingMoreTeams;
    }

    @Override
    public int getItemCount() {
        //If there are greater than MAX_NUM_DEFAULT_VISIBLE_TEAMS selected selected teams, and user has not opt'ed to view more.
        if (!viewingMoreTeams && selectedTeams.size() > MAX_NUM_DEFAULT_VISIBLE_TEAMS) {
            return MAX_NUM_DEFAULT_VISIBLE_TEAMS;

            //Cases:
            //  1. There are MAX_NUM_DEFAULT_VISIBLE_TEAMS or less selected selected teams (both opt'd and not opt'd)
            //  2. There are greater than MAX_NUM_DEFAULT_VISIBLE_TEAMS number of selected teams selected, and user has opt'd to view more.
        } else {
            return selectedTeams.size();
        }
    }

    class ViewHolderSettingTeam extends RecyclerView.ViewHolder {

        ImageView teamLogo;
        TextView teamName;
        TextView teamCity;

        ViewHolderSettingTeam(View itemView) {
            super(itemView);
            teamLogo = itemView.findViewById(R.id.setting_team_logo);
            teamName = itemView.findViewById(R.id.setting_team_name);
            teamCity = itemView.findViewById(R.id.setting_team_city);
        }

        void bindTo(Team team) {
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
        }
    }

    public interface TeamCountingListener {

        void onNumberOfTeamsCounted(boolean moreTeamsThanDefaultNumber);

    }
}
