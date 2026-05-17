package com.cunyi.gemma.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class DownloadState {
    object Idle : DownloadState()
    data class Progress(val progressPercent: Float, val downloadedBytes: Long, val totalBytes: Long) : DownloadState()
    data class Success(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class ModelDownloader(private val context: Context) {

    // Expected file sizes for post-download verification
    private val EXPECTED_SIZES = mapOf(
        "gemma3-270m-it-q8.litertlm" to 304005120L,
        "gemma-4-E2B-it.litertlm" to 2588147712L,
    )

    /**
     * Downloads a model file from [url] to the internal files folder.
     * Supports resume if the file was partially downloaded.
     * Supports both Wi-Fi and mobile data (for rural users without stable Wi-Fi).
     */
    fun downloadModel(url: String, fileName: String): Flow<DownloadState> = flow {
        val destinationFile = File(context.filesDir, fileName)
        var downloadedBytes = if (destinationFile.exists()) destinationFile.length() else 0L

        try {
            var finalUrl = url
            var connection: HttpURLConnection

            // Follow HTTP redirects (up to 10 hops)
            var redirects = 0
            while (true) {
                connection = URL(finalUrl).openConnection() as HttpURLConnection
                if (downloadedBytes > 0) {
                    connection.setRequestProperty("Range", "bytes=$downloadedBytes-")
                }
                connection.connectTimeout = 30000
                connection.readTimeout = 60000
                connection.instanceFollowRedirects = false
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                    responseCode == 307 || responseCode == 308) {

                    val newUrl = connection.getHeaderField("Location")
                    if (newUrl != null && redirects < 10) {
                        finalUrl = newUrl
                        redirects++
                        continue
                    }
                }
                break
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                if (responseCode == HttpURLConnection.HTTP_OK && downloadedBytes > 0) {
                    // Server doesn't support range requests for this URL, restart download
                    Log.d("ModelDownloader", "Server returned HTTP_OK with existing partial file, restarting")
                    downloadedBytes = 0L
                    destinationFile.delete()
                }

                val contentLength = connection.contentLengthCompat
                val totalBytes = if (contentLength != -1L) downloadedBytes + contentLength else -1L

                connection.inputStream.use { input ->
                    FileOutputStream(destinationFile, downloadedBytes > 0).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var lastEmitTime = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            val currentTime = System.currentTimeMillis()
                            if (totalBytes > 0 && currentTime - lastEmitTime > 500) {
                                lastEmitTime = currentTime
                                val percent = (downloadedBytes.toFloat() / totalBytes) * 100f
                                emit(DownloadState.Progress(percent, downloadedBytes, totalBytes))
                            }
                        }
                    }
                }

                // Post-download verification
                val finalSize = destinationFile.length()
                val expectedSize = EXPECTED_SIZES[fileName]

                if (expectedSize != null && finalSize < expectedSize * 90 / 100) {
                    val sizeMb = finalSize / (1024.0 * 1024.0)
                    val expMb = expectedSize / (1024.0 * 1024.0)
                    Log.e("ModelDownloader", "Download incomplete: got ${finalSize} bytes, expected ~${expectedSize}")
                    emit(DownloadState.Error(
                        "⚠️ 下载不完整！\n已下载：${String.format("%.1f", sizeMb)} MB\n应有：${String.format("%.1f", expMb)} MB\n\n网络可能中断了，请重新下载。\n建议在Wi-Fi环境下下载。"
                    ))
                    return@flow
                }

                Log.d("ModelDownloader", "Download complete: ${destinationFile.name} (${finalSize} bytes)")
                emit(DownloadState.Success(destinationFile))

            } else if (responseCode == 416) {
                // Range not satisfiable - file already fully downloaded
                emit(DownloadState.Success(destinationFile))
            } else {
                val body = try { connection.errorStream?.bufferedReader()?.readText()?.take(200) ?: "" } catch (_: Exception) { "" }
                Log.e("ModelDownloader", "HTTP error $responseCode: $body")
                emit(DownloadState.Error("服务器错误：$responseCode\n\n$body"))
            }

        } catch (e: Exception) {
            Log.e("ModelDownloader", "Download failed", e)
            val userMsg = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> "网络超时，请检查网络连接后重试"
                e.message?.contains("ECONNREFUSED", ignoreCase = true) == true ||
                e.message?.contains("ENETUNREACH", ignoreCase = true) == true -> "网络不可达，请确认已开启移动数据或连接Wi-Fi"
                e.message?.contains("SSL", ignoreCase = true) == true -> "SSL证书问题，请稍后重试"
                e.message?.contains("ENOENT", ignoreCase = true) == true ||
                e.message?.contains("No space left", ignoreCase = true) == true -> "手机存储空间不足！\n请清理一些空间后再下载（需要至少预留模型大小2倍的空间）"
                else -> "下载失败：${e.message ?: "未知错误"}\n\n请检查网络连接后重试"
            }
            emit(DownloadState.Error(userMsg))
        }

    }.flowOn(Dispatchers.IO)

}

private val HttpURLConnection.contentLengthCompat: Long
    get() {
        return try {
            getHeaderField("Content-Length")?.toLong() ?: -1L
        } catch (e: NumberFormatException) {
            -1L
        }
    }
