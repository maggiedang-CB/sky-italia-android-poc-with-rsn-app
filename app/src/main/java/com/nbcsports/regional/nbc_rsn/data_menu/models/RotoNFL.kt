package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RotoNFLPlayer(
        @SerializedName("TeamGlobalID")
        val teamGlobalId: Int = -1,
        @SerializedName("PlayerGlobalID")
        val playerGlobalId: Int = -1,
        @SerializedName("FirstName")
        val firstName: String = "",
        @SerializedName("LastName")
        val lastName: String = "",
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
) : Parcelable

@Parcelize
data class RotoNFLCarousel(
        @SerializedName("GamesPlayed")
        val gamesPlayed: Int = -1,
        @SerializedName("RedZoneSuccesses")
        val redZoneSuccesses: Int = -1,
        @SerializedName("RedZoneAttempts")
        val redZoneAttempts: Int = -1,
        @SerializedName("RedZoneEfficiency")
        val redZoneEfficiency: String = "",
        @SerializedName("ThirdDownConversions")
        val thirdDownConversions: Int = -1,
        @SerializedName("ThirdDownAttempts")
        val thirdDownAttempts: Int = -1,
        @SerializedName("ThirdDownEfficiency")
        val thirdDownEfficiency: String = ""
): Parcelable