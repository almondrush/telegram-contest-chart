package com.almondrush.telegramquest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToLong

class ChartValueAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val lineWidth = 1
    private val lineColor = Color.rgb(200, 200, 200)
    private val linePaint = Paint()

    private val textColor = Color.rgb(130, 130, 130)
    private val textPaint = Paint()
    private val textSize = 32F
    private val textBottomMargin = 12

    private val drawingRect = Rect()

    private val nonZeroLineCount = 5

    private var maxY = 0L

    init {
        linePaint.style = Paint.Style.STROKE
        linePaint.color = lineColor
        textPaint.color = textColor
        textPaint.textSize = textSize
    }

    fun setMaxY(maxY: Long) {
        this.maxY= maxY
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        getDrawingRect(drawingRect)
    }

    private fun calculateValuesDistance(lineCount: Int, valueHeight: Float, canvasHeight: Int): Float {
        // Space between top value bottom and drawing rect bottom
        val availableHeight = canvasHeight - valueHeight

        val availableSpace = availableHeight - valueHeight * lineCount
        return availableSpace / lineCount - 1
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val zeroLineOffset = (lineWidth / 2).takeIf { it > 1 } ?: 1
        canvas.apply {
            val valueHeight = textBottomMargin + textSize
            val valuesPadding = calculateValuesDistance(nonZeroLineCount, valueHeight, drawingRect.height())
            val zeroLineY = drawingRect.height().toFloat() - zeroLineOffset
            drawValueWithLine(zeroLineY, "0")

            for (i in 1..nonZeroLineCount) {
                val y = zeroLineY - i * (valueHeight + valuesPadding)
                drawValueWithLine(y, getRealYValueByPixel(y).toString())
            }
        }
    }

    private fun getRealYValueByPixel(yPixel: Float): Long {
        val realValue = maxY * (drawingRect.height() - yPixel) / drawingRect.height()
        return realValue.roundToLong()
    }

    private fun Canvas.drawValueWithLine(y: Float, value: String) {
        drawHorizontalLine(y, linePaint)
        drawValue(y - textBottomMargin, value, textPaint)
    }

    private fun Canvas.drawValue(y: Float, value: String, paint: Paint) {
        drawText(value, 0F, y, paint)
    }

    private fun Canvas.drawHorizontalLine(y: Float, paint: Paint) {
        drawLine(0F, y, width.toFloat(), y, paint)
    }
}
