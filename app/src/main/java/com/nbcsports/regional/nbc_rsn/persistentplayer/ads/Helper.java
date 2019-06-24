package com.nbcsports.regional.nbc_rsn.persistentplayer.ads;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.nbcsports.regional.nbc_rsn.BuildConfig;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.Asset;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.FreeWheelHash;

import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;

public class Helper {

    public static final String FREEWHEEL_DEFAULT_PROFILE = "nbcu_olympics_mobile_live";
    public static final String FREEWHEEL_NETWORK_ID = "169843";

    public static String getAdobeBootstrapUrl(Context context,
                                              String streamUrl,
                                              Asset asset,
                                              String mvpdHash,
                                              String csid,
                                              String sfid,
                                              String afid,
                                              String profile,
                                              boolean isDebug) {

        if (TextUtils.isEmpty(profile)) profile = FREEWHEEL_DEFAULT_PROFILE;

        String assetId = asset.getId();

        SecureRandom random = new SecureRandom();
        String base64ManifestUrl = Base64.encodeToString(streamUrl.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
        String md5OfVodType = (asset.isLive() || asset.isReplay()) ? "81abd5ebcea265629d8c89e9fbd19d7e": "b7ab0d83c067298ffa48f182a2958032";
        String userId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        String assetDuration = String.valueOf(asset.getLength());

        String adOpportunityId;
        if (asset.isLive()) {
            adOpportunityId = "live";
        } else if (asset.isReplay()) {
            adOpportunityId = "fer";
        } else {
            adOpportunityId = "vod";
        }

        // Build the bootstrap URL string
        StringBuilder sb = new StringBuilder();
        sb.append("http://manifest.auditude.com/auditude/variant/" + assetId);
        //sb.append("http://sandbox.manifest.auditude.com/auditude/variant/" + assetId);
        sb.append("/" + base64ManifestUrl + ".m3u8");

        // Auditude parameters
        sb.append("?u=" + md5OfVodType);
        sb.append("&z=114100");
        sb.append("&live=" + asset.isLive());
        sb.append("&pttrackingmode=sstm");
        sb.append("&pttrackingversion=v1");
        sb.append("&ptcueformat=nbc");
        sb.append("&ptassetid=" + assetId);
        sb.append("&g=1000018");
        sb.append("&pttoken=true");

        // Freewheel parameters
        sb.append("&k=");
        sb.append("nw=" + FREEWHEEL_NETWORK_ID); //Distributor's FreeWheel network ID.
        sb.append(";prof=" + profile); //Player profile that is pre-configured by FreeWheel
        sb.append(";vcid2=" + userId); //Used to support a cookie collection for FW so features based on user state can work in these environments, including UEX, frequency cap, etc.
        sb.append(";caid=" + assetId); //Custom content video asset ID. Indicates "what" video is being played.
        sb.append(";vdur=" + assetDuration); //Content video duration in seconds. Usually used to match Commercial Break Patterns.
        sb.append(";pvrn=" + random.nextInt(Integer.MAX_VALUE)); //A random number generated per page view. It is required for forecasting system to work properly. Rule of thumb: when csid changes, generate new pvrn.
        sb.append(";vprn=" + random.nextInt(Integer.MAX_VALUE)); //A random number generated per video instance (re-randomize on a new video play even on the same page). It is required for forecasting to work properly. Rule of thumb: when caid changes, generate new vprn.
        //sb.append(";vip=" + ipAddress); //should not be overridden by server ip
        sb.append(";_fw_ae=" + mvpdHash); //MVPD hash. See TV Everywhere: http://hub.freewheel.tv/display/MUG/TV+Everywhere:+MVPD+Key+Values , MVPD Key Values for details.
        sb.append(";_fw_h_user_agent=" + getUserAgent());
        sb.append(";metr=1031"); // (the ads can send impressions and quartiles, but no click callbacks) Default capabilities of all ads for this request. It's a bitmap representing whether this integration supports some pre-defined capabilities like clickable, pausable, mutable, etc.
        sb.append(";csid=" + csid); //Custom Site Section ID. Indicates "where" the video is played.  There will be unique brand specific SSIDs provided.
        sb.append(";sfid=" + sfid);
        sb.append(";afid=" + afid);
        sb.append(";AD_OPPORTUNITY_ID=" + adOpportunityId);
        //sb.append(";pb="); //A parameter used only when a special midroll "podbuster" is inserted from the CMS. Used only in live events. Value would be established in CMS.

        sb.append("&ptdebug=" + isDebug);
        sb.append("&__sid__=" + random.nextInt(Integer.MAX_VALUE));

        if (! BuildConfig.IS_PROD) {
            Log.i("ADOBE BOOTSTRAP URL", sb.toString());
        }

        return sb.toString();
    }

    public static String getUserAgent(){
        String deviceName = Build.MODEL;
        String osVersion = Build.VERSION.RELEASE;
        String appVersion = BuildConfig.VERSION_NAME;
        return "Mozilla/5.0 (" + deviceName + "; Android " + osVersion + "); NBCSports/" + appVersion;
    }
    public static String getFreeWheelHash(Config config, Auth auth) {

        if (auth == null){return null;}
        if (auth.getAuthNToken() == null){return null;}

        for(FreeWheelHash freeWheelHash : config.getFreeWheelHashes()){
            if (StringUtils.equalsIgnoreCase(auth.getAuthNToken().getMvpd(), freeWheelHash.getMvpdId())){
                return freeWheelHash.getFreewheelMd5Hash();
            }
        }
        return null;
    }

}
