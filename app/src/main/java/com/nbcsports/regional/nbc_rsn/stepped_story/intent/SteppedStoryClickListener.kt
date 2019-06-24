package com.nbcsports.regional.nbc_rsn.stepped_story.intent

import androidx.recyclerview.widget.RecyclerView
import android.widget.LinearLayout
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.*
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager
import com.nbcsports.regional.nbc_rsn.stepped_story.SteppedStoryFragment
import com.nbcsports.regional.nbc_rsn.stepped_story.components.SteppedUpNextViewHolder

class SteppedStoryClickListenerFactory(
        steppedStoryFragment: SteppedStoryFragment?,
        private val dataManager: TeamFeedDataManager,
        private val dataPos: Int
) : ViewHolderClickListenerFactory<SteppedStoryFragment>(steppedStoryFragment, dataManager.team, dataManager.data) {

    override fun getClickListener(vh: RecyclerView.ViewHolder, fragment: SteppedStoryFragment?): ViewHolderClickListener {
        return UpNextClickListener(fragment, vh, dataManager, dataPos)
    }
}

private class UpNextClickListener(
        private val hostFragment: SteppedStoryFragment?,
        vh: RecyclerView.ViewHolder,
        private val dataManager: TeamFeedDataManager,
        private val dataPos: Int
) : ViewHolderClickListener(vh, dataManager.team, dataManager.data) {

    override fun checkViewHolderType(vh: RecyclerView.ViewHolder): Boolean {
        return vh is SteppedUpNextViewHolder
    }

    override fun onViewHolderClicked(adapterPos: Int) {

        val backgroundViewToFadeTo = hostFragment?.view?.findViewById<LinearLayout>(R.id.stepped_background_to_fade_to) ?: return

        NavigationManager.getInstance().openCommonCardDetailsScreen(
                backgroundViewToFadeTo,
                hostFragment.parentFragment!!.childFragmentManager,
                team,
                mValues,
                dataPos,
                null)
    }
}