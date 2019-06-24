package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SettingsSupport (
        var localizationsKey: String? = null,
        var action: String? = null,
        var url: String? = null
) : Parcelable