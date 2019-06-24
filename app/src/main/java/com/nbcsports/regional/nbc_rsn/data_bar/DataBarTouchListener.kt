package com.nbcsports.regional.nbc_rsn.data_bar

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.pm.ActivityInfo.*
import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.nbcsports.regional.nbc_rsn.MainActivity
import com.nbcsports.regional.nbc_rsn.MainContract
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Config
import com.nbcsports.regional.nbc_rsn.common.Constants.CONFIG_KEY
import com.nbcsports.regional.nbc_rsn.common.Constants.TEAM_KEY
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuContract
import com.nbcsports.regional.nbc_rsn.data_menu.DataMenuFragment
import com.nbcsports.regional.nbc_rsn.extensions.d
import com.nbcsports.regional.nbc_rsn.persistentplayer.PersistentPlayerContract
import com.nbcsports.regional.nbc_rsn.team_view.TeamContainerFragment
import com.nbcsports.regional.nbc_rsn.utils.ActivityUtils
import com.nbcsports.regional.nbc_rsn.utils.DataMenuUtils
import com.nbcsports.regional.nbc_rsn.utils.FtueUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class DataBarTouchListener(private val teamContainerFragment: TeamContainerFragment, private val childFragmentManager: FragmentManager, vararg views: View?): View.OnTouchListener {

    private var teamContainerRootConstraintLayout: View? = null
    private var teamViewContent: View? = null
    private var dataMenuContent: View? = null
    private var teamViewAndDataMenuMiddleLayerRelativeLayout: View? = null
    private var dataBarAndDataMenuGapCoverLayerRelativeLayout: View? = null
    private var teamViewMvpdBar: View? = null

    private var dataBarInitY: Float = -1.0f
    private var dataBarFinalY: Float = -1.0f
    private var userTouchInitY: Float = -1.0f
    private var dataBarTouchDownY: Float = -1.0f
    private var teamContainerHeight: Int = 0
    private var isDataBarMoved: Boolean = false

    init {
        for (view in views){
            view?.let {
                when (it.id) {
                    R.id.team_container_root_constraint_layout -> teamContainerRootConstraintLayout = it
                    R.id.team_view_content -> teamViewContent = it
                    R.id.data_menu_content -> dataMenuContent = it
                    R.id.team_view_and_data_menu_middle_layer_relative_layout -> teamViewAndDataMenuMiddleLayerRelativeLayout = it
                    R.id.databar_and_data_menu_gap_cover_layer_relative_layout -> dataBarAndDataMenuGapCoverLayerRelativeLayout = it
                    R.id.team_view_mvpd_bar -> teamViewMvpdBar = it
                }
            }
        }
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if ((view?.context as MainActivity)?.fab.isFabBeingTouched()) return true

        when(event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                view?.let {
                    d(String.format("This is the enter point: view.height: %s", it.height))
                    dataBarInitY = teamViewContent?.y ?: 0.0f
                    if (dataBarInitY > 0.0f){
                        dataBarInitY -= 1.0f * it.height
                    }
                    teamContainerHeight = teamContainerRootConstraintLayout?.height ?: 0
                    d(String.format("This is the enter point: onTouch teamContainerHeight: %s", teamContainerHeight))
                    dataBarFinalY = 1.0f * (teamContainerHeight - it.height)
                    userTouchInitY = event.rawY
                    dataBarTouchDownY = it.y
                    isDataBarMoved = false
                    d(String.format("This is the enter point: onTouch dataBarInitY: %s", dataBarInitY))
                    d(String.format("This is the enter point: onTouch dataBarFinalY: %s", dataBarFinalY))
                    d(String.format("This is the enter point: onTouch userTouchInitY: %s", userTouchInitY))
                    d(String.format("This is the enter point: onTouch dataBarTouchDownY: %s", dataBarTouchDownY))
                    // This piece of code will remove the previous listeners of data bar's animator
                    // Remove all listeners before doing any initialization,
                    // because previous listeners may modify initialized values
                    removeAllListenersOfAnimator(it, dataBarTouchDownY)
                    setDataMenuIsOpened(true)
                    initOrientation()
                    removeTeamViewContentTopConstraintFromDataBar(it.height)
                    initDataMenuFragmentIfPossible(dataBarFinalY.roundToInt())
                    initAndShowTeamViewAndDataBarMiddleLayer(dataBarTouchDownY)
                    initDataBarAndDataMenuGapCoverLayer(dataBarFinalY.roundToInt())
                    initDataBarAndDataMenuAndGapCoverColor(dataBarTouchDownY)
                    initFabMenuButtonVisibility(dataBarTouchDownY)
                    initTeamViewMvpdBarVisibility(dataBarTouchDownY)
                    initSystemStatusBarColor(dataBarTouchDownY)
                    initMiniPersistentPlayerFadeInAndFadeOut(dataBarTouchDownY)
                    // Store in SharedPreferences that the user has opened the data menu
                    FtueUtil.setHasOpenedDataMenu(true)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                view?.let {
                    val rangeStart: Float = dataBarInitY + (userTouchInitY - dataBarTouchDownY)
                    val rangeEnd: Float = dataBarFinalY + (userTouchInitY - dataBarTouchDownY)
                    if (event.rawY in rangeStart..rangeEnd){
                        val newDataBarY = event.rawY - (userTouchInitY - dataBarTouchDownY)
                        d(String.format("This is the enter point: onTouch newDataBarY: %s", newDataBarY))
                        if (newDataBarY > (dataBarInitY + 10.0f) && newDataBarY < (dataBarFinalY - 10.0f)){
                            isDataBarMoved = true
                        }
                        it.animate().y(newDataBarY)
                                .setDuration(0L)
                                .start()
                        dataMenuContent?.animate()
                                ?.y(newDataBarY - dataBarFinalY.roundToInt())
                                ?.setDuration(0L)
                                ?.start()
                        dataBarAndDataMenuGapCoverLayerRelativeLayout?.animate()
                                ?.y(newDataBarY - dataBarFinalY.roundToInt())
                                ?.setDuration(0L)
                                ?.start()
                        handleTeamViewAndDataBarMiddleLayerWhileMoving(newDataBarY)
                        handleDataBarAndDataMenuAndGapCoverColor(newDataBarY)
                        handleFabMenuButtonFadeInAndFadeOut(newDataBarY)
                        handleFabMenuButtonTouchDisable(true)
                        handleTeamViewMvpdBarFadeInAndFadeOut(newDataBarY)
                        handleSystemStatusBarColor(newDataBarY)
                        handleMiniPersistentPlayerFadeInAndFadeOut(newDataBarY)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                view?.let {
                    val dataBarMovingRange = dataBarFinalY + dataBarInitY
                    if (it.y < (dataBarMovingRange / 2.0f) && isDataBarMoved){
                        teamContainerFragment.onDataMenuClosed()
                        it.animate().y(dataBarInitY)
                                .setDuration(400L)
                                .setInterpolator(AccelerateDecelerateInterpolator())
                                .setUpdateListener(object: ValueAnimator.AnimatorUpdateListener {
                                    override fun onAnimationUpdate(valueAnimator: ValueAnimator?) {
                                        dataMenuContent?.animate()
                                                ?.y(it.y - dataBarFinalY.roundToInt())
                                                ?.setDuration(0L)
                                                ?.start()
                                        dataBarAndDataMenuGapCoverLayerRelativeLayout?.animate()
                                                ?.y(it.y - dataBarFinalY.roundToInt())
                                                ?.setDuration(0L)
                                                ?.start()
                                        handleTeamViewAndDataBarMiddleLayerWhileMoving(it.y)
                                        handleDataBarAndDataMenuAndGapCoverColor(it.y)
                                        handleFabMenuButtonFadeInAndFadeOut(it.y)
                                        handleTeamViewMvpdBarFadeInAndFadeOut(it.y)
                                        handleSystemStatusBarColor(it.y)
                                        handleMiniPersistentPlayerFadeInAndFadeOut(it.y)
                                    }
                                })
                                .setListener(object: Animator.AnimatorListener {
                                    override fun onAnimationStart(animator: Animator?) {}
                                    override fun onAnimationRepeat(animator: Animator?) {}
                                    override fun onAnimationEnd(animator: Animator?) {
                                        addTeamViewContentTopConstraintToDataBar()
                                        if (it.y == dataBarInitY){
                                            disableTeamViewAndDataBarMiddleLayer()
                                            handleFabMenuButtonTouchDisable(false)
                                            handleDataBarAndDataMenuAndGapCoverColor(it.y)
                                            handleFabMenuButtonFadeInAndFadeOut(it.y)
                                            handleTeamViewMvpdBarFadeInAndFadeOut(it.y)
                                            handleSystemStatusBarColor(it.y)
                                            handleMiniPersistentPlayerFadeInAndFadeOut(it.y)
                                            setDataMenuIsOpened(false)
                                            removeDataMenuFragmentIfPossible()
                                            resetOrientation()
                                        }
                                        removeAllListenersOfAnimator(it, it.y)
                                    }
                                    override fun onAnimationCancel(animator: Animator?) {}
                                })
                                .start()
                    } else if (it.y >= (dataBarMovingRange / 2.0f) && isDataBarMoved){
                        teamContainerFragment.onDataMenuOpened()
                        // animate the opening
                        it.animate().y(dataBarFinalY)
                                .setDuration(400L)
                                .setInterpolator(AccelerateDecelerateInterpolator())
                                .setUpdateListener(object: ValueAnimator.AnimatorUpdateListener {
                                    override fun onAnimationUpdate(valueAnimator: ValueAnimator?) {
                                        dataMenuContent?.animate()
                                                ?.y(it.y - dataBarFinalY.roundToInt())
                                                ?.setDuration(0L)
                                                ?.start()
                                        dataBarAndDataMenuGapCoverLayerRelativeLayout?.animate()
                                                ?.y(it.y - dataBarFinalY.roundToInt())
                                                ?.setDuration(0L)
                                                ?.start()
                                        handleTeamViewAndDataBarMiddleLayerWhileMoving(it.y)
                                        handleDataBarAndDataMenuAndGapCoverColor(it.y)
                                        handleFabMenuButtonFadeInAndFadeOut(it.y)
                                        handleTeamViewMvpdBarFadeInAndFadeOut(it.y)
                                        handleSystemStatusBarColor(it.y)
                                        handleMiniPersistentPlayerFadeInAndFadeOut(it.y)
                                    }
                                })
                                .setListener(object: Animator.AnimatorListener {
                                    override fun onAnimationStart(animator: Animator?) {}
                                    override fun onAnimationRepeat(animator: Animator?) {}
                                    override fun onAnimationEnd(animator: Animator?) {
                                        if (it.y == dataBarFinalY){
                                            handleDataBarAndDataMenuAndGapCoverColor(it.y)
                                            handleFabMenuButtonFadeInAndFadeOut(it.y)
                                            handleTeamViewMvpdBarFadeInAndFadeOut(it.y)
                                            handleSystemStatusBarColor(it.y)
                                            handleMiniPersistentPlayerFadeInAndFadeOut(it.y)
                                        }
                                        removeAllListenersOfAnimator(it, it.y)
                                    }
                                    override fun onAnimationCancel(animator: Animator?) {}
                                })
                                .start()
                    } else if ((it.y in dataBarInitY..(dataBarInitY + 10.0f)) && !isDataBarMoved){
                        teamContainerFragment.onDataMenuOpened()
                        it.animate().y(dataBarFinalY)
                                .setDuration(400L)
                                .setInterpolator(AccelerateDecelerateInterpolator())
                                .setUpdateListener(object: ValueAnimator.AnimatorUpdateListener {
                                    override fun onAnimationUpdate(valueAnimator: ValueAnimator?) {
                                        dataMenuContent?.animate()
                                                ?.y(it.y - dataBarFinalY.roundToInt())
                                                ?.setDuration(0L)
                                                ?.start()
                                        dataBarAndDataMenuGapCoverLayerRelativeLayout?.animate()
                                                ?.y(it.y - dataBarFinalY.roundToInt())
                                                ?.setDuration(0L)
                                                ?.start()
                                        handleTeamViewAndDataBarMiddleLayerWhileMoving(it.y)
                                        handleDataBarAndDataMenuAndGapCoverColor(it.y)
                                        handleFabMenuButtonFadeInAndFadeOut(it.y)
                                        handleTeamViewMvpdBarFadeInAndFadeOut(it.y)
                                        handleSystemStatusBarColor(it.y)
                                        handleMiniPersistentPlayerFadeInAndFadeOut(it.y)
                                    }
                                })
                                .setListener(object: Animator.AnimatorListener {
                                    override fun onAnimationStart(animator: Animator?) {}
                                    override fun onAnimationRepeat(animator: Animator?) {}
                                    override fun onAnimationEnd(animator: Animator?) {
                                        if (it.y == dataBarFinalY){
                                            handleDataBarAndDataMenuAndGapCoverColor(it.y)
                                            handleFabMenuButtonFadeInAndFadeOut(it.y)
                                            handleTeamViewMvpdBarFadeInAndFadeOut(it.y)
                                            handleSystemStatusBarColor(it.y)
                                            handleMiniPersistentPlayerFadeInAndFadeOut(it.y)
                                        }
                                        removeAllListenersOfAnimator(it, it.y)
                                    }
                                    override fun onAnimationCancel(animator: Animator?) {}
                                })
                                .start()
                    } else if ((it.y in (dataBarFinalY - 10.0f)..dataBarFinalY) && !isDataBarMoved){
                        teamContainerFragment.onDataMenuClosed()
                        it.animate().y(dataBarInitY)
                                .setDuration(400L)
                                .setInterpolator(AccelerateDecelerateInterpolator())
                                .setUpdateListener(object: ValueAnimator.AnimatorUpdateListener {
                                    override fun onAnimationUpdate(valueAnimator: ValueAnimator?) {
                                        dataMenuContent?.animate()
                                                ?.y(it.y - dataBarFinalY.roundToInt())
                                                ?.setDuration(0L)
                                                ?.start()
                                        dataBarAndDataMenuGapCoverLayerRelativeLayout?.animate()
                                                ?.y(it.y - dataBarFinalY.roundToInt())
                                                ?.setDuration(0L)
                                                ?.start()
                                        handleTeamViewAndDataBarMiddleLayerWhileMoving(it.y)
                                        handleDataBarAndDataMenuAndGapCoverColor(it.y)
                                        handleFabMenuButtonFadeInAndFadeOut(it.y)
                                        handleTeamViewMvpdBarFadeInAndFadeOut(it.y)
                                        handleSystemStatusBarColor(it.y)
                                        handleMiniPersistentPlayerFadeInAndFadeOut(it.y)
                                    }
                                })
                                .setListener(object: Animator.AnimatorListener {
                                    override fun onAnimationStart(animator: Animator?) {}
                                    override fun onAnimationRepeat(animator: Animator?) {}
                                    override fun onAnimationEnd(animator: Animator?) {
                                        addTeamViewContentTopConstraintToDataBar()
                                        if (it.y == dataBarInitY){
                                            disableTeamViewAndDataBarMiddleLayer()
                                            handleFabMenuButtonTouchDisable(false)
                                            handleDataBarAndDataMenuAndGapCoverColor(it.y)
                                            handleFabMenuButtonFadeInAndFadeOut(it.y)
                                            handleTeamViewMvpdBarFadeInAndFadeOut(it.y)
                                            handleSystemStatusBarColor(it.y)
                                            handleMiniPersistentPlayerFadeInAndFadeOut(it.y)
                                            setDataMenuIsOpened(false)
                                            removeDataMenuFragmentIfPossible()
                                            resetOrientation()
                                        }
                                        removeAllListenersOfAnimator(it, it.y)
                                    }
                                    override fun onAnimationCancel(animator: Animator?) {}
                                })
                                .start()
                    }
                }
            }
        }
        return true
    }

    /**
     * This method is used to set activity's orientation to portrait because
     * data menu is going to be opened
     */
    private fun initOrientation() {
        teamContainerFragment.activity?.apply {
            requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        }
    }

    /**
     * This method is used to reset activity's orientation base on conditions
     * Note: this method must be called after setDataMenuIsOpened(false), because
     *       one of the condition will be checking DATA_MENU_IS_OPENED
     */
    private fun resetOrientation() {
        teamContainerFragment.resetOrientationAfterDataMenuIsClosed()
    }

    private fun removeAllListenersOfAnimator(view: View, currentDataBarY: Float){
        view.animate().y(currentDataBarY)
                .setDuration(0L)
                .setUpdateListener(null)
                .setListener(null)
                .start()
    }

    private fun setDataMenuIsOpened(opened: Boolean) {
        DataMenuUtils.DATA_MENU_IS_OPENED = opened
    }

    private fun removeTeamViewContentTopConstraintFromDataBar(height: Int) {
        teamContainerRootConstraintLayout?.let {
            val constraintSet = ConstraintSet()
            constraintSet.clone((it as ConstraintLayout))
            constraintSet.clear(R.id.team_view_content, ConstraintSet.TOP)
            constraintSet.connect(R.id.team_view_content, ConstraintSet.TOP, R.id.team_view_mvpd_bar, ConstraintSet.BOTTOM, height)
            constraintSet.applyTo((it as ConstraintLayout))
        }
    }

    private fun addTeamViewContentTopConstraintToDataBar() {
        teamContainerRootConstraintLayout?.let {
            val constraintSet = ConstraintSet()
            constraintSet.clone((it as ConstraintLayout))
            constraintSet.clear(R.id.team_view_content, ConstraintSet.TOP)
            constraintSet.connect(R.id.team_view_content, ConstraintSet.TOP, R.id.databar_layout, ConstraintSet.BOTTOM, 0)
            constraintSet.applyTo((it as ConstraintLayout))
        }
    }

    private fun initDataMenuFragmentIfPossible(height: Int) {
        setDataMenuContentHeight(height)
        val fragment = childFragmentManager.findFragmentById(R.id.data_menu_content)
        if (fragment == null){
            childFragmentManager
                    .beginTransaction()
                    .add(R.id.data_menu_content,
                            DataMenuFragment.newInstance(
                                    teamContainerFragment.arguments?.getParcelable(TEAM_KEY),
                                    teamContainerFragment.arguments?.getParcelable<Config>(CONFIG_KEY),
                                    teamContainerFragment.isDataBarGameStateOffseason()))
                    .commitAllowingStateLoss()
        }
    }

    private fun removeDataMenuFragmentIfPossible() {
        setDataMenuContentHeight(0)
        val fragment = childFragmentManager.findFragmentById(R.id.data_menu_content)
        if (fragment != null && fragment is DataMenuFragment){
            childFragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss()
        }
    }

    private fun getDataMenuFragment(): Fragment? {
        return childFragmentManager.findFragmentById(R.id.data_menu_content)
    }

    private fun setDataMenuContentHeight(height: Int) {
        dataMenuContent?.apply {
            (layoutParams as ConstraintLayout.LayoutParams).height = height
            invalidate()
            requestLayout()
        }
    }

    private fun initAndShowTeamViewAndDataBarMiddleLayer(currentDataBarY: Float) {
        enableTeamViewAndDataBarMiddleLayer(true)
        setAppropriateAlphaOfTeamViewAndDataBarMiddleLayer(currentDataBarY)
        addMiddleLayerBottomConstraintToParent()
    }

    private fun initDataBarAndDataMenuGapCoverLayer(height: Int) {
        setDataBarAndDataMenuGapCoverLayerHeight(height)
    }

    private fun initDataBarAndDataMenuAndGapCoverColor(currentDataBarY: Float) {
        handleDataBarAndDataMenuAndGapCoverColor(currentDataBarY)
    }

    private fun initFabMenuButtonVisibility(currentDataBarY: Float) {
        handleFabMenuButtonTouchDisable(true)
        handleFabMenuButtonFadeInAndFadeOut(currentDataBarY)
    }

    private fun initTeamViewMvpdBarVisibility(currentDataBarY: Float) {
        handleTeamViewMvpdBarFadeInAndFadeOut(currentDataBarY)
    }

    private fun initSystemStatusBarColor(currentDataBarY: Float) {
        handleSystemStatusBarColor(currentDataBarY)
    }

    private fun initMiniPersistentPlayerFadeInAndFadeOut(currentDataBarY: Float) {
        handleMiniPersistentPlayerFadeInAndFadeOut(currentDataBarY)
    }

    private fun handleTeamViewAndDataBarMiddleLayerWhileMoving(currentDataBarY: Float) {
        enableTeamViewAndDataBarMiddleLayer(true)
        setAppropriateAlphaOfTeamViewAndDataBarMiddleLayer(currentDataBarY)
    }

    private fun setAppropriateAlphaOfTeamViewAndDataBarMiddleLayer(currentDataBarY: Float) {
        teamViewAndDataMenuMiddleLayerRelativeLayout?.apply {
            if (dataBarFinalY - dataBarInitY > 0.0f){
                alpha = (currentDataBarY - dataBarInitY) / (dataBarFinalY - dataBarInitY)
            }
        }
    }

    private fun addMiddleLayerBottomConstraintToParent() {
        teamContainerRootConstraintLayout?.let {
            val constraintSet = ConstraintSet()
            constraintSet.clone((it as ConstraintLayout))
            constraintSet.clear(R.id.team_view_and_data_menu_middle_layer_relative_layout,
                    ConstraintSet.BOTTOM)
            constraintSet.connect(R.id.team_view_and_data_menu_middle_layer_relative_layout,
                    ConstraintSet.BOTTOM, R.id.team_container_root_constraint_layout,
                    ConstraintSet.BOTTOM, 0)
            constraintSet.applyTo((it as ConstraintLayout))
        }
    }

    private fun removeMiddleLayerBottomConstraintToParent() {
        teamContainerRootConstraintLayout?.let {
            val constraintSet = ConstraintSet()
            constraintSet.clone((it as ConstraintLayout))
            constraintSet.clear(R.id.team_view_and_data_menu_middle_layer_relative_layout,
                    ConstraintSet.BOTTOM)
            constraintSet.connect(R.id.team_view_and_data_menu_middle_layer_relative_layout,
                    ConstraintSet.BOTTOM, R.id.team_container_root_constraint_layout,
                    ConstraintSet.TOP, 0)
            constraintSet.applyTo((it as ConstraintLayout))
        }
    }

    private fun disableTeamViewAndDataBarMiddleLayer() {
        enableTeamViewAndDataBarMiddleLayer(false)
    }

    private fun enableTeamViewAndDataBarMiddleLayer(enable: Boolean) {
        teamViewAndDataMenuMiddleLayerRelativeLayout?.isClickable = enable
    }

    private fun handleDataBarAndDataMenuAndGapCoverColor(currentDataBarY: Float) {
        if (dataBarFinalY - dataBarInitY > 0.0f){
            var databarColorPercent: Float = 10.0f * (currentDataBarY - dataBarInitY) / (dataBarFinalY - dataBarInitY)
            if (databarColorPercent > 1.0f) {
                databarColorPercent = 1.0f
            }
            teamContainerFragment.setDataBarColor(databarColorPercent)
            (getDataMenuFragment() as? DataMenuContract.View)?.setDataMenuColor(databarColorPercent)
            dataBarAndDataMenuGapCoverLayerRelativeLayout?.alpha = databarColorPercent
        }
    }

    private fun handleFabMenuButtonFadeInAndFadeOut(currentDataBarY: Float) {
        if (dataBarFinalY - dataBarInitY > 0.0f){
            var percent: Float = 2.0f * (currentDataBarY - dataBarInitY) / (dataBarFinalY - dataBarInitY)
            if (percent > 1.0f) {
                percent = 1.0f
            }
            (teamContainerFragment.activity as? MainContract.View)?.fadeInAndFadeOurFab(percent)
        }
    }

    private fun handleFabMenuButtonTouchDisable(disableTouch: Boolean) {
        (teamContainerFragment.activity as? MainContract.View)?.disableFabTouch(disableTouch)
    }

    private fun setDataBarAndDataMenuGapCoverLayerHeight(height: Int) {
        dataBarAndDataMenuGapCoverLayerRelativeLayout?.apply {
            // Increase 20 higher than data menu content in order to cover the gap
            // between data bar and data menu
            (layoutParams as ConstraintLayout.LayoutParams).height = height + 20
            invalidate()
            requestLayout()
        }
    }

    private fun handleTeamViewMvpdBarFadeInAndFadeOut(currentDataBarY: Float) {
        if (dataBarFinalY - dataBarInitY > 0.0f){
            var percent: Float = 10.0f * (currentDataBarY - dataBarInitY) / (dataBarFinalY - dataBarInitY)
            if (percent > 1.0f) {
                percent = 1.0f
            }
            teamViewMvpdBar?.alpha = 1.0f - percent
        }
    }

    private fun handleSystemStatusBarColor(currentDataBarY: Float) {
        if (dataBarFinalY - dataBarInitY > 0.0f){
            var percent: Float = 10.0f * (currentDataBarY - dataBarInitY) / (dataBarFinalY - dataBarInitY)
            if (percent > 1.0f) {
                percent = 1.0f
            }
            teamContainerFragment.activity?.let {
                val currentTeam: Team? = teamContainerFragment.arguments?.getParcelable(TEAM_KEY)
                currentTeam?.primaryColor?.let { primaryColorString ->
                    when (percent){
                        0.0f -> ActivityUtils.setStatusBarColor(it, Color.parseColor(primaryColorString))
                        else -> {
                            val colorIntOriginal = Color.parseColor(primaryColorString)
                            val alpha: Int = Color.alpha(colorIntOriginal)
                            val red: Int = (Math.abs((1.0f - percent) * Color.red(colorIntOriginal))).roundToInt()
                            val green: Int = (Math.abs((1.0f - percent) * Color.green(colorIntOriginal))).roundToInt()
                            val blue: Int = (Math.abs((1.0f - percent) * Color.blue(colorIntOriginal))).roundToInt()
                            ActivityUtils.setStatusBarColor(it, Color.argb(alpha, red, green, blue))
                        }
                    }
                }
            }
        }
    }

    private fun handleMiniPersistentPlayerFadeInAndFadeOut(currentDataBarY: Float) {
        if (dataBarFinalY - dataBarInitY > 0.0f) {
            var percent: Float = 2.0f * (currentDataBarY - dataBarInitY) / (dataBarFinalY - dataBarInitY)
            if (percent > 1.0f) {
                percent = 1.0f
            }
            (teamContainerFragment.activity as? PersistentPlayerContract.Main.View)?.fadeInAndFadeOurMini(percent)
        }
    }

}