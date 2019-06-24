package com.nbcsports.regional.nbc_rsn.authentication.clientless_response

import com.google.gson.annotations.SerializedName

data class AuthNToken (
        val mvpd: String,
        val userId: String,
        val requestor: String,
        @SerializedName("expires") val expires: String
)