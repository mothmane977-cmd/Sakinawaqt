package app.sakina

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp

enum class SakinaLuxuryTheme(
    val titleAr: String,
    val bgDeep: Color,
    val bgCard: Color,
    val accentGold: Color,
    val accentBright: Color,
    val parchmentTop: Color,
    val parchmentBottom: Color
) {
    ROYAL_NAVY(
        "الأزرق الملكي",
        Color(0xFF071422), Color(0xFF0B1C2E),
        Color(0xFFC5A059), Color(0xFFF0DAA0),
        Color(0xFFF9F4E8), Color(0xFFEDE3CC)
    ),
    ROYAL_EMERALD(
        "الزمردي الحرمي",
        Color(0xFF051A19), Color(0xFF0B2B29),
        Color(0xFFD4AF37), Color(0xFFF5E1A8),
        Color(0xFFF7F4EA), Color(0xFFE9E2CE)
    ),
    MIDNIGHT_AMOLED(
        "الأسود AMOLED",
        Color(0xFF000000), Color(0xFF10141A),
        Color(0xFFD4AF37), Color(0xFFFFE7A3),
        Color(0xFFF6F2E9), Color(0xFFE7DEC9)
    ),
    ANDALUSIAN_BURGUNDY(
        "العنّابي الأندلسي",
        Color(0xFF1A0911), Color(0xFF2B101C),
        Color(0xFFD9A773), Color(0xFFF7D9B4),
        Color(0xFFFAF2EA), Color(0xFFEEE0D0)
    ),
    DESERT_AMBER(
        "الكهرماني المكي",
        Color(0xFF181108), Color(0xFF281D0F),
        Color(0xFFD4A24C), Color(0xFFF6D99C),
        Color(0xFFFAF4E6), Color(0xFFEDE0C8)
    )
}

val LocalSakinaTheme = compositionLocalOf { SakinaLuxuryTheme.ROYAL_NAVY }

object SakinaBrandColors {
    val MidnightMihrab = Color(0xFF071422)
    val SakinaSurface = Color(0xFF0B1C2E)
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

val SakinaEditorialFont = FontFamily(
    Font(R.font.noto_naskh_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_naskh_arabic_bold, FontWeight.Bold)
)

val SakinaHeadingFont = SakinaEditorialFont
val SakinaBodyFont = SakinaEditorialFont

val SakinaNumeralFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private val SakinaTypography = Typography(
    displayLarge = TextStyle(fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 34.sp, fontFeatureSettings = "tnum", color = SakinaBrandColors.GoldBright),
    titleLarge = TextStyle(fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SakinaBrandColors.LuminousIvory),
    bodyMedium = TextStyle(fontFamily = SakinaBodyFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, color = SakinaBrandColors.LuminousIvory)
)

@Composable
fun SakinaBrandTheme(
    theme: SakinaLuxuryTheme = SakinaLuxuryTheme.ROYAL_NAVY,
    isRtl: Boolean = true,
    content: @Composable () -> Unit
) {
    val direction = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val scheme = darkColorScheme(
        primary = theme.accentGold,
        onPrimary = theme.bgDeep,
        secondary = SakinaBrandColors.RoyalEmerald,
        onSecondary = SakinaBrandColors.LuminousIvory,
        background = theme.bgDeep,
        onBackground = SakinaBrandColors.LuminousIvory,
        surface = theme.bgCard,
        onSurface = SakinaBrandColors.LuminousIvory
    )
    CompositionLocalProvider(
        LocalLayoutDirection provides direction,
        LocalSakinaTheme provides theme
    ) {
        MaterialTheme(colorScheme = scheme, typography = SakinaTypography, content = content)
    }
}
