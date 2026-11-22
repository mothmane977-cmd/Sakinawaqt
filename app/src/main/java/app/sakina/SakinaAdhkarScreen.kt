package app.sakina
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Slider
import kotlinx.coroutines.delay
import android.os.Build
import android.media.MediaPlayer
import android.media.AudioAttributes

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class DhikrItem(val text: String, val targetCount: Int, val source: String)
data class DhikrCategory(val title: String, @DrawableRes val iconRes: Int, val items: List<DhikrItem>)

data class AsmaUlHusnaEntry(
    val nameAr: String,
    val meaningAr: String,
    val virtueAr: String,
    val meaningEn: String,
    val virtueEn: String
)

private val ASMA_UL_HUSNA_DAILY = listOf(
    AsmaUlHusnaEntry(
        "اللَّهُ الرَّحْمَٰنُ الرَّحِيمُ",
        "ذو الرحمة الواسعة التي وسعت كل شيء، والرحمة الواصلة للمؤمنين في الدنيا والآخرة.",
        "الفضل: افتتاح كل أمر ببسم الله الرحمن الرحيم يبارك فيه، ومن دعا بهما أدركته رحمة الله الواسعة (صحيح مسلم 2752).",
        "The Most Gracious, The Most Merciful — Whose vast mercy encompasses all creation.",
        "Virtue: Beginning actions with Bismillah Ar-Rahman Ar-Rahim brings divine blessing and mercy (Sahih Muslim 2752)."
    ),
    AsmaUlHusnaEntry(
        "المَلِكُ القُدُّوسُ السَّلَامُ",
        "المالك المتصرف في ملكه، المنزّه عن كل نقص وعيب، الذي سلّم عباده من الظلم ومنه السلام.",
        "الفضل: كان النبي ﷺ يقول عقب الوتر: «سبحان الملك القدوس» ثلاثاً، وقال عقب كل صلاة: «اللهم أنت السلام ومنك السلام» (صحيح مسلم 592).",
        "The Sovereign, The Most Pure, The Source of Peace — free from all imperfection.",
        "Virtue: The Prophet ﷺ said after every prayer: 'Allahumma Antas-Salam wa minkas-salam' (Sahih Muslim 592)."
    ),
    AsmaUlHusnaEntry(
        "الحَيُّ القَيُّومُ",
        "الكامل في حياته الذاتية الأزلية، القائم بنفسه المقيم لكل ما سواه من المخلوقات.",
        "الفضل: هما في اسم الله الأعظم الذي إذا دُعي به أجاب وإذا سُئل به أعطى، وكان النبي ﷺ إذا كربه أمر قال: «يا حي يا قيوم برحمتك أستغيث» (الترمذي 3524).",
        "The Ever-Living, The All-Sustaining — part of Allah's Greatest Name (Al-Ism Al-A'zam).",
        "Virtue: When distressed, the Prophet ﷺ supplicated: 'Ya Hayyu Ya Qayyum, bi-rahmatika astaghith' (Tirmidhi 3524)."
    ),
    AsmaUlHusnaEntry(
        "الوَهَّابُ الرَّزَّاقُ الفَتَّاحُ",
        "الكثير العطاء بلا عوض، المتكفل بأرزاق الخلائق، الذي يفتح أبواب الرحمة والرزق والنصر.",
        "الفضل: من لازم الدعاء بـ «ربنا لا تزغ قلوبنا بعد إذ هديتنا وهب لنا من لدنك رحمة إنك أنت الوهاب» ثبّت الله قلبه ووسّع رزقه (آل عمران: 8).",
        "The All-Bestower, The Total Provider, The Supreme Opener of doors of mercy and provision.",
        "Virtue: Supplicating with 'Innaka Antal-Wahhab' brings steadfastness of heart and abundant provision (Qur'an 3:8)."
    ),
    AsmaUlHusnaEntry(
        "الغَفَّارُ التَّوَّابُ العَفُوُّ",
        "الذي يستر الذنوب ويتجاوز عن الخطايا ويقبل توبة التائبين ويمحو السيئات.",
        "الفضل: علم النبي ﷺ عائشة رضي الله عنها أفضل الدعاء: «اللهم إنك عفو تحب العفو فاعف عني» (رواه الترمذي 3513 وصححه).",
        "The All-Forgiving, The Acceptor of Repentance, The Pardoner Who erases sins completely.",
        "Virtue: The Prophet ﷺ taught: 'Allahumma innaka Afuwwun tuhibbul-afwa fa'fu anni' (Tirmidhi 3513)."
    ),
    AsmaUlHusnaEntry(
        "اللَّطِيفُ الخَبِيرُ الحَلِيمُ",
        "البرّ بعباده الذي يوصل إليهم مصالحهم برفق وخفاء، العالم بدقائق الأمور، الذي لا يعاجل بالعقوبة.",
        "الفضل: استحضار اسم الله اللطيف في الشدائد يورث السكينة واليقين بأن تدبير الله الخفيّ كله خير ورحمة (يوسف: 100).",
        "The Subtle and Gentle, The All-Aware, The Forbearing Who brings relief in unseen ways.",
        "Virtue: Remembering Al-Latif in hardship fills the heart with serenity and trust in Allah's decree (Qur'an 12:100)."
    ),
    AsmaUlHusnaEntry(
        "الأَحَدُ الصَّمَدُ ذُو الجَلَالِ وَالإِكْرَامِ",
        "المتفرد بالكمال، المقصود في جميع الحوائج، المستحق للتعظيم والإجلال والإنعام على أوليائه.",
        "الفضل: قال النبي ﷺ: «ألظّوا بيا ذا الجلال والإكرام» أي الزموها وأكثروا منها في دعائكم (رواه الترمذي 3525 وصححه الألباني).",
        "The One and Only, The Eternal Refuge, Possessor of Majesty and Honor.",
        "Virtue: The Prophet ﷺ commanded: 'Be constant in supplicating with Ya Dhal-Jalali wal-Ikram' (Tirmidhi 3525)."
    )
)

