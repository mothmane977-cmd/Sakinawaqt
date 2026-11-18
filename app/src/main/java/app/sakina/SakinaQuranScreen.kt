package app.sakina

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
    val ayahs: List<String>
) {
    fun localizedName(lang: SakinaLang): String =
        if (lang == SakinaLang.AR || lang == SakinaLang.UR) nameAr else nameLatin

    fun localizedType(s: SakinaStrings): String =
        if (isMakki) s.makkiWord else s.madaniWord
}

private data class MushafAyahRef(
    val surahNum: Int,
    val surahNameAr: String,
    val ayahNum: Int,
    val text: String,
    val isSurahStart: Boolean
)

private data class MushafPageData(
    val pageNumber: Int, // 1..604
    val juzNumber: Int,
    val primarySurahName: String,
    val primarySurahNum: Int,
    val items: List<MushafAyahRef>
)

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

private val MADANI_SET = setOf(
    2, 3, 4, 5, 8, 9, 22, 24, 33, 47, 48, 49, 55, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 76, 98, 99, 110
)

private val quranCache = mutableMapOf<QuranRiwaya, List<SurahEntry>>()

private fun defaultSurahIndex(): List<SurahEntry> = (1..114).map { num ->
    SurahEntry(
        number = num,
        nameAr = SURAH_NAMES_AR[num - 1],
        nameLatin = SURAH_NAMES_LATIN[num - 1],
        isMakki = num !in MADANI_SET,
        ayahCount = SURAH_AYAH_COUNTS[num - 1],
        ayahs = emptyList()
    )
}

private fun loadMushafAsset(context: Context, riwaya: QuranRiwaya): List<SurahEntry> {
    quranCache[riwaya]?.let { return it }
    return try {
        val raw = context.assets.open(riwaya.assetFile).bufferedReader(Charsets.UTF_8).use { it.readText() }.trim()
        val parsed: List<SurahEntry> = when {
            // Format A (used in GitHub Actions build.yml): {"1": ["...", ...], "2": [...], ..., "114": [...]} (#5)
            raw.startsWith("{") && !raw.contains("\"quran\"") -> {
                val root = JSONObject(raw)
                (1..114).map { num ->
                    val arr = root.optJSONArray(num.toString())
                    val list = if (arr != null) List(arr.length()) { i -> arr.getString(i) } else emptyList()
                    SurahEntry(
                        number = num,
                        nameAr = SURAH_NAMES_AR[num - 1],
                        nameLatin = SURAH_NAMES_LATIN[num - 1],
                        isMakki = num !in MADANI_SET,
                        ayahCount = if (list.isNotEmpty()) list.size else SURAH_AYAH_COUNTS[num - 1],
                        ayahs = list
                    )
                }
            }
            // Format B: {"quran": [{"chapter": 1, "verse": 1, "text": "..."}]}
            raw.startsWith("{") && raw.contains("\"quran\"") -> {
                val verses = JSONObject(raw).getJSONArray("quran")
                val byChapter = Array(115) { mutableListOf<String>() }
                for (i in 0 until verses.length()) {
                    val v = verses.getJSONObject(i)
                    val ch = v.optInt("chapter", 0)
                    if (ch in 1..114) byChapter[ch].add(v.optString("text", ""))
                }
                (1..114).map { num ->
                    val list = byChapter[num]
                    SurahEntry(
                        number = num,
                        nameAr = SURAH_NAMES_AR[num - 1],
                        nameLatin = SURAH_NAMES_LATIN[num - 1],
                        isMakki = num !in MADANI_SET,
                        ayahCount = if (list.isNotEmpty()) list.size else SURAH_AYAH_COUNTS[num - 1],
                        ayahs = list
                    )
                }
            }
            // Format C: JSONArray of Surah objects
            else -> {
                val arr = JSONArray(raw)
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    val num = obj.optInt("number", i + 1)
                    val ayArr = obj.getJSONArray("ayahs")
                    val list = (0 until ayArr.length()).map { j -> ayArr.getString(j) }
                    SurahEntry(
                        number = num,
                        nameAr = SURAH_NAMES_AR.getOrElse(num - 1) { "سورة $num" },
                        nameLatin = SURAH_NAMES_LATIN.getOrElse(num - 1) { "Surah $num" },
                        isMakki = num !in MADANI_SET,
                        ayahCount = list.size,
                        ayahs = list
                    )
                }
            }
        }
        quranCache[riwaya] = parsed
        parsed
    } catch (_: Exception) {
        defaultSurahIndex()
    }
}

