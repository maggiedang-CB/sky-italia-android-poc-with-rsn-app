package com.nbcsports.regional.nbc_rsn.authentication.clientless_response

data class ShortMediaToken (
        val resource: String,
        val requestor: String,
        val expires: String,
        val serializedToken: String,
        val userId: String,
        val mvpdId: String
)


