package com.nbcsports.regional.nbc_rsn.data_menu.viewholders

import androidx.appcompat.widget.AppCompatTextView
import android.view.View
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeBase

class DataMenuScoringSummaryHeaderVH(itemView: View, itemViewType: Int) : ViewHolderTypeBase(itemView, itemViewType) {

    private val scoringSummaryHeaderTextView: AppCompatTextView? = itemView.findViewById(R.id.scoring_summary_header_text_view)

    init {
        scoringSummaryHeaderTextView?.text = if (LocalizationManager.isInitialized()){
            LocalizationManager.DataMenu.BoxscoreNHLScoringSummary
        } else itemView.resources.getString(R.string.scoring_summary)
    }

    override fun toString(): String {
        return "DataMenuScoringSummaryHeaderVH"
    }

}