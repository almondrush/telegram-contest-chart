package com.almondrush.telegramquest

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.system.measureTimeMillis

class TelegramChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private val paint = Paint()
    private val path = Path()

    private val data = testData
    private val timeRange: IntRange = 0 until 100

    private var pathRect = Rect(0, 0, width, height)

    init {
        paint.setARGB(255, 255, 0, 0)
        paint.strokeWidth = 5F
    }

    override fun onDraw(canvas: Canvas) {
        measureTimeMillis {
            val xValues = timeDataToPixelValues(data.timeData, pathRect)
            val yValuesList = linesDataToPixelValues(data.lines, pathRect)

            yValuesList.forEachIndexed { lineIndex, yValues ->
                path.rewind()
                paint.color = Color.parseColor(data.lines[lineIndex].color)
                path.moveTo(xValues[0], yValues[0])
                for (i in 1 until xValues.size) path.lineTo(xValues[i], yValues[i])
                canvas.drawPath(path, paint)
            }

        }.let {
            Log.v("ESTIMATE", "Estimate draw: $it")
        }
    }

    private fun timeDataToPixelValues(timeData: Array<Long>, pathRect: Rect): Array<Float> {
        val startXPixel = pathRect.left
        val endXPixel = pathRect.right
        val xPixelCount = endXPixel - startXPixel
        val pixelsPerTime = xPixelCount.toFloat() / (timeRange.endInclusive - timeRange.start).toFloat()

        return Array(timeData.size) { i ->
            (timeData[i] - timeData[0]) * pixelsPerTime
        }
    }

    private fun linesDataToPixelValues(lines: List<ChartLine>, pathRect: Rect): List<Array<Float>> {
        val startYPixel = pathRect.left
        val endYPixel = pathRect.right
        val yPixelCount = endYPixel - startYPixel

        val (minYValue, maxYValue) = findMinAndMaxValues(lines)

        val pixelsPerValue = yPixelCount.toFloat() / (maxYValue - minYValue).toFloat()

        return lines.map { line ->
            Array(line.data.size) { i ->
                (line.data[i] - minYValue) * pixelsPerValue
            }
        }
    }

    private fun findMinAndMaxValues(lines: List<ChartLine>): Pair<Long, Long> = lines.map { line ->
        var lineMin = line.data.first()
        var lineMax = line.data.first()
        line.data.forEach {
            if (it < lineMin) lineMin = it
            if (it > lineMax) lineMax = it
        }
        lineMin to lineMax
    }.reduce { (oldMin, oldMax), (currentMin, currentMax) ->
        Math.min(oldMin, currentMin) to Math.max(oldMax, currentMax)
    }

}