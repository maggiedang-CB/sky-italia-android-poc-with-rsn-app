package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class StatsNHLTeamStats(
        val gamesPlayed: Int,
        val goals: StatsNHLTeamGoal,
        val shotsAgainst: Int,
        val savePercentage: String,
        val powerPlayOpportunities: Int
) : Parcelable

@Parcelize
data class StatsNHLTeamGoal(
        val powerPlay: Int
) : Parcelable

// Arrays here should have one placeholder object by default, so we can still show the
// card with '-' indicating no data available
@Parcelize
data class BoxScoreNHL(
        override val teamId: Int = -1,
        val playerSkaterStats: Array<SkaterStats> = Array(1) { SkaterStats() },
        val playerGoaltenderStats: Array<GoalieStats> = Array(1) { GoalieStats() }
) : BoxScoreData {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoxScoreNHL

        if (teamId != other.teamId) return false
        if (!Arrays.equals(playerSkaterStats, other.playerSkaterStats)) return false
        if (!Arrays.equals(playerGoaltenderStats, other.playerGoaltenderStats)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = teamId
        result = 31 * result + Arrays.hashCode(playerSkaterStats)
        result = 31 * result + Arrays.hashCode(playerGoaltenderStats)
        return result
    }
}

@Parcelize
data class SkaterStats(
        val player: StatsPlayer = StatsPlayer(),
        val goals: StatsTotal = StatsTotal(),
        val assists: StatsTotal = StatsTotal(),
        val shotsOnGoal: Int = -1
) : Parcelable, Comparable<SkaterStats> {
    val points: Int
        get() = goals.total + assists.total

    override fun compareTo(other: SkaterStats): Int {
        return (other.points - points).let {
            if (it == 0) player.compareTo(other.player) else it
        }
    }
}

@Parcelize
data class GoalieStats(
        val player: StatsPlayer = StatsPlayer(),
        val goalsAgainst: StatsGoalAgainst = StatsGoalAgainst(),
        val shotsAgainst: Int = -1,
        val saves: Int = -1
) : Parcelable

@Parcelize
data class StatsGoalAgainst(
        val totalAgainst: Int = -1
) : Parcelable

@Parcelize
data class PeriodDetail(
        val period: Int = -1,
        val goals: Array<PeriodDetailGoal> = emptyArray()
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PeriodDetail

        if (period != other.period) return false
        if (!Arrays.equals(goals, other.goals)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = period
        result = 31 * result + Arrays.hashCode(goals)
        return result
    }
}

@Parcelize
data class PeriodDetailGoal(
        val player: StatsPlayer = StatsPlayer(),
        val teamId: Int = -1,
        val time: PeriodDetailGoalTime? = null,
        val goalNumber: PeriodDetailGoalNumber? = null,
        val strength: PeriodDetailGoalStrength? = null,
        val assists: Array<PeriodDetailGoalAssist> = emptyArray(),
        val currentScore: PeriodDetailGoalCurrentScore? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PeriodDetailGoal

        if (player != other.player) return false
        if (!Arrays.equals(assists, other.assists)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = player.hashCode()
        result = 31 * result + Arrays.hashCode(assists)
        return result
    }
}

@Parcelize
data class PeriodDetailGoalAssist(
        val player: StatsPlayer = StatsPlayer(),
        val game: Int = -1,
        val season: Int = -1
) : Parcelable

@Parcelize
data class PeriodDetailGoalTime(
        val minutes: Int = -1,
        val seconds: Int = -1
) : Parcelable

@Parcelize
data class PeriodDetailGoalNumber(
        val game: Int = -1,
        val season: Int = -1
) : Parcelable

@Parcelize
data class PeriodDetailGoalStrength(
        val strengthId: Int = -1,
        val name: String = ""
) : Parcelable

@Parcelize
data class PeriodDetailGoalCurrentScore(
        val homeScore: Int = -1,
        val awayScore: Int = -1
) : Parcelable
