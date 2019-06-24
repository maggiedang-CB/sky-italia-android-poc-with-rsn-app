package com.nbcsports.regional.nbc_rsn.fabigation

import android.content.Context
import android.content.res.Resources
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.internal.operators.completable.CompletableTimer
import java.util.concurrent.TimeUnit

/**
 * Created by justin on 2018-03-23.
 */

class FabMenu @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface FabPositionInterface {
        fun scrollToPercent(x: Float)
        fun center()
    }

    enum class MenuOrientation {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    private companion object SpringParams {
        const val STIFFNESS: Float = SpringForce.STIFFNESS_LOW
        const val DAMPING_RATIO: Float = SpringForce.DAMPING_RATIO_NO_BOUNCY
        const val SPRING_FINAL_POSITION: Float = 0f
        const val VIBRATION_DURATION: Long = 30L
        val SCREEN_WIDTH: Int by lazy {
            Resources.getSystem().displayMetrics.widthPixels
        }
        const val FLICK_DELAY_DURATION: Long = 250
    }

    // interfaces
    var fabPositionInterface: FabPositionInterface? = null
    private var menuInterface: FabMenuInterface? = null

    private var menuOrientation: MenuOrientation = MenuOrientation.LEFT_TO_RIGHT
        set(value) {
            maxFabStartPosition = when (value) {
                MenuOrientation.LEFT_TO_RIGHT -> {
                    SCREEN_WIDTH * 0.3
                }
                MenuOrientation.RIGHT_TO_LEFT -> {
                    SCREEN_WIDTH * 0.7
                }
            }

            field = value
        }

    // variables
    private var initialX: Float = 0f
    private var startX: Float = 0f
    private var isMenuOpened: Boolean = false
    var isMenuAnimating: Boolean = false
    private lateinit var animation: SpringAnimation

    private var fabBeingTouched : Boolean = false
    // flick anim
    private var touchDisabled: Boolean = false
    private val flickMoveAmt: Float = SCREEN_WIDTH * 0.2f
    private var animDisposable: Disposable? = null

    // init to LTR orientation
    private var maxFabStartPosition: Double = SCREEN_WIDTH * 0.3
    private val maxFabEndPosition: Double = 0.8


    fun setMenuOpened(isOpened: Boolean) {
        isMenuOpened = isOpened
        isMenuAnimating = false
    }

    fun isFabBeingTouched() : Boolean {
        return fabBeingTouched
    }

    init {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                animation = createSpringAnim(view = this@FabMenu, property = DynamicAnimation.TRANSLATION_X)
                viewTreeObserver.removeOnGlobalLayoutListener(this::onGlobalLayout)
            }
        })

        setOnLongClickListener(OnLongClickListener {
            enterMenuView()
            ActivityUtils.vibrate(context, VIBRATION_DURATION)
            return@OnLongClickListener false
        })

        setUpOnTouchListener(true)
    }

    fun initFabMenu(menuInterface: FabMenuInterface, menuOrientation: MenuOrientation) {
        this.menuInterface = menuInterface
        this.menuOrientation = menuOrientation
    }

    fun createSpringAnim(view: View, property: DynamicAnimation.ViewProperty): SpringAnimation {
        return SpringAnimation(view, property)
                .setSpring(
                        SpringForce(SPRING_FINAL_POSITION).apply {
                            stiffness = STIFFNESS
                            dampingRatio = DAMPING_RATIO
                        })
    }

    fun setUpOnTouchListener(enableCustomOnTouch: Boolean) {
        setOnTouchListener(OnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    fabBeingTouched = true
                    startX = v.x - event.rawX
                    initialX = v.x
                    animation.cancel()
                    cancelFtueAnimation()

                    if (!enableCustomOnTouch){
                        // If it is first launch and user touch the fab
                        // Then show the slide indicator
                        if (FtueUtil.isAppFirstLaunch()) {
                            menuInterface?.setSlideIndicatorVisibleWithAnimation(true)
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (enableCustomOnTouch){
                        val newX: Float = event.rawX + startX
                        this.animate()
                                .x(newX)
                                .setDuration(0)
                                .start()

                        if (SCREEN_WIDTH > 0) {
                            val startPos = newX - v.width
                            val maxPos = maxFabEndPosition * SCREEN_WIDTH - v.width
                            val percent = (startPos - startX) / (maxPos - startX)

                            if (isMenuOpened && !isMenuAnimating) fabPositionInterface?.scrollToPercent(percent.toFloat())

                            // If it is first launch and fab move right more than 10% of max width of screen
                            // Then hide the slide indicator and set isAppFirstLaunch to false
                            if (FtueUtil.isAppFirstLaunch() && percent > 0.1) {
                                menuInterface?.setSlideIndicatorVisibleWithAnimation(false)
                                FtueUtil.setIsAppFirstLaunch(false)
                            }
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    fabBeingTouched = false

                    // If it is first launch and user leave the fab
                    // Then hide the slide indicator
                    if (FtueUtil.isAppFirstLaunch()) {
                        menuInterface?.setSlideIndicatorVisibleWithAnimation(false)
                    }

                    if (enableCustomOnTouch){
                        animation.start()
                        if (isMenuOpened || isMenuAnimating) {
                            val diff = (v.x - initialX) / SCREEN_WIDTH
                            val isAppFirstLaunch: Boolean = FtueUtil.isAppFirstLaunch()
                            when {
                                diff < 0.01 && isAppFirstLaunch -> exitMenuView(true)
                                else -> exitMenuView(false)
                            }
                        } else {
                            val diff = (v.x - initialX) / SCREEN_WIDTH

                            when {
                                -0.01 < diff && diff < 0.01 -> menuInterface?.tapFab(true)
                                diff < -0.05 -> menuInterface?.flingLeft()
                                diff > 0.05 -> menuInterface?.flingRight()
                            }
                        }
                    }
                }
            }
            return@OnTouchListener false
        })
    }

    private fun exitMenuView(goBackToFabOutro: Boolean) {
        if (goBackToFabOutro){
            menuInterface?.exitMenu(FabMenuAdapter.FabCardType.FabOutro)
        } else {
            // If it is first launch and user leave the fab (on menu shown)
            // Then set isAppFirstLaunch to false
            if (FtueUtil.isAppFirstLaunch()) {
                FtueUtil.setIsAppFirstLaunch(false)
            }
            fabPositionInterface?.center()
        }
        isMenuOpened = false
        isMenuAnimating = false
    }

    private fun enterMenuView() {
        menuInterface?.enterMenu()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return touchDisabled || super.dispatchTouchEvent(ev)
    }

    fun showFtueFabAnim(isRTL: Boolean) {
        if (animDisposable == null) {
            animDisposable = setTouchDisabled(true) // disable user touch during animation
                    .andThen(flickAnim(isRTL))
                    .andThen(returnAnim())
                    .andThen(delayAnim())
                    .andThen(flickAnim(isRTL))
                    .andThen(returnAnim())
                    .andThen(setTouchDisabled(false)) // enable user touch once anim has completed
                    .andThen(clearAnim())
                    .subscribe()
        }
    }

    private fun cancelFtueAnimation() {
        animDisposable?.dispose()
        animDisposable = null
        touchDisabled = false
        animate().setDuration(0).translationX(SPRING_FINAL_POSITION).start()
    }

    private fun flickAnim(isRTL: Boolean) : Completable {
        return Completable.create { emitter ->

            if (fabBeingTouched) {
                cancelFtueAnimation()
                emitter.onComplete()

            } else {
                val anim = SpringAnimation(this, DynamicAnimation.TRANSLATION_X)
                        .setSpring(SpringForce(if (isRTL) -flickMoveAmt else flickMoveAmt)
                                .apply {
                                    stiffness = SpringForce.STIFFNESS_MEDIUM
                                    dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                                })
                anim.addEndListener { _, _, _, _ -> emitter.onComplete() }
                anim.start()
            }
        }
    }

    private fun returnAnim() : Completable {
        return Completable.create { emitter ->
            if (fabBeingTouched){
                cancelFtueAnimation()
                emitter.onComplete()

            } else {
                val anim = SpringAnimation(this, DynamicAnimation.TRANSLATION_X)
                        .setSpring(SpringForce(SPRING_FINAL_POSITION).apply {
                            stiffness = SpringForce.STIFFNESS_LOW
                            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                        })
                anim.addEndListener { _, _, _, _ -> emitter.onComplete() }
                anim.start()
            }
        }
    }

    private fun delayAnim() : Completable {
        return CompletableTimer(FLICK_DELAY_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
    }

    private fun clearAnim() : Completable {
        return Completable.create { emitter ->
            cancelFtueAnimation()
            emitter.onComplete()
        }
    }

    fun setTouchDisabled(disableTouch: Boolean): Completable {
        return Completable.create { emitter ->
            touchDisabled = disableTouch
            emitter.onComplete()
        }
    }
}