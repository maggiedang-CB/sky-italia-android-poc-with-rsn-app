package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RotoNHLSkater(
        @SerializedName("TeamGlobalID")
        val teamGlobalId: Int = -1,
        @SerializedName("PlayerGlobalID")
        val playerGlobalId: Int = -1,
        @SerializedName("FirstName")
        val firstName: String = "",
        @SerializedName("LastName")
        val lastName: String = "",
        @SerializedName("Goals")
        val goals: String = "",
        @SerializedName("Assists")
        val assists: String = "",
        @SerializedName("ShotsOnGoal")
        val shotsOnGoal: String = ""
) : Parcelable

@Parcelize
data class RotoNHLGoalie(
        @SerializedName("TeamGlobalID")
        val teamGlobalId: Int = -1,
        @SerializedName("PlayerGlobalID")
        val playerGlobalId: Int = -1,
        @SerializedName("FirstName")
        val firstName: String = "",
        @SerializedName("LastName")
        val lastName: String = "",
        @SerializedName("GoalsAgainst")
        val goalsAgainst: String = "",
        @SerializedName("ShotsAgainst")
        val shotsAgainst: String = "",
        @SerializedName("Saves")
        val saves: String = ""
) : Parcelable

@Parcelize
data class RotoNHLScoringPlay(
        @SerializedName("Period")
        val period: Int = -1,
        @SerializedName("GoalSequence")
        val goalSequence: Int = -1,
        @SerializedName("TeamGlobalID")
        val teamGlobalId: Int = -1,
        @SerializedName("GameClock")
        val gameClock: String? = "",
        @SerializedName("IsPowerPlay")
        val isPowerPlay: Boolean = false,
        @SerializedName("IsShortHanded")
        val isShortHanded: Boolean = false,
        @SerializedName("IsEvenStrength")
        val isEvenStrength: Boolean = false,
        @SerializedName("IsEmptyNet")
        val isEmptyNet: Boolean = false,
        @SerializedName("IsPenaltyShot")
        val isPenaltyShot: Boolean = false,

        @SerializedName("FirstName")
        val firstName: String = "",
        @SerializedName("LastName")
        val lastName: String = "",
        @SerializedName("Jersey")
        val jersey: String = "",
        @SerializedName("TotalGoalsSeason")
        val totalGoalsSeason: Int = 0,

        @SerializedName("Assist1FirstName")
        val assist1FirstName: String? = "",
        @SerializedName("Assist1LastName")
        val assist1LastName: String? = "",
        @SerializedName("Assist1Jersey")
        val assist1Jersey: String? = "",
        @SerializedName("TotalAssistsSeason1")
        val totalAssistsSeason1: Int? = 0,

        @SerializedName("Assist2FirstName")
        val assist2FirstName: String? = "",
        @SerializedName("Assist2LastName")
        val assist2LastName: String? = "",
        @SerializedName("Assist2Jersey")
        val assist2Jersey: String? = "",
        @SerializedName("TotalAssistsSeason2")
        val totalAssistsSeason2: Int? = 0
) : Parcelable

@Parcelize
data class RotoNHLCarousel(
        @SerializedName("GamesPlayed")
        val gamesPlayed: Int = -1,
        @SerializedName("PowerPlayGoals")
        val powerPlayGoals: Int = -1,
        @SerializedName("PowerPlayOpportunities")
        val powerPlayOpportunities: Int = -1,
        @SerializedName("SavePercentage")
        val savePercentage: String = "",
        @SerializedName("ShotsAgainst")
        val shotsAgainst: Int = -1
): Parcelable