package app.sakina

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────── الخطوط ─────────────
val NotoNaskh = FontFamily(
    Font(R.font.notonaskharabic_regular, FontWeight.Normal),
    Font(R.font.notonaskharabic_bold, FontWeight.Bold)
)
val NotoKufi = FontFamily(
    Font(R.font.notokufiarabic_regular, FontWeight.Normal),
    Font(R.font.notokufiarabic_bold, FontWeight.Bold)
)
val NotoSans = FontFamily(
    Font(R.font.notosansarabic_regular, FontWeight.Normal),
    Font(R.font.notosansarabic_bold, FontWeight.Bold)
)

private fun sakinaTypography(): Typography {
    val b = Typography()
    fun TextStyle.n() = copy(fontFamily = NotoSans)
    return b.copy(
        displayLarge = b.displayLarge.n(), displayMedium = b.displayMedium.n(), displaySmall = b.displaySmall.n(),
        headlineLarge = b.headlineLarge.n(), headlineMedium = b.headlineMedium.n(), headlineSmall = b.headlineSmall.n(),
        titleLarge = b.titleLarge.n(), titleMedium = b.titleMedium.n(), titleSmall = b.titleSmall.n(),
        bodyLarge = b.bodyLarge.n(), bodyMedium = b.bodyMedium.n(), bodySmall = b.bodySmall.n(),
        labelLarge = b.labelLarge.n(), labelMedium = b.labelMedium.n(), labelSmall = b.labelSmall.n()
    )
}

@Immutable
class Sky(
    val top: Color,
    val bottom: Color,
    val accent: Color,
    val dark: Boolean,
    val ink: Color,
    val ink2: Color,
    val glass: Color,
    val halo: Color
) {
    val hairline: Color get() = accent.copy(alpha = if (dark) 0.32f else 0.42f)
}

private val IVORY = Color(0xFFFAF8F5)
private val GOLD = Color(0xFFD4AF37)
private val GLASS_DARK = Color(0xB81B222E) 
private val GLASS_LIGHT = Color(0xCCFAF8F5)

private fun lightSky(top: Long, bottom: Long, accent: Long, ink2: Long = 0xFF5E5140) =
    Sky(Color(top), Color(bottom), Color(accent), false, Color(0xFF1C1613), Color(ink2), GLASS_LIGHT, Color(accent).copy(alpha = 0.25f))

private fun darkSky(top: Long, bottom: Long, accent: Long, ink2: Long) =
    Sky(Color(top), Color(bottom), Color(accent), true, IVORY, Color(ink2), GLASS_DARK, Color(accent).copy(alpha = 0.2f))

object Brand {
    val GOLD = Color(0xFFD4AF37)
    val ON_GOLD = Color(0xFF1A1408)
    val SKIES = listOf(
        darkSky(0xFF171D36, 0xFF5B4B6E, 0xFFE6C280, 0xFFCFC8DA),   // Fajr
        lightSky(0xFFEFE7D6, 0xFFE8DEC8, 0xFFB88A3E),              // Duha
        lightSky(0xFFE8DEC8, 0xFFD8B88C, 0xFF9E722B),              // Dhuhr
        lightSky(0xFFD8B88C, 0xFFC69C53, 0xFF8C5A2B, 0xFF4F4334),  // Asr
        darkSky(0xFF7D4347, 0xFF2B2238, 0xFFE8B468, 0xFFE4DCD0),   // Maghrib
        darkSky(0xFF0B111C, 0xFF0A0E12, 0xFFD4AF6A, 0xFFB8B0A0)    // Isha
    )
    val AMOLED = Sky(Color(0xFF000000), Color(0xFF000000), GOLD, true, IVORY, Color(0xFF8A8A8A), Color(0x14FFFFFF), Color(0x1AD4AF37))
    val TNUM = TextStyle(fontFeatureSettings = "tnum")
}

private fun Sky.toColorScheme(): ColorScheme = if (dark) {
    darkColorScheme(
        primary = accent, onPrimary = Color(0xFF0B111C), secondary = accent,
        surface = glass, onSurface = ink, outline = hairline
    )
} else {
    lightColorScheme(
        primary = accent, onPrimary = Color.White, secondary = accent,
        surface = glass, onSurface = ink, outline = hairline
    )
}

@Composable
fun SakinaTheme(sky: Sky, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = sky.toColorScheme(), typography = sakinaTypography(), content = content)
}

@Composable
fun switchColors(sky: Sky) = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = sky.accent,
    checkedBorderColor = sky.accent,
    uncheckedThumbColor = sky.ink2,
    uncheckedTrackColor = sky.glass,
    uncheckedBorderColor = sky.hairline
)

