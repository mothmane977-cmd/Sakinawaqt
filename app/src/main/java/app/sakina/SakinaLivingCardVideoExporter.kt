package app.sakina

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.*

/**
 * Sakina Al-Qalb ("سكينة القلب") — Offline HD PNG & Living MP4 Exporter
 * - Renders 1080x1420 HD PNG via android.graphics.Canvas (100% Compose 1.6 compatible, zero GraphicsLayer dependency).
 * - Renders 720x1280 24fps H.264 MP4 via MediaCodec + MediaMuxer with breathing Islamic sacred geometry & golden waveform.
 * - Saves directly to Android Gallery via MediaStore (zero FileProvider / Manifest edits required on Android 10+).
 */
object SakinaLivingCardVideoExporter {

    fun drawCardFrame(
        canvas: Canvas,
        w: Int,
        h: Int,
        verse: HeartCardVerse,
        phase: PrayerLightPhase,
        hijriDateLabel: String,
        showBilingualMirror: Boolean,
        attachAudioPulse: Boolean,
        breathScale: Float = 1f,
        wavePhase: Float = 0f
    ) {
        val wf = w.toFloat()
        val hf = h.toFloat()

        // 1. Deep luxury backdrop gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                wf * 0.5f, hf * 0.36f, hf * 0.72f,
                intArrayOf(
                    phase.primaryGlowArgb,
                    0xFF0A192F.toInt(),
                    0xFF06101F.toInt()
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, wf, hf, bgPaint)

        // 2. Subtle ambient corner emerald/gold glow
        val cornerGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                wf * 0.5f, hf * 0.5f, wf * 0.65f,
                intArrayOf(0x2AD4AF37, 0x161EE3A8, 0x00000000),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(wf * 0.5f, hf * 0.5f, wf * 0.65f, cornerGlow)

        // 3. Outer beveled metallic 24k gold border + inner recessed hairline
        val pad = wf * 0.045f
        val cornerR = wf * 0.055f
        val outerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = wf * 0.0055f
            shader = LinearGradient(
                pad, pad, wf - pad, hf - pad,
                intArrayOf(0xFF8B6514.toInt(), 0xFFD4AF37.toInt(), 0xFFFFF8DC.toInt(), 0xFFD4AF37.toInt(), 0xFF8B6514.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(RectF(pad, pad, wf - pad, hf - pad), cornerR, cornerR, outerBorder)

        val innerPad = pad + wf * 0.018f
        val innerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = wf * 0.0022f
            color = 0x55F3DFA2
        }
        canvas.drawRoundRect(RectF(innerPad, innerPad, wf - innerPad, hf - innerPad), cornerR * 0.85f, cornerR * 0.85f, innerBorder)

        // 4. Deterministic Generative Islamic Sacred Geometry (breathing scale)
        canvas.save()
        val cx = wf * 0.5f
        val cy = hf * 0.46f
        canvas.scale(breathScale, breathScale, cx, cy)
        val seed = verse.surahNumber * 1000 + verse.ayahNumber + verse.mood.seedOffset
        val baseRadius = min(wf, hf) * 0.36f
        val folds = 8 + (seed % 3) * 4

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = wf * 0.0024f
        }
        for (ring in 1..3) {
            ringPaint.color = Color.argb((42 / ring).coerceIn(10, 48), 212, 175, 55)
            canvas.drawCircle(cx, cy, baseRadius * (0.48f + ring * 0.18f), ringPaint)
        }

        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = wf * 0.0022f
            color = 0x38F3DFA2
        }
        val innerR = baseRadius * (0.42f + (seed % 5) * 0.04f)
        val outerR = baseRadius * 0.96f
        val angleStep = 360f / folds
        for (i in 0 until folds) {
            canvas.save()
            canvas.rotate(i * angleStep + (seed % 15), cx, cy)
            val p = Path().apply {
                moveTo(cx, cy - outerR)
                lineTo(cx + innerR * 0.28f, cy - innerR)
                lineTo(cx, cy - innerR * 0.55f)
                lineTo(cx - innerR * 0.28f, cy - innerR)
                close()
            }
            canvas.drawPath(p, starPaint)
            canvas.restore()
        }
        canvas.restore()

        // 5. Top mood & light phase header
        val topY = innerPad + hf * 0.055f
        val tagPaintRight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xDDF3DFA2.toInt()
            textSize = wf * 0.031f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val tagPaintLeft = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xAAFFFFFF.toInt()
            textSize = wf * 0.028f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        canvas.drawText("✦ ${verse.mood.labelAr}", wf - innerPad - wf * 0.04f, topY, tagPaintRight)
        canvas.drawText(phase.labelAr, innerPad + wf * 0.04f, topY, tagPaintLeft)

