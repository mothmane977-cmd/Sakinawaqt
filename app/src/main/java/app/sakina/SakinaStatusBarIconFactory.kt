package app.sakina

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat

// -----------------------------------------------------------------------------
// Sakina Status Bar Dynamic Icon Factory ("أيقونة الإشعار الحي الدائمة")
// - remainingMillis >= 3_600_000L (60+ min): Filled Dome + Crescent + 2 Minarets + Kufic "سكينة"
// - remainingMillis <  3_600_000L (0..59 min): Wide Hollow Dome + Crescent +
//   2 Solid Minarets + Large Remaining Minutes + Kufic "سكينة" underneath.
// -----------------------------------------------------------------------------

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

        drawMinaret(canvas, fillPaint, cx = 11f * s, topY = 12f * s, bottomY = 71f * s, width = 6.8f * s)
        drawMinaret(canvas, fillPaint, cx = 85f * s, topY = 12f * s, bottomY = 71f * s, width = 6.8f * s)
        drawCrescent(canvas, cx = 48f * s, cy = 8f * s, radius = 5.4f * s)

        val domePath = Path().apply {
            moveTo(19f * s, 70f * s)
            lineTo(19f * s, 45f * s)
            cubicTo(19f * s, 24f * s, 36f * s, 18f * s, 48f * s, 12f * s)
            cubicTo(60f * s, 18f * s, 77f * s, 24f * s, 77f * s, 45f * s)
            lineTo(77f * s, 70f * s)
            close()
        }
        canvas.drawPath(domePath, fillPaint)
        canvas.drawRect(16f * s, 66f * s, 80f * s, 71f * s, fillPaint)
        drawGeometricKuficSakina(canvas, s)
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
            strokeWidth = 3.5f * s
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        drawMinaret(canvas, fillPaint, cx = 7.5f * s, topY = 12f * s, bottomY = 71f * s, width = 5.6f * s)
        drawMinaret(canvas, fillPaint, cx = 88.5f * s, topY = 12f * s, bottomY = 71f * s, width = 5.6f * s)
        drawCrescent(canvas, cx = 48f * s, cy = 8f * s, radius = 5.4f * s)

        val hollowDome = Path().apply {
            moveTo(13f * s, 69f * s)
            lineTo(13f * s, 44f * s)
            cubicTo(13f * s, 23f * s, 32f * s, 18f * s, 48f * s, 12f * s)
            cubicTo(64f * s, 18f * s, 83f * s, 23f * s, 83f * s, 44f * s)
            lineTo(83f * s, 69f * s)
            close()
        }
        canvas.drawPath(hollowDome, strokePaint)

        val label = minutes.toString()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = if (label.length == 1) 43f * s else 36f * s
            letterSpacing = -0.04f
        }
        val fm = textPaint.fontMetrics
        val centerY = 48f * s - (fm.ascent + fm.descent) / 2f
        canvas.drawText(label, 48f * s, centerY, textPaint)
        drawGeometricKuficSakina(canvas, s)
        return bmp
    }

    private fun drawGeometricKuficSakina(canvas: Canvas, s: Float) {
        val kufiStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3.2f * s
            strokeCap = Paint.Cap.SQUARE
            strokeJoin = Paint.Join.MITER
        }
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val base = 87f * s
        val stem = Path().apply {
            moveTo(77f * s, 79f * s); lineTo(77f * s, base)
            moveTo(70f * s, 79f * s); lineTo(70f * s, base)
            moveTo(63f * s, 79f * s); lineTo(63f * s, base)
            moveTo(77f * s, base); lineTo(34f * s, base)
            moveTo(54f * s, base); lineTo(54f * s, 76f * s); lineTo(60f * s, 76f * s)
            moveTo(44f * s, 80f * s); lineTo(44f * s, base)
            moveTo(34f * s, 80f * s); lineTo(34f * s, base)
        }
        canvas.drawPath(stem, kufiStroke)

        val taa = Path().apply {
            moveTo(28f * s, base)
            lineTo(19f * s, base)
            lineTo(19f * s, 79f * s)
            lineTo(28f * s, 79f * s)
            close()
        }
        canvas.drawPath(taa, kufiStroke)

        val d = 2.6f * s
        canvas.drawRect(45.5f * s, 91f * s, 45.5f * s + d, 91f * s + d, dotPaint)
        canvas.drawRect(41.0f * s, 91f * s, 41.0f * s + d, 91f * s + d, dotPaint)
        canvas.drawRect(32.7f * s, 74f * s, 32.7f * s + d, 74f * s + d, dotPaint)
        canvas.drawRect(24.5f * s, 74f * s, 24.5f * s + d, 74f * s + d, dotPaint)
        canvas.drawRect(20.0f * s, 74f * s, 20.0f * s + d, 74f * s + d, dotPaint)
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

    private fun drawMinaret(
        canvas: Canvas,
        paint: Paint,
        cx: Float,
        topY: Float,
        bottomY: Float,
        width: Float
    ) {
        val half = width / 2f
        val shaftTop = topY + width * 1.15f
        val spire = Path().apply {
            moveTo(cx, topY)
            lineTo(cx + half * 1.1f, shaftTop)
            lineTo(cx - half * 1.1f, shaftTop)
            close()
        }
        canvas.drawPath(spire, paint)
        canvas.drawRect(cx - half * 1.28f, shaftTop, cx + half * 1.28f, shaftTop + width * 0.35f, paint)
        canvas.drawRect(cx - half, shaftTop + width * 0.35f, cx + half, bottomY, paint)
    }
}
