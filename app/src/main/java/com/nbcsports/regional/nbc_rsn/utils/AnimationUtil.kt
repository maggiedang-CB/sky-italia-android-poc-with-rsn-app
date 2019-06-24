package com.nbcsports.regional.nbc_rsn.utils

import android.view.View

object AnimationUtil {
    fun fadeOut(animationDuration: Int, backgroundViewToFadeTo: View) {

        // Set the view to 0% (or 50%) opacity but visible, so that it is visible
        // (but transparent) during the animation.
        backgroundViewToFadeTo.alpha = 0.1f//(0.5f);//(0f);
        backgroundViewToFadeTo.visibility = View.VISIBLE

        // Animate the view to 100% opacity, and clear any animation listener set on the view.
        backgroundViewToFadeTo.animate()
                .alpha(0.9f)//(1f)
                .setDuration(animationDuration.toLong())
                .setListener(null)
                .start() //?

        // Recover normal appearance
        recoverFromFadingOut(animationDuration, backgroundViewToFadeTo)
    }

    fun fadeIn(animationDuration: Int, backgroundViewToFadeTo: View) {

        backgroundViewToFadeTo.alpha = 0.9f
        backgroundViewToFadeTo.visibility = View.VISIBLE

        // Animate the view to 100% opacity, and clear any animation listener set on the view.
        backgroundViewToFadeTo.animate()
                .alpha(0.1f)//(1f)
                .setDuration(animationDuration.toLong())
                .setListener(null)
                .start() //?

        // Recover normal appearance
        recoverFromFadingOut(animationDuration, backgroundViewToFadeTo)
    }

    private fun recoverFromFadingOut(animationDuration: Int, backgroundViewToFadeTo: View) {
        backgroundViewToFadeTo.postDelayed({
            backgroundViewToFadeTo.alpha = 0f//(0.5f);//(0f);
            backgroundViewToFadeTo.visibility = View.GONE
        }, animationDuration.toLong())
    }
}