package com.nbcsports.regional.nbc_rsn.stepped_story.components

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import android.view.MotionEvent
import android.view.View

class SteppedListLayoutManager(private val rv: RecyclerView, private val overScrollHelper: OverScrollHelper?) : LinearLayoutManager(rv.context), View.OnTouchListener {

    private var currentItem = 0
    /**
     * The vertical over-scroll distance. Positive indicates over-scrolling bottom, negative indicates top.
     */
    private var overScrollY = 0
    private var lastScrollRequestY = 0

    private var animateTotalDistance = 0
    private var animateScrolledDistance = 0

    private val OVERSCROLL_BOUNCE_THRESHOLD: Float = 150f
    private val OVERSCROLL_MAX = 200

    private var scrollState = ScrollState.REACHED_TOP
    /**
     * Sometimes ACTION_UP is called before the last vertical scroll request. If this request happens
     * to be an over-scroll, bounce back animation will not happen. This boolean thus exists to
     * prevent further over-scroll if there's no user interaction.
     */
    private var userInteracting = false

    init {
        rv.setOnTouchListener(this)
        rv.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                return scrollState == ScrollState.ANIMATE_NEXT
                        || scrollState == ScrollState.ANIMATE_PREVIOUS
                        || scrollState == ScrollState.ANIMATE_BOUNCE_TOP
                        || scrollState == ScrollState.ANIMATE_BOUNCE_BOTTOM
            }
        }

        rv.requestDisallowInterceptTouchEvent(true)
        rv.parent.requestDisallowInterceptTouchEvent(true)
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        // Calling super.scrollVerticallyBy() will trigger the scroll. In other words, the value passed
        // into super.scrollVerticallyBy() is the distance you want to scroll, the value returned
        // is the value that's actually scrolled.
        if (scrollState == ScrollState.ANIMATE_NEXT || scrollState == ScrollState.ANIMATE_PREVIOUS) {
            // calculate scroll percentage so the ViewHolder can animate view position.
            val scrollRange: Int = super.scrollVerticallyBy(dy, recycler, state)
            animateScrolledDistance += scrollRange
            val percent = animateScrolledDistance.toFloat()/animateTotalDistance

            // currentItem has already been update to the destination position
            if (scrollState == ScrollState.ANIMATE_PREVIOUS) {
                (rv.findViewHolderForAdapterPosition(currentItem+1) as? SteppedItemViewHolder)?.apply {
                    animateToHero(percent)
                }
                (rv.findViewHolderForAdapterPosition(currentItem) as? SteppedItemViewHolder)?.apply {
                    animateToNormal(1.0f)
                }
                (rv.findViewHolderForAdapterPosition(currentItem) as? SteppedCoverViewHolder)?.apply {
                    initVariationVideo()
                }
            } else {
                (rv.findViewHolderForAdapterPosition(currentItem) as? SteppedItemViewHolder)?.apply {
                    animateToNormal(percent)
                }
            }

            return scrollRange
        } else if (scrollState == ScrollState.ANIMATE_BOUNCE_TOP || scrollState == ScrollState.ANIMATE_BOUNCE_BOTTOM) {
            // This is caused by translating the RecyclerView to create the overscroll effect.
            // If we don't handle this, the list will look shakey.
            return 0
        }

        if ((scrollState == ScrollState.REACHED_BOTTOM || scrollState == ScrollState.REACHED_TOP)
                && !userInteracting) {
            return 0
        }

        val result = when {
            dy > 0 -> scrollDownBy(dy, recycler, state)
            dy < 0 -> scrollUpBy(dy, recycler, state)
            else -> 0
        }
        lastScrollRequestY = dy

        return result
    }

    // region Scroll helper methods
    private fun scrollDownBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        val lastVisiblePos = findLastVisibleItemPosition()

        when {
            // Checking scroll state first, because the OVERSCROLL_TOP can still happen when the next
            // item preview is visible, if the current item is relatively short.
            scrollState == ScrollState.OVERSCROLL_TOP -> {
                // User is scrolling down while over-scrolling at the top
                var result = 0
                when {
                    lastScrollRequestY < 0 -> {
                        // Do nothing. This is caused by animating the RecyclerView to create the
                        // overscrolling effect.
                    }
                    overScrollY + dy > 0 -> {
                        // cancel the over-scroll effect
                        overScrollHelper?.bounceBack()
                        // then scroll the rest
                        result = super.scrollVerticallyBy(dy + overScrollY, recycler, state)
                        scrollState = ScrollState.NORMAL
                        // reset over-scroll value as no over scroll is happening right now
                        overScrollY = 0
                    }
                    overScrollY + dy == 0 -> {
                        scrollState = ScrollState.REACHED_TOP
                        overScrollY = 0
                        overScrollHelper?.bounceBack()
                    }
                    else -> {
                        // still over-scrolling
                        overScrollY += dy
                        showOverScrollEffect()
                    }
                }
                return result
            }
            lastVisiblePos > currentItem ->
                // Hero image of the next item is showing
                return (rv.findViewHolderForAdapterPosition(lastVisiblePos) as? SteppedListAdapter.ViewHolder)?.let {
                    // We don't want to allow scrolling beyond the hero image of the next ViewHolder.
                    // Need to determine whether we can scroll further

                    // todo: this is a temporary solution. Would be the best if we can figure out what's going on with the rv height.
                    val scrollParentHeight = if (lastVisiblePos == itemCount - 1) {
                        rv.height
                    } else {
                        overScrollHelper?.getScrollParentHeight() ?: rv.height
                    }
                    val scrollThreshold = scrollParentHeight - it.getPreviewHeight()
                    handleScrollDown(it.itemView.top - scrollThreshold, dy, recycler, state)
                } ?: normalScroll(dy, recycler, state)
            currentItem == itemCount.minus(1) ->
                // This is a special case because there won't be 'the next hero image'.
                return rv.findViewHolderForAdapterPosition(currentItem)?.itemView?.let {
                    handleScrollDown(it.bottom - rv.bottom, dy, recycler, state)
                } ?: normalScroll(dy, recycler, state)
            else -> {
                // state here could be normal or reached_top, either way, we're resetting it to normal
                return normalScroll(dy, recycler, state)
            }
        }
    }

    /**
     * Handle the scroll down attempt based on different scenarios.
     */
    private fun handleScrollDown(allowedDistance: Int, dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        return when {
            scrollState == ScrollState.NORMAL && allowedDistance < dy -> {
                // This can be a fling attempt, or simply the first time that we reach the
                // bottom. Either way, the attempted scroll will go beyond the hero image.
                // Therefore, need to adjust how much scrolling is allowed

                // Change the scroll flags so if further scroll down gesture comes in, we
                // can handle it as an overscroll
                scrollState = ScrollState.REACHED_BOTTOM
//                overScrollY = 0

                // Allow just enough to show the entire hero image.
                super.scrollVerticallyBy(allowedDistance, recycler, state)
            }
            scrollState == ScrollState.REACHED_BOTTOM || scrollState == ScrollState.OVERSCROLL_BOTTOM -> {
                // over scrolling
                scrollState = ScrollState.OVERSCROLL_BOTTOM
                val deltaY = if (overScrollY + dy > OVERSCROLL_MAX) { OVERSCROLL_MAX + 1 - overScrollY } else dy
                overScrollY += deltaY

                // We're going to let the helper show over-scroll effect, and scroll no more.
                // Thus return 0
                showOverScrollEffect()
                0
            }
            else -> {
                // Scroll is still permitted, haven't reached bottom of next hero image yet
                normalScroll(dy, recycler, state)
            }
        }
    }

    private fun normalScroll(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
//        overScrollY = 0
        scrollState = ScrollState.NORMAL
        return super.scrollVerticallyBy(dy, recycler, state)
    }

    private fun scrollUpBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        val firstVisiblePos = findFirstVisibleItemPosition()
        // The reason that we use 'less or equals to' here is, it is possible that user initiates an
        // up-scroll when the down-scroll transition animation hasn't finished yet.
        // In that case, first visible item position will be smaller than current.
        if (firstVisiblePos <= currentItem) {
            // need to determine whether we can scroll further
            return rv.findViewHolderForAdapterPosition(currentItem)?.itemView?.let {
                if (scrollState == ScrollState.NORMAL && it.top - dy > 0) {
                    // This can be a fling attempt, or simply the first time that we reach the
                    // top. Either way, the attempted scroll will go beyond the hero image.
                    // Therefore, need to adjust how much scrolling is allowed

                    // Change the scroll flags so if further scroll down gesture comes in, we
                    // can handle it as an overscroll
                    scrollState = ScrollState.REACHED_TOP
//                    overScrollY = 0
                    super.scrollVerticallyBy(it.top, recycler, state)
                } else if (scrollState == ScrollState.REACHED_TOP || scrollState == ScrollState.OVERSCROLL_TOP) {
                    // over scrolling
                    scrollState = ScrollState.OVERSCROLL_TOP
                    val deltaY = if (overScrollY + dy < -OVERSCROLL_MAX) { -OVERSCROLL_MAX - 1 - overScrollY } else dy
                    overScrollY += deltaY

                    // We're going to let the helper show over-scroll effect, and scroll no more.
                    // Thus return 0
                    showOverScrollEffect()
                    0
                } else if (scrollState == ScrollState.OVERSCROLL_BOTTOM) {
                    // User is scrolling up while over-scrolling at the bottom
                    var result = 0
                    when {
                        lastScrollRequestY > 0 -> {
                            // Do nothing. This is caused by animating the RecyclerView to create the
                            // overscrolling effect.
                        }
                        overScrollY == 0 -> {
                            // We're probably bouncing back.
                            // This sometimes could happen. Could be caused by the scrolling velocity.
                            // do nothing...
                        }
                        overScrollY + dy < 0 -> {
                            // cancel the over-scroll effect
                            overScrollHelper?.bounceBack()
                            // then scroll the rest
                            result = super.scrollVerticallyBy(dy + overScrollY, recycler, state)
                            scrollState = ScrollState.NORMAL
                            // reset over-scroll value as no over scroll is happening right now
                            overScrollY = 0
                        }
                        overScrollY + dy == 0 -> {
                            scrollState = ScrollState.REACHED_BOTTOM
                            overScrollY = 0
                            overScrollHelper?.bounceBack()
                        }
                        else -> {
                            // still over-scrolling
                            overScrollY += dy
                            showOverScrollEffect()
                        }
                    }
                    result
                } else {
                    // Scroll is still permitted, haven't reached top yet
                    normalScroll(dy, recycler, state)
                }
            } ?: normalScroll(dy, recycler, state)
        } else {
            return normalScroll(dy, recycler, state)
        }
    }
    // endregion

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        // In RecyclerView's onTouchEvent(), if velocity is not 0 but rv is not fling, it will try
        // to stop animation by setting scrollState to SCROLL_STATE_IDLE and call stopScrollersInternal().
        // This method calls stop() on ViewFlinger, which will abort the smooth scroll event.
        // Therefore, if we don't claim to consume the touch event when scrolling to next item,
        // the animation can be cancelled sometimes (if state is not fling for some reason).
        var consumed = false

        when (event?.actionMasked) {
            MotionEvent.ACTION_UP -> {
                // check do we need to bounce back
                val shouldGoNext = when (scrollState) {
                    ScrollState.OVERSCROLL_BOTTOM -> overScrollY > OVERSCROLL_BOUNCE_THRESHOLD
                    ScrollState.OVERSCROLL_TOP -> overScrollY < -OVERSCROLL_BOUNCE_THRESHOLD
                    else -> null
                }

                if (shouldGoNext == true) {
                    // let's move to the next item
                    if (scrollState == ScrollState.OVERSCROLL_BOTTOM) {
                        val nextVH = rv.findViewHolderForAdapterPosition(currentItem+1) as? SteppedListAdapter.ViewHolder
                        if (nextVH?.allowScrollTo() == true) {
                            // Fire adobe analytics info
                            overScrollHelper?.onStepChanged()

                            currentItem++
                            scrollState = ScrollState.ANIMATE_NEXT
                            rv.findViewHolderForAdapterPosition(currentItem)?.itemView?.let {
                                animateTotalDistance = it.top
                                animateScrolledDistance = 0
                                rv.smoothScrollBy(0, it.top)
                                consumed = true
                            }
                        } else {
                            scrollState = ScrollState.ANIMATE_BOUNCE_BOTTOM
                            // If current item is the last item, or the next item does not allow
                            // to be scrolled to top, we have reached the bottom of the list.
                            if (nextVH?.allowScrollTo() == false
                                    || currentItem == itemCount.minus(1)) {
                                overScrollHelper?.onBottomOverScrollComplete(overScrollY)
                            }
                        }
                    } else {
                        if (currentItem == 0) {
                            scrollState = ScrollState.ANIMATE_BOUNCE_TOP
                            overScrollHelper?.onTopOverScrollComplete(overScrollY)
                        } else {
                            // Fire adobe analytics info
                            overScrollHelper?.onStepChanged()

                            scrollState = ScrollState.ANIMATE_PREVIOUS
                            // Scroll back is a bit tricky. We need to scroll the hero image
                            // of current item to the bottom
                            (rv.findViewHolderForAdapterPosition(currentItem) as? SteppedListAdapter.ViewHolder)?.let {
                                val startPos = it.itemView.top
                                val endPos = (overScrollHelper?.getScrollParentHeight() ?: rv.height) - it.getPreviewHeight()
                                val deltaY = startPos - endPos
                                animateTotalDistance = deltaY
                                animateScrolledDistance = 0
                                rv.smoothScrollBy(0, deltaY)
                                consumed = true
                            }
                            currentItem--
                        }
                    }
                    overScrollHelper?.bounceBack()
                } else if (shouldGoNext == false) {
                    overScrollHelper?.bounceBack()
                    scrollState = when (scrollState) {
                        ScrollState.OVERSCROLL_TOP -> ScrollState.ANIMATE_BOUNCE_TOP
                        else -> ScrollState.ANIMATE_BOUNCE_BOTTOM
                    }
                } else {
                    if (currentItem == 0) {
                        overScrollHelper?.bounceBack()
                    }
                }

                overScrollY = 0
                userInteracting = false
            }
            MotionEvent.ACTION_DOWN -> {
                performActionDown()
            }
            MotionEvent.ACTION_MOVE -> {
                if (!userInteracting) {
                    performActionDown()
                }
            }
            else -> {}
        }

        return consumed
    }

    private fun performActionDown() {
        if (scrollState == ScrollState.ANIMATE_NEXT || scrollState == ScrollState.ANIMATE_BOUNCE_TOP) {
            // Calling scrollBy will trigger scrollVerticallyBy() immediately, where the
            // animation will be completed.
            // Without calling this, the title section positioning will be messed up if
            // user stops a scrolling animation.
            rv.scrollBy(0, animateTotalDistance - animateScrolledDistance)

            scrollState = ScrollState.NORMAL
            // Things are a bit more complicated here.
            // Because the current scroll location might have been restored from saved instance,
            // instead of scrolled by user. In that case, our scroll state is not quite accurate.
            // That's why we're double checking the scroll state here.
            val firstVisiblePos = findFirstVisibleItemPosition()
            val firstVisibleVH = rv.findViewHolderForAdapterPosition(firstVisiblePos)
            if (firstVisibleVH != null) {
                if (firstVisibleVH.itemView.top == 0) {
                    scrollState = ScrollState.REACHED_TOP
                } else {
                    val lastVisiblePos = findLastVisibleItemPosition()
                    if (firstVisiblePos != lastVisiblePos) {
                        val lastVisibleVH = rv.findViewHolderForAdapterPosition(lastVisiblePos) as? SteppedItemViewHolder
                        if (lastVisibleVH != null
                                && (overScrollHelper?.getScrollParentHeight() ?: rv.height) - lastVisibleVH.getPreviewHeight() == lastVisibleVH.itemView.top) {
                            scrollState = ScrollState.REACHED_BOTTOM
                        }
                    }
                }
            }
        } else if (scrollState == ScrollState.ANIMATE_PREVIOUS || scrollState == ScrollState.ANIMATE_BOUNCE_BOTTOM) {
            rv.scrollBy(0, animateTotalDistance - animateScrolledDistance)
            scrollState = ScrollState.REACHED_BOTTOM
        }
        userInteracting = true
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
        val smoothScroller = SnappedSmoothScroller(rv.context)
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    private fun showOverScrollEffect() {
        overScrollHelper?.showOverScrollEffect(overScrollY)

        if (overScrollY > 0) {
            // bottom
            val nextVH = rv.findViewHolderForAdapterPosition(currentItem+1) as? SteppedListAdapter.ViewHolder
            if (nextVH?.allowScrollTo() == false
                    || currentItem == itemCount.minus(1)) {
                // list bottom
                overScrollHelper?.onBottomOverScrolling(overScrollY)
            }
        } else if (currentItem == 0) {
            // list top
            overScrollHelper?.onTopOverScrolling(overScrollY)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = Bundle()
        state.putParcelable(STATE_KEY_SUPER, super.onSaveInstanceState())
        state.putInt(STATE_KEY_POS, currentItem)
        state.putInt(STATE_KEY_SCROLL_STATE, scrollState.ordinal)
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            currentItem = state.getInt(STATE_KEY_POS)
            scrollState = ScrollState.values()[state.getInt(STATE_KEY_SCROLL_STATE, ScrollState.REACHED_TOP.ordinal)]
            // We can't scroll the recycler view yet. I mean... code will run but ui won't be updated.
            super.onRestoreInstanceState(state.getParcelable(STATE_KEY_SUPER))
        }
    }
}

