package com.nbcsports.regional.nbc_rsn.data_bar

import androidx.recyclerview.widget.RecyclerView
import com.nbcsports.regional.nbc_rsn.team_feed.template.TeamFeedFragment
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil

class DataBarScrollListener(var callback: TeamFeedFragment.DataBarScrolling): RecyclerView.OnScrollListener() {

    private var totalScroll = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        // Check if data menu ftue is in process and not done yet and vertical scroll distance is
        // not 0 and is shown long enough, if so, marks data menu ftue as done (scenario 6)
        if (!FtueUtil.hasDoneDataMenuFtue()
                && NotificationsManagerKt.isFtueDataMenuBottomBannerShowing()
                && dy != 0
                && FtueUtil.isDataMenuMsgShownLongEnough()){
            // Set data menu ftue done to true
            FtueUtil.setHasDoneDataMenuFtue(true)
        }

        totalScroll += dy

        // This will fix data bar not expands to full size
        // when scroll to the top of recycler view sometime
        if (!recyclerView.canScrollVertically(-1)){
            totalScroll = 0
        }

        callback.onDataBarScroll(dx, totalScroll)
    }
}