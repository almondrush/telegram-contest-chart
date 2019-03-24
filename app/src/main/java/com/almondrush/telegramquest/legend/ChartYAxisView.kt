package com.almondrush.telegramquest.legend

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.almondrush.telegramquest.R
import com.almondrush.textHeight
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class ChartYAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val ANIMATION_DURATION_MS = 300L
    }

    private var lineColor: Int = 0
    private var labelCount = 0
    private var lineWidth = 0F
    private var textColor = 0
    private var textSize = 0F
    private var textBottomMargin = 0F

    private val linePaint = Paint()
    private val textPaint = Paint()

    private val drawingRect = Rect()

    private var labelPositionsPx: List<Int> = emptyList()
    private var labelSeries = mutableListOf<LabelSeries>()

    private var maxY = 0L
    private var targetMaxY = 0L

    private val maxYAnimator = createFloatAnimator(onComplete = { isCancelled ->
        if (!isCancelled) labelSeries.removeAll { !it.isAppearing }
    })

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ChartYAxisView, defStyleAttr, defStyleRes).apply {
            try {
                lineColor = getColor(R.styleable.ChartYAxisView_chartLegendLineColor, 0)
                labelCount = getInteger(R.styleable.ChartYAxisView_chartLegendYLineCount, 6)
                lineWidth = getDimension(R.styleable.ChartYAxisView_chartLegendLineWidth, 1F)
                lineColor = getColor(R.styleable.ChartYAxisView_chartLegendLineColor, 0)
                textColor = getColor(R.styleable.ChartYAxisView_chartLegendLabelTextColor, 0)
                textSize = getDimension(R.styleable.ChartYAxisView_chartLegendLabelTextSize, 0F)
                textBottomMargin = getDimension(R.styleable.ChartYAxisView_chartLegendLineToLabelMargin, 0f)
            } finally {
                recycle()
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = lineColor
        linePaint.strokeWidth = lineWidth
        textPaint.color = textColor
        textPaint.textSize = textSize
    }

    fun setMaxY(newMaxY: Long) {
        if (newMaxY != targetMaxY) {
            maxYAnimator.cancel()
            targetMaxY = newMaxY
            val oldMaxY = maxY
            val distance = newMaxY - oldMaxY
            labelSeries.forEach {
                if (it.isAppearing) {
                    it.isAppearing = false
                    it.animatedValue = 1 - it.animatedValue
                }
            }
            labelSeries.add(createLabelSeries(newMaxY, labelPositionsPx))

            maxYAnimator.addUpdateListener { animator ->
                val animatedValue = animator.animatedValue as Float
                val animatedMaxY = (oldMaxY + distance * animatedValue).toLong()
                if (animatedMaxY != maxY) {
                    labelSeries.forEach { series ->
                        series.animatedValue = Math.max(series.animatedValue, animatedValue)
                    }
                    maxY = animatedMaxY
                    invalidate()
                }
            }
            maxYAnimator.start()
        }

        invalidate()
    }

    private fun createLabelSeries(maxY: Long, labelPositionsPx: List<Int>): LabelSeries {
        return LabelSeries(labelPositionsPx.map { pixelToRealValue(it, maxY) }, maxY, true, 0F)
    }

    private fun updateLabelSeries(labelSeries: LabelSeries, labelPositionsPx: List<Int>) {
        labelSeries.labels = labelPositionsPx.map { pixelToRealValue(it, labelSeries.maxY) }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val labelHeight = (textPaint.textHeight + textBottomMargin).roundToInt()
        getDrawingRect(drawingRect)
        labelPositionsPx = calculateLabelPositionsPx(labelCount, drawingRect.height(), labelHeight)
        labelSeries.forEach { updateLabelSeries(it, labelPositionsPx) }
    }

    private fun calculateLabelPositionsPx(labelCount: Int, availableHeight: Int, labelHeight: Int): List<Int> {
        val distanceBetweenFirstAndLastLabelBottom = availableHeight - labelHeight
        val labelHeightWithMargin = distanceBetweenFirstAndLastLabelBottom / (labelCount - 1)

        return (0 until labelCount)
            .map { index -> labelHeightWithMargin * index }
    }

    override fun onDraw(canvas: Canvas) {
        labelSeries.forEach { series ->
            val animMultiplier = if (series.isAppearing) series.animatedValue else 1 - series.animatedValue

            linePaint.color = Color.argb(
                (255 * animMultiplier).toInt(),
                Color.red(lineColor),
                Color.green(lineColor),
                Color.blue(lineColor)
            )
            textPaint.color = Color.argb(
                (255 * animMultiplier).toInt(),
                Color.red(textColor),
                Color.green(textColor),
                Color.blue(textColor)
            )
            series.labels.forEach { label ->
                canvas.drawValueWithLine(realValueToPixel(label, maxY), label.toString(), linePaint, textPaint)
            }
        }
    }

    private fun pixelToRealValue(yPixel: Int, maxY: Long): Long {
        return ((yPixel.toFloat() / drawingRect.height()) * maxY).roundToLong()
    }

    private fun realValueToPixel(yValue: Long, maxY: Long): Float {
        val pixelValue = drawingRect.height() - (yValue.toFloat() / maxY) * drawingRect.height()
        return Math.round(pixelValue).toFloat()
    }

    private fun Canvas.drawValueWithLine(y: Float, value: String, linePaint: Paint, textPaint: Paint) {
        drawHorizontalLine(y, linePaint)
        drawValue(y - textBottomMargin, value, textPaint)
    }

    private fun Canvas.drawValue(y: Float, value: String, paint: Paint) {
        drawText(value, 0F, y, paint)
    }

    private fun Canvas.drawHorizontalLine(y: Float, paint: Paint) {
        drawLine(0F, y - 1, width.toFloat(), y - 1, paint)
    }

    private data class LabelSeries(
        var labels: List<Long>,
        val maxY: Long,
        var isAppearing: Boolean,
        var animatedValue: Float
    )

    private fun createFloatAnimator(onComplete: ((cancelled: Boolean) -> Unit)? = null) =
        ValueAnimator.ofFloat(0F, 1F).apply {
            duration = ANIMATION_DURATION_MS
            addListener(object : Animator.AnimatorListener {
                var isCancelled = false
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    removeAllUpdateListeners()
                    onComplete?.invoke(isCancelled)
                }

                override fun onAnimationCancel(animation: Animator?) {
                    isCancelled = true
                    removeAllUpdateListeners()
                }

                override fun onAnimationStart(animation: Animator?) {}
            })
        }
}
