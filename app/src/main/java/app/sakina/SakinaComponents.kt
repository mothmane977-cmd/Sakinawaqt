package app.sakina

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// v19 Point 11: 4 balanced bottom tabs (Home, Qibla, Quran, Hijri Calendar)
enum class SakinaNavTab(@DrawableRes val iconRes: Int) {
    HOME(R.drawable.ic_mosque),
    QIBLA(R.drawable.ic_kaaba),
    QURAN(R.drawable.ic_adhkar),
    CALENDAR(R.drawable.ic_calendar);

    fun label(s: SakinaStrings): String = when (this) {
        HOME -> s.tabHome
        QIBLA -> s.tabQibla
        QURAN -> s.tabQuran
        CALENDAR -> s.tabCalendar
    }
}

val SakinaInkDark = Color(0xFF152230)

val Sakina3DGoldBevelBrush = Brush.linearGradient(
    listOf(Color(0xFF6B5224), Color(0xFFC5A059), Color(0xFFFFF2C2), Color(0xFFC5A059), Color(0xFF5C441B))
)

val SakinaDarkPlateBrush = Brush.verticalGradient(
    listOf(Color(0xF6071721), Color(0xFA030B11))
)

fun String.toWesternDigits(): String {
    val hindi = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val western = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    var out = this
    for (i in hindi.indices) out = out.replace(hindi[i], western[i])
    return out
}

@Composable
fun SakinaCalligraphyBrush(): Brush = Brush.verticalGradient(
    listOf(Color(0xFFFFFFFF), Color(0xFFFFF4C2), Color(0xFFE6C36E), Color(0xFFC5A059), Color(0xFF8C6B28))
)

// v19 Point 2 (solid dark backing) & Point 16 (useAmiri option for HomeScreen "سَكِينة")
@Composable
fun SakinaTitle3D(
    text: String,
    fontSize: TextUnit = 21.sp,
    modifier: Modifier = Modifier,
    useAmiri: Boolean = false
) {
    val isRtl = LocalSakinaLang.current.isRtl
    val pillShape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .wrapContentWidth()
            .shadow(8.dp, pillShape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(pillShape)
            .background(SakinaDarkPlateBrush)
            .border(1.3.dp, Sakina3DGoldBevelBrush, pillShape)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontFamily = when {
                    !isRtl -> SakinaLatinLuxuryFontFamily
                    useAmiri -> SakinaNaskhFontFamily
                    else -> SakinaOrnateArabicFont
                },
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                brush = SakinaCalligraphyBrush(),
                shadow = Shadow(color = Color(0xFF000000), offset = Offset(0f, 2f), blurRadius = 6f),
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }
}

