package app.sakina

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

// ============================================================================
// 1. لوحة ألوان سَكِينة المعتمدة (Sakina Identity Palette - v33 Orbital Horizon)
// ============================================================================
object SakinaOrbitalColors {
    val DeepSpaceNavy = Color(0xFF06101F)
    val MidnightNavy = Color(0xFF0A192F)
    val DeepEmerald = Color(0xFF0D3B2E)
    val ChampagneGold = Color(0xFFD4AF37)
    val SoftGoldGlow = Color(0xFFF3DFA2)
    val WarmCream = Color(0xFFFAF6EC)
    val InkNavy = Color(0xFF0F1E36)
    val MutedGoldText = Color(0xFF7A6320)
    val GlassNavy = Color(0xCC0D1F38)
}

// ============================================================================
// 2. نماذج البيانات، التوطين، وحالة الشاشة (Localized State Models)
// ============================================================================
enum class PrayerType {
    FAJR, DHUHR, ASR, MAGHRIB, ISHA
}

enum class PrayerAlarmMode {
    ADHAN,        // أذان كامل
    NOTIFICATION, // إشعار هادئ
    SILENT;       // صامت

    fun next(): PrayerAlarmMode = when (this) {
        ADHAN -> NOTIFICATION
        NOTIFICATION -> SILENT
        SILENT -> ADHAN
    }
}

data class OrbitalPrayerNode(
    val type: PrayerType,
    val localizedName: String,
    val timeText: String,
    val isCompleted: Boolean = false,
    val alarmMode: PrayerAlarmMode = PrayerAlarmMode.ADHAN
)

data class DayChipItem(
    val dayLabel: String,
    val dayNumber: String,
    val isSelected: Boolean = false
)

data class SakinaHomeStrings(
    val appTitle: String = "سَكِينة",
    val menuDesc: String = "القائمة",
    val settingsDesc: String = "الإعدادات",
    val todayProgressFormat: String = "%d/%d اليوم",
    val remainingPrefix: String = "المتبقي لصلاة",
    val prayerTimePrefix: String = "وقت صلاة",
    val alarmAdhanLabel: String = "أذان",
    val alarmNotifLabel: String = "إشعار",
    val alarmSilentLabel: String = "صامت",
    val imsakLabel: String = "الإمساك",
    val sunriseLabel: String = "الشروق",
    val duhaLabel: String = "الضحى",
    val midnightLabel: String = "منتصف الليل",
    val tahajjudLabel: String = "التهجد",
    val tabHome: String = "الرئيسية",
    val tabQibla: String = "القبلة",
    val tabQuran: String = "القرآن",
    val tabCalendar: String = "التقويم",
    val toggleCompleteDesc: String = "تسجيل أداء صلاة",
    val toggleAlarmDesc: String = "تغيير تنبيه",
    val switchLayoutLabel: String = "كلاسيكي"
)

data class SakinaOrbitalHomeState(
    val cityName: String = "فرندة",
    val hijriDate: String = "12 جمادى الآخرة 1448 هـ",
    val nextPrayerIndex: Int = 1,
    val countdownText: String = "02:46:19",
    val days: List<DayChipItem> = emptyList(),
    val prayers: List<OrbitalPrayerNode> = emptyList(),
    val imsakTime: String = "05:59",
    val sunriseTime: String = "07:38",
    val duhaTime: String = "07:53",
    val midnightTime: String = "23:58",
    val tahajjudTime: String = "02:02",
    val languageCode: String = "AR"
)

