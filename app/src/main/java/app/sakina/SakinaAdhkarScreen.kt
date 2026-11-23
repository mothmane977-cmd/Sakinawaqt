package app.sakina
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Slider
import kotlinx.coroutines.delay
import android.os.Build
import android.media.MediaPlayer
import android.media.AudioAttributes

import android.content.Context
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.text.BasicTextField
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
fun SakinaAdhkarScreen(
    ctx: Context,
    initialCategoryIndex: Int? = null,
    onBackClick: () -> Unit = {},
    onOpenPortal: (WorshipPortalType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    var selectedCategoryIndex by remember(initialCategoryIndex) { mutableStateOf<Int?>(initialCategoryIndex) }
    var phraseIdx by remember { mutableIntStateOf(0) }
    var count by remember { mutableIntStateOf(sp.getInt("tasbih_count", 0)) }
    var round by remember { mutableIntStateOf(sp.getInt("tasbih_round", 1)) }
    val itemCounts = remember { mutableStateMapOf<String, Int>() }

    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Offline MediaPlayer for Morning / Evening / Sleep Adhkar (Mishary Al-Afasy OGG)
    var audioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    fun stopAdhkarAudio() {
        try { audioPlayer?.stop() } catch (_: Exception) {}
        try { audioPlayer?.release() } catch (_: Exception) {}
        audioPlayer = null
        isAudioPlaying = false
    }

    DisposableEffect(selectedCategoryIndex) {
        onDispose { stopAdhkarAudio() }
    }

    BackHandler(enabled = selectedCategoryIndex != null) {
        stopAdhkarAudio()
        selectedCategoryIndex = null
    }

    val audioRawNameForCat: (Int?) -> String? = { idx ->
        when (idx) {
            0 -> "adhkar_sabah"
            1 -> "adhkar_masaa"
            3 -> "adhkar_nawm"
            else -> null
        }
    }

    fun toggleCategoryAudio(catIdx: Int?) {
        val rawName = audioRawNameForCat(catIdx) ?: return
        val rawId = ctx.resources.getIdentifier(rawName, "raw", ctx.packageName)
        if (rawId == 0) return
        val cur = audioPlayer
        if (cur != null) {
            if (cur.isPlaying) {
                cur.pause()
                isAudioPlaying = false
            } else {
                cur.start()
                isAudioPlaying = true
            }
        } else {
            val mp = MediaPlayer.create(ctx.applicationContext, rawId) ?: return
            mp.setOnCompletionListener { isAudioPlaying = false }
            mp.start()
            audioPlayer = mp
            isAudioPlaying = true
        }
    }

    fun progressForCat(idx: Int): Float {
        val cat = ADHKAR_CATEGORIES.getOrNull(idx) ?: return 0f
        if (cat.items.isEmpty()) return 0f
        val done = cat.items.indices.count { i ->
            (itemCounts["${cat.title}_$i"] ?: 0) >= cat.items[i].targetCount
        }
        return done.toFloat() / cat.items.size.toFloat()
    }

    val portals = listOf(
        WorshipPortalCard(
            WorshipPortalType.MORNING_ADHKAR,
            s.adhkarCatTitles.getOrElse(0) { "أذكار الصباح" },
            sakinaTr(lang, "أوفلاين · صوت", "Offline · Audio", "Hors-ligne · Audio", "Çevrimdışı · Ses", "Offline · Audio", "آف لائن · آڈیو"),
            progressForCat(0),
            true
        ),
        WorshipPortalCard(
            WorshipPortalType.EVENING_ADHKAR,
            s.adhkarCatTitles.getOrElse(1) { "أذكار المساء" },
            sakinaTr(lang, "أوفلاين · صوت", "Offline · Audio", "Hors-ligne · Audio", "Çevrimdışı · Ses", "Offline · Audio", "آف لائن · آڈیو"),
            progressForCat(1),
            true
        ),
        WorshipPortalCard(
            WorshipPortalType.SLEEP_AND_POST_PRAYER,
            s.adhkarCatTitles.getOrElse(3) { "أذكار النوم" },
            sakinaTr(lang, "أوفلاين · صوت", "Offline · Audio", "Hors-ligne · Audio", "Çevrimdışı · Ses", "Offline · Audio", "آف لائن · آڈیو"),
            progressForCat(3),
            true
        ),
        WorshipPortalCard(
            WorshipPortalType.RUQYAH,
            s.ruqyahTitle,
            sakinaTr(lang, "آيات وأدعية الحصن", "Quran & Sunnah", "Coran et Sunna", "Kur'an ve Sünnet", "Al-Qur'an & Sunnah", "قرآن و سنت"),
            0.25f,
            false
        ),
        WorshipPortalCard(
            WorshipPortalType.NAMES_OF_ALLAH,
            sakinaTr(lang, "أسماء الله الحسنى", "99 Names of Allah", "Les 99 Noms", "Esmaül Hüsna", "Asmaul Husna", "اسماء الحسنیٰ"),
            sakinaTr(lang, "99 اسماً وشرحاً", "99 Names", "99 Noms", "99 İsim", "99 Nama", "99 اسماء"),
            0.50f,
            false
        ),
        WorshipPortalCard(
            WorshipPortalType.DUAS_AND_TRACKER,
            sakinaTr(lang, "الأدعية والعبادات", "Duas & Worship", "Invocations", "Dualar", "Doa & Ibadah", "دعائیں اور عبادات"),
            sakinaTr(lang, "أدعية نبوية مأثورة", "Prophetic Duas", "Douas prophétiques", "Nebevî Dualar", "Doa Nabawi", "مسنون دعائیں"),
            progressForCat(2),
            false
        )
    )

    val activeCat = selectedCategoryIndex?.let { ADHKAR_CATEGORIES.getOrNull(it) }
    val activeItems = remember(selectedCategoryIndex, itemCounts.size, itemCounts.values.sum()) {
        if (activeCat == null) emptyList()
        else activeCat.items.mapIndexed { idx, it ->
            val key = "${activeCat.title}_$idx"
            DhikrCardItem(
                id = idx,
                text = it.text.toWesternDigits(),
                reference = it.source.toWesternDigits(),
                targetCount = it.targetCount,
                currentCount = (itemCounts[key] ?: 0).coerceAtMost(it.targetCount)
            )
        }
    }

    val oasisStrings = SakinaAdhkarStrings(
        oasisTitle = sakinaTr(lang, "واحة الذكر", "Adhkar Oasis", "Oasis du Dhikr", "Zikir Bahçesi", "Oase Dzikir", "ذکر و اذکار"),
        backDesc = sakinaTr(lang, "رجوع", "Back", "Retour", "Geri", "Kembali", "واپس"),
        searchDesc = sakinaTr(lang, "بحث في الأذكار", "Search Adhkar", "Rechercher", "Ara", "Cari", "تلاش کریں"),
        roundFormat = sakinaTr(lang, "الجولة %d", "Round %d", "Tour %d", "Tur %d", "Putaran %d", "دور %d"),
        offlineAudioLabel = sakinaTr(lang, "استماع أوفلاين · أذكار", "Offline Audio · Adhkar", "Audio hors-ligne", "Çevrimdışı Ses", "Audio Offline", "آف لائن آڈیو"),
        reciterLabel = sakinaTr(lang, "بصوت الشيخ مشاري العفاسي", "Recited by Mishary Al-Afasy", "Mishary Al-Afasy", "Mişari El-Afasi", "Syekh Misyari Al-Afasy", "مشاري راشد العفاسي")
    )

    val oasisState = SakinaAdhkarOasisState(
        tasbih = TasbihState(
            dhikrText = TASBIH_PHRASES[phraseIdx],
            count = count,
            target = 33,
            round = round
        ),
        portals = portals,
        activeCategoryTitle = selectedCategoryIndex?.let { s.adhkarCatTitles.getOrElse(it) { activeCat?.title } },
        activeCategoryHasAudio = audioRawNameForCat(selectedCategoryIndex)?.let {
            ctx.resources.getIdentifier(it, "raw", ctx.packageName) != 0
        } ?: false,
        isAudioPlaying = isAudioPlaying,
        activeDhikrItems = activeItems
    )

    SakinaAdhkarOasisScreen(
        state = oasisState,
        strings = oasisStrings,
        headingFontFamily = SakinaHeadingFont,
        bodyFontFamily = SakinaBodyFont,
        showBottomDock = false,
        hostBottomBarReserve = 24.dp,
        onBackClick = onBackClick,
        onSearchClick = { showSearchDialog = true },
        onTasbihTap = {
            if (count + 1 >= 33) {
                count = 0
                round++
            } else {
                count++
            }
            sp.edit().putInt("tasbih_count", count).putInt("tasbih_round", round).apply()
        },
        onTasbihReset = {
            count = 0
            round = 1
            sp.edit().putInt("tasbih_count", 0).putInt("tasbih_round", 1).apply()
        },
        onPrevDhikr = { phraseIdx = (phraseIdx - 1 + TASBIH_PHRASES.size) % TASBIH_PHRASES.size },
        onNextDhikr = { phraseIdx = (phraseIdx + 1) % TASBIH_PHRASES.size },
        onPortalClick = { portal ->
            when (portal) {
                WorshipPortalType.MORNING_ADHKAR -> selectedCategoryIndex = 0
                WorshipPortalType.EVENING_ADHKAR -> selectedCategoryIndex = 1
                WorshipPortalType.SLEEP_AND_POST_PRAYER -> selectedCategoryIndex = 3
                WorshipPortalType.RUQYAH -> onOpenPortal(WorshipPortalType.RUQYAH)
                WorshipPortalType.NAMES_OF_ALLAH -> onOpenPortal(WorshipPortalType.NAMES_OF_ALLAH)
                WorshipPortalType.DUAS_AND_TRACKER -> onOpenPortal(WorshipPortalType.DUAS_AND_TRACKER)
            }
        },
        onCloseCategory = {
            stopAdhkarAudio()
            selectedCategoryIndex = null
        },
        onToggleAudioPlay = { toggleCategoryAudio(selectedCategoryIndex) },
        onDhikrCardTap = { id ->
            val cat = activeCat ?: return@SakinaAdhkarOasisScreen
            val item = cat.items.getOrNull(id) ?: return@SakinaAdhkarOasisScreen
            val key = "${cat.title}_$id"
            val cur = itemCounts[key] ?: 0
            if (cur < item.targetCount) itemCounts[key] = cur + 1
        }
    )

    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            containerColor = theme.bgCard,
            title = { Text(oasisStrings.searchDesc, fontFamily = SakinaHeadingFont, color = theme.accentBright) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontFamily = SakinaBodyFont, fontSize = 14.sp),
                        cursorBrush = SolidColor(theme.accentBright),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(10.dp)
                    )
                    ADHKAR_CATEGORIES.forEachIndexed { idx, cat ->
                        val title = s.adhkarCatTitles.getOrElse(idx) { cat.title }
                        if (searchQuery.isBlank() || title.contains(searchQuery, ignoreCase = true)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x16FFFFFF))
                                    .clickable {
                                        selectedCategoryIndex = idx
                                        showSearchDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                            ) {
                                Text(title, fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory)
                            }
                        }
                    }
                }
            },
            confirmButton = { SakinaBackIconButton(onClick = { showSearchDialog = false }) }
        )
    }
}