private const val STATE_KEY_SUPER = "super.state"
private const val STATE_KEY_POS = "current_position"
private const val STATE_KEY_SCROLL_STATE = "scroll_state"

private class SnappedSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun getVerticalSnapPreference(): Int {
        return LinearSmoothScroller.SNAP_TO_START
    }
}

private enum class ScrollState {
    NORMAL,
    REACHED_BOTTOM,
    REACHED_TOP,
    OVERSCROLL_BOTTOM,
    OVERSCROLL_TOP,
    ANIMATE_NEXT,
    ANIMATE_PREVIOUS,
    ANIMATE_BOUNCE_TOP,
    ANIMATE_BOUNCE_BOTTOM
}

interface OverScrollHelper {
    fun showOverScrollEffect(dy: Int)
    fun bounceBack()
    fun onTopOverScrollComplete(dy: Int)
    fun onBottomOverScrollComplete(dy: Int)
    fun onTopOverScrolling(dy: Int)
    fun onBottomOverScrolling(dy: Int)
    /**
     * Return the height of the scrollable area. We need this value to determine the position of
     * preview and expanding view holder to fill screen when too short.
     */
    fun getScrollParentHeight(): Int
    /**
     * This method keep tracking on step changes
     * For each step changed, fire adobe analytics info
     */
    fun onStepChanged()
}