package app.sakina

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.ceil

/**
 * شاشة «ختمتي» الكاملة (v63) — مطابقة تماماً للهوية البصرية الزمردية الذهبية لتطبيق نور قنوت:
 * - 3 ختمات متزامنة + معالج إنشاء الختمة والقوالب الجاهزة
 * - قوس المحراب الذهبي مع حلقة التقدم، السلسلة، الختم المتوقع، ورد اليوم، وأزرار +1/+ربع/+حزب
 * - بطاقة ورد الميقات المرن (5 صلوات) + اليوم المزدحم + المعوّض الصامت + هجين بالصوت
 * - خريطة الـ 604 صفحة المضيئة عبر الأجزاء الـ 30
 * - سجل القراءة (الأمس، اليوم، الدقائق، الإجمالي + 7 أيام + ساعة بداية اليوم)
 * - مؤقّت المصحف الورقي + دعاء الختم ومشاركة بطاقة الإنجاز
 */
@Composable
fun SakinaKhatmahTabContent(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onOpenPageInMushaf: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    var activeSlotIdx by remember { mutableIntStateOf(SakinaKhatmahStore.activeSlot(ctx)) }

    val slot = remember(tick, activeSlotIdx) { SakinaKhatmahStore.loadSlot(ctx, activeSlotIdx) }
    val streakDays = remember(tick) { SakinaV45StatsPrefs.readingStreakDays(ctx) }
    val todayP = remember(tick) { SakinaKhatmahStore.pagesToday(ctx) }
    val yestP = remember(tick) { SakinaKhatmahStore.pagesYesterday(ctx) }
    val todayM = remember(tick) { SakinaKhatmahStore.minutesToday(ctx) }
    val totalP = remember(tick) { SakinaKhatmahStore.totalLoggedPages(ctx) }
    val last7 = remember(tick) { SakinaKhatmahStore.last7DaysLog(ctx) }
    val dayStartHour = remember(tick) { SakinaKhatmahStore.dayStartHour(ctx) }

    var showPlanWizard by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showPaperTimerDialog by remember { mutableStateOf(false) }
    var showKhatmDuaDialog by remember { mutableStateOf(false) }
    var showLogDetails by remember { mutableStateOf(false) }

    val velvetBg = remember {
        Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18)))
    }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.62f)
    val prayerSplit = remember(slot.effectiveDailyGoal) { SakinaKhatmahStore.prayerWirdSplit(slot.effectiveDailyGoal) }
    val endWirdPage = (slot.currentPage + slot.effectiveDailyGoal - 1).coerceAtMost(604)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. شريط اختيار الختمة (1 · 2 · 3) + زرا الإعدادات والمنبه الذكي
        item(key = "khatmah_top_bar") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(velvetBg)
                        .border(1.dp, goldBorder, CircleShape)
                        .clickable { showPlanWizard = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚙", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 16.sp)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (0..2).forEach { idx ->
                        val selected = idx == activeSlotIdx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (selected) Brush.horizontalGradient(listOf(Color(0xFF0E4B3B), Color(0xFF14644F)))
                                    else Brush.horizontalGradient(listOf(Color(0xD9051A18), Color(0xD9051A18)))
                                )
                                .border(
                                    width = if (selected) 1.dp else 0.7.dp,
                                    color = if (selected) SakinaLuxuryColors.SoftGoldGlow else goldBorder.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    activeSlotIdx = idx
                                    SakinaKhatmahStore.setActiveSlot(ctx, idx)
                                    tick++
                                }
                                .padding(horizontal = 11.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "ختمة ${idx + 1}",
                                color = if (selected) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f),
                                fontSize = 11.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = bodyFontFamily,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(velvetBg)
                        .border(
                            1.dp,
                            if (slot.reminderEnabled) SakinaLuxuryColors.EmeraldGlow else goldBorder,
                            CircleShape
                        )
                        .clickable { showReminderDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔔", fontSize = 15.sp)
                }
            }
        }

        // 2. قوس المحراب الرئيسي: حلقة التقدم الذهبية + السلسلة + ورد اليوم + متابعة القراءة + التسجيل السريع
        item(key = "khatmah_hero_mihrab") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0B372D), Color(0xFF06211B), Color(0xFF041414))
                        )
                    )
                    .border(1.2.dp, SakinaLuxuryPalette.MetallicGoldTextBrush, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // اسم الختمة ووسم المقصد
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = slot.name,
                            style = TextStyle(
                                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = headingFontFamily,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0E4B3B))
                                .border(0.7.dp, goldBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = slot.intentTag,
                                color = SakinaLuxuryColors.SoftGoldGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = bodyFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // حلقة التقدم الذهبية ثلاثية الأبعاد
                    Box(
                        modifier = Modifier.size(116.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeW = 10.dp.toPx()
                            val pad = strokeW / 2f + 2.dp.toPx()
                            val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
                            drawArc(
                                color = Color(0xFF123B32),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = Offset(pad, pad),
                                size = arcSize,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                            val sweep = (slot.progressFraction * 360f).coerceAtLeast(if (slot.totalReadCount > 0) 6f else 0f)
                            if (sweep > 0f) {
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(Color(0xFFB69454), Color(0xFFF2DAA4), Color(0xFFFFEAB8), Color(0xFFD8B87A))
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = Offset(pad, pad),
                                    size = arcSize,
                                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${slot.percent}%",
                                style = TextStyle(
                                    brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                    shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SakinaNumeralFont,
                                    fontFeatureSettings = "tnum",
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                            Text(
                                text = "${slot.totalReadCount} / 604 ص",
                                color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SakinaNumeralFont,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // شريط السلسلة والختمات
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "السلسلة $streakDays يوماً · الختمات ${slot.completedCount} · المتبقي ${slot.remainingPages} ص",
                            color = Color(0xFF071A18),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = bodyFontFamily,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "الختم المتوقع: ${SakinaKhatmahStore.expectedCompletionDateAr(slot)} · ورد اليوم: ص ${slot.currentPage} ← ${endWirdPage}",
                        color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.86f),
                        fontSize = 11.5.sp,
                        fontFamily = bodyFontFamily,
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // زر متابعة القراءة الذهبي البارز
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                            .clickable { onOpenPageInMushaf(slot.currentPage) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "متابعة القراءة · صفحة ${slot.currentPage}",
                            color = Color(0xFF071A18),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = headingFontFamily,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }

                    Spacer(modifier = Modifier.height(9.dp))

                    // أزرار التسجيل اليدوي السريع (+1 صفحة، +ربع، +حزب، إتمام الورد)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "+1 صفحة" to 1,
                            "+ربع (2 ص)" to 2,
                            "+حزب (10 ص)" to 10,
                            "✓ أتممت الورد" to slot.effectiveDailyGoal
                        ).forEach { (lbl, pages) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF082E26))
                                    .border(0.85.dp, goldBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        SakinaKhatmahStore.logPages(ctx, activeSlotIdx, pages, minutesSpent = pages * 2)
                                        tick++
                                    }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lbl,
                                    color = SakinaLuxuryColors.SoftGoldGlow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = bodyFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. بطاقة «ورد الميقات المرن» (تقسيم الورد على الصلوات الخمس + اليوم المزدحم + المعوّض الصامت + هجين بالصوت)
        item(key = "khatmah_flexible_wird") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(velvetBg)
                    .border(1.dp, goldBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ورد الميقات المرن (${slot.effectiveDailyGoal} ص اليوم)",
                    style = TextStyle(
                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = headingFontFamily
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    SakinaKhatmahStore.PRAYER_NAMES_AR.forEachIndexed { pIdx, pName ->
                        val pPages = prayerSplit.getOrElse(pIdx) { 0 }
                        val done = remember(tick, activeSlotIdx) {
                            SakinaKhatmahStore.isPrayerWirdDoneToday(ctx, activeSlotIdx, pIdx)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (done) Color(0xFF125A46) else Color(0xFF041716))
                                .border(
                                    width = if (done) 1.1.dp else 0.75.dp,
                                    color = if (done) SakinaLuxuryColors.EmeraldGlow else goldBorder.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    SakinaKhatmahStore.togglePrayerWirdDoneToday(ctx, activeSlotIdx, pIdx, pPages)
                                    tick++
                                }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (done) "$pName ✓" else pName,
                                color = if (done) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = bodyFontFamily,
                                maxLines = 1
                            )
                            Text(
                                text = "$pPages ص",
                                color = if (done) SakinaLuxuryColors.EmeraldGlow else SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.85f),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaNumeralFont
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3 مفاتيح ذكية تكيفية: اليوم المزدحم · المعوّض الصامت · هجين بالصوت
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AdaptiveChip(
                        label = "اليوم المزدحم",
                        active = slot.isBusyDay,
                        bodyFontFamily = bodyFontFamily,
                        modifier = Modifier.weight(1f),
                        onClick = { SakinaKhatmahStore.toggleBusyDay(ctx, activeSlotIdx); tick++ }
                    )
                    AdaptiveChip(
                        label = "المعوّض الصامت",
                        active = slot.isZeroGuilt,
                        bodyFontFamily = bodyFontFamily,
                        modifier = Modifier.weight(1f),
                        onClick = { SakinaKhatmahStore.toggleZeroGuilt(ctx, activeSlotIdx); tick++ }
                    )
                    AdaptiveChip(
                        label = "هجين بالصوت",
                        active = slot.isHybridAudio,
                        bodyFontFamily = bodyFontFamily,
                        modifier = Modifier.weight(1f),
                        onClick = { SakinaKhatmahStore.toggleHybridAudio(ctx, activeSlotIdx); tick++ }
                    )
                }
            }
        }

        // 4. خريطة الـ 604 صفحة المضيئة (30 جزءاً × 20 صفحة)
        item(key = "khatmah_604_map") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(velvetBg)
                    .border(1.dp, goldBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "خريطة الـ 604 صفحة المضيئة",
                        style = TextStyle(
                            brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = headingFontFamily
                        )
                    )
                    Text(
                        text = "30 جزءاً · ${slot.totalReadCount} مضيئة",
                        color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f),
                        fontSize = 10.5.sp,
                        fontFamily = SakinaNumeralFont
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // شبكة 3 صفوف × 10 أجزاء (كل جزء 5 أعمدة × 4 صفوف = 20 نقطة)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                ) {
                    val blockCols = 10
                    val blockRows = 3
                    val blockGapX = 5.dp.toPx()
                    val blockGapY = 6.dp.toPx()
                    val blockW = (size.width - blockGapX * (blockCols - 1)) / blockCols
                    val blockH = (size.height - blockGapY * (blockRows - 1)) / blockRows
                    val dotCols = 5
                    val dotRows = 4
                    val r = (minOf(blockW / dotCols, blockH / dotRows) * 0.34f).coerceAtLeast(1.4f)

                    var pageIdx = 0
                    for (br in 0 until blockRows) {
                        for (bc in 0 until blockCols) {
                            // من اليمين إلى اليسار للترتيب العربي
                            val drawCol = (blockCols - 1) - bc
                            val bx = drawCol * (blockW + blockGapX)
                            val by = br * (blockH + blockGapY)
                            val cellW = blockW / dotCols
                            val cellH = blockH / dotRows
                            for (dr in 0 until dotRows) {
                                for (dc in 0 until dotCols) {
                                    if (pageIdx >= 604) break
                                    val isRead = slot.readPagesMask.getOrNull(pageIdx) == '1'
                                    val cx = bx + ((dotCols - 1 - dc) + 0.5f) * cellW
                                    val cy = by + (dr + 0.5f) * cellH
                                    if (isRead) {
                                        drawCircle(
                                            color = Color(0x55F2DAA4),
                                            radius = r * 1.9f,
                                            center = Offset(cx, cy)
                                        )
                                        drawCircle(
                                            color = Color(0xFFF4DDA6),
                                            radius = r,
                                            center = Offset(cx, cy)
                                        )
                                    } else {
                                        drawCircle(
                                            color = Color(0xFF1A433A),
                                            radius = r * 0.85f,
                                            center = Offset(cx, cy)
                                        )
                                    }
                                    pageIdx++
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. لوحة سجل القراءة (4 مؤشرات + سجل آخر 7 أيام + ساعة بداية اليوم)
        item(key = "khatmah_reading_log") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(velvetBg)
                    .border(0.95.dp, goldBorder, RoundedCornerShape(18.dp))
                    .clickable { showLogDetails = !showLogDetails }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سجل القراءة (بداية اليوم: ${if (dayStartHour == 0) "12 ص" else "$dayStartHour ص"})",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = headingFontFamily
                    )
                    Text(
                        text = if (showLogDetails) "إخفاء ▴" else "آخر 7 أيام ▾",
                        color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f),
                        fontSize = 11.sp,
                        fontFamily = bodyFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LogKpiMini("الأمس", "$yestP ص", Modifier.weight(1f), bodyFontFamily)
                    LogKpiMini("اليوم", "$todayP ص", Modifier.weight(1f), bodyFontFamily)
                    LogKpiMini("الوقت", "$todayM د", Modifier.weight(1f), bodyFontFamily)
                    LogKpiMini("الإجمالي", "$totalP ص", Modifier.weight(1f), bodyFontFamily)
                }

                if (showLogDetails) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = goldBorder.copy(alpha = 0.35f))
                    Spacer(modifier = Modifier.height(6.dp))
                    last7.forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = entry.dayLabel,
                                color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontFamily = bodyFontFamily
                            )
                            Text(
                                text = "${entry.pages} صفحة · ${entry.minutes} دقيقة",
                                color = if (entry.pages > 0) SakinaLuxuryColors.EmeraldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.55f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SakinaNumeralFont
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مسح سجل القراءة",
                        color = Color(0xFFE58A7E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable {
                                SakinaKhatmahStore.clearReadingLog(ctx)
                                tick++
                            }
                            .padding(4.dp)
                    )
                }
            }
        }

        // 6. الشريط السفلي: مؤقّت المصحف الورقي + دعاء الختم وبطاقة الإنجاز
        item(key = "khatmah_bottom_actions") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(velvetBg)
                        .border(1.dp, goldBorder, RoundedCornerShape(18.dp))
                        .clickable { showPaperTimerDialog = true }
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏳ مؤقّت المصحف الورقي",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(velvetBg)
                        .border(1.dp, goldBorder, RoundedCornerShape(18.dp))
                        .clickable { showKhatmDuaDialog = true }
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✦ دعاء الختم والإنجاز",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // نافذة معالج إنشاء وتخصيص الختمة (2-1)
    if (showPlanWizard) {
        KhatmahPlanWizardDialog(
            initial = slot,
            headingFontFamily = headingFontFamily,
            bodyFontFamily = bodyFontFamily,
            onDismiss = { showPlanWizard = false },
            onSave = { name, intent, startJuz, targetDays, dailyGoal ->
                SakinaKhatmahStore.savePlan(ctx, activeSlotIdx, name, intent, startJuz, targetDays, dailyGoal)
                showPlanWizard = false
                tick++
            }
        )
    }

    // نافذة المنبّه الذكي وساعة بداية اليوم (2-6، 2-7)
    if (showReminderDialog) {
        KhatmahReminderDialog(
            initial = slot,
            initialDayStartHour = dayStartHour,
            headingFontFamily = headingFontFamily,
            bodyFontFamily = bodyFontFamily,
            onDismiss = { showReminderDialog = false },
            onSave = { enabled, hour, minute, smartHabit, newDayStart ->
                SakinaKhatmahStore.saveReminder(ctx, activeSlotIdx, enabled, hour, minute, smartHabit)
                SakinaKhatmahStore.setDayStartHour(ctx, newDayStart)
                showReminderDialog = false
                tick++
            }
        )
    }

    // نافذة مؤقّت المصحف الورقي (2-4)
    if (showPaperTimerDialog) {
        PaperMushafTimerDialog(
            currentPage = slot.currentPage,
            headingFontFamily = headingFontFamily,
            bodyFontFamily = bodyFontFamily,
            onDismiss = { showPaperTimerDialog = false },
            onCommit = { pagesRead, mins ->
                SakinaKhatmahStore.logPages(ctx, activeSlotIdx, pagesRead, minutesSpent = mins)
                showPaperTimerDialog = false
                tick++
            }
        )
    }

    // نافذة دعاء الختم ومشاركة بطاقة الإنجاز (2-8)
    if (showKhatmDuaDialog) {
        var fontSp by remember { mutableIntStateOf(16) }
        AlertDialog(
            onDismissRequest = { showKhatmDuaDialog = false },
            containerColor = Color(0xFF071E1B),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "دعاء ختم القرآن الكريم",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontWeight = FontWeight.Bold,
                        fontFamily = headingFontFamily,
                        fontSize = 17.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "A-",
                            color = SakinaLuxuryColors.SoftGoldGlow,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { fontSp = (fontSp - 2).coerceAtLeast(13) }.padding(4.dp)
                        )
                        Text(
                            text = "A+",
                            color = SakinaLuxuryColors.SoftGoldGlow,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { fontSp = (fontSp + 2).coerceAtMost(26) }.padding(4.dp)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = SakinaKhatmahStore.KHATM_DUA_TEXT,
                        color = SakinaOrbitalColors.WarmCream,
                        fontSize = fontSp.sp,
                        lineHeight = (fontSp + 12).sp,
                        fontFamily = headingFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val shareTxt = "الحمد لله الذي بنعمته تتم الصالحات ✦\n" +
                            "إنجازي في ${slot.name} عبر تطبيق «نُورُ قُنُوت»: ${slot.percent}% (${slot.totalReadCount}/604 صفحة) · السلسلة: $streakDays يوماً."
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareTxt)
                        }
                        ctx.startActivity(Intent.createChooser(intent, "مشاركة بطاقة الإنجاز"))
                    }
                ) {
                    Text("مشاركة الإنجاز", color = SakinaLuxuryColors.EmeraldGlow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKhatmDuaDialog = false }) {
                    Text("إغلاق", color = SakinaLuxuryColors.SoftGoldGlow)
                }
            }
        )
    }
}

