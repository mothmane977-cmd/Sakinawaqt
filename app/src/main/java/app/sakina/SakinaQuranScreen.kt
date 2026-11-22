package app.sakina
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString

import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

enum class QuranRiwaya(val assetFile: String) {
    HAFS("quran_hafs_full.json"),
    WARSH("quran_warsh_full.json");

    fun displayTitle(s: SakinaStrings): String = when (this) {
        HAFS -> s.riwayaHafs
        WARSH -> s.riwayaWarsh
    }
}

data class SurahEntry(
    val number: Int,
    val nameAr: String,
    val nameLatin: String,
    val isMakki: Boolean,
    val ayahCount: Int,
    val startPage: Int,
    val ayahs: List<String>
) {
    fun localizedName(lang: SakinaLang): String =
        if (lang == SakinaLang.AR || lang == SakinaLang.UR) nameAr else nameLatin

    fun localizedType(s: SakinaStrings): String =
        if (isMakki) s.makkiWord else s.madaniWord
}

private val SURAH_NAMES_AR = listOf(
    "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال",
    "التوبة", "يونس", "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل",
    "الإسراء", "الكهف", "مريم", "طه", "الأنبياء", "الحج", "المؤمنون", "النور",
    "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم", "لقمان", "السجدة",
    "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر",
    "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح",
    "الحجرات", "ق", "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة",
    "الحديد", "المجادلة", "الحشر", "الممتحنة", "الصف", "الجمعة", "المنافقون", "التغابن",
    "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج", "نوح", "الجن",
    "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس",
    "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية",
    "الفجر", "البلد", "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق",
    "القدر", "البينة", "الزلزلة", "العاديات", "القارعة", "التكاثر", "العصر", "الهمزة",
    "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر", "المسد", "الإخلاص",
    "الفلق", "الناس"
)

private val SURAH_AYAH_COUNTS = intArrayOf(
    7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111, 43, 52, 99, 128,
    111, 110, 98, 135, 112, 78, 118, 64, 77, 227, 93, 88, 69, 60, 34, 30,
    73, 54, 45, 83, 182, 88, 75, 85, 54, 53, 89, 59, 37, 35, 38, 29,
    18, 45, 60, 49, 62, 55, 78, 96, 29, 22, 24, 13, 14, 11, 11, 18,
    12, 12, 30, 52, 52, 44, 28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
    29, 19, 36, 25, 22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19,
    5, 8, 8, 11, 11, 8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6
)

// Exact 1405H/1423H Madinah Mushaf starting page (1..604) for each of the 114 Surahs
private val SURAH_START_PAGES = intArrayOf(
    1, 2, 50, 77, 106, 128, 151, 177, 187, 208, 221, 235, 249, 255, 262, 267,
    282, 293, 305, 312, 322, 332, 342, 350, 359, 367, 377, 385, 396, 404, 411, 415,
    418, 428, 434, 440, 446, 453, 458, 467, 477, 483, 489, 496, 499, 502, 507, 511,
    515, 518, 520, 523, 526, 528, 531, 534, 537, 542, 545, 549, 551, 553, 554, 556,
    558, 560, 562, 564, 566, 568, 570, 572, 574, 575, 577, 578, 580, 582, 583, 585,
    586, 587, 587, 589, 590, 591, 591, 592, 593, 594, 595, 595, 596, 596, 597, 597,
    598, 598, 599, 599, 600, 600, 601, 601, 601, 602, 602, 602, 603, 603, 603, 604,
    604, 604
)

private val MADANI_SET = setOf(
    2, 3, 4, 5, 8, 9, 22, 24, 33, 47, 48, 49, 55, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 76, 98, 99, 110
)


private val JUZ_START_PAGES = intArrayOf(
    1, 22, 42, 62, 82, 102, 121, 142, 162, 182,
    201, 222, 242, 262, 282, 302, 322, 342, 362, 382,
    402, 422, 442, 462, 482, 502, 522, 542, 562, 582
)

private val HIZB_START_PAGES = intArrayOf(
    1, 11, 22, 32, 42, 51, 62, 72, 82, 92,
    102, 111, 121, 132, 142, 151, 162, 173, 182, 192,
    201, 212, 222, 231, 242, 252, 262, 272, 282, 292,
    302, 312, 322, 332, 342, 352, 362, 371, 382, 392,
    402, 413, 422, 431, 442, 451, 462, 472, 482, 491,
    502, 513, 522, 531, 542, 553, 562, 572, 582, 591
)

private val quranCache = mutableMapOf<QuranRiwaya, List<SurahEntry>>()

private fun defaultSurahIndex(): List<SurahEntry> = (1..114).map { num ->
    SurahEntry(
        number = num,
        nameAr = SURAH_NAMES_AR[num - 1],
        nameLatin = SURAH_NAMES_LATIN[num - 1],
        isMakki = num !in MADANI_SET,
        ayahCount = SURAH_AYAH_COUNTS[num - 1],
        startPage = SURAH_START_PAGES[num - 1],
        ayahs = emptyList()
    )
}

