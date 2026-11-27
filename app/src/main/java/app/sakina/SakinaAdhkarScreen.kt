package app.sakina

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ============================================================================
// ركن الأذكار الجامع — نُورُ قُنُوت (v74 — إعادة بناء كاملة من الصفر)
// 4 تبويبات متكاملة: ذِكْرُك الآن · حصن المسلم · المسبحة الذكية · أدعيتي والأسماء الحسنى
// ============================================================================

@Composable
fun SakinaAdhkarScreen(
    ctx: Context,
    initialCategoryIndex: Int? = null,
    onBackClick: () -> Unit = {},
    onOpenPortal: (WorshipPortalType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val tts = remember { SakinaDhikrTtsEngine(ctx) }
    var playingKey by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.release()
        }
    }

    var activeTab by remember { mutableStateOf(AdhkarVaultTab.NOW) }
    var activeSlot by remember {
        mutableStateOf(
            when (initialCategoryIndex) {
                0 -> DailyWirdSlot.MORNING
                1 -> DailyWirdSlot.EVENING
                2 -> DailyWirdSlot.POST_PRAYER
                3 -> DailyWirdSlot.SLEEP
                else -> SakinaAdhkarVaultRepo.detectCurrentSlot(ctx)
            }
        )
    }
    var wirdLevel by remember { mutableStateOf(VaultWirdLevel.COMPLETE) }
    var warmNightMode by remember { mutableStateOf(false) }

    // Sensory full-card reader state
    var sensoryReaderActive by remember { mutableStateOf(false) }
    var sensoryIndex by remember { mutableIntStateOf(0) }
    val dhikrProgress = remember { mutableStateMapOf<String, Int>() }

    // Hisn 10 Axes state
    var selectedHisnAxis by remember { mutableStateOf<VaultHisnAxis?>(null) }
    var showNawawi40 by remember { mutableStateOf(false) }
    var showRuqyahPro by remember { mutableStateOf(false) }

    // Persistent stats & favorites
    var baqiyatTotal by remember { mutableLongStateOf(SakinaAdhkarVaultRepo.getBaqiyatTotal(ctx)) }
    var baqiyatToday by remember { mutableIntStateOf(SakinaAdhkarVaultRepo.getBaqiyatToday(ctx)) }
    var streakDays by remember { mutableStateOf(SakinaAdhkarVaultRepo.getLast14DaysStreak(ctx)) }
    var currentStreak by remember { mutableIntStateOf(SakinaAdhkarVaultRepo.getCurrentStreakCount(ctx)) }
    var favoriteIds by remember { mutableStateOf(SakinaAdhkarVaultRepo.getFavoriteIds(ctx)) }

    fun recordDhikrTick(delta: Int = 1) {
        val (tot, tod) = SakinaAdhkarVaultRepo.incrementBaqiyat(ctx, delta)
        baqiyatTotal = tot
        baqiyatToday = tod
        streakDays = SakinaAdhkarVaultRepo.getLast14DaysStreak(ctx)
        currentStreak = SakinaAdhkarVaultRepo.getCurrentStreakCount(ctx)
    }

    fun speakItem(key: String, text: String) {
        if (playingKey == key) {
            tts.stop()
            playingKey = null
        } else {
            playingKey = key
            tts.toggleSpeak(key.hashCode(), text)
        }
    }

    BackHandler(enabled = sensoryReaderActive || selectedHisnAxis != null || showNawawi40 || showRuqyahPro) {
        when {
            showNawawi40 -> showNawawi40 = false
            showRuqyahPro -> showRuqyahPro = false
            sensoryReaderActive -> {
                tts.stop()
                playingKey = null
                sensoryReaderActive = false
            }
            selectedHisnAxis != null -> selectedHisnAxis = null
        }
    }

    val bgColor = if (warmNightMode) Color(0xFF1A1208) else SakinaLuxuryPalette.ObsidianNavy
    val cardEmerald = if (warmNightMode) Color(0xFF281B0D) else Color(0xE606221E)
    val cardBorder = if (warmNightMode) Color(0x99E5B567) else SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.55f)
    val warmCream = if (warmNightMode) Color(0xFFFFE9C7) else SakinaOrbitalColors.WarmCream

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (!warmNightMode) {
            SakinaAmbientLuxuryBackdrop(showGeometricPattern = true, showStarDust = true)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = if (sensoryReaderActive) 12.dp else 118.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. الترويسة الفاخرة الموحّدة (زر رجوع ذهبي ثلاثي الأبعاد + العنوان + زر الوضع الليلي الدافئ)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SakinaTactileGoldCoinButton(
                    onClick = {
                        when {
                            sensoryReaderActive -> {
                                tts.stop()
                                playingKey = null
                                sensoryReaderActive = false
                            }
                            selectedHisnAxis != null -> selectedHisnAxis = null
                            else -> onBackClick()
                        }
                    },
                    size = 40.dp,
                    drawableRes = R.drawable.ic_quran_back_gold_3d,
                    contentDescription = "رجوع"
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            sensoryReaderActive -> activeSlot.titleAr
                            selectedHisnAxis != null -> selectedHisnAxis?.titleAr ?: "حصن المسلم"
                            else -> "ركن الأذكار الجامع"
                        },
                        style = TextStyle(
                            brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                            shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SakinaHeadingFont
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (warmNightMode) "الوضع الليلي الدافئ للسكينة"
                        else "خزينة اليوم: $baqiyatToday ذكر · السلسلة $currentStreak يوم",
                        color = warmCream.copy(alpha = 0.74f),
                        fontSize = 10.5.sp,
                        fontFamily = SakinaBodyFont,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }

                // زر التبديل للوضع الليلي الدافئ للنوم
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (warmNightMode) Color(0xFF472F13) else Color(0xCC06221E))
                        .border(1.dp, cardBorder, CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            warmNightMode = !warmNightMode
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (warmNightMode) "☀" else "☾",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. شريط التبويبات الـ 4 الموحّد (مماثل لشريط ركن القرآن الكريم)
            if (!sensoryReaderActive && selectedHisnAxis == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (warmNightMode) Color(0xFF24170A) else Color(0xEE041815))
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val tabs = AdhkarVaultTab.entries
                    for (i in tabs.indices) {
                        val tab = tabs[i]
                        val selected = activeTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selected) Brush.horizontalGradient(
                                        if (warmNightMode) listOf(Color(0xFF5A3B16), Color(0xFF7A5220), Color(0xFF5A3B16))
                                        else listOf(Color(0xFF0E4B3B), Color(0xFF166B54), Color(0xFF0E4B3B))
                                    ) else SolidColor(Color.Transparent)
                                )
                                .then(
                                    if (selected) Modifier.border(0.85.dp, SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.80f), RoundedCornerShape(16.dp))
                                    else Modifier
                                )
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    activeTab = tab
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.titleAr,
                                color = if (selected) SakinaLuxuryColors.SoftGoldGlow else warmCream.copy(alpha = 0.78f),
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = SakinaBodyFont,
                                maxLines = 1,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = true))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. محتوى التبويب أو القارئ اللمسي
            Box(modifier = Modifier.weight(1f)) {
                when {
                    sensoryReaderActive -> {
                        val list = remember(activeSlot, wirdLevel) {
                            SakinaAdhkarVaultRepo.dailyWirdItems(activeSlot, wirdLevel)
                        }
                        SensoryFullCardReader(
                            ctx = ctx,
                            items = list,
                            currentIndex = sensoryIndex.coerceIn(0, (list.lastIndex).coerceAtLeast(0)),
                            progressMap = dhikrProgress,
                            warmNightMode = warmNightMode,
                            playingKey = playingKey,
                            favoriteIds = favoriteIds,
                            onIndexChange = { sensoryIndex = it },
                            onIncrement = { item ->
                                val cur = dhikrProgress[item.id] ?: 0
                                val next = (cur + 1).coerceAtMost(item.targetCount)
                                dhikrProgress[item.id] = next
                                recordDhikrTick(1)
                                if (next >= item.targetCount) {
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    if (sensoryIndex < list.lastIndex) {
                                        sensoryIndex += 1
                                    }
                                } else {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            },
                            onResetItem = { item -> dhikrProgress[item.id] = 0 },
                            onSpeak = { key, txt -> speakItem(key, txt) },
                            onToggleFav = { id -> favoriteIds = SakinaAdhkarVaultRepo.toggleFavoriteId(ctx, id) },
                            onExit = {
                                tts.stop()
                                playingKey = null
                                sensoryReaderActive = false
                            }
                        )
                    }

                    selectedHisnAxis != null -> {
                        HisnAxisDetailView(
                            ctx = ctx,
                            axis = selectedHisnAxis!!,
                            progressMap = dhikrProgress,
                            cardBg = cardEmerald,
                            cardBorder = cardBorder,
                            warmCream = warmCream,
                            playingKey = playingKey,
                            favoriteIds = favoriteIds,
                            onTapDhikr = { item ->
                                val cur = dhikrProgress[item.id] ?: 0
                                val next = if (cur >= item.targetCount) 0 else cur + 1
                                dhikrProgress[item.id] = next
                                if (next > cur) recordDhikrTick(1)
                                view.performHapticFeedback(
                                    if (next >= item.targetCount) HapticFeedbackConstants.LONG_PRESS
                                    else HapticFeedbackConstants.CLOCK_TICK
                                )
                            },
                            onSpeak = { key, txt -> speakItem(key, txt) },
                            onToggleFav = { id -> favoriteIds = SakinaAdhkarVaultRepo.toggleFavoriteId(ctx, id) }
                        )
                    }

                    else -> {
                        AnimatedContent(targetState = activeTab, label = "adhkarVaultTab") { tab ->
                            when (tab) {
                                AdhkarVaultTab.NOW -> {
                                    AdhkarNowTab(
                                        ctx = ctx,
                                        activeSlot = activeSlot,
                                        wirdLevel = wirdLevel,
                                        progressMap = dhikrProgress,
                                        streakDays = streakDays,
                                        currentStreak = currentStreak,
                                        baqiyatToday = baqiyatToday,
                                        cardBg = cardEmerald,
                                        cardBorder = cardBorder,
                                        warmCream = warmCream,
                                        onSelectSlot = { activeSlot = it },
                                        onSelectLevel = { wirdLevel = it },
                                        onStartSensoryAt = { idx ->
                                            sensoryIndex = idx
                                            sensoryReaderActive = true
                                        },
                                        onQuickTapItem = { item ->
                                            val cur = dhikrProgress[item.id] ?: 0
                                            val next = if (cur >= item.targetCount) 0 else cur + 1
                                            dhikrProgress[item.id] = next
                                            if (next > cur) recordDhikrTick(1)
                                            view.performHapticFeedback(
                                                if (next >= item.targetCount) HapticFeedbackConstants.LONG_PRESS
                                                else HapticFeedbackConstants.CLOCK_TICK
                                            )
                                        }
                                    )
                                }

                                AdhkarVaultTab.HISN -> {
                                    Hisn10AxesTab(
                                        ctx = ctx,
                                        cardBg = cardEmerald,
                                        cardBorder = cardBorder,
                                        warmCream = warmCream,
                                        favoriteIds = favoriteIds,
                                        onOpenAxis = { selectedHisnAxis = it },
                                        onOpenNawawi40 = { showNawawi40 = true },
                                        onOpenRuqyah = { showRuqyahPro = true },
                                        onToggleFav = { id -> favoriteIds = SakinaAdhkarVaultRepo.toggleFavoriteId(ctx, id) },
                                        onSpeak = { key, txt -> speakItem(key, txt) },
                                        playingKey = playingKey
                                    )
                                }

                                AdhkarVaultTab.TASBIH -> {
                                    SmartTasbihStudioTab(
                                        ctx = ctx,
                                        baqiyatTotal = baqiyatTotal,
                                        baqiyatToday = baqiyatToday,
                                        cardBg = cardEmerald,
                                        cardBorder = cardBorder,
                                        warmCream = warmCream,
                                        onIncrementGlobal = { recordDhikrTick(1) }
                                    )
                                }

                                AdhkarVaultTab.DUAS_NAMES -> {
                                    DuasAndDivineNamesTab(
                                        ctx = ctx,
                                        favoriteIds = favoriteIds,
                                        cardBg = cardEmerald,
                                        cardBorder = cardBorder,
                                        warmCream = warmCream,
                                        onRecordDhikr = { recordDhikrTick(1) }
                                    )
                                }
                            }
                        }
                    }
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
        if (showRuqyahPro) {
            RuqyahProOverlayDialog(
                headingFontFamily = SakinaHeadingFont,
                bodyFontFamily = SakinaBodyFont,
                onDismiss = { showRuqyahPro = false }
            )
        }
    }
}

// ============================================================================
// التبويب 1: «ذِكْرُك الآن» — المحرك السياقي + 3 مستويات + المواظبة الهادئة
// ============================================================================
@Composable
private fun AdhkarNowTab(
    ctx: Context,
    activeSlot: DailyWirdSlot,
    wirdLevel: VaultWirdLevel,
    progressMap: Map<String, Int>,
    streakDays: List<Pair<String, Boolean>>,
    currentStreak: Int,
    baqiyatToday: Int,
    cardBg: Color,
    cardBorder: Color,
    warmCream: Color,
    onSelectSlot: (DailyWirdSlot) -> Unit,
    onSelectLevel: (VaultWirdLevel) -> Unit,
    onStartSensoryAt: (Int) -> Unit,
    onQuickTapItem: (VaultDhikrItem) -> Unit
) {
    val items = remember(activeSlot, wirdLevel) {
        SakinaAdhkarVaultRepo.dailyWirdItems(activeSlot, wirdLevel)
    }
    val completedCount = items.count { (progressMap[it.id] ?: 0) >= it.targetCount }
    val progressFrac = if (items.isEmpty()) 0f else completedCount.toFloat() / items.size.toFloat()
    val firstIncompleteIdx = items.indexOfFirst { (progressMap[it.id] ?: 0) < it.targetCount }.let { if (it < 0) 0 else it }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 14.dp)
    ) {
        // بطاقة المِحراب السياقية الرئيسية
        item(key = "hero_mihrab") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0A392F), Color(0xFF05201B))
                        )
                    )
                    .border(1.1.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.72f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x33D9AE58))
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "✦ الورد المناسب لوقتك الآن",
                                    color = SakinaLuxuryColors.SoftGoldGlow,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SakinaBodyFont
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activeSlot.titleAr,
                                style = TextStyle(
                                    brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SakinaHeadingFont
                                )
                            )
                            Text(
                                text = "${activeSlot.subtitleAr} · ${items.size} أذكار (${wirdLevel.labelAr})",
                                color = warmCream.copy(alpha = 0.80f),
                                fontSize = 11.5.sp,
                                fontFamily = SakinaBodyFont
                            )
                        }

                        // حلقة نسبة الإنجاز الذهبية
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 5.5.dp.toPx()
                                drawArc(
                                    color = Color(0x33E6C372),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(stroke, cap = StrokeCap.Round)
                                )
                                drawArc(
                                    brush = Brush.sweepGradient(listOf(Color(0xFFC99B3B), Color(0xFFF6DF9D), Color(0xFFC99B3B))),
                                    startAngle = -90f,
                                    sweepAngle = 360f * progressFrac,
                                    useCenter = false,
                                    style = Stroke(stroke, cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = "${(progressFrac * 100).toInt()}%",
                                color = SakinaLuxuryColors.SoftGoldGlow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = SakinaNumeralFont,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                    }

                    // زر البدء اللمسي البارز
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFC99B3B), Color(0xFFF3D78E), Color(0xFFC99B3B))))
                            .clickable { onStartSensoryAt(firstIncompleteIdx) }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (completedCount == 0) "ابدأ الورد بالبطاقة اللمسية الكاملة ✦"
                            else if (completedCount < items.size) "واصل الورد ($completedCount / ${items.size}) ✦"
                            else "✓ أتممت الورد — أعد القراءة أو المراجعة",
                            color = Color(0xFF0A1D19),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = SakinaHeadingFont
                        )
                    }
                }
            }
        }

        // اختيار الورد اليومي (5 أوقات)
        item(key = "slot_chips") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(DailyWirdSlot.entries) { slot ->
                    val sel = slot == activeSlot
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (sel) Color(0xFF115543) else cardBg)
                            .border(
                                if (sel) 1.dp else 0.75.dp,
                                if (sel) SakinaLuxuryColors.SoftGoldGlow else cardBorder.copy(alpha = 0.45f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectSlot(slot) }
                            .padding(horizontal = 13.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = slot.titleAr,
                            color = if (sel) SakinaLuxuryColors.SoftGoldGlow else warmCream.copy(alpha = 0.85f),
                            fontSize = 11.5.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = SakinaBodyFont,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = true))
                        )
                    }
                }
            }
        }

        // 3 مستويات للورد (كامل · مختار · مختصر)
        item(key = "level_selector") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(0.85.dp, cardBorder.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val levels = VaultWirdLevel.entries
                for (i in levels.indices) {
                    val lv = levels[i]
                    val sel = lv == wirdLevel
                    val cnt = SakinaAdhkarVaultRepo.dailyWirdItems(activeSlot, lv).size
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) Color(0xFF0E4B3B) else Color.Transparent)
                            .then(
                                if (sel) Modifier.border(0.8.dp, SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                                else Modifier
                            )
                            .clickable { onSelectLevel(lv) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${lv.labelAr} ($cnt)",
                            color = if (sel) SakinaLuxuryColors.SoftGoldGlow else warmCream.copy(alpha = 0.85f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SakinaBodyFont
                        )
                        Text(
                            text = lv.hintAr,
                            color = warmCream.copy(alpha = 0.60f),
                            fontSize = 9.5.sp,
                            fontFamily = SakinaBodyFont
                        )
                    }
                }
            }
        }

        // سلسلة المواظبة الهادئة (14 يوماً)
        item(key = "quiet_streak") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(0.85.dp, cardBorder.copy(alpha = 0.50f), RoundedCornerShape(18.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سلسلة المواظبة الهادئة (آخر 14 يوماً)",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SakinaHeadingFont
                    )
                    Text(
                        text = "اليوم: $baqiyatToday · متتالية: $currentStreak",
                        color = warmCream.copy(alpha = 0.80f),
                        fontSize = 10.5.sp,
                        fontFamily = SakinaNumeralFont,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (idx in streakDays.indices) {
                        val (dayNum, isDone) = streakDays[idx]
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDone) Brush.radialGradient(listOf(Color(0xFFF5DC96), Color(0xFFC49434)))
                                        else SolidColor(Color(0x33FFFFFF))
                                    )
                                    .border(0.7.dp, if (isDone) Color(0xFFFFF3C8) else Color(0x33E6C372), CircleShape)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = dayNum,
                                color = warmCream.copy(alpha = if (isDone) 0.90f else 0.45f),
                                fontSize = 8.5.sp,
                                fontFamily = SakinaNumeralFont
                            )
                        }
                    }
                }
            }
        }

        // قائمة أذكار الورد الحالي للمعاينة السريعة أو الفتح اللمسي
        items(items.indices.toList(), key = { idx -> items[idx].id }) { idx ->
            val item = items[idx]
            val cur = progressMap[item.id] ?: 0
            val done = cur >= item.targetCount
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (done) Color(0xCC0C3A2E) else cardBg)
                    .border(
                        if (done) 1.dp else 0.8.dp,
                        if (done) SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.78f) else cardBorder.copy(alpha = 0.48f),
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { onStartSensoryAt(idx) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (done) Brush.radialGradient(listOf(Color(0xFFF3D58A), Color(0xFFC69634)))
                            else SolidColor(Color(0xFF051C18))
                        )
                        .border(1.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.7f), CircleShape)
                        .clickable { onQuickTapItem(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (done) "✓" else "$cur/${item.targetCount}",
                        color = if (done) Color(0xFF0A1D19) else SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = if (done) 18.sp else 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = SakinaNumeralFont
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = item.titleAr,
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SakinaHeadingFont
                    )
                    Text(
                        text = item.textAr,
                        color = warmCream.copy(alpha = 0.90f),
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = SakinaBodyFont
                    )
                    Text(
                        text = item.takhrijAr,
                        color = warmCream.copy(alpha = 0.56f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = SakinaBodyFont
                    )
                }
            }
        }
    }
}

