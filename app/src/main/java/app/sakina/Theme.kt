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

// ───────────── الخطوط ─────────────
// (استخدمت Noto Kufi كبديل لـ Cairo إذا لم يكن مرفوعاً)
val NotoKufi = FontFamily(
    Font(R.font.noto_kufi_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_kufi_arabic_semibold, FontWeight.SemiBold),
    Font(R.font.noto_kufi_arabic_bold, FontWeight.Bold)
)
val NotoSans = FontFamily(
    Font(R.font.noto_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_sans_arabic_bold, FontWeight.Bold)
)
val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private fun sakinaTypography(): Typography {
    val b = Typography()
    fun TextStyle.k() = copy(fontFamily = NotoKufi)
    return b.copy(
        displayLarge = b.displayLarge.k(), displayMedium = b.displayMedium.k(), displaySmall = b.displaySmall.k(),
        headlineLarge = b.headlineLarge.k(), headlineMedium = b.headlineMedium.k(), headlineSmall = b.headlineSmall.k(),
        titleLarge = b.titleLarge.k(), titleMedium = b.titleMedium.k(), titleSmall = b.titleSmall.k(),
        bodyLarge = b.bodyLarge.k(), bodyMedium = b.bodyMedium.k(), bodySmall = b.bodySmall.k(),
        labelLarge = b.labelLarge.k(), labelMedium = b.labelMedium.k(), labelSmall = b.labelSmall.k()
    )
}

// ───────────── الألوان الجديدة (من مواصفاتك) ─────────────
@Immutable
class Sky(
    val top: Color,
    val bottom: Color,
    val accent: Color,
    val dark: Boolean,
    val ink: Color,
    val ink2: Color,
    val glass: Color,
    val halo: Color,
    val turquoise: Color
) {
    val hairline: Color get() = accent.copy(alpha = 0.25f)
}

val NAVY_DARK = Color(0xFF0B1630)
val NAVY_GRADIENT = Color(0xFF162A4A)
val GOLD = Color(0xFFD4AF5A)
val IVORY = Color(0xFFF7F2E8)
val TURQUOISE = Color(0xFF4FB3A8)
val CARD_NAVY = Color(0xFF172847)
val TEXT_SEC = Color(0xFFAAB7C8)

private fun darkSky(top: Long, bottom: Long, accent: Long, ink2: Long) =
    Sky(Color(top), Color(bottom), Color(accent), true, IVORY, Color(ink2), CARD_NAVY.copy(alpha = 0.85f), Color(accent).copy(alpha = 0.15f), TURQUOISE)

object Brand {
    val GOLD = Color(0xFFD4AF5A)
    val ON_GOLD = Color(0xFF0B1630) // نص كحلي على زر ذهبي
    val SKIES = listOf(
        darkSky(0xFF171D36, 0xFF162A4A, 0xFFD4AF5A, 0xFFAAB7C8),
        darkSky(0xFF0B1630, 0xFF162A4A, 0xFFD4AF5A, 0xFFAAB7C8),
        darkSky(0xFF0B1630, 0xFF162A4A, 0xFFD4AF5A, 0xFFAAB7C8),
        darkSky(0xFF0B1630, 0xFF162A4A, 0xFFD4AF5A, 0xFFAAB7C8),
        darkSky(0xFF7D4347, 0xFF2B2238, 0xFFE8B468, 0xFFE4DCD0),
        darkSky(0xFF0B1630, 0xFF0B1630, 0xFFD4AF5A, 0xFFAAB7C8)
    )
    val AMOLED = Sky(Color(0xFF000000), Color(0xFF000000), GOLD, true, IVORY, Color(0xFF8A8A8A), Color(0x14FFFFFF), Color(0x1AD4AF37), TURQUOISE)
}

private fun Sky.toColorScheme(): ColorScheme = darkColorScheme(
    primary = accent, onPrimary = Brand.ON_GOLD, secondary = turquoise,
    surface = glass, onSurface = ink, outline = hairline,
    background = NAVY_DARK, onBackground = ink
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
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.fillMaxWidth().clip(shape).background(sky.glass).border(0.75.dp, sky.hairline, shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp), content = content
    )
}

@Composable
fun SectionTitle(sky: Sky, text: String) {
    Text(text, color = sky.ink, fontSize = 18.sp, fontFamily = NotoKufi, fontWeight = FontWeight.SemiBold)
}

@Composable
fun GoldButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick, 
        modifier = modifier.heightIn(min = 44.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Brand.GOLD, contentColor = Brand.ON_GOLD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = NotoKufi)
    }
}

@Composable
fun GlassButton(sky: Sky, text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, 
        modifier = modifier.heightIn(min = 44.dp), 
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        border = BorderStroke(0.75.dp, sky.hairline), 
        colors = ButtonDefaults.outlinedButtonColors(contentColor = sky.ink),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, fontSize = 14.sp, fontFamily = NotoKufi)
    }
}

@Composable
fun ModeChip(sky: Sky, label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, modifier = Modifier.width(100.dp).heightIn(min = 40.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        border = BorderStroke(0.75.dp, sky.hairline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = sky.ink),
        shape = RoundedCornerShape(12.dp)
    ) { Text(label, fontSize = 12.sp, maxLines = 1, softWrap = false, fontFamily = NotoKufi) }
}

@Composable
fun StepButton(sky: Sky, text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, modifier = Modifier.size(40.dp),
        contentPadding = PaddingValues(0.dp), border = BorderStroke(0.75.dp, sky.hairline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = sky.ink),
        shape = CircleShape
    ) { Text(text, fontSize = 18.sp, fontFamily = InterFont) }
}

fun modeIcon(mode: Int): ImageVector = when (mode) {
    0 -> Icons.Rounded.NotificationsActive
    1 -> Icons.Rounded.VolumeUp
    2 -> Icons.Rounded.Vibration
    else -> Icons.Rounded.NotificationsOff
}

@Composable
fun ModeIconButton(sky: Sky, mode: Int, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(modeIcon(mode), contentDescription = description, tint = sky.accent, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun BrandNav(sky: Sky, labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val icons = listOf(Icons.Rounded.Home, Icons.Rounded.Explore, Icons.Rounded.Tune, Icons.Rounded.Book, Icons.Rounded.Star, Icons.Rounded.DateRange)
    val shape = RoundedCornerShape(24.dp)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(shape).background(sky.glass).border(0.75.dp, sky.hairline, shape).padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        labels.forEachIndexed { i, l ->
            val on = i == selected
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).clickable { onSelect(i) }.padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) {
                // إذا كانت الأيقونة من Material
                if (i < icons.size) {
                    Icon(icons[i], contentDescription = l, tint = if (on) sky.accent else sky.ink2, modifier = Modifier.size(22.dp))
                } else {
                    // يمكنك إضافة أيقونات مخصصة هنا
                }
                Text(l, color = if (on) sky.accent else sky.ink2, fontSize = 11.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal, fontFamily = NotoKufi)
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
