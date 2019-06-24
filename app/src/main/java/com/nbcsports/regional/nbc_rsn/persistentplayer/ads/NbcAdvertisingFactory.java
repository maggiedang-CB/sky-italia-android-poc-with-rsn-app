package com.nbcsports.regional.nbc_rsn.persistentplayer.ads;

import android.content.Context;
import android.util.Base64;

import com.adobe.mediacore.DefaultAdvertisingFactory;
import com.adobe.mediacore.MediaPlayerItem;
import com.adobe.mediacore.metadata.AdSignalingMode;
import com.adobe.mediacore.metadata.DefaultMetadataKeys;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.TimedMetadata;
import com.adobe.mediacore.timeline.advertising.ContentResolver;
import com.adobe.mediacore.timeline.advertising.MetadataResolver;
import com.adobe.mediacore.timeline.advertising.customadmarkers.CustomAdMarkersContentResolver;
import com.adobe.mediacore.timeline.advertising.customadmarkers.DeleteContentResolver;
import com.adobe.mediacore.utils.TimeRangeCollection;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class NbcAdvertisingFactory extends DefaultAdvertisingFactory {

    private final String pid;
    private final String channel;
    private final boolean isLive;
    private final Context context;
    private NbcContentResolver nbcCR;

    public NbcAdvertisingFactory(AdSignalingMode adSignalingMode, Context context,
                                 String pid, String channel, boolean isLive) {
        super(adSignalingMode, context);
        this.pid = pid;
        this.channel = channel;
        this.isLive = isLive;
        this.context = context;
    }

    @Override
    public List<ContentResolver> createContentResolvers(MediaPlayerItem item) {
        List<ContentResolver> contentResolvers = new ArrayList<ContentResolver>();
        Metadata metadata = item.getResource().getMetadata();

        if (metadata != null) {
            if (metadata.containsKey(DefaultMetadataKeys.TIME_RANGES_METADATA_KEY.getValue())) {
                String timeRangeType = metadata.getValue(DefaultMetadataKeys.TIME_RANGES_METADATA_KEY.getValue());
                if (timeRangeType.equals(TimeRangeCollection.TIME_RANGE_TYPE_DELETE)) {
                    contentResolvers.add(new DeleteContentResolver());
                } else if (timeRangeType.equals(TimeRangeCollection.TIME_RANGE_TYPE_REPLACE)) {
                    contentResolvers.add(new DeleteContentResolver());
                } else if (timeRangeType.equals(TimeRangeCollection.TIME_RANGE_TYPE_MARK)) {
                    contentResolvers.add(new CustomAdMarkersContentResolver());
                }
            }
            if (metadata.containsKey(DefaultMetadataKeys.AUDITUDE_METADATA_KEY.getValue())) {
                nbcCR = new NbcContentResolver(context.getApplicationContext(), pid, channel, isLive);
                contentResolvers.add(nbcCR);
            } else if (metadata.containsKey(DefaultMetadataKeys.JSON_METADATA_KEY.getValue())) {
                contentResolvers.add(new MetadataResolver());
            }
        }
        return contentResolvers;
    }

    public void configureMidroll(TimedMetadata timedMetadata) {
        if ( timedMetadata.getMetadata().containsKey( "CAID" )
                && timedMetadata.getMetadata().containsKey( "ID" ) ){

            String cueId = timedMetadata.getMetadata().getValue( "ID" );
            String caidBase64 = timedMetadata.getMetadata().getValue( "CAID" );
            byte[] data = Base64.decode(caidBase64, Base64.DEFAULT);

            try {
                String caidString = new String(data, "UTF-8");
                //Timber.d("midroll onTimedMetadata caidString: %s", caidString);
                setCaidString(caidString);
                setCueId(cueId);
            } catch ( UnsupportedEncodingException e ) {
                e.printStackTrace();
            }
        }
    }
    private void setCaidString( String caidString ) {
        if ( nbcCR != null )
            nbcCR.setCaidString(caidString);
    }

    private void setCueId( String cueId ) {
        if ( nbcCR != null )
            nbcCR.setCueId(cueId);
    }
}
