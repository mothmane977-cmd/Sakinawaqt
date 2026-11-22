package app.sakina

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RawRes
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.pow

enum class AdhanVoice(
    val key: String,
    @RawRes val rawRes: Int,
    val titleAr: String,
    val titleEn: String,
    val titleFr: String,
    val titleTr: String,
    val titleId: String,
    val titleUr: String
) {
    MADINAH(
        "madinah", R.raw.madinah,
        "أذان المسجد النبوي", "Madinah Adhan", "Adhan de Médine",
        "Medine Ezanı", "Adzan Madinah", "اذانِ مسجد نبوی"
    ),
    ABDUL_BASIT(
        "abdul_basit", R.raw.abdul_basit_abdus_samad,
        "الشيخ عبد الباسط عبد الصمد", "Abdul Basit Abdus Samad", "Abdul Basit Abdus Samad",
        "Abdülbasit Abdüssamed", "Abdul Basit Abdus Samad", "قاری عبدالباسط عبدالصمد"
    ),
    RIAD(
        "riad", R.raw.riad,
        "الشيخ رياض", "Sheikh Riad", "Cheikh Riad",
        "Şeyh Riad", "Syekh Riad", "شیخ ریاض"
    ),
    RIAD_FAJR(
        "riad_fajr", R.raw.riad_fajr,
        "أذان الفجر (رياض)", "Riad (Fajr)", "Riad (Fajr)",
        "Riad (Sabah Ezanı)", "Riad (Subuh)", "اذانِ فجر (ریاض)"
    ),
    ASEREHY(
        "aserehy", R.raw.aserehy,
        "الشيخ عبد المجيد السريحي", "Sheikh Abdulmajeed Al-Suraihi", "Cheikh Al-Suraihi",
        "Şeyh El-Süreyhi", "Syekh Al-Suraihi", "شیخ عبدالمجید السریحي"
    ),
    MAKKAH(
        "makkah", R.raw.makkah,
        "أذان المسجد الحرام (مكة)", "Makkah Al-Haram Adhan", "Adhan de La Mecque",
        "Mekke Ezanı", "Adzan Makkah", "اذانِ مسجد الحرام"
    ),
    AFASY(
        "afasy", R.raw.afasy,
        "الشيخ مشاري العفاسي", "Mishary Al-Afasy", "Mishary Al-Afasy",
        "Mişari El-Afasi", "Mishary Al-Afasy", "شیخ مشاری العفاسی"
    ),
    AQSA(
        "aqsa", R.raw.aqsa,
        "أذان المسجد الأقصى", "Al-Aqsa Adhan", "Adhan d'Al-Aqsa",
        "Mescid-i Aksa Ezanı", "Adzan Al-Aqsa", "اذانِ مسجد اقصیٰ"
    );

    fun localizedTitle(lang: SakinaLang): String = when (lang) {
        SakinaLang.AR -> titleAr
        SakinaLang.EN -> titleEn
        SakinaLang.FR -> titleFr
        SakinaLang.TR -> titleTr
        SakinaLang.ID -> titleId
        SakinaLang.UR -> titleUr
    }
}

object SakinaAdhanAudio {
    private const val PREF_VOICE = "adhan_voice_key"
    private var activePlayer: MediaPlayer? = null
    private var flipSm: SensorManager? = null
    private var flipListener: SensorEventListener? = null
    private var activeAudioManager: AudioManager? = null
    private var activeFocusRequest: AudioFocusRequest? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var fadeRunnable: Runnable? = null
    private val isStopping = AtomicBoolean(false)

    fun selectedVoice(ctx: Context): AdhanVoice {
        val sp = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val key = sp.getString(PREF_VOICE, AdhanVoice.MADINAH.key)
        return AdhanVoice.entries.firstOrNull { it.key == key } ?: AdhanVoice.MADINAH
    }

