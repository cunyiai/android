package com.cunyi.gemma.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import com.cunyi.gemma.ai.GemmaManager
import com.cunyi.gemma.ui.components.ElderlyButton
import com.cunyi.gemma.ui.components.PrimaryActionButton
import com.cunyi.gemma.ui.components.SosButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modelStatus: GemmaManager.ModelStatus,
    errorMessage: String?,
    onVoiceConsult: () -> Unit,
    onMedicineId: () -> Unit,
    onHealthRecords: () -> Unit,
    onSos: () -> Unit,
    onSettings: () -> Unit,
    downloadProgress: Float? = null,
    onDownloadModel: (String, String) -> Unit = { _, _ -> },
) {
    var showModelSelection by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "村医 AI",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "设置",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Model status indicator
                if (modelStatus != GemmaManager.ModelStatus.READY) {
                    ModelStatusBar(
                        status = modelStatus,
                        errorMessage = errorMessage,
                        downloadProgress = downloadProgress,
                        onShowModelSelection = { showModelSelection = true }
                    )
                }

                // Primary action - voice consultation
                PrimaryActionButton(
                    text = "说说哪里不舒服",
                    subtext = "按下开始说话",
                    icon = Icons.Default.Mic,
                    onClick = onVoiceConsult
                )

                // Secondary actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElderlyButton(
                        text = "拍照识药",
                        icon = Icons.Default.CameraAlt,
                        onClick = onMedicineId,
                        modifier = Modifier.weight(1f)
                    )
                    ElderlyButton(
                        text = "健康记录",
                        icon = Icons.Default.MonitorHeart,
                        onClick = onHealthRecords,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // SOS button - always at bottom
            Column {
                SosButton(
                    text = "拨打 120（紧急求救）",
                    icon = Icons.Default.PhoneInTalk,
                    onClick = onSos
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showModelSelection) {
        ModalBottomSheet(onDismissRequest = { showModelSelection = false }) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                Text(
                    text = "下载 AI 大脑（约 2.4GB）",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Gemma 4 E2B - ModelScope mirror
                Card(
                    onClick = {
                        showModelSelection = false
                        onDownloadModel(
                            "https://www.modelscope.cn/models/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
                            "gemma-4-E2B-it.litertlm"
                        )
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI 大脑（Gemma 4）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("模型大小约 2.4GB，回答聪明全面，\n兼容绝大多数带 NPU 的手机。\n阿里云 ModelScope 源，国内高速下载", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Fallback: HuggingFace original (for users with VPN)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "下载需要稳定的网络环境。如果失败请稍后重试。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModelStatusBar(
    status: GemmaManager.ModelStatus,
    errorMessage: String?,
    downloadProgress: Float?,
    onShowModelSelection: () -> Unit,
) {
    val (text, color) = when (status) {
        GemmaManager.ModelStatus.INITIALIZING -> "AI 模型加载中..." to MaterialTheme.colorScheme.primary
        GemmaManager.ModelStatus.DOWNLOADING -> "AI 模型下载中..." to MaterialTheme.colorScheme.secondary
        GemmaManager.ModelStatus.COPYING -> "正在复制模型文件，请稍候..." to MaterialTheme.colorScheme.primary
        GemmaManager.ModelStatus.NEED_MODEL_FILE -> "需装配本村 AI 大脑才能看病" to MaterialTheme.colorScheme.secondary
        GemmaManager.ModelStatus.UNAVAILABLE -> "此设备太老旧，不支持搭载" to MaterialTheme.colorScheme.error
        GemmaManager.ModelStatus.ERROR -> "装配遇到问题，请重试" to MaterialTheme.colorScheme.error
        else -> return
    }

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(color.copy(alpha = 0.15f), color.copy(alpha = 0.05f))
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(gradientBrush)
            .padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (status == GemmaManager.ModelStatus.INITIALIZING
                    || status == GemmaManager.ModelStatus.DOWNLOADING
                    || status == GemmaManager.ModelStatus.COPYING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = color
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            }

            // Show error detail
            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Show download progress component when downloading
            if (downloadProgress != null) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "下载进度：${"%.1f".format(downloadProgress)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }

            // Show download button for NEED_MODEL_FILE and ERROR states
            if ((status == GemmaManager.ModelStatus.NEED_MODEL_FILE || status == GemmaManager.ModelStatus.ERROR) && downloadProgress == null) {
                if (errorMessage == null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "当前设备不支持自动分发模型。\n需要下载本地 LiteRT-LM 大脑（约 2GB）\n\n支持移动数据网络和 Wi-Fi 下载",
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onShowModelSelection,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("一键下载 AI 大脑", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
