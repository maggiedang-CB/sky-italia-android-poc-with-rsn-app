package com.nbcsports.regional.nbc_rsn.common

import com.google.gson.annotations.SerializedName

data class IpGeolocation(
        val continent: String,
        @SerializedName("country_code")
        val countryCode: String,
        @SerializedName("region_code")
        val regionCode: String,
        val city: String,
        val dma: String,
        val msa: String,
        val pmsa: String,
        @SerializedName("areacode")
        val areaCode: String,
        @SerializedName("lat")
        val latitude: String,
        @SerializedName("long")
        val longitude: String,
        val county: String,
        val timezone: String,
        val zip: String,
        val military: String,
        val xffip: String,
        val fips: String
)