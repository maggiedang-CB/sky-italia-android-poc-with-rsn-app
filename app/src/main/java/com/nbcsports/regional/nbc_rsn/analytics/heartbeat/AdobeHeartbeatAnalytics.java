package com.nbcsports.regional.nbc_rsn.analytics.heartbeat;

import android.content.Context;
import android.os.Build;
import androidx.core.hardware.display.DisplayManagerCompat;
import android.text.TextUtils;
import android.view.Display;

import com.adobe.mediacore.MediaPlayer;
import com.adobe.mediacore.Version;
import com.adobe.mediacore.metadata.DefaultMetadataKeys;
import com.adobe.mediacore.metadata.MetadataNode;
import com.adobe.mediacore.timeline.advertising.Ad;
import com.adobe.mediacore.videoanalytics.VideoAnalyticsMetadata;
import com.adobe.mediacore.videoanalytics.VideoAnalyticsProvider;
import com.nbcsports.regional.nbc_rsn.BuildConfig;
import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.analytics.PlayerAnalytics;
import com.nbcsports.regional.nbc_rsn.authentication.Auth;
import com.nbcsports.regional.nbc_rsn.common.Asset;
import com.nbcsports.regional.nbc_rsn.common.Config;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PrimetimePlayerAd;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.HashMap;

public class AdobeHeartbeatAnalytics implements PlayerAnalytics {

    public final static String CLIENT_ID = "us-800148";

    private final Context context;
    private final DisplayManagerCompat displayService;
    private final PersistentPlayer persistentPlayer;
    private Config config;
    private MediaPlayer player;
    private Auth auth;
    private String videonetwork;

    private Asset asset;
    private VideoAnalyticsProvider videoAnalyticsProvider;

    public AdobeHeartbeatAnalytics(Context context,
                                   Auth authorization,
                                   PersistentPlayer persistentPlayer,
                                   Config configuration) {
        this.context = context;
        this.auth = authorization;
        this.persistentPlayer = persistentPlayer;
        this.config = configuration;

        // TODO: map to channel
        this.videonetwork = context.getString(R.string.app_name);
        displayService = DisplayManagerCompat.getInstance(context.getApplicationContext());
    }

    public void attachPlayer(final MediaPlayer player) {
        this.player = player;
        videoAnalyticsProvider = new VideoAnalyticsProvider(context.getApplicationContext());
        videoAnalyticsProvider.attachMediaPlayer(player);
    }

    public void detachPlayer() {
        if (videoAnalyticsProvider != null){
            videoAnalyticsProvider.detachMediaPlayer();
        }
    }

    public void trackVideoComplete(){
        if (videoAnalyticsProvider != null){
            videoAnalyticsProvider.trackVideoComplete();
        }
    }

    @Override
    public void onDestroy() {
        if (player == null) return;
        detachPlayer();
    }

    @Override
    public void onResume() {}

    public void onPause() {}

    public void startPlayback(Asset asset, MetadataNode metadata, MediaSource mediaSource) {

        this.asset = asset;

        // RSN assets do not have videoSources
        // this.viewModel.initSelectedSource(context.getResources().getString(R.string.device));

        if (asset != null && !TextUtils.isEmpty(asset.getDisplayLogo())) {
            this.videonetwork = asset.getDisplayLogo();
        }

        VideoAnalyticsMetadata vaMetadata = (VideoAnalyticsMetadata) metadata.getNode(DefaultMetadataKeys.VIDEO_ANALYTICS_METADATA_KEY.getValue());
        if (vaMetadata == null) {
            vaMetadata = new VideoAnalyticsMetadata();
            metadata.setNode(DefaultMetadataKeys.VIDEO_ANALYTICS_METADATA_KEY.getValue(), vaMetadata);
        }

        vaMetadata.debugLogging = BuildConfig.DEBUG;
        vaMetadata.quietMode = BuildConfig.DEBUG;

        vaMetadata.setTrackingServer("nbcume.hb.omtrdc.net");
        vaMetadata.setPublisher("nbc");

        setStandardMetadata(vaMetadata, mediaSource);
        setCustomMetadata(vaMetadata, mediaSource);

        com.adobe.mobile.Config.setContext(context.getApplicationContext());
    }

