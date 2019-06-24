package com.nbcsports.regional.nbc_rsn.data_menu.components.carousel_components

import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import android.view.View
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.CircleProgressBar
import com.nbcsports.regional.nbc_rsn.common.Team
import kotlin.math.roundToInt

class DataMenuItemCarouselItemPresenter(view: DataMenuItemCarouselItemContract.View):
        DataMenuItemCarouselItemContract.Presenter {

    init {
        view.setPresenter(this)
    }

    override fun setUpCircleProgressBarColor(team: Team?, vararg views: View?) {
        var carouselItemCircleProgressBar: View? = null
        for (view in views){
            view?.let {
                when (it.id){
                    R.id.data_menu_item_carousel_item_circle_progress_bar -> carouselItemCircleProgressBar = it
                }
            }
        }
        carouselItemCircleProgressBar?.apply {
            var foregroundColorInt: Int = ResourcesCompat.getColor(resources, R.color.carousel_default_red, null)
            var backgroundColorInt: Int = ResourcesCompat.getColor(resources, R.color.carousel_default_red, null)
            // Set foreground color and background color with team's primary color and
            // secondary color appropriately
            // For light profile teams = primary color
            // For dark profile teams = secondary color
            team?.let {
                when (it.lightProfileTeam){
                    true -> {
                        foregroundColorInt = Color.parseColor(it.primaryColor)
                        backgroundColorInt = Color.parseColor(it.primaryColor)
                    }
                    else -> {
                        foregroundColorInt = Color.parseColor(it.secondaryColor)
                        backgroundColorInt = Color.parseColor(it.secondaryColor)
                    }
                }
            }
            // Set up alpha value for background color
            backgroundColorInt = Color.argb((255.0f * 0.35f).roundToInt(),
                    backgroundColorInt.red, backgroundColorInt.green, backgroundColorInt.blue)
            // Apply foreground color and background color
            // into carousel item circle progress bar
            (this as CircleProgressBar).setProgressBarForegroundColor(foregroundColorInt)
            (this as CircleProgressBar).setProgressBarBackgroundColor(backgroundColorInt)
        }
    }

}