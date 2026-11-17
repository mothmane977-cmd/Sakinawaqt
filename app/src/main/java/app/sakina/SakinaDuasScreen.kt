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

private data class DuaCategory(val title: String, val subtitle: String, val duas: List<Pair<String, String>>)

private val DUA_CATEGORIES = listOf(
    DuaCategory("أدعية قرآنية", "جوامع الدعاء من كتاب الله", listOf(
        "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ." to "سورة البقرة: 201",
        "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً." to "سورة آل عمران: 8",
        "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي." to "سورة طه: 25-26",
        "لَا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ." to "سورة الأنبياء: 87"
    )),
    DuaCategory("أدعية نبوية جامعة", "من أصح ما دعا به النبي ﷺ", listOf(
        "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى." to "رواه مسلم (2721)",
        "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ." to "رواه الترمذي (2140)",
        "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ." to "رواه أبو داود (1522)"
    )),
    DuaCategory("الكرب والهم", "لتفريج الهموم والضيق", listOf(
        "لَا إِلَٰهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَٰهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ." to "متفق عليه",
        "اللَّهُمَّ رَحْمَتَكَ أَرْجُو فَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ." to "رواه أبو داود (5090)",
        "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ." to "رواه البخاري (4563)"
    )),
    DuaCategory("الرزق والبركة", "طلب الرزق الحلال وقضاء الدين", listOf(
        "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ." to "رواه الترمذي (3563)",
        "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْماً نَافِعاً، وَرِزْقاً طَيِّباً، وَعَمَلاً مُتَقَبَّلاً." to "رواه ابن ماجه (925)"
    ))
)

@Composable
fun SakinaDuasScreen(onBackToHome: () -> Unit, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    var selected by remember { mutableStateOf<DuaCategory?>(null) }

    BackHandler {
        if (selected != null) selected = null else onBackToHome()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 66.dp, bottom = 82.dp),
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
                Text(if (selected != null) "الأقسام" else "رجوع", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = theme.accentBright)
            }
            SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 5.dp)) {
                Text(selected?.title ?: "الأدعية المصنفة", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = theme.accentBright)
            }
            Spacer(Modifier.width(44.dp))
        }

        Spacer(Modifier.height(12.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (selected == null) {
                DUA_CATEGORIES.forEach { cat ->
                    SakinaGlassCard(modifier = Modifier.fillMaxWidth().clickable { selected = cat }, glowGold = true) {
                        Text(cat.title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = theme.accentBright)
                        Text(cat.subtitle, fontFamily = SakinaBodyFont, fontSize = 12.sp, color = SakinaBrandColors.SoftSilver)
                    }
                }
            } else {
                selected!!.duas.forEach { (text, source) ->
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
