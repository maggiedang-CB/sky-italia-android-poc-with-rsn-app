package com.nbcsports.regional.nbc_rsn.teamselector;

import com.nbcsports.regional.nbc_rsn.common.Team;

public interface TeamSelectorListener {

    void onTeamDeselected(Team team);

    void onTeamSelected(Team team);

}
