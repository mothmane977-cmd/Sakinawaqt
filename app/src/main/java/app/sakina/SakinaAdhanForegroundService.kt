package app.sakina

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class SakinaAdhanForegroundService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            SakinaAdhanAudio.stop()
            stopSelf()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER) ?: "الصلاة"
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sakina:adhan_wl").apply {
                acquire(5 * 60_000L)
            }
        } catch (_: Exception) {}

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
            .setSmallIcon(R.drawable.ic_stat_sakina)
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

        startForeground(779, notif)

        SakinaAdhanAudio.playAlarmAdhan(this, prayerName) {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
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
