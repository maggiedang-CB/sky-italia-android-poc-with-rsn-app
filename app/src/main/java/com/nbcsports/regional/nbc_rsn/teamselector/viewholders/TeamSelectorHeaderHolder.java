package com.nbcsports.regional.nbc_rsn.teamselector.viewholders;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager;

public class TeamSelectorHeaderHolder extends RecyclerView.ViewHolder {

    private TextView myTeamLabelTextView;

    public TeamSelectorHeaderHolder(View view) {
        super(view);
        myTeamLabelTextView = view.findViewById(R.id.my_teams_label);
    }

    public void init() {
        if (LocalizationManager.isInitialized()){
            myTeamLabelTextView.setText(LocalizationManager.TeamSelector.MyTeams);
        }
    }

}
