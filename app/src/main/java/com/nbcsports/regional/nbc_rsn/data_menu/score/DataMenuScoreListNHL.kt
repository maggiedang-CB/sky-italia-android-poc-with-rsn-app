package com.nbcsports.regional.nbc_rsn.data_menu.score

import com.nbcsports.regional.nbc_rsn.data_bar.StatsTeam
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuDataUtils
import com.nbcsports.regional.nbc_rsn.data_menu.models.*
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager

class DataMenuScoreAdapterNHL : DataMenuScoreAdapter<BoxScoreNHL>(DataMenuScoreListPresenterNHL()) {

    // the score summary card at the bottom
    override fun getCustomCardCount(): Int {
        return 1
    }

}

class DataMenuScoreListPresenterNHL : DataMenuScoreListPresenter<BoxScoreNHL>() {

    private enum class CardType(override val localizationName: () -> String) : NamedEnum {
        SKATERS ({ LocalizationManager.DataMenu.BoxscoreNHLSkaters }),
        GOALIE ({ LocalizationManager.DataMenu.BoxscoreNHLGoalie });
    }

    private enum class SkaterColumn(override val localizationName: () -> String,
                                    val getValue: (SkaterStats)->String) : NamedEnum {
        G({ LocalizationManager.DataMenu.BoxscoreNHLGoalsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.goals.total) }),
        A({ LocalizationManager.DataMenu.BoxscoreNHLAssistsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.assists.total) }),
        PTS({ LocalizationManager.DataMenu.BoxscorePointsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.points) }),
        SOG({ LocalizationManager.DataMenu.BoxscoreNHLShotsOnGoalAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.shotsOnGoal) })
    }

    private enum class GoalieColumn(override val localizationName: () -> String,
                                    val getValue: (GoalieStats)->String) : NamedEnum {
        GA({ LocalizationManager.DataMenu.BoxscoreNHLGoalsAgainstAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.goalsAgainst.totalAgainst) }),
        SA({ LocalizationManager.DataMenu.BoxscoreNHLShotsAgainstAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.shotsAgainst) }),
        SV({ LocalizationManager.DataMenu.BoxscoreNHLSavesAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.saves) }),
        SV_P({ LocalizationManager.DataMenu.BoxscoreNHLSavePercentAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.saves.toFloat() / stats.shotsAgainst, DataMenuDataUtils.decimalFormatThree) })
    }

    override fun getStatsCardCount(): Int {
        return CardType.values().size
    }

    override fun getStatsCardColumnCount(viewType: Int): Int {
        return when (viewType) {
            CardType.SKATERS.ordinal -> SkaterColumn.values().size
            CardType.GOALIE.ordinal -> GoalieColumn.values().size
            else -> 0
        }
    }

    override fun bindStats(view: DataMenuContract.ScoreStatsView, rowIndex: Int, columnIndex: Int, boxScore: BoxScoreNHL, viewType: Int) {
        when (viewType) {
            CardType.SKATERS.ordinal -> {
                view.bind(SkaterColumn.values()[columnIndex].getValue(boxScore.playerSkaterStats[rowIndex]))
            }
            CardType.GOALIE.ordinal -> {
                view.bind(GoalieColumn.values()[columnIndex].getValue(boxScore.playerGoaltenderStats[rowIndex]))
            }
        }
    }

    override fun getPlayers(boxScore: BoxScoreNHL, viewType: Int): Array<StatsPlayer>? {
        return when (viewType) {
            CardType.SKATERS.ordinal -> boxScore.playerSkaterStats.map { it.player }.toTypedArray()
            CardType.GOALIE.ordinal -> boxScore.playerGoaltenderStats.map { it.player }.toTypedArray()
            else -> null
        }
    }

    override fun getStatsCardHeader(viewType: Int, columnIndex: Int): String {
        return when (viewType) {
            CardType.SKATERS.ordinal -> NamedEnum.getName(SkaterColumn.values()[columnIndex])
            CardType.GOALIE.ordinal ->  {
                if (columnIndex == GoalieColumn.SV_P.ordinal) {
                    "SV%"
                } else {
                    NamedEnum.getName(GoalieColumn.values()[columnIndex])
                }
            }
            else -> null
        } ?: "-"
    }

    override fun getStatsCardColumnLongestString(boxScore: BoxScoreNHL, position: Int, viewType: Int): String? {
        return when (viewType) {
            CardType.SKATERS.ordinal -> {
                boxScore.playerSkaterStats.let { stats ->
                    // add the column header row
                    val column = SkaterColumn.values()[position / (stats.size + 1)]
                    getLongestString(stats, column.getValue, NamedEnum.getName(column))
                }
            }
            CardType.GOALIE.ordinal -> {
                boxScore.playerGoaltenderStats.let { stats ->
                    // add the column header row
                    val column = GoalieColumn.values()[position / (stats.size + 1)]
                    getLongestString(stats, column.getValue, NamedEnum.getName(column))
                }
            }
            else -> null
        }
    }


    override fun getCardTitle(viewType: Int): String {
        return if (viewType >= 0 && viewType < CardType.values().size) {
            NamedEnum.getName(CardType.values()[viewType])
        } else ""
    }

    override fun getSummaryRegularColumnCount(): Int {
        return 3
    }

    override fun sortData(boxScore: BoxScoreNHL) {
        boxScore.playerSkaterStats.sort()
    }

    override fun getPeriodDetails(): List<PeriodDetail>? {
        val fullPeriodDetailsList = getFullPeriodDetails()
        val currentTeamId = getBoxScore()?.teamId ?: return emptyList()
        val resultList = mutableListOf<PeriodDetail>()

        fullPeriodDetailsList.forEach { periodDetail ->
            // compose a new detail object if exists any goal from current team
            periodDetail.goals.filter { it.teamId == currentTeamId }.run {
                if (isNotEmpty())
                    resultList.add(PeriodDetail(periodDetail.period, toTypedArray()))
            }
        }

        return resultList
    }

    override fun getScoringSummaryPeriodDetailsList(): List<PeriodDetail> {
        return getFullPeriodDetails().filter { it.goals.isNotEmpty() }
    }

    override fun getShootoutGoalsHeader(): String {
        return if (LocalizationManager.isInitialized()) {
            LocalizationManager.DataMenu.BoxscoreLinescoreShootOut
        } else "SO"
    }

    override fun getShootoutGoalsColumnCount(): Int {
        return if (getHighestShootoutGoals() > 0) {
            1
        } else 0
    }

    override fun bindSummaryShootoutGoals(view: DataMenuContract.ScoreStatsView, statsTeam: StatsTeam) {
        view.bind(when (statsTeam.teamId) {
            getHighestShootoutGoalsTeamId() -> "1"
            else -> "0"
        })
    }
}