// ============================================================================
// 3. الشاشة الرئيسية: المدار الكوني التفاعلي (Orbital Living Horizon)
// ============================================================================
@Composable
fun SakinaOrbitalHomeScreen(
    state: SakinaOrbitalHomeState,
    strings: SakinaHomeStrings = SakinaHomeStrings(),
    headingFontFamily: FontFamily = FontFamily.Default,
    bodyFontFamily: FontFamily = FontFamily.Default,
    selectedTabIndex: Int = 0,
    showBottomDock: Boolean = false,
    onMenuClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onCityClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onToggleClassicLayout: () -> Unit = {},
    onDaySelected: (Int) -> Unit = {},
    onTogglePrayerCompleted: (PrayerType) -> Unit = {},
    onTodayStatsClick: () -> Unit = {},
    onCyclePrayerAlarmMode: (PrayerType) -> Unit = {},
    onTabSelected: (Int) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var focusedIndex by remember(state.nextPrayerIndex) { mutableIntStateOf(state.nextPrayerIndex) }

    val focusedPrayer = state.prayers.getOrElse(focusedIndex) {
        state.prayers.firstOrNull() ?: OrbitalPrayerNode(PrayerType.FAJR, "الفجر", "06:00")
    }
    val completedCount = state.prayers.count { it.isCompleted }

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
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = if (showBottomDock) 8.dp else 82.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. الشريط العلوي المختصر (القائمة + المدينة والهجري + اللغة + تبديل النمط + الإعدادات)
            MinimalistOrbitalHeader(
                appTitle = strings.appTitle,
                cityName = state.cityName,
                hijriDate = state.hijriDate,
                languageCode = state.languageCode,
                menuDesc = strings.menuDesc,
                settingsDesc = strings.settingsDesc,
                headingFontFamily = headingFontFamily,
                bodyFontFamily = bodyFontFamily,
                onMenuClick = onMenuClick,
                onSettingsClick = onSettingsClick,
                onCityClick = onCityClick,
                onLanguageClick = onLanguageClick,
                onToggleClassicLayout = onToggleClassicLayout
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2. شريط الأيام السبعة للمعاينة الفورية
            if (state.days.isNotEmpty()) {
                SevenDaySelectorStrip(
                    days = state.days,
                    bodyFontFamily = bodyFontFamily,
                    onDaySelected = { idx ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDaySelected(idx)
                    }
                )
            }

            // 3. المدار الكوني الإهليلجي (BoxWithConstraints + absoluteOffset لمحاذاة 0px في RTL وLTR)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (state.prayers.isNotEmpty()) {
                    OrbitalPrayerDial(
                        prayers = state.prayers,
                        focusedIndex = focusedIndex,
                        nextPrayerIndex = state.nextPrayerIndex,
                        bodyFontFamily = bodyFontFamily,
                        onSelectPrayer = { idx ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            focusedIndex = idx
                        },
                        onQuickToggleCompleted = { type ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTogglePrayerCompleted(type)
                        }
                    )
                }
            }

            // 4. بطاقة التركيز المضيئة (الورق الكريمي + العداد + التنبيه الثلاثي + التسجيل)
            FocalSpotlightCard(
                prayer = focusedPrayer,
                isNextPrayer = focusedIndex == state.nextPrayerIndex,
                countdownText = state.countdownText,
                completedCount = completedCount,
                totalPrayers = state.prayers.size.coerceAtLeast(5),
                strings = strings,
                headingFontFamily = headingFontFamily,
                bodyFontFamily = bodyFontFamily,
                onTodayStatsClick = onTodayStatsClick,
                onToggleCompleted = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTogglePrayerCompleted(focusedPrayer.type)
                },
                onCycleAlarmMode = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCyclePrayerAlarmMode(focusedPrayer.type)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. شريط الشروق والنوافل الكامل (الإمساك، الشروق، الضحى، منتصف الليل، التهجد)
            ComprehensiveNafilahStrip(
                state = state,
                strings = strings,
                bodyFontFamily = bodyFontFamily
            )

            if (showBottomDock) {
                Spacer(modifier = Modifier.height(10.dp))
                LiquidDynamicDock(
                    selectedIndex = selectedTabIndex,
                    strings = strings,
                    bodyFontFamily = bodyFontFamily,
                    onTabSelected = onTabSelected
                )
            }
        }
    }
}

// ============================================================================
// 4. المكونات الفرعية
// ============================================================================

