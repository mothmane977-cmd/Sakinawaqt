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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

private enum class QuranRiwaya(val key: String, val assetFile: String) {
    HAFS("hafs", "quran_hafs.json"),
    WARSH("warsh", "quran_warsh.json");

    fun label(s: SakinaStrings): String = if (this == HAFS) s.riwayaHafs else s.riwayaWarsh
}

private data class SurahMeta(val number: Int, val nameAr: String, val ayahs: Int, val isMadani: Boolean) {
    fun displayName(lang: SakinaLang): String =
        if (lang.isRtl) nameAr else SURAH_NAMES_LATIN.getOrElse(number - 1) { nameAr }
}

private val MADANI_NUMBERS = setOf(2, 3, 4, 5, 8, 9, 13, 22, 24, 33, 47, 48, 49, 55, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 76, 98, 99, 110)

private fun normalizeSearch(input: String): String {
    return input.lowercase()
        .replace(Regex("[\u064B-\u065F\u0670'-]"), "")
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
        .replace('ة', 'ه').replace('ى', 'ي')
        .trim()
}

private val ALL_SURAHS = listOf(
    SurahMeta(1, "سورة الفاتحة", 7, false), SurahMeta(2, "سورة البقرة", 286, true), SurahMeta(3, "سورة آل عمران", 200, true),
    SurahMeta(4, "سورة النساء", 176, true), SurahMeta(5, "سورة المائدة", 120, true), SurahMeta(6, "سورة الأنعام", 165, false),
    SurahMeta(7, "سورة الأعراف", 206, false), SurahMeta(8, "سورة الأنفال", 75, true), SurahMeta(9, "سورة التوبة", 129, true),
    SurahMeta(10, "سورة يونس", 109, false), SurahMeta(11, "سورة هود", 123, false), SurahMeta(12, "سورة يوسف", 111, false),
    SurahMeta(13, "سورة الرعد", 43, true), SurahMeta(14, "سورة إبراهيم", 52, false), SurahMeta(15, "سورة الحجر", 99, false),
    SurahMeta(16, "سورة النحل", 128, false), SurahMeta(17, "سورة الإسراء", 111, false), SurahMeta(18, "سورة الكهف", 110, false),
    SurahMeta(19, "سورة مريم", 98, false), SurahMeta(20, "سورة طه", 135, false), SurahMeta(21, "سورة الأنبياء", 112, false),
    SurahMeta(22, "سورة الحج", 78, true), SurahMeta(23, "سورة المؤمنون", 118, false), SurahMeta(24, "سورة النور", 64, true),
    SurahMeta(25, "سورة الفرقان", 77, false), SurahMeta(26, "سورة الشعراء", 227, false), SurahMeta(27, "سورة النمل", 93, false),
    SurahMeta(28, "سورة القصص", 88, false), SurahMeta(29, "سورة العنكبوت", 69, false), SurahMeta(30, "سورة الروم", 60, false),
    SurahMeta(31, "سورة لقمان", 34, false), SurahMeta(32, "سورة السجدة", 30, false), SurahMeta(33, "سورة الأحزاب", 73, true),
    SurahMeta(34, "سورة سبأ", 54, false), SurahMeta(35, "سورة فاطر", 45, false), SurahMeta(36, "سورة يس", 83, false),
    SurahMeta(37, "سورة الصافات", 182, false), SurahMeta(38, "سورة ص", 88, false), SurahMeta(39, "سورة الزمر", 75, false),
    SurahMeta(40, "سورة غافر", 85, false), SurahMeta(41, "سورة فصلت", 54, false), SurahMeta(42, "سورة الشورى", 53, false),
    SurahMeta(43, "سورة الزخرف", 89, false), SurahMeta(44, "سورة الدخان", 59, false), SurahMeta(45, "سورة الجاثية", 37, false),
    SurahMeta(46, "سورة الأحقاف", 35, false), SurahMeta(47, "سورة محمد", 38, true), SurahMeta(48, "سورة الفتح", 29, true),
    SurahMeta(49, "سورة الحجرات", 18, true), SurahMeta(50, "سورة ق", 45, false), SurahMeta(51, "سورة الذاريات", 60, false),
    SurahMeta(52, "سورة الطور", 49, false), SurahMeta(53, "سورة النجم", 62, false), SurahMeta(54, "سورة القمر", 55, false),
    SurahMeta(55, "سورة الرحمن", 78, true), SurahMeta(56, "سورة الواقعة", 96, false), SurahMeta(57, "سورة الحديد", 29, true),
    SurahMeta(58, "سورة المجادلة", 22, true), SurahMeta(59, "سورة الحشر", 24, true), SurahMeta(60, "سورة الممتحنة", 13, true),
    SurahMeta(61, "سورة الصف", 14, true), SurahMeta(62, "سورة الجمعة", 11, true), SurahMeta(63, "سورة المنافقون", 11, true),
    SurahMeta(64, "سورة التغابن", 18, true), SurahMeta(65, "سورة الطلاق", 12, true), SurahMeta(66, "سورة التحريم", 12, true),
    SurahMeta(67, "سورة الملك", 30, false), SurahMeta(68, "سورة القلم", 52, false), SurahMeta(69, "سورة الحاقة", 52, false),
    SurahMeta(70, "سورة المعارج", 44, false), SurahMeta(71, "سورة نوح", 28, false), SurahMeta(72, "سورة الجن", 28, false),
    SurahMeta(73, "سورة المزمل", 20, false), SurahMeta(74, "سورة المدثر", 56, false), SurahMeta(75, "سورة القيامة", 40, false),
    SurahMeta(76, "سورة الإنسان", 31, true), SurahMeta(77, "سورة المرسلات", 50, false), SurahMeta(78, "سورة النبأ", 40, false),
    SurahMeta(79, "سورة النازعات", 46, false), SurahMeta(80, "سورة عبس", 42, false), SurahMeta(81, "سورة التكوير", 29, false),
    SurahMeta(82, "سورة الانفطار", 19, false), SurahMeta(83, "سورة المطففين", 36, false), SurahMeta(84, "سورة الانشقاق", 25, false),
    SurahMeta(85, "سورة البروج", 22, false), SurahMeta(86, "سورة الطارق", 17, false), SurahMeta(87, "سورة الأعلى", 19, false),
    SurahMeta(88, "سورة الغاشية", 26, false), SurahMeta(89, "سورة الفجر", 30, false), SurahMeta(90, "سورة البلد", 20, false),
    SurahMeta(91, "سورة الشمس", 15, false), SurahMeta(92, "سورة الليل", 21, false), SurahMeta(93, "سورة الضحى", 11, false),
    SurahMeta(94, "سورة الشرح", 8, false), SurahMeta(95, "سورة التين", 8, false), SurahMeta(96, "سورة العلق", 19, false),
    SurahMeta(97, "سورة القدر", 5, false), SurahMeta(98, "سورة البينة", 8, true), SurahMeta(99, "سورة الزلزلة", 8, true),
    SurahMeta(100, "سورة العاديات", 11, false), SurahMeta(101, "سورة القارعة", 11, false), SurahMeta(102, "سورة التكاثر", 8, false),
    SurahMeta(103, "سورة العصر", 3, false), SurahMeta(104, "سورة الهمزة", 9, false), SurahMeta(105, "سورة الفيل", 5, false),
    SurahMeta(106, "سورة قريش", 4, false), SurahMeta(107, "سورة الماعون", 7, false), SurahMeta(108, "سورة الكوثر", 3, false),
    SurahMeta(109, "سورة الكافرون", 6, false), SurahMeta(110, "سورة النصر", 3, true), SurahMeta(111, "سورة المسد", 5, false),
    SurahMeta(112, "سورة الإخلاص", 4, false), SurahMeta(113, "سورة الفلق", 5, false), SurahMeta(114, "سورة الناس", 6, false)
)

