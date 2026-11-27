package app.sakina

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat

/**
 * خدمة الأذان المحصّنة (v69 — sakina_master_blueprint §6.4):
 * - مهلة أمان صارمة 240 ثانية (4 دقائق) لمنع تسرب WakeLock (سياسة Google Play 2026)
 * - إيقاف فوري بقلب الهاتف على وجهه (Flip-to-Silence عبر TYPE_ACCELEROMETER)
 * - مراعاة المكالمات الجارية (عدم تشغيل الصوت المرتفع أثناء مكالمة)
 * - أيقونة شريط حالة نورقنوت الموحّدة
 */
class SakinaAdhanForegroundService : Service(), SensorEventListener {
    private var wakeLock: PowerManager.WakeLock? = null
    private var sensorManager: SensorManager? = null
    private var faceDownHits = 0
    private val safetyHandler = Handler(Looper.getMainLooper())
    private val hardTimeoutRunnable = Runnable {
        SakinaAdhanAudio.stop()
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val act = intent?.action
        if (act != null && act.startsWith("app.sakina.ACTION_LUX_")) {
            SakinaLiveNotifier.handleNotificationAction(this, act)
            stopSelf(startId)
            return START_NOT_STICKY
        }
        if (act == ACTION_STOP) {
            SakinaAdhanAudio.stop()
            stopSelf()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER) ?: "الصلاة"

        // مهلة أمان صارمة 240 ثانية للـ WakeLock وللخدمة
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sakina:adhan_wl").apply {
                acquire(240_000L)
            }
        } catch (_: Exception) {}
        safetyHandler.removeCallbacks(hardTimeoutRunnable)
        safetyHandler.postDelayed(hardTimeoutRunnable, 240_000L)

        val chId = "sakina_adhan_fg_service"
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(chId, "تشغيل الأذان", NotificationManager.IMPORTANCE_HIGH).apply {
                    setSound(null, null)
                }
            )
        }

        val openPi = PendingIntent.getActivity(
            this, 780,
            Intent(this, MainActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopPi = PendingIntent.getBroadcast(
            this, 781, Intent(this, StopAdhanReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, chId)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setSmallIcon(SakinaStatusBarIconFactory.createLiveStatusIcon(Long.MAX_VALUE))
                } else {
                    setSmallIcon(R.drawable.ic_stat_sakina)
                }
            }
            .setColor(0xFFC69C53.toInt())
            .setContentTitle("حان وقت صلاة $prayerName")
            .setContentText("جاري رفع الأذان · اقلب الهاتف أو اضغط إيقاف للكتم")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(openPi)
            .setDeleteIntent(stopPi)
            .addAction(0, "إيقاف الأذان", stopPi)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            androidx.core.app.ServiceCompat.startForeground(
                this,
                779,
                notif,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(779, notif)
        }

        // مراعاة المكالمات الجارية (تجنب الصوت المرتفع أثناء مكالمة هاتفية أو صوتية)
        val am = try { getSystemService(Context.AUDIO_SERVICE) as AudioManager } catch (_: Exception) { null }
        val inCall = am != null && (am.mode == AudioManager.MODE_IN_CALL || am.mode == AudioManager.MODE_IN_COMMUNICATION)
        if (inCall) {
            safetyHandler.postDelayed({ stopSelf() }, 6000L)
            return START_NOT_STICKY
        }

        // تفعيل حساس قلب الهاتف (Flip-to-Silence) فقط أثناء تشغيل الأذان
        try {
            faceDownHits = 0
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accel != null) {
                sensorManager?.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } catch (_: Exception) {}

        SakinaQuranAudioController.pauseForAdhan()
        SakinaAdhanAudio.playAlarmAdhan(this, prayerName) {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val z = event?.values?.getOrNull(2) ?: return
        if (z < -8.0f) {
            faceDownHits++
            if (faceDownHits >= 3) {
                SakinaAdhanAudio.stop()
                stopSelf()
            }
        } else {
            faceDownHits = 0
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onDestroy() {
        safetyHandler.removeCallbacks(hardTimeoutRunnable)
        try { sensorManager?.unregisterListener(this) } catch (_: Exception) {}
        sensorManager = null
        SakinaAdhanAudio.stop()
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (_: Exception) {}
        wakeLock = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PRAYER = "prayer_name"
        const val ACTION_STOP = "app.sakina.STOP_ADHAN_FG"

        fun start(ctx: Context, prayerName: String) {
            val i = Intent(ctx, SakinaAdhanForegroundService::class.java)
                .putExtra(EXTRA_PRAYER, prayerName)
            if (Build.VERSION.SDK_INT >= 26) {
                ctx.startForegroundService(i)
            } else {
                ctx.startService(i)
            }
        }

        fun stop(ctx: Context) {
            try {
                ctx.stopService(Intent(ctx, SakinaAdhanForegroundService::class.java))
            } catch (_: Exception) {}
        }
    }
}
