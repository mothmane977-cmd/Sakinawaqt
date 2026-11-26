package app.sakina

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat

/**
 * Sakina v54 — Architectural Status Bar Dynamic Icon Factory
 * - remainingMillis >= 60 min: Solid pointed mosque dome + 2 sturdy tiered minarets + crescent + real bold Arabic "قنوت"
 * - remainingMillis <  60 min: Extra-wide graceful hollow dome cradling large bold minutes + 2 tiered minarets + "قنوت"
 */
object SakinaStatusBarIconFactory {

    fun createLiveStatusIcon(remainingMillis: Long): IconCompat {
        val bitmap = if (remainingMillis >= 3_600_000L) {
            renderFilledMosqueBitmap(sizePx = 96)
        } else {
            val mins = ((remainingMillis + 59_999L) / 60_000L).coerceIn(0L, 59L).toInt()
            renderHollowDomeWithMinutesBitmap(minutes = mins, sizePx = 96)
        }
        return IconCompat.createWithBitmap(bitmap)
    }

    fun renderFilledMosqueBitmap(sizePx: Int = 96): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val s = sizePx / 96f

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        drawTieredMinaret(canvas, fillPaint, cx = 11f * s, topY = 8f * s, bottomY = 64f * s, width = 9.5f * s)
        drawTieredMinaret(canvas, fillPaint, cx = 85f * s, topY = 8f * s, bottomY = 64f * s, width = 9.5f * s)
        drawCrescent(canvas, cx = 48f * s, cy = 7f * s, radius = 5.6f * s)

        val domePath = Path().apply {
            moveTo(19f * s, 64f * s)
            lineTo(19f * s, 44f * s)
            cubicTo(19f * s, 24f * s, 35f * s, 18f * s, 48f * s, 11f * s)
            cubicTo(61f * s, 18f * s, 77f * s, 24f * s, 77f * s, 44f * s)
            lineTo(77f * s, 64f * s)
            close()
        }
        canvas.drawPath(domePath, fillPaint)
        canvas.drawRect(8f * s, 60f * s, 88f * s, 65f * s, fillPaint)
        drawArabicSakinaLabel(canvas, s)
        return bmp
    }

    fun renderHollowDomeWithMinutesBitmap(minutes: Int, sizePx: Int = 96): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val s = sizePx / 96f

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4.6f * s
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        // صومعتان معماريتان بارزتان على أقصى الطرفين
        drawTieredMinaret(canvas, fillPaint, cx = 8.5f * s, topY = 8f * s, bottomY = 64f * s, width = 8.4f * s)
        drawTieredMinaret(canvas, fillPaint, cx = 87.5f * s, topY = 8f * s, bottomY = 64f * s, width = 8.4f * s)
        drawCrescent(canvas, cx = 48f * s, cy = 7f * s, radius = 5.4f * s)

        // قبة واسعة مفرّغة تتسع لرقمين كبيرين بوضوح عالٍ (x = 15..81)
        val hollowDome = Path().apply {
            moveTo(15f * s, 63f * s)
            lineTo(15f * s, 43f * s)
            cubicTo(15f * s, 23f * s, 32f * s, 17f * s, 48f * s, 11f * s)
            cubicTo(64f * s, 17f * s, 81f * s, 23f * s, 81f * s, 43f * s)
            lineTo(81f * s, 63f * s)
        }
        canvas.drawPath(hollowDome, strokePaint)

        // أرقام الدقائق المتبقية بخط عريض وواضح جداً وسط القبة
        val label = minutes.toString()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = if (label.length == 1) 46f * s else 39f * s
            letterSpacing = -0.05f
        }
        val fm = textPaint.fontMetrics
        val centerY = 44f * s - (fm.ascent + fm.descent) / 2f
        canvas.drawText(label, 48f * s, centerY, textPaint)

        drawArabicSakinaLabel(canvas, s)
        return bmp
    }

    /**
     * كتابة «سكينة» بخط عربي عريض حقيقي أسفل القبة والصومعتين لتكون مقروءة تماماً في شريط الحالة
     */
    private fun drawArabicSakinaLabel(canvas: Canvas, s: Float) {
        val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = 27f * s
            textScaleX = 1.08f
        }
        val fm = tp.fontMetrics
        val y = 80f * s - (fm.ascent + fm.descent) / 2f
        canvas.drawText("قنوت", 48f * s, y, tp)
    }

    private fun drawCrescent(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val outer = Path().apply { addCircle(cx, cy, radius, Path.Direction.CW) }
        val inner = Path().apply {
            addCircle(cx + radius * 0.38f, cy - radius * 0.18f, radius * 0.80f, Path.Direction.CW)
        }
        outer.op(inner, Path.Op.DIFFERENCE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawPath(outer, paint)
    }

    private fun drawTieredMinaret(
        canvas: Canvas,
        paint: Paint,
        cx: Float,
        topY: Float,
        bottomY: Float,
        width: Float
    ) {
        val half = width / 2f
        val spireH = width * 1.35f
        val balcony1Y = topY + spireH
        val balcony2Y = topY + (bottomY - topY) * 0.56f

        val spire = Path().apply {
            moveTo(cx, topY)
            lineTo(cx + half * 0.95f, balcony1Y)
            lineTo(cx - half * 0.95f, balcony1Y)
            close()
        }
        canvas.drawPath(spire, paint)
        // الشرفة العلوية
        canvas.drawRect(cx - half * 1.25f, balcony1Y, cx + half * 1.25f, balcony1Y + width * 0.34f, paint)
        // العمود العلوي
        canvas.drawRect(cx - half * 0.78f, balcony1Y + width * 0.34f, cx + half * 0.78f, balcony2Y, paint)
        // الشرفة الوسطى
        canvas.drawRect(cx - half * 1.28f, balcony2Y, cx + half * 1.28f, balcony2Y + width * 0.36f, paint)
        // القاعدة السفلية المتينة
        canvas.drawRect(cx - half, balcony2Y + width * 0.36f, cx + half, bottomY, paint)
    }
}
