package com.almondrush.telegramquest

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class TelegramChartControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val childChart = TelegramChartView(context, attrs, defStyleAttr, defStyleRes)
    private val thumb = TelegramChartControlThumb(context, attrs, defStyleAttr, defStyleRes)

    init {
        addView(childChart)
        addView(thumb)
    }

    fun setListener(listener: TimeRangeUpdatedListener) {
        thumb.timeRangeUpdatedListener = listener
    }


    interface TimeRangeUpdatedListener {
        fun onTimeRangeUpdated(timeRange: IntRange)
    }
}