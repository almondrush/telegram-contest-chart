package com.almondrush.telegramquest

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.almondrush.telegramquest.dto.Line
import kotlinx.android.parcel.Parcelize

class MainActivity : AppCompatActivity() {

    companion object {
        private const val STATE = "saved_state"
    }

    private lateinit var state: State

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        state = if (savedInstanceState != null) {
            savedInstanceState.classLoader = this.classLoader
            savedInstanceState.getParcelable(STATE) as State
        } else {
            State(DataParser.getChartsFromAssets(this))
        }
        fillChartList()
    }

    private fun fillChartList() {
        val chartList = findViewById<ViewGroup>(R.id.activity_main_chart_list)
        state.charts.forEachIndexed { index, chart ->
            LayoutInflater.from(this).inflate(R.layout.chart_item, chartList, false).let { view ->
                val number = (index + 1).toString()
                view.findViewById<TextView>(R.id.chart_item_number).text = number
                view.setOnClickListener { onChartClick(chart) }
                chartList.addView(view)
            }
        }
    }

    private fun onChartClick(chart: List<Line>) {
        val intent = Intent(this, ChartActivity::class.java)
        val extras = Bundle().apply {
            putParcelableArrayList(ChartActivity.CHART_DATA_EXTRA, ArrayList(chart))
        }
        intent.putExtras(extras)
        startActivity(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE, state)
    }

    @Parcelize
    private data class State(var charts: List<List<Line>>) : Parcelable
}