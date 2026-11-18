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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

private enum class QuranRiwaya(val labelAr: String, val key: String, val assetFile: String) {
    HAFS("حفص عن عاصم", "hafs", "quran_hafs.json"),
    WARSH("ورش عن نافع", "warsh", "quran_warsh.json")
}

private data class SurahMeta(val number: Int, val name: String, val ayahs: Int)

private fun normalizeArabicForSearch(input: String): String {
    return input
        .replace(Regex("[\u064B-\u065F\u0670]"), "")
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
        .replace('ة', 'ه').replace('ى', 'ي')
        .trim()
}

private val ALL_SURAHS = listOf(
    SurahMeta(1, "سورة الفاتحة", 7), SurahMeta(2, "سورة البقرة", 286), SurahMeta(3, "سورة آل عمران", 200),
    SurahMeta(4, "سورة النساء", 176), SurahMeta(5, "سورة المائدة", 120), SurahMeta(6, "سورة الأنعام", 165),
    SurahMeta(7, "سورة الأعراف", 206), SurahMeta(8, "سورة الأنفال", 75), SurahMeta(9, "سورة التوبة", 129),
    SurahMeta(10, "سورة يونس", 109), SurahMeta(11, "سورة هود", 123), SurahMeta(12, "سورة يوسف", 111),
    SurahMeta(13, "سورة الرعد", 43), SurahMeta(14, "سورة إبراهيم", 52), SurahMeta(15, "سورة الحجر", 99),
    SurahMeta(16, "سورة النحل", 128), SurahMeta(17, "سورة الإسراء", 111), SurahMeta(18, "سورة الكهف", 110),
    SurahMeta(19, "سورة مريم", 98), SurahMeta(20, "سورة طه", 135), SurahMeta(21, "سورة الأنبياء", 112),
    SurahMeta(22, "سورة الحج", 78), SurahMeta(23, "سورة المؤمنون", 118), SurahMeta(24, "سورة النور", 64),
    SurahMeta(25, "سورة الفرقان", 77), SurahMeta(26, "سورة الشعراء", 227), SurahMeta(27, "سورة النمل", 93),
    SurahMeta(28, "سورة القصص", 88), SurahMeta(29, "سورة العنكبوت", 69), SurahMeta(30, "سورة الروم", 60),
    SurahMeta(31, "سورة لقمان", 34), SurahMeta(32, "سورة السجدة", 30), SurahMeta(33, "سورة الأحزاب", 73),
    SurahMeta(34, "سورة سبأ", 54), SurahMeta(35, "سورة فاطر", 45), SurahMeta(36, "سورة يس", 83),
    SurahMeta(37, "سورة الصافات", 182), SurahMeta(38, "سورة ص", 88), SurahMeta(39, "سورة الزمر", 75),
    SurahMeta(40, "سورة غافر", 85), SurahMeta(41, "سورة فصلت", 54), SurahMeta(42, "سورة الشورى", 53),
    SurahMeta(43, "سورة الزخرف", 89), SurahMeta(44, "سورة الدخان", 59), SurahMeta(45, "سورة الجاثية", 37),
    SurahMeta(46, "سورة الأحقاف", 35), SurahMeta(47, "سورة محمد", 38), SurahMeta(48, "سورة الفتح", 29),
    SurahMeta(49, "سورة الحجرات", 18), SurahMeta(50, "سورة ق", 45), SurahMeta(51, "سورة الذاريات", 60),
    SurahMeta(52, "سورة الطور", 49), SurahMeta(53, "سورة النجم", 62), SurahMeta(54, "سورة القمر", 55),
    SurahMeta(55, "سورة الرحمن", 78), SurahMeta(56, "سورة الواقعة", 96), SurahMeta(57, "سورة الحديد", 29),
    SurahMeta(58, "سورة المجادلة", 22), SurahMeta(59, "سورة الحشر", 24), SurahMeta(60, "سورة الممتحنة", 13),
    SurahMeta(61, "سورة الصف", 14), SurahMeta(62, "سورة الجمعة", 11), SurahMeta(63, "سورة المنافقون", 11),
    SurahMeta(64, "سورة التغابن", 18), SurahMeta(65, "سورة الطلاق", 12), SurahMeta(66, "سورة التحريم", 12),
    SurahMeta(67, "سورة الملك", 30), SurahMeta(68, "سورة القلم", 52), SurahMeta(69, "سورة الحاقة", 52),
    SurahMeta(70, "سورة المعارج", 44), SurahMeta(71, "سورة نوح", 28), SurahMeta(72, "سورة الجن", 28),
    SurahMeta(73, "سورة المزمل", 20), SurahMeta(74, "سورة المدثر", 56), SurahMeta(75, "سورة القيامة", 40),
    SurahMeta(76, "سورة الإنسان", 31), SurahMeta(77, "سورة المرسلات", 50), SurahMeta(78, "سورة النبأ", 40),
    SurahMeta(79, "سورة النازعات", 46), SurahMeta(80, "سورة عبس", 42), SurahMeta(81, "سورة التكوير", 29),
    SurahMeta(82, "سورة الانفطار", 19), SurahMeta(83, "سورة المطففين", 36), SurahMeta(84, "سورة الانشقاق", 25),
    SurahMeta(85, "سورة البروج", 22), SurahMeta(86, "سورة الطارق", 17), SurahMeta(87, "سورة الأعلى", 19),
    SurahMeta(88, "سورة الغاشية", 26), SurahMeta(89, "سورة الفجر", 30), SurahMeta(90, "سورة البلد", 20),
    SurahMeta(91, "سورة الشمس", 15), SurahMeta(92, "سورة الليل", 21), SurahMeta(93, "سورة الضحى", 11),
    SurahMeta(94, "سورة الشرح", 8), SurahMeta(95, "سورة التين", 8), SurahMeta(96, "سورة العلق", 19),
    SurahMeta(97, "سورة القدر", 5), SurahMeta(98, "سورة البينة", 8), SurahMeta(99, "سورة الزلزلة", 8),
    SurahMeta(100, "سورة العاديات", 11), SurahMeta(101, "سورة القارعة", 11), SurahMeta(102, "سورة التكاثر", 8),
    SurahMeta(103, "سورة العصر", 3), SurahMeta(104, "سورة الهمزة", 9), SurahMeta(105, "سورة الفيل", 5),
    SurahMeta(106, "سورة قريش", 4), SurahMeta(107, "سورة الماعون", 7), SurahMeta(108, "سورة الكوثر", 3),
    SurahMeta(109, "سورة الكافرون", 6), SurahMeta(110, "سورة النصر", 3), SurahMeta(111, "سورة المسد", 5),
    SurahMeta(112, "سورة الإخلاص", 4), SurahMeta(113, "سورة الفلق", 5), SurahMeta(114, "سورة الناس", 6)
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
                .padding(start = 18.dp, end = 18.dp, top = 74.dp, bottom = 82.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.bgCard)
                        .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(16.dp))
                        .clickable { openedSurah = null }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "السور", tint = theme.accentBright, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("السور", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.accentBright)
                }
                Text("${surah.name} · ${riwaya.labelAr}", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = theme.accentBright)
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = "تذكر",
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

    val filteredSurahs = remember(searchQuery) {
        val normQ = normalizeArabicForSearch(searchQuery)
        if (normQ.isEmpty()) ALL_SURAHS
        else ALL_SURAHS.filter {
            normalizeArabicForSearch(it.name).contains(normQ) || it.number.toString() == normQ
        }
    }
    val lastSurahMeta = ALL_SURAHS.firstOrNull { it.number == lastSurahNum } ?: ALL_SURAHS.first()

    Column(
        modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(start = 18.dp, end = 18.dp, top = 74.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp)) {
            Text(LocalSakinaStrings.current.quranTitle, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = theme.accentBright)
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(18.dp)).background(theme.bgCard).border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(18.dp)).padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            QuranRiwaya.entries.forEach { r ->
                val sel = r == riwaya
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(15.dp)).background(
                        if (sel) Brush.horizontalGradient(listOf(theme.accentGold, theme.accentBright)) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    ).clickable { riwaya = r; sp.edit().putString("riwaya", r.key).apply() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(r.labelAr, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (sel) theme.bgDeep else theme.accentBright)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape(19.dp)).background(theme.bgCard).border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(19.dp)).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = theme.accentGold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isEmpty()) Text(LocalSakinaStrings.current.searchSurahHint, fontFamily = SakinaBodyFont, fontSize = 12.sp, color = SakinaBrandColors.SoftSilver)
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = SakinaBodyFont, fontSize = 13.sp, color = SakinaBrandColors.LuminousIvory),
                    cursorBrush = SolidColor(theme.accentBright),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SakinaGlassCard(modifier = Modifier.fillMaxWidth().clickable { openedSurah = lastSurahMeta }, cornerRadius = 16.dp, glowGold = true, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(id = R.drawable.ic_adhkar), contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("${LocalSakinaStrings.current.lastReadLabel}: ${lastSurahMeta.name}", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.accentBright)
                        Text("الرواية: ${riwaya.labelAr}", fontFamily = SakinaBodyFont, fontSize = 11.sp, color = SakinaBrandColors.SoftSilver)
                    }
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Brush.horizontalGradient(listOf(theme.accentGold, theme.accentBright))).padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text(LocalSakinaStrings.current.resumeLabel, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = theme.bgDeep)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SakinaParchmentPanel(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredSurahs) { s ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            lastSurahNum = s.number
                            sp.edit().putInt("last_surah", s.number).putString("riwaya", riwaya.key).apply()
                            openedSurah = s
                        }.padding(horizontal = 6.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(theme.bgDeep).border(1.dp, Sakina3DGoldBevelBrush, CircleShape), contentAlignment = Alignment.Center) {
                                Text("${s.number}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = theme.accentBright)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(s.name, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SakinaInkDark)
                        }
                        Text("${s.ayahs} آية".toWesternDigits(), fontFamily = SakinaNumeralFont, fontSize = 11.5.sp, color = Color(0xFF6B5224))
                    }
                    HorizontalDivider(color = Color(0x22C5A059))
                }
            }
        }
    }
}
