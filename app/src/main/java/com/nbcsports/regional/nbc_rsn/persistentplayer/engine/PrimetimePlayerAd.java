package com.nbcsports.regional.nbc_rsn.persistentplayer.engine;

import android.content.Context;
import android.provider.Settings;

import com.adobe.mediacore.metadata.AdSignalingMode;
import com.adobe.mediacore.metadata.AuditudeSettings;
import com.adobe.mediacore.metadata.DefaultMetadataKeys;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.MetadataNode;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.RsnApplication;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.Asset;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.ads.Helper;
import com.nbcsports.regional.nbc_rsn.persistentplayer.ads.NbcAdvertisingFactory;

import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;

public class PrimetimePlayerAd {

    public static String PLATFORM = "android";
    private static final String AD_MEDIA_ID = "nbcsports_default_vod";
    private static final String ADVERTISING_ID = "optout";
    public static final String ADOBE_CLIENT_ADS = "default";
    public static final String LIVE_PREROLL_OPPORTUNITY_ID = "preroll";
    public static final String LIVE_MIDROLL_OPPORTUNITY_ID = "midroll";
    public static final String VOD_PREROLL_OPPORTUNITY_ID = "vod";
    static String NIELSEN_APP_ID_FREEWHEEL = "PB01313FD-FAC3-4AC4-A154-6503E334A0D2";

    public static AuditudeSettings result;

    public static MetadataNode createMetadata(Context context, MediaSource mediaSource, Auth auth) {

        Config config = null;
        if (context instanceof MainActivity){
            config = ((MainActivity) context).getConfig();
        }
        MetadataNode metadata = new MetadataNode();
        if (config == null){
            return metadata;
        }
        String csid = getSiteSectionId(config, context, mediaSource);
        String sfid = getSfidForAsset(config, mediaSource);
        String afid = getAfidForAsset(config, mediaSource);
        String freeWheelHash = Helper.getFreeWheelHash(config, auth);
        String assetId = getAssetId(mediaSource);
        createAuditudeMetadata(csid, sfid, afid, config, assetId, freeWheelHash, mediaSource);
        metadata.setNode(DefaultMetadataKeys.AUDITUDE_METADATA_KEY.getValue(), result);
        return metadata;
    }

    private static String getAssetId(MediaSource mediaSource) {
        String assetId = "";

        // get the assetId from videoSource
        String videoAssetID = "";
        if (mediaSource.getAsset() != null && mediaSource.getAsset().getVideoSources() != null){
            if (! mediaSource.getAsset().getVideoSources().isEmpty()){
                Asset.VideoSource videoSource = mediaSource.getAsset().getVideoSources().get(0);
                if (videoSource != null){
                    videoAssetID = videoSource.getAssetID();
                }
            }
        }
        if (! StringUtils.isEmpty(videoAssetID)) {
            assetId = videoAssetID;
        } else if (!StringUtils.isEmpty(mediaSource.getPid())){
            assetId = mediaSource.getPid();
        } else if (!StringUtils.isEmpty(mediaSource.getId())){
            assetId = mediaSource.getId();
        }
        return assetId;
    }


    private static String getAfidForAsset(Config config, MediaSource mediaSource) {
        int afid = 0;
        if (config == null || config.getAds() == null || mediaSource == null){
            return Integer.toString(afid);
        }
        // 1. Get live asset's afId
        // 2. Get vod's afId
        if (mediaSource.getLive() && config.getAds().getLive() != null){
            afid = config.getAds().getLive().getAfId();
        } else if (!mediaSource.getLive() && config.getAds().getNonLive() != null){
            afid = config.getAds().getNonLive().getAfId();
        }
        return Integer.toString(afid);
    }


    private static String getSfidForAsset(Config config, MediaSource mediaSource) {
        int sfid = 0;
        if (config == null || config.getAds() == null || mediaSource == null){
            return Integer.toString(sfid);
        }
        // 1. Get live asset's sfId
        // 2. Get vod's sfId
        if (mediaSource.getLive() && config.getAds().getLive() != null){
            sfid = config.getAds().getLive().getSfId();
        } else if (!mediaSource.getLive() && config.getAds().getNonLive() != null){
            sfid = config.getAds().getNonLive().getSfId();
        }
        return Integer.toString(sfid);
    }