        // 6. Main Sacred Arabic Calligraphy (multiline StaticLayout centered)
        val textBoxW = (wf * 0.78f).toInt()
        val versePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = wf * 0.068f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            shader = LinearGradient(
                0f, hf * 0.30f, 0f, hf * 0.55f,
                intArrayOf(0xFFFFFAE6.toInt(), 0xFFF3DFA2.toInt(), 0xFFD4AF37.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
        }
        val verseLayout = buildStaticLayout(verse.arabicText, versePaint, textBoxW, 1.38f)

        val transPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xCCFFFFFF.toInt()
            textSize = wf * 0.031f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        }
        val transLayout = if (showBilingualMirror) buildStaticLayout(verse.translationEn, transPaint, textBoxW, 1.3f) else null

        val notePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xD0FAF6EC.toInt()
            textSize = wf * 0.032f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val noteLayout = buildStaticLayout(verse.reflectionNote, notePaint, textBoxW, 1.35f)

        val gap = hf * 0.024f
        val totalTextH = verseLayout.height + (transLayout?.let { it.height + gap } ?: 0f) + gap + noteLayout.height
        var drawY = (hf * 0.46f - totalTextH * 0.5f).coerceAtLeast(topY + hf * 0.05f)
        val leftX = (wf - textBoxW) * 0.5f

        canvas.save()
        canvas.translate(leftX, drawY)
        verseLayout.draw(canvas)
        canvas.restore()
        drawY += verseLayout.height + gap

        if (transLayout != null) {
            canvas.save()
            canvas.translate(leftX, drawY)
            transLayout.draw(canvas)
            canvas.restore()
            drawY += transLayout.height + gap
        }

        canvas.save()
        canvas.translate(leftX, drawY)
        noteLayout.draw(canvas)
        canvas.restore()

