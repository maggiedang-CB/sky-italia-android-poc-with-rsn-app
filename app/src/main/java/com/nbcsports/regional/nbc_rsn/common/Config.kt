package com.nbcsports.regional.nbc_rsn.common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Config(
        val currentStoreVersion: String,
        val highlights: Highlights,
        val playNonFeatureGifs: Boolean = false,
        val affiliates: String = "",
        val liveDataRefreshInterval: Long = -1,
        val teamViewRefreshInterval: Long = -1,
        var teams: List<Team>,
        val sharing: ShareLinks,
        @SerializedName("providers")
        val mvpdProviders: MvpdProviders,
        val dataBar: DataBarConfigInfo?,
        val dataMenu: DataMenuConfigInfo?,
        val liveAssetsUrl: String,
        val liveAssetStartBuffer: Long = 0,
        val adobePass: AdobePass,
        val imagesBaseUrl: String,
        val localization: Localization,
        val settingsSupport: List<SettingsSupport>,
        val editorialDetailsUrl: String,
        val steppedStoryUrl: String,
        @SerializedName("pushNotifications")
        val pushNotificationTags: PushNotificationTags,
        val geolocationCheckUrl: String = "",
        val ads: Ads,
        var freeWheelHashes: List<FreeWheelHash>,
        val totalCast: TotalCast,
        val chromecast: Chromecast,
        val entitlements: Entitlements,
        @SerializedName("reviewPromptLaunchesThreshold")
        val reviewPromptLaunchesThreshold: Int,
        @SerializedName("reviewPromptDaysMaximum")
        val reviewPromptDaysMaximum: Int,
        val switchScreenEnabled: Boolean = false
) : Parcelable {

    @Parcelize
    data class DataBarConfigInfo(
            val baseUrl: String,
            @SerializedName("dataBarV5.7BaseUrl")
            val rotoBaseUrl: String,
            @SerializedName("prevNextFlipTime")
            val prevNextFlipTime: Int,
            val enabled: Boolean,
            val activeLeagues: List<String>,
            val liveGameDelay: Int,
            val teamLogos: String
    ) : Parcelable

    @Parcelize
    data class DataMenuConfigInfo(
            @SerializedName("v5.6+enabled")
            val isEnabled: Boolean = false,
            val activeLeagues: List<String>?,
            val activeDetailsPages: ActiveDetailsPages?,

            // Rotoworld API
            val dataMenuBaseUrl: String = "",
            val dataMenuStandingsSubcomponent: String = "",
            val dataMenuScheduleSubcomponent: String = "",
            val dataMenuScoresSubcomponent: String = "",
            val dataMenuRosterSubcomponent: String = "",

            // Stats.com API
            val statsBaseUrl: String = "",
            val statsMLBSubcomponent: String = "",
            val statsNBASubcomponent: String = "",
            val statsNFLSubcomponent: String = "",
            val statsNHLSubcomponent: String = "",
            val statsStandingsSubcomponent: String = "",
            val statsScheduleSubcomponent: String = "",
            val statsStatisticsSubcomponent: String = "",
            val statsRosterSubcomponent: String = "",
            val statsInjuriesSubcomponent: String = "",
            val statsInjuryDetailsSubcomponent: String = "",
            val statsBoxscoresSubcomponent: String = ""
    ) : Parcelable

    @Parcelize
    data class Highlights(
            val url: String,
            val isEnabled: Boolean = false,
            val pollingInterval: Int = 0
    ) : Parcelable

    @Parcelize
    data class AdobePass(
            val baseUrl: String,
            val checkAuthenticationPath: String,
            val reggieCodePath: String,
            val authenticatePath: String,
            val authnTokenPath: String,
            val authnTokenTimeoutSeconds: Int,
            val authorizePath: String,
            val authzTokenPath: String,
            @SerializedName("multiCDNTokenizationUrl")
            val multiCDNTokenizationUrl: String = "",
            val tokenizationUrl: String,
            val redirectUrl: String,
            val logoutPath: String,
            val tempPassUrl: String,
            val mediaTokenPath: String,
            val tempPassRequestor: String,
            val tempPassProvider: String,
            val getUserMetadataPath: String
    ) : Parcelable

    @Parcelize
    data class TotalCast(
            val zipLookupUrl: String,
            val postBody: String
    ) : Parcelable

    @Parcelize
    data class Chromecast(
            val enabled: Boolean = false,
            val nwId: Long = 0,
            val profileId: String,
            val live: AdValues,
            val nonLive: AdValues
    ) : Parcelable {

        @Parcelize
        data class AdValues(
                val opportunityId: String,
                val siteSectionId: String,
                val sfId: Long = 0,
                val afId: Long = 0

        ) : Parcelable
    }

    @Parcelize
    data class Entitlements(
            val enabled: Boolean,
            val entitlementChannels: ArrayList<String> = ArrayList(),
            val rsnEntitlementsUrl: String,
            val entitlementUrl: String,
            val retransmissionUrl: String,
            val nbcBlackoutUrl: String,
            val mlbBlackoutUrl: String,
            val gmoKey: String,
            val gmoDeviceType: String,
            val nbcDomain: String,
            val gmoDomain: String
    ) : Parcelable
}

@Parcelize
data class ShareLinks(
        val smartLink: String,
        val smartLinkStg: String,
        @SerializedName("facebookLink")
        val facebookShareBaseUrl: String
) : Parcelable

@Parcelize
data class MvpdProviders(
        val logosUrl: String,
        val requestorIds: String,
        val freewheelHashesUrl: String
) : Parcelable


@Parcelize
data class Localization(
        val languageId: String,
        val url: String

) : Parcelable

@Parcelize
data class PushNotificationTags(
        val breakingNewsTag: String,
        val teamNewsTag: String,
        val teamGameStartTag: String,
        val teamFinalScoreTag: String
) : Parcelable

@Parcelize
data class ActiveDetailsPages(
        val score: Boolean = true,
        val roster: Boolean = true,
        val schedule: Boolean = true,
        val standings: Boolean = true
) : Parcelable
