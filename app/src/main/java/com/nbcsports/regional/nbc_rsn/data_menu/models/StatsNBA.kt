package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class StatsNBATeamStats(
        val teamOwnFlag: Boolean,
        val pointsPerGame: String,
        val fieldGoals: StatsNBATeamFieldGoal,
        val threePointFieldGoals: StatsNBATeamFieldGoal,
        val effectiveFieldGoalPercentage: String
) : Parcelable

@Parcelize
data class StatsNBATeamFieldGoal(
        val percentage: String
) : Parcelable

// Array here should have one placeholder object by default, so we can still show the
// card with '-' indicating no data available
@Parcelize
data class BoxScoreNBA(
        override val teamId: Int = 0,
        val playerStats: Array<PlayerStatsNBA> = Array(1) { PlayerStatsNBA() }
) : BoxScoreData {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoxScoreNBA

        if (teamId != other.teamId) return false
        if (!Arrays.equals(playerStats, other.playerStats)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = teamId
        result = 31 * result + Arrays.hashCode(playerStats)
        return result
    }
}

@Parcelize
data class PlayerStatsNBA(
        val player: StatsPlayer = StatsPlayer(),
        val startingPosition: StatsPlayerPosition = StatsPlayerPosition(),
        val minutesPlayed: Int = -1,
        val rebounds: StatsTotal = StatsTotal(),
        val assists: Int = -1,
        val points: Int = -1
) : Parcelable, Comparable<PlayerStatsNBA> {
    override fun compareTo(other: PlayerStatsNBA): Int {
        // first compare points, descending order
        return (other.points - points).let {
            // if tie, then compare name, alphabetic order
            if (it == 0) player.compareTo(other.player) else it
        }
    }
}
