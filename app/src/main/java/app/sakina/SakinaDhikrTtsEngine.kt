package app.sakina

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Sakina v46 (Option 4-أ) — Male Human Reciter Per-Dhikr Audio Engine
 * Downloads & caches authentic male human Arabic per-Dhikr recitations permanently
 * in filesDir/adhkar_audio/ (instant offline playback after first tap).
 * Falls back strictly to a male-filtered low-pitch Arabic voice only if offline before cache.
 */
class SakinaDhikrTtsEngine(
    private val ctx: Context,
    private val onStateChanged: (playingId: Int?) -> Unit
) {
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var currentPlayingId: Int? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    // Verified Hisn al-Muslim male human Arabic per-Dhikr audio IDs
    private val hisnAudioIds = mapOf(
        0 to 75, 1 to 76, 2 to 77, 3 to 78, 4 to 79, 5 to 80,
        6 to 81, 7 to 82, 8 to 83, 9 to 84, 10 to 85, 11 to 86,
        12 to 75, 13 to 76, 14 to 77, 15 to 79, 16 to 78, 17 to 87,
        18 to 82, 19 to 81, 20 to 88, 21 to 84,
        22 to 66, 23 to 67, 24 to 68, 25 to 69, 26 to 70, 27 to 71, 28 to 72,
        29 to 98, 30 to 99, 31 to 100, 32 to 101, 33 to 102, 34 to 103, 35 to 104,
        36 to 18, 37 to 19, 38 to 20, 39 to 21,
        40 to 16, 41 to 17, 42 to 15, 43 to 15
    )

    init {
        try {
            tts = TextToSpeech(ctx.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val ar = Locale("ar")
                    val res = tts?.setLanguage(ar)
                    ttsReady = res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED
                    // Strictly prefer a male Arabic voice if installed on device
                    try {
                        val maleVoice = tts?.voices?.firstOrNull { v ->
                            v.locale.language == "ar" && (
                                v.name.contains("male", ignoreCase = true) ||
                                v.name.contains("-m-", ignoreCase = true) ||
                                v.name.contains("ard", ignoreCase = true) ||
                                v.name.contains("are", ignoreCase = true)
                            )
                        }
                        if (maleVoice != null) tts?.voice = maleVoice
                    } catch (_: Exception) {}
                    tts?.setSpeechRate(0.76f)
                    tts?.setPitch(0.78f) // Deep calm male resonance
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}
                        override fun onDone(utteranceId: String?) {
                            currentPlayingId = null
                            onStateChanged(null)
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            currentPlayingId = null
                            onStateChanged(null)
                        }
                    })
                }
            }
        } catch (_: Exception) {
            ttsReady = false
        }
    }

    fun toggleSpeak(dhikrId: Int, rawText: String) {
        if (currentPlayingId == dhikrId) {
            stop()
            return
        }
        stop()
        currentPlayingId = dhikrId
        onStateChanged(dhikrId)

        val audioNum = hisnAudioIds[dhikrId]
        if (audioNum != null) {
            scope.launch {
                val cached = withContext(Dispatchers.IO) { fetchOrGetCachedMaleAudio(audioNum) }
                if (currentPlayingId != dhikrId) return@launch
                if (cached != null && cached.exists() && cached.length() > 1024L) {
                    playLocalFile(dhikrId, cached)
                } else {
                    speakMaleFallback(dhikrId, rawText)
                }
            }
        } else {
            speakMaleFallback(dhikrId, rawText)
        }
    }

    private fun fetchOrGetCachedMaleAudio(trackNum: Int): File? {
        return try {
            val dir = File(ctx.filesDir, "adhkar_audio").apply { mkdirs() }
            val out = File(dir, "hisn_$trackNum.mp3")
            if (out.exists() && out.length() > 2048L) return out
            val tmp = File(dir, "hisn_$trackNum.tmp")
            val url = URL("https://www.hisnmuslim.com/audio/ar/$trackNum.mp3")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 10000
                setRequestProperty("User-Agent", "SakinaApp/1.0")
            }
            if (conn.responseCode == 200) {
                conn.inputStream.use { inp ->
                    tmp.outputStream().use { outS -> inp.copyTo(outS) }
                }
                if (tmp.length() > 2048L) {
                    tmp.renameTo(out)
                    out
                } else {
                    tmp.delete()
                    null
                }
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun playLocalFile(dhikrId: Int, file: File) {
        try {
            mediaPlayer?.release()
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    currentPlayingId = null
                    onStateChanged(null)
                }
                setOnErrorListener { _, _, _ ->
                    currentPlayingId = null
                    onStateChanged(null)
                    true
                }
                prepare()
                start()
            }
            mediaPlayer = mp
        } catch (_: Exception) {
            currentPlayingId = null
            onStateChanged(null)
        }
    }

    private fun speakMaleFallback(dhikrId: Int, rawText: String) {
        if (!ttsReady) {
            currentPlayingId = null
            onStateChanged(null)
            return
        }
        val cleanText = rawText
            .replace("﴿", " ")
            .replace("﴾", " ")
            .replace("«", " ")
            .replace("»", " ")
            .replace("*", " ")
            .trim()
        try {
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "dhikr_$dhikrId")
        } catch (_: Exception) {
            currentPlayingId = null
            onStateChanged(null)
        }
    }

    fun stop() {
        try { mediaPlayer?.stop() } catch (_: Exception) {}
        try { mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null
        try { tts?.stop() } catch (_: Exception) {}
        if (currentPlayingId != null) {
            currentPlayingId = null
            onStateChanged(null)
        }
    }

    fun release() {
        stop()
        try { tts?.shutdown() } catch (_: Exception) {}
        tts = null
    }
}
