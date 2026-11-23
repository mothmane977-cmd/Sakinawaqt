package app.sakina

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlin.math.sin

// ============================================================================
// 1. نماذج بيانات واحة الذكر والتسبيح والعبادات
// ============================================================================
enum class WorshipPortalType {
    MORNING_ADHKAR,
    EVENING_ADHKAR,
    SLEEP_AND_POST_PRAYER,
    RUQYAH,
    NAMES_OF_ALLAH,
    DUAS_AND_TRACKER
}

data class WorshipPortalCard(
    val type: WorshipPortalType,
    val title: String,
    val subtitle: String,
    val progressFraction: Float = 0f,
    val hasOfflineAudio: Boolean = false
)

data class DhikrCardItem(
    val id: Int,
    val text: String,
    val reference: String,
    val targetCount: Int = 1,
    val currentCount: Int = 0
)

data class TasbihState(
    val dhikrText: String = "سُبْحَانَ اللَّهِ",
    val count: Int = 0,
    val target: Int = 33,
    val round: Int = 1
)

data class SakinaAdhkarStrings(
    val oasisTitle: String = "واحة الذكر",
    val backDesc: String = "رجوع",
    val searchDesc: String = "بحث في الأذكار",
    val roundFormat: String = "الجولة %d",
    val tasbihTapHint: String = "المس الحلقة للتسبيح",
    val offlineAudioLabel: String = "استماع أوفلاين · أذكار",
    val reciterLabel: String = "بصوت الشيخ مشاري العفاسي",
    val resetDesc: String = "تصفير العداد"
)

data class SakinaAdhkarOasisState(
    val tasbih: TasbihState = TasbihState(),
    val portals: List<WorshipPortalCard> = listOf(
        WorshipPortalCard(WorshipPortalType.MORNING_ADHKAR, "أذكار الصباح", "أوفلاين · صوت", 0.75f, true),
        WorshipPortalCard(WorshipPortalType.EVENING_ADHKAR, "أذكار المساء", "أوفلاين · صوت", 0.40f, true),
        WorshipPortalCard(WorshipPortalType.SLEEP_AND_POST_PRAYER, "النوم وبعد الصلاة", "أوفلاين · صوت", 0.90f, true),
        WorshipPortalCard(WorshipPortalType.RUQYAH, "الرقية الشرعية", "4 باقات حصن", 0.25f, false),
        WorshipPortalCard(WorshipPortalType.NAMES_OF_ALLAH, "أسماء الله الحسنى", "99 اسماً وشرحاً", 0.10f, false),
        WorshipPortalCard(WorshipPortalType.DUAS_AND_TRACKER, "الأدعية والعبادات", "إنجازك 0/12", 0.0f, false)
    ),
    val activeCategoryTitle: String? = null,
    val activeCategoryHasAudio: Boolean = false,
    val isAudioPlaying: Boolean = false,
    val activeDhikrItems: List<DhikrCardItem> = emptyList()
)

