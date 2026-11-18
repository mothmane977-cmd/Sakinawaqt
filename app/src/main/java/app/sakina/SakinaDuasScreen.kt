package app.sakina

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DuaCardItem(val title: String, val text: String, val source: String)
data class DuaCategory(val title: String, val subtitle: String, val items: List<DuaCardItem>)

private val DUA_CATEGORIES = listOf(
    DuaCategory(
        "الأدعية القرآنية الجامعة",
        "ربنا آتنا في الدنيا حسنة وفي الآخرة حسنة",
        listOf(
            DuaCardItem("جوامع الخير في الدارين", "﴿ رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ ﴾", "البقرة: 201"),
            DuaCardItem("الثبات على الهداية", "﴿ رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِنْ لَدُنْكَ رَحْمَةً إِنَّكَ أَنْتَ الْوَهَّابُ ﴾", "آل عمران: 8"),
            DuaCardItem("التيسير وانشراح الصدر", "﴿ رَبِّ اشْرَحْ لِي صَدْرِي * وَيَسِّرْ لِي أَمْرِي ﴾", "طه: 25-26"),
            DuaCardItem("دعاء ذي النون لتفريج الكرب", "﴿ لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ ﴾", "الأنبياء: 87")
        )
    ),
    DuaCategory(
        "الأدعية النبوية المأثورة",
        "جوامع الدعاء الثابتة في الصحيحين",
        listOf(
            DuaCardItem("الهدى والتقى والعفاف والغنى", "«اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى».", "رواه مسلم (2721)"),
            DuaCardItem("يا مقلب القلوب", "«يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ».", "رواه الترمذي (2140)"),
            DuaCardItem("العفو والعافية في الدنيا والآخرة", "«اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي وَآمِنْ رَوْعَاتِي».", "رواه أبو داود (5074)"),
            DuaCardItem("إصلاح الدين والدنيا والآخرة", "«اللَّهُمَّ أَصْلِحْ لِي دِينِي الَّذِي هُوَ عِصْمَةُ أَمْرِي، وَأَصْلِحْ لِي دُنْيَايَ الَّتِي فِيهَا مَعَاشِي، وَأَصْلِحْ لِي آخِرَتِي الَّتِي فِيهَا مَعَادِي».", "رواه مسلم (2720)")
        )
    ),
    DuaCategory(
        "أدعية تفريج الكرب والهمّ والدَّين",
        "دعاء الكرب العظيم وأدعية قضاء الدين",
        listOf(
            DuaCardItem("دعاء الكرب العظيم", "«لَا إِلَٰهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَٰهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ، لَا إِلَٰهَ إِلَّا اللَّهُ رَبُّ السَّمَاوَاتِ وَرَبُّ الْأَرْضِ وَرَبُّ الْعَرْشِ الْكَرِيمِ».", "متفق عليه - البخاري (6346) ومسلم (2730)"),
            DuaCardItem("دعاء قضاء الدَّين والغلبة", "«اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ، وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ».", "رواه الترمذي (3563)"),
            DuaCardItem("الاستعاذة من الهم والحزن", "«اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ».", "رواه البخاري (6369)")
        )
    ),
    DuaCategory(
        "أدعية الوالدين والأهل والذرية",
        "رب ارحمهما كما ربياني صغيراً",
        listOf(
            DuaCardItem("بر الوالدين والترحم عليهما", "﴿ رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا ﴾", "الإسراء: 24"),
            DuaCardItem("صلاح الأهل والذرية", "﴿ رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا ﴾", "الفرقان: 74"),
            DuaCardItem("المغفرة للوالدين والمؤمنين", "﴿ رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ ﴾", "إبراهيم: 41")
        )
    ),
    // #21: Travel Supplication (دعاء السفر والركوب)
    DuaCategory(
        "دعاء السفر والركوب",
        "سبحان الذي سخر لنا هذا وما كنا له مقرنين",
        listOf(
            DuaCardItem(
                "دعاء السفر الكامل المأثور",
                "«اللهُ أَكْبَرُ، اللهُ أَكْبَرُ، اللهُ أَكْبَرُ، ﴿ سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ * وَإِنَّا إِلَىٰ رَبِّنَا لَمُنْقَلِبُونَ ﴾، اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَٰذَا الْبِرَّ وَالتَّقْوَىٰ، وَمِنَ الْعَمَلِ مَا تَرْضَىٰ، اللَّهُمَّ هَوِّنْ عَلَيْنَا سَفَرَنَا هَٰذَا وَاطْوِ عَنَّا بُعْدَهُ، اللَّهُمَّ أَنْتَ الصَّاحِبُ فِي السَّفَرِ، وَالْخَلِيفَةُ فِي الْأَهْلِ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ وَعْثَاءِ السَّفَرِ، وَكَآبَةِ الْمَنْظَرِ، وَسُوءِ الْمُنْقَلَبِ فِي الْمَالِ وَالْأَهْلِ».",
                "رواه مسلم في صحيحه (1342) عن ابن عمر رضي الله عنهما"
            ),
            DuaCardItem(
                "ما يزاد عند الرجوع من السفر",
                "وإذا رجع قالهن وزاد فيهن: «آيِبُونَ، تَائِبُونَ، عَابِدُونَ، لِرَبِّنَا حَامِدُونَ».",
                "رواه مسلم (1342)"
            ),
            DuaCardItem(
                "دعاء نزول المنزل في السفر أو غيره",
                "«أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ» (من نزل منزلاً فقالها لم يضره شيء حتى يرتحل من منزله ذلك).",
                "رواه مسلم (2708)"
            ),
            DuaCardItem(
                "دعاء دخول القرية أو البلدة",
                "«اللَّهُمَّ رَبَّ السَّمَاوَاتِ السَّبْعِ وَمَا أَظْلَلْنَ، وَرَبَّ الْأَرَضِينَ السَّبْعِ وَمَا أَقْلَلْنَ، وَرَبَّ الشَّيَاطِينِ وَمَا أَضْلَلْنَ، وَرَبَّ الرِّيَاحِ وَمَا ذَرَيْنَ، أَسْأَلُكَ خَيْرَ هَٰذِهِ الْقَرْيَةِ وَخَيْرَ أَهْلِهَا وَخَيْرَ مَا فِيهَا، وَأَعُوذُ بِكَ مِنْ شَرِّهَا وَشَرِّ أَهْلِهَا وَشَرِّ مَا فِيهَا».",
                "رواه النسائي في الكبرى والحاكم وصححه"
            )
        )
    )
)

