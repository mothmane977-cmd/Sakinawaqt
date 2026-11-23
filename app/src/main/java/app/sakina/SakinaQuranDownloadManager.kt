package app.sakina

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class QuranReciter(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val riwaya: QuranRiwaya,
    val serverUrl: String,
    val sizesMb: Map<Int, Double> = emptyMap()
) {
    fun displayName(lang: SakinaLang): String =
        if (lang == SakinaLang.AR || lang == SakinaLang.UR) nameAr else nameEn
}

/**
 * v32: Permanent On-Demand Surah Download Manager (filesDir + WorkManager + Atomic tmp->renameTo)
 * with instant 0ms Surah MB sizes and automatic Reciter filtering by Riwaya (Hafs / Warsh).
 */
object SakinaQuranDownloadManager {

    private const val PREFS_NAME = "sakina_quran_audio_prefs"
    private const val KEY_RECITER_HAFS = "reciter_hafs"
    private const val KEY_RECITER_WARSH = "reciter_warsh"

    private var catalogLoaded = false
    private var catalogReciters: List<QuranReciter> = emptyList()

    // Key format: "$reciterId:$surahNum" -> progress 0.0f..1.0f
    private val _downloads = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloads: StateFlow<Map<String, Float>> = _downloads.asStateFlow()

    // Incremented whenever a file finishes downloading or is deleted so Compose refreshes storage state
    private val _storageVersion = MutableStateFlow(0)
    val storageVersion: StateFlow<Int> = _storageVersion.asStateFlow()

    // Dynamic HEAD size cache for reciters/surahs missing from static JSON
    private val dynamicSizeCache = mutableMapOf<String, Double>()

    private fun defaultReciters(): List<QuranReciter> = listOf(
        QuranReciter(
            id = "afasy",
            nameAr = "مشاري راشد العفاسي",
            nameEn = "Mishary Al-Afasy",
            riwaya = QuranRiwaya.HAFS,
            serverUrl = "https://server8.mp3quran.net/afs/"
        ),
        QuranReciter(
            id = "husary",
            nameAr = "محمود خليل الحصري",
            nameEn = "Mahmoud Al-Husary",
            riwaya = QuranRiwaya.HAFS,
            serverUrl = "https://server13.mp3quran.net/husr/"
        ),
        QuranReciter(
            id = "basit",
            nameAr = "عبد الباسط عبد الصمد",
            nameEn = "AbdulBaset AbdulSamad",
            riwaya = QuranRiwaya.HAFS,
            serverUrl = "https://server7.mp3quran.net/basit/"
        ),
        QuranReciter(
            id = "maher",
            nameAr = "ماهر المعيقلي",
            nameEn = "Maher Al-Muaiqly",
            riwaya = QuranRiwaya.HAFS,
            serverUrl = "https://server12.mp3quran.net/maher/"
        ),
        QuranReciter(
            id = "qazabri_warsh",
            nameAr = "عمر القزابري (ورش)",
            nameEn = "Omar Al-Qazabri (Warsh)",
            riwaya = QuranRiwaya.WARSH,
            serverUrl = "https://server9.mp3quran.net/omar_warsh/"
        ),
        QuranReciter(
            id = "koshi_warsh",
            nameAr = "العيون الكوشي (ورش)",
            nameEn = "Laayoun El Kouchi (Warsh)",
            riwaya = QuranRiwaya.WARSH,
            serverUrl = "https://server11.mp3quran.net/koshi/"
        ),
        QuranReciter(
            id = "yassin_warsh",
            nameAr = "ياسين الجزائري (ورش)",
            nameEn = "Yassin Al-Jazaery (Warsh)",
            riwaya = QuranRiwaya.WARSH,
            serverUrl = "https://server11.mp3quran.net/qari/"
        )
    )

