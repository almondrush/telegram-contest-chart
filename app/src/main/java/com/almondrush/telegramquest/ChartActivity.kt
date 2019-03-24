package com.almondrush.telegramquest

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Parcelable
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
import kotlinx.android.parcel.Parcelize

class ChartActivity : AppCompatActivity() {

    companion object {
        private const val STATE = "saved_state"
        const val CHART_DATA_EXTRA = "chart_data"
    }

    private lateinit var state: State

    private lateinit var chartView: ChartWithLegendView
    private lateinit var controlView: ChartControlView
    private lateinit var chartLabels: ViewGroup


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        state = if (savedInstanceState != null) {
            savedInstanceState.classLoader = this.classLoader
            savedInstanceState.getParcelable(STATE) as State
        } else {
            L.d(intent.extras)
            val extras = requireNotNull(intent.extras)
            extras.classLoader = this.classLoader
            val lines = extras.getParcelableArrayList<Line>(CHART_DATA_EXTRA)!!
            State(XRange.MIN, XRange.MAX, lines.associateWith { true }.toMutableMap())
        }

        chartView = findViewById(R.id.chart_view)
        controlView = findViewById(R.id.chart_control)
        chartLabels = findViewById(R.id.chart_line_names_layout)

        controlView.setListener(object: ChartControlView.XRangeUpdatedListener {
            override fun onXRangeUpdated(xRange: IntRange) {
                state.xRangeStart = xRange.start
                state.xRangeEnd = xRange.endInclusive
                chartView.setXRange(xRange)
            }
        })

        setLines(state.lines.keys.toList())

        val xRange = state.xRangeStart..state.xRangeEnd
        controlView.setXRange(xRange)
    }

    private fun setLines(lines: List<Line>) {
        createLabels(lines)
        updateChild()
    }

    private fun updateChild() {
        val lines = state.lines.mapNotNull { (line, isEnabled) -> if (isEnabled) line else null }
        chartView.setLines(lines)
        controlView.setLines(lines)
    }

    private fun onLabelClick(line: Line) {
        val isEnabled = state.lines[line]!!
        state.lines[line] = !isEnabled
        updateChild()
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

        checkBox.isChecked = state.lines[line]!!

        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val colors = intArrayOf(line.color, line.color)
        checkBox.buttonTintList = ColorStateList(states, colors)
        textView.text = line.name
        view.setOnRippleClickListener {
            onLabelClick(line)
            checkBox.isChecked = state.lines[line]!!
        }
        divider.visibility = if (isDividerEnabled) View.VISIBLE else View.GONE

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE, state)
    }

    @Parcelize
    private data class State(
        var xRangeStart: Int,
        var xRangeEnd: Int,
        var lines: MutableMap<Line, Boolean>
    ) : Parcelable
}