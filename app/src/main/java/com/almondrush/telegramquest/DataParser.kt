package com.almondrush.telegramquest

import android.content.Context
import android.graphics.Color
import com.almondrush.telegramquest.dto.Line
import com.almondrush.telegramquest.dto.PointL
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedInputStream

object DataParser {
    private const val COLUMNS = "columns"
    private const val TYPES = "types"
    private const val NAMES = "names"
    private const val COLORS = "colors"

    private const val TYPE_X = "x"
    private const val TYPE_LINE = "line"

    fun getChartsFromAssets(context: Context): List<List<Line>> {
        var parsedCharts: (List<List<Line>>)? = null
        context.resources.assets.open("source.json").let { BufferedInputStream(it) }.bufferedReader().use {
            val array = JSONTokener(it.readText()).nextValue() as JSONArray

            val charts = mutableListOf<JSONObject>()
            for (i in 0 until array.length()) {
                charts.add(array.getJSONObject(i))
            }

            parsedCharts = charts.map { chart ->
                val types = chart.getJSONObject(TYPES)
                types.keys().asSequence().groupBy { key -> types.getString(key) }.let { series ->
                    val xSeries = requireNotNull(series[TYPE_X])
                        .let { list ->
                            require(list.size == 1)
                            list.first()
                        }
                        .let { key -> parseX(key, chart) }
                    requireNotNull(series[TYPE_LINE]).map { key ->
                        parseLine(key, xSeries, chart)
                    }
                }
            }
        }
        return requireNotNull(parsedCharts)
    }

    private fun parseX(key: String, chart: JSONObject): List<Long> {
        val column = findColumn(key, chart)
        L.d(column.length())
        val xSeries = ArrayList<Long>(column.length() - 1)

        for (i in 1 until column.length()) {
            xSeries.add(column.getLong(i))
        }

        return xSeries
    }

    private fun parseLine(key: String, xSeries: List<Long>, chart: JSONObject): Line {
        val name = chart.getJSONObject(NAMES).getString(key)
        val color = chart.getJSONObject(COLORS).getString(key)

        val column = findColumn(key, chart)

        check(column.length() - 1 == xSeries.size) {
            "Column length=${column.length()} != xSeries length=${xSeries.size})"
        }

        return Line(
            Color.parseColor(color),
            name,
            xSeries.mapIndexed { index, x -> PointL(x, column.getLong(index + 1)) }
        )
    }

    private fun findColumn(key: String, chart: JSONObject): JSONArray {
        val columns = chart.getJSONArray(COLUMNS)
        for (i in 0 until columns.length()) {
            val column = columns.getJSONArray(i)
            val columnKey = column.getString(0)
            if (columnKey == key) return column
        }

        throw IllegalArgumentException("column for key=$key is not found")
    }
}