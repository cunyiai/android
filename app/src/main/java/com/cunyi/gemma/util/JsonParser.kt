package com.cunyi.gemma.util

import com.cunyi.gemma.data.model.DiagnosisResult
import com.cunyi.gemma.data.model.MedicineResult
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName

/**
 * Fault-tolerant parser for Gemma JSON output.
 * Handles markdown code blocks, partial JSON, and extraction fallbacks.
 */
object JsonParser {

    private val gson = Gson()

    fun parseDiagnosis(raw: String): DiagnosisResult {
        val cleaned = cleanJson(raw)
        return try {
            val dto = gson.fromJson(cleaned, DiagnosisDto::class.java)
            if (dto == null) return fallbackDiagnosis(raw)
            DiagnosisResult(
                symptoms = dto.symptoms ?: "",
                possibleCause = dto.possibleCause ?: "",
                suggestion = dto.suggestion ?: "",
                seeDoctor = dto.seeDoctor ?: false,
                dangerLevel = dto.dangerLevel ?: 1,
                displayText = dto.displayText ?: ""
            )
        } catch (e: Exception) {
            fallbackDiagnosis(raw)
        }
    }

    fun parseMedicine(raw: String): MedicineResult {
        val cleaned = cleanJson(raw)
        return try {
            val dto = gson.fromJson(cleaned, MedicineDto::class.java)
            if (dto == null) return MedicineResult(displayText = raw.take(200))
            MedicineResult(
                name = dto.name ?: "",
                usage = dto.usage ?: "",
                dosage = dto.dosage ?: "",
                warning = dto.warning ?: "",
                displayText = dto.displayText ?: ""
            )
        } catch (e: Exception) {
            MedicineResult(displayText = raw.take(200))
        }
    }

    /**
     * Tries to extract display_text or name from JSON, or returns null if not JSON.
     */
    fun parseSafe(raw: String?): String? {
        if (raw == null) return null
        val cleaned = if (raw.contains("{") && raw.contains("}")) cleanJson(raw) else return null
        return try {
            val map = gson.fromJson(cleaned, Map::class.java)
            (map["display_text"] as? String) ?: (map["name"] as? String) ?: (map["content"] as? String)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Strip markdown code fences and leading/trailing whitespace.
     */
    private fun cleanJson(raw: String): String {
        var s = raw.trim()
        // Remove ```json ... ``` wrapping
        if (s.startsWith("```")) {
            s = s.removePrefix("```json").removePrefix("```")
            val endIdx = s.lastIndexOf("```")
            if (endIdx > 0) s = s.substring(0, endIdx)
        }
        // Find first { and last }
        val start = s.indexOf('{')
        val end = s.lastIndexOf('}')
        if (start >= 0 && end > start) {
            s = s.substring(start, end + 1)
        }
        return s.trim()
    }

    private fun fallbackDiagnosis(raw: String): DiagnosisResult {
        val dangerLevel = Regex(""""danger_level"\s*:\s*(\d)""")
            .find(raw)?.groupValues?.get(1)?.toIntOrNull() ?: 1
        val displayText = Regex(""""display_text"\s*:\s*"([^"]+)"""")
            .find(raw)?.groupValues?.get(1) ?: raw.take(200)
        return DiagnosisResult(
            displayText = displayText,
            dangerLevel = dangerLevel,
            seeDoctor = dangerLevel >= 3
        )
    }

    private data class DiagnosisDto(
        val symptoms: String?,
        @SerializedName("possible_cause") val possibleCause: String?,
        val suggestion: String?,
        @SerializedName("see_doctor") val seeDoctor: Boolean?,
        @SerializedName("danger_level") val dangerLevel: Int?,
        @SerializedName("display_text") val displayText: String?
    )

    private data class MedicineDto(
        val name: String?,
        val usage: String?,
        val dosage: String?,
        val warning: String?,
        @SerializedName("display_text") val displayText: String?
    )
}
