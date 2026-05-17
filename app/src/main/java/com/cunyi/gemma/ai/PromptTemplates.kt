package com.cunyi.gemma.ai

object PromptTemplates {

    val SYSTEM_PROMPT = """
你是一位经验丰富的乡村全科医生助手。
规则：
1. 用最简单的大白话回答，像跟不识字的老人说话
2. 永远返回合法JSON，不要有任何多余内容
3. 不能确定的事，说"建议去卫生院确认"
4. 危险程度1=没问题，5=立刻送医
5. 你的建议仅供参考，不替代医生诊断
    """.trimIndent()

    fun buildDiagnosisPrompt(userAge: Int, conditions: List<String>, transcribedText: String): Pair<String, String> {
        val userContent = """
患者信息：${userAge}岁，已知病史：${conditions.joinToString("、").ifEmpty { "无" }}
患者描述：$transcribedText

请分析症状，返回严格JSON（不要有其他内容）：
{"symptoms":"识别到的主要症状","possible_cause":"最可能的原因（1句话）","suggestion":"建议怎么做（1-2句话）","see_doctor":true或false,"danger_level":1到5的整数,"display_text":"显示给老人看的大白话（不超过60字）"}
        """.trimIndent()
        return SYSTEM_PROMPT to userContent
    }

    fun buildVoiceDiagnosisPrompt(userAge: Int, conditions: List<String>): Pair<String, String> {
        val userContent = """
患者信息：${userAge}岁，已知病史：${conditions.joinToString("、").ifEmpty { "无" }}
上面的音频是患者的口述，请先听懂内容，然后分析症状。

返回严格JSON（不要有其他内容）：
{"symptoms":"识别到的主要症状","possible_cause":"最可能的原因（1句话）","suggestion":"建议怎么做（1-2句话）","see_doctor":true或false,"danger_level":1到5的整数,"display_text":"显示给老人看的大白话（不超过60字）"}
        """.trimIndent()
        return SYSTEM_PROMPT to userContent
    }

    fun buildMedicinePrompt(currentMeds: List<String>): Pair<String, String> {
        val userContent = """
请识别图片中的药品，返回严格JSON（不要有其他内容）：
{"name":"药品通用名","usage":"这个药治什么病（1句话）","dosage":"怎么吃，吃多少（1句话）","warning":"要注意什么（1句话）","display_text":"用大白话告诉老人这个药怎么用（不超过80字）"}

${if (currentMeds.isNotEmpty()) "患者目前在吃的药：${currentMeds.joinToString("、")}，如果有冲突请在warning里特别说明" else ""}
        """.trimIndent()
        return SYSTEM_PROMPT to userContent
    }

    fun buildMedicineFromTextPrompt(ocrText: String, currentMeds: List<String>): Pair<String, String> {
        val userContent = """
以下是从药品包装上用OCR识别出的文字：
$ocrText

请根据上面的文字判断这是什么药品，返回严格JSON（不要有其他内容）：
{"name":"药品通用名","usage":"这个药治什么病（1句话）","dosage":"怎么吃，吃多少（1句话）","warning":"要注意什么（1句话）","display_text":"用大白话告诉老人这个药怎么用（不超过80字）"}

${if (currentMeds.isNotEmpty()) "患者目前在吃的药：${currentMeds.joinToString("、")}，如果有冲突请在warning里特别说明" else ""}
        """.trimIndent()
        return SYSTEM_PROMPT to userContent
    }

    fun buildSosSummaryPrompt(symptoms: String): Pair<String, String> {
        val userContent = """
用一句话总结以下症状，给家人发短信用，口语化，不超过50字：
$symptoms
只返回纯文本，不要JSON。
        """.trimIndent()
        return SYSTEM_PROMPT to userContent
    }

    fun buildWeeklyReportPrompt(recordsSummary: String, userAge: Int, conditions: List<String>): Pair<String, String> {
        val userContent = """
患者信息：${userAge}岁，已知病史：${conditions.joinToString("、").ifEmpty { "无" }}
以下是过去7天的健康记录：
$recordsSummary

请生成一份简单的周报，用大白话，包含：
1. 这周整体情况好不好
2. 哪些指标需要注意
3. 下周建议怎么做

返回纯文本，3-5句话即可。
        """.trimIndent()
        return SYSTEM_PROMPT to userContent
    }
}
