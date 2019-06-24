package com.nbcsports.regional.nbc_rsn.data_menu.schedule

import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.data_bar.SeasonState
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuDataManager
import com.nbcsports.regional.nbc_rsn.data_menu.intent.DataMenuRotoScheduleWatcher
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoEvent
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoResponse
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoSchedule
import com.nbcsports.regional.nbc_rsn.data_menu.viewholders.DataMenuScheduleFilterVH
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

class DataMenuScheduleListPresenter(private val view: DataMenuContract.ScheduleList) :
        DataMenuContract.ScheduleFilterListener, DataMenuRotoScheduleWatcher {

    private val rotoEventList = mutableListOf(
            RotoEvent(SeasonState.REGULAR_SEASON.seasonName, listOf()),
            RotoEvent(SeasonState.POSTSEASON.seasonName, listOf())
    )
    private val eventTypeOptions: ArrayList<String> = ArrayList()
    private var currentEventTypePos: Int = 0

    fun bindView(pos: Int, scheduleItemView: DataMenuScheduleItemView) {
        if (rotoEventList.isEmpty()) {
            return
        }

        if (scheduleItemView is DataMenuScheduleFilterVH) {
            if (rotoEventList[currentEventTypePos].events.isNotEmpty()) {
                rotoEventList[currentEventTypePos].events.last().let {
                    scheduleItemView.bind(it, eventTypeOptions)
                }
            } else {
                scheduleItemView.bind(RotoSchedule(homeGlobalId = getStatsTeamId(), homeWins = 0, homeLosses = 0), eventTypeOptions)
            }

        } else {
            if (rotoEventList[currentEventTypePos].events.isNotEmpty()) {
                scheduleItemView.bind(rotoEventList[currentEventTypePos].events[pos - 1])
            }
        }
    }

    fun getCount(): Int {
        return if (rotoEventList.isEmpty()) 0 else rotoEventList[currentEventTypePos].events.size + 1
    }

    fun subscribe() {
        DataMenuDataManager.subscribe(this)
    }

    fun unsubscribe() {
        DataMenuDataManager.unsubscribe(this)
    }

    private val dateComparator = compareBy<RotoSchedule> {
        LocalDateTime.parse(it.gameDateTime, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
    }

    override fun onDataReady(data: RotoResponse<RotoSchedule>) {
        data.schedule?.let { scheduleList ->

            val eventList = mutableListOf<RotoEvent>()

            scheduleList.sortedWith(dateComparator)
                    .groupBy { it.gameType }
                    .forEach { (gameType, events) ->
                        eventList.add(RotoEvent(
                                name = gameType,
                                events = events
                        ))
                    }

            // Add upcoming event list only if there are games remaining in the season
            scheduleList.filter { it.isPreGame() }.sortedWith(dateComparator).let { upcomingGames ->
                if (upcomingGames.isNotEmpty()) {
                    eventList.add(RotoEvent(
                            name = "Upcoming",
                            events = upcomingGames
                    ))
                }
            }

            currentEventTypePos = if (eventList.isEmpty()) 0 else eventList.size - 1

            rotoEventList.clear()
            rotoEventList.addAll(eventList)
            view.notifyDataSetUpdated()
        }
    }

    fun getRecentGamePosition(): Int {
        val items = rotoEventList[currentEventTypePos].events
        val eventId = DataMenuDataManager.findLatestEventId(items.toTypedArray())
        return items.indexOfFirst { it.gameGlobalId == eventId.toInt() }
    }

    // region Filter related methods
    override fun getFilterOptions(): List<String> {
        return rotoEventList.map { it.name ?: "" }
    }

    override fun getStatsTeamId(): Int {
        return TeamManager.getInstance()?.getSelectedTeam()?.statsTeamID ?: 0
    }

    override fun onNewFilterSelected(position: Int) {
        if (position != currentEventTypePos) {
            currentEventTypePos = position
            view.notifyDataSetUpdated()
        }
    }

    override fun getInitFilterIndex(): Int {
        return currentEventTypePos
    }
    // endregion
}
