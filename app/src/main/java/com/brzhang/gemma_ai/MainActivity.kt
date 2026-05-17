package com.brzhang.gemma_ai

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brzhang.gemma_ai.ai.GemmaManager
import com.brzhang.gemma_ai.data.db.AppDatabase
import com.brzhang.gemma_ai.data.db.DrugDatabase
import com.brzhang.gemma_ai.data.entity.UserProfile
import com.brzhang.gemma_ai.ui.health.HealthScreen
import com.brzhang.gemma_ai.ui.home.HomeScreen
import com.brzhang.gemma_ai.ui.medicine.MedicineScreen
import com.brzhang.gemma_ai.ui.setup.SetupScreen
import com.brzhang.gemma_ai.network.ModelDownloader
import com.brzhang.gemma_ai.network.DownloadState
import com.brzhang.gemma_ai.ui.sos.SosScreen
import com.brzhang.gemma_ai.ui.theme.VillageDocTheme
import com.brzhang.gemma_ai.ui.voice.VoiceConsultScreen
import com.brzhang.gemma_ai.util.TtsHelper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    private val gemmaManager = GemmaManager()
    private lateinit var db: AppDatabase
    private lateinit var drugDb: DrugDatabase
    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        db = AppDatabase.getInstance(this)
        drugDb = DrugDatabase(this)
        ttsHelper = TtsHelper(this)

        // Initialize AI model in background (tries ML Kit first, then MediaPipe)
        lifecycleScope.launch {
            gemmaManager.initialize(this@MainActivity)
        }

        setContent {
            VillageDocTheme {
                val navController = rememberNavController()
                val modelStatus by gemmaManager.status.collectAsState()
                val modelError by gemmaManager.errorMessage.collectAsState()
                var userProfile by remember { mutableStateOf<UserProfile?>(null) }

                // Load user profile
                LaunchedEffect(Unit) {
                    userProfile = db.userProfileDao().getProfile()
                }

                val startDest = if (userProfile == null) "setup" else "home"

                var gemmaDownloadProgress by remember { mutableStateOf<Float?>(null) }
                val modelDownloader = remember { ModelDownloader(this@MainActivity) }

                val startDownload: (String, String) -> Unit = { url, fileName ->
                    lifecycleScope.launch {
                        modelDownloader.downloadModel(url, fileName).collect { state ->
                            when (state) {
                                is DownloadState.Progress -> {
                                    gemmaDownloadProgress = state.progressPercent
                                }
                                is DownloadState.Success -> {
                                    gemmaDownloadProgress = null
                                    gemmaManager.initializeWithModelPath(this@MainActivity, state.file.absolutePath)
                                }
                                is DownloadState.Error -> {
                                    gemmaDownloadProgress = null
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_LONG).show()
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = startDest
                ) {
                    composable("setup") {
                        var downloadedModels by remember { mutableStateOf(emptyList<File>()) }
                        var activeModelPath by remember { mutableStateOf<String?>(null) }
                        
                        LaunchedEffect(Unit) {
                            withContext(Dispatchers.IO) {
                                downloadedModels = filesDir.listFiles { _, name -> name.endsWith(".litertlm") }?.toList() ?: emptyList()
                                val prefs = getSharedPreferences("ai_settings", Context.MODE_PRIVATE)
                                activeModelPath = prefs.getString("active_model_path", null)
                            }
                        }

                        SetupScreen(
                            initialProfile = userProfile,
                            downloadedModels = downloadedModels,
                            activeModelPath = activeModelPath,
                            onActivateModel = { file ->
                                activeModelPath = file.absolutePath
                                val prefs = getSharedPreferences("ai_settings", Context.MODE_PRIVATE)
                                prefs.edit().putString("active_model_path", file.absolutePath).apply()
                                
                                lifecycleScope.launch {
                                    Toast.makeText(this@MainActivity, "正在为您切换并挂载：${file.name}...", Toast.LENGTH_SHORT).show()
                                    gemmaManager.initializeWithModelPath(this@MainActivity, file.absolutePath)
                                }
                            },
                            onDeleteModel = { file ->
                                file.delete()
                                downloadedModels = filesDir.listFiles { _, name -> name.endsWith(".litertlm") }?.toList() ?: emptyList()
                                if (activeModelPath == file.absolutePath) {
                                    activeModelPath = null
                                    val prefs = getSharedPreferences("ai_settings", Context.MODE_PRIVATE)
                                    prefs.edit().remove("active_model_path").apply()
                                    // Could theoretically inform GemmaManager to stop, but fine to just delete
                                }
                                Toast.makeText(this@MainActivity, "已成功删除 ${file.name}", Toast.LENGTH_SHORT).show()
                            },
                            onComplete = { name, age, conditions, contacts, village ->
                                lifecycleScope.launch {
                                    val condList = conditions.split("、", "，", ",")
                                        .map { it.trim() }.filter { it.isNotEmpty() }
                                    val contactList = contacts.split(",")
                                        .map { it.trim() }.filter { it.isNotEmpty() }
                                    val profile = UserProfile(
                                        name = name,
                                        age = age,
                                        chronicConditions = Gson().toJson(condList),
                                        emergencyContacts = Gson().toJson(contactList),
                                        village = village
                                    )
                                    db.userProfileDao().upsert(profile)
                                    userProfile = profile
                                    navController.navigate("home") {
                                        popUpTo("setup") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            modelStatus = modelStatus,
                            errorMessage = modelError,
                            onVoiceConsult = { navController.navigate("voice") },
                            onMedicineId = { navController.navigate("medicine") },
                            onHealthRecords = { navController.navigate("health") },
                            onSos = { navController.navigate("sos") },
                            onSettings = { navController.navigate("setup") },
                            downloadProgress = gemmaDownloadProgress,
                            onDownloadModel = startDownload
                        )
                    }

                    composable("voice") {
                        VoiceConsultScreen(
                            gemmaManager = gemmaManager,
                            db = db,
                            ttsHelper = ttsHelper,
                            userProfile = userProfile,
                            onBack = { navController.popBackStack() },
                            onSos = { navController.navigate("sos") }
                        )
                    }

                    composable("medicine") {
                        MedicineScreen(
                            gemmaManager = gemmaManager,
                            drugDb = drugDb,
                            userProfile = userProfile,
                            ttsHelper = ttsHelper,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("health") {
                        HealthScreen(
                            db = db,
                            gemmaManager = gemmaManager,
                            userProfile = userProfile,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("sos") {
                        SosScreen(
                            gemmaManager = gemmaManager,
                            db = db,
                            userProfile = userProfile,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gemmaManager.close()
        ttsHelper.shutdown()
    }
}
