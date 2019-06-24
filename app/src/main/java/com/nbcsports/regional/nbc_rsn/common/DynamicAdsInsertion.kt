package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class DynamicAdsInsertion(
    val prerollsEnabled: Boolean,
    val midrollsEnabled: Boolean,
    val opportunityId: String,
    val prerollOpportunityId: String,
    val midrollOpportunityId: String,
    val tabletSiteSectionId: String,
    val siteSectionId: String,
    val sfId: Int,
    val afId: Int
) : Parcelable