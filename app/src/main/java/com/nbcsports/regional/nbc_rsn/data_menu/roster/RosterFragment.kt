package com.nbcsports.regional.nbc_rsn.data_menu.roster

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuDataManager
import com.nbcsports.regional.nbc_rsn.data_menu.components.SubMenuBaseFragment
import com.nbcsports.regional.nbc_rsn.data_menu.intent.DataMenuRosterWatcher
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoPlayer
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoResponse
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.nbcsports.regional.nbc_rsn.utils.DataMenuUtils
import kotlinx.android.synthetic.main.roster_fragment.*

class RosterFragment : SubMenuBaseFragment(), DataMenuRosterWatcher {

    private var adapter: RosterAdapter? = null
    private var statsRoster = mutableListOf<RotoPlayer>()
//    private var statsParticipants: StatsParticipants? = null

    override fun getSubLayout(): Int {
        return R.layout.roster_fragment
    }

    override fun getTitle(): String {
        return if (LocalizationManager.isInitialized()) {
            "${TeamManager.getInstance()?.getSelectedTeam()?.displayName} ${LocalizationManager.DataMenu.Roster}"
        } else {
            "${TeamManager.getInstance()?.getSelectedTeam()?.displayName} ${getString(R.string.roster)}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataMenuUtils.DATA_MENU_ROSTER_IS_OPENED = true
        DataMenuDataManager.subscribe(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roster_recyclerview.layoutManager = LinearLayoutManager(context)
        adapter = RosterAdapter(this, RosterAdapter.SortOption.NAME)
        roster_recyclerview.adapter = adapter

        updateAdapter()
    }

    private fun updateAdapter() {
        adapter?.rosters = statsRoster
    }

    override fun onDataReady(data: RotoResponse<RotoPlayer>) {
        data.roster?.let {
            statsRoster = it.toMutableList()
            updateAdapter()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DataMenuDataManager.unsubscribe(this)
        DataMenuUtils.DATA_MENU_ROSTER_IS_OPENED = false
    }

}