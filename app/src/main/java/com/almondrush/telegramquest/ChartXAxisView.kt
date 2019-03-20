package com.almondrush.telegramquest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.almondrush.interval
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

internal class ChartXAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        val MILLISECONDS_IN_A_DAY = TimeUnit.DAYS.toMillis(1)
        private const val DATE_PATTERN = "MMM d"
        private const val LABEL_TEXT_TO_MEASURE = "WWW 99"
    }

    private val dateFormat = SimpleDateFormat(DATE_PATTERN, context.resources.configuration.locale)

    private var chartPaddingLeft: Int = 0
    private var chartPaddingRight: Int = 0
    private val labelMargin = 64

    private val textMarginTop: Int = 0
    private val textSize = 24F
    private val textColor = Color.parseColor("#888888")
    private val textPaint = Paint()

    private var textLineHeight = 0F
    private var textLabelWidth = 0F
    private var textTop = 0F

    private val drawingRect = Rect()
    private lateinit var fullTimeRange: LongRange
    private lateinit var timeRange: LongRange
    private var xRange: IntRange = XRange.FULL

    init {
        textPaint.color = textColor
        textPaint.textSize = textSize
    }

    fun setChartPadding(left: Int, right: Int) {
        chartPaddingLeft = left
        chartPaddingRight = right
    }

    fun setTimeRange(timeRange: LongRange) {
        this.fullTimeRange = timeRange
        updateTimeRange()
    }

    fun setXRange(xRange: IntRange) {
        this.xRange = xRange
        updateTimeRange()
    }

    fun updateTimeRange() {
        val timeInXRangeUnit = fullTimeRange.interval / XRange.MAX
        timeRange = (xRange.start * timeInXRangeUnit)..(xRange.endInclusive * timeInXRangeUnit)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val fontMetrics = textPaint.fontMetrics
        textTop = -fontMetrics.top
        textLineHeight = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading
        textLabelWidth = textPaint.measureText(LABEL_TEXT_TO_MEASURE)
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            textLineHeight.roundToInt() + textMarginTop
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        getDrawingRect(drawingRect)
        drawingRect.set(
            drawingRect.left + chartPaddingLeft,
            drawingRect.top,
            drawingRect.right - chartPaddingRight,
            drawingRect.bottom
        )
    }

    override fun onDraw(canvas: Canvas) {
        val labelsCount = calculateLabelsCount(width, labelMargin, textLabelWidth)
        val dayStep = calculateDayStep(labelsCount, timeRange)
        val days = findDays(timeRange, dayStep)
        val pixelValues = convertTimeToPixelValues(days, timeRange)
        pixelValues
            .mapIndexed { index, value -> value to getDayString(days[index]) }
            .forEach { (x, label) ->
                canvas.drawTextAlignCenter(label, x.toFloat(), textTop + textMarginTop.toFloat(), textPaint)
            }
    }

    fun convertTimeToPixelValues(days: List<Long>, timeRange: LongRange): List<Int> {
        val pixelCount = drawingRect.width()
        val pixelPerTimeUnit = pixelCount / timeRange.interval.toFloat()
        return days.map { day -> ((day - this.timeRange.start) * pixelPerTimeUnit + chartPaddingLeft).toInt() }
    }

    fun getDayString(time: Long) = dateFormat.format(Date(time))

    fun findDays(timeRange: LongRange, dayStep: Int): MutableList<Long> {
        var currentDay = findNextDay(timeRange.start, timeRange.endInclusive)
        val days = mutableListOf<Long>()
        while (currentDay != null) {
            days.add(currentDay)
            currentDay = findNextDay(currentDay + MILLISECONDS_IN_A_DAY * dayStep, timeRange.endInclusive)
        }
        return days
    }

    fun findNextDay(startTime: Long, endTime: Long) = ((startTime / MILLISECONDS_IN_A_DAY) * MILLISECONDS_IN_A_DAY)
        .takeIf { it <= endTime }

    fun calculateDayStep(maxLabelsCount: Int, timeRange: LongRange): Int {
        val daysInTimeRange = TimeUnit.MILLISECONDS.toDays(timeRange.interval)
        val days = Math.ceil((daysInTimeRange / maxLabelsCount).toDouble()).toInt()
        val powOf2 = Math.log10(days.toDouble()) / Math.log10(2.0)
        val result = Math.pow(2.0, Math.floor(powOf2)).toInt()
        return result
    }

    private fun calculateLabelsCount(availableSpace: Int, labelMargin: Int, textLabelWidth: Float): Int {
        return Math.floor((availableSpace / (textLabelWidth + labelMargin)).toDouble()).toInt()
            .takeIf { it > 0 } ?: 1
    }

    private fun Canvas.drawTextAlignCenter(text: String, x: Float, y: Float, paint: Paint) {
        val measuredSize = paint.measureText(text)
        drawText(text, x - measuredSize / 2, y, paint)
    }
}

