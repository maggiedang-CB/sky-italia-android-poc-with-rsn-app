package com.nbcsports.regional.nbc_rsn.authentication.clientless_response

data class UserMetadata (
       val data: Data
) {
    data class Data (
            val hba_status: String,
            val upstreamUserID: String,
            val userID: String,
            val mvpd: String,
            val encryptedZip: String,
            val zip: String
    )
}
