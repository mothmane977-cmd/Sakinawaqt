package app.sakina

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

/**
 * Sakina v53 — Unified Luxury Notification System (لوحة الأفق الحيّ + الدورة الثلاثية للصلوات)
 * Harmonized with Sakina's midnight-navy-emerald (#081B2C -> #0E3B34) and champagne gold (#F3DFA2 / #D4AF37) palette.
 */
object SakinaLuxuryNotifications {

    const val LIVE_CHANNEL_ID = "sakina_live_prayer_bar"
    const val STAGE_CHANNEL_ID = "sakina_prayer_lifecycle_channel"
    const val LIVE_NOTIF_ID = 777
    const val STAGE_NOTIF_ID = 1002

    const val ACTION_MARK_PRAYED = "app.sakina.ACTION_LUX_MARK_PRAYED"
    const val ACTION_CYCLE_ALERT = "app.sakina.ACTION_LUX_CYCLE_ALERT"
    const val ACTION_TASBIH_TAP = "app.sakina.ACTION_LUX_TASBIH_TAP"
    const val ACTION_PREPARED = "app.sakina.ACTION_LUX_PREPARED"
    const val ACTION_SNOOZE_5M = "app.sakina.ACTION_LUX_SNOOZE_5M"
    const val ACTION_STOP_ADHAN = "app.sakina.ACTION_LUX_STOP_ADHAN"
    const val ACTION_IQAMA_COUNTDOWN = "app.sakina.ACTION_LUX_IQAMA_COUNTDOWN"
    const val ACTION_POST_DHIKR_TAP = "app.sakina.ACTION_LUX_POST_DHIKR_TAP"
    const val ACTION_POST_DHIKR_DONE = "app.sakina.ACTION_LUX_POST_DHIKR_DONE"

    // Harmonized Sakina midnight-navy-emerald sky phases
    enum class SkyPhase(val topColor: Int, val bottomColor: Int, val arcColor: Int) {
        FAJR_DAWN(0xFF061524.toInt(), 0xFF0E3E37.toInt(), 0xFFF3DFA2.toInt()),
        DHUHR_GOLD(0xFF081D31.toInt(), 0xFF13483D.toInt(), 0xFFD4AF37.toInt()),
        MAGHRIB_TWILIGHT(0xFF0A1B2E.toInt(), 0xFF283A33.toInt(), 0xFFF3DFA2.toInt()),
        NIGHT_VIGIL(0xFF05101E.toInt(), 0xFF0B2736.toInt(), 0xFFD4AF37.toInt())
    }

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val liveCh = NotificationChannel(
            LIVE_CHANNEL_ID,
            "شريط مواقيت الصلاة الحي",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "لوحة الأفق الحيّ الدائمة لمواقيت الصلاة والتسبيح بدون إزعاج"
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(liveCh)

        val stageCh = NotificationChannel(
            STAGE_CHANNEL_ID,
            "دورة إشعارات الصلاة (الاستعداد · الأذان · الأذكار)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات ما قبل الصلاة وعند الأذان وأذكار الختام"
            setShowBadge(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(stageCh)
    }

    /**
     * 1. الإشعار الحيّ الدائم (لوحة الأفق الحيّ + تخطيط مضغوط 64dp للوضع المطوي)
     */
    fun showLiveHorizonNotification(
        context: Context,
        prayerName: String,
        prayerTimeFormatted: String,
        remainingMillis: Long,
        cityAndHijri: String,
        fivePrayersStrip: String,
        alertModeLabel: String,
        tasbihCount: Int,
        skyPhase: SkyPhase,
        dayProgressFraction: Float,
        contentPendingIntent: PendingIntent,
        deletePendingIntent: PendingIntent? = null
    ) {
        ensureChannels(context)
        val countdownStr = formatCountdown(remainingMillis)

        // وضع مطوي مخصّص (64dp) يمنع قصّ الأزرار في Android 12+ / HyperOS
        val collapsedRv = RemoteViews(context.packageName, R.layout.notif_sakina_live_collapsed).apply {
            setTextViewText(R.id.tv_collapsed_prayer, "$prayerName · $prayerTimeFormatted")
            setTextViewText(R.id.tv_collapsed_countdown, countdownStr)
            setTextViewText(R.id.tv_collapsed_meta, cityAndHijri)
        }

        // الوضع الموسّع الكامل (لوحة الأفق الحيّ)
        val expandedRv = RemoteViews(context.packageName, R.layout.notif_sakina_live_horizon).apply {
            setImageViewBitmap(
                R.id.iv_horizon_backdrop,
                renderHorizonBackdropBitmap(skyPhase, dayProgressFraction.coerceIn(0f, 1f))
            )
            setTextViewText(R.id.tv_live_city_hijri, cityAndHijri)
            setTextViewText(R.id.tv_next_prayer, "$prayerName $prayerTimeFormatted")
            setTextViewText(R.id.tv_remaining_time, countdownStr)
            setTextViewText(R.id.tv_five_prayers_strip, fivePrayersStrip)
            val spNotif = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val activeIdx = spNotif.getInt("live_active_prayer_idx", 0).coerceIn(0, 4)
            val todayK = SakinaAdvancedPrefs.dateKeyOffset(0)
            val prayedNow = SakinaAdvancedPrefs.isPrayerLogged(context, todayK, activeIdx)
            setTextViewText(R.id.btn_live_prayed, if (prayedNow) "✓ تمّت الصلاة" else "○ صليت")
            setTextViewText(R.id.btn_live_alert_mode, alertModeLabel)
            setTextViewText(R.id.btn_live_tasbih, "تسبيحة ${tasbihCount % 34}/33")

            setOnClickPendingIntent(R.id.btn_live_prayed, actionPi(context, ACTION_MARK_PRAYED, 1))
            setOnClickPendingIntent(R.id.btn_live_alert_mode, actionPi(context, ACTION_CYCLE_ALERT, 2))
            setOnClickPendingIntent(R.id.btn_live_tasbih, actionPi(context, ACTION_TASBIH_TAP, 3))
        }

        val builder = NotificationCompat.Builder(context, LIVE_CHANNEL_ID)
            .setCustomContentView(collapsedRv)
            .setCustomBigContentView(expandedRv)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFFD4AF37.toInt())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setSmallIcon(SakinaStatusBarIconFactory.createLiveStatusIcon(remainingMillis))
        } else {
            builder.setSmallIcon(R.drawable.ic_stat_sakina)
        }
        if (deletePendingIntent != null) {
            builder.setDeleteIntent(deletePendingIntent)
        }

        val notif = builder.build()
        notif.flags = notif.flags or
            android.app.Notification.FLAG_ONGOING_EVENT or
            android.app.Notification.FLAG_NO_CLEAR

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(LIVE_NOTIF_ID, notif)
    }

