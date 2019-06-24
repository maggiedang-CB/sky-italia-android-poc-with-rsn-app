package com.nbcsports.regional.nbc_rsn.data_menu.score

import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.models.BoxScoreNBA
import com.nbcsports.regional.nbc_rsn.data_menu.models.PlayerStatsNBA
import com.nbcsports.regional.nbc_rsn.data_menu.models.StatsPlayer
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuDataUtils
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager

class DataMenuScoreAdapterNBA : DataMenuScoreAdapter<BoxScoreNBA>(DataMenuScoreListPresenterNBA())

class DataMenuScoreListPresenterNBA : DataMenuScoreListPresenter<BoxScoreNBA>() {

    private enum class CardType(override val localizationName: () -> String) : NamedEnum{
        PLAYERS({ LocalizationManager.DataMenu.BoxscoreNBAPlayers })
    }

    private enum class PlayerColumn(override val localizationName: () -> String,
                                    val getValue: (PlayerStatsNBA)->String) : NamedEnum {
        MIN({ LocalizationManager.DataMenu.BoxscoreNBAMinutesAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.minutesPlayed) }),
        REB({ LocalizationManager.DataMenu.BoxscoreNBAReboundsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.rebounds.total) }),
        AST({ LocalizationManager.DataMenu.BoxscoreNBAAssistsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.assists) }),
        PTS({ LocalizationManager.DataMenu.BoxscorePointsAbbreviation },
                { stats -> DataMenuDataUtils.getScoreString(stats.points) })
    }

    override fun getStatsCardCount(): Int {
        return CardType.values().size
    }

    override fun getPlayers(boxScore: BoxScoreNBA, viewType: Int): Array<StatsPlayer>? {
        return boxScore.playerStats.map { it.player }.toTypedArray()
    }

    override fun getCardTitle(viewType: Int): String {
        return NamedEnum.getName(CardType.PLAYERS)
    }

    override fun getStatsCardColumnCount(viewType: Int): Int {
        return PlayerColumn.values().size
    }

    override fun bindStats(view: DataMenuContract.ScoreStatsView, rowIndex: Int, columnIndex: Int, boxScore: BoxScoreNBA, viewType: Int) {
        view.bind(PlayerColumn.values()[columnIndex].getValue(boxScore.playerStats[rowIndex]))
    }

    override fun getStatsCardHeader(viewType: Int, columnIndex: Int): String {
        return NamedEnum.getName(PlayerColumn.values()[columnIndex])
    }

    override fun getStatsCardColumnLongestString(boxScore: BoxScoreNBA, position: Int, viewType: Int): String? {
        return boxScore.playerStats.let { stats ->
            // add the column header row
            val column = PlayerColumn.values()[position / (stats.size + 1)]
            getLongestString(stats, column.getValue, NamedEnum.getName(column))
        }
    }

    override fun sortData(boxScore: BoxScoreNBA) {
        boxScore.playerStats.sort()
    }

}
