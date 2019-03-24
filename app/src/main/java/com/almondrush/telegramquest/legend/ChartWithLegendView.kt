package com.almondrush.telegramquest.legend

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.almondrush.telegramquest.ChartUtil
import com.almondrush.telegramquest.ChartView
import com.almondrush.telegramquest.R
import com.almondrush.telegramquest.XRange
import com.almondrush.telegramquest.dto.Line

class ChartWithLegendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var chartPaddingRight = 0
    private var chartPaddingLeft = 0

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

        context.theme.obtainStyledAttributes(attrs, R.styleable.ChartWithLegendView, defStyleAttr, defStyleRes).apply {
            try {
                chartPaddingLeft = getDimensionPixelSize(R.styleable.ChartWithLegendView_chartLegendPaddingLeft, 0)
                chartPaddingRight = getDimensionPixelSize(R.styleable.ChartWithLegendView_chartLegendPaddingRight, 0)
            } finally {
                recycle()
            }
        }



        pointerInfoView.setupWith(xAxisPointerView)
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
            chartPaddingLeft + chartPaddingRight,
            heightMeasureSpec,
            xAxisView.measuredHeight
        )
        measureChildWithMargins(
            chart,
            widthMeasureSpec,
            chartPaddingLeft + chartPaddingRight,
            heightMeasureSpec,
            xAxisView.measuredHeight
        )
        measureChildWithMargins(
            pointerInfoView,
            widthMeasureSpec,
            chartPaddingLeft + chartPaddingRight,
            heightMeasureSpec,
            xAxisView.measuredHeight
        )
        measureChildWithMargins(
            xAxisPointerView,
            widthMeasureSpec,
            chartPaddingLeft + chartPaddingRight,
            heightMeasureSpec,
            xAxisView.measuredHeight
        )
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        chart.layout(chartPaddingLeft, 0, chart.measuredWidth + chartPaddingRight, chart.measuredHeight)
        yAxisView.layout(chartPaddingLeft, 0, yAxisView.measuredWidth + chartPaddingRight, yAxisView.measuredHeight)
        xAxisView.layout(0, height - xAxisView.measuredHeight, xAxisView.measuredWidth, bottom)
        xAxisPointerView.layout(chartPaddingLeft, 0, chart.measuredWidth + chartPaddingRight, chart.measuredHeight)
        pointerInfoView.layout(
            width - (chartPaddingRight + pointerInfoView.measuredWidth),
            0,
            width - chartPaddingRight,
            pointerInfoView.measuredHeight
        )

        pointerInfoView
    }
}