@Composable
private fun AdaptiveChip(
    label: String,
    active: Boolean,
    bodyFontFamily: FontFamily,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) SakinaLuxuryPalette.MetallicGoldButtonBrush else Brush.horizontalGradient(listOf(Color(0xFF041716), Color(0xFF041716))))
            .border(
                width = 0.85.dp,
                color = if (active) SakinaLuxuryColors.SoftGoldGlow else SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = bodyFontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LogKpiMini(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    bodyFontFamily: FontFamily
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF041716))
            .border(0.7.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = SakinaLuxuryColors.SoftGoldGlow,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SakinaNumeralFont
        )
        Text(
            text = title,
            color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.72f),
            fontSize = 10.sp,
            fontFamily = bodyFontFamily
        )
    }
}

@Composable
private fun KhatmahPlanWizardDialog(
    initial: KhatmahSlotState,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var intentTag by remember { mutableStateOf(initial.intentTag) }
    var startJuz by remember { mutableIntStateOf(initial.startJuz) }
    var targetDays by remember { mutableIntStateOf(initial.targetDays) }
    val remainingFromStart = (604 - SakinaKhatmahStore.juzStartPage(startJuz) + 1).coerceAtLeast(1)
    val dailyCalc = ceil(remainingFromStart.toDouble() / targetDays.toDouble()).toInt().coerceAtLeast(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071E1B),
        title = {
            Text(
                text = "معالج إعداد خطة الختمة",
                color = SakinaLuxuryColors.SoftGoldGlow,
                fontWeight = FontWeight.Bold,
                fontFamily = headingFontFamily
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الختمة", color = SakinaLuxuryColors.SoftGoldGlow) },
                    singleLine = true,
                    textStyle = TextStyle(color = SakinaOrbitalColors.WarmCream, fontFamily = bodyFontFamily)
                )

                Text("المقصد من الختمة:", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SakinaKhatmahStore.INTENT_TAGS) { tag ->
                        AdaptiveChip(
                            label = tag,
                            active = intentTag == tag,
                            bodyFontFamily = bodyFontFamily,
                            modifier = Modifier.width(68.dp),
                            onClick = { intentTag = tag }
                        )
                    }
                }

                Text("قوالب جاهزة:", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SakinaKhatmahStore.PRESET_TEMPLATES) { (lbl, days) ->
                        AdaptiveChip(
                            label = lbl,
                            active = targetDays == days,
                            bodyFontFamily = bodyFontFamily,
                            modifier = Modifier.width(122.dp),
                            onClick = { targetDays = days }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("المدة (بالأيام): $targetDays يوماً", color = SakinaOrbitalColors.WarmCream, fontSize = 12.5.sp, fontFamily = SakinaNumeralFont)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("−", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 20.sp, modifier = Modifier.clickable { targetDays = (targetDays - 5).coerceAtLeast(3) }.padding(6.dp))
                        Text("+", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 20.sp, modifier = Modifier.clickable { targetDays = (targetDays + 5).coerceAtMost(240) }.padding(6.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("بداية الختمة: الجزء $startJuz", color = SakinaOrbitalColors.WarmCream, fontSize = 12.5.sp, fontFamily = SakinaNumeralFont)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("−", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 20.sp, modifier = Modifier.clickable { startJuz = (startJuz - 1).coerceAtLeast(1) }.padding(6.dp))
                        Text("+", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 20.sp, modifier = Modifier.clickable { startJuz = (startJuz + 1).coerceAtMost(30) }.padding(6.dp))
                    }
                }

                Text(
                    text = "الورد اليومي المحسوب: $dailyCalc صفحة يومياً",
                    color = SakinaLuxuryColors.EmeraldGlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = bodyFontFamily
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, intentTag, startJuz, targetDays, dailyCalc) }) {
                Text("حفظ الخطة", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
private fun KhatmahReminderDialog(
    initial: KhatmahSlotState,
    initialDayStartHour: Int,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onDismiss: () -> Unit,
    onSave: (Boolean, Int, Int, Boolean, Int) -> Unit
) {
    var remOn by remember { mutableStateOf(initial.reminderEnabled) }
    var hour by remember { mutableIntStateOf(initial.reminderHour) }
    var minute by remember { mutableIntStateOf(initial.reminderMinute) }
    var smartHabit by remember { mutableStateOf(initial.smartHabitEnabled) }
    var dayStart by remember { mutableIntStateOf(initialDayStartHour) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071E1B),
        title = {
            Text("المنبّه الذكي وبداية اليوم", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("تفعيل منبّه الورد اليومي", color = SakinaOrbitalColors.WarmCream, fontSize = 13.sp, fontFamily = bodyFontFamily)
                    Switch(
                        checked = remOn,
                        onCheckedChange = { remOn = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SakinaLuxuryColors.SoftGoldGlow, checkedTrackColor = Color(0xFF14644F))
                    )
                }

                if (remOn) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "وقت المنبّه: %02d:%02d".format(hour, minute),
                            color = SakinaLuxuryColors.SoftGoldGlow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SakinaNumeralFont
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("ساعة+", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 12.sp, modifier = Modifier.clickable { hour = (hour + 1) % 24 }.padding(4.dp))
                            Text("15د+", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 12.sp, modifier = Modifier.clickable { minute = (minute + 15) % 60 }.padding(4.dp))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("التنبيه السياقي الذكي (نافذتك المفضلة)", color = SakinaOrbitalColors.WarmCream, fontSize = 12.sp, fontFamily = bodyFontFamily, modifier = Modifier.weight(1f))
                    Switch(
                        checked = smartHabit,
                        onCheckedChange = { smartHabit = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SakinaLuxuryColors.SoftGoldGlow, checkedTrackColor = Color(0xFF14644F))
                    )
                }

                HorizontalDivider(color = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.3f))
                Text("ساعة بداية اليوم الجديد (لقراء ما بعد منتصف الليل):", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.5.sp, fontFamily = bodyFontFamily)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    (0..4).forEach { h ->
                        AdaptiveChip(
                            label = if (h == 0) "12 ص" else "$h ص",
                            active = dayStart == h,
                            bodyFontFamily = bodyFontFamily,
                            modifier = Modifier.weight(1f),
                            onClick = { dayStart = h }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(remOn, hour, minute, smartHabit, dayStart) }) {
                Text("حفظ", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
private fun PaperMushafTimerDialog(
    currentPage: Int,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onDismiss: () -> Unit,
    onCommit: (Int, Int) -> Unit
) {
    var running by remember { mutableStateOf(true) }
    var elapsedSec by remember { mutableIntStateOf(0) }
    var pagesRead by remember { mutableIntStateOf(2) }

    LaunchedEffect(running) {
        while (running) {
            delay(1000L)
            elapsedSec++
        }
    }

    val mins = elapsedSec / 60
    val secs = elapsedSec % 60

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071E1B),
        title = {
            Text("مؤقّت المصحف الورقي", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "%02d:%02d".format(mins, secs),
                    color = SakinaLuxuryColors.SoftGoldGlow,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SakinaNumeralFont
                )
                Text(
                    text = "بداية الجلسة من صفحة $currentPage",
                    color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontFamily = bodyFontFamily
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0E4B3B))
                        .clickable { running = !running }
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (running) "إيقاف مؤقت" else "استئناف المؤقّت",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الصفحات المقروءة: $pagesRead ص", color = SakinaOrbitalColors.WarmCream, fontSize = 13.sp, fontFamily = SakinaNumeralFont)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("−", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 22.sp, modifier = Modifier.clickable { pagesRead = (pagesRead - 1).coerceAtLeast(1) }.padding(4.dp))
                        Text("+", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 22.sp, modifier = Modifier.clickable { pagesRead = (pagesRead + 1).coerceAtMost(50) }.padding(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCommit(pagesRead, (elapsedSec / 60).coerceAtLeast(1)) }) {
                Text("تسجيل الجلسة", color = SakinaLuxuryColors.EmeraldGlow, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.7f))
            }
        }
    )
}
