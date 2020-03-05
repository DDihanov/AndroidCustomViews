package bg.dihanov.customviewexamples.views.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import bg.dihanov.customviewexamples.R
import bg.dihanov.customviewexamples.px
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin


//TODO: kinda laggy need to fix it
class SpiralProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        //60 FPS
        const val STEPS_PER_ROTATION = 60

        private val DEFAULT_HEIGHT = 100.px
        private val DEFAULT_WIDTH = 100.px
    }

    private val gradientPaint = Paint().apply {
        this.style = Paint.Style.FILL
        strokeWidth = 2.px.toFloat()
        this.isAntiAlias = true
    }

    private var startColor = ContextCompat.getColor(context, R.color.colorAccent)
        set(value) {
            field = value
            invalidate()
        }

    var endColor = ContextCompat.getColor(context, R.color.colorPrimary)
        set(value) {
            field = value
            invalidate()
        }

    var greyColor = ContextCompat.getColor(context, R.color.grey)

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.SpiralProgressBar)

            endColor = a.getColor(R.styleable.SpiralProgressBar_spiralProgressBar_endColor, greyColor)
            startColor = a.getColor(R.styleable.SpiralProgressBar_spiralProgressBar_startColor, greyColor)

            a.recycle()
        }
    }


    var speed: Speed = Speed.Slow
        set(value) {
            field = value
            invalidate()
        }

    /**
     * specify in DP
     */
    var spaceBetweenSegments = 5.px
        set(value) {
            field = value.px
            invalidate()
        }

    var numberOfSegments = 5
        set(value) {
            field = value
            invalidate()
        }

    private var centerX = 0.0f
    private var centerY = 0.0f
    private var segmentsAngles = numberOfSegments * Math.PI

    private var increment = 2 * Math.PI / STEPS_PER_ROTATION
    private var mod = 0.0f

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

        centerX = width / 2.toFloat()
        centerY = height / 2.toFloat()


        gradientPaint.shader = RadialGradient(centerX, centerY, height.toFloat() / 2,
            startColor,
            endColor,
            Shader.TileMode.CLAMP)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawSpiral(it)
            //TODO: can probably optimise this
            if (mod >= segmentsAngles) mod %= 12 else mod += speed.value
            postInvalidateDelayed((100 * speed.value).toLong())
        }
    }

    private fun drawSpiral(canvas: Canvas) {
        var x0 = centerX
        var y0 = centerY
        var theta = increment
        while (theta < segmentsAngles) {
            val x = (centerX + spaceBetweenSegments * theta * cos(theta - mod)).toFloat()
            val y = (centerY + spaceBetweenSegments * theta * sin(theta - mod)).toFloat()

            canvas.drawLine(x0, y0, x, y, gradientPaint)
            theta += increment

            x0 = x
            y0 = y
        }
    }

    sealed class Speed(var value: Float) {
        object UltraSlow: Speed(0.02f)
        object Slow: Speed(0.075f)
        object Normal: Speed(0.1f)
        object Fast: Speed(0.5f)
    }
}