private val ADHKAR_CATEGORIES = listOf(
    DhikrCategory("أذكار الصباح", R.drawable.ic_fajr, listOf(
        DhikrItem("أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ: ﴿ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ ﴾.", 1, "آية الكرسي - البقرة: 255"),
        DhikrItem("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ: ﴿ قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ * لَمْ يَلِدْ وَلَمْ يُولَدْ * وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ ﴾ (والمعوذتين ثلاث مرات).", 3, "أبو داود والترمذي"),
        DhikrItem("أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ.", 1, "رواه مسلم"),
        DhikrItem("اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ.", 1, "سيد الاستغفار - رواه البخاري"),
        DhikrItem("بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.", 3, "أبو داود والترمذي"),
        DhikrItem("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ.", 3, "رواه مسلم")
    )),
    DhikrCategory("أذكار المساء", R.drawable.ic_maghrib, listOf(
        DhikrItem("أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ.", 1, "رواه مسلم"),
        DhikrItem("اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ.", 1, "رواه الترمذي"),
        DhikrItem("أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.", 3, "رواه مسلم"),
        DhikrItem("حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ.", 7, "رواه أبو داود")
    )),
    DhikrCategory("أذكار بعد الصلاة", R.drawable.ic_after_prayer, listOf(
        DhikrItem("أَسْتَغْفِرُ اللَّهَ (ثَلَاثًا).. اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ.", 1, "رواه مسلم"),
        DhikrItem("لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ.", 1, "متفق عليه"),
        DhikrItem("سُبْحَانَ اللَّهِ (33) · الْحَمْدُ لِلَّهِ (33) · اللَّهُ أَكْبَرُ (33)، ويتم المائة بـ: لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ.", 33, "رواه مسلم")
    )),
    DhikrCategory("أذكار النوم", R.drawable.ic_isha, listOf(
        DhikrItem("بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ.", 1, "متفق عليه"),
        DhikrItem("بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا.", 1, "رواه البخاري")
    )),
    DhikrCategory("أذكار الدخول والخروج من المسجد", R.drawable.ic_mosque, listOf(
        DhikrItem("عِنْدَ الدُّخُولِ (يُقدّم رجله اليمنى): «بِسْمِ اللَّهِ، وَالصَّلَاةُ وَالسَّلَامُ عَلَىٰ رَسُولِ اللَّهِ، اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ».", 1, "رواه مسلم (713)"),
        DhikrItem("عِنْدَ الْخُرُوجِ (يُقدّم رجله اليسرى): «بِسْمِ اللَّهِ، وَالصَّلَاةُ وَالسَّلَاُ عَلَىٰ رَسُولِ اللَّهِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ، اللَّهُمَّ اعْصِمْنِي مِنَ الشَّيْطَانِ الرَّجِيمِ».", 1, "رواه مسلم (713) وابن ماجه")
    )),
    DhikrCategory("أذكار الدخول والخروج من البيت", R.drawable.ic_home_adhkar, listOf(
        DhikrItem("عِنْدَ الْخُرُوجِ مِنَ الْبَيْتِ: «بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ».", 1, "رواه أبو داود (5095) والترمذي (3426)"),
        DhikrItem("عِنْدَ الدُّخُولِ إِلَى الْبَيْتِ: «بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَىٰ رَبِّنَا تَوَكَّلْنَا» ثم يُسلّم على أهله.", 1, "رواه أبو داود (5096) ومسلم (2018)")
    ))
)