@Composable
private fun MinimalistOrbitalHeader(
    appTitle: String,
    cityName: String,
    hijriDate: String,
    languageCode: String,
    menuDesc: String,
    settingsDesc: String,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCityClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onToggleClassicLayout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = menuDesc,
                    tint = SakinaOrbitalColors.ChampagneGold
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.45f), CircleShape)
                    .clickable(onClick = onLanguageClick)
                    .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
                Text(
                    text = languageCode,
                    color = SakinaOrbitalColors.SoftGoldGlow,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = bodyFontFamily
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onCityClick)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = appTitle,
                color = SakinaOrbitalColors.WarmCream,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = headingFontFamily,
                maxLines = 1
            )
            Text(
                text = "$cityName · $hijriDate",
                color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.88f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = bodyFontFamily,
                maxLines = 1
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // 1-tap toggle to Classic List layout anytime
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.40f), CircleShape)
                    .clickable(onClick = onToggleClassicLayout)
                    .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "☰",
                    color = SakinaOrbitalColors.SoftGoldGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = settingsDesc,
                    tint = SakinaOrbitalColors.ChampagneGold
                )
            }
        }
    }
}

@Composable
private fun SevenDaySelectorStrip(
    days: List<DayChipItem>,
    bodyFontFamily: FontFamily,
    onDaySelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        itemsIndexed(days) { index, day ->
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (day.isSelected) {
                            SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.92f)
                        } else {
                            SakinaOrbitalColors.GlassNavy.copy(alpha = 0.55f)
                        }
                    )
                    .border(
                        width = 0.8.dp,
                        color = if (day.isSelected) {
                            SakinaOrbitalColors.SoftGoldGlow
                        } else {
                            SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.22f)
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onDaySelected(index) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = day.dayLabel,
                    color = if (day.isSelected) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.72f),
                    fontSize = 10.sp,
                    fontFamily = bodyFontFamily
                )
                Text(
                    text = day.dayNumber,
                    color = if (day.isSelected) SakinaLuxuryColors.WarmCream else SakinaOrbitalColors.SoftGoldGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = bodyFontFamily,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OrbitalPrayerDial(
    prayers: List<OrbitalPrayerNode>,
    focusedIndex: Int,
    nextPrayerIndex: Int,
    bodyFontFamily: FontFamily,
    onSelectPrayer: (Int) -> Unit,
    onQuickToggleCompleted: (PrayerType) -> Unit
) {
    val count = prayers.size.coerceAtLeast(1)
    val twoPi = (2.0 * PI).toFloat()

    var continuousRotation by remember {
        mutableFloatStateOf(((PI / 2.0) - (focusedIndex * (2.0 * PI / count))).toFloat())
    }
    LaunchedEffect(focusedIndex, count) {
        val desired = ((PI / 2.0) - (focusedIndex * (2.0 * PI / count))).toFloat()
        var delta = (desired - continuousRotation) % twoPi
        if (delta > PI.toFloat()) delta -= twoPi
        if (delta < -PI.toFloat()) delta += twoPi
        continuousRotation += delta
    }

    val animatedRotation by animateFloatAsState(
        targetValue = continuousRotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "orbitalRotation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val orbitTiltDeg = -14f
    val tiltRad = orbitTiltDeg * (PI / 180.0)
    val cosTilt = cos(tiltRad)
    val sinTilt = sin(tiltRad)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(248.dp)
            .pointerInput(focusedIndex) {
                var accumulatedDrag = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        accumulatedDrag += dragAmount
                        if (accumulatedDrag > 64f) {
                            accumulatedDrag = 0f
                            onSelectPrayer((focusedIndex - 1 + count) % count)
                        } else if (accumulatedDrag < -64f) {
                            accumulatedDrag = 0f
                            onSelectPrayer((focusedIndex + 1) % count)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val dotCenterOffsetPx = with(density) { 14.dp.toPx() }
        val rxPx = (widthPx * 0.38f).coerceAtMost(with(density) { 155.dp.toPx() })
        val ryPx = (heightPx * 0.30f).coerceAtMost(with(density) { 82.dp.toPx() })

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.24f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = 52.dp.toPx()
                ),
                radius = 52.dp.toPx(),
                center = center
            )

            rotate(degrees = orbitTiltDeg, pivot = center) {
                // المدار الخارجي المتوهّج متعدد الطبقات (ذهب مصقول + هالة نورانية)
                drawGlowingGoldOval(
                    topLeft = Offset(center.x - rxPx, center.y - ryPx),
                    size = Size(rxPx * 2f, ryPx * 2f),
                    coreStrokePx = 3.2.dp.toPx(),
                    alphaScale = 1.0f
                )
                // حلقة مدارية وسطى دقيقة بلمعة ذهبية
                drawGlowingGoldOval(
                    topLeft = Offset(center.x - rxPx * 0.82f, center.y - ryPx * 0.82f),
                    size = Size(rxPx * 1.64f, ryPx * 1.64f),
                    coreStrokePx = 1.0.dp.toPx(),
                    alphaScale = 0.45f
                )
                // حلقة داخلية خافتة تبرز الكعبة في المركز
                drawOval(
                    color = SakinaLuxuryPalette.GoldBright.copy(alpha = 0.24f),
                    topLeft = Offset(center.x - rxPx * 0.60f, center.y - ryPx * 0.60f),
                    size = Size(rxPx * 1.20f, ryPx * 1.20f),
                    style = Stroke(width = 0.75.dp.toPx())
                )
            }

            // هالة نورانية خلف مجسم الكعبة ثلاثي الأبعاد
            drawLuxuryKaabaEmblem(
                center = center,
                sizePx = 38.dp.toPx(),
                withLightPillar = false
            )
        }

        // مجسّم الكعبة ثلاثي الأبعاد الحقيقي بالكسوة والحزام الذهبي المتوهج
        Image(
            painter = painterResource(id = R.drawable.ic_kaaba_3d),
            contentDescription = null,
            modifier = Modifier.size(72.dp)
        )

        prayers.forEachIndexed { index, node ->
            val angle = animatedRotation.toDouble() + index * (2.0 * PI / count)
            val rawX = cos(angle) * rxPx
            val rawY = sin(angle) * ryPx

            val rotatedX = rawX * cosTilt - rawY * sinTilt
            val rotatedY = rawX * sinTilt + rawY * cosTilt

            val isFocused = index == focusedIndex
            val isNext = index == nextPrayerIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .absoluteOffset {
                        IntOffset(
                            x = rotatedX.roundToInt(),
                            y = (rotatedY + dotCenterOffsetPx).roundToInt()
                        )
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectPrayer(index) },
                        onLongClick = { onQuickToggleCompleted(node.type) }
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .semantics {
                        contentDescription = "${node.localizedName} ${node.timeText}"
                    }
            ) {
                Box(
                    modifier = Modifier.size(if (isFocused) 26.dp else 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val orbColor = when {
                            isFocused -> SakinaLuxuryPalette.GoldSpecular
                            node.isCompleted -> SakinaLuxuryPalette.EmeraldSpecular
                            else -> SakinaLuxuryPalette.GoldBright
                        }
                        val haloColor = when {
                            isFocused -> SakinaLuxuryPalette.GoldBright
                            node.isCompleted -> SakinaLuxuryPalette.EmeraldGlow
                            else -> SakinaLuxuryPalette.GoldRich
                        }
                        drawGlowingLuxuryOrb(
                            center = Offset(size.width / 2f, size.height / 2f),
                            coreRadiusPx = (if (isFocused) 5.5f * pulseScale else 3.8f).dp.toPx(),
                            coreColor = orbColor,
                            haloColor = haloColor,
                            haloScale = if (isFocused) 2.8f else 2.2f,
                            intensity = if (isFocused || isNext) 1.0f else 0.78f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (node.isCompleted) "${node.localizedName} ✓" else node.localizedName,
                    color = if (isFocused) SakinaOrbitalColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream,
                    fontSize = if (isFocused) 13.sp else 11.5.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = bodyFontFamily
                )
                Text(
                    text = node.timeText,
                    color = if (isFocused) SakinaOrbitalColors.WarmCream else SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.88f),
                    fontSize = 11.5.sp,
                    fontFamily = bodyFontFamily,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }
    }
}

@Composable
private fun FocalSpotlightCard(
    prayer: OrbitalPrayerNode,
    isNextPrayer: Boolean,
    countdownText: String,
    completedCount: Int,
    totalPrayers: Int,
    strings: SakinaHomeStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onTodayStatsClick: () -> Unit,
    onToggleCompleted: () -> Unit,
    onCycleAlarmMode: () -> Unit
) {
    val badgeBg by animateColorAsState(
        targetValue = if (prayer.isCompleted) {
            SakinaOrbitalColors.DeepEmerald
        } else {
            SakinaOrbitalColors.WarmCream
        },
        label = "checkBg"
    )

    val alarmLabel = when (prayer.alarmMode) {
        PrayerAlarmMode.ADHAN -> strings.alarmAdhanLabel
        PrayerAlarmMode.NOTIFICATION -> strings.alarmNotifLabel
        PrayerAlarmMode.SILENT -> strings.alarmSilentLabel
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth(0.86f)
                .padding(bottom = 20.dp)
                .sakinaParchmentGlowCard(
                    cornerRadius = 24.dp,
                    haloAlpha = 0.36f,
                    doubleBorder = true
                )
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp, bottom = 26.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$completedCount/$totalPrayers ${strings.todaySuffix}",
                        color = SakinaOrbitalColors.DeepEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        maxLines = 1,
                        softWrap = false,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SakinaOrbitalColors.DeepEmerald.copy(alpha = 0.10f))
                            .clickable(onClick = onTodayStatsClick)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (prayer.alarmMode) {
                                    PrayerAlarmMode.ADHAN -> SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.18f)
                                    PrayerAlarmMode.NOTIFICATION -> SakinaOrbitalColors.DeepEmerald.copy(alpha = 0.10f)
                                    PrayerAlarmMode.SILENT -> Color.Gray.copy(alpha = 0.12f)
                                }
                            )
                            .clickable(onClick = onCycleAlarmMode)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .semantics {
                                contentDescription = "${strings.toggleAlarmDesc} ${prayer.localizedName}"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = when (prayer.alarmMode) {
                                PrayerAlarmMode.ADHAN -> SakinaOrbitalColors.MutedGoldText
                                PrayerAlarmMode.NOTIFICATION -> SakinaOrbitalColors.DeepEmerald
                                PrayerAlarmMode.SILENT -> Color.Gray
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.size(3.dp))
                        Text(
                            text = alarmLabel,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = bodyFontFamily,
                            maxLines = 1,
                            softWrap = false,
                            color = when (prayer.alarmMode) {
                                PrayerAlarmMode.ADHAN -> SakinaOrbitalColors.MutedGoldText
                                PrayerAlarmMode.NOTIFICATION -> SakinaOrbitalColors.DeepEmerald
                                PrayerAlarmMode.SILENT -> Color.Gray
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${prayer.localizedName} · ${prayer.timeText}",
                    color = SakinaOrbitalColors.MutedGoldText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily,
                    maxLines = 1,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isNextPrayer) countdownText else prayer.timeText,
                    color = SakinaOrbitalColors.InkNavy,
                    fontSize = 33.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = bodyFontFamily,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isNextPrayer) {
                        "${strings.remainingPrefix} ${prayer.localizedName}"
                    } else {
                        "${strings.prayerTimePrefix} ${prayer.localizedName}"
                    },
                    color = SakinaOrbitalColors.InkNavy.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    fontFamily = bodyFontFamily
                )
            }
        }

        Surface(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onToggleCompleted)
                .semantics { contentDescription = "${strings.toggleCompleteDesc} ${prayer.localizedName}" },
            shape = CircleShape,
            color = badgeBg,
            border = BorderStroke(2.dp, SakinaOrbitalColors.ChampagneGold),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = if (prayer.isCompleted) {
                        SakinaOrbitalColors.SoftGoldGlow
                    } else {
                        SakinaOrbitalColors.InkNavy
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ComprehensiveNafilahStrip(
    state: SakinaOrbitalHomeState,
    strings: SakinaHomeStrings,
    bodyFontFamily: FontFamily
) {
    val items = listOf(
        strings.imsakLabel to state.imsakTime,
        strings.sunriseLabel to state.sunriseTime,
        strings.duhaLabel to state.duhaTime,
        strings.midnightLabel to state.midnightTime,
        strings.tahajjudLabel to state.tahajjudTime
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        itemsIndexed(items) { _, (label, time) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.65f),
                    fontSize = 10.sp,
                    fontFamily = bodyFontFamily
                )
                Text(
                    text = time,
                    color = SakinaOrbitalColors.SoftGoldGlow,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = bodyFontFamily,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }
    }
}

