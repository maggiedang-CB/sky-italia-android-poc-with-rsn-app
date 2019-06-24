package com.nbcsports.regional.nbc_rsn.stepped_story.components

import kotlin.math.roundToInt

class CoverLayoutManager(val baseView: SteppedCoverLayout) {
    val touchRecorder = TouchRecorder()

    var viewAtEdge: Boolean = false
    var layoutExpanded = false
    var isOpened = false

    fun onUserDown(downY: Int) {
        // do nothing
    }

    fun onUserUp(upY: Int) {
        if (!touchRecorder.isRecordingTouch()) {
            return
        }

        val translationY = touchRecorder.startingY - upY

        if (isCompletePull(translationY)) {
            onCompletePull()
        } else {
            onResetPull()
        }
        touchRecorder.stopRecordingTouch()
    }

    fun onUserMove(moveY: Int) {
        notifyPullListener()

        if (viewAtEdge) {
            touchRecorder.startRecordingTouch(moveY)
        }

//        if (!layoutExpanded && touchRecorder.isRecordingTouch() && viewAtEdge) {
        if (touchRecorder.isRecordingTouch() && viewAtEdge) {
            val prevY = touchRecorder.previousTouchY

            if (isPulling(prevY, moveY)) {

            }

            val distanceTravelledFromDownY = moveY - touchRecorder.startingY

            if (isInsidePullBounds(distanceTravelledFromDownY)) {
                onValidPullMovement(distanceTravelledFromDownY, moveY)
            } else if (isOutsidePullBounds(distanceTravelledFromDownY)) {
                onInvalidPullMovement()
            }
            touchRecorder.record(moveY)
        }
    }

    fun isPulling(prevY: Int, moveY: Int): Boolean {
        return prevY > moveY
    }

    fun onValidPullMovement(distanceTravelled: Int, moveY: Int) {
        baseView.disableScrolling()
        baseView.expandAnimation(distanceTravelled, 0)
    }

    fun onInvalidPullMovement() {
        if ((baseView.translationY.roundToInt()) != 0) {
            baseView.expandAnimation(0, 0)
        }

        baseView.enableScrolling()
    }

    fun onCompletePull() {
        layoutExpanded = true
        baseView.completeAnimation()
        baseView.onPullListener?.onPullComplete()
    }

    fun onResetPull() {
        baseView.postDelayed({
            baseView.onPullListener.let {
                it?.onClose()
                isOpened = false
            }
        }, baseView.collapseDuration.toLong())
        baseView.collapseAnimation()
        layoutExpanded = false
        baseView.enableScrolling()
    }

    fun notifyPullListener() {
        baseView.onPullListener.let {

            val translationY = baseView.translationY.roundToInt()

            //translationY > 0 for refresh
            if (isExpanding(translationY)) {
                baseView.onPullListener?.onOpen()
                isOpened = true

            } else if (translationY == 0 && isOpened) {
                baseView.onPullListener?.onClose()
                isOpened = false
            }
        }
    }

    fun isCompletePull(translationY: Int): Boolean {
//        return -1 * baseView.maxPullHeight <= translationY
//                && translationY <= -1 * baseView.pullCompletionThreshold
        return baseView.maxPullHeight <= translationY
    }

    fun isIncompletePull(translationY: Int): Boolean {
        return translationY > 0 && !layoutExpanded
    }

    fun isInsidePullBounds(distanceTravelled: Int): Boolean {
//        return 0 < distanceTravelled && distanceTravelled <= baseView.maxPullHeight
        return true
    }

    fun isOutsidePullBounds(distanceTravelled: Int): Boolean {
        return distanceTravelled < 0
    }

    fun isExpanding(translationY: Int): Boolean {
        return translationY > 0 && !isOpened
    }

    class TouchRecorder {

        var startingY: Int = -1
        var previousTouchY: Int = -1

        fun startRecordingTouch(moveY: Int) {
            if (startingY == -1) {
                startingY = moveY
                previousTouchY = startingY
            }
        }

        fun isRecordingTouch(): Boolean {
            return startingY != -1
        }

        fun stopRecordingTouch() {
            startingY = -1
            previousTouchY = -1
        }

        fun record(moveY: Int) {
            previousTouchY = moveY
        }
    }
}