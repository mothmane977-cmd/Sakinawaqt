package app.sakina

import android.graphics.BlurMaskFilter
import android.graphics.Paint as AndroidPaint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Sakina v41 — Master Luminous Luxury Gold & Glass Rendering Engine
 * Combines real studio drawable assets (bg_sakina_luxury_filigree, ic_kaaba_3d_glow,
 * ic_lunar_moon_glow, ic_luxury_corner_gold) with true Android BlurMaskFilter Gaussian
 * bloom and 3-stop brushed 24k metallic gold gradients.
 */
object SakinaLuxuryPalette {
    val ObsidianNavy = Color(0xFF050D1A)
    val MidnightNavy = Color(0xFF0A192F)
    val DeepEmerald = Color(0xFF093A30)
    val GlassSurfaceTop = Color(0xD4122D3F)
    val GlassSurfaceMid = Color(0xCC0A242D)
    val GlassSurfaceBottom = Color(0xDC061322)

    // 24k Brushed Metallic Gold Spectrum (zero flat yellow!)
    val GoldSpecular = Color(0xFFFFF8DC)   // Bright white-gold specular highlight
    val GoldBright = Color(0xFFF6D983)     // Luminous champagne gold
    val GoldRich = Color(0xFFD4AF37)       // Rich 24k gold
    val GoldDeep = Color(0xFF8B6514)       // Deep metallic bronze-gold shadow

    // Exact 24k warm brushed antique champagne-gold stops sampled directly from 22194.jpg & Mihrab filigree
    val MetallicGoldButtonBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF0D79E),
            Color(0xFFD5B576),
            Color(0xFFAE8B4D)
        )
    )
    val MetallicGoldTextBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF2DAA4),
            Color(0xFFD8B87A),
            Color(0xFFB69454)
        )
    )

    // Luminous Emerald Accent
    val EmeraldSpecular = Color(0xFF7CFFD4)
    val EmeraldGlow = Color(0xFF22E3A1)
    val EmeraldSoft = Color(0xFF147A5E)

    // Warm Parchment Spectrum
    val ParchmentCenter = Color(0xFFFFFDF6)
    val ParchmentEdge = Color(0xFFF2E2C1)
    val ParchmentBorder = Color(0xFFD9B664)
}

/**
 * Full-screen luxury deep navy + emerald atmospheric backdrop powered by our real
 * studio-rendered filigree asset (bg_sakina_luxury_filigree) with deep obsidian base.
 */
@Composable
fun SakinaAmbientLuxuryBackdrop(
    modifier: Modifier = Modifier,
    showGeometricPattern: Boolean = true,
    showStarDust: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakinaLuxuryPalette.ObsidianNavy)
    ) {
        if (showGeometricPattern) {
            Image(
                painter = painterResource(id = R.drawable.bg_sakina_luxury_filigree),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.94f,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SakinaLuxuryPalette.ObsidianNavy,
                                SakinaLuxuryPalette.MidnightNavy,
                                SakinaLuxuryPalette.DeepEmerald.copy(alpha = 0.60f),
                                SakinaLuxuryPalette.ObsidianNavy
                            )
                        )
                    )
            )
        }

        if (showStarDust) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val starCoords = listOf(
                    0.14f to 0.14f, 0.82f to 0.16f, 0.22f to 0.34f,
                    0.86f to 0.40f, 0.12f to 0.62f, 0.88f to 0.68f
                )
                starCoords.forEachIndexed { idx, (fx, fy) ->
                    val pos = Offset(w * fx, h * fy)
                    val r = if (idx % 2 == 0) 2.0.dp.toPx() else 1.2.dp.toPx()
                    drawGlowingLuxuryOrb(
                        center = pos,
                        coreRadiusPx = r * 0.6f,
                        coreColor = SakinaLuxuryPalette.GoldSpecular,
                        haloColor = SakinaLuxuryPalette.GoldBright,
                        haloScale = 3.5f,
                        intensity = 0.62f
                    )
                }
            }
        }
    }
}

/**
 * Renders our real studio-cut transparent 24k metallic gold arabesque corner filigree
 * (R.drawable.ic_luxury_corner_gold) cleanly in all four corners of any Box card.
 */