// ============================================================================
// 2. شاشة واحة الذكر والتسبيح (Hub + قارئ الأذكار الصوتي)
// ============================================================================
@Composable
fun SakinaAdhkarOasisScreen(
    state: SakinaAdhkarOasisState,
    strings: SakinaAdhkarStrings = SakinaAdhkarStrings(),
    dockStrings: SakinaHomeStrings = SakinaHomeStrings(),
    headingFontFamily: FontFamily = FontFamily.Default,
    bodyFontFamily: FontFamily = FontFamily.Default,
    selectedTabIndex: Int = 0,
    showBottomDock: Boolean = false,
    hostBottomBarReserve: Dp = 82.dp,
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onTasbihTap: () -> Unit = {},
    onTasbihReset: () -> Unit = {},
    onPrevDhikr: () -> Unit = {},
    onNextDhikr: () -> Unit = {},
    onPortalClick: (WorshipPortalType) -> Unit = {},
    onCloseCategory: () -> Unit = {},
    onToggleAudioPlay: () -> Unit = {},
    onDhikrCardTap: (Int) -> Unit = {},
    onTabSelected: (Int) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val isCategoryOpen = state.activeCategoryTitle != null

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
                .padding(bottom = if (!showBottomDock && !isCategoryOpen) hostBottomBarReserve else 0.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // 1. ترويسة واحدة نظيفة (زر رجوع واحد فقط — إلغاء التكرار المزدوج)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { if (isCategoryOpen) onCloseCategory() else onBackClick() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = strings.backDesc,
                        tint = SakinaOrbitalColors.ChampagneGold
                    )
                }

                Text(
                    text = state.activeCategoryTitle ?: strings.oasisTitle,
                    color = SakinaOrbitalColors.WarmCream,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily
                )

                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = strings.searchDesc,
                        tint = SakinaOrbitalColors.ChampagneGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedContent(
                targetState = isCategoryOpen,
                modifier = Modifier.weight(1f),
                label = "oasisView"
            ) { inCategory ->
                if (!inCategory) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 2. طوق التسبيح المداري المضيء
                        LuminousOrbitalTasbihRing(
                            tasbih = state.tasbih,
                            strings = strings,
                            headingFontFamily = headingFontFamily,
                            bodyFontFamily = bodyFontFamily,
                            onTap = {
                                val nextCount = state.tasbih.count + 1
                                if (nextCount >= state.tasbih.target) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onTasbihTap()
                            },
                            onReset = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTasbihReset()
                            },
                            onPrev = onPrevDhikr,
                            onNext = onNextDhikr
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. شبكة البوابات الست (Bento 2-Column Grid)
                        BentoWorshipPortalGrid(
                            portals = state.portals,
                            headingFontFamily = headingFontFamily,
                            bodyFontFamily = bodyFontFamily,
                            onPortalClick = { type ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPortalClick(type)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    DhikrCategoryReaderContent(
                        hasAudio = state.activeCategoryHasAudio,
                        isPlaying = state.isAudioPlaying,
                        items = state.activeDhikrItems,
                        strings = strings,
                        headingFontFamily = headingFontFamily,
                        bodyFontFamily = bodyFontFamily,
                        onToggleAudioPlay = onToggleAudioPlay,
                        onDhikrCardTap = { id ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDhikrCardTap(id)
                        }
                    )
                }
            }

            if (showBottomDock && !isCategoryOpen) {
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
// 3. طوق التسبيح المداري المضيء (33 خرزة ضوئية)
// ============================================================================
@Composable
private fun LuminousOrbitalTasbihRing(
    tasbih: TasbihState,
    strings: SakinaAdhkarStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onTap: () -> Unit,
    onReset: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val fraction = (tasbih.count.toFloat() / tasbih.target.coerceAtLeast(1)).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tasbihProgress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(214.dp)
                .clip(CircleShape)
                .clickable(onClick = onTap),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerR = size.minDimension * 0.44f
                val innerR = size.minDimension * 0.36f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SakinaOrbitalColors.DeepEmerald.copy(alpha = 0.5f), Color.Transparent),
                        center = center,
                        radius = outerR * 1.15f
                    ),
                    radius = outerR * 1.15f,
                    center = center
                )

                drawGlowingGoldOval(
                    topLeft = Offset(center.x - outerR, center.y - outerR),
                    size = Size(outerR * 2f, outerR * 2f),
                    coreStrokePx = 2.6.dp.toPx(),
                    alphaScale = 0.72f
                )

                drawArc(
                    color = SakinaLuxuryPalette.GoldBright.copy(alpha = 0.32f),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedFraction,
                    useCenter = false,
                    topLeft = Offset(center.x - outerR, center.y - outerR),
                    size = Size(outerR * 2f, outerR * 2f),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(SakinaLuxuryPalette.GoldRich, SakinaLuxuryPalette.GoldSpecular, SakinaLuxuryPalette.GoldBright)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedFraction,
                    useCenter = false,
                    topLeft = Offset(center.x - outerR, center.y - outerR),
                    size = Size(outerR * 2f, outerR * 2f),
                    style = Stroke(width = 3.8.dp.toPx(), cap = StrokeCap.Round)
                )

                drawCircle(
                    color = SakinaLuxuryPalette.EmeraldGlow.copy(alpha = 0.22f),
                    radius = innerR,
                    center = center,
                    style = Stroke(width = 5.dp.toPx())
                )
                drawCircle(
                    color = SakinaLuxuryPalette.EmeraldGlow.copy(alpha = 0.78f),
                    radius = innerR,
                    center = center,
                    style = Stroke(width = 1.4.dp.toPx())
                )

                val beads = tasbih.target.coerceIn(12, 33)
                for (i in 0 until beads) {
                    val angle = (-PI / 2.0) + (i.toDouble() / beads) * (2.0 * PI)
                    val bx = center.x + (cos(angle) * outerR).toFloat()
                    val by = center.y + (sin(angle) * outerR).toFloat()
                    val lit = i < tasbih.count
                    if (lit) {
                        drawGlowingLuxuryOrb(
                            center = Offset(bx, by),
                            coreRadiusPx = 3.2.dp.toPx(),
                            coreColor = SakinaLuxuryPalette.GoldSpecular,
                            haloColor = SakinaLuxuryPalette.GoldBright,
                            haloScale = 2.6f,
                            intensity = 0.95f
                        )
                    } else {
                        drawCircle(
                            color = SakinaLuxuryPalette.GoldBright.copy(alpha = 0.34f),
                            radius = 1.9.dp.toPx(),
                            center = Offset(bx, by)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = tasbih.dhikrText,
                    color = SakinaOrbitalColors.WarmCream,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${tasbih.count}",
                    color = SakinaOrbitalColors.SoftGoldGlow,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                Text(
                    text = "/ ${tasbih.target} · ${strings.roundFormat.format(tasbih.round)}",
                    color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontFamily = bodyFontFamily,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // شريط تبديل الذكر وتصفير العداد
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SakinaOrbitalColors.GlassNavy)
                    .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.35f), CircleShape)
                    .clickable(onClick = onPrev),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = SakinaOrbitalColors.SoftGoldGlow, modifier = Modifier.size(18.dp))
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SakinaOrbitalColors.GlassNavy)
                    .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.45f), CircleShape)
                    .clickable(onClick = onReset)
                    .semantics { contentDescription = strings.resetDesc },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = SakinaOrbitalColors.ChampagneGold,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SakinaOrbitalColors.GlassNavy)
                    .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.35f), CircleShape)
                    .clickable(onClick = onNext),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = SakinaOrbitalColors.SoftGoldGlow, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ============================================================================
