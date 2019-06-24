package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Arkady Koplyarov on 2018-03-29.
 */
@Parcelize
data class FeedComponent(
        var componentId: String,
        var title: String,
        var description: String,
        @SerializedName("authorReference")
        var author: String,
        @SerializedName("tags")
        var tag: String,
        var episode: String,
        var cardType: String,
        var f1CutOut: String?,
        @SerializedName("mobAppsContentType")
        var contentType: String,
        @SerializedName("contentImageAsset")
        var imageAssetUrl : String,
        @SerializedName("contentVideoAsset")
        var videoAssetUrl : String,
        @SerializedName("contentAudioAsset")
        var audioAssetUrl : String,
        var streamUrl: String?,
        var contentDuration: String,
        var publishedDate: String,
        var isPublished: Boolean = false,
        var mediaSource: MediaSource?

) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "", null, "", "", false, null) {
        this.type = Type.COMPONENT
    }

    constructor(type: Type) : this("", "", "", "", "", "","", "", "", "", "", "", null, "", "", false, null) {
        this.type = type
    }

    var type: Type = Type.COMPONENT // used by recyclerview

    // It's only used for the header and feed label items, in the card list of a team view.
    var team: Team? = null
        private set


    enum class Type {
        PERSISTENT_PLAYER_MEDIUM,
        HEADER,
        THEFEED_LABEL,
        COMPONENT,
        FOOTER,
        FEED_PROMO
    }

    // It's only used for the header and feed label items, in the card list of a team view.
    fun set(team: Team) {
        this.team = team
    }
}
