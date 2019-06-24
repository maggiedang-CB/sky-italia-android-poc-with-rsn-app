package com.nbcsports.regional.nbc_rsn.data_menu.score

import com.nbcsports.regional.nbc_rsn.data_bar.Linescore
import com.nbcsports.regional.nbc_rsn.data_bar.StatsTeam
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.models.*
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager

abstract class DataMenuScoreListPresenter<T: BoxScoreData> : DataMenuContract.ScorePlayerPresenter,
        DataMenuContract.ScoreStatsPresenter, DataMenuContract.ScoreSummaryPresenter {

    private var currentActiveTab = DataMenuScoreListTabs.HOME
    private var event: BoxEvent<T>? = null

    var eventOverview: RotoSchedule? = null
        private set

    fun switchTeam(newActiveTab: DataMenuScoreListTabs) {
        currentActiveTab = newActiveTab
    }

    fun updateOverview(scheduleList: Array<RotoSchedule>?) {
        scheduleList?.firstOrNull { it.gameGlobalId.toLong() == event?.eventId }?.let { eventOverview = it }
    }

    fun getCardCount(): Int {
        return if (event == null) 0 else getStatsCardCount()
    }

    fun update(newData: BoxEvent<T>) {
        event = newData
        event?.boxscores?.forEach { sortData(it) }
    }

    fun getAwayTeamName(): String {
        return event?.teams?.firstOrNull { it.isAway() }?.nickname ?: ""
    }

    fun getHomeTeamName(): String {
        return event?.teams?.firstOrNull { it.isHome() }?.nickname ?: ""
    }

    fun getHighestShootoutGoalsTeamId(): Int {
        var resultTeamId = 0
        event?.teams?.let {
            var maxShootoutGoals = 0
            for (statsTeam in it){
                if (statsTeam.shootoutGoals > maxShootoutGoals){
                    maxShootoutGoals = statsTeam.shootoutGoals
                    resultTeamId = statsTeam.teamId
                }
            }
        }
        return resultTeamId
    }

    fun getHighestShootoutGoals(): Int {
        var maxShootoutGoals = 0
        event?.teams?.let {
            for (statsTeam in it){
                if (statsTeam.shootoutGoals > maxShootoutGoals){
                    maxShootoutGoals = statsTeam.shootoutGoals
                }
            }
        }
        return maxShootoutGoals
    }

    override fun getAwayTeamAbbreviation(): String {
        return event?.teams?.firstOrNull { it.isAway() }?.abbreviation ?: ""
    }

    override fun getHomeTeamAbbreviation(): String {
        return event?.teams?.firstOrNull { it.isHome() }?.abbreviation ?: ""
    }

    override fun getScoringSummaryPeriodDetailsList(): List<PeriodDetail> {
        return listOf()
    }

    protected fun getAwayTeamId(): Int {
        return event?.teams?.firstOrNull { it.isAway() }?.teamId ?: -1
    }

    protected fun getHomeTeamId(): Int {
        return event?.teams?.firstOrNull { it.isHome() }?.teamId ?: -1
    }

    protected fun getFullPeriodDetails(): List<PeriodDetail> {
        return event?.periodDetails?.toList() ?: listOf()
    }

    final override fun getNameCount(viewType: Int): Int {
        return when (viewType) {
            SCORE_SUMMARY_UNIQUE_VIEW_TYPE -> SCORE_SUMMARY_ROW_COUNT
            else -> getBoxScore()?.let { getPlayers(it, viewType) }?.size ?: 0
        }
    }

    final override fun getLongestName(viewType: Int): String {
        return when (viewType) {
            SCORE_SUMMARY_UNIQUE_VIEW_TYPE -> // use a relatively wide letter to allocate enough space for team names
                event?.teams?.maxBy { it.abbreviation.length }?.abbreviation?.length?.let { "M".repeat(it) } ?: ""
            else -> getBoxScore()?.let { getPlayers(it, viewType) }?.maxBy { it.lastName.length }?.getFullName() ?: ""
        }
    }

    final override fun bindView(view: DataMenuContract.ScorePlayerView, position: Int, viewType: Int) {
        when (viewType) {
            SCORE_SUMMARY_UNIQUE_VIEW_TYPE -> {
                event?.teams?.let { com.nbcsports.regional.nbc_rsn.extensions.fromInt<DataMenuScoreListTabs>(position)
                        ?.let { teamType -> teamType.getTeam(it) } }?.abbreviation?.toUpperCase()
            }
            else -> getBoxScore()?.let { getPlayers(it, viewType) }?.get(position)?.getFullName()
        }?.let { view.bind(it) }
    }

    final override fun getRowCount(viewType: Int): Int {
        return when (viewType) {
            SCORE_SUMMARY_UNIQUE_VIEW_TYPE -> SCORE_SUMMARY_ROW_COUNT
            else -> getBoxScore()?.let { getPlayers(it, viewType) }?.size
        }?.plus(1) ?: 1
    }

    final override fun getColumnCount(viewType: Int): Int {
        return if (event == null) 0 else when (viewType) {
            SCORE_SUMMARY_UNIQUE_VIEW_TYPE -> event?.teams?.get(0)?.linescores?.size?.plus(getShootoutGoalsColumnCount() + 1) ?: 0
            else -> getStatsCardColumnCount(viewType)
        }
    }

    final override fun bindView(view: DataMenuContract.ScoreStatsView, position: Int, viewType: Int) {
        val rowCount = getRowCount(viewType)
        if (rowCount == 0) {
            return
        }

        // minus one to exclude the header row
        val rowIndex = position % rowCount - 1
        val columnIndex = position / rowCount

        when (viewType) {
            SCORE_SUMMARY_UNIQUE_VIEW_TYPE -> {
                event?.teams?.let { com.nbcsports.regional.nbc_rsn.extensions.fromInt<DataMenuScoreListTabs>(rowIndex)
                        ?.let { teamType -> teamType.getTeam(it) } }
                        ?.let {
                            if (it.linescores?.size?.let { count -> columnIndex >= (count + getShootoutGoalsColumnCount()) } != false) {
                                // total
                                bindSummaryTotal(view, it)
                            } else if (getShootoutGoalsColumnCount() > 0 && it.linescores?.size == columnIndex){
                                bindSummaryShootoutGoals(view, it)
                            } else {
                                it.linescores?.get(columnIndex)?.let { it1 -> bindSummaryStats(view, it1) }
                            }
                        }
            }
            // minus the header index
            else -> getBoxScore()?.let { bindStats(view, rowIndex, columnIndex, it, viewType) }
        }
    }

    final override fun bindHeader(view: DataMenuContract.ScoreStatsView, position: Int, viewType: Int) {
        val rowCount = getRowCount(viewType)
        if (rowCount == 0) {
            return
        }

        val columnIndex = position / rowCount

        view.bind(
                when (viewType) {
                    // add one to make it 1-indexed
                    SCORE_SUMMARY_UNIQUE_VIEW_TYPE -> (columnIndex + 1).let { periodNum ->
                        when {
                            periodNum >= getColumnCount(viewType) -> getSummaryTotalHeader()
                            periodNum <= getSummaryRegularColumnCount() -> periodNum.toString()
                            getShootoutGoalsColumnCount() > 0 && periodNum == (getColumnCount(viewType) - 1) -> getShootoutGoalsHeader()
                            else -> // overtime
                            {
                                // FIXME: this is a dumb way of checking for extra innings in baseball
                                if (getSummaryRegularColumnCount() == 9) {
                                    periodNum.toString()
                                } else {
                                    // Handle NHL Shootouts
                                    /*
                                    FIXME: this is a temp solution because shootout handling needs to be reworked
                                     */
                                    if (isShootoutResult() && periodNum > getSummaryRegularColumnCount() + 1) {
                                        getShootoutGoalsHeader()
                                    } else {
                                        getSummaryOvertimeHeader(periodNum - getSummaryRegularColumnCount())
                                    }
                                }
                            }
                        }
                    }
                    else -> getStatsCardHeader(viewType, columnIndex)
                }
        )
    }

    private fun isShootoutResult() : Boolean {
        return event?.teams?.get(0)?.isShootoutResult ?: false
    }

    final override fun getColumnLongestString(position: Int, viewType: Int): String {
        return when (viewType) {
            SCORE_SUMMARY_UNIQUE_VIEW_TYPE -> {
                event?.teams?.let { teamList ->
                    val columnIndex = position / getRowCount(viewType)
                    return when (teamList.size) {
                        0 -> ""
                        1 -> {
                            val team0 = event?.teams?.get(0)
                            if (columnIndex >= team0?.linescores?.size ?: 0) {
                                team0?.linescoreTotals?.runs?.toString() ?: ""
                            } else {
                                team0?.linescores?.get(columnIndex)?.inning?.toString() ?: ""
                            }
                        }
                        else -> {
                            val team0 = event?.teams?.get(0)
                            val team1 = event?.teams?.get(1)

                            if (columnIndex >= team0?.linescores?.size ?: 0 || columnIndex >= team1?.linescores?.size ?: 0) {
                                // total
                                getLongerString(
                                        team0?.linescoreTotals?.runs?.toString() ?: "",
                                        team1?.linescoreTotals?.runs?.toString() ?: ""
                                )
                            } else {
                                getLongerString(
                                        team0?.linescores?.get(columnIndex)?.inning?.toString()
                                                ?: "",
                                        team1?.linescores?.get(columnIndex)?.inning?.toString()
                                                ?: ""
                                )
                            }
                        }
                    }
                }

                // no teams, so return empty string
                return ""
            }
            else -> getBoxScore()?.let { getStatsCardColumnLongestString(it, position, viewType) }
        }?.plus("4") ?: ""
        // 4 is a relatively wide number, use it to add a bit more horizontal buffer when calculating
        // text width
    }

    open fun getSummaryTotalHeader(): String {
        return "T"
    }

    open fun getShootoutGoalsHeader(): String {
        return "SO"
    }

    /**
     * This value does not include overtime or total.
     */
    open fun getSummaryRegularColumnCount(): Int {
        return 4
    }

    open fun getSummaryOvertimeHeader(overtimeIndex: Int): String {
        var headerString = "OT"
        if (LocalizationManager.isInitialized()){
            headerString = LocalizationManager.DataMenu.BoxscoreLinescoreOverTime
        }
        return if (overtimeIndex == 1) headerString else "$overtimeIndex$headerString"
    }

    open fun getPeriodDetails(): List<PeriodDetail>? {
        return listOf()
    }

    open fun getShootoutGoalsColumnCount(): Int {
        return 0
    }

    protected open fun bindSummaryStats(view: DataMenuContract.ScoreStatsView, lineScore: Linescore) {
        view.bind(lineScore.score)
    }

    protected open fun bindSummaryTotal(view: DataMenuContract.ScoreStatsView, statsTeam: StatsTeam) {
        view.bind(statsTeam.score ?: 0)
    }

    protected open fun bindSummaryShootoutGoals(view: DataMenuContract.ScoreStatsView, statsTeam: StatsTeam) {
        view.bind(0)
    }

    protected fun getBoxScore(): T? {
        val currentTeamId = event?.teams?.let { currentActiveTab.getTeam(it) }?.teamId ?: -1
        return event?.boxscores?.firstOrNull { (it as? BoxScoreData)?.teamId == currentTeamId }
    }

    private fun getLongerString(s1: String, s2: String): String {
        return if (s2.length > s1.length) {
            s2
        } else {
            s1
        }
    }

    protected fun <T> getLongestString(candidates: Array<T>, getValue: (T) -> String, header: String): String? {
        return candidates.maxBy { getValue(it).length }
                ?.let { getValue(it) }
                ?.let { getLongerString(it, header) }
    }

    // region Abstract methods
    // counts
    protected abstract fun getStatsCardCount(): Int
    protected abstract fun getStatsCardColumnCount(viewType: Int): Int

    // bind
    /**
     * @param rowIndex 0-based row index. This value excluded the header already.
     */
    protected abstract fun bindStats(view: DataMenuContract.ScoreStatsView, rowIndex: Int, columnIndex: Int, boxScore: T, viewType: Int)

    // get info
    protected abstract fun getPlayers(boxScore: T, viewType: Int): Array<StatsPlayer>?
    protected abstract fun getStatsCardHeader(viewType: Int, columnIndex: Int): String
    protected abstract fun getStatsCardColumnLongestString(boxScore: T, position: Int, viewType: Int): String?
    abstract fun getCardTitle(viewType: Int): String

    // sort
    protected abstract fun sortData(boxScore: T)
    // endregion
}

enum class DataMenuScoreListTabs(val getTeam: (Array<StatsTeam>) -> StatsTeam?) {
    AWAY({ teams -> teams.firstOrNull { it.isAway() } }),
    HOME({ teams -> teams.firstOrNull { it.isHome() } })
}