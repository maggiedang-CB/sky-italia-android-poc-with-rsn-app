package com.nbcsports.regional.nbc_rsn.common

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.nbcsports.regional.nbc_rsn.R
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

@Parcelize
data class Asset (
        val pid: String = "",
        val id: String = "",
        var info: String = "",
        var title: String = "",
        val free: Int,
        var androidStreamUrl: String = "",
        var image: String = "",
        val homeTeam: String = "",
        val awayTeam: String = "",
        val channel: String = "",
        val requestorId: String = "",
        val source: String = "",
        val displayLogo: String = "",
        val status: Int,
        val duration: String = "",
        val sport: String = "",
        val sportName: String = "",
        val league: String = "",
        val start: String = "",
        val broadcastEvent: Boolean,
        val researchTitle: String = "",
        val entitlementId: String = "",
        val blackoutID: String = "",
        val length: Double,
        val ssaiType: String = "",
        @SerializedName("primaryCDN")
        val primaryCDN: String = "",
        val videoSources: List<VideoSource>

) : Parcelable {

    @Parcelize
    data class VideoSource(
            val assetID: String
    ) : Parcelable

    companion object {
        fun getThumbnailUrl(context : Context, imageId: String?): String {
            if (imageId == null) return ""

            val baseUrl = LiveAssetManager.getInstance().assetImageBaseUrl
            val imageSizeSuffix = context.resources.getString(R.string.image_size_suffix)
            return String.format("%s%s%s", baseUrl, imageId, imageSizeSuffix)
        }
    }

    fun isFree(): Boolean {
        return free == 1
    }

    fun getStartDateTime(): DateTime {
        var pattern = DateTimeFormat.forPattern("yyyyMMdd-HHmm").withZoneUTC()
        var date = DateTime.now(DateTimeZone.UTC)
        try {
            date = DateTime.parse(start, pattern)
        } catch (e: Exception) {
            pattern = DateTimeFormat.forPattern("yyyyMMdd-kkmm").withZoneUTC()
        }
        try {
            date = DateTime.parse(start, pattern)
        } catch (e: Exception) {}
        return date
    }

    fun isLive(): Boolean {
        return status == 3
    }

    fun isReplay(): Boolean {
        return status == 5
    }
}

