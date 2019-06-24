package com.nbcsports.regional.nbc_rsn.data_menu

import com.nbcsports.regional.nbc_rsn.data_bar.*
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoSchedule
import com.nbcsports.regional.nbc_rsn.data_menu.models.RotoStandings
import com.nbcsports.regional.nbc_rsn.data_menu.standings.*
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import java.util.*

object DataMenuDataUtils {
    const val decimalFormatOne = "%.1f"
    const val decimalFormatThree = "%.3f"


    fun updateStandingsTableData(data: Array<RotoStandings>?, sportName: String): LeagueStandings? {
        val conferenceLabel = if (LocalizationManager.isInitialized()) LocalizationManager.DataMenu.Conference else ""
        val divisionLabel = if (LocalizationManager.isInitialized()) LocalizationManager.DataMenu.Division else ""

        val tabConference = StandingsTab(
                name = conferenceLabel.toUpperCase(),
                conferences = getStandingsByConference(data = data, sportName = sportName)
        )
        val tabDivision = StandingsTab(
                name = divisionLabel.toUpperCase(),
                conferences = getStandingsByDivision(data = data, sportName = sportName)
        )

        val tabs = listOf(tabConference, tabDivision)
        return LeagueStandings(tabs = tabs)
    }

    fun getStandingsByConference(data: Array<RotoStandings>?, sportName: String): List<StandingsConference> {

        val divisionLeadersLabel = if (LocalizationManager.isInitialized()) LocalizationManager.DataMenu.DivisionLeaders else ""
        val wildCardLabel = if (LocalizationManager.isInitialized()) LocalizationManager.DataMenu.WildCard else ""

        val conferenceList = data?.groupBy { it.conference }
                ?.map confListMapping@{ (conferenceName, standingsList) ->
                    val sortedList = standingsList.sortedBy { it.rankConference }
                    val tableList = mutableListOf<StandingsTable>()

                    val teamList = sortedList.map teamListMapping@{
                        val record = Record(
                                wins = it.wins,
                                losses = it.losses,
                                ties = it.ties,
                                otl = it.otLosses,
                                teamPoints = it.standingsPoints,
                                overtimeLosses = it.otLosses
                        )

                        val gamesBehind: String = getGamesBehindForTeam(sportName = sportName, standing = it, isDivision = false)

                        return@teamListMapping StandingsRow(
                                name = it.teamAbbr,
                                teamId = it.teamGlobalId,
                                record = DataBarUtil.getRecordForTeam(record = record, isDataBar = false, isDataMenuCarousel = false, isCarouselNFL = false),
                                superscript = getClinchStatusForTeam(standing = it),
                                gamesBehind = gamesBehind,
                                standingsPoints = it.standingsPoints,
                                conferenceRank = it.rankConference,
                                divisionRank = it.rankDivision,
                                wildCardRank = it.rankWildCard,
                                conferenceName = it.conference,
                                divisionName = it.division
                        )
                    }

                    when (sportName) {
                        "basketball" -> {
                            tableList.add(StandingsTable(
                                    label = conferenceName,
                                    columnLabels = getColumnLabelsForSport(sport = sportName),
                                    teams = teamList
                            ))
                        }
                        "baseball" -> {
                            val (divisionLeaders, wildcard) = teamList.partition { it.wildCardRank == -1 }
                            tableList.add(StandingsTable(
                                    label = divisionLeadersLabel,
                                    columnLabels = getColumnLabelsForSport(sport = sportName),
                                    teams = divisionLeaders
                            ))
                            tableList.add(StandingsTable(
                                    label = wildCardLabel,
                                    columnLabels = getColumnLabelsForSport(sport = sportName),
                                    teams = wildcard.sortedBy { it.wildCardRank }
                            ))
                        }
                        "football" -> {
                            val (divisionLeaders, wildcard) = teamList.partition { it.divisionRank == 1 }

                            tableList.add(StandingsTable(
                                    label = divisionLeadersLabel,
                                    columnLabels = getColumnLabelsForSport(sport = sportName),
                                    teams = divisionLeaders
                            ))
                            tableList.add(StandingsTable(
                                    label = wildCardLabel,
                                    columnLabels = getColumnLabelsForSport(sport = sportName),
                                    teams = wildcard.sortedBy { it.wildCardRank }
                            ))
                        }
                        "hockey" -> {
                            val (divisionLeaders, wildcard) = teamList.partition { it.divisionRank!! <= 3 }

                            // 2x tables for div leaders in NHL
                            tableList.addAll(
                                    divisionLeaders.groupBy { it.divisionName }
                                            .map divLeaderMapping@{ (divisionName, divisionList) ->
                                                return@divLeaderMapping StandingsTable(
                                                        label = divisionName,
                                                        columnLabels = getColumnLabelsForSport(sport = sportName),
                                                        teams = divisionList
                                                )
                                            }
                            )

                            // wildcard table
                            tableList.add(StandingsTable(
                                    label = wildCardLabel,
                                    columnLabels = getColumnLabelsForSport(sport = sportName),
                                    teams = wildcard
                            ))
                        }
                    }

                    return@confListMapping StandingsConference(
                            name = conferenceName,
                            tables = tableList.sortedBy { it.label }
                    )
                }

        return conferenceList ?: listOf()
    }