    fun setSelectedVoice(ctx: Context, voice: AdhanVoice) {
        ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_VOICE, voice.key)
            .apply()
    }

    fun voiceForPrayer(ctx: Context, prayerNameAr: String?): AdhanVoice {
        if (!prayerNameAr.isNullOrBlank()) {
            val sp = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val perKey = sp.getString("adhan_voice_$prayerNameAr", null)
            if (!perKey.isNullOrBlank()) {
                AdhanVoice.entries.firstOrNull { it.key == perKey }?.let { return it }
            }
        }
        val global = selectedVoice(ctx)
        if (prayerNameAr == "الفجر" && global == AdhanVoice.RIAD) return AdhanVoice.RIAD_FAJR
        return global
    }

    fun copyVoiceToAllPrayers(ctx: Context, voice: AdhanVoice) {
        val ed = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit()
        ed.putString(PREF_VOICE, voice.key)
        listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء").forEach { pName ->
            ed.remove("adhan_voice_$pName")
        }
        ed.apply()
    }

    fun setVoiceForPrayer(ctx: Context, prayerNameAr: String, voice: AdhanVoice?) {
        val ed = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit()
        if (voice == null) ed.remove("adhan_voice_$prayerNameAr")
        else ed.putString("adhan_voice_$prayerNameAr", voice.key)
        ed.apply()
    }

    @RawRes
    fun rawResForPrayer(ctx: Context, prayerNameAr: String? = null): Int =
        voiceForPrayer(ctx, prayerNameAr).rawRes

    private fun isInActiveCall(am: AudioManager?): Boolean {
        if (am == null) return false
        return try {
            am.mode == AudioManager.MODE_IN_CALL || am.mode == AudioManager.MODE_IN_COMMUNICATION
        } catch (_: Exception) { false }
    }

    private fun isHeadphonesConnected(am: AudioManager?): Boolean {
        if (am == null) return false
        return try {
            val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            devices.any { d ->
                d.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    d.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    d.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    d.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    (Build.VERSION.SDK_INT >= 26 && d.type == AudioDeviceInfo.TYPE_USB_HEADSET)
            }
        } catch (_: Exception) { false }
    }

    private fun requestAudioFocus(am: AudioManager?, attrs: AudioAttributes) {
        if (am == null) return
        try {
            activeAudioManager = am
            if (Build.VERSION.SDK_INT >= 26) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(attrs)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener { change ->
                        if (change == AudioManager.AUDIOFOCUS_LOSS) stop()
                    }
                    .build()
                activeFocusRequest = req
                am.requestAudioFocus(req)
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            }
        } catch (_: Exception) {}
    }

    private fun abandonAudioFocus() {
        try {
            val am = activeAudioManager
            if (am != null) {
                if (Build.VERSION.SDK_INT >= 26) {
                    activeFocusRequest?.let { am.abandonAudioFocusRequest(it) }
                } else {
                    @Suppress("DEPRECATION")
                    am.abandonAudioFocus(null)
                }
            }
        } catch (_: Exception) {}
        activeFocusRequest = null
        activeAudioManager = null
    }

    private fun cancelFade() {
        fadeRunnable?.let { mainHandler.removeCallbacks(it) }
        fadeRunnable = null
    }

    private fun startLogarithmicFadeIn(mp: MediaPlayer, targetVol: Float, durationMs: Long = 12_000L) {
        cancelFade()
        val steps = 24
        val stepDelay = durationMs / steps
        var currentStep = 0
        try { mp.setVolume(0.06f, 0.06f) } catch (_: Exception) {}

        val r = object : Runnable {
            override fun run() {
                if (activePlayer !== mp) return
                currentStep++
                val progress = (currentStep.toFloat() / steps).coerceIn(0f, 1f)
                // Smooth logarithmic-feel curve from 0.06f up to targetVol
                val curved = (0.06f + (targetVol - 0.06f) * progress.pow(1.8f)).coerceIn(0.05f, 1.0f)
                try {
                    if (mp.isPlaying) mp.setVolume(curved, curved)
                } catch (_: Exception) {
                    return
                }
                if (currentStep < steps) {
                    mainHandler.postDelayed(this, stepDelay)
                }
            }
        }
        fadeRunnable = r
        mainHandler.postDelayed(r, stepDelay)
    }

    private fun registerFlipToSilence(ctx: Context) {
        unregisterFlipToSilence()
        if (!SakinaAdvancedPrefs.flipToSilence(ctx)) return
        try {
            val sm = ctx.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
            val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
            var seenFaceUp = false
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val z = event.values[2]
                    if (z > 6.0f) seenFaceUp = true
                    if (seenFaceUp && z < -7.5f) {
                        stop()
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sm.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
            flipSm = sm
            flipListener = listener
        } catch (_: Exception) {}
    }

    private fun unregisterFlipToSilence() {
        try {
            if (flipSm != null && flipListener != null) {
                flipSm?.unregisterListener(flipListener)
            }
        } catch (_: Exception) {}
        flipSm = null
        flipListener = null
    }

    private fun createSafeMediaPlayer(ctx: Context, @RawRes resId: Int): MediaPlayer? {
        return try {
            MediaPlayer.create(ctx.applicationContext, resId)
        } catch (_: Exception) {
            null
        } ?: try {
            MediaPlayer.create(ctx.applicationContext, R.raw.madinah)
        } catch (_: Exception) {
            null
        }
    }

    fun startPreview(ctx: Context, voice: AdhanVoice, onComplete: () -> Unit = {}) {
        stop()
        try {
            val am = ctx.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            var vol = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getFloat("adhan_vol", 0.85f)
                .coerceIn(0.05f, 1.0f)
            if (isHeadphonesConnected(am)) vol = vol.coerceAtMost(0.55f)

            val mp = createSafeMediaPlayer(ctx, voice.rawRes) ?: run { onComplete(); return }
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            mp.setAudioAttributes(attrs)
            mp.setVolume(vol, vol)
            mp.setOnCompletionListener { stop(); onComplete() }
            mp.setOnErrorListener { _, _, _ -> stop(); onComplete(); true }
            mp.start()
            activePlayer = mp
        } catch (_: Exception) {
            stop()
            onComplete()
        }
    }

    fun playAlarmAdhan(ctx: Context, prayerNameAr: String? = null, onComplete: () -> Unit = {}) {
        stop()
        try {
            val am = ctx.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            // Call safety: never blast loud alarm Adhan over an active phone/VoIP call
            if (isInActiveCall(am)) {
                onComplete()
                return
            }

            var targetVol = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getFloat("adhan_vol", 0.85f)
                .coerceIn(0.05f, 1.0f)
            // Headphone ear-safety cap
            if (isHeadphonesConnected(am)) {
                targetVol = targetVol.coerceAtMost(0.50f)
            }

            val resId = rawResForPrayer(ctx, prayerNameAr)
            val mp = createSafeMediaPlayer(ctx, resId) ?: run { onComplete(); return }
            val useAlarm = SakinaAdvancedPrefs.useAlarmStream(ctx) ||
                (!prayerNameAr.isNullOrBlank() && SakinaAdvancedPrefs.silentBypassFor(ctx, prayerNameAr))
            val attrs = AudioAttributes.Builder()
                .setUsage(if (useAlarm) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            mp.setAudioAttributes(attrs)
            requestAudioFocus(am, attrs)

            val isFajrFade = (prayerNameAr == "الفجر") && Prefs.fajrFade(ctx)
            mp.setOnCompletionListener { stop(); onComplete() }
            mp.setOnErrorListener { _, _, _ -> stop(); onComplete(); true }
            activePlayer = mp
            mp.start()

            if (isFajrFade) {
                startLogarithmicFadeIn(mp, targetVol, durationMs = 12_000L)
            } else {
                mp.setVolume(targetVol, targetVol)
            }
            registerFlipToSilence(ctx)
        } catch (_: Exception) {
            stop()
            onComplete()
        }
    }

    fun isPlaying(): Boolean = try { activePlayer?.isPlaying == true } catch (_: Exception) { false }

    fun stop() {
        if (!isStopping.compareAndSet(false, true)) return
        try {
            cancelFade()
            unregisterFlipToSilence()
            activePlayer?.let {
                try { if (it.isPlaying) it.stop() } catch (_: Exception) {}
                try { it.release() } catch (_: Exception) {}
            }
            activePlayer = null
            abandonAudioFocus()
        } finally {
            isStopping.set(false)
        }
    }
}