private fun loadSurahAyahs(ctx: Context, riwaya: QuranRiwaya, surahNum: Int): List<String> {
    try {
        ctx.assets.open(riwaya.assetFile).bufferedReader().use { reader ->
            val root = JSONObject(reader.readText())
            val arr = root.optJSONArray(surahNum.toString())
            if (arr != null && arr.length() > 0) return List(arr.length()) { i -> arr.getString(i).toWesternDigits() }
        }
    } catch (_: Exception) { }
    return listOf("افتح السورة بعد إتمام البناء في Actions لتضمين ملف ${riwaya.assetFile} أوفلاين.")
}

@Composable
fun SakinaQuranScreen(ctx: Context, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val lang = LocalSakinaLang.current
    val s = LocalSakinaStrings.current
    val sp = remember { ctx.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE) }
    var riwaya by remember { mutableStateOf(if (sp.getString("riwaya", "hafs") == "warsh") QuranRiwaya.WARSH else QuranRiwaya.HAFS) }
    var lastSurahNum by remember { mutableIntStateOf(sp.getInt("last_surah", 1)) }
    var openedSurah by remember { mutableStateOf<SurahMeta?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    BackHandler(enabled = openedSurah != null) { openedSurah = null }

    if (openedSurah != null) {
        val surah = openedSurah!!
        val ayahs = remember(surah.number, riwaya) { loadSurahAyahs(ctx, riwaya, surah.number) }
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 54.dp, bottom = 78.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SakinaBackIconButton(onClick = { openedSurah = null })
                Text("${surah.displayName(lang)} · ${riwaya.label(s)}", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = theme.accentBright, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = null,
                    tint = theme.accentBright,
                    modifier = Modifier.size(22.dp).clickable {
                        lastSurahNum = surah.number
                        sp.edit().putInt("last_surah", surah.number).putString("riwaya", riwaya.key).apply()
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            SakinaParchmentPanel(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ayahs.forEach { ayah ->
                        Text(ayah.toWesternDigits(), fontFamily = SakinaEditorialFont, fontSize = 19.sp, lineHeight = 34.sp, color = SakinaInkDark, textAlign = TextAlign.Justify, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
                        HorizontalDivider(color = Color(0x22C5A059))
                    }
                }
            }
        }
        return
    }

    val filteredSurahs = remember(searchQuery, lang) {
        val normQ = normalizeSearch(searchQuery)
        if (normQ.isEmpty()) ALL_SURAHS
        else ALL_SURAHS.filter {
            normalizeSearch(it.nameAr).contains(normQ) ||
                normalizeSearch(it.displayName(lang)).contains(normQ) ||
                it.number.toString() == normQ
        }
    }
    val lastSurahMeta = ALL_SURAHS.firstOrNull { it.number == lastSurahNum } ?: ALL_SURAHS.first()

    Column(
        modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, top = 52.dp, bottom = 76.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 32.dp, vertical = 6.dp)) {
            Text(s.quranTitle, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = theme.accentBright, maxLines = 1)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(34.dp).clip(RoundedCornerShape(17.dp)).background(theme.bgCard).border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(17.dp)).padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            QuranRiwaya.entries.forEach { r ->
                val sel = r == riwaya
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(14.dp)).background(
                        if (sel) Brush.horizontalGradient(listOf(theme.accentGold, theme.accentBright)) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    ).clickable { riwaya = r; sp.edit().putString("riwaya", r.key).apply() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(r.label(s), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = if (sel) theme.bgDeep else theme.accentBright, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(18.dp)).background(theme.bgCard).border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(18.dp)).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = theme.accentGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isEmpty()) Text(s.searchSurahHint, fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.SoftSilver, maxLines = 1)
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory),
                    cursorBrush = SolidColor(theme.accentBright),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // #14: Cleanly formatted Last Read card across all 6 languages
        SakinaGlassCard(modifier = Modifier.fillMaxWidth().clickable { openedSurah = lastSurahMeta }, cornerRadius = 16.dp, glowGold = true, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Image(painter = painterResource(id = R.drawable.ic_adhkar), contentDescription = null, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${s.lastReadLabel}: ${lastSurahMeta.displayName(lang)}", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = theme.accentBright, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${s.riwayaLabel}: ${riwaya.label(s)}", fontFamily = SakinaBodyFont, fontSize = 10.5.sp, color = SakinaBrandColors.SoftSilver, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Brush.horizontalGradient(listOf(theme.accentGold, theme.accentBright))).padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text(s.resumeLabel, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = theme.bgDeep, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // #3 & #14: 114 Surahs with Makki/Madani + Ayah count inside a mini ornate Cartouche!
        SakinaParchmentPanel(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredSurahs) { surah ->
                    val typeLabel = if (surah.isMadani) s.madaniWord else s.makkiWord
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            lastSurahNum = surah.number
                            sp.edit().putInt("last_surah", surah.number).putString("riwaya", riwaya.key).apply()
                            openedSurah = surah
                        }.padding(horizontal = 4.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(theme.bgDeep).border(1.dp, Sakina3DGoldBevelBrush, CircleShape), contentAlignment = Alignment.Center) {
                                Text("${surah.number}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = theme.accentBright)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(surah.displayName(lang), fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SakinaInkDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        // Mini ornate cartouche badge for Makki/Madani + Ayah count (#3)
                        Box(
                            modifier = Modifier.width(118.dp).height(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.bg_cartouche),
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.matchParentSize()
                            )
                            Text(
                                text = "$typeLabel · ${surah.ayahs} ${s.ayahWord}".toWesternDigits(),
                                fontFamily = SakinaBodyFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = theme.accentBright,
                                maxLines = 1
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0x22C5A059))
                }
            }
        }
    }
}
