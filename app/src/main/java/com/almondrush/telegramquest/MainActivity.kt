package com.almondrush.telegramquest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chartView: TelegramChartView = findViewById(R.id.chart_view)
        val controlView: TelegramChartControlView = findViewById(R.id.chart_control)

        controlView.setListener(object: TelegramChartControlView.TimeRangeUpdatedListener {
            override fun onTimeRangeUpdated(timeRange: IntRange) {
                chartView.setTimeRange(timeRange)
            }
        })
    }

}