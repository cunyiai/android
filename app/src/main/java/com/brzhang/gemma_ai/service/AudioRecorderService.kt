package com.brzhang.gemma_ai.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Records PCM audio for Gemma voice input.
 * 16kHz, 16-bit, mono - optimal for speech recognition.
 */
class AudioRecorderService(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    val bufferSize: Int = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Suppress("MissingPermission")
    suspend fun startRecording(): ByteArray = withContext(Dispatchers.IO) {
        val outputStream = ByteArrayOutputStream()
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val buffer = ByteArray(minBufferSize)

        android.util.Log.d("AudioRecorder", "Recording started. SampleRate: $sampleRate")

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            minBufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            android.util.Log.e("AudioRecorder", "Failed to initialize AudioRecord")
            return@withContext ByteArray(0)
        }

        audioRecord?.startRecording()
        isRecording = true

        while (isRecording) {
            val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (read > 0) {
                outputStream.write(buffer, 0, read)
            } else if (read < 0) {
                android.util.Log.e("AudioRecorder", "AudioRecord read error: $read")
                break
            }
        }

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        var pcmData = outputStream.toByteArray()
        val minPcmSize = sampleRate * 1 * 2 * 1.5 // 1.5 seconds minimum (16k * 1ch * 2bytes * 1.5s = 48000 bytes)
        
        if (pcmData.size < minPcmSize) {
            android.util.Log.w("AudioRecorder", "Audio too short: ${pcmData.size} bytes. Padding to 1.5s...")
            val paddedPcm = ByteArray(minPcmSize.toInt())
            System.arraycopy(pcmData, 0, paddedPcm, 0, pcmData.size)
            pcmData = paddedPcm
        }

        android.util.Log.d("AudioRecorder", "Recording finished. Final PCM size: ${pcmData.size} bytes")
        addWavHeader(pcmData)
    }

    private fun addWavHeader(pcmData: ByteArray): ByteArray {
        val header = ByteArray(44)
        val pcmDataSize = pcmData.size
        val wavFileSize = pcmDataSize + 44
        val channels = 1 // Mono
        val bitsPerSample: Short = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8

        // RIFF/WAVE header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (wavFileSize and 0xff).toByte()
        header[5] = (wavFileSize shr 8 and 0xff).toByte()
        header[6] = (wavFileSize shr 16 and 0xff).toByte()
        header[7] = (wavFileSize shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0 // Sub-chunk size (16 for PCM)
        header[20] = 1
        header[21] = 0 // Audio format (1 for PCM)
        header[22] = channels.toByte()
        header[23] = 0 // Number of channels
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte()
        header[33] = 0 // Block align
        header[34] = bitsPerSample.toByte()
        header[35] = (bitsPerSample.toInt() shr 8 and 0xff).toByte() // Bits per sample
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmDataSize and 0xff).toByte()
        header[41] = (pcmDataSize shr 8 and 0xff).toByte()
        header[42] = (pcmDataSize shr 16 and 0xff).toByte()
        header[43] = (pcmDataSize shr 24 and 0xff).toByte()

        return header + pcmData
    }

    fun stopRecording() {
        isRecording = false
    }

    fun isCurrentlyRecording(): Boolean = isRecording
}