private fun surahForMushafPage(pageNum: Int): Int {
    for (i in SURAH_START_PAGES.indices.reversed()) {
        if (pageNum >= SURAH_START_PAGES[i]) return i + 1
    }
    return 1
}


// v29 #6: Offline Color-Coded Tajweed Engine (Hafs & Warsh Uthmani Text)
private fun buildTajweedAnnotatedText(raw: String, darkTheme: Boolean): AnnotatedString {
    val cMadd = if (darkTheme) Color(0xFFF87171) else Color(0xFFC62828)      // Red: Madd
    val cGhunnah = if (darkTheme) Color(0xFF4ADE80) else Color(0xFF15803D)   // Green: Ghunnah / Ikhfa
    val cQalqalah = if (darkTheme) Color(0xFF60A5FA) else Color(0xFF1D4ED8)  // Blue: Qalqalah
    val cSilent = if (darkTheme) Color(0xFF9CA3AF) else Color(0xFF78716C)    // Gray: Silent / Wasl
    val qalqalahLetters = setOf('ق', 'ط', 'ب', 'ج', 'د')
    val sukunChars = setOf('ْ', 'ۡ')
    val shadda = 'ّ'
    val maddMark = 'ٓ'
    val tanweenChars = setOf('ً', 'ٌ', 'ٍ', 'ࣰ', 'ࣱ', 'ࣲ')

    return buildAnnotatedString {
        var i = 0
        val n = raw.length
        while (i < n) {
            val ch = raw[i]
            // Collect base char + following Arabic diacritics
            var j = i + 1
            while (j < n && raw[j] in 'ً'..'ٟ' || (j < n && raw[j] in 'ۖ'..'ۭ') || (j < n && raw[j] == 'ٰ')) {
                j++
            }
            val cluster = raw.substring(i, j)
            val color: Color? = when {
                ch == 'آ' || cluster.any { it == maddMark } -> cMadd
                (ch == 'ن' || ch == 'م') && cluster.any { it == shadda } -> cGhunnah
                ch == 'ن' && cluster.any { it in sukunChars } -> cGhunnah
                cluster.any { it in tanweenChars } -> cGhunnah
                ch in qalqalahLetters && cluster.any { it in sukunChars } -> cQalqalah
                ch == 'ٱ' -> cSilent
                else -> null
            }
            if (color != null) {
                withStyle(SpanStyle(color = color)) { append(cluster) }
            } else {
                append(cluster)
            }
            i = j
        }
    }
}

private val TRAILING_AYAH_NUM_REGEX = Regex("\\s*(?:\\([0-9٠-٩]+\\)|﴿[0-9٠-٩]+﴾|\\[[0-9٠-٩]+\\])\\s*$")

private fun cleanAyahText(raw: String): String =
    raw.replace(TRAILING_AYAH_NUM_REGEX, "").trim()

private fun extractAyahText(arr: JSONArray, index: Int): String {
    val item = arr.opt(index)
    val raw = when (item) {
        is String -> item
        is JSONObject -> item.optString("text", item.optString("ayah", ""))
        else -> arr.optString(index, "")
    }
    return cleanAyahText(raw)
}

private fun openQuranJsonText(context: Context, riwaya: QuranRiwaya): String? {
    val candidates = mutableListOf(riwaya.assetFile)
    try {
        val allAssets = context.assets.list("")?.toList().orEmpty()
        val key = if (riwaya == QuranRiwaya.WARSH) "warsh" else "hafs"
        allAssets.filter { it.endsWith(".json", ignoreCase = true) && it.contains(key, ignoreCase = true) }
            .forEach { if (it !in candidates) candidates.add(it) }
        allAssets.filter { it.endsWith(".json", ignoreCase = true) && it.contains("quran", ignoreCase = true) }
            .forEach { if (it !in candidates) candidates.add(it) }
    } catch (_: Exception) {}

    for (name in candidates) {
        try {
            val txt = context.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }.trim()
            if (txt.length > 100) return txt
        } catch (_: Exception) {}
    }
    return null
}

