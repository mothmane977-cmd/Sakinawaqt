package app.sakina

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RuqyahItem(val heading: String, val content: String, val note: String)
data class RuqyahCategory(val title: String, val subtitle: String, val items: List<RuqyahItem>)

private val RUQYAH_CATEGORIES = listOf(
    RuqyahCategory(
        "الرقية الشرعية الشاملة",
        "الفاتحة وآية الكرسي وخواتيم البقرة والمعوذات",
        listOf(
            RuqyahItem("سورة الفاتحة (سبع مرات)", "﴿ بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ * الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ * الرَّحْمَٰنِ الرَّحِيمِ * مَالِكِ يَوْمِ الدِّينِ * إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ * اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ * صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ ﴾.", "أعظم سورة في القرآن - البخاري (5007)"),
            RuqyahItem("آية الكرسي", "﴿ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ ﴾.", "البقرة: 255"),
            RuqyahItem("خواتيم سورة البقرة", "﴿ آمَنَ الرَّسُولُ بِمَا أُنْزِلَ إِلَيْهِ مِنْ رَبِّهِ وَالْمُؤْمِنُونَ كُلٌّ آمَنَ بِاللَّهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ لَا نُفَرِّقُ بَيْنَ أَحَدٍ مِنْ رُسُلِهِ وَقَالُوا سَمِعْنَا وَأَطَعْنَا غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيرُ * لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ... ﴾.", "من قرأ بالآيتين من آخر سورة البقرة في ليلة كفتاه - متفق عليه")
        )
    ),
    RuqyahCategory(
        "رقية السكينة والطمأنينة",
        "لراحة القلب والسكينة والرقاد",
        listOf(
            RuqyahItem("آية السكينة", "﴿ هُوَ الَّذِي أَنْزَلَ السَّكِينَةَ فِي قُلُوبِ الْمُؤْمِنِينَ لِيَزْدَادُوا إِيمَانًا مَعَ إِيمَانِهِمْ ﴾.", "الفتح: 4"),
            RuqyahItem("اطمئنان القلوب بذكر الله", "﴿ الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُمْ بِذِكْرِ اللَّهِ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾.", "الرعد: 28")
        )
    ),
    RuqyahCategory(
        "رقية التحصين من العين والحسد",
        "التحصين النبوي المأثور في الصحيحين",
        listOf(
            RuqyahItem("تعويذ إبراهيم وإسماعيل وإسحاق", "«أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّةِ، مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ، وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ».", "رواه البخاري (3371)"),
            RuqyahItem("رقية جبريل للنبي ﷺ", "«بِاسْمِ اللَّهِ أَرْقِيكَ، مِنْ كُلِّ شَيْءٍ يُؤْذِيكَ، مِنْ شَرِّ كُلِّ نَفْسٍ أَوْ عَيْنِ حَاسِدٍ، اللَّهُ يَشْفِيكَ، بِاسْمِ اللَّهِ أَرْقِيكَ».", "رواه مسلم (2186)")
        )
    ),
    RuqyahCategory(
        "رقية الشفاء",
        "توضع اليد على موضع الألم مع الدعاء المأثور",
        listOf(
            RuqyahItem("الدعاء على موضع الوجع", "ضع يدك على الذي تألم من جسدك وقل: «بِاسْمِ اللَّهِ» ثلاثاً، وقل سبع مرات: «أَعُوذُ بِاللَّهِ وَقُدْرَتِهِ مِنْ شَرِّ مَا أَجِدُ وَأُحَاذِرُ».", "رواه مسلم (2202)"),
            RuqyahItem("دعاء رب الناس مذهب البأس", "«اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَأْسَ، اشْفِهِ وَأَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا».", "متفق عليه - البخاري (5743) ومسلم (2191)")
        )
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
            .padding(start = 16.dp, end = 16.dp, top = 50.dp, bottom = 74.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SakinaBackIconButton(
                onClick = { if (selectedIdx != null) selectedIdx = null else onBackToHome() },
                modifier = Modifier.align(Alignment.CenterStart)
            )
            SakinaOrnamentCartouche(
                modifier = Modifier.align(Alignment.Center),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 7.dp)
            ) {
                Text(
                    text = selectedIdx?.let { s.ruqyahCatTitles[it] } ?: s.ruqyahTitle,
                    style = TextStyle(
                        fontFamily = SakinaEditorialFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        brush = SakinaCalligraphyBrush()
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedIdx == null) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                RUQYAH_CATEGORIES.forEachIndexed { idx, cat ->
                    SakinaGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIdx = idx },
                        cornerRadius = 16.dp,
                        glowGold = idx == 0,
                        contentPadding = PaddingValues(13.dp)
                    ) {
                        Text(
                            text = s.ruqyahCatTitles[idx],
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = theme.accentBright
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = s.ruqyahCatSubs[idx],
                            fontFamily = SakinaBodyFont,
                            fontSize = 12.sp,
                            color = SakinaBrandColors.SoftSilver
                        )
                    }
                }
            }
        } else {
            val cat = RUQYAH_CATEGORIES[selectedIdx!!]
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cat.items) { r ->
                    SakinaParchmentPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = "❖ ${r.heading} ❖".toWesternDigits(),
                            fontFamily = SakinaNaskhFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F3D3E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = r.content.toWesternDigits(),
                            fontFamily = SakinaNaskhFontFamily,
                            fontSize = 18.sp,
                            lineHeight = 32.sp,
                            color = SakinaInkDark,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = r.note.toWesternDigits(),
                            fontFamily = SakinaNaskhFontFamily,
                            fontSize = 11.5.sp,
                            color = Color(0xFF6B5224),
                            textAlign = TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
