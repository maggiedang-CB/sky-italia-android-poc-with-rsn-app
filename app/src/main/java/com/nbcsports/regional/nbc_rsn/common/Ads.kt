package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ads (
    val live: DynamicAdsInsertion?,
    val nonLive: DynamicAdsInsertion?,
    val auditudeZone: Int,
    val auditudeDomain: String,
    val nwId: Int

): Parcelable