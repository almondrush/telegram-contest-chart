package com.almondrush.telegramquest

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.almondrush.setOnRippleClickListener
import com.almondrush.telegramquest.control.ChartControlView
import com.almondrush.telegramquest.dto.Line
import com.almondrush.telegramquest.legend.ChartWithLegendView

class MainActivity : AppCompatActivity() {

    lateinit var chartView: ChartWithLegendView
    lateinit var controlView: ChartControlView
    lateinit var chartLabels: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chartView = findViewById(R.id.chart_view)
        controlView = findViewById(R.id.chart_control)
        chartLabels = findViewById(R.id.chart_line_names_layout)

        controlView.setListener(object: ChartControlView.XRangeUpdatedListener {
            override fun onXRangeUpdated(timeRange: IntRange) {
                chartView.setXRange(timeRange)
            }
        })

        setLines(testLines)
    }

    private fun setLines(lines: List<Line>) {
        chartView.setLines(lines)
        controlView.setLines(lines)
        createLabels(lines)
    }

    private fun onLabelClick(line: Line) {
        line.isEnabled = !line.isEnabled
    }


    private fun createLabels(lines: List<Line>) {
        lines.mapIndexed { index, line ->
            val label = createLabel(line, index != lines.size - 1)
            chartLabels.addView(label)
        }
    }

    private fun createLabel(line: Line, isDividerEnabled: Boolean): View {
        val view = LayoutInflater.from(this).inflate(R.layout.chat_label, chartLabels, false)
        val checkBox: CheckBox = view.findViewById(R.id.chart_label_checkbox)
        val textView: TextView = view.findViewById(R.id.chart_label_text)
        val divider: View = view.findViewById(R.id.chart_label_divider)

        checkBox.isChecked = line.isEnabled
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val colors = intArrayOf(line.color, line.color)
        checkBox.buttonTintList = ColorStateList(states, colors)
        textView.text = line.name
        view.setOnRippleClickListener {
            onLabelClick(line)
            checkBox.isChecked = line.isEnabled
        }
        divider.visibility = if (isDividerEnabled) View.VISIBLE else View.GONE

        return view
    }

}