package bg.dihanov.customviewexamples.views.progress

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import bg.dihanov.customviewexamples.R
import bg.dihanov.customviewexamples.px
import kotlin.math.max
import kotlin.math.min


class ColorfulProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        private const val PROGRESS_KEY = "colorfulprogressbarprogresskey"
        private const val SUPER_STATE_KEY = "colorfulprogressbarprogresskeysuper"
        private const val DEFAULT_DURATION = 5000L
        private val DEFAULT_HEIGHT = 30.px
        private val DEFAULT_WIDTH = 30.px
    }

    private val greyColor = ContextCompat.getColor(context, R.color.grey)

    private var currentColor: Int = 0
    private var startColor: Int = greyColor
    private var endColor: Int = greyColor
    private var cornerRadius: Float = 10.px.toFloat()

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.BUTT
    }

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ColorfulProgressBar)

            endColor = a.getColor(R.styleable.ColorfulProgressBar_colorfulBar_endColor, greyColor)
            startColor = a.getColor(R.styleable.ColorfulProgressBar_colorfulBar_startColor, greyColor)
            cornerRadius = a.getFloat(R.styleable.ColorfulProgressBar_colorfulBar_cornerRadius, 10.px.toFloat())
            expandFromMiddle = a.getBoolean(R.styleable.ColorfulProgressBar_colorfulBar_expandFromMiddle, false)

            a.recycle()
        }
    }

    var progress = 0
        set(value) {
            field = value
            invalidate()
        }

    var expandFromMiddle = false
        set(value) {
            field = value
            invalidate()
        }

    fun progressToManually(duration: Long = DEFAULT_DURATION, interpolator: TimeInterpolator = AccelerateDecelerateInterpolator()) {
        ValueAnimator.ofInt(0, 100).apply {
            this.duration = duration
            this.interpolator = interpolator
            start()
            addUpdateListener {
               progress = it.animatedValue as Int
            }
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
            width += DEFAULT_WIDTH
            width = max(width, suggestedMinimumWidth)
            if (widthMode == MeasureSpec.AT_MOST) width = min(widthSize, width)
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else {
            height += DEFAULT_HEIGHT
            height = max(height, suggestedMinimumHeight)
            if (heightMode == MeasureSpec.AT_MOST) height = min(height, heightSize)
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = currentColor
        val fraction = width * progress * 0.01f
        if (expandFromMiddle) {
            //color change is twice as fast because we are only progressing 50% on both sides
            currentColor = ColorUtils.blendARGB(startColor, endColor, min(1f, progress * 0.02f))
            canvas?.let {
                canvas.drawRoundRect(
                    max(0f, width / 2f - fraction), 0f, min(width.toFloat(), width / 2 + fraction), height.toFloat() - paddingBottom, cornerRadius, cornerRadius, paint
                )
            }
        } else {
            currentColor = ColorUtils.blendARGB(startColor, endColor, progress * 0.01f)
            canvas?.let {
                canvas.drawRoundRect(
                    0f, 0f, fraction, height.toFloat() - paddingBottom, cornerRadius, cornerRadius, paint
                )
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return Bundle().apply {
            this.putInt(PROGRESS_KEY, progress)
            this.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var newState = state
        if (newState is Bundle) {
            progress = newState.getInt(PROGRESS_KEY)
            newState = newState.getParcelable(SUPER_STATE_KEY)
        }
        super.onRestoreInstanceState(newState)
    }

}