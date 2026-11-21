package app.sakina

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.IslamicCalendar
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

object SakinaLiveNotifier {
    private const val CHANNEL_ID = "sakina_live_prayer_bar"
    private const val NOTIF_ID = 777
    private const val ACTION_REFRESH = "app.sakina.ACTION_REFRESH_LIVE_NOTIF"
    private var receiverRegistered = false

    private fun ensureTickAndRefreshReceiver(ctx: Context) {
        if (receiverRegistered) return
        try {
            val appCtx = ctx.applicationContext
            val filter = IntentFilter().apply {
                addAction(ACTION_REFRESH)
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (context != null) {
                        update(context)
                        if (intent?.action == ACTION_REFRESH) {
                            SakinaWidgetUpdater.updateAll(context)
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= 33) {
                appCtx.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                appCtx.registerReceiver(receiver, filter)
            }
            receiverRegistered = true
        } catch (_: Exception) {}
    }

    private fun activeLang(ctx: Context): SakinaLang {
        val code = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("app_lang", "ar") ?: "ar"
        return SakinaLang.entries.firstOrNull { it.code == code } ?: SakinaLang.AR
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

            ensureTickAndRefreshReceiver(ctx)

            val lang = activeLang(ctx)
            val s = stringsFor(lang)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val chName = sakinaTr(
                    lang,
                    "شريط مواقيت الصلاة الحي",
                    "Live Prayer Times Bar",
                    "Barre des prières en direct",
                    "Canlı Namaz Vakitleri",
                    "Notifikasi Jadwal Sholat",
                    "لائیو نماز بار"
                )
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    chName,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = sakinaTr(
                        lang,
                        "إشعار دائم يعرض الصلاة القادمة والعدّ التنازلي والتاريخ بدون إزعاج",
                        "Quiet ongoing notification showing next prayer, countdown and date",
                        "Notification permanente affichant la prochaine prière et le compte à rebours",
                        "Sonraki namazı ve geri sayımı sessizce gösterir",
                        "Notifikasi aktif jadwal sholat dan hitung mundur",
                        "آنے والی نماز اور تاریخ کی خاموش اطلاع"
                    )
                    setShowBadge(false)
                    setSound(null, null)
                    enableVibration(false)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                nm.createNotificationChannel(ch)
            }

            val now = System.currentTimeMillis()
            val today = PrayerEngine.day(ctx, 0)
            val yesterday = PrayerEngine.day(ctx, -1)
            val tomorrow = PrayerEngine.day(ctx, 1)

            val nextIdx = today.indexOfFirst { it.second.time > now }
            val isTomorrow = nextIdx < 0
            val resolvedNextIdx = if (nextIdx >= 0) nextIdx else 0
            val (_, nextDate) = if (nextIdx >= 0) today[nextIdx] else tomorrow.first()
            val prevIdx = when {
                nextIdx > 0 -> nextIdx - 1
                else -> 4
            }
            val (_, prevDate) = when {
                nextIdx > 0 -> today[nextIdx - 1]
                nextIdx == 0 -> yesterday.last()
                else -> today.last()
            }

            val calNow = Calendar.getInstance().apply { timeInMillis = now }
            val isFriday = calNow.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY

            // Contextual Friday Jumu'ah name when Dhuhr is next on Friday
            val nextDisplayName = if (isFriday && !isTomorrow && resolvedNextIdx == 1) {
                sakinaTr(lang, "الجمعة", "Jumu'ah", "Jumu'ah", "Cuma", "Jumat", "جمعہ")
            } else {
                s.prayers.getOrElse(resolvedNextIdx) { today[resolvedNextIdx].first }
            }
            val prevDisplayName = s.prayers.getOrElse(prevIdx) { today[prevIdx].first }

            val remMinTotal = ((nextDate.time - now).coerceAtLeast(0L) / 60_000L)
            val remH = remMinTotal / 60
            val remM = remMinTotal % 60
            val remStr = when (lang) {
                SakinaLang.AR, SakinaLang.UR -> if (remH > 0) "${remH}س و ${remM}د" else "${remM}د"
                SakinaLang.FR -> if (remH > 0) "${remH}h ${remM}min" else "${remM} min"
                SakinaLang.TR -> if (remH > 0) "${remH}sa ${remM}dk" else "${remM} dk"
                SakinaLang.ID -> if (remH > 0) "${remH}j ${remM}m" else "${remM} mnt"
                else -> if (remH > 0) "${remH}h ${remM}m" else "${remM}m"
            }.toWesternDigits()

            val elapMinTotal = ((now - prevDate.time).coerceAtLeast(0L) / 60_000L)
            val elapH = elapMinTotal / 60
            val elapM = elapMinTotal % 60
            val elapStr = when (lang) {
                SakinaLang.AR, SakinaLang.UR -> if (elapH > 0) "${elapH}س و ${elapM}د" else "${elapM}د"
                SakinaLang.FR -> if (elapH > 0) "${elapH}h ${elapM}min" else "${elapM} min"
                SakinaLang.TR -> if (elapH > 0) "${elapH}sa ${elapM}dk" else "${elapM} dk"
                SakinaLang.ID -> if (elapH > 0) "${elapH}j ${elapM}m" else "${elapM} mnt"
                else -> if (elapH > 0) "${elapH}h ${elapM}m" else "${elapM}m"
            }.toWesternDigits()

            val nextTimeFmt = SakinaAdvancedPrefs.formatPrayerTime(ctx, nextDate).toWesternDigits()
            val gregStr = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(now)).toWesternDigits()
            val ic = IslamicCalendar().apply {
                timeInMillis = now
                add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
            }
            val hijriMonth = s.hijriMonths.getOrElse(ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)) { "" }
            val hijriSuffix = sakinaTr(lang, "هـ", "AH", "H", "H", "H", "ھ")
            val hijriStr = "${ic.get(IslamicCalendar.DAY_OF_MONTH)} $hijriMonth ${ic.get(IslamicCalendar.YEAR)} $hijriSuffix".toWesternDigits()
            val place = localizeCityName(Place.name(ctx), lang)

