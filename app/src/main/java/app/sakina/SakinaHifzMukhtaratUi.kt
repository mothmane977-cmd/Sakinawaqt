package app.sakina

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * واجهة «التلاوة والحفظ والمختارات» الكاملة (v65):
 * 1. القراء والتلاوة أوفلاين (بحث عن قارئ + اختيار القارئ + إدارة السور المحمّلة)
 * 2. محرّك التحفيظ الثلاثي «اسمع وكرّر» (7 مستويات نطاق + تكرار الآية + تكرار المقطع + طول السكتة بقدر الآية + اختبار بعد الجلسة)
 * 3. المختارات (مختارات المصحف الـ 14 + مختاراتك المخصصة)
 * 4. كبسولة جلسة التحفيظ العائمة داخل المصحف
 */
@Composable
fun SakinaRecitationHifzHub(
    state: SakinaQuranScreenState,
    strings: SakinaQuranStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onReciterClick: () -> Unit,
    onExpandPlayerClick: () -> Unit,
    onSurahSelected: (SurahAudioItem) -> Unit,
    onOpenPageClick: (Int) -> Unit,
    onStartHifzSession: (HifzSessionConfig) -> Unit,
    onDownloadSurahClick: (Int) -> Unit,
    onDeleteDownloadedSurah: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var subMode by remember { mutableIntStateOf(0) } // 0=التلاوة، 1=التحفيظ، 2=المختارات
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.55f)

    Column(modifier = modifier.fillMaxSize()) {
        // شريط فرعي أنيق للتنقل بين التلاوة · التحفيظ · المختارات
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xD9051C19))
                .border(0.8.dp, goldBorder.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("القراء والتلاوة", "اسمع وكرّر (تحفيظ)", "المختارات").forEachIndexed { idx, lbl ->
                val sel = subMode == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            if (sel) SakinaLuxuryPalette.MetallicGoldButtonBrush
                            else SolidColor(Color.Transparent)
                        )
                        .clickable { subMode = idx }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lbl,
                        color = if (sel) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (subMode) {
            0 -> RecitersAndOfflineSection(
                state = state,
                strings = strings,
                headingFontFamily = headingFontFamily,
                bodyFontFamily = bodyFontFamily,
                onReciterClick = onReciterClick,
                onExpandPlayerClick = onExpandPlayerClick,
                onSurahSelected = onSurahSelected,
                onDownloadSurahClick = onDownloadSurahClick,
                onDeleteDownloadedSurah = onDeleteDownloadedSurah
            )
            1 -> HifzTripleRepeatSection(
                surahs = state.surahs,
                reciterName = state.player.reciterName,
                headingFontFamily = headingFontFamily,
                bodyFontFamily = bodyFontFamily,
                onReciterClick = onReciterClick,
                onStartSession = onStartHifzSession
            )
            else -> MukhtaratSection(
                surahs = state.surahs,
                headingFontFamily = headingFontFamily,
                bodyFontFamily = bodyFontFamily,
                onOpenPageClick = onOpenPageClick
            )
        }
    }
}

