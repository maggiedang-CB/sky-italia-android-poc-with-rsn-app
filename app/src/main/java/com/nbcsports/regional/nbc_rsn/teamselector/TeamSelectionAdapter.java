package com.nbcsports.regional.nbc_rsn.teamselector;


import android.location.Location;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.teamselector.viewholders.TeamSelectorHeaderHolder;
import com.nbcsports.regional.nbc_rsn.teamselector.viewholders.TeamSelectorHeadingHolder;
import com.nbcsports.regional.nbc_rsn.teamselector.viewholders.TeamSelectorItemHolder;
import com.nbcsports.regional.nbc_rsn.utils.LocationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lombok.Setter;

public class TeamSelectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_POSITION = 0;

    private static final int HEADING_SIZE = 1; //Heading (Region title) occupies 1 element. (Not HEADER)

    private HashMap<String, ArrayList<Team>> teamMap = new HashMap<>();
    private ArrayList<String> regions = new ArrayList<>(); //Keys are set by region
    private HashSet<Team> selectedTeams = new HashSet<>();
    private TeamSelectorListener teamSelectListener;
    private RecyclerView recyclerView;

    @Setter
    private boolean progressBarIsVisible = false;

    public enum ViewType {
        HEADER, HEADING, ITEM
    }

    TeamSelectionAdapter(TeamSelectorListener listener) {
        if (listener != null) {
            teamSelectListener = listener;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ViewType.HEADING.ordinal()) {
            TeamSelectorHeadingHolder headingHolder = new TeamSelectorHeadingHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.team_selection_heading, parent, false));
            headingHolder.setOnClickListener(new HeadingOnClickListener(headingHolder, recyclerView));
            return headingHolder;

        } else if (viewType == ViewType.ITEM.ordinal()) {
            TeamSelectorItemHolder itemHolder = new TeamSelectorItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.team_selection_item, parent, false));
            itemHolder.setOnClickListener(new ItemOnClickListener(itemHolder, recyclerView));
            return itemHolder;

        } else {
            return new TeamSelectorHeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.team_selection_header, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (position == HEADER_POSITION) {
            if (holder instanceof TeamSelectorHeaderHolder){
                ((TeamSelectorHeaderHolder) holder).init();
            }
            return;
        }

        if (isHeadingPosition(position)) {
            TeamSelectorHeadingHolder teamSelectionHeadingHolder = (TeamSelectorHeadingHolder) holder;
            Team team = findTeamByPosition(position);
            teamSelectionHeadingHolder.bindTo(team, allTeamsInRegionSelected(team));

        } else {
            TeamSelectorItemHolder teamItemHolder = (TeamSelectorItemHolder) holder;
            Team team = findTeamByPosition(position);
            teamItemHolder.bindTo(team, selectedTeams.contains(team));

        }
    }

    private Team findTeamByPosition(int position) {
        //Flatten the HashMap teamMap of String region : ArrayList<Team> and search for the team by
        // position, considering a pseudo header for each region
        /*
        Example:
            {
                "Boston" : [Celtics, Patriots]
                "Washington" : [Capitals, Wizards]
            }

            "Flatten the HashMap" -> {Boston, Celtics, Patriots, Washington, Capitals, Wizards}

            Therefore:  position == 0 -> Returns null (Since it is a region, not Team)
                        position == 1 -> Returns Team Celtics
                        position == 3 -> Returns null (Since it is a region, not Team)
                        position == 5 -> Returns Team Wizards
         */
        int base = 1;
        for (String region : regions) {

            ArrayList<Team> teamsInRegion = teamMap.get(region);

            if (base <= position && position <= base + teamsInRegion.size()) {
                if (position == base) {
                    return teamsInRegion.get(position - base); //use base + 1
                }
                return teamsInRegion.get(position - base - 1); //position - base
            } else {

                base += teamsInRegion.size() + 1;
            }
        }

        return null;
    }

    private boolean isHeadingPosition(int position) {
        int heading = 1;
        for (String region : regions) {
            if (position == heading) {
                return true;
            }
            heading += teamMap.get(region).size() + HEADING_SIZE;
        }
        return false;
    }

    private int findPositionByTeam(Team team) {
        if (team == null) return -1;

        int position = 1;
        String teamRegion = team.getRegionGroupingName();
        for (String region : regions) {
            if (region.equals(teamRegion)) {
                position += HEADING_SIZE;

                ArrayList<Team> teamsInRegion = teamMap.get(region);
                for (Team teamInRegion : teamsInRegion) {
                    if (teamInRegion.getTeamId().equals(team.getTeamId())) {
                        return position;
                    } else {
                        //Skip teams of no concern.
                        position += 1;
                    }
                }
            } else {
                //Skip regions of no concern
                position += teamMap.get(region).size() + HEADING_SIZE;
            }
        }
        return -1;
    }

    private int findHeadingPositionByTeam(Team team) {
        if (team == null) {
            return -1;
        }

        int position = 1;
        String teamRegion = team.getRegionGroupingName();
        for (String region : regions) {
            if (!region.equals(teamRegion)) {
                position += teamMap.get(region).size() + 1;
            } else {
                return position;
            }
        }
        return -1;
    }

    private boolean allTeamsInRegionSelected(Team team) {
        if (team == null) {
            return false;
        }
        String region = team.getRegionGroupingName();
        ArrayList<Team> teams = teamMap.get(region);
        int count = 0;
        for (Team teamInRegion : teams) {
            if (selectedTeams.contains(teamInRegion)) {
                count += 1;
            }
        }
        return count == teams.size();
    }

    public void deselectTeam(Team team) {
        if (team == null || recyclerView == null) {
            return;
        }

        int position = findPositionByTeam(team);
        TeamSelectorItemHolder itemHolder = (TeamSelectorItemHolder) recyclerView.findViewHolderForAdapterPosition(position);

        if (itemHolder != null) {
            itemHolder.deselect();
        }

        if (teamSelectListener != null) {
            teamSelectListener.onTeamDeselected(team);
        }

        selectedTeams.remove(team);
        notifyItemChanged(position);

        if (!allTeamsInRegionSelected(team)) {
            int headingPosition = findHeadingPositionByTeam(team);
            TeamSelectorHeadingHolder headingHolder = (TeamSelectorHeadingHolder)
                    recyclerView.findViewHolderForAdapterPosition(headingPosition);
            if (headingHolder != null) {
                headingHolder.deselectRegion();
            } else {
                notifyItemChanged(headingPosition);
            }
        }
    }

    public void setSelectedTeams(List<Team> newSelectedTeams) {
        if (newSelectedTeams != null) {
            selectedTeams = new HashSet<>(newSelectedTeams);
            notifyDataSetChanged();
        }
    }

    public void setTeamMap(HashMap<String, ArrayList<Team>> newTeamMap) {
        if (newTeamMap == null) return;

        Location userLocation = LocationUtils.getUserLocation();

        if (userLocation != null){
            regions = getSortedRegionNamesByDistance(userLocation, new HashMap<>(newTeamMap));

        } else {
            regions.clear();
            regions.addAll(newTeamMap.keySet());
        }

        for (String region : regions){
            // alphabetically sort the teams in each region
            teamMap.put(region, getTeamsSortedAlphabetically(newTeamMap.get(region)));
        }

        notifyDataSetChanged();
    }

    /***
     * Returns an ArrayList<String> representing a sorted (closest first) version of regions in the teamMap (global).
     *  The regions (regions) are sorted by a representative Team in each region. The representative
     *  Team will have the closest location to the user's location. Since Teams in the region are
     *  sorted by location in the method: sortTeamsInRegionByLocation, the representing Team will be
     *  the first team in the Region.
     */
    private ArrayList<String> getSortedRegionNamesByDistance(Location userLocation, HashMap<String, ArrayList<Team>> newTeamMap) {
        if (userLocation == null || newTeamMap == null || newTeamMap.isEmpty()) return new ArrayList<>();

        ArrayList<String> regions = new ArrayList<>(newTeamMap.keySet());

        ArrayList<Team> closestTeamsFromEachRegion = new ArrayList<>();

        for (String region : regions) {
            Team closestTeam = getClosestTeam(userLocation, newTeamMap.get(region));
            if (closestTeam != null){
                closestTeamsFromEachRegion.add(closestTeam);
            }
        }
        // sort the closest teams from each region
        Collections.sort(closestTeamsFromEachRegion, new TeamLocationComparator(userLocation));

        ArrayList<String> sortedRegionNames = new ArrayList<>();
        for (Team team : closestTeamsFromEachRegion) {
            sortedRegionNames.add(team.getRegionGroupingName());
        }

        return sortedRegionNames;
    }

    private Team getClosestTeam(Location userLocation, ArrayList<Team> teams){
        if (userLocation == null || teams == null || teams.isEmpty()) return null;

        ArrayList<Team> sortedTeams = new ArrayList<>(teams);
        Collections.sort(sortedTeams, new TeamLocationComparator(userLocation));
        return sortedTeams.get(0);
    }

    private ArrayList<Team> getTeamsSortedAlphabetically(ArrayList<Team> teams){
        ArrayList<Team> sortedTeams = new ArrayList<>(teams);
        Collections.sort(sortedTeams, new TeamNameAlphaComparator());
        return sortedTeams;
    }

    @Override
    public int getItemCount() {
        int size = regions.size() + 1;  // for header and headings
        for (String key : regions) {
            size += teamMap.get(key).size();
        }
        return size;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ViewType.HEADER.ordinal();
        } else if (isHeadingPosition(position)) {
            return ViewType.HEADING.ordinal();
        } else {
            return ViewType.ITEM.ordinal();
        }
    }

    class HeadingOnClickListener implements View.OnClickListener {

        TeamSelectorHeadingHolder itemHolder;
        RecyclerView recyclerView;

        HeadingOnClickListener(TeamSelectorHeadingHolder itemHolder, RecyclerView recyclerView) {
            this.itemHolder = itemHolder;
            this.recyclerView = recyclerView;
        }

        @Override
        public void onClick(View v) {

            if (progressBarIsVisible) return;

            int position = itemHolder.getAdapterPosition();
            Team team = findTeamByPosition(position);

            if (team == null || team.getRegionGroupingName().isEmpty()) {
                return;
            }

            ArrayList<Team> teamsFromRegion = teamMap.get(team.getRegionGroupingName());

            if (allTeamsInRegionSelected(team)) {
                deselectAllFromRegion(teamsFromRegion);
                itemHolder.deselectRegion(); //UI update
            } else {
                selectAllFromRegion(teamsFromRegion);
                itemHolder.selectRegion(); //UI update
            }
        }

        private void deselectAllFromRegion(ArrayList<Team> teamsFromRegion) {
            if (recyclerView == null) return;

            for (Team team : teamsFromRegion) {

                if (selectedTeams.contains(team)) {
                    if (teamSelectListener != null) {
                        teamSelectListener.onTeamDeselected(team);
                    }
                    selectedTeams.remove(team);
                    updateDeselectedItemHolder(team);
                }
            }
        }

        private void updateDeselectedItemHolder(Team team) {
            int teamPosition = findPositionByTeam(team);
            TeamSelectorItemHolder itemHolder =
                    ((TeamSelectorItemHolder) recyclerView.findViewHolderForAdapterPosition(teamPosition));
            if (itemHolder != null) { //Not in view
                itemHolder.deselect();
            } else {
                notifyItemChanged(teamPosition);
            }
        }

        private void selectAllFromRegion(ArrayList<Team> teamsFromRegion) {
            if (recyclerView == null) return;

            for (Team team : teamsFromRegion) {
                if (!selectedTeams.contains(team)) {
                    selectedTeams.add(team);
                    teamSelectListener.onTeamSelected(team);
                    updateSelectedItemHolder(team);
                }
            }
        }

        private void updateSelectedItemHolder(Team team) {
            int teamPosition = findPositionByTeam(team);
            TeamSelectorItemHolder itemHolder =
                    ((TeamSelectorItemHolder) recyclerView.findViewHolderForAdapterPosition(teamPosition));
            if (itemHolder != null) {
                itemHolder.select();
            } else {
                notifyItemChanged(teamPosition);
            }
        }

    }

    class ItemOnClickListener implements View.OnClickListener {

        TeamSelectorItemHolder itemHolder;
        RecyclerView recyclerView;

        ItemOnClickListener(TeamSelectorItemHolder itemHolder, RecyclerView recyclerView) {
            this.itemHolder = itemHolder;
            this.recyclerView = recyclerView;
        }

        @Override
        public void onClick(View v) {

            if (progressBarIsVisible) return;

            int position = itemHolder.getAdapterPosition();

            Team team = findTeamByPosition(position);

            if (team == null) {
                return;
            }

            if (selectedTeams.contains(team)) {
                teamSelectListener.onTeamDeselected(team);
                selectedTeams.remove(team);
                itemHolder.deselect();
            } else {
                teamSelectListener.onTeamSelected(team);
                selectedTeams.add(team);
                itemHolder.select();
            }

            updateRegionHeader(team);
        }

        private void updateRegionHeader(Team team) {
            if (team == null || recyclerView == null) return;

            int headingPosition = findHeadingPositionByTeam(team);
            TeamSelectorHeadingHolder headingHolder = ((TeamSelectorHeadingHolder)
                    recyclerView.findViewHolderForAdapterPosition(headingPosition));

            if (headingHolder != null) {
                if (allTeamsInRegionSelected(team)) {
                    headingHolder.selectRegion();
                } else {
                    headingHolder.deselectRegion();
                }
            } else {
                notifyItemChanged(headingPosition);
            }
        }


    }

    final class TeamLocationComparator implements Comparator<Team> {

        Location userLocation;

        TeamLocationComparator(Location location) {
            userLocation = location;
        }

        @Override
        public int compare(Team team1, Team team2) {
            Location teamLocation1 = new Location("");
            teamLocation1.setLatitude(team1.getGeolocation().getLatitude());
            teamLocation1.setLongitude(team1.getGeolocation().getLongitude());

            Location teamLocation2 = new Location("");
            teamLocation2.setLatitude(team2.getGeolocation().getLatitude());
            teamLocation2.setLongitude(team2.getGeolocation().getLongitude());

            return Double.compare(userLocation.distanceTo(teamLocation1), userLocation.distanceTo(teamLocation2));
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof TeamLocationComparator
                    && userLocation.getLatitude() == ((TeamLocationComparator) obj).userLocation.getLatitude()
                    && userLocation.getLongitude() == ((TeamLocationComparator) obj).userLocation.getLongitude();
        }
    }

    final class TeamNameAlphaComparator implements Comparator<Team> {

        @Override
        public int compare(Team team1, Team team2) {
            String team1Name = team1.getDisplayName();
            String team2Name = team2.getDisplayName();
            return team1Name.compareTo(team2Name);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof TeamNameAlphaComparator;
        }
    }

}
