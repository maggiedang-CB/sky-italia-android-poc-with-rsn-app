package com.nbcsports.regional.nbc_rsn.data_menu.standings

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LeagueStandings(
        var name: String = "",
        var sport: String = "",
        var tabs: List<StandingsTab>? = null
) : Parcelable

@Parcelize
data class StandingsTab(
        var name: String = "",
        var conferences: List<StandingsConference>? = null
) : Parcelable

@Parcelize
data class StandingsConference(
        var name: String = "",
        var tables: List<StandingsTable>? = null
) : Parcelable

@Parcelize
data class StandingsTable(
        var label: String = "",
        var columnLabels: Pair<String, String>? = null,
        var teams: List<StandingsRow>? = null
) : Parcelable

@Parcelize
data class StandingsRow(
        var name: String = "",
        var superscript: String = "",
        var teamId: Int = -1,
        var record: String = "",
        var gamesBehind: String = "",
        var conferenceRank: Int? = -1,
        var divisionRank: Int? = -1,
        var wildCardRank: Int? = -1,
        var divisionName: String = "",
        var conferenceName: String = "",
        var standingsPoints: Int? = -1
) : Parcelable