// ============================================================================
// البطاقة اللمسية الكاملة (Full-Card Sensory Reader + اختبار الحفظ + التخريج + TTS)
// ============================================================================
@Composable
private fun SensoryFullCardReader(
    ctx: Context,
    items: List<VaultDhikrItem>,
    currentIndex: Int,
    progressMap: Map<String, Int>,
    warmNightMode: Boolean,
    playingKey: String?,
    favoriteIds: Set<String>,
    onIndexChange: (Int) -> Unit,
    onIncrement: (VaultDhikrItem) -> Unit,
    onResetItem: (VaultDhikrItem) -> Unit,
    onSpeak: (String, String) -> Unit,
    onToggleFav: (String) -> Unit,
    onExit: () -> Unit
) {
    if (items.isEmpty()) return
    val item = items[currentIndex]
    val curCount = progressMap[item.id] ?: 0
    val target = item.targetCount.coerceAtLeast(1)
    val frac = (curCount.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val done = curCount >= target

    var showTakhrij by remember(item.id) { mutableStateOf(false) }
    var quizMode by remember(item.id) { mutableStateOf(false) }
    var quizRevealed by remember(item.id) { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (done) 1.01f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardPulse"
    )

    val displayBody = remember(item.textAr, item.quizWord, quizMode, quizRevealed) {
        if (quizMode && !quizRevealed && item.quizWord.isNotBlank() && item.textAr.contains(item.quizWord)) {
            item.textAr.replace(item.quizWord, " [ ...... ] ")
        } else {
            item.textAr
        }
    }

    val cardGradient = if (warmNightMode) {
        Brush.verticalGradient(listOf(Color(0xFF2D1E0E), Color(0xFF1E1409)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF0A392F), Color(0xFF041A16)))
    }
    val textColor = if (warmNightMode) Color(0xFFFFE7BF) else SakinaOrbitalColors.WarmCream

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // شريط التقدم المئوي الذهبي
        val overallDone = items.count { (progressMap[it.id] ?: 0) >= it.targetCount }
        val overallFrac = overallDone.toFloat() / items.size.toFloat()
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "الذكر ${currentIndex + 1} من ${items.size}",
                    color = SakinaLuxuryColors.SoftGoldGlow,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SakinaNumeralFont
                )
                Text(
                    text = "إنجاز الورد: ${(overallFrac * 100).toInt()}%",
                    color = textColor.copy(alpha = 0.80f),
                    fontSize = 11.5.sp,
                    fontFamily = SakinaNumeralFont
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x33E6C372))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(overallFrac.coerceIn(0.03f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFC99B3B), Color(0xFFF6DF9D))))
                )
            }
        }

        // البطاقة اللمسية الكبرى (المس أي مكان فيها للعدّ)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .scale(scale)
                .clip(RoundedCornerShape(26.dp))
                .background(cardGradient)
                .border(
                    1.3.dp,
                    if (done) SakinaLuxuryColors.SoftGoldGlow else SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.65f),
                    RoundedCornerShape(26.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (quizMode && !quizRevealed) {
                        quizRevealed = true
                    } else {
                        onIncrement(item)
                    }
                }
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // عنوان الذكر + شارة التوثيق
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = item.titleAr,
                        style = TextStyle(
                            brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SakinaHeadingFont
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (quizMode && !quizRevealed) "اضغط على البطاقة لكشف الكلمة المخفية"
                        else "المس أي مكان في البطاقة للعدّ والانتقال التلقائي",
                        color = textColor.copy(alpha = 0.62f),
                        fontSize = 10.5.sp,
                        fontFamily = SakinaBodyFont
                    )
                }

                // متن الذكر بالتشكيل الكامل (قابل للتمرير للنصوص الطويلة)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 10.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayBody,
                        color = textColor,
                        fontSize = 20.sp,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = SakinaHeadingFont
                    )
                }

                // ميدالية العدّ الدائرية الذهبية
                Box(
                    modifier = Modifier.size(74.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val st = 5.5.dp.toPx()
                        drawArc(Color(0x33E6C372), -90f, 360f, false, style = Stroke(st, cap = StrokeCap.Round))
                        drawArc(
                            brush = Brush.sweepGradient(listOf(Color(0xFFC99B3B), Color(0xFFFFE7A0), Color(0xFFC99B3B))),
                            startAngle = -90f,
                            sweepAngle = 360f * frac,
                            useCenter = false,
                            style = Stroke(st, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (done) "✓" else "$curCount",
                            color = SakinaLuxuryColors.SoftGoldGlow,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = SakinaNumeralFont
                        )
                        if (!done) {
                            Text(
                                text = "من $target",
                                color = textColor.copy(alpha = 0.72f),
                                fontSize = 10.sp,
                                fontFamily = SakinaNumeralFont
                            )
                        }
                    }
                }
            }
        }

        // درج التخريج وغريب الألفاظ وفضل الذكر
        AnimatedVisibility(visible = showTakhrij) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xE6051E1A))
                    .border(0.85.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("📖 التخريج: ${item.takhrijAr}", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaBodyFont)
                Text("✦ الفضل: ${item.virtueAr}", color = textColor.copy(alpha = 0.92f), fontSize = 11.5.sp, fontFamily = SakinaBodyFont)
                if (item.gharibAr.isNotBlank()) {
                    Text("💡 غريب الألفاظ: ${item.gharibAr}", color = textColor.copy(alpha = 0.80f), fontSize = 11.sp, fontFamily = SakinaBodyFont)
                }
            }
        }

        // شريط الأدوات أسفل البطاقة (السابق · استماع · التخريج · اختبار الحفظ · مفضلة · مشاركة · التالي)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniGoldPill("السابق", enabled = currentIndex > 0) { onIndexChange((currentIndex - 1).coerceAtLeast(0)) }
            MiniGoldPill(if (playingKey == item.id) "إيقاف" else "استماع") { onSpeak(item.id, item.textAr) }
            MiniGoldPill(if (showTakhrij) "إخفاء" else "التخريج") { showTakhrij = !showTakhrij }
            if (item.quizWord.isNotBlank()) {
                MiniGoldPill(if (quizMode) "عادي" else "اختبار") {
                    quizMode = !quizMode
                    quizRevealed = false
                }
            }
            MiniGoldPill(if (favoriteIds.contains(item.id)) "★" else "☆") { onToggleFav(item.id) }
            MiniGoldPill("مشاركة") { SakinaAdhkarVaultRepo.shareDhikrCard(ctx, item.titleAr, item.textAr, item.takhrijAr) }
            MiniGoldPill(
                label = if (currentIndex < items.lastIndex) "التالي" else "إنهاء",
                enabled = true
            ) {
                if (currentIndex < items.lastIndex) onIndexChange(currentIndex + 1)
                else onExit()
            }
        }

        if (curCount > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "↺ تصفير عدّاد هذا الذكر",
                    color = textColor.copy(alpha = 0.60f),
                    fontSize = 10.5.sp,
                    fontFamily = SakinaBodyFont,
                    modifier = Modifier.clickable { onResetItem(item) }.padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniGoldPill(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (enabled) Color(0xCC06241F) else Color(0x5506241F))
            .border(0.75.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = if (enabled) 0.55f else 0.22f), RoundedCornerShape(11.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) SakinaLuxuryColors.SoftGoldGlow else Color(0x66E6C372),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SakinaBodyFont,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = true))
        )
    }
}

