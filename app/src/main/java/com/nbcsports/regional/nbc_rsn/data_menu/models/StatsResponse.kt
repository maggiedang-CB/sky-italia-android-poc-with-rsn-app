package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import com.nbcsports.regional.nbc_rsn.data_bar.*
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class StatsResponse<T : Parcelable>(
        val status: String = "",
        val recordCount: Int = -1,
        val startTimestamp: String = "",
        val endTimestamp: String = "",
        val timeTaken: Float = -1f,
        val apiResults: Array<ApiResult<T>> = emptyArray()
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataBarModel

        if (status != other.status) return false
        if (recordCount != other.recordCount) return false
        if (startTimestamp != other.startTimestamp) return false
        if (endTimestamp != other.endTimestamp) return false
        if (timeTaken != other.timeTaken) return false
        if (!Arrays.equals(apiResults, other.apiResults)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + recordCount
        result = 31 * result + startTimestamp.hashCode()
        result = 31 * result + endTimestamp.hashCode()
        result = 31 * result + timeTaken.hashCode()
        result = 31 * result + Arrays.hashCode(apiResults)
        return result
    }
}

@Parcelize
data class ApiResult<T : Parcelable>(
        val sportId: Int = -1,
        val name: String = "",
        val league: StatsLeague<T> = StatsLeague()
) : Parcelable

@Parcelize
data class StatsLeague<T : Parcelable>(
        val leagueId: Int = -1,
        val name: String = "",
        val abbreviation: String = "",
        val displayName: String = "",
        val players: Array<StatsPlayer>? = null,
        // some endpoints return teams, some return seasons
        val teams: Array<StatsTeamWithSeason<T>>? = null,
        val seasons: Array<StatsSeason<T>>? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StatsLeague<*>

        if (leagueId != other.leagueId) return false
        if (name != other.name) return false
        if (abbreviation != other.abbreviation) return false
        if (displayName != other.displayName) return false
        if (players != null) {
            if (other.players == null) return false
            if (!players.contentEquals(other.players)) return false
        } else if (other.players != null) return false
        if (teams != null) {
            if (other.teams == null) return false
            if (!teams.contentEquals(other.teams)) return false
        } else if (other.teams != null) return false
        if (seasons != null) {
            if (other.seasons == null) return false
            if (!seasons.contentEquals(other.seasons)) return false
        } else if (other.seasons != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = leagueId
        result = 31 * result + name.hashCode()
        result = 31 * result + abbreviation.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + (players?.contentHashCode() ?: 0)
        result = 31 * result + (teams?.contentHashCode() ?: 0)
        result = 31 * result + (seasons?.contentHashCode() ?: 0)
        return result
    }
}

@Parcelize
data class StatsTeamWithSeason<T : Parcelable>(
        val teamId: Int = -1,
        val location: String = "",
        val nickname: String = "",
        val abbreviation: String = "",
        val seasons: Array<StatsSeason<T>> = emptyArray()
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StatsTeamWithSeason<*>

        if (teamId != other.teamId) return false
        if (location != other.location) return false
        if (nickname != other.nickname) return false
        if (abbreviation != other.abbreviation) return false
        if (!seasons.contentEquals(other.seasons)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = teamId
        result = 31 * result + location.hashCode()
        result = 31 * result + nickname.hashCode()
        result = 31 * result + abbreviation.hashCode()
        result = 31 * result + seasons.contentHashCode()
        return result
    }
}

@Parcelize
data class StatsSeason<T : Parcelable>(
        val season: Int = -1,
        val name: String = "",
        val isActive: Boolean = false,
        // some endpoints return eventType, some return injuryDetails
        val eventType: Array<StatsEventType<T>>? = null,
        val injuryDetails: Array<StatsInjuryDetail>? = null,
        val injuries: Array<StatsInjuries>? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StatsSeason<*>

        if (season != other.season) return false
        if (name != other.name) return false
        if (isActive != other.isActive) return false
        if (eventType != null) {
            if (other.eventType == null) return false
            if (!eventType.contentEquals(other.eventType)) return false
        } else if (other.eventType != null) return false
        if (injuryDetails != null) {
            if (other.injuryDetails == null) return false
            if (!injuryDetails.contentEquals(other.injuryDetails)) return false
        } else if (other.injuryDetails != null) return false
        if (injuries != null) {
            if (other.injuries == null) return false
            if (!injuries.contentEquals(other.injuries)) return false
        } else if (other.injuries != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = season
        result = 31 * result + name.hashCode()
        result = 31 * result + isActive.hashCode()
        result = 31 * result + (eventType?.contentHashCode() ?: 0)
        result = 31 * result + (injuryDetails?.contentHashCode() ?: 0)
        result = 31 * result + (injuries?.contentHashCode() ?: 0)
        return result
    }
}

