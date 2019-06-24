package com.nbcsports.regional.nbc_rsn.data_menu.components

import androidx.fragment.app.FragmentManager
import android.view.View
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.common.VelocityWrapContentHeightViewPager
import com.nbcsports.regional.nbc_rsn.common.ZoomFadePageTransformer
import com.nbcsports.regional.nbc_rsn.data_menu.components.carousel_components.DataMenuItemCarouselViewPagerAdapter
import com.nbcsports.regional.nbc_rsn.data_menu.models.DataMenuOverviewCarouselDataModel
import com.nbcsports.regional.nbc_rsn.data_menu.models.DataMenuOverviewDataModel
import com.nbcsports.regional.nbc_rsn.team_feed.components.ViewHolderTypeBase
import com.nbcsports.regional.nbc_rsn.utils.DisplayUtils

class DataMenuItemCarouselViewHolder(private val fragmentManager: FragmentManager, itemView: View, itemViewType: Int, private val team: Team) : ViewHolderTypeBase(itemView, itemViewType) {

    private var mainViewPager: VelocityWrapContentHeightViewPager? = null
    private var dataMenuItemCarouselViewPagerAdapter: DataMenuItemCarouselViewPagerAdapter? = null

    private val MIN_SCALE: Float = 0.5f
    private val MIN_ROOT_ALPHA: Float = 0.5f
    private val MIN_DATA_TITLE_ALPHA: Float = 0.0f
    private val MIN_BOTTOM_LABEL_ALPHA: Float = 0.0f
    private val DATA_TITLE_TRANSIT_FACTOR: Float = 0.3f
    private val BOTTOM_LABEL_TRANSIT_FACTOR: Float = 0.3f

    init {
        mainViewPager = itemView.findViewById(R.id.data_menu_item_carousel_main_view_pager)

        initViewPager()
    }

    private fun initViewPager() {
        dataMenuItemCarouselViewPagerAdapter = DataMenuItemCarouselViewPagerAdapter(fragmentManager, team)
        mainViewPager?.apply {
            setPadding(DisplayUtils.getScreenWidth(context) / 4, 0,
                    DisplayUtils.getScreenWidth(context) / 4, 0)
            offscreenPageLimit = 10
            setPageTransformer(false, ZoomFadePageTransformer(
                    1.0f * paddingLeft,
                    MIN_SCALE, MIN_ROOT_ALPHA,
                    MIN_DATA_TITLE_ALPHA,
                    MIN_BOTTOM_LABEL_ALPHA,
                    DATA_TITLE_TRANSIT_FACTOR,
                    BOTTOM_LABEL_TRANSIT_FACTOR))
            adapter = dataMenuItemCarouselViewPagerAdapter
        }
    }

    private fun setCarouselData(itemList: List<DataMenuOverviewCarouselDataModel>) {
        dataMenuItemCarouselViewPagerAdapter?.setData(itemList)
        mainViewPager?.setCurrentItem((itemList.size - 1) / 2, false)
    }

    fun bind(dataModel: DataMenuOverviewDataModel) {
        dataModel.carouselList?.let {
            setCarouselData(it)
        }
    }

    override fun toString(): String {
        return "DataMenuItemCarouselViewHolder"
    }

}