private fun loadMushafAsset(context: Context, riwaya: QuranRiwaya): List<SurahEntry> {
    quranCache[riwaya]?.let { cached ->
        if (cached.any { it.ayahs.isNotEmpty() }) return cached
    }
    return try {
        val raw = openQuranJsonText(context, riwaya) ?: return defaultSurahIndex()
        val parsed: List<SurahEntry> = if (raw.startsWith("{")) {
            val root = JSONObject(raw)
            when {
                root.has("1") -> {
                    (1..114).map { num ->
                        val arr = root.optJSONArray(num.toString())
                        val list = if (arr != null) List(arr.length()) { i -> extractAyahText(arr, i) } else emptyList()
                        SurahEntry(
                            number = num,
                            nameAr = SURAH_NAMES_AR[num - 1],
                            nameLatin = SURAH_NAMES_LATIN[num - 1],
                            isMakki = num !in MADANI_SET,
                            ayahCount = if (list.isNotEmpty()) list.size else SURAH_AYAH_COUNTS[num - 1],
                            startPage = SURAH_START_PAGES[num - 1],
                            ayahs = list
                        )
                    }
                }
                root.has("quran") -> {
                    val verses = root.getJSONArray("quran")
                    val byChapter = Array(115) { mutableListOf<String>() }
                    for (i in 0 until verses.length()) {
                        val v = verses.getJSONObject(i)
                        val ch = v.optInt("chapter", 0)
                        if (ch in 1..114) byChapter[ch].add(cleanAyahText(v.optString("text", "")))
                    }
                    (1..114).map { num ->
                        val list = byChapter[num]
                        SurahEntry(
                            number = num,
                            nameAr = SURAH_NAMES_AR[num - 1],
                            nameLatin = SURAH_NAMES_LATIN[num - 1],
                            isMakki = num !in MADANI_SET,
                            ayahCount = if (list.isNotEmpty()) list.size else SURAH_AYAH_COUNTS[num - 1],
                            startPage = SURAH_START_PAGES[num - 1],
                            ayahs = list
                        )
                    }
                }
                root.has("data") -> {
                    val surahsArr = root.getJSONObject("data").getJSONArray("surahs")
                    (0 until surahsArr.length()).map { i ->
                        val obj = surahsArr.getJSONObject(i)
                        val num = obj.optInt("number", i + 1)
                        val ayArr = obj.getJSONArray("ayahs")
                        val list = List(ayArr.length()) { j -> extractAyahText(ayArr, j) }
                        SurahEntry(
                            number = num,
                            nameAr = SURAH_NAMES_AR.getOrElse(num - 1) { "سورة $num" },
                            nameLatin = SURAH_NAMES_LATIN.getOrElse(num - 1) { "Surah $num" },
                            isMakki = num !in MADANI_SET,
                            ayahCount = if (list.isNotEmpty()) list.size else SURAH_AYAH_COUNTS.getOrElse(num - 1) { 0 },
                            startPage = SURAH_START_PAGES.getOrElse(num - 1) { 1 },
                            ayahs = list
                        )
                    }
                }
                else -> defaultSurahIndex()
            }
        } else {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val num = obj.optInt("number", i + 1)
                val ayArr = obj.getJSONArray("ayahs")
                val list = List(ayArr.length()) { j -> extractAyahText(ayArr, j) }
                SurahEntry(
                    number = num,
                    nameAr = SURAH_NAMES_AR.getOrElse(num - 1) { "سورة $num" },
                    nameLatin = SURAH_NAMES_LATIN.getOrElse(num - 1) { "Surah $num" },
                    isMakki = num !in MADANI_SET,
                    ayahCount = if (list.isNotEmpty()) list.size else SURAH_AYAH_COUNTS.getOrElse(num - 1) { 0 },
                    startPage = SURAH_START_PAGES.getOrElse(num - 1) { 1 },
                    ayahs = list
                )
            }
        }
        if (parsed.any { it.ayahs.isNotEmpty() }) {
            quranCache[riwaya] = parsed
        }
        parsed
    } catch (_: Exception) {
        defaultSurahIndex()
    }
}