    private fun ensureCatalog(context: Context) {
        if (catalogLoaded) return
        catalogLoaded = true
        val parsed = mutableListOf<QuranReciter>()
        try {
            val raw = context.assets.open("quran_reciters_catalog.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val id = o.optString("id")
                val server = o.optString("server")
                if (id.isBlank() || server.isBlank()) continue
                val riw = if (o.optString("riwaya").equals("WARSH", true)) QuranRiwaya.WARSH else QuranRiwaya.HAFS
                val sizesObj = o.optJSONObject("sizesMb")
                val sizesMap = mutableMapOf<Int, Double>()
                if (sizesObj != null) {
                    val keys = sizesObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        k.toIntOrNull()?.let { sNum ->
                            sizesMap[sNum] = sizesObj.optDouble(k, 0.0)
                        }
                    }
                }
                parsed.add(
                    QuranReciter(
                        id = id,
                        nameAr = o.optString("nameAr", id),
                        nameEn = o.optString("nameEn", id),
                        riwaya = riw,
                        serverUrl = if (server.endsWith("/")) server else "$server/",
                        sizesMb = sizesMap
                    )
                )
            }
        } catch (_: Exception) {}
        catalogReciters = if (parsed.isNotEmpty()) parsed else defaultReciters()
    }

    fun recitersForRiwaya(context: Context, riwaya: QuranRiwaya): List<QuranReciter> {
        ensureCatalog(context)
        return catalogReciters.filter { it.riwaya == riwaya }.ifEmpty {
            defaultReciters().filter { it.riwaya == riwaya }
        }
    }

    fun getSelectedReciter(context: Context, riwaya: QuranRiwaya): QuranReciter {
        val list = recitersForRiwaya(context, riwaya)
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = if (riwaya == QuranRiwaya.WARSH) KEY_RECITER_WARSH else KEY_RECITER_HAFS
        val savedId = sp.getString(key, null)
        return list.firstOrNull { it.id == savedId } ?: list.first()
    }

    fun setSelectedReciter(context: Context, reciter: QuranReciter) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = if (reciter.riwaya == QuranRiwaya.WARSH) KEY_RECITER_WARSH else KEY_RECITER_HAFS
        sp.edit().putString(key, reciter.id).apply()
        _storageVersion.update { it + 1 }
    }

    fun permanentFile(context: Context, reciterId: String, surahNum: Int): File {
        val padded = surahNum.toString().padStart(3, '0')
        val dir = File(File(context.filesDir, "quran_audio"), reciterId)
        return File(dir, "$padded.mp3")
    }

    fun hasBundledRaw(context: Context, reciterId: String, surahNum: Int): Boolean {
        if (reciterId != "afasy") return false
        val padded = surahNum.toString().padStart(3, '0')
        return context.resources.getIdentifier("quran_s$padded", "raw", context.packageName) != 0
    }

    fun isDownloadedOnDisk(context: Context, reciterId: String, surahNum: Int): Boolean {
        val f = permanentFile(context, reciterId, surahNum)
        return f.exists() && f.length() > 4096L
    }

    fun isSurahReadyOffline(context: Context, reciterId: String, surahNum: Int): Boolean =
        isDownloadedOnDisk(context, reciterId, surahNum) || hasBundledRaw(context, reciterId, surahNum)

    fun surahUrl(reciter: QuranReciter, surahNum: Int): String {
        val padded = surahNum.toString().padStart(3, '0')
        return "${reciter.serverUrl}$padded.mp3"
    }

    /**
     * Instant 0ms size lookup in MB from pre-bundled catalog (or realistic fallback from Ayah count).
     */
    fun getSurahSizeMb(reciter: QuranReciter, surahNum: Int, ayahCountFallback: Int = 20): Double {
        val key = "${reciter.id}:$surahNum"
        dynamicSizeCache[key]?.let { if (it > 0.05) return it }
        reciter.sizesMb[surahNum]?.let { if (it > 0.05) return it }
        // Clean proportional fallback (~0.17 MB per ayah on average for 64–96 kbps MP3)
        return (ayahCountFallback * 0.17).coerceAtLeast(0.3)
    }

    fun formatSizeMb(mb: Double): String =
        String.format(java.util.Locale.US, "%.1f MB", mb.coerceAtLeast(0.1))

    fun downloadedFileSizeMb(context: Context, reciterId: String, surahNum: Int): Double {
        val f = permanentFile(context, reciterId, surahNum)
        if (!f.exists()) return 0.0
        return f.length().toDouble() / (1024.0 * 1024.0)
    }

    fun downloadKey(reciterId: String, surahNum: Int): String = "$reciterId:$surahNum"

    fun enqueueDownload(
        context: Context,
        reciter: QuranReciter,
        surahNum: Int,
        surahNameAr: String
    ) {
        val key = downloadKey(reciter.id, surahNum)
        if (_downloads.value.containsKey(key)) return

        _downloads.update { it + (key to 0.02f) }

        val data = Data.Builder()
            .putString(SakinaQuranDownloadWorker.KEY_RECITER_ID, reciter.id)
            .putString(SakinaQuranDownloadWorker.KEY_URL, surahUrl(reciter, surahNum))
            .putInt(SakinaQuranDownloadWorker.KEY_SURAH_NUM, surahNum)
            .putString(SakinaQuranDownloadWorker.KEY_SURAH_NAME, surahNameAr)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val req = OneTimeWorkRequestBuilder<SakinaQuranDownloadWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork("sakina_dl_$key", ExistingWorkPolicy.KEEP, req)
    }

    fun deleteDownloadedSurah(context: Context, reciterId: String, surahNum: Int): Boolean {
        val f = permanentFile(context, reciterId, surahNum)
        val ok = if (f.exists()) f.delete() else false
        if (ok) {
            _storageVersion.update { it + 1 }
        }
        return ok
    }

    internal fun reportProgress(key: String, progress: Float) {
        _downloads.update { it + (key to progress.coerceIn(0f, 1f)) }
    }

    internal fun reportFinished(key: String, success: Boolean, sizeMb: Double = 0.0) {
        if (sizeMb > 0.05) dynamicSizeCache[key] = sizeMb
        _downloads.update { it - key }
        if (success) {
            _storageVersion.update { it + 1 }
        }
    }
}

