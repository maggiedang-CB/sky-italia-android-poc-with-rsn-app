package com.nbcsports.regional.nbc_rsn.stepped_story.components

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.exoplayer2.ui.PlayerView
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.common.Team
import com.nbcsports.regional.nbc_rsn.extensions.e
import com.nbcsports.regional.nbc_rsn.stepped_story.SteppedStoryPresenter
import com.nbcsports.regional.nbc_rsn.team_view.PeacockImageView
import com.nbcsports.regional.nbc_rsn.team_view.PicassoLoadListener
import com.nbcsports.regional.nbc_rsn.utils.DateFormatUtils
import kotlinx.android.synthetic.main.cover_layout.view.*
import kotlinx.android.synthetic.main.fragment_stepped_story.view.*
import kotlin.math.roundToInt

class SteppedCoverLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val BR_TAG = "<br>"
    private val P_TAG = "</*p>"
    private val NEW_LINE_CHAR_PATTERN = "(\r)?\n"

    var baseView: View

    val expandDuration = 200
    val expandInterpolation = 1.5f

    val collapseDuration = 200
    val collapseInterpolation = 1.5f

    var layoutHeight = 0
    var layoutWidth = 0
    var maxPullHeightPercent = 0.3f
    var paramsSubmitted = false
    var completionThreshold = 0
    var completionThresholdPercent = 0.25f
    var onCompletionHeight = 0
    var onCompletionHeightPercent = 0.35f

    var canScrollVertically: Boolean = true

    var isVariationVideo: Boolean = false

    var onPullListener: OnPullListener? = null

    var maxPullHeight: Int = 0
    val pullCompletionThreshold: Int = 0

    var collapsedHeaderState: View? = null

    private val COVER_OVERLAY_ALPHA: Float = 0.3f

    val coverShare: ImageView
    private val coverTag: AppCompatTextView
    private val coverTitle: AppCompatTextView
    private val color1: View
    private val color2: View
    private val authorDate: AppCompatTextView

    private var hlsVideoUrl: String? = null

    init {
        View.inflate(context, R.layout.cover_layout, this)
        baseView = findViewById(R.id.top_view)

        val overlay = findViewById<View>(R.id.stepped_cover_overlay)
        overlay.alpha = COVER_OVERLAY_ALPHA

        coverShare = findViewById(R.id.stepped_cover_share)
        coverTag = findViewById(R.id.stepped_cover_tag)
        coverTitle = findViewById(R.id.stepped_cover_title)
        color1 = findViewById(R.id.stepped_cover_color_1)
        color2 = findViewById(R.id.stepped_cover_color_2)
        authorDate = findViewById(R.id.stepped_cover_author_date)
    }

    companion object {
        private var maxScale: Float = 1f
        private var tagFinalPositionY = -300f
        private var shareFinalPositionY = -300f
        private var titleFinalPositionY = -300f
        private var colorFinalPositionY = -300f
        private var authorDateFinalPositionY = -300f
        private var arrowFinalPositionY = -300f
    }

    fun setData(team: Team, type: String?, component: SteppedComponent?, presenter: SteppedStoryPresenter?, playNonFeatureGifs: Boolean) {
        // TODO: video need not be hard coded
        isVariationVideo = component?.variation.equals("video", ignoreCase = true)
        hlsVideoUrl = getStreamUrl(component)

        if (isVariationVideo && !hlsVideoUrl.isNullOrBlank() && presenter != null) {
            initVariationVideo(presenter)
        } else if (presenter != null){
            initVariationImage(presenter, component, team, playNonFeatureGifs)
        }

        // Set up pivot point and scale of baseView initially
        val viewTreeObserver = baseView.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                baseView.apply {
                    pivotX = (layoutWidth / 2).toFloat()
                    pivotY = 0f
                    scaleX = maxScale
                    scaleY = maxScale
                }
            }
        })

        coverTag.text = type?.toUpperCase()
        val rawDisplayText = component?.coverTitle
        coverTitle.text = rawDisplayText?.replace(NEW_LINE_CHAR_PATTERN, "")
                ?.replace(BR_TAG, "")?.replace(P_TAG, "")
        checkAndAdjustTitleTextSize(coverTitle)

        color1.setBackgroundColor(Color.parseColor(team.primaryColor))
        color2.setBackgroundColor(Color.parseColor(team.secondaryColor))

        val publishDate = DateFormatUtils.getMonthDayYearText(component?.publishedDate?.toUpperCase())
        authorDate.text = "${component?.authorReference} / $publishDate"
    }

    fun checkAndAdjustTitleTextSize(title: TextView?) {
        if (title != null && title.text.length < 50) {
            // Set font size as per project "F1 Long Title" in Zeplin
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 42.0f) // Note: not COMPLEX_UNIT_SP, to be consistent with bias
        }
    }

    fun getStreamUrl(component: SteppedComponent?): String? {
        return component?.mediaSource?.streamUrl ?: ""
    }

    private fun initVariationImage(presenter: SteppedStoryPresenter?, component: SteppedComponent?,
                                   team: Team, playNonFeatureGifs: Boolean) {
        baseView = findViewById(R.id.top_view)
        baseView.visibility = View.VISIBLE
        val pv: View = findViewById(R.id.top_player_view)
        pv.visibility = View.GONE
        baseView.setOnTouchListener(getOnTouchListener())
        val iv = baseView as PeacockImageView
        val url = component?.coverImage
        if (url?.isNotBlank() == true) {
            iv.loadImage(presenter?.view?.context, playNonFeatureGifs, url, team.primaryColor,
                    object : PicassoLoadListener {
                override fun onSuccess() {
                    iv.requestLayout()
                }

                override fun onError(e: Throwable?) {
                    e("error -> $e")
                }
            })
        }
    }

    private fun initVariationVideo(presenter: SteppedStoryPresenter?) {
        baseView = findViewById(R.id.top_player_view)
        baseView.visibility = View.VISIBLE
        val iv: View = findViewById(R.id.top_view)
        iv.visibility = View.GONE
        // May need to change the getOnTouchListener()
        baseView.setOnTouchListener(getOnTouchListener())
        (presenter as SteppedStoryPresenter).playSteppedCoverVideo(hlsVideoUrl)
        (presenter as SteppedStoryPresenter).switchTarget(null, (baseView as PlayerView))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (!paramsSubmitted) { //Resize layouts only once.
            val displayMetrics = DisplayMetrics()
            val manager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            manager.defaultDisplay.getMetrics(displayMetrics)

            // use the fullscreen height
            layoutHeight = displayMetrics.heightPixels
            layoutWidth = View.MeasureSpec.getSize(widthMeasureSpec)

            maxPullHeight = (layoutHeight * maxPullHeightPercent).toInt()
            completionThreshold = (layoutHeight * completionThresholdPercent).toInt()
            onCompletionHeight = (layoutHeight * onCompletionHeightPercent).toInt()
            paramsSubmitted = true
        }

        val iv = baseView
        if (iv.measuredHeight > 0) {
            maxScale = layoutHeight / iv.measuredHeight.toFloat()
        }
    }

    var customLayoutManager = CoverLayoutManager(this)
    var startTranslationY: Float = 0f

    private fun getOnTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { v, event ->
            val eventY = event.rawY.roundToInt()

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> customLayoutManager.onUserDown(eventY)
                MotionEvent.ACTION_UP -> customLayoutManager.onUserUp(eventY)
                MotionEvent.ACTION_MOVE -> customLayoutManager.onUserMove(eventY)
            }

            return@OnTouchListener true
        }
    }

    fun expandAnimation(distanceFromDownY: Int, duration: Int) {
        if (maxScale <= 1f) return

        val toScale = ((distanceFromDownY + layoutHeight) / baseView.measuredHeight.toFloat())

        val percent = (maxScale - toScale) / (maxScale - 1f)
        var finalY: Float = 0f
        collapsedHeaderState?.let {
            finalY = it.height * percent
        }

        if (toScale in 1f..maxScale) {
            baseView.animate()
                    .scaleY(toScale)
                    .scaleX(toScale)
                    .translationY(finalY)
                    .setDuration(duration.toLong())
                    .start()

            onPullListener?.onScaleTo(toScale)
        }

        val animationSet = AnimatorSet()
        animationSet.playTogether(
                ObjectAnimator.ofFloat(stepped_cover_tag, "alpha", 1 - percent),
                ObjectAnimator.ofFloat(stepped_cover_share, "alpha", 1 - percent),
                ObjectAnimator.ofFloat(stepped_cover_title, "alpha", 1 - percent),
                ObjectAnimator.ofFloat(stepped_cover_color_1, "alpha", 1 - percent),
                ObjectAnimator.ofFloat(stepped_cover_color_2, "alpha", 1 - percent),
                ObjectAnimator.ofFloat(stepped_cover_author_date, "alpha", 1 - percent),
                ObjectAnimator.ofFloat(stepped_cover_arrow, "alpha", 1 - percent),
                ObjectAnimator.ofFloat(stepped_cover_overlay, "alpha", COVER_OVERLAY_ALPHA * (1 - percent)),

                ObjectAnimator.ofFloat(stepped_cover_tag, "translationY", percent * tagFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_share, "translationY", percent * shareFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_title, "translationY", percent * titleFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_color_1, "translationY", percent * colorFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_color_2, "translationY", percent * colorFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_author_date, "translationY", percent * authorDateFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_arrow, "translationY", percent * arrowFinalPositionY)
        )
        animationSet.interpolator = AccelerateDecelerateInterpolator()
        animationSet.duration = duration.toLong()
        animationSet.start()
    }

    fun completeAnimation() {
        val animationSet = AnimatorSet()
        animationSet.playTogether(
                ObjectAnimator.ofFloat(stepped_cover_tag, "alpha", 0f),
                ObjectAnimator.ofFloat(stepped_cover_share, "alpha", 0f),
                ObjectAnimator.ofFloat(stepped_cover_title, "alpha", 0f),
                ObjectAnimator.ofFloat(stepped_cover_color_1, "alpha", 0f),
                ObjectAnimator.ofFloat(stepped_cover_color_2, "alpha", 0f),
                ObjectAnimator.ofFloat(stepped_cover_author_date, "alpha", 0f),
                ObjectAnimator.ofFloat(stepped_cover_arrow, "alpha", 0f),
                ObjectAnimator.ofFloat(stepped_cover_overlay, "alpha", 0f),

                ObjectAnimator.ofFloat(stepped_cover_tag, "translationY", tagFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_share, "translationY", shareFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_title, "translationY", titleFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_color_1, "translationY", colorFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_color_2, "translationY", colorFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_author_date, "translationY", authorDateFinalPositionY),
                ObjectAnimator.ofFloat(stepped_cover_arrow, "translationY", arrowFinalPositionY)
        )
        animationSet.interpolator = AccelerateDecelerateInterpolator()
        animationSet.duration = 0L
        animationSet.start()

        onPullListener?.onScaleTo(1f)
    }

    fun collapseAnimation() {
        val animationSet = AnimatorSet()
        animationSet.playTogether(
                ObjectAnimator.ofFloat(baseView, "scaleX", maxScale),
                ObjectAnimator.ofFloat(baseView, "scaleY", maxScale),

                ObjectAnimator.ofFloat(stepped_cover_tag, "alpha", 1f),
                ObjectAnimator.ofFloat(stepped_cover_share, "alpha", 1f),
                ObjectAnimator.ofFloat(stepped_cover_title, "alpha", 1f),
                ObjectAnimator.ofFloat(stepped_cover_color_1, "alpha", 1f),
                ObjectAnimator.ofFloat(stepped_cover_color_2, "alpha", 1f),
                ObjectAnimator.ofFloat(stepped_cover_author_date, "alpha", 1f),
                ObjectAnimator.ofFloat(stepped_cover_arrow, "alpha", 1f),
                ObjectAnimator.ofFloat(stepped_cover_overlay, "alpha", 0.3f),

                ObjectAnimator.ofFloat(baseView, "translationY", 0f),
                ObjectAnimator.ofFloat(steppedCover, "translationY", 0f),

                ObjectAnimator.ofFloat(stepped_cover_tag, "translationY", 0f),
                ObjectAnimator.ofFloat(stepped_cover_share, "translationY", 0f),
                ObjectAnimator.ofFloat(stepped_cover_title, "translationY", 0f),
                ObjectAnimator.ofFloat(stepped_cover_color_1, "translationY", 0f),
                ObjectAnimator.ofFloat(stepped_cover_color_2, "translationY", 0f),
                ObjectAnimator.ofFloat(stepped_cover_author_date, "translationY", 0f),
                ObjectAnimator.ofFloat(stepped_cover_arrow, "translationY", 0f)
        )
        animationSet.interpolator = AccelerateDecelerateInterpolator()
        animationSet.duration = collapseDuration.toLong()
        animationSet.start()

        onPullListener?.onScaleTo(maxScale)
    }

    fun resetView() {
        visibility = View.VISIBLE

        // onResetPull() calls collapseAnimation() above
        customLayoutManager.onResetPull()
        enableScrolling()
    }

    fun enableScrolling() {
        canScrollVertically = true
    }

    fun disableScrolling() {
        canScrollVertically = false
    }

    fun setViewAtEdge(b: Boolean) {
        customLayoutManager.viewAtEdge = b
    }

    interface OnPullListener {
        fun onOpen()

        fun onClose()

        fun onPullComplete()

        fun onScaleTo(value: Float)
    }
}