package com.brzhang.gemma_ai.data.db

import androidx.room.*

data class DrugInfo(
    val drugId: String,
    val drugName: String,
    val brandNames: String,
    val usage: String,
    val dosage: String,
    val warnings: String,
    val category: String
)

data class DrugInteraction(
    val drugA: String,
    val drugB: String,
    val severity: Int,       // 1=mild, 2=moderate, 3=severe
    val description: String
)

@Dao
interface DrugDao {

    @Query("SELECT * FROM drug_info WHERE drug_name LIKE '%' || :name || '%' OR brand_names LIKE '%' || :name || '%' LIMIT 10")
    suspend fun searchByName(name: String): List<DrugInfo>

    @Query("SELECT * FROM drug_info WHERE drug_id = :id")
    suspend fun getById(id: String): DrugInfo?

    @Query("""
        SELECT * FROM drug_interactions
        WHERE (drug_a = :drugName OR drug_b = :drugName)
        AND (drug_a IN (:currentMeds) OR drug_b IN (:currentMeds))
    """)
    suspend fun checkInteractions(drugName: String, currentMeds: List<String>): List<DrugInteraction>

    @Query("SELECT * FROM drug_interactions WHERE drug_a = :drugA AND drug_b = :drugB")
    suspend fun getInteraction(drugA: String, drugB: String): DrugInteraction?
}