    private void setStandardMetadata(VideoAnalyticsMetadata metadata, MediaSource mediaSource) {

        // deal with friendly name:
        // a.media.friendlyname
        if (asset != null && ! StringUtils.isEmpty(asset.getTitle())){
            metadata.setVideoName(asset.getTitle());
        } else if (mediaSource != null && ! StringUtils.isEmpty(mediaSource.getTitle())) {
            metadata.setVideoName(mediaSource.getTitle());
        } else {
            metadata.setVideoName("");
        }

        if (asset != null){
            String videoId = asset.getId();
            if (videoId == null)
                videoId = !mediaSource.getLive() ? asset.getId() : asset.getPid();
            if (videoId != null) {
                metadata.setValue("videoId", videoId);
            }
            metadata.setValue("videoLength", String.valueOf(asset.getDuration()));
            metadata.setValue("streamType", String.valueOf(asset.getStatus()));
        }

        if (asset != null && !TextUtils.isEmpty(asset.getDisplayLogo())) {
            metadata.setChannel(asset.getDisplayLogo());
        } else {
            metadata.setChannel(StringUtils.defaultIfEmpty(videonetwork, ""));
        }
        metadata.setAppVersion(Version.getVersion());
        metadata.setValue("playerName", "Primetime Player v" + Version.getVersion());
    }

