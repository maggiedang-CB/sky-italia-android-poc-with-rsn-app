package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import com.nbcsports.regional.nbc_rsn.deeplink.Deeplink
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MediaSource (
        val pid: String? = "",
        val id: String? = "",
        var title: String = "",
        var streamUrl: String = "",
        var image: String?,
        var backupImage: String? = "",
        var requestorId: String? = "",
        val live: Boolean = false,
        var deeplink : Deeplink?,
        val channel: String? = "",
        var asset: Asset?) : Parcelable {

    // Clone constructor
    constructor(mediaSource: MediaSource) :
            this(mediaSource.pid,
                    mediaSource.id,
                    mediaSource.title,
                    mediaSource.streamUrl,
                    mediaSource.image,
                    mediaSource.backupImage,
                    mediaSource.requestorId,
                    mediaSource.live,
                    mediaSource.deeplink,
                    mediaSource.channel,
                    mediaSource.asset)

    constructor(pid: String? ="",
                id : String? = "",
                title: String, streamUrl: String, image : String, requestorId : String,
                live : Boolean, deeplink: Deeplink?, channel: String,
                asset: Asset) :
            this(pid, id, title, streamUrl, image, "", requestorId, live, deeplink, channel, asset)

}