    /**
     * 2. إشعار ما قبل الصلاة (نداء الاستعداد)
     */
    fun showPrePrayerNotification(
        context: Context,
        prayerName: String,
        minutesLeft: Int,
        prayerTimeFormatted: String,
        contentPendingIntent: PendingIntent
    ) {
        ensureChannels(context)
        val rv = RemoteViews(context.packageName, R.layout.notif_sakina_lifecycle_stage).apply {
            setImageViewBitmap(
                R.id.iv_stage_backdrop,
                renderStageBackdropBitmap(0xFF0E3B34.toInt(), 0xFFD4AF37.toInt(), progress = 0.78f)
            )
            setTextViewText(R.id.tv_stage_title, "اقترب أذان $prayerName (بعد $minutesLeft د)")
            setTextViewText(R.id.tv_stage_subtitle, "حان وقت التهيؤ والوضوء · $prayerTimeFormatted")
            setViewVisibility(R.id.tv_stage_parchment_dhikr, View.GONE)

            setTextViewText(R.id.btn_stage_primary, "✓ استعددت")
            setTextViewText(R.id.btn_stage_secondary, "⏳ قبل 5 د")
            setViewVisibility(R.id.btn_stage_tertiary, View.GONE)

            setOnClickPendingIntent(R.id.btn_stage_primary, actionPi(context, ACTION_PREPARED, 11))
            setOnClickPendingIntent(R.id.btn_stage_secondary, actionPi(context, ACTION_SNOOZE_5M, 12))
        }
        notifyStage(context, rv, contentPendingIntent, onlyAlertOnce = false)
    }

    /**
     * 3. إشعار عند دخول وقت الصلاة (نداء الأذان الحيّ)
     */
    fun showAtAdhanNotification(
        context: Context,
        prayerName: String,
        prayerTimeFormatted: String,
        iqamaMinutes: Int,
        contentPendingIntent: PendingIntent
    ) {
        ensureChannels(context)
        val rv = RemoteViews(context.packageName, R.layout.notif_sakina_lifecycle_stage).apply {
            setImageViewBitmap(
                R.id.iv_stage_backdrop,
                renderStageBackdropBitmap(0xFF13483D.toInt(), 0xFFF3DFA2.toInt(), progress = 1f)
            )
            setTextViewText(R.id.tv_stage_title, "حان الآن موعد أذان $prayerName · $prayerTimeFormatted")
            setTextViewText(R.id.tv_stage_subtitle, "الإقامة بعد $iqamaMinutes د · نور قنوت")
            setViewVisibility(R.id.tv_stage_parchment_dhikr, View.VISIBLE)
            setTextViewText(R.id.tv_stage_parchment_dhikr, "قال رسول الله ﷺ: «أحبُّ الأعمال إلى الله: الصلاةُ على وقتها» — متفق عليه")

            setTextViewText(R.id.btn_stage_primary, "🔇 إيقاف")
            setTextViewText(R.id.btn_stage_secondary, "✓ صليت")
            setViewVisibility(R.id.btn_stage_tertiary, View.VISIBLE)
            setTextViewText(R.id.btn_stage_tertiary, "⏱ الإقامة")

            setOnClickPendingIntent(R.id.btn_stage_primary, actionPi(context, ACTION_STOP_ADHAN, 21))
            setOnClickPendingIntent(R.id.btn_stage_secondary, actionPi(context, ACTION_MARK_PRAYED, 22))
            setOnClickPendingIntent(R.id.btn_stage_tertiary, actionPi(context, ACTION_IQAMA_COUNTDOWN, 23))
        }
        notifyStage(context, rv, contentPendingIntent, onlyAlertOnce = false)
    }

