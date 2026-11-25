package app.sakina

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
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
 * v32.1: Instant-start Permanent Surah Download Manager (filesDir + Atomic tmp->renameTo)
 * - Runs immediately on Dispatchers.IO (bypasses Xiaomi HyperOS WorkManager/JobScheduler deferral!)
 * - Supports automatic fallback mirrors + clean error state (never stalls at 2%).
 * - Preserves permanent storage in context.filesDir/quran_audio/{reciterId}/{001..114}.mp3.
 */
object SakinaQuranDownloadManager {

    private const val PREFS_NAME = "sakina_quran_audio_prefs"
    private const val KEY_RECITER_HAFS = "reciter_hafs"
    private const val KEY_RECITER_WARSH = "reciter_warsh"

    private val dlScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var catalogLoaded = false
    private var catalogReciters: List<QuranReciter> = emptyList()

    // Key format: "$reciterId:$surahNum" -> progress 0.0f..1.0f
    private val _downloads = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloads: StateFlow<Map<String, Float>> = _downloads.asStateFlow()

    // Key format: "$reciterId:$surahNum" -> error message if download failed
    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    val errors: StateFlow<Map<String, String>> = _errors.asStateFlow()

    // Incremented whenever a file finishes downloading or is deleted so Compose refreshes immediately
    private val _storageVersion = MutableStateFlow(0)
    val storageVersion: StateFlow<Int> = _storageVersion.asStateFlow()

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
        ),
        QuranReciter(
            id = "yasser",
            nameAr = "ياسر الدوسري",
            nameEn = "Yasser Al-Dosari",
            riwaya = QuranRiwaya.HAFS,
            serverUrl = "https://server11.mp3quran.net/yasser/"
        ),
        QuranReciter(
            id = "qtm",
            nameAr = "ناصر القطامي",
            nameEn = "Nasser Al-Qatami",
            riwaya = QuranRiwaya.HAFS,
            serverUrl = "https://server6.mp3quran.net/qtm/"
        ),
        QuranReciter(
            id = "minsh",
            nameAr = "محمد صديق المنشاوي",
            nameEn = "Muhammad Siddiq Al-Minshawi",
            riwaya = QuranRiwaya.HAFS,
            serverUrl = "https://server10.mp3quran.net/minsh/"
        ),
        QuranReciter(
            id = "ghamdi",
            nameAr = "سعد الغامدي",
            nameEn = "Saad Al-Ghamdi",
            riwaya = QuranRiwaya.HAFS,
            serverUrl = "https://server7.mp3quran.net/s_gmd/"
        ),
        QuranReciter(
            id = "shur",
            nameAr = "سعود الشريم",
            nameEn = "Saud Al-Shuraim",
            riwaya = QuranRiwaya.HAFS,
            serverUrl = "https://server7.mp3quran.net/shur/"
        ),
        QuranReciter(
            id = "frs_a",
            nameAr = "فارس عباد",
            nameEn = "Fares Abbad",
            riwaya = QuranRiwaya.HAFS,
            serverUrl = "https://server8.mp3quran.net/frs_a/"
        ),
        QuranReciter(
            id = "islam",
            nameAr = "إسلام صبحي",
            nameEn = "Islam Sobhi",
            riwaya = QuranRiwaya.HAFS,
            serverUrl = "https://server14.mp3quran.net/islam/Rewayat-Hafs-A-n-Assem/"
        ),
        QuranReciter(
            id = "soufi_warsh",
            nameAr = "عبد الرشيد صوفي (ورش)",
            nameEn = "Abdulrashid Soufi (Warsh)",
            riwaya = QuranRiwaya.WARSH,
            serverUrl = "https://server16.mp3quran.net/soufi/Rewayat-Warsh-A-n-Nafi/"
        ),
        QuranReciter(
            id = "ibrahim_dosari_warsh",
            nameAr = "إبراهيم الدوسري (ورش)",
            nameEn = "Ibrahim Al-Dosari (Warsh)",
            riwaya = QuranRiwaya.WARSH,
            serverUrl = "https://server10.mp3quran.net/ibrahim_dosri/Rewayat-Warsh-A-n-Nafi/"
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

    private fun candidateUrls(reciter: QuranReciter, surahNum: Int): List<String> {
        val padded = surahNum.toString().padStart(3, '0')
        val list = mutableListOf("${reciter.serverUrl}$padded.mp3")
        when (reciter.id) {
            "afasy" -> list.add("https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/$padded.mp3")
            "husary" -> list.add("https://download.quranicaudio.com/quran/mahmood_khaleel_al-husaree/$padded.mp3")
            "basit" -> list.add("https://download.quranicaudio.com/quran/abdul_basit_murattal/$padded.mp3")
            "qazabri_warsh" -> {
                list.add("https://server9.mp3quran.net/omar_warsh/$padded.mp3")
                list.add("https://download.quranicaudio.com/quran/abdulbaset_warsh/$padded.mp3")
            }
            "koshi_warsh" -> {
                list.add("https://server11.mp3quran.net/koshi/$padded.mp3")
                list.add("https://download.quranicaudio.com/quran/abdulbaset_warsh/$padded.mp3")
            }
            "yassin_warsh" -> {
                list.add("https://server11.mp3quran.net/qari/$padded.mp3")
                list.add("https://download.quranicaudio.com/quran/abdulbaset_warsh/$padded.mp3")
            }
        }
        return list.distinct()
    }

    fun getSurahSizeMb(reciter: QuranReciter, surahNum: Int, ayahCountFallback: Int = 20): Double {
        val key = "${reciter.id}:$surahNum"
        dynamicSizeCache[key]?.let { if (it > 0.05) return it }
        reciter.sizesMb[surahNum]?.let { if (it > 0.05) return it }
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

        val appCtx = context.applicationContext
        _errors.update { it - key }
        _downloads.update { it + (key to 0.01f) }

        dlScope.launch {
            val finalFile = permanentFile(appCtx, reciter.id, surahNum)
            finalFile.parentFile?.mkdirs()
            val tmpFile = File(finalFile.parentFile, "${finalFile.nameWithoutExtension}.tmp")

            val nm = appCtx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
                    val b = NotificationCompat.Builder(appCtx, chId)
                        .setSmallIcon(R.drawable.ic_stat_sakina)
                        .setColor(0xFFC69C53.toInt())
                        .setContentTitle(if (done) "تم حفظ سورة $surahNameAr أوفلاين ✓" else "جاري تحميل سورة $surahNameAr")
                        .setContentText(if (done) "جاهزة للاستماع بدون إنترنت" else "$pct%")
                        .setOnlyAlertOnce(true)
                        .setOngoing(!done)
                        .setAutoCancel(done)
                    if (!done) b.setProgress(100, pct, pct == 0)
                    nm.notify(notifId, b.build())
                } catch (_: Exception) {}
            }

            var success = false
            for (urlStr in candidateUrls(reciter, surahNum)) {
                var conn: HttpURLConnection? = null
                try {
                    postNotif(0)
                    conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15_000
                        readTimeout = 25_000
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Android; SakinaApp/32)")
                        instanceFollowRedirects = true
                    }
                    val code = conn.responseCode
                    if (code !in 200..299) {
                        conn.disconnect()
                        continue
                    }

                    val totalBytes = conn.contentLengthLong
                    var downloaded = 0L
                    var lastPct = -1

                    conn.inputStream.use { input ->
                        FileOutputStream(tmpFile).use { output ->
                            val buf = ByteArray(16 * 1024)
                            while (true) {
                                val read = input.read(buf)
                                if (read <= 0) break
                                output.write(buf, 0, read)
                                downloaded += read
                                val frac = if (totalBytes > 0L) {
                                    (downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0.02f, 0.99f)
                                } else {
                                    (downloaded.toFloat() / (1024f * 1024f * 2f)).coerceIn(0.05f, 0.90f)
                                }
                                val pct = (frac * 100).toInt()
                                if (pct >= lastPct + 2) {
                                    lastPct = pct
                                    _downloads.update { it + (key to frac) }
                                    postNotif(pct)
                                }
                            }
                            output.flush()
                        }
                    }

                    if (tmpFile.exists() && tmpFile.length() > 4096L) {
                        if (finalFile.exists()) finalFile.delete()
                        if (tmpFile.renameTo(finalFile)) {
                            val mb = finalFile.length().toDouble() / (1024.0 * 1024.0)
                            dynamicSizeCache[key] = mb
                            _downloads.update { it - key }
                            _storageVersion.update { it + 1 }
                            postNotif(100, done = true)
                            success = true
                            break
                        }
                    }
                    tmpFile.delete()
                } catch (_: Exception) {
                    tmpFile.delete()
                } finally {
                    try { conn?.disconnect() } catch (_: Exception) {}
                }
            }

            if (!success) {
                tmpFile.delete()
                _downloads.update { it - key }
                _errors.update { it + (key to "تعذّر التحميل · تحقق من الإنترنت") }
                try { nm.cancel(notifId) } catch (_: Exception) {}
            }
        }
    }

    fun deleteDownloadedSurah(context: Context, reciterId: String, surahNum: Int): Boolean {
        val f = permanentFile(context, reciterId, surahNum)
        val ok = if (f.exists()) f.delete() else false
        if (ok) {
            _storageVersion.update { it + 1 }
        }
        return ok
    }
}
