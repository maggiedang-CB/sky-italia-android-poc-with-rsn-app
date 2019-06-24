package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MvpdRequestor (
    @SerializedName("resourceID")
    var resourceID: String? = null,
    @SerializedName("requestorID")
    var requestorID: String? = null,
    @SerializedName("Signed Requestor ID")
    var signedID: String? = null,
    @SerializedName("mvpdPremium")
    var mvpdPremium: String? = null,
    var mvpdStandard: String? = null
) : Parcelable
//Rename