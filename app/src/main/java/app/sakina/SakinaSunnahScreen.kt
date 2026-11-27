package app.sakina

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ركن السنة النبوية الشامل داخل «المزيد» (v73) — 4 تبويبات فرعية موثّقة 100% أوفلاين:
 * 1. «هَديُه ﷺ»: الجدول النبوي اليومي + السنن المهجورة + تحدي سنة الأسبوع (7 أيام) + حديث اليوم
 * 2. «المتون»: متون التأسيس بشارة الحكم اللونية (صحيح/حسن) + إظهار/إخفاء السند + غريب الألفاظ + الأربعون النووية
 * 3. «الشمائل»: الشمائل المحمدية + الخط الزمني للسيرة وربطه بالقرآن + تراجم الصحابة
 * 4. «لا يصح»: درع التحقق قبل النشر + أشهر الأحاديث التي لا تصح + البديل الصحيح
 */
@Composable
fun SakinaSunnahScreen(
    ctx: Context,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }
    val haptic = LocalHapticFeedback.current
    var activeTab by remember { mutableStateOf(SunnahSubSection.DAILY) }
    var showNawawi40 by remember { mutableStateOf(false) }

    val velvetBg = remember { Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.58f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakinaLuxuryPalette.ObsidianNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SakinaAmbientLuxuryBackdrop(showGeometricPattern = true, showStarDust = true)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // 1. الترويسة الموحّدة بزر الرجوع المعدني الذهبي البارز وعنوان أميري ذهبي
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SakinaTactileGoldCoinButton(
                    onClick = onBackClick,
                    size = 38.dp,
                    drawableRes = R.drawable.ic_quran_back_gold_3d,
                    contentDescription = "رجوع"
                )
                Text(
                    text = "ركن السنة النبوية",
                    style = TextStyle(
                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                        shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SakinaHeadingFont,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(modifier = Modifier.width(38.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. شريط التبويبات الفرعية الأربعة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xE6051C19))
                    .border(0.95.dp, goldBorder, RoundedCornerShape(18.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                SunnahSubSection.values().forEach { sub ->
                    val sel = activeTab == sub
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                if (sel) Brush.horizontalGradient(listOf(Color(0xFF0E4B3B), Color(0xFF156953), Color(0xFF0E4B3B)))
                                else SolidColor(Color.Transparent)
                            )
                            .then(
                                if (sel) Modifier.border(0.8.dp, SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.75f), RoundedCornerShape(15.dp))
                                else Modifier
                            )
                            .clickable { activeTab = sub }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sub.labelAr,
                            color = if (sel) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f),
                            fontSize = 11.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = SakinaBodyFont,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(9.dp))

            when (activeTab) {
                SunnahSubSection.DAILY -> DailySunnahTab(ctx, velvetBg, goldBorder, haptic)
                SunnahSubSection.BOOKS -> HadithBooksTab(ctx, velvetBg, goldBorder, onOpenNawawi = { showNawawi40 = true })
                SunnahSubSection.SHAMAIL -> ShamailSeerahTab(velvetBg, goldBorder)
                SunnahSubSection.WEAK_SHIELD -> WeakHadithShieldTab(ctx, velvetBg, goldBorder)
            }
        }
    }

    if (showNawawi40) {
        Nawawi40OverlayDialog(
            headingFontFamily = SakinaHeadingFont,
            bodyFontFamily = SakinaBodyFont,
            onDismiss = { showNawawi40 = false }
        )
    }
}