// ============================================================================
// التبويب 2: «حصن المسلم» — 10 محاور حياتية + بحث شامل + الأربعون النووية + الرقية
// ============================================================================
@Composable
private fun Hisn10AxesTab(
    ctx: Context,
    cardBg: Color,
    cardBorder: Color,
    warmCream: Color,
    favoriteIds: Set<String>,
    onOpenAxis: (VaultHisnAxis) -> Unit,
    onOpenNawawi40: () -> Unit,
    onOpenRuqyah: () -> Unit,
    onToggleFav: (String) -> Unit,
    onSpeak: (String, String) -> Unit,
    playingKey: String?
) {
    var query by remember { mutableStateOf("") }
    val normalizedQ = remember(query) { SakinaAdhkarVaultRepo.normalizeArabic(query) }
    val searchResults = remember(normalizedQ) {
        if (normalizedQ.length < 2) emptyList()
        else SakinaAdhkarVaultRepo.hisn10Axes.flatMap { ax ->
            ax.items.filter {
                SakinaAdhkarVaultRepo.normalizeArabic(it.titleAr).contains(normalizedQ) ||
                    SakinaAdhkarVaultRepo.normalizeArabic(it.textAr).contains(normalizedQ) ||
                    SakinaAdhkarVaultRepo.normalizeArabic(it.virtueAr).contains(normalizedQ)
            }.map { ax.titleAr to it }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 14.dp)
    ) {
        // شريط البحث الفوري في الموسوعة
        item(key = "hisn_search") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(0.9.dp, cardBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = SakinaLuxuryColors.SoftGoldGlow, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "ابحث في حصن المسلم والأدعية والتخريج...",
                            color = warmCream.copy(alpha = 0.50f),
                            fontSize = 12.sp,
                            fontFamily = SakinaBodyFont
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(color = warmCream, fontSize = 12.5.sp, fontFamily = SakinaBodyFont),
                        cursorBrush = SolidColor(SakinaLuxuryColors.SoftGoldGlow),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = null,
                        tint = warmCream.copy(alpha = 0.7f),
                        modifier = Modifier.size(17.dp).clickable { query = "" }
                    )
                }
            }
        }

        if (normalizedQ.length >= 2) {
            if (searchResults.isEmpty()) {
                item(key = "no_res") {
                    Text(
                        text = "لا توجد نتائج مطابقة لـ «$query».",
                        color = warmCream.copy(alpha = 0.70f),
                        fontSize = 12.sp,
                        fontFamily = SakinaBodyFont,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                items(searchResults, key = { it.second.id }) { (axisName, item) ->
                    VerifiedScientificDhikrCard(
                        ctx = ctx,
                        item = item,
                        subtitleBadge = axisName,
                        currentCount = 0,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        warmCream = warmCream,
                        isPlaying = playingKey == item.id,
                        isFavorite = favoriteIds.contains(item.id),
                        onTapCount = {},
                        onSpeak = { onSpeak(item.id, item.textAr) },
                        onToggleFav = { onToggleFav(item.id) }
                    )
                }
            }
        } else {
            // بوابتا الأربعين النووية والرقية الشرعية الموثّقة
            item(key = "featured_gateways") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF0E493A), Color(0xFF06231D))))
                            .border(1.dp, SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.70f), RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenRuqyah)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("✦ الرقية الشرعية", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                            Text("آيات وأدعية التحصين الموثّقة", color = warmCream.copy(alpha = 0.78f), fontSize = 10.5.sp, fontFamily = SakinaBodyFont)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF0E493A), Color(0xFF06231D))))
                            .border(1.dp, SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.70f), RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenNawawi40)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("✦ الأربعون النووية", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                            Text("جوامع الكلم بالشروح والفوائد", color = warmCream.copy(alpha = 0.78f), fontSize = 10.5.sp, fontFamily = SakinaBodyFont)
                        }
                    }
                }
            }

            // شبكة المحاور العشرة (عمودان أنيقان)
            val rows = SakinaAdhkarVaultRepo.hisn10Axes.chunked(2)
            items(rows.indices.toList(), key = { idx -> "axis_row_$idx" }) { rowIdx ->
                val pair = rows[rowIdx]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (j in pair.indices) {
                        val ax = pair[j]
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(cardBg)
                                .border(0.9.dp, cardBorder.copy(alpha = 0.58f), RoundedCornerShape(18.dp))
                                .clickable { onOpenAxis(ax) }
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x33D9AE58))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(ax.numberLabel, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaNumeralFont)
                                }
                                Text("${ax.items.size} أبواب", color = warmCream.copy(alpha = 0.60f), fontSize = 10.sp, fontFamily = SakinaBodyFont)
                            }
                            Text(
                                text = ax.titleAr,
                                color = SakinaLuxuryColors.SoftGoldGlow,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaHeadingFont,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = ax.subtitleAr,
                                color = warmCream.copy(alpha = 0.72f),
                                fontSize = 10.5.sp,
                                lineHeight = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = SakinaBodyFont
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HisnAxisDetailView(
    ctx: Context,
    axis: VaultHisnAxis,
    progressMap: Map<String, Int>,
    cardBg: Color,
    cardBorder: Color,
    warmCream: Color,
    playingKey: String?,
    favoriteIds: Set<String>,
    onTapDhikr: (VaultDhikrItem) -> Unit,
    onSpeak: (String, String) -> Unit,
    onToggleFav: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 14.dp)
    ) {
        items(axis.items, key = { it.id }) { item ->
            VerifiedScientificDhikrCard(
                ctx = ctx,
                item = item,
                subtitleBadge = null,
                currentCount = progressMap[item.id] ?: 0,
                cardBg = cardBg,
                cardBorder = cardBorder,
                warmCream = warmCream,
                isPlaying = playingKey == item.id,
                isFavorite = favoriteIds.contains(item.id),
                onTapCount = { onTapDhikr(item) },
                onSpeak = { onSpeak(item.id, item.textAr) },
                onToggleFav = { onToggleFav(item.id) }
            )
        }
    }
}

