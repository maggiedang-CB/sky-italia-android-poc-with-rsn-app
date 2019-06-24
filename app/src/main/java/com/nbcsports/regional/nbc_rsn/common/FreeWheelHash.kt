package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FreeWheelHash (
        val mvpdName: String? = "",
        val mvpdId: String? = "",
        val freewheelMd5Hash: String? = ""
): Parcelable

