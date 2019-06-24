package com.nbcsports.regional.nbc_rsn.data_bar

import com.nbcsports.regional.nbc_rsn.BuildConfig
import com.nbcsports.regional.nbc_rsn.debug_options.DebugPresenter
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils

object DataBarConfig {
    val STATS_API_KEY: String
        get() {
            return if (PreferenceUtils.getBoolean(DebugPresenter.IS_PROD_STATE, BuildConfig.IS_PROD)) {
                BuildConfig.STATS_PROD_API_KEY
            } else {
                BuildConfig.STATS_DEV_API_KEY
            }
        }

    val ROTO_API_KEY: String
        get() {
            return if (PreferenceUtils.getBoolean(DebugPresenter.IS_PROD_STATE, BuildConfig.IS_PROD)) {
                BuildConfig.ROTO_PROD_API_KEY
            } else {
                BuildConfig.ROTO_DEV_API_KEY
            }
        }
}