// Loads real Madinah Mushaf page PNG (bundled in assets/mushaf/ OR cached locally in filesDir/mushaf_pages/ OR fetched once & cached offline)
private suspend fun loadMushafPageBitmap(ctx: Context, pageNum: Int): ImageBitmap? = withContext(Dispatchers.IO) {
    val padded = pageNum.toString().padStart(3, '0')
    // 1. Check bundled APK assets
    for (assetPath in listOf("mushaf/$padded.webp", "mushaf/page$padded.webp", "mushaf/page$padded.png", "mushaf/$padded.png", "mushaf/$pageNum.png")) {
        try {
            ctx.assets.open(assetPath).use { stream ->
                val bmp = BitmapFactory.decodeStream(stream)
                if (bmp != null) return@withContext bmp.asImageBitmap()
            }
        } catch (_: Exception) {}
    }

    // 2. Check permanent offline cache on device
    val cacheDir = File(ctx.filesDir, "mushaf_pages").apply { mkdirs() }
    val cachedFile = File(cacheDir, "$padded.png")
    if (cachedFile.exists() && cachedFile.length() > 1024L) {
        try {
            val bmp = BitmapFactory.decodeFile(cachedFile.absolutePath)
            if (bmp != null) return@withContext bmp.asImageBitmap()
        } catch (_: Exception) {}
    }

    // 3. Download once from open-source GovarJabbar/Quran-PNG & save permanently offline
    try {
        val url = URL("https://raw.githubusercontent.com/GovarJabbar/Quran-PNG/master/$padded.png")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 10000
            instanceFollowRedirects = true
        }
        if (conn.responseCode == 200) {
            val bytes = conn.inputStream.use { it.readBytes() }
            if (bytes.size > 1024) {
                cachedFile.writeBytes(bytes)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) return@withContext bmp.asImageBitmap()
            }
        }
    } catch (_: Exception) {}

    null
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SakinaQuranScreen(ctx: Context, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    val isArOrUr = lang == SakinaLang.AR || lang == SakinaLang.UR
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var riwaya by remember {
        val saved = sp.getString("quran_riwaya", QuranRiwaya.HAFS.name) ?: QuranRiwaya.HAFS.name
        mutableStateOf(if (saved == QuranRiwaya.WARSH.name) QuranRiwaya.WARSH else QuranRiwaya.HAFS)
    }
    var surahs by remember { mutableStateOf(defaultSurahIndex()) }
    var isReaderOpen by remember { mutableStateOf(false) }
    var targetPageZeroIdx by remember { mutableIntStateOf(sp.getInt("quran_last_page", 0).coerceIn(0, 603)) }
    var searchQuery by remember { mutableStateOf("") }
    var lastReadSurahNum by remember { mutableIntStateOf(sp.getInt("quran_last_surah", 1)) }

    var showSurahPicker by remember { mutableStateOf(false) }
    var indexTab by remember { mutableIntStateOf(0) }
    var showPagePicker by remember { mutableStateOf(false) }
    var showAyahPicker by remember { mutableStateOf(false) }
    var tajweedEnabled by remember { mutableStateOf(sp.getBoolean("v29_tajweed_on", true)) }

    BackHandler(enabled = isReaderOpen) { isReaderOpen = false }

    LaunchedEffect(riwaya) {
        surahs = withContext(Dispatchers.IO) { loadMushafAsset(ctx, riwaya) }
    }

    fun openAtSurah(surahNum: Int) {
        lastReadSurahNum = surahNum
        sp.edit().putInt("quran_last_surah", surahNum).apply()
        val pNum = SURAH_START_PAGES.getOrElse(surahNum - 1) { 1 }
        targetPageZeroIdx = (pNum - 1).coerceIn(0, 603)
        isReaderOpen = true
    }

    val filteredSurahs = remember(surahs, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) surahs
        else surahs.filter {
            it.nameAr.contains(q) || it.nameLatin.lowercase().contains(q) || it.number.toString() == q
        }
    }

    if (!isReaderOpen) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 50.dp, bottom = 74.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // v29 #3: Deep-Emerald & 3D Gold Ornate Cartouche matching Sakina brand palette
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF062920), Color(0xFF0E4A3B), Color(0xFF062920))))
                    .border(1.4.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "❖ ${s.quranTitle} ❖",
                    style = TextStyle(
                        fontFamily = SakinaEditorialFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        brush = SakinaCalligraphyBrush()
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Riwaya Switcher (Hafs / Warsh)
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
                QuranRiwaya.entries.forEach { r ->
                    val selected = r == riwaya
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                if (selected) Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold))
                                else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable {
                                riwaya = r
                                sp.edit().putString("quran_riwaya", r.name).apply()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = r.displayTitle(s),
                            fontFamily = SakinaEditorialFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selected) theme.bgDeep else SakinaBrandColors.LuminousIvory,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(theme.bgCard.copy(alpha = 0.90f))
                    .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(17.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (searchQuery.isEmpty()) {
                        Text(s.searchSurahHint, fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.SoftSilver, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory),
                        cursorBrush = SolidColor(theme.accentBright),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Icon(Icons.Rounded.Close, contentDescription = null, tint = SakinaBrandColors.SoftSilver, modifier = Modifier.size(16.dp).clickable { searchQuery = "" })
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Last Read Card (#4: 40.dp icon)
            val lastSurahObj = surahs.firstOrNull { it.number == lastReadSurahNum }
            val lastSurahDisplay = lastSurahObj?.localizedName(lang)
                ?: if (isArOrUr) SURAH_NAMES_AR.getOrElse(lastReadSurahNum - 1) { "الفاتحة" }
                else SURAH_NAMES_LATIN.getOrElse(lastReadSurahNum - 1) { "Al-Fatihah" }

            SakinaGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isReaderOpen = true },
                cornerRadius = 16.dp,
                glowGold = true,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Image(painter = painterResource(id = R.drawable.ic_adhkar), contentDescription = null, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "${s.lastReadLabel}: $lastSurahDisplay",
                                fontFamily = SakinaHeadingFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = theme.accentBright,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${s.riwayaLabel}: ${riwaya.displayTitle(s)} · ${targetPageZeroIdx + 1}/604".toWesternDigits(),
                                fontFamily = SakinaBodyFont,
                                fontSize = 11.sp,
                                color = SakinaBrandColors.SoftSilver,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold)))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(s.resumeLabel, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = theme.bgDeep, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // v28 #09: Quick Daily Surahs (Kahf, Yasin, Rahman, Waqi'ah) + Surah/Juz/Hizb Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf(
                    18 to sakinaTr(lang, "الكهف", "Kahf", "Al-Kahf", "Kehf", "Al-Kahfi", "الکہف"),
                    36 to sakinaTr(lang, "يس", "Yasin", "Ya-Sin", "Yasin", "Yasin", "یس"),
                    55 to sakinaTr(lang, "الرحمن", "Rahman", "Ar-Rahman", "Rahman", "Ar-Rahman", "الرحمٰن"),
                    56 to sakinaTr(lang, "الواقعة", "Waqi'ah", "Al-Waqi'ah", "Vakıa", "Al-Waqi'ah", "الواقعہ")
                ).forEach { (sNum, sLbl) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xEE041218))
                            .border(0.8.dp, theme.accentGold.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            .clickable { openAtSurah(sNum) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sLbl,
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = theme.accentBright,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xEE041218))
                    .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf(
                    sakinaTr(lang, "السور (114)", "Surahs (114)", "Sourates", "Sureler", "Surah", "سورتیں"),
                    sakinaTr(lang, "الأجزاء (30)", "Juz (30)", "Juz (30)", "Cüz (30)", "Juz (30)", "پارے (30)"),
                    sakinaTr(lang, "الأحزاب (60)", "Hizb (60)", "Hizb (60)", "Hizib (60)", "Hizb (60)", "احزاب (60)")
                ).forEachIndexed { tIdx, tLbl ->
                    val sel = indexTab == tIdx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) theme.accentGold else Color.Transparent)
                            .clickable { indexTab = tIdx },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = SakinaAdvancedPrefs.formatDigits(ctx, tLbl),
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 114 Surahs / 30 Juz / 60 Hizb Index
            SakinaParchmentPanel(
                modifier = Modifier.fillMaxWidth().weight(1f),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (indexTab == 1) {
                        items((1..30).toList(), key = { "juz_$it" }) { juzNum ->
                            val startP = JUZ_START_PAGES[juzNum - 1]
                            val sNum = surahForMushafPage(startP)
                            val sTitle = SURAH_NAMES_AR.getOrElse(sNum - 1) { "" }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        targetPageZeroIdx = (startP - 1).coerceIn(0, 603)
                                        lastReadSurahNum = sNum
                                        sp.edit().putInt("quran_last_surah", sNum).apply()
                                        isReaderOpen = true
                                    }
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SakinaHexagonNumberBadge(number = juzNum, size = 34.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = SakinaAdvancedPrefs.formatDigits(ctx, "${sakinaTr(lang, "الجزء", "Juz", "Juz", "Cüz", "Juz", "پارہ")} $juzNum"),
                                            fontFamily = SakinaEditorialFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = SakinaInkDark
                                        )
                                        Text(
                                            text = "${sakinaTr(lang, "يبدأ بسورة", "Starts at", "Débute à", "Başlangıç", "Mulai di", "آغاز")}: $sTitle",
                                            fontFamily = SakinaBodyFont,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF5D4A2E)
                                        )
                                    }
                                }
                                Text(
                                    text = SakinaAdvancedPrefs.formatDigits(ctx, "${sakinaTr(lang, "ص", "p.", "p.", "s.", "hal.", "ص")} $startP"),
                                    fontFamily = SakinaNumeralFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF0A4D3C)
                                )
                            }
                            HorizontalDivider(color = Color(0x220A2A20))
                        }
                    } else if (indexTab == 2) {
                        items((1..60).toList(), key = { "hizb_$it" }) { hizbNum ->
                            val startP = HIZB_START_PAGES[hizbNum - 1]
                            val sNum = surahForMushafPage(startP)
                            val sTitle = SURAH_NAMES_AR.getOrElse(sNum - 1) { "" }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        targetPageZeroIdx = (startP - 1).coerceIn(0, 603)
                                        lastReadSurahNum = sNum
                                        sp.edit().putInt("quran_last_surah", sNum).apply()
                                        isReaderOpen = true
                                    }
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SakinaHexagonNumberBadge(number = hizbNum, size = 34.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = SakinaAdvancedPrefs.formatDigits(ctx, "${sakinaTr(lang, "الحزب", "Hizb", "Hizb", "Hizib", "Hizb", "حزب")} $hizbNum"),
                                            fontFamily = SakinaEditorialFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = SakinaInkDark
                                        )
                                        Text(
                                            text = "${sakinaTr(lang, "الجزء", "Juz", "Juz", "Cüz", "Juz", "پارہ")} ${((hizbNum - 1) / 2) + 1} · $sTitle",
                                            fontFamily = SakinaBodyFont,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF5D4A2E)
                                        )
                                    }
                                }
                                Text(
                                    text = SakinaAdvancedPrefs.formatDigits(ctx, "${sakinaTr(lang, "ص", "p.", "p.", "s.", "hal.", "ص")} $startP"),
                                    fontFamily = SakinaNumeralFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF0A4D3C)
                                )
                            }
                            HorizontalDivider(color = Color(0x220A2A20))
                        }
                    } else items(filteredSurahs, key = { it.number }) { surah ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openAtSurah(surah.number) }
                                .padding(horizontal = 2.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                                SakinaHexagonNumberBadge(number = surah.number, size = 35.dp)
                                Spacer(modifier = Modifier.width(9.dp))
                                Text(
                                    text = surah.localizedName(lang),
                                    fontFamily = SakinaEditorialFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SakinaInkDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 132.dp, max = 168.dp)
                                    .height(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.bg_cartouche),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.matchParentSize()
                                )
                                Text(
                                    text = "${surah.localizedType(s)} · ${surah.ayahCount}".toWesternDigits(),
                                    fontFamily = SakinaBodyFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.2.sp,
                                    color = theme.accentBright,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 4.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0x22C5A059))
                    }
                }
            }
        }
    } else {
        // Real 604-Page Illustrated Mushaf Reader (#10)
        val pagerState = rememberPagerState(
            initialPage = targetPageZeroIdx.coerceIn(0, 603),
            pageCount = { 604 }
        )

        LaunchedEffect(targetPageZeroIdx) {
            if (pagerState.currentPage != targetPageZeroIdx) {
                pagerState.scrollToPage(targetPageZeroIdx.coerceIn(0, 603))
            }
        }

        LaunchedEffect(pagerState.currentPage) {
            val pNum = pagerState.currentPage + 1
            val sNum = surahForMushafPage(pNum)
            sp.edit().putInt("quran_last_page", pagerState.currentPage).apply()
            lastReadSurahNum = sNum
            sp.edit().putInt("quran_last_surah", sNum).apply()
        }

        val currentPageNum = pagerState.currentPage + 1
        val currentSurahNum = surahForMushafPage(currentPageNum)
        val currentSurahObj = surahs.firstOrNull { it.number == currentSurahNum }

        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 10.dp, end = 10.dp, top = 44.dp, bottom = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top In-Page Mushaf Controls Bar (Back + Surah + Page + Ayah + Riwaya)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(theme.bgDeep.copy(alpha = 0.94f))
                    .border(0.9.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SakinaBackIconButton(onClick = { isReaderOpen = false })
                MushafQuickChip(
                    label = currentSurahObj?.localizedName(lang) ?: SURAH_NAMES_AR[currentSurahNum - 1],
                    modifier = Modifier.weight(1.2f),
                    onClick = { showSurahPicker = true }
                )
                MushafQuickChip(
                    label = if (isArOrUr) "ص $currentPageNum" else "P.$currentPageNum",
                    modifier = Modifier.weight(0.8f),
                    onClick = { showPagePicker = true }
                )
                MushafQuickChip(
                    label = if (isArOrUr) "آية" else "Ayah",
                    modifier = Modifier.weight(0.65f),
                    onClick = { showAyahPicker = true }
                )
                MushafQuickChip(
                    label = if (riwaya == QuranRiwaya.HAFS) (if (isArOrUr) "حفص" else "Hafs") else (if (isArOrUr) "ورش" else "Warsh"),
                    modifier = Modifier.weight(0.7f),
                    highlight = true,
                    onClick = {
                        riwaya = if (riwaya == QuranRiwaya.HAFS) QuranRiwaya.WARSH else QuranRiwaya.HAFS
                        sp.edit().putString("quran_riwaya", riwaya.name).apply()
                    }
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // 604-Page RTL Mushaf Pager
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) { pageIdx ->
                    val pNum = pageIdx + 1
                    val sNumForPage = surahForMushafPage(pNum)
                    val surahForPage = surahs.firstOrNull { it.number == sNumForPage }
                    val mushafMode = remember { SakinaAdvancedPrefs.mushafTheme(ctx) }
                    val fontScale = remember { SakinaAdvancedPrefs.readingFontScale(ctx) }
                    val isDarkPage = mushafMode == 2
                    val pageBg = when (mushafMode) {
                        2 -> Color(0xFF0E1513)
                        1 -> Color(0xFFFAF4E6)
                        else -> Color(0xFFFFFFFF)
                    }
                    val pageInk = if (isDarkPage) Color(0xFFF3EFE6) else SakinaInkDark
                    val readerView = androidx.compose.ui.platform.LocalView.current
                    DisposableEffect(readerView) {
                        val prev = readerView.keepScreenOn
                        if (SakinaAdvancedPrefs.keepScreenOn(ctx)) readerView.keepScreenOn = true
                        onDispose { readerView.keepScreenOn = prev }
                    }

                    // Slice exact ayahs belonging to page pNum within surahForPage
                    val pageAyahs = remember(surahForPage, pNum) {
                        val all = surahForPage?.ayahs.orEmpty()
                        if (all.isEmpty()) emptyList<Pair<Int, String>>()
                        else {
                            val sStart = SURAH_START_PAGES.getOrElse(sNumForPage - 1) { 1 }
                            val sEnd = if (sNumForPage < 114) (SURAH_START_PAGES[sNumForPage] - 1).coerceAtLeast(sStart) else 604
                            val totalPages = (sEnd - sStart + 1).coerceAtLeast(1)
                            val relPage = (pNum - sStart).coerceIn(0, totalPages - 1)
                            val perPage = kotlin.math.ceil(all.size.toDouble() / totalPages.toDouble()).toInt().coerceAtLeast(1)
                            val fromIdx = (relPage * perPage).coerceIn(0, all.size)
                            val toIdx = if (relPage == totalPages - 1) all.size else ((relPage + 1) * perPage).coerceIn(fromIdx, all.size)
                            (fromIdx until toIdx).map { i -> (i + 1) to all[i] }
                        }
                    }

                    SakinaParchmentPanel(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(pageBg)
                                .border(1.2.dp, Color(0x99C5A059), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Ornate Deep-Emerald Surah Header + Tajweed Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0A3B2E))
                                    .border(0.8.dp, Color(0x99C5A059), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SakinaAdvancedPrefs.formatDigits(ctx, "❖ سورة ${SURAH_NAMES_AR[sNumForPage - 1]} · ص $pNum ❖"),
                                    fontFamily = SakinaNaskhFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = theme.accentBright
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (tajweedEnabled) theme.accentGold else Color(0x33FFFFFF))
                                        .clickable {
                                            tajweedEnabled = !tajweedEnabled
                                            sp.edit().putBoolean("v29_tajweed_on", tajweedEnabled).apply()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (tajweedEnabled) sakinaTr(lang, "التجويد ✓", "Tajweed ✓", "Tajwid ✓", "Tecvid ✓", "Tajwid ✓", "تجوید ✓")
                                        else sakinaTr(lang, "التجويد", "Tajweed", "Tajwid", "Tecvid", "Tajwid", "تجوید"),
                                        fontFamily = SakinaBodyFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (tajweedEnabled) theme.bgDeep else Color.White
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            if (pageAyahs.isNotEmpty()) {
                                val fullText = remember(pageAyahs, pNum) {
                                    pageAyahs.joinToString(" ") { (num, body) ->
                                        "$body ﴿${SakinaAdvancedPrefs.formatDigits(ctx, num.toString())}﴾"
                                    }
                                }
                                if (tajweedEnabled) {
                                    val styled = remember(fullText, isDarkPage) { buildTajweedAnnotatedText(fullText, isDarkPage) }
                                    Text(
                                        text = styled,
                                        fontFamily = SakinaNaskhFontFamily,
                                        fontSize = (21f * fontScale).sp,
                                        lineHeight = (39f * fontScale).sp,
                                        color = pageInk,
                                        textAlign = TextAlign.Justify
                                    )
                                } else {
                                    Text(
                                        text = fullText,
                                        fontFamily = SakinaNaskhFontFamily,
                                        fontSize = (21f * fontScale).sp,
                                        lineHeight = (39f * fontScale).sp,
                                        color = pageInk,
                                        textAlign = TextAlign.Justify
                                    )
                                }
                            } else {
                                Text(
                                    text = sakinaTr(lang, "جاري تحميل نص الصفحة...", "Loading page text...", "Chargement...", "Yükleniyor...", "Memuat...", "لوڈ ہو رہا ہے..."),
                                    fontFamily = SakinaNaskhFontFamily,
                                    fontSize = 16.sp,
                                    color = pageInk,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp)
                                )
                            }
                        }

                        // v29 #6: Tajweed Color Legend Bar below Page Frame
                        if (tajweedEnabled) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x160A3B2E))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    Color(0xFFC62828) to sakinaTr(lang, "مدّ", "Madd", "Madd", "Med", "Mad", "مد"),
                                    Color(0xFF15803D) to sakinaTr(lang, "غنة وإخفاء", "Ghunnah", "Ghunna", "Gunne", "Ghunnah", "غنہ"),
                                    Color(0xFF1D4ED8) to sakinaTr(lang, "قلقلة", "Qalqalah", "Qalqala", "Kalkale", "Qalqalah", "قلقلہ"),
                                    Color(0xFF78716C) to sakinaTr(lang, "صامت/وصل", "Silent", "Muet", "Sessiz", "Senyap", "ساکن")
                                ).forEach { (dotCol, lbl) ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(dotCol))
                                        Text(lbl, fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = SakinaInkDark)
                                    }
                                }
                            }
                        }

                        // Bottom Page Navigation Strip
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x180F3D3E))
                                    .clickable(enabled = pageIdx > 0) {
                                        scope.launch { pagerState.animateScrollToPage(pageIdx - 1) }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF0F3D3E), modifier = Modifier.size(16.dp))
                                Text("السابقة", fontFamily = SakinaNaskhFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F3D3E))
                            }
                            Text(
                                text = "﴿ $pNum / 604 ﴾".toWesternDigits(),
                                fontFamily = SakinaNaskhFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color(0xFF6B5224)
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x180F3D3E))
                                    .clickable(enabled = pageIdx < 603) {
                                        scope.launch { pagerState.animateScrollToPage(pageIdx + 1) }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("التالية", fontFamily = SakinaNaskhFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F3D3E))
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = Color(0xFF0F3D3E), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        if (showSurahPicker) {
            AlertDialog(
                onDismissRequest = { showSurahPicker = false },
                containerColor = theme.bgCard,
                title = { Text(s.surahsBtn, fontFamily = SakinaHeadingFont, color = theme.accentBright) },
                text = {
                    LazyColumn(modifier = Modifier.height(340.dp)) {
                        items(surahs, key = { it.number }) { sr ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val p = (sr.startPage - 1).coerceIn(0, 603)
                                        targetPageZeroIdx = p
                                        showSurahPicker = false
                                        scope.launch { pagerState.scrollToPage(p) }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${sr.number}. ${sr.localizedName(lang)}".toWesternDigits(), fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory)
                                Text("${if (isArOrUr) "ص" else "P."} ${sr.startPage}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontSize = 11.sp, color = theme.accentBright)
                            }
                            HorizontalDivider(color = Color(0x22FFFFFF))
                        }
                    }
                },
                confirmButton = { SakinaBackIconButton(onClick = { showSurahPicker = false }) }
            )
        }

        if (showPagePicker) {
            var pageInput by remember { mutableStateOf(currentPageNum.toString()) }
            AlertDialog(
                onDismissRequest = { showPagePicker = false },
                containerColor = theme.bgCard,
                title = { Text(if (isArOrUr) "الانتقال إلى صفحة (1–604)" else "Jump to Page (1–604)", fontFamily = SakinaHeadingFont, color = theme.accentBright) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        BasicTextField(
                            value = pageInput,
                            onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) pageInput = it.toWesternDigits() },
                            textStyle = TextStyle(fontFamily = SakinaNumeralFont, fontSize = 18.sp, color = theme.accentBright, textAlign = TextAlign.Center),
                            cursorBrush = SolidColor(theme.accentBright),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.bgDeep)
                                .border(1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 150, 300, 450, 604).forEach { pNum ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x22FFFFFF))
                                        .clickable {
                                            val t = (pNum - 1).coerceIn(0, 603)
                                            targetPageZeroIdx = t
                                            showPagePicker = false
                                            scope.launch { pagerState.scrollToPage(t) }
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$pNum".toWesternDigits(), fontFamily = SakinaNumeralFont, fontSize = 11.sp, color = theme.accentBright)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.accentGold)
                            .clickable {
                                val parsed = pageInput.toWesternDigits().trim().toIntOrNull() ?: currentPageNum
                                val target = parsed.coerceIn(1, 604) - 1
                                targetPageZeroIdx = target
                                showPagePicker = false
                                scope.launch { pagerState.scrollToPage(target) }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(s.resumeLabel, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, color = theme.bgDeep)
                    }
                }
            )
        }

        if (showAyahPicker) {
            val totalAyahs = currentSurahObj?.ayahCount ?: 7
            var ayahInput by remember { mutableStateOf("1") }
            AlertDialog(
                onDismissRequest = { showAyahPicker = false },
                containerColor = theme.bgCard,
                title = { Text("${currentSurahObj?.localizedName(lang) ?: ""} (1–$totalAyahs)".toWesternDigits(), fontFamily = SakinaHeadingFont, color = theme.accentBright) },
                text = {
                    BasicTextField(
                        value = ayahInput,
                        onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) ayahInput = it.toWesternDigits() },
                        textStyle = TextStyle(fontFamily = SakinaNumeralFont, fontSize = 18.sp, color = theme.accentBright, textAlign = TextAlign.Center),
                        cursorBrush = SolidColor(theme.accentBright),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.bgDeep)
                            .border(1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    )
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.accentGold)
                            .clickable {
                                val targetAy = (ayahInput.toWesternDigits().trim().toIntOrNull() ?: 1).coerceIn(1, totalAyahs)
                                val sStart = SURAH_START_PAGES.getOrElse(currentSurahNum - 1) { 1 }
                                val sEnd = if (currentSurahNum < 114) (SURAH_START_PAGES[currentSurahNum] - 1).coerceAtLeast(sStart) else 604
                                val totalP = (sEnd - sStart + 1).coerceAtLeast(1)
                                val ratio = (targetAy - 1).toFloat() / totalAyahs.coerceAtLeast(1)
                                val estPage = (sStart + (totalP * ratio).toInt() - 1).coerceIn(sStart - 1, sEnd - 1)
                                targetPageZeroIdx = estPage
                                showAyahPicker = false
                                scope.launch { pagerState.scrollToPage(estPage) }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(s.resumeLabel, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, color = theme.bgDeep)
                    }
                }
            )
        }
    }
}

@Composable
private fun MushafQuickChip(
    label: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    val theme = LocalSakinaTheme.current
    Box(
        modifier = modifier
            .height(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlight) theme.accentGold.copy(alpha = 0.25f) else Color(0x1EFFFFFF))
            .border(0.7.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.toWesternDigits(),
            fontFamily = SakinaBodyFont,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            color = theme.accentBright,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
