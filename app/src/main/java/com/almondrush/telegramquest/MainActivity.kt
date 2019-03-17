package com.almondrush.telegramquest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chartView: TelegramChartWithLegendView = findViewById(R.id.chart_view)
        val controlView: TelegramChartControlView = findViewById(R.id.chart_control)

        chartView.setLines(testLines)
        controlView.setLines(testLines)


        controlView.setListener(object: TelegramChartControlView.XRangeUpdatedListener {
            override fun onXRangeUpdated(timeRange: IntRange) {
                chartView.setXRange(timeRange)
            }
        })
    }

}