    fun getStandingsByDivision(data: Array<RotoStandings>?, sportName: String): List<StandingsConference> {

        val conferenceList = data?.groupBy { it.conference }
                ?.map confMapping@{ (conferenceName, confList) ->
                    val sortedList = confList.sortedBy { it.rankDivision }

                    val tableList = sortedList.groupBy { it.division }
                            .map tableListMapping@{ (divisionName, divList) ->

                                val teamList = divList.map teamListMapping@{
                                    val record = Record(
                                            wins = it.wins,
                                            losses = it.losses,
                                            ties = it.ties,
                                            otl = it.otLosses,
                                            teamPoints = it.standingsPoints,
                                            overtimeLosses = it.otLosses
                                    )

                                    val gamesBehind: String = getGamesBehindForTeam(sportName = sportName, standing = it, isDivision = true)

                                    return@teamListMapping StandingsRow(
                                            name = it.teamAbbr,
                                            teamId = it.teamGlobalId,
                                            record = DataBarUtil.getRecordForTeam(record = record, isDataBar = false, isDataMenuCarousel = false, isCarouselNFL = false),
                                            superscript = getClinchStatusForTeam(standing = it),
                                            gamesBehind = gamesBehind,
                                            standingsPoints = it.standingsPoints,
                                            conferenceRank = it.rankConference,
                                            divisionRank = it.rankDivision,
                                            wildCardRank = it.rankWildCard,
                                            conferenceName = it.conference,
                                            divisionName = it.division
                                    )
                                }

                                return@tableListMapping StandingsTable(
                                        label = divisionName,
                                        columnLabels = getColumnLabelsForSport(sport = sportName),
                                        teams = teamList
                                )
                            }

                    return@confMapping StandingsConference(
                            name = conferenceName,
                            tables = tableList.sortedBy { it.label }
                    )
                }

        return conferenceList ?: listOf()
    }

    private fun getClinchStatusForTeam(standing: RotoStandings): String {
        var result = ""
        if (standing.isClinchedPresidentsTrophy) result += "w"
        if (standing.IsClinchedPostseason) result += "x"
        if (standing.isClinchedDivision) result += "y"
        if (standing.isClinchedConference) result += "z"
        return result
    }


    private fun getColumnLabelsForSport(sport: String): Pair<String, String> {
        var rec = ""
        var gb = ""
        var pct = ""
        var pts = ""
        if (LocalizationManager.isInitialized()) {
            rec = LocalizationManager.DataMenu.RecordAbbreviation
            gb = LocalizationManager.DataMenu.GamesBackAbbreviation
            pct = "PCT"
            pts = LocalizationManager.DataMenu.PointsAbbreviation
        }

        return when (sport) {
            "baseball", "basketball" -> Pair(rec, gb)
            "football" -> Pair(rec, pct)
            "hockey" -> Pair(rec, pts)
            else -> Pair("", "")
        }
    }

