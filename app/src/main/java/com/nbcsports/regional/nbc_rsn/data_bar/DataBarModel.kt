package com.nbcsports.regional.nbc_rsn.data_bar

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class DataBarModel(
        val status: String = "",
        val recordCount: Int,
        val startTimestamp: String = "",
        val endTimestamp: String = "",
        val timeTaken: Float,
        val apiResults: Array<ApiResult>?
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
data class ApiResult(
        val sportId: Int,
        val name: String = "",
        val league: League?
) : Parcelable

@Parcelize
data class League(
        val leagueId: Int,
        val name: String = "",
        val abbreviation: String = "",
        val displayName: String = "",
        val season: Season?
) : Parcelable

@Parcelize
data class Season(
        val season: Int = -1,
        val name: String = "",
        val isActive: Boolean = false,
        /**
         * This value does not come from JSON. Only exists to assist data handling.
         */
//        var isOffSeason: Boolean = false,
        val eventType: Array<EventType>? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Season

        if (season != other.season) return false
        if (name != other.name) return false
        if (isActive != other.isActive) return false
        if (!Arrays.equals(eventType, other.eventType)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = season
        result = 31 * result + name.hashCode()
        result = 31 * result + isActive.hashCode()
        result = 31 * result + Arrays.hashCode(eventType)
        return result
    }
}

@Parcelize
data class EventType(
        val eventTypeId: Int,
        val name: String = "",
        val conferences: Array<Conference>?,
        val events: Array<Event>? = emptyArray()
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventType

        if (eventTypeId != other.eventTypeId) return false
        if (name != other.name) return false
        if (!Arrays.equals(events, other.events)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eventTypeId
        result = 31 * result + name.hashCode()
        result = 31 * result + Arrays.hashCode(events)
        return result
    }
}

@Parcelize
data class Event(
        val eventId: Long = -1,
        val eventRound: EventRound?,
        val startDate: Array<StatsDate>,
        val isTba: Boolean = false,
        val isDataConfirmed: DataConfirmed?,
        val scheduledPeriods: Int,
        val eventStatus: EventStatus?,
        val isDoubleHeader: Boolean = false,
        val doubleheaderInfo: DoubleheaderInfo?,
        val venue: Venue?,
        val tvStations: Array<TvStation>?,
        val teams: Array<StatsTeam>,
        val seriesGameNumber: Int,
        val isIfNecessary: Boolean = false,
        val makeupEventId: Int,
        val makeupDate: Array<StatsDate>?,
        val suspendedResumeDate: Array<StatsDate>?,
        val eventConference: Conference?,
        val coverageLevel: CoverageLevel?,
        val extraGameInfo: ExtraGameInfo?
) : Parcelable, Comparable<Event> {
    override fun compareTo(other: Event): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return sdf.parse(startDate[0].full).compareTo(sdf.parse(other.startDate[0].full))
    }

    fun getLocalStartDate(): StatsDate? {
        return startDate.firstOrNull { it.dateType == "Local" }
    }
}

@Parcelize
data class ExtraGameInfo(
        val gameOnTheBoard: Boolean = false,
        val reporterLocation: String = "",
        val videoReview: Boolean = false,
        val shortStoppage: Boolean = false,
        val shortStoppageReason: String = "",
        val tripToMound: Boolean = false
) : Parcelable

@Parcelize
data class DoubleheaderInfo(
        val gameNumber: Int,
        val partnerEventId: Int
) : Parcelable

@Parcelize
data class EventRound(
        val gameInSeries: Int,
        val eventRoundId: Int,
        val name: String = ""
) : Parcelable

@Parcelize
data class StatsDate(
        val year: Int = -1,
        val month: Int = -1,
        val date: Int = -1,
        val hour: Int = -1,
        val minute: Int = -1,
        val full: String = "",
        val dateType: String = ""
) : Parcelable {
    
    fun getCalendarInstance(): Calendar {
        val calendar = Calendar.getInstance()
        // month in Calendar is 0-based
        calendar.set(year, month - 1, date)
        return calendar
    }
}

