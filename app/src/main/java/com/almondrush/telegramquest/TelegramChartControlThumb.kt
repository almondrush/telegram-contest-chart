package com.almondrush.telegramquest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt
import kotlin.properties.Delegates

internal class TelegramChartControlThumb @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var thumbColor = Color.argb(128, 0, 0, 255)
    private var fogColor = Color.argb(64, 0, 0, 255)

    private val paint = Paint()
    private val path = Path()

    internal var timeRangeUpdatedListener: TelegramChartControlView.TimeRangeUpdatedListener? = null

    private var timeRange by Delegates.observable(300..500) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            updateTimeRangePx()
            timeRangeUpdatedListener?.onTimeRangeUpdated(newValue)
            invalidate()
        }
    }

    private lateinit var timeRangePixels: IntRange

    private var pixelsPerTimeRange: Float = 0F

    private var frameWidth = 4
    private var thumbWidth = 16

    private var drawingRect = RectF()
    private var outerRect = RectF()
    private var innerRect = RectF()

    private var tracking = Trackable.NOTHING

    private fun setTimeRange(start: Int = timeRange.start, end: Int = timeRange.endInclusive) {
        timeRange = start..end
    }

    private fun updateTimeRangePx() {
        if (pixelsPerTimeRange == 0F) return
        val start = (timeRange.first * pixelsPerTimeRange).roundToInt()
        val end = (timeRange.endInclusive * pixelsPerTimeRange).roundToInt()
        timeRangePixels = start..end
    }

    private fun setLeftThumbTo(x: Float) {
        val positionPx = Math.max(x - thumbWidth / 2, drawingRect.left)
        val timeValue = positionPx / pixelsPerTimeRange
        setTimeRange(start = timeValue.toInt())
    }

    private fun setRightThumbTo(x: Float) {
        val positionPx = Math.min(x + thumbWidth / 2, drawingRect.right)
        val timeValue = positionPx / pixelsPerTimeRange
        setTimeRange(end = timeValue.toInt())
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        drawingRect.set(0F, 0F, measuredWidth.toFloat(), measuredHeight.toFloat())

        pixelsPerTimeRange = measuredWidth.toFloat() / TimeRange.MAX
        updateTimeRangePx()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                onActionDown(event.x)
                true
            }
            MotionEvent.ACTION_MOVE ->{
                onActionMove(event.x)
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun onActionDown(x: Float) {
        val leftThumbStart = timeRangePixels.start
        val leftThumbEnd = timeRangePixels.start + thumbWidth
        val rightThumbStart = timeRangePixels.endInclusive - thumbWidth
        val rightThumbEnd = timeRangePixels.endInclusive

        L.d(x)
        L.d("$leftThumbStart..$leftThumbEnd $rightThumbStart..$rightThumbEnd")

        tracking = when {
            x < leftThumbStart -> {
                setLeftThumbTo(x)
                Trackable.LEFT_THUMB
            }
            x <= leftThumbEnd -> Trackable.LEFT_THUMB
            x < rightThumbStart -> Trackable.FRAME
            x <= rightThumbEnd -> Trackable.RIGHT_THUMB
            else -> {
                setRightThumbTo(x)
                Trackable.RIGHT_THUMB
            }
        }
        L.d(tracking)
    }

    private fun onActionMove(x: Float) {
        L.d(x)
        L.d(tracking)
        when (tracking) {
            Trackable.LEFT_THUMB -> setLeftThumbTo(x)
            Trackable.RIGHT_THUMB -> setRightThumbTo(x)
        }
    }

    override fun onDraw(canvas: Canvas) {
        outerRect.set(
            drawingRect.width() * timeRange.start / TimeRange.MAX,
            0F,
            drawingRect.width() * timeRange.endInclusive / TimeRange.MAX,
            drawingRect.bottom
        )
        innerRect.set(
            outerRect.left + thumbWidth,
            outerRect.top + frameWidth,
            outerRect.right - thumbWidth,
            outerRect.bottom - frameWidth
        )

        // Draw frame
        paint.color = thumbColor
        paint.style = Paint.Style.FILL
        path.rewind()
        path.moveTo(outerRect.left, outerRect.top)
        path.addRect(outerRect, Path.Direction.CW)
        path.moveTo(innerRect.left, innerRect.top)
        path.addRect(innerRect, Path.Direction.CCW)
        canvas.drawPath(path, paint)

        // Draw left and right fog
        paint.color = fogColor
        canvas.drawRect(drawingRect.left, drawingRect.top, outerRect.left, drawingRect.bottom, paint)
        canvas.drawRect(outerRect.right, drawingRect.top, drawingRect.right, drawingRect.bottom, paint)

    }

    enum class Trackable {
        NOTHING, LEFT_THUMB, RIGHT_THUMB, FRAME
    }

}

fun Pair<Number, Number>.center(): Float {
    val a = first.toFloat()
    val b = second.toFloat()
    val min = Math.min(a, b)
    val max = Math.max(a, b)
    return min + (max - min) / 2
}