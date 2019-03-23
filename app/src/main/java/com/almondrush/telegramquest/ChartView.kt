package com.almondrush.telegramquest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.almondrush.telegramquest.dto.Line
import com.almondrush.telegramquest.dto.LinePx
import kotlin.system.measureTimeMillis

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val paint = Paint()
    private val path = Path()
    private val pathRect: Rect = Rect()

    private var lineWidth: Float = 5F

    private var shouldUpdate: Boolean = true

    private var xRange: IntRange = XRange.FULL
    private var lines: List<Line> = emptyList()
    private var maxY = 0L

    private var linesPx: List<LinePx> = emptyList()

    init {
        initPaint()
    }

    fun setLines(lines: List<Line>) {
        this.lines = lines
        shouldUpdate = true
        invalidate()
    }

    fun setMaxY(y: Long) {
        maxY = y
        shouldUpdate = true
        invalidate()
    }

    fun setXRange(range: IntRange) {
        xRange = range
        shouldUpdate = true
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        pathRect.set(0, measuredHeight, measuredWidth, 0)
        // Stroke width padding
        val strokePadding = (lineWidth / 2).toInt()
        pathRect.set(
            pathRect.left + strokePadding,
            pathRect.top - strokePadding,
            pathRect.right - strokePadding,
            pathRect.bottom + strokePadding
        )
        shouldUpdate = true
    }

    override fun onDraw(canvas: Canvas) {
        measureTimeMillis {
            if (shouldUpdate) {
                linesPx = ChartUtil.calculatePixelValues(lines, xRange, maxY, pathRect)
                shouldUpdate = false
            }

            linesPx.forEach { line ->
                path.rewind()
                paint.color = line.color
                line.data.forEachIndexed { index, point ->
                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                canvas.drawPath(path, paint)
            }

        }.let {
            Log.v("CHART_ESTIMATE", "Estimate draw: $it")
        }
    }

    private fun initPaint() {
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = lineWidth
            isAntiAlias = true
        }
    }
}