@Parcelize
data class DataConfirmed(
        val score: Boolean = false
) : Parcelable

@Parcelize
data class EventStatus(
        val eventStatusId: Int,
        val isActive: Boolean = false,
        val name: String = "",
        val isUnderReview: Boolean = false,
        val isPenaltyShot: Boolean = false,
        val balls: Int,
        val strikes: Int,
        val outs: Int,
        val inning: Int,
        val inningDivision: String = "",
        val currentBatter: Batter?,
        val runnersOnBase: Array<RunnerOnBase>?,
        val period: Int,
        val teamPossessionId: Int,
        val yardsFromGoal: Int,
        val down: Int,
        val distance: Int,
        val isInjuryTimeout: Boolean = false,
        val minutes: Int,
        val seconds: Int
) : Parcelable

@Parcelize
data class RunnerOnBase(
        val baseNumber: Int,
        val player: StatsPlayer?
) : Parcelable

@Parcelize
data class Batter(
        val playerId: Long,
        val firstName: String = "",
        val lastName: String = "",
        val uniform: String = "",
        val batSide: BatSide?,
        val isTemporaryPlayerId: Boolean = false
) : Parcelable

@Parcelize
data class BatSide(
        val handId: Int,
        val name: String = ""
) : Parcelable

@Parcelize
data class Venue(
        val venueId: Int,
        val name: String = "",
        val city: String = "",
        val state: State?,
        val country: Country?
) : Parcelable

@Parcelize
data class State(
        val stateId: Int,
        val name: String = "",
        val abbreviation: String = ""
) : Parcelable

@Parcelize
data class TvStation(
        val tvStationId: Int,
        val name: String = "",
        val callLetters: String = "",
        val networkType: NetworkType?,
        val country: Country?
) : Parcelable

@Parcelize
data class NetworkType(
        val networkTypeId: Int,
        val name: String = ""
) : Parcelable

@Parcelize
data class Country(
        val countryId: Int,
        val name: String = "",
        val abbreviation: String = ""
) : Parcelable

@Parcelize
data class StatsTeam(
        val series: Series? = null,
        var teamId: Int = -1,
        val location: String = "",
        var nickname: String = "",
        var abbreviation: String = "",
        val division: TeamDivision? = null,
        val playoffSeed: Int = -1,
        var teamLocationType: TeamLocationType? = null,
        val isWinner: Boolean = false,
        val isSplitSquad: Boolean = false,
        val pitchers: Array<Pitcher>? = null,
        val conference: Conference? = null,
        var linescores: Array<Linescore>? = null,
        var linescoreTotals: LinescoreTotals? = null,
        val liveState: LiveState? = null,
        var record: Record? = null,
        val streaks: Array<Streak>? = null,
        val clinchStatus: ClinchStatus? = null,
        val recordDetails: Array<RecordDetail>? = null,
        var score: Int? = null,
        val timeoutsLeft: Int = -1,
        val wildCard: WildCard? = null,
        val wildcardGamesBehind: Double? = -1.0,
        var shootoutGoals: Int = -1,

        // custom variables to handle NHL shootouts
        var isShootoutResult: Boolean = false,
        var isShootoutWinner: Boolean = false
) : Parcelable {
    fun isHome(): Boolean {
        return teamLocationType?.teamLocationTypeId == 1
    }

    fun isAway(): Boolean {
        return teamLocationType?.teamLocationTypeId == 2
    }
}

@Parcelize
data class WildCard(
        val rank : Int,
        val seed : Int,
        val gamesBehind: Double
) : Parcelable

@Parcelize
data class ClinchStatus(
        val clinchStatusId: Int,
        val name: String = ""
) : Parcelable

@Parcelize
data class Linescore(
        val inning: Int = -1,
        val period: Int = -1,
        val score: Int = -1
) : Parcelable

@Parcelize
data class LiveState(
        val currentPitcher: CurrentPitcher? = null,
        val nextUpBatters: Array<Batter>? = null,
        val isDueUp: Boolean = false,
        val strength: PpStrength?,
        val onIce: Int,
        val powerPlayMinutes: String? = "",
        val powerPlaySeconds: String? = ""
) : Parcelable

