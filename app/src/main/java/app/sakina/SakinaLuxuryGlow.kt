package app.sakina

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Sakina v40 — Luminous Luxury Gold & Glass Rendering Engine
 * Pure Jetpack Compose Canvas + Multi-Pass Radial Bloom (60fps, zero bitmap bloat,
 * compatible with Android 8–15 / Compose 1.6.x without hardware-blur artifacts).
 */
object SakinaLuxuryPalette {
    val ObsidianNavy = Color(0xFF050D1A)
    val MidnightNavy = Color(0xFF0A192F)
    val DeepEmerald = Color(0xFF093A30)
    val GlassSurfaceTop = Color(0xCC112A3A)
    val GlassSurfaceMid = Color(0xC80B242B)
    val GlassSurfaceBottom = Color(0xD6071525)

    // Metallic Gold Spectrum
    val GoldSpecular = Color(0xFFFFF8DC)   // Bright white-gold specular core
    val GoldBright = Color(0xFFF6D983)     // Luminous champagne gold
    val GoldRich = Color(0xFFD4AF37)       // Classic brushed 24k gold
    val GoldDeep = Color(0xFF8B6514)       // Deep bronze-gold shadow

    // Luminous Emerald Accent
    val EmeraldSpecular = Color(0xFF7CFFD4)
    val EmeraldGlow = Color(0xFF22E3A1)
    val EmeraldSoft = Color(0xFF147A5E)

    // Warm Parchment Spectrum
    val ParchmentCenter = Color(0xFFFFFDF6)
    val ParchmentEdge = Color(0xFFF3E4C6)
    val ParchmentBorder = Color(0xFFD9B664)
}

/**
 * Full-screen luxury deep navy + emerald atmospheric backdrop with subtle
 * procedural Islamic 8-point geometric star lattice and starlight dust.
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SakinaLuxuryPalette.ObsidianNavy,
                        SakinaLuxuryPalette.MidnightNavy,
                        SakinaLuxuryPalette.DeepEmerald.copy(alpha = 0.62f),
                        SakinaLuxuryPalette.ObsidianNavy
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft ambient emerald-teal & warm gold atmospheric light pools
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SakinaLuxuryPalette.EmeraldSoft.copy(alpha = 0.24f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.85f, h * 0.16f),
                    radius = w * 0.72f
                ),
                radius = w * 0.72f,
                center = Offset(w * 0.85f, h * 0.16f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SakinaLuxuryPalette.GoldRich.copy(alpha = 0.11f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.50f, h * 0.42f),
                    radius = w * 0.68f
                ),
                radius = w * 0.68f,
                center = Offset(w * 0.50f, h * 0.42f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SakinaLuxuryPalette.EmeraldGlow.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.20f, h * 0.82f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.20f, h * 0.82f)
            )

            // Procedural Islamic 8-point star filigree lattice (ultra-delicate gold)
            if (showGeometricPattern) {
                val cell = 64.dp.toPx()
                val cols = (w / cell).toInt() + 2
                val rows = (h / cell).toInt() + 2
                val stroke = Stroke(width = 0.65.dp.toPx())
                val patternColor = SakinaLuxuryPalette.GoldBright.copy(alpha = 0.055f)
                for (r in 0..rows) {
                    for (c in 0..cols) {
                        val cx = c * cell
                        val cy = r * cell
                        drawEightPointStar(
                            center = Offset(cx, cy),
                            outerR = cell * 0.34f,
                            innerR = cell * 0.15f,
                            color = patternColor,
                            stroke = stroke
                        )
                    }
                }
            }

            // Delicate golden starlight dust particles
            if (showStarDust) {
                val starCoords = listOf(
                    0.12f to 0.10f, 0.28f to 0.18f, 0.76f to 0.12f, 0.89f to 0.22f,
                    0.18f to 0.36f, 0.84f to 0.38f, 0.08f to 0.54f, 0.92f to 0.58f,
                    0.24f to 0.72f, 0.74f to 0.68f, 0.42f to 0.14f, 0.61f to 0.26f,
                    0.15f to 0.84f, 0.86f to 0.86f, 0.52f to 0.08f, 0.35f to 0.50f
                )
                starCoords.forEachIndexed { idx, (fx, fy) ->
                    val pos = Offset(w * fx, h * fy)
                    val r = if (idx % 3 == 0) 2.2.dp.toPx() else 1.2.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.55f),
                                SakinaLuxuryPalette.GoldBright.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = pos,
                            radius = r * 3.8f
                        ),
                        radius = r * 3.8f,
                        center = pos
                    )
                    drawCircle(
                        color = SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.78f),
                        radius = r * 0.55f,
                        center = pos
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawEightPointStar(
    center: Offset,
    outerR: Float,
    innerR: Float,
    color: Color,
    stroke: Stroke
) {
    val path = Path()
    for (i in 0 until 16) {
        val angle = (i * PI / 8.0)
        val radius = if (i % 2 == 0) outerR else innerR
        val x = center.x + (cos(angle) * radius).toFloat()
        val y = center.y + (sin(angle) * radius).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = color, style = stroke)
}

/**
 * Luxury dark glassmorphic card modifier with:
 * - Deep navy-emerald translucent gradient fill
 * - Metallic gold gradient border
 * - Optional recessed inner golden hairline frame (as in Control Room Settings)
 * - Signature bright top-center & bottom-center specular light flares
 * - Optional gentle golden shimmer animation along the rim
 */
