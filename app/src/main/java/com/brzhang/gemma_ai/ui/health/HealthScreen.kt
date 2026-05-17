package com.brzhang.gemma_ai.ui.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.brzhang.gemma_ai.ai.GemmaManager
import com.brzhang.gemma_ai.data.RulesEngine
import com.brzhang.gemma_ai.data.db.AppDatabase
import com.brzhang.gemma_ai.data.entity.HealthRecord
import com.brzhang.gemma_ai.data.entity.UserProfile
import com.brzhang.gemma_ai.ui.components.DangerBanner
import com.brzhang.gemma_ai.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    db: AppDatabase,
    gemmaManager: GemmaManager,
    userProfile: UserProfile?,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var records by remember { mutableStateOf<List<HealthRecord>>(emptyList()) }
    var showInputDialog by remember { mutableStateOf(false) }
    var alert by remember { mutableStateOf<RulesEngine.Alert?>(null) }

    // Load recent records
    LaunchedEffect(Unit) {
        records = db.healthRecordDao().getRecent()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("健康记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showInputDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(64.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text("记录数据", style = MaterialTheme.typography.labelMedium)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Alert banner
            alert?.let { a ->
                if (a.level != RulesEngine.AlertLevel.GREEN) {
                    DangerBanner(message = a.message)
                    Spacer(Modifier.height(12.dp))
                }
            }

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MonitorHeart,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("还没有记录", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                        Text("点击下方按钮添加血压、血糖等数据", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(records) { record ->
                        HealthRecordItem(record)
                    }
                }
            }
        }
    }

    // Input dialog
    if (showInputDialog) {
        HealthInputDialog(
            onDismiss = { showInputDialog = false },
            onSave = { type, value, secondary, unit ->
                scope.launch {
                    val record = HealthRecord(
                        recordType = type,
                        value = value,
                        secondaryValue = secondary,
                        unit = unit
                    )
                    db.healthRecordDao().insert(record)

                    // Rules engine check
                    val ruleAlert = RulesEngine.evaluate(record)
                    alert = ruleAlert

                    // Trend check
                    val recent3 = db.healthRecordDao().getLast3ByType(type)
                    val trendAlert = RulesEngine.checkTrend(recent3)
                    if (trendAlert != null && (alert?.level ?: RulesEngine.AlertLevel.GREEN) < trendAlert.level) {
                        alert = trendAlert
                    }

                    records = db.healthRecordDao().getRecent()
                    showInputDialog = false
                }
            }
        )
    }
}

@Composable
private fun HealthRecordItem(record: HealthRecord) {
    val ruleAlert = RulesEngine.evaluate(record)
    val bgColor = when (ruleAlert.level) {
        RulesEngine.AlertLevel.RED -> DangerBackground
        RulesEngine.AlertLevel.YELLOW -> WarningBackground
        RulesEngine.AlertLevel.GREEN -> SafeBackground
    }
    val indicatorColor = when (ruleAlert.level) {
        RulesEngine.AlertLevel.RED -> DangerRed
        RulesEngine.AlertLevel.YELLOW -> WarningYellow
        RulesEngine.AlertLevel.GREEN -> SafeGreen
    }

    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    val typeLabel = when (record.recordType) {
        "blood_pressure" -> "血压"
        "glucose" -> "血糖"
        "heart_rate" -> "心率"
        else -> record.recordType
    }
    val valueText = when (record.recordType) {
        "blood_pressure" -> "${record.value.toInt()}/${record.secondaryValue?.toInt() ?: "-"} ${record.unit}"
        else -> "${record.value} ${record.unit}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(indicatorColor)
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(typeLabel, style = MaterialTheme.typography.titleMedium)
                Text(
                    dateFormat.format(Date(record.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Text(
                valueText,
                style = MaterialTheme.typography.headlineMedium,
                color = indicatorColor
            )
        }
    }
}

@Composable
private fun HealthInputDialog(
    onDismiss: () -> Unit,
    onSave: (type: String, value: Float, secondary: Float?, unit: String) -> Unit,
) {
    var selectedType by remember { mutableStateOf("blood_pressure") }
    var value1 by remember { mutableStateOf("") }
    var value2 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录健康数据", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("blood_pressure" to "血压", "glucose" to "血糖", "heart_rate" to "心率").forEach { (type, label) ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                value1 = ""
                                value2 = ""
                            },
                            label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                when (selectedType) {
                    "blood_pressure" -> {
                        OutlinedTextField(
                            value = value1,
                            onValueChange = { value1 = it.filter { c -> c.isDigit() } },
                            label = { Text("高压（收缩压）") },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = value2,
                            onValueChange = { value2 = it.filter { c -> c.isDigit() } },
                            label = { Text("低压（舒张压）") },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "glucose" -> {
                        OutlinedTextField(
                            value = value1,
                            onValueChange = { value1 = it },
                            label = { Text("血糖值 (mmol/L)") },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "heart_rate" -> {
                        OutlinedTextField(
                            value = value1,
                            onValueChange = { value1 = it.filter { c -> c.isDigit() } },
                            label = { Text("心率 (次/分)") },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v1 = value1.toFloatOrNull() ?: return@Button
                    val v2 = value2.toFloatOrNull()
                    val unit = when (selectedType) {
                        "blood_pressure" -> "mmHg"
                        "glucose" -> "mmol/L"
                        "heart_rate" -> "bpm"
                        else -> ""
                    }
                    onSave(selectedType, v1, v2, unit)
                },
                enabled = value1.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
