package com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper

import android.app.Application
import com.adobe.mobile.Analytics
import com.adobe.mobile.Config
import com.nbcsports.regional.nbc_rsn.BuildConfig
import java.util.HashMap
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelperTags.ADOBE_PASS_MVPD
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelperTags.ADOBE_PASS_NETWORK
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelperTags.CUSTOM_LINK
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelperTags.LINK_TYPE
import com.nbcsports.regional.nbc_rsn.extensions.d

class TrackingHelper {
    /*public HashMap<String, Object> getGlobalValues(){
        HashMap<String, Object> values = new HashMap<>();
        values.put( "channel", "RSN" );
        values.put( "nbcs.platform", "Mobile App" );
        values.put( "nbcs.userid", getUserID() );
        values.put( "nbcs.appversion", "Android:" + getVersion());
        return values;
    }*/

    var userID: String? = null
        get() = if (field == null) "" else field

    companion object {
        fun configureAnalytics(application: Application) {
            Config.setContext(application)
        }

        fun trackAuthEvent(info: AuthInfo?) {
            d("Adobe analytics trackAuthEvent() called with: info = [$info]")

            if (info == null) {
                return
            }

            val values = HashMap<String, Any>()
            values[LINK_TYPE] = "lnk_o"
            values[CUSTOM_LINK] = info.customLink
            values[info.contextData] = "true"
            values[ADOBE_PASS_MVPD] = info.mvpd
            if (info.passAuthentication != "") {
                values[TrackingHelperTags.PASS_AUTHN] = info.passAuthentication
            }
            if (info.passAuthorization != "") {
                values[TrackingHelperTags.PASS_AUTHZ] = info.passAuthorization
            }
            values[ADOBE_PASS_NETWORK] = info.adobePassNetwork
            if (info.adobePassGUID != "") {
                values[TrackingHelperTags.PASS_GUID] = info.adobePassGUID
            }

            Analytics.trackAction(info.customLink, values)

        }

        fun trackPageEvent(info: PageInfo?) {
            d("Adobe analytics trackPageEvent() called with: info = [$info]")
            if (info == null) {
                return
            }

            val values = HashMap<String, Any>()
            val pageName = buildPageName(info)
            //values.put(TrackingHelperTags.PAGE_NAME, pageName);
            if (info.contextData) {
                values[TrackingHelperTags.PASS_LOGIN] = info.contextData
            }

            if (info.businessUnit.isNotBlank()) {
                values[TrackingHelperTags.BUSINESS_UNIT] = info.businessUnit
            }

            if (info.team.isNotBlank()) {
                values[TrackingHelperTags.TEAM] = info.team
            }

            values[TrackingHelperTags.SECTION] = info.section

            if (info.subSection.isNotBlank()) {
                values[TrackingHelperTags.SUB_SECTION] = info.subSection
            }

            if (info.contentType.isNotBlank()) {
                values[TrackingHelperTags.CONTENT_TYPE] = info.contentType
            }

            if (info.adobePassNetwork.isNotBlank()) {
                values[TrackingHelperTags.ADOBE_PASS_NETWORK] = info.adobePassNetwork
            }

            if (info.league.isNotBlank()) {
                values[TrackingHelperTags.LEAGUE] = info.league
            }

            if (info.author.isNotBlank()) {
                values[TrackingHelperTags.AUTHOR] = info.author
            }

            if (info.articleID.isNotBlank()) {
                values[TrackingHelperTags.ASSET_ID] = info.articleID
            }

            if (info.articleTitle.isNotBlank()) {
                values[TrackingHelperTags.ASSET_TITLE] = info.articleTitle
            }

            Analytics.trackState(pageName, values)

        }

        private val appName: String
            get() = "RSN"

        private fun buildPageName(info: PageInfo): String {
            var pageName = appName
            if (info.businessUnit.isNotBlank() && !info.contextData) {
                pageName += ":" + info.businessUnit
            }
            if (info.team.isNotBlank()) {
                pageName += ":" + info.team
            }
            pageName += ":" + info.section
            if (info.subSection.isNotBlank()) {
                pageName += ":" + info.subSection
            }
            return pageName
        }

        // Get app version
        val version: String
            get() = BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE
    }
}
