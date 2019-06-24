package com.nbcsports.regional.nbc_rsn.data_menu.score

import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager


interface NamedEnum {

    companion object {
        fun getName(n: NamedEnum): String =
                if (LocalizationManager.isInitialized()) {
                    n.localizationName()
                } else {
                    n.name
                }
    }

    val name: String
    val localizationName: () -> String
}
