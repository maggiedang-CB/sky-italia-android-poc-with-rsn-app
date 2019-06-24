package com.nbcsports.regional.nbc_rsn.data_menu.score

import com.nbcsports.regional.nbc_rsn.data_bar.Linescore
import com.nbcsports.regional.nbc_rsn.data_bar.StatsTeam
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.models.BoxScoreMLB
import com.nbcsports.regional.nbc_rsn.data_menu.models.MLBBattingStats
import com.nbcsports.regional.nbc_rsn.data_menu.models.MLBPitchingStats
import com.nbcsports.regional.nbc_rsn.data_menu.models.StatsPlayer
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuDataUtils
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager

class DataMenuScoreAdapterMLB : DataMenuScoreAdapter<BoxScoreMLB>(DataMenuScoreListPresenterMLB())

class DataMenuScoreListPresenterMLB : DataMenuScoreListPresenter<BoxScoreMLB>() {

    private enum class CardType(override val localizationName: () -> String) : NamedEnum {
        BATTING ({ LocalizationManager.DataMenu.BoxscoreMLBBatting }),
        PITCHING ({ LocalizationManager.DataMenu.BoxscoreMLBPitching })
    }

    private enum class BattingColumn(override val localizationName: () -> String,
                                     val getValue: (MLBBattingStats)->String) : NamedEnum {
        AB({ LocalizationManager.DataMenu.BoxscoreMLBAtBatsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.atBats.game) }),
        R({ LocalizationManager.DataMenu.BoxscoreMLBARunsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.runs.game) }),
        H({ LocalizationManager.DataMenu.BoxscoreMLBAHitsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.hits.game) }),
        BB({ LocalizationManager.DataMenu.BoxscoreMLBABasesOnBallsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.walks.game) }),
        RBI({ LocalizationManager.DataMenu.BoxscoreMLBARunsBattedInAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.runsBattedIn.game) })
    }

    private enum class PitchingColumn(override val localizationName: () -> String,
                                      val getValue: (MLBPitchingStats)->String) : NamedEnum {
        IP({ LocalizationManager.DataMenu.BoxscoreMLBAInningsPitchedAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.inningsPitched.game) }),
        H({ LocalizationManager.DataMenu.BoxscoreMLBAHitsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.hits.game) }),
        ER({ LocalizationManager.DataMenu.BoxscoreMLBAEarnedRunsPitchedAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.earnedRuns.game) }),
        BB({ LocalizationManager.DataMenu.BoxscoreMLBABasesOnBallsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.walks.game) }),
        SO({ LocalizationManager.DataMenu.BoxscoreMLBAStrikeOutsPitchedAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.strikeouts.game) })
    }

    override fun getSummaryTotalHeader(): String {
        return "R"
    }

    override fun getStatsCardCount(): Int {
        return CardType.values().size
    }

    override fun getStatsCardColumnCount(viewType: Int): Int {
        return when (viewType) {
            CardType.BATTING.ordinal -> BattingColumn.values().size
            CardType.PITCHING.ordinal -> PitchingColumn.values().size
            else -> 0
        }
    }

    override fun bindSummaryStats(view: DataMenuContract.ScoreStatsView, lineScore: Linescore) {
        view.bind(lineScore.inning)
    }

    override fun bindSummaryTotal(view: DataMenuContract.ScoreStatsView, statsTeam: StatsTeam) {
        view.bind(statsTeam.linescoreTotals?.runs ?: 0)
    }

    override fun bindStats(view: DataMenuContract.ScoreStatsView, rowIndex: Int, columnIndex: Int, boxScore: BoxScoreMLB, viewType: Int) {
        when (viewType) {
            CardType.BATTING.ordinal -> {
                view.bind(BattingColumn.values()[columnIndex].getValue(boxScore.playerBattingStats[rowIndex]))
            }
            CardType.PITCHING.ordinal -> {
                view.bind(PitchingColumn.values()[columnIndex].getValue(boxScore.playerPitchingStats[rowIndex]))
            }
        }
    }

    override fun getPlayers(boxScore: BoxScoreMLB, viewType: Int): Array<StatsPlayer>? {
        return when (viewType) {
            CardType.BATTING.ordinal -> boxScore.playerBattingStats.map { it.player }.toTypedArray()
            CardType.PITCHING.ordinal -> boxScore.playerPitchingStats.map { it.player }.toTypedArray()
            else -> null
        }
    }

    override fun getStatsCardHeader(viewType: Int, columnIndex: Int): String {
        return when (viewType) {
            CardType.BATTING.ordinal -> NamedEnum.getName(BattingColumn.values()[columnIndex])
            CardType.PITCHING.ordinal -> NamedEnum.getName(PitchingColumn.values()[columnIndex])
            else -> null
        } ?: "-"
    }

    override fun getStatsCardColumnLongestString(boxScore: BoxScoreMLB, position: Int, viewType: Int): String? {
        return when (viewType) {
            CardType.BATTING.ordinal -> {
                boxScore.playerBattingStats.let { stats ->
                    // add the column header row
                    val column = BattingColumn.values()[position / (stats.size + 1)]
                    getLongestString(stats, column.getValue, NamedEnum.getName(column))
                }
            }
            CardType.PITCHING.ordinal -> {
                boxScore.playerPitchingStats.let { stats ->
                    // add the column header row
                    val column = PitchingColumn.values()[position / (stats.size + 1)]
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
        return 9
    }

    override fun sortData(boxScore: BoxScoreMLB) {
        boxScore.playerBattingStats.sortBy { it.battingSlot }
        boxScore.playerPitchingStats.sortBy { it.sequence }
    }
}