    public static String getSiteSectionId(Config config, Context context, MediaSource mediaSource) {
        String siteSection = "";

        if (config == null || config.getAds() == null || mediaSource == null){ return siteSection; }

        if (context != null && context.getResources() != null) {
            boolean tabletSize = context.getResources().getBoolean(R.bool.isTablet);
            // 1. Get live asset's tabletSiteSectionId
            // 2. Get live asset's siteSectionId
            // 3. Get vod's tabletSiteSectionId
            // 4. Get vod's siteSectionId
            if (tabletSize && mediaSource.getLive() && config.getAds().getLive() != null
                    && config.getAds().getLive().getTabletSiteSectionId() != null){
                siteSection = config.getAds().getLive().getTabletSiteSectionId();
            } else if (!tabletSize && mediaSource.getLive() && config.getAds().getLive() != null
                    && config.getAds().getLive().getSiteSectionId() != null){
                siteSection = config.getAds().getLive().getSiteSectionId();
            } else if (tabletSize && !mediaSource.getLive() && config.getAds().getNonLive() != null
                    && config.getAds().getNonLive().getTabletSiteSectionId() != null){
                siteSection = config.getAds().getNonLive().getTabletSiteSectionId();
            } else if (!tabletSize && !mediaSource.getLive() && config.getAds().getNonLive() != null
                    && config.getAds().getNonLive().getSiteSectionId() != null){
                siteSection = config.getAds().getNonLive().getSiteSectionId();
            }
        }

        return siteSection;
    }

    /**
     * This method is used to get preroll ad opportunity id for live stream
     *
     * @param config
     * @return preroll ad opportunity id from config
     */
    public static String getLivePrerollOpportunityId(Config config) {
        String adOpportunityId = LIVE_PREROLL_OPPORTUNITY_ID;
        if (config != null
                && config.getAds() != null
                && config.getAds().getLive() != null
                && config.getAds().getLive().getPrerollOpportunityId() != null){
            adOpportunityId = config.getAds().getLive().getPrerollOpportunityId();
        }
        return adOpportunityId;
    }

    /**
     * This method is used to get midroll ad opportunity id for live stream
     *
     * @param config
     * @return midroll ad opportunity id from config
     */
    public static String getLiveMidrollOpportunityId(Config config) {
        String adOpportunityId = LIVE_MIDROLL_OPPORTUNITY_ID;
        if (config != null
                && config.getAds() != null
                && config.getAds().getLive() != null
                && config.getAds().getLive().getMidrollOpportunityId() != null){
            adOpportunityId = config.getAds().getLive().getMidrollOpportunityId();
        }
        return adOpportunityId;
    }

    /**
     * This method is used to get preroll ad opportunity id for vod
     *
     * @param config
     * @return preroll ad opportunity id from config
     */
    public static String getVodPrerollOpportunityId(Config config) {
        String adOpportunityId = VOD_PREROLL_OPPORTUNITY_ID;
        if (config != null
                && config.getAds() != null
                && config.getAds().getNonLive() != null
                && config.getAds().getNonLive().getOpportunityId() != null){
            adOpportunityId = config.getAds().getNonLive().getOpportunityId();
        }
        return adOpportunityId;
    }

