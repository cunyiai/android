package com.brzhang.gemma_ai.service

import android.content.Context
import androidx.work.*
import com.brzhang.gemma_ai.ai.GemmaManager
import com.brzhang.gemma_ai.ai.PromptTemplates
import com.brzhang.gemma_ai.data.db.AppDatabase
import com.brzhang.gemma_ai.data.entity.UserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.TimeUnit

/**
 * Weekly health report generator using WorkManager.
 * Queries last 7 days of health records, sends to Gemma for summary.
 */
class WeeklyReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val gemma = GemmaManager()

        return try {
            gemma.initialize(applicationContext)
            if (!gemma.isReady()) return Result.retry()

            val profile = db.userProfileDao().getProfile() ?: return Result.success()
            val now = System.currentTimeMillis()
            val weekAgo = now - 7 * 24 * 60 * 60 * 1000L

            val records = db.healthRecordDao().getByDateRange(weekAgo, now)
            if (records.isEmpty()) return Result.success()

            val conditions: List<String> = try {
                Gson().fromJson(profile.chronicConditions, object : TypeToken<List<String>>() {}.type)
            } catch (e: Exception) { emptyList() }

            val summary = records.groupBy { it.recordType }.entries.joinToString("\n") { (type, recs) ->
                val typeName = when (type) {
                    "blood_pressure" -> "血压"
                    "glucose" -> "血糖"
                    "heart_rate" -> "心率"
                    else -> type
                }
                val values = recs.takeLast(7).joinToString(", ") { "${it.value}" }
                "$typeName: $values ${recs.firstOrNull()?.unit ?: ""}"
            }

            val prompt = PromptTemplates.buildWeeklyReportPrompt(summary, profile.age, conditions)
            val report = gemma.inferText(prompt)

            // Save as a special health record
            db.healthRecordDao().insert(
                com.brzhang.gemma_ai.data.entity.HealthRecord(
                    recordType = "weekly_report",
                    value = 0f,
                    unit = "",
                    aiSuggestion = report
                )
            )

            gemma.close()
            Result.success()
        } catch (e: Exception) {
            gemma.close()
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "weekly_health_report"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WeeklyReportWorker>(
                7, TimeUnit.DAYS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