@Composable
fun LiquidDynamicDock(
    selectedIndex: Int,
    strings: SakinaHomeStrings,
    bodyFontFamily: FontFamily,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dockTabs = listOf(
        SakinaDockTab("0", strings.tabHome, R.drawable.ic_sakina_home),
        SakinaDockTab("1", strings.tabQibla, R.drawable.ic_sakina_qibla),
        SakinaDockTab("2", strings.tabQuran, R.drawable.ic_sakina_quran),
        SakinaDockTab("3", strings.tabCalendar, R.drawable.ic_sakina_calendar)
    )
    SakinaLuxuryFloatingDock(
        tabs = dockTabs,
        selectedRoute = selectedIndex.coerceIn(0, 3).toString(),
        onTabSelected = { route -> onTabSelected(route.toIntOrNull() ?: 0) },
        modifier = modifier
    )
    return
    @Suppress("UNREACHABLE_CODE")
    val labels = listOf(strings.tabHome, strings.tabQibla, strings.tabQuran, strings.tabCalendar)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .sakinaLuxuryGlassCard(
                cornerRadius = 32.dp,
                innerHairline = false,
                emeraldFlare = false,
                shimmer = true,
                borderAlpha = 0.78f
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                val tint = if (isSelected) {
                    SakinaOrbitalColors.SoftGoldGlow
                } else {
                    SakinaOrbitalColors.WarmCream.copy(alpha = 0.65f)
                }

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (isSelected) {
                                SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.18f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (index == 0) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = label,
                            tint = tint,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            when (index) {
                                1 -> drawCompassVector(tint)
                                2 -> drawBookVector(tint)
                                else -> drawCalendarVector(tint)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        color = tint,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = bodyFontFamily,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ============================================================================
// 5. أيقونات متجهة خفيفة مدمجة (بدون material-icons-extended)
// ============================================================================
private fun DrawScope.drawCompassVector(tint: Color) {
    val stroke = 1.6.dp.toPx()
    drawCircle(color = tint, radius = size.minDimension * 0.42f, style = Stroke(stroke))
    val path = Path().apply {
        moveTo(size.width * 0.5f, size.height * 0.22f)
        lineTo(size.width * 0.62f, size.height * 0.54f)
        lineTo(size.width * 0.5f, size.height * 0.78f)
        lineTo(size.width * 0.38f, size.height * 0.46f)
        close()
    }
    drawPath(path = path, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

private fun DrawScope.drawBookVector(tint: Color) {
    val stroke = 1.6.dp.toPx()
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.28f)
        lineTo(w * 0.15f, h * 0.22f)
        lineTo(w * 0.15f, h * 0.78f)
        lineTo(w * 0.5f, h * 0.84f)
        lineTo(w * 0.85f, h * 0.78f)
        lineTo(w * 0.85f, h * 0.22f)
        close()
        moveTo(w * 0.5f, h * 0.28f)
        lineTo(w * 0.5f, h * 0.84f)
    }
    drawPath(path = path, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

private fun DrawScope.drawCalendarVector(tint: Color) {
    val stroke = 1.6.dp.toPx()
    val w = size.width
    val h = size.height
    drawRoundRect(
        color = tint,
        topLeft = Offset(w * 0.14f, h * 0.22f),
        size = Size(w * 0.72f, h * 0.64f),
        cornerRadius = CornerRadius(3.dp.toPx()),
        style = Stroke(stroke)
    )
    drawLine(tint, Offset(w * 0.14f, h * 0.42f), Offset(w * 0.86f, h * 0.42f), strokeWidth = stroke)
    drawLine(tint, Offset(w * 0.34f, h * 0.12f), Offset(w * 0.34f, h * 0.28f), strokeWidth = stroke)
    drawLine(tint, Offset(w * 0.66f, h * 0.12f), Offset(w * 0.66f, h * 0.28f), strokeWidth = stroke)
}
