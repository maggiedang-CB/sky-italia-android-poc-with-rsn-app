package com.nbcsports.regional.nbc_rsn.common

import androidx.viewpager.widget.ViewPager
import android.view.View
import com.nbcsports.regional.nbc_rsn.R

class ZoomFadePageTransformer(private val viewPagerPadding: Float, private val minScale: Float,
                              private val minRootAlpha: Float, private val minDataTitleAlpha: Float,
                              private val minBottomLabelAlpha: Float,
                              private val dataTitleTransitFactor: Float,
                              private val bottomLabelTransitFactor: Float): ViewPager.PageTransformer {

    private var offset: Float = -1.0f

    /**
     * This method perform the page transform animation
     *
     * ViewPager's left edge position -1
     * ViewPager's right edge position 1
     * ViewPager's centre position 0
     */
    override fun transformPage(view: View, position: Float) {
        if (offset == -1.0f){
            offset = viewPagerPadding / view.measuredWidth * 1.0f
        }
        // Get the correct position by removing the padding offset
        val correctPosition: Float = position - offset
        when {
            correctPosition < -2 -> {
                // Keep the same state after exist view pager's left edge + half screen width
                // (i.e. circle progress bar, data output, data title and bottom label)
                view.alpha = minRootAlpha

                val carouselItemDataTitleTextView: View? = view.findViewById(R.id.data_menu_item_carousel_item_data_title_text_view)
                carouselItemDataTitleTextView?.alpha = minDataTitleAlpha

                val carouselItemBottomLabelTextView: View? = view.findViewById(R.id.data_menu_item_carousel_item_bottom_label_text_view)
                carouselItemBottomLabelTextView?.alpha = minBottomLabelAlpha
            }
            correctPosition <= 2 -> {
                // Scaling (i.e. circle progress bar and data output)
                val scaleFactor: Float = Math.max(minScale, 1.0f - Math.abs(correctPosition * (1.0f - minScale)))
                val carouselItemCircleProgressBar: View? = view.findViewById(R.id.data_menu_item_carousel_item_circle_progress_bar)
                carouselItemCircleProgressBar?.apply {
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }

                val carouselItemDataOutputTextView: View? = view.findViewById(R.id.data_menu_item_carousel_item_data_output_text_view)
                carouselItemDataOutputTextView?.apply {
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }

                // Changing alpha (i.e. circle progress bar, data output, data title and bottom label)
                val alphaRootFactor: Float = Math.max(minRootAlpha, 1.0f - Math.abs(correctPosition * (1.0f - minRootAlpha)))
                view.alpha = alphaRootFactor

                val alphaDataTitleFactor: Float = Math.max(minDataTitleAlpha, 1.0f - Math.abs(correctPosition * (1.0f - minDataTitleAlpha)))
                val carouselItemDataTitleTextView: View? = view.findViewById(R.id.data_menu_item_carousel_item_data_title_text_view)
                carouselItemDataTitleTextView?.alpha = alphaDataTitleFactor

                val alphaBottomLabelFactor: Float = Math.max(minBottomLabelAlpha, 1.0f - Math.abs(correctPosition * (1.0f - minBottomLabelAlpha)))
                val carouselItemBottomLabelTextView: View? = view.findViewById(R.id.data_menu_item_carousel_item_bottom_label_text_view)
                carouselItemBottomLabelTextView?.alpha = alphaBottomLabelFactor

                // Transitioning (i.e. data title and bottom label)
                carouselItemDataTitleTextView?.animate()?.apply {
                    x(dataTitleTransitFactor * view.measuredWidth * correctPosition)
                    duration = 0L
                    start()
                }

                carouselItemBottomLabelTextView?.animate()?.apply {
                    x(bottomLabelTransitFactor * view.measuredWidth * correctPosition)
                    duration = 0L
                    start()
                }
            }
            else -> {
                // Keep the same state after exist view pager's right edge + half screen width
                // (i.e. circle progress bar, data output, data title and bottom label)
                view.alpha = minRootAlpha

                val carouselItemDataTitleTextView: View? = view.findViewById(R.id.data_menu_item_carousel_item_data_title_text_view)
                carouselItemDataTitleTextView?.alpha = minDataTitleAlpha

                val carouselItemBottomLabelTextView: View? = view.findViewById(R.id.data_menu_item_carousel_item_bottom_label_text_view)
                carouselItemBottomLabelTextView?.alpha = minBottomLabelAlpha
            }
        }
    }

}