package com.nbcsports.regional.nbc_rsn.data_bar

import com.nbcsports.regional.nbc_rsn.common.Team
import java.util.*

object DataBarUtil {

    private val nbcMap = mapOf(
            "NBSB" to "NBCSB",
            "NSBA" to "NBCSBA",
            "NSCA" to "NBCSCA",
            "NSCH" to "NBCSC",
            "NSNW" to "NBCSNW",
            "NSPA" to "NBCSP",
            "NSWA" to "NBCSW",
            "NBCS" to "NBCS"
    )

    fun getRequestParams(team: Team?): RequestParam {
        val leagueName = team?.league?.toLowerCase()
        return when (leagueName) {
            "mlb" -> RequestParam("baseball", leagueName)
            "nba" -> RequestParam("basketball", leagueName)
            "nfl" -> RequestParam("football", leagueName)
            "nhl" -> RequestParam("hockey", leagueName)
            else -> RequestParam("", "")
        }
    }

    fun getTvStations(event: Event): String {
        return getTvStationsArray(event).joinToString(", ")
    }

    fun getTvStation(event: Event): String {
        return getTvStationsArray(event).let {
            if (it.isNotEmpty()) it[0] else ""
        }
    }

    private fun getTvStationsArray(event: Event): Array<String> {
        // Add TV station and Game clock info

        val affiliatesList: Set<String> = DataBarManager?.affiliates?.asSequence()?.map { it.station }?.toSet()
                ?: setOf()
        val scoreMap = mutableMapOf<String, Int>()

        // assigning scores based on station list being an affiliate or NBCSN
        event.tvStations?.forEach {
            val letters = it.callLetters.toUpperCase()
            scoreMap[letters] = when (letters) {
                in nbcMap.keys -> 0
                in affiliatesList -> 1
                else -> 2
            }
        }

        // sort
        val sortedStationList = scoreMap.toList().sortedBy { (_, value) -> value }.toMap().keys

        // re-map with NBCSN names
        // city name is not included per RSNAPP-799
        return Array(sortedStationList.size) {
            val label = sortedStationList.elementAt(it).toUpperCase()
            if (nbcMap.containsKey(label)) {
                nbcMap[label] ?: label
            } else {
                label
            }
        }
    }

    fun getTvStations(input: String?): String {
        if (input.isNullOrEmpty()) return ""

        val tvStations = input.split(", ")
        val affiliatesList: Set<String> = DataBarManager?.affiliates?.asSequence()?.map { it.station }?.toSet()
                ?: setOf()
        val scoreMap = mutableMapOf<String, Int>()

        tvStations.forEach {
            val letters = it.toUpperCase()
            scoreMap[letters] = when (letters) {
                in nbcMap.keys -> 0
                in affiliatesList -> 1
                else -> 2
            }
        }

        val sortedStationList = scoreMap.toList().sortedBy { (_, value) -> value }.toMap().keys

        val result = Array(sortedStationList.size) {
            val label = sortedStationList.elementAt(it).toUpperCase()
            if (nbcMap.containsKey(label)) {
                nbcMap[label] ?: label
            } else {
                label
            }
        }

        return result.joinToString(", ")
    }

    /**
     * This method is used to get formatted team's record base on conditions
     *
     * 1. For data bar, there are spaces between "-" and records
     * 2. For data menu carousel, there are spaces between "-" and records,
     *    and only showing "ties" (i.e. otl) for NFL,
     *    and for NHL, "losses" will be (losses + overtimeLosses + shootoutLosses)
     * 3. For other screens, there are no space between "-" and records
     */
    fun getRecordForTeam(record: Record?, isDataBar: Boolean, isDataMenuCarousel: Boolean, isCarouselNFL: Boolean): String {
        if (record == null) {
            return ""
        }

        val recordTies = record.otl ?: record.overtimeLosses ?: record.ties ?: 0
        val overAllLosses = if (isDataMenuCarousel) ((record.losses) + (record.overtimeLosses ?: 0) + (record.shootoutLosses ?: 0)) else record.losses
        return "${record.wins}${if (isDataMenuCarousel || isDataBar) " - $overAllLosses" else "-$overAllLosses"}${when {
            !isDataBar && isDataMenuCarousel && isCarouselNFL && recordTies > 0 -> " - $recordTies"
            isDataBar && !isDataMenuCarousel && recordTies > 0 -> " - $recordTies"
            !isDataBar && !isDataMenuCarousel && recordTies > 0 -> "-$recordTies"
            else -> ""
        }}"
    }

    /**
     * This method is used to get formatted team's record base on conditions
     * (For Roto APIs)
     *
     * 1. For data bar, there are spaces between "-" and records
     * 2. For data menu carousel, there are spaces between "-" and records,
     *    and only showing "ties" for NFL,
     *    and for NHL, "losses" will be (losses + overtimeLosses + shootoutLosses),
     *    and show overtimeLosses (i.e. otl) separately
     * 3. For other screens, there are no space between "-" and records
     */
    fun getRotoRecordForTeam(wins: Int, losses: Int, otl: Int, ties: Int,
                             shootoutLosses: Int, isDataBar: Boolean,
                             isDataMenuCarousel: Boolean): String {
        val recordTies = when {
            otl > 0 -> otl
            ties > 0 -> ties
            else -> 0
        }
        var overAllLosses = losses
        overAllLosses = when {
            isDataMenuCarousel && shootoutLosses > 0 -> overAllLosses + shootoutLosses
            else -> overAllLosses
        }
        return "$wins${if ((isDataMenuCarousel && recordTies <= 0) || isDataBar) " - $overAllLosses" else "-$overAllLosses"}${when {
            !isDataBar && isDataMenuCarousel && recordTies > 0 -> "-$recordTies"
            isDataBar && !isDataMenuCarousel && recordTies > 0 -> " - $recordTies"
            !isDataBar && !isDataMenuCarousel && recordTies > 0 -> "-$recordTies"
            else -> ""
        }}"
    }

    fun getCurrentSeasonForDataMenu(team: Team?): Int {
        val leagueName = team?.league?.toLowerCase()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        val nextYearStartMonth = Calendar.MARCH

        return when (leagueName) {
            "nfl" -> {
                val currentDate = calendar.get(Calendar.DATE)
                if (currentMonth > nextYearStartMonth || currentMonth == nextYearStartMonth && currentDate > 15) {
                    // after super bowl
                    currentYear
                } else {
                    currentYear - 1
                }
            }
            "nhl", "nba" -> {
                if (currentMonth >= Calendar.SEPTEMBER) {
                    currentYear
                } else {
                    currentYear - 1
                }
            }
            else -> currentYear
        }
    }

    fun getCurrentSeasonForDataBar(team: Team?): Int {
        val leagueName = team?.league?.toLowerCase()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        return when (leagueName) {
            "mlb" -> {
                if (calendar.get(Calendar.DAY_OF_YEAR) < calendar.getActualMaximum(Calendar.DAY_OF_YEAR) / 6) {
                    currentYear - 1
                } else {
                    // march or later
                    currentYear
                }
            }
            "nfl" -> {
                // NFL seasons normally end in late December or early Jan
                if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
                    currentYear
                } else {
                    currentYear-1
                }
            }
            // Both NBA and NHL seasons start in the second half of the year and ends in the next year.
            else -> currentYear-1
        }
    }
}

data class RequestParam(val sportName: String, val leagueName: String)