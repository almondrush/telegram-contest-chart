package com.almondrush.telegramquest.legend

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.almondrush.interval
import com.almondrush.telegramquest.ChartUtil
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

    private val pointInnerRadius: Float = 10F
    private val pointOuterRadius: Float = 15F
    private val pointInnerColor = Color.parseColor("#FFFFFF")

    private var touchX: Float? = null
    private var isTouched = false

    private val linePaint = Paint()
    private val pointPaint = Paint()

    private lateinit var fullTimeRange: LongRange
    private lateinit var lines: List<Line>
    private var xRange: IntRange = XRange.FULL
    private var maxY = 0L

    private var chartPoints = emptyArray<ChartPoint>()

    init {
        linePaint.color = Color.parseColor("#333333")
        linePaint.strokeWidth = 1F
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
            }
            MotionEvent.ACTION_MOVE -> {
                touchX = event.x
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                touchX = null
                invalidate()
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
        }
    }

    private fun Canvas.drawChartPoint(x: Float, y: Float, color: Int) {
        pointPaint.color = color
        drawCircle(x, height - y, pointOuterRadius, pointPaint)
        pointPaint.color = pointInnerColor
        drawCircle(x, height - y, pointInnerRadius, pointPaint)
    }

    private data class ChartPoint(var color: Int, var x: Int = 0, var y: Int = 0, var time: Long = 0, var value: Long = 0)
}
