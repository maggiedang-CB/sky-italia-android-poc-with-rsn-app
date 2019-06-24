package com.nbcsports.regional.nbc_rsn.stepped_story.components

import android.net.Uri
import android.os.Handler
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.holder_item_layout.view.*
import kotlin.math.roundToInt
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.nbcsports.regional.nbc_rsn.team_view.PeacockImageView
import com.nbcsports.regional.nbc_rsn.team_view.PicassoLoadListener
import java.lang.Exception

class SteppedItemViewHolder(itemView: View, itemViewType: Int, team: Team, val playNonFeatureGifs: Boolean) : SteppedListAdapter.ViewHolder(itemView, itemViewType, team) {

    var isVariationVideo: Boolean = false

    private var totalScrollDistance = 0
    private var steppedItemExoPlayer: SimpleExoPlayer? = null
    private var hlsVideoUrl: String? = null
    private var currentWindow: Int = 0
    private var playbackPosition: Long = 0
    private var steppedComponentPosition: Int = -1

    fun bind(steppedComponent: SteppedComponent, sectionIndex: Int, sectionTotal: Int, position: Int, cover: SteppedComponent?) {
        steppedComponentPosition = position
        if (steppedComponentPosition == 0) {
            cover?.let {
                steppedComponent.variation = it.variation
                steppedComponent.entryImage = it.coverImage
                steppedComponent.mediaSource = it.mediaSource
            }
        }
        bind(steppedComponent)

        itemView.ss_section_num.text = "$sectionIndex".padStart(2, '0')
        itemView.ss_section_total.text = "/${"$sectionTotal".padStart(2, '0')}"
    }

    override fun bind(steppedComponent: SteppedComponent) {
        // this is because our animation changed the positioning of views in this layout
        // now we want to reset it since it is getting reused
        (itemView as? ConstraintLayout)?.let {
            val constraints = ConstraintSet()
            constraints.clone(it.context, R.layout.holder_item_layout)
            constraints.applyTo(it)

            it.ss_title_wrapper.translationY = 0f
        }

        // TODO: video need not be hard coded
        isVariationVideo = steppedComponent.variation.equals("video", ignoreCase = true)
        hideRelativeViews()
        if (steppedComponentPosition > 0) {
            itemView.ss_image.visibility = View.VISIBLE
        }
        initVariationImage(steppedComponent)
        if (isVariationVideo) {
            initVariationVideo(steppedComponent)
        }

        setAttributes(itemView.ss_text, steppedComponent.displayText, team.primaryColor)
        val rawDisplayText = steppedComponent.displayTitle
        itemView.ss_title.text = rawDisplayText?.replace(NEW_LINE_CHAR_PATTERN, "")
                ?.replace(BR_TAG, "")?.replace(P_TAG, "")
        // Set the same text as ss_title into ss_title_placeholder
        // to hold the exact height
        itemView.ss_title_placeholder.text = itemView.ss_title.text
        // Since recycler view will reuse view holder
        // So every time need to reset the view back to hero on bind
        // This is for reuse while scrolling down
        // For scrolling up, it is handled by SteppedListLayoutManager.scrollVerticallyBy(...)
        animateToHero(1.0f)
    }

    fun setUpForSteppedCover(steppedCoverLayout: SteppedCoverLayout?) {
        hideRelativeViews()
        if (steppedCoverLayout?.isVariationVideo == true) {
            itemView.ss_player_view.visibility = View.VISIBLE
        } else {
            itemView.ss_image.visibility = View.VISIBLE
        }
    }

    private fun initVariationImage(steppedComponent: SteppedComponent) {
        val url = steppedComponent.entryImage
        if (!url.isNullOrBlank()) {
            val iv = itemView.ss_image as PeacockImageView
            iv.loadImage(itemView.context, playNonFeatureGifs, url, team.primaryColor,
                    object : PicassoLoadListener {
                        override fun onSuccess() {
                            iv.requestLayout()
                        }

                        override fun onError(e: Throwable?) {
                            e("error -> $e")
                        }
                    })
        }
    }

    private fun initVariationVideo(steppedComponent: SteppedComponent) {
        hlsVideoUrl = getStreamUrl(steppedComponent)
        initSteppedItemExoPlayer()

        itemView.ss_image.visibility = View.INVISIBLE
        itemView.ss_player_view.visibility = View.VISIBLE
    }

    private fun getStreamUrl(steppedComponent: SteppedComponent): String? {
        if (steppedComponent?.mediaSource != null) {
            return (steppedComponent.mediaSource as MediaSource).streamUrl
        }
        return ""
    }

    private fun initSteppedItemExoPlayer() {
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        steppedItemExoPlayer = ExoPlayerFactory.newSimpleInstance(itemView.context, trackSelector)

        (steppedItemExoPlayer as SimpleExoPlayer).setPlayWhenReady(false)
        (steppedItemExoPlayer as SimpleExoPlayer).seekTo(currentWindow, playbackPosition)
        itemView.ss_player_view.player = steppedItemExoPlayer
    }