@Composable
private fun VerifiedScientificDhikrCard(
    ctx: Context,
    item: VaultDhikrItem,
    subtitleBadge: String?,
    currentCount: Int,
    cardBg: Color,
    cardBorder: Color,
    warmCream: Color,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onTapCount: () -> Unit,
    onSpeak: () -> Unit,
    onToggleFav: () -> Unit
) {
    var expandedDetails by remember(item.id) { mutableStateOf(false) }
    val done = currentCount >= item.targetCount

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (done) Color(0xCC0C3A2E) else cardBg)
            .border(0.95.dp, if (done) SakinaLuxuryColors.SoftGoldGlow else cardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onTapCount)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (subtitleBadge != null) {
                    Text(subtitleBadge, color = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.78f), fontSize = 10.sp, fontFamily = SakinaBodyFont)
                }
                Text(
                    text = item.titleAr,
                    color = SakinaLuxuryColors.SoftGoldGlow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SakinaHeadingFont
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (done) Color(0xFFD9AE58) else Color(0xFF041815))
                    .border(0.8.dp, SakinaLuxuryColors.ChampagneGold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (done) "✓ تم" else "$currentCount / ${item.targetCount}",
                    color = if (done) Color(0xFF071A18) else SakinaLuxuryColors.SoftGoldGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SakinaNumeralFont
                )
            }
        }

        Text(
            text = item.textAr,
            color = warmCream,
            fontSize = 15.5.sp,
            lineHeight = 27.sp,
            fontFamily = SakinaHeadingFont
        )

        Text(
            text = "📖 ${item.takhrijAr}",
            color = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.85f),
            fontSize = 10.5.sp,
            fontFamily = SakinaBodyFont
        )

        AnimatedVisibility(visible = expandedDetails) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x88031412))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("✦ فضل الذكر: ${item.virtueAr}", color = warmCream.copy(alpha = 0.92f), fontSize = 11.5.sp, fontFamily = SakinaBodyFont)
                if (item.gharibAr.isNotBlank()) {
                    Text("💡 غريب الألفاظ: ${item.gharibAr}", color = warmCream.copy(alpha = 0.78f), fontSize = 11.sp, fontFamily = SakinaBodyFont)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniGoldPill(if (expandedDetails) "إخفاء الفضل" else "الفضل والغريب") { expandedDetails = !expandedDetails }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniGoldPill(if (isPlaying) "إيقاف" else "استماع", onClick = onSpeak)
                MiniGoldPill(if (isFavorite) "★" else "☆", onClick = onToggleFav)
                MiniGoldPill("مشاركة") { SakinaAdhkarVaultRepo.shareDhikrCard(ctx, item.titleAr, item.textAr, item.takhrijAr) }
            }
        }
    }
}

