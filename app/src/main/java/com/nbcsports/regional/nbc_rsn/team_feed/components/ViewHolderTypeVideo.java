package com.nbcsports.regional.nbc_rsn.team_feed.components;

import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import com.nbcsports.regional.nbc_rsn.R;
import com.nbcsports.regional.nbc_rsn.common.Team;
import com.nbcsports.regional.nbc_rsn.team_view.PeacockImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ViewHolderTypeVideo extends ViewHolderTypeBase
        implements FragmentLifeCycleListener {

    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0l;
    // Exo Player
    protected SimpleExoPlayer player;

    @BindView(R.id.f1_standard_player)
    protected PlayerView exoPlayerView;

    @Nullable
    @BindView(R.id.f1_standard_video_artwork)
    PeacockImageView artwork;

    @Nullable
    @BindView(R.id.title)
    TextView title;

    @Nullable
    @BindView(R.id.description)
    TextView description;

    @Nullable
    @BindView(R.id.duration_indicator_layout)
    LinearLayout durationIndicatorLayout;

    @Nullable
    @BindView(R.id.duration_indicator_bar)
    ImageView durationIndicatorBar;

    @Nullable
    @BindView(R.id.duration)
    TextView duration;

    public ViewHolderTypeVideo(FragmentLifeCycleListener.Interface lifeCycleInterface, View view, int itemViewType) {
        super(view, itemViewType);
        ButterKnife.bind(this, view);

        lifeCycleInterface.addFragmentLifeCycleListener(this);
    }

    @Override
    public void onResume() {
        Timber.d("onResume");
        playVideo();
    }

    @Override
    public void onPause() {
        Timber.d("onPause");
        releasePlayer();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            playVideo();
        } else {
            releasePlayer();
        }
    }

    @Override
    public String toString() {
        return "ViewHolderTypeVideo" + (title == null ? "" : title.getText());
    }

    protected void playVideo() {
        String hlsVideoUrl = getStreamUrl();
        if (hlsVideoUrl == null || hlsVideoUrl.isEmpty()) {
            return;
        }

        if (artwork != null) {
            artwork.setVisibility(View.GONE);
        }

        if (player == null) {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(itemView.getContext(), trackSelector);

            player.setPlayWhenReady(false);
            player.seekTo(currentWindow, playbackPosition);
            exoPlayerView.setPlayer(player);
        }

        if (player.getPlaybackState() == Player.STATE_READY) {
            player.setPlayWhenReady(true);
            return;
        }

        try {
            DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(itemView.getContext(),
                    Util.getUserAgent(itemView.getContext(), "Exo2"), defaultBandwidthMeter);
            Handler mainHandler = new Handler();

            HlsMediaSource hlsMediaSource = new HlsMediaSource(Uri.parse(hlsVideoUrl),
                    dataSourceFactory, mainHandler, null);

            player.prepare(hlsMediaSource, true, false);
            player.setRepeatMode(Player.REPEAT_MODE_ONE);
            player.setVolume(0f);
            player.seekTo(currentWindow, playbackPosition);
            player.setPlayWhenReady(playWhenReady);


        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Nullable
    protected String getStreamUrl() {
        if (mItem != null && mItem.getMediaSource() != null) {
            return mItem.getMediaSource().getStreamUrl();
        } else {
            return null;
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    public void setCardAttributes(boolean playNonFeatureGifs, Team team, GradientDrawable teamColorGradient, int intPrimaryColor, int intSecondaryColor, String regionBackgroundURL, boolean isLightTeam) {
        if (mItem == null) {
            return;
        }

        setBG(teamColorGradient, intPrimaryColor, regionBackgroundURL, isLightTeam);
        if (durationIndicatorBar != null) {
            durationIndicatorBar.setBackgroundColor(intSecondaryColor);
        }
        if (description != null) {
            description.setText(mItem.getDescription());
        }
        checkAndAdjustTitleTextSize(title);
        title.setText(mItem.getTitle());

        artwork.loadImage(playNonFeatureGifs, mItem.getImageAssetUrl(), team.getPrimaryColor(), null);

        if (mItem.getContentDuration().isEmpty()) {
            if (durationIndicatorLayout != null) {
                durationIndicatorLayout.setVisibility(View.INVISIBLE);
            }
        } else {
            if (durationIndicatorLayout != null) {
                durationIndicatorLayout.setVisibility(View.VISIBLE);
            }
            if (duration != null) {
                duration.setText(mItem.getContentDuration());
            }
        }
    }

}
