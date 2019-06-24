package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TeamViewFeed(
        var teamView: TeamView?,
        var liveAssets : List<Asset>,
        var _247Assets : List<Asset>
) : Parcelable