/**
 * Background CoroutineWorker ensuring long Surahs (e.g. Al-Baqarah) finish downloading
 * even if the screen locks or user switches apps, using atomic .tmp -> .mp3 rename.
 */
class SakinaQuranDownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val reciterId = inputData.getString(KEY_RECITER_ID) ?: return@withContext Result.failure()
        val urlStr = inputData.getString(KEY_URL) ?: return@withContext Result.failure()
        val surahNum = inputData.getInt(KEY_SURAH_NUM, 0)
        val surahName = inputData.getString(KEY_SURAH_NAME) ?: "سورة $surahNum"
        if (surahNum <= 0) return@withContext Result.failure()

        val dlKey = SakinaQuranDownloadManager.downloadKey(reciterId, surahNum)
        val finalFile = SakinaQuranDownloadManager.permanentFile(applicationContext, reciterId, surahNum)
        finalFile.parentFile?.mkdirs()
        val tmpFile = File(finalFile.parentFile, "${finalFile.nameWithoutExtension}.tmp")

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chId = "sakina_quran_downloads"
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(chId, "تحميل السور القرآنية", NotificationManager.IMPORTANCE_LOW).apply {
                    setSound(null, null)
                }
            )
        }
        val notifId = 8200 + surahNum

        fun postNotif(pct: Int, done: Boolean = false) {
            try {
                val b = NotificationCompat.Builder(applicationContext, chId)
                    .setSmallIcon(R.drawable.ic_stat_sakina)
                    .setColor(0xFFC69C53.toInt())
                    .setContentTitle(if (done) "تم حفظ سورة $surahName أوفلاين ✓" else "جاري تحميل سورة $surahName")
                    .setContentText(if (done) "جاهزة للاستماع بدون إنترنت" else "$pct%")
                    .setOnlyAlertOnce(true)
                    .setOngoing(!done)
                    .setAutoCancel(done)
                if (!done) b.setProgress(100, pct, pct == 0)
                nm.notify(notifId, b.build())
            } catch (_: Exception) {}
        }

        var conn: HttpURLConnection? = null
        try {
            postNotif(0)
            conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 30_000
                setRequestProperty("User-Agent", "SakinaApp/32")
                instanceFollowRedirects = true
            }
            if (conn.responseCode !in 200..299) {
                SakinaQuranDownloadManager.reportFinished(dlKey, false)
                nm.cancel(notifId)
                return@withContext Result.retry()
            }

            val totalBytes = conn.contentLengthLong
            var downloaded = 0L
            var lastReportedPct = -1

            conn.inputStream.use { input ->
                FileOutputStream(tmpFile).use { output ->
                    val buf = ByteArray(16 * 1024)
                    while (true) {
                        val read = input.read(buf)
                        if (read <= 0) break
                        output.write(buf, 0, read)
                        downloaded += read
                        if (totalBytes > 0L) {
                            val frac = (downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 0.99f)
                            val pct = (frac * 100).toInt()
                            if (pct >= lastReportedPct + 3) {
                                lastReportedPct = pct
                                SakinaQuranDownloadManager.reportProgress(dlKey, frac)
                                postNotif(pct)
                            }
                        }
                    }
                    output.flush()
                }
            }

            if (tmpFile.exists() && tmpFile.length() > 4096L) {
                if (finalFile.exists()) finalFile.delete()
                val renamed = tmpFile.renameTo(finalFile)
                if (renamed) {
                    val mb = finalFile.length().toDouble() / (1024.0 * 1024.0)
                    SakinaQuranDownloadManager.reportFinished(dlKey, true, mb)
                    postNotif(100, done = true)
                    return@withContext Result.success()
                }
            }
            tmpFile.delete()
            SakinaQuranDownloadManager.reportFinished(dlKey, false)
            nm.cancel(notifId)
            Result.failure()
        } catch (_: Exception) {
            tmpFile.delete()
            SakinaQuranDownloadManager.reportFinished(dlKey, false)
            nm.cancel(notifId)
            Result.retry()
        } finally {
            try { conn?.disconnect() } catch (_: Exception) {}
        }
    }

    companion object {
        const val KEY_RECITER_ID = "reciter_id"
        const val KEY_URL = "url"
        const val KEY_SURAH_NUM = "surah_num"
        const val KEY_SURAH_NAME = "surah_name"
    }
}
