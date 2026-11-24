package app.sakina

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

// ============================================================================
// 1. نماذج البيانات، التوطين، وحالة التقويم الهجري والقمر الفلكي
// ============================================================================
enum class CalendarDisplayMode {
    HIJRI,
    GREGORIAN
}

data class CalendarDayCell(
    val dayNumber: Int,
    val isCurrentMonth: Boolean = true,
    val isToday: Boolean = false,
    val isFriday: Boolean = false,
    val isWhiteDay: Boolean = false,       // الأيام البيض 13، 14، 15
    val hasOccasion: Boolean = false,
    val occasionName: String? = null
)

data class DayPrayerSummary(
    val fajr: String = "06:09",
    val dhuhr: String = "12:43",
    val asr: String = "15:26",
    val maghrib: String = "17:48",
    val isha: String = "19:10"
)

data class SakinaCalendarStrings(
    val screenTitle: String = "التقويم الهجري",
    val hijriModeLabel: String = "هجري",
    val gregorianModeLabel: String = "ميلادي",
    val sharePngLabel: String = "PNG",
    val sharePngDesc: String = "مشاركة الإمساكية الشهرية كصورة",
    val prevMonthDesc: String = "الشهر السابق",
    val nextMonthDesc: String = "الشهر التالي",
    val weekdayHeaders: List<String> = listOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة"),
    val fajrLabel: String = "الفجر",
    val dhuhrLabel: String = "الظهر",
    val asrLabel: String = "العصر",
    val maghribLabel: String = "المغرب",
    val ishaLabel: String = "العشاء",
    val whiteDayBadge: String = "من الأيام البيض",
    val hadithCardTitle: String = "حديث اليوم",
    val moonPhaseWaxingCrescent: String = "هلال متزايد",
    val moonPhaseFirstQuarter: String = "تربيع أول",
    val moonPhaseWaxingGibbous: String = "أحدب متزايد",
    val moonPhaseFullMoon: String = "بدر مكتمل",
    val moonPhaseWaningGibbous: String = "أحدب متناقص",
    val moonPhaseLastQuarter: String = "تربيع أخير",
    val moonPhaseWaningCrescent: String = "هلال متناقص",
    val moonPhaseNewMoon: String = "محاق"
)

data class SakinaLunarCalendarState(
    val displayMode: CalendarDisplayMode = CalendarDisplayMode.HIJRI,
    val hijriDayOfMonth: Int = 12,
    val hijriDateTitle: String = "12 جمادى الآخرة 1448 هـ",
    val monthHeaderTitle: String = "جمادى الآخرة 1448 هـ",
    val gregorianSubtitle: String = "23 نوفمبر 2026",
    val monthCells: List<CalendarDayCell> = emptyList(),
    val selectedDayNumber: Int = 12,
    val selectedDayPrayers: DayPrayerSummary = DayPrayerSummary(),
    val hadithTopic: String = "صيام الإثنين والخميس",
    val hadithText: String = "سُئل رسول الله ﷺ عن صوم يوم الإثنين فقال: «ذاك يوم وُلدتُ فيه، ويوم بُعثتُ أو أُنزل عليَّ فيه».",
    val hadithSource: String = "رواه مسلم (1162)"
)