@Parcelize
data class StatsEventType<T : Parcelable>(
        val eventTypeId: Int = -1,
        val name: String = "",
        // some endpoints return splits, some return events
        val splits: Array<StatsEventSplit<T>>? = null,
        val events: Array<BoxEvent<T>>? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StatsEventType<*>

        if (eventTypeId != other.eventTypeId) return false
        if (name != other.name) return false
        if (splits != null) {
            if (other.splits == null) return false
            if (!splits.contentEquals(other.splits)) return false
        } else if (other.splits != null) return false
        if (events != null) {
            if (other.events == null) return false
            if (!events.contentEquals(other.events)) return false
        } else if (other.events != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eventTypeId
        result = 31 * result + name.hashCode()
        result = 31 * result + (splits?.contentHashCode() ?: 0)
        result = 31 * result + (events?.contentHashCode() ?: 0)
        return result
    }
}

@Parcelize
data class StatsEventSplit<T : Parcelable>(
        val splitId: Int = -1,
        val name: String = "",
        val teamStats: Array<T>? = null
) : Parcelable {
    inline fun <reified S: Parcelable> castTo(): StatsEventSplit<S>? {
        val teamStatsCandidate = teamStats?.filterIsInstance(S::class.java)
        return if (!teamStatsCandidate.isNullOrEmpty()) {
            StatsEventSplit(splitId, name, teamStatsCandidate?.toTypedArray())
        } else {
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StatsEventSplit<*>

        if (splitId != other.splitId) return false
        if (name != other.name) return false
        if (teamStats != null) {
            if (other.teamStats == null) return false
            if (!teamStats.contentEquals(other.teamStats)) return false
        } else if (other.teamStats != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = splitId
        result = 31 * result + name.hashCode()
        result = 31 * result + (teamStats?.contentHashCode() ?: 0)
        return result
    }
}

@Parcelize
data class BoxEvent<T: Parcelable>(
        var eventId: Long = -1,
        var startDate: Array<StatsDate> = emptyArray(),
        var teams: Array<StatsTeam> = emptyArray(),
        var boxscores: Array<T>? = null,
        // NHL data menu box scores only
        var periodDetails: Array<PeriodDetail>? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoxEvent<*>

        if (eventId != other.eventId) return false
        if (!Arrays.equals(startDate, other.startDate)) return false
        if (!Arrays.equals(teams, other.teams)) return false
        if (!Arrays.equals(boxscores, other.boxscores)) return false
        if (!Arrays.equals(periodDetails, other.periodDetails)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eventId.hashCode()
        result = 31 * result + Arrays.hashCode(startDate)
        result = 31 * result + Arrays.hashCode(teams)
        result = 31 * result + (boxscores?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + (periodDetails?.let { Arrays.hashCode(it) } ?: 0)
        return result
    }

    inline fun <reified S: Parcelable> castTo(): BoxEvent<S>? {
        val scoreCandidates = boxscores?.filterIsInstance(S::class.java)
        return if (!scoreCandidates.isNullOrEmpty()) {
            BoxEvent(eventId, startDate, teams, scoreCandidates?.toTypedArray(), periodDetails)
        } else {
            null
        }
    }
}

@Parcelize
data class StatsTotal(
        val total: Int = -1
) : Parcelable

@Parcelize
data class DataMenuOverviewModel<T : Parcelable>(
        val statsEventSplit: StatsEventSplit<T> = StatsEventSplit(),
        val standingTeam: StatsTeam = StatsTeam()
) : Parcelable

interface BoxScoreData : Parcelable {
    val teamId: Int
}