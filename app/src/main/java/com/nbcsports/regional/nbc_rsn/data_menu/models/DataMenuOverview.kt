package com.nbcsports.regional.nbc_rsn.data_menu.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DataMenuOverviewDataModel(
        var ctaId: DataMenuCTAId = DataMenuCTAId.NONE,
        var ctaTitle: String = "",
        var ctaSubtitle: String = "",
        var carouselList: List<DataMenuOverviewCarouselDataModel>? = null
) : Parcelable

@Parcelize
data class DataMenuOverviewCarouselDataModel(
        var carouselTitle: String = "",
        var carouselProgress: Float = 0.0f,
        var carouselValue: String = "",
        var carouselLabel: String = "",
        var carouselIsPlaceHolder: Boolean = false
) : Parcelable

enum class DataMenuCTAId {
    NONE,
    ROSTER,
    SCORE,
    SCHEDULE,
    STANDINGS
}