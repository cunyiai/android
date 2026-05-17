package com.brzhang.gemma_ai.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

/**
 * Dual-backend AI manager:
 * 1. ML Kit GenAI Prompt API (AICore devices: Pixel 9+, Samsung S25, OPPO Find X8 etc.)
 * 2. MediaPipe LLM Inference (all other devices, e.g. OPPO Find X7)
 */
class GemmaManager {

    private var activeBackend: InferenceBackend? = null

    private val _status = MutableStateFlow(ModelStatus.INITIALIZING)
    val status: StateFlow<ModelStatus> = _status

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress

    private var _backendName = "none"
    val backendName: String get() = _backendName

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    var lastResponse: String = ""

    // Expected model file sizes (in bytes) - accept a RANGE (+-10%) for each model
    // Gemma 3: 290MB (q8 quantized) OR 1573MB (full precision from HuggingFace)
    // Gemma 4: 2467MB (E2B)
    private val EXPECTED_SIZE_RANGES = listOf(
        290L * 1024 * 1024 to 310L * 1024 * 1024,      // gemma3 q8 ~290-310MB
        1500L * 1024 * 1024 to 1600L * 1024 * 1024,   // gemma3 full ~1.5-1.6GB
        2400L * 1024 * 1024 to 2600L * 1024 * 1024,   // gemma4 E2B ~2.4-2.6GB
    )

    enum class ModelStatus {
        INITIALIZING, DOWNLOADING, COPYING, READY, UNAVAILABLE, NEED_MODEL_FILE, ERROR
    }

    suspend fun initialize(context: Context) {
        _status.value = ModelStatus.INITIALIZING
        if (tryInitMlKit()) return
        if (tryInitLiteRT(context)) return
        _status.value = ModelStatus.NEED_MODEL_FILE
        Log.w(TAG, "No backend available. Need Gemma model file for LiteRT.")
    }

    suspend fun initializeWithModelPath(context: Context, modelPath: String) {
        _errorMessage.value = null
        _status.value = ModelStatus.COPYING
        withContext(Dispatchers.IO) {
            try {
                val file = File(modelPath)
                if (!file.exists()) {
                    _errorMessage.value = "模型文件不存在：${file.name}\n\n请重新下载"
                    _status.value = ModelStatus.ERROR
                    return@withContext
                }

                val fileSize = file.length()
                val fileName = file.name
                Log.d(TAG, "Loading model: $fileName ($fileSize bytes)")

                // Step 1: Check file size (accept any known range)
                val inRange = EXPECTED_SIZE_RANGES.any { (min, max) -> fileSize in min..max }
                val tooSmall = fileSize < 10 * 1024 * 1024  // < 10MB is definitely wrong

                if (tooSmall) {
                    val sizeMb = fileSize / (1024.0 * 1024.0)
                    _errorMessage.value = "⚠️ 模型文件太小（${String.format("%.1f", sizeMb)} MB）\n文件可能已损坏，请删除后重新下载"
                    _status.value = ModelStatus.ERROR
                    return@withContext
                }

                if (!inRange) {
                    val sizeMb = fileSize / (1024.0 * 1024.0)
                    Log.w(TAG, "Model size $sizeMb MB is not in any expected range, but trying anyway...")
                    // Don't reject - just warn. Maybe it's a new model variant.
                }

                // Step 2: Try loading with different backend configurations
                _status.value = ModelStatus.INITIALIZING
                var lastError: String? = null

                // Strategy A: CPU only (most compatible)
                try {
                    Log.d(TAG, "Trying CPU-only backend...")
                    loadEngine(modelPath, Backend.CPU(), Backend.CPU(), Backend.CPU(), context.cacheDir.absolutePath)
                    _status.value = ModelStatus.READY
                    saveActiveModel(context, modelPath)
                    Log.d(TAG, "Model loaded successfully with CPU-only backend")
                    return@withContext
                } catch (e: Exception) {
                    lastError = e.message ?: e.toString()
                    Log.w(TAG, "CPU-only backend failed: $lastError")
                }

                // Strategy B: CPU+GPU (better performance if supported)
                try {
                    Log.d(TAG, "Trying CPU+GPU backend...")
                    loadEngine(modelPath, Backend.CPU(), Backend.GPU(), Backend.CPU(), context.cacheDir.absolutePath)
                    _status.value = ModelStatus.READY
                    saveActiveModel(context, modelPath)
                    Log.d(TAG, "Model loaded successfully with CPU+GPU backend")
                    return@withContext
                } catch (e: Exception) {
                    Log.w(TAG, "CPU+GPU backend failed: ${e.message}")
                    if (lastError == null) lastError = e.message ?: e.toString()
                }

                // All strategies failed
                val hint = buildString {
                    append("模型加载失败。\n\n")
                    append("技术原因：${lastError?.take(120) ?: "未知"}\n\n")
                    append("可能的解决办法：\n")
                    append("• 确认手机存储空间充足（至少预留模型大小2倍的空间）\n")
                    append("• 删除当前模型文件后重新下载\n")
                    append("• 重启手机后再试\n")
                    append("• 如果持续失败，可能是您的设备不支持此模型格式")
                }
                _errorMessage.value = hint
                _status.value = ModelStatus.ERROR

            } catch (e: Exception) {
                val msg = e.message ?: e.toString()
                Log.e(TAG, "Unexpected error loading model: $msg", e)
                _errorMessage.value = "加载出错：${msg.take(150)}\n\n请重启应用或重新下载模型"
                _status.value = ModelStatus.ERROR
            }
        }
    }

