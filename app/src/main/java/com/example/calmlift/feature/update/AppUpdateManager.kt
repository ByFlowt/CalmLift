package com.example.calmlift.feature.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.example.calmlift.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class UpdateInfo(
    val tagName: String,
    val versionName: String,
    val releaseNotes: String,
    val assetId: Long,
    val assetName: String,
    val assetSize: Long
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class UpToDate(val currentVersion: String) : UpdateState
    data class Available(val updateInfo: UpdateInfo) : UpdateState
    data class Downloading(val progress: Float) : UpdateState
    data class ReadyToInstall(val apkFile: File) : UpdateState
    data class Error(val message: String) : UpdateState
}

object AppUpdateManager {

    private const val TAG = "CalmLiftUpdateManager"
    private const val RELEASES_URL = "https://api.github.com/repos/ByFlowt/CalmLift/releases"

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    private var downloadJob: Job? = null

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _updateState.value = UpdateState.Idle
    }

    /**
     * Checks if a valid, newer update APK is already cached on disk.
     */
    fun checkDownloadedApk(context: Context) {
        if (_updateState.value is UpdateState.Downloading) return
        val updatesDir = File(context.cacheDir, "updates")
        if (!updatesDir.exists()) return
        val files = updatesDir.listFiles { f -> f.extension.equals("apk", ignoreCase = true) } ?: return
        val latestApk = files.maxByOrNull { it.lastModified() }
        if (latestApk != null && latestApk.exists() && latestApk.length() > 0) {
            val pkgInfo = context.packageManager.getPackageArchiveInfo(latestApk.absolutePath, 0)
            if (pkgInfo != null && pkgInfo.packageName == context.packageName && isNewerVersion(pkgInfo.versionName ?: "", BuildConfig.VERSION_NAME)) {
                _updateState.value = UpdateState.ReadyToInstall(latestApk)
            } else {
                files.forEach { it.delete() }
            }
        }
    }

    /**
     * Query GitHub Releases for the latest CalmLift APK.
     */
    suspend fun checkForUpdates(): UpdateState = withContext(Dispatchers.IO) {
        _updateState.value = UpdateState.Checking
        try {
            val url = URL(RELEASES_URL)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("Authorization", "Bearer ${GitHubAuth.resolveAuth()}")
                setRequestProperty("User-Agent", "CalmLift-App")
                connectTimeout = 15000
                readTimeout = 15000
            }

            if (conn.responseCode !in 200..299) {
                val err = "GitHub Releases HTTP ${conn.responseCode}: ${conn.responseMessage}"
                Log.w(TAG, err)
                val state = UpdateState.Error(err)
                _updateState.value = state
                return@withContext state
            }

            val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val releases = JSONArray(body)
            if (releases.length() == 0) {
                val state = UpdateState.UpToDate(BuildConfig.VERSION_NAME)
                _updateState.value = state
                return@withContext state
            }

            var bestUpdateInfo: UpdateInfo? = null
            var bestVersion: String? = null

            for (i in 0 until releases.length()) {
                val rel = releases.getJSONObject(i)
                if (rel.optBoolean("draft", false)) continue

                val relTagName = rel.optString("tag_name", "")
                val relVersion = relTagName.removePrefix("v").removePrefix("V").substringBefore("-")
                if (relVersion.isBlank()) continue

                if (!isNewerVersion(relVersion, BuildConfig.VERSION_NAME)) continue
                if (bestVersion != null && !isNewerVersion(relVersion, bestVersion)) continue

                val assets = rel.optJSONArray("assets") ?: JSONArray()
                if (assets.length() == 0) continue

                var assetId = -1L
                var assetName = ""
                var assetSize = 0L

                // Prioritize official production release APK
                for (j in 0 until assets.length()) {
                    val asset = assets.getJSONObject(j)
                    val name = asset.optString("name", "")
                    if (!name.endsWith(".apk", ignoreCase = true)) continue
                    if (name.contains("wear", ignoreCase = true)) continue
                    if (name.contains("debug", ignoreCase = true)) continue

                    assetId = asset.optLong("id", -1)
                    assetName = name
                    assetSize = asset.optLong("size", 0)
                    break
                }

                // Fallback to any APK if release name varies
                if (assetId == -1L) {
                    for (j in 0 until assets.length()) {
                        val asset = assets.getJSONObject(j)
                        val name = asset.optString("name", "")
                        if (!name.endsWith(".apk", ignoreCase = true)) continue
                        if (name.contains("wear", ignoreCase = true)) continue

                        assetId = asset.optLong("id", -1)
                        assetName = name
                        assetSize = asset.optLong("size", 0)
                        break
                    }
                }

                if (assetId != -1L) {
                    bestVersion = relVersion
                    bestUpdateInfo = UpdateInfo(
                        tagName = relTagName,
                        versionName = relVersion,
                        releaseNotes = rel.optString("body", ""),
                        assetId = assetId,
                        assetName = assetName,
                        assetSize = assetSize
                    )
                }
            }

            val state = if (bestUpdateInfo != null) {
                UpdateState.Available(bestUpdateInfo)
            } else {
                UpdateState.UpToDate(BuildConfig.VERSION_NAME)
            }
            _updateState.value = state
            state
        } catch (e: Exception) {
            Log.e(TAG, "Update check failed", e)
            val state = UpdateState.Error(e.message ?: "Update check failed")
            _updateState.value = state
            state
        }
    }

    /**
     * Downloads APK from GitHub Releases (handling 302 S3 redirect safely) and triggers install.
     */
    suspend fun downloadAndInstall(context: Context, updateInfo: UpdateInfo) = withContext(Dispatchers.IO) {
        _updateState.value = UpdateState.Downloading(0f)
        downloadJob = coroutineContext[Job]
        try {
            val assetApiUrl = "https://api.github.com/repos/ByFlowt/CalmLift/releases/assets/${updateInfo.assetId}"
            val initialConn = (URL(assetApiUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = false
                setRequestProperty("Accept", "application/octet-stream")
                setRequestProperty("Authorization", "Bearer ${GitHubAuth.resolveAuth()}")
                setRequestProperty("User-Agent", "CalmLift-App")
                connectTimeout = 15000
                readTimeout = 15000
            }

            if (initialConn.responseCode !in 200..399) {
                throw java.io.IOException("GitHub asset HTTP ${initialConn.responseCode}: ${initialConn.responseMessage}")
            }

            // S3 Redirect Handling: Never forward GitHub Authorization header to S3
            val redirectUrlStr = if (initialConn.responseCode in 300..399) {
                val loc = initialConn.getHeaderField("Location")
                if (loc != null && !loc.startsWith("https://", ignoreCase = true)) {
                    throw SecurityException("Insecure HTTP redirect rejected for update asset download")
                }
                loc
            } else null

            val downloadConn = if (redirectUrlStr != null) {
                (URL(redirectUrlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "CalmLift-App")
                    connectTimeout = 30000
                    readTimeout = 30000
                }
            } else {
                initialConn
            }

            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val apkFile = File(updatesDir, updateInfo.assetName.ifBlank { "CalmLift-update.apk" })

            val totalSize = downloadConn.contentLengthLong.takeIf { it > 0 } ?: updateInfo.assetSize
            downloadConn.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalDownloaded = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead
                        if (totalSize > 0) {
                            val progress = (totalDownloaded.toFloat() / totalSize).coerceIn(0f, 1f)
                            _updateState.value = UpdateState.Downloading(progress)
                        }
                    }
                    output.flush()
                }
            }

            _updateState.value = UpdateState.ReadyToInstall(apkFile)
            withContext(Dispatchers.Main) {
                installApk(context, apkFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            _updateState.value = UpdateState.Error("Download failed: ${e.message}")
        } finally {
            downloadJob = null
        }
    }

    /**
     * Verifies package archive integrity, unknown sources permission, and triggers OS package installer.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            val pkgInfo = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            if (pkgInfo == null || pkgInfo.packageName != context.packageName) {
                apkFile.delete()
                throw SecurityException("APK validation failed: package (${pkgInfo?.packageName}) does not match expected (${context.packageName})")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Install launch failed", e)
            _updateState.value = UpdateState.Error("Installation could not be started: ${e.message}")
        }
    }

    internal fun isNewerVersion(remote: String, local: String): Boolean {
        val cleanRemote = remote.removePrefix("v").removePrefix("V").substringBefore("-")
        val cleanLocal = local.removePrefix("v").removePrefix("V").substringBefore("-")
        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = cleanLocal.split(".").mapNotNull { it.toIntOrNull() }
        if (remoteParts.isEmpty() || localParts.isEmpty()) return false
        val maxLen = maxOf(remoteParts.size, localParts.size)

        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