@Composable
fun BoxScope.SakinaLuxuryCornerOverlay(
    cornerSize: Dp = 26.dp,
    alpha: Float = 0.88f
) {
    val painter = painterResource(id = R.drawable.ic_luxury_corner_gold)
    Image(
        painter = painter,
        contentDescription = null,
        alpha = alpha,
        modifier = Modifier
            .size(cornerSize)
            .align(AbsoluteAlignment.TopLeft)
            .padding(3.dp)
    )
    Image(
        painter = painter,
        contentDescription = null,
        alpha = alpha,
        modifier = Modifier
            .size(cornerSize)
            .align(AbsoluteAlignment.TopRight)
            .padding(3.dp)
            .graphicsLayer { scaleX = -1f }
    )
    Image(
        painter = painter,
        contentDescription = null,
        alpha = alpha,
        modifier = Modifier
            .size(cornerSize)
            .align(AbsoluteAlignment.BottomLeft)
            .padding(3.dp)
            .graphicsLayer { scaleY = -1f }
    )
    Image(
        painter = painter,
        contentDescription = null,
        alpha = alpha,
        modifier = Modifier
            .size(cornerSize)
            .align(AbsoluteAlignment.BottomRight)
            .padding(3.dp)
            .graphicsLayer { scaleX = -1f; scaleY = -1f }
    )
}

/**
 * Luxury dark glassmorphic card modifier with:
 * - True BlurMaskFilter Gaussian outer glow halo
 * - Deep translucent navy-emerald glass gradient fill + top crystal sheen
 * - Metallic 24k gold gradient border + optional recessed inner golden hairline
 * - Bright top-center & bottom-center specular light flares (as in 21486/21489)
 */
fun Modifier.sakinaLuxuryGlassCard(
    cornerRadius: Dp = 20.dp,
    innerHairline: Boolean = false,
    emeraldFlare: Boolean = false,
    shimmer: Boolean = false,
    borderAlpha: Float = 0.78f
): Modifier = composed {
    val shimmerPhase = if (shimmer) {
        val transition = rememberInfiniteTransition(label = "luxuryGoldShimmer")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerPhase"
        )
        phase
    } else {
        0.5f
    }

    this.drawBehind {
        val cr = cornerRadius.toPx()
        val w = size.width
        val h = size.height
        val flareColor = if (emeraldFlare) SakinaLuxuryPalette.EmeraldGlow else SakinaLuxuryPalette.GoldBright

        // 1. True Gaussian BlurMaskFilter ambient border bloom
        drawIntoCanvas { canvas ->
            val glowPaint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.STROKE
                strokeWidth = 3.5.dp.toPx()
                color = flareColor.copy(alpha = 0.36f).toArgb()
                maskFilter = BlurMaskFilter(11.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawRoundRect(0f, 0f, w, h, cr, cr, glowPaint)
        }

        // 2. Translucent deep navy-emerald glass fill
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    SakinaLuxuryPalette.GlassSurfaceTop,
                    SakinaLuxuryPalette.GlassSurfaceMid,
                    SakinaLuxuryPalette.GlassSurfaceBottom
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            cornerRadius = CornerRadius(cr, cr)
        )

        // 3. Subtle top crystal specular sheen
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.09f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = h * 0.42f
            ),
            cornerRadius = CornerRadius(cr, cr)
        )

        // 4. Outer metallic 24k gold border (with optional animated specular glint)
        val glintX = w * shimmerPhase
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    SakinaLuxuryPalette.GoldDeep.copy(alpha = borderAlpha * 0.72f),
                    SakinaLuxuryPalette.GoldRich.copy(alpha = borderAlpha),
                    SakinaLuxuryPalette.GoldSpecular.copy(alpha = min(1f, borderAlpha + 0.22f)),
                    SakinaLuxuryPalette.GoldRich.copy(alpha = borderAlpha),
                    SakinaLuxuryPalette.GoldDeep.copy(alpha = borderAlpha * 0.72f)
                ),
                start = Offset(glintX - w * 0.5f, 0f),
                end = Offset(glintX + w * 0.5f, h)
            ),
            cornerRadius = CornerRadius(cr, cr),
            style = Stroke(width = 1.25.dp.toPx())
        )

        // 5. Optional recessed inner thin golden hairline frame
        if (innerHairline && w > 24.dp.toPx() && h > 24.dp.toPx()) {
            val inset = 4.5.dp.toPx()
            val innerCr = (cr - inset).coerceAtLeast(4.dp.toPx())
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        SakinaLuxuryPalette.GoldBright.copy(alpha = 0.42f),
                        SakinaLuxuryPalette.GoldDeep.copy(alpha = 0.20f),
                        SakinaLuxuryPalette.GoldBright.copy(alpha = 0.42f)
                    )
                ),
                topLeft = Offset(inset, inset),
                size = Size(w - inset * 2f, h - inset * 2f),
                cornerRadius = CornerRadius(innerCr, innerCr),
                style = Stroke(width = 0.7.dp.toPx())
            )
        }

        // 6. Crisp pinpoint diamond specular glint (never bleeds across stacked cards or small circles)
        if (w >= 72.dp.toPx()) {
            val glintR = 11.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.72f),
                        flareColor.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, 0f),
                    radius = glintR
                ),
                radius = glintR,
                center = Offset(w * 0.5f, 0f)
            )
        }
    }
}