// Build 604 balanced Mushaf Pages (#10) from the loaded Surahs
private fun buildMushafPages(surahs: List<SurahEntry>): List<MushafPageData> {
    val allRefs = mutableListOf<MushafAyahRef>()
    for (s in surahs) {
        s.ayahs.forEachIndexed { idx, txt ->
            allRefs.add(MushafAyahRef(s.number, s.nameAr, idx + 1, txt, idx == 0))
        }
    }
    if (allRefs.isEmpty()) return emptyList()

    // Page 1 = Al-Fatihah; distribute remaining 603 pages proportionally by character weight
    val fatihah = allRefs.filter { it.surahNum == 1 }
    val rest = allRefs.filter { it.surahNum > 1 }
    val pages = mutableListOf<MushafPageData>()
    pages.add(MushafPageData(1, 1, "الفاتحة", 1, fatihah))

    val totalChars = rest.sumOf { it.text.length + 12 }.coerceAtLeast(1)
    val targetPerPage = totalChars / 603.0
    var cursor = 0
    for (p in 2..604) {
        val remainingPages = 604 - p + 1
        val remainingRefs = rest.size - cursor
        if (remainingRefs <= 0) {
            pages.add(MushafPageData(p, ((p - 1) / 20 + 1).coerceAtMost(30), pages.last().primarySurahName, pages.last().primarySurahNum, emptyList()))
            continue
        }
        if (p == 604 || remainingRefs <= remainingPages) {
            val chunk = if (p == 604) rest.subList(cursor, rest.size) else listOf(rest[cursor])
            cursor += chunk.size
            val first = chunk.first()
            pages.add(MushafPageData(p, ((p - 1) / 20 + 1).coerceAtMost(30), first.surahNameAr, first.surahNum, chunk))
        } else {
            var charSum = 0
            val chunk = mutableListOf<MushafAyahRef>()
            while (cursor < rest.size && (rest.size - cursor) > (604 - p)) {
                val r = rest[cursor]
                if (chunk.isNotEmpty() && charSum + r.text.length > targetPerPage * 1.15) break
                chunk.add(r)
                charSum += r.text.length + 12
                cursor++
                if (charSum >= targetPerPage) break
            }
            if (chunk.isEmpty() && cursor < rest.size) {
                chunk.add(rest[cursor++])
            }
            val first = chunk.first()
            pages.add(MushafPageData(p, ((p - 1) / 20 + 1).coerceAtMost(30), first.surahNameAr, first.surahNum, chunk))
        }
    }
    return pages
}

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
    var mushafPages by remember { mutableStateOf<List<MushafPageData>>(emptyList()) }
    var isReaderOpen by remember { mutableStateOf(false) }
    var targetPageZeroIdx by remember { mutableIntStateOf(sp.getInt("quran_last_page", 0).coerceIn(0, 603)) }
    var searchQuery by remember { mutableStateOf("") }
    var lastReadSurahNum by remember { mutableIntStateOf(sp.getInt("quran_last_surah", 1)) }

    // In-page Mushaf pickers (#10)
    var showSurahPicker by remember { mutableStateOf(false) }
    var showPagePicker by remember { mutableStateOf(false) }
    var showAyahPicker by remember { mutableStateOf(false) }

    BackHandler(enabled = isReaderOpen) { isReaderOpen = false }

    LaunchedEffect(riwaya) {
        val loaded = withContext(Dispatchers.IO) { loadMushafAsset(ctx, riwaya) }
        surahs = loaded
        mushafPages = withContext(Dispatchers.Default) { buildMushafPages(loaded) }
    }

    fun openAtSurah(surahNum: Int, ayahNum: Int = 1) {
        lastReadSurahNum = surahNum
        sp.edit().putInt("quran_last_surah", surahNum).apply()
        val foundIdx = mushafPages.indexOfFirst { p ->
            p.items.any { it.surahNum == surahNum && it.ayahNum >= ayahNum }
        }
        targetPageZeroIdx = if (foundIdx >= 0) foundIdx else 0
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
            SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 36.dp, vertical = 7.dp)) {
                Text(
                    text = s.quranTitle,
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                    color = theme.accentBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
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

            // 114 Surahs Index (#2, #3, #5)
            SakinaParchmentPanel(
                modifier = Modifier.fillMaxWidth().weight(1f),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredSurahs, key = { it.number }) { surah ->
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
        // #10: Ornate Illustrated Mushaf Page View (1..604) with in-page controls for Surah, Page, Ayah & Riwaya!
        val pageCount = mushafPages.size.coerceAtLeast(1)
        val pagerState = rememberPagerState(
            initialPage = targetPageZeroIdx.coerceIn(0, pageCount - 1),
            pageCount = { pageCount }
        )

        LaunchedEffect(pagerState.currentPage, mushafPages) {
            if (mushafPages.isNotEmpty()) {
                val pg = mushafPages[pagerState.currentPage]
                sp.edit().putInt("quran_last_page", pagerState.currentPage).apply()
                lastReadSurahNum = pg.primarySurahNum
                sp.edit().putInt("quran_last_surah", pg.primarySurahNum).apply()
            }
        }

        val currentPageObj = mushafPages.getOrNull(pagerState.currentPage)

        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, top = 46.dp, bottom = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top In-Page Mushaf Controls Bar (#10): Back + Surah + Page + Ayah + Riwaya
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(theme.bgDeep.copy(alpha = 0.92f))
                    .border(0.9.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SakinaBackIconButton(onClick = { isReaderOpen = false })
                MushafQuickChip(
                    label = currentPageObj?.primarySurahName ?: "الفاتحة",
                    modifier = Modifier.weight(1.2f),
                    onClick = { showSurahPicker = true }
                )
                MushafQuickChip(
                    label = if (isArOrUr) "ص ${pagerState.currentPage + 1}" else "P.${pagerState.currentPage + 1}",
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

            Spacer(modifier = Modifier.height(6.dp))

            // Illustrated Mushaf Page Frame (RTL HorizontalPager)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) { pageIdx ->
                    val pageData = mushafPages.getOrNull(pageIdx)
                    SakinaParchmentPanel(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        if (pageData == null || pageData.items.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "جاري تجهيز صفحات المصحف الشريف...",
                                    fontFamily = SakinaNaskhFontFamily,
                                    fontSize = 16.sp,
                                    color = SakinaInkDark
                                )
                            }
                        } else {
                            // Top Page Header (Juz | Surah Name | Page Number)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.8.dp, Color(0x66C5A059), RoundedCornerShape(8.dp))
                                    .background(Color(0x14C5A059), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الجزء ${pageData.juzNumber}".toWesternDigits(),
                                    fontFamily = SakinaNaskhFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF0F3D3E)
                                )
                                Text(
                                    text = "سورة ${pageData.primarySurahName}",
                                    fontFamily = SakinaNaskhFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F3D3E)
                                )
                                Text(
                                    text = "صفحة ${pageData.pageNumber} / 604".toWesternDigits(),
                                    fontFamily = SakinaNaskhFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B5224)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Inner Double-Line Manuscript Frame
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .border(1.1.dp, Color(0x99C5A059), RoundedCornerShape(10.dp))
                                    .padding(3.dp)
                                    .border(0.6.dp, Color(0x660F3D3E), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    // Group contiguous ayahs by Surah so each new Surah gets its Ornate Surah Header Banner
                                    val grouped = remember(pageData) {
                                        val out = mutableListOf<Pair<Int, List<MushafAyahRef>>>()
                                        var curNum = -1
                                        var curList = mutableListOf<MushafAyahRef>()
                                        for (item in pageData.items) {
                                            if (item.surahNum != curNum) {
                                                if (curList.isNotEmpty()) out.add(curNum to curList)
                                                curNum = item.surahNum
                                                curList = mutableListOf(item)
                                            } else {
                                                curList.add(item)
                                            }
                                        }
                                        if (curList.isNotEmpty()) out.add(curNum to curList)
                                        out
                                    }

                                    grouped.forEach { (sNum, refs) ->
                                        if (refs.first().isSurahStart) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF0F3D3E))
                                                    .border(1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(10.dp))
                                                    .padding(vertical = 5.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "❖ سورة ${refs.first().surahNameAr} ❖",
                                                    fontFamily = SakinaNaskhFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = theme.accentBright
                                                )
                                            }
                                            if (sNum != 1 && sNum != 9) {
                                                Text(
                                                    text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                                    fontFamily = SakinaNaskhFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = Color(0xFF0F3D3E),
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                                )
                                            }
                                        }
                                        val pageText = refs.joinToString(" ") { ref ->
                                            "${ref.text} ﴿${ref.ayahNum}﴾"
                                        }.toWesternDigits()
                                        Text(
                                            text = pageText,
                                            fontFamily = SakinaNaskhFontFamily,
                                            fontSize = 19.sp,
                                            lineHeight = 35.sp,
                                            color = SakinaInkDark,
                                            textAlign = TextAlign.Justify,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Bottom Page Turn Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                    text = "﴿ ${pageData.pageNumber} ﴾".toWesternDigits(),
                                    fontFamily = SakinaNaskhFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B5224)
                                )
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x180F3D3E))
                                        .clickable(enabled = pageIdx < pageCount - 1) {
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
        }

        // In-page Surah Picker Dialog
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
                                        showSurahPicker = false
                                        val idx = mushafPages.indexOfFirst { p -> p.items.any { it.surahNum == sr.number } }.coerceAtLeast(0)
                                        scope.launch { pagerState.scrollToPage(idx) }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${sr.number}. ${sr.localizedName(lang)}".toWesternDigits(), fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory)
                                Text("${sr.ayahCount} ${s.ayahWord}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontSize = 11.sp, color = theme.accentBright)
                            }
                            HorizontalDivider(color = Color(0x22FFFFFF))
                        }
                    }
                },
                confirmButton = { SakinaBackIconButton(onClick = { showSurahPicker = false }) }
            )
        }

        // In-page Page Picker Dialog (1..604)
        if (showPagePicker) {
            var pageInput by remember { mutableStateOf((pagerState.currentPage + 1).toString()) }
            AlertDialog(
                onDismissRequest = { showPagePicker = false },
                containerColor = theme.bgCard,
                title = { Text(if (isArOrUr) "الانتقال إلى صفحة (1–604)" else "Jump to Page (1–604)", fontFamily = SakinaHeadingFont, color = theme.accentBright) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        BasicTextField(
                            value = pageInput,
                            onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) pageInput = it },
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
                                            showPagePicker = false
                                            scope.launch { pagerState.scrollToPage((pNum - 1).coerceIn(0, pageCount - 1)) }
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
                                val target = (pageInput.toIntOrNull() ?: 1).coerceIn(1, pageCount) - 1
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

        // In-page Ayah Picker Dialog
        if (showAyahPicker && currentPageObj != null) {
            val curSurah = surahs.firstOrNull { it.number == currentPageObj.primarySurahNum }
            val totalAyahs = curSurah?.ayahCount ?: 7
            var ayahInput by remember { mutableStateOf("1") }
            AlertDialog(
                onDismissRequest = { showAyahPicker = false },
                containerColor = theme.bgCard,
                title = { Text("${curSurah?.localizedName(lang) ?: ""} (1–$totalAyahs)".toWesternDigits(), fontFamily = SakinaHeadingFont, color = theme.accentBright) },
                text = {
                    BasicTextField(
                        value = ayahInput,
                        onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) ayahInput = it },
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
                                val targetAy = (ayahInput.toIntOrNull() ?: 1).coerceIn(1, totalAyahs)
                                val idx = mushafPages.indexOfFirst { p ->
                                    p.items.any { it.surahNum == currentPageObj.primarySurahNum && it.ayahNum >= targetAy }
                                }.coerceAtLeast(0)
                                showAyahPicker = false
                                scope.launch { pagerState.scrollToPage(idx) }
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