// #23: Sub-tab "حصن المسلم" (Authentic chapters from Hisn al-Muslim)
private val HISN_MUSLIM_CATEGORIES = listOf(
    DuaCategory(
        "1. أذكار الاستيقاظ ولبس الثوب",
        "الحمد لله الذي أحيانا بعد ما أماتنا وإليه النشور",
        listOf(
            DuaCardItem("ذكر الاستيقاظ من النوم", "«الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ».", "رواه البخاري (6312)"),
            DuaCardItem("دعاء لبس الثوب", "«الْحَمْدُ لِلَّهِ الَّذِي كَسَانِي هَٰذَا (الثَّوْبَ) وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ».", "رواه أبو داود (4023) والترمذي")
        )
    ),
    DuaCategory(
        "2. أذكار الخلاء والوضوء والأذان",
        "غفرانك · أشهد أن لا إله إلا الله",
        listOf(
            DuaCardItem("دخول الخلاء والخروج منه", "عند الدخول: «بِسْمِ اللَّهِ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ». وعند الخروج: «غُفْرَانَكَ».", "متفق عليه وأبو داود"),
            DuaCardItem("الذكر بعد الفراغ من الوضوء", "«أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ، اللَّهُمَّ اجْعَلْنِي مِنَ التَّوَّابِينَ وَاجْعَلْنِي مِنَ الْمُتَطَهِّرِينَ».", "رواه مسلم (234) والترمذي (55)"),
            DuaCardItem("الذكر عقب الأذان", "«اللَّهُمَّ رَبَّ هَٰذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ».", "رواه البخاري (614)")
        )
    ),
    DuaCategory(
        "3. أدعية الاستفتاح والركوع والسجود",
        "سبحانك اللهم وبحمدك وتبارك اسمك",
        listOf(
            DuaCardItem("دعاء الاستفتاح", "«سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَىٰ جَدُّكَ، وَلَا إِلَٰهَ غَيْرُكَ».", "رواه أبو داود والترمذي"),
            DuaCardItem("الدعاء بين السجدتين", "«رَبِّ اغْفِرْ لِي، رَبِّ اغْفِرْ لِي، اللَّهُمَّ اغْفِرْ لِي وَارْحَمْنِي وَاهْدِنِي وَاجْبُرْنِي وَعَافِنِي وَارْزُقْنِي وَارْفَعْنِي».", "رواه أبو داود والترمذي"),
            DuaCardItem("الدعاء قبل السلام من الصلاة", "«اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عَذَابِ جَهَنَّمَ، وَمِنْ عَذَابِ الْقَبْرِ، وَمِنْ فِتْنَةِ الْمَحْيَا وَالْمَمَاتِ، وَمِنْ شَرِّ فِتْنَةِ الْمَسِيحِ الدَّجَّالِ».", "رواه مسلم (588)")
        )
    ),
    DuaCategory(
        "4. دعاء الاستخارة ودعاء القنوت",
        "اللهم إني أستخيرك بعلمك وأستقدرك بقدرتك",
        listOf(
            DuaCardItem("دعاء صلاة الاستخارة", "«اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ؛ فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الْغُيُوبِ، اللَّهُمَّ إِنْ كُنْتَ تَعْلَمُ أَنَّ هَٰذَا الْأَمْرَ خَيْرٌ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي فَاقْدُرْهُ لِي وَيَسِّرْهُ لِي ثُمَّ بَارِكْ لِي فِيهِ، وَإِنْ كُنْتَ تَعْلَمُ أَنَّ هَٰذَا الْأَمْرَ شَرٌّ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي فَاصْرِفْهُ عَنِّي وَاصْرِفْنِي عَنْهُ، وَاقْدُرْ لِيَ الْخَيْرَ حَيْثُ كَانَ ثُمَّ أَرْضِنِي بِهِ».", "رواه البخاري (1162)"),
            DuaCardItem("دعاء قنوت الوتر", "«اللَّهُمَّ اهْدِنِي فِيمَنْ هَدَيْتَ، وَعَافِنِي فِيمَنْ عَافَيْتَ، وَتَوَلَّنِي فِيمَنْ تَوَلَّيْتَ، وَبَارِكْ لِي فِيمَا أَعْطَيْتَ، وَقِنِي شَرَّ مَا قَضَيْتَ؛ فَإِنَّكَ تَقْضِي وَلَا يُقْضَىٰ عَلَيْكَ، وَإِنَّهُ لَا يَذِلُّ مَنْ وَالَيْتَ، وَلَا يَعِزُّ مَنْ عَادَيْتَ، تَبَارَكْتَ رَبَّنَا وَتَعَالَيْتَ».", "رواه أبو داود والترمذي والنسائي")
        )
    ),
    DuaCategory(
        "5. أذكار الطعام والضيافة والمجلس",
        "الحمد لله الذي أطعمني هذا ورزقنيه · كفارة المجلس",
        listOf(
            DuaCardItem("الذكر قبل الطعام وبعده", "قبل الطعام: «بِسْمِ اللَّهِ» فإن نسي قال: «بِسْمِ اللَّهِ أَوَّلَهُ وَآخِرَهُ». وبعده: «الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَٰذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ».", "رواه أبو داود والترمذي"),
            DuaCardItem("دعاء الضيف لأهل الطعام", "«اللَّهُمَّ بَارِكْ لَهُمْ فِيمَا رَزَقْتَهُمْ، وَاغْفِرْ لَهُمْ وَارْحَمْهُمْ».", "رواه مسلم (2042)"),
            DuaCardItem("كفارة المجلس", "«سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ».", "رواه أبو داود والترمذي")
        )
    ),
    DuaCategory(
        "6. دعاء المطر والريح وعيادة المريض",
        "اللهم صيباً نافعاً · لا بأس طهور إن شاء الله",
        listOf(
            DuaCardItem("دعاء نزول المطر وبعده", "عند نزوله: «اللَّهُمَّ صَيِّبًا نَافِعًا». وبعد نزوله: «مُطِرْنَا بِفَضْلِ اللَّهِ وَرَحْمَتِهِ».", "رواه البخاري (1032)"),
            DuaCardItem("دعاء عيادة المريض", "«لَا بَأْسَ طَهُورٌ إِنْ شَاءَ اللَّهُ» · «أَسْأَلُ اللَّهَ الْعَظِيمَ رَبَّ الْعَرْشِ الْعَظِيمِ أَنْ يَشْفِيَكَ» (سبع مرات).", "رواه البخاري وأبو داود")
        )
    )
)