    private static void createAuditudeMetadata(String csid, String sfid, String afid, Config config,
                                                           String assetId,
                                                           String mvpdFreewheelHash, MediaSource mediaSource) {
        result = new AuditudeSettings();
        String platform = PLATFORM;

        // Check if preroll and midroll are enabled
        // 1. Below checks will handle the cases where
        //     a) live asset && preroll: enable && midroll: enable -> adOpportunity = "preroll"
        //     b) live asset && preroll: enable && midroll: disable -> adOpportunity = "preroll"
        //     c) live asset && preroll: disable && midroll: enable -> adOpportunity = "midroll"
        //     d) live asset && preroll: disable && midroll: disable -> adOpportunity = ""
        //     e) vod && preroll: enable -> adOpportunity = "vod"
        //     f) vod && preroll: disable -> adOpportunity = ""
        // 2. Checking order is matter, need to check midroll first
        String adOpportunity = "";
        if (mediaSource != null && mediaSource.getLive()
                && config.getAds() != null
                && config.getAds().getLive() != null
                && config.getAds().getLive().getMidrollsEnabled()){
            adOpportunity = getLiveMidrollOpportunityId(config);
            result.setSignalingMode(AdSignalingMode.MANIFEST_CUES);
        }
        if (mediaSource != null && mediaSource.getLive()
                && config.getAds() != null
                && config.getAds().getLive() != null
                && config.getAds().getLive().getPrerollsEnabled()){
            adOpportunity = getLivePrerollOpportunityId(config);
            result.setSignalingMode(AdSignalingMode.MANIFEST_CUES);
        }
        if (mediaSource != null && !mediaSource.getLive()
                && config.getAds() != null
                && config.getAds().getNonLive() != null
                && config.getAds().getNonLive().getPrerollsEnabled()){
            adOpportunity = getVodPrerollOpportunityId(config);
            result.setSignalingMode(AdSignalingMode.SERVER_MAP);
        }

        String zoneId = Integer.toString(config.getAds().getAuditudeZone());
        result.setZoneId(zoneId);

        String domain = config.getAds().getAuditudeDomain();
        result.setDomain(domain);
        result.setMediaId(AD_MEDIA_ID);

        final Metadata customParameters = new MetadataNode();
        customParameters.setValue("PROFILE_ID", "nbcu_olympics_mobile_live");

        String networkId = Integer.toString(config.getAds().getNwId());
        customParameters.setValue("NW_ID", networkId);
        customParameters.setValue("AF_ID", afid);
        customParameters.setValue("ASSET_ID", assetId);
        customParameters.setValue("SF_ID", sfid);
        customParameters.setValue("crtp", "vast3ap");

        String siteSectionID = csid;

        customParameters.setValue("SITE_SECTION_ID", siteSectionID);
        customParameters.setValue("POS_ID", "0");

        String advertisingID = ADVERTISING_ID;
        if (advertisingID == null) advertisingID = "";

        String deviceID = getDeviceId();
        if (advertisingID == "optout") deviceID = advertisingID;

        // comscore
        String comscore = "&comscore_did_x=none"
                + "&comscore_impl_type=none"
                + "&comscore_platform=none"
                + "&comscore_device=none";

        if (advertisingID != "optout") {
            comscore = "&comscore_did_x=" + URLEncoder.encode(advertisingID)
                    + "&comscore_impl_type=a"
                    + "&comscore_platform=android"
                    + "&comscore_device=" + URLEncoder.encode(android.os.Build.MODEL);
        }

        customParameters.setValue("MVPD", mvpdFreewheelHash
                + "&_fw_h_user_agent=" + URLEncoder.encode(Helper.getUserAgent())
                + "&_fw_vcid2=" + URLEncoder.encode(getDeviceId())
                + "&_fw_did_google_advertising_id=" + URLEncoder.encode(advertisingID)
                + "&_fw_did_android_id=" +  URLEncoder.encode(getDeviceId())
                + "&metr=7"
                );


        customParameters.setValue("TIME_POSITION", "0.000000");
        customParameters.setValue("AD_OPPORTUNITY_ID", adOpportunity);
        customParameters.setValue( "devid", advertisingID);
        customParameters.setValue( "DEVICE_ID", "devid," + URLEncoder.encode(advertisingID));


        //app pid for adRequest. Hardcoded by requirements: https://nbcsports.atlassian.net/browse/LEAPP-606
        customParameters.setValue("asid,", "air.com.nbcuni.com.nbcsports.liveextra");
        result.setCustomParameters(customParameters);
        result.setUserAgent(Helper.getUserAgent().replace("nielsen", "")); //for some crazy reason, freewheel doesnt like the string "nielsen" in the UA

        final Metadata targetingParameters = new MetadataNode();
        targetingParameters.setValue("AD_OPPORTUNITY_ID", adOpportunity);
        targetingParameters.setValue("device", platform);

        result.setTargetingParameters(targetingParameters);
        result.setCreativeRepackagingEnabled(true);
        result.setFallbackOnInvalidCreativeEnabled(true);
    }

    private static String getDeviceId() {
        return Settings.Secure.getString(RsnApplication.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static NbcAdvertisingFactory createAdClient(Context context, MediaSource mediaSource) {
        String pid;
        if (! StringUtils.isEmpty(mediaSource.getPid())){
            pid = mediaSource.getPid();
        } else {
            pid = mediaSource.getId();
        }
        String channel;
        if (! StringUtils.isEmpty(mediaSource.getChannel())){
            channel = mediaSource.getChannel();
        } else {
            channel = mediaSource.getRequestorId();
        }

        NbcAdvertisingFactory adClient = new NbcAdvertisingFactory(AdSignalingMode.MANIFEST_CUES,
                context, pid, channel, true);
        return adClient;
    }

    /**
     * This method is used to modify the existing AuditudeSettings (that associate with
     * the live stream currently playing) to midroll of live stream
     *
     * @param context
     */
    public static void setUpMidrolls(Context context, MediaSource mediaSource) {
        // Check result and context
        if (result == null || !(context instanceof MainActivity)) return;

        // Check if midroll is enabled
        Config config = ((MainActivity) context).getConfig();
        if (config == null
                || config.getAds() == null
                || config.getAds().getLive() == null
                || !config.getAds().getLive().getMidrollsEnabled()) return;

        // Get midrollOpportunityId from config
        String midrollOpportunityId = getLiveMidrollOpportunityId(config);

        // Set up midroll with existing AuditudeSettings
        String csid = getSiteSectionId(config, context, mediaSource);
        Metadata newTargetingParams = new MetadataNode();
        String adOpportunityId = result.getTargetingParameters().getValue("AD_OPPORTUNITY_ID");
        if (adOpportunityId != null && adOpportunityId.equalsIgnoreCase(midrollOpportunityId)) {
            Metadata customParameters = result.getCustomParameters();
            customParameters.setValue("SITE_SECTION_ID", csid);
            result.setCustomParameters(customParameters);
            return;
        }

        newTargetingParams.setValue("AD_OPPORTUNITY_ID", midrollOpportunityId);
        String platform = PLATFORM;
        newTargetingParams.setValue("device", platform);

        Metadata customParameters = result.getCustomParameters();
        customParameters.setValue("SITE_SECTION_ID", csid);
        customParameters.setValue("AD_OPPORTUNITY_ID", midrollOpportunityId);
        result.setCustomParameters(customParameters);

        result.setTargetingParameters(newTargetingParams);
    }
}
