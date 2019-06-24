package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RotoNBAPlayer(
        @SerializedName("TeamGlobalID")
        val teamGlobalId: Int = -1,
        @SerializedName("PlayerGlobalID")
        val playerGlobalId: Int = -1,
        @SerializedName("FirstName")
        val firstName: String = "",
        @SerializedName("LastName")
        val lastName: String = "",
        @SerializedName("SecondsPlayed")
        val secondsPlayed: String = "",
        @SerializedName("Rebounds")
        val rebounds: String = "",
        @SerializedName("Assists")
        val assists: String = "",
        @SerializedName("Points")
        val points: String = ""
): Parcelable

@Parcelize
data class RotoNBACarousel(
        @SerializedName("FieldGoalPercentage")
        val fieldGoalPercentage: String = "",
        @SerializedName("PointsPerGame")
        val pointsPerGame: String = "",
        @SerializedName("ThreePointFieldGoals")
        val threePointFieldGoals: String = "",
        @SerializedName("EffectiveFieldGoalPercentage")
        val effectiveFieldGoalPercentage: String = ""
): Parcelable