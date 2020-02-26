package bg.dihanov.customviewexamples.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import bg.dihanov.customviewexamples.R
import kotlin.math.max
import kotlin.math.min

class CircularProgressView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    // required to draw the arcs
    private val rectF = RectF()

    // Used to draw pretty much anything on a canvas, which is what we will be drawing on
    private val paint = Paint().apply {
        // how we want the arcs to be draw, we want to make sure the arc centers are not colored
        // so we use a STROKE instead.
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // default percentage set to 0
    private var percentage = 0

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

        setMeasuredDimension(width, height)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            rectF.apply {
                val width = (it.width.div(2)).toFloat() // center X of the canvas
                val height = (it.height.div(2)).toFloat() // center Y of the canvas

                // place the rectF it at the center of the screen with height and width of 200dp
                set(width - 200, height - 200, width + 200, height + 200)
            }

            // draw an arc that will represent an empty loading view
            it.drawArc(rectF, 0f, 360f, false, paint.apply {
                color = ContextCompat.getColor(context, R.color.grey)

                // how wide the stroke should be, typically more than or equal to the strokeWidth
                // of the arc representing a filled percentage
                strokeWidth = 60f
            })

            // get the actual percentage as a float
            val fillPercentage = (360 * (percentage / 100.0)).toFloat()

            // draw the arc that will represent the percentage filled up
            it.drawArc(rectF, 270f, fillPercentage, false, paint.apply {
                color = ContextCompat.getColor(context, R.color.green) // filled percentage color

                // how wide the stroke should be, typically less than or equal to the strokeWidth
                // of the empty arc
                strokeWidth = 30f
            })
        }
    }

    fun setPercentage(percentage: Int) {
        this.percentage = percentage
        invalidate()
    }
}