package com.nbcsports.regional.nbc_rsn.stepped_story

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.WindowManager
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import com.airbnb.lottie.LottieAnimationView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.gson.Gson
import com.jakewharton.rxbinding2.view.RxView
import com.nbcsports.regional.nbc_rsn.MainPresenter
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.PageInfo
import com.nbcsports.regional.nbc_rsn.analytics.TrackingHelper.TrackingHelper
import com.nbcsports.regional.nbc_rsn.common.BaseFragment
import com.nbcsports.regional.nbc_rsn.common.FeedComponent
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.common.TeamFeedDataManager
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager.*
import com.nbcsports.regional.nbc_rsn.stepped_story.components.*
import com.nbcsports.regional.nbc_rsn.stepped_story.intent.SteppedStoryClickListenerFactory
import com.nbcsports.regional.nbc_rsn.team_feed.template.TeamFeedFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.collapsed_header_view.*
import kotlinx.android.synthetic.main.fragment_stepped_story.*
import kotlinx.android.synthetic.main.holder_cover_layout.view.*
import kotlinx.android.synthetic.main.holder_item_layout.view.*
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class SteppedStoryFragment : BaseFragment(),
        SteppedStoryContract.View, SteppedCoverLayout.OnPullListener {

    private val BR_TAG = "<br>"
    private val P_TAG = "</*p>"
    private val NEW_LINE_CHAR_PATTERN = "(\r)?\n"

    private var presenter: SteppedStoryPresenter? = null
    private var adapter: SteppedListAdapter? = null

    private val whiteFooter = true //Color of the final element of the list.
    private val BLACK_PEACOCK_ANIMATION = "peacock_animation_black.json"
    private val WHITE_PEACOCK_ANIMATION = "peacock_animation_white.json"

    private val COMPONENT_TYPE_COVER = "cover"
    private val COMPONENT_TYPE_OVERVIEW = "overview"
    private val COMPONENT_TYPE_LIST = "ordered_entry"

    private val EXIT_ANIM_MIN_PROGRESS = 0.08f
    private val EXIT_ANIM_MAX_PROGRESS = 0.44f
    private val ON_EXIT_ALPHA_ANIM_DUR = 500

    private val COVER_ANIM_DURATION = 200L
    private val COVER_ANIM_DELAY = 0.8

    private var collapsedHeaderStateAnimationDuration: Long = 0
    private var isCollapsedHeaderStateHidden = true

    private var translatedRecyclerViewY: Float = 0f
    private lateinit var teamFeedManager: TeamFeedDataManager
    private lateinit var teamFeed: ArrayList<FeedComponent>

    private var previousCollapsedColor: Int = Color.WHITE
    private var previousCollapsedDividerColor: Int = Color.GRAY

    private var layoutHeight: Float = 0f
    private var lottieAnimationView: LottieAnimationView? = null
    private var lottieAnimMinProgress = -1f
    private var lottieAnimMaxProgress = -1f
    private val iconHeightPercent: Float = 0.1f
    private val repeatCount: Int = 50

    var componentId: String = ""
        private set
    private var selectedFeedComponentPosition: Int = -1

    private var collapsedHeaderStateRef: FrameLayout? = null
    private var collapsedHeroTitleRef: TextView? = null
    private var shareButtonInCollapsedHeaderRef: ImageButton? = null
    private var collapsedDividerRef: View? = null

    private var ssCloseTextRef: TextView? = null
    private var steppedRecyclerViewRef: RecyclerView? = null
    private var steppedCoverRef: SteppedCoverLayout? = null
    private var steppedBackgroundToFadeToRef: LinearLayout? = null

    private var hasRVAnimationEnded = false
    private var collapsedHeaderHeight = 0
    private var isCoverVisible: Boolean = true


    override fun setPresenter(steppedStoryPresenter: SteppedStoryPresenter) {
        presenter = steppedStoryPresenter
    }

    override fun doExit(animated: Boolean) {
        if (parentFragment is TeamFeedFragment) {
            (parentFragment as TeamFeedFragment).closePage(animated)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_stepped_story
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = MainPresenter.Injection.provideView(context)
        val config = mainActivity?.mainPresenter?.lastKnownConfig
        setupLayouts()

        SteppedStoryPresenter(this, config)
        setupPresenter(arguments)

        initLottieAnimation(WHITE_PEACOCK_ANIMATION)
        initSteppedCover()
        setupCollapsedHeader()
        initRecyclerView(config?.playNonFeatureGifs == true)

        getSteppedStoryData(config?.editorialDetailsUrl, (config?.playNonFeatureGifs == true))

        // When returning to this fragment from FabMenu, we need to re-hide the cover
        if (savedInstanceState != null) {
            collapsedHeaderHeight = savedInstanceState.getInt("collapsed_header_height", 0)
            isCoverVisible = savedInstanceState.getBoolean("is_cover_visible", true)
            if (!isCoverVisible) hideCover(true)
            adapter?.requestRecoverTitlePosition()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("collapsed_header_height", collapsedHeaderHeight)
        outState.putBoolean("is_cover_visible", isCoverVisible)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // Without delay because
        // 1. There is no isCurrentSteppedFragment() check in hideDataBar() method, which means
        //    no race condition (due to the fragment transition animation)
        // TODO: When databar is required in SteppedStoryFragment, instead of hiding databar,
        // TODO: do databar modification here
        hideDataBar()
    }

    override fun onDetach() {
        // With delay because
        // 1. There is isCurrentSteppedFragment() check, which means there is race condition
        //    (due to the fragment transition animation)
        // 2. There is isCurrentSteppedFragment() check in showDataBar() also
        // TODO: When databar is required in SteppedStoryFragment, instead of hiding databar,
        // TODO: do databar modification here
        Observable.timer(600L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (isCurrentSteppedFragment()) {
                        hideDataBar()
                    } else {
                        showDataBar()
                    }
                    // Using ParentFragment here instead of this Fragment
                    // Because at this point, this.getActivity is equal to null
                    presenter?.let {
                        it.setAppropriateStatusBarColor(parentFragment?.activity, isCurrentSteppedFragment())
                    }
                }
        super.onDetach()
    }

    private fun setupLayouts() {
        collapsedHeaderStateRef = collapsed_header_state
        collapsedHeroTitleRef = collapsed_hero_title
        shareButtonInCollapsedHeaderRef = share_button_in_collapsed_header
        collapsedDividerRef = collapsed_divider
        ssCloseTextRef = ssCloseText
        steppedRecyclerViewRef = steppedRecyclerView
        steppedCoverRef = steppedCover
        steppedBackgroundToFadeToRef = stepped_background_to_fade_to
    }

    private fun setupPresenter(arguments: Bundle?) {
        teamFeed = arguments?.getParcelableArrayList<FeedComponent>(ARGUMENT_TEAM_FEED_COMPONENTS_LIST)
                ?: return
        selectedFeedComponentPosition = arguments.getInt(ARGUMENT_SELECTED_FEED_COMPONENT_POSITION, -1)
        if (selectedFeedComponentPosition >= 0) {
            componentId = teamFeed[selectedFeedComponentPosition].componentId
        }
        val team = arguments.getParcelable<Team>(ARGUMENT_TEAM) ?: return
        teamFeedManager = TeamFeedDataManager(teamFeed, team)
        presenter?.setTeam(team)
        presenter?.setFeedComponent(teamFeed[selectedFeedComponentPosition])
    }

    private fun getSteppedStoryData(url: String?, playNonFeatureGifs: Boolean) {
        var upNextFeedComponentPosition = selectedFeedComponentPosition + 1
        var feedComponent: FeedComponent? = null
        if (upNextFeedComponentPosition < teamFeed.size) {
            feedComponent = teamFeed[upNextFeedComponentPosition]
            // Note, the FeedComponent.Type.FOOTER is the type of the very last item that is expected to be at teem feed list.
            // So in this card's stepped story list, add a Recirculation item only if the next item in the teem feed list
            // is a real card, i.e. of type COMPONENT, but not of types PERSISTENT_PLAYER_MEDIUM (under index 0), FEED_PROMO (under index 11 or last but before FOOTER),
            // HEADER, THEFEED_LABEL or FOOTER.

            // Find next none feed promo, componentId not empty and contentType not empty feed to display
            while ((FeedComponent.Type.FEED_PROMO == feedComponent?.type) || (feedComponent?.componentId.isNullOrEmpty())
                    || (feedComponent?.contentType.isNullOrEmpty())) {
                if (upNextFeedComponentPosition < (teamFeed.size - 1)) {
                    feedComponent = teamFeed[++upNextFeedComponentPosition]
                } else {
                    upNextFeedComponentPosition = 0
                }
            }
            // Set up SteppedStoryClickListenerFactory with correct
            // upNextFeedComponentPosition
            val clickFactory = SteppedStoryClickListenerFactory(this, teamFeedManager, upNextFeedComponentPosition)
            adapter?.setSteppedStoryClickListenerFactory(clickFactory)
        }

        if (url.isNullOrBlank()) {
            adapter?.setData(mutableListOf(), feedComponent)
            steppedCoverRef?.setData(teamFeedManager.team, null, null, presenter, playNonFeatureGifs)
            setCollapsedHeaderTitle(null)
            return
        }

        val fullUrl = url!!.replace("[id]", componentId)

        val disposable = SteppedStoryDataManager.getInstance()?.getSteppedStoryDetailsFromServer(fullUrl, Gson())
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(
                        { feedItem ->
                            val steppedStory = feedItem?.steppedStory
                            val components = steppedStory?.steppedComponents

                            components?.let { filteredList ->
                                val cover = filteredList.first { it.componentType.equals(COMPONENT_TYPE_COVER, ignoreCase = true) }

                                // make sure the first ordered entry has the same image as cover if we don't have an overview
                                if (!filteredList.any { it.componentType.equals(COMPONENT_TYPE_OVERVIEW, ignoreCase = true) }) {
                                    filteredList.first { it.componentType.equals(COMPONENT_TYPE_LIST, ignoreCase = true) }?.entryImage = cover.coverImage
                                }

                                adapter?.setData(filteredList, feedComponent)

                                /*
                                * Another important thing to recover RV's scroll position is that the adapter needs to have data when
                                * calculating layout. If not, RV will immediately try to draw using the saved states, except that no
                                * data is available yet, so the saved states will be lost since its been used (even though failed).
                                *
                                * The solution is as simple as don't assign adapter to RV until we set its data.
                                * */
                                steppedRecyclerViewRef?.adapter = adapter
                                steppedCoverRef?.setData(teamFeedManager.team, steppedStory.type, cover, presenter, playNonFeatureGifs)
                                setCollapsedHeaderTitle(cover)
                                onPostRVAnimation()
                            }
                        },
                        {
                            e(it)
                        }
                )
        disposable?.let { compositeDisposable.add(it) }
    }

    private fun setCollapsedHeaderTitle(cover: SteppedComponent?) {
        var rawDisplayText = cover?.coverTitle ?: ""
        rawDisplayText = rawDisplayText
                .replace(NEW_LINE_CHAR_PATTERN.toRegex(), "")
                .replace(BR_TAG.toRegex(), "")
                .replace(P_TAG.toRegex(), "")
        collapsedHeroTitleRef?.text = rawDisplayText

        shareButtonInCollapsedHeaderRef?.apply {
            compositeDisposable.add(
                    RxView.clicks(this)
                            .observeOn(AndroidSchedulers.mainThread())
                            .throttleFirst(500L, TimeUnit.MILLISECONDS)
                            .subscribe {
                                onShareClick()
                            }
            )
        }
    }

    private fun initLottieAnimation(filename: String) {
        lottieAnimationView = ssCloseAnimation
        lottieAnimationView?.setAnimation(filename)
        lottieAnimMinProgress = EXIT_ANIM_MIN_PROGRESS
        lottieAnimMaxProgress = EXIT_ANIM_MAX_PROGRESS
        lottieAnimationView?.scale = iconHeightPercent
        lottieAnimationView?.repeatCount = repeatCount
    }

    private fun updateLottieProgress(percent: Float) {
        var progress = percent

        if (lottieAnimMinProgress > 0 && lottieAnimMaxProgress > 0) {
            val maxProgressWithOffSet = lottieAnimMaxProgress - lottieAnimMinProgress
            progress = (percent * maxProgressWithOffSet) + lottieAnimMinProgress
        }

        lottieAnimationView?.progress = progress
        lottieAnimationView?.cancelAnimation()
    }

    private var lottieAnimPercent: Float = 0f

    private fun initRecyclerView(playNonFeatureGifs: Boolean) {
        val displayMetrics = DisplayMetrics()
        val manager = context!!.applicationContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.getMetrics(displayMetrics)

        layoutHeight = displayMetrics.heightPixels.toFloat()
        steppedRecyclerViewRef?.translationY = layoutHeight

        val layoutManager = SteppedListLayoutManager((steppedRecyclerViewRef ?: steppedRecyclerView), object : OverScrollHelper {
            override fun showOverScrollEffect(dy: Int) {
                steppedRecyclerViewRef?.translationY = -dy.toFloat()

                lottieAnimationView?.alpha = 0f
                ssCloseTextRef?.alpha = 0f

                translatedRecyclerViewY = dy.toFloat()
            }

            override fun bounceBack() {
                steppedRecyclerViewRef?.animate()?.translationY(0f)?.start()

                val closeAlphaAnim = lottieAnimationView?.let {
                    ObjectAnimator.ofFloat(it, "alpha", 0f)
                }
                val closeTextAlphaAnim = ssCloseTextRef?.let {
                    ObjectAnimator.ofFloat(it, "alpha", 0f)
                }
                val valueAnimator = ValueAnimator.ofFloat(lottieAnimPercent, 0f).apply {
                    addUpdateListener {
                        updateLottieProgress(percent = 1f - it.animatedValue as Float)
                    }
                }

                AnimatorSet().apply {
                    duration = 1000L
                    play(closeAlphaAnim).with(closeTextAlphaAnim)
                    play(closeAlphaAnim).with(valueAnimator)
                    addListener {
                        doOnEnd {
                            addListener(doOnEnd { lottieAnimationView?.visibility = View.GONE })
                            addListener(doOnEnd { ssCloseTextRef?.visibility = View.GONE })
                        }
                    }
                    start()
                }

                bounceBackList()
            }

            override fun onTopOverScrollComplete(dy: Int) {
                showCover()
            }

            override fun onBottomOverScrollComplete(dy: Int) {
                val lm = steppedRecyclerViewRef?.layoutManager as? SteppedListLayoutManager
                val lastItem = lm?.findLastVisibleItemPosition()
                if (lastItem == steppedRecyclerViewRef?.adapter?.itemCount!! - 1) {
                    doExit(true)
                }
            }

            override fun onBottomOverScrolling(dy: Int) {
                val MAX_PULL_HEIGHT = 0.1f
                lottieAnimPercent = 1f - Math.min(1.0f, dy / (MAX_PULL_HEIGHT * layoutHeight))

                lottieAnimationView?.bringToFront()
                ssCloseTextRef?.bringToFront()
                lottieAnimationView?.visibility = View.VISIBLE
                ssCloseTextRef?.visibility = View.VISIBLE

                lottieAnimationView?.alpha = (1f - lottieAnimPercent)
                ssCloseTextRef?.alpha = (1f - lottieAnimPercent)

                updateLottieProgress(lottieAnimPercent)
            }

            override fun onTopOverScrolling(dy: Int) {
                overscrollCover(dy)
            }

            override fun getScrollParentHeight(): Int {
                return getRecyclerViewHeight()
            }

            override fun onStepChanged() {
                // Fire off adobe analytics info
                TrackingHelper.trackPageEvent(pageInfo)
            }
        })
        steppedRecyclerViewRef?.layoutManager = layoutManager
        steppedRecyclerViewRef?.overScrollMode = View.OVER_SCROLL_NEVER

        adapter = SteppedListAdapter(teamFeedManager.team)
        adapter?.setSteppedCoverLayout(steppedCoverRef)
        adapter?.setPlayNonFeatureGifs(playNonFeatureGifs)
        adapter?.setSteppedStoryPresenter(presenter)
        adapter?.viewHelper = object : SteppedStoryContract.ViewHelper {
            override fun adjustHeightIfNeeded(incomingHolder: SteppedListAdapter.ViewHolder) {
                if (incomingHolder.adapterPosition < 0 || incomingHolder.adapterPosition >= (adapter?.itemCount ?: 0)) {
                    return
                }

                val targetVH: RecyclerView.ViewHolder?
                val previewHeight: Int
                val firstVisiblePos = (steppedRecyclerViewRef?.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                if (firstVisiblePos == incomingHolder.adapterPosition) {
                    // scroll up, previous item attached to window
                    targetVH = incomingHolder
                    // findViewHolderForAdapterPosition returns null here
                    previewHeight = (steppedRecyclerViewRef?.findViewHolderForLayoutPosition(incomingHolder.adapterPosition + 1) as? SteppedListAdapter.ViewHolder)?.getPreviewHeight() ?: 0
                } else {
                    // scroll down, next item attached to window
                    previewHeight = incomingHolder.getPreviewHeight()
                    // findViewHolderForAdapterPosition returns null here
                    targetVH = steppedRecyclerViewRef?.findViewHolderForLayoutPosition(incomingHolder.adapterPosition - 1)
                }

                if (targetVH == null || previewHeight <= 0) {
                    return
                }

                targetVH.itemView.post {
                    val rvHeight = getRecyclerViewHeight()
                    if (targetVH.itemView.height + previewHeight < rvHeight) {
                        val layoutParams = targetVH.itemView.layoutParams
                        layoutParams.height = rvHeight - previewHeight
                        targetVH.itemView.layoutParams = layoutParams
                    }
                }
            }
        }
    }

    private fun getRecyclerViewHeight(): Int {
        // Incorrect RV height will cause ghosting effect of the preview image when overscrolling.
        // Technically rv.height will give us the actual visible rv height, and also because
        // rv is already set to the bottom of collapsed header in xml
        return (steppedRecyclerViewRef?.height ?: 0)
    }

    private fun resetScale() {

    }

    private fun initSteppedCover() {
        steppedCoverRef?.canScrollVertically = true
        steppedCoverRef?.setViewAtEdge(true)
        steppedCoverRef?.onPullListener = this

        steppedCoverRef?.apply {
            compositeDisposable.add(
                    RxView.clicks(this.coverShare)
                            .observeOn(AndroidSchedulers.mainThread())
                            .throttleFirst(500L, TimeUnit.MILLISECONDS)
                            .subscribe {
                                e("share -> click rxviewssss")
                                onShareClick()
                            }
            )
        }
    }

    /* START CollapsedHeader */
    private fun setupCollapsedHeader() {
//        // get the default animation duration of collapsedHeaderState, in case if we want to change and restore it
        collapsedHeaderStateAnimationDuration = collapsedHeaderStateRef?.animate()?.duration ?: 0L

        // must be measured after the steppedCover is initialized
        steppedCoverRef?.collapsedHeaderState = collapsedHeaderStateRef

        if (isCollapsedHeaderWhiteTheme()) {
            setCollapsedHeaderBlackTheme()
        }
    }

    private fun isCollapsedHeaderWhiteTheme(): Boolean {
        collapsedHeaderStateRef?.let {
            previousCollapsedColor = (it.background as ColorDrawable).color
            previousCollapsedDividerColor = (collapsedDividerRef?.background as? ColorDrawable)
                    ?.color ?: Color.BLACK
            return (it.background as ColorDrawable).color == Color.WHITE
        }

        return true
    }

    private fun setCollapsedHeaderBlackTheme() {
        collapsedHeaderStateRef?.apply {
            setBackgroundColor(Color.BLACK)
            collapsedHeroTitleRef?.setTextColor(Color.WHITE)
            shareButtonInCollapsedHeaderRef?.setImageResource(R.drawable.ic_share_white)
            collapsedDividerRef?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.grey_2, null))
        }
    }

    private fun resetCollapsedHeaderTheme() {
        collapsedHeaderStateRef?.apply {
            setBackgroundColor(previousCollapsedColor)
            shareButtonInCollapsedHeaderRef?.setImageResource(R.drawable.ic_share_black)
            collapsedDividerRef?.setBackgroundColor(previousCollapsedDividerColor)
        }
    }

    fun hideCollapsedHeaderState(hide: Boolean, duration: Long) {
        val direction = -1 // -1 means 'up', 1 means 'down'
        if (hide) { // hide
            if (!isCollapsedHeaderStateHidden) {
                // slide the collapsedHeaderState to the 'direction', by its height
                collapsedHeaderStateRef?.animate()?.translationY((direction * (collapsedHeaderStateRef?.height ?: 0)).toFloat())
                        ?.setDuration(duration)
                        ?.start()
                isCollapsedHeaderStateHidden = true

                collapsedHeaderStateRef?.visibility = View.GONE
            }
        } else { // show
            if (isCollapsedHeaderStateHidden) {
                collapsedHeaderStateRef?.visibility = View.VISIBLE
                // slide the collapsedHeaderState back to its original position
                collapsedHeaderStateRef?.animate()?.translationY(0f)
                        ?.setDuration(duration)
                        ?.start()
                isCollapsedHeaderStateHidden = false
            }
        }
    }
    /* END CollapsedHeader */

    override fun onResume() {
        super.onResume()
        // Fire off adobe analytics info
        if (presenter?.isTeamDisplayNameTheSameAsSelected() == true){
            TrackingHelper.trackPageEvent(pageInfo)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.clear()
        presenter?.unsubscribe()
        adapter?.unsubscribe()
        SteppedStoryDataManager.release()

        resetCollapsedHeaderTheme()
    }

    fun onShareClick() {
        presenter?.share(collapsedHeroTitleRef?.text?.toString())
    }

    override fun getPageInfo(): PageInfo? {
        return presenter?.getPageInfo()
    }

    override fun onPullComplete() {
        hideCover()
    }

    private fun hideCover(restoring: Boolean = false) {
        // Fire off adobe analytics info
        TrackingHelper.trackPageEvent(pageInfo)

        val headerHeight = collapsedHeaderStateRef?.measuredHeight ?: 0
        if (headerHeight > 0) {
            collapsedHeaderHeight = headerHeight
        }

        if (restoring) {
            steppedCoverRef?.visibility = View.INVISIBLE
        }

        steppedCoverRef?.baseView?.animate()
                ?.scaleX(1f)
                ?.scaleY(1f)
                ?.translationY(collapsedHeaderHeight.toFloat())
                ?.setDuration(COVER_ANIM_DURATION)
                ?.start()

        val animationSet = AnimationSet(true)
        val slideinAnimation = TranslateAnimation(
                Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, layoutHeight, Animation.ABSOLUTE, 0f
        )

        slideinAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                hasRVAnimationEnded = true
                onPostRVAnimation()
            }

            override fun onAnimationStart(animation: Animation?) {
                steppedRecyclerViewRef?.translationY = 0f
                if (restoring) {
                    steppedRecyclerViewRef?.visibility = View.INVISIBLE
                } else {
                    steppedRecyclerViewRef?.visibility = View.VISIBLE
                }
                hideCollapsedHeaderState(hide = false, duration = collapsedHeaderStateAnimationDuration)
            }
        })

        val alphaAnimation = AlphaAnimation(0.0f, 1.0f)
        alphaAnimation.startOffset = 30L
        animationSet.addAnimation(slideinAnimation)
        animationSet.addAnimation(alphaAnimation)
        animationSet.apply {
            duration = COVER_ANIM_DURATION
            fillAfter = true
        }

        // If OS version is less than Android 8
        // RecyclerView need to call requestLayout() before startAnimation(...)
        // Otherwise, RecyclerView won't show up
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            steppedRecyclerViewRef?.requestLayout()
        }
        hasRVAnimationEnded = false
        steppedRecyclerViewRef?.startAnimation(animationSet)
    }

    private fun onPostRVAnimation() {
        if (!hasRVAnimationEnded || steppedRecyclerViewRef?.adapter?.itemCount == 0) {
            return
        }
        isCoverVisible = false

        steppedRecyclerViewRef?.postDelayed({
            steppedCoverRef?.visibility = View.GONE

            // Get the first view holder on recycler view and show related view (image or video)
            val holder = steppedRecyclerViewRef?.findViewHolderForLayoutPosition(0)
            if (holder != null && holder is SteppedCoverViewHolder) {
                holder.showRelatedViews()
            } else if (holder != null && holder is SteppedItemViewHolder){
                holder.setUpForSteppedCover(steppedCoverRef)
            }

            if (holder != null && holder is SteppedCoverViewHolder && steppedCoverRef?.isVariationVideo == true) {
                presenter?.switchTarget((steppedCoverRef?.baseView as? PlayerView), holder.itemView.ss_item_cover_player_view)
            } else if (holder != null && holder is SteppedItemViewHolder && steppedCoverRef?.isVariationVideo == true){
                presenter?.switchTarget((steppedCoverRef?.baseView as? PlayerView), holder.itemView.ss_player_view)
            }

            steppedRecyclerViewRef?.visibility = View.VISIBLE
        }, 10L)
    }

    @SuppressLint("CheckResult")
    private fun showCover() {
        // Fire off adobe analytics info
        TrackingHelper.trackPageEvent(pageInfo)

        // Delay showing of the cover to show sliding out of the recyclerview
        Observable.timer(COVER_ANIM_DURATION * COVER_ANIM_DELAY.toLong(), TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    steppedCoverRef?.startTranslationY = -1 * translatedRecyclerViewY
                    steppedCoverRef?.resetView()
                    steppedCoverRef?.setViewAtEdge(true)
                }

        val animationSet = AnimationSet(true)
        val translateAnimation = TranslateAnimation(
                Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, -1 * translatedRecyclerViewY, Animation.ABSOLUTE, layoutHeight
        )

        translateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                steppedRecyclerViewRef?.translationY = layoutHeight
                translatedRecyclerViewY = 0f
                steppedRecyclerViewRef?.visibility = View.GONE
                isCoverVisible = true
            }

            override fun onAnimationStart(animation: Animation?) {
                // Get the first view holder on recycler view and hide all related view (image and video)
                val holder = steppedRecyclerViewRef?.findViewHolderForAdapterPosition(0)
                if (holder != null && holder is SteppedCoverViewHolder) {
                    holder.hideRelativeViews()
                } else if (holder != null && holder is SteppedItemViewHolder){
                    holder.hideRelativeViews()
                }

                if (holder != null && holder is SteppedCoverViewHolder && steppedCoverRef?.isVariationVideo == true) {
                    presenter?.switchTarget(holder.itemView.ss_item_cover_player_view,
                            (steppedCoverRef?.baseView as? PlayerView))
                } else if (holder != null && holder is SteppedItemViewHolder && steppedCoverRef?.isVariationVideo == true){
                    presenter?.switchTarget(holder.itemView.ss_player_view,
                            (steppedCoverRef?.baseView as? PlayerView))
                }
                hideCollapsedHeaderState(hide = true, duration = collapsedHeaderStateAnimationDuration)
            }
        })

        val alphaAnimation = AlphaAnimation(1f, 0f)
        alphaAnimation.startOffset = 30L
        animationSet.addAnimation(translateAnimation)
        animationSet.addAnimation(alphaAnimation)
        animationSet.apply {
            duration = COVER_ANIM_DURATION
            fillAfter = true
        }

        steppedRecyclerViewRef?.startAnimation(animationSet)
        steppedRecyclerViewRef?.overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun overscrollCover(dy: Int) {
        val holder = steppedRecyclerViewRef?.findViewHolderForAdapterPosition(0)
        if (holder != null && holder is SteppedCoverViewHolder) {
            holder.showRelatedViews()

            val height = holder.itemView.ss_item_cover_player_view.measuredHeight.toFloat()

            if (height > 0) {
                val percent = (height + abs(dy.toFloat())) / height

//                holder.itemView.ss_item_cover_media_container.animate()
//                        .scaleX(percent)
//                        .scaleY(percent)
//                        .setDuration(0L)
//                        .start()

//                holder.itemView.ss_item_first_text.animate()
//                        .translationY(-dy.toFloat())
//                        .setDuration(0L)
//                        .start()
            }
        }
    }

    private fun bounceBackList() {
        val holder = steppedRecyclerViewRef?.findViewHolderForAdapterPosition(0)
        if (holder != null && holder is SteppedCoverViewHolder) {
            holder.showRelatedViews()
        }
    }

    private fun isCurrentSteppedFragment(): Boolean {
        val parentFragment = this.parentFragment
        if (parentFragment is TeamFeedFragment){
            val fragment = parentFragment.childFragmentManager.findFragmentById(R.id.editorial_detail)
            return fragment is SteppedStoryFragment
        }
        return false
    }

    private fun showDataBar() {
        val parentFragment = this.parentFragment
        if (parentFragment is TeamFeedFragment){
            parentFragment.showDataBar()
        }
    }

    private fun hideDataBar() {
        val parentFragment = this.parentFragment
        if (parentFragment is TeamFeedFragment){
            parentFragment.hideDataBar()
        }
    }

    override fun onScaleTo(value: Float) {
        e("onScaleTo=>$value")
    }

    override fun onOpen() {
    }

    override fun onClose() {
    }
}