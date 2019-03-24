package com.almondrush

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Paint
import android.util.TypedValue
import android.view.View

fun Pair<Number, Number>.center(): Float {
    val a = first.toFloat()
    val b = second.toFloat()
    val min = Math.min(a, b)
    val max = Math.max(a, b)
    return min + (max - min) / 2
}

val IntRange.interval get() = endInclusive - start

val LongRange.interval get() = endInclusive - start

fun Number.dpToInt(context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)
}

fun View.setOnRippleClickListener(listener: () -> Unit) {
    setOnClickListener {
        handler.postDelayed(listener, 150)
    }
}

val Paint.textHeight get() = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading

fun createFloatAnimator(
    from: Float = 0F,
    to: Float = 1F,
    onUpdate: (value: Float) -> Unit,
    onComplete: ((isCancelled: Boolean) -> Unit)? = null
) = ValueAnimator.ofFloat(from, to).apply {
    addUpdateListener { onUpdate(it.animatedValue as Float) }
    addListener(object: Animator.AnimatorListener {
        private var isCancelled = false
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) {
            onComplete?.invoke(isCancelled)
            removeAllUpdateListeners()
        }
        override fun onAnimationCancel(animation: Animator?) {
            isCancelled = true
        }
        override fun onAnimationStart(animation: Animator?) {}
    })
}