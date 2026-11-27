package app.sakina

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * واجهة «التدبّر وعلوم القرآن» الأوفلاين (v66):
 * - التجويد (10 دروس ورش من طريق الأزرق)
 * - المتون الـ 3 (تحفة الأطفال، الجزرية، الشاطبية) بترقيم الأبيات وفهرس الأبواب ونسخ وتكبير الخط
 * - الفوائد وخرائط جزء عمّ وسؤال اليوم مع المفضلة
 * - دفتر ملاحظاتي الشخصي أوفلاين
 * - + نافذة «جميع المعاني ^» داخل قارئ المصحف
 */
@Composable
fun SakinaTadabburTabContent(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    currentSurahNumber: Int = 1,
    currentSurahName: String = "سورة الفاتحة",
    currentPage: Int = 1,
    currentPageAyahs: List<MushafAyahUi> = emptyList(),
    modifier: Modifier = Modifier
) {
    var innerTab by remember { mutableIntStateOf(0) } // 0=التفسير والهدايات، 1=تجويد ورش، 2=المتون، 3=الفوائد، 4=ملاحظاتي
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.55f)

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xD9051C19))
                .border(0.8.dp, goldBorder.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            listOf("التفسير", "التجويد", "المتون", "الفوائد", "ملاحظاتي").forEachIndexed { idx, lbl ->
                val sel = innerTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (sel) SakinaLuxuryPalette.MetallicGoldButtonBrush else SolidColor(Color.Transparent))
                        .clickable { innerTab = idx }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lbl,
                        color = if (sel) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (innerTab) {
            0 -> SakinaPageTafsirContent(
                surahNumber = currentSurahNumber,
                surahName = currentSurahName,
                pageNumber = currentPage,
                pageAyahs = currentPageAyahs,
                headingFontFamily = headingFontFamily,
                bodyFontFamily = bodyFontFamily
            )
            1 -> WarshTajweedLessonsSection(headingFontFamily, bodyFontFamily)
            2 -> ThreeMutunSection(headingFontFamily, bodyFontFamily)
            3 -> FawaidSection(headingFontFamily, bodyFontFamily)
            else -> PersonalNotesSection(headingFontFamily, bodyFontFamily)
        }
    }
}

