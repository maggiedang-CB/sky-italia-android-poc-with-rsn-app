package com.nbcsports.regional.nbc_rsn.data_menu.score

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuDataManager
import com.nbcsports.regional.nbc_rsn.data_menu.components.SubMenuBaseFragment
import com.nbcsports.regional.nbc_rsn.data_menu.intent.*
import com.nbcsports.regional.nbc_rsn.data_menu.models.*
import com.nbcsports.regional.nbc_rsn.extensions.d
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.nbcsports.regional.nbc_rsn.utils.DataMenuUtils
import kotlinx.android.synthetic.main.fragment_data_menu_score.*

class DataMenuScoreFragment : SubMenuBaseFragment() {

    private var adapter: DataMenuScoreAdapter<*>? = null

    private val dataWatcherMLB = object : DataMenuBoxScoreWatcherMLB {
        override fun onDataReady(data: BoxEvent<BoxScoreMLB>) {
            resetAdapter(DataMenuScoreAdapterMLB(), data)
        }
    }

    private val dataWatcherNBA = object : DataMenuBoxScoreWatcherNBA {
        override fun onDataReady(data: BoxEvent<BoxScoreNBA>) {
            resetAdapter(DataMenuScoreAdapterNBA(), data)
        }
    }

    private val dataWatcherNFL = object : DataMenuBoxScoreWatcherNFL {
        override fun onDataReady(data: BoxEvent<BoxScoreNFL>) {
            resetAdapter(DataMenuScoreAdapterNFL(), data)
        }
    }

    private val dataWatcherNHL = object : DataMenuBoxScoreWatcherNHL {
        override fun onDataReady(data: BoxEvent<BoxScoreNHL>) {
            d(String.format("This is the enter point: BoxEvent<BoxScoreNHL>: %s", data.periodDetails?.toList()))
            resetAdapter(DataMenuScoreAdapterNHL(), data)
        }
    }

    private val scheduleWatcher = object : DataMenuRotoScheduleWatcher {
        override fun onDataReady(data: RotoResponse<RotoSchedule>) {
            adapter?.updateOverview(data.schedule)
        }
    }

    override fun getSubLayout(): Int {
        return R.layout.fragment_data_menu_score
    }

    override fun getTitle(): String {
        return if (LocalizationManager.isInitialized()) {
            LocalizationManager.DataMenu.BoxscoreRecentGame
        } else {
            getString(R.string.recent_game)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataMenuUtils.DATA_MENU_SCORE_IS_OPENED = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scores_list.layoutManager = LinearLayoutManager(context)

        TeamManager.getInstance()?.getSelectedTeam()?.let { team ->
            when (team.league.toLowerCase()) {
                "nfl" -> dataWatcherNFL
                "nhl" -> dataWatcherNHL
                "nba" -> dataWatcherNBA
                "mlb" -> dataWatcherMLB
                else -> null
            }?.let { DataMenuDataManager.subscribe(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DataMenuDataManager.unsubscribe(dataWatcherNHL)
        DataMenuDataManager.unsubscribe(dataWatcherNFL)
        DataMenuDataManager.unsubscribe(dataWatcherMLB)
        DataMenuDataManager.unsubscribe(dataWatcherNBA)
        DataMenuDataManager.unsubscribe(scheduleWatcher)
        DataMenuUtils.DATA_MENU_SCORE_IS_OPENED = false
    }

    private fun <T : BoxScoreData> resetAdapter(newAdapter: DataMenuScoreAdapter<T>, newData: BoxEvent<T>) {
        newAdapter.update(newData)
        scores_list.adapter = newAdapter
        adapter = newAdapter

        DataMenuDataManager.subscribe(scheduleWatcher)
    }
}