package com.nbcsports.regional.nbc_rsn.team_feed.components

import android.view.View
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.localization.LocalizationManager
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils
import kotlinx.android.synthetic.main.teamview_card_type_thefeed_label.view.*

/**
 * Created by pengzhiquah on 2018-04-19.
 */
class ViewHolderTypeTheFeedLabel(view: View, itemViewType: Int) : ViewHolderTypeBase(view, itemViewType) {

    override fun toString(): String {
        return "ViewHolderTypeTheFeedLabel"
    }

    fun setAttributes(team: Team) {

        itemView.topSeparatorLine?.setBackgroundColor(0x44FFFFFF) //(Color.WHITE); // 25% transparent
        itemView.thefeed_label_container?.setBackgroundColor(team.getTeamColor())

        if (LocalizationManager.isInitialized()) {
            itemView.thefeed_label?.text = LocalizationManager.TeamView.TheFeed
        }

        itemView.thefeed_label_date?.text = DateFormatUtils.getCurrentThreeLetterMonthDayText().toUpperCase()
    }
}
