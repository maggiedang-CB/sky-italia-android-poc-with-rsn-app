package com.nbcsports.regional.nbc_rsn.data_bar

import com.google.gson.annotations.SerializedName

data class Affiliates(val affiliates: List<Affiliate>?)
data class Affiliate(
        @SerializedName("id")
        val affiliateId: String = "",
        val station: String = "",
        val station_es: String = "",
        val dmaCode: String = ""
)