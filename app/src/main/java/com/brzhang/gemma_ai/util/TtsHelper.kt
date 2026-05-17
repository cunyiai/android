package com.brzhang.gemma_ai.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale

/**
 * TTS helper for reading AI responses aloud to elderly users.
 * Optimized for Chinese domestic ROMs (OPPO, Xiaomi etc.)
 */
class TtsHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    init {
        Log.d(TAG, "Starting TTS Engine initialization...")
        // On some devices, explicit engine selection might be needed, 
        // but default is usually best unless it's missing.
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS native service connected successfully.")
            
            // Try different variants of Chinese locale
            val locales = listOf(Locale.CHINESE, Locale.CHINA, Locale.SIMPLIFIED_CHINESE)
            var supported = false
            
            for (loc in locales) {
                val result = tts?.setLanguage(loc)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.d(TAG, "Successfully set TTS language to: $loc")
                    supported = true
                    break
                }
            }

            if (!supported) {
                Log.e(TAG, "No Chinese language support found in any variant.")
                showToast("您的手机暂不支持中文朗读，请在设置中下载语音包")
                isReady = false
            } else {
                tts?.setSpeechRate(0.8f) // Slower for elderly
                tts?.setPitch(1.0f)
                isReady = true
                Log.d(TAG, "TTS Engine is now READY")
                
                // Speak pending text if any
                pendingText?.let {
                    Log.d(TAG, "Speaking buffered text...")
                    speak(it)
                    pendingText = null
                }
            }
            
            // Log available engines for diagnosis
            tts?.engines?.forEach { 
                Log.d(TAG, "Found TTS engine: ${it.name} (${it.label})")
            }
        } else {
            Log.e(TAG, "TTS Initialization failed with error status: $status")
            showToast("语音助手启动失败 (错误码: $status)")
            isReady = false
        }
    }

    private fun showToast(msg: String) {
        try {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // Ignore if context is invalid
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        
        if (!isReady) {
            Log.w(TAG, "TTS not ready, buffering request...")
            pendingText = text
            return
        }

        Log.d(TAG, "Attempting to speak text: ${text.take(30)}...")
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "village_doc_tts")
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "TTS speak() returned ERROR")
            showToast("朗读失败，请检查系统语音设置")
        }
    }

    fun stop() {
        tts?.stop()
        pendingText = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
        pendingText = null
    }

    companion object {
        private const val TAG = "TtsHelper"
    }
}
