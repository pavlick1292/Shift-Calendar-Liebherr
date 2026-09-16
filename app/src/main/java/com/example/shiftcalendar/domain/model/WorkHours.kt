package com.example.shiftcalendar.domain.model

data class WorkHours(
    val personId: Long,
    val year: Int,
    val regularHours: Double,
    val nightHours: Double,
    val roadHours: Double,
    val manualAdjustment: Double = 0.0,
    val yearlyNorm: Double = 1972.0
) {
    val total: Double get() = regularHours + nightHours + roadHours + manualAdjustment

    val overtime: Double get() = (total - yearlyNorm).coerceAtLeast(0.0)

    val normProgress: Float get() =
        if (yearlyNorm > 0) (total / yearlyNorm).toFloat() else 0f
}
