package com.nbcsports.regional.nbc_rsn.fabigation

import android.content.Context
import android.content.res.Resources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import com.nbcsports.regional.nbc_rsn.R

class FabMenuRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var dx: Int = 0
    var totalWidth = 0

    val FIRST_ADAPTER_POSITION = 0

    fun scrollToPercent(percent: Float) {
        val tempWidth = computeTotalWidth()
        if (tempWidth > 0 && (totalWidth == 0 || totalWidth != tempWidth)) {
            // only update total width if it is measured
            totalWidth = tempWidth
        }

        if (totalWidth > 0) {
            val xx = (percent * totalWidth).toInt()
            val diff: Int = xx - dx
            scrollBy(diff, 0)
            dx = xx
        }
    }

    fun computeTotalWidth(): Int {
        var totalWidth = 0
        val itemCount = layoutManager?.itemCount ?: 0

        if (itemCount > 1) {
            findViewHolderForAdapterPosition(FIRST_ADAPTER_POSITION)?.itemView?.let {
                totalWidth = (itemCount - 1) * it.layoutParams.width
            }

            // add settings
            totalWidth += findViewHolderForAdapterPosition(0)?.itemView?.layoutParams?.width ?: 0
        }

        return totalWidth
    }

    fun resetPosition() {
        dx = 0
    }

    fun getSelectedPosition(isRTL: Boolean = false): Int {
        requireNotNull(layoutManager) { "LayoutManager not initialized." }

        val lm = layoutManager as LinearLayoutManager
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val marginWidth = (resources.getDimension(R.dimen.menu_card_margin)).toInt()
        val middle = (screenWidth / 2) + marginWidth

        val firstItem = lm.findFirstVisibleItemPosition()
        val lastItem = lm.findLastVisibleItemPosition()
        val firstX = if (firstItem == -1) 0 else findViewHolderForAdapterPosition(firstItem)?.itemView?.x?.toInt() ?: 0
        val lastX = if (lastItem == -1) 0 else findViewHolderForAdapterPosition(lastItem)?.itemView?.x?.toInt() ?: 0

        return if (firstItem != -1 && lastItem != -1 && lastItem - firstItem == 2) {
            (lastItem + firstItem) / 2
        } else {
            if ((isRTL && firstX > middle) || (!isRTL && lastX < middle)) {
                lastItem
            } else {
                firstItem
            }
        }
    }

    fun scrollToCenterItem(isRTL: Boolean = false) {
        requireNotNull(layoutManager) { "LayoutManager not initialized." }

        val lm = layoutManager as LinearLayoutManager
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val marginWidth = (resources.getDimension(R.dimen.menu_card_margin)).toInt()

        val pos = getSelectedPosition(isRTL = isRTL)
        if (pos != NO_POSITION && pos < lm.itemCount) {
            val view = lm.findViewByPosition(pos) ?: return

            val leftEdge = view.left
            val rightEdge = view.right

            val leftMargin = ((screenWidth - view.width) / 2)
            val rightMargin = (screenWidth - view.width) / 2 + view.width

            val scrollDistanceLeft = leftEdge - leftMargin
            val scrollDistanceRight = rightMargin - rightEdge

            if (leftEdge > screenWidth / 2 + marginWidth) {
                smoothScrollBy(-scrollDistanceRight, 0)
            } else {
                smoothScrollBy(scrollDistanceLeft, 0)
            }
        }
    }
}