    private fun loadEngine(
        modelPath: String,
        backend: Backend,
        visionBackend: Backend,
        audioBackend: Backend,
        cacheDir: String
    ) {
        val config = EngineConfig(
            modelPath = modelPath,
            backend = backend,
            visionBackend = visionBackend,
            audioBackend = audioBackend,
            maxNumTokens = 2048,
            cacheDir = cacheDir
        )
        val engine = Engine(config)
        engine.initialize()

        activeBackend = LiteRTBackend(engine)
        _backendName = "LiteRT (${File(modelPath).name})"
    }

    private fun saveActiveModel(context: Context, modelPath: String) {
        val prefs = context.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("active_model_path", modelPath).apply()
    }

    private suspend fun tryInitMlKit(): Boolean {
        return try {
            val client = Generation.getClient()
            val featureStatus = client.checkStatus()

            when (featureStatus) {
                FeatureStatus.AVAILABLE -> {
                    client.warmup()
                    activeBackend = MlKitBackend(client)
                    _backendName = "ML Kit AICore"
                    _status.value = ModelStatus.READY
                    Log.d(TAG, "ML Kit backend ready")
                    true
                }
                FeatureStatus.DOWNLOADABLE -> {
                    _status.value = ModelStatus.DOWNLOADING
                    client.download().collect { ds ->
                        when (ds) {
                            is DownloadStatus.DownloadProgress -> {
                                _downloadProgress.value = ds.totalBytesDownloaded.toFloat()
                            }
                            is DownloadStatus.DownloadCompleted -> {
                                client.warmup()
                                activeBackend = MlKitBackend(client)
                                _backendName = "ML Kit AICore"
                                _status.value = ModelStatus.READY
                                Log.d(TAG, "ML Kit model downloaded and ready")
                            }
                            is DownloadStatus.DownloadFailed -> {
                                Log.w(TAG, "ML Kit download failed")
                            }
                            else -> {}
                        }
                    }
                    _status.value == ModelStatus.READY
                }
                else -> {
                    Log.d(TAG, "ML Kit AICore not available on this device")
                    false
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "ML Kit init failed: ${e.message}")
            false
        }
    }

    private suspend fun tryInitLiteRT(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val modelFile = findModelFile(context) ?: return@withContext false

                val engineConfig = EngineConfig(
                    modelPath = modelFile.absolutePath,
                    backend = Backend.CPU(),
                    visionBackend = Backend.CPU(),
                    audioBackend = Backend.CPU(),
                    maxNumTokens = 2048,
                    cacheDir = context.cacheDir.absolutePath
                )
                val engine = Engine(engineConfig)
                engine.initialize()

                activeBackend = LiteRTBackend(engine)
                _backendName = "LiteRT (${modelFile.name})"
                _status.value = ModelStatus.READY
                Log.d(TAG, "LiteRT backend ready with ${modelFile.name}")
                true
            } catch (e: Exception) {
                Log.w(TAG, "LiteRT init failed: ${e.message}")
                false
            }
        }
    }

    private fun findModelFile(context: Context): File? {
        val prefs = context.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)
        val activePath = prefs.getString("active_model_path", null)
        if (activePath != null) {
            val file = File(activePath)
            if (file.exists() && file.isFile) {
                return file
            }
        }

        val searchDirs = listOf(
            context.filesDir,
            File(context.filesDir, "models"),
            context.getExternalFilesDir("models"),
            File("/sdcard/Download"),
        )

        for (dir in searchDirs) {
            if (dir == null || !dir.exists()) continue
            val files = dir.listFiles() ?: continue
            for (file in files) {
                if (file.name.endsWith(".litertlm", ignoreCase = true)) {
                    return file
                }
            }
        }

        return null
    }

    // ============= Inference API =============

    suspend fun inferText(prompt: Pair<String, String>): String {
        val backend = activeBackend ?: throw IllegalStateException("Model not ready")
        return withTimeout(TIMEOUT_MS) {
            val result = backend.generateText(prompt)
            lastResponse = result
            result
        }
    }

    suspend fun inferText(prompt: String): String =
        inferText(PromptTemplates.SYSTEM_PROMPT to prompt)

    suspend fun inferWithImage(prompt: Pair<String, String>, image: Bitmap): String {
        val backend = activeBackend ?: throw IllegalStateException("Model not ready")
        return withTimeout(TIMEOUT_MS) {
            val result = backend.generateWithImage(prompt, image)
            lastResponse = result
            result
        }
    }

    suspend fun inferWithAudio(prompt: Pair<String, String>, audioBytes: ByteArray): String {
        val backend = activeBackend ?: throw IllegalStateException("Model not ready")
        return withTimeout(TIMEOUT_MS) {
            val result = backend.generateWithAudio(prompt, audioBytes)
            lastResponse = result
            result
        }
    }

    suspend fun inferWithImage(prompt: String, image: Bitmap): String =
        inferWithImage(PromptTemplates.SYSTEM_PROMPT to prompt, image)

    suspend fun inferWithAudio(prompt: String, audioBytes: ByteArray): String =
        inferWithAudio(PromptTemplates.SYSTEM_PROMPT to prompt, audioBytes)

    fun isReady(): Boolean = _status.value == ModelStatus.READY

    fun close() {
        activeBackend?.close()
        activeBackend = null
    }

    // ============= Backend implementations =============

    private interface InferenceBackend {
        suspend fun generateText(prompt: Pair<String, String>): String
        suspend fun generateWithImage(prompt: Pair<String, String>, image: Bitmap): String
        suspend fun generateWithAudio(prompt: Pair<String, String>, audioBytes: ByteArray): String
        fun close()
    }

    private class MlKitBackend(private val model: GenerativeModel) : InferenceBackend {

        override suspend fun generateText(prompt: Pair<String, String>): String {
            val fullPrompt = "${prompt.first}\n\n${prompt.second}"
            val response = model.generateContent(fullPrompt)
            return response.candidates.firstOrNull()?.text ?: ""
        }

        override suspend fun generateWithImage(prompt: Pair<String, String>, image: Bitmap): String {
            val fullPrompt = "${prompt.first}\n\n${prompt.second}"
            val request = generateContentRequest(ImagePart(image), TextPart(fullPrompt)) {}
            val response = model.generateContent(request)
            return response.candidates.firstOrNull()?.text ?: ""
        }

        override suspend fun generateWithAudio(prompt: Pair<String, String>, audioBytes: ByteArray): String {
            return generateText(prompt.first to "${prompt.second}\n[Audio payload attached, but API limit fallback to text summary]")
        }

        override fun close() {
            model.close()
        }
    }

    private class LiteRTBackend(private val engine: Engine) : InferenceBackend {
        private var lastSystemPrompt: String? = null
        private var conversation: Conversation? = null

        private fun getOrCreateConversation(systemPrompt: String): Conversation {
            if (conversation != null && lastSystemPrompt == systemPrompt) {
                return conversation!!
            }
            conversation?.close()
            lastSystemPrompt = systemPrompt
            conversation = engine.createConversation(
                ConversationConfig(
                    systemInstruction = Contents.of(Content.Text(systemPrompt))
                )
            )
            return conversation!!
        }

        override suspend fun generateText(prompt: Pair<String, String>): String {
            return withContext(Dispatchers.IO) {
                val conv = getOrCreateConversation(prompt.first)
                executeSendMessage(conv, Contents.of(Content.Text(prompt.second)))
            }
        }

        override suspend fun generateWithImage(prompt: Pair<String, String>, image: Bitmap): String {
            return withContext(Dispatchers.IO) {
                val conv = getOrCreateConversation(prompt.first)
                val bos = java.io.ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.PNG, 100, bos)
                val imageBytes = bos.toByteArray()

                val contents = listOf(
                    Content.ImageBytes(imageBytes),
                    Content.Text(prompt.second)
                )
                executeSendMessage(conv, Contents.of(contents))
            }
        }

        override suspend fun generateWithAudio(prompt: Pair<String, String>, audioBytes: ByteArray): String {
            return withContext(Dispatchers.IO) {
                val conv = getOrCreateConversation(prompt.first)
                val contents = listOf(
                    Content.AudioBytes(audioBytes),
                    Content.Text(prompt.second)
                )
                executeSendMessage(conv, Contents.of(contents))
            }
        }

        private suspend fun executeSendMessage(conv: Conversation, contents: Contents): String {
            return callbackFlow {
                conv.sendMessageAsync(contents, object : MessageCallback {
                    val responseText = StringBuilder()
                    override fun onMessage(message: Message) {
                        val chunkText = message.contents.contents
                            .filterIsInstance<Content.Text>()
                            .joinToString("") { it.text }
                        responseText.append(chunkText)
                        trySend(responseText.toString())
                    }

                    override fun onDone() {
                        close()
                    }

                    override fun onError(throwable: Throwable) {
                        Log.e("LiteRTBackend", "Inference error", throwable)
                        close(throwable)
                    }
                })
                awaitClose()
            }.last()
        }

        override fun close() {
            conversation?.close()
            engine.close()
        }
    }

    companion object {
        private const val TAG = "GemmaManager"
        private const val TIMEOUT_MS = 300_000L
    }
}
