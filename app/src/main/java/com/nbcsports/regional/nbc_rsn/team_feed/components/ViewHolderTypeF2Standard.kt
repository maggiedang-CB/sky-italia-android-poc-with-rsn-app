package com.nbcsports.regional.nbc_rsn.team_feed.components

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.View
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Constants
import com.nbcsports.regional.nbc_rsn.common.Team
import kotlinx.android.synthetic.main.teamview_card_type_standard_f2.view.*

class ViewHolderTypeF2Standard internal constructor(view: View, viewType: Int) : ViewHolderTypeBase(view, viewType) {

    fun setCardAttributes(playNonFeatureGifs: Boolean, team: Team, intPrimaryColor: Int) {
        itemView.setBackgroundColor(intPrimaryColor)
        itemView.f1_standard_video_artwork.loadImage(playNonFeatureGifs, mItem.imageAssetUrl, team.primaryColor, null)

        itemView.title?.text = mItem.title

        when (mItem.contentType) {
            Constants.CONTENT_TYPE_AUDIO -> {
                itemView.title_icon.visibility = View.VISIBLE
                itemView.title_vertical_bar.visibility = View.GONE
                itemView.title_icon.setImageResource(contentIcon)
                itemView.tag_vertical.visibility = View.VISIBLE
                itemView.tag_vertical.text = mItem.tag

                val durationText = itemView.resources.getString(
                        R.string.teamview_card_episode_duration_format,
                        mItem.episode,
                        if (mItem.episode.isBlank()|| mItem.contentDuration.isBlank()) "" else " - ",
                        if (mItem.contentDuration.isNotBlank()) ViewHolderTypeBase.PLACEHOLDER_CLOCK else "",
                        mItem.contentDuration
                )
                val durationSpannable = SpannableString(durationText)
                val placeholderIndex = durationText.indexOf(ViewHolderTypeVideo.PLACEHOLDER_CLOCK)
                if (placeholderIndex >= 0) {
                    durationSpannable.setSpan(
                            ImageSpan(itemView.context, R.drawable.ic_clock, ImageSpan.ALIGN_BASELINE),
                            placeholderIndex,
                            placeholderIndex + ViewHolderTypeVideo.PLACEHOLDER_CLOCK.length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                itemView.duration_or_tag.text = durationSpannable
            }
            else -> {
                itemView.title_icon.visibility = View.GONE
                itemView.title_vertical_bar.visibility = View.VISIBLE
                itemView.tag_vertical.visibility = View.GONE
                itemView.duration_or_tag.text = mItem.tag
            }
        }
    }

    override fun toString(): String {
        return "ViewHolderTypeF2Standard " + itemView.title?.text
    }
}