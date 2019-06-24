package com.nbcsports.regional.nbc_rsn.team_feed.components

import android.graphics.drawable.GradientDrawable
import android.view.View
import com.nbcsports.regional.nbc_rsn.common.Constants
import kotlinx.android.synthetic.main.teamview_card_type_cut_out_f1.view.*

class ViewHolderTypeF1CutOut internal constructor(view: View, viewType: Int) : ViewHolderTypeBase(view, viewType) {

    fun setCardAttributes(playNonFeatureGifs: Boolean, teamColorGradient: GradientDrawable, intPrimaryColor: Int, intSecondaryColor: Int, regionBackgroundURL: String, isLightTeam: Boolean) {
        setBG(teamColorGradient, intPrimaryColor, regionBackgroundURL, isLightTeam)
        itemView.duration_indicator_bar?.setBackgroundColor(intSecondaryColor)

        //Included in XML layout: teamview_card_type_base_date1_and_region, populated by team view template.
        itemView.f1_standard_video_artwork?.visibility = View.VISIBLE
        itemView.f1_standard_video_artwork?.loadImage(playNonFeatureGifs, mItem.f1CutOut, Constants.COLOUR_TRANSPARENT, null)

        checkAndAdjustTitleTextSize(itemView.title)
        itemView.title?.text = mItem.title

        // tag and icon
        itemView.topic_tag_text.text = mItem.tag
        when (mItem.contentType) {
            Constants.CONTENT_TYPE_VIDEO, Constants.CONTENT_TYPE_AUDIO -> {
                itemView.topic_tag_icon.visibility = View.VISIBLE
                itemView.topic_tag_icon.setImageResource(contentIcon)
            }
            else -> itemView.topic_tag_icon.visibility = View.GONE
        }

        // duration
        if (mItem.contentDuration.isEmpty()) {
            itemView.duration.visibility = View.GONE
            // set to invisible to keep the space
            itemView.invisible_duration.visibility = View.INVISIBLE
        } else {
            itemView.duration.visibility = View.VISIBLE
            itemView.duration.text = mItem.contentDuration
            itemView.invisible_duration.visibility = View.GONE
        }
    }

    override fun toString(): String {
        return "ViewHolderTypeF1CutOut ${itemView.title?.text}"
    }

}