// ============================================================================
// 2. شاشة التقويم الهجري والقمر الفلكي (Lunar Phase & Calendar Screen)
// ============================================================================
@Composable
fun SakinaLunarCalendarScreen(
    state: SakinaLunarCalendarState,
    strings: SakinaCalendarStrings = SakinaCalendarStrings(),
    dockStrings: SakinaHomeStrings = SakinaHomeStrings(),
    headingFontFamily: FontFamily = FontFamily.Default,
    bodyFontFamily: FontFamily = FontFamily.Default,
    selectedTabIndex: Int = 3,
    showBottomDock: Boolean = false,
    hostBottomBarReserve: Dp = 82.dp,
    onDisplayModeChange: (CalendarDisplayMode) -> Unit = {},
    onShareMonthlyPngClick: () -> Unit = {},
    onPrevMonthClick: () -> Unit = {},
    onNextMonthClick: () -> Unit = {},
    onDayClick: (CalendarDayCell) -> Unit = {},
    onTabSelected: (Int) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val selectedCell = state.monthCells.firstOrNull { it.isCurrentMonth && it.dayNumber == state.selectedDayNumber }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SakinaLuxuryPalette.ObsidianNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SakinaAmbientLuxuryBackdrop(showGeometricPattern = true, showStarDust = true)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 50.dp, bottom = if (!showBottomDock) hostBottomBarReserve else 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // 1. الترويسة العلوية: طور القمر الفلكي + مبدّل هجري/ميلادي + زر PNG
                LunarCalendarHeroHeader(
                    state = state,
                    strings = strings,
                    headingFontFamily = headingFontFamily,
                    bodyFontFamily = bodyFontFamily,
                    onDisplayModeChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDisplayModeChange(it)
                    },
                    onShareMonthlyPngClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onShareMonthlyPngClick()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. بطاقة الشبكة الشهرية الزجاجية
                MonthlyCalendarGlassCard(
                    state = state,
                    strings = strings,
                    headingFontFamily = headingFontFamily,
                    bodyFontFamily = bodyFontFamily,
                    onPrevMonthClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPrevMonthClick()
                    },
                    onNextMonthClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNextMonthClick()
                    },
                    onDayClick = { cell ->
                        if (cell.isCurrentMonth) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDayClick(cell)
                        }
                    }
                )

                // شارة المناسبة أو الأيام البيض لليوم المختار إن وُجدت
                if (selectedCell != null && (selectedCell.isWhiteDay || !selectedCell.occasionName.isNullOrBlank())) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (selectedCell.isWhiteDay) {
                            OccasionChip(text = strings.whiteDayBadge, bodyFontFamily = bodyFontFamily)
                        }
                        selectedCell.occasionName?.takeIf { it.isNotBlank() }?.let { occ ->
                            OccasionChip(text = occ, bodyFontFamily = bodyFontFamily)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. شريط مواقيت اليوم المختار
                SelectedDayPrayerBar(
                    prayers = state.selectedDayPrayers,
                    strings = strings,
                    bodyFontFamily = bodyFontFamily
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. بطاقة حديث اليوم على الورق الكريمي
                HadithParchmentCard(
                    title = "${strings.hadithCardTitle} · ${state.hadithTopic}",
                    hadithText = state.hadithText,
                    source = state.hadithSource,
                    headingFontFamily = headingFontFamily,
                    bodyFontFamily = bodyFontFamily
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            if (showBottomDock) {
                LiquidDynamicDock(
                    selectedIndex = selectedTabIndex,
                    strings = dockStrings,
                    bodyFontFamily = bodyFontFamily,
                    onTabSelected = onTabSelected
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ============================================================================
// 3. المكونات الفرعية للشاشة
// ============================================================================

@Composable
private fun LunarCalendarHeroHeader(
    state: SakinaLunarCalendarState,
    strings: SakinaCalendarStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onDisplayModeChange: (CalendarDisplayMode) -> Unit,
    onShareMonthlyPngClick: () -> Unit
) {
    val lunarPhaseLabel = remember(state.hijriDayOfMonth, strings) {
        when (state.hijriDayOfMonth) {
            1, 29, 30 -> strings.moonPhaseNewMoon
            in 2..6 -> strings.moonPhaseWaxingCrescent
            7, 8 -> strings.moonPhaseFirstQuarter
            in 9..13 -> strings.moonPhaseWaxingGibbous
            14, 15, 16 -> strings.moonPhaseFullMoon
            in 17..21 -> strings.moonPhaseWaningGibbous
            22, 23 -> strings.moonPhaseLastQuarter
            else -> strings.moonPhaseWaningCrescent
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // مبدّل هجري / ميلادي
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(SakinaOrbitalColors.GlassNavy)
                    .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.38f), RoundedCornerShape(18.dp))
                    .padding(3.dp)
            ) {
                listOf(
                    CalendarDisplayMode.HIJRI to strings.hijriModeLabel,
                    CalendarDisplayMode.GREGORIAN to strings.gregorianModeLabel
                ).forEach { (mode, label) ->
                    val selected = state.displayMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(15.dp))
                            .background(if (selected) SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.92f) else Color.Transparent)
                            .clickable { onDisplayModeChange(mode) }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (selected) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f),
                            fontSize = 11.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = bodyFontFamily
                        )
                    }
                }
            }

            // زر مشاركة الإمساكية الشهرية PNG
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.16f))
                    .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onShareMonthlyPngClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .semantics { contentDescription = strings.sharePngDesc }
            ) {
                Canvas(modifier = Modifier.size(14.dp)) {
                    drawShareVector(SakinaOrbitalColors.SoftGoldGlow)
                }
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = strings.sharePngLabel,
                    color = SakinaOrbitalColors.SoftGoldGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = bodyFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // صف القمر الفلكي ثلاثي الأبعاد مع التاريخ والطور
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.hijriDateTitle,
                    color = SakinaOrbitalColors.SoftGoldGlow,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${state.gregorianSubtitle} · $lunarPhaseLabel",
                    color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    fontFamily = bodyFontFamily,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            AstronomicalMoonPhaseCanvas(
                hijriDay = state.hijriDayOfMonth,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
private fun AstronomicalMoonPhaseCanvas(
    hijriDay: Int,
    modifier: Modifier = Modifier
) {
    val targetPhase = ((hijriDay.coerceIn(1, 30) - 1) / 29.5f).coerceIn(0f, 1f)
    val animatedPhase by animateFloatAsState(
        targetValue = targetPhase,
        animationSpec = tween(durationMillis = 600),
        label = "moonPhase"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // صورة القمر الفلكي الواقعي بالهالة الذهبية الدافئة
        Image(
            painter = painterResource(id = R.drawable.ic_lunar_moon_glow),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        // طبقة ظل فلكي ديناميكية تعكس طور القمر الهجري اليومي بدقة
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.37f
            val illumination = ((1.0 - cos(animatedPhase * 2.0 * PI)) / 2.0).coerceIn(0.0, 1.0)
            val shadowFrac = (1.0 - illumination).toFloat()
            if (shadowFrac > 0.06f) {
                val waxing = animatedPhase <= 0.5f
                val shadowOffsetX = if (waxing) -radius * (1.25f - shadowFrac * 1.1f) else radius * (1.25f - shadowFrac * 1.1f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xD8050D1A),
                            Color(0xA6050D1A),
                            Color.Transparent
                        ),
                        center = Offset(center.x + shadowOffsetX, center.y),
                        radius = radius * (0.75f + shadowFrac * 0.55f)
                    ),
                    radius = radius,
                    center = center
                )
            }
        }
    }
}