            val imminentBadge = if (remMinTotal in 1..15) {
                " · " + sakinaTr(lang, "اقترب الوقت", "Soon", "Bientôt", "Yaklaştı", "Segera", "قریب ہے")
            } else ""
            val silentBadge = if (SakinaAdvancedPrefs.autoSilentEnabled(ctx)) " · 🔇" else ""

            // Collapsed header & concise next-prayer line
            val collapsedTitle = "$nextDisplayName ($nextTimeFmt) · ${sakinaTr(lang, "بقي", "In", "Dans", "Kalan", "Dalam", "باقی")} $remStr$imminentBadge$silentBadge".toWesternDigits()
            val collapsedSub = "$place · $hijriStr · $gregStr".toWesternDigits()

            // Expanded view: full 5-prayer daily schedule + elapsed since previous prayer
            val allFiveLine = today.take(5).mapIndexed { idx, (_, dt) ->
                val pName = s.prayers.getOrElse(idx) { "" }
                "$pName ${SakinaAdvancedPrefs.formatPrayerTime(ctx, dt)}"
            }.joinToString(" · ").toWesternDigits()
            val prevLine = "${sakinaTr(lang, "مضى على", "Since", "Depuis", "Geçen", "Sejak", "گزرے")} $prevDisplayName: $elapStr".toWesternDigits()

            val openIntent = Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openPi = PendingIntent.getActivity(
                ctx, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val refreshIntent = Intent(ACTION_REFRESH).setPackage(ctx.packageName)
            val refreshPi = PendingIntent.getBroadcast(
                ctx, 777, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_sakina)
                .setContentTitle(collapsedTitle)
                .setContentText(collapsedSub)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle(collapsedTitle)
                        .bigText("$collapsedSub\n$allFiveLine\n$prevLine")
                )
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setShowWhen(false)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(0xFFC5A059.toInt())
                .setContentIntent(openPi)
                .setDeleteIntent(refreshPi)
                .addAction(0, sakinaTr(lang, "تحديث الآن ↻", "Refresh ↻", "Actualiser ↻", "Yenile ↻", "Segarkan ↻", "تازہ کریں ↻"), refreshPi)
                .addAction(0, sakinaTr(lang, "افتح سَكِينة", "Open Sakina", "Ouvrir Sakina", "Sakina'yı Aç", "Buka Sakina", "سکینہ کھولیں"), openPi)

            val notification = builder.build()
            notification.flags = notification.flags or
                android.app.Notification.FLAG_ONGOING_EVENT or
                android.app.Notification.FLAG_NO_CLEAR

            nm.notify(NOTIF_ID, notification)
        } catch (_: Exception) {}
    }
}
