package com.nbcsports.regional.nbc_rsn.common

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.View
import androidx.core.view.children

class VelocityWrapContentHeightViewPager: ViewPager {

    constructor(context: Context): super(context) {

    }

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxChildHeight = 0
        for (childView: View in children){
            childView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val childViewHeight: Int = childView.measuredHeight
            if (childViewHeight > maxChildHeight){
                maxChildHeight = childViewHeight
            }
        }
        // Using 3/4 of max child height is because after setting
        // padding (25% of screen width each side) to view pager,
        // max child height will be equaled to original child height
        // So need to remove 1/4 of the child height to remove empty
        // space
        when(maxChildHeight * 3 / 4) {
            0 -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            else -> super.onMeasure(widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(maxChildHeight * 3 / 4, MeasureSpec.EXACTLY))
        }
    }

}