@Composable
private fun MonthlyCalendarGlassCard(
    state: SakinaLunarCalendarState,
    strings: SakinaCalendarStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onPrevMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onDayClick: (CalendarDayCell) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .sakinaLuxuryGlassCard(
                cornerRadius = 22.dp,
                innerHairline = false,
                emeraldFlare = true,
                shimmer = false,
                borderAlpha = 0.65f
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // شريط التنقل بين الأشهر
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.14f))
                        .clickable(onClick = onPrevMonthClick)
                        .semantics { contentDescription = strings.prevMonthDesc },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = SakinaOrbitalColors.SoftGoldGlow, modifier = Modifier.size(20.dp))
                }

                Text(
                    text = state.monthHeaderTitle,
                    color = SakinaOrbitalColors.WarmCream,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.14f))
                        .clickable(onClick = onNextMonthClick)
                        .semantics { contentDescription = strings.nextMonthDesc },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = SakinaOrbitalColors.SoftGoldGlow, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // رؤوس أيام الأسبوع
            Row(modifier = Modifier.fillMaxWidth()) {
                strings.weekdayHeaders.forEachIndexed { idx, dayName ->
                    val isFri = idx == strings.weekdayHeaders.lastIndex
                    Text(
                        text = dayName,
                        color = if (isFri) SakinaOrbitalColors.ChampagneGold else SakinaOrbitalColors.WarmCream.copy(alpha = 0.65f),
                        fontSize = 10.sp,
                        fontWeight = if (isFri) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = bodyFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // شبكة الأيام (صفوف من 7 خانات)
            val rows = remember(state.monthCells) { state.monthCells.chunked(7) }
            rows.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0 until 7) {
                        val cell = week.getOrNull(col)
                        if (cell == null) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val isSelected = cell.isCurrentMonth && cell.dayNumber == state.selectedDayNumber
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.08f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .then(
                                        when {
                                            isSelected -> Modifier.background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                                            cell.isToday -> Modifier.background(SakinaLuxuryPalette.GoldBright.copy(alpha = 0.22f))
                                            else -> Modifier
                                        }
                                    )
                                    .clickable(enabled = cell.isCurrentMonth) { onDayClick(cell) }
                            ) {
                                Text(
                                    text = if (cell.isCurrentMonth && cell.dayNumber > 0) cell.dayNumber.toString() else "",
                                    color = when {
                                        isSelected -> SakinaOrbitalColors.InkNavy
                                        !cell.isCurrentMonth -> SakinaOrbitalColors.WarmCream.copy(alpha = 0.28f)
                                        cell.isFriday -> SakinaOrbitalColors.SoftGoldGlow
                                        else -> SakinaOrbitalColors.WarmCream
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected || cell.isToday || cell.isFriday) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = bodyFontFamily,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )

                                // مؤشر نقطي للأيام البيض أو المناسبات الدينية
                                if (cell.isCurrentMonth && (cell.isWhiteDay || cell.hasOccasion)) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 1.dp)
                                            .size(4.dp)
                                            .background(
                                                color = when {
                                                    isSelected -> SakinaOrbitalColors.InkNavy
                                                    cell.hasOccasion -> Color(0xFF34D399)
                                                    else -> SakinaOrbitalColors.SoftGoldGlow
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OccasionChip(text: String, bodyFontFamily: FontFamily) {
    Text(
        text = text,
        color = SakinaOrbitalColors.SoftGoldGlow,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = bodyFontFamily,
        modifier = Modifier
            .background(SakinaOrbitalColors.DeepEmerald.copy(alpha = 0.78f), RoundedCornerShape(12.dp))
            .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun SelectedDayPrayerBar(
    prayers: DayPrayerSummary,
    strings: SakinaCalendarStrings,
    bodyFontFamily: FontFamily
) {
    val entries = listOf(
        strings.fajrLabel to prayers.fajr,
        strings.dhuhrLabel to prayers.dhuhr,
        strings.asrLabel to prayers.asr,
        strings.maghribLabel to prayers.maghrib,
        strings.ishaLabel to prayers.isha
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .sakinaLuxuryGlassCard(
                cornerRadius = 20.dp,
                innerHairline = false,
                emeraldFlare = false,
                shimmer = false,
                borderAlpha = 0.60f
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            entries.forEach { (label, time) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        style = TextStyle(
                            brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = bodyFontFamily
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = time,
                        color = SakinaOrbitalColors.WarmCream,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
            }
        }
    }
}

@Composable
private fun HadithParchmentCard(
    title: String,
    hadithText: String,
    source: String,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .sakinaParchmentGlowCard(
                cornerRadius = 20.dp,
                haloAlpha = 0.38f,
                doubleBorder = false
            )
            .clip(RoundedCornerShape(20.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_parchment_card),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "◆ $title ◆",
                color = SakinaOrbitalColors.MutedGoldText,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = headingFontFamily,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hadithText,
                color = SakinaOrbitalColors.InkNavy,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                fontFamily = bodyFontFamily,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = source,
                color = SakinaOrbitalColors.DeepEmerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = bodyFontFamily
            )
        }
    }
}

private fun DrawScope.drawShareVector(tint: Color) {
    val stroke = 1.6.dp.toPx()
    val w = size.width
    val h = size.height
    drawRoundRect(
        color = tint,
        topLeft = Offset(w * 0.16f, h * 0.34f),
        size = Size(w * 0.68f, h * 0.52f),
        cornerRadius = CornerRadius(2.5.dp.toPx()),
        style = Stroke(stroke)
    )
    drawLine(tint, Offset(w * 0.5f, h * 0.58f), Offset(w * 0.5f, h * 0.12f), strokeWidth = stroke, cap = StrokeCap.Round)
    drawLine(tint, Offset(w * 0.34f, h * 0.28f), Offset(w * 0.5f, h * 0.12f), strokeWidth = stroke, cap = StrokeCap.Round)
    drawLine(tint, Offset(w * 0.66f, h * 0.28f), Offset(w * 0.5f, h * 0.12f), strokeWidth = stroke, cap = StrokeCap.Round)
}
