package com.cunyi.gemma.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Pre-bundled drug safety database (read-only).
 * Ships as asset or is created on first launch with seed data.
 */
class DrugDatabase(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS drug_info (
                drug_id     TEXT PRIMARY KEY,
                drug_name   TEXT,
                brand_names TEXT,
                usage       TEXT,
                dosage      TEXT,
                warnings    TEXT,
                category    TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS drug_interactions (
                drug_a      TEXT,
                drug_b      TEXT,
                severity    INTEGER,
                description TEXT,
                PRIMARY KEY (drug_a, drug_b)
            )
        """)
        seedCommonDrugs(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future migration
    }

    private fun seedCommonDrugs(db: SQLiteDatabase) {
        val drugs = listOf(
            DrugSeed("amlodipine", "氨氯地平", "[\"络活喜\",\"安内真\"]", "高血压", "每日一次，每次5mg", "低血压患者慎用；孕妇禁用", "降压药"),
            DrugSeed("metformin", "二甲双胍", "[\"格华止\"]", "2型糖尿病", "每日2-3次，每次500mg，随餐服用", "肾功能不全禁用；饮酒时慎用", "降糖药"),
            DrugSeed("nifedipine", "硝苯地平", "[\"拜新同\",\"心痛定\"]", "高血压、心绞痛", "缓释片每日一次30mg", "低血压禁用；不能掰开或咀嚼缓释片", "降压药"),
            DrugSeed("captopril", "卡托普利", "[\"开博通\"]", "高血压、心力衰竭", "每日2-3次，每次12.5-25mg", "双侧肾动脉狭窄禁用；干咳为常见副作用", "降压药"),
            DrugSeed("glipizide", "格列吡嗪", "[\"美吡达\",\"迪沙\"]", "2型糖尿病", "每日1-2次，每次5mg，餐前30分钟", "低血糖风险；肝肾功能不全慎用", "降糖药"),
            DrugSeed("aspirin", "阿司匹林", "[\"拜阿司匹灵\"]", "抗血小板、解热镇痛", "抗血小板：每日100mg；解热：每次300-600mg", "胃溃疡禁用；出血倾向者禁用", "解热镇痛/抗血小板"),
            DrugSeed("atorvastatin", "阿托伐他汀", "[\"立普妥\"]", "高血脂", "每日一次10-20mg，睡前服用", "肝病禁用；肌肉酸痛需及时就医", "降脂药"),
            DrugSeed("metoprolol", "美托洛尔", "[\"倍他乐克\"]", "高血压、心绞痛、心律失常", "每日2次，每次25-50mg", "严重心动过缓禁用；哮喘患者禁用", "降压药"),
            DrugSeed("insulin_glargine", "甘精胰岛素", "[\"来得时\",\"长秀霖\"]", "糖尿病", "每日一次皮下注射，剂量个体化", "低血糖风险；注射部位需轮换", "胰岛素"),
            DrugSeed("omeprazole", "奥美拉唑", "[\"洛赛克\"]", "胃溃疡、胃酸过多", "每日一次20mg，早餐前", "长期使用注意骨质疏松风险", "质子泵抑制剂"),
            DrugSeed("clopidogrel", "氯吡格雷", "[\"波立维\"]", "预防血栓", "每日一次75mg", "活动性出血禁用；手术前需停药", "抗血小板"),
            DrugSeed("hydrochlorothiazide", "氢氯噻嗪", "[\"双克\"]", "高血压、水肿", "每日一次12.5-25mg", "痛风患者慎用；注意低钾", "利尿药"),
            DrugSeed("nitroglycerin", "硝酸甘油", "[\"耐绞宁\"]", "心绞痛急性发作", "舌下含服0.5mg，可重复", "低血压禁用；用后可能头痛", "硝酸酯类"),
            DrugSeed("compound_danshen", "复方丹参滴丸", "[\"复方丹参滴丸\"]", "胸闷心痛、冠心病", "每次10粒，每日3次", "出血倾向者慎用", "中成药"),
            DrugSeed("suxiao_jiuxin", "速效救心丸", "[\"速效救心丸\"]", "心绞痛急救", "舌下含服4-6粒", "低血压慎用；过敏者禁用", "中成药"),
        )

        val interactions = listOf(
            InteractionSeed("aspirin", "clopidogrel", 2, "同时使用增加出血风险，需医生评估"),
            InteractionSeed("aspirin", "metformin", 1, "阿司匹林可能增强二甲双胍降糖效果"),
            InteractionSeed("captopril", "hydrochlorothiazide", 1, "常见联合用药，注意低血压"),
            InteractionSeed("metformin", "insulin_glargine", 2, "联用增加低血糖风险，需监测血糖"),
            InteractionSeed("amlodipine", "atorvastatin", 1, "联用时阿托伐他汀不超过20mg"),
            InteractionSeed("metoprolol", "nifedipine", 2, "联用可能导致严重低血压和心动过缓"),
            InteractionSeed("nitroglycerin", "nifedipine", 2, "联用可能导致严重低血压"),
            InteractionSeed("aspirin", "nitroglycerin", 1, "常见联合用药，注意出血"),
            InteractionSeed("omeprazole", "clopidogrel", 3, "奥美拉唑显著降低氯吡格雷疗效，建议换用泮托拉唑"),
            InteractionSeed("captopril", "metformin", 1, "卡托普利可能增强降糖效果"),
        )

        db.beginTransaction()
        try {
            for (d in drugs) {
                db.execSQL(
                    "INSERT OR IGNORE INTO drug_info VALUES (?,?,?,?,?,?,?)",
                    arrayOf(d.id, d.name, d.brands, d.usage, d.dosage, d.warnings, d.category)
                )
            }
            for (i in interactions) {
                db.execSQL(
                    "INSERT OR IGNORE INTO drug_interactions VALUES (?,?,?,?)",
                    arrayOf<Any>(i.drugA, i.drugB, i.severity, i.desc)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun searchDrug(name: String): List<DrugInfo> {
        val results = mutableListOf<DrugInfo>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM drug_info WHERE drug_name LIKE ? OR brand_names LIKE ? LIMIT 10",
            arrayOf("%$name%", "%$name%")
        )
        cursor.use {
            while (it.moveToNext()) {
                results.add(DrugInfo(
                    drugId = it.getString(0),
                    drugName = it.getString(1),
                    brandNames = it.getString(2),
                    usage = it.getString(3),
                    dosage = it.getString(4),
                    warnings = it.getString(5),
                    category = it.getString(6)
                ))
            }
        }
        return results
    }

    fun checkInteractions(drugName: String, currentMeds: List<String>): List<DrugInteraction> {
        if (currentMeds.isEmpty()) return emptyList()
        val results = mutableListOf<DrugInteraction>()
        val db = readableDatabase
        val placeholders = currentMeds.joinToString(",") { "?" }
        val cursor = db.rawQuery(
            """SELECT * FROM drug_interactions
               WHERE (drug_a = ? AND drug_b IN ($placeholders))
               OR (drug_b = ? AND drug_a IN ($placeholders))""",
            arrayOf(drugName, *currentMeds.toTypedArray(), drugName, *currentMeds.toTypedArray())
        )
        cursor.use {
            while (it.moveToNext()) {
                results.add(DrugInteraction(
                    drugA = it.getString(0),
                    drugB = it.getString(1),
                    severity = it.getInt(2),
                    description = it.getString(3)
                ))
            }
        }
        return results
    }

    private data class DrugSeed(val id: String, val name: String, val brands: String, val usage: String, val dosage: String, val warnings: String, val category: String)
    private data class InteractionSeed(val drugA: String, val drugB: String, val severity: Int, val desc: String)

    companion object {
        private const val DATABASE_NAME = "drug_safety.db"
        private const val DATABASE_VERSION = 1
    }
}
