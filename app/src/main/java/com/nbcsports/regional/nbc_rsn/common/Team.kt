package com.nbcsports.regional.nbc_rsn.common

import android.graphics.Color
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Team (
        val teamId: String,
        val rsndmas: IntArray = IntArray(0),
        val displayName: String = "",
        val regionName: String = "",
        val regionGroupingName: String = "",
        val cityName: String = "",
        val primaryColor: String = "#000000",  // Use a default color
        val secondaryColor: String = "#000000",
        val textColor: String = "#000000",
        val lightProfileTeam: Boolean = false,
        val contentUrl: String = "",
        val logoUrl: String = "",
        val placeholderImageUrl: String = "",
        val regionBackgroundUrl: String = "",
        val regionBackgroundGreyscaleUrl: String = "",
        val geolocation: Geolocation,
        val statsTeamID: Int,
        val league: String = "",
        val liveFeedPromoImageUrl: String = "",
        val requestorId: String = "",
        val homeTeamPriorityList: ArrayList<String> = ArrayList()

) : Parcelable {
    constructor(teamId: String) :
            this(
                    teamId,
                    IntArray(0),
                    "",
                    "",
                    "",
                    "",
                    "#000000",
                    "#000000",
                    "#000000",
                    false,
                    "",
                    "",
                    "",
                    "",
                    "",
                    Geolocation(0.0f,0.0f),
                    -1,
                    "",
                    "",
                    "",
                    ArrayList()
            )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Team

        if (teamId != other.teamId) return false
        if (!Arrays.equals(rsndmas, other.rsndmas)) return false
        if (displayName != other.displayName) return false
        if (regionName != other.regionName) return false
        if (regionGroupingName != other.regionGroupingName) return false
        if (cityName != other.cityName) return false
        if (primaryColor != other.primaryColor) return false
        if (secondaryColor != other.secondaryColor) return false
        if (textColor != other.textColor) return false
        if (lightProfileTeam != other.lightProfileTeam) return false
        if (contentUrl != other.contentUrl) return false
        if (logoUrl != other.logoUrl) return false
        if (placeholderImageUrl != other.placeholderImageUrl) return false
        if (regionBackgroundUrl != other.regionBackgroundUrl) return false
        if (regionBackgroundGreyscaleUrl != other.regionBackgroundGreyscaleUrl) return false
        if (geolocation != other.geolocation) return false
        if (statsTeamID != other.statsTeamID) return false
        if (league != other.league) return false
        if (liveFeedPromoImageUrl != other.liveFeedPromoImageUrl) return false

        return true
    }
    override fun hashCode(): Int {
        var result = teamId.hashCode()
        result = 31 * result + Arrays.hashCode(rsndmas)
        result = 31 * result + displayName.hashCode()
        result = 31 * result + regionName.hashCode()
        result = 31 * result + regionGroupingName.hashCode()
        result = 31 * result + cityName.hashCode()
        result = 31 * result + primaryColor.hashCode()
        result = 31 * result + secondaryColor.hashCode()
        result = 31 * result + textColor.hashCode()
        result = 31 * result + lightProfileTeam.hashCode()
        result = 31 * result + contentUrl.hashCode()
        result = 31 * result + logoUrl.hashCode()
        result = 31 * result + placeholderImageUrl.hashCode()
        result = 31 * result + regionBackgroundUrl.hashCode()
        result = 31 * result + regionBackgroundGreyscaleUrl.hashCode()
        result = 31 * result + geolocation.hashCode()
        result = 31 * result + statsTeamID.hashCode()
        result = 31 * result + league.hashCode()
        result = 31 * result + liveFeedPromoImageUrl.hashCode()
        return result
    }

    fun getTeamColor() : Int {
        // Items (e.g. Topic Tags) receive darker color treatment.
        // For dark profile teams = primary color
        // For light profile teams = secondary color

        var teamColor = Color.BLACK // Will color with black if color is not parsable.
        try {
            if (primaryColor.isNotEmpty()) {
                teamColor = Color.parseColor(primaryColor)
            }
        } catch (e: IllegalArgumentException) {
            // Simply do not color the text if the respective color in the received JSON feed
            // is not parsable.
            e.printStackTrace()
        }
        return teamColor
    }

    fun getTeamColor(defaultColor: Int) : Int {
        var teamColor = defaultColor
        try {
            if (primaryColor.isNotEmpty()) {
                teamColor = Color.parseColor(primaryColor)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        // returns the defaultColor if any errors arise
        return teamColor
    }
}