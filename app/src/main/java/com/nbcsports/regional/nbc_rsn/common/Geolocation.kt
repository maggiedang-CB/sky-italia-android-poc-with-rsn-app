package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Geolocation (
        val latitude: Float,
        val longitude: Float
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other as Geolocation

        if (this.latitude != other.latitude) return false
        if (this.longitude != other.longitude) return false

        return true
    }
}