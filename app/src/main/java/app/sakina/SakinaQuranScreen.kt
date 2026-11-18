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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
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

private val MADANI_SET = setOf(
    2, 3, 4, 5, 8, 9, 22, 24, 33, 47, 48, 49, 55, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 76, 98, 99, 110
)

private val quranCache = mutableMapOf<QuranRiwaya, List<SurahEntry>>()

private fun loadMushafAsset(context: Context, riwaya: QuranRiwaya): List<SurahEntry> {
    quranCache[riwaya]?.let { return it }
    return try {
        val raw = context.assets.open(riwaya.assetFile).bufferedReader(Charsets.UTF_8).use { it.readText() }.trim()
        val parsed = if (raw.startsWith("{") && raw.contains(""quran"")) {
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
                    nameAr = SURAH_NAMES_AR.getOrElse(num - 1) { "سورة $num" },
                    nameLatin = SURAH_NAMES_LATIN.getOrElse(num - 1) { "Surah $num" },
                    isMakki = num !in MADANI_SET,
                    ayahCount = list.size,
                    ayahs = list
                )
            }
        } else {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val num = obj.optInt("number", i + 1)
                val ayArr = obj.getJSONArray("ayahs")
                val list = (0 until ayArr.length()).map { j -> ayArr.getString(j) }
                SurahEntry(
                    number = num,
                    nameAr = SURAH_NAMES_AR.getOrElse(num - 1) { obj.optString("name", "سورة $num") },
                    nameLatin = SURAH_NAMES_LATIN.getOrElse(num - 1) { "Surah $num" },
                    isMakki = num !in MADANI_SET,
                    ayahCount = list.size,
                    ayahs = list
                )
            }
        }
        quranCache[riwaya] = parsed
        parsed
    } catch (_: Exception) {
        emptyList()
    }
}

@Composable
fun SakinaQuranScreen(ctx: Context, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }
    var riwaya by remember {
        val saved = sp.getString("quran_riwaya", QuranRiwaya.HAFS.name) ?: QuranRiwaya.HAFS.name
        mutableStateOf(
            if (saved == QuranRiwaya.WARSH.name) QuranRiwaya.WARSH else QuranRiwaya.HAFS
        )
    }
    var surahs by remember { mutableStateOf<List<SurahEntry>>(emptyList()) }
    var selectedSurahNum by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var lastReadSurahNum by remember { mutableIntStateOf(sp.getInt("quran_last_surah", 1)) }
    val readerScrollState = rememberLazyListState()

    BackHandler(enabled = selectedSurahNum != null) { selectedSurahNum = null }

    LaunchedEffect(riwaya) {
        surahs = withContext(Dispatchers.IO) { loadMushafAsset(ctx, riwaya) }
    }

    val activeSurah = remember(surahs, selectedSurahNum) {
        selectedSurahNum?.let { num -> surahs.firstOrNull { it.number == num } }
    }

    val filteredSurahs = remember(surahs, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) surahs
        else surahs.filter {
            it.nameAr.contains(q) || it.nameLatin.lowercase().contains(q) || it.number.toString() == q
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 50.dp, bottom = 74.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Ornate Cartouche
        Box(modifier = Modifier.fillMaxWidth()) {
            if (activeSurah != null) {
                SakinaBackIconButton(
                    onClick = { selectedSurahNum = null },
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
            SakinaOrnamentCartouche(
                modifier = Modifier.align(Alignment.Center),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 7.dp)
            ) {
                Text(
                    text = activeSurah?.localizedName(lang) ?: s.quranTitle,
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                    color = theme.accentBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
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

        if (activeSurah == null) {
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
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = null,
                        tint = SakinaBrandColors.SoftSilver,
                        modifier = Modifier.size(16.dp).clickable { searchQuery = "" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Last Read Card (#4: Enlarged 40.dp icon so it is bold and clear!)
            val lastSurahObj = surahs.firstOrNull { it.number == lastReadSurahNum }
            val lastSurahDisplay = lastSurahObj?.localizedName(lang)
                ?: if (lang == SakinaLang.AR || lang == SakinaLang.UR) SURAH_NAMES_AR.getOrElse(lastReadSurahNum - 1) { "الفاتحة" }
                else SURAH_NAMES_LATIN.getOrElse(lastReadSurahNum - 1) { "Al-Fatihah" }

            SakinaGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (surahs.isNotEmpty()) selectedSurahNum = lastReadSurahNum
                    },
                cornerRadius = 16.dp,
                glowGold = true,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        // Enlarged 40.dp Quran icon (#4, #19)
                        Image(
                            painter = painterResource(id = R.drawable.ic_adhkar),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
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
                                text = "${s.riwayaLabel}: ${riwaya.displayTitle(s)}",
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
                        Text(
                            text = s.resumeLabel,
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = theme.bgDeep,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 114 Surahs Index with Ornate Hexagon Number Badge (#3) & Enlarged Mini Ornate Cartouche (#2)
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
                                .clickable {
                                    lastReadSurahNum = surah.number
                                    sp.edit().putInt("quran_last_surah", surah.number).apply()
                                    selectedSurahNum = surah.number
                                }
                                .padding(horizontal = 2.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f).padding(end = 6.dp)
                            ) {
                                // Ornate Hexagon Badge for Surah Number at the start/right of Surah name (#3)
                                SakinaHexagonNumberBadge(
                                    number = surah.number,
                                    size = 35.dp
                                )
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

                            // Enlarged Mini Ornate Cartouche (#2): wide enough + safe 26.dp horizontal padding
                            // so "Makki · 7" or "Madani · 286" or "مكية · 286" never touches the gold finials!
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
        } else {
            // Mushaf Reader always uses Classical Naskh (#25)
            SakinaParchmentPanel(
                modifier = Modifier.fillMaxWidth().weight(1f),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                val flowText = remember(activeSurah, riwaya) {
                    activeSurah.ayahs.mapIndexed { idx, ayah ->
                        "$ayah ﴿${idx + 1}﴾"
                    }.joinToString(" ").toWesternDigits()
                }
                LazyColumn(state = readerScrollState, modifier = Modifier.fillMaxSize()) {
                    if (activeSurah.number != 1 && activeSurah.number != 9) {
                        item {
                            Text(
                                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                fontFamily = SakinaNaskhFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF0F3D3E),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                            )
                        }
                    }
                    item {
                        Text(
                            text = flowText,
                            fontFamily = SakinaNaskhFontFamily,
                            fontSize = 21.sp,
                            lineHeight = 38.sp,
                            color = SakinaInkDark,
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
