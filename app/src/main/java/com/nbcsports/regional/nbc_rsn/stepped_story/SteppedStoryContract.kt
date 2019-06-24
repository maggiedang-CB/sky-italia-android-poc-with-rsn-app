package com.nbcsports.regional.nbc_rsn.stepped_story

import android.app.Activity
import com.google.android.exoplayer2.ui.PlayerView
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo
import com.nbcsports.regional.nbc_rsn.common.FeedComponent
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.stepped_story.components.SteppedListAdapter

interface SteppedStoryContract {

    interface View {
        fun setPresenter(steppedStoryPresenter: SteppedStoryPresenter)

        fun doExit(animated: Boolean)
    }

    interface Presenter {
        fun share(title: String?)

        fun setTeam(team: Team?)

        fun setFeedComponent(feedComponent: FeedComponent?)

        fun getPageInfo() : PageInfo?

        fun switchTarget(oldPlayerView: PlayerView?, newPlayerView: PlayerView?)

        fun setAppropriateStatusBarColor(activity: Activity?, isCurrentSteppedFragment: Boolean)
    }

    interface ViewHelper {
        /**
         * In case the current item is too short, we want to increase the height to be just enough
         * to show the next item preview at the bottom of the screen.
         */
        fun adjustHeightIfNeeded(incomingHolder: SteppedListAdapter.ViewHolder)
    }
}