@Composable
fun TabularText(
    text: String,
    fontSize: TextUnit,
    color: Color,
    fontFamily: FontFamily = NotoSans,
    fontWeight: FontWeight = FontWeight.Normal
) {
    val dw = with(LocalDensity.current) { (fontSize.toPx() * 0.6f).toDp() }
    val cw = with(LocalDensity.current) { (fontSize.toPx() * 0.32f).toDp() }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            text.forEach { ch ->
                Box(Modifier.width(if (ch.isDigit()) dw else cw), contentAlignment = Alignment.Center) {
                    Text(
                        ch.toString(), fontSize = fontSize, color = color, fontFamily = fontFamily,
                        fontWeight = fontWeight, maxLines = 1, softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun GlassCard(sky: Sky, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier.fillMaxWidth()
            .clip(shape)
            .background(sky.glass)
            .border(0.75.dp, sky.hairline, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun SectionTitle(sky: Sky, text: String) {
    Text(text, color = sky.ink, fontSize = 18.sp, fontFamily = NotoKufi, fontWeight = FontWeight.Bold)
}

@Composable
fun GoldButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Brand.GOLD, contentColor = Brand.ON_GOLD)
    ) { Text(text, fontSize = 13.sp, maxLines = 1, softWrap = false, fontWeight = FontWeight.Bold, fontFamily = NotoKufi) }
}

@Composable
fun GlassButton(sky: Sky, text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
        border = BorderStroke(0.75.dp, sky.hairline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = sky.ink)
    ) { Text(text, fontSize = 13.sp, maxLines = 1, softWrap = false, fontFamily = NotoSans) }
}

@Composable
fun ModeChip(sky: Sky, label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.width(112.dp).heightIn(min = 44.dp),
        contentPadding = PaddingValues(horizontal = 6.dp),
        border = BorderStroke(0.75.dp, sky.hairline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = sky.ink)
    ) { Text(label, fontSize = 12.sp, maxLines = 1, softWrap = false, fontFamily = NotoSans) }
}

fun modeIcon(mode: Int): ImageVector = when (mode) {
    0 -> Icons.Rounded.NotificationsActive
    1 -> Icons.Rounded.VolumeUp
    2 -> Icons.Rounded.Vibration
    else -> Icons.Rounded.NotificationsOff
}

@Composable
fun ModeIconButton(sky: Sky, mode: Int, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        Icon(modeIcon(mode), contentDescription = description, tint = sky.accent, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun StepButton(sky: Sky, text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        contentPadding = PaddingValues(0.dp),
        border = BorderStroke(0.75.dp, sky.hairline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = sky.ink)
    ) { Text(text, fontSize = 18.sp, fontFamily = NotoSans) }
}

@Composable
fun BrandNav(sky: Sky, labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val icons = listOf(Icons.Rounded.Home, Icons.Rounded.Explore, Icons.Rounded.Tune)
    val shape = RoundedCornerShape(28.dp)
    Row(
        Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(shape)
            .background(sky.glass)
            .border(0.75.dp, sky.hairline, shape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEachIndexed { i, l ->
            val on = i == selected
            Column(
                Modifier.weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onSelect(i) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    icons[i], contentDescription = l,
                    tint = if (on) sky.accent else sky.ink2, modifier = Modifier.size(22.dp)
                )
                Text(
                    l, color = if (on) sky.ink else sky.ink2, fontSize = 12.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = NotoKufi
                )
                Box(
                    Modifier.padding(top = 3.dp)
                        .size(width = if (on) 20.dp else 0.dp, height = 2.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brand.GOLD)
                )
            }
        }
    }
}

@Composable
fun SkyBackdrop(sky: Sky, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(listOf(sky.top, sky.bottom)))
        val c = Offset(w / 2f, h * 0.42f)
        val glowR = w * 0.95f
        drawCircle(
            brush = Brush.radialGradient(listOf(sky.halo, Color.Transparent), center = c, radius = glowR),
            radius = glowR, center = c
        )
        for (k in 1..5) {
            drawCircle(sky.accent.copy(alpha = 0.07f), w * 0.17f * k, c, style = Stroke(1.dp.toPx()))
        }
        for (k in 0 until 8) {
            val a = Math.toRadians(k * 45.0 + 22.5)
            drawLine(
                sky.accent.copy(alpha = 0.06f), c,
                Offset(c.x + (w * 1.2f * Math.cos(a)).toFloat(), c.y + (w * 1.2f * Math.sin(a)).toFloat()),
                1.dp.toPx()
            )
        }
        if (sky.dark) {
            val rnd = java.util.Random(7)
            for (i in 0 until 48) {
                val x = rnd.nextFloat() * w
                val y = rnd.nextFloat() * h * 0.55f
                val r = 1f + rnd.nextFloat() * 1.8f
                val al = 0.25f + rnd.nextFloat() * 0.5f
                drawCircle(Color.White.copy(alpha = al), r, Offset(x, y))
            }
        }
    }
}
