package com.nbcsports.regional.nbc_rsn.editorial_detail

import android.app.Activity
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo
import com.nbcsports.regional.nbc_rsn.common.FeedComponent
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem
import java.util.ArrayList

interface EditorialDetailContract {

    interface View {
        fun setPresenter(editorialDetailPresenter: EditorialDetailPresenter)

        fun doExit(animated: Boolean)

        fun showTeamviewListItems(team: Team, teamFeedComponentList: ArrayList<FeedComponent>, items: List<EditorialDetailItem>)

        fun getEditorialDetailsPageChangeListener() : EditorialDetailsPageChangeListener
    }

    interface Presenter {
        fun getTeamviewItemData()

        fun share(title: String?)

        fun setTeam(team: Team?)

        fun setTeamFeedComponentList(teamFeedComponentList: ArrayList<FeedComponent>?)

        fun setSelectedFeedComponentPosition(selectedFeedComponentPosition: Int)

        fun setComponentId(componentId: String)

        fun getPageInfo() : PageInfo?

        fun setAppropriateStatusBarColor(activity: Activity?, isCurrentSteppedFragment: Boolean)
    }
}
