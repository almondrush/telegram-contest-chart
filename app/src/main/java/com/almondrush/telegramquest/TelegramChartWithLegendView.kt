package com.almondrush.telegramquest

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.almondrush.telegramquest.dto.Line

class TelegramChartWithLegendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val chart = TelegramChartView(context, attrs, defStyleAttr, defStyleRes)
    private val yAxisView = TelegramChartValueAxisView(context, attrs, defStyleAttr, defStyleRes)

    private var lines: List<Line> = emptyList()

    init {
        addView(yAxisView)
        addView(chart)
    }

    fun setXRange(xRange: IntRange) {
        setData(lines, xRange)
    }

    fun setLines(lines: List<Line>) {
        this.lines = lines
        setData(lines, XRange.FULL)
    }

    private fun setData(lines: List<Line>, xRange: IntRange) {
        val maxY = ChartUtil.findMaxYValueRanged(lines, xRange)
        chart.setData(lines, xRange = xRange, maxY = maxY)
        yAxisView.setMaxY(maxY)
    }

}