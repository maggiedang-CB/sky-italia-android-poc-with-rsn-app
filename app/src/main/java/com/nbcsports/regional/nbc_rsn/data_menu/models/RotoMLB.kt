package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RotoMLBHitting(
        @SerializedName("TeamGlobalID")
        val teamGlobalId: Int = -1,
        @SerializedName("PlayerGlobalID")
        val playerGlobalId: Int = -1,
        @SerializedName("FirstName")
        val firstName: String = "",
        @SerializedName("LastName")
        val lastName: String = "",
        @SerializedName("AtBats")
        val atBats: String = "",
        @SerializedName("RunsScored")
        val runsScored: String = "",
        @SerializedName("Hits")
        val hits: String = "",
        @SerializedName("Walks")
        val walks: String = "",
        @SerializedName("RunsBattedIn")
        val runsBattedIn: String = "",
        @SerializedName("BattingSlot")
        val battingSlot: String = "",
        @SerializedName("BattingSlotIndex")
        val battingSlotIndex: String = ""
): Parcelable

@Parcelize
data class RotoMLBPitching(
        @SerializedName("TeamGlobalID")
        val teamGlobalId: Int = -1,
        @SerializedName("PlayerGlobalID")
        val playerGlobalId: Int = -1,
        @SerializedName("FirstName")
        val firstName: String = "",
        @SerializedName("LastName")
        val lastName: String = "",
        @SerializedName("OutsPitched")
        val outsPitched: String = "",
        @SerializedName("HitsAllowed")
        val hitsAllowed: String = "",
        @SerializedName("EarnedRunsAllowed")
        val earnedRunsAllowed: String = "",
        @SerializedName("WalksAllowed")
        val walksAllowed: String = "",
        @SerializedName("StrikeOutsThrown")
        val strikeOutsThrown: String = "",
        @SerializedName("PitchingSequence")
        val pitchingSequence: String = ""
): Parcelable

@Parcelize
data class RotoMLBCarousel(
        @SerializedName("Wins")
        val wins: Int = -1,
        @SerializedName("Losses")
        val losses: Int = -1,
        @SerializedName("DivisionRank")
        val divisionRank: Int = -1,
        @SerializedName("DivisionRankIsTied")
        val divisionRankIsTied: Boolean = false,
        @SerializedName("DivisionGamesBehind")
        val divisionGamesBehind: String = "",
        @SerializedName("WinsAmongLastTenGames")
        val winsAmongLastTenGames: Int = -1,
        @SerializedName("LossesAmongLastTenGames")
        val lossesAmongLastTenGames: Int = -1,
        @SerializedName("StreaksKind")
        val streaksKind: String = "",
        @SerializedName("StreaksGames")
        val streaksGames: Int = -1,
        @SerializedName("EliminationNumberDivision")
        val eliminationNumberDivision: Int = -1,
        @SerializedName("EliminationNumberWildCard")
        val eliminationNumberWildCard: Int = -1
): Parcelable