        // 7. Bottom golden acoustic waveform pulse
        if (attachAudioPulse) {
            val waveW = wf * 0.56f
            val waveLeft = (wf - waveW) * 0.5f
            val waveMidY = hf - innerPad - hf * 0.125f
            val waveMaxH = hf * 0.042f
            val bars = 29
            val step = waveW / bars
            val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xCCD4AF37.toInt()
                strokeWidth = wf * 0.0055f
                strokeCap = Paint.Cap.ROUND
            }
            for (i in 0 until bars) {
                val distCenter = 1f - abs(i - bars / 2f) / (bars / 2f)
                val amp = (0.25f + 0.75f * distCenter) * (0.45f + 0.55f * abs(sin(wavePhase + i * 0.52f)))
                val halfH = waveMaxH * amp * 0.5f
                val bx = waveLeft + i * step + step * 0.5f
                canvas.drawLine(bx, waveMidY - halfH, bx, waveMidY + halfH, wavePaint)
            }
        }

        // 8. Footer: Surah/Ayah citation + Sakina Hijri stamp
        val footY = hf - innerPad - hf * 0.042f
        val footRight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD4AF37.toInt()
            textSize = wf * 0.029f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val footLeft = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x99FFFFFF.toInt()
            textSize = wf * 0.025f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        canvas.drawText(verse.sourceCitation, wf - innerPad - wf * 0.04f, footY, footRight)
        canvas.drawText("سَكِينة · $hijriDateLabel", innerPad + wf * 0.04f, footY, footLeft)
    }

    private fun buildStaticLayout(text: String, paint: TextPaint, width: Int, spacingMult: Float): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, spacingMult)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, width, Layout.Alignment.ALIGN_CENTER, spacingMult, 0f, false)
        }
    }

    /**
     * Export HD 1080x1420 PNG directly to Gallery (MediaStore on Android 10+) and open Share sheet.
     */
    suspend fun exportAndShareHdPng(
        context: Context,
        verse: HeartCardVerse,
        phase: PrayerLightPhase,
        hijriDateLabel: String,
        showBilingualMirror: Boolean,
        attachAudioPulse: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val bmp = Bitmap.createBitmap(1080, 1420, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawCardFrame(
                canvas = canvas,
                w = 1080,
                h = 1420,
                verse = verse,
                phase = phase,
                hijriDateLabel = hijriDateLabel,
                showBilingualMirror = showBilingualMirror,
                attachAudioPulse = attachAudioPulse,
                breathScale = 1.0f,
                wavePhase = 0.8f
            )
            val fileName = "sakina_heart_${System.currentTimeMillis()}.png"
            val shareUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Sakina")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                }
                uri
            } else {
                val dir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
                val file = File(dir, fileName)
                FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
                runCatching { FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file) }.getOrNull()
            }
            bmp.recycle()

            if (shareUri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    putExtra(Intent.EXTRA_TEXT, "${verse.sourceCitation} — سَكِينة")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                withContext(Dispatchers.Main) {
                    context.startActivity(Intent.createChooser(intent, "مشاركة بطاقة سكينة القلب"))
                }
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }

    /**
     * Export Living 8-second 720x1280 24fps H.264 MP4 via MediaCodec + MediaMuxer
     * directly to Gallery (Movies/Sakina) and open Share sheet.
     */
    suspend fun exportAndShareLivingMp4(
        context: Context,
        verse: HeartCardVerse,
        phase: PrayerLightPhase,
        hijriDateLabel: String,
        showBilingualMirror: Boolean,
        attachAudioPulse: Boolean,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val width = 720
        val height = 1280
        val fps = 24
        val durationSec = 8
        val totalFrames = fps * durationSec
        val tempFile = File(context.cacheDir, "sakina_living_${System.currentTimeMillis()}.mp4")

        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var muxerStarted = false

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, 3_200_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = codec.createInputSurface()
            codec.start()

            muxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val bufferInfo = MediaCodec.BufferInfo()
            var trackIndex = -1

            fun drainEncoder(endOfStream: Boolean) {
                if (endOfStream) codec?.signalEndOfInputStream()
                while (true) {
                    val outIndex = codec?.dequeueOutputBuffer(bufferInfo, 10_000) ?: break
                    when {
                        outIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                            if (!endOfStream) break
                        }
                        outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            if (!muxerStarted) {
                                trackIndex = muxer!!.addTrack(codec!!.outputFormat)
                                muxer!!.start()
                                muxerStarted = true
                            }
                        }
                        outIndex >= 0 -> {
                            val encodedData = codec!!.getOutputBuffer(outIndex)
                            if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                                    encodedData.position(bufferInfo.offset)
                                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                                    muxer!!.writeSampleData(trackIndex, encodedData, bufferInfo)
                                }
                            }
                            codec!!.releaseOutputBuffer(outIndex, false)
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
                        }
                    }
                }
            }

            for (frame in 0 until totalFrames) {
                val t = frame.toFloat() / fps.toFloat()
                val breath = 0.97f + 0.03f * sin(t * (2.0 * PI / 4.0)).toFloat()
                val wave = t * (2.0 * PI / 2.2).toFloat()

                val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    inputSurface.lockHardwareCanvas()
                } else {
                    inputSurface.lockCanvas(null)
                }
                try {
                    drawCardFrame(
                        canvas = canvas,
                        w = width,
                        h = height,
                        verse = verse,
                        phase = phase,
                        hijriDateLabel = hijriDateLabel,
                        showBilingualMirror = showBilingualMirror,
                        attachAudioPulse = attachAudioPulse,
                        breathScale = breath,
                        wavePhase = wave
                    )
                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }
                drainEncoder(false)
                if (frame % 6 == 0 || frame == totalFrames - 1) {
                    withContext(Dispatchers.Main) {
                        onProgress((frame + 1).toFloat() / totalFrames.toFloat())
                    }
                }
            }
            drainEncoder(true)
            inputSurface.release()

            codec.stop()
            codec.release()
            codec = null
            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()
            muxer = null

            val fileName = "sakina_heart_${System.currentTimeMillis()}.mp4"
            val shareUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Sakina")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(tempFile).use { input -> input.copyTo(out) }
                    }
                    values.clear()
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                }
                tempFile.delete()
                uri
            } else {
                runCatching { FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile) }.getOrNull()
            }

            if (shareUri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/mp4"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    putExtra(Intent.EXTRA_TEXT, "${verse.sourceCitation} — سَكِينة")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                withContext(Dispatchers.Main) {
                    context.startActivity(Intent.createChooser(intent, "مشاركة الفيديو الحيّ — سكينة القلب"))
                }
                true
            } else {
                false
            }
        } catch (_: Exception) {
            tempFile.delete()
            false
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            runCatching { if (muxerStarted) muxer?.stop() }
            runCatching { muxer?.release() }
        }
    }
}
