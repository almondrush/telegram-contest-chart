package com.almondrush.telegramquest

import com.almondrush.interval
import com.almondrush.telegramquest.dto.Line
import com.almondrush.telegramquest.dto.PointL

object ChartUtil {

    fun selectTimeRange(fullTimeRange: LongRange, selectionRange: IntRange): LongRange {
        val timeInXRangeUnit = fullTimeRange.interval / XRange.MAX
        val start = (fullTimeRange.start + selectionRange.start * timeInXRangeUnit)
        val end = (fullTimeRange.start + selectionRange.endInclusive * timeInXRangeUnit)
        return start..end
    }

    fun getTimeRange(lines: List<Line>): LongRange {
        if (lines.isEmpty()) return 0L..0L
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

    fun fitPointsIntoRange(points: List<PointL>, xRange: IntRange): List<PointL> {
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