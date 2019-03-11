package com.almondrush.telegramquest

class ChartData(val timeData: Array<Long>, val lines: List<ChartLine>) {

    init {
        require(lines.all { line -> line.data.size == timeData.size }) {
            "Every line data array must have the same size as the time data array"
        }
    }

    fun forTimeRange(timeRange: IntRange) {
        require(timeRange.start >= 0 && timeRange.endInclusive < 100) { "Time range must be in 0 until 100" }


    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChartData

        if (!timeData.contentEquals(other.timeData)) return false
        if (lines != other.lines) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeData.contentHashCode()
        result = 31 * result + lines.hashCode()
        return result
    }
}