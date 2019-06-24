package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class StatsNFLTeamStats(
        val teamOwnFlag: Boolean,
        val gamesPlayed: Int,
        val redZoneEfficiency: StatsNFLTeamEfficiency,
        val thirdDownEfficiency: StatsNFLTeamEfficiency
) : Parcelable

@Parcelize
data class StatsNFLTeamEfficiency (
        val made: Int,
        val attempts: Int,
        val percentage: String
): Parcelable

@Parcelize
data class BoxScoreNFL(
        override val teamId: Int = -1,
        val playerStats: PlayerStatsNFL = PlayerStatsNFL()
) : BoxScoreData

// Arrays here should have one placeholder object by default, so we can still show the
// card with '-' indicating no data available
@Parcelize
data class PlayerStatsNFL(
        var passingStats: Array<NFLStatsPassing> = Array(1) { NFLStatsPassing() },
        var rushingStats: Array<NFLStatsRushing> = Array(1) { NFLStatsRushing() },
        var receivingStats: Array<NFLStatsReceiving> = Array(1) { NFLStatsReceiving() },
        var defenseStats: Array<NFLStatsDefense> = Array(1) { NFLStatsDefense() },
        var kickReturnStats: Array<NFLStatsReturn> = Array(1) { NFLStatsReturn() },
        var puntReturnStats: Array<NFLStatsReturn> = Array(1) { NFLStatsReturn() },
        var fieldGoalStats: Array<NFLStatsKicking> = Array(1) { NFLStatsKicking() },
        // the XP value in 'kicking' will come from here
        var patStats: Array<NFLStatsKicking> = Array(1) { NFLStatsKicking() },
        var puntingStats: Array<NFLStatsPunting> = Array(1) { NFLStatsPunting() }
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerStatsNFL

        if (!Arrays.equals(passingStats, other.passingStats)) return false
        if (!Arrays.equals(rushingStats, other.rushingStats)) return false
        if (!Arrays.equals(receivingStats, other.receivingStats)) return false
        if (!Arrays.equals(defenseStats, other.defenseStats)) return false
        if (!Arrays.equals(kickReturnStats, other.kickReturnStats)) return false
        if (!Arrays.equals(puntReturnStats, other.puntReturnStats)) return false
        if (!Arrays.equals(fieldGoalStats, other.fieldGoalStats)) return false
        if (!Arrays.equals(puntingStats, other.puntingStats)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(passingStats)
        result = 31 * result + Arrays.hashCode(rushingStats)
        result = 31 * result + Arrays.hashCode(receivingStats)
        result = 31 * result + Arrays.hashCode(defenseStats)
        result = 31 * result + Arrays.hashCode(kickReturnStats)
        result = 31 * result + Arrays.hashCode(puntReturnStats)
        result = 31 * result + Arrays.hashCode(fieldGoalStats)
        result = 31 * result + Arrays.hashCode(puntingStats)
        return result
    }
}

@Parcelize
data class NFLStatsPassing(
        val player: StatsPlayer = StatsPlayer(),
        val completions: Int = -1,
        val attempts: Int = -1,
        val yards: Int = -1,
        val touchdowns: Int = -1,
        val interceptions: Int = -1
) : Parcelable, Comparable<NFLStatsPassing> {
    override fun compareTo(other: NFLStatsPassing): Int {
        return (other.yards - yards).let {
            if (it == 0) player.compareTo(other.player) else it
        }
    }
}

@Parcelize
data class NFLStatsRushing(
        val player: StatsPlayer = StatsPlayer(),
        val attempts: Int = -1,
        val yards: Int = -1,
        val touchdowns: Int = -1,
        val yardsPerAttempt: String = ""
) : Parcelable, Comparable<NFLStatsRushing> {
    override fun compareTo(other: NFLStatsRushing): Int {
        return (other.yards - yards).let {
            if (it == 0) player.compareTo(other.player) else it
        }
    }
}

@Parcelize
data class NFLStatsReceiving(
        val player: StatsPlayer = StatsPlayer(),
        val receptions: Int = -1,
        val yards: Int = -1,
        val touchdowns: Int = -1
) : Parcelable, Comparable<NFLStatsReceiving> {
    override fun compareTo(other: NFLStatsReceiving): Int {
        return (other.yards - yards).let {
            if (it == 0) player.compareTo(other.player) else it
        }
    }

    val average: Float
        get() {
            return if (yards >= 0 && receptions > 0) {
                yards.toFloat() / receptions
            } else -1f
        }
}

@Parcelize
data class NFLStatsDefense(
        val player: StatsPlayer = StatsPlayer(),
        val tackles: Int = -1,
        val assists: Int = -1,
        val sacks: Int = -1,
        val passesDefensed: Int = -1
) : Parcelable, Comparable<NFLStatsDefense> {
    override fun compareTo(other: NFLStatsDefense): Int {
        return (other.tackles - tackles).let {
            if (it == 0) player.compareTo(other.player) else it
        }
    }
}

@Parcelize
data class NFLStatsReturn(
        val player: StatsPlayer = StatsPlayer(),
        val returns: Int = -1,
        val yards: Int = -1,
        val touchdowns: Int = -1
) : Parcelable, Comparable<NFLStatsReturn> {
    override fun compareTo(other: NFLStatsReturn): Int {
        return (other.yards - yards).let {
            if (it == 0) player.compareTo(other.player) else it
        }
    }

    val average: Float
        get() {
            return if (yards >= 0 && returns > 0) {
                yards.toFloat() / returns
            } else -1f
        }
}

@Parcelize
data class NFLStatsKicking(
        val player: StatsPlayer = StatsPlayer(),
        val made: Int = -1,
        val long: Int = -1,
        val attempts: Int = -1
) : Parcelable, Comparable<NFLStatsKicking> {
    override fun compareTo(other: NFLStatsKicking): Int {
        return (other.made - made).let {
            if (it == 0) player.compareTo(other.player) else it
        }
    }

    val percent: Float
        get() {
            return if (made >= 0 && attempts > 0) {
                made.toFloat() * 100 / attempts
            } else -1f
        }
}

@Parcelize
data class NFLStatsPunting(
        val player: StatsPlayer = StatsPlayer(),
        val punts: Int = -1,
        val returnYards: Int = -1,
        val in20: Int = -1
) : Parcelable, Comparable<NFLStatsPunting> {
    override fun compareTo(other: NFLStatsPunting): Int {
        return (other.returnYards - returnYards).let {
            if (it == 0) player.compareTo(other.player) else it
        }
    }

    val average: Float
        get() {
            return if (returnYards >= 0 && punts > 0) {
                returnYards.toFloat() / punts
            } else -1f
        }
}