package app.sakina

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * واجهة المرحلة الأولى من ركن الأذكار الجامع (v70):
 * - تبويب «ذِكْرُك الآن» (المحرك السياقي + 3 مستويات للورد + البطاقة اللمسية الكاملة بالاهتزاز والانتقال التلقائي + سلسلة المواظبة)
 * - دفتر «أدعيتي الخاصة» المبدئي في تبويب «أدعيتي»
 */
@Composable
fun SakinaAdhkarNowTabContent(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onOpenFullNawawi: () -> Unit,
    onOpenFullRuqyah: () -> Unit
) {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    val banner = remember(tick) { SakinaAdhkarContextualEngine.resolveCurrentMoment(ctx) }
    var selectedLevel by remember { mutableStateOf(WirdLevel.SELECTED) }
    var openSensoryReader by remember { mutableStateOf(false) }
    val morningDone = remember(tick) { SakinaAdhkarContextualEngine.isMorningDoneToday(ctx) }
    val eveningDone = remember(tick) { SakinaAdhkarContextualEngine.isEveningDoneToday(ctx) }
    val streak = remember(tick) { SakinaAdhkarContextualEngine.quietStreakDays(ctx) }
    val last14 = remember(tick) { SakinaAdhkarContextualEngine.last14DaysStatus(ctx) }
    val velvetBg = remember { Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. بطاقة المحرّك السياقي الذكي «ذِكْرُك الآن»
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF0B3A2F), Color(0xFF06201B), Color(0xFF041414))))
                .border(1.2.dp, SakinaLuxuryPalette.MetallicGoldTextBrush, RoundedCornerShape(22.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF135C48))
                        .border(0.75.dp, SakinaLuxuryColors.SoftGoldGlow, RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text("✦ ${banner.badge}", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = bodyFontFamily)
                }
                Text(
                    text = "المواظبة: $streak يوم",
                    color = SakinaLuxuryColors.EmeraldGlow,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SakinaNumeralFont
                )
            }

            Text(
                text = banner.title,
                style = TextStyle(
                    brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                    shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily
                )
            )
            Text(
                text = banner.subtitle,
                color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.88f),
                fontSize = 12.sp,
                lineHeight = 19.sp,
                fontFamily = bodyFontFamily
            )

            // مستويات الورد الثلاثة (الكامل · المختار · المختصر للمستعجل والمسافر)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WirdLevel.values().forEach { lvl ->
                    val sel = selectedLevel == lvl
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) SakinaLuxuryPalette.MetallicGoldButtonBrush else SolidColor(Color(0xFF041716)))
                            .border(0.8.dp, goldBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { selectedLevel = lvl }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(lvl.labelAr, color = if (sel) Color(0xFF071A18) else SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = bodyFontFamily)
                        Text(lvl.descAr, color = if (sel) Color(0xFF071A18).copy(alpha = 0.8f) else SakinaOrbitalColors.WarmCream.copy(alpha = 0.65f), fontSize = 9.sp, fontFamily = bodyFontFamily, maxLines = 1)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                    .clickable { openSensoryReader = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ابدأ الورد الآن (${SakinaAdhkarContextualEngine.itemsForLevel(selectedLevel).size} أذكار موثّقة)",
                    color = Color(0xFF071A18),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily
                )
            }
        }

        // 2. بطاقة المواظبة الهادئة للوردين (صباح ومساء + خريطة 14 يوماً)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(velvetBg)
                .border(0.95.dp, goldBorder, RoundedCornerShape(18.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("المواظبة الهادئة (14 يوماً)", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (morningDone) "☀ الصباح ✓" else "☀ الصباح ○", color = if (morningDone) SakinaLuxuryColors.EmeraldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.65f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(if (eveningDone) "🌙 المساء ✓" else "🌙 المساء ○", color = if (eveningDone) SakinaLuxuryColors.EmeraldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.65f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                last14.forEach { done ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (done) SakinaLuxuryColors.SoftGoldGlow else Color(0xFF10342C))
                            .border(0.6.dp, goldBorder.copy(alpha = 0.45f), CircleShape)
                    )
                }
            }
        }

        // 3. وصول سريع للأربعين النووية والرقية الشرعية الموثّقة
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(velvetBg)
                    .border(0.9.dp, goldBorder, RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenFullNawawi)
                    .padding(horizontal = 12.dp, vertical = 11.dp)
            ) {
                Column {
                    Text("✦ الأربعون النووية", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                    Text("أحاديث شريفة · أوفلاين", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.75f), fontSize = 10.sp, fontFamily = bodyFontFamily)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(velvetBg)
                    .border(0.9.dp, goldBorder, RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenFullRuqyah)
                    .padding(horizontal = 12.dp, vertical = 11.dp)
            ) {
                Column {
                    Text("✦ الرقية الشرعية", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                    Text("ارقِ نفسك وغيرك", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 10.sp, fontFamily = bodyFontFamily)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    if (openSensoryReader) {
        SensoryWirdReaderDialog(
            level = selectedLevel,
            isMorning = banner.isMorning,
            defaultWarmNight = banner.isNight,
            headingFontFamily = headingFontFamily,
            bodyFontFamily = bodyFontFamily,
            onCompletedAll = {
                SakinaAdhkarContextualEngine.markWirdCompleted(ctx, banner.isMorning)
                tick++
                openSensoryReader = false
            },
            onDismiss = { openSensoryReader = false }
        )
    }
}

/**
 * قارئ الورد الحسّي المريح (v70):
 * - الضغط في أي مكان على البطاقة يحسب التكرار مع اهتزاز لطيف وينتقل تلقائياً للذكر التالي عند الاكتمال
 * - شريط تقدم مئوي 0%–100% + وضع ليلي دافئ للنوم + A-/A+ + اختبار حفظ الذكر + توثيق (التخريج + الغريب + الفضل)
 */
@Composable
private fun SensoryWirdReaderDialog(
    level: WirdLevel,
    isMorning: Boolean,
    defaultWarmNight: Boolean,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onCompletedAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val items = remember(level) { SakinaAdhkarContextualEngine.itemsForLevel(level) }
    var curIdx by remember { mutableIntStateOf(0) }
    val counts = remember { mutableStateMapOf<String, Int>() }
    var fontSp by remember { mutableIntStateOf(16) }
    var warmNight by remember { mutableStateOf(defaultWarmNight) }
    var quizHide by remember { mutableStateOf(false) }

    val item = items.getOrElse(curIdx) { items.first() }
    val curCount = counts[item.id] ?: 0
    val totalSteps = items.sumOf { it.targetCount }.coerceAtLeast(1)
    val doneSteps = items.sumOf { (counts[it.id] ?: 0).coerceAtMost(it.targetCount) }
    val overallProgress = (doneSteps.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    val bgCol = if (warmNight) Color(0xFF14110C) else Color(0xFF061B18)
    val textCol = if (warmNight) Color(0xFFF3DEC0) else SakinaOrbitalColors.WarmCream

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = bgCol,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${item.title} (${curIdx + 1}/${items.size})", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (quizHide) "👁 إظهار" else "👁 اختبر", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { quizHide = !quizHide }.padding(3.dp))
                        Text(if (warmNight) "☀" else "🌙", fontSize = 12.sp, modifier = Modifier.clickable { warmNight = !warmNight }.padding(3.dp))
                        Text("A-", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp - 2).coerceAtLeast(13) }.padding(2.dp))
                        Text("A+", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp + 2).coerceAtMost(24) }.padding(2.dp))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("التقدم في الورد (${level.labelAr})", color = textCol.copy(alpha = 0.72f), fontSize = 10.sp)
                    Text("${(overallProgress * 100).toInt()}%", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaNumeralFont)
                }
                LinearProgressIndicator(
                    progress = { overallProgress },
                    color = SakinaLuxuryColors.SoftGoldGlow,
                    trackColor = Color(0xFF10382F),
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (warmNight) Color(0xFF1E1810) else Color(0xFF041414))
                    .border(
                        width = if (curCount >= item.targetCount) 1.5.dp else 0.9.dp,
                        color = if (curCount >= item.targetCount) SakinaLuxuryColors.EmeraldGlow else SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        val next = (curCount + 1).coerceAtMost(item.targetCount)
                        counts[item.id] = next
                        if (next >= item.targetCount) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (curIdx < items.lastIndex) curIdx++
                            else onCompletedAll()
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.text,
                    color = textCol,
                    fontSize = fontSp.sp,
                    lineHeight = (fontSp + 12).sp,
                    fontFamily = headingFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(if (quizHide) 0.08f else 1f)
                )

                // عدّاد التكرار الدائري الذهبي (المس البطاقة في أي مكان للعدّ)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$curCount / ${item.targetCount}  (المس البطاقة للعدّ)",
                        color = Color(0xFF071A18),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SakinaNumeralFont
                    )
                }

                HorizontalDivider(color = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.3f))
                Text("التخريج: ${item.takhrij}", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("غريب الألفاظ: ${item.gharib}", color = textCol.copy(alpha = 0.8f), fontSize = 10.5.sp, textAlign = TextAlign.Center)
                Text("الفضل: ${item.fadl}", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 10.5.sp, textAlign = TextAlign.Center)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("< السابق", color = if (curIdx > 0) SakinaLuxuryColors.SoftGoldGlow else Color.Gray, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(enabled = curIdx > 0) { curIdx-- }.padding(4.dp))
                    Text("↗ مشاركة", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                        val sh = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${item.title}\n\n${item.text}\n\n(${item.takhrij}) — نور قنوت")
                        }
                        ctx.startActivity(Intent.createChooser(sh, "مشاركة الذكر"))
                    }.padding(4.dp))
                    Text("التالي >", color = if (curIdx < items.lastIndex) SakinaLuxuryColors.SoftGoldGlow else Color.Gray, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(enabled = curIdx < items.lastIndex) { curIdx++ }.padding(4.dp))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold) } }
    )
}

