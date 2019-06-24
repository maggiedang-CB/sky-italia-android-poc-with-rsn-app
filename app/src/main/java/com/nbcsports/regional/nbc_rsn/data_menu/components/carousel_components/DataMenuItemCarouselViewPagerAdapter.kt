package com.nbcsports.regional.nbc_rsn.data_menu.components.carousel_components

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.data_menu.models.DataMenuOverviewCarouselDataModel

class DataMenuItemCarouselViewPagerAdapter(fragmentManager: FragmentManager, private val team: Team) : FragmentStatePagerAdapter(fragmentManager) {

    private var carouselItemList: MutableList<DataMenuOverviewCarouselDataModel>? = null

    init {
        carouselItemList = mutableListOf()
    }

    override fun getItem(position: Int): Fragment {
        return DataMenuItemCarouselItemFragment.newInstance(
                team,
                carouselItemList?.get(position))
    }

    override fun getCount(): Int {
        return carouselItemList?.size ?: 0
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    fun setData(itemList: List<DataMenuOverviewCarouselDataModel>) {
        if (carouselItemList == null){
            carouselItemList = mutableListOf()
        }
        carouselItemList?.apply {
            clear()
            addAll(itemList)
        }
        notifyDataSetChanged()
    }

}