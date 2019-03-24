package com.almondrush.telegramquest

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.almondrush.telegramquest.dto.Line
import com.almondrush.telegramquest.dto.LinePx
import com.almondrush.telegramquest.dto.PointL
import kotlin.system.measureTimeMillis

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val ANIMATION_DURATION_MS = 300L
    }

    private val paint = Paint()
    private val path = Path()
    private val pathRect: Rect = Rect()

    private var lineWidth: Float = 5F

    private var xRange: IntRange = XRange.FULL

    private var maxY = 0L
    private var targetMaxY = 0L
    private val maxYAnimator = createFloatAnimator()

    private var animatingLines: MutableList<AnimatingLine> = mutableListOf()
    private var animatingLinesPx: List<AnimatingLinePx> = emptyList()
    private val linesAnimator = createFloatAnimator(onComplete = {
        animatingLines.removeAll { !it.isAppearing }
    })

    init {
        initPaint()
    }

    fun setXRange(range: IntRange) {
        xRange = range
        invalidate()
    }

    fun setLines(newLines: List<Line>) {
        val oldLines = animatingLines.map { it.line }
        val appearing = newLines.minus(oldLines)
        val disappearing = oldLines.minus(newLines)

        if (appearing.isNotEmpty() || disappearing.isNotEmpty()) {

            appearing.forEach { line ->
                animatingLines.find { it.line == line }
                    ?.let { it.isAppearing = true }
                    ?: animatingLines.add(AnimatingLine(line, true, 0F))
            }

            disappearing.forEach { line ->
                animatingLines.find { it.line == line }
                    ?.let {
                        if (it.isAppearing) {
                            it.isAppearing = false
                            it.value = 1 - it.value
                        }
                    }
                    ?: animatingLines.add(AnimatingLine(line, false, 0F))
            }

            linesAnimator.cancel()
            linesAnimator.addUpdateListener { animator ->
                val animatedValue = animator.animatedValue as Float
                animatingLines.forEach {
                    if (it.value <= animatedValue) {
                        it.value = animatedValue
                        invalidate()
                    }
                }
            }
            linesAnimator.start()
        }
    }

    fun setMaxY(newMaxY: Long) {
        if (targetMaxY != newMaxY) {
            maxYAnimator.cancel()
            targetMaxY = newMaxY
            val oldMaxY = maxY
            val distance = newMaxY - maxY
            maxYAnimator.addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                val animatedMaxY = (oldMaxY + distance * value).toLong()
                if (animatedMaxY != maxY) {
                    maxY = animatedMaxY
                    invalidate()
                }
            }
            maxYAnimator.start()
        }
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
    }

    override fun onDraw(canvas: Canvas) {
        measureTimeMillis {
            animatingLinesPx = animatingLines.map {
                AnimatingLinePx(calculatePixelValues(it.line, xRange, maxY, pathRect), it.isAppearing, it.value)
            }

            animatingLinesPx.forEach { animatingLine ->
                path.rewind()
                val animMultiplier = if (animatingLine.appearing) animatingLine.value else 1 - animatingLine.value
                val color = animatingLine.line.color
                paint.color =
                    Color.argb((255 * animMultiplier).toInt(), Color.red(color), Color.green(color), Color.blue(color))
                animatingLine.line.data.forEachIndexed { index, point ->
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

    private fun calculatePixelValues(line: Line, xRange: IntRange, maxY: Long, pathRect: Rect): LinePx {
        val rangedValues = ChartUtil.fitPointsIntoRange(line.data, xRange)

        val xStart = line.data.first().x
        val xEnd = line.data.last().x

        val xInterval = xEnd - xStart

        val xRangeUnitValue = xInterval / XRange.MAX

        val timeRangeStart = xStart + xRange.start * xRangeUnitValue
        val timeRangeEnd = xStart + xRange.endInclusive * xRangeUnitValue

        val timeRange = timeRangeStart..timeRangeEnd

        return LinePx(line.color, convertToPixelValues(timeRange, rangedValues, maxY, pathRect))
    }

    private fun convertToPixelValues(
        timeRangeMs: LongRange,
        points: List<PointL>,
        yMax: Long,
        pathRect: Rect
    ): List<PointF> {
        val pixelsPerXValue =
            (pathRect.right - pathRect.left).toFloat() / (timeRangeMs.endInclusive - timeRangeMs.start)
        val pixelsPerYValue = (pathRect.top - pathRect.bottom).toFloat() / yMax

        return points.map { point ->
            val time = (point.x - timeRangeMs.start) * pixelsPerXValue
            val value = pathRect.top - (pathRect.bottom + point.y * pixelsPerYValue)
            PointF(time, value)
        }
    }

    private fun createFloatAnimator(onComplete: (() -> Unit)? = null) =
        ValueAnimator.ofFloat(0F, 1F).apply {
        duration = ANIMATION_DURATION_MS
        addListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                removeAllUpdateListeners()
                onComplete?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) { removeAllUpdateListeners() }

            override fun onAnimationStart(animation: Animator?) {}
        })
    }

    private class AnimatingLine(val line: Line, var isAppearing: Boolean, var value: Float)
    private class AnimatingLinePx(val line: LinePx, var appearing: Boolean, var value: Float)
}
