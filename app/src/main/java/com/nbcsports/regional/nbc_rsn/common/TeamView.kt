package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by arkadykoplyarov on 2018-05-24.
 */
@Parcelize
data class TeamView(
        var components: List<FeedComponent>? = null
) : Parcelable
