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

    private var pathRect: Rect = Rect()

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5F
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        pathRect.set(0, measuredHeight, measuredWidth, 0)
    }

    override fun onDraw(canvas: Canvas) {
        measureTimeMillis {
            d("pathRect: $pathRect")

            val xValues = timeDataToPixelValues(data.timeData, pathRect)
            val yValuesList = linesDataToPixelValues(data.lines, pathRect)

            d(xValues.toList().toString())

            yValuesList.forEachIndexed { lineIndex, yValues ->
                path.rewind()
                paint.color = Color.parseColor(data.lines[lineIndex].color)
                path.moveTo(xValues[0], yValues[0])
                for (i in 1 until xValues.size) path.lineTo(xValues[i], yValues[i])
                path.moveTo(xValues[0], yValues[0])
                canvas.drawPath(path, paint)
            }

        }.let {
            Log.v("CHART_ESTIMATE", "Estimate draw: $it")
        }
    }

    private fun timeDataToPixelValues(timeData: Array<Long>, pathRect: Rect): Array<Float> {
        val startXPixel = pathRect.left
        val endXPixel = pathRect.right
        val xPixelCount = endXPixel - startXPixel
        val pixelsPerTime = xPixelCount.toFloat() / (timeData.last() - timeData.first()).toFloat()

        return Array(timeData.size) { i ->
            (timeData[i] - timeData[0]) * pixelsPerTime
        }
    }

    private fun linesDataToPixelValues(lines: List<ChartLine>, pathRect: Rect): List<Array<Float>> {
        val startYPixel = pathRect.bottom
        val endYPixel = pathRect.top
        val yPixelCount = endYPixel - startYPixel

        val (minYValue, maxYValue) = findMinAndMaxValues(lines)

        val pixelsPerValue = yPixelCount.toFloat() / (maxYValue - minYValue).toFloat()

        return lines.map { line ->
            Array(line.data.size) { i ->
                pathRect.top - ((line.data[i] - minYValue) * pixelsPerValue)
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

    fun d(t: String) =  Log.d("CHART_VIEW", t)

}