    private fun getGamesBehindForTeam(sportName: String, standing: RotoStandings, isDivision: Boolean): String {
        return when (sportName) {
            "baseball" -> {
                if (standing.rankDivision == 1 || (isDivision && standing.gamesBackDivision == 0.0f)) {
                    "-"
                } else {
                    if (isDivision) {
                        "${standing.gamesBackDivision}"
                    } else {
                        // handle special case for wildcard labelling
                        // - negative values should be shown as positive
                        // - 0.0 values should be dashes
                        when {
                            standing.gamesBackWildCard < 0f     -> "+${-1.0f * standing.gamesBackWildCard}"
                            standing.gamesBackWildCard == 0f    -> "-"
                            else                                -> "${standing.gamesBackWildCard}"
                        }
                    }
                }
            }
            "basketball" -> {
                if (isDivision) {
                    if (standing.gamesBackDivision == 0f) {
                        "-"
                    } else {
                        "${standing.gamesBackDivision}"
                    }
                } else {
                    if (standing.gamesBackConference == 0f) {
                        "-"
                    } else {
                        "${standing.gamesBackConference}"
                    }
                }
            }
            "football" -> {
                decimalFormatThree.format(if (standing.wins <= 0) {
                    0f
                } else {
                    val wins = standing.wins.toFloat()
                    val losses = standing.losses.toFloat()
                    val ties = standing.ties.toFloat()
                    (2f * wins + ties) / (2f * (wins + losses + ties))
                })
            }
            "hockey" -> {
                standing.standingsPoints.toString()
            }
            else -> ""
        }
    }

    // Rotoworld API does not return Pre-Season data
    fun findLatestSeason(seasonSchedule: List<RotoSchedule>): SeasonState {
        seasonSchedule.sortedByDescending { it.gameDateTime }

        val gameTypes = setOf(
                "First Round Playoffs",
                "Second Round Playoffs",
                "Conference Championships",
                "Super Bowl",
                "Wild Card Elimination Game",
                "Division Playoff",
                "LCS",
                "World Series",
                "First Round",
                "Second Round",
                "Conf Finals",
                "Finals",
                "Division Semifinals",
                "Division Finals",
                "Conference Finals",
                "Stanley Cup Final"
        )

        return if (seasonSchedule.any { it.gameType in gameTypes }) {
            SeasonState.POSTSEASON
        } else {
            SeasonState.REGULAR_SEASON
        }
    }

    fun findLatestSeason(seasonSchedule: Season): SeasonState {
        if (seasonSchedule.eventType?.size?.let { it > SeasonState.POSTSEASON.ordinal } == true) {
            // post season exists, auto select post season
            return SeasonState.POSTSEASON
        } else {
            // check if current date is later than the last game in preseason
            // first find preseason
            seasonSchedule.eventType?.firstOrNull { it.eventTypeId == SeasonState.PRESEASON.ordinal }
                    // then find the date of last game
                    ?.events?.let { it[it.lastIndex] }?.getLocalStartDate()
                    ?.let {
                        // now compare with today
                        val lastEventDate = it.getCalendarInstance()
                        val today = Calendar.getInstance()
                        if (today.after(lastEventDate)) {
                            // today is after preseason, so auto select regular season
                            return SeasonState.REGULAR_SEASON
                        }
                    }
            return SeasonState.PRESEASON
        }
    }

    fun getScoreString(score: Int): String {
        return if (score < 0) "-" else score.toString()
    }

    fun getScoreString(score: Float, decimalFormat: String = decimalFormatOne): String {
        return if (score < 0) "-" else decimalFormat.format(score)
    }

    fun getScoreString(score: String): String {
        return if (score.isEmpty()) "-" else score
    }

    fun getScoreString(leftValue: Int, rightValue: Int): String {
        return if (leftValue >= 0 && rightValue >= 0) {
            "$leftValue/$rightValue"
        } else {
            ""
        }
    }

    /**
     * This method adds a 0 in front of the input time if its less than 10.
     */
    fun getTimeString(input: Int): String {
        return if (input < 10) "0$input" else input.toString()
    }
}