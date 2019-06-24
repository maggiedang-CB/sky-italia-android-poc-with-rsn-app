package com.nbcsports.regional.nbc_rsn.persistentplayer.switchscreen;

import com.google.gson.Gson;
import com.nbcsports.regional.nbc_rsn.common.MediaSource;
import com.nbcsports.regional.nbc_rsn.utils.PreferenceUtils;


public final class ViewingHistory {

    private static String MOST_RECENT_KEY = "_prefMostRecentMediaSource";
    private static String SECOND_MOST_RECENT_KEY = "_prefSecondMostRecentMediaSource";

    /*
     * Adds the Video video into the queue to become the most recent video.
     *
     *  If Video video is the first ever video to be watched, or if two videos with the same pid is
     *  watched twice in a row, place that video in the queue.
     *
     * If Video video has a different pid with the one in the queue, replace the video in the queue
     *  with Video video. Then move the Video in queue to the most recent position.
     *
     * Similar to a Queue with max 2 items.
     *
     */
    public static void addMediaSource(MediaSource newMediaSource) {

        if (newMediaSource.getStreamUrl().contains("live") || newMediaSource.getLive()){
            return; //Do not add Live content to the viewing history
        }

        Gson gson = new Gson();
        String mostRecentMediaSourceStr = PreferenceUtils.INSTANCE.getString(MOST_RECENT_KEY, "");
        MediaSource mostRecentMediaSource = mostRecentMediaSourceStr.isEmpty() ? null : gson.fromJson(mostRecentMediaSourceStr, MediaSource.class);

        if (mostRecentMediaSource == null || mostRecentMediaSource.getStreamUrl().equals(newMediaSource.getStreamUrl() ) ){
            PreferenceUtils.INSTANCE.setString(MOST_RECENT_KEY, gson.toJson(newMediaSource));

        } else if (!mostRecentMediaSource.getStreamUrl().equals(newMediaSource.getStreamUrl())) {
            PreferenceUtils.INSTANCE.setString(MOST_RECENT_KEY, gson.toJson(newMediaSource));
            PreferenceUtils.INSTANCE.setString(SECOND_MOST_RECENT_KEY, gson.toJson(mostRecentMediaSource));
        }
    }

    public static MediaSource getMostRecentMediaSource() {
        String mostRecent = PreferenceUtils.INSTANCE.getString(MOST_RECENT_KEY, "");
        if (mostRecent.isEmpty()){
            return null;
        }
        return new Gson().fromJson(mostRecent, MediaSource.class);
    }

    public static MediaSource getSecondMostRecentMediaSource() {
        String mostRecent = PreferenceUtils.INSTANCE.getString(SECOND_MOST_RECENT_KEY, "");
        if (mostRecent.isEmpty()){
            return null;
        }
        return new Gson().fromJson(mostRecent, MediaSource.class);
    }
}