fun Modifier.sakinaLuxuryGlassCard(
    cornerRadius: Dp = 20.dp,
    innerHairline: Boolean = false,
    emeraldFlare: Boolean = false,
    shimmer: Boolean = false,
    borderAlpha: Float = 0.68f
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

        // 1. Soft outer ambient glow
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    flareColor.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = maxOf(w, h) * 0.68f
            ),
            topLeft = Offset(-6.dp.toPx(), -6.dp.toPx()),
            size = Size(w + 12.dp.toPx(), h + 12.dp.toPx()),
            cornerRadius = CornerRadius(cr + 6.dp.toPx(), cr + 6.dp.toPx())
        )

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

        // 3. Subtle top glass specular sheen
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = h * 0.42f
            ),
            cornerRadius = CornerRadius(cr, cr)
        )

        // 4. Outer metallic gold border (with optional animated specular glint)
        val glintX = w * shimmerPhase
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    SakinaLuxuryPalette.GoldDeep.copy(alpha = borderAlpha * 0.65f),
                    SakinaLuxuryPalette.GoldRich.copy(alpha = borderAlpha),
                    SakinaLuxuryPalette.GoldSpecular.copy(alpha = min(1f, borderAlpha + 0.25f)),
                    SakinaLuxuryPalette.GoldRich.copy(alpha = borderAlpha),
                    SakinaLuxuryPalette.GoldDeep.copy(alpha = borderAlpha * 0.65f)
                ),
                start = Offset(glintX - w * 0.5f, 0f),
                end = Offset(glintX + w * 0.5f, h)
            ),
            cornerRadius = CornerRadius(cr, cr),
            style = Stroke(width = 1.15.dp.toPx())
        )

        // 5. Optional recessed inner thin golden hairline frame
        if (innerHairline && w > 24.dp.toPx() && h > 24.dp.toPx()) {
            val inset = 4.5.dp.toPx()
            val innerCr = (cr - inset).coerceAtLeast(4.dp.toPx())
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        SakinaLuxuryPalette.GoldBright.copy(alpha = 0.34f),
                        SakinaLuxuryPalette.GoldDeep.copy(alpha = 0.16f),
                        SakinaLuxuryPalette.GoldBright.copy(alpha = 0.34f)
                    )
                ),
                topLeft = Offset(inset, inset),
                size = Size(w - inset * 2f, h - inset * 2f),
                cornerRadius = CornerRadius(innerCr, innerCr),
                style = Stroke(width = 0.65.dp.toPx())
            )
        }

        // 6. Signature top-center & bottom-center luminous specular flares
        val flareRadius = (w * 0.30f).coerceIn(28.dp.toPx(), 96.dp.toPx())
        // Top flare
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.68f),
                    flareColor.copy(alpha = 0.30f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, 0f),
                radius = flareRadius
            ),
            topLeft = Offset(w * 0.5f - flareRadius, -6.dp.toPx()),
            size = Size(flareRadius * 2f, 12.dp.toPx())
        )
        // Bottom flare
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.56f),
                    flareColor.copy(alpha = 0.24f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h),
                radius = flareRadius * 0.85f
            ),
            topLeft = Offset(w * 0.5f - flareRadius * 0.85f, h - 5.dp.toPx()),
            size = Size(flareRadius * 1.7f, 10.dp.toPx())
        )
    }
}

