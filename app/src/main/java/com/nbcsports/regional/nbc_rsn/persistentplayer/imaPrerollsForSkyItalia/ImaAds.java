package com.nbcsports.regional.nbc_rsn.persistentplayer.imaPrerollsForSkyItalia;

import android.content.Context;
import android.view.ViewGroup;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent.AdErrorListener;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsLoader.AdsLoadedListener;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;

import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract;
import com.nbcsports.regional.nbc_rsn.persistentplayer.PlayerConstants;
import com.nbcsports.regional.nbc_rsn.persistentplayer.engine.PrimetimePlayerEngine;

import lombok.Getter;
import lombok.Setter;
import timber.log.Timber;

/**
 *  For [Sky Italia] Sample
 */
public class ImaAds {

    private Context context;

    private ImaSdkFactory imaFactory;
    @Getter
    private AdsLoader adsLoader;
    @Getter @Setter
    private AdsManager adsManager;
    @Getter @Setter
    private boolean isAdDisplayed = false;

    // TAG for ad ( Testing ). URL of the ad's VAST XML.
    @Setter
    private String adTag = "https://pubads.g.doubleclick.net/gampad/ads?sz=400x300&iu=/316816995/tagred&cust_params=camp%3Dasus20170115&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]";
    //private String adTag = "http://link.theplatform.com/s/BxmELC/media/aeTqK6a5kdUm?feed=Mobile_Feed%20v3-%20Mobile%2FTablet&formats=m3u%2Cmpeg4";
    //private PersistentPlayerContract.View currentView;
    private ViewGroup currentView;
    //private PrimetimePlayerEngine pTPlayer;
    private PersistentPlayer pTPlayer;

    /**
     * @param context
     */
    public ImaAds(Context context, ViewGroup currentView, PersistentPlayer player) {
        this.context = context;
        this.currentView = currentView;
        this.pTPlayer = player;
    }

    /**
     * From : https://developers.google.com/interactive-media-ads/docs/sdks/android/
     * Create an Ads loader using IMA SDK
     */
    public void initAdsLoader() {
        Timber.d("[Sky Italia] InitAdsLoader");
        imaFactory = ImaSdkFactory.getInstance();
        AdDisplayContainer adDisplayContainer = imaFactory.createAdDisplayContainer();
        //adDisplayContainer.setAdContainer(currentView.getPrimetimeView());
        adDisplayContainer.setAdContainer(currentView);
        ImaSdkSettings settings = imaFactory.createImaSdkSettings();
        adsLoader = imaFactory.createAdsLoader(context, settings, adDisplayContainer);

        // Attach a listener
        adsLoader.addAdsLoadedListener(new AdsLoadedListener() {
            @Override
            public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
                // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
                // events for ad playback and errors.
                adsManager = adsManagerLoadedEvent.getAdsManager();
                Timber.d("[Sky Italia] on Ads Manager Loaded");
                // Attach event and error event listeners.
                //mAdsManager.addAdErrorListener(VideoFragment.this);
                adsManager.addAdEventListener(pTPlayer);
                //adsManager.addAdEventListener((PrimetimePlayerEngine) pTPlayer.getPlayerEngine());

                adsManager.init();
                // start ad manager
                adsManager.start();
            }
        });
    }

    /**
     * From: https://developers.google.com/interactive-media-ads/docs/sdks/android/
     * Request video ads from the given VAST ad tag.
     */
    public void requestAds() {
        // Create the ads request.
        AdsRequest request = imaFactory.createAdsRequest();
        // Set VAST XML URL of the ad
        request.setAdTagUrl(adTag);

        /*
        request.setContentProgressProvider(new ContentProgressProvider() {
            @Override
            public VideoProgressUpdate getContentProgress() {
                if (isAdDisplayed || pTPlayer == null || pTPlayer.getPlayerEngine().getDuration() <= 0) {
                    Timber.d("[Sky Italia] getContentProgress -- isAdDisplayed = True");
                    return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }
                Timber.d("[Sky Italia] getContentProgress -- isAdDisplayed = False");
                return new VideoProgressUpdate(pTPlayer.getPlayerEngine().getCurrentPosition(), pTPlayer.getPlayerEngine().getDuration());
            }
        });
        */
        Timber.d("[Sky Italia] Request ad");
        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        adsLoader.requestAds(request);
    }

    public void setCurrentView(ViewGroup view) {
        currentView = view;
        // Reset Ads loader to this view
        AdDisplayContainer adDisplayContainer = imaFactory.createAdDisplayContainer();
        adDisplayContainer.setAdContainer(currentView);
        ImaSdkSettings settings = imaFactory.createImaSdkSettings();
        adsLoader = imaFactory.createAdsLoader(context, settings, adDisplayContainer);
        // Attach a listener
        adsLoader.addAdsLoadedListener(new AdsLoadedListener() {
            @Override
            public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
                // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
                // events for ad playback and errors.
                adsManager = adsManagerLoadedEvent.getAdsManager();
                Timber.d("[Sky Italia] on Ads Manager Loaded");
                // Attach event and error event listeners.
                //mAdsManager.addAdErrorListener(VideoFragment.this);
                adsManager.addAdEventListener(pTPlayer);
                //adsManager.addAdEventListener((PrimetimePlayerEngine) pTPlayer.getPlayerEngine());

                adsManager.init();
                // start ad manager
                adsManager.start();
            }
        });
    }

}
