package com.nbcsports.regional.nbc_rsn.team_feed

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import androidx.fragment.app.Fragment
import com.nbcsports.regional.nbc_rsn.common.FeedComponent
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.common.TeamViewFeed
import org.joda.time.DateTime

interface TeamFeedContract {
    interface View {
        fun setTeamColorGradient(gradient: GradientDrawable)
        fun setStatusBarColor(color: Int)
        // region List update
        fun updatedAdapter(items: List<FeedComponent>, feedLabelIndex: Int, team: Team): Boolean
        fun updateAdapterForBackAndFore(items: List<FeedComponent>, feedLabelIndex: Int, team: Team)
        fun updateAdapterForLiveAssetsAutoRefresh(items: List<FeedComponent>, feedLabelIndex: Int, team: Team)
        fun updateAdapterForTeamFeedAutoRefresh(items: List<FeedComponent>, feedLabelIndex: Int, team: Team)
        fun setupRecyclerView(items: List<FeedComponent>, feedLabelIndex: Int, team: Team)
        // endregion
        fun resumeAdapter(): Boolean
        fun setUpLifeCycleListenerAndAutoRefreshDO()
        fun showRefreshError()
        fun getTopChildFragment(): Fragment?
        fun getActivityRef(): Activity?
    }

    interface RepoListener {
        fun onTeamFeedSucceed(latestTeamViewFeed: TeamViewFeed?, currentTime: DateTime,
                              isBackAndFore: Boolean, isLiveAssetsAutoRefresh: Boolean, isTeamFeedAutoRefresh: Boolean)
        fun onTeamFeedFailed(e: Throwable)
        fun onPreloadSucceed()
        fun onPreloadFailed(e: Throwable)
        fun onStartFabTapAnimation()
        fun onStopFabTapAnimation()
        fun onShowFabTapMessage()
        fun onHideFabTapMessage()
        fun onShowDataMenuMessage()
        fun onHideDataMenuMessage()
        fun hasAtLeastOneStackableViewOpened(): Boolean
        fun isCurrentSteppedFragment(): Boolean
    }
}