/**
 * Warm golden outer halo + ornamental double golden hairline frame for
 * Warm Cream parchment cards (Spotlight, Hadith, Surah List, Mushaf page).
 */
fun Modifier.sakinaParchmentGlowCard(
    cornerRadius: Dp = 24.dp,
    haloAlpha: Float = 0.34f,
    doubleBorder: Boolean = true
): Modifier = this.drawBehind {
    val cr = cornerRadius.toPx()
    val w = size.width
    val h = size.height

    // Outer warm golden halo bloom
    val spread = 14.dp.toPx()
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(
                SakinaLuxuryPalette.GoldBright.copy(alpha = haloAlpha),
                SakinaLuxuryPalette.GoldRich.copy(alpha = haloAlpha * 0.42f),
                Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = maxOf(w, h) * 0.65f
        ),
        topLeft = Offset(-spread, -spread),
        size = Size(w + spread * 2f, h + spread * 2f),
        cornerRadius = CornerRadius(cr + spread, cr + spread)
    )

    // Warm luminous parchment fill
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(
                SakinaLuxuryPalette.ParchmentCenter,
                SakinaLuxuryPalette.ParchmentEdge
            ),
            center = Offset(w * 0.5f, h * 0.42f),
            radius = maxOf(w, h) * 0.78f
        ),
        cornerRadius = CornerRadius(cr, cr)
    )

    // Outer metallic gold border
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
        style = Stroke(width = 1.4.dp.toPx())
    )

    // Optional ornamental inner hairline border
    if (doubleBorder && w > 28.dp.toPx() && h > 28.dp.toPx()) {
        val inset = 5.dp.toPx()
        val innerCr = (cr - inset).coerceAtLeast(6.dp.toPx())
        drawRoundRect(
            color = SakinaLuxuryPalette.GoldRich.copy(alpha = 0.42f),
            topLeft = Offset(inset, inset),
            size = Size(w - inset * 2f, h - inset * 2f),
            cornerRadius = CornerRadius(innerCr, innerCr),
            style = Stroke(width = 0.75.dp.toPx())
        )
    }
}

/**
 * Draws delicate procedural Islamic ornamental golden filigree arcs & diamonds
 * in the four corners of a card (as in Adhkar Oasis Bento cards).
 */
