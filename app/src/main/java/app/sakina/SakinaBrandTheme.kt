package app.sakina

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp

object SakinaBrandColors {
    val MidnightMihrab = Color(0xFF0A0E12)
    val SakinaSurface = Color(0xFF131B22)
    val GlassTop = Color(0xC71B2531)
    val GlassBottom = Color(0xE60C1118)
    val RoyalEmerald = Color(0xFF0F3D3E)
    val ManuscriptGold = Color(0xFFD4AF37)
    val GoldBright = Color(0xFFF3DC8B)
    val MutedGold = Color(0xFFB89F5D)
    val LuminousIvory = Color(0xFFFAF8F5)
    val SoftSilver = Color(0xFFA7B0BA)
    val ActiveRowMidnight = Color(0xFF11223B)
    val StableEmerald = Color(0xFF4ADE80)
    val DividerGold = Color(0x33D4AF37)
}

val SakinaHeadingFont = FontFamily(
    Font(R.font.noto_kufi_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_kufi_arabic_bold, FontWeight.Bold)
)

val SakinaBodyFont = FontFamily(
    Font(R.font.noto_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_sans_arabic_bold, FontWeight.Bold)
)

val SakinaEditorialFont = FontFamily(
    Font(R.font.noto_naskh_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_naskh_arabic_bold, FontWeight.Bold)
)

val SakinaNumeralFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private val SakinaDarkScheme = darkColorScheme(
    primary = SakinaBrandColors.ManuscriptGold,
    onPrimary = SakinaBrandColors.MidnightMihrab,
    secondary = SakinaBrandColors.RoyalEmerald,
    onSecondary = SakinaBrandColors.LuminousIvory,
    background = SakinaBrandColors.MidnightMihrab,
    onBackground = SakinaBrandColors.LuminousIvory,
    surface = SakinaBrandColors.SakinaSurface,
    onSurface = SakinaBrandColors.LuminousIvory,
    surfaceVariant = Color(0xFF18222C),
    onSurfaceVariant = SakinaBrandColors.SoftSilver
)

private val SakinaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SakinaNumeralFont,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        fontFeatureSettings = "tnum",
        color = SakinaBrandColors.GoldBright
    ),
    titleLarge = TextStyle(
        fontFamily = SakinaHeadingFont,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = SakinaBrandColors.LuminousIvory
    ),
    bodyMedium = TextStyle(
        fontFamily = SakinaBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = SakinaBrandColors.LuminousIvory
    )
)

@Composable
fun SakinaBrandTheme(
    isRtl: Boolean = true,
    content: @Composable () -> Unit
) {
    val direction = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        MaterialTheme(
            colorScheme = SakinaDarkScheme,
            typography = SakinaTypography,
            content = content
        )
    }
}