/**
 * Warm golden Gaussian outer halo + ornamental double metallic gold frame for
 * Warm Cream parchment cards (Spotlight, Hadith, Surah List, Mushaf page).
 */
fun Modifier.sakinaParchmentGlowCard(
    cornerRadius: Dp = 24.dp,
    haloAlpha: Float = 0.48f,
    doubleBorder: Boolean = true
): Modifier = this.drawBehind {
    val cr = cornerRadius.toPx()
    val w = size.width
    val h = size.height

    // 1. True Gaussian golden outer halo radiating onto the dark navy backdrop
    drawIntoCanvas { canvas ->
        val haloPaint = AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.FILL
            color = SakinaLuxuryPalette.GoldBright.copy(alpha = haloAlpha).toArgb()
            maskFilter = BlurMaskFilter(18.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawRoundRect(0f, 0f, w, h, cr, cr, haloPaint)
    }

    // 2. Warm luminous parchment fill
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(
                SakinaLuxuryPalette.ParchmentCenter,
                SakinaLuxuryPalette.ParchmentEdge
            ),
            center = Offset(w * 0.5f, h * 0.42f),
            radius = maxOf(w, h) * 0.82f
        ),
        cornerRadius = CornerRadius(cr, cr)
    )

    // 3. Outer 24k metallic gold border
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                SakinaLuxuryPalette.GoldBright,
                SakinaLuxuryPalette.GoldSpecular,
                SakinaLuxuryPalette.GoldRich,
                SakinaLuxuryPalette.GoldBright
            )
        ),
        cornerRadius = CornerRadius(cr, cr),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // 4. Optional ornamental inner hairline border
    if (doubleBorder && w > 28.dp.toPx() && h > 28.dp.toPx()) {
        val inset = 5.dp.toPx()
        val innerCr = (cr - inset).coerceAtLeast(6.dp.toPx())
        drawRoundRect(
            color = SakinaLuxuryPalette.GoldRich.copy(alpha = 0.48f),
            topLeft = Offset(inset, inset),
            size = Size(w - inset * 2f, h - inset * 2f),
            cornerRadius = CornerRadius(innerCr, innerCr),
            style = Stroke(width = 0.8.dp.toPx())
        )
    }
}

/**
 * Procedural golden corner filigree fallback for non-Box cards.
 */
fun Modifier.sakinaCornerFiligree(
    goldColor: Color = SakinaLuxuryPalette.GoldBright,
    alpha: Float = 0.62f
): Modifier = this.drawWithContent {
    drawContent()
    val w = size.width
    val h = size.height
    val arm = min(w, h) * 0.25f
    val pad = 6.dp.toPx()
    val c = goldColor.copy(alpha = alpha)
    val stroke = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round)

    fun drawCorner(ox: Float, oy: Float, dx: Float, dy: Float) {
        val p = Path().apply {
            moveTo(ox + dx * arm, oy)
            quadraticBezierTo(ox, oy, ox, oy + dy * arm)
        }
        drawPath(p, color = c, style = stroke)
        val p2 = Path().apply {
            moveTo(ox + dx * (arm * 0.68f), oy + dy * 3.dp.toPx())
            quadraticBezierTo(
                ox + dx * 3.dp.toPx(),
                oy + dy * 3.dp.toPx(),
                ox + dx * 3.dp.toPx(),
                oy + dy * (arm * 0.68f)
            )
        }
        drawPath(p2, color = c.copy(alpha = alpha * 0.68f), style = Stroke(width = 0.7.dp.toPx()))
        val dCenter = Offset(ox + dx * 5.dp.toPx(), oy + dy * 5.dp.toPx())
        val dR = 2.2.dp.toPx()
        val dpPath = Path().apply {
            moveTo(dCenter.x, dCenter.y - dR)
            lineTo(dCenter.x + dR, dCenter.y)
            lineTo(dCenter.x, dCenter.y + dR)
            lineTo(dCenter.x - dR, dCenter.y)
            close()
        }
        drawPath(dpPath, color = SakinaLuxuryPalette.GoldSpecular.copy(alpha = alpha * 0.90f))
    }

    drawCorner(pad, pad, 1f, 1f)
    drawCorner(w - pad, pad, -1f, 1f)
    drawCorner(pad, h - pad, 1f, -1f)
    drawCorner(w - pad, h - pad, -1f, -1f)
}

/**
 * True Gaussian-bloom glowing 24k gold oval/ellipse stroke (for Orbital Home, Tasbih, Qibla).
 */