fun Modifier.sakinaCornerFiligree(
    goldColor: Color = SakinaLuxuryPalette.GoldBright,
    alpha: Float = 0.52f
): Modifier = this.drawWithContent {
    drawContent()
    val w = size.width
    val h = size.height
    val arm = min(w, h) * 0.24f
    val pad = 6.dp.toPx()
    val c = goldColor.copy(alpha = alpha)
    val stroke = Stroke(width = 1.0.dp.toPx(), cap = StrokeCap.Round)

    // Helper to draw a curved ornamental bracket at each corner
    fun drawCorner(ox: Float, oy: Float, dx: Float, dy: Float) {
        val p = Path().apply {
            moveTo(ox + dx * arm, oy)
            quadraticBezierTo(ox, oy, ox, oy + dy * arm)
        }
        drawPath(p, color = c, style = stroke)
        // Secondary fine inner curve
        val p2 = Path().apply {
            moveTo(ox + dx * (arm * 0.68f), oy + dy * 3.dp.toPx())
            quadraticBezierTo(
                ox + dx * 3.dp.toPx(),
                oy + dy * 3.dp.toPx(),
                ox + dx * 3.dp.toPx(),
                oy + dy * (arm * 0.68f)
            )
        }
        drawPath(p2, color = c.copy(alpha = alpha * 0.65f), style = Stroke(width = 0.65.dp.toPx()))
        // Tiny golden diamond accent
        val dCenter = Offset(ox + dx * 5.dp.toPx(), oy + dy * 5.dp.toPx())
        val dR = 2.dp.toPx()
        val dpPath = Path().apply {
            moveTo(dCenter.x, dCenter.y - dR)
            lineTo(dCenter.x + dR, dCenter.y)
            lineTo(dCenter.x, dCenter.y + dR)
            lineTo(dCenter.x - dR, dCenter.y)
            close()
        }
        drawPath(dpPath, color = SakinaLuxuryPalette.GoldSpecular.copy(alpha = alpha * 0.85f))
    }

    drawCorner(pad, pad, 1f, 1f)
    drawCorner(w - pad, pad, -1f, 1f)
    drawCorner(pad, h - pad, 1f, -1f)
    drawCorner(w - pad, h - pad, -1f, -1f)
}

/**
 * Multi-pass glowing golden oval/ellipse stroke (for Orbital Home, Tasbih, Qibla).
 */
