package app.sakina

import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
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
import androidx.compose.ui.text.PlatformTextStyle
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
import androidx.compose.ui.layout.ContentScale

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
    val appTitle: String = "نور قنوت",
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
@OptIn(ExperimentalFoundationApi::class)
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
    var showFivePrayersSheet by remember { mutableStateOf(false) }

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
        var prayersExpanded by remember { mutableStateOf(false) }
        var lastInteractionTick by remember { mutableIntStateOf(0) }

        // العودة الآلية لقائمة الصلوات الخمس بعد 8 ثوانٍ من عدم الاستعمال أو عند مغادرة الشاشة
        LaunchedEffect(prayersExpanded, lastInteractionTick) {
            if (prayersExpanded) {
                delay(25_000L)
                prayersExpanded = false
            }
        }
        DisposableEffect(Unit) {
            onDispose { prayersExpanded = false }
        }

        val timeDhikr = remember {
            val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            when (h) {
                in 4..6 -> "أذكار الفجر" to "«رَكْعَتَا الْفَجْرِ خَيْرٌ مِنَ الدُّنْيَا وَمَا فِيهَا»"
                in 7..11 -> "ذكر الضحى" to "«يُصْبِحُ عَلَى كُلِّ سُلامَى مِنْ أَحَدِكُمْ صَدَقَةٌ، وَيُجْزِئُ مِنْ ذَلِكَ رَكْعَتَانِ يَرْكَعُهُمَا مِنَ الضُّحَى»"
                in 12..14 -> "ذكر الظهر" to "«أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ» — الرعد: 28"
                in 15..17 -> "ذكر العصر" to "«مَنْ صَلَّى الْبَرْدَيْنِ دَخَلَ الْجَنَّةَ» — متفق عليه"
                in 18..19 -> "أذكار المساء" to "«أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ وَالْحَمْدُ لِلَّهِ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ»"
                else -> "سكون الليل" to "«يَنْزِلُ رَبُّنَا تَبَارَكَ وَتَعَالَى كُلَّ لَيْلَةٍ إِلَى السَّمَاءِ الدُّنْيَا حِينَ يَبْقَى ثُلُثُ اللَّيْلِ الْآخِرُ»"
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = if (showBottomDock) 8.dp else 118.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. ترويسة «نور قنوت» + الخط المزخرف + NurQunoot + المدينة والهجري
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onCityClick)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_nurqunoot_gold),
                    contentDescription = "نُورُ قُنُوت",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(40.dp)
                )
                Canvas(
                    modifier = Modifier
                        .width(126.dp)
                        .height(8.dp)
                ) {
                    val cy = size.height / 2f
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFFD5B576), Color(0xFFF2DAA4), Color(0xFFD5B576), Color.Transparent)
                        ),
                        start = Offset(0f, cy),
                        end = Offset(size.width, cy),
                        strokeWidth = 1.2.dp.toPx()
                    )
                    val cx = size.width / 2f
                    val d = 3.dp.toPx()
                    val diamond = Path().apply {
                        moveTo(cx, cy - d)
                        lineTo(cx + d, cy)
                        lineTo(cx, cy + d)
                        lineTo(cx - d, cy)
                        close()
                    }
                    drawPath(diamond, color = Color(0xFFF2DAA4))
                }
                Text(
                    text = "NurQunoot",
                    style = TextStyle(
                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.2.sp,
                        fontFamily = SakinaLatinLuxuryFontFamily,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${state.cityName} · ${state.hijriDate}",
                    color = Color(0xFFD8B87A),
                    fontSize = 11.sp,
                    fontFamily = SakinaHeadingFont,
                    maxLines = 1,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 2. قوس المحراب النوراني المذهّب مدمجاً مباشرة في قاعدة خانة الصلاة القادمة
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(212.dp)
                    .clickable { onTodayStatsClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_home_mihrab_hero_3d),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 64.dp, start = 24.dp, end = 24.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = focusedPrayer.localizedName,
                        style = TextStyle(
                            brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                            shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SakinaHeadingFont,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                    Text(
                        text = focusedPrayer.timeText,
                        style = TextStyle(
                            brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                            shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                            fontSize = 30.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SakinaNumeralFont,
                            fontFeatureSettings = "tnum",
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = if (focusedIndex == state.nextPrayerIndex) "متبقي" else "اليوم",
                            style = TextStyle(
                                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaHeadingFont,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                        Text(
                            text = if (focusedIndex == state.nextPrayerIndex) state.countdownText
                            else "${completedCount}/${state.prayers.size.coerceAtLeast(5)}",
                            style = TextStyle(
                                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaNumeralFont,
                                fontFeatureSettings = "tnum",
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            // 3. خانة الصلاة القادمة المتلاحمة مع القوس + التوسع السلس (650ms) للصلوات الخمس مع طيّ آلي بعد 25 ثانية
            val chevronRotation by animateFloatAsState(
                targetValue = if (prayersExpanded) 180f else 0f,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
                label = "chevronRot"
            )
            val activeIdx = state.nextPrayerIndex.coerceIn(0, (state.prayers.lastIndex).coerceAtLeast(0))
            val activeNode = state.prayers.getOrNull(activeIdx)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-8).dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // الحالة المطويّة: خانة الصلاة القادمة المزخرفة مع السهم الذهبي الفاخر
                AnimatedVisibility(
                    visible = !prayersExpanded && activeNode != null,
                    enter = expandVertically(animationSpec = tween(950, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))) + fadeIn(tween(450)),
                    exit = shrinkVertically(animationSpec = tween(850, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))) + fadeOut(tween(350))
                ) {
                    if (activeNode != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .combinedClickable(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        lastInteractionTick++
                                        prayersExpanded = true
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onCyclePrayerAlarmMode(activeNode.type)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.bg_prayer_row_active_emerald_3d),
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.matchParentSize()
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 44.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = activeNode.localizedName,
                                        style = TextStyle(
                                            brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                            shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                                            fontSize = 17.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SakinaHeadingFont,
                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                        )
                                    )
                                    SakinaLuxuryGoldChevron(rotationDeg = chevronRotation)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = activeNode.timeText,
                                        style = TextStyle(
                                            brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                            shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                                            fontSize = 16.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SakinaNumeralFont,
                                            fontFeatureSettings = "tnum",
                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                        )
                                    )
                                    if (activeNode.isCompleted) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_3d_check_gold),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    onTogglePrayerCompleted(activeNode.type)
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // الحالة الموسّعة: جميع الصلوات الخمس المزخرفة (22194.jpg) مع طيّ سلس وهادئ
                AnimatedVisibility(
                    visible = prayersExpanded,
                    enter = expandVertically(animationSpec = tween(950, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))) + fadeIn(tween(750)),
                    exit = shrinkVertically(animationSpec = tween(950, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))) + fadeOut(tween(650))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        state.prayers.forEachIndexed { idx, node ->
                            val isHighlighted = idx == state.nextPrayerIndex
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .combinedClickable(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            lastInteractionTick++
                                            if (focusedIndex == idx) onTogglePrayerCompleted(node.type)
                                            else focusedIndex = idx
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            lastInteractionTick++
                                            onCyclePrayerAlarmMode(node.type)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = if (isHighlighted) R.drawable.bg_prayer_row_active_emerald_3d
                                        else R.drawable.bg_prayer_row_gold_3d
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.matchParentSize()
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 44.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Text(
                                            text = node.localizedName,
                                            style = TextStyle(
                                                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                                shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SakinaHeadingFont,
                                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                                            )
                                        )
                                        if (isHighlighted) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        prayersExpanded = false
                                                    }
                                                    .padding(4.dp)
                                            ) {
                                                SakinaLuxuryGoldChevron(rotationDeg = chevronRotation)
                                            }
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = node.timeText,
                                            style = TextStyle(
                                                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                                shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SakinaNumeralFont,
                                                fontFeatureSettings = "tnum",
                                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                                            )
                                        )
                                        if (node.isCompleted) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_3d_check_gold),
                                                contentDescription = null,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        lastInteractionTick++
                                                        onTogglePrayerCompleted(node.type)
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. شريط النوافل + بطاقة «ذكر الوقت» بنفس خامات وزخارف خانات الصلوات والشريط السفلي 100%
            AnimatedVisibility(
                visible = !prayersExpanded,
                enter = expandVertically(animationSpec = tween(950, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))) + fadeIn(tween(750)),
                exit = shrinkVertically(animationSpec = tween(850, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))) + fadeOut(tween(350))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // شريط النوافل بارتفاع مريح 66dp ودرجة زمرد موحّدة مع الخانات والشريط السفلي
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bg_prayer_row_gold_3d),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 42.dp, vertical = 5.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0x660B4236), Color(0xAA146854), Color(0x660B4236))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            ComprehensiveNafilahStrip(
                                state = state,
                                strings = strings,
                                bodyFontFamily = SakinaHeadingFont
                            )
                        }
                    }

                    // بطاقة «ذكر الوقت» داخل نفس الزجاج الزمردي ذي الأركان الذهبية واللمعة المائلة (bg_home_prayer_capsule_3d)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bg_home_prayer_capsule_3d),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize()
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✦ ${timeDhikr.first} ✦",
                                style = TextStyle(
                                    brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                    shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SakinaHeadingFont
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = timeDhikr.second,
                                style = TextStyle(
                                    brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                    shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                                    fontSize = 13.5.sp,
                                    lineHeight = 22.sp,
                                    fontFamily = SakinaHeadingFont,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (showBottomDock) {
                LiquidDynamicDock(
                    selectedIndex = selectedTabIndex,
                    strings = strings,
                    bodyFontFamily = SakinaHeadingFont,
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
private fun SakinaLuxuryGoldChevron(rotationDeg: Float) {
    Canvas(
        modifier = Modifier
            .size(18.dp)
            .graphicsLayer { rotationZ = rotationDeg }
    ) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.16f, h * 0.34f)
            lineTo(w * 0.50f, h * 0.68f)
            lineTo(w * 0.84f, h * 0.34f)
        }
        drawPath(
            path = path,
            color = Color(0x88040C16),
            style = Stroke(width = 3.6.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFF2DAA4), Color(0xFFD5B576), Color(0xFFAE8B4D))
            ),
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun MinimalistOrbitalHeader(
    appTitle: String,
    cityName: String,
    hijriDate: String,
    langCode: String,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onMenuClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onLocationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // زر القائمة الموحّدة الوحيد
        Box(
            modifier = Modifier
                .size(38.dp)
                .sakinaLuxuryGlassCard(cornerRadius = 19.dp, borderAlpha = 0.75f)
                .clip(CircleShape)
                .clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = SakinaLuxuryColors.SoftGoldGlow,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onLocationClick)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = appTitle,
                style = TextStyle(
                    brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily
                )
            )
            Text(
                text = "$cityName · $hijriDate",
                color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.90f),
                fontSize = 11.sp,
                fontFamily = bodyFontFamily,
                maxLines = 1,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
        }

        // زر تبديل اللغة وحده في الجهة المقابلة
        Box(
            modifier = Modifier
                .size(38.dp)
                .sakinaLuxuryGlassCard(cornerRadius = 19.dp, borderAlpha = 0.75f)
                .clip(CircleShape)
                .clickable(onClick = onLanguageClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = langCode.uppercase(),
                color = SakinaLuxuryColors.SoftGoldGlow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = bodyFontFamily
            )
        }
    }
}