// 4. شبكة Bento للعبادات والأذكار
// ============================================================================
@Composable
private fun BentoWorshipPortalGrid(
    portals: List<WorshipPortalCard>,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onPortalClick: (WorshipPortalType) -> Unit
) {
    val rows = remember(portals) { portals.chunked(2) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { card ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .sakinaLuxuryGlassCard(
                                cornerRadius = 18.dp,
                                innerHairline = false,
                                emeraldFlare = card.hasOfflineAudio,
                                shimmer = false,
                                borderAlpha = 0.68f
                            )
                            .sakinaCornerFiligree(alpha = 0.56f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onPortalClick(card.type) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = card.title,
                                    color = SakinaOrbitalColors.WarmCream,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = headingFontFamily
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = card.subtitle,
                                    color = if (card.hasOfflineAudio) Color(0xFF34D399) else SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.8f),
                                    fontSize = 10.5.sp,
                                    fontFamily = bodyFontFamily,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier.size(34.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val s = 2.6.dp.toPx()
                                    drawCircle(SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.2f), style = Stroke(s))
                                    drawArc(
                                        color = SakinaOrbitalColors.SoftGoldGlow,
                                        startAngle = -90f,
                                        sweepAngle = 360f * card.progressFraction.coerceIn(0f, 1f),
                                        useCenter = false,
                                        style = Stroke(s, cap = StrokeCap.Round)
                                    )
                                }
                                Text(
                                    text = "${(card.progressFraction * 100).toInt()}%",
                                    color = SakinaOrbitalColors.SoftGoldGlow,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ============================================================================
// 5. شاشة قراءة الأذكار بالورق الكريمي وكبسولة الصوت الأوفلاين
// ============================================================================
@Composable
private fun DhikrCategoryReaderContent(
    hasAudio: Boolean,
    isPlaying: Boolean,
    items: List<DhikrCardItem>,
    strings: SakinaAdhkarStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onToggleAudioPlay: () -> Unit,
    onDhikrCardTap: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (hasAudio) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleAudioPlay),
                shape = RoundedCornerShape(20.dp),
                color = SakinaOrbitalColors.DeepEmerald,
                border = BorderStroke(1.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SakinaOrbitalColors.ChampagneGold),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPlaying) {
                            Canvas(modifier = Modifier.size(14.dp)) {
                                drawPauseIcon(SakinaOrbitalColors.InkNavy)
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = SakinaOrbitalColors.InkNavy,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.offlineAudioLabel,
                            color = SakinaOrbitalColors.SoftGoldGlow,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = bodyFontFamily
                        )
                        Text(
                            text = strings.reciterLabel,
                            color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f),
                            fontSize = 10.5.sp,
                            fontFamily = bodyFontFamily
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(items, key = { _, it -> it.id }) { _, dhikr ->
                val done = dhikr.currentCount >= dhikr.targetCount
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = SakinaOrbitalColors.ChampagneGold)
                        .clickable { onDhikrCardTap(dhikr.id) },
                    shape = RoundedCornerShape(20.dp),
                    color = SakinaOrbitalColors.WarmCream,
                    border = BorderStroke(
                        width = if (done) 1.6.dp else 1.dp,
                        color = if (done) SakinaOrbitalColors.DeepEmerald else SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = dhikr.text,
                            color = SakinaOrbitalColors.InkNavy,
                            fontSize = 17.sp,
                            lineHeight = 30.sp,
                            fontFamily = headingFontFamily
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (done) SakinaOrbitalColors.DeepEmerald else SakinaOrbitalColors.InkNavy)
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "${dhikr.currentCount} / ${dhikr.targetCount}",
                                    color = SakinaOrbitalColors.SoftGoldGlow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                            Text(
                                text = dhikr.reference,
                                color = SakinaOrbitalColors.MutedGoldText,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = bodyFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPauseIcon(tint: Color) {
    val barW = size.width * 0.28f
    drawRoundRect(tint, Offset(size.width * 0.16f, 0f), Size(barW, size.height), CornerRadius(2.dp.toPx()))
    drawRoundRect(tint, Offset(size.width * 0.56f, 0f), Size(barW, size.height), CornerRadius(2.dp.toPx()))
}
