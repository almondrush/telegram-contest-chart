package com.almondrush

fun Pair<Number, Number>.center(): Float {
    val a = first.toFloat()
    val b = second.toFloat()
    val min = Math.min(a, b)
    val max = Math.max(a, b)
    return min + (max - min) / 2
}