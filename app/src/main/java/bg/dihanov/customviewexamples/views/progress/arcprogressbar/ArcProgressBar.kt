package bg.dihanov.customviewexamples.views.progress.arcprogressbar

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import bg.dihanov.customviewexamples.R
import bg.dihanov.customviewexamples.px
import kotlin.math.max
import kotlin.math.min


class ArcProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        private const val DEFAULT_STROKE_WIDTH = 50f
        private const val LOADING_WIDTH_MODIFIER = 20f
        private const val DEFAULT_STROKE_LOADING_WIDTH =
            DEFAULT_STROKE_WIDTH - LOADING_WIDTH_MODIFIER
    }

    private val rect = RectF()
    private val textRect = Rect()

    private val greyColor = ContextCompat.getColor(context, R.color.grey)
    private val loadingColor = ContextCompat.getColor(context, R.color.colorAccent)

    private val bgPaint = Paint().apply {
        color = greyColor
        style = Paint.Style.STROKE
        strokeWidth =
            DEFAULT_STROKE_WIDTH
        strokeCap = Paint.Cap.BUTT
        isAntiAlias = true
    }

    private val loadingPaint = Paint().apply {
        color = loadingColor
        strokeWidth =
            DEFAULT_STROKE_LOADING_WIDTH
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        isAntiAlias = true
    }

    private val textPaint = TextPaint().apply {
        isAntiAlias = true
    }

    private var _progress = 0
    var progress
        get() = _progress
        set(value) {
            setProgressValue(value)
        }

    private var _textColor = greyColor
    var textColor
        get() = _textColor
        set(value) {
            _textColor = value
            textPaint.color = _textColor
            invalidate()
        }

    private var _progressColor = loadingColor
    var progressColor
        get() = _progressColor
        set(value) {
            _progressColor = value
            loadingPaint.color = _progressColor
            invalidate()
        }

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ArcProgressBar)

            textColor = a.getColor(R.styleable.ArcProgressBar_arc_textColor, greyColor)
            progressColor = a.getColor(R.styleable.ArcProgressBar_arc_progressColor, loadingColor)

            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var width = paddingLeft + paddingRight
        var height = paddingTop + paddingBottom

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        } else {
            width += 200.px
            width = max(width, suggestedMinimumWidth)
            if (widthMode == MeasureSpec.AT_MOST) width = min(widthSize, width)
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else {
            height += 200.px
            height = max(height, suggestedMinimumHeight)
            if (heightMode == MeasureSpec.AT_MOST) height = min(height, heightSize)
        }

        val strokeRatio = (width / 8).toFloat()
        loadingPaint.strokeWidth = strokeRatio
        bgPaint.strokeWidth = strokeRatio

        val rectWidthStart = 0f + strokeRatio
        val rectWidthEnd = width - strokeRatio

        val rectHeightStart = 0f + strokeRatio
        val rectHeightEnd = height - strokeRatio

        rect.set(rectWidthStart, rectHeightStart, rectWidthEnd, rectHeightEnd)

        textPaint.textSize = rectHeightEnd * 0.2f
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let { canv ->
            canv.drawArc(rect, 180f, 180f, false, bgPaint)

            val fillPercentage = (180f * (progress / 100.0)).toFloat()

            canv.drawArc(rect, 180f, fillPercentage, false, loadingPaint)

            drawCenter(canvas, textPaint, progress.toString())
        }
    }


    private fun drawCenter(
        canvas: Canvas,
        paint: Paint,
        text: String
    ) {
        canvas.getClipBounds(textRect)
        val cHeight = textRect.height()
        val cWidth = textRect.width()
        paint.textAlign = Paint.Align.LEFT
        paint.getTextBounds(text, 0, text.length, textRect)
        val x = cWidth / 2f - textRect.width() / 2f - textRect.left
        //+10 to start at baseline of the arc
        val y = cHeight / 2f + textRect.height() / 2f - textRect.bottom - 30
        canvas.drawText(text, x, y, paint)
    }

    fun progressToValueManually(
        duration: Long,
        interpolator: TimeInterpolator = AccelerateDecelerateInterpolator()
    ) {
        ValueAnimator.ofInt(0, 100).apply {
            addUpdateListener { updatedAnimation ->
                setProgressValue(updatedAnimation.animatedValue as Int)
            }
            this.interpolator = interpolator
            this.duration = duration
            start()
        }
        ValueAnimator.ofInt(0, 100)
        setProgressValue(progress)
    }

    private fun setProgressValue(value: Int) {
        _progress = value
        invalidate()
    }
}