@Composable
private fun DailySunnahTab(
    ctx: Context,
    velvetBg: Brush,
    goldBorder: Color,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    var tick by remember { mutableIntStateOf(0) }
    val todayHadith = SakinaSunnahEncyclopediaRepo.VERIFIED_HADITHS.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // بطاقة حديث اليوم الموثّق
        item(key = "hadith_today") {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brush.verticalGradient(listOf(Color(0xFF0B3A2F), Color(0xFF051A18)))).border(1.1.dp, SakinaLuxuryPalette.MetallicGoldTextBrush, RoundedCornerShape(18.dp)).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("✦ حديث اليوم الصحيح", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                    Text(todayHadith.grade.labelAr, color = Color(todayHadith.grade.colorHex), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
                Text(todayHadith.matn, color = SakinaOrbitalColors.WarmCream, fontSize = 14.5.sp, lineHeight = 23.sp, fontFamily = SakinaHeadingFont, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text("الفائدة: ${todayHadith.benefit}", color = SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.9f), fontSize = 11.5.sp, fontFamily = SakinaBodyFont)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(todayHadith.source, color = SakinaLuxuryColors.EmeraldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    Text("↗ مشاركة موثّقة", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                        val sh = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${todayHadith.matn}\n\n[الحكم: ${todayHadith.grade.labelAr} · ${todayHadith.source}]\n— تطبيق نُورُ قُنُوت")
                        }
                        ctx.startActivity(Intent.createChooser(sh, "مشاركة الحديث"))
                    })
                }
            }
        }

        // تحدي سُنة الأسبوع (تتبع 7 أيام هادئ)
        item(key = "weekly_challenge") {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(velvetBg).border(0.95.dp, goldBorder, RoundedCornerShape(18.dp)).padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text("✦ ${SakinaSunnahEncyclopediaRepo.WEEKLY_CHALLENGE_TITLE}", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                Text(SakinaSunnahEncyclopediaRepo.WEEKLY_CHALLENGE_DESC, color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.88f), fontSize = 11.5.sp, lineHeight = 19.sp, fontFamily = SakinaBodyFont)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("س", "ح", "ن", "ث", "ر", "خ", "ج").forEachIndexed { idx, d ->
                        val done = remember(tick) { SakinaSunnahEncyclopediaRepo.isWeeklyDayDone(ctx, idx) }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (done) SakinaLuxuryPalette.MetallicGoldButtonBrush else SolidColor(Color(0xFF041716)))
                                .border(0.85.dp, goldBorder, CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    SakinaSunnahEncyclopediaRepo.toggleWeeklyDay(ctx, idx)
                                    tick++
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (done) "✓" else d, color = if (done) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ركن السنن المهجورة بالدليل الصحيح
        item(key = "ab_hdr") {
            Text("السنن المهجورة وهديه ﷺ اليومي", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont, modifier = Modifier.padding(top = 2.dp))
        }

        items(SakinaSunnahEncyclopediaRepo.ABANDONED_SUNNAHS, key = { it.id }) { s ->
            val done = remember(tick) { SakinaSunnahEncyclopediaRepo.isDailySunnahDone(ctx, s.id) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(velvetBg)
                    .border(if (done) 1.2.dp else 0.85.dp, if (done) SakinaLuxuryColors.EmeraldGlow else goldBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        SakinaSunnahEncyclopediaRepo.toggleDailySunnah(ctx, s.id)
                        tick++
                    }
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${s.title} · ${s.timeWindow}", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont, modifier = Modifier.weight(1f))
                    Text(if (done) "✓ طُبّقت" else "○ طبّقها", color = if (done) SakinaLuxuryColors.EmeraldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.65f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(s.practice, color = SakinaOrbitalColors.WarmCream, fontSize = 12.sp, fontFamily = SakinaBodyFont)
                Text("الدليل: ${s.evidence}", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HadithBooksTab(
    ctx: Context,
    velvetBg: Brush,
    goldBorder: Color,
    onOpenNawawi: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showIsnad by remember { mutableStateOf(false) }
    var selectedCol by remember { mutableStateOf("الكل") }
    val cols = listOf("الكل", "قدسي", "عمدة الأحكام", "رياض الصالحين")

    val filtered = remember(query, selectedCol) {
        val normQ = SakinaSunnahEncyclopediaRepo.normalizeAr(query)
        SakinaSunnahEncyclopediaRepo.VERIFIED_HADITHS.filter { h ->
            (selectedCol == "الكل" || h.collection == selectedCol) &&
                (normQ.isBlank() || SakinaSunnahEncyclopediaRepo.normalizeAr("${h.title} ${h.matn} ${h.companion} ${h.topic}").contains(normQ))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xE6051C19)).border(0.8.dp, goldBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = SakinaLuxuryColors.SoftGoldGlow, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) Text("بحث حديثي مرن (النص، الصحابي، الموضوع)...", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.45f), fontSize = 11.5.sp, fontFamily = SakinaBodyFont)
                BasicTextField(value = query, onValueChange = { query = it }, singleLine = true, textStyle = TextStyle(color = SakinaOrbitalColors.WarmCream, fontSize = 12.sp, fontFamily = SakinaBodyFont), cursorBrush = SolidColor(SakinaLuxuryColors.SoftGoldGlow), modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                items(cols) { c ->
                    val sel = selectedCol == c
                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(if (sel) SakinaLuxuryPalette.MetallicGoldButtonBrush else SolidColor(Color(0xFF041716))).border(0.75.dp, goldBorder.copy(alpha = 0.45f), RoundedCornerShape(10.dp)).clickable { selectedCol = c }.padding(horizontal = 9.dp, vertical = 4.dp)) {
                        Text(c, color = if (sel) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = if (showIsnad) "إخفاء السند" else "إظهار السند",
                color = SakinaLuxuryColors.EmeraldGlow,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { showIsnad = !showIsnad }.padding(start = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 18.dp)) {
            item(key = "nawawi_link") {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF0E4B3B)).border(0.9.dp, SakinaLuxuryColors.SoftGoldGlow, RoundedCornerShape(14.dp)).clickable(onClick = onOpenNawawi).padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦ فتح متن «الأربعون النووية» الكامل أوفلاين", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                }
            }

            items(filtered, key = { it.id }) { h ->
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(velvetBg).border(0.9.dp, goldBorder, RoundedCornerShape(16.dp)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${h.title} (${h.collection})", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont, modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(h.grade.colorHex).copy(alpha = 0.18f)).border(0.7.dp, Color(h.grade.colorHex), RoundedCornerShape(8.dp)).padding(horizontal = 7.dp, vertical = 2.dp)) {
                            Text(h.grade.labelAr, color = Color(h.grade.colorHex), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (showIsnad) {
                        Text(h.isnad, color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.75f), fontSize = 11.5.sp, fontFamily = SakinaBodyFont)
                    }
                    Text(h.matn, color = SakinaOrbitalColors.WarmCream, fontSize = 14.sp, lineHeight = 23.sp, fontFamily = SakinaHeadingFont, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    HorizontalDivider(color = goldBorder.copy(alpha = 0.25f))
                    Text("الراوي: ${h.companion} · ${h.typeLabel}", color = SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.85f), fontSize = 10.5.sp)
                    Text("التخريج: ${h.source}", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    Text("غريب الألفاظ: ${h.gharib}", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f), fontSize = 10.5.sp)
                    Text(
                        text = "↗ مشاركة بطاقة موثّقة",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End).clickable {
                            val sh = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${h.matn}\n\n[${h.grade.labelAr} · ${h.source}]\n— نُورُ قُنُوت")
                            }
                            ctx.startActivity(Intent.createChooser(sh, "مشاركة"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShamailSeerahTab(velvetBg: Brush, goldBorder: Color) {
    var sec by remember { mutableIntStateOf(0) } // 0=الشمائل، 1=السيرة، 2=تراجم الصحابة
    val items = remember(sec) { SakinaSunnahEncyclopediaRepo.SHAMAIL_AND_SEERAH.filter { it.section == sec } }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("الشمائل المحمدية", "السيرة والقرآن", "تراجم الصحابة").forEachIndexed { idx, lbl ->
                val sel = sec == idx
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(11.dp)).background(if (sel) SakinaLuxuryPalette.MetallicGoldButtonBrush else SolidColor(Color(0xFF041716))).border(0.75.dp, goldBorder.copy(alpha = 0.45f), RoundedCornerShape(11.dp)).clickable { sec = idx }.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Text(lbl, color = if (sel) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 18.dp)) {
            items(items) { it ->
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(velvetBg).border(0.9.dp, goldBorder, RoundedCornerShape(16.dp)).padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(it.title, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                    Text(it.subtitle, color = SakinaLuxuryColors.EmeraldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(it.body, color = SakinaOrbitalColors.WarmCream, fontSize = 12.5.sp, lineHeight = 21.sp, fontFamily = SakinaBodyFont)
                    if (it.quranLink.isNotEmpty()) {
                        HorizontalDivider(color = goldBorder.copy(alpha = 0.25f))
                        Text(it.quranLink, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeakHadithShieldTab(ctx: Context, velvetBg: Brush, goldBorder: Color) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val normQ = SakinaSunnahEncyclopediaRepo.normalizeAr(query)
        if (normQ.isBlank()) SakinaSunnahEncyclopediaRepo.WEAK_WARNINGS
        else SakinaSunnahEncyclopediaRepo.WEAK_WARNINGS.filter { w ->
            SakinaSunnahEncyclopediaRepo.normalizeAr("${w.widespreadText} ${w.category} ${w.reason}").contains(normQ)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // الباحث السريع للتحقق قبل النشر
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xE6051C19)).border(0.85.dp, Color(0xFFF87171).copy(alpha = 0.6f), RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) Text("تحقّق من أي حديث منتشر قبل نشره...", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.5f), fontSize = 11.5.sp, fontFamily = SakinaBodyFont)
                BasicTextField(value = query, onValueChange = { query = it }, singleLine = true, textStyle = TextStyle(color = SakinaOrbitalColors.WarmCream, fontSize = 12.sp, fontFamily = SakinaBodyFont), cursorBrush = SolidColor(Color(0xFFF87171)), modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 18.dp)) {
            items(filtered, key = { it.id }) { w ->
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(velvetBg).border(0.95.dp, Color(0xFFF87171).copy(alpha = 0.55f), RoundedCornerShape(16.dp)).padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("باب: ${w.category}", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.75f), fontSize = 11.sp)
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x33F87171)).border(0.7.dp, Color(0xFFF87171), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text("⚠ لا يصح", color = Color(0xFFF87171), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(w.widespreadText, color = Color(0xFFFCA5A5), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Text("الحكم: ${w.verdict}\nالسبب: ${w.reason}", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f), fontSize = 11.5.sp, lineHeight = 18.sp, fontFamily = SakinaBodyFont)
                    HorizontalDivider(color = goldBorder.copy(alpha = 0.3f))
                    // بطاقة البديل الصحيح الخضراء
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF0A3327)).border(0.8.dp, SakinaLuxuryColors.EmeraldGlow.copy(alpha = 0.65f), RoundedCornerShape(12.dp)).padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text("✓ البديل الصحيح الذي يُغني عنه:", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(w.authenticAlternative, color = SakinaOrbitalColors.WarmCream, fontSize = 12.5.sp, lineHeight = 20.sp, fontFamily = SakinaHeadingFont)
                        Text(w.alternativeSource, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "↗ مشاركة التنبيه والبديل الصحيح",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End).clickable {
                            val sh = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "⚠ تنبيه حديثي:\n${w.widespreadText}\nالحكم: ${w.verdict}\n\n✓ البديل الصحيح:\n${w.authenticAlternative}\n(${w.alternativeSource})\n— نُورُ قُنُوت")
                            }
                            ctx.startActivity(Intent.createChooser(sh, "مشاركة التنبيه"))
                        }
                    )
                }
            }
        }
    }
}