// v19 Point 3: Luxury Horological Analog Clock (fluted gold bezel, guilloche sunburst dial, sculpted Dauphine hands, ruby jewel cap)
@Composable
fun SakinaAnalogClockDial(
    timeMs: Long,
    size: Dp = 176.dp,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r = this.size.minDimension / 2f - 2.dp.toPx()

        // 0. Outer Shadow Halo
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0x88000000), Color.Transparent),
                center = Offset(cx, cy + 3.dp.toPx()),
                radius = r * 1.06f
            ),
            radius = r * 1.04f,
            center = Offset(cx, cy + 2.dp.toPx())
        )

        // 1. Sculpted 3D Brushed Gold Fluted Outer Case & Bezel
        drawCircle(
            brush = Brush.sweepGradient(
                listOf(
                    Color(0xFF7B5B24), Color(0xFFFFF3C4), Color(0xFFC5A059),
                    Color(0xFF5D4318), Color(0xFFF5DE9A), Color(0xFFB88E44), Color(0xFF7B5B24)
                ),
                center = Offset(cx, cy)
            ),
            radius = r,
            center = Offset(cx, cy)
        )

        // Fluted bezel ridges (72 fine luxury notches around the rim)
        if (r > 36.dp.toPx()) {
            for (i in 0 until 72) {
                val ang = (i * 5.0) * PI / 180.0
                drawLine(
                    color = if (i % 2 == 0) Color(0x55FFF8DC) else Color(0x553A280A),
                    start = Offset(cx + (r * 0.91f * cos(ang)).toFloat(), cy + (r * 0.91f * sin(ang)).toFloat()),
                    end = Offset(cx + (r * 0.99f * cos(ang)).toFloat(), cy + (r * 0.99f * sin(ang)).toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // 2. Deep Sunburst Emerald-Obsidian Dial Face
        val dialR = r * 0.90f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF133647), theme.bgCard, Color(0xFF040D14)),
                center = Offset(cx, cy - dialR * 0.15f),
                radius = dialR * 1.15f
            ),
            radius = dialR,
            center = Offset(cx, cy)
        )

        // Subtle guilloche sunburst rays
        for (i in 0 until 36) {
            val ang = (i * 10.0) * PI / 180.0
            drawLine(
                color = Color(0x14F3DC8B),
                start = Offset(cx, cy),
                end = Offset(cx + (dialR * 0.96f * cos(ang)).toFloat(), cy + (dialR * 0.96f * sin(ang)).toFloat()),
                strokeWidth = 0.65.dp.toPx()
            )
        }

        // Inner decorative gold chapter rings
        drawCircle(brush = Sakina3DGoldBevelBrush, radius = dialR, center = Offset(cx, cy), style = Stroke(width = 1.4.dp.toPx()))
        drawCircle(color = Color(0x55F3DC8B), radius = dialR * 0.89f, center = Offset(cx, cy), style = Stroke(width = 0.7.dp.toPx()))
        drawCircle(color = Color(0x33D4AF37), radius = dialR * 0.54f, center = Offset(cx, cy), style = Stroke(width = 0.6.dp.toPx()))

        // 3. 60 Minute Railway Ticks + 12 Sculpted Diamond-Faceted Gold Hour Indices
        for (i in 0 until 60) {
            val ang = (i * 6.0 - 90.0) * PI / 180.0
            val cosA = cos(ang).toFloat()
            val sinA = sin(ang).toFloat()
            if (i % 5 == 0) {
                val isTwelve = i == 0
                val isQuarter = i % 15 == 0
                val outer = dialR * 0.88f
                val inner = if (isQuarter) dialR * 0.67f else dialR * 0.74f
                val perpX = -sinA
                val perpY = cosA
                if (isTwelve) {
                    // Double luxury baton at 12 o'clock
                    val gap = (dialR * 0.036f).coerceAtLeast(1.8.dp.toPx())
                    for (sign in listOf(-1f, 1f)) {
                        val ox = perpX * gap * sign
                        val oy = perpY * gap * sign
                        drawLine(
                            brush = Sakina3DGoldBevelBrush,
                            start = Offset(cx + inner * cosA + ox, cy + inner * sinA + oy),
                            end = Offset(cx + outer * cosA + ox, cy + outer * sinA + oy),
                            strokeWidth = (dialR * 0.045f).coerceAtLeast(2.dp.toPx()),
                            cap = StrokeCap.Round
                        )
                    }
                } else {
                    // Faceted diamond-tipped gold baton
                    val halfW = if (isQuarter) (dialR * 0.038f).coerceAtLeast(1.8.dp.toPx()) else (dialR * 0.025f).coerceAtLeast(1.2.dp.toPx())
                    val baton = Path().apply {
                        moveTo(cx + inner * cosA, cy + inner * sinA)
                        lineTo(cx + (inner + (outer - inner) * 0.25f) * cosA + perpX * halfW, cy + (inner + (outer - inner) * 0.25f) * sinA + perpY * halfW)
                        lineTo(cx + outer * cosA + perpX * (halfW * 0.75f), cy + outer * sinA + perpY * (halfW * 0.75f))
                        lineTo(cx + outer * cosA - perpX * (halfW * 0.75f), cy + outer * sinA - perpY * (halfW * 0.75f))
                        lineTo(cx + (inner + (outer - inner) * 0.25f) * cosA - perpX * halfW, cy + (inner + (outer - inner) * 0.25f) * sinA - perpY * halfW)
                        close()
                    }
                    drawPath(baton, brush = Sakina3DGoldBevelBrush)
                    drawCircle(
                        color = Color(0xFFFFF8DC),
                        radius = if (isQuarter) 1.6.dp.toPx() else 1.1.dp.toPx(),
                        center = Offset(cx + (outer + dialR * 0.045f) * cosA, cy + (outer + dialR * 0.045f) * sinA)
                    )
                }
            } else {
                drawLine(
                    color = Color(0x77F3DC8B),
                    start = Offset(cx + (dialR * 0.85f * cosA), cy + (dialR * 0.85f * sinA)),
                    end = Offset(cx + (dialR * 0.89f * cosA), cy + (dialR * 0.89f * sinA)),
                    strokeWidth = 0.75.dp.toPx()
                )
            }
        }

        val cal = Calendar.getInstance().apply { timeInMillis = timeMs }
        val sec = cal.get(Calendar.SECOND)
        val min = cal.get(Calendar.MINUTE) + sec / 60f
        val hr = (cal.get(Calendar.HOUR) % 12) + min / 60f

        val hrAng = (hr * 30.0 - 90.0) * PI / 180.0
        val minAng = (min * 6.0 - 90.0) * PI / 180.0
        val secAng = (sec * 6.0 - 90.0) * PI / 180.0

        fun drawDauphineHand(angle: Double, length: Float, tail: Float, halfWidth: Float, fillBrush: Brush, highlightColor: Color) {
            val cosA = cos(angle).toFloat()
            val sinA = sin(angle).toFloat()
            val perpX = -sinA
            val perpY = cosA
            val tip = Offset(cx + length * cosA, cy + length * sinA)
            val tailPt = Offset(cx - tail * cosA, cy - tail * sinA)
            val leftPt = Offset(cx + perpX * halfWidth, cy + perpY * halfWidth)
            val rightPt = Offset(cx - perpX * halfWidth, cy - perpY * halfWidth)

            // Soft drop shadow
            val shadowOff = Offset(1.5.dp.toPx(), 2.2.dp.toPx())
            val shadowPath = Path().apply {
                moveTo(tip.x + shadowOff.x, tip.y + shadowOff.y)
                lineTo(leftPt.x + shadowOff.x, leftPt.y + shadowOff.y)
                lineTo(tailPt.x + shadowOff.x, tailPt.y + shadowOff.y)
                lineTo(rightPt.x + shadowOff.x, rightPt.y + shadowOff.y)
                close()
            }
            drawPath(shadowPath, color = Color(0x66000000))

            // Full Dauphine facet
            val fullPath = Path().apply {
                moveTo(tip.x, tip.y); lineTo(leftPt.x, leftPt.y); lineTo(tailPt.x, tailPt.y); lineTo(rightPt.x, rightPt.y); close()
            }
            drawPath(fullPath, brush = fillBrush)

            // Bright upper bevel facet (3D metallic reflection)
            val brightFacet = Path().apply {
                moveTo(tip.x, tip.y); lineTo(leftPt.x, leftPt.y); lineTo(tailPt.x, tailPt.y); close()
            }
            drawPath(brightFacet, color = highlightColor)
        }

        // 4. Sculpted Dauphine Gold Hour Hand
        drawDauphineHand(
            angle = hrAng,
            length = dialR * 0.50f,
            tail = dialR * 0.14f,
            halfWidth = (dialR * 0.058f).coerceAtLeast(3.dp.toPx()),
            fillBrush = Sakina3DGoldBevelBrush,
            highlightColor = Color(0x55FFF8DC)
        )

        // 5. Sculpted Dauphine Ivory-Gold Minute Hand
        drawDauphineHand(
            angle = minAng,
            length = dialR * 0.72f,
            tail = dialR * 0.16f,
            halfWidth = (dialR * 0.042f).coerceAtLeast(2.2.dp.toPx()),
            fillBrush = Brush.linearGradient(listOf(Color(0xFFFFFBF0), Color(0xFFEBD39A), Color(0xFFC5A059))),
            highlightColor = Color(0x66FFFFFF)
        )

        // 6. Fine Sweeping Gold Second Hand with Ring Counterweight
        val secCos = cos(secAng).toFloat()
        val secSin = sin(secAng).toFloat()
        drawLine(
            color = Color(0xFFFFD86B),
            start = Offset(cx - dialR * 0.22f * secCos, cy - dialR * 0.22f * secSin),
            end = Offset(cx + dialR * 0.80f * secCos, cy + dialR * 0.80f * secSin),
            strokeWidth = 1.3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0xFFFFD86B),
            radius = (dialR * 0.045f).coerceAtLeast(2.2.dp.toPx()),
            center = Offset(cx - dialR * 0.17f * secCos, cy - dialR * 0.17f * secSin),
            style = Stroke(width = 1.3.dp.toPx())
        )

        // 7. Layered Gold & Emerald Center Jewel Cap + Sapphire Crystal Sheen
        drawCircle(brush = Sakina3DGoldBevelBrush, radius = (dialR * 0.085f).coerceAtLeast(4.dp.toPx()), center = Offset(cx, cy))
        drawCircle(color = Color(0xFF0A2A2B), radius = (dialR * 0.042f).coerceAtLeast(2.dp.toPx()), center = Offset(cx, cy))
        drawCircle(color = Color(0xFFFFF4C2), radius = (dialR * 0.018f).coerceAtLeast(1.dp.toPx()), center = Offset(cx - 1.dp.toPx(), cy - 1.dp.toPx()))

        drawArc(
            brush = Brush.linearGradient(listOf(Color(0x30FFFFFF), Color.Transparent)),
            startAngle = -148f,
            sweepAngle = 108f,
            useCenter = false,
            style = Stroke(width = (dialR * 0.07f).coerceAtLeast(2.dp.toPx()), cap = StrokeCap.Round)
        )
    }
}

