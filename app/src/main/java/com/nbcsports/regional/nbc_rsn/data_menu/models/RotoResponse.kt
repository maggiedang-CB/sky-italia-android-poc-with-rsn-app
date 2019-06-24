package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.nbcsports.regional.nbc_rsn.data_bar.*
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlin.math.roundToInt

@Parcelize
data class RotoResponseScores<T : Parcelable>(
        @SerializedName("Game")
        val game: Array<Array<RotoGame>>? = null,
        @SerializedName("ScoreSummary")
        val scoreSummary: Array<Array<RotoScoreSummary>>? = null,

        // NFL/NBA
        @SerializedName("Boxscore")
        val boxscore: Array<Array<RotoBoxscore>>? = null,

        // MLB
        @SerializedName("HittingStats")
        val hittingStats: Array<Array<RotoMLBHitting>>? = null,
        @SerializedName("PitchingStats")
        val pitchingStats: Array<Array<RotoMLBPitching>>? = null,

        // NHL
        @SerializedName("SkaterStats")
        val skaterStats: Array<Array<RotoNHLSkater>>? = null,
        @SerializedName("GoalieStats")
        val goalieStats: Array<Array<RotoNHLGoalie>>? = null,
        @SerializedName("ScoringPlays")
        val scoringPlays: Array<Array<RotoNHLScoringPlay>>? = null
) : Parcelable {

    /*
    This takes the current period score map and sums up values to get the current game score.

    Note that this only shows the score up to the previous goal. So add 1 for the scoring team.
     */
    fun getCurrentScore(homeTeamId: Int, awayTeamId: Int, scoringTeamId: Int, map: Map<Int, ArrayList<PeriodDetailGoal>?>?): PeriodDetailGoalCurrentScore {
        var awayScore = 0
        var homeScore = 0

        map?.onEach { (_, goalList) ->
            goalList?.forEach {
                when (it.teamId) {
                    homeTeamId -> homeScore++
                    awayTeamId -> awayScore++
                }
            }
        }

        when (scoringTeamId) {
            homeTeamId -> homeScore++
            awayTeamId -> awayScore++
        }

        return PeriodDetailGoalCurrentScore(
                awayScore = awayScore,
                homeScore = homeScore
        )
    }

    inline fun <reified T : Parcelable> convertToBoxEvent(teamGlobalId: Int, t: T): BoxEvent<out Parcelable>? {
        var eventId: Long = -1L
        val awayTeam = StatsTeam()
        val homeTeam = StatsTeam()

        game?.let {
            val gameData = it[0][0]
            eventId = gameData.gameGlobalId.toLong()

            if (gameData.isShootoutResult()) {
                awayTeam.isShootoutResult = true
                homeTeam.isShootoutResult = true
                awayTeam.isShootoutWinner = gameData.getShootoutWinner() == gameData.awayGlobalId
                homeTeam.isShootoutWinner = gameData.getShootoutWinner() == gameData.homeGlobalId
            }

            // away team
            awayTeam.apply {
                teamId = gameData.awayGlobalId
                abbreviation = gameData.awayAbbr
                nickname = gameData.awayName
                score = gameData.awayScore
                teamLocationType = TeamLocationType(teamLocationTypeId = 2)
                record = Record(
                        wins = gameData.awayWins,
                        losses = gameData.awayLosses,
                        otl = gameData.awayOtLosses,
                        ties = gameData.awayTies
                )
            }

            // home team
            homeTeam.apply {
                teamId = gameData.homeGlobalId
                abbreviation = gameData.homeAbbr
                nickname = gameData.homeName
                score = gameData.homeScore
                teamLocationType = TeamLocationType(teamLocationTypeId = 1)
                record = Record(
                        wins = gameData.homeWins,
                        losses = gameData.homeLosses,
                        otl = gameData.homeOtLosses,
                        ties = gameData.homeTies
                )
            }
        }

        return when (t) {
            is BoxScoreMLB -> {
                val boxscores = mutableListOf<BoxScoreMLB>()

                scoreSummary?.let { period ->
                    val awayLine = mutableListOf<Linescore>()
                    val homeLine = mutableListOf<Linescore>()

                    period.forEach { i1 ->
                        val item = i1[0]
                        when (item.teamGlobalId) {
                            awayTeam.teamId -> awayLine.add(Linescore(inning = item.score))
                            homeTeam.teamId -> homeLine.add(Linescore(inning = item.score))
                            else -> {
                            }
                        }
                    }

                    awayTeam.linescores = awayLine.toTypedArray()
                    awayTeam.linescoreTotals = LinescoreTotals(runs = awayLine.sumBy { it.inning })
                    homeTeam.linescores = homeLine.toTypedArray()
                    homeTeam.linescoreTotals = LinescoreTotals(runs = homeLine.sumBy { it.inning })
                }

                val awayBatters = mutableListOf<MLBBattingStats>()
                val awayPitchers = mutableListOf<MLBPitchingStats>()

                val homeBatters = mutableListOf<MLBBattingStats>()
                val homePitchers = mutableListOf<MLBPitchingStats>()

                hittingStats?.let {
                    it.forEach { entry ->
                        val row = entry[0]
                        val battingStats = MLBBattingStats(
                                player = StatsPlayer(
                                        playerId = row.playerGlobalId,
                                        firstName = row.firstName,
                                        lastName = row.lastName
                                ),
                                battingSlot = row.battingSlotIndex.toInt(),
                                atBats = MLBStatsDetail(game = row.atBats.toInt()),
                                runs = MLBStatsDetail(game = row.runsScored.toInt()),
                                hits = MLBStatsDetail(game = row.hits.toInt()),
                                walks = MLBStatsDetail(game = row.walks.toInt()),
                                runsBattedIn = MLBStatsDetail(game = row.runsBattedIn.toInt())
                        )
                        when (row.teamGlobalId) {
                            awayTeam.teamId -> awayBatters.add(battingStats)
                            homeTeam.teamId -> homeBatters.add(battingStats)
                            else -> {
                            }
                        }
                    }
                }

                pitchingStats?.let {
                    it.forEach { entry ->
                        val row = entry[0]

                        val inningsPitched = (row.outsPitched.toInt() / 3) + (row.outsPitched.toInt() % 3) * 0.1f

                        val pitchingStats = MLBPitchingStats(
                                player = StatsPlayer(
                                        playerId = row.playerGlobalId,
                                        firstName = row.firstName,
                                        lastName = row.lastName
                                ),
                                sequence = row.pitchingSequence.toInt(),
                                earnedRuns = MLBStatsDetail(game = row.earnedRunsAllowed.toInt()),
                                hits = MLBStatsDetail(game = row.hitsAllowed.toInt()),
                                walks = MLBStatsDetail(game = row.walksAllowed.toInt()),
                                strikeouts = MLBStatsDetail(game = row.strikeOutsThrown.toInt()),
                                inningsPitched = MLBStatsDetailString(game = String.format("%.1f", inningsPitched))
                        )
                        when (row.teamGlobalId) {
                            awayTeam.teamId -> awayPitchers.add(pitchingStats)
                            homeTeam.teamId -> homePitchers.add(pitchingStats)
                            else -> {
                            }
                        }
                    }
                }

                boxscores.add(BoxScoreMLB(teamId = awayTeam.teamId, playerBattingStats = awayBatters.toTypedArray(), playerPitchingStats = awayPitchers.toTypedArray()))
                boxscores.add(BoxScoreMLB(teamId = homeTeam.teamId, playerBattingStats = homeBatters.toTypedArray(), playerPitchingStats = homePitchers.toTypedArray()))

                val boxEvent = BoxEvent(
                        eventId = eventId,
                        boxscores = (boxscores as List<BoxScoreMLB>).toTypedArray(),
                        teams = arrayOf(awayTeam, homeTeam)
                )

                boxEvent
            }
            is BoxScoreNBA -> {
                val boxscores = mutableListOf<BoxScoreNBA>()

                scoreSummary?.let { period ->
                    val awayLine = mutableListOf<Linescore>()
                    val homeLine = mutableListOf<Linescore>()

                    period.forEach { i1 ->
                        val item = i1[0]
                        when (item.teamGlobalId) {
                            awayTeam.teamId -> awayLine.add(Linescore(period = item.period, score = item.score))
                            homeTeam.teamId -> homeLine.add(Linescore(period = item.period, score = item.score))
                            else -> {
                            }
                        }
                    }

                    awayTeam.linescores = awayLine.toTypedArray()
                    homeTeam.linescores = homeLine.toTypedArray()
                }

                boxscore?.let {
                    val awayPlayers = mutableListOf<PlayerStatsNBA>()
                    val homePlayers = mutableListOf<PlayerStatsNBA>()

                    it.forEach { entry ->
                        val row = entry[0]
                        val playerStats = PlayerStatsNBA(
                                player = StatsPlayer(
                                        playerId = row.playerGlobalId,
                                        firstName = row.firstName,
                                        lastName = row.lastName
                                ),
                                startingPosition = StatsPlayerPosition(
                                ),
                                minutesPlayed = (row.secondsPlayed.toFloat() / 60f).roundToInt(),
                                rebounds = StatsTotal(
                                        total = row.rebounds.toInt()
                                ),
                                assists = row.assists.toInt(),
                                points = row.points.toInt()
                        )
                        when (row.teamGlobalId) {
                            awayTeam.teamId -> awayPlayers.add(playerStats)
                            homeTeam.teamId -> homePlayers.add(playerStats)
                            else -> {
                            }
                        }
                    }

                    boxscores.add(BoxScoreNBA(teamId = awayTeam.teamId, playerStats = awayPlayers.toTypedArray()))
                    boxscores.add(BoxScoreNBA(teamId = homeTeam.teamId, playerStats = homePlayers.toTypedArray()))
                }

                BoxEvent(
                        eventId = eventId,
                        boxscores = (boxscores as List<BoxScoreNBA>).toTypedArray(),
                        teams = arrayOf(awayTeam, homeTeam)
                )
            }
            is BoxScoreNFL -> {
                val boxscores = mutableListOf<BoxScoreNFL>()

                scoreSummary?.let { period ->
                    val awayLine = mutableListOf<Linescore>()
                    val homeLine = mutableListOf<Linescore>()

                    period.forEach { i1 ->
                        val item = i1[0]
                        when (item.teamGlobalId) {
                            awayTeam.teamId -> awayLine.add(Linescore(period = item.period, score = item.score))
                            homeTeam.teamId -> homeLine.add(Linescore(period = item.period, score = item.score))
                            else -> {
                            }
                        }
                    }

                    boxscore?.let {
                        val awayPlayers = mutableListOf<PlayerStatsNFL>()
                        val homePlayers = mutableListOf<PlayerStatsNFL>()

                        it.forEach { entry ->
                            val row = entry[0]
                            val playerStats = PlayerStatsNFL()
                            row.getPassingStats()?.let { p1 -> playerStats.passingStats = arrayOf(p1) }
                            row.getRushingStats()?.let { p1 -> playerStats.rushingStats = arrayOf(p1) }
                            row.getReceivingStats()?.let { p1 -> playerStats.receivingStats = arrayOf(p1) }
                            row.getDefenseStats()?.let { p1 -> playerStats.defenseStats = arrayOf(p1) }
                            row.getKickReturnsStats()?.let { p1 -> playerStats.kickReturnStats = arrayOf(p1) }
                            row.getPuntReturnsStats()?.let { p1 -> playerStats.puntReturnStats = arrayOf(p1) }
                            row.getFieldGoalStats()?.let { p1 -> playerStats.fieldGoalStats = arrayOf(p1) }
                            row.getPatStats()?.let { p1 -> playerStats.patStats = arrayOf(p1) }
                            row.getPuntingStats()?.let { p1 -> playerStats.puntingStats = arrayOf(p1) }

                            when (row.teamGlobalId) {
                                awayTeam.teamId -> awayPlayers.add(playerStats)
                                homeTeam.teamId -> homePlayers.add(playerStats)
                                else -> {
                                }
                            }
                        }

                        boxscores.add(BoxScoreNFL(teamId = awayTeam.teamId))
                        boxscores.add(BoxScoreNFL(teamId = homeTeam.teamId))
                    }

                    awayTeam.linescores = awayLine.toTypedArray()
                    homeTeam.linescores = homeLine.toTypedArray()
                }

                BoxEvent(
                        eventId = eventId,
                        boxscores = (boxscores as List<BoxScoreNFL>).toTypedArray(),
                        teams = arrayOf(awayTeam, homeTeam)
                )
            }
            is BoxScoreNHL -> {
                val boxscores = mutableListOf<BoxScoreNHL>()
                val periodDetails = mutableListOf<PeriodDetail>()

                scoreSummary?.let { period ->
                    val awayLine = mutableListOf<Linescore>()
                    val homeLine = mutableListOf<Linescore>()

                    period.forEach { i1 ->
                        val item = i1[0]
                        when (item.teamGlobalId) {
                            awayTeam.teamId -> awayLine.add(Linescore(period = item.period, score = item.score))
                            homeTeam.teamId -> homeLine.add(Linescore(period = item.period, score = item.score))
                            else -> {
                            }
                        }
                    }

                    awayTeam.linescores = awayLine.toTypedArray()
                    homeTeam.linescores = homeLine.toTypedArray()
                }

                val awaySkaters = mutableListOf<SkaterStats>()
                val awayGoalies = mutableListOf<GoalieStats>()
                val homeSkaters = mutableListOf<SkaterStats>()
                val homeGoalies = mutableListOf<GoalieStats>()

                skaterStats?.let {
                    it.forEach { entry ->
                        val row = entry[0]
                        val skaterStats = SkaterStats(
                                player = StatsPlayer(
                                        playerId = row.playerGlobalId,
                                        firstName = row.firstName,
                                        lastName = row.lastName
                                ),
                                goals = StatsTotal(total = row.goals.toInt()),
                                assists = StatsTotal(total = row.assists.toInt()),
                                shotsOnGoal = row.shotsOnGoal.toInt()
                        )
                        when (row.teamGlobalId) {
                            awayTeam.teamId -> awaySkaters.add(skaterStats)
                            homeTeam.teamId -> homeSkaters.add(skaterStats)
                            else -> {
                            }
                        }
                    }
                }

                goalieStats?.let {
                    it.forEach { entry ->
                        val row = entry[0]
                        val goalieStats = GoalieStats(
                                player = StatsPlayer(
                                        playerId = row.playerGlobalId,
                                        firstName = row.firstName,
                                        lastName = row.lastName
                                ),
                                goalsAgainst = StatsGoalAgainst(row.goalsAgainst.toInt()),
                                shotsAgainst = row.shotsAgainst.toInt(),
                                saves = row.saves.toInt()
                        )
                        when (row.teamGlobalId) {
                            awayTeam.teamId -> awayGoalies.add(goalieStats)
                            homeTeam.teamId -> homeGoalies.add(goalieStats)
                            else -> {
                            }
                        }
                    }
                }

                scoringPlays?.let {

                    val periodData = mutableMapOf<Int, ArrayList<PeriodDetailGoal>>()

                    it.forEach { entry ->
                        val row = entry[0]
                        val (minutes, seconds) = row.gameClock?.split(":") ?: listOf("0", "0")

                        val assists = mutableListOf<PeriodDetailGoalAssist>()
                        if (row.assist1FirstName?.isNotEmpty() == true) {
                            assists.add(PeriodDetailGoalAssist(
                                    player = StatsPlayer(
                                            firstName = row.assist1FirstName,
                                            lastName = row.assist1LastName ?: "",
                                            uniform = row.assist1Jersey ?: ""
                                    ),
                                    season = row.totalAssistsSeason1 ?: 0
                            ))
                        }
                        if (row.assist2FirstName?.isNotEmpty() == true) {
                            assists.add(PeriodDetailGoalAssist(
                                    player = StatsPlayer(firstName = row.assist2FirstName,
                                            lastName = row.assist2LastName ?: "",
                                            uniform = row.assist2Jersey ?: ""
                                    ),
                                    season = row.totalAssistsSeason2 ?: 0
                            ))
                        }

                        val pdg = PeriodDetailGoal(
                                player = StatsPlayer(
                                        firstName = row.firstName,
                                        lastName = row.lastName,
                                        uniform = row.jersey
                                ),
                                teamId = row.teamGlobalId,
                                time = PeriodDetailGoalTime(
                                        minutes = minutes.toInt(),
                                        seconds = seconds.toInt()
                                ),
                                goalNumber = PeriodDetailGoalNumber(
                                        season = row.totalGoalsSeason
                                ),
                                strength = PeriodDetailGoalStrength(

                                ),
                                assists = assists.toTypedArray(),
                                currentScore = getCurrentScore(homeTeamId = homeTeam.teamId, awayTeamId = awayTeam.teamId, scoringTeamId = row.teamGlobalId, map = periodData)
                        )

                        // store period data
                        val key = row.period
                        if (!periodData.containsKey(key)) {
                            periodData[key] = ArrayList()
                        }
                        periodData[key]?.add(pdg)
                    }

                    periodData.forEach { periodEntry ->
                        periodDetails.add(PeriodDetail(
                                period = periodEntry.key,
                                goals = periodEntry.value.toTypedArray()
                        ))
                    }
                }

                boxscores.add(BoxScoreNHL(teamId = awayTeam.teamId, playerSkaterStats = awaySkaters.toTypedArray(), playerGoaltenderStats = awayGoalies.toTypedArray()))
                boxscores.add(BoxScoreNHL(teamId = homeTeam.teamId, playerSkaterStats = homeSkaters.toTypedArray(), playerGoaltenderStats = homeGoalies.toTypedArray()))

                BoxEvent(
                        eventId = eventId,
                        boxscores = (boxscores as List<BoxScoreNHL>).toTypedArray(),
                        teams = arrayOf(awayTeam, homeTeam),
                        periodDetails = periodDetails.toTypedArray()
                )
            }
            else -> null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RotoResponseScores<*>

        if (game != null) {
            if (other.game == null) return false
            if (!game.contentDeepEquals(other.game)) return false
        } else if (other.game != null) return false
        if (scoreSummary != null) {
            if (other.scoreSummary == null) return false
            if (!scoreSummary.contentDeepEquals(other.scoreSummary)) return false
        } else if (other.scoreSummary != null) return false
        if (boxscore != null) {
            if (other.boxscore == null) return false
            if (!boxscore.contentDeepEquals(other.boxscore)) return false
        } else if (other.boxscore != null) return false
        if (hittingStats != null) {
            if (other.hittingStats == null) return false
            if (!hittingStats.contentDeepEquals(other.hittingStats)) return false
        } else if (other.hittingStats != null) return false
        if (pitchingStats != null) {
            if (other.pitchingStats == null) return false
            if (!pitchingStats.contentDeepEquals(other.pitchingStats)) return false
        } else if (other.pitchingStats != null) return false
        if (skaterStats != null) {
            if (other.skaterStats == null) return false
            if (!skaterStats.contentDeepEquals(other.skaterStats)) return false
        } else if (other.skaterStats != null) return false
        if (goalieStats != null) {
            if (other.goalieStats == null) return false
            if (!goalieStats.contentDeepEquals(other.goalieStats)) return false
        } else if (other.goalieStats != null) return false
        if (scoringPlays != null) {
            if (other.scoringPlays == null) return false
            if (!scoringPlays.contentDeepEquals(other.scoringPlays)) return false
        } else if (other.scoringPlays != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = game?.contentDeepHashCode() ?: 0
        result = 31 * result + (scoreSummary?.contentDeepHashCode() ?: 0)
        result = 31 * result + (boxscore?.contentDeepHashCode() ?: 0)
        result = 31 * result + (hittingStats?.contentDeepHashCode() ?: 0)
        result = 31 * result + (pitchingStats?.contentDeepHashCode() ?: 0)
        result = 31 * result + (skaterStats?.contentDeepHashCode() ?: 0)
        result = 31 * result + (goalieStats?.contentDeepHashCode() ?: 0)
        result = 31 * result + (scoringPlays?.contentDeepHashCode() ?: 0)
        return result
    }
}

@Parcelize
data class RotoResponse<T : Parcelable?>(
        @SerializedName("roster")
        val roster: Array<T>? = null,
        @SerializedName("schedule")
        val schedule: Array<T>? = null,
        @SerializedName("standings")
        val standings: Array<T>? = null,
        @SerializedName("dataBar")
        val databar: Array<T>? = null,
        @SerializedName("carousel")
        val carousel: Array<T>? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RotoResponse<*>

        if (roster != null) {
            if (other.roster == null) return false
            if (!roster.contentEquals(other.roster)) return false
        } else if (other.roster != null) return false
        if (schedule != null) {
            if (other.schedule == null) return false
            if (!schedule.contentEquals(other.schedule)) return false
        } else if (other.schedule != null) return false
        if (standings != null) {
            if (other.standings == null) return false
            if (!standings.contentEquals(other.standings)) return false
        } else if (other.standings != null) return false
        if (databar != null) {
            if (other.databar == null) return false
            if (!databar.contentEquals(other.databar)) return false
        } else if (other.databar != null) return false
        if (carousel != null) {
            if (other.carousel == null) return false
            if (!carousel.contentEquals(other.carousel)) return false
        } else if (other.carousel != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roster?.contentHashCode() ?: 0
        result = 31 * result + (schedule?.contentHashCode() ?: 0)
        result = 31 * result + (standings?.contentHashCode() ?: 0)
        result = 31 * result + (databar?.contentHashCode() ?: 0)
        result = 31 * result + (carousel?.contentHashCode() ?: 0)
        return result
    }

}

@Parcelize
data class RotoPlayer(
        @SerializedName("PlayerGlobalID")
        val playerGlobalId: Int = -1,
        @SerializedName("FirstName")
        val firstName: String = "",
        @SerializedName("LastName")
        val lastName: String = "",
        @SerializedName("Position")
        val position: String = "",
        @SerializedName("Jersey")
        val jersey: String = "",
        @SerializedName("BirthDate")
        val birthDate: String = "",
        @SerializedName("Height")
        val height: String = "",
        @SerializedName("Weight")
        val weight: Int = -1
) : Parcelable

@Parcelize
data class RotoEvent(
        val name: String? = null,
        val events: List<RotoSchedule> = mutableListOf()
) : Parcelable

@Parcelize
data class RotoSchedule(
        @SerializedName("GameGlobalID")
        val gameGlobalId: Int = -1,
        @SerializedName("GameDateTime")
        val gameDateTime: String = "",
        @SerializedName("GameDateTimeUTC")
        val gameDateTimeUTC: String = "",
        @SerializedName("SeasonWeek")
        val seasonWeek: Int = -1,
        @SerializedName("GameType")
        val gameType: String = "",
        @SerializedName("GameStatus")
        val gameStatus: String = "",
        @SerializedName("Station")
        val station: String = "",
        @SerializedName("IsTBA")
        val isTBA: Boolean = false,

        // region Away
        @SerializedName("AwayGlobalID")
        val awayGlobalId: Int = -1,
        @SerializedName("AwayAbbr")
        val awayAbbr: String = "",
        @SerializedName("AwayScore")
        val awayScore: Int = -1,
        @SerializedName("AwayResult")
        val awayResult: String = "",
        @SerializedName("AwayWins")
        val awayWins: Int = -1,
        @SerializedName("AwayLosses")
        val awayLosses: Int = -1,
        @SerializedName("AwayTies")
        val awayTies: Int? = -1,
        @SerializedName("AwayOvertimeLosses")
        val awayOtLosses: Int = -1,
        // endregion

        // region Home
        @SerializedName("HomeGlobalID")
        val homeGlobalId: Int = -1,
        @SerializedName("HomeAbbr")
        val homeAbbr: String = "",
        @SerializedName("HomeScore")
        val homeScore: Int = -1,
        @SerializedName("HomeResult")
        val homeResult: String = "",
        @SerializedName("HomeWins")
        val homeWins: Int = -1,
        @SerializedName("HomeLosses")
        val homeLosses: Int = -1,
        @SerializedName("HomeTies")
        val homeTies: Int? = -1,
        @SerializedName("HomeOvertimeLosses")
        val homeOtLosses: Int = -1
        // endregion
) : Parcelable {

    fun isHomeWin(): Boolean {
        return homeResult.toLowerCase() == "win"
    }

    fun isAwayWin(): Boolean {
        return awayResult.toLowerCase() == "win"
    }

    fun isPreGame(): Boolean {
        return gameStatus == "Pre-Game"
    }

    fun isGameStatusFinal(): Boolean {
        return gameStatus.toLowerCase() == "final"
    }

    fun isGameStatusTba(): Boolean {
        return isTBA
    }
}

@Parcelize
data class RotoGame(
        @SerializedName("GameGlobalID")
        val gameGlobalId: Int = -1,
        @SerializedName("Season")
        val season: Int = -1,
        @SerializedName("GameDateTime")
        val gameDateTime: String = "",
        @SerializedName("GameStatus")
        val gameStatus: String = "",
        @SerializedName("PeriodStr")
        val periodStr: String = "",
        @SerializedName("LastUpdated")
        val lastUpdated: String = "",

        // region Away
        @SerializedName("AwayGlobalID")
        val awayGlobalId: Int = -1,
        @SerializedName("AwayName")
        val awayName: String = "",
        @SerializedName("AwayAbbr")
        val awayAbbr: String = "",
        @SerializedName("AwayScore")
        val awayScore: Int = -1,
        @SerializedName("AwayResult")
        val awayResult: String = "",
        @SerializedName("AwayWins")
        val awayWins: Int = -1,
        @SerializedName("AwayLosses")
        val awayLosses: Int = -1,
        @SerializedName("AwayOvertimeLosses")
        val awayOtLosses: Int = -1,
        @SerializedName("AwayTies")
        val awayTies: Int = -1,
        // endregion

        // region Home
        @SerializedName("HomeGlobalID")
        val homeGlobalId: Int = -1,
        @SerializedName("HomeName")
        val homeName: String = "",
        @SerializedName("HomeAbbr")
        val homeAbbr: String = "",
        @SerializedName("HomeScore")
        val homeScore: Int = -1,
        @SerializedName("HomeResult")
        val homeResult: String = "",
        @SerializedName("HomeWins")
        val homeWins: Int = -1,
        @SerializedName("HomeLosses")
        val homeLosses: Int = -1,
        @SerializedName("HomeOvertimeLosses")
        val homeOtLosses: Int = -1,
        @SerializedName("HomeTies")
        val homeTies: Int = -1
        // endregion

) : Parcelable {
    fun isShootoutResult(): Boolean {
        return awayResult.contains("Shootout", ignoreCase = true) || homeResult.contains("Shootout", ignoreCase = true)
    }

    fun getShootoutWinner(): Int {
        return if (isShootoutResult()) {
            if (awayResult.contains("win", ignoreCase = true)) {
                awayGlobalId
            } else {
                homeGlobalId
            }
        } else {
            -1
        }
    }
}

@Parcelize
data class RotoScoreSummary(
        @SerializedName("Period")
        val period: Int = -1,
        @SerializedName("TeamGlobalID")
        val teamGlobalId: Int = -1,
        @SerializedName("Score")
        val score: Int = -1,
        @SerializedName("IsNotNecessary")
        val isNotNecessary: Boolean = false
) : Parcelable

@Parcelize
data class RotoBoxscore(
        @SerializedName("TeamGlobalID")
        val teamGlobalId: Int = -1,
        @SerializedName("PlayerGlobalID")
        val playerGlobalId: Int = -1,
        @SerializedName("FirstName")
        val firstName: String = "",
        @SerializedName("LastName")
        val lastName: String = "",

        // region NBA
        @SerializedName("SecondsPlayed")
        val secondsPlayed: String = "",
        @SerializedName("Rebounds")
        val rebounds: String = "",
        @SerializedName("Assists")
        val assists: String = "",
        @SerializedName("Points")
        val points: String = "",
        // endregion

        // region NFL
        @SerializedName("PassCompletions")
        val passCompletions: String = "",
        @SerializedName("PassAttempts")
        val passAttempts: String = "",
        @SerializedName("PassYards")
        val passYards: String = "",
        @SerializedName("PassTouchdowns")
        val passTouchdowns: String = "",
        @SerializedName("RushAttempts")
        val rushAttempts: String = "",
        @SerializedName("RushYards")
        val rushYards: String = "",
        @SerializedName("RushTouchdowns")
        val rushTouchdowns: String = "",
        @SerializedName("Receptions")
        val receptions: String = "",
        @SerializedName("RecYards")
        val recYards: String = "",
        @SerializedName("RecTouchdowns")
        val recTouchdowns: String = "",
        @SerializedName("DefTackles")
        val defTackles: String = "",
        @SerializedName("DefAssists")
        val defAssists: String = "",
        @SerializedName("DefSacks")
        val defSacks: String = "",
        @SerializedName("Interceptions")
        val interceptions: String = "",
        @SerializedName("RetKickoffs")
        val retKickoffs: String = "",
        @SerializedName("RetKickoffYards")
        val retKickoffYards: String = "",
        @SerializedName("RetKickoffTDs")
        val retKickoffTds: String = "",
        @SerializedName("RetPunts")
        val retPunts: String = "",
        @SerializedName("RetPuntYards")
        val retPuntYards: String = "",
        @SerializedName("RetPuntTDs")
        val retPuntTds: String = "",
        @SerializedName("FGMade")
        val fgMade: String = "",
        @SerializedName("FGAttempted")
        val fgAttempted: String = "",
        @SerializedName("FGLongestYards")
        val fgLongestYards: String = "",
        @SerializedName("ExtraPointsMade")
        val extraPointsMade: String = "",
        @SerializedName("Punts")
        val punts: String = "",
        @SerializedName("PuntYards")
        val puntYards: String = "",
        @SerializedName("PuntLongestYards")
        val puntLongestYards: String = "",
        @SerializedName("PuntsInside20")
        val puntsInside20: String = ""
        // endregion
) : Parcelable {

    // region getters
    fun getStatsPlayer(): StatsPlayer {
        return StatsPlayer(
                playerId = playerGlobalId,
                firstName = firstName,
                lastName = lastName
        )
    }

    fun getPassingStats(): NFLStatsPassing? {
        return if (hasPassingStats()) {
            NFLStatsPassing(
                    player = getStatsPlayer(),
                    completions = passCompletions.toInt(),
                    attempts = passAttempts.toInt(),
                    yards = passYards.toInt(),
                    touchdowns = passTouchdowns.toInt(),
                    interceptions = interceptions.toInt()
            )
        } else {
            null
        }
    }

    fun getRushingStats(): NFLStatsRushing? {
        return if (hasRushingStats()) {
            NFLStatsRushing(
                    player = getStatsPlayer(),
                    attempts = rushAttempts.toInt(),
                    yards = rushYards.toInt(),
                    touchdowns = rushTouchdowns.toInt()
            )
        } else {
            null
        }
    }

    fun getReceivingStats(): NFLStatsReceiving? {
        return if (hasReceivingStats()) {
            NFLStatsReceiving(
                    player = getStatsPlayer(),
                    receptions = receptions.toInt(),
                    yards = recYards.toInt(),
                    touchdowns = recTouchdowns.toInt()
            )
        } else {
            null
        }
    }

    fun getDefenseStats(): NFLStatsDefense? {
        return if (hasDefenseStats()) {
            NFLStatsDefense(
                    player = getStatsPlayer(),
                    tackles = defTackles.toInt(),
                    assists = defAssists.toInt(),
                    sacks = defSacks.toInt()
            )
        } else {
            null
        }
    }

    fun getKickReturnsStats(): NFLStatsReturn? {
        return if (hasKickReturnsStats()) {
            NFLStatsReturn(
                    player = getStatsPlayer(),
                    returns = retKickoffs.toInt(),
                    yards = retKickoffYards.toInt(),
                    touchdowns = retKickoffTds.toInt()
            )
        } else {
            null
        }
    }

    fun getPuntReturnsStats(): NFLStatsReturn? {
        return if (hasPuntReturnsStats()) {
            NFLStatsReturn(
                    player = getStatsPlayer(),
                    returns = retPunts.toInt(),
                    yards = retPuntYards.toInt(),
                    touchdowns = retPuntTds.toInt()
            )
        } else {
            null
        }
    }

    fun getFieldGoalStats(): NFLStatsKicking? {
        return if (hasFieldGoalStats()) {
            NFLStatsKicking(
                    player = getStatsPlayer(),
                    made = fgMade.toInt(),
                    long = fgLongestYards.toInt(),
                    attempts = fgAttempted.toInt()
            )
        } else {
            null
        }
    }

    fun getPatStats(): NFLStatsKicking? {
        return if (hasPatStats()) {
            NFLStatsKicking(
                    player = getStatsPlayer(),
                    made = extraPointsMade.toInt()
            )
        } else {
            null
        }
    }

    fun getPuntingStats(): NFLStatsPunting? {
        return if (hasPuntingStats()) {
            NFLStatsPunting(
                    player = getStatsPlayer(),
                    punts = punts.toInt(),
                    returnYards = puntYards.toInt(),
                    in20 = puntsInside20.toInt()
            )
        } else {
            null
        }
    }
    // endregion

    // region boolean checks
    fun hasPassingStats(): Boolean {
        return listOf(
                passCompletions,
                passAttempts,
                passYards,
                passTouchdowns,
                interceptions
        ).any { it.isNotEmpty() }
    }

    fun hasRushingStats(): Boolean {
        return listOf(rushAttempts, rushTouchdowns, rushYards).any { it.isNotEmpty() }
    }

    fun hasReceivingStats(): Boolean {
        return listOf(receptions, recTouchdowns, recYards).any { it.isNotEmpty() }
    }

    fun hasDefenseStats(): Boolean {
        return listOf(defTackles, defAssists, defSacks).any { it.isNotEmpty() }
    }

    fun hasKickReturnsStats(): Boolean {
        return listOf(retKickoffTds, retKickoffYards, retKickoffs).any { it.isNotEmpty() }
    }

    fun hasPuntReturnsStats(): Boolean {
        return listOf(retPunts, retPuntTds, retPuntYards).any { it.isNotEmpty() }
    }

    fun hasFieldGoalStats(): Boolean {
        return listOf(fgMade, fgLongestYards, fgAttempted).any { it.isNotEmpty() }
    }

    fun hasPatStats(): Boolean {
        return listOf(extraPointsMade).any { it.isNotEmpty() }
    }

    fun hasPuntingStats(): Boolean {
        return listOf(punts, puntYards, puntsInside20).any { it.isNotEmpty() }
    }
    // endregion
}

@Parcelize
data class RotoStandings(
        @SerializedName("StandingDate")
        val standingDate: String = "",
        @SerializedName("TeamAbbr")
        val teamAbbr: String = "",
        @SerializedName("TeamGlobalID")
        val teamGlobalId: Int = -1,
        @SerializedName("Conference")
        val conference: String = "",
        @SerializedName("Division")
        val division: String = "",
        @SerializedName("Wins")
        val wins: Int = -1,
        @SerializedName("Losses")
        val losses: Int = -1,
        @SerializedName("Ties")
        val ties: Int = -1,
        @SerializedName("OvertimeLosses")
        val otLosses: Int = -1,
        @SerializedName("StandingsPoints")
        val standingsPoints: Int = -1,
        @SerializedName("RankLeague")
        val rankLeague: Int = -1,
        @SerializedName("RankConference")
        val rankConference: Int = -1,
        @SerializedName("GamesBackConference")
        val gamesBackConference: Float = -1.0f,
        @SerializedName("RankDivision")
        val rankDivision: Int = -1,
        @SerializedName("GamesBackDivision")
        val gamesBackDivision: Float = -1.0f,
        @SerializedName("RankWildCard")
        val rankWildCard: Int = -1,
        @SerializedName("GamesBackWildCard")
        val gamesBackWildCard: Float = -1.0f,

        @SerializedName("IsEliminatedPostseason")
        val isEliminatedPostseason: Boolean = false,
        @SerializedName("IsClinchedPostseason")
        val IsClinchedPostseason: Boolean = false,
        @SerializedName("IsClinchedDivision")
        val isClinchedDivision: Boolean = false,
        @SerializedName("IsClinchedConference")
        val isClinchedConference: Boolean = false,
        @SerializedName("IsClinchedPresidentsTrophy")
        val isClinchedPresidentsTrophy: Boolean = false
) : Parcelable

@Parcelize
data class RotoResponseOverview<T : Parcelable?, B : Parcelable?>(
        val standingsResponse: T? = null,
        val carouselResponse: B? = null
) : Parcelable


@Parcelize
data class RotoDataBar(
        @SerializedName("GameGlobalID")
        val gameGlobalId: Int,
        @SerializedName("GameType")
        val gameType: String = "",
        @SerializedName("GameDateTimeUTC")
        val gameDateTimeUTC: String = "",
        @SerializedName("LastUpdateUTC")
        val lastUpdateUTC: String = "",
        @SerializedName("TeamAbbr")
        val teamAbbr: String = "",


        @SerializedName("AwayAbbr")
        val awayAbbr: String = "",
        @SerializedName("AwayGlobalID")
        val awayGlobalId: Int = -1,
        @SerializedName("AwayScore")
        val awayScore: Int = -1,
        @SerializedName("AwayTimeouts")
        val awayTimeouts: Int = -1,


        @SerializedName("HomeAbbr")
        val homeAbbr: String = "",
        @SerializedName("HomeGlobalID")
        val homeGlobalId: Int = -1,
        @SerializedName("HomeScore")
        val homeScore: Int = -1,
        @SerializedName("HomeTimeouts")
        val homeTimeouts: Int = -1,


        @SerializedName("GameStatus")
        val gameStatus: String = "",
        @SerializedName("GameStatusID")
        val gameStatusId: Int = -1,
        @SerializedName("Period")
        val period: Int = -1,
        @SerializedName("PeriodStr")
        val periodStr: String = "",

        @SerializedName("GameClockSeconds")
        val gameClockSeconds: Float? = -1f,
        @SerializedName("GameClock")
        val gameClock: String = "",
        @SerializedName("PosessionTeamGlobalID")
        val posessionTeamGlobalId: Int,

        @SerializedName("VenueCity")
        val venueCity: String = "",
        @SerializedName("StationCallLetters")
        val stationCallLetters: String = "",
        @SerializedName("IsTBA")
        val isTBA: Boolean = false,

        // MLB
        @SerializedName("AwayRuns")
        val awayRuns: Int = -1,
        @SerializedName("AwayHits")
        val awayHits: Int = -1,
        @SerializedName("AwayErrors")
        val awayErrors: Int = -1,

        @SerializedName("HomeRuns")
        val homeRuns: Int = -1,
        @SerializedName("HomeHits")
        val homeHits: Int = -1,
        @SerializedName("HomeErrors")
        val homeErrors: Int = -1,

        @SerializedName("IsBottomHalf")
        val isBottomHalf: Boolean = false,
        @SerializedName("Balls")
        val balls: Int = -1,
        @SerializedName("Strikes")
        val strikes: Int = -1,
        @SerializedName("Outs")
        val outs: Int = -1,
        @SerializedName("BaseRunners")
        val baseRunners: Int? = -1,

        @SerializedName("IsDoubleHeader")
        val isDoubleHeader: Boolean = false,
        @SerializedName("DoubleHeaderNumber")
        val doubleHeaderNumber: Int = -1,


        // NBA
        @SerializedName("AwayFullTimeoutsLeft")
        val awayFullTimeoutsLeft: Int = -1,
        @SerializedName("AwayShortTimeoutsLeft")
        val awayShortTimeoutsLeft: Int = -1,
        @SerializedName("HomeFullTimeoutsLeft")
        val homeFullTimeoutsLeft: Int = -1,
        @SerializedName("HomeShortTimeoutsLeft")
        val homeShortTimeoutsLeft: Int = -1,

        // NHL
        @SerializedName("AwayStrength")
        val awayStrength: Int? = -1,
        @SerializedName("AwayStrengthName")
        val awayStrengthName: String = "",
        @SerializedName("HomeStrength")
        val homeStrength: Int? = -1,
        @SerializedName("HomeStrengthName")
        val homeStrengthName: String = "",
        @SerializedName("IsShootout")
        val isShootout: Boolean = false,


        // NFL
        @SerializedName("Down")
        val down: Int = -1,
        @SerializedName("Distance")
        val distance: Int = -1,


        // off-season mode
        @SerializedName("Season")
        val season: String? = "",
        @SerializedName("Wins")
        val wins: Int = -1,
        @SerializedName("Losses")
        val losses: Int = -1,
        @SerializedName("Ties")
        val ties: Int? = -1,
        @SerializedName("OvertimeLosses")
        val otLosses: Int? = -1
) : Parcelable {
    fun isSeasonActive(): Boolean {
        if (season == null || season.isEmpty()) {
            return true
        }

        return false
    }

    fun isOffseasonMode(): Boolean {
        return !isSeasonActive()
    }

    fun isHomePowerplay(): Boolean {
        if (homeStrengthName.isNullOrEmpty()) return false
        return homeStrengthName.equals("powerplay", ignoreCase = true)
    }

    fun isAwayPowerplay(): Boolean {
        if (awayStrengthName.isNullOrEmpty()) return false
        return awayStrengthName.equals("powerplay", ignoreCase = true)
    }

    fun getEventStatus(): EventStatus {
        return EventStatus(
                eventStatusId = gameStatusId,
                balls = -1,
                currentBatter = null,
                distance = -1,
                down = -1,
                inning = -1,
                minutes = -1,
                outs = -1,
                period = -1,
                runnersOnBase = null,
                seconds = -1,
                strikes = -1,
                teamPossessionId = -1,
                yardsFromGoal = -1
        )
    }

    fun getEventStatusId(): Int {
        return gameStatusId
    }

    fun getRecord(league: String): String {
        var record = "$wins-$losses"

        if (ties != null && league.equals("nfl", ignoreCase = true) && ties > 0) {
            record += ties
        }

        if (otLosses != null) {
            record += otLosses
        }

        return record
    }
}

