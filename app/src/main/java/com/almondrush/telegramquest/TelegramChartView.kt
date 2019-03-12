package com.almondrush.telegramquest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.system.measureTimeMillis

class TelegramChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val paint = Paint()
    private val path = Path()
    private val pathRect: Rect = Rect()

    private var lineWidth: Float = 5F

    private var shouldUpdate: Boolean = true

    private var timeRange: IntRange = TimeRange.FULL

    private var lines: List<Line> = emptyList()
    private var linesPx: List<LinePx> = emptyList()

    init {
        initPaint()
        setChartData(testData)
    }

    fun setChartData(data: ChartData) {
        lines = data.lines.map { line ->
            Line(Color.parseColor(line.color), line.data.mapIndexed { index, value ->
                PointL(data.timeData[index], value)
            })
        }
        shouldUpdate = true
        invalidate()
    }

    fun setTimeRange(range: IntRange) {
        require(TimeRange.MIN <= range.first && range.last <= TimeRange.MAX)
        timeRange = range
        shouldUpdate = true
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        pathRect.set(0, measuredHeight, measuredWidth, 0)
        // Stroke width padding
        val strokePadding = (lineWidth / 2).toInt()
        pathRect.set(
            pathRect.left + strokePadding,
            pathRect.top - strokePadding,
            pathRect.right - strokePadding,
            pathRect.bottom + strokePadding
        )
        shouldUpdate = true
    }

    override fun onDraw(canvas: Canvas) {
        measureTimeMillis {
            if (shouldUpdate) {
                linesPx = updatePixelValues(lines, timeRange, pathRect)
                shouldUpdate = false
            }

            linesPx.forEach { line ->
                path.rewind()
                paint.color = line.color
                line.data.forEachIndexed { index, point ->
                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                canvas.drawPath(path, paint)
            }

        }.let {
            Log.v("CHART_ESTIMATE", "Estimate draw: $it")
        }
    }

    private fun initPaint() {
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = lineWidth
            isAntiAlias = true
        }
    }

    private fun updatePixelValues(lines: List<Line>, timeRange: IntRange, pathRect: Rect): List<LinePx> {
        val (minY, maxY) = findMinAndMaxYValues(lines)
        return lines.map { line ->
            val rangedValues = fitValuesIntoRange(line.data, timeRange)
            LinePx(line.color, convertToPixelValues(rangedValues, pathRect, minY, maxY))
        }
    }

    private fun fitValuesIntoRange(values: List<PointL>, selectedRange: IntRange): List<PointL> {
        val timeInterval = values.last().x - values.first().x

        val startTime = values.first().x + timeInterval * selectedRange.start / TimeRange.MAX
        val endTime = values.last().x - timeInterval * (TimeRange.MAX - selectedRange.endInclusive) / TimeRange.MAX

        var rangedValues = values.filter { it.x in startTime..endTime }

        val firstRangedValue = rangedValues.first()
        val lastRangedValue = rangedValues.last()

        //        val firstRangedValueIndex = values.indexOf(firstRangedValue)
        //        val lastRangedValueIndex = values.indexOf(lastRangedValue)
        //TODO add synthetic points
        return rangedValues
    }

    private fun convertToPixelValues(
        values: List<PointL>,
        pathRect: Rect,
        minYValue: Long,
        maxYValue: Long
    ): Array<PointF> {
        val pixelsPerXValue = (pathRect.right - pathRect.left).toFloat() / (values.last().x - values.first().x)
        val pixelsPerYValue = (pathRect.top - pathRect.bottom).toFloat() / (maxYValue - minYValue)

        return Array(values.size) { i ->
            val currentValue = values[i]
            val time = (currentValue.x - values.first().x) * pixelsPerXValue
            val value = pathRect.top - ((currentValue.y - minYValue) * pixelsPerYValue)
            PointF(time, value)
        }
    }

    private fun findMinAndMaxYValues(lines: List<Line>): Pair<Long, Long> {
        var maxY = lines.first().data.first().y
        var minY = lines.first().data.first().y
        lines.forEach { line ->
            line.data.forEach {
                if (it.y > maxY) maxY = it.y
                if (it.y < minY) minY = it.y
            }
        }
        return minY to maxY
    }

    fun d(t: String) =  Log.d("CHART_VIEW", t)

    data class Line(val color: Int, val data: List<PointL>)
    data class LinePx(val color: Int, val data: Array<PointF>)
    data class PointL(val x: Long, val y: Long)

}