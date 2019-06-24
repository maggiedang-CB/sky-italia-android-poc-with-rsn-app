package com.nbcsports.regional.nbc_rsn.editorial_detail.models

import com.google.gson.annotations.SerializedName
import com.nbcsports.regional.nbc_rsn.common.MediaSource
import com.nbcsports.regional.nbc_rsn.common.Team

/**
 * Created by Arkady Koplyarov on 2018-05-18.
 */
data class EditorialDetailItem(
        var componentType: String,
        var variation: String,
        @SerializedName("heroImage")
        var componentAssetUrl: String,
        @SerializedName("authorReference")
        var author: String,
        var authorImage: String,
        var publishedDate: String,
        var mediaSource: MediaSource?,
        var isPublished: Boolean = false,
        var tag: String,
        @SerializedName("displayBodyText")
        var bodyText : String,
        @SerializedName("displayHeroText")
        var heroText : String,
        var tweetId: String,
        var source: String?,
        var caption: String?,
        var inlineImage: String?
) {
    constructor() : this("", "", "", "", "", "",
            null,
            false,"", "", "", "0","", "", "") {
        this.type = Type.COMPONENT
    }

    constructor(type: Type) : this("", "", "", "", "", "", null,
            false, "", "", "", "0","", "", "") {
        this.type = type
    }

    var type: Type = Type.COMPONENT // used by recyclerview

    //TODO: remove?
    // It's only used for the header and feed label items, in the card list of a team view.
    var team: Team? = null
        private set

    //TODO: rework?...
    enum class Type {
        HEADER,
        COMPONENT,
        FOOTER
    }

    //TODO: remove?
    // It's only used for the header items
    fun set(team: Team) {
        this.team = team
    }
}