@Composable
fun SakinaDuasScreen(onBackToHome: () -> Unit, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Main Duas, 1 = Hisn al-Muslim (#23)
    var selectedIdx by remember { mutableStateOf<Int?>(null) }

    BackHandler {
        if (selectedIdx != null) selectedIdx = null else onBackToHome()
    }

    val currentList = if (activeSubTab == 0) DUA_CATEGORIES else HISN_MUSLIM_CATEGORIES

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
                val titleText = selectedIdx?.let { idx ->
                    if (activeSubTab == 0) s.duaCatTitles.getOrElse(idx) { currentList[idx].title }
                    else currentList[idx].title
                } ?: s.duasTitle
                Text(
                    text = titleText,
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = theme.accentBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2-Tab Sub-Switcher: "الأدعية المأثورة" | "حصن المسلم" (#23)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(theme.bgCard)
                .border(0.9.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(18.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(s.duasMainTab, s.hisnMuslimTab).forEachIndexed { idx, tabLabel ->
                val sel = activeSubTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            if (sel) Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold))
                            else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable {
                            activeSubTab = idx
                            selectedIdx = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabLabel,
                        fontFamily = SakinaEditorialFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedIdx == null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(currentList) { idx, cat ->
                    val title = if (activeSubTab == 0) s.duaCatTitles.getOrElse(idx) { cat.title } else cat.title
                    val sub = if (activeSubTab == 0) s.duaCatSubs.getOrElse(idx) { cat.subtitle } else cat.subtitle
                    SakinaGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIdx = idx },
                        cornerRadius = 16.dp,
                        glowGold = idx == 0 || idx == 4,
                        contentPadding = PaddingValues(13.dp)
                    ) {
                        Text(
                            text = title.toWesternDigits(),
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = theme.accentBright
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = sub.toWesternDigits(),
                            fontFamily = SakinaBodyFont,
                            fontSize = 11.5.sp,
                            color = SakinaBrandColors.SoftSilver
                        )
                    }
                }
            }
        } else {
            val cat = currentList[selectedIdx!!]
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cat.items) { dua ->
                    SakinaParchmentPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = "❖ ${dua.title} ❖",
                            fontFamily = SakinaNaskhFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F3D3E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dua.text.toWesternDigits(),
                            fontFamily = SakinaNaskhFontFamily,
                            fontSize = 18.sp,
                            lineHeight = 32.sp,
                            color = SakinaInkDark,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dua.source.toWesternDigits(),
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