@Composable
private fun WarshTajweedLessonsSection(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily
) {
    var activeLesson by remember { mutableStateOf<TajweedLesson?>(null) }
    var fontSp by remember { mutableIntStateOf(14) }
    val velvetBg = remember { Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.5f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SakinaTadabburOfflineRepo.WARSH_TAJWEED_LESSONS, key = { it.number }) { lesson ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(velvetBg)
                    .border(0.9.dp, goldBorder, RoundedCornerShape(16.dp))
                    .clickable { activeLesson = lesson }
                    .padding(horizontal = 13.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "${lesson.number}. ${lesson.title}",
                    color = SakinaLuxuryColors.SoftGoldGlow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = lesson.summary,
                    color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f),
                    fontSize = 11.5.sp,
                    fontFamily = bodyFontFamily
                )
            }
        }
    }

    activeLesson?.let { les ->
        AlertDialog(
            onDismissRequest = { activeLesson = null },
            containerColor = Color(0xFF071E1B),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${les.number}. ${les.title}", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("A-", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp - 2).coerceAtLeast(12) }.padding(4.dp))
                        Text("A+", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp + 2).coerceAtMost(24) }.padding(4.dp))
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = les.body,
                        color = SakinaOrbitalColors.WarmCream,
                        fontSize = fontSp.sp,
                        lineHeight = (fontSp + 10).sp,
                        fontFamily = bodyFontFamily
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { activeLesson = null }) {
                    Text("إغلاق", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ThreeMutunSection(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily
) {
    val ctx = LocalContext.current
    var selectedMatnIdx by remember { mutableIntStateOf(0) }
    var fontSp by remember { mutableIntStateOf(15) }
    val matn = SakinaTadabburOfflineRepo.THREE_MUTUN[selectedMatnIdx]
    val velvetBg = remember { Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.5f)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            SakinaTadabburOfflineRepo.THREE_MUTUN.forEachIndexed { idx, m ->
                val sel = idx == selectedMatnIdx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (sel) Color(0xFF0E4B3B) else Color(0xFF041716))
                        .border(0.8.dp, if (sel) SakinaLuxuryColors.SoftGoldGlow else goldBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { selectedMatnIdx = idx }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (idx) { 0 -> "تحفة الأطفال"; 1 -> "الجزرية"; else -> "الشاطبية" },
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        maxLines = 1
                    )
                }
            }
            Text("A-", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp - 1).coerceAtLeast(12) }.padding(4.dp))
            Text("A+", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp + 1).coerceAtMost(22) }.padding(4.dp))
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("${matn.title} — ${matn.author}", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f), fontSize = 11.5.sp, fontFamily = bodyFontFamily)
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(matn.chapters) { ch ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(velvetBg)
                        .border(0.9.dp, goldBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("✦ ${ch.chapterTitle}", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                        Text(
                            text = "نسخ",
                            color = SakinaLuxuryColors.EmeraldGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val txt = ch.verses.joinToString("\n") { "${it.first}. ${it.second}" }
                                cm.setPrimaryClip(ClipData.newPlainText(matn.title, "${matn.title} - ${ch.chapterTitle}\n$txt"))
                            }.padding(4.dp)
                        )
                    }
                    HorizontalDivider(color = goldBorder.copy(alpha = 0.3f))
                    ch.verses.forEach { (num, line) ->
                        Text(
                            text = "$num. $line",
                            color = SakinaOrbitalColors.WarmCream,
                            fontSize = fontSp.sp,
                            lineHeight = (fontSp + 11).sp,
                            fontFamily = headingFontFamily,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FawaidSection(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily
) {
    val ctx = LocalContext.current
    var selectedCat by remember { mutableStateOf("الكل") }
    var favTick by remember { mutableIntStateOf(0) }
    val favs = remember(favTick) { SakinaTadabburOfflineRepo.favoriteFawaidIds(ctx) }
    val categories = listOf("الكل", "المفضلة", "لطائف", "آية وقصة", "خرائط جزء عمّ", "التعريف بالسور", "سؤال اليوم")
    val velvetBg = remember { Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.5f)

    val filtered = remember(selectedCat, favs) {
        when (selectedCat) {
            "الكل" -> SakinaTadabburOfflineRepo.FAWAID_ARTICLES
            "المفضلة" -> SakinaTadabburOfflineRepo.FAWAID_ARTICLES.filter { it.id in favs }
            else -> SakinaTadabburOfflineRepo.FAWAID_ARTICLES.filter { it.category == selectedCat }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories) { cat ->
                val active = selectedCat == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) SakinaLuxuryPalette.MetallicGoldButtonBrush else SolidColor(Color(0xFF041716)))
                        .border(0.75.dp, goldBorder.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .clickable { selectedCat = cat }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(cat, color = if (active) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(filtered, key = { it.id }) { card ->
                val isFav = card.id in favs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(velvetBg)
                        .border(0.9.dp, goldBorder, RoundedCornerShape(16.dp))
                        .padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("✦ ${card.category}", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isFav) "★ محفوظة" else "☆ مفضلة",
                            color = if (isFav) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                SakinaTadabburOfflineRepo.toggleFavoriteFawaid(ctx, card.id)
                                favTick++
                            }.padding(4.dp)
                        )
                    }
                    Text(card.title, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                    Text(card.content, color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.9f), fontSize = 12.5.sp, lineHeight = 21.sp, fontFamily = bodyFontFamily)
                }
            }
        }
    }
}

@Composable
private fun PersonalNotesSection(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily
) {
    val ctx = LocalContext.current
    val sp = remember(ctx) { ctx.getSharedPreferences("nurqunoot_notes_v63", Context.MODE_PRIVATE) }
    var noteText by remember { mutableStateOf(sp.getString("personal_tadabbur_note", "") ?: "") }
    val velvetBg = remember { Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.55f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(velvetBg)
            .border(1.dp, goldBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Text("✦ دفتر ملاحظاتي وتدبّراتي", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF041414))
                .border(0.8.dp, goldBorder.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            if (noteText.isEmpty()) {
                Text(
                    text = "اكتب هنا خواطرك وتدبّراتك القرآنية (أو أضف آيات مباشرة بالضغط المطوّل من داخل المصحف)...",
                    color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.45f),
                    fontSize = 12.sp,
                    fontFamily = bodyFontFamily
                )
            }
            BasicTextField(
                value = noteText,
                onValueChange = {
                    noteText = it
                    sp.edit().putString("personal_tadabbur_note", it).apply()
                },
                textStyle = TextStyle(color = SakinaOrbitalColors.WarmCream, fontSize = 13.sp, lineHeight = 21.sp, fontFamily = bodyFontFamily),
                cursorBrush = SolidColor(SakinaLuxuryColors.SoftGoldGlow),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** درج «جميع المعاني ^» العائم داخل المصحف (4-3) */
@Composable
fun MushafPageMeaningsDialog(
    surahName: String,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    val items = remember(surahName) { SakinaTadabburOfflineRepo.meaningsForSurahHint(surahName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071E1B),
        title = { Text("معاني الكلمات وغريب القرآن · $surahName", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { entry ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF041515))
                            .border(0.7.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(9.dp)
                    ) {
                        Text("﴿ ${entry.word} ﴾", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                        Text(entry.meaning, color = SakinaOrbitalColors.WarmCream, fontSize = 12.sp, lineHeight = 19.sp, fontFamily = bodyFontFamily)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold) } }
    )
}
