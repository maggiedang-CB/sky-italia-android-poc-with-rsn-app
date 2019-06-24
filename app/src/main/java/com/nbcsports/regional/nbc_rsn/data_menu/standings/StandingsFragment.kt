package com.nbcsports.regional.nbc_rsn.data_menu.standings

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View

import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.data_bar.*
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuDataManager
import com.nbcsports.regional.nbc_rsn.data_menu.components.SubMenuBaseFragment
import com.nbcsports.regional.nbc_rsn.data_menu.intent.DataMenuLeagueStandingsWatcher
import com.nbcsports.regional.nbc_rsn.utils.DataMenuUtils
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager

import kotlinx.android.synthetic.main.fragment_standings.*

class StandingsFragment : SubMenuBaseFragment(), DataMenuLeagueStandingsWatcher {

    private var adapter: StandingsLeagueAdapter? = null
    private var leagueStandings: LeagueStandings? = null

    override fun getSubLayout(): Int {
        return R.layout.fragment_standings
    }

    override fun getTitle(): String {
        return if (LocalizationManager.isInitialized()) {
            "${LocalizationManager.DataMenu.League} ${LocalizationManager.DataMenu.Standings}"
        } else {
            getString(R.string.league_standings)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataMenuUtils.DATA_MENU_STANDINGS_IS_OPENED = true
        DataMenuDataManager.subscribe(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Regular Season
        standings_view.layoutManager = LinearLayoutManager(context)
        adapter = StandingsLeagueAdapter()
        standings_view.adapter = adapter

        updateAdapter()
    }

    private fun updateAdapter() {
        val team = TeamManager.getInstance()?.getSelectedTeam()
        val (sportName, _) = DataBarUtil.getRequestParams(team)
        adapter?.setData(standings = leagueStandings, selectConference = true, sportName = sportName)
    }

    override fun onDataReady(leagueStandings: LeagueStandings) {
        this.leagueStandings = leagueStandings
        updateAdapter()
    }

    override fun onResume() {
        super.onResume()
        updateAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DataMenuDataManager.unsubscribe(this)
        DataMenuUtils.DATA_MENU_STANDINGS_IS_OPENED = false
    }
}
