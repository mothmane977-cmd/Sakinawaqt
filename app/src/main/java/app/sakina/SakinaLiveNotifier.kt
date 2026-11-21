package app.sakina

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.icu.util.IslamicCalendar
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

object SakinaLiveNotifier {
    private const val CHANNEL_ID = "sakina_live_prayer_bar"
    private const val NOTIF_ID = 777
    private const val ACTION_RESTORE = "app.sakina.ACTION_RESTORE_LIVE_NOTIF"
    private var receiverRegistered = false

    private val HIJRI_MONTHS_AR = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private fun ensureDismissRestoreReceiver(ctx: Context) {
        if (receiverRegistered) return
        try {
            val appCtx = ctx.applicationContext
            val filter = IntentFilter(ACTION_RESTORE)
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (context != null) update(context)
                }
            }
            if (Build.VERSION.SDK_INT >= 33) {
                appCtx.registerReceiver(receiver, filter, 4)
            } else {
                appCtx.registerReceiver(receiver, filter)
            }
            receiverRegistered = true
        } catch (_: Exception) {}
    }

    fun update(ctx: Context) {
        try {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!SakinaAdvancedPrefs.persistentNotifEnabled(ctx)) {
                nm.cancel(NOTIF_ID)
                return
            }
            val winCode = SakinaAdvancedPrefs.persistentNotifWindow(ctx)
            if (winCode > 0) {
                val limitMs = (if (winCode == 1) 30L else 45L) * 60_000L
                val nowCheck = System.currentTimeMillis()
                val nearAny = PrayerEngine.day(ctx, 0).any { kotlin.math.abs(it.second.time - nowCheck) <= limitMs }
                if (!nearAny) {
                    nm.cancel(NOTIF_ID)
                    return
                }
            }
            ensureDismissRestoreReceiver(ctx)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "شريط مواقيت الصلاة الحي",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "إشعار دائم يعرض التاريخ بالتقويمين والصلاة القادمة"
                    setShowBadge(false)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                nm.createNotificationChannel(ch)
            }

            val now = System.currentTimeMillis()
            val today = PrayerEngine.day(ctx, 0)
            val yesterday = PrayerEngine.day(ctx, -1)
            val tomorrow = PrayerEngine.day(ctx, 1)

            val nextIdx = today.indexOfFirst { it.second.time > now }
            val (nextName, nextDate) = if (nextIdx >= 0) today[nextIdx] else tomorrow.first()
            val (prevName, prevDate) = when {
                nextIdx > 0 -> today[nextIdx - 1]
                nextIdx == 0 -> yesterday.last()
                else -> today.last()
            }

            val remMinTotal = ((nextDate.time - now).coerceAtLeast(0L) / 60_000L)
            val remH = remMinTotal / 60
            val remM = remMinTotal % 60
            val remStr = if (remH > 0) "${remH}س و ${remM}د" else "${remM}د"

            val elapMinTotal = ((now - prevDate.time).coerceAtLeast(0L) / 60_000L)
            val elapH = elapMinTotal / 60
            val elapM = elapMinTotal % 60
            val elapStr = if (elapH > 0) "${elapH}س و ${elapM}د" else "${elapM}د"

            val gregStr = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(now))
            val ic = IslamicCalendar().apply {
                timeInMillis = now
                add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
            }
            val hijriStr = "${ic.get(IslamicCalendar.DAY_OF_MONTH)} ${HIJRI_MONTHS_AR[ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)]} ${ic.get(IslamicCalendar.YEAR)} هـ"
            val place = Place.name(ctx)
            val hm = SimpleDateFormat("HH:mm", Locale.US)

            val placeAndDate = "📍 $place · $hijriStr · $gregStr".toWesternDigits()
            val nextLine = "الصلاة القادمة: $nextName (${SakinaAdvancedPrefs.formatPrayerTime(ctx, nextDate)}) · بقي $remStr".toWesternDigits()
            val prevLine = "الصلاة الفائتة: $prevName · مضى عليها $elapStr".toWesternDigits()

            val openIntent = Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pi = PendingIntent.getActivity(
                ctx, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val restoreIntent = Intent(ACTION_RESTORE).setPackage(ctx.packageName)
            val deletePi = PendingIntent.getBroadcast(
                ctx, 777, restoreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // v18 Point 20: Clean text-only Live Notification (no large image)
            val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_sakina)
                .setContentTitle(placeAndDate)
                .setContentText(nextLine)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle(placeAndDate)
                        .bigText("$nextLine\n$prevLine")
                )
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(0xFF071422.toInt())
                .setColorized(true)
                .setContentIntent(pi)
                .setDeleteIntent(deletePi)

            val notification = builder.build()
            notification.flags = notification.flags or
                android.app.Notification.FLAG_ONGOING_EVENT or
                android.app.Notification.FLAG_NO_CLEAR

            nm.notify(NOTIF_ID, notification)
        } catch (_: Exception) {}
    }
}
