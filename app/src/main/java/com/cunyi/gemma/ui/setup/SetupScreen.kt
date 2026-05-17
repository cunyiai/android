package com.cunyi.gemma.ui.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cunyi.gemma.data.entity.UserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    initialProfile: UserProfile?,
    downloadedModels: List<File>,
    activeModelPath: String?,
    onActivateModel: (File) -> Unit,
    onDeleteModel: (File) -> Unit,
    onComplete: (name: String, age: Int, conditions: String, contacts: String, village: String) -> Unit
) {
    val gson = remember { Gson() }
    
    // Parse JSON lists safely
    val initialConditions = remember(initialProfile) {
        try {
            val type = object : TypeToken<List<String>>() {}.type
            val list: List<String> = gson.fromJson(initialProfile?.chronicConditions ?: "[]", type)
            list.joinToString("、")
        } catch (e: Exception) { "" }
    }
    
    val initialContacts = remember(initialProfile) {
        try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(initialProfile?.emergencyContacts ?: "[]", type)
        } catch (e: Exception) { emptyList() }
    }

    var name by remember(initialProfile) { mutableStateOf(initialProfile?.name ?: "") }
    var age by remember(initialProfile) { mutableStateOf(initialProfile?.age?.toString() ?: "") }
    var conditions by remember(initialProfile) { mutableStateOf(initialConditions) }
    var contact1 by remember(initialProfile) { mutableStateOf(initialContacts.getOrNull(0) ?: "") }
    var contact2 by remember(initialProfile) { mutableStateOf(initialContacts.getOrNull(1) ?: "") }
    var village by remember(initialProfile) { mutableStateOf(initialProfile?.village ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置中心", style = MaterialTheme.typography.headlineMedium) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // AI Model Management Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🧠 本地 AI 大脑管理",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(16.dp))

                    if (downloadedModels.isEmpty()) {
                        Text("当前未下载任何模型，请在主页点击下载。", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        downloadedModels.forEach { file ->
                            ModelItem(
                                file = file,
                                isActive = file.absolutePath == activeModelPath,
                                onActivate = { onActivateModel(file) },
                                onDelete = { onDeleteModel(file) }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Healthcare Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "📋 个人健康档案",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column {
                        Text("您的姓名", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = { Text("请输入姓名") },
                            singleLine = true
                        )
                    }

                    Column {
                        Text("年龄", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = { Text("请输入年龄") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Column {
                        Text("已有的病（用顿号分开）", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = conditions,
                            onValueChange = { conditions = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = { Text("比如：高血压、糖尿病") },
                            singleLine = true
                        )
                    }

                    Column {
                        Text("紧急联系人电话（家人）", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = contact1,
                            onValueChange = { contact1 = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = { Text("第一个电话号码") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = contact2,
                            onValueChange = { contact2 = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = { Text("第二个电话号码（可不填）") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                    }

                    Column {
                        Text("村庄名称", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = { Text("比如：张家村") },
                            singleLine = true
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val contacts = listOfNotNull(
                        contact1.takeIf { it.isNotBlank() },
                        contact2.takeIf { it.isNotBlank() }
                    ).joinToString(",")
                    onComplete(
                        name,
                        age.toIntOrNull() ?: 65,
                        conditions,
                        contacts,
                        village
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = name.isNotBlank() && age.isNotBlank() && contact1.isNotBlank()
            ) {
                Text("保存档案", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ModelItem(
    file: File,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    val sizeMb = file.length() / (1024 * 1024)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isActive) CardDefaults.outlinedCardBorder().copy(width = 2.dp, brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Icon(Icons.Default.CheckCircle, "Active", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(text = "大小：${sizeMb} MB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button
                FilledTonalButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("删除")
                }
                Spacer(Modifier.width(8.dp))
                
                // Activate button
                if (!isActive) {
                    Button(onClick = onActivate) {
                        Text("激活此大脑")
                    }
                } else {
                    OutlinedButton(onClick = { }, enabled = false) {
                        Text("当前使用中")
                    }
                }
            }
        }
    }
}