@Parcelize
data class PpStrength(
        val strengthId: Int,
        val name: String = ""
) : Parcelable

@Parcelize
data class CurrentPitcher(
        val playerId: Long,
        val firstName: String = "",
        val lastName: String = "",
        val uniform: String = "",
        val throwingHand: ThrowingHand?
) : Parcelable

@Parcelize
data class ThrowingHand(
        val handId: Int,
        val name: String = ""
) : Parcelable

@Parcelize
data class Record(
        val wins: Int = 0,
        val losses: Int = 0,
        val ties: Int? = null,
        val otl: Int? = null,
        val percentage: String? = "0",
        val teamPoints: Int? = null,
        val overtimeLosses: Int? = null,
        val shootoutLosses: Int? = null
) : Parcelable

@Parcelize
data class LinescoreTotals(
        val runs: Int? = null,
        val hits: Int? = null,
        val errors: Int? = null
) : Parcelable

@Parcelize
data class Pitcher(
        val type: PitcherType? = null,
        val player: StatsPlayer?,
        val wins: Int,
        val losses: Int,
        val earnedRunAverage: String = ""
) : Parcelable

@Parcelize
data class PitcherType(
        val typeId: Int,
        val name: String = ""
) : Parcelable

@Parcelize
data class StatsPlayer(
        val playerId: Long = -1,
        val firstName: String = "",
        val lastName: String = "",
        val displayName: String = "",
        val uniform: String = "",
        val isTemporaryPlayerId: Boolean = false
) : Parcelable

@Parcelize
data class Conference(
        val conferenceId: Int,
        val name: String = "",
        val abbreviation: String = "",
        val rank: Int,
        val seed: Int,
        val gamesBehind: Double = -1.0,
        val magicNumber: Int,
        val eliminationNumber: Int,
        val divisions: Array<Divisions>? = null,
        val isInterleague: Boolean = false
) : Parcelable

@Parcelize
data class Divisions(
        val divisionId: Int,
        val name: String = "",
        val abbreviation: String = "",
        val teams: Array<StatsTeam>? = null,
        val rank: Int,
        val seed: Int,
        val gamesBehind: String = "",
        val magicNumber: Int,
        val eliminationNumber: Int
) : Parcelable

@Parcelize
data class Series(
        val wins: Int,
        val losses: Int,
        val ties: Int,
        val isSeriesWinner: Boolean = false
) : Parcelable

@Parcelize
data class TeamLocationType(
        val teamLocationTypeId: Int,
        val name: String = ""
) : Parcelable

@Parcelize
data class CoverageLevel(
        val coverageLevelId: Int,
        val details: String = "",
        val name: String = ""
) : Parcelable

@Parcelize
data class RecordDetail(
        val recordDetailId: Int,
        val name: String = "",
        val wins: Int,
        val losses: Int,
        val percentage: String
) : Parcelable

@Parcelize
data class TeamDivision(
        val rank: Int? = null,
        val seed: Int,
        val gamesBehind: Double,
        val magicNumber: Int
) : Parcelable

@Parcelize
data class Streak(
        val kind: String = "",
        val games: Int
) : Parcelable

enum class GameState(val eid: Int, val statusName: String) {
    OFF_SEASON(-1, "Off-Season"),
    PRE_GAME(1, "Pre-Game"),
    IN_PROGRESS(2, "In-Progress"),
    FINAL(4, "Final"),
    POSTPONED(5, "Postponed"),
    SUSPENDED(6, "Suspended"),
    CANCELLED(9, "Cancelled"),
    DELAYED(23, "Delayed")
}
enum class SportLeague(val leagueName: String, val sportName: String) {
    MLB("mlb", "baseball"),
    NFL("nfl", "football"),
    NHL("nhl", "hockey"),
    NBA("nba", "basketball")
}

enum class SeasonState(val seasonName: String) {
    PRESEASON("Pre-Season"),
    REGULAR_SEASON("Regular Season"),
    POSTSEASON("Postseason")
}