fun DrawScope.drawGlowingGoldOval(
    topLeft: Offset,
    size: Size,
    coreStrokePx: Float,
    glowColor: Color = SakinaLuxuryPalette.GoldRich,
    specularColor: Color = SakinaLuxuryPalette.GoldSpecular,
    alphaScale: Float = 1f
) {
    // Pass 1: Wide atmospheric bloom
    drawOval(
        color = glowColor.copy(alpha = 0.14f * alphaScale),
        topLeft = topLeft,
        size = size,
        style = Stroke(width = coreStrokePx * 4.8f)
    )
    // Pass 2: Medium warm golden halo
    drawOval(
        color = SakinaLuxuryPalette.GoldBright.copy(alpha = 0.34f * alphaScale),
        topLeft = topLeft,
        size = size,
        style = Stroke(width = coreStrokePx * 2.3f)
    )
    // Pass 3: Metallic gradient core
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(
                SakinaLuxuryPalette.GoldDeep.copy(alpha = 0.85f * alphaScale),
                SakinaLuxuryPalette.GoldBright.copy(alpha = 0.96f * alphaScale),
                specularColor.copy(alpha = 1f * alphaScale),
                SakinaLuxuryPalette.GoldRich.copy(alpha = 0.94f * alphaScale),
                SakinaLuxuryPalette.GoldBright.copy(alpha = 0.90f * alphaScale)
            ),
            start = topLeft,
            end = Offset(topLeft.x + size.width, topLeft.y + size.height)
        ),
        topLeft = topLeft,
        size = size,
        style = Stroke(width = coreStrokePx)
    )
    // Pass 4: Crisp inner specular hairline
    drawOval(
        color = specularColor.copy(alpha = 0.78f * alphaScale),
        topLeft = topLeft,
        size = size,
        style = Stroke(width = (coreStrokePx * 0.34f).coerceAtLeast(1f))
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
                coreColor.copy(alpha = 0.65f * intensity),
                haloColor.copy(alpha = 0.34f * intensity),
                haloColor.copy(alpha = 0.10f * intensity),
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
                Color.White.copy(alpha = 0.95f * intensity),
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
 * Crisp 3D-feel vector Kaaba emblem with dark obsidian faces, gleaming golden
 * Kiswah belt, golden door, radial golden halo, and optional vertical light pillar.
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
    val topLift = sizePx * 0.20f

    // Optional vertical golden light pillar (Qibla beacon)
    if (withLightPillar) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    glowColor.copy(alpha = 0.18f),
                    SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.55f)
                ),
                startY = center.y - pillarHeightPx,
                endY = center.y - halfH * 0.4f
            ),
            topLeft = Offset(center.x - halfW * 0.55f, center.y - pillarHeightPx),
            size = Size(halfW * 1.1f, pillarHeightPx - halfH * 0.4f)
        )
    }

    // Soft radial golden halo behind the Kaaba
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.45f),
                glowColor.copy(alpha = 0.22f),
                Color.Transparent
            ),
            center = center,
            radius = sizePx * 1.15f
        ),
        radius = sizePx * 1.15f,
        center = center
    )

    // Top roof diamond face
    val topFace = Path().apply {
        moveTo(center.x, center.y - halfH - topLift)
        lineTo(center.x + halfW, center.y - halfH + topLift * 0.35f)
        lineTo(center.x, center.y - halfH + topLift * 1.35f)
        lineTo(center.x - halfW, center.y - halfH + topLift * 0.35f)
        close()
    }
    drawPath(
        topFace,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF28241C), Color(0xFF14120E)),
            start = Offset(center.x, center.y - halfH - topLift),
            end = Offset(center.x, center.y - halfH + topLift)
        )
    )

    // Left dark obsidian wall
    val leftWall = Path().apply {
        moveTo(center.x - halfW, center.y - halfH + topLift * 0.35f)
        lineTo(center.x, center.y - halfH + topLift * 1.35f)
        lineTo(center.x, center.y + halfH)
        lineTo(center.x - halfW, center.y + halfH - topLift * 0.75f)
        close()
    }
    drawPath(leftWall, color = Color(0xFF0E0D0C))

    // Right dark obsidian wall (slightly lit)
    val rightWall = Path().apply {
        moveTo(center.x, center.y - halfH + topLift * 1.35f)
        lineTo(center.x + halfW, center.y - halfH + topLift * 0.35f)
        lineTo(center.x + halfW, center.y + halfH - topLift * 0.75f)
        lineTo(center.x, center.y + halfH)
        close()
    }
    drawPath(rightWall, color = Color(0xFF1C1A16))

    // Golden Kiswah belt (Hizam) across both walls
    val beltYLeft = center.y - halfH * 0.32f
    val beltMidY = center.y - halfH * 0.08f
    val beltPath = Path().apply {
        moveTo(center.x - halfW, beltYLeft)
        lineTo(center.x, beltMidY)
        lineTo(center.x + halfW, beltYLeft)
    }
    drawPath(
        beltPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                SakinaLuxuryPalette.GoldRich,
                SakinaLuxuryPalette.GoldSpecular,
                SakinaLuxuryPalette.GoldRich
            ),
            startX = center.x - halfW,
            endX = center.x + halfW
        ),
        style = Stroke(width = (sizePx * 0.095f).coerceAtLeast(2f), join = StrokeJoin.Miter)
    )

    // Golden door accent on right wall
    val doorPath = Path().apply {
        val dx1 = center.x + halfW * 0.32f
        val dx2 = center.x + halfW * 0.62f
        val dyTop = center.y + halfH * 0.05f
        val dyBot = center.y + halfH * 0.64f
        moveTo(dx1, dyTop + 2f)
        lineTo(dx2, dyTop - 2f)
        lineTo(dx2, dyBot - 4f)
        lineTo(dx1, dyBot)
        close()
    }
    drawPath(doorPath, color = SakinaLuxuryPalette.GoldBright.copy(alpha = 0.88f))

    // Crisp metallic gold rim around roof & vertical edges
    drawPath(
        topFace,
        color = SakinaLuxuryPalette.GoldBright.copy(alpha = 0.80f),
        style = Stroke(width = 1.dp.toPx(), join = StrokeJoin.Round)
    )
}
