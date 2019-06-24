package com.nbcsports.regional.nbc_rsn.stepped_story.components

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

data class SteppedStoryFeed(val steppedStory: SteppedStory?)

data class SteppedStory(val type: String? = "", @SerializedName("components") val steppedComponents: ArrayList<SteppedComponent>? = ArrayList())

data class SteppedComponent(
        val componentType: String? = "",
        var variation: String? = "",
        val coverTitle: String? = "",
        var coverImage: String? = "",
        val authorReference: String? = "",
        val publishedDate: String? = "",
        val componentId: String? = "",
        val displayTitle: String? = "",
        val displayText: String? = "",
        var entryImage: String? = "",  // this is mutable to ensure we can overwrite the first ordered entry image when there is no overview
        var mediaSource: MediaSource?
)

data class MediaSource(
        val title: String? = "",
        val streamUrl: String? = ""
)
