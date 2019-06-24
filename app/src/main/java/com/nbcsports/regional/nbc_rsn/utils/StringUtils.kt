package com.nbcsports.regional.nbc_rsn.utils

import android.annotation.SuppressLint
import android.os.Build

object StringUtils {

    @SuppressLint("DefaultLocale")
    fun getOrdinalString(period: Int?): String {
        if (period == null) {
            return ""
        }

        val format = "{0,ordinal}"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            android.icu.text.MessageFormat.format(format, period).toUpperCase()
        } else {
            com.ibm.icu.text.MessageFormat.format(format, period).toUpperCase()
        }
    }
}
