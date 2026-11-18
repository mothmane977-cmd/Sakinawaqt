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

    private fun renderBorderlessBanner(
        ctx: Context,
        placeAndDate: String,
        nextLine: String,
        prevLine: String
    ): Bitmap {
        val w = 1080
        val h = 360
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)

        // 1. Full-bleed edge-to-edge high-res photo with NO borders and NO frames (#1)
        try {
            val src = BitmapFactory.decodeResource(ctx.resources, R.drawable.bg_header_mosque)
            if (src != null) {
                val srcRatio = src.width.toFloat() / src.height.toFloat()
                val dstRatio = w.toFloat() / h.toFloat()
                val srcRect = if (srcRatio > dstRatio) {
                    val cropW = (src.height * dstRatio).toInt()
                    val left = (src.width - cropW) / 2
                    Rect(left, 0, left + cropW, src.height)
                } else {
                    val cropH = (src.width / dstRatio).toInt()
                    val top = (src.height - cropH) / 2
                    Rect(0, top, src.width, top + cropH)
                }
                canvas.drawBitmap(src, srcRect, Rect(0, 0, w, h), Paint(Paint.FILTER_BITMAP_FLAG))
            }
        } catch (_: Exception) {}

        // 2. Smooth translucent navy vignette (no border lines)
        val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(0xD9071422.toInt(), 0xA80A232E.toInt(), 0xD9071422.toInt()),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), overlayPaint)

        // 3. Mosque Dome & Minaret icon + small ultra-crisp "سَكِينة" right underneath it (#1)
        try {
            val iconBmp = BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_stat_sakina)
            if (iconBmp != null) {
                val iconSize = 54
                val iconX = w - 96f
                val iconY = 22f
                val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = PorterDuffColorFilter(0xFFF3DC8B.toInt(), PorterDuff.Mode.SRC_IN)
                }
                canvas.drawBitmap(
                    iconBmp,
                    null,
                    RectF(iconX, iconY, iconX + iconSize, iconY + iconSize),
                    iconPaint
                )
                val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    textSize = 24f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    setShadowLayer(5f, 0f, 2f, 0xFF000000.toInt())
                }
                canvas.drawText("سَكِينة", iconX + iconSize / 2f, iconY + iconSize + 24f, brandPaint)
            }
        } catch (_: Exception) {}

        // 4. Crisp High-Contrast Typography
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF3DC8B.toInt()
            textSize = 31f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setShadowLayer(6f, 0f, 3f, 0xDD000000.toInt())
        }
        val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 42f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setShadowLayer(8f, 0f, 3f, 0xEE000000.toInt())
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE6CF94.toInt()
            textSize = 30f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            setShadowLayer(6f, 0f, 2f, 0xDD000000.toInt())
        }

        canvas.drawText(placeAndDate, w / 2f - 28f, 84f, datePaint)
        canvas.drawText(nextLine, w / 2f, 192f, mainPaint)
        canvas.drawText(prevLine, w / 2f, 286f, subPaint)

        return out
    }

    fun update(ctx: Context) {
        try {
            ensureDismissRestoreReceiver(ctx)
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "شريط مواقيت الصلاة الحي",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "إشعار دائم يعرض صورة مواقيت الصلاة"
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
            val nextLine = "الصلاة القادمة: $nextName (${hm.format(nextDate)}) · بقي $remStr".toWesternDigits()
            val prevLine = "الصلاة الفائتة: $prevName · مضى عليها $elapStr".toWesternDigits()

            val bannerBitmap = renderBorderlessBanner(ctx, placeAndDate, nextLine, prevLine)

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

            // Image-only BigPictureStyle with minimal empty header/summary so the borderless image takes center stage (#1)
            val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_sakina)
                .setContentTitle(" ")
                .setContentText(" ")
                .setLargeIcon(bannerBitmap)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bannerBitmap)
                        .bigLargeIcon(null as Bitmap?)
                        .setBigContentTitle(" ")
                        .setSummaryText(" ")
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