fun DrawScope.drawGlowingGoldOval(
    topLeft: Offset,
    size: Size,
    coreStrokePx: Float,
    glowColor: Color = SakinaLuxuryPalette.GoldRich,
    specularColor: Color = SakinaLuxuryPalette.GoldSpecular,
    alphaScale: Float = 1f
) {
    // Pass 1: Real BlurMaskFilter Gaussian Golden Bloom
    drawIntoCanvas { canvas ->
        val bloomPaint = AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.STROKE
            strokeWidth = coreStrokePx * 2.6f
            color = glowColor.copy(alpha = 0.62f * alphaScale).toArgb()
            maskFilter = BlurMaskFilter(12.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawOval(
            topLeft.x,
            topLeft.y,
            topLeft.x + size.width,
            topLeft.y + size.height,
            bloomPaint
        )
    }
    // Pass 2: Metallic 24k gradient core
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(
                SakinaLuxuryPalette.GoldDeep.copy(alpha = 0.90f * alphaScale),
                SakinaLuxuryPalette.GoldBright.copy(alpha = 0.98f * alphaScale),
                specularColor.copy(alpha = 1f * alphaScale),
                SakinaLuxuryPalette.GoldRich.copy(alpha = 0.96f * alphaScale),
                SakinaLuxuryPalette.GoldBright.copy(alpha = 0.92f * alphaScale)
            ),
            start = topLeft,
            end = Offset(topLeft.x + size.width, topLeft.y + size.height)
        ),
        topLeft = topLeft,
        size = size,
        style = Stroke(width = coreStrokePx)
    )
    // Pass 3: Razor-sharp bright champagne specular highlight
    drawOval(
        color = specularColor.copy(alpha = 0.88f * alphaScale),
        topLeft = topLeft,
        size = size,
        style = Stroke(width = (coreStrokePx * 0.36f).coerceAtLeast(1.2f))
    )
}

/**
 * True Gaussian-bloom glowing line (for Qibla laser beam & horizon arc).
 */
fun DrawScope.drawGlowingGoldLine(
    start: Offset,
    end: Offset,
    coreStrokePx: Float,
    glowColor: Color = SakinaLuxuryPalette.GoldBright
) {
    drawIntoCanvas { canvas ->
        val bloomPaint = AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.STROKE
            strokeCap = AndroidPaint.Cap.ROUND
            strokeWidth = coreStrokePx * 3.0f
            color = glowColor.copy(alpha = 0.65f).toArgb()
            maskFilter = BlurMaskFilter(10.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawLine(start.x, start.y, end.x, end.y, bloomPaint)
    }
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(glowColor, SakinaLuxuryPalette.GoldSpecular, glowColor),
            start = start,
            end = end
        ),
        start = start,
        end = end,
        strokeWidth = coreStrokePx,
        cap = StrokeCap.Round
    )
}

/**
 * Multi-pass luminous glowing orb (prayer planets, Tasbih beads, timeline nodes).
 */
fun DrawScope.drawGlowingLuxuryOrb(
    center: Offset,
    coreRadiusPx: Float,
    coreColor: Color = SakinaLuxuryPalette.GoldSpecular,
    haloColor: Color = SakinaLuxuryPalette.GoldBright,
    haloScale: Float = 3.2f,
    intensity: Float = 1f
) {
    val haloR = coreRadiusPx * haloScale
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                coreColor.copy(alpha = 0.72f * intensity),
                haloColor.copy(alpha = 0.38f * intensity),
                haloColor.copy(alpha = 0.12f * intensity),
                Color.Transparent
            ),
            center = center,
            radius = haloR
        ),
        radius = haloR,
        center = center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.96f * intensity),
                coreColor,
                haloColor
            ),
            center = Offset(center.x - coreRadiusPx * 0.25f, center.y - coreRadiusPx * 0.25f),
            radius = coreRadiusPx * 1.25f
        ),
        radius = coreRadiusPx,
        center = center
    )
}

/**
 * Fallback vector Kaaba helper (kept for binary compatibility with older callers).
 */
fun DrawScope.drawLuxuryKaabaEmblem(
    center: Offset,
    sizePx: Float,
    withLightPillar: Boolean = false,
    pillarHeightPx: Float = sizePx * 3.5f,
    glowColor: Color = SakinaLuxuryPalette.GoldBright
) {
    val halfW = sizePx * 0.46f
    val halfH = sizePx * 0.44f
    if (withLightPillar) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    glowColor.copy(alpha = 0.20f),
                    SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.58f)
                ),
                startY = center.y - pillarHeightPx,
                endY = center.y
            ),
            topLeft = Offset(center.x - halfW * 0.65f, center.y - pillarHeightPx),
            size = Size(halfW * 1.3f, pillarHeightPx)
        )
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.42f),
                glowColor.copy(alpha = 0.18f),
                Color.Transparent
            ),
            center = center,
            radius = sizePx * 1.25f
        ),
        radius = sizePx * 1.25f,
        center = center
    )
}
