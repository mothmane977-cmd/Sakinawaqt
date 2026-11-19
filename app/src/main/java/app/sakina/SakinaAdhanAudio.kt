package app.sakina

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.annotation.RawRes

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
        "الشيخ السريحي", "Sheikh Aserehy", "Cheikh Aserehy",
        "Şeyh Aserehy", "Syekh Aserehy", "شیخ السریحي"
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

    fun setVoiceForPrayer(ctx: Context, prayerNameAr: String, voice: AdhanVoice?) {
        val ed = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit()
        if (voice == null) ed.remove("adhan_voice_$prayerNameAr")
        else ed.putString("adhan_voice_$prayerNameAr", voice.key)
        ed.apply()
    }

    @RawRes
    fun rawResForPrayer(ctx: Context, prayerNameAr: String? = null): Int =
        voiceForPrayer(ctx, prayerNameAr).rawRes

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

    fun startPreview(ctx: Context, voice: AdhanVoice, onComplete: () -> Unit = {}) {
        stop()
        try {
            val vol = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getFloat("adhan_vol", 0.85f)
                .coerceIn(0.05f, 1.0f)
            val mp = MediaPlayer.create(ctx.applicationContext, voice.rawRes) ?: return
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mp.setVolume(vol, vol)
            mp.setOnCompletionListener {
                stop()
                onComplete()
            }
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
            val vol = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getFloat("adhan_vol", 0.85f)
                .coerceIn(0.05f, 1.0f)
            val resId = rawResForPrayer(ctx, prayerNameAr)
            val mp = MediaPlayer.create(ctx.applicationContext, resId) ?: return
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            mp.setVolume(vol, vol)
            mp.setOnCompletionListener {
                stop()
                onComplete()
            }
            mp.start()
            activePlayer = mp
            registerFlipToSilence(ctx)
        } catch (_: Exception) {
            stop()
            onComplete()
        }
    }

    fun isPlaying(): Boolean = try { activePlayer?.isPlaying == true } catch (_: Exception) { false }

    fun stop() {
        unregisterFlipToSilence()
        try {
            activePlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        activePlayer = null
    }
}