@Composable
private fun RecitersAndOfflineSection(
    state: SakinaQuranScreenState,
    strings: SakinaQuranStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onReciterClick: () -> Unit,
    onExpandPlayerClick: () -> Unit,
    onSurahSelected: (SurahAudioItem) -> Unit,
    onDownloadSurahClick: (Int) -> Unit,
    onDeleteDownloadedSurah: (Int) -> Unit
) {
    var reciterQuery by remember { mutableStateOf("") }
    val downloaded = remember(state.surahs) { state.surahs.filter { it.audioStatus == SurahAudioStatus.DOWNLOADED } }
    val velvetBg = remember { Brush.horizontalGradient(listOf(Color(0xFF06231D), Color(0xFF082E26), Color(0xFF051819))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.55f)

    // قائمة القراء المعتمدين للبحث السريع (بشارات خطية ذهبية خالية من صور الأشخاص)
    val featuredReciters = remember {
        listOf(
            "عمر القزابري" to "ورش عن نافع",
            "العيون الكوشي" to "ورش عن نافع",
            "ياسين الجزائري" to "ورش عن نافع",
            "محمود خليل الحصري" to "حفص / ورش",
            "عبد الباسط عبد الصمد" to "حفص / ورش",
            "إبراهيم الدوسري" to "ورش عن نافع",
            "محمد صديق المنشاوي" to "حفص عن عاصم",
            "مشاري راشد العفاسي" to "حفص عن عاصم"
        )
    }
    val filteredReciters = remember(reciterQuery) {
        if (reciterQuery.isBlank()) featuredReciters
        else featuredReciters.filter { it.first.contains(reciterQuery, ignoreCase = true) || it.second.contains(reciterQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "rec_active") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(velvetBg)
                    .border(1.dp, goldBorder, RoundedCornerShape(16.dp))
                    .clickable(onClick = onReciterClick)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("القارئ النشط حالياً", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.7f), fontSize = 10.5.sp, fontFamily = bodyFontFamily)
                    Text(state.player.reciterName, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                        .clickable(onClick = onExpandPlayerClick)
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Text("المشغّل الماسي", color = Color(0xFF071A18), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = bodyFontFamily)
                }
            }
        }

        item(key = "rec_search") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE6051C19))
                    .border(0.8.dp, goldBorder.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = SakinaLuxuryColors.SoftGoldGlow, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (reciterQuery.isEmpty()) {
                        Text("ابحث عن قارئ (ورش / حفص)...", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.45f), fontSize = 11.5.sp, fontFamily = bodyFontFamily)
                    }
                    BasicTextField(
                        value = reciterQuery,
                        onValueChange = { reciterQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = SakinaOrbitalColors.WarmCream, fontSize = 12.sp, fontFamily = bodyFontFamily),
                        cursorBrush = SolidColor(SakinaLuxuryColors.SoftGoldGlow),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item(key = "rec_cards") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(filteredReciters) { (rName, rStyle) ->
                    Column(
                        modifier = Modifier
                            .width(126.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(velvetBg)
                            .border(0.8.dp, goldBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable(onClick = onReciterClick)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0E4B3B))
                                .border(0.8.dp, SakinaLuxuryColors.SoftGoldGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("۞", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(rName, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = bodyFontFamily, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(rStyle, color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.7f), fontSize = 9.5.sp, fontFamily = bodyFontFamily, maxLines = 1)
                    }
                }
            }
        }

        item(key = "rec_dl_title") {
            Text(
                text = "السور الجاهزة أوفلاين (${downloaded.size} سورة · اضغط مطوّلاً للحذف)",
                color = SakinaLuxuryColors.SoftGoldGlow,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = headingFontFamily
            )
        }

        itemsIndexed(downloaded, key = { _, it -> "off_${it.number}" }) { _, surah ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(velvetBg)
                    .border(0.8.dp, goldBorder.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .clickable { onSurahSelected(surah) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${surah.number}. ${surah.arabicName}", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("✓ ${strings.offlineReadyLabel}", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "حذف",
                        color = Color(0xFFE58A7E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onDeleteDownloadedSurah(surah.number) }.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HifzTripleRepeatSection(
    surahs: List<SurahAudioItem>,
    reciterName: String,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onReciterClick: () -> Unit,
    onStartSession: (HifzSessionConfig) -> Unit
) {
    val ctx = LocalContext.current
    var cfg by remember { mutableStateOf(SakinaHifzMukhtaratStore.loadHifzConfig(ctx)) }
    var showHelpDialog by remember { mutableStateOf(false) }
    val selectedSurah = remember(surahs, cfg.surahNumber) {
        surahs.firstOrNull { it.number == cfg.surahNumber } ?: surahs.firstOrNull()
    }
    val maxAyah = (selectedSurah?.ayahCount ?: 7).coerceAtLeast(1)
    val velvetBg = remember { Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.55f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        item(key = "hifz_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(velvetBg)
                    .border(1.dp, goldBorder, RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("محرّك «اسمع وكرّر» للتحفيظ", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                    Text(
                        text = "؟ مساعدة",
                        color = SakinaLuxuryColors.EmeraldGlow,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showHelpDialog = true }.padding(4.dp)
                    )
                }

                // اختيار نطاق التكرار (7+ مستويات)
                Text("نطاق الجلسة:", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f), fontSize = 11.5.sp, fontFamily = bodyFontFamily)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(HifzScopeLevel.values().toList()) { sc ->
                        val active = cfg.scope == sc
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) SakinaLuxuryPalette.MetallicGoldButtonBrush else SolidColor(Color(0xFF041716)))
                                .border(0.8.dp, goldBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { cfg = cfg.copy(scope = sc) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(sc.labelAr, color = if (active) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // اختيار السورة ونطاق الآيات
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "السورة: ${selectedSurah?.arabicName ?: cfg.surahName} (${cfg.surahNumber})",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = headingFontFamily
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("السابقة", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                            val nextNum = (cfg.surahNumber - 1).coerceAtLeast(1)
                            val sObj = surahs.firstOrNull { it.number == nextNum }
                            cfg = cfg.copy(surahNumber = nextNum, surahName = sObj?.arabicName ?: "سورة $nextNum", startPage = sObj?.startPage ?: 1, fromAyah = 1, toAyah = (sObj?.ayahCount ?: 7).coerceAtMost(10))
                        }.padding(4.dp))
                        Text("التالية", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                            val nextNum = (cfg.surahNumber + 1).coerceAtMost(114)
                            val sObj = surahs.firstOrNull { it.number == nextNum }
                            cfg = cfg.copy(surahNumber = nextNum, surahName = sObj?.arabicName ?: "سورة $nextNum", startPage = sObj?.startPage ?: 1, fromAyah = 1, toAyah = (sObj?.ayahCount ?: 7).coerceAtMost(10))
                        }.padding(4.dp))
                    }
                }

                // من آية إلى آية
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepperRowBox("من آية", cfg.fromAyah, Modifier.weight(1f),
                        onDec = { cfg = cfg.copy(fromAyah = (cfg.fromAyah - 1).coerceAtLeast(1)) },
                        onInc = { cfg = cfg.copy(fromAyah = (cfg.fromAyah + 1).coerceAtMost(maxAyah), toAyah = cfg.toAyah.coerceAtLeast((cfg.fromAyah + 1).coerceAtMost(maxAyah))) }
                    )
                    StepperRowBox("إلى آية", cfg.toAyah.coerceIn(cfg.fromAyah, maxAyah), Modifier.weight(1f),
                        onDec = { cfg = cfg.copy(toAyah = (cfg.toAyah - 1).coerceAtLeast(cfg.fromAyah)) },
                        onInc = { cfg = cfg.copy(toAyah = (cfg.toAyah + 1).coerceAtMost(maxAyah)) }
                    )
                }

                HorizontalDivider(color = goldBorder.copy(alpha = 0.3f))

                // التكرار الثلاثي المتقدم: تكرار الآية + تكرار المقطع + طول السكتة بقدر الآية
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepperRowBox("تكرار الآية", cfg.ayahRepeat, Modifier.weight(1f),
                        onDec = { cfg = cfg.copy(ayahRepeat = (cfg.ayahRepeat - 1).coerceAtLeast(1)) },
                        onInc = { cfg = cfg.copy(ayahRepeat = (cfg.ayahRepeat + 1).coerceAtMost(20)) }
                    )
                    StepperRowBox("تكرار المقطع", cfg.rangeRepeat, Modifier.weight(1f),
                        onDec = { cfg = cfg.copy(rangeRepeat = (cfg.rangeRepeat - 1).coerceAtLeast(1)) },
                        onInc = { cfg = cfg.copy(rangeRepeat = (cfg.rangeRepeat + 1).coerceAtMost(20)) }
                    )
                }

                Text("طول السكتة بعد كل آية للترديد والتسميع:", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("بدون" to 0, "بقدر الآية 1×" to 1, "ضعف الآية 2×" to 2, "3 أضعاف" to 3).forEach { (lbl, mul) ->
                        val active = cfg.pauseMultiplier == mul
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) SakinaLuxuryPalette.MetallicGoldButtonBrush else SolidColor(Color(0xFF041716)))
                                .border(0.75.dp, goldBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { cfg = cfg.copy(pauseMultiplier = mul) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(lbl, color = if (active) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }

                // مفتاح اختبار بعد الجلسة + القارئ
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("اختبار تسميع تلقائي بعد الجلسة", color = SakinaOrbitalColors.WarmCream, fontSize = 12.sp, fontFamily = bodyFontFamily)
                    SakinaJewelSwitch(checked = cfg.postSessionTest, onCheckedChange = { cfg = cfg.copy(postSessionTest = it) })
                    )
                }

                Text(
                    text = "القارئ: $reciterName (اضغط للتغيير)",
                    color = SakinaLuxuryColors.EmeraldGlow,
                    fontSize = 11.5.sp,
                    fontFamily = bodyFontFamily,
                    modifier = Modifier.clickable(onClick = onReciterClick)
                )

                // زر بدء جلسة التحفيظ الذهبي البارز
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                        .clickable {
                            val activeCfg = cfg.copy(
                                isActive = true,
                                surahName = selectedSurah?.arabicName ?: cfg.surahName,
                                startPage = selectedSurah?.startPage ?: cfg.startPage
                            )
                            SakinaHifzMukhtaratStore.saveHifzConfig(ctx, activeCfg)
                            onStartSession(activeCfg)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦ بدء جلسة التحفيظ داخل المصحف", color = Color(0xFF071A18), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = Color(0xFF071E1B),
            title = { Text("دليل التكرار الثلاثي للتحفيظ", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily) },
            text = {
                Text(
                    text = "• تكرار الآية الواحدة: يعيد كل آية بمفردها قبل الانتقال للتالية لترسيخ اللفظ.\n\n• طول السكتة بقدر الآية: يترك صمتاً بعد القارئ مساوياً لطول الآية أو ضعفها لتردّدها غيباً بصوتك.\n\n• تكرار المقطع: يعيد كامل النطاق المختار للربط بين الآيات، ثم يفعّل زر العين للاختبار الفوري.",
                    color = SakinaOrbitalColors.WarmCream,
                    fontSize = 12.5.sp,
                    lineHeight = 20.sp,
                    fontFamily = bodyFontFamily
                )
            },
            confirmButton = { TextButton(onClick = { showHelpDialog = false }) { Text("فهمت", color = SakinaLuxuryColors.SoftGoldGlow) } }
        )
    }
}

