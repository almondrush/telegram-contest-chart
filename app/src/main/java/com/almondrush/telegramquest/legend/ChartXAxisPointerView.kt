package com.almondrush.telegramquest.legend

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.almondrush.dpToPx
import com.almondrush.interval
import com.almondrush.telegramquest.ChartUtil
import com.almondrush.telegramquest.R
import com.almondrush.telegramquest.XRange
import com.almondrush.telegramquest.dto.Line
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal class ChartXAxisPointerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val DP_Y_TO_STOP_TRACKING_TOUCH = 50
    }

    var listener: Listener? = null

    private var lineColor: Int = 0
    private var lineWidth = 0
    private var pointInnerRadius = 0
    private var pointOuterRadius = 0
    private var pointInnerColor = 0

    private var touchX: Float? = null
    private var isTouched = false
    private var touchY: Float = 0F
    private val pxYToStopTrackingTouch = DP_Y_TO_STOP_TRACKING_TOUCH.dpToPx(context)

    private val linePaint = Paint()
    private val pointPaint = Paint()

    private lateinit var fullTimeRange: LongRange
    private lateinit var lines: List<Line>
    private var xRange: IntRange = XRange.FULL
    private var maxY = 0L

    private var chartPoints = emptyArray<ChartPoint>()

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ChartXAxisPointerView, defStyleAttr, defStyleRes)
            .apply {
                try {
                    lineColor = getColor(R.styleable.ChartXAxisPointerView_chartLegendLineColor, 0)
                    lineWidth = getDimensionPixelSize(R.styleable.ChartXAxisPointerView_chartLegendLineWidth, 0)
                    pointInnerRadius = getDimensionPixelSize(R.styleable.ChartXAxisPointerView_pointInnerRadius, 0)
                    pointOuterRadius = getDimensionPixelSize(R.styleable.ChartXAxisPointerView_pointOuterRadius, 0)
                    pointInnerColor = getColor(R.styleable.ChartXAxisPointerView_pointInnerColor, 0xFFFFFF)
                } finally {
                    recycle()
                }
            }

        linePaint.color = lineColor
        linePaint.strokeWidth = lineWidth.toFloat()
        pointPaint.style = Paint.Style.FILL
    }

    fun setMaxYValue(value: Long) {
        maxY = value
    }

    fun setFullTimeRange(range: LongRange) {
        fullTimeRange = range
    }

    fun setLines(lines: List<Line>) {
        this.lines = lines
        chartPoints = Array(lines.size) { index -> ChartPoint(lines[index].color) }
    }

    fun setXRange(range: IntRange) {
        this.xRange = range
    }

    private fun calculateChartPoints(x: Float) {
        val time = findClosestTimeValue(x, lines.first())
        val displayX = timeToXPixel(time, fullTimeRange, xRange)
        lines.mapIndexed { index, line ->
            val point = requireNotNull(line.data.find { it.x == time })
            val y = ((point.y.toFloat() / maxY) * height).roundToInt()
            chartPoints[index].let {
                it.color = line.color
                it.time = time
                it.value = point.y
                it.x = displayX
                it.y = y
            }
        }
    }

    private fun findClosestTimeValue(x: Float, line: Line): Long {
        val xRelative = x / width
        val xRangeValue = xRange.start + xRange.interval * xRelative
        val xTimeValue = ((fullTimeRange.interval * xRangeValue) / XRange.MAX + fullTimeRange.start).roundToLong()
        return requireNotNull(line.data.minBy { Math.abs(it.x - xTimeValue) }).x
    }

    private fun timeToXPixel(time: Long, fullTimeRange: LongRange, xRange: IntRange): Int {
        val timeRange = ChartUtil.selectTimeRange(fullTimeRange, xRange)
        val timeRelative = (time.toFloat() - timeRange.start) / timeRange.interval
        return  (width * timeRelative).roundToInt()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                touchX = event.x
                isTouched = true
                invalidate()
                touchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(event.y - touchY) > pxYToStopTrackingTouch) {
                    parent.requestDisallowInterceptTouchEvent(false)
                    touchX = null
                    invalidate()
                    listener?.onSelectionRemoved()
                    return false
                }
                touchX = event.x
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                touchX = null
                invalidate()
                listener?.onSelectionRemoved()
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        touchX?.let { x ->
            calculateChartPoints(x)
            val displayX = chartPoints.first().x.toFloat()
            canvas.drawLine(displayX, 0F, displayX, height.toFloat(), linePaint)
            chartPoints.forEach { canvas.drawChartPoint(it.x.toFloat(), it.y.toFloat(), it.color) }
            listener?.onSelectedPointChanged(chartPoints.first().time, chartPoints.map { it.value })
        }
    }

    private fun Canvas.drawChartPoint(x: Float, y: Float, color: Int) {
        pointPaint.color = color
        drawCircle(x, height - y, pointOuterRadius.toFloat(), pointPaint)
        pointPaint.color = pointInnerColor
        drawCircle(x, height - y, pointInnerRadius.toFloat(), pointPaint)
    }

    private data class ChartPoint(var color: Int, var x: Int = 0, var y: Int = 0, var time: Long = 0, var value: Long = 0)

    interface Listener {
        fun onSelectedPointChanged(time: Long, values: List<Long>)
        fun onSelectionRemoved()
    }
}
