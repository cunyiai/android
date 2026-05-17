package com.cunyi.gemma.ui.medicine

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cunyi.gemma.ai.GemmaManager
import com.cunyi.gemma.ai.PromptTemplates
import com.cunyi.gemma.data.db.DrugDatabase
import com.cunyi.gemma.data.db.DrugInteraction
import com.cunyi.gemma.data.entity.UserProfile
import com.cunyi.gemma.data.model.MedicineResult
import com.cunyi.gemma.ui.components.DangerBanner
import com.cunyi.gemma.ui.components.DisclaimerBar
import com.cunyi.gemma.ui.components.ResultCard
import com.cunyi.gemma.ui.theme.*
import com.cunyi.gemma.util.JsonParser
import com.cunyi.gemma.util.TtsHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    gemmaManager: GemmaManager,
    drugDb: DrugDatabase,
    userProfile: UserProfile?,
    ttsHelper: TtsHelper,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<MedicineResult?>(null) }
    var interactions by remember { mutableStateOf<List<DrugInteraction>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val currentMeds: List<String> = remember(userProfile) {
        try {
            Gson().fromJson(
                userProfile?.currentMedications ?: "[]",
                object : TypeToken<List<String>>() {}.type
            )
        } catch (e: Exception) { emptyList() }
    }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拍照识药") },
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!hasPermission) {
                Spacer(Modifier.height(40.dp))
                Text("需要相机权限才能拍照识药", style = MaterialTheme.typography.bodyLarge)
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("允许相机")
                }
            } else if (capturedBitmap == null) {
                // Camera preview
                CameraPreviewSection(
                    imageCapture = imageCapture,
                    cameraExecutor = cameraExecutor,
                    onCapture = { bitmap ->
                        capturedBitmap = bitmap
                        isAnalyzing = true
                        scope.launch {
                            try {
                                val prompt = PromptTemplates.buildMedicinePrompt(currentMeds)
                                val response = gemmaManager.inferWithImage(prompt, bitmap)
                                val parsed = JsonParser.parseMedicine(response)
                                result = parsed

                                // Cross-check with local drug DB
                                if (parsed.name.isNotBlank()) {
                                    interactions = drugDb.checkInteractions(
                                        parsed.name, currentMeds
                                    )
                                }

                                ttsHelper.speak(parsed.displayText)
                            } catch (e: Exception) {
                                errorMsg = "识别失败：${e.message}"
                            } finally {
                                isAnalyzing = false
                            }
                        }
                    }
                )
            } else {
                // Show captured image
                Image(
                    bitmap = capturedBitmap!!.asImageBitmap(),
                    contentDescription = "拍摄的药品照片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Text("AI 正在识别药品...", style = MaterialTheme.typography.bodyLarge)
                }

                errorMsg?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DangerBackground)
                    ) {
                        Text(msg, modifier = Modifier.padding(16.dp), color = DangerRed)
                    }
                }

                result?.let { med ->
                    // Drug name - large
                    ResultCard(
                        title = "药品名称",
                        content = med.name.ifBlank { "未能识别" },
                        dangerLevel = 1
                    )

                    if (med.dosage.isNotBlank()) {
                        ResultCard(
                            title = "怎么吃",
                            content = med.dosage,
                            dangerLevel = 1
                        )
                    }

                    if (med.warning.isNotBlank()) {
                        ResultCard(
                            title = "注意事项",
                            content = med.warning,
                            dangerLevel = 3
                        )
                    }

                    // Drug interaction warnings
                    if (interactions.isNotEmpty()) {
                        for (interaction in interactions) {
                            DangerBanner(
                                message = "药物冲突警告：与${interaction.drugB}有冲突 - ${interaction.description}"
                            )
                        }
                    }

                    DisclaimerBar()
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            ttsHelper.stop()
                            ttsHelper.speak(med.displayText)
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "朗读", modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("🔊 大声朗读药品用法", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Retake button
                Button(
                    onClick = {
                        capturedBitmap = null
                        result = null
                        interactions = emptyList()
                        errorMsg = null
                        ttsHelper.stop()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, null)
                    Spacer(Modifier.width(8.dp))
                    Text("重新拍照")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CameraPreviewSection(
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService,
    onCapture: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "对准药盒或说明书拍照",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )

        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            // Camera init failed
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Button(
            onClick = {
                imageCapture.takePicture(
                    cameraExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = image.toBitmap()
                            image.close()
                            onCapture(bitmap)
                        }
                        override fun onError(exc: ImageCaptureException) {
                            // Handle error
                        }
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text("拍照识别", style = MaterialTheme.typography.labelLarge)
        }
    }
}
