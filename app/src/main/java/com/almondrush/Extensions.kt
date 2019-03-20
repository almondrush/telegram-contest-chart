package com.almondrush

import android.content.Context
import android.util.TypedValue

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

fun Number.ceil() = Math.ceil(toDouble()).toInt()

fun Number.floor() = Math.floor(toDouble()).toInt()