@Composable
private fun StepperRowBox(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    onDec: () -> Unit,
    onInc: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF041716))
            .border(0.75.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f), fontSize = 10.5.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("−", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onDec).padding(horizontal = 4.dp))
            Text("$value", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaNumeralFont)
            Text("+", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onInc).padding(horizontal = 4.dp))
        }
    }
}

@Composable
private fun MukhtaratSection(
    surahs: List<SurahAudioItem>,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onOpenPageClick: (Int) -> Unit
) {
    val ctx = LocalContext.current
    var showCustom by remember { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    val customList = remember(tick) { SakinaHifzMukhtaratStore.loadCustomMukhtarat(ctx) }
    val velvetBg = remember { Brush.horizontalGradient(listOf(Color(0xFF06231D), Color(0xFF082E26), Color(0xFF051819))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.5f)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!showCustom) Color(0xFF0E4B3B) else Color(0xFF041716))
                    .border(0.8.dp, goldBorder, RoundedCornerShape(12.dp))
                    .clickable { showCustom = false }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("مختارات المصحف (14)", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = bodyFontFamily)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (showCustom) Color(0xFF0E4B3B) else Color(0xFF041716))
                    .border(0.8.dp, goldBorder, RoundedCornerShape(12.dp))
                    .clickable { showCustom = true }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("مختاراتك (${customList.size})", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = bodyFontFamily)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showCustom) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                    .clickable { showAddDialog = true }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("+ إضافة سورة أو نطاق آيات إلى مختاراتك", color = Color(0xFF071A18), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = bodyFontFamily)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        val itemsToShow = if (showCustom) customList else SakinaHifzMukhtaratStore.CURATED_MUKHTARAT
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(itemsToShow, key = { it.id }) { m ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(velvetBg)
                        .border(0.85.dp, goldBorder, RoundedCornerShape(14.dp))
                        .clickable { onOpenPageClick(m.page) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m.title, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                        Text(m.subtitle, color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f), fontSize = 11.sp, fontFamily = bodyFontFamily)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("ص ${m.page}", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaNumeralFont)
                        if (m.isCustom) {
                            Text("×", color = Color(0xFFE58A7E), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                                SakinaHifzMukhtaratStore.deleteCustomMukhtar(ctx, m.id)
                                tick++
                            }.padding(4.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var surahNum by remember { mutableIntStateOf(18) }
        var fromAy by remember { mutableIntStateOf(1) }
        var toAy by remember { mutableIntStateOf(10) }
        val sObj = surahs.firstOrNull { it.number == surahNum }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF071E1B),
            title = { Text("إضافة إلى مختاراتك", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان المختارة (مثلاً: ورد الجمعة)", color = SakinaLuxuryColors.SoftGoldGlow) },
                        singleLine = true,
                        textStyle = TextStyle(color = SakinaOrbitalColors.WarmCream, fontFamily = bodyFontFamily)
                    )
                    StepperRowBox("رقم السورة (${sObj?.arabicName ?: ""})", surahNum, Modifier.fillMaxWidth(),
                        onDec = { surahNum = (surahNum - 1).coerceAtLeast(1) },
                        onInc = { surahNum = (surahNum + 1).coerceAtMost(114) }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StepperRowBox("من آية", fromAy, Modifier.weight(1f),
                            onDec = { fromAy = (fromAy - 1).coerceAtLeast(1) },
                            onInc = { fromAy = (fromAy + 1).coerceAtMost(286) }
                        )
                        StepperRowBox("إلى آية", toAy, Modifier.weight(1f),
                            onDec = { toAy = (toAy - 1).coerceAtLeast(0) },
                            onInc = { toAy = (toAy + 1).coerceAtMost(286) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    SakinaHifzMukhtaratStore.addCustomMukhtar(
                        ctx = ctx,
                        title = title,
                        surahNumber = surahNum,
                        surahName = sObj?.arabicName ?: "سورة $surahNum",
                        page = sObj?.startPage ?: 1,
                        fromAyah = fromAy,
                        toAyah = toAy
                    )
                    tick++
                    showAddDialog = false
                }) { Text("إضافة", color = SakinaLuxuryColors.EmeraldGlow, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("إلغاء", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.7f)) } }
        )
    }
}

