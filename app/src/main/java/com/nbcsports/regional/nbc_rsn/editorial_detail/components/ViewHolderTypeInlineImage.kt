package com.nbcsports.regional.nbc_rsn.editorial_detail.components

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.ablanco.zoomy.ZoomListener
import com.ablanco.zoomy.Zoomy
import com.google.android.exoplayer2.ui.PlayerView
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem
import com.nbcsports.regional.nbc_rsn.team_feed.components.FragmentLifeCycleListener
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeVideo
import com.nbcsports.regional.nbc_rsn.team_view.PeacockImageView
import kotlinx.android.synthetic.main.component_editorial_inline_image.view.*

class ViewHolderTypeInlineImage(lifeCycleInterface: FragmentLifeCycleListener.Interface, root: View, viewType: Int, private val team: Team)
    : ViewHolderTypeVideo(lifeCycleInterface, root, viewType), ZoomListener {

    /**
     * This view exists to absorb all the clicks while inline image is zooming, so nothing
     * in the background will respond to touch/click.
     */
    private var clickBlocker: View? = null
    private var editorialDetailItem: EditorialDetailItem? = null

    private var duplicatedTarget: PlayerView? = null

    override fun onViewStartedZooming(view: View?) {
        clickBlocker = View(itemView.context)
        clickBlocker?.isClickable = true
        ((itemView.context as? Activity)?.window?.decorView as? ViewGroup)?.addView(clickBlocker)

        if (streamUrl?.isNotEmpty() == true) {
            // video exists
            PlayerView.switchTargetView(player, exoPlayerView, duplicatedTarget)
        }
    }

    override fun onViewEndedZooming(view: View?) {
        clickBlocker?.let {
            ((itemView.context as? Activity)?.window?.decorView as? ViewGroup)?.removeView(it)
        }
        clickBlocker = null

        if (streamUrl?.isNotEmpty() == true) {
            // video exists
            PlayerView.switchTargetView(player, duplicatedTarget, exoPlayerView)
        }
    }

    override fun getStreamUrl(): String? {
        return editorialDetailItem?.mediaSource?.streamUrl
    }

    fun bind(detailItem: EditorialDetailItem) {
        editorialDetailItem = detailItem

        // source
        if (detailItem.source.isNullOrBlank()) {
            itemView.image_source.visibility = View.GONE
        } else {
            itemView.image_source.visibility = View.VISIBLE
            itemView.image_source.text = detailItem.source
        }

        // caption
        if (detailItem.caption.isNullOrBlank()) {
            itemView.image_caption.visibility = View.GONE
        } else {
            itemView.image_caption.visibility = View.VISIBLE
            itemView.image_caption.text = detailItem.caption
        }
    }

    fun updateImage(playNonFeatureGifs: Boolean) {

        val builder = Zoomy.Builder(itemView.context as Activity)

        val inlineImageUrl = editorialDetailItem?.inlineImage
        val videoUrl = editorialDetailItem?.mediaSource?.streamUrl
        if (inlineImageUrl?.isEmpty() == false) {
            itemView.inline_image.visibility = View.VISIBLE
            itemView.f1_standard_player.visibility = View.GONE

            itemView.inline_image.loadImage(playNonFeatureGifs, inlineImageUrl, team.primaryColor, null)

            // now we need to build a duplicated PeacockImageView for zooming gif
            if (playNonFeatureGifs && inlineImageUrl.endsWith(".gif")) {
                val duplicate = PeacockImageView(itemView.context)
                // set width and height and other properties
                val displayMetrics = itemView.resources.displayMetrics
                duplicate.layoutParams = ViewGroup.LayoutParams(displayMetrics.widthPixels, displayMetrics.widthPixels * 9 / 16)

                // load gif
                duplicate.loadImage(playNonFeatureGifs, inlineImageUrl, team.primaryColor, null)
                builder.setTargetDuplicate(duplicate)
            }

            builder.target(itemView.inline_image)
                    .zoomListener(this)
                    .register()
        } else if (videoUrl?.isEmpty() == false) {
            itemView.inline_image.visibility = View.GONE
            itemView.f1_standard_player.visibility = View.VISIBLE

            duplicatedTarget = View.inflate(itemView.context, R.layout.exo_player_single, null) as? PlayerView
            val displayMetrics = itemView.resources.displayMetrics
            duplicatedTarget?.layoutParams = ViewGroup.LayoutParams(displayMetrics.widthPixels, displayMetrics.widthPixels * 9 / 16)
            builder.setTargetDuplicate(duplicatedTarget)
            builder.target(itemView.f1_standard_player)
                    .zoomListener(this)
                    .register()
        }
    }


}