package app.sakina

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
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
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlin.math.log10
import kotlin.math.max

class AdhanService : Service() {
    companion object {
        const val ACTION_STOP = "app.sakina.STOP_ADHAN"
        private const val CH_ID = "adhan_service"
        private const val NOTIF_ID = 7001
        private const val FADE_MS = 12_000L

        fun start(c: Context, name: String, mode: Int) {
            ContextCompat.startForegroundService(
                c, Intent(c, AdhanService::class.java).putExtra("n", name).putExtra("m", mode)
            )
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var focus: AudioFocusRequest? = null
    private var sensors: SensorManager? = null
    private var flip: SensorEventListener? = null
    private var stopped = false

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            DiagLog.add(this, "أوقف المستخدم الأذان")
            finish()
            return START_NOT_STICKY
        }
        val name = intent?.getStringExtra("n") ?: "الصلاة"
        val mode = intent?.getIntExtra("m", 0) ?: 0
        stopped = false
        releaseAll()
        promote(name)
        play(name, mode)
        return START_NOT_STICKY
    }

    private fun promote(name: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CH_ID, "تشغيل الأذان", NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, AdhanService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(this, CH_ID)
            .setSmallIcon(R.drawable.ic_stat_sakina)
            .setColor(0xFFC69C53.toInt())
            .setContentTitle("حان وقت $name")
            .setContentText("اضغط لفتح التطبيق، أو أوقف الأذان")
            .setContentIntent(open)
            .addAction(0, "إيقاف", stop)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        ServiceCompat.startForeground(
            this, NOTIF_ID, n,
            if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0
        )
    }

    private fun headphones(am: AudioManager): Boolean =
        am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                it.type == AudioDeviceInfo.TYPE_USB_HEADSET
        }

    private fun play(name: String, mode: Int) {
        val am = getSystemService(AudioManager::class.java)

        if (am.mode == AudioManager.MODE_IN_CALL || am.mode == AudioManager.MODE_IN_COMMUNICATION) {
            DiagLog.add(this, "مكالمة جارية: اهتزاز بدل صوت $name")
            vibrate()
            handler.postDelayed({ finish() }, 6_000L)
            return
        }

        val resName = when {
            mode == 1 -> "takbir"
            name == "الفجر" -> "adhan_fajr"
            else -> "adhan"
        }
        var rid = resources.getIdentifier(resName, "raw", packageName)
        if (rid == 0) rid = resources.getIdentifier("adhan", "raw", packageName)
        if (rid == 0) {
            DiagLog.add(this, "ملف الصوت غير موجود: استعمال إشعار عادي")
            Notifier.show(this, name, mode)
            finish()
            return
        }

        val cap = if (headphones(am)) 0.5f else 1.0f
        val fade = name == "الفجر" && Prefs.fajrFade(this)
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val mp = MediaPlayer()
            mp.setAudioAttributes(attrs)
            val afd = resources.openRawResourceFd(rid)
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
            mp.setOnCompletionListener {
                DiagLog.add(this, "انتهى صوت $name")
                finish()
            }
            mp.setOnErrorListener { _, what, extra ->
                DiagLog.add(this, "خطأ في الصوت ($what/$extra): استعمال إشعار عادي")
                Notifier.show(this, name, mode)
                finish()
                true
            }
            mp.prepare()
            val dur = mp.duration.toLong()

            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener { }
                .build()
            am.requestAudioFocus(req)
            focus = req

            val v0 = if (fade) 0.15f * cap else cap
            mp.setVolume(v0, v0)
            mp.start()
            player = mp
            DiagLog.add(
                this,
                "بدأ صوت $name (${Modes.labels[mode]}) مدته ${dur / 1000} ث" +
                    (if (fade) " مع تدرّج" else "") + (if (cap < 1f) " بسماعات" else "")
            )

            if (fade) fadeIn(cap)

            val limit = max(240_000L, dur + 10_000L).coerceAtMost(360_000L)
            handler.postDelayed({
                DiagLog.add(this, "مهلة الأمان (${limit / 1000} ث): إيقاف")
                finish()
            }, limit)

            watchFlip()
        } catch (e: Exception) {
            DiagLog.add(this, "تعذّر تشغيل الصوت: ${e.javaClass.simpleName}")
            Notifier.show(this, name, mode)
            finish()
        }
    }

    private fun fadeIn(cap: Float) {
        val t0 = System.currentTimeMillis()
        val tick = object : Runnable {
            override fun run() {
                val p = player ?: return
                val t = ((System.currentTimeMillis() - t0).toFloat() / FADE_MS).coerceIn(0f, 1f)
                val v = (0.15f + 0.85f * log10(1f + 9f * t)) * cap
                try { p.setVolume(v, v) } catch (e: Exception) { return }
                if (t < 1f) handler.postDelayed(this, 100L)
            }
        }
        handler.post(tick)
    }

    private fun watchFlip() {
        val sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        val l = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                if (e.values[2] < -8f) {
                    DiagLog.add(this@AdhanService, "قُلب الهاتف على وجهه: إيقاف الأذان")
                    finish()
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        sm.registerListener(l, acc, SensorManager.SENSOR_DELAY_NORMAL)
        sensors = sm
        flip = l
    }

    private fun vibrate() {
        val vib = getSystemService(Vibrator::class.java) ?: return
        vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 600, 300, 600), -1))
    }

    private fun releaseAll() {
        handler.removeCallbacksAndMessages(null)
        flip?.let { sensors?.unregisterListener(it) }
        flip = null
        player?.let {
            try { if (it.isPlaying) it.stop() } catch (e: Exception) { }
            it.release()
        }
        player = null
        focus?.let { getSystemService(AudioManager::class.java).abandonAudioFocusRequest(it) }
        focus = null
    }

    private fun finish() {
        if (stopped) return
        stopped = true
        releaseAll()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        releaseAll()
        super.onDestroy()
    }
}
