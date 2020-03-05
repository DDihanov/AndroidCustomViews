package bg.dihanov.customviewexamples.views.graph

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import bg.dihanov.customviewexamples.R
import bg.dihanov.customviewexamples.px
import java.text.NumberFormat
import kotlin.math.max
import kotlin.math.min


//finished as per https://proandroiddev.com/building-a-custom-view-a-practical-example-2753cb9d0e80
//animated with PathMeasure - couldn't get this to animate the markers as well
//so this is just a proof of concept that it works with PathMeasure as well as a manual implementation
class AnimatedGraphWithPathMeasure @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        private const val MAX_WEEKS = 4
        private var INTERVAL = 5.px.toFloat()
        private var DOTTED_STROKE_WIDTH_DP = 1.px.toFloat()
        private var STROKE_WIDTH_DP = 1.px.toFloat()
        private var TEXT_BG_RADIUS = 20.px.toFloat()
        private var POINT_RADIUS = 3.px.toFloat()
        private var WEEK_PADDING = 30.px
        private var WEEK_DISTANCE = 10.px
        private var GRADUATIONS_BOTTOM_PADDING = 10.px
        private var GRADUATIONS_SIDE_PADDING = 10.px
    }

    private var _colorStart: Int = ContextCompat.getColor(context, R.color.colorStart)
    var colorStart
        get() = _colorStart
        set(value) {
            _colorStart = value
            colors = intArrayOf(colorStart, colorEnd)
            invalidate()
        }

    private var _colorEnd: Int = ContextCompat.getColor(context, R.color.colorEnd)
    var colorEnd
        get() = _colorEnd
        set(value) {
            _colorEnd = value
            colors = intArrayOf(colorStart, colorEnd)
            invalidate()
        }

    private var _lineColor: Int = ContextCompat.getColor(context, R.color.lineColor)
    var lineColor
        get() = _lineColor
        set(value) {
            _lineColor = value
            invalidate()
        }

    private var _dottedLineColor: Int = ContextCompat.getColor(context, R.color.dottedLineColor)
    var dottedLineColor
        get() = _dottedLineColor
        set(value) {
            _dottedLineColor = value
            invalidate()
        }

    // prepare the gradient paint
    private var _colors = intArrayOf(colorStart, colorEnd)
    var colors
        get() = _colors
        set(value) {
            _colors = value
            invalidate()
        }

    private var _bgColor = ContextCompat.getColor(context, R.color.bg)
    var bgColor
        get() = _bgColor
        set(value) {
            _bgColor = value
            invalidate()
        }
    private var _outlineColor = ContextCompat.getColor(context, R.color.greyOutline)
    var outlineColor
        get() = _outlineColor
        set(value) {
            _outlineColor = value
            invalidate()
        }

    private val graduations: MutableList<Int> = mutableListOf()

    private var gradient: LinearGradient? = null
    private var gradientPaint: Paint? = null

    private val dottedPaint: Paint
    private val strokePaint: Paint
    private val pointPaint: Paint
    private val textPaint: TextPaint = TextPaint().apply {
        this.color = ContextCompat.getColor(context, R.color.textColor)
        this.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        isAntiAlias = true
    }
    private val graduationsTextPaint: TextPaint = TextPaint().apply {
        this.color = ContextCompat.getColor(context, R.color.textColor)
        this.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        isAntiAlias = true
    }
    private val textPaintBg: Paint = Paint()

    private val textRect: Rect = Rect()

    private var zeroY: Float = 0f
    private var pxPerUnit: Float = 0f

    private var graduationsDistanceScale: Float = 0f
    private var weeksDistanceScale: Float = 0f

    private var markers: List<Marker> = mutableListOf()
    private var weeks: MutableList<String> = mutableListOf()

    private val gradientPath: Path = Path()
    private val linePath: Path = Path()
    private val circlePath: Path = Path()
    private val guidelinePath: Path = Path()

    private var linePathMeasure: PathMeasure = PathMeasure()

    private var tempPath: Path = Path()

    private var lineLength = 0f
    private var percentage = 0

    private var chartHeight: Int = 0
    private var chartWidth: Int = 0
    private var scaleSpaceToLeaveForGraduations: Int = 0

    init {
        dottedPaint = Paint().apply {
            this.style = Paint.Style.STROKE
            this.strokeWidth = DOTTED_STROKE_WIDTH_DP
            this.pathEffect = DashPathEffect(floatArrayOf(INTERVAL, INTERVAL), 0f)
            this.isAntiAlias = true
            this.color = dottedLineColor
        }
        strokePaint = Paint().apply {
            this.style = Paint.Style.STROKE
            this.strokeWidth = STROKE_WIDTH_DP
            this.isAntiAlias = true
            this.color = lineColor
        }
        pointPaint = Paint().apply {
            this.style = Paint.Style.FILL
            this.isAntiAlias = true
            this.color = lineColor
        }
    }

    fun setMarkersAndWeeks(markers: List<Marker>, weeks: List<String>) {
        this.weeks = weeks.toMutableList()
        this.markers = markers
        initGraduations()
        initWeeks()
        ValueAnimator.ofInt(100).apply {
            duration = 5000
            addUpdateListener {
                percentage = it.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    private fun initWeeks() {
        val count = weeks.count()
        val temp = weeks.take(MAX_WEEKS)
        weeks.clear()
        weeks.addAll(temp)
        if (count > MAX_WEEKS) {
            weeks.add("${count - MAX_WEEKS}+")
        }
    }

    private fun initGraduations() {
        val max = markers.maxBy { it.value }!!
        if (max.value <= 100) {
            graduations.add(0)
            graduations.add(100)
            return
        }
        var start = max.value - max.value % 100
        while (start >= 0) {
            graduations.add(start)
            start -= 100
        }
        graduations.reverse()
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

        //compensate for the week text padding
        chartHeight = height - WEEK_PADDING
        chartWidth = width
        scaleSpaceToLeaveForGraduations = (width - 2 * GRADUATIONS_SIDE_PADDING) / markers.size
        weeksDistanceScale = (width / weeks.size).toFloat()
        graduationsDistanceScale = (height / graduations.size) * 0.8.toFloat()
        textPaint.textSize = weeksDistanceScale / weeks.size / 1.5f
        graduationsTextPaint.textSize = scaleSpaceToLeaveForGraduations * 0.4.toFloat()
        calcAndInvalidate()
        setMeasuredDimension(chartWidth, height + WEEK_PADDING)
    }

    private fun calcAndInvalidate() {
        calcPositions(markers)
        initGradient()
        calcLineMarkerPath()
        linePathMeasure.setPath(linePath, false)
        lineLength = linePathMeasure.length
    }

    private fun initGradient() {
        gradientPaint = Paint().apply {
            this.style = Paint.Style.FILL
            this.shader = gradient
            this.isAntiAlias = true
        }
        gradient = LinearGradient(
            0f, paddingTop.toFloat(), 0f, zeroY, colors, null, Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        drawGradient(canvas)
        drawGuidelines(canvas)
        drawLines(canvas)
        drawMarkers(canvas)
        drawWeeks(canvas)
        drawGraduations(canvas)
    }

    private fun drawMarkers(canvas: Canvas) {
        canvas.drawPath(circlePath, pointPaint)
    }

    private fun drawGraduations(canvas: Canvas) {
        val x = markers.last().currentPos.x + GRADUATIONS_SIDE_PADDING
        //leave some padding in the bottom
        var step = 0f + GRADUATIONS_BOTTOM_PADDING
        for (value in graduations) {
            val y = zeroY - step
            val formatted = NumberFormat.getIntegerInstance().format(value)
            canvas.drawText(formatted, x, y, graduationsTextPaint)
            step += graduationsDistanceScale
        }
    }

    private fun drawLines(canvas: Canvas) {
        tempPath.reset()

        linePathMeasure.getSegment(0.0f, lineLength * percentage * 0.01f, tempPath, true)

        tempPath.rLineTo(
            0.0f,
            0.0f
        ) // workaround to display on hardware accelerated canvas as described in docs

        canvas.drawPath(tempPath, strokePaint)
    }

    private fun drawWeeks(canvas: Canvas) {
        for ((i, week) in weeks.withIndex()) {
            textPaint.getTextBounds(week, 0, week.length, textRect)
            val x = middle(i) + WEEK_DISTANCE * 2
            val y = zeroY + textRect.height() + WEEK_DISTANCE
            val halfHeight = textRect.height() / 2f
            val left = x - WEEK_DISTANCE
            val top = y - halfHeight - WEEK_DISTANCE
            val right = x + textRect.width() + WEEK_DISTANCE
            val bottom = y + WEEK_DISTANCE
            textRect.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            textPaintBg.color = bgColor
            textPaintBg.style = Paint.Style.FILL
            canvas.drawRoundRect(textRect.toRectF(), TEXT_BG_RADIUS, TEXT_BG_RADIUS, textPaintBg)
            textPaintBg.color = outlineColor
            textPaintBg.style = Paint.Style.STROKE
            textPaintBg.strokeWidth = 2.px.toFloat()
            canvas.drawRoundRect(textRect.toRectF(), TEXT_BG_RADIUS, TEXT_BG_RADIUS, textPaintBg)
            canvas.drawText(week, x, y, textPaint)
        }
    }

    private fun middle(i: Int): Float {
        return (i * (chartWidth / weeks.count())).toFloat()
    }

    private fun drawGuidelines(canvas: Canvas) {
//        val first = findFirstDayOfWeekInMonth(markers)
//        val first = markers[0]
        for (i in 0..markers.lastIndex step 3) {
            val marker = markers[i]
            guidelinePath.reset()
            guidelinePath.moveTo(marker.currentPos.x, paddingTop.toFloat())
            guidelinePath.lineTo(marker.currentPos.x, zeroY)
            canvas.drawPath(guidelinePath, dottedPaint)
        }
    }

    private fun drawGradient(canvas: Canvas) {
        gradientPath.reset()
        gradientPath.moveTo(paddingLeft.toFloat(), zeroY)

        for (marker in markers) {
            gradientPath.lineTo(marker.currentPos.x, marker.currentPos.y)
        }

        // close the path
        gradientPath.lineTo(markers.last().currentPos.x, zeroY)
        gradientPath.lineTo(paddingLeft.toFloat(), zeroY)

        canvas.drawPath(gradientPath, gradientPaint!!)
    }


    private fun calcPositions(markers: List<Marker>) {
        val max = markers.maxBy { it.value }!!
        val min = markers.minBy { it.value }!!
        pxPerUnit = (chartHeight - paddingTop - paddingBottom)/ (max.value - min.value).toFloat()
        zeroY = max.value * pxPerUnit + paddingTop

        val step = (chartWidth - 2 * GRADUATIONS_SIDE_PADDING - scaleSpaceToLeaveForGraduations) / (markers.size - 1)
        for ((i, marker) in markers.withIndex()) {
            val x = step * i + paddingLeft
            val y = zeroY - marker.value * pxPerUnit
            marker.currentPos.x = x.toFloat()
            marker.currentPos.y = y
        }
    }

    private fun calcLineMarkerPath() {
        var previousMarker: Marker? = null
        val first = markers.first()
        linePath.moveTo(first.currentPos.x, first.currentPos.y)
        circlePath.moveTo(first.currentPos.x, first.currentPos.y)
        for (marker in markers) {
            if (previousMarker != null) {
                // draw the line
                val p = marker.currentPos
                linePath.lineTo(p.x, p.y)
            }
            previousMarker = marker

            // draw the marker
            circlePath.addCircle(
                marker.currentPos.x,
                marker.currentPos.y,
                POINT_RADIUS, Path.Direction.CW
            )
        }
    }
}
