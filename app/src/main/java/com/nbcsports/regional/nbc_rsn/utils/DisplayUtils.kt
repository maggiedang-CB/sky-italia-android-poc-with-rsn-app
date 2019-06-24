package com.nbcsports.regional.nbc_rsn.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.core.graphics.ColorUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.nbcsports.regional.nbc_rsn.RsnApplication
import com.squareup.picasso.Picasso
import io.reactivex.Single
import kotlin.math.roundToInt


class DisplayUtils {

    companion object {
        //35 % From the Zeplin file
        val GRADIENT_START = "#59FFFFFF"
        val GRADIENT_END = "#00FFFFFF"
        @JvmStatic
        fun getScreenWidth(context: Context): Int {
            val displayMetrics = DisplayMetrics()
            val manager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            manager.defaultDisplay.getMetrics(displayMetrics)

            return displayMetrics.widthPixels
        }

        @JvmStatic
        fun getScreenHeight(context: Context): Int {
            val displayMetrics = DisplayMetrics()
            val manager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            manager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.heightPixels
        }

        @JvmStatic
        fun animateColorChange(view: View, colorFrom: Int, colorTo: Int) {
            requireNotNull(view)
            if (colorFrom == colorTo) return
            //Change the object from int to drawable
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
            colorAnimation.addUpdateListener { animator ->
                view.setBackgroundColor(animator.animatedValue as Int)
            }
            colorAnimation
                    .setDuration(100)  // use a default animation duration
                    .start()
        }

        @JvmStatic
        fun animateStatusBarColorChange(context: Context, colorFrom: Int, colorTo: Int) {
            requireNotNull(context)
            if (colorFrom == colorTo) return

            val colorAnimation = ValueAnimator.ofFloat(0f, 1f)
            colorAnimation.addUpdateListener {
                val position: Float = it.animatedFraction
                val blended: Int = blendColors(colorFrom, colorTo, position)
                ActivityUtils.setStatusBarColor((context as Activity), blended)
            }
            colorAnimation
                    .setDuration(100)  // use a default duration
                    .start()
    }
        private fun blendColors(from: Int, to: Int, ratio: Float): Int {
            val inverseRatio: Float = 1f - ratio

            val r: Float = Color.red(to) * ratio + Color.red(from) * inverseRatio
            val g: Float = Color.green(to) * ratio + Color.green(from) * inverseRatio
            val b: Float = Color.blue(to) * ratio + Color.blue(from) * inverseRatio

            return Color.rgb(r.toInt(), g.toInt(), b.toInt())
        }

        @JvmStatic
        fun getStatusBarHeight(): Int {
            var result = 0
            val resourceId = RsnApplication.getInstance().getResources().getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = RsnApplication.getInstance().getResources().getDimensionPixelSize(resourceId)
            }
            return result
        }

        @JvmStatic
        fun isViewOverlapping(firstView: View, secondView: View): Boolean {
            val firstPosition = IntArray(2)
            val secondPosition = IntArray(2)

            firstView.getLocationOnScreen(firstPosition)
            secondView.getLocationOnScreen(secondPosition)

            // Rect constructor parameters: left, top, right, bottom
            val rectFirstView = Rect(firstPosition[0], firstPosition[1],
                    firstPosition[0] + firstView.measuredWidth, firstPosition[1] + firstView.measuredHeight)
            val rectSecondView = Rect(secondPosition[0], secondPosition[1],
                    secondPosition[0] + secondView.measuredWidth, secondPosition[1] + secondView.measuredHeight)
            return rectFirstView.intersect(rectSecondView)
        }

        @JvmStatic
        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        @JvmStatic
        fun pxToDp(px: Int): Int {
            return (px / Resources.getSystem().displayMetrics.density).toInt()
        }

        @JvmStatic
        fun getGradientStartColor(rgb: Int, gradientValue: Double): Int {
            return Color.argb(gradientValue.roundToInt(), rgb.red, rgb.green, rgb.blue)
        }

        @JvmStatic
        fun getColorWithAlpha(yourColor: Int, alpha: Int): Int {
            val red = Color.red(yourColor)
            val blue = Color.blue(yourColor)
            val green = Color.green(yourColor)
            return ColorUtils.compositeColors(Color.argb(alpha, red, green, blue), Color.WHITE)
        }

        @JvmStatic
        fun getBitmapSingle(picasso: Picasso, url: String): Single<Bitmap> = Single.create {
            try {
                if (!it.isDisposed) {
                    val bitmap: Bitmap = picasso.load(url).get()
                    it.onSuccess(bitmap)
                }
            } catch (e: Throwable) {
                it.onError(e)
            }
        }

        @JvmStatic
        fun isVisible(view: View?): Boolean {
            if (view == null) {
                return false
            }
            if (!view.isShown) {
                return false
            }

            val displayMetrics = DisplayMetrics()
            (view.context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val width = displayMetrics.widthPixels

            val actualPosition = Rect()
            view.getGlobalVisibleRect(actualPosition)
            val screen = Rect(0, 0, width, height)
            return actualPosition.intersect(screen)
        }

        @JvmStatic
        fun applyWhiteGradient (view: View) {
            val filter = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor(GRADIENT_START), Color.parseColor(GRADIENT_END)))
            view.background = filter
        }

        @JvmStatic
        fun applyColorGradient (view: View, color: Int) {
            val filter = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor(GRADIENT_START), Color.parseColor(GRADIENT_END)))
            val layers = arrayOf(ColorDrawable(color), filter)
            val background = LayerDrawable(layers)
            view.background = background
        }
    }
}