// ============================================================================
// التبويب 3: «المسبحة الذكية» — طوق ثلاثي الأبعاد + أزرار الصوت + خزينة الباقيات
// ============================================================================
@Composable
private fun SmartTasbihStudioTab(
    ctx: Context,
    baqiyatTotal: Long,
    baqiyatToday: Int,
    cardBg: Color,
    cardBorder: Color,
    warmCream: Color,
    onIncrementGlobal: () -> Unit
) {
    val view = LocalView.current
    var customWirds by remember { mutableStateOf(SakinaAdhkarVaultRepo.loadCustomWirds(ctx)) }
    val allWirds = remember(customWirds) { SakinaAdhkarVaultRepo.builtInTasbihWirds + customWirds }
    var selectedIdx by remember { mutableIntStateOf(0) }
    val currentWird = allWirds.getOrElse(selectedIdx) { SakinaAdhkarVaultRepo.builtInTasbihWirds.first() }

    var count by remember(currentWird.id) { mutableIntStateOf(0) }
    var round by remember(currentWird.id) { mutableIntStateOf(1) }
    var target by remember(currentWird.id) { mutableIntStateOf(currentWird.target) }
    var volumeKeyEnabled by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(volumeKeyEnabled) {
        if (volumeKeyEnabled) {
            try { focusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    fun doTasbihTick() {
        onIncrementGlobal()
        if (target <= 0) {
            count += 1
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } else if (count + 1 >= target) {
            count = 0
            round += 1
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        } else {
            count += 1
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    val frac = if (target <= 0) ((count % 100) / 100f) else (count.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val animFrac by animateFloatAsState(targetValue = frac, animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "tasbihArc")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { ev ->
                if (volumeKeyEnabled && ev.type == KeyEventType.KeyDown && (ev.key == Key.VolumeUp || ev.key == Key.VolumeDown)) {
                    doTasbihTick()
                    true
                } else {
                    false
                }
            }
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. طوق التسبيح المداري الذهبي ثلاثي الأبعاد
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF0A392F), Color(0xFF041915))))
                .border(1.1.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.70f), RoundedCornerShape(24.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = currentWird.textAr,
                    style = TextStyle(
                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SakinaHeadingFont
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = currentWird.virtueAr,
                    color = warmCream.copy(alpha = 0.74f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = SakinaBodyFont
                )

                // الحلقة اللمسية الكبرى
                Box(
                    modifier = Modifier
                        .size(196.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { doTasbihTick() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                        val stroke = 11.dp.toPx()
                        val r = (size.minDimension - stroke) / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(Color(0xFF041714), radius = r)
                        drawCircle(Color(0x33E6C372), radius = r, style = Stroke(stroke))
                        drawArc(
                            brush = Brush.sweepGradient(listOf(Color(0xFFC4922F), Color(0xFFFFE7A1), Color(0xFFC4922F))),
                            startAngle = -90f,
                            sweepAngle = 360f * animFrac,
                            useCenter = false,
                            topLeft = Offset(center.x - r, center.y - r),
                            size = Size(r * 2f, r * 2f),
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                        val angleRad = (-90.0 + 360.0 * animFrac) * PI / 180.0
                        val orbPos = Offset(
                            center.x + (r * cos(angleRad)).toFloat(),
                            center.y + (r * sin(angleRad)).toFloat()
                        )
                        drawCircle(Color(0xFFFFF3C7), radius = 7.dp.toPx(), center = orbPos)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$count",
                            style = TextStyle(
                                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = SakinaNumeralFont,
                                fontFeatureSettings = "tnum"
                            )
                        )
                        Text(
                            text = if (target <= 0) "مفتوح ∞ · الجولة $round" else "الهدف $target · الجولة $round",
                            color = warmCream.copy(alpha = 0.78f),
                            fontSize = 11.5.sp,
                            fontFamily = SakinaBodyFont
                        )
                        Text(
                            text = "المس الحلقة للتسبيح",
                            color = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.65f),
                            fontSize = 10.sp,
                            fontFamily = SakinaBodyFont
                        )
                    }
                }

                // اختيار الهدف (33 / 99 / 100 / 1000 / مفتوح) + التصفير
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val targets = listOf(33 to "33", 99 to "99", 100 to "100", 1000 to "1000", 0 to "∞")
                    for (i in targets.indices) {
                        val (tVal, tLbl) = targets[i]
                        val sel = target == tVal
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (sel) Color(0xFFD9AE58) else Color(0xFF041714))
                                .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .clickable { target = tVal }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = tLbl,
                                color = if (sel) Color(0xFF071A18) else SakinaLuxuryColors.SoftGoldGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaNumeralFont
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF041714))
                            .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.6f), CircleShape)
                            .clickable {
                                count = 0
                                round = 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "تصفير", tint = SakinaLuxuryColors.SoftGoldGlow, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // 2. مفتاح التسبيح بأزرار الصوت (بمفتاح الذهب ثلاثي الأبعاد)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cardBg)
                .border(0.9.dp, cardBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("التسبيح بأزرار الصوت", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                Text("سبّح بزر رفع أو خفض الصوت دون النظر للشاشة", color = warmCream.copy(alpha = 0.72f), fontSize = 10.5.sp, fontFamily = SakinaBodyFont)
            }
            SakinaJewelSwitch(
                checked = volumeKeyEnabled,
                onCheckedChange = { volumeKeyEnabled = it }
            )
        }

        // 3. خزينة «الباقيات الصالحات» التراكمية
        val nextMilestone = when {
            baqiyatTotal < 100L -> 100L
            baqiyatTotal < 1_000L -> 1_000L
            baqiyatTotal < 10_000L -> 10_000L
            else -> ((baqiyatTotal / 10_000L) + 1L) * 10_000L
        }
        val mileFrac = (baqiyatTotal.toFloat() / nextMilestone.toFloat()).coerceIn(0f, 1f)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cardBg)
                .border(0.9.dp, cardBorder, RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("✦ خزينة الباقيات الصالحات", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                Text("اليوم: $baqiyatToday", color = warmCream.copy(alpha = 0.80f), fontSize = 11.sp, fontFamily = SakinaNumeralFont)
            }
            Text(
                text = "إجمالي غرسك المحفوظ: $baqiyatTotal تسبيحة",
                color = warmCream,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SakinaNumeralFont
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x33E6C372))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(mileFrac.coerceAtLeast(0.03f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFC99B3B), Color(0xFFF6DF9D))))
                )
            }
            Text(
                text = "الوسام القادم: $nextMilestone تسبيحة",
                color = warmCream.copy(alpha = 0.65f),
                fontSize = 10.5.sp,
                fontFamily = SakinaBodyFont
            )
        }

        // 4. رفّ الأوراد المدمجة والمخصّصة + زر إضافة ورد جديد
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("اختر ورد التسبيح (${allWirds.size})", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0E4B3B))
                    .border(0.8.dp, SakinaLuxuryColors.SoftGoldGlow, RoundedCornerShape(12.dp))
                    .clickable { showAddDialog = true }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = SakinaLuxuryColors.SoftGoldGlow, modifier = Modifier.size(14.dp))
                Text("ورد مخصّص", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaBodyFont)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            for (idx in allWirds.indices) {
                val w = allWirds[idx]
                val sel = idx == selectedIdx
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .background(if (sel) Color(0xFF0E4B3B) else cardBg)
                        .border(if (sel) 1.dp else 0.75.dp, if (sel) SakinaLuxuryColors.SoftGoldGlow else cardBorder.copy(alpha = 0.45f), RoundedCornerShape(15.dp))
                        .clickable { selectedIdx = idx }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(w.textAr, color = if (sel) SakinaLuxuryColors.SoftGoldGlow else warmCream, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                        Text("الهدف: ${if (w.target <= 0) "مفتوح" else w.target}", color = warmCream.copy(alpha = 0.65f), fontSize = 10.sp, fontFamily = SakinaBodyFont)
                    }
                    if (!w.isBuiltIn) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "حذف",
                            tint = Color(0xFFFF9B85),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    val updated = customWirds.filterNot { it.id == w.id }
                                    customWirds = updated
                                    SakinaAdhkarVaultRepo.saveCustomWirds(ctx, updated)
                                    if (selectedIdx >= (SakinaAdhkarVaultRepo.builtInTasbihWirds.size + updated.size)) {
                                        selectedIdx = 0
                                    }
                                }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showAddDialog) {
        var newText by remember { mutableStateOf("") }
        var newTarget by remember { mutableStateOf("100") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF06231D),
            title = { Text("إضافة ورد تسبيح مخصّص", color = SakinaLuxuryColors.SoftGoldGlow, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF041613))
                            .border(0.8.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        if (newText.isEmpty()) Text("اكتب صيغة الذكر هنا...", color = warmCream.copy(alpha = 0.45f), fontSize = 12.sp)
                        BasicTextField(
                            value = newText,
                            onValueChange = { newText = it },
                            textStyle = TextStyle(color = warmCream, fontSize = 13.sp, fontFamily = SakinaBodyFont),
                            cursorBrush = SolidColor(SakinaLuxuryColors.SoftGoldGlow),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF041613))
                            .border(0.8.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        if (newTarget.isEmpty()) Text("الهدف (مثلاً 33 أو 100)", color = warmCream.copy(alpha = 0.45f), fontSize = 12.sp)
                        BasicTextField(
                            value = newTarget,
                            onValueChange = { newTarget = it.filter { ch -> ch.isDigit() } },
                            textStyle = TextStyle(color = warmCream, fontSize = 13.sp, fontFamily = SakinaNumeralFont),
                            cursorBrush = SolidColor(SakinaLuxuryColors.SoftGoldGlow),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                MiniGoldPill("حفظ") {
                    if (newText.isNotBlank()) {
                        val added = CustomTasbihWird(
                            id = "c_${System.currentTimeMillis()}",
                            textAr = newText.trim(),
                            virtueAr = "ورد شخصي محفوظ",
                            target = newTarget.toIntOrNull()?.coerceAtLeast(1) ?: 33,
                            isBuiltIn = false
                        )
                        val updated = customWirds + added
                        customWirds = updated
                        SakinaAdhkarVaultRepo.saveCustomWirds(ctx, updated)
                        selectedIdx = SakinaAdhkarVaultRepo.builtInTasbihWirds.size + updated.lastIndex
                    }
                    showAddDialog = false
                }
            },
            dismissButton = { MiniGoldPill("إلغاء") { showAddDialog = false } }
        )
    }
}

