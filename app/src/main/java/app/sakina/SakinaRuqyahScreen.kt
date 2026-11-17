package app.sakina

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class RuqyahCategory(val title: String, val need: String, val items: List<Pair<String, String>>)

private val RUQYAH_CATEGORIES = listOf(
    RuqyahCategory("الرقية العامة الشاملة", "التحصين الشامل من القرآن والسنة", listOf(
        "سورة الفاتحة (7 مرات)." to "سورة الفاتحة",
        "آية الكرسي: اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ..." to "البقرة: 255",
        "المعوذات: الإخلاص والفلق والناس (3 مرات)." to "سنن أبي داود"
    )),
    RuqyahCategory("رقية السكينة والطمأنينة", "لعلاج القلق والضيق والأرق", listOf(
        "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ." to "الرعد: 28",
        "هُوَ الَّذِي أَنزَلَ السَّكِينَةَ فِي قُلُوبِ الْمُؤْمِنِينَ لِيَزْدَادُوا إِيمَاناً مَّعَ إِيمَانِهِمْ." to "الفتح: 4",
        "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ غَضَبِهِ وَعِقَابِهِ وَشَرِّ عِبَادِهِ." to "رواه الترمذي"
    )),
    RuqyahCategory("رقية العين والحسد", "لدفع الأذى والعين بإذن الله", listOf(
        "وَإِن يَكَادُ الَّذِينَ كَفَرُوا لَيُزْلِقُونَكَ بِأَبْصَارِهِمْ لَمَّا سَمِعُوا الذِّكْرَ." to "القلم: 51",
        "أُعِيذُكَ بِكَلِمَاتِ اللَّهِ التَّامَّةِ مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ." to "رواه البخاري (3371)"
    )),
    RuqyahCategory("رقية الشفاء والألم", "تُقرأ مع وضع اليد على موضع الألم", listOf(
        "بسم الله (3 مرات)، أعوذ بالله وقدرته من شر ما أجد وأحاذر (7 مرات)." to "رواه مسلم (2202)",
        "اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَاسَ، اشْفِهِ وَأَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ." to "رواه البخاري (5743)"
    ))
)

@Composable
fun SakinaRuqyahScreen(onBackToHome: () -> Unit, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    var selected by remember { mutableStateOf<RuqyahCategory?>(null) }

    BackHandler {
        if (selected != null) selected = null else onBackToHome()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 74.dp, bottom = 82.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.bgCard)
                    .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(16.dp))
                    .clickable { if (selected != null) selected = null else onBackToHome() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (selected != null) "الأنواع" else "رجوع", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = theme.accentBright)
            }
            SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 5.dp)) {
                Text(selected?.title ?: "الرقية الشرعية", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = theme.accentBright)
            }
            Spacer(Modifier.width(44.dp))
        }

        Spacer(Modifier.height(12.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (selected == null) {
                RUQYAH_CATEGORIES.forEach { cat ->
                    SakinaGlassCard(modifier = Modifier.fillMaxWidth().clickable { selected = cat }, glowGold = true) {
                        Text(cat.title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = theme.accentBright)
                        Text(cat.need, fontFamily = SakinaBodyFont, fontSize = 12.sp, color = SakinaBrandColors.SoftSilver)
                    }
                }
            } else {
                selected!!.items.forEach { (text, source) ->
                    SakinaParchmentPanel(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                        Text(text.toWesternDigits(), fontFamily = SakinaEditorialFont, fontSize = 16.5.sp, lineHeight = 29.sp, color = SakinaInkDark)
                        Spacer(Modifier.height(4.dp))
                        Text(source.toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 11.sp, color = Color(0xFF6B5224))
                    }
                }
            }
        }
    }
}
