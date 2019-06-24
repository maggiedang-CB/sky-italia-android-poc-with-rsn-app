package com.nbcsports.regional.nbc_rsn.teamselector.viewholders;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;

public class TeamSelectorHeadingHolder extends RecyclerView.ViewHolder {

    private TextView teamCity;
    private TextView selectAllButton;

    public TeamSelectorHeadingHolder(View view) {
        super(view);
        teamCity = view.findViewById(R.id.team_city);
        selectAllButton = view.findViewById(R.id.select_all_button);
    }

    public void bindTo(Team team, boolean allTeamsInRegionSelected) {
        if (team == null) { return; }

        if (!team.getCityName().isEmpty()) {
            teamCity.setText(team.getRegionGroupingName());
        }

        if (allTeamsInRegionSelected) {
            selectRegion();
        } else {
            deselectRegion();
        }
    }

    public void selectRegion() {
        if (LocalizationManager.isInitialized()) {
            selectAllButton.setText(LocalizationManager.TeamSelector.RemoveAll);
        }
    }

    public void deselectRegion() {
        if (LocalizationManager.isInitialized()){
            selectAllButton.setText(LocalizationManager.TeamSelector.SelectAll);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener){
        selectAllButton.setOnClickListener(onClickListener);
    }

}
