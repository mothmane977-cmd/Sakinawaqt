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

    private fun renderSolarArcCard(
        ctx: Context,
        lang: SakinaLang,
        nextBadge: String,
        nextName: String,
        nextTime: String,
        remStr: String,
        place: String,
        hijriStr: String,
        afterNextLabel: String,
        dayProgress: Float,
        prayerFractions: List<Float>,
        nextIdx: Int
    ): Bitmap {
        val w = 960
        val h = 360
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val isRtl = lang == SakinaLang.AR || lang == SakinaLang.UR

        // Deep emerald gradient background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(0xFF062920.toInt(), 0xFF0B3D30.toInt(), 0xFF07281F.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        val bgRect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        c.drawRoundRect(bgRect, 28f, 28f, bgPaint)

        // Subtle gold border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = 0x55D4AF37.toInt()
        }
        c.drawRoundRect(RectF(2f, 2f, w - 2f, h - 2f), 26f, 26f, borderPaint)

        // Subtle geometric star accent
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                if (isRtl) w * 0.24f else w * 0.76f, h * 0.45f, 220f,
                intArrayOf(0x28D4AF37.toInt(), 0x00D4AF37.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        c.drawCircle(if (isRtl) w * 0.24f else w * 0.76f, h * 0.45f, 220f, glowPaint)

        // Split: Text side (54%) and Solar Arc side (46%)
        val arcLeft = if (isRtl) 42f else w * 0.52f
        val arcRight = if (isRtl) w * 0.47f else w - 42f
        val arcWidth = arcRight - arcLeft
        val arcBaseY = 262f
        val arcPeakY = 92f

        fun arcPoint(tRaw: Float): Pair<Float, Float> {
            val t = tRaw.coerceIn(0f, 1f)
            val x = arcLeft + t * arcWidth
            val y = arcBaseY - kotlin.math.sin(t * Math.PI).toFloat() * (arcBaseY - arcPeakY)
            return x to y
        }

        // Baseline horizon
        val horizonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33FFFFFF.toInt()
            strokeWidth = 2f
        }
        c.drawLine(arcLeft - 8f, arcBaseY, arcRight + 8f, arcBaseY, horizonPaint)

        // Full solar arc path (muted teal)
        val fullArc = Path()
        for (i in 0..60) {
            val (px, py) = arcPoint(i / 60f)
            if (i == 0) fullArc.moveTo(px, py) else fullArc.lineTo(px, py)
        }
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            strokeCap = Paint.Cap.ROUND
            color = 0x44A7F3D0.toInt()
        }
        c.drawPath(fullArc, trackPaint)

        // Elapsed solar arc path (warm gold)
        val clampedProg = dayProgress.coerceIn(0.02f, 0.98f)
        val progArc = Path()
        val steps = (clampedProg * 60).toInt().coerceAtLeast(1)
        for (i in 0..steps) {
            val (px, py) = arcPoint(i / 60f)
            if (i == 0) progArc.moveTo(px, py) else progArc.lineTo(px, py)
        }
        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
            color = 0xFFE5C158.toInt()
        }
        c.drawPath(progArc, goldPaint)

        // Prayer milestone dots on arc
        val dotDone = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFE5C158.toInt() }
        val dotFuture = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x66A7F3D0.toInt() }
        val dotNextRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = 0xFFFFFFFF.toInt()
        }
        prayerFractions.forEachIndexed { idx, frac ->
            val (dx, dy) = arcPoint(frac)
            if (idx == nextIdx) {
                c.drawCircle(dx, dy, 9f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF0B3D30.toInt() })
                c.drawCircle(dx, dy, 9f, dotNextRing)
            } else {
                c.drawCircle(dx, dy, 5f, if (frac <= clampedProg) dotDone else dotFuture)
            }
        }

        // Current glowing sun dot
        val (sx, sy) = arcPoint(clampedProg)
        c.drawCircle(sx, sy, 16f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x44FDE68A.toInt() })
        c.drawCircle(sx, sy, 9.5f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFDE68A.toInt() })
        c.drawCircle(sx, sy, 5f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() })

        // Arc caption (after-next prayer)
        val arcSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD1FAE5.toInt()
            textSize = 25f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        c.drawText(afterNextLabel, (arcLeft + arcRight) / 2f, 314f, arcSubPaint)

        // Text side
        val textAnchorX = if (isRtl) w - 42f else 42f
        val textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT

        // Pill badge: "التالي" + Prayer Name
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE5C158.toInt()
            textSize = 36f
            this.textAlign = textAlign
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        c.drawText("$nextBadge · $nextName", textAnchorX, 82f, namePaint)

        // Giant crisp time
        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 76f
            this.textAlign = textAlign
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        c.drawText(nextTime, textAnchorX, 174f, timePaint)

        // Countdown line
        val remPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFA7F3D0.toInt()
            textSize = 30f
            this.textAlign = textAlign
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        c.drawText(remStr, textAnchorX, 232f, remPaint)

        // City + Hijri Date
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD1D5DB.toInt()
            textSize = 25f
            this.textAlign = textAlign
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        c.drawText("$place · $hijriStr", textAnchorX, 294f, metaPaint)

        return bmp
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
                        "إشعار دائم يعرض الصلاة القادمة والقوس الشمسي والتاريخ بدون إزعاج",
                        "Quiet ongoing notification showing next prayer, solar arc and date",
                        "Notification permanente affichant la prochaine prière et l'arc solaire",
                        "Sonraki namazı ve güneş yayını sessizce gösterir",
                        "Notifikasi aktif jadwal sholat dan busur matahari",
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
            val tomorrow = PrayerEngine.day(ctx, 1)

            val nextIdx = today.indexOfFirst { it.second.time > now }
            val isTomorrow = nextIdx < 0
            val resolvedNextIdx = if (nextIdx >= 0) nextIdx else 0
            val (_, nextDate) = if (nextIdx >= 0) today[nextIdx] else tomorrow.first()

            val afterIdx = (resolvedNextIdx + 1) % 5
            val (_, afterDate) = when {
                !isTomorrow && resolvedNextIdx + 1 < today.size -> today[resolvedNextIdx + 1]
                !isTomorrow -> tomorrow.first()
                else -> tomorrow.getOrElse(1) { tomorrow.first() }
            }

            val calNow = Calendar.getInstance().apply { timeInMillis = now }
            val isFriday = calNow.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY

            val nextDisplayName = SakinaAdvancedPrefs.displayPrayerName(ctx, lang, resolvedNextIdx, isFriday && !isTomorrow)
            val afterDisplayName = SakinaAdvancedPrefs.displayPrayerName(ctx, lang, afterIdx, false)

            val remMinTotal = ((nextDate.time - now).coerceAtLeast(0L) / 60_000L)
            val remH = remMinTotal / 60
            val remM = remMinTotal % 60
            val remCompact = SakinaAdvancedPrefs.formatDigits(
                ctx,
                when (lang) {
                    SakinaLang.AR, SakinaLang.UR -> if (remH > 0) "${remH}س ${remM}د" else "${remM}د"
                    SakinaLang.FR -> if (remH > 0) "${remH}h ${remM}m" else "${remM}m"
                    SakinaLang.TR -> if (remH > 0) "${remH}s ${remM}d" else "${remM}d"
                    SakinaLang.ID -> if (remH > 0) "${remH}j ${remM}m" else "${remM}m"
                    else -> if (remH > 0) "${remH}h ${remM}m" else "${remM}m"
                }
            )

            val nextTimeFmt = SakinaAdvancedPrefs.formatPrayerTime(ctx, nextDate)
            val afterTimeFmt = SakinaAdvancedPrefs.formatPrayerTime(ctx, afterDate)

            val ic = IslamicCalendar().apply {
                timeInMillis = now
                add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
            }
            val hijriMonth = s.hijriMonths.getOrElse(ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)) { "" }
            val hijriShort = SakinaAdvancedPrefs.formatDigits(ctx, "${ic.get(IslamicCalendar.DAY_OF_MONTH)} $hijriMonth")
            val place = localizeCityName(Place.name(ctx), lang)

            // Compact collapsed lines — zero truncation on Xiaomi/HyperOS
            val inWord = sakinaTr(lang, "بعد", "In", "Dans", "Kalan", "Dalam", "بعد")
            val collapsedTitle = "$nextDisplayName $nextTimeFmt · $inWord $remCompact"
            val collapsedSub = "$place · $hijriShort"

            // Solar arc day progress between Fajr and Isha
            val fajrMs = today.first().second.time
            val ishaMs = today.last().second.time
            val span = (ishaMs - fajrMs).coerceAtLeast(1L).toFloat()
            val dayProgress = ((now - fajrMs).toFloat() / span).coerceIn(0f, 1f)
            val fractions = today.map { ((it.second.time - fajrMs).toFloat() / span).coerceIn(0f, 1f) }

            val nextBadge = sakinaTr(lang, "التالي", "Next", "Suivant", "Sonraki", "Berikutnya", "اگلی")
            val afterWord = sakinaTr(lang, "ثم", "Then", "Puis", "Sonra", "Lalu", "پھر")
            val afterLabel = "$afterWord $afterDisplayName $afterTimeFmt"

            val solarBmp = renderSolarArcCard(
                ctx = ctx,
                lang = lang,
                nextBadge = nextBadge,
                nextName = nextDisplayName,
                nextTime = nextTimeFmt,
                remStr = "$inWord $remCompact",
                place = place,
                hijriStr = hijriShort,
                afterNextLabel = afterLabel,
                dayProgress = dayProgress,
                prayerFractions = fractions,
                nextIdx = resolvedNextIdx
            )

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
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(solarBmp)
                        .setBigContentTitle(collapsedTitle)
                        .setSummaryText(collapsedSub)
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
                .addAction(0, sakinaTr(lang, "افتح سَكِينة", "Open Sakina", "Ouvrir Sakina", "Sakina'yı Aç", "Buka Sakina", "سکینہ کھولیں"), openPi)
                .addAction(0, sakinaTr(lang, "تحديث ↻", "Refresh ↻", "Actualiser ↻", "Yenile ↻", "Segarkan ↻", "تازہ ↻"), refreshPi)

            val notification = builder.build()
            notification.flags = notification.flags or
                android.app.Notification.FLAG_ONGOING_EVENT or
                android.app.Notification.FLAG_NO_CLEAR

            nm.notify(NOTIF_ID, notification)
        } catch (_: Exception) {}
    }
}
