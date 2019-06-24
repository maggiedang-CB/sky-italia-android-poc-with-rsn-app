package com.nbcsports.regional.nbc_rsn.team_feed.components

import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.View
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Team
import kotlinx.android.synthetic.main.teamview_card_type_standard_f2.view.*

class ViewHolderTypeF2Video internal constructor(teamViewComponentsAdapter: TeamViewComponentsAdapter, view: View, viewType: Int) : ViewHolderTypeVideo(teamViewComponentsAdapter, view, viewType){

    init {
        itemView.title_icon.visibility = View.VISIBLE
        itemView.title_vertical_bar.visibility = View.GONE
        itemView.tag_vertical.visibility = View.VISIBLE
    }

    override fun setCardAttributes(playNonFeatureGifs: Boolean, team: Team, teamColorGradient: GradientDrawable, intPrimaryColor: Int, intSecondaryColor: Int, regionBackgroundURL: String, isLightTeam: Boolean) {
        itemView.setBackgroundColor(intPrimaryColor)
        itemView.f1_standard_video_artwork.loadImage(playNonFeatureGifs, mItem.imageAssetUrl, team.primaryColor, null)

        itemView.title.text = mItem.title
        itemView.title_icon.setImageResource(contentIcon)
        itemView.tag_vertical.text = mItem.tag

        val durationText = itemView.resources.getString(
                R.string.teamview_card_episode_duration_format,
                mItem.episode,
                if (mItem.episode.isBlank()|| mItem.contentDuration.isBlank()) "" else " - ",
                if (mItem.contentDuration.isNotBlank()) ViewHolderTypeBase.PLACEHOLDER_CLOCK else "",
                mItem.contentDuration
        )

        val durationSpannable = SpannableString(durationText)
        val placeholderIndex = durationText.indexOf(PLACEHOLDER_CLOCK)
        if (placeholderIndex >= 0) {
            durationSpannable.setSpan(
                    ImageSpan(itemView.context, R.drawable.ic_clock, ImageSpan.ALIGN_BASELINE),
                    placeholderIndex,
                    placeholderIndex + PLACEHOLDER_CLOCK.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        itemView.duration_or_tag.text = durationSpannable
    }

    override fun toString(): String {
        return "ViewHolderTypeF2Video " + itemView.title?.text
    }
}