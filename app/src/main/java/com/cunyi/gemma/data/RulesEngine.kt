package com.cunyi.gemma.data

import com.cunyi.gemma.data.entity.HealthRecord

/**
 * Local rules engine for immediate health alerts.
 * Pure deterministic logic - no AI dependency, millisecond response.
 */
object RulesEngine {

    enum class AlertLevel { GREEN, YELLOW, RED }

    data class Alert(
        val level: AlertLevel,
        val message: String
    )

    fun evaluate(record: HealthRecord): Alert {
        return when (record.recordType) {
            "blood_pressure" -> evaluateBloodPressure(record)
            "glucose" -> evaluateGlucose(record)
            "heart_rate" -> evaluateHeartRate(record)
            else -> Alert(AlertLevel.GREEN, "")
        }
    }

    /**
     * Check if the last 3 records show a rising trend.
     */
    fun checkTrend(recentRecords: List<HealthRecord>): Alert? {
        if (recentRecords.size < 3) return null
        val last3 = recentRecords.take(3).map { it.value }
        // Records are DESC order, so reverse for chronological
        val chronological = last3.reversed()
        val isRising = chronological[0] < chronological[1] && chronological[1] < chronological[2]
        if (isRising) {
            return Alert(
                AlertLevel.YELLOW,
                "${recentRecords[0].recordType} 连续3次上升，请注意观察"
            )
        }
        return null
    }

    private fun evaluateBloodPressure(record: HealthRecord): Alert {
        val systolic = record.value
        val diastolic = record.secondaryValue ?: 0f

        return when {
            systolic >= 180 || diastolic >= 120 -> Alert(
                AlertLevel.RED,
                "血压严重偏高（${systolic.toInt()}/${diastolic.toInt()}），请立即休息并考虑就医"
            )
            systolic >= 160 || diastolic >= 100 -> Alert(
                AlertLevel.YELLOW,
                "血压偏高（${systolic.toInt()}/${diastolic.toInt()}），请休息后再测一次"
            )
            systolic < 90 || diastolic < 60 -> Alert(
                AlertLevel.YELLOW,
                "血压偏低（${systolic.toInt()}/${diastolic.toInt()}），注意头晕，慢慢活动"
            )
            else -> Alert(AlertLevel.GREEN, "血压正常")
        }
    }

    private fun evaluateGlucose(record: HealthRecord): Alert {
        val glucose = record.value
        return when {
            glucose >= 16.7f -> Alert(
                AlertLevel.RED,
                "血糖严重偏高（${glucose}mmol/L），请立即就医"
            )
            glucose >= 11.1f -> Alert(
                AlertLevel.YELLOW,
                "血糖偏高（${glucose}mmol/L），注意饮食，建议复查"
            )
            glucose < 3.9f -> Alert(
                AlertLevel.RED,
                "血糖偏低（${glucose}mmol/L），请立即吃点糖"
            )
            else -> Alert(AlertLevel.GREEN, "血糖正常")
        }
    }

    private fun evaluateHeartRate(record: HealthRecord): Alert {
        val hr = record.value
        return when {
            hr >= 150 -> Alert(AlertLevel.RED, "心率过快（${hr.toInt()}次/分），请立即休息")
            hr >= 100 -> Alert(AlertLevel.YELLOW, "心率偏快（${hr.toInt()}次/分），注意休息")
            hr < 50 -> Alert(AlertLevel.YELLOW, "心率偏慢（${hr.toInt()}次/分），如有不适请就医")
            else -> Alert(AlertLevel.GREEN, "心率正常")
        }
    }
}