@Composable
private fun SevenDaySelectorStrip(
    days: List<DayChipItem>,
    bodyFontFamily: FontFamily,
    onDaySelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val visibleDays = days.take(7)
        for (index in visibleDays.indices) {
            val day = visibleDays[index]
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = if (day.isSelected) {
                            SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.94f)
                        } else {
                            SakinaOrbitalColors.GlassNavy.copy(alpha = 0.58f)
                        }
                    )
                    .border(
                        width = if (day.isSelected) 1.1.dp else 0.7.dp,
                        color = if (day.isSelected) {
                            SakinaLuxuryColors.SoftGoldGlow
                        } else {
                            SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.25f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onDaySelected(index) }
                    .padding(horizontal = 2.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = day.dayLabel,
                    color = if (day.isSelected) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.75f),
                    fontSize = 8.5.sp,
                    maxLines = 1,
                    softWrap = false,
                    fontFamily = bodyFontFamily
                )
                Text(
                    text = day.dayNumber,
                    color = if (day.isSelected) SakinaLuxuryColors.WarmCream else SakinaOrbitalColors.SoftGoldGlow,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
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
        val rxPx = (widthPx * 0.38f).coerceAtMost(with(density) { 152.dp.toPx() })
        val ryPx = (heightPx * 0.25f).coerceAtMost(with(density) { 70.dp.toPx() })

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

        // v54-polish: قرص الهلال الذهبي متوازن في المركز (148dp) حتى لا يتداخل مع أسماء الصلوات الخمس
        Image(
            painter = painterResource(id = R.drawable.bg_home_crescent_dial_3d),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(148.dp)
        )

        // مجسّم الكعبة ثلاثي الأبعاد الحقيقي بالكسوة والحزام الذهبي المتوهج
        Image(
            painter = painterResource(id = R.drawable.ic_kaaba_3d),
            contentDescription = null,
            modifier = Modifier.size(60.dp)
        )

        for (index in prayers.indices) {
            val node = prayers[index]
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
    prayers: List<OrbitalPrayerNode>,
    nextPrayerIndex: Int,
    isNextPrayer: Boolean,
    isExpanded: Boolean,
    countdownText: String,
    completedCount: Int,
    totalPrayers: Int,
    strings: SakinaHomeStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onTodayStatsClick: () -> Unit,
    onExpandFivePrayers: () -> Unit = {},
    onSelectPrayerFromList: (Int) -> Unit = {},
    onCyclePrayerAlarmModeByType: (PrayerType) -> Unit = {},
    onToggleCompleted: () -> Unit,
    onCycleAlarmMode: () -> Unit
) {
    val alarmLabel = when (prayer.alarmMode) {
        PrayerAlarmMode.ADHAN -> strings.alarmAdhanLabel
        PrayerAlarmMode.NOTIFICATION -> strings.alarmNotifLabel
        PrayerAlarmMode.SILENT -> strings.alarmSilentLabel
    }

    val quadModes = remember(prayers) {
        androidx.compose.runtime.mutableStateMapOf<PrayerType, Int>().apply {
            for (p in prayers) {
                this[p.type] = when (p.alarmMode) {
                    PrayerAlarmMode.ADHAN -> 0
                    PrayerAlarmMode.NOTIFICATION -> 1
                    PrayerAlarmMode.SILENT -> 3
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 368.dp)
                .fillMaxWidth(0.93f)
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onExpandFivePrayers)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_glass_gold_card),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp, start = 22.dp, end = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // v52 #1: ثلاثة أزرار كبسولية فاخرة مذهّبة الحواف داخل الإطار الآمن بعيداً عن الزخارف
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.todayProgressFormat.format(completedCount, totalPrayers),
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x99061726))
                            .border(0.85.dp, SakinaLuxuryPalette.MetallicGoldButtonBrush, RoundedCornerShape(50))
                            .clickable(onClick = onTodayStatsClick)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    )

                    Text(
                        text = if (isExpanded) "الصلوات الخمس ▴" else "الصلوات الخمس ▾",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.85f))
                            .border(1.0.dp, SakinaLuxuryPalette.MetallicGoldButtonBrush, RoundedCornerShape(50))
                            .clickable(onClick = onExpandFivePrayers)
                            .padding(horizontal = 11.dp, vertical = 4.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x99061726))
                            .border(0.85.dp, SakinaLuxuryPalette.MetallicGoldButtonBrush, RoundedCornerShape(50))
                            .clickable(onClick = onCycleAlarmMode)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = SakinaLuxuryColors.SoftGoldGlow,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.size(3.dp))
                        Text(
                            text = alarmLabel,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = bodyFontFamily,
                            color = SakinaLuxuryColors.SoftGoldGlow
                        )
                    }
                }

                // خط فاصل ذهبي رفيع جداً
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(vertical = 5.dp)
                        .height(0.7.dp)
                        .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                )

                // عرض مضغوط وأنيق لاسم الصلاة والعداد في ارتفاع أصغر
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${prayer.localizedName} · ${prayer.timeText}",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = headingFontFamily,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = if (isNextPrayer) countdownText else prayer.timeText,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // زر تسجيل أداء الصلاة بوضوح تام للمستخدم (سجّل: صلّيت ✓ / تمّت الصلاة ✓)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (prayer.isCompleted) SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.92f)
                            else Color(0x6606101F)
                        )
                        .border(
                            width = 1.dp,
                            brush = SakinaLuxuryPalette.MetallicGoldButtonBrush,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable(onClick = onToggleCompleted)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .semantics { contentDescription = "${strings.toggleCompleteDesc} ${prayer.localizedName}" }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = if (prayer.isCompleted) SakinaLuxuryColors.EmeraldGlow else SakinaLuxuryColors.SoftGoldGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text(
                        text = if (prayer.isCompleted) "تمّت صلاة ${prayer.localizedName} ✓" else "سجّل أداء ${prayer.localizedName}",
                        color = if (prayer.isCompleted) SakinaLuxuryColors.EmeraldGlow else SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily
                    )
                }

                // الانبساط المتّصل لنفس البطاقة وكأنها كانت مطويّة وانفتحت (Unfolding 5-Prayer Scroll)
                AnimatedVisibility(visible = isExpanded && prayers.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.8.dp)
                                .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        for (idx in prayers.indices) {
                            val item = prayers[idx]
                            val isNext = idx == nextPrayerIndex
                            val qMode = quadModes[item.type] ?: 0
                            val modeLabel = when (qMode) {
                                0 -> "أذان كامل"
                                1 -> "تكبير"
                                2 -> "اهتزاز"
                                else -> "صامت"
                            }
                            val modeColor = when (qMode) {
                                0 -> SakinaLuxuryColors.SoftGoldGlow
                                1 -> SakinaLuxuryColors.EmeraldGlow
                                2 -> SakinaOrbitalColors.ChampagneGold
                                else -> Color(0xFF9CA3AF)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isNext) 46.dp else 40.dp)
                                    .clickable { onSelectPrayerFromList(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = if (isNext) R.drawable.bg_prayer_row_active_emerald_3d else R.drawable.bg_prayer_row_gold_3d
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.matchParentSize()
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                Text(
                                    text = if (isNext) "✦ ${item.localizedName}" else item.localizedName,
                                    color = if (isNext) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream,
                                    fontSize = if (isNext) 14.sp else 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = headingFontFamily,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = item.timeText,
                                    color = if (isNext) Color.White else SakinaLuxuryColors.SoftGoldGlow,
                                    fontSize = if (isNext) 14.5.sp else 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = bodyFontFamily,
                                    style = TextStyle(fontFeatureSettings = "tnum"),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Text(
                                    text = modeLabel,
                                    color = modeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = bodyFontFamily,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .border(0.7.dp, modeColor.copy(alpha = 0.7f), RoundedCornerShape(50))
                                        .clickable {
                                            quadModes[item.type] = (qMode + 1) % 4
                                            onCyclePrayerAlarmModeByType(item.type)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (label, time) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = TextStyle(
                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                        fontSize = 9.5.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = time,
                    style = TextStyle(
                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                        fontSize = 10.5.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SakinaNumeralFont,
                        fontFeatureSettings = "tnum",
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    maxLines = 1,
                    softWrap = false
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
        SakinaDockTab("0", strings.tabHome, R.drawable.ic_3d_mosque),
        SakinaDockTab("1", strings.tabQibla, R.drawable.ic_3d_qibla_compass),
        SakinaDockTab("2", strings.tabQuran, R.drawable.ic_3d_quran_stand),
        SakinaDockTab("3", strings.tabCalendar, R.drawable.ic_3d_calendar_crescent)
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
            for (index in labels.indices) {
                val label = labels[index]
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

@Composable
private fun FivePrayersExpandedModal(
    prayers: List<OrbitalPrayerNode>,
    nextPrayerIndex: Int,
    countdownText: String,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onCycleAlarmMode: (PrayerType) -> Unit,
    onSelectPrayer: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // حالات التبديل الرباعية لكل صلاة (أذان كامل · تكبير · اهتزاز · صامت)
    val quadModes = remember(prayers) {
        androidx.compose.runtime.mutableStateMapOf<PrayerType, Int>().apply {
            for (p in prayers) {
                this[p.type] = when (p.alarmMode) {
                    PrayerAlarmMode.ADHAN -> 0
                    PrayerAlarmMode.NOTIFICATION -> 1
                    PrayerAlarmMode.SILENT -> 3
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC030A14))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 400.dp)
                .sakinaLuxuryGlassCard(cornerRadius = 24.dp, borderAlpha = 0.85f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مواقيت الصلوات الخمس",
                    style = TextStyle(
                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = headingFontFamily
                    )
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.75f))
                        .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.75f), CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            for (idx in prayers.indices) {
                val item = prayers[idx]
                val isNext = idx == nextPrayerIndex
                val qMode = quadModes[item.type] ?: 0
                val modeLabel = when (qMode) {
                    0 -> "أذان كامل"
                    1 -> "تكبير"
                    2 -> "اهتزاز"
                    else -> "صامت"
                }
                val modeColor = when (qMode) {
                    0 -> SakinaLuxuryColors.SoftGoldGlow
                    1 -> SakinaLuxuryColors.EmeraldGlow
                    2 -> SakinaOrbitalColors.ChampagneGold
                    else -> Color(0xFF9CA3AF)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(if (isNext) 18.dp else 14.dp))
                        .background(
                            if (isNext) {
                                Brush.horizontalGradient(
                                    listOf(
                                        SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.96f),
                                        Color(0xFF145340),
                                        SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.96f)
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        SakinaOrbitalColors.GlassNavy.copy(alpha = 0.82f),
                                        SakinaOrbitalColors.GlassNavy.copy(alpha = 0.68f)
                                    )
                                )
                            }
                        )
                        .border(
                            width = if (isNext) 1.5.dp else 0.9.dp,
                            brush = SakinaLuxuryPalette.MetallicGoldButtonBrush,
                            shape = RoundedCornerShape(if (isNext) 18.dp else 14.dp)
                        )
                        .clickable { onSelectPrayer(idx) }
                        .padding(
                            horizontal = if (isNext) 14.dp else 12.dp,
                            vertical = if (isNext) 13.dp else 9.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isNext) "✦ ${item.localizedName} (القادمة)" else item.localizedName,
                            color = if (isNext) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream,
                            fontSize = if (isNext) 15.5.sp else 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = headingFontFamily
                        )
                        if (isNext) {
                            Text(
                                text = "المتبقي: $countdownText",
                                color = SakinaLuxuryColors.EmeraldGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = bodyFontFamily,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                    }

                    Text(
                        text = item.timeText,
                        color = if (isNext) Color.White else SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = if (isNext) 17.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x6606101F))
                            .border(0.8.dp, modeColor.copy(alpha = 0.72f), RoundedCornerShape(50))
                            .clickable {
                                val nextM = (qMode + 1) % 4
                                quadModes[item.type] = nextM
                                onCycleAlarmMode(item.type)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = modeLabel,
                            color = modeColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = bodyFontFamily,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
