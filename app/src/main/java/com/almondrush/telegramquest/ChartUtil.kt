package com.almondrush.telegramquest

import android.graphics.PointF
import android.graphics.Rect
import com.almondrush.interval
import com.almondrush.telegramquest.dto.Line
import com.almondrush.telegramquest.dto.LinePx
import com.almondrush.telegramquest.dto.PointL

object ChartUtil {

    fun selectTimeRange(fullTimeRange: LongRange, selectionRange: IntRange): LongRange {
        val timeInXRangeUnit = fullTimeRange.interval / XRange.MAX
        val start = (fullTimeRange.start + selectionRange.start * timeInXRangeUnit)
        val end = (fullTimeRange.start + selectionRange.endInclusive * timeInXRangeUnit)
        return start..end
    }

    fun getTimeRange(lines: List<Line>): LongRange {
        val startTime = requireNotNull(lines.map { it.data.first() }.minBy { it.x }).x
        val endTime = requireNotNull(lines.map { it.data.last() }.maxBy { it.x }).x
        return startTime..endTime
    }

    fun findMaxYValueRanged(lines: List<Line>, xRange: IntRange) = lines.mapNotNull {
        fitPointsIntoRange(it.data, xRange).maxBy(PointL::y)
    }.maxBy(PointL::y)?.y ?: 0

    fun findMaxYValue(lines: List<Line>) = lines.mapNotNull {
        it.data.maxBy(PointL::y)
    }.maxBy(PointL::y)?.y ?: 0

    fun calculatePixelValues(lines: List<Line>, xRange: IntRange, maxY: Long, pathRect: Rect): List<LinePx> {
        return lines.map { line ->
            val rangedValues = fitPointsIntoRange(line.data, xRange)

            val xStart = line.data.first().x
            val xEnd = line.data.last().x

            val xInterval = xEnd - xStart

            val xRangeUnitValue = xInterval / XRange.MAX

            val timeRangeStart = xStart + xRange.start * xRangeUnitValue
            val timeRangeEnd = xStart + xRange.endInclusive * xRangeUnitValue

            val timeRange = timeRangeStart..timeRangeEnd

            LinePx(
                line.color,
                convertToPixelValues(timeRange, rangedValues, maxY, pathRect)
            )
        }
    }

    private fun convertToPixelValues(
        timeRangeMs: LongRange,
        points: List<PointL>,
        yMax: Long,
        pathRect: Rect
    ): List<PointF> {
        val pixelsPerXValue =
            (pathRect.right - pathRect.left).toFloat() / (timeRangeMs.endInclusive - timeRangeMs.start)
        val pixelsPerYValue = (pathRect.top - pathRect.bottom).toFloat() / yMax

        return points.map { point ->
            val time = (point.x - timeRangeMs.start) * pixelsPerXValue
            val value = pathRect.top - (pathRect.bottom + point.y * pixelsPerYValue)
            PointF(time, value)
        }
    }

    private fun fitPointsIntoRange(points: List<PointL>, xRange: IntRange): List<PointL> {
        val xInterval = points.last().x - points.first().x

        val xStart = points.first().x + xInterval * xRange.start / XRange.MAX
        val xEnd = points.last().x - xInterval * (XRange.MAX - xRange.endInclusive) / XRange.MAX

        val rangedValues = points.filter { it.x in xStart..xEnd }.toMutableList()

        val firstRangedValueIndex = points.indexOf(rangedValues.first())
        val lastRangedValueIndex = points.indexOf(rangedValues.last())

        if (firstRangedValueIndex > 0) {
            rangedValues.add(0, points[firstRangedValueIndex - 1])
        }

        if (lastRangedValueIndex < points.size - 1) {
            rangedValues.add(points[lastRangedValueIndex + 1])
        }

        return rangedValues
    }
}