package com.nbcsports.regional.nbc_rsn.team_feed.intent

import androidx.recyclerview.widget.RecyclerView
import android.widget.LinearLayout
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.FeedComponent
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.common.ViewHolderClickListener
import com.nbcsports.regional.nbc_rsn.common.ViewHolderClickListenerFactory
import com.nbcsports.regional.nbc_rsn.common.components.ViewHolderTypeFeedTextOnly
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager
import com.nbcsports.regional.nbc_rsn.persistentplayer.Behaviour
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayer
import com.nbcsports.regional.nbc_rsn.team_feed.components.*
import com.nbcsports.regional.nbc_rsn.team_feed.template.TeamFeedFragment
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt
import com.nbcsports.regional.nbc_rsn.utils.NativeShareUtils

class TeamFeedItemClickListenerFactory(
        teamFeedFragment: TeamFeedFragment,
        team: Team,
        feed: List<FeedComponent>,
        private val persistentPlayer: PersistentPlayer
) : ViewHolderClickListenerFactory<TeamFeedFragment>(teamFeedFragment, team, feed) {

    override fun getClickListener(vh: RecyclerView.ViewHolder, fragment: TeamFeedFragment?): ViewHolderClickListener {
        return when (vh) {
            is ViewHolderTypeFeedPromo -> TeamFeedPromoClickListener(fragment, vh, team, feed, persistentPlayer)
            else -> TeamFeedEditorialClickListener(fragment, vh, team, feed)
        }
    }
}

private class TeamFeedEditorialClickListener(
        private val teamFeedFragment: TeamFeedFragment?,
        vh: RecyclerView.ViewHolder,
        team: Team,
        values: List<FeedComponent>
) : ViewHolderClickListener(vh, team, values) {

    override fun checkViewHolderType(vh: RecyclerView.ViewHolder): Boolean {
        return vh is ViewHolderTypeImage
                || vh is ViewHolderTypeVideo
                || vh is ViewHolderTypeAudio
                || vh is ViewHolderTypeF1CutOut
                || vh is ViewHolderTypeF2Standard
                || vh is ViewHolderTypeFeedStandard
                || vh is ViewHolderTypeFeedTextOnly
    }

    override fun onViewHolderClicked(adapterPos: Int) {

        val backgroundViewToFadeTo = teamFeedFragment?.view?.findViewById<LinearLayout>(R.id.teamview_page_background_to_fade_to)
                ?: return

        NavigationManager.getInstance().openCommonCardDetailsScreen(
                backgroundViewToFadeTo,
                teamFeedFragment.childFragmentManager,
                team,
                mValues,
                adapterPos,
                teamFeedFragment
        )
    }
}

private class TeamFeedPromoClickListener(
        private val teamFeedFragment: TeamFeedFragment?,
        vh: RecyclerView.ViewHolder,
        team: Team,
        values: List<FeedComponent>,
        private val persistentPlayer: PersistentPlayer
) : ViewHolderClickListener(vh, team, values) {

    override fun checkViewHolderType(vh: RecyclerView.ViewHolder): Boolean {
        return vh is ViewHolderTypeFeedPromo
    }

    override fun onViewHolderClicked(adapterPos: Int) {
        val feedPromoMediaSource = mValues[adapterPos].mediaSource ?: return
        // todo: should we do something when media source is null?

        if (!Behaviour.isCellularDataAllow(feedPromoMediaSource, teamFeedFragment?.context)) {
            NotificationsManagerKt.showCellularDataNotAllowError()
            return
        }

        if (persistentPlayer.isMiniShown) {
            persistentPlayer.showMini(false)
        }
        persistentPlayer.setMediaSource(feedPromoMediaSource)

        val shareInfo = NativeShareUtils.generateShareInfo(
                teamFeedFragment,
                null,
                feedPromoMediaSource.title,
                feedPromoMediaSource,
                team.teamId
        )
        persistentPlayer.updateShareInfo(shareInfo)
        persistentPlayer.showAs247()
    }
}