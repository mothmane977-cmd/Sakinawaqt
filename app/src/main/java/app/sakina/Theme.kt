package app.sakina

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

// ───────────── الخطوط الجديدة (بناءً على الحزمة التي رفعتها) ─────────────
val NotoNaskh = FontFamily(
    Font(R.font.noto_naskh_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_naskh_arabic_bold, FontWeight.Bold)
)
val NotoKufi = FontFamily(
    Font(R.font.noto_kufi_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_kufi_arabic_bold, FontWeight.Bold)
)
val NotoSans = FontFamily(
    Font(R.font.noto_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_sans_arabic_bold, FontWeight.Bold)
)
// خط Inter للأرقام (تم رفعه بنجاح)
val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
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

// ───────────── الألوان ─────────────
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
    val hairline: Color get() = accent.copy(alpha = 0.25f)
}

val MIDNIGHT = Color(0xFF0A0E12)
val GOLD = Color(0xFFD4AF37)
val IVORY = Color(0xFFFAF8F5)
val GLASS_DARK = Color(0xB8131B22)

private fun darkSky(top: Long, bottom: Long, accent: Long, ink2: Long) =
    Sky(Color(top), Color(bottom), Color(accent), true, IVORY, Color(ink2), GLASS_DARK, Color(accent).copy(alpha = 0.15f))

object Brand {
    val GOLD = Color(0xFFD4AF37)
    val ON_GOLD = Color(0xFF1A1408)
    val SKIES = listOf(
        darkSky(0xFF171D36, 0xFF5B4B6E, 0xFFE6C280, 0xFFCFC8DA),   // Fajr
        darkSky(0xFF0A0E12, 0xFF1C1812, 0xFFD4AF37, 0xFFB8B0A0),   // Duha
        darkSky(0xFF0A0E12, 0xFF131B22, 0xFFD4AF37, 0xFFB8B0A0),   // Dhuhr
        darkSky(0xFF0A0E12, 0xFF131B22, 0xFFD4AF37, 0xFFB8B0A0),   // Asr
        darkSky(0xFF7D4347, 0xFF2B2238, 0xFFE8B468, 0xFFE4DCD0),   // Maghrib
        darkSky(0xFF0B111C, 0xFF0A0E12, 0xFFD4AF6A, 0xFFB8B0A0)    // Isha
    )
    val AMOLED = Sky(Color(0xFF000000), Color(0xFF000000), GOLD, true, IVORY, Color(0xFF8A8A8A), Color(0x14FFFFFF), Color(0x1AD4AF37))
}

private fun Sky.toColorScheme(): ColorScheme = darkColorScheme(
    primary = accent, onPrimary = Color(0xFF0B111C), secondary = accent,
    surface = glass, onSurface = ink, outline = hairline,
    background = MIDNIGHT, onBackground = ink
)

@Composable
fun SakinaTheme(sky: Sky, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = sky.toColorScheme(), typography = sakinaTypography(), content = content)
}

@Composable
fun switchColors(sky: Sky) = SwitchDefaults.colors(
    checkedThumbColor = Color.White, checkedTrackColor = sky.accent, checkedBorderColor = sky.accent,
    uncheckedThumbColor = sky.ink2, uncheckedTrackColor = sky.glass, uncheckedBorderColor = sky.hairline
)

@Composable
fun TabularText(text: String, fontSize: TextUnit, color: Color, fontFamily: FontFamily = InterFont, fontWeight: FontWeight = FontWeight.Normal) {
    val dw = with(LocalDensity.current) { (fontSize.toPx() * 0.6f).toDp() }
    val cw = with(LocalDensity.current) { (fontSize.toPx() * 0.32f).toDp() }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            text.forEach { ch ->
                Box(Modifier.width(if (ch.isDigit()) dw else cw), contentAlignment = Alignment.Center) {
                    Text(ch.toString(), fontSize = fontSize, color = color, fontFamily = fontFamily, fontWeight = fontWeight, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

@Composable
fun GlassCard(sky: Sky, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier.fillMaxWidth().clip(shape).background(sky.glass).border(0.75.dp, sky.hairline, shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp), content = content
    )
}

@Composable
fun SectionTitle(sky: Sky, text: String) {
    Text(text, color = sky.ink, fontSize = 18.sp, fontFamily = NotoKufi, fontWeight = FontWeight.Bold)
}

@Composable
fun GoldButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = Brand.GOLD, contentColor = Brand.ON_GOLD), shape = RoundedCornerShape(16.dp)) {
        Text(text, fontSize = 14.sp, maxLines = 1, softWrap = false, fontWeight = FontWeight.Bold, fontFamily = NotoKufi)
    }
}

@Composable
fun GlassButton(sky: Sky, text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp), border = BorderStroke(0.75.dp, sky.hairline), colors = ButtonDefaults.outlinedButtonColors(contentColor = sky.ink), shape = RoundedCornerShape(16.dp)) {
        Text(text, fontSize = 14.sp, maxLines = 1, softWrap = false, fontFamily = NotoSans)
    }
}

@Composable
fun ModeChip(sky: Sky, label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.width(112.dp).heightIn(min = 44.dp), contentPadding = PaddingValues(horizontal = 6.dp), border = BorderStroke(0.75.dp, sky.hairline), colors = ButtonDefaults.outlinedButtonColors(contentColor = sky.ink), shape = RoundedCornerShape(16.dp)) {
        Text(label, fontSize = 12.sp, maxLines = 1, softWrap = false, fontFamily = NotoSans)
    }
}

@Composable
fun StepButton(sky: Sky, text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.size(48.dp), contentPadding = PaddingValues(0.dp), border = BorderStroke(0.75.dp, sky.hairline), colors = ButtonDefaults.outlinedButtonColors(contentColor = sky.ink), shape = CircleShape) {
        Text(text, fontSize = 18.sp, fontFamily = InterFont)
    }
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
fun BrandNav(sky: Sky, labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val icons = listOf(Icons.Rounded.Home, Icons.Rounded.Explore, Icons.Rounded.Tune)
    val shape = RoundedCornerShape(32.dp)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).clip(shape).background(sky.glass).border(0.75.dp, sky.hairline, shape).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEachIndexed { i, l ->
            val on = i == selected
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).clickable { onSelect(i) }.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) {
                Icon(icons[i], contentDescription = l, tint = if (on) sky.accent else sky.ink2, modifier = Modifier.size(24.dp))
                Text(l, color = if (on) sky.accent else sky.ink2, fontSize = 12.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal, fontFamily = NotoKufi)
            }
        }
    }
}

@Composable
fun SkyBackdrop(sky: Sky, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        drawRect(Brush.verticalGradient(listOf(sky.top, sky.bottom)))
        val c = Offset(w / 2f, h * 0.42f)
        val glowR = w * 0.95f
        drawCircle(brush = Brush.radialGradient(listOf(sky.halo, Color.Transparent), center = c, radius = glowR), radius = glowR, center = c)
        for (k in 1..5) { drawCircle(sky.accent.copy(alpha = 0.05f), w * 0.17f * k, c, style = Stroke(1.dp.toPx())) }
    }
}
