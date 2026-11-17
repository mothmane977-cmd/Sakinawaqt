package app.sakina

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.IslamicCalendar
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

object SakinaLiveNotifier {
    private const val CHANNEL_ID = "sakina_live_countdown"
    private const val NOTIF_ID = 9001

    private val HIJRI_M = listOf(
        "محرم", "صفر", "ربيع1", "ربيع2", "جمادى1", "جمادى2",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private fun formatDur(ms: Long): String {
        val totalMin = (ms.coerceAtLeast(0L) / 60_000L).toInt()
        val h = totalMin / 60
        val m = totalMin % 60
        return if (h > 0) "${h}س و${m}د" else "${m}د"
    }

    fun update(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        val ch = NotificationChannel(CHANNEL_ID, "العد التنازلي للصلاة القادمة", NotificationManager.IMPORTANCE_LOW).apply {
            setShowBadge(false); enableLights(false); enableVibration(false); setSound(null, null)
        }
        nm.createNotificationChannel(ch)

        val nowMs = System.currentTimeMillis()
        val today = PrayerEngine.day(ctx, 0)
        val tomorrow = PrayerEngine.day(ctx, 1)
        val yesterday = PrayerEngine.day(ctx, -1)

        val next = today.firstOrNull { it.second.time > nowMs } ?: tomorrow.first()
        val prev = today.lastOrNull { it.second.time <= nowMs } ?: yesterday.last()

        val remStr = formatDur(next.second.time - nowMs)
        val elapsedStr = formatDur(nowMs - prev.second.time)

        val gDate = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(nowMs))
        val ic = IslamicCalendar().apply {
            timeInMillis = nowMs
            add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
        }
        val hDate = "${ic.get(IslamicCalendar.DAY_OF_MONTH)} ${HIJRI_M[ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)]} ${ic.get(IslamicCalendar.YEAR)}هـ"
        val place = Place.name(ctx)

        val titleLine = "القادمة: ${next.first} (بقي $remStr) · الفائتة: ${prev.first} (مضى $elapsedStr)".toWesternDigits()
        val subLine = "$place · $hDate | $gDate".toWesternDigits()

        val openIntent = PendingIntent.getActivity(ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)

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
            .setSubText(subLine)
            .setContentTitle(titleLine)
            .setContentText(
                if (prev.first == "العشاء") {
                    val hm = SimpleDateFormat("HH:mm", Locale.US)
                    val maghribMs = today.first { it.first == "المغرب" }.second.time
                    val nextFajrMs = tomorrow.first { it.first == "الفجر" }.second.time
                    "منتصف الليل ${hm.format(Date(NightMath.midnight(maghribMs, nextFajrMs)))} · الثلث الأخير ${hm.format(Date(NightMath.lastThird(maghribMs, nextFajrMs)))}".toWesternDigits()
                } else subLine
            )
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        nm.notify(NOTIF_ID, notif)
    }
}
