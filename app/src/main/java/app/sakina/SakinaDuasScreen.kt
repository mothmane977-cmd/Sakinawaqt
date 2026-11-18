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

private val DUAS_BY_CAT = listOf(
    listOf(
        "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ." to "البقرة: 201",
        "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً." to "آل عمران: 8",
        "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي." to "طه: 25-26",
        "لَا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ." to "الأنبياء: 87"
    ),
    listOf(
        "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى." to "صحيح مسلم (2721)",
        "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ." to "سنن الترمذي (2140)",
        "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ." to "سنن أبي داود (1522)"
    ),
    listOf(
        "لَا إِلَٰهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَٰهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ." to "متفق عليه",
        "اللَّهُمَّ رَحْمَتَكَ أَرْجُو فَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ." to "سنن أبي داود (5090)",
        "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ." to "صحيح البخاري (4563)"
    ),
    listOf(
        "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ." to "سنن الترمذي (3563)",
        "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْماً نَافِعاً، وَرِزْقاً طَيِّباً، وَعَمَلاً مُتَقَبَّلاً." to "سنن ابن ماجه (925)"
    )
)

@Composable
fun SakinaDuasScreen(onBackToHome: () -> Unit, modifier: Modifier = Modifier) {
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
                    text = if (selectedIdx != null) s.duaCatTitles[selectedIdx!!] else s.duasTitle,
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
                s.duaCatTitles.forEachIndexed { idx, title ->
                    SakinaGlassCard(modifier = Modifier.fillMaxWidth().clickable { selectedIdx = idx }, glowGold = true) {
                        Text(title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = theme.accentBright)
                        Text(s.duaCatSubs[idx], fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.SoftSilver)
                    }
                }
            } else {
                DUAS_BY_CAT[selectedIdx!!].forEach { (text, source) ->
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
