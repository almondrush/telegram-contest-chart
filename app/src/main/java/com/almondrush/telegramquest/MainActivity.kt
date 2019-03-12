package com.almondrush.telegramquest

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chartView: TelegramChartView = findViewById(R.id.chart_view)

        val seekBar1: SeekBar = findViewById(R.id.seek_bar1)
        val seekBar2: SeekBar = findViewById(R.id.seek_bar2)

        fun onSeekChanged() {
            chartView.setTimeRange((seekBar1.progress / 2)..(50 + seekBar2.progress / 2))
        }

        seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onSeekChanged()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onSeekChanged()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

}