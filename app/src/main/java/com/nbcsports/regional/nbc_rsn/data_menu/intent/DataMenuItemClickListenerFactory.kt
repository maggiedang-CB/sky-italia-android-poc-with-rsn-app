package com.nbcsports.regional.nbc_rsn.data_menu.intent

import androidx.recyclerview.widget.RecyclerView
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.common.ViewHolderClickListener
import com.nbcsports.regional.nbc_rsn.common.ViewHolderClickListenerFactory
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuFragment
import com.nbcsports.regional.nbc_rsn.data_menu.components.DataMenuAdapter
import com.nbcsports.regional.nbc_rsn.data_menu.components.DataMenuItemCTAViewHolder
import com.nbcsports.regional.nbc_rsn.data_menu.models.DataMenuCTAId
import com.nbcsports.regional.nbc_rsn.data_menu.roster.RosterFragment
import com.nbcsports.regional.nbc_rsn.data_menu.schedule.DataMenuScheduleFragment
import com.nbcsports.regional.nbc_rsn.data_menu.score.DataMenuScoreFragment
import com.nbcsports.regional.nbc_rsn.data_menu.standings.StandingsFragment
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils

class DataMenuItemClickListenerFactory(dataMenuFragment: DataMenuFragment, team: Team, val adapter: DataMenuAdapter) : ViewHolderClickListenerFactory<DataMenuFragment>(dataMenuFragment, team, emptyList()) {

    override fun getClickListener(vh: RecyclerView.ViewHolder, fragment: DataMenuFragment?): ViewHolderClickListener {
        return CTAClickListener(vh, team, fragment, adapter)
    }

    private class CTAClickListener(
            vh: RecyclerView.ViewHolder,
            team: Team,
            val fragment: DataMenuFragment?,
            val adapter: DataMenuAdapter) : ViewHolderClickListener(vh, team, emptyList()) {

        override fun checkViewHolderType(vh: RecyclerView.ViewHolder): Boolean {
            return vh is DataMenuItemCTAViewHolder
        }

        override fun onViewHolderClicked(adapterPos: Int) {
            fragment?.let {
                val (newFrag, rootTag) = when (adapter.getDataMenuItemList()[adapterPos].ctaId) {
                    DataMenuCTAId.ROSTER -> Pair(RosterFragment(), "DataMenuRoster")
                    DataMenuCTAId.SCHEDULE -> Pair(DataMenuScheduleFragment(), "DataMenuSchedule")
                    DataMenuCTAId.STANDINGS -> Pair(StandingsFragment(), "DataMenuStandings")
                    DataMenuCTAId.SCORE -> Pair(DataMenuScoreFragment(), "DataMenuScores")
                    else -> Pair(null, "")
                }

                newFrag?.let {
                    when(it) {
                        is DataMenuScoreFragment -> NavigationManager.getInstance().scoresFragment = it
                        is RosterFragment -> NavigationManager.getInstance().rosterFragment = it
                        is DataMenuScheduleFragment -> NavigationManager.getInstance().dataMenuScheduleFragment = it
                        is StandingsFragment -> NavigationManager.getInstance().standingsFragment = it
                    }
                    val activity = fragment.activity!!
                    ActivityUtils.addFragmentToActivity(
                            activity.supportFragmentManager,
                            newFrag,
                            R.id.fragment_container
                    )
                    ActivityUtils.showAndAddFragmentToBackStackWithFade(
                            rootTag,
                            activity.supportFragmentManager,
                            newFrag
                    )
                }
            }
        }
    }
}