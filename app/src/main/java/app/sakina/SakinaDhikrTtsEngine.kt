package app.sakina

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Sakina v44 — Offline Per-Dhikr Recitation Engine
 * Allows the user to listen to ANY individual Dhikr card on demand (100% offline,
 * zero extra APK size) with calm, slow Arabic recitation cadence.
 */
class SakinaDhikrTtsEngine(
    ctx: Context,
    private val onStateChanged: (playingId: Int?) -> Unit
) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var currentPlayingId: Int? = null

    init {
        try {
            tts = TextToSpeech(ctx.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val res = tts?.setLanguage(Locale("ar"))
                    ready = res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED
                    tts?.setSpeechRate(0.78f)
                    tts?.setPitch(0.95f)
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
            ready = false
        }
    }

    fun toggleSpeak(dhikrId: Int, rawText: String) {
        if (currentPlayingId == dhikrId) {
            stop()
            return
        }
        stop()
        if (!ready) return
        val cleanText = rawText
            .replace("﴿", " ")
            .replace("﴾", " ")
            .replace("«", " ")
            .replace("»", " ")
            .replace("*", " ")
            .trim()
        currentPlayingId = dhikrId
        onStateChanged(dhikrId)
        try {
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "dhikr_$dhikrId")
        } catch (_: Exception) {
            currentPlayingId = null
            onStateChanged(null)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        if (currentPlayingId != null) {
            currentPlayingId = null
            onStateChanged(null)
        }
    }

    fun release() {
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }
}
