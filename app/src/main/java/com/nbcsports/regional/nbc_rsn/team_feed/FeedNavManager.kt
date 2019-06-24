package com.nbcsports.regional.nbc_rsn.team_feed

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.view.View
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.editorial_detail.EditorialDetailTemplateFragment
import com.nbcsports.regional.nbc_rsn.stepped_story.SteppedStoryFragment
import com.nbcsports.regional.nbc_rsn.utils.AnimationUtil
import kotlinx.android.synthetic.main.editorial_detail_components_list.*
import kotlinx.android.synthetic.main.fragment_stepped_story.*

class FeedNavManager(private val fragmentManager: FragmentManager) {

    companion object {
        fun newInstance(fragmentManager: FragmentManager): FeedNavManager? {
            return FeedNavManager(fragmentManager)
        }
    }

    private val ANIMATION_DURATION = 500  // R.integer.fragment_animation_duration2

    /*
    Extension method to check whether these fragments can be stacked.

    Currently only stepped story and editorials can stack. We can update this function
    if more are added in the future
     */
    private fun Fragment.isStackableFragment(): Boolean {
        return (this is SteppedStoryFragment || this is EditorialDetailTemplateFragment)
    }

    private fun withinLimit(position: Int): Boolean {
        return position in 0..fragmentManager.fragments.size
    }

    fun closePage(animated: Boolean, backgroundFadeTo: View) {
        val fragmentListSize = fragmentManager.fragments.size
        val positionPrevious = fragmentListSize - 2
        var fragment: Fragment? = null
        val withinLimit = withinLimit(positionPrevious)

        if (withinLimit) {
            fragment = fragmentManager.fragments[positionPrevious]
        }

        if (animated) {
            when (fragment) {
                is EditorialDetailTemplateFragment -> AnimationUtil.fadeIn(ANIMATION_DURATION, fragment.editorial_background_to_fade_to)
                is SteppedStoryFragment -> AnimationUtil.fadeIn(ANIMATION_DURATION, fragment.stepped_background_to_fade_to)
                else -> AnimationUtil.fadeIn(ANIMATION_DURATION, backgroundFadeTo)
            }
        }

        var enter = 0
        var exit = 0
        if (animated) {
            enter = R.anim.no_anim
            exit = R.anim.slide_out_up_exit
        }


        val fragmentTransaction = fragmentManager
                .beginTransaction()
                .setCustomAnimations(enter, exit)

        val fragmentList = fragmentManager.fragments
        if (fragmentList.isEmpty()) {
            fragmentTransaction
                    .commit()
        } else {
            fragmentTransaction
                    .remove(fragmentList[fragmentListSize - 1])
                    .commit()
        }

        fragmentManager.popBackStack()
    }

    fun closeAllPages(animated: Boolean, backgroundFadeTo: View) {
        val fragList = fragmentManager.fragments.filter { it.isStackableFragment() }

        if (fragList.isEmpty()) {
            return
        }

        var enter = 0
        var exit = 0
        if (animated) {
            AnimationUtil.fadeIn(ANIMATION_DURATION, backgroundFadeTo)
            enter = R.anim.no_anim
            exit = R.anim.slide_out_down_exit
        }

        for (i in fragList.indices.reversed()) {
            if (i == fragList.size - 1) { //Final item.
                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(enter, exit)
                        .remove(fragList[i])
                        .commit()
            } else {
                fragmentManager
                        .beginTransaction()
                        .remove(fragList[i])
                        .commit()
            }
            // fragments were getting removed from the list,
            // but were not being removed from the back stack
            fragmentManager.popBackStack()
        }
    }

    fun hasAtLeastOneStackableViewOpen(): Boolean {
        return fragmentManager.fragments.any { it.isStackableFragment() }
    }
}