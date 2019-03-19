package com.almondrush.telegramquest

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.almondrush.telegramquest.dto.Line

class ChartControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val chart = ChartView(context, attrs, defStyleAttr, defStyleRes)
    private val thumb = ChartControlThumbView(context, attrs, defStyleAttr, defStyleRes)

    init {
        addView(chart)
        addView(thumb)
    }

    fun setListener(listener: XRangeUpdatedListener) {
        thumb.xRangeUpdatedListener = listener
    }

    fun setLines(lines: List<Line>) {
        chart.setData(lines, maxY = ChartUtil.findMaxYValue(lines))
    }

    interface XRangeUpdatedListener {
        fun onXRangeUpdated(timeRange: IntRange)
    }
}