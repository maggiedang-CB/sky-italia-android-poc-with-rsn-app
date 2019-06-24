package com.nbcsports.regional.nbc_rsn.persistentplayer.ads;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;

import com.adobe.mediacore.metadata.AuditudeSettings;
import com.adobe.mediacore.metadata.DefaultMetadataKeys;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.MetadataNode;
import com.adobe.mediacore.timeline.PlacementOpportunity;
import com.adobe.mediacore.timeline.advertising.auditude.AuditudeResolver;
import com.google.gson.Gson;

import timber.log.Timber;

public class NbcContentResolver extends AuditudeResolver {

    private final String pid;
    private final String channel;
    private final boolean isLive;
    private final Context context;
    private PlacementOpportunity placementOpportunity;
    private String id;
    private MetadataNode metadata;
    private AuditudeSettings auditudeMetadata;
    private String originalSiteSection;
    private String beforeAffiliateAppended;
    private boolean affiliateAppended;
    private String caidString;
    private String cueId;

    public NbcContentResolver(Context context, String pid, String channel, boolean isLive) {
        super(context);
        this.context = context;
        this.pid = pid;
        this.channel = channel;
        this.isLive = isLive;
    }

    @Override
    protected synchronized void doResolveAds(final Metadata metadata, final PlacementOpportunity placementOpportunity) {

        // keep original site section id

        this.placementOpportunity = placementOpportunity;
        id = placementOpportunity.getId();

        if (!isLive || TextUtils.isEmpty(pid) || TextUtils.isEmpty(id)) {
            Timber.d("Bailing on alternate ad call...isLiveStream=" + Boolean.toString(isLive) + ", pid=" + pid + ", id=" + id);
            super.doResolveAds(metadata, placementOpportunity);
            return;
        }

        this.metadata = (MetadataNode) metadata;
        auditudeMetadata =
                (com.adobe.mediacore.metadata.AuditudeSettings) this.metadata.getNode(DefaultMetadataKeys.AUDITUDE_METADATA_KEY.getValue());
        Timber.d("Placement Id: " + placementOpportunity.getId());
        Timber.d("Placement Info: " + placementOpportunity.getPlacementInformation().toString());

        // on first request
        originalSiteSection = auditudeMetadata.getCustomParameters().getValue("SITE_SECTION_ID");

        if (originalSiteSection != null && beforeAffiliateAppended != null
                && originalSiteSection.contains(beforeAffiliateAppended) && affiliateAppended) {
            if (originalSiteSection.contains("_es") && !beforeAffiliateAppended.contains("_es")) {
                originalSiteSection = beforeAffiliateAppended + "_es";
            } else {
                originalSiteSection = beforeAffiliateAppended;
            }
            affiliateAppended = false;
        }

        processCaid( caidString );
    }

    private void processCaid( String caidString ){
        Timber.d("midroll start processing CAID...");
        String siteSection = originalSiteSection;
        String param = "";

        if ( ! TextUtils.isEmpty( caidString ) ) {

            Timber.d("midroll caid string found, encode to json");
            Gson gson = new Gson();
            Midroll midroll = null;
            try {
                midroll = gson.fromJson( caidString, Midroll.class );
                midroll.setCueID( Long.parseLong( cueId ) );
            } catch ( Exception ignore ) {
            }

            if ( midroll != null ) {
                param = midroll.getFreewheelParam();
            }
            auditudeMetadata.getCustomParameters().setValue( "SITE_SECTION_ID", siteSection );
            String ltlg = getLtlgParam();
            if (!TextUtils.isEmpty(param) && !TextUtils.isEmpty(ltlg)) {
                param += "&";
            }
            setParams(param + ltlg);
            Timber.d("midroll setup success!");
            Timber.d("midroll setup json: %s", midroll);
            Timber.d("midroll setup param: %s", param);
            Timber.d("midroll setup siteSection: %s", siteSection);

        } else {

            auditudeMetadata.getCustomParameters().setValue("SITE_SECTION_ID", siteSection);
            String ltlg = getLtlgParam();
            if (!TextUtils.isEmpty(param) && !TextUtils.isEmpty(ltlg)) {
                param += "&";
            }
            setParams(param + ltlg);
        }
    }

    private void setParams(String param) {
        auditudeMetadata.getCustomParameters().setValue("FW_PARAMS", param);
        Timber.d("Midroll param: " + param);
        resolve(metadata, placementOpportunity);
    }

    private String getLtlgParam() {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        if (locationManager != null) {

            Location location;

            try {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } catch (SecurityException ex) {
                return "";
            }

            if (location != null) {
                return "ltlg=" + location.getLatitude() + "," + location.getLongitude();
            }
        }
        return "";
    }

    public void resolve(Metadata metadata, PlacementOpportunity placementOpportunity) {
        super.doResolveAds(metadata, placementOpportunity);
    }

    public void setCaidString( String caidString ) {
        this.caidString = caidString;
    }

    public void setCueId( String cueId ) {
        this.cueId = cueId;
    }
}
