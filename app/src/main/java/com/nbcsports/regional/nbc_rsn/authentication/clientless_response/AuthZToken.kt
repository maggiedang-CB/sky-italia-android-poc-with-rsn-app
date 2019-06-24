package com.nbcsports.regional.nbc_rsn.authentication.clientless_response

data class AuthZToken (
        val mvpd: String,
        val resource: String,
        val requestor: String,
        val expires: String,
        val proxyMvpd: String
)