/** كبسولة جلسة التحفيظ العائمة داخل المصحف (3-4) مع عدّاد السكتة وزر العين والاختبار */
@Composable
fun HifzFloatingSessionCapsule(
    session: HifzSessionConfig,
    isHiddenForTest: Boolean,
    bodyFontFamily: FontFamily,
    onToggleHide: () -> Unit,
    onEndSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    var curAyah by remember(session) { mutableIntStateOf(session.fromAyah) }
    var curAyahRep by remember(session) { mutableIntStateOf(1) }
    var curRangeRep by remember(session) { mutableIntStateOf(1) }
    var isPaused by remember { mutableStateOf(false) }
    var inSilentPause by remember { mutableStateOf(false) }

    LaunchedEffect(session, isPaused) {
        while (!isPaused && session.isActive) {
            inSilentPause = false
            delay(4500L)
            if (session.pauseMultiplier > 0) {
                inSilentPause = true
                delay(session.pauseMultiplier * 3500L)
            }
            if (curAyahRep < session.ayahRepeat) {
                curAyahRep++
            } else {
                curAyahRep = 1
                if (curAyah < session.toAyah) {
                    curAyah++
                } else if (curRangeRep < session.rangeRepeat) {
                    curRangeRep++
                    curAyah = session.fromAyah
                } else {
                    if (session.postSessionTest && !isHiddenForTest) onToggleHide()
                    isPaused = true
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xF006221C))
            .border(1.1.dp, SakinaLuxuryPalette.MetallicGoldTextBrush, RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (inSilentPause) "🤫 دورك للترديد الآن · آية $curAyah" else "تحفيظ ${session.surahName} · آية $curAyah (${curAyahRep}/${session.ayahRepeat})",
                color = if (inSilentPause) SakinaLuxuryColors.EmeraldGlow else SakinaLuxuryColors.SoftGoldGlow,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = bodyFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "المقطع $curRangeRep/${session.rangeRepeat} · سكتة ${session.pauseMultiplier}×",
                color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f),
                fontSize = 10.sp,
                fontFamily = SakinaNumeralFont
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isHiddenForTest) "👁 إظهار" else "👁 اختبار",
                color = SakinaLuxuryColors.SoftGoldGlow,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF0E4B3B)).clickable(onClick = onToggleHide).padding(horizontal = 7.dp, vertical = 4.dp)
            )
            Text(
                text = if (isPaused) "▶" else "⏸",
                color = SakinaLuxuryColors.SoftGoldGlow,
                fontSize = 13.sp,
                modifier = Modifier.clickable { isPaused = !isPaused }.padding(4.dp)
            )
            Text(
                text = "إنهاء",
                color = Color(0xFFE58A7E),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onEndSession).padding(4.dp)
            )
        }
    }
}
