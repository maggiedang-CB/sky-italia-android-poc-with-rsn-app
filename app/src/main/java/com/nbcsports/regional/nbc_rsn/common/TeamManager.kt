package com.nbcsports.regional.nbc_rsn.common

import android.annotation.SuppressLint
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.squareup.picasso.Picasso
import java.util.ArrayList
import kotlin.collections.HashMap

class TeamManager {
    companion object {

        // TODO: change this class to a Kotlin object?
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: TeamManager? = null
        const val MAX_USER_LIST = 7

        @Synchronized
        fun getInstance(): TeamManager? {
            if (instance == null) {
                instance = TeamManager()
            }
            return instance
        }
    }

    private val masterList = ArrayList<Team>()
    val usersTeams = ArrayList<Team>()
    private var selectedTeam: Team? = null
    val masterHashMap = HashMap<String, ArrayList<Team>>()
    private val logoUrlMap = HashMap<Int, String>()

    val teamsForMenu: List<Team>
        get() {
            val x = usersTeams.size
            return when {
                x <= MAX_USER_LIST -> usersTeams
                else -> {
                    // remove one item so that we show the More Teams card
                    usersTeams.subList(0, MAX_USER_LIST - 1)
                }
            }
        }

    // selectedTeam is most likely null
    val moreTeamsList: List<Team>
        get() {
            return when {
                MAX_USER_LIST >= usersTeams.size -> ArrayList()
                else -> usersTeams.subList(MAX_USER_LIST - 1, usersTeams.size)
            }
        }

    fun setLogoUrlMap(newMap: HashMap<Int, String>) {
        // first reset the map
        logoUrlMap.clear()
        logoUrlMap.putAll(newMap)

        // then preload all icons
        logoUrlMap.values.forEach {
            Picasso.get().load(it).fetch()
        }
    }

    fun setMasterList(list: List<Team>?) {
        if (list == null) return
        masterList.clear()
        masterList.addAll(list)
        setHashMap(masterList)
    }

    fun getMasterList(): List<Team> {
        return masterList
    }

    fun setUserList(list: List<Team>?) {
        if (list == null) return
        usersTeams.clear()
        usersTeams.addAll(list)

        // 1. Set the first team to selectedTeam
        // 2. If there is no team, then set selectedTeam to null (FYI, this fixes the crash below:
        //    Let's say dev has an extra team that doesn't exists in prod, then follow the flow
        //    Select only extra team on dev -> switch to prod on debug menu -> reopen the app
        //    -> crash (because selectedTeam is not null, but usersTeams is empty then crash
        //    the TeamsPagerAdapter)
        when {
            (usersTeams.size > 0) -> { selectedTeam = usersTeams[0] }
            else -> { selectedTeam = null }
        }
    }

    fun getTeamByStatsId(statsTeamId: Int) : Team? {
        return usersTeams.find { it.statsTeamID == statsTeamId }
    }

    /**
     * This takes a comma-separated string of Team IDs and looks them up in the master list.
     * The user list is then updated from this filtered set.
     *
     * Input: Boston-Celtics,Philadelphia-Sixers,Boston-Bruins,
     * Output: Filtered list of Team models
     */
    fun restoreUserList(input: String) {
        if (masterList.isEmpty() || input.isEmpty()) return

        val teamIds = input
                .trimEnd(',')
                .split(",")

        val filteredList = ArrayList<Team>()
        for (tid in teamIds) {
            val item = masterList.find { it.teamId == tid }
            if (item != null) {
                filteredList.add(item)
            }
        }

        setUserList(filteredList)
    }

    fun setSelectedTeam(selectedTeam: Team) {
        e("setSelectedTeam() called with: selectedTeam = [" + selectedTeam.teamId + "]")

        if (selectedTeam in usersTeams) {
            this.selectedTeam = selectedTeam
        }
    }

    fun getSelectedTeam(): Team? {
        if (selectedTeam == null && usersTeams.size > 0) {
            selectedTeam = usersTeams[0]
        }
        return selectedTeam
    }

    fun getSelectedTeamIndex(): Int {
        return if (selectedTeam == null || usersTeams.size <= 0) {
            -1
        } else {
            usersTeams.indexOf(selectedTeam!!)
        }
    }

    private fun setHashMap(masterList: List<Team>) {
        masterHashMap.clear()
        for (team in masterList) {
            val key = team.regionGroupingName
            if (!masterHashMap.containsKey(key)) {
                masterHashMap[key] = ArrayList()
            }
            if (!masterHashMap[key]!!.contains(team)) {
                masterHashMap[key]!!.add(team)
            }
        }
    }

    fun getLogoUrl(statsTeamId: Int) : String? {
        return logoUrlMap.get(statsTeamId)
    }

    fun getUserTeamByTeamId(teamId : String) : Team? {
        for (team in usersTeams){
            if (team.teamId.equals(teamId, true)){
                return team
            }
        }
        return null
    }

    fun getUserTeamByTeamDisplayName(displayName : String) : Team? {
        for (team in usersTeams){
            if (team.displayName.equals(displayName, true)){
                return team
            }
        }
        return null
    }
}
