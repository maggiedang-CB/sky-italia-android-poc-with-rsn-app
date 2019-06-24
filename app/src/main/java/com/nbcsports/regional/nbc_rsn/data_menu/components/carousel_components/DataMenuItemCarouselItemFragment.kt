package com.nbcsports.regional.nbc_rsn.data_menu.components.carousel_components

import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import android.view.View
import android.widget.ProgressBar
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.BaseFragment
import com.nbcsports.regional.nbc_rsn.common.CircleProgressBar
import com.nbcsports.regional.nbc_rsn.common.Constants.*
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.data_menu.models.DataMenuOverviewCarouselDataModel

class DataMenuItemCarouselItemFragment: BaseFragment(), DataMenuItemCarouselItemContract.View {

    companion object {
        fun newInstance(team: Team?, carouselData: DataMenuOverviewCarouselDataModel?): DataMenuItemCarouselItemFragment {
            val dataMenuItemCarouselItemFragment = DataMenuItemCarouselItemFragment()

            val args = Bundle()
            args.putParcelable(TEAM_KEY, team)
            args.putParcelable(CAROUSEL_DATA_KEY, carouselData)
            dataMenuItemCarouselItemFragment.arguments = args

            return dataMenuItemCarouselItemFragment
        }
    }

    private var carouselItemCircleProgressBar: CircleProgressBar? = null
    private var carouselItemDataOutputTextView: AppCompatTextView? = null
    private var carouselItemDataTitleTextView: AppCompatTextView? = null
    private var carouselItemBottomLabelTextView: AppCompatTextView? = null

    private var presenter: DataMenuItemCarouselItemContract.Presenter? = null
    private var currentTeam: Team? = null
    private var currentCarouselData: DataMenuOverviewCarouselDataModel? = null

    override fun getLayout(): Int {
        return R.layout.data_menu_item_carousel_item_layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataMenuItemCarouselItemPresenter(this)
        currentTeam = arguments?.getParcelable(TEAM_KEY)
        currentCarouselData = arguments?.getParcelable(CAROUSEL_DATA_KEY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // show loading spinner
        view.findViewById<ProgressBar>(R.id.data_menu_item_carousel_spinner)?.visibility = View.VISIBLE

        carouselItemCircleProgressBar = view.findViewById(R.id.data_menu_item_carousel_item_circle_progress_bar)
        carouselItemDataOutputTextView = view.findViewById(R.id.data_menu_item_carousel_item_data_output_text_view)
        carouselItemDataTitleTextView = view.findViewById(R.id.data_menu_item_carousel_item_data_title_text_view)
        carouselItemBottomLabelTextView = view.findViewById(R.id.data_menu_item_carousel_item_bottom_label_text_view)

        if (currentCarouselData?.carouselIsPlaceHolder == true){
            hideViews()
            return
        }

        // hide loading spinner
        view.findViewById<ProgressBar>(R.id.data_menu_item_carousel_spinner)?.visibility = View.GONE

        setUpCircleProgressBarColor()

        setUpProgressAndDataOutputAndDataTitleAndBottomLabel()
    }

    override fun setPresenter(presenter: DataMenuItemCarouselItemContract.Presenter) {
        this.presenter = presenter
    }

    override fun setScale(scaleFactor: Float) {
        carouselItemCircleProgressBar?.apply {
            scaleX = scaleFactor
            scaleY = scaleFactor
        }
        carouselItemDataOutputTextView?.apply {
            scaleX = scaleFactor
            scaleY = scaleFactor
        }
    }

    private fun hideViews() {
        carouselItemCircleProgressBar?.alpha = 0.0f
        carouselItemDataOutputTextView?.alpha = 0.0f
        carouselItemDataTitleTextView?.alpha = 0.0f
        carouselItemBottomLabelTextView?.alpha = 0.0f
    }

    private fun setUpCircleProgressBarColor() {
        presenter?.setUpCircleProgressBarColor(currentTeam, carouselItemCircleProgressBar)
    }

    private fun setUpProgressAndDataOutputAndDataTitleAndBottomLabel() {
        currentCarouselData?.let {
            carouselItemDataTitleTextView?.text = it.carouselTitle
            carouselItemCircleProgressBar?.setProgress(it.carouselProgress)
            carouselItemDataOutputTextView?.text = it.carouselValue
            carouselItemBottomLabelTextView?.text = it.carouselLabel
        }
    }

}