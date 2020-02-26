package bg.dihanov.customviewexamples.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import bg.dihanov.customviewexamples.R

class IndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val rect: RectF = RectF(0f, 0f, 0f, 0f)

    var colors: List<Color> = emptyList()
        set(value) {
            if (field != value || arcs.isEmpty()) {
                field = value
                computeArcs()
            }
        }

    private val paint: Paint = Paint()
    private val greyColor = ContextCompat.getColor(context, R.color.other)
    private var arcs = emptyList<Arc>()

    private val colorMap = mapOf(Color.WHITE to ContextCompat.getColor(context, R.color.white),
        Color.BLUE to ContextCompat.getColor(context, R.color.blue),
        Color.BLACK to ContextCompat.getColor(context, R.color.black),
        Color.RED to ContextCompat.getColor(context, R.color.red),
        Color.GREEN to ContextCompat.getColor(context, R.color.green))

    private fun computeArcs() {
        arcs = if (colors.isEmpty()) {
            listOf(Arc(0f, 360f, greyColor))
        } else {
            val sweepSize: Float = 360f / colors.size
            colors.mapIndexed { index, color ->
                val startAngle = index * sweepSize
                Arc(start = startAngle, sweep = sweepSize, color = colorMap.getValue(color))
            }
        }
        invalidate()
    }

    init {
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        computeArcs()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.set(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        arcs.forEach { arc ->
            paint.color = arc.color
            canvas.drawArc(rect, START_ANGLE + arc.start, arc.sweep, true, paint)
        }
    }
}

private const val START_ANGLE = 225f

private class Arc(val start: Float, val sweep: Float, val color: Int)

enum class Color {
    WHITE, BLUE, BLACK, RED, GREEN
}