package com.nbcsports.regional.nbc_rsn.common

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.Keep
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.nbcsports.regional.nbc_rsn.R

class CircleProgressBar: View {

    private var rectF: RectF? = null
    private var progressBarForegroundPaint: Paint? = null
    private var progressBarBackgroundPaint: Paint? = null

    private var progressMin: Int = 0
    private var progressMax: Int = 100
    private var progress: Float = 0.0f
    private var progressBarThickness: Float = 4.0f
    private var progressBarStartAngle: Float = -90.0f
    private var progressBarForegroundColor: Int = Color.WHITE
    private var progressBarBackgroundColor: Int = Color.GRAY
    private var progressBarForegroundStrokeCap: Paint.Cap = Paint.Cap.BUTT
    private var progressBarProgressAnimationDuration: Long = 1500L

    enum class StrokeCap {
        STROKE_CAP_NONE,
        STROKE_CAP_ROUND
    }

    constructor(context: Context): super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        rectF = RectF()

        attrs?.let {
            val typedArray: TypedArray = context.theme.obtainStyledAttributes(
                    it, R.styleable.CircleProgressBar, defStyleAttr, defStyleRes)
            // Get value from xml layout
            progressMin = typedArray.getInt(R.styleable.CircleProgressBar_progressMin, progressMin)
            progressMax = typedArray.getInt(R.styleable.CircleProgressBar_progressMax, progressMax)
            progress = typedArray.getFloat(R.styleable.CircleProgressBar_progress, progress)
            progressBarThickness = typedArray.getDimension(R.styleable.CircleProgressBar_progressBarThickness, progressBarThickness)
            progressBarForegroundColor = typedArray.getInt(R.styleable.CircleProgressBar_progressBarForegroundColor, progressBarForegroundColor)
            progressBarBackgroundColor = typedArray.getInt(R.styleable.CircleProgressBar_progressBarBackgroundColor, progressBarBackgroundColor)
            progressBarForegroundStrokeCap = when (StrokeCap.values()[typedArray.getInt(R.styleable.CircleProgressBar_progressBarForegroundStrokeCap, 0)]){
                StrokeCap.STROKE_CAP_NONE -> Paint.Cap.BUTT
                StrokeCap.STROKE_CAP_ROUND -> Paint.Cap.ROUND
            }
            progressBarProgressAnimationDuration = typedArray.getFloat(R.styleable.CircleProgressBar_progressBarProgressAnimationDuration, progressBarProgressAnimationDuration.toFloat()).toLong()
        }

        progressBarForegroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        progressBarForegroundPaint?.apply {
            color = progressBarForegroundColor
            style = Paint.Style.STROKE
            strokeWidth = progressBarThickness
            strokeCap = progressBarForegroundStrokeCap
        }

        progressBarBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        progressBarBackgroundPaint?.apply {
            color = progressBarBackgroundColor
            style = Paint.Style.STROKE
            strokeWidth = progressBarThickness
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height: Int = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width: Int = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min: Int = Math.min(height, width)
        setMeasuredDimension(min, min)
        rectF?.set(0.0f + progressBarThickness / 2.0f,
                0.0f + progressBarThickness / 2.0f,
                min - progressBarThickness / 2.0f,
                min - progressBarThickness / 2.0f)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            if (rectF != null
                    && progressBarForegroundPaint != null
                    && progressBarBackgroundPaint != null){
                it.drawOval(rectF!!, progressBarBackgroundPaint!!)
                val angle: Float = 360.0f * progress / progressMax
                it.drawArc(rectF!!, progressBarStartAngle, angle, false, progressBarForegroundPaint!!)
            }
        }
    }

    fun getProgressMin(): Int {
        return progressMin
    }

    fun setProgressMin(progressMin: Int) {
        this.progressMin = progressMin
        invalidate()
    }

    fun getProgressMax(): Int {
        return progressMax
    }

    fun setProgressMax(progressMax: Int) {
        this.progressMax = progressMax
        invalidate()
    }

    fun getProgress(): Float {
        return progress
    }

    @Keep
    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    fun getProgressBarThickness(): Float {
        return progressBarThickness
    }

    fun setProgressBarThickness(progressBarThickness: Float) {
        this.progressBarThickness = progressBarThickness
        progressBarForegroundPaint?.strokeWidth = progressBarThickness
        progressBarBackgroundPaint?.strokeWidth = progressBarThickness
        invalidate()
        requestLayout()
    }

    fun getProgressBarForegroundColor(): Int {
        return progressBarForegroundColor
    }

    fun setProgressBarForegroundColor(progressBarForegroundColor: Int) {
        this.progressBarForegroundColor = progressBarForegroundColor
        progressBarForegroundPaint?.color = progressBarForegroundColor
        invalidate()
        requestLayout()
    }

    fun getProgressBarBackgroundColor(): Int {
        return progressBarBackgroundColor
    }

    fun setProgressBarBackgroundColor(progressBarBackgroundColor: Int) {
        this.progressBarBackgroundColor = progressBarBackgroundColor
        progressBarBackgroundPaint?.color = progressBarBackgroundColor
        invalidate()
        requestLayout()
    }

    fun getProgressBarForegroundStrokeCap(): Paint.Cap {
        return progressBarForegroundStrokeCap
    }

    fun setProgressBarForegroundStrokeCap(progressBarForegroundStrokeCap: Paint.Cap) {
        this.progressBarForegroundStrokeCap = progressBarForegroundStrokeCap
        progressBarForegroundPaint?.strokeCap = progressBarForegroundStrokeCap
        invalidate()
        requestLayout()
    }

    fun setProgressWithAnimation(progress: Float) {
        val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(this, "progress", progress)
        objectAnimator.apply {
            duration = progressBarProgressAnimationDuration
            interpolator = DecelerateInterpolator()
            start()
        }
    }

}