    fun playSteppedItemVideo() {
        if (steppedItemExoPlayer == null) {
            initSteppedItemExoPlayer()
        }

        val simpleExoPlayer = steppedItemExoPlayer as SimpleExoPlayer

        if (simpleExoPlayer.playbackState == Player.STATE_READY) {
            simpleExoPlayer.playWhenReady = true
            return
        }

        try {
            val defaultBandwidthMeter = DefaultBandwidthMeter()
            val dataSourceFactory = DefaultDataSourceFactory(itemView.context,
                    Util.getUserAgent(itemView.context, "Exo2"), defaultBandwidthMeter)
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

    fun releaseExoPlayer() {
        if (steppedItemExoPlayer != null) {
            playbackPosition = (steppedItemExoPlayer as SimpleExoPlayer).getCurrentPosition()
            currentWindow = (steppedItemExoPlayer as SimpleExoPlayer).getCurrentWindowIndex()
            (steppedItemExoPlayer as SimpleExoPlayer).release()
            steppedItemExoPlayer = null
        }
    }

    private fun animateTitleWrapper(destination: Int) {
        /*
        * On iOS, they can set speed for each individual child of the container in
        * a scroll view. However, Android doesn't support that. Only the container
        * gets a callback 'offsetTopAndBottom'.
        *
        * The implementation of offsetTopAndBottom() in View doesn't really pass
        * anything to the children either. It only offsets the current view's position.
        *
        * The following method is recommended by an iOS developer.
        * */
        val start = itemView.ss_title_wrapper.translationY.roundToInt()
        if (destination > start) {
            (start..destination).forEach { eachPixel ->
                itemView.ss_title_wrapper.translationY = eachPixel.toFloat()
            }
        } else {
            (start downTo destination).forEach { eachPixel ->
                itemView.ss_title_wrapper.translationY = eachPixel.toFloat()
            }
        }
    }

    private fun animateNormalTitleWrapper(scrolledPercent: Float) {
        itemView.ss_title.postDelayed({
            totalScrollDistance = itemView.ss_section_num_placeholder.top - itemView.ss_title_wrapper.top

            // Avoid redundant animation. Without this check, animation might look a bit shaky at the end.
            if (itemView.ss_title_wrapper.translationY != totalScrollDistance.toFloat()) {
                animateTitleWrapper((scrolledPercent * totalScrollDistance).roundToInt())
            }
        }, 20)
    }

    private fun animateNormalOther(scrolledPercent: Float) {
        applyFontSize(20f, 14f, scrolledPercent, itemView.ss_title)
        applyFontSize(14f, 2f, scrolledPercent, itemView.ss_section_num)

        itemView.ss_section_total.post {
            itemView.ss_section_total.visibility = View.VISIBLE
            itemView.ss_section_total.alpha = scrolledPercent
        }

        itemView.ss_media_container.post {
            itemView.ss_image_overlay.alpha = 1 - scrolledPercent

            if (scrolledPercent == 1f) {
                itemView.ss_image_overlay.visibility = View.GONE
                if (isVariationVideo) {
                    itemView.ss_image.visibility = View.INVISIBLE
                    itemView.ss_player_view.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun applyFontSize(baseSize: Float, totalIncrease: Float, percent: Float, textView: TextView) {
        textView.post {
            val newFontSize = baseSize + totalIncrease * when {
                percent < 0.25 -> 0.25f
                percent < 0.5 -> 0.5f
                percent < 0.75 -> 0.75f
                else -> 1f
            }
            val newFontSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, newFontSize, textView.resources.displayMetrics)
            if (textView.textSize != newFontSizePx) {
                textView.textSize = newFontSize
                textView.invalidate()
                textView.requestLayout()
            }
        }
    }

    fun animateToNormal(scrolledPercent: Float) {
        if (scrolledPercent < 0 || scrolledPercent > 1) {
            return
        }

        itemView.postDelayed({
            animateNormalOther(scrolledPercent)
        }, 10)

        // This delay exists to capture the latest wrapper.top value after text size update.
        // Without the delay, if user pauses the animation, title positioning might be incorrect.
        itemView.postDelayed({
            animateNormalTitleWrapper(scrolledPercent)
        }, 30)

    }

    fun animateToNormalImmediately() {
        itemView.ss_title_wrapper.visibility = View.INVISIBLE

        applyFontSize(20f, 14f, 1.0f, itemView.ss_title)
        applyFontSize(14f, 2f, 1.0f, itemView.ss_section_num)

        itemView.post {
            itemView.ss_section_total.visibility = View.VISIBLE
            itemView.ss_section_total.alpha = 1.0f
            itemView.ss_image_overlay.alpha = 0f
            itemView.ss_image_overlay.visibility = View.GONE
            if (isVariationVideo) {
                itemView.ss_image.visibility = View.INVISIBLE
                itemView.ss_player_view.visibility = View.VISIBLE
            }
        }

        itemView.ss_title.post {
            totalScrollDistance = itemView.ss_section_num_placeholder.top - itemView.ss_title_wrapper.top
            itemView.ss_title_wrapper.translationY = totalScrollDistance.toFloat()
        }

        itemView.ss_title.post {
            itemView.ss_title_wrapper.visibility = View.VISIBLE
        }
    }

    fun animateToHero(scrolledPercent: Float) {
        if (scrolledPercent <= 1.0f) {
            itemView.post {
                animateTitleWrapper(((1 - scrolledPercent) * totalScrollDistance).roundToInt())
            }

            applyFontSize(34f, -14f, scrolledPercent, itemView.ss_title)
            applyFontSize(16f, -2f, scrolledPercent, itemView.ss_section_num)

            itemView.post {
                itemView.ss_section_total.alpha = 1 - scrolledPercent
                if (itemView.ss_section_total.alpha < 0.05) {
                    itemView.ss_section_total.visibility = View.GONE
                }
                itemView.ss_image_overlay.visibility = View.VISIBLE
                itemView.ss_image_overlay.alpha = scrolledPercent
            }
        }
    }

    fun hideRelativeViews() {
        itemView.ss_image.visibility = View.INVISIBLE
        itemView.ss_player_view.visibility = View.INVISIBLE
    }
}