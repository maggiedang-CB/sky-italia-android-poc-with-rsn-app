package com.nbcsports.regional.nbc_rsn.common

import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils

/*
"relativeTime": "421531:35:01",
"relativeTimeDuration": "",
"teamID": "USA",
"teaser": false,
"title": "Lindsey Vonn",
"description": "14:35:00 Stamford Time",
"color": "",
"playID": 1060
*/

data class TimelineMarker(
        val relativeTime: String = "",
        val description: String = "",
        val title: String = "",
        private var _epochTime: Long
) {

    fun getEpochTime(): Long {
        return DateFormatUtils.getEpochFromHHmmss(relativeTime)
    }
}
