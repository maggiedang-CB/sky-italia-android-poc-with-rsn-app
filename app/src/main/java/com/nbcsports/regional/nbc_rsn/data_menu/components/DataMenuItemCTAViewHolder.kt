package com.nbcsports.regional.nbc_rsn.data_menu.components

import android.view.View
import android.widget.TextView
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.data_menu.models.DataMenuOverviewDataModel
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeBase

class DataMenuItemCTAViewHolder(itemView: View, itemViewType: Int, val team: Team) : ViewHolderTypeBase(itemView, itemViewType) {

    private var dataMenuItemCTATitleTextView: TextView? = null
    private var dataMenuItemCTASubtitleTextView: TextView? = null

    init {
        dataMenuItemCTATitleTextView = itemView.findViewById(R.id.data_menu_item_cta_title_text_view)
        dataMenuItemCTASubtitleTextView = itemView.findViewById(R.id.data_menu_item_cta_subtitle_text_view)
    }

    fun bind(dataModel: DataMenuOverviewDataModel) {
        dataMenuItemCTATitleTextView?.text = dataModel.ctaTitle
        dataMenuItemCTASubtitleTextView?.text = dataModel.ctaSubtitle
    }

    override fun toString(): String {
        return "DataMenuItemCTAViewHolder"
    }

}