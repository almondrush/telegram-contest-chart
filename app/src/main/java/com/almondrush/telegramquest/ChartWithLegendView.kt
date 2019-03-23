package com.almondrush.telegramquest

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.almondrush.telegramquest.dto.Line

class ChartWithLegendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val pRight = 32
    private val pLeft = 32

    private val chart = ChartView(context, attrs, defStyleAttr, defStyleRes)
    private val yAxisView = ChartYAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val xAxisView = ChartXAxisView(context, attrs, defStyleAttr, defStyleRes)

    private var lines: List<Line> = emptyList()

    init {
        addView(yAxisView)
        addView(chart)
        addView(xAxisView)
        xAxisView.setChartPadding(pLeft, pRight)
    }

    fun setXRange(xRange: IntRange) {
        setData(lines, xRange)
    }

    fun setLines(lines: List<Line>) {
        this.lines = lines
        xAxisView.setFullTimeRange(ChartUtil.getTimeRange(lines))
        setData(lines, XRange.FULL)
    }

    private fun setData(lines: List<Line>, xRange: IntRange) {
        val maxY = ChartUtil.findMaxYValueRanged(lines, xRange)
        chart.setData(lines, xRange, maxY)
        yAxisView.setMaxY(maxY)
        xAxisView.setXRange(xRange)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(xAxisView, widthMeasureSpec, heightMeasureSpec)
        L.d(xAxisView.measuredHeight)
        measureChildWithMargins(
            yAxisView,
            widthMeasureSpec,
            pLeft + pRight,
            heightMeasureSpec,
            xAxisView.measuredHeight
        )
        measureChildWithMargins(chart, widthMeasureSpec, pLeft + pRight, heightMeasureSpec, xAxisView.measuredHeight)

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        chart.layout(pLeft, 0, chart.measuredWidth + pRight, chart.measuredHeight)
        yAxisView.layout(pLeft, 0, yAxisView.measuredWidth + pRight, yAxisView.measuredHeight)
        xAxisView.layout(0, height - xAxisView.measuredHeight, xAxisView.measuredWidth, bottom)
    }
}