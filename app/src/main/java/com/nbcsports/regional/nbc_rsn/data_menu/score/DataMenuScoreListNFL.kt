package com.nbcsports.regional.nbc_rsn.data_menu.score

import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuDataUtils

import com.nbcsports.regional.nbc_rsn.data_menu.models.*
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager

class DataMenuScoreAdapterNFL : DataMenuScoreAdapter<BoxScoreNFL>(DataMenuScoreListPresenterNFL())

class DataMenuScoreListPresenterNFL : DataMenuScoreListPresenter<BoxScoreNFL>() {

    private enum class PassingColumn(override val localizationName: () -> String,
                                     val getValue: (NFLStatsPassing)->String) : NamedEnum {
        C_ATT({ LocalizationManager.DataMenu.BoxscoreNFLAttemptsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.attempts, stats.completions) }),
        YDS({ LocalizationManager.DataMenu.BoxscoreNFLYardsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.yards) }),
        TD({ LocalizationManager.DataMenu.BoxscoreNFLTouchdownsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.touchdowns) }),
        INT({ LocalizationManager.DataMenu.BoxscoreNFLInterceptionsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.interceptions) })
    }

    private enum class RushingColumn(override val localizationName: () -> String,
                                     val getValue: (NFLStatsRushing)->String) : NamedEnum {
        CAR({ LocalizationManager.DataMenu.BoxscoreNFLCarriesAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.attempts) }),
        YDS({ LocalizationManager.DataMenu.BoxscoreNFLYardsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.yards) }),
        AVG({ LocalizationManager.DataMenu.BoxscoreNFLAverageAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.yardsPerAttempt) }),
        TD({ LocalizationManager.DataMenu.BoxscoreNFLTouchdownsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.touchdowns) })
    }

    private enum class ReceivingColumn(override val localizationName: () -> String,
                                       val getValue: (NFLStatsReceiving)->String) : NamedEnum {
        REC({ LocalizationManager.DataMenu.BoxscoreNFLReceptionsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.receptions) }),
        YDS({ LocalizationManager.DataMenu.BoxscoreNFLYardsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.yards) }),
        AVG({ LocalizationManager.DataMenu.BoxscoreNFLAverageAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.average) }),
        TD({ LocalizationManager.DataMenu.BoxscoreNFLTouchdownsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.touchdowns) })
    }

    private enum class DefenseColumn(override val localizationName: () -> String,
                                     val getValue: (NFLStatsDefense)->String) : NamedEnum {
        TCK_AST({ LocalizationManager.DataMenu.BoxscoreNFLTacklesAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.tackles, stats.assists) }),
        SAC({ LocalizationManager.DataMenu.BoxscoreNFLSacksAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.sacks) }),
        INT({ LocalizationManager.DataMenu.BoxscoreNFLInterceptionsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.passesDefensed) })
    }

    // shared by KickReturns and PuntReturns
    private enum class ReturnColumn(override val localizationName: () -> String,
                                    val getValue: (NFLStatsReturn)->String) : NamedEnum {
        RET({ LocalizationManager.DataMenu.BoxscoreNFLReturnsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.returns) }),
        YDS({ LocalizationManager.DataMenu.BoxscoreNFLYardsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.yards) }),
        AVG({ LocalizationManager.DataMenu.BoxscoreNFLAverageAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.average) }),
        TD({ LocalizationManager.DataMenu.BoxscoreNFLTouchdownsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.touchdowns) })
    }

    private enum class KickingColumn(override val localizationName: () -> String,
                                     val getValue: (NFLStatsKicking)->String) : NamedEnum {
        FG({ LocalizationManager.DataMenu.BoxscoreNFLFieldGoalsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.made, stats.attempts) }),
        PCT({ LocalizationManager.DataMenu.BoxscoreNFLPercentAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.percent) }),
        LNG({ LocalizationManager.DataMenu.BoxscoreNFLLongestAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.long) }),
        /**
         * This XP value is obtained from patStats instead of fieldGoalStats even thought they data
         * type is the same
         */
        XP({ LocalizationManager.DataMenu.BoxscoreNFLExtraPointAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.made, stats.attempts) })
    }

    private enum class PuntingColumn(override val localizationName: () -> String,
                                     val getValue: (NFLStatsPunting)->String) : NamedEnum {
        NO({ LocalizationManager.DataMenu.BoxscoreNFLNumberAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.punts) }),
        YDS({ LocalizationManager.DataMenu.BoxscoreNFLYardsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.returnYards) }),
        AVG({ LocalizationManager.DataMenu.BoxscoreNFLAverageAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.average) }),
        IN20({ LocalizationManager.DataMenu.BoxscoreNFLIn20Abbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.in20) })
    }


    private enum class CardType(
            override val localizationName: () -> String,
            val getPlayers: (BoxScoreNFL) -> Array<StatsPlayer>,
            val columnCount: Int,
            val bindValue: (BoxScoreNFL, DataMenuContract.ScoreStatsView, Int, Int) -> Unit
    ) : NamedEnum {
        PASSING(
                { LocalizationManager.DataMenu.BoxscoreNFLPassing },
                {boxScore -> boxScore.playerStats.passingStats.map { it.player }.toTypedArray() },
                PassingColumn.values().size,
                {boxScore, view, columnInd, rowInd -> view.bind(PassingColumn.values()[columnInd].getValue(boxScore.playerStats.passingStats[rowInd])) }
        ),
        RUSHING(
                { LocalizationManager.DataMenu.BoxscoreNFLRushing },
                {boxScore -> boxScore.playerStats.rushingStats.map { it.player }.toTypedArray() },
                RushingColumn.values().size,
                {boxScore, view, columnInd, rowInd -> view.bind(RushingColumn.values()[columnInd].getValue(boxScore.playerStats.rushingStats[rowInd])) }
        ),
        RECEIVING(
                { LocalizationManager.DataMenu.BoxscoreNFLReceiving },
                {boxScore -> boxScore.playerStats.receivingStats.map { it.player }.toTypedArray() },
                ReceivingColumn.values().size,
                {boxScore, view, columnInd, rowInd -> view.bind(ReceivingColumn.values()[columnInd].getValue(boxScore.playerStats.receivingStats[rowInd])) }
        ),
        DEFENSE(
                { LocalizationManager.DataMenu.BoxscoreNFLDefense },
                {boxScore -> boxScore.playerStats.defenseStats.map { it.player }.toTypedArray() },
                DefenseColumn.values().size,
                {boxScore, view, columnInd, rowInd -> view.bind(DefenseColumn.values()[columnInd].getValue(boxScore.playerStats.defenseStats[rowInd])) }
        ),
        KICK_RETURNS(
                { LocalizationManager.DataMenu.BoxscoreNFLKickReturns },
                {boxScore -> boxScore.playerStats.kickReturnStats.map { it.player }.toTypedArray() },
                ReturnColumn.values().size,
                {boxScore, view, columnInd, rowInd -> view.bind(ReturnColumn.values()[columnInd].getValue(boxScore.playerStats.kickReturnStats[rowInd])) }
        ),
        PUNT_RETURNS(
                { LocalizationManager.DataMenu.BoxscoreNFLPuntReturns },
                {boxScore -> boxScore.playerStats.puntReturnStats.map { it.player }.toTypedArray() },
                ReturnColumn.values().size,
                {boxScore, view, columnInd, rowInd -> view.bind(ReturnColumn.values()[columnInd].getValue(boxScore.playerStats.puntReturnStats[rowInd])) }
        ),
        KICKING(
                { LocalizationManager.DataMenu.BoxscoreNFLKicking },
                {boxScore -> boxScore.playerStats.fieldGoalStats.map { it.player }.toTypedArray() },
                KickingColumn.values().size,
                {boxScore, view, columnInd, rowInd ->
                    if (columnInd != KickingColumn.XP.ordinal) {
                        view.bind(KickingColumn.values()[columnInd].getValue(boxScore.playerStats.fieldGoalStats[rowInd]))
                    } else {
                        view.bind(KickingColumn.values()[columnInd].getValue(boxScore.playerStats.patStats[rowInd]))
                    }
                }
        ),
        PUNTING(
                { LocalizationManager.DataMenu.BoxscoreNFLPunting },
                {boxScore -> boxScore.playerStats.puntingStats.map { it.player }.toTypedArray() },
                PuntingColumn.values().size,
                {boxScore, view, columnInd, rowInd -> view.bind(PuntingColumn.values()[columnInd].getValue(boxScore.playerStats.puntingStats[rowInd])) }
        )
    }

    override fun getStatsCardCount(): Int {
        return CardType.values().size
    }

    override fun getStatsCardColumnCount(viewType: Int): Int {
        return com.nbcsports.regional.nbc_rsn.extensions.fromInt<CardType>(viewType)?.columnCount ?: 0
    }

    override fun bindStats(view: DataMenuContract.ScoreStatsView, rowIndex: Int, columnIndex: Int, boxScore: BoxScoreNFL, viewType: Int) {
        com.nbcsports.regional.nbc_rsn.extensions.fromInt<CardType>(viewType)?.bindValue?.let { it(boxScore, view, columnIndex, rowIndex) }
    }

    override fun getPlayers(boxScore: BoxScoreNFL, viewType: Int): Array<StatsPlayer>? {
        return com.nbcsports.regional.nbc_rsn.extensions.fromInt<CardType>(viewType)?.getPlayers?.let { it(boxScore) }
    }

    override fun getStatsCardHeader(viewType: Int, columnIndex: Int): String {
        return when (viewType) {
            CardType.PASSING.ordinal -> NamedEnum.getName(PassingColumn.values()[columnIndex]).replace("_", "/")
            CardType.RUSHING.ordinal -> NamedEnum.getName(RushingColumn.values()[columnIndex])
            CardType.RECEIVING.ordinal -> NamedEnum.getName(ReceivingColumn.values()[columnIndex])
            CardType.DEFENSE.ordinal -> NamedEnum.getName(DefenseColumn.values()[columnIndex]).replace("_", "/")
            CardType.KICK_RETURNS.ordinal, CardType.PUNT_RETURNS.ordinal -> NamedEnum.getName(ReturnColumn.values()[columnIndex])
            CardType.KICKING.ordinal -> NamedEnum.getName(KickingColumn.values()[columnIndex])
            CardType.PUNTING.ordinal -> NamedEnum.getName(PuntingColumn.values()[columnIndex])
            else -> null
        } ?: ""
    }

    override fun getStatsCardColumnLongestString(boxScore: BoxScoreNFL, position: Int, viewType: Int): String? {
        return when (viewType) {
            CardType.PASSING.ordinal -> {
                boxScore.playerStats.passingStats.let { stats ->
                    // add the column header row
                    val column = PassingColumn.values()[position / (stats.size + 1)]
                    getLongestString(stats, column.getValue, NamedEnum.getName(column))
                }

            }
            CardType.RUSHING.ordinal -> {
                boxScore.playerStats.rushingStats.let { stats ->
                    // add the column header row
                    val column = RushingColumn.values()[position / (stats.size + 1)]
                    getLongestString(stats, column.getValue, NamedEnum.getName(column))
                }
            }
            CardType.RECEIVING.ordinal -> {
                boxScore.playerStats.receivingStats.let { stats ->
                    // add the column header row
                    val column = ReceivingColumn.values()[position / (stats.size + 1)]
                    getLongestString(stats, column.getValue, NamedEnum.getName(column))
                }
            }
            CardType.DEFENSE.ordinal -> {
                boxScore.playerStats.defenseStats.let { stats ->
                    // add the column header row
                    val column = DefenseColumn.values()[position / (stats.size + 1)]
                    getLongestString(stats, column.getValue, NamedEnum.getName(column))
                }
            }
            CardType.KICK_RETURNS.ordinal -> {
                boxScore.playerStats.kickReturnStats.let { stats ->
                    // add the column header row
                    val column = ReturnColumn.values()[position / (stats.size + 1)]
                    getLongestString(stats, column.getValue, NamedEnum.getName(column))
                }
            }
            CardType.PUNT_RETURNS.ordinal -> {
                boxScore.playerStats.puntReturnStats.let { stats ->
                    // add the column header row
                    val column = ReturnColumn.values()[position / (stats.size + 1)]
                    getLongestString(stats, column.getValue, NamedEnum.getName(column))
                }
            }
            CardType.KICKING.ordinal -> {
                boxScore.playerStats.fieldGoalStats.let { stats ->
                    // add the column header row
                    val column = KickingColumn.values()[position / (stats.size + 1)]
                    if (column == KickingColumn.XP) {
                        getLongestString(boxScore.playerStats.patStats, KickingColumn.XP.getValue, NamedEnum.getName(KickingColumn.XP))
                    } else {
                        getLongestString(stats, column.getValue, NamedEnum.getName(column))
                    }
                }
            }
            CardType.PUNTING.ordinal -> {
                boxScore.playerStats.puntingStats.let { stats ->
                    // add the column header row
                    val column = PuntingColumn.values()[position / (stats.size + 1)]
                    getLongestString(stats, column.getValue, NamedEnum.getName(column))
                }
            }
            else -> null
        }
    }


    override fun getCardTitle(viewType: Int): String {
        return if (viewType >= 0 && viewType < CardType.values().size) {
            NamedEnum.getName(CardType.values()[viewType]).replace("_", " ")
        } else ""
    }

    override fun sortData(boxScore: BoxScoreNFL) {
        boxScore.playerStats.passingStats.sort()
        boxScore.playerStats.rushingStats.sort()
        boxScore.playerStats.receivingStats.sort()
        boxScore.playerStats.defenseStats.sort()
        boxScore.playerStats.kickReturnStats.sort()
        boxScore.playerStats.puntingStats.sort()
        boxScore.playerStats.puntReturnStats.sort()
        boxScore.playerStats.patStats.sort()
        boxScore.playerStats.fieldGoalStats.sortByDescending {
            boxScore.playerStats.patStats.firstOrNull { reference ->
                reference.player.playerId == it.player.playerId
            }?.made ?: -1
        }
    }
}