// ============================================================================
// التبويب 4: «أدعيتي والأسماء الحسنى» — الدفتر الشخصي + 99 اسماً + التذكيرات الهادئة
// ============================================================================
@Composable
private fun DuasAndDivineNamesTab(
    ctx: Context,
    favoriteIds: Set<String>,
    cardBg: Color,
    cardBorder: Color,
    warmCream: Color,
    onRecordDhikr: () -> Unit
) {
    var subMode by remember { mutableIntStateOf(0) } // 0 = أدعيتي, 1 = الأسماء الحسنى, 2 = التذكيرات
    var personalDuas by remember { mutableStateOf(SakinaAdhkarVaultRepo.loadPersonalDuas(ctx)) }
    var showAddDua by remember { mutableStateOf(false) }
    var heartFilter by remember { mutableStateOf(HeartStateFilter.ALL) }
    var remPrefs by remember { mutableStateOf(SakinaAdhkarVaultRepo.loadReminderPrefs(ctx)) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // مبدّل فرعي أنيق لـ 3 أقسام
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(0.85.dp, cardBorder.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val labels = listOf("أدعيتي الخاصة", "الأسماء الحسنى", "تذكيرات هادئة")
            for (i in labels.indices) {
                val sel = subMode == i
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (sel) Color(0xFF0E4B3B) else Color.Transparent)
                        .then(if (sel) Modifier.border(0.75.dp, SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.7f), RoundedCornerShape(13.dp)) else Modifier)
                        .clickable { subMode = i }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labels[i],
                        color = if (sel) SakinaLuxuryColors.SoftGoldGlow else warmCream.copy(alpha = 0.78f),
                        fontSize = 11.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = SakinaBodyFont,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = true))
                    )
                }
            }
        }

        when (subMode) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 14.dp)
                ) {
                    item(key = "add_dua_btn") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFFC99B3B), Color(0xFFF3D78E), Color(0xFFC99B3B))))
                                .clickable { showAddDua = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ إضافة دعاء جديد إلى دفتري الخاص", color = Color(0xFF071A18), fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, fontFamily = SakinaHeadingFont)
                        }
                    }

                    items(personalDuas, key = { it.id }) { dua ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(cardBg)
                                .border(0.9.dp, cardBorder, RoundedCornerShape(18.dp))
                                .clickable {
                                    val updated = personalDuas.map { if (it.id == dua.id) it.copy(count = it.count + 1) else it }
                                    personalDuas = updated
                                    SakinaAdhkarVaultRepo.savePersonalDuas(ctx, updated)
                                    onRecordDhikr()
                                }
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("✦ ${dua.titleAr}", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                                Text("ترديد: ${dua.count}", color = warmCream.copy(alpha = 0.72f), fontSize = 11.sp, fontFamily = SakinaNumeralFont)
                            }
                            Text(dua.bodyAr, color = warmCream, fontSize = 14.5.sp, lineHeight = 25.sp, fontFamily = SakinaHeadingFont)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                MiniGoldPill("مشاركة") { SakinaAdhkarVaultRepo.shareDhikrCard(ctx, dua.titleAr, dua.bodyAr, "أدعيتي الخاصة") }
                                Spacer(modifier = Modifier.width(6.dp))
                                MiniGoldPill("حذف") {
                                    val updated = personalDuas.filterNot { it.id == dua.id }
                                    personalDuas = updated
                                    SakinaAdhkarVaultRepo.savePersonalDuas(ctx, updated)
                                }
                            }
                        }
                    }

                    if (favoriteIds.isNotEmpty()) {
                        val favItems = SakinaAdhkarVaultRepo.hisn10Axes.flatMap { it.items }.filter { favoriteIds.contains(it.id) }
                        if (favItems.isNotEmpty()) {
                            item(key = "fav_hdr") {
                                Text("★ المفضلة من حصن المسلم (${favItems.size})", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                            }
                            items(favItems, key = { "fav_${it.id}" }) { fi ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(cardBg)
                                        .border(0.8.dp, cardBorder.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(fi.titleAr, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                                    Text(fi.textAr, color = warmCream, fontSize = 13.5.sp, lineHeight = 23.sp, fontFamily = SakinaBodyFont)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // أسماء الله الحسنى التفاعلية بفلترة حال القلب
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(HeartStateFilter.entries) { st ->
                        val sel = st == heartFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (sel) Color(0xFF115543) else cardBg)
                                .border(if (sel) 1.dp else 0.7.dp, if (sel) SakinaLuxuryColors.SoftGoldGlow else cardBorder.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .clickable { heartFilter = st }
                                .padding(horizontal = 11.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = st.labelAr,
                                color = if (sel) SakinaLuxuryColors.SoftGoldGlow else warmCream.copy(alpha = 0.82f),
                                fontSize = 11.sp,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = SakinaBodyFont,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = true))
                            )
                        }
                    }
                }

                val filteredNames = remember(heartFilter) {
                    if (heartFilter == HeartStateFilter.ALL) SakinaAdhkarVaultRepo.interactiveDivineNames
                    else SakinaAdhkarVaultRepo.interactiveDivineNames.filter { it.heartNeeds.contains(heartFilter) }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 14.dp)
                ) {
                    items(filteredNames, key = { it.number }) { nm ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(cardBg)
                                .border(0.95.dp, cardBorder, RoundedCornerShape(20.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = nm.nameAr,
                                    style = TextStyle(
                                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SakinaHeadingFont
                                    )
                                )
                                MiniGoldPill("مشاركة الدعاء") {
                                    SakinaAdhkarVaultRepo.shareDhikrCard(ctx, "الدعاء باسم الله ${nm.nameAr}", nm.howToPrayAr, nm.quranRefAr)
                                }
                            }
                            Text(nm.meaningAr, color = warmCream.copy(alpha = 0.90f), fontSize = 12.5.sp, lineHeight = 20.sp, fontFamily = SakinaBodyFont)
                            Text(nm.quranRefAr, color = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.85f), fontSize = 11.5.sp, fontFamily = SakinaHeadingFont)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x99031512))
                                    .border(0.7.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("كيف تدعو الله بهذا الاسم؟", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaBodyFont)
                                    Text(nm.howToPrayAr, color = warmCream, fontSize = 13.sp, lineHeight = 22.sp, fontFamily = SakinaHeadingFont)
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // التذكيرات الهادئة مع الكتم التلقائي وقت النوم
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(0.95.dp, cardBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تذكيرات الذكر الهادئة", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                            Text("إشعار لطيف صامت يذكّرك بذكر قصير خلال اليوم", color = warmCream.copy(alpha = 0.74f), fontSize = 11.sp, fontFamily = SakinaBodyFont)
                        }
                        SakinaJewelSwitch(
                            checked = remPrefs.enabled,
                            onCheckedChange = {
                                remPrefs = remPrefs.copy(enabled = it)
                                SakinaAdhkarVaultRepo.saveReminderPrefs(ctx, remPrefs)
                            }
                        )
                    }

                    Text("الفاصل الزمني بين التذكيرات", color = warmCream.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaBodyFont)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val opts = listOf(1 to "كل ساعة", 2 to "كل ساعتين", 3 to "كل 3 ساعات")
                        for (i in opts.indices) {
                            val (h, lbl) = opts[i]
                            val sel = remPrefs.intervalHours == h
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (sel) Color(0xFF115543) else Color(0xFF041714))
                                    .border(0.85.dp, if (sel) SakinaLuxuryColors.SoftGoldGlow else cardBorder.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        remPrefs = remPrefs.copy(intervalHours = h)
                                        SakinaAdhkarVaultRepo.saveReminderPrefs(ctx, remPrefs)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(lbl, color = if (sel) SakinaLuxuryColors.SoftGoldGlow else warmCream, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaBodyFont)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("الكتم التلقائي وقت النوم", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaHeadingFont)
                            Text("إيقاف التذكيرات تلقائياً من 11:00 مساءً حتى 5:00 صباحاً", color = warmCream.copy(alpha = 0.74f), fontSize = 11.sp, fontFamily = SakinaBodyFont)
                        }
                        SakinaJewelSwitch(
                            checked = remPrefs.muteAtSleep,
                            onCheckedChange = {
                                remPrefs = remPrefs.copy(muteAtSleep = it)
                                SakinaAdhkarVaultRepo.saveReminderPrefs(ctx, remPrefs)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDua) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDua = false },
            containerColor = Color(0xFF06231D),
            title = { Text("إضافة دعاء خاص", color = SakinaLuxuryColors.SoftGoldGlow, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF041613))
                            .border(0.8.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        if (title.isEmpty()) Text("عنوان الدعاء (مثلاً: دعاء التوفيق)", color = warmCream.copy(alpha = 0.45f), fontSize = 12.sp)
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(color = warmCream, fontSize = 13.sp, fontFamily = SakinaBodyFont),
                            cursorBrush = SolidColor(SakinaLuxuryColors.SoftGoldGlow),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF041613))
                            .border(0.8.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        if (body.isEmpty()) Text("نص الدعاء...", color = warmCream.copy(alpha = 0.45f), fontSize = 12.sp)
                        BasicTextField(
                            value = body,
                            onValueChange = { body = it },
                            textStyle = TextStyle(color = warmCream, fontSize = 13.sp, fontFamily = SakinaBodyFont),
                            cursorBrush = SolidColor(SakinaLuxuryColors.SoftGoldGlow),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                MiniGoldPill("حفظ") {
                    if (body.isNotBlank()) {
                        val entry = PersonalDuaEntry(
                            id = "pd_${System.currentTimeMillis()}",
                            titleAr = title.ifBlank { "دعاء خاص" }.trim(),
                            bodyAr = body.trim()
                        )
                        val updated = listOf(entry) + personalDuas
                        personalDuas = updated
                        SakinaAdhkarVaultRepo.savePersonalDuas(ctx, updated)
                    }
                    showAddDua = false
                }
            },
            dismissButton = { MiniGoldPill("إلغاء") { showAddDua = false } }
        )
    }
}
