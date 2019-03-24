package com.almondrush.telegramquest.legend

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.almondrush.interval
import com.almondrush.telegramquest.BuildConfig
import com.almondrush.telegramquest.ChartUtil
import com.almondrush.telegramquest.L
import com.almondrush.telegramquest.XRange
import com.almondrush.textHeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.properties.Delegates

internal class ChartXAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        val MILLISECONDS_IN_A_DAY = TimeUnit.DAYS.toMillis(1)
        private const val DATE_PATTERN = "MMM d"
        private const val LABEL_TEXT_TO_MEASURE = "MMM 99"
    }

    private val dateFormat = SimpleDateFormat(DATE_PATTERN, context.resources.configuration.locale)

    private var chartPaddingLeft: Int = 0
    private var chartPaddingRight: Int = 0
    private val labelMargin = 15

    private val textMarginTop: Int = 0
    private val textSize = 20F
    private val textColor = Color.parseColor("#888888")
    private val textPaint = Paint()

    private var textLineHeight = 0F
    private var labelWidth = 0F
    private var textTop = 0F

    private val measureRect = Rect()
    private val drawingRect = Rect()
    private var xRange: IntRange = XRange.FULL
    private lateinit var fullTimeRange: LongRange
    private lateinit var timeRange: LongRange
    private var labelsCount: Float = 0F
    private lateinit var timeOfDaysToShow: List<Long>
    private var dayStep: Int by Delegates.observable(0) {_, oldValue, newValue ->
        if (oldValue != newValue) timeOfDaysToShow = findDays(fullTimeRange, dayStep)
    }

    init {
        textPaint.color = textColor
        textPaint.textSize = textSize
    }

    fun setFullTimeRange(timeRange: LongRange) {
        this.fullTimeRange = timeRange
        updateTimeRange()
    }

    fun setXRange(xRange: IntRange) {
        this.xRange = xRange
        updateTimeRange()
    }

    private fun updateTimeRange() {
        if (::fullTimeRange.isInitialized) {
            timeRange = ChartUtil.selectTimeRange(fullTimeRange, xRange)
            dayStep = calculateDayStep(labelsCount, timeRange)
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val fontMetrics = textPaint.fontMetrics
        textTop = -fontMetrics.top
        textLineHeight = textPaint.textHeight

        textPaint.getTextBounds(LABEL_TEXT_TO_MEASURE, 0, LABEL_TEXT_TO_MEASURE.length - 1, measureRect)

//        labelWidth = textPaint.measureText(LABEL_TEXT_TO_MEASURE)
        labelWidth = measureRect.width().toFloat()
        L.d(labelWidth)

        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            textLineHeight.roundToInt() + textMarginTop
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            getDrawingRect(drawingRect)
            drawingRect.set(
                drawingRect.left + chartPaddingLeft,
                drawingRect.top,
                drawingRect.right - chartPaddingRight,
                drawingRect.bottom
            )
            labelsCount = calculateLabelsCount(drawingRect.width(), labelMargin, labelWidth)
            updateTimeRange()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val pixelValues = convertTimeToPixelValues(timeOfDaysToShow, timeRange)
        val spaceBetweenLabels = getDistanceBetweenLabels(pixelValues)
        val alpha = Math.min(spaceBetweenLabels / (labelWidth + labelMargin), 1.0F)
        L.d("alpha $alpha")

        pixelValues.mapIndexed { index, value -> value to getDayString(timeOfDaysToShow[index]) }
            .forEachIndexed { index, (x, label) ->
                if (index % 2 != 0) {
                    textPaint.color = Color.argb(
                        (255 * alpha).toInt(),
                        Color.red(textColor),
                        Color.green(textColor),
                        Color.blue(textColor)
                    )
                } else {
                    textPaint.color = textColor
                }
                canvas.drawTextLabel(label, x.toFloat(), textTop + textMarginTop.toFloat(), textPaint)
            }
    }

    fun getDistanceBetweenLabels(xValues: List<Int>): Float {
        return if (xValues.size < 3) {
            labelWidth
        } else {
            L.d("left ${xValues[2]} right ${xValues[0]} lawidth $labelWidth")
            (xValues[2] - xValues[0].toFloat() - 2 * (labelWidth + labelMargin)) / 2
        }
    }

    internal fun setChartPadding(left: Int, right: Int) {
        chartPaddingLeft = left
        chartPaddingRight = right
        updateTimeRange()
    }

    private fun convertTimeToPixelValues(days: List<Long>, timeRange: LongRange): List<Int> {
        val pixelCount = drawingRect.width()
        val pixelPerTimeUnit = pixelCount / timeRange.interval.toFloat()
        return days.map { day -> ((day - this.timeRange.start) * pixelPerTimeUnit + chartPaddingLeft).toInt() }
    }

    private fun getDayString(time: Long) = dateFormat.format(Date(time))

    private fun findDays(fullTimeRange: LongRange, dayStep: Int): List<Long> {
        if (fullTimeRange.interval == 0L) return emptyList()
        var currentDay = findNextDay(fullTimeRange.start, fullTimeRange.endInclusive)
        val days = mutableListOf<Long>()
        while (currentDay != null) {
            days.add(currentDay)
            currentDay = findNextDay(currentDay + MILLISECONDS_IN_A_DAY * dayStep, fullTimeRange.endInclusive)
        }
        return days
    }

    private fun findNextDay(startTime: Long, endTime: Long) = ((startTime / MILLISECONDS_IN_A_DAY) * MILLISECONDS_IN_A_DAY)
        .takeIf { it <= endTime }

    private fun calculateDayStep(maxLabelsCount: Float, timeRange: LongRange): Int {
        if (maxLabelsCount < 1) return 0
        val daysInTimeRange = TimeUnit.MILLISECONDS.toDays(timeRange.interval)
        val days = daysInTimeRange / maxLabelsCount
        val powOf2 = Math.log10(days.toDouble()) / Math.log10(2.0)
        return Math.pow(2.0, Math.floor(powOf2)).toInt()
    }

    private fun calculateLabelsCount(availableSpace: Int, labelMargin: Int, textLabelWidth: Float): Float {
        return Math.floor((availableSpace / (textLabelWidth + labelMargin)).toDouble() / 2)
            .takeIf { it > 1.0 }?.toFloat() ?: 1.0F
    }

    private fun Canvas.drawTextLabel(text: String, x: Float, y: Float, paint: Paint) {
        drawText(text, x, y, paint)
        if (BuildConfig.DEBUG) {
            //Show precise day point
            val fontMetrics = paint.fontMetrics
            val lineHeight = -fontMetrics.top + fontMetrics.ascent
            drawLine(x, 0F, x, lineHeight, paint)
        }
    }
}

