package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

// Arrays here should have one placeholder object by default, so we can still show the
// card with '-' indicating no data available
@Parcelize
data class BoxScoreMLB(
        override val teamId: Int = -1,
        val playerBattingStats: Array<MLBBattingStats> = Array(1) { MLBBattingStats() },
        val playerPitchingStats: Array<MLBPitchingStats> = Array(1) { MLBPitchingStats() }
) : BoxScoreData {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoxScoreMLB

        if (teamId != other.teamId) return false
        if (!Arrays.equals(playerBattingStats, other.playerBattingStats)) return false
        if (!Arrays.equals(playerPitchingStats, other.playerPitchingStats)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = teamId
        result = 31 * result + Arrays.hashCode(playerBattingStats)
        result = 31 * result + Arrays.hashCode(playerPitchingStats)
        return result
    }
}

@Parcelize
data class MLBBattingStats(
        val player: StatsPlayer = StatsPlayer(),
        val battingSlot: Int = -1,
        val atBats: MLBStatsDetail = MLBStatsDetail(),
        val runs: MLBStatsDetail = MLBStatsDetail(),
        val hits: MLBStatsDetail = MLBStatsDetail(),
        val walks: MLBStatsDetail = MLBStatsDetail(),
        val runsBattedIn: MLBStatsDetail = MLBStatsDetail()
) : Parcelable

@Parcelize
data class MLBPitchingStats(
        val player: StatsPlayer = StatsPlayer(),
        val sequence: Int = -1,
        val inningsPitched: MLBStatsDetailString = MLBStatsDetailString(),
        val earnedRuns: MLBStatsDetail = MLBStatsDetail(),
        val hits: MLBStatsDetail = MLBStatsDetail(),
        val walks: MLBStatsDetail = MLBStatsDetail(),
        val strikeouts: MLBStatsDetail = MLBStatsDetail()
) : Parcelable

@Parcelize
data class MLBStatsDetail(
        val game: Int = -1,
        val season: Int = -1
) : Parcelable

@Parcelize
data class MLBStatsDetailString(
        val game: String = "",
        val season: String = ""
) : Parcelable