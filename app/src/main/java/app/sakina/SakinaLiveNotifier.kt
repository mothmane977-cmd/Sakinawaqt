package app.sakina

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

object SakinaLiveNotifier {
    private const val CHANNEL_ID = "sakina_live_countdown"
    private const val NOTIF_ID = 9001

    fun update(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        val ch = NotificationChannel(
            CHANNEL_ID,
            "العد التنازلي للصلاة القادمة",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
        }
        nm.createNotificationChannel(ch)

        val nowMs = System.currentTimeMillis()
        val today = PrayerEngine.day(ctx, 0)
        val tomorrow = PrayerEngine.day(ctx, 1)
        val next = today.firstOrNull { it.second.time > nowMs } ?: tomorrow.first()
        val prev = today.lastOrNull { it.second.time <= nowMs } ?: PrayerEngine.day(ctx, -1).last()

        val hm = SimpleDateFormat("HH:mm", Locale.US)
        val nextTimeStr = hm.format(next.second)
        val maghribMs = today.first { it.first == "المغرب" }.second.time
        val nextFajrMs = tomorrow.first { it.first == "الفجر" }.second.time
        val midnightStr = hm.format(Date(NightMath.midnight(maghribMs, nextFajrMs)))
        val lastThirdStr = hm.format(Date(NightMath.lastThird(maghribMs, nextFajrMs)))

        val openIntent = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_sakina)
            .setColor(0xFFC5A059.toInt())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(next.second.time)
            .setShowWhen(true)
            .setContentTitle("${next.first} · $nextTimeStr (${Place.name(ctx)})")
            .setContentText("الآن: ${prev.first} | منتصف الليل $midnightStr · الثلث الأخير $lastThirdStr")
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        nm.notify(NOTIF_ID, notif)
    }
}
