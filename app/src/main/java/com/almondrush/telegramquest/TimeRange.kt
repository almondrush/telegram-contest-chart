package com.almondrush.telegramquest

object TimeRange {
    const val MAX = 1000
    const val MIN = 0
    val FULL get() = MIN..MAX
    val MIN_LENGTH = 100
}