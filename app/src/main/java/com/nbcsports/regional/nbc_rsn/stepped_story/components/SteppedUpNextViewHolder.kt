package com.nbcsports.regional.nbc_rsn.stepped_story.components

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Team
import kotlinx.android.synthetic.main.teamview_card_type_feed_text_only.view.*

class SteppedUpNextViewHolder(root: View, itemViewType: Int, team: Team) : SteppedListAdapter.ViewHolder(root, itemViewType, team) {

    init {
        // hide useless views
        itemView.firstFeedCardBackground.visibility = View.GONE
        itemView.firstFeedCardBackgroundExtra.visibility = View.GONE
        itemView.topic_tag_background_layer_container.visibility = View.GONE
        itemView.title_editorial_indicator.visibility = View.GONE
        itemView.title_indicator.visibility = View.GONE
        itemView.title.visibility = View.GONE

        itemView.title_editorial.visibility = View.VISIBLE
        itemView.topSeparatorLineInEditorial.visibility = View.VISIBLE
        itemView.upnext_label.visibility = View.VISIBLE
        itemView.bottom_space.visibility = View.VISIBLE

        // make it dark theme
        itemView.setBackgroundResource(R.color.black)
        itemView.topic_tag_text.setBackgroundResource(R.color.black)
        itemView.title_editorial.setTextColor(Color.WHITE)
        itemView.topic_tag_text.setTextColor(Color.WHITE)
        itemView.upnext_label.setTextColor(Color.WHITE)
    }

    override fun bind() {}

    override fun allowScrollTo(): Boolean {
        return false
    }

    override fun getPreviewHeight(): Int {
        return itemView.height
    }

    fun setCardAttributes() {

        itemView.topic_tag_text.text = mItem.tag.toUpperCase()
        itemView.title_editorial.text = mItem.title

        val ss = SpannableString("${mItem.author} / ${DateFormatUtils.getTimeAgoText(mItem.publishedDate)}")
        ss.setSpan(ForegroundColorSpan(Color.WHITE), 0, mItem.author.length, 0)
        ss.setSpan(ForegroundColorSpan(Color.GRAY), mItem.author.length + 1, ss.length, 0)
        itemView.author_and_publish_time.text = ss
        // This would be an alternative Author/Publish_time implementation as per the xml layout
        //author.setText(editorialDetailItem.getAuthor());
        //publish_time.setText(editorialDetailItem.getPublishedDate());
    }

    fun setCardAttributesForEditorialDetailFragment() {}
}