// v19 Points 14 & 15: Symmetric 3D Gold-Beveled Clock Action Toggle Buttons (flanking the center clock cartouche)
@Composable
fun SakinaClockActionButton(
    isActive: Boolean,
    iconType: Int, // 0 = Analog/Digital clock switcher (Right), 1 = Current Time / Countdown switcher (Left)
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .size(42.dp)
            .shadow(6.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .background(SakinaDarkPlateBrush)
            .border(1.3.dp, Sakina3DGoldBevelBrush, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val gold = if (isActive) Color(0xFFFFF2C2) else theme.accentBright
            val deepGold = theme.accentGold

            if (iconType == 0) {
                if (!isActive) {
                    // Show Luxury Analog Watch Icon (tap to switch to Analog)
                    val r = w * 0.44f
                    drawCircle(brush = Sakina3DGoldBevelBrush, radius = r, center = Offset(cx, cy), style = Stroke(width = 1.9.dp.toPx()))
                    drawLine(color = gold, start = Offset(cx, cy), end = Offset(cx, cy - r * 0.56f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(color = deepGold, start = Offset(cx, cy), end = Offset(cx + r * 0.45f, cy + r * 0.18f), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
                    drawCircle(color = gold, radius = 1.8.dp.toPx(), center = Offset(cx, cy))
                } else {
                    // Show Digital Display Icon (tap to return to Digital)
                    drawRoundRect(
                        brush = Sakina3DGoldBevelBrush,
                        topLeft = Offset(1.dp.toPx(), h * 0.20f),
                        size = Size(w - 2.dp.toPx(), h * 0.60f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                    // Minimal digital segments "12:00"
                    drawCircle(color = gold, radius = 1.3.dp.toPx(), center = Offset(cx, cy - 2.2.dp.toPx()))
                    drawCircle(color = gold, radius = 1.3.dp.toPx(), center = Offset(cx, cy + 2.2.dp.toPx()))
                    drawLine(color = gold, start = Offset(cx - 5.5.dp.toPx(), cy - 2.5.dp.toPx()), end = Offset(cx - 5.5.dp.toPx(), cy + 2.5.dp.toPx()), strokeWidth = 1.7.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(color = gold, start = Offset(cx + 5.5.dp.toPx(), cy - 2.5.dp.toPx()), end = Offset(cx + 5.5.dp.toPx(), cy + 2.5.dp.toPx()), strokeWidth = 1.7.dp.toPx(), cap = StrokeCap.Round)
                }
            } else {
                // Left Button (Point 15): Switch between Remaining Countdown & Current Time (Hourglass <-> Live Clock)
                if (!isActive) {
                    // Currently showing Countdown -> show Live Clock / Switch Arrows icon
                    val r = w * 0.40f
                    drawArc(brush = Sakina3DGoldBevelBrush, startAngle = -60f, sweepAngle = 290f, useCenter = false, style = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round))
                    drawLine(color = gold, start = Offset(cx, cy), end = Offset(cx, cy - r * 0.52f), strokeWidth = 1.7.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(color = deepGold, start = Offset(cx, cy), end = Offset(cx + r * 0.42f, cy), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
                    // Small arrow tip on arc
                    drawCircle(color = gold, radius = 1.8.dp.toPx(), center = Offset(cx + r * 0.72f, cy - r * 0.55f))
                } else {
                    // Currently showing Current Time -> show Luxury Hourglass icon (tap to show Countdown)
                    val topY = h * 0.14f
                    val botY = h * 0.86f
                    val leftX = w * 0.24f
                    val rightX = w * 0.76f
                    drawLine(brush = Sakina3DGoldBevelBrush, start = Offset(leftX - 1.dp.toPx(), topY), end = Offset(rightX + 1.dp.toPx(), topY), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(brush = Sakina3DGoldBevelBrush, start = Offset(leftX - 1.dp.toPx(), botY), end = Offset(rightX + 1.dp.toPx(), botY), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    val glass = Path().apply {
                        moveTo(leftX, topY); lineTo(rightX, topY); lineTo(cx, cy); lineTo(rightX, botY); lineTo(leftX, botY); lineTo(cx, cy); close()
                    }
                    drawPath(glass, color = gold, style = Stroke(width = 1.5.dp.toPx(), join = StrokeJoin.Round))
                    // Golden sand drop
                    drawCircle(color = deepGold, radius = 1.5.dp.toPx(), center = Offset(cx, cy + h * 0.18f))
                }
            }
        }
    }
}

// Ornate Islamic Hexagon Badge for Surah Numbers
@Composable
fun SakinaHexagonNumberBadge(
    number: Int,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val rOuter = this.size.minDimension / 2f - 1.5.dp.toPx()
            val rInner = rOuter * 0.80f

            val outerHex = Path().apply {
                for (i in 0 until 6) {
                    val ang = (i * 60.0 - 90.0) * PI / 180.0
                    val x = cx + (rOuter * cos(ang)).toFloat()
                    val y = cy + (rOuter * sin(ang)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            val innerHex = Path().apply {
                for (i in 0 until 6) {
                    val ang = (i * 60.0 - 90.0) * PI / 180.0
                    val x = cx + (rInner * cos(ang)).toFloat()
                    val y = cy + (rInner * sin(ang)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            drawPath(outerHex, color = theme.bgDeep)
            drawPath(outerHex, brush = Sakina3DGoldBevelBrush, style = Stroke(width = 1.6.dp.toPx()))
            drawPath(innerHex, color = Color(0x55C5A059), style = Stroke(width = 0.8.dp.toPx()))

            for (i in 0 until 6) {
                val ang = (i * 60.0 - 90.0) * PI / 180.0
                val vx = cx + (rOuter * cos(ang)).toFloat()
                val vy = cy + (rOuter * sin(ang)).toFloat()
                drawCircle(color = theme.accentBright, radius = 1.3.dp.toPx(), center = Offset(vx, vy))
            }
        }
        Text(
            text = "$number".toWesternDigits(),
            fontFamily = SakinaNumeralFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            color = theme.accentBright,
            textAlign = TextAlign.Center,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
    }
}

// v19 Point 2: Dark solid obsidian-emerald backing on Back Button so it NEVER clashes with Mihrab or background
@Composable
fun SakinaBackIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    val lang = LocalSakinaLang.current
    val pill = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .shadow(8.dp, pill, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(pill)
            .background(SakinaDarkPlateBrush)
            .border(1.3.dp, Sakina3DGoldBevelBrush, pill)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = null,
            tint = theme.accentBright,
            modifier = Modifier.size(17.dp)
        )
        Text(
            text = sakinaTr(lang, "رجوع", "Back", "Retour", "Geri", "Kembali", "واپس"),
            fontFamily = SakinaBodyFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = theme.accentBright,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
    }
}

// v19 Point 12: Redesigned 3D Brushed-Gold Embossed Drawer Trigger Buttons (identical & mirrored left/right)
@Composable
fun SakinaGraduatedMenuIcon(
    mirror: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileShape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(40.dp)
            .shadow(7.dp, tileShape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(tileShape)
            .background(SakinaDarkPlateBrush)
            .border(1.4.dp, Sakina3DGoldBevelBrush, tileShape)
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val barH = 3.2.dp.toPx()
            val widths = listOf(w, w * 0.74f, w * 0.50f)
            val ys = listOf(0f, (h - barH) / 2f, h - barH)

            val gold3D = Brush.verticalGradient(
                listOf(Color(0xFFFFF8DC), Color(0xFFE8C46A), Color(0xFFB68A35), Color(0xFF7D5A1C))
            )

            for (i in 0..2) {
                val bw = widths[i]
                val left = if (mirror) 0f else (w - bw)
                val top = ys[i]
                // Drop shadow under each 3D gold bar
                drawRoundRect(
                    color = Color(0x99000000),
                    topLeft = Offset(left, top + 1.2.dp.toPx()),
                    size = Size(bw, barH),
                    cornerRadius = CornerRadius(barH / 2f, barH / 2f)
                )
                // Metallic 3D gold bar
                drawRoundRect(
                    brush = gold3D,
                    topLeft = Offset(left, top),
                    size = Size(bw, barH),
                    cornerRadius = CornerRadius(barH / 2f, barH / 2f)
                )
                // Top specular highlight glint
                drawRoundRect(
                    color = Color(0x88FFFFFF),
                    topLeft = Offset(left + 1.dp.toPx(), top + 0.4.dp.toPx()),
                    size = Size((bw - 2.dp.toPx()).coerceAtLeast(2f), barH * 0.32f),
                    cornerRadius = CornerRadius(barH / 4f, barH / 4f)
                )
            }
        }
    }
}

@Composable
fun SakinaStarPolygonBadge(
    @DrawableRes iconRes: Int,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val rOuter = this.size.minDimension / 2f
            val rInner = rOuter * 0.80f
            val starPath = Path().apply {
                for (i in 0 until 16) {
                    val ang = (i * 22.5 - 90.0) * PI / 180.0
                    val r = if (i % 2 == 0) rOuter else rInner
                    val x = cx + (r * cos(ang)).toFloat()
                    val y = cy + (r * sin(ang)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(starPath, color = theme.bgDeep)
            drawPath(starPath, brush = Sakina3DGoldBevelBrush, style = Stroke(width = 1.3.dp.toPx()))
        }
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(size * 0.68f)
        )
    }
}

@Composable
fun SakinaBackgroundLayer(
    modifier: Modifier = Modifier,
    @DrawableRes headerImageRes: Int = R.drawable.bg_header_mosque,
    showMihrabFrame: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = LocalSakinaTheme.current
    val deep = theme.bgDeep
    Box(modifier = modifier.fillMaxSize().background(deep)) {
        Image(
            painter = painterResource(id = headerImageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.44f).align(Alignment.TopCenter)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.46f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        0.0f to deep.copy(alpha = 0.58f),
                        0.55f to deep.copy(alpha = 0.48f),
                        0.88f to deep.copy(alpha = 0.90f),
                        1.0f to deep
                    )
                )
        )

        if (showMihrabFrame) {
            Image(
                painter = painterResource(id = R.drawable.bg_mihrab),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                alpha = 0.72f,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .align(Alignment.TopCenter)
            )
        }

        // Render screen content ABOVE the Mihrab arch so titles & back buttons NEVER get covered by the arch (Point 2)
        content()
    }
}

// v19 Point 2: Dark solid backing capsule inside SakinaOrnamentCartouche so titles & clock NEVER clash with background
@Composable
fun SakinaOrnamentCartouche(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp, vertical = 10.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val pillShape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .widthIn(min = 148.dp, max = 264.dp)
            .defaultMinSize(minHeight = 46.dp)
            .shadow(8.dp, pillShape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(pillShape)
            .background(SakinaDarkPlateBrush)
            .border(1.3.dp, Sakina3DGoldBevelBrush, pillShape),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.padding(contentPadding), contentAlignment = Alignment.Center, content = content)
    }
}

@Composable
fun SakinaParchmentPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalSakinaTheme.current
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(10.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(theme.parchmentTop, theme.parchmentBottom)))
            .drawBehind {
                val step = 20.dp.toPx()
                var x = -size.height
                while (x < size.width + size.height) {
                    drawLine(Color(0x0FA67C2E), Offset(x, 0f), Offset(x + size.height, size.height), strokeWidth = 0.7.dp.toPx())
                    drawLine(Color(0x0FA67C2E), Offset(x + size.height, 0f), Offset(x, size.height), strokeWidth = 0.7.dp.toPx())
                    x += step
                }
            }
            .border(1.4.dp, Sakina3DGoldBevelBrush, shape)
    ) {
        val cSize = 22.dp
        Image(
            painter = painterResource(id = R.drawable.ic_corner_arabesque),
            contentDescription = null,
            alpha = 0.88f,
            modifier = Modifier.size(cSize).align(AbsoluteAlignment.TopLeft).padding(2.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_corner_arabesque),
            contentDescription = null,
            alpha = 0.88f,
            modifier = Modifier.size(cSize).align(AbsoluteAlignment.TopRight).padding(2.dp).graphicsLayer { rotationZ = 90f }
        )
        Image(
            painter = painterResource(id = R.drawable.ic_corner_arabesque),
            contentDescription = null,
            alpha = 0.88f,
            modifier = Modifier.size(cSize).align(AbsoluteAlignment.BottomRight).padding(2.dp).graphicsLayer { rotationZ = 180f }
        )
        Image(
            painter = painterResource(id = R.drawable.ic_corner_arabesque),
            contentDescription = null,
            alpha = 0.88f,
            modifier = Modifier.size(cSize).align(AbsoluteAlignment.BottomLeft).padding(2.dp).graphicsLayer { rotationZ = 270f }
        )

        Column(modifier = Modifier.fillMaxWidth().padding(contentPadding), content = content)
    }
}

@Composable
fun SakinaGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    glowGold: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalSakinaTheme.current
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val borderBrush = if (glowGold) Sakina3DGoldBevelBrush
    else Brush.verticalGradient(listOf(theme.accentGold.copy(alpha = 0.55f), Color(0x22FFFFFF)))

    Column(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(theme.bgCard.copy(alpha = 0.94f), theme.bgDeep.copy(alpha = 0.97f))))
            .border(1.dp, borderBrush, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun SakinaDrawerPillItem(
    @DrawableRes iconRes: Int,
    title: String,
    onClick: () -> Unit
) {
    val theme = LocalSakinaTheme.current
    val textColor = LocalSakinaTextColor.current
    val pillShape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(pillShape)
            .background(Brush.verticalGradient(listOf(theme.bgCard, theme.bgDeep)))
            .border(0.9.dp, Sakina3DGoldBevelBrush, pillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontFamily = SakinaEditorialFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SakinaBottomPillBar(
    currentTab: SakinaNavTab,
    onTabSelected: (SakinaNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val barShape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .height(62.dp)
            .clip(barShape)
            .background(Brush.verticalGradient(listOf(theme.bgCard.copy(alpha = 0.96f), theme.bgDeep)))
            .border(1.dp, Sakina3DGoldBevelBrush, barShape)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SakinaNavTab.entries.forEach { tab ->
            val selected = tab == currentTab
            val textColor by animateColorAsState(if (selected) theme.bgDeep else theme.accentBright, label = "tabColor")
            val tabShape = RoundedCornerShape(14.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(tabShape)
                    .background(
                        if (selected) Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold))
                        else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onTabSelected(tab) }
                    .padding(vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(painter = painterResource(id = tab.iconRes), contentDescription = tab.label(s), modifier = Modifier.size(23.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tab.label(s),
                    fontFamily = SakinaHeadingFont,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 9.5.sp,
                    lineHeight = 11.sp,
                    color = textColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }
        }
    }
}

@Composable
fun SakinaGoldSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val thumbOffset by animateDpAsState(if (checked) 36.dp else 4.dp, label = "switchThumb")
    val borderBrush = if (checked) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color(0x55FFFFFF), Color(0x33FFFFFF)))

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .width(66.dp)
                .height(30.dp)
                .clip(CircleShape)
                .background(if (checked) theme.accentGold.copy(alpha = 0.24f) else Color(0x331B2531))
                .border(0.8.dp, borderBrush, CircleShape)
                .clickable { onCheckedChange(!checked) },
            contentAlignment = Alignment.CenterStart
        ) {
            if (checked) {
                Text("ON", fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = theme.accentBright, modifier = Modifier.padding(start = 10.dp))
            }
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(22.dp)
                    .shadow(8.dp, CircleShape, ambientColor = theme.accentGold, spotColor = theme.accentGold)
                    .clip(CircleShape)
                    .background(
                        if (checked) Brush.radialGradient(listOf(Color(0xFFFFF4C2), theme.accentBright, theme.accentGold))
                        else Brush.radialGradient(listOf(Color(0xFF8B949E), Color(0xFF4B5563)))
                    )
            )
        }
    }
}
