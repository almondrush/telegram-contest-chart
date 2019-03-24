package com.almondrush.telegramquest.legend

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.almondrush.telegramquest.ChartUtil
import com.almondrush.telegramquest.ChartView
import com.almondrush.telegramquest.XRange
import com.almondrush.telegramquest.dto.Line
import kotlin.jvm.internal.Intrinsics

class ChartWithLegendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val MAX_Y_ANIMATION_DURATION_MS = 3000L
    }

    private val pRight = 32
    private val pLeft = 32

    private val chart = ChartView(context, attrs, defStyleAttr, defStyleRes)
    private val yAxisView = ChartYAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val xAxisView = ChartXAxisView(context, attrs, defStyleAttr, defStyleRes)
    private val xAxisPointerView = ChartXAxisPointerView(context, attrs, defStyleAttr, defStyleRes)
    private val pointerInfoView = ChartPointerInfoView(context, attrs, defStyleAttr, defStyleRes)

    private var xRange: IntRange = XRange.FULL
    private var lines: List<Line> = emptyList()

    init {
        addView(yAxisView)
        addView(chart)
        addView(xAxisView)
        addView(xAxisPointerView)
        addView(pointerInfoView)

        pointerInfoView.setupWith(xAxisPointerView)

        xAxisView.setChartPadding(pLeft, pRight)
    }

    fun setXRange(xRange: IntRange) {
        this.xRange = xRange
        setData(lines, xRange)
    }

    fun setLines(lines: List<Line>) {
        this.lines = lines

        chart.setLines(lines)
        xAxisPointerView.setLines(lines)
        pointerInfoView.setLines(lines)

        setFullTimeRangeToChildren(ChartUtil.getTimeRange(lines))
        setData(lines, xRange)
    }

    private fun setData(lines: List<Line>, xRange: IntRange) {
        val maxY = ChartUtil.findMaxYValueRanged(lines, xRange)
        setMaxYToChildren(maxY)
        setXRangeToChildren(xRange)
    }

    private fun setXRangeToChildren(xRange: IntRange) {
        chart.setXRange(xRange)
        xAxisView.setXRange(xRange)
        xAxisPointerView.setXRange(xRange)
    }

    private fun setMaxYToChildren(maxY: Long) {
        chart.setMaxY(maxY)
        yAxisView.setMaxY(maxY)
        xAxisPointerView.setMaxYValue(maxY)
    }

    private fun setFullTimeRangeToChildren(range: LongRange) {
        xAxisView.setFullTimeRange(range)
        xAxisPointerView.setFullTimeRange(range)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(xAxisView, widthMeasureSpec, heightMeasureSpec)
        measureChildWithMargins(
            yAxisView,
            widthMeasureSpec,
            pLeft + pRight,
            heightMeasureSpec,
            xAxisView.measuredHeight
        )
        measureChildWithMargins(chart, widthMeasureSpec, pLeft + pRight, heightMeasureSpec, xAxisView.measuredHeight)
        measureChildWithMargins(
            pointerInfoView,
            widthMeasureSpec,
            pLeft + pRight,
            heightMeasureSpec,
            xAxisView.measuredHeight
        )
        measureChildWithMargins(xAxisPointerView, widthMeasureSpec, pLeft + pRight, heightMeasureSpec, xAxisView.measuredHeight)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        chart.layout(pLeft, 0, chart.measuredWidth + pRight, chart.measuredHeight)
        yAxisView.layout(pLeft, 0, yAxisView.measuredWidth + pRight, yAxisView.measuredHeight)
        xAxisView.layout(0, height - xAxisView.measuredHeight, xAxisView.measuredWidth, bottom)
        xAxisPointerView.layout(pLeft, 0, chart.measuredWidth + pRight, chart.measuredHeight)
        pointerInfoView.layout(
            width - (pRight + pointerInfoView.measuredWidth),
            0,
            width - pRight,
            pointerInfoView.measuredHeight
        )

        pointerInfoView
    }
}