    /**
     * 4. إشعار ما بعد الصلاة (أذكار الختام بالرقعة الكريمية)
     */
    fun showPostPrayerNotification(
        context: Context,
        prayerName: String,
        dhikrText: String,
        tasbihCount: Int,
        contentPendingIntent: PendingIntent
    ) {
        ensureChannels(context)
        val rv = RemoteViews(context.packageName, R.layout.notif_sakina_lifecycle_stage).apply {
            setImageViewBitmap(
                R.id.iv_stage_backdrop,
                renderStageBackdropBitmap(0xFF0E3B34.toInt(), 0xFF1EE3A8.toInt(), progress = 0f)
            )
            setTextViewText(R.id.tv_stage_title, "أذكار ما بعد صلاة $prayerName")
            setTextViewText(R.id.tv_stage_subtitle, "نور قنوت · أذكار الختام")
            setViewVisibility(R.id.tv_stage_parchment_dhikr, View.VISIBLE)
            setTextViewText(R.id.tv_stage_parchment_dhikr, dhikrText)

            setTextViewText(R.id.btn_stage_primary, "📿 تسبيح ${tasbihCount % 34}/33")
            setTextViewText(R.id.btn_stage_secondary, "✓ أتممتُها")
            setViewVisibility(R.id.btn_stage_tertiary, View.GONE)

            setOnClickPendingIntent(R.id.btn_stage_primary, actionPi(context, ACTION_POST_DHIKR_TAP, 31))
            setOnClickPendingIntent(R.id.btn_stage_secondary, actionPi(context, ACTION_POST_DHIKR_DONE, 32))
        }
        notifyStage(context, rv, contentPendingIntent, onlyAlertOnce = true)
    }

    fun dismissStageNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(STAGE_NOTIF_ID)
    }

    private fun notifyStage(
        context: Context,
        rv: RemoteViews,
        contentPendingIntent: PendingIntent,
        onlyAlertOnce: Boolean
    ) {
        val notif = NotificationCompat.Builder(context, STAGE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_sakina)
            .setCustomContentView(rv)
            .setCustomBigContentView(rv)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(contentPendingIntent)
            .setOnlyAlertOnce(onlyAlertOnce)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFFD4AF37.toInt())
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(STAGE_NOTIF_ID, notif)
    }

    private fun actionPi(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            setPackage(context.packageName)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatCountdown(remainingMillis: Long): String {
        val totalMin = (remainingMillis.coerceAtLeast(0L) + 59_999L) / 60_000L
        val h = totalMin / 60L
        val m = totalMin % 60L
        return String.format(Locale.US, "-%02d:%02d", h, m)
    }

    private fun renderHorizonBackdropBitmap(phase: SkyPhase, dayProgress: Float): Bitmap {
        val w = 900
        val h = 360
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(phase.topColor, 0xFF0A2033.toInt(), phase.bottomColor),
                floatArrayOf(0f, 0.52f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Luminous horizon dome glow
        val sunX = w * (0.18f + 0.64f * dayProgress)
        val sunY = h * 0.60f - sin(dayProgress * PI).toFloat() * (h * 0.34f)
        val halo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                sunX, sunY, 210f,
                intArrayOf(0x48F3DFA2, 0x1A1EE3A8, 0x00000000),
                floatArrayOf(0f, 0.48f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        c.drawCircle(sunX, sunY, 210f, halo)

        // Subtle solar arc across the horizon
        val arcPath = Path()
        for (i in 0..48) {
            val t = i / 48f
            val ax = w * (0.14f + 0.72f * t)
            val ay = h * 0.64f - sin(t * PI).toFloat() * (h * 0.36f)
            if (i == 0) arcPath.moveTo(ax, ay) else arcPath.lineTo(ax, ay)
        }
        c.drawPath(
            arcPath,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = 0x44F3DFA2
            }
        )
        c.drawCircle(sunX, sunY, 7f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = phase.arcColor })
        return bmp
    }

    private fun renderStageBackdropBitmap(emeraldTint: Int, accentGold: Int, progress: Float): Bitmap {
        val w = 900
        val h = 360
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(0xFF061322.toInt(), 0xFF0A2033.toInt(), emeraldTint),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.22f, h * 0.42f, 220f,
                intArrayOf((accentGold and 0x00FFFFFF) or 0x44000000, 0x00000000),
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.drawCircle(w * 0.22f, h * 0.42f, 220f, glow)

        if (progress > 0f) {
            val barY = h * 0.54f
            val left = w * 0.08f
            val right = w * 0.92f
            c.drawRoundRect(
                RectF(left, barY - 4f, right, barY + 4f), 4f, 4f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33FFFFFF }
            )
            c.drawRoundRect(
                RectF(left, barY - 4f, left + (right - left) * progress.coerceIn(0f, 1f), barY + 4f), 4f, 4f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentGold }
            )
        }
        return bmp
    }
}
