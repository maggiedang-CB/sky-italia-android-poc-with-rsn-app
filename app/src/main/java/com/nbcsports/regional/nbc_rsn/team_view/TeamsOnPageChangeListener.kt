package com.nbcsports.regional.nbc_rsn.team_view

import android.app.Activity
import androidx.viewpager.widget.ViewPager

import com.nbcsports.regional.nbc_rsn.MainActivity
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelper
import com.nbcsports.regional.nbc_rsn.fabigation.FabMenuInterface

class TeamsOnPageChangeListener(var viewPagerAdapter: TeamsPagerAdapter?, private val activity: Activity, private val fabMenuInterface: FabMenuInterface) : ViewPager.OnPageChangeListener {
    private val teamSize: Int
        get() = viewPagerAdapter!!.teams.size

    init {
        viewPagerAdapter?.currentFragment?.let {
            (it as TeamContainerFragment).trackPage()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        if (position % teamSize >= teamSize) {
            return
        }

        viewPagerAdapter?.teams?.get(position % teamSize)?.let {
            (activity as MainActivity).teamManager.setSelectedTeam(it)
            (activity as MainActivity).onTeamsPageChange()
            fabMenuInterface.switchFabLogo(it)
            val pageInfo = PageInfo(
                    contextData = false,
                    businessUnit = it.regionName,
                    team = it.displayName,
                    section = "Home"
            )
            TrackingHelper.trackPageEvent(pageInfo)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}


}
