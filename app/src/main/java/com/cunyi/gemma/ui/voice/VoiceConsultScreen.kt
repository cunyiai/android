package com.cunyi.gemma.ui.voice

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cunyi.gemma.ai.GemmaManager
import com.cunyi.gemma.ai.PromptTemplates
import com.cunyi.gemma.data.db.AppDatabase
import com.cunyi.gemma.data.entity.ChatHistory
import com.cunyi.gemma.data.entity.UserProfile
import com.cunyi.gemma.data.model.DiagnosisResult
import com.cunyi.gemma.service.AudioRecorderService
import com.cunyi.gemma.ui.components.DisclaimerBar
import com.cunyi.gemma.ui.theme.*
import com.cunyi.gemma.util.JsonParser
import com.cunyi.gemma.util.TtsHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceConsultScreen(
    gemmaManager: GemmaManager,
    db: AppDatabase,
    ttsHelper: TtsHelper,
    userProfile: UserProfile?,
    onBack: () -> Unit,
    onSos: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioRecorder = remember { AudioRecorderService(context) }
    val listState = rememberLazyListState()

    var isRecording by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var chatMessages by remember { mutableStateOf<List<ChatHistory>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    // Load initial history
    LaunchedEffect(Unit) {
        db.chatHistoryDao().getAllHistory().collect { history ->
            chatMessages = history
        }
    }

    // Auto-scroll to bottom
    LaunchedEffect(chatMessages.size, isAnalyzing) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Parse user conditions
    val conditions: List<String> = remember(userProfile) {
        try {
            Gson().fromJson(
                userProfile?.chronicConditions ?: "[]",
                object : TypeToken<List<String>>() {}.type
            )
        } catch (e: Exception) { emptyList() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("智能村医对话") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        },
        bottomBar = {
            VoiceInputSection(
                isRecording = isRecording,
                isAnalyzing = isAnalyzing,
                onStartRecording = {
                    isRecording = true
                    errorMsg = null
                    scope.launch {
                        try {
                            val audioData = audioRecorder.startRecording()
                            isRecording = false
                            isAnalyzing = true

                            // 1. Save user placeholder or just send to AI
                            // For simplicity in this multimodal path, we don't know the exact text yet
                            // but we can add a "Voice message" entry
                            val userMsg = ChatHistory(role = "user", content = "[语音消息]", inputType = "voice")
                            db.chatHistoryDao().insert(userMsg)

                            // 2. AI Inference
                            val prompt = PromptTemplates.buildVoiceDiagnosisPrompt(
                                userAge = userProfile?.age ?: 65,
                                conditions = conditions
                            )
                            val response = gemmaManager.inferWithAudio(prompt, audioData)
                            
                            // 3. Save AI response
                            db.chatHistoryDao().insert(ChatHistory(
                                role = "assistant",
                                content = response,
                                inputType = "voice"
                            ))

                            // 4. Auto TTS for elderly
                            val parsed = JsonParser.parseDiagnosis(response)
                            ttsHelper.speak(parsed.displayText)

                        } catch (e: Exception) {
                            errorMsg = "村医助理暂时连接不上：${e.message}"
                        } finally {
                            isAnalyzing = false
                            isRecording = false
                        }
                    }
                },
                onStopRecording = {
                    audioRecorder.stopRecording()
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Intro message if empty
                if (chatMessages.isEmpty() && !isAnalyzing) {
                    item {
                        IntroMessage()
                    }
                }

                items(chatMessages) { message ->
                    ChatMessageBubble(
                        message = message,
                        onRead = { ttsHelper.speak(it) },
                        onSos = onSos
                    )
                }

                if (isAnalyzing) {
                    item {
                        ThinkingBubble()
                    }
                }

                if (errorMsg != null) {
                    item {
                        ErrorMessageBubble(errorMsg!!)
                    }
                }

                item {
                    DisclaimerBar()
                }
            }
            
            if (!hasPermission) {
                PermissionOverlay {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatHistory,
    onRead: (String) -> Unit,
    onSos: () -> Unit
) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) Primary else Color(0xFFF0F0F0)
    val textColor = if (isUser) TextOnPrimary else TextPrimary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).padding(top = 8.dp),
                    tint = Primary
                )
                Spacer(Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 0.dp,
                            bottomEnd = if (isUser) 0.dp else 16.dp
                        )
                    )
                    .background(bgColor)
                    .padding(16.dp)
                    .clickable { 
                        if (!isUser) {
                            val parsed = JsonParser.parseSafe(message.content)
                            onRead(parsed ?: message.content)
                        }
                    }
            ) {
                if (isUser) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, null, tint = textColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(message.content, color = textColor, fontSize = 18.sp)
                    }
                } else {
                    val result = try { 
                        JsonParser.parseDiagnosis(message.content)
                    } catch (e: Exception) { null }

                    if (result != null) {
                        Text(
                            text = result.displayText,
                            color = textColor,
                            fontSize = 20.sp, // Extra large for elderly
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (result.dangerLevel >= 4) {
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = onSos,
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Sos, null)
                                Spacer(Modifier.width(8.dp))
                                Text("立即通知家人", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(message.content, color = textColor, fontSize = 20.sp)
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            Icons.Default.VolumeUp, 
                            null, 
                            tint = Primary, 
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (isUser) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).padding(top = 8.dp),
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun VoiceInputSection(
    isRecording: Boolean,
    isAnalyzing: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val scale by animateFloatAsState(
                targetValue = if (isRecording) 1.2f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(if (isRecording) scale else 1f)
                    .clip(CircleShape)
                    .background(if (isRecording) DangerRed else Primary)
                    .clickable {
                        if (isRecording) onStopRecording()
                        else if (!isAnalyzing) onStartRecording()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    isRecording -> "正在听... 说完再按一下"
                    isAnalyzing -> "村医助理正在思考..."
                    else -> "点一下开始说话"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRecording) DangerRed else Primary
            )
        }
    }
}

@Composable
fun IntroMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "您好！我是您的智能村医助理。",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "请点击下方的蓝色按钮，告诉我您哪里不舒服，或者有什么想问的。",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ThinkingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Face, null, tint = Primary, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(8.dp))
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text("正在思考...", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun ErrorMessageBubble(msg: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DangerBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            msg,
            modifier = Modifier.padding(16.dp),
            color = DangerRed,
            fontSize = 16.sp
        )
    }
}

@Composable
fun PermissionOverlay(onRequest: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.MicNone, null, modifier = Modifier.size(64.dp), tint = Primary)
                Spacer(Modifier.height(16.dp))
                Text(
                    "需要录音权限",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "请允许权限，这样村医才能听到您的声音。",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onRequest,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("去开启权限", fontSize = 18.sp)
                }
            }
        }
    }
}