private val TASBIH_PHRASES = listOf("سُبْحَانَ اللَّهِ", "الْحَمْدُ لِلَّهِ", "اللَّهُ أَكْبَرُ", "لَا إِلَٰهَ إِلَّا اللَّهُ", "أَسْتَغْفِرُ اللَّهَ")

@Composable
fun SakinaAdhkarScreen(ctx: Context, initialCategoryIndex: Int? = null, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    val isArOrUr = lang == SakinaLang.AR || lang == SakinaLang.UR
    val view = LocalView.current
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    var selectedCategoryIndex by remember { mutableStateOf<Int?>(initialCategoryIndex) }
    LaunchedEffect(initialCategoryIndex) {
        if (initialCategoryIndex != null) selectedCategoryIndex = initialCategoryIndex
    }
    var phraseIdx by remember { mutableIntStateOf(0) }
    var count by remember { mutableIntStateOf(sp.getInt("tasbih_count", 0)) }
    val target = 33
    val itemCounts = remember { mutableStateMapOf<String, Int>() }

    // Daily Name of Allah index (#20)
    val initialAsmaIdx = remember {
        Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % ASMA_UL_HUSNA_DAILY.size
    }
    var asmaIdx by remember { mutableIntStateOf(initialAsmaIdx) }
    var showAsmaDialog by remember { mutableStateOf(false) }
    val currentAsma = ASMA_UL_HUSNA_DAILY[asmaIdx]

    BackHandler(enabled = selectedCategoryIndex != null) { selectedCategoryIndex = null }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 48.dp, bottom = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Header Cartouche
        Box(modifier = Modifier.fillMaxWidth()) {
            if (selectedCategoryIndex != null) {
                SakinaBackIconButton(
                    onClick = { selectedCategoryIndex = null },
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
            SakinaOrnamentCartouche(
                modifier = Modifier.align(Alignment.Center),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 6.dp)
            ) {
                Text(
                    text = selectedCategoryIndex?.let { s.adhkarCatTitles[it] } ?: s.adhkarTitle,
                    style = TextStyle(
                        fontFamily = SakinaEditorialFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        brush = SakinaCalligraphyBrush()
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (selectedCategoryIndex == null) {
            // 2. Surah Al-Ahzab: 35 directly under the cartouche in Classical Naskh (#24, #25)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.verticalGradient(listOf(theme.bgCard.copy(alpha = 0.92f), theme.bgDeep.copy(alpha = 0.96f))))
                        .border(1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontFamily = SakinaNaskhFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = theme.accentGold,
                        textAlign = TextAlign.Center,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                    Text(
                        text = "﴿ وَالذَّاكِرِينَ اللَّهَ كَثِيرًا وَالذَّاكِرَاتِ أَعَدَّ اللَّهُ لَهُمْ مَغْفِرَةً وَأَجْرًا عَظِيمًا ﴾ {الأحزاب: 35}".toWesternDigits(),
                        fontFamily = SakinaNaskhFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.2.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFFFF7D6), Color(0xFFE6CF94), Color(0xFFD4AF37))
                            ),
                            shadow = Shadow(color = Color(0xAA000000), offset = Offset(0f, 2f), blurRadius = 4f),
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                    }
                }
            }

            // 3. 3x2 Adhkar Category Cards with larger 28.dp icons (#19)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                for (row in 0 until 3) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        for (col in 0 until 2) {
                            val idx = row * 2 + col
                            val cat = ADHKAR_CATEGORIES[idx]
                            val locTitle = s.adhkarCatTitles[idx]
                            val fontSize = if (locTitle.length > 22) 10.sp else 11.5.sp
                            SakinaGlassCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(66.dp)
                                    .clickable { selectedCategoryIndex = idx },
                                cornerRadius = 15.dp,
                                glowGold = idx < 2,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = cat.iconRes),
                                        contentDescription = locTitle,
                                        modifier = Modifier.size(27.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = locTitle,
                                        fontFamily = SakinaHeadingFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = fontSize,
                                        lineHeight = 12.5.sp,
                                        color = theme.accentBright,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Asma ul-Husna (Name of the Day, Meaning & Virtue) Card (#20)
            SakinaParchmentPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAsmaDialog = true },
                cornerRadius = 15.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF0F3D3E),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                asmaIdx = (asmaIdx - 1 + ASMA_UL_HUSNA_DAILY.size) % ASMA_UL_HUSNA_DAILY.size
                            }
                    )
                    Text(
                        text = "❖ ${s.asmaTitle} ❖",
                        fontFamily = SakinaNaskhFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF6B5224),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = Color(0xFF0F3D3E),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                asmaIdx = (asmaIdx + 1) % ASMA_UL_HUSNA_DAILY.size
                            }
                    )
                }
                Text(
                    text = currentAsma.nameAr,
                    fontFamily = SakinaNaskhFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = Color(0xFF0F3D3E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isArOrUr) currentAsma.meaningAr else currentAsma.meaningEn,
                    fontFamily = SakinaBodyFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    lineHeight = 13.5.sp,
                    color = SakinaInkDark,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = (if (isArOrUr) currentAsma.virtueAr else currentAsma.virtueEn).toWesternDigits(),
                    fontFamily = SakinaBodyFont,
                    fontSize = 9.2.sp,
                    lineHeight = 12.5.sp,
                    color = Color(0xFF6B5224),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 5. Compact Interactive Misbaha / Tasbih Counter Bar
            val pulseScale by animateFloatAsState(
                targetValue = if (count % 2 == 1) 1.02f else 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "tasbihPulse"
            )
            val progress = (count % target).toFloat() / target.toFloat()

            SakinaGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                glowGold = true,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left/Start: Ergonomic 60.dp Gold-Ringed Tap Counter Medallion (crisp number only, zero clipping!)
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(theme.bgDeep.copy(alpha = 0.7f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                count++
                                sp.edit().putInt("tasbih_count", count).apply()
                                if (count % target == 0) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                else view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val r = size.minDimension / 2f - 4.dp.toPx()
                            drawCircle(color = Color(0x33D4AF37), radius = r, style = Stroke(width = 2.dp.toPx()))
                            drawArc(
                                brush = Sakina3DGoldBevelBrush,
                                startAngle = -90f,
                                sweepAngle = progress * 360f,
                                useCenter = false,
                                style = Stroke(width = 3.4.dp.toPx(), cap = StrokeCap.Round)
                            )
                            for (i in 0 until 12) {
                                val angle = (i * 30.0 - 90.0) * PI / 180.0
                                val bx = cx + (r * cos(angle)).toFloat()
                                val by = cy + (r * sin(angle)).toFloat()
                                val active = i <= ((count % target) * 12 / target)
                                drawCircle(
                                    color = if (active) theme.accentBright else Color(0x44D4AF37),
                                    radius = if (active) 2.2.dp.toPx() else 1.4.dp.toPx(),
                                    center = Offset(bx, by)
                                )
                            }
                        }
                        Text(
                            text = "$count".toWesternDigits(),
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = theme.accentBright,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }

                    // Right/Main: Spacious Phrase Selector + Prompt/Progress + Reset Button
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22FFFFFF))
                                    .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                                    .clickable { phraseIdx = (phraseIdx - 1 + TASBIH_PHRASES.size) % TASBIH_PHRASES.size },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(17.dp))
                            }
                            Text(
                                text = TASBIH_PHRASES[phraseIdx],
                                fontFamily = SakinaNaskhFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = theme.accentBright,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22FFFFFF))
                                    .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                                    .clickable { phraseIdx = (phraseIdx + 1) % TASBIH_PHRASES.size },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(17.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22FFFFFF))
                                    .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                                    .clickable {
                                        count = 0
                                        sp.edit().putInt("tasbih_count", 0).apply()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Reset", tint = theme.accentBright, modifier = Modifier.size(14.dp))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${s.tasbihPrompt} (${count % target}/$target)".toWesternDigits(),
                                fontFamily = SakinaBodyFont,
                                fontSize = 10.sp,
                                color = SakinaBrandColors.SoftSilver,
                                maxLines = 1
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                                TASBIH_PHRASES.forEachIndexed { idx, _ ->
                                    val sel = idx == phraseIdx
                                    Box(
                                        modifier = Modifier
                                            .size(if (sel) 16.dp else 6.dp, 6.dp)
                                            .clip(CircleShape)
                                            .background(if (sel) theme.accentBright else Color(0x44FFFFFF))
                                            .clickable { phraseIdx = idx }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val catIdx = selectedCategoryIndex!!
            val category = ADHKAR_CATEGORIES[catIdx]
            val audioRawName = when (catIdx) {
                0 -> "adhkar_sabah"
                1 -> "adhkar_masaa"
                3 -> "adhkar_nawm"
                else -> null
            }
            if (audioRawName != null) {
                SakinaAdhkarAudioPlayerBar(
                    ctx = ctx,
                    rawName = audioRawName,
                    title = s.adhkarCatTitles.getOrElse(catIdx) { category.title },
                    lang = lang
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(category.items) { idx, item ->
                    val key = "${category.title}_$idx"
                    val doneCount = itemCounts[key] ?: 0
                    val completed = doneCount >= item.targetCount

                    SakinaParchmentPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (doneCount < item.targetCount) {
                                    itemCounts[key] = doneCount + 1
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            },
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = item.text.toWesternDigits(),
                            fontFamily = SakinaNaskhFontFamily,
                            fontSize = 17.sp,
                            lineHeight = 30.sp,
                            color = SakinaInkDark,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.source.toWesternDigits(),
                                fontFamily = SakinaNaskhFontFamily,
                                fontSize = 11.5.sp,
                                color = Color(0xFF6B5224)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (completed) Color(0xFF1E5631) else Color(0xFF0F3D3E))
                                    .border(0.9.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${doneCount.coerceAtMost(item.targetCount)} / ${item.targetCount}".toWesternDigits(),
                                    fontFamily = SakinaNumeralFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = theme.accentBright
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAsmaDialog) {
        AlertDialog(
            onDismissRequest = { showAsmaDialog = false },
            containerColor = theme.bgCard,
            title = {
                Text(
                    text = "${s.asmaTitle}\n${currentAsma.nameAr}",
                    fontFamily = SakinaNaskhFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = theme.accentBright,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isArOrUr) "المعنى: ${currentAsma.meaningAr}" else "Meaning: ${currentAsma.meaningEn}",
                        fontFamily = SakinaBodyFont,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = SakinaBrandColors.LuminousIvory
                    )
                    Text(
                        text = (if (isArOrUr) currentAsma.virtueAr else currentAsma.virtueEn).toWesternDigits(),
                        fontFamily = SakinaBodyFont,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = theme.accentBright
                    )
                    Text(
                        text = if (isArOrUr) "الحديث الجامع: قال رسول الله ﷺ: «إن لله تسعة وتسعين اسماً، مائة إلا واحداً، من أحصاها دخل الجنة» (متفق عليه: البخاري 2736 ومسلم 2677).".toWesternDigits()
                        else "Hadith: The Prophet ﷺ said: 'Allah has ninety-nine Names, one hundred less one; whoever learns, understands, and acts upon them will enter Paradise.' (Sahih al-Bukhari 2736 & Sahih Muslim 2677).",
                        fontFamily = SakinaBodyFont,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = SakinaBrandColors.SoftSilver
                    )
                }
            },
            confirmButton = {
                SakinaBackIconButton(onClick = { showAsmaDialog = false })
            }
        )
    }
}


// v30: Built-in 100% Offline Adhkar Audio Player (Morning, Evening, Sleep — Mishary Al-Afasy OGG)
@Composable
private fun SakinaAdhkarAudioPlayerBar(
    ctx: Context,
    rawName: String,
    title: String,
    lang: SakinaLang
) {
    val theme = LocalSakinaTheme.current
    val rawId = remember(rawName) { ctx.resources.getIdentifier(rawName, "raw", ctx.packageName) }
    if (rawId == 0) return

    var player by remember(rawId) { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember(rawId) { mutableStateOf(false) }
    var isLooping by remember(rawId) { mutableStateOf(false) }
    var posMs by remember(rawId) { mutableIntStateOf(0) }
    var durMs by remember(rawId) { mutableIntStateOf(0) }
    var speedIdx by remember(rawId) { mutableIntStateOf(0) }
    val speeds = listOf(1.0f, 1.25f, 1.5f)

    fun fmtMs(ms: Int): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val m = totalSec / 60
        val sec = totalSec % 60
        return SakinaAdvancedPrefs.formatDigits(ctx, String.format(java.util.Locale.US, "%02d:%02d", m, sec))
    }

    fun ensurePlayer(): MediaPlayer? {
        player?.let { return it }
        return try {
            SakinaAdhanAudio.stop()
            val mp = MediaPlayer.create(ctx.applicationContext, rawId) ?: return null
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            mp.isLooping = isLooping
            durMs = mp.duration.coerceAtLeast(1)
            mp.setOnCompletionListener {
                if (!mp.isLooping) {
                    isPlaying = false
                    posMs = 0
                    try { mp.seekTo(0) } catch (_: Exception) {}
                }
            }
            player = mp
            mp
        } catch (_: Exception) { null }
    }

    LaunchedEffect(isPlaying, player) {
        while (isPlaying) {
            val mp = player
            if (mp != null) {
                try {
                    if (mp.isPlaying) {
                        posMs = mp.currentPosition
                        if (durMs <= 1) durMs = mp.duration.coerceAtLeast(1)
                    } else {
                        isPlaying = false
                    }
                } catch (_: Exception) {
                    isPlaying = false
                }
            }
            delay(400L)
        }
    }

    DisposableEffect(rawId) {
        onDispose {
            try {
                player?.let {
                    if (it.isPlaying) it.stop()
                    it.release()
                }
            } catch (_: Exception) {}
            player = null
            isPlaying = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xEE062920), Color(0xEE0D4435))))
            .border(1.1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sakinaTr(lang, "🎧 استماع أوفلاين · $title", "🎧 Offline Audio · $title", "🎧 Audio hors-ligne · $title", "🎧 Çevrimdışı Ses · $title", "🎧 Audio Offline · $title", "🎧 آف لائن آڈیو · $title"),
                        fontFamily = SakinaHeadingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = theme.accentBright,
                        maxLines = 1
                    )
                    Text(
                        text = sakinaTr(lang, "بصوت الشيخ مشاري العفاسي", "Recited by Mishary Al-Afasy", "Récité par Mishary Al-Afasy", "Mişari El-Afasi", "Qari Mishary Al-Afasy", "قاری مشاری العفاسی"),
                        fontFamily = SakinaBodyFont,
                        fontSize = 10.sp,
                        color = SakinaBrandColors.SoftSilver,
                        maxLines = 1
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Speed toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x28FFFFFF))
                            .clickable {
                                speedIdx = (speedIdx + 1) % speeds.size
                                val spd = speeds[speedIdx]
                                if (Build.VERSION.SDK_INT >= 23) {
                                    try {
                                        player?.let { mp ->
                                            val wasPlaying = mp.isPlaying
                                            mp.playbackParams = mp.playbackParams.setSpeed(spd)
                                            if (!wasPlaying) mp.pause()
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${speeds[speedIdx]}x",
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = theme.accentBright
                        )
                    }
                    // Loop toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isLooping) theme.accentGold else Color(0x28FFFFFF))
                            .clickable {
                                isLooping = !isLooping
                                try { player?.isLooping = isLooping } catch (_: Exception) {}
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "↻",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isLooping) theme.bgDeep else Color.White
                        )
                    }
                    // Play / Pause main button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.accentGold)
                            .clickable {
                                val mp = ensurePlayer()
                                if (mp != null) {
                                    if (isPlaying) {
                                        try { mp.pause() } catch (_: Exception) {}
                                        isPlaying = false
                                    } else {
                                        try {
                                            if (Build.VERSION.SDK_INT >= 23) {
                                                mp.playbackParams = mp.playbackParams.setSpeed(speeds[speedIdx])
                                            }
                                            mp.start()
                                            isPlaying = true
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isPlaying) sakinaTr(lang, "إيقاف ❚❚", "Pause ❚❚", "Pause ❚❚", "Duraklat ❚❚", "Jeda ❚❚", "روکیں ❚❚")
                            else sakinaTr(lang, "استمع ▶", "Play ▶", "Écouter ▶", "Dinle ▶", "Putar ▶", "سنیں ▶"),
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = theme.bgDeep
                        )
                    }
                }
            }

            if (durMs > 1) {
                Slider(
                    value = posMs.toFloat().coerceIn(0f, durMs.toFloat()),
                    onValueChange = { v ->
                        posMs = v.toInt()
                        try { player?.seekTo(posMs) } catch (_: Exception) {}
                    },
                    valueRange = 0f..durMs.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = theme.accentBright,
                        activeTrackColor = theme.accentGold,
                        inactiveTrackColor = Color(0x44FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth().height(26.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(fmtMs(posMs), fontFamily = SakinaNumeralFont, fontSize = 10.sp, color = SakinaBrandColors.SoftSilver)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "-15s",
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = theme.accentBright,
                            modifier = Modifier.clickable {
                                val next = (posMs - 15_000).coerceAtLeast(0)
                                posMs = next
                                try { player?.seekTo(next) } catch (_: Exception) {}
                            }
                        )
                        Text(
                            text = "+15s",
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = theme.accentBright,
                            modifier = Modifier.clickable {
                                val next = (posMs + 15_000).coerceAtMost(durMs)
                                posMs = next
                                try { player?.seekTo(next) } catch (_: Exception) {}
                            }
                        )
                    }
                    Text(fmtMs(durMs), fontFamily = SakinaNumeralFont, fontSize = 10.sp, color = SakinaBrandColors.SoftSilver)
                }
            }
        }
    }
}
