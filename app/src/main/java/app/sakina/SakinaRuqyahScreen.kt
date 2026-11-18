package app.sakina

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RUQYAH_BY_CAT = listOf(
    listOf(
        "سورة الفاتحة (7 مرات)." to "سورة الفاتحة",
        "آية الكرسي: اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ..." to "البقرة: 255",
        "المعوذات: الإخلاص والفلق والناس (3 مرات)." to "سنن أبي داود"
    ),
    listOf(
        "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ." to "الرعد: 28",
        "هُوَ الَّذِي أَنزَلَ السَّكِينَةَ فِي قُلُوبِ الْمُؤْمِنِينَ لِيَزْدَادُوا إِيمَاناً مَّعَ إِيمَانِهِمْ." to "الفتح: 4",
        "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ غَضَبِهِ وَعِقَابِهِ وَشَرِّ عِبَادِهِ." to "سنن الترمذي"
    ),
    listOf(
        "وَإِن يَكَادُ الَّذِينَ كَفَرُوا لَيُزْلِقُونَكَ بِأَبْصَارِهِمْ لَمَّا سَمِعُوا الذِّكْرَ." to "القلم: 51",
        "أُعِيذُكَ بِكَلِمَاتِ اللَّهِ التَّامَّةِ مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ." to "صحيح البخاري (3371)"
    ),
    listOf(
        "بسم الله (3 مرات)، أعوذ بالله وقدرته من شر ما أجد وأحاذر (7 مرات)." to "صحيح مسلم (2202)",
        "اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَاسَ، اشْفِهِ وَأَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ." to "صحيح البخاري (5743)"
    )
)

@Composable
fun SakinaRuqyahScreen(onBackToHome: () -> Unit, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    var selectedIdx by remember { mutableStateOf<Int?>(null) }

    BackHandler {
        if (selectedIdx != null) selectedIdx = null else onBackToHome()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 54.dp, bottom = 78.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SakinaBackIconButton(onClick = { if (selectedIdx != null) selectedIdx = null else onBackToHome() })
            SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 32.dp, vertical = 6.dp)) {
                Text(
                    text = if (selectedIdx != null) s.ruqyahCatTitles[selectedIdx!!] else s.ruqyahTitle,
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = theme.accentBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.size(32.dp))
        }

        Spacer(Modifier.height(10.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            if (selectedIdx == null) {
                s.ruqyahCatTitles.forEachIndexed { idx, title ->
                    SakinaGlassCard(modifier = Modifier.fillMaxWidth().clickable { selectedIdx = idx }, glowGold = true) {
                        Text(title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = theme.accentBright)
                        Text(s.ruqyahCatSubs[idx], fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.SoftSilver)
                    }
                }
            } else {
                RUQYAH_BY_CAT[selectedIdx!!].forEach { (text, source) ->
                    SakinaParchmentPanel(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                        Text(text.toWesternDigits(), fontFamily = SakinaEditorialFont, fontSize = 16.sp, lineHeight = 28.sp, color = SakinaInkDark)
                        Spacer(Modifier.height(4.dp))
                        Text(source.toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 10.5.sp, color = Color(0xFF6B5224))
                    }
                }
            }
        }
    }
}
