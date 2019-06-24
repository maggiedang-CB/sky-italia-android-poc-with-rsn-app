package com.nbcsports.regional.nbc_rsn.stepped_story

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo
import com.nbcsports.regional.nbc_rsn.common.Config
import com.nbcsports.regional.nbc_rsn.common.FeedComponent
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.common.TeamManager
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.nbcsports.regional.nbc_rsn.team_feed.components.TeamViewComponentsAdapter
import com.nbcsports.regional.nbc_rsn.team_feed.components.TeamViewComponentsAdapter.*
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils

class SteppedStoryPresenter(var view: SteppedStoryFragment?, val config: Config?) : SteppedStoryContract.Presenter {

    private var steppedCoverExoPlayer: SimpleExoPlayer? = null
    private var currentWindow: Int = 0
    private var playbackPosition: Long = 0
    private lateinit var team: Team
    private lateinit var feedComponent: FeedComponent

    init {
        view?.setPresenter(this)
    }

    fun unsubscribe() {
        view = null
        releaseExoPlayer()
    }

    override fun setFeedComponent(feedComponent: FeedComponent?) {
        if (feedComponent == null) return
        this.feedComponent = feedComponent
    }

    override fun share(title: String?) {
        val titleString = title ?: feedComponent.title
        val shareInfo = NativeShareUtils.generateShareInfo(view, feedComponent, titleString, null, team.teamId)
        shareInfo.let { NativeShareUtils.share(it) }
    }

    override fun setTeam(team: Team?) {
        if (team == null) return
        this.team = team
    }

    override fun getPageInfo(): PageInfo? {
        var section = ""
        var subSection = ""
        var league = ""
        var author = ""
        val cardType = feedComponent.cardType
        val contentType = feedComponent.contentType
        val type = feedComponent.type
        val componentTag = feedComponent.tag
        val itemViewType = TeamViewComponentsAdapter.getItemViewType(type, cardType, contentType)
        when (itemViewType) {
            VIEW_TYPE_Feed_Standard_Image, VIEW_TYPE_F1_Standard_Image, VIEW_TYPE_Feed_Standard_Video, VIEW_TYPE_F1_Standard_Video, VIEW_TYPE_Feed_Standard_Text -> {
                section = "Article"
                league = team.league
                author = feedComponent.author
                subSection = componentTag
            }
            VIEW_TYPE_Feed_Standard_Podcast, VIEW_TYPE_F1_Standard_Audio -> {
                section = "Podcast"
                league = team.league
                author = feedComponent.author
            }
            else -> section = "default"
        }
        return PageInfo(false, team.regionName, team.displayName, section, subSection, feedComponent.contentType, "", league, author, feedComponent.componentId, feedComponent.title)
    }

    override fun switchTarget(oldPlayerView: PlayerView?, newPlayerView: PlayerView?) {
        if (steppedCoverExoPlayer == null) {
            initSteppedCoverExoPlayer()
        }
        PlayerView.switchTargetView((steppedCoverExoPlayer as SimpleExoPlayer), oldPlayerView, newPlayerView)
    }

    override fun setAppropriateStatusBarColor(activity: Activity?, isCurrentSteppedFragment: Boolean) {
        activity?.let {
            if (isTeamDisplayNameTheSameAsSelected()){
                if (isCurrentSteppedFragment){
                    ActivityUtils.setStatusBarColor(it, Color.BLACK)
                } else {
                    ActivityUtils.setStatusBarColor(it, Color.parseColor(team.primaryColor))
                }
            }
        }
    }

    private fun initSteppedCoverExoPlayer() {
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        steppedCoverExoPlayer = ExoPlayerFactory.newSimpleInstance(view?.context, trackSelector)

        (steppedCoverExoPlayer as SimpleExoPlayer).playWhenReady = false
        (steppedCoverExoPlayer as SimpleExoPlayer).seekTo(currentWindow, playbackPosition)
    }

    fun playSteppedCoverVideo(hlsVideoUrl: String?) {
        if (steppedCoverExoPlayer == null) {
            initSteppedCoverExoPlayer()
        }

        val simpleExoPlayer = steppedCoverExoPlayer as SimpleExoPlayer

        if (simpleExoPlayer.playbackState == Player.STATE_READY) {
            simpleExoPlayer.playWhenReady = true
            return
        }

        try {
            val defaultBandwidthMeter = DefaultBandwidthMeter()
            val dataSourceFactory = DefaultDataSourceFactory(view?.context,
                    Util.getUserAgent(view?.context, "Exo2"), defaultBandwidthMeter)
            val mainHandler = Handler()

            val hlsMediaSource = HlsMediaSource(Uri.parse(hlsVideoUrl),
                    dataSourceFactory, mainHandler, null)

            simpleExoPlayer.apply {
                prepare(hlsMediaSource, true, false)
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                seekTo(currentWindow, playbackPosition)
                playWhenReady = true
            }
        } catch (e: Exception) {
            e("error -> $e")
        }
    }

    private fun releaseExoPlayer() {
        if (steppedCoverExoPlayer != null) {
            playbackPosition = (steppedCoverExoPlayer as SimpleExoPlayer).getCurrentPosition()
            currentWindow = (steppedCoverExoPlayer as SimpleExoPlayer).getCurrentWindowIndex()
            (steppedCoverExoPlayer as SimpleExoPlayer).release()
            steppedCoverExoPlayer = null
        }
    }

    fun isTeamDisplayNameTheSameAsSelected(): Boolean {
        val selectedTeam = TeamManager.getInstance()?.getSelectedTeam() ?: return false
        return selectedTeam.displayName.equals(team.displayName, ignoreCase = true)
    }
}