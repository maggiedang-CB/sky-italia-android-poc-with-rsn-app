package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MvpdProviderUrl (
    @SerializedName("MVPD NAME")
    var providerName: String? = null,
    @SerializedName("ADOBE PASS MVPD ID")
    var providerID: String? = null,
    @SerializedName("Top Bar - Android Phone 90x24")
    var smallSizeLogo: String? = null,
    @SerializedName("Picker - Android Phones 156x90")
    var largeLogo: String? = null,
    @SerializedName("Top Bar- iPhone4 120x30")
    var iphoneLogo: String? = null,
    @SerializedName("STATUS")
    var status: String? = null,
    @SerializedName("MVPD URL")
    var mvpdUrl: String? = null
) : Parcelable
//Rename