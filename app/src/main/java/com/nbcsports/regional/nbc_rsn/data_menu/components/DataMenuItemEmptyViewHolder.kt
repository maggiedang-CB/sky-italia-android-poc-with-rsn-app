package com.nbcsports.regional.nbc_rsn.data_menu.components

import android.view.View
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeBase

class DataMenuItemEmptyViewHolder(itemView: View, itemViewType: Int, val team: Team) : ViewHolderTypeBase(itemView, itemViewType) {

    override fun toString(): String {
        return "DataMenuItemEmptyViewHolder"
    }

}