/** دفتر المسلم الخاص «أدعيتي الخاصة» (أساس تبويب أدعيتي) */
@Composable
fun PersonalDuasQuickTab(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily
) {
    val ctx = LocalContext.current
    val sp = remember(ctx) { ctx.getSharedPreferences("nurqunoot_personal_duas_v70", android.content.Context.MODE_PRIVATE) }
    var duasText by remember { mutableStateOf(sp.getString("my_duas_text", "") ?: "") }
    val velvetBg = remember { Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.55f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(velvetBg)
                .border(1.dp, goldBorder, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Text("✦ دفتر «أدعيتي الخاصة»", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF041414))
                    .border(0.8.dp, goldBorder.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                if (duasText.isEmpty()) {
                    Text(
                        text = "اكتب هنا أدعيتك الخاصة (للوالدين، للأبناء، للأموات، لقضاء الحاجات، أو أمانيك بين الأذان والإقامة) — تُحفظ أوفلاين تلقائياً...",
                        color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.48f),
                        fontSize = 12.sp,
                        fontFamily = bodyFontFamily
                    )
                }
                BasicTextField(
                    value = duasText,
                    onValueChange = {
                        duasText = it
                        sp.edit().putString("my_duas_text", it).apply()
                    },
                    textStyle = TextStyle(color = SakinaOrbitalColors.WarmCream, fontSize = 13.5.sp, lineHeight = 22.sp, fontFamily = bodyFontFamily),
                    cursorBrush = SolidColor(SakinaLuxuryColors.SoftGoldGlow),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
