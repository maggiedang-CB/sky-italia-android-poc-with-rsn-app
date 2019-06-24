package com.nbcsports.regional.nbc_rsn.stepped_story.components

import android.view.View
import com.google.android.exoplayer2.ui.PlayerView
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.nbcsports.regional.nbc_rsn.stepped_story.SteppedStoryPresenter
import com.nbcsports.regional.nbc_rsn.team_view.PeacockImageView
import com.nbcsports.regional.nbc_rsn.team_view.PicassoLoadListener
import kotlinx.android.synthetic.main.holder_cover_layout.view.*

class SteppedCoverViewHolder(itemView: View, itemViewType: Int, team: Team, val playNonFeatureGifs: Boolean, val steppedCoverRef: SteppedCoverLayout?, val presenter: SteppedStoryPresenter?) : SteppedListAdapter.ViewHolder(itemView, itemViewType, team) {

    var isVariationVideo: Boolean = false

    override fun bind(steppedComponent: SteppedComponent) {
        // TODO: video need not be hard coded
        if (steppedComponent.variation != null) {
            isVariationVideo = steppedComponent.variation.equals("video", ignoreCase = true)
            if (!isVariationVideo) {
                initVariationImage(steppedComponent)
            }
        }

        setAttributes(itemView.ss_item_first_text, steppedComponent.displayText, team.primaryColor)
    }

    private fun initVariationImage(steppedComponent: SteppedComponent) {
        val url = steppedComponent.coverImage
        if (!url.isNullOrBlank()) {
            val iv = itemView.ss_item_cover_image as PeacockImageView
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

    fun initVariationVideo() {
        if (steppedCoverRef?.isVariationVideo == true){
            presenter?.switchTarget((steppedCoverRef?.baseView as? PlayerView), itemView.ss_item_cover_player_view)
        }
    }

    fun showRelatedViews() {
        if (isVariationVideo) {
            itemView.ss_item_cover_image.visibility = View.INVISIBLE
            itemView.ss_item_cover_player_view.visibility = View.VISIBLE
        } else {
            itemView.ss_item_cover_image.visibility = View.VISIBLE
            itemView.ss_item_cover_player_view.visibility = View.INVISIBLE
        }
    }

    fun hideRelativeViews() {
        itemView.ss_item_cover_image.visibility = View.INVISIBLE
        itemView.ss_item_cover_player_view.visibility = View.INVISIBLE
    }
}