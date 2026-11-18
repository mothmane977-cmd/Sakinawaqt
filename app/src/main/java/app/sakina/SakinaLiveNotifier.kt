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

    private fun renderHighResBanner(
        ctx: Context,
        dateHeader: String,
        nextLine: String,
        prevLine: String
    ): Bitmap {
        val w = 1080
        val h = 380
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)

        // 1. Draw high-res mosque background photo (#12)
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

        // 2. Luxury translucent royal navy & gold vignette overlay so text is ultra-crisp
        val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(
                    0xE6071422.toInt(),
                    0xB80A232E.toInt(),
                    0xE6071422.toInt()
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), overlayPaint)

        // 3. Double Ornate Gold Frame
        val goldBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = 0xFFD4AF37.toInt()
        }
        canvas.drawRoundRect(RectF(16f, 16f, w - 16f, h - 16f), 28f, 28f, goldBorder)
        goldBorder.strokeWidth = 1.5f
        goldBorder.color = 0x88F3DC8B.toInt()
        canvas.drawRoundRect(RectF(26f, 26f, w - 26f, h - 26f), 22f, 22f, goldBorder)

        // 4. Crisp Gold & Luminous Ivory Typography
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF3DC8B.toInt()
            textSize = 33f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setShadowLayer(6f, 0f, 3f, 0xDD000000.toInt())
        }
        val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 44f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setShadowLayer(8f, 0f, 3f, 0xEE000000.toInt())
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE6CF94.toInt()
            textSize = 31f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            setShadowLayer(6f, 0f, 2f, 0xDD000000.toInt())
        }

        canvas.drawText("❖ $dateHeader ❖", w / 2f, 95f, titlePaint)
        canvas.drawText(nextLine, w / 2f, 200f, mainPaint)
        canvas.drawText(prevLine, w / 2f, 295f, subPaint)

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
                    description = "إشعار دائم يعرض التاريخ الهجري والميلادي والصلاة الفائتة والقادمة"
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

            val titleLine = "📍 $place · $hijriStr | $gregStr".toWesternDigits()
            val nextLine = "الصلاة القادمة: $nextName (${hm.format(nextDate)}) · بقي $remStr".toWesternDigits()
            val prevLine = "الصلاة الفائتة: $prevName · مضى عليها $elapStr".toWesternDigits()
            val bodyLine = "القادمة: $nextName (${hm.format(nextDate)}) بقي $remStr  •  الفائتة: $prevName مضى $elapStr".toWesternDigits()

            val bannerBitmap = renderHighResBanner(ctx, "$place · $hijriStr · $gregStr".toWesternDigits(), nextLine, prevLine)

            val openIntent = Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pi = PendingIntent.getActivity(
                ctx, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // DeleteIntent immediately fires ACTION_RESTORE if user swipes the notification (#13)
            val restoreIntent = Intent(ACTION_RESTORE).setPackage(ctx.packageName)
            val deletePi = PendingIntent.getBroadcast(
                ctx, 777, restoreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_sakina) // Mosque Dome & Minaret icon (#22)
                .setContentTitle(titleLine)
                .setContentText(bodyLine)
                .setLargeIcon(bannerBitmap)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bannerBitmap)
                        .bigLargeIcon(null as Bitmap?)
                        .setBigContentTitle(titleLine)
                        .setSummaryText(bodyLine)
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
