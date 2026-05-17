package com.cunyi.gemma.ui.sos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SmsManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cunyi.gemma.ai.GemmaManager
import com.cunyi.gemma.ai.PromptTemplates
import com.cunyi.gemma.data.db.AppDatabase
import com.cunyi.gemma.data.entity.UserProfile
import com.cunyi.gemma.ui.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosScreen(
    gemmaManager: GemmaManager,
    db: AppDatabase,
    userProfile: UserProfile?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showConfirm by remember { mutableStateOf(true) }
    var isCalling by remember { mutableStateOf(false) }
    var callMade by remember { mutableStateOf(false) }
    var smsSent by remember { mutableStateOf(false) }
    var smsContent by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Permissions
    var hasCallPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val callPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCallPermission = granted }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasSmsPermission = granted }

    val emergencyContacts: List<String> = remember(userProfile) {
        try {
            Gson().fromJson(
                userProfile?.emergencyContacts ?: "[]",
                object : TypeToken<List<String>>() {}.type
            )
        } catch (e: Exception) { emptyList() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("紧急求救", color = DangerRed) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Request permissions if missing
            if (!hasCallPermission || !hasSmsPermission) {
                Icon(Icons.Default.Warning, null, tint = DangerRed, modifier = Modifier.size(80.dp))
                Spacer(Modifier.height(16.dp))
                Text("需要授权才能使用紧急求救", style = MaterialTheme.typography.headlineMedium, color = DangerRed)
                Spacer(Modifier.height(8.dp))
                if (!hasCallPermission) {
                    Button(onClick = { callPermissionLauncher.launch(Manifest.permission.CALL_PHONE) }) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("允许拨打电话")
                    }
                    Spacer(Modifier.height(8.dp))
                }
                if (!hasSmsPermission) {
                    OutlinedButton(onClick = { smsPermissionLauncher.launch(Manifest.permission.SEND_SMS) }) {
                        Icon(Icons.Default.Sms, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("允许发送短信")
                    }
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onBack) { Text("返回") }
            } else if (showConfirm) {
                // Confirmation screen - show what will happen
                Icon(Icons.Default.PhoneInTalk, null, tint = DangerRed, modifier = Modifier.size(80.dp))
                Spacer(Modifier.height(16.dp))

                Text(
                    "确定要拨打 120 急救电话吗？",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DangerRed
                )
                Spacer(Modifier.height(12.dp))

                // Action summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, null, tint = DangerRed, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("自动拨打 120 急救中心")
                        }
                        if (emergencyContacts.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Sms, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("同时给 ${emergencyContacts.size} 位家人发短信通知")
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("手机持续震动提醒")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Call 120 button
                Button(
                    onClick = {
                        showConfirm = false
                        isCalling = true
                        vibrateLong(context)

                        scope.launch {
                            try {
                                // Step 1: Dial 120 immediately
                                dialPhone(context, "120")

                                // Step 2: Send SMS to family (async, don't block UI)
                                if (emergencyContacts.isNotEmpty()) {
                                    try {
                                        val recentChats = db.chatHistoryDao().getRecent(limit = 5)
                                        val symptomText = recentChats
                                            .filter { it.role == "assistant" }
                                            .joinToString("; ") { it.content.take(100) }
                                            .ifBlank { "老人按下了紧急求救按钮" }

                                        val aiSummary = try {
                                            val prompt = PromptTemplates.buildSosSummaryPrompt(symptomText)
                                            gemmaManager.inferText(prompt)
                                        } catch (e: Exception) {
                                            "老人感到不适，请尽快联系"
                                        }

                                        val dateStr = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
                                            .format(Date())
                                        val name = userProfile?.name ?: "老人"
                                        val village = userProfile?.village ?: ""

                                        smsContent = buildString {
                                            append("【村医AI紧急提醒】")
                                            append("${name}于${dateStr}按下了急救按钮，已拨打120。")
                                            append(aiSummary.take(50))
                                            if (village.isNotBlank()) append(" 位置：$village")
                                            append(" 请立即关注！")
                                        }

                                        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            context.getSystemService(SmsManager::class.java)
                                        } else {
                                            @Suppress("DEPRECATION")
                                            SmsManager.getDefault()
                                        }

                                        for (contact in emergencyContacts) {
                                            if (contact.isNotBlank()) {
                                                smsManager.sendTextMessage(contact, null, smsContent, null, null)
                                            }
                                        }
                                        smsSent = true
                                    } catch (e: Exception) {
                                        // SMS failure should not block the SOS flow
                                        errorMsg = "短信发送失败（120已拨打）：${e.message}"
                                    }
                                }

                                callMade = true
                            } catch (e: Exception) {
                                errorMsg = "拨号失败：${e.message}"
                            } finally {
                                isCalling = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed,
                        contentColor = TextOnDanger
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PhoneInTalk, null, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("立即拨打 120", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(Modifier.height(16.dp))

                // Alternative: Call family first
                if (emergencyContacts.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            showConfirm = false
                            isCalling = true
                            vibrateLong(context)

                            // Call first emergency contact instead of 120
                            val contact = emergencyContacts.firstOrNull { it.isNotBlank() } ?: return@OutlinedButton
                            dialPhone(context, contact)

                            // Also send SMS to other contacts
                            scope.launch {
                                try {
                                    val dateStr = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault()).format(Date())
                                    val name = userProfile?.name ?: "老人"
                                    smsContent = "【村医AI紧急提醒】${name}于${dateStr}按下了求救按钮，请尽快联系确认安全！"

                                    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        context.getSystemService(SmsManager::class.java)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        SmsManager.getDefault()
                                    }

                                    for (c in emergencyContacts) {
                                        if (c.isNotBlank() && c != contact) {
                                            smsManager.sendTextMessage(c, null, smsContent, null, null)
                                        }
                                    }
                                    smsSent = true
                                } catch (_: Exception) {}
                                isCalling = false
                                callMade = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.ContactPhone, null, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("先打给家人：${emergencyContacts.firstOrNull{it.isNotBlank()}?.take(10)}...", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Cancel button
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("取消，我没事", style = MaterialTheme.typography.labelMedium)
                }

            } else if (isCalling) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = DangerRed,
                    strokeWidth = 5.dp
                )
                Spacer(Modifier.height(16.dp))
                Text("正在拨打 120...", style = MaterialTheme.typography.titleLarge, color = DangerRed)
                Spacer(Modifier.height(8.dp))
                Text("请将手机贴近耳朵接听", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

            } else if (callMade) {
                // Success - call made
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = SafeGreen,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "已拨打 120",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SafeGreen
                )
                Spacer(Modifier.height(8.dp))

                if (smsSent && emergencyContacts.isNotEmpty()) {
                    Text(
                        "已同时通知 ${emergencyContacts.size} 位家人",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Show sent SMS content
                if (smsContent.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("短信内容：", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            Text(smsContent, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Redial button
                Button(
                    onClick = { dialPhone(context, "120") },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.PhoneInTalk, null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("再次拨打 120")
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onBack) { Text("返回首页") }

            } else {
                // Error state
                errorMsg?.let { msg ->
                    Icon(Icons.Default.Error, null, tint = DangerRed, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(msg, style = MaterialTheme.typography.bodyLarge, color = DangerRed)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showConfirm = true; errorMsg = null }) {
                        Text("重试")
                    }
                }
            }
        }
    }
}

/**
 * Dials a phone number using ACTION_CALL (direct dial, no user confirmation).
 * Falls back to ACTION_DIAL (opens dialer with number pre-filled) if direct call fails.
 */
private fun dialPhone(context: Context, phoneNumber: String) {
    try {
        // Try direct call first (ACTION_CALL)
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(callIntent)
    } catch (e: SecurityException) {
        // Fallback: open dialer with number pre-filled
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        } catch (e2: Exception) {
            throw e2
        }
    }
}

private fun vibrateLong(context: Context) {
    try {
        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    } catch (e: Exception) {
        // Vibration not available
    }
}
