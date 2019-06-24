package com.nbcsports.regional.nbc_rsn.data_bar

data class LogoConfig(
        val leagues: List<League>
) {
    data class League(
            val leagueName: String,
            val logos: List<Logo>
    ) {

        data class Logo(
                val statsTeamId: Int,
                val logoUrl: String
        )
    }
}