    private void setCustomMetadata(VideoAnalyticsMetadata metadata, MediaSource mediaSource) {

        metadata.setVideoMetadataBlock(new VideoAnalyticsMetadata.VideoMetadataBlock() {
            @Override
            public HashMap<String, String> call() {
                HashMap<String, String> map = new HashMap<>();

                String videouserid = "";
                if (auth != null){
                    if (auth.getAuthNToken() != null && auth.getAuthNToken().getMvpd() != null) {
                        map.put("videomvpd", auth.getAuthNToken().getMvpd());
                    }
                    if (auth.getAuthNToken() != null){
                        videouserid = StringUtils.defaultIfEmpty(auth.getAuthNToken().getUserId(), "");
                    }
                }

                if (asset != null){
                    map.put("videoguid", !mediaSource.getLive() ? asset.getId() : asset.getPid());
                    if (asset.getSport() != null) {
                        map.put("videosport", StringUtils.defaultIfEmpty(asset.getSportName(), ""));
                    }

                    if (asset.getLeague() != null) {
                        map.put("videoleague", StringUtils.defaultIfEmpty(asset.getLeague(), ""));
                    }

                    map.put("videostatus", asset.isFree() ? "Unrestricted" : "Restricted");
                    map.put("videoairdate", asset.getStartDateTime().toString());
                    map.put("videobroadcast", asset.getBroadcastEvent() ? "Broadcast" : "Digital");
                    map.put("videoresearchtitle", StringUtils.defaultIfEmpty(asset.getResearchTitle(), ""));
                    map.put("videorsn", StringUtils.defaultIfEmpty(asset.getChannel(), ""));
                    map.put("videonetwork", StringUtils.defaultIfEmpty(asset.getChannel(), ""));
                    map.put("videorequestorid", StringUtils.defaultIfEmpty(asset.getChannel(), ""));
                } else {
                    map.put("videostatus", "Unrestricted");
                }

                map.put("videoscreen", getScreenState(persistentPlayer));
                // no selected source/video sources in RSN assets
                map.put("videocamtype", context.getString(R.string.app_name));

                DateTime start = DateTime.now();
                map.put("videominute", start.toString(DateTimeFormat.forPattern("HH:mm")));
                map.put("videohour", start.toString(DateTimeFormat.forPattern("HH:00")));
                map.put("videoday", start.toString(DateTimeFormat.forPattern("EEEE")));
                map.put("videodate", start.toString(DateTimeFormat.forPattern("MM/dd/yyyy")));

                String language = player.getCurrentItem().getSelectedAudioTrack().getLanguage();
                if (language.equalsIgnoreCase("unknown")) {
                    language = player.getCurrentItem().getSelectedAudioTrack().getName();
                }

                map.put("videolanguage", StringUtils.defaultIfEmpty(language, ""));

                String sponsor;
                sponsor = PrimetimePlayerAd.getSiteSectionId(config, context, mediaSource);
                if (sponsor != null && !sponsor.isEmpty()) {
                    map.put("videosponsor", sponsor);
                }

                map.put("videoplayertech", "Primetime: Android");
                map.put("videoplatform", "Mobile");
                map.put("videoapp", getVersion());
                map.put("videouserid", videouserid);
                map.put("videocrossdevice", "F");

                return map;
            }
        });

        metadata.setAdMetadataBlock(new VideoAnalyticsMetadata.AdMetadataBlock() {

            @Override
            public HashMap<String, String> call(Ad ad) {

                HashMap<String, String> map = new HashMap<>();

                if (asset != null){

                    map.put("videoguid", !mediaSource.getLive() ? asset.getId() : asset.getPid());
                    if (asset.getChannel() != null) {
                        map.put("videorequestorid", StringUtils.defaultIfEmpty(asset.getChannel(), ""));
                    }

                    if (asset.getSport() != null) {
                        map.put("videosport", StringUtils.defaultIfEmpty(asset.getSportName(), ""));
                    }

                    if (asset.getLeague() != null) {
                        map.put("videoleague", StringUtils.defaultIfEmpty(asset.getLeague(), ""));
                    }

                    map.put("videostatus", asset.isFree() ? "Unrestricted" : "Restricted");
                    map.put("videorsn", StringUtils.defaultIfEmpty(asset.getChannel(), ""));
                    map.put("videoairdate", asset.getStartDateTime().toString());
                }

                map.put("videonetwork", StringUtils.defaultIfEmpty(videonetwork, ""));


                if (auth != null && auth.getAuthNToken() != null && auth.getAuthNToken().getMvpd() != null) {
                    map.put("videomvpd", auth.getAuthNToken().getMvpd());
                }

                HashMap<String, String> adMetadata = new HashMap<String, String>();

                map.put("videoscreen", getScreenState(persistentPlayer));
                map.put("videocamtype", context.getString(R.string.app_name));

                DateTime start = DateTime.now();
                map.put("videominute", start.toString(DateTimeFormat.forPattern("HH:mm")));
                map.put("videohour", start.toString(DateTimeFormat.forPattern("HH:00")));
                map.put("videoday", start.toString(DateTimeFormat.forPattern("EEEE")));
                map.put("videodate", start.toString(DateTimeFormat.forPattern("MM/dd/yyyy")));

                String language = player.getCurrentItem().getSelectedAudioTrack().getLanguage();
                if (language.equalsIgnoreCase("unknown")) {
                    language = player.getCurrentItem().getSelectedAudioTrack().getName();
                }

                map.put("videolanguage", StringUtils.defaultIfEmpty(language, ""));

                String sponsor;
                sponsor = PrimetimePlayerAd.getSiteSectionId(config, context, mediaSource);
                if (sponsor != null && !sponsor.isEmpty()) {
                    map.put("videosponsor", sponsor);
                }

                map.put("videoplayertech", "Primetime: Android");
                map.put("videoplatform", "Mobile");
                map.put("videoapp", getVersion());
                String videouserid = "";
                if (auth != null && auth.getAuthNToken() != null){
                    videouserid = StringUtils.defaultIfEmpty(auth.getAuthNToken().getUserId(), "");
                }
                map.put("videouserid", StringUtils.defaultIfEmpty(videouserid , ""));

                return map;
            }
        });
    }

    private String getAdType() {

        // instead of getting adType from the onAdBreakStart event in the ad listener, get it manually.
        // this was suggested by Adobe engineers from adobe ticket #33226
        // also, nielsen is looking for ad type strings of either "preroll" or "midroll"

        if (player.getCurrentTime() == 0) {
            return "preroll";
        }
        return "midroll";
    }

    public String getScreenState(PersistentPlayer persistentPlayer) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display[] displays = displayService.getDisplays(DisplayManagerCompat.DISPLAY_CATEGORY_PRESENTATION);
            boolean hasPresentationDisplay = displays.length > 0;
            if (hasPresentationDisplay) return "External";
        }
        return persistentPlayer.getType() == PlayerConstants.Type.LANDSCAPE ? "Full Screen" : "Normal";
    }

    public static String getVersion() {
        // Get app version
        return BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE;
    }
}

