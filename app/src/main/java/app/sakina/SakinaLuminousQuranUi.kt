package app.sakina

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

// ============================================================================
// 1. نماذج البيانات، الروايات، التجويد/أصول ورش، وحالة الصوت الدائم
// ============================================================================
enum class QuranRiwayah {
    HAFS,
    WARSH
}

enum class QuranIndexTab {
    SURAHS,
    JUZS,
    HIZBS
}

enum class SurahAudioStatus {
    DOWNLOADED,     // مخزّنة في filesDir وجاهزة أوفلاين للأبد
    DOWNLOADING,    // قيد التحميل مع نسبة مئوية
    NOT_DOWNLOADED  // غير محمّلة (تظهر حجم الملف بالميجابايت)
}

enum class QuranReaderTheme {
    WARM_PARCHMENT,
    NIGHT_EMERALD
}

enum class TajweedRuleType(val arabicTitle: String, val explanation: String, val color: Color) {
    MADD("المدّ", "إطالة الصوت بحرف المدّ بمقدار 2 أو 4 أو 6 حركات حسب نوعه.", Color(0xFFC62828)),
    GHUNNAH("الغنة والإخفاء", "صوت لذيذ يخرج من الخيشوم بمقدار حركتين عند النون والميم.", Color(0xFF1B8A5A)),
    QALQALAH("القلقلة", "اضطراب الحرف الساكن (قطب جد) عند النطق به ليُسمع له نبرة.", Color(0xFF1565C0)),
    SILENT("صامت / همزة وصل", "حرف يُكتب ولا يُلفظ عند الوصل مثل همزة الوصل واللام الشمسية.", Color(0xFF8D8D8D))
}

enum class WarshUsulRule(val arabicTitle: String, val explanation: String) {
    BADAL("مدّ البدل", "يمدّ ورش مدّ البدل (مثل: آمَنُوا) بالقصر 2 أو التوسط 4 أو الإشباع 6 حركات."),
    TAQLIL("التقليل / الإمالة", "إمالة الألف نحو الياء في ذوات الياء ورؤوس الآي بنطق منضبط."),
    NAQL("النقل", "نقل حركة الهمزة إلى الساكن الصحيح قبلها وحذف الهمزة (مثل: قَدَ افْلَحَ)."),
    TAGHLIZ_LAM("تغليظ اللام", "تفخيم اللام المفتوحة إذا سبقها صاد أو طاء أو ظاء مفتوحة أو ساكنة."),
    TARQIQ_RA("ترقيق الراء", "ترقيق الراء المفتوحة أو المضمومة إذا سبقتها كسرة لازمة أو ياء ساكنة.")
}

data class SurahAudioItem(
    val number: Int,
    val arabicName: String,
    val localizedName: String,
    val revelationType: String,
    val ayahCount: Int,
    val startPage: Int,
    val audioSizeMb: Float,
    val audioStatus: SurahAudioStatus = SurahAudioStatus.NOT_DOWNLOADED,
    val downloadProgress: Float = 0f
)

data class QuranDivisionItem(
    val number: Int,
    val title: String,
    val subtitle: String,
    val startPage: Int
)

data class MushafAyahUi(
    val ayahNumber: Int,
    val styledText: AnnotatedString
)

data class QuranAudioPlayerState(
    val selectedSurahNumber: Int = 2,
    val selectedSurahName: String = "سورة البقرة",
    val reciterName: String = "مشاري العفاسي",
    val audioStatus: SurahAudioStatus = SurahAudioStatus.NOT_DOWNLOADED,
    val audioSizeMb: Float = 48.2f,
    val downloadProgress: Float = 0f,
    val isPlaying: Boolean = false,
    val currentPositionSec: Int = 6,
    val totalDurationSec: Int = 272,
    val playbackSpeed: Float = 1.0f,
    val isRepeatEnabled: Boolean = false,
    val isSleepTimerActive: Boolean = false
)

data class SakinaQuranStrings(
    val screenTitle: String = "القرآن الكريم",
    val hafsLabel: String = "حفص عن عاصم",
    val warshLabel: String = "ورش عن نافع",
    val hafsShort: String = "حفص",
    val warshShort: String = "ورش",
    val searchPlaceholder: String = "ابحث عن اسم السورة أو رقمها...",
    val lastReadFormat: String = "آخر قراءة: %s · ص %d / %d",
    val resumeLabel: String = "متابعة",
    val tabSurahs: String = "السور (114)",
    val tabJuzs: String = "الأجزاء (30)",
    val tabHizbs: String = "الأحزاب (60)",
    val offlineReadyLabel: String = "أوفلاين ✓",
    val downloadFormat: String = "تحميل · %.1f MB",
    val mbFormat: String = "%.1f MB",
    val tajweedLabel: String = "التجويد",
    val warshUsulLabel: String = "أصول ورش",
    val prevPageLabel: String = "السابقة",
    val nextPageLabel: String = "التالية",
    val pageCounterFormat: String = "%d / %d",
    val backDesc: String = "رجوع",
    val themeToggleDesc: String = "تبديل مظهر الصفحة",
    val speedFormat: String = "%.1fx",
    val skipBackwardLabel: String = "-10s",
    val skipForwardLabel: String = "+10s",
    val longPressDeleteHint: String = "اضغط مطولاً على السورة المحمّلة لحذفها"
)

data class SakinaQuranScreenState(
    val riwayah: QuranRiwayah = QuranRiwayah.HAFS,
    val indexTab: QuranIndexTab = QuranIndexTab.SURAHS,
    val searchQuery: String = "",
    val lastReadSurahName: String = "سورة البقرة",
    val lastReadPage: Int = 2,
    val totalPages: Int = 604,
    val quickSurahs: List<SurahAudioItem> = emptyList(),
    val surahs: List<SurahAudioItem> = emptyList(),
    val juzs: List<QuranDivisionItem> = emptyList(),
    val hizbs: List<QuranDivisionItem> = emptyList(),
    val isReaderOpen: Boolean = false,
    val currentReaderSurah: String = "سورة البقرة",
    val currentReaderPage: Int = 2,
    val currentPageAyahs: List<MushafAyahUi> = emptyList(),
    val isTajweedEnabled: Boolean = true,
    val readerTheme: QuranReaderTheme = QuranReaderTheme.WARM_PARCHMENT,
    val player: QuranAudioPlayerState = QuranAudioPlayerState()
)

// ============================================================================
// 2. شاشة القرآن الكريم الموحدة (بوابة الفهرس + القارئ المضيء + المشغّل التفاعلي)
// ============================================================================
@Composable
fun SakinaLuminousQuranScreen(
    state: SakinaQuranScreenState,
    strings: SakinaQuranStrings = SakinaQuranStrings(),
    dockStrings: SakinaHomeStrings = SakinaHomeStrings(),
    headingFontFamily: FontFamily = FontFamily.Default,
    bodyFontFamily: FontFamily = FontFamily.Default,
    quranFontFamily: FontFamily = FontFamily.Serif,
    selectedTabIndex: Int = 2,
    showBottomDock: Boolean = false,
    hostBottomBarReserve: Dp = 82.dp,
    onReaderActiveChanged: (Boolean) -> Unit = {},
    onRiwayahChange: (QuranRiwayah) -> Unit = {},
    onIndexTabChange: (QuranIndexTab) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onResumeClick: () -> Unit = {},
    onSurahSelected: (SurahAudioItem) -> Unit = {},
    onDivisionSelected: (QuranDivisionItem) -> Unit = {},
    onCloseReader: () -> Unit = {},
    onToggleTajweed: () -> Unit = {},
    onToggleReaderTheme: () -> Unit = {},
    onPageChanged: (Int) -> Unit = {},
    onPlayPauseClick: () -> Unit = {},
    onDownloadSurahClick: (Int) -> Unit = {},
    onDeleteDownloadedSurah: (Int) -> Unit = {},
    onSeekToSec: (Int) -> Unit = {},
    onSkipBySec: (Int) -> Unit = {},
    onCycleSpeedClick: () -> Unit = {},
    onToggleRepeatClick: () -> Unit = {},
    onSleepTimerClick: () -> Unit = {},
    onReciterClick: () -> Unit = {},
    isCurrentPageBookmarked: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    pageAyahsProvider: (Int) -> List<MushafAyahUi> = { state.currentPageAyahs },
    onTabSelected: (Int) -> Unit = {}
) {
    val hostView = LocalView.current
    DisposableEffect(state.isReaderOpen) {
        hostView.keepScreenOn = state.isReaderOpen
        onDispose { hostView.keepScreenOn = false }
    }
    val haptic = LocalHapticFeedback.current
    var isPlayerExpanded by remember { mutableStateOf(false) }
    var readerBarsVisible by remember { mutableStateOf(true) }
    LaunchedEffect(state.isReaderOpen) {
        onReaderActiveChanged(state.isReaderOpen)
        if (state.isReaderOpen) readerBarsVisible = true
    }

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
                .padding(horizontal = if (state.isReaderOpen) 4.dp else 14.dp)
                .padding(
                    top = if (state.isReaderOpen) 4.dp else 46.dp,
                    bottom = if (state.isReaderOpen) 4.dp else if (!showBottomDock) hostBottomBarReserve else 0.dp
                )
        ) {
            if (!state.isReaderOpen) {
                QuranIndexPortalContent(
                    state = state,
                    strings = strings,
                    headingFontFamily = headingFontFamily,
                    bodyFontFamily = bodyFontFamily,
                    modifier = Modifier.weight(1f),
                    onRiwayahChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onRiwayahChange(it)
                    },
                    onIndexTabChange = onIndexTabChange,
                    onSearchQueryChange = onSearchQueryChange,
                    onResumeClick = onResumeClick,
                    onSurahSelected = { surah ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSurahSelected(surah)
                    },
                    onDivisionSelected = onDivisionSelected,
                    onDownloadSurahClick = onDownloadSurahClick,
                    onDeleteDownloadedSurah = { surahNum ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteDownloadedSurah(surahNum)
                    }
                )
            } else {
                QuranMushafReaderContent(
                    state = state,
                    strings = strings,
                    headingFontFamily = headingFontFamily,
                    bodyFontFamily = bodyFontFamily,
                    quranFontFamily = quranFontFamily,
                    modifier = Modifier.weight(1f),
                    onCloseReader = onCloseReader,
                    onRiwayahToggle = {
                        val next = if (state.riwayah == QuranRiwayah.HAFS) QuranRiwayah.WARSH else QuranRiwayah.HAFS
                        onRiwayahChange(next)
                    },
                    onToggleTajweed = onToggleTajweed,
                    onToggleReaderTheme = onToggleReaderTheme,
                    isCurrentPageBookmarked = isCurrentPageBookmarked,
                    onToggleBookmark = onToggleBookmark,
                    barsVisible = readerBarsVisible,
                    onToggleBars = { readerBarsVisible = !readerBarsVisible },
                    pageAyahsProvider = pageAyahsProvider,
                    onPageChanged = onPageChanged
                )
            }

            if (!state.isReaderOpen || readerBarsVisible) {
            Spacer(modifier = Modifier.height(6.dp))

            KineticWaveMiniPlayer(
                player = state.player,
                strings = strings,
                bodyFontFamily = bodyFontFamily,
                onExpandClick = { isPlayerExpanded = true },
                onPlayPauseClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPlayPauseClick()
                },
                onDownloadClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDownloadSurahClick(state.player.selectedSurahNumber)
                }
            )
            }

            if (showBottomDock && !state.isReaderOpen) {
                Spacer(modifier = Modifier.height(8.dp))
                LiquidDynamicDock(
                    selectedIndex = selectedTabIndex,
                    strings = dockStrings,
                    bodyFontFamily = bodyFontFamily,
                    onTabSelected = onTabSelected
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(180))
        ) {
            ExpandedLuxuryAudioOverlay(
                player = state.player,
                bottomReserve = if (!showBottomDock) hostBottomBarReserve else 0.dp,
                strings = strings,
                headingFontFamily = headingFontFamily,
                bodyFontFamily = bodyFontFamily,
                onDismiss = { isPlayerExpanded = false },
                onPlayPauseClick = onPlayPauseClick,
                onDownloadClick = { onDownloadSurahClick(state.player.selectedSurahNumber) },
                onSeekToSec = onSeekToSec,
                onSkipBySec = onSkipBySec,
                onCycleSpeedClick = onCycleSpeedClick,
                onToggleRepeatClick = onToggleRepeatClick,
                onSleepTimerClick = onSleepTimerClick,
                onReciterClick = onReciterClick
            )
        }
    }
}

// ============================================================================
// 3. بوابة فهرس القرآن الكريم (مع حذف السور بالضغط المطوّل)
// ============================================================================
@Composable
private fun QuranIndexPortalContent(
    state: SakinaQuranScreenState,
    strings: SakinaQuranStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    modifier: Modifier = Modifier,
    onRiwayahChange: (QuranRiwayah) -> Unit,
    onIndexTabChange: (QuranIndexTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onResumeClick: () -> Unit,
    onSurahSelected: (SurahAudioItem) -> Unit,
    onDivisionSelected: (QuranDivisionItem) -> Unit,
    onDownloadSurahClick: (Int) -> Unit,
    onDeleteDownloadedSurah: (Int) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = strings.screenTitle,
            style = TextStyle(
                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = headingFontFamily,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(SakinaOrbitalColors.GlassNavy)
                .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                .padding(3.dp)
        ) {
            listOf(
                QuranRiwayah.HAFS to strings.hafsLabel,
                QuranRiwayah.WARSH to strings.warshLabel
            ).forEach { (riwayah, label) ->
                val selected = state.riwayah == riwayah
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(19.dp))
                        .background(if (selected) SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.92f) else Color.Transparent)
                        .clickable { onRiwayahChange(riwayah) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (selected) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = bodyFontFamily
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SakinaOrbitalColors.GlassNavy.copy(alpha = 0.72f))
                .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = SakinaOrbitalColors.ChampagneGold,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (state.searchQuery.isEmpty()) {
                    Text(
                        text = strings.searchPlaceholder,
                        color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.45f),
                        fontSize = 12.sp,
                        fontFamily = bodyFontFamily
                    )
                }
                BasicTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = SakinaOrbitalColors.WarmCream,
                        fontSize = 12.5.sp,
                        fontFamily = bodyFontFamily
                    ),
                    cursorBrush = SolidColor(SakinaOrbitalColors.ChampagneGold),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.searchQuery.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = SakinaOrbitalColors.WarmCream.copy(alpha = 0.65f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onSearchQueryChange("") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .sakinaLuxuryGlassCard(
                    cornerRadius = 18.dp,
                    innerHairline = false,
                    emeraldFlare = true,
                    shimmer = true,
                    borderAlpha = 0.72f
                )
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onResumeClick)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.lastReadFormat.format(
                        state.lastReadSurahName,
                        state.lastReadPage,
                        state.totalPages
                    ),
                    color = SakinaOrbitalColors.WarmCream,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = bodyFontFamily,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = strings.resumeLabel,
                        color = SakinaOrbitalColors.InkNavy,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily
                    )
                }
            }
        }

        if (state.quickSurahs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(state.quickSurahs) { item ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(SakinaOrbitalColors.GlassNavy.copy(alpha = 0.7f))
                            .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.38f), RoundedCornerShape(14.dp))
                            .clickable { onSurahSelected(item) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = item.arabicName,
                            color = SakinaOrbitalColors.SoftGoldGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = bodyFontFamily
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                QuranIndexTab.SURAHS to strings.tabSurahs,
                QuranIndexTab.JUZS to strings.tabJuzs,
                QuranIndexTab.HIZBS to strings.tabHizbs
            ).forEach { (tab, label) ->
                val active = state.indexTab == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onIndexTabChange(tab) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        color = if (active) SakinaOrbitalColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = bodyFontFamily,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(2.dp)
                            .background(
                                if (active) SakinaOrbitalColors.ChampagneGold else Color.Transparent,
                                CircleShape
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .sakinaParchmentGlowCard(
                    cornerRadius = 22.dp,
                    haloAlpha = 0.32f,
                    doubleBorder = false
                )
                .clip(RoundedCornerShape(22.dp))
        ) {
            val filteredSurahs = remember(state.surahs, state.searchQuery) {
                if (state.searchQuery.isBlank()) state.surahs
                else state.surahs.filter {
                    it.arabicName.contains(state.searchQuery, ignoreCase = true) ||
                        it.localizedName.contains(state.searchQuery, ignoreCase = true) ||
                        it.number.toString() == state.searchQuery.trim()
                }
            }

            when (state.indexTab) {
                QuranIndexTab.SURAHS -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(filteredSurahs, key = { _, it -> it.number }) { idx, surah ->
                            SurahIndexRow(
                                surah = surah,
                                strings = strings,
                                headingFontFamily = headingFontFamily,
                                bodyFontFamily = bodyFontFamily,
                                onClick = { onSurahSelected(surah) },
                                onDownloadClick = { onDownloadSurahClick(surah.number) },
                                onLongPressDelete = { onDeleteDownloadedSurah(surah.number) }
                            )
                            if (idx < filteredSurahs.lastIndex) {
                                HorizontalDivider(
                                    color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.22f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
                QuranIndexTab.JUZS, QuranIndexTab.HIZBS -> {
                    val itemsList = if (state.indexTab == QuranIndexTab.JUZS) state.juzs else state.hizbs
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(itemsList) { idx, item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.1.dp, SakinaLuxuryPalette.MetallicGoldButtonBrush, RoundedCornerShape(16.dp))
                                    .clickable { onDivisionSelected(item) }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.bg_parchment_card),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.matchParentSize()
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OctagonalNumberBadge(number = item.number, bodyFontFamily = bodyFontFamily)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.title,
                                                color = SakinaOrbitalColors.InkNavy,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = headingFontFamily
                                            )
                                            Text(
                                                text = item.subtitle,
                                                color = SakinaOrbitalColors.InkNavy.copy(alpha = 0.70f),
                                                fontSize = 11.sp,
                                                fontFamily = bodyFontFamily
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF0E4436), SakinaLuxuryColors.DeepEmerald, Color(0xFF07251D))
                                                )
                                            )
                                            .border(1.1.dp, SakinaLuxuryPalette.MetallicGoldButtonBrush, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "✦ ص ${item.startPage}",
                                            color = SakinaLuxuryColors.SoftGoldGlow,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = bodyFontFamily,
                                            style = TextStyle(fontFeatureSettings = "tnum")
                                        )
                                    }
                                }
                            }
                            if (idx < itemsList.lastIndex) {
                                HorizontalDivider(
                                    color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.22f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SurahIndexRow(
    surah: SurahAudioItem,
    strings: SakinaQuranStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onLongPressDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.1.dp, SakinaLuxuryPalette.MetallicGoldButtonBrush, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (surah.audioStatus == SurahAudioStatus.DOWNLOADED) {
                        onLongPressDelete()
                    }
                }
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_parchment_card),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. يميناً: النجمة المثمنة الذهبية + اسم السورة
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.1f)
            ) {
                OctagonalNumberBadge(number = surah.number, bodyFontFamily = bodyFontFamily)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = surah.arabicName,
                    color = SakinaOrbitalColors.InkNavy,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily,
                    maxLines = 1
                )
            }

            // 2. في الوسط: أيقونة التحميل وحجم الملف مصغّرين وأنيقين
            Box(
                modifier = Modifier.padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                when (surah.audioStatus) {
                    SurahAudioStatus.DOWNLOADED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SakinaOrbitalColors.DeepEmerald.copy(alpha = 0.12f))
                                .border(0.6.dp, SakinaOrbitalColors.DeepEmerald.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = SakinaOrbitalColors.DeepEmerald,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = strings.offlineReadyLabel,
                                color = SakinaOrbitalColors.DeepEmerald,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = bodyFontFamily
                            )
                        }
                    }
                    SurahAudioStatus.DOWNLOADING -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                progress = { surah.downloadProgress.coerceIn(0f, 1f) },
                                color = SakinaOrbitalColors.ChampagneGold,
                                strokeWidth = 1.8.dp,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${(surah.downloadProgress * 100).roundToInt()}%",
                                color = SakinaOrbitalColors.MutedGoldText,
                                fontSize = 8.5.sp,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                    }
                    SurahAudioStatus.NOT_DOWNLOADED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.08f))
                                .border(0.6.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                                .clickable(onClick = onDownloadClick)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Canvas(modifier = Modifier.size(10.dp)) {
                                drawDownloadVector(SakinaOrbitalColors.MutedGoldText)
                            }
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", surah.audioSizeMb)} MB",
                                color = SakinaOrbitalColors.MutedGoldText,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = bodyFontFamily,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                    }
                }
            }

            // 3. يساراً: كرتوشة مزخرفة مذهّبة بنفس خامة النجمة المثمنة (مكية/مدنية · عدد الآيات)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0E4436), SakinaLuxuryColors.DeepEmerald, Color(0xFF07251D))
                        )
                    )
                    .border(1.2.dp, SakinaLuxuryPalette.MetallicGoldButtonBrush, RoundedCornerShape(12.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦ ${surah.revelationType} · ${surah.ayahCount} آية",
                    color = SakinaLuxuryColors.SoftGoldGlow,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = bodyFontFamily,
                    maxLines = 1,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }
    }
}

@Composable
private fun OctagonalNumberBadge(number: Int, bodyFontFamily: FontFamily) {
    SakinaStarMedallionBadge(
        numberText = number.toString(),
        starBgRes = R.drawable.ic_star_medallion_gold,
        size = 40.dp
    )
}

// ============================================================================
// 4. قارئ المصحف المضيء (HorizontalPager + Spine Scrubber + فصل حفص/ورش)
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuranMushafReaderContent(
    state: SakinaQuranScreenState,
    strings: SakinaQuranStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    quranFontFamily: FontFamily,
    modifier: Modifier = Modifier,
    onCloseReader: () -> Unit,
    onRiwayahToggle: () -> Unit,
    onToggleTajweed: () -> Unit,
    onToggleReaderTheme: () -> Unit,
    isCurrentPageBookmarked: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    barsVisible: Boolean = true,
    onToggleBars: () -> Unit = {},
    pageAyahsProvider: (Int) -> List<MushafAyahUi>,
    onPageChanged: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val localBarsVisible = barsVisible
    val isParchment = state.readerTheme == QuranReaderTheme.WARM_PARCHMENT
    val pageBg = if (isParchment) SakinaOrbitalColors.WarmCream else Color(0xFF0A1F1A)
    val pageTextColor = if (isParchment) SakinaOrbitalColors.InkNavy else SakinaOrbitalColors.WarmCream

    var activeRuleExplanation by remember(state.riwayah, state.currentReaderPage) {
        mutableStateOf<Pair<String, String>?>(null)
    }

    val pagerState = rememberPagerState(
        initialPage = (state.currentReaderPage - 1).coerceIn(0, (state.totalPages - 1).coerceAtLeast(0)),
        pageCount = { state.totalPages.coerceAtLeast(1) }
    )

    // v51 #2: التقليب الآلي للصفحات متزامناً مع تقدم المشغّل الصوتي
    LaunchedEffect(state.player.isPlaying, state.player.currentPositionSec, state.player.totalDurationSec, state.player.surahNumber) {
        if (state.player.isPlaying && state.player.totalDurationSec > 10) {
            val sIdx = (state.player.surahNumber - 1).coerceIn(0, state.surahs.lastIndex.coerceAtLeast(0))
            val curSurah = state.surahs.getOrNull(sIdx)
            val nextSurah = state.surahs.getOrNull(sIdx + 1)
            if (curSurah != null) {
                val startP = curSurah.startPage.coerceIn(1, state.totalPages)
                val endP = (nextSurah?.startPage?.minus(1) ?: state.totalPages).coerceIn(startP, state.totalPages)
                val pageSpan = (endP - startP + 1).coerceAtLeast(1)
                if (pageSpan > 1) {
                    val progress = (state.player.currentPositionSec.toFloat() / state.player.totalDurationSec.toFloat()).coerceIn(0f, 0.999f)
                    val targetPage = (startP + (progress * pageSpan).toInt()).coerceIn(startP, endP)
                    val targetIdx = (targetPage - 1).coerceIn(0, (state.totalPages - 1).coerceAtLeast(0))
                    if (pagerState.currentPage != targetIdx) {
                        pagerState.animateScrollToPage(targetIdx)
                    }
                }
            }
        }
    }

    // مزامنة تقليب الإصبع مع الحالة الخارجية والعكس
    LaunchedEffect(pagerState.currentPage) {
        val target = pagerState.currentPage + 1
        if (target != state.currentReaderPage) {
            onPageChanged(target)
        }
    }
    LaunchedEffect(state.currentReaderPage) {
        val targetIdx = (state.currentReaderPage - 1).coerceIn(0, (state.totalPages - 1).coerceAtLeast(0))
        if (pagerState.currentPage != targetIdx) {
            pagerState.animateScrollToPage(targetIdx)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(4.dp))

        // ترويسة عائمة تختفي وتظهر بلمسة وسط الصفحة لخشوع تام
        AnimatedVisibility(visible = localBarsVisible) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onCloseReader) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = strings.backDesc,
                    tint = SakinaOrbitalColors.ChampagneGold
                )
            }

            Text(
                text = "${state.currentReaderSurah} · ص ${state.currentReaderPage}",
                color = SakinaOrbitalColors.WarmCream,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = headingFontFamily,
                style = TextStyle(fontFeatureSettings = "tnum")
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                ReaderTopChip(
                    label = if (state.riwayah == QuranRiwayah.HAFS) strings.hafsShort else strings.warshShort,
                    isActive = true,
                    bodyFontFamily = bodyFontFamily,
                    onClick = onRiwayahToggle
                )
                Spacer(modifier = Modifier.width(5.dp))
                ReaderTopChip(
                    label = if (state.riwayah == QuranRiwayah.HAFS) {
                        if (state.isTajweedEnabled) "${strings.tajweedLabel} ✓" else strings.tajweedLabel
                    } else {
                        strings.warshUsulLabel
                    },
                    isActive = state.riwayah == QuranRiwayah.WARSH || state.isTajweedEnabled,
                    bodyFontFamily = bodyFontFamily,
                    onClick = {
                        if (state.riwayah == QuranRiwayah.HAFS) onToggleTajweed()
                        else activeRuleExplanation = WarshUsulRule.BADAL.arabicTitle to WarshUsulRule.BADAL.explanation
                    }
                )
                Spacer(modifier = Modifier.width(5.dp))
                // زر حفظ المرجعية (🔖) لصفحة المصحف الحالية
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentPageBookmarked) SakinaLuxuryColors.DeepEmerald else SakinaOrbitalColors.GlassNavy)
                        .border(
                            width = if (isCurrentPageBookmarked) 1.1.dp else 0.8.dp,
                            color = if (isCurrentPageBookmarked) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable(onClick = onToggleBookmark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCurrentPageBookmarked) "★" else "☆",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // زر تبديل الثيم المستقل (ورقي دافئ / زمردي ليلي)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SakinaOrbitalColors.GlassNavy)
                        .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onToggleReaderTheme)
                        .semantics { contentDescription = strings.themeToggleDesc },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isParchment) "🌙" else "☀",
                        fontSize = 12.sp
                    )
                }
            }
        }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (isParchment) {
                            Modifier.sakinaParchmentGlowCard(
                                cornerRadius = 22.dp,
                                haloAlpha = 0.36f,
                                doubleBorder = true
                            )
                        } else {
                            Modifier.sakinaLuxuryGlassCard(
                                cornerRadius = 22.dp,
                                innerHairline = true,
                                emeraldFlare = true,
                                shimmer = false,
                                borderAlpha = 0.68f
                            )
                        }
                    )
                    .clip(RoundedCornerShape(22.dp))
            ) {
                // v49 #4: إزالة الإطار المتداخل واستغلال 100% من عرض الشاشة بهوامش آمنة
                Column(modifier = Modifier.fillMaxSize()) {
                    // تقليب الصفحات بالسحب الأفقي الحقيقي (HorizontalPager)
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) { pageIndex ->
                        val ayahsForThisPage = pageAyahsProvider(pageIndex + 1)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onToggleBars() }
                                .padding(
                                    start = 14.dp,
                                    end = 14.dp,
                                    top = 12.dp,
                                    bottom = 10.dp
                                )
                        ) {
                            val combinedAyahs = buildAnnotatedString {
                                ayahsForThisPage.forEach { ayah ->
                                    append(ayah.styledText)
                                    withStyle(
                                        SpanStyle(
                                            color = SakinaOrbitalColors.MutedGoldText,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append(" ﴿${ayah.ayahNumber}﴾ ")
                                    }
                                }
                            }

                            val charCount = combinedAyahs.length
                            val autoFontSp = when {
                                charCount < 280 -> 25.sp
                                charCount < 480 -> 21.sp
                                charCount < 700 -> 18.5.sp
                                charCount < 950 -> 16.5.sp
                                else -> 15.sp
                            }
                            val autoLineSp = when {
                                charCount < 280 -> 44.sp
                                charCount < 480 -> 36.sp
                                charCount < 700 -> 31.sp
                                charCount < 950 -> 27.sp
                                else -> 24.sp
                            }
                            Text(
                                text = combinedAyahs,
                                color = pageTextColor,
                                fontSize = autoFontSp,
                                lineHeight = autoLineSp,
                                fontFamily = quranFontFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // بطاقة شرح الحكم الفوري عند لمس أحد ألوان التجويد أو أصول ورش
                    AnimatedVisibility(visible = activeRuleExplanation != null) {
                        activeRuleExplanation?.let { (title, desc) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SakinaOrbitalColors.DeepEmerald)
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        color = SakinaOrbitalColors.SoftGoldGlow,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = headingFontFamily
                                    )
                                    Text(
                                        text = desc,
                                        color = SakinaOrbitalColors.WarmCream,
                                        fontSize = 10.5.sp,
                                        fontFamily = bodyFontFamily
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = SakinaOrbitalColors.SoftGoldGlow,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { activeRuleExplanation = null }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = localBarsVisible) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                    // الشريط السفلي العلمي المفصول: ألوان تجويد حفص vs شريط أصول ورش التفاعلي
                    if (state.riwayah == QuranRiwayah.HAFS && state.isTajweedEnabled) {
                        HafsTajweedLegendStrip(
                            isParchment = isParchment,
                            bodyFontFamily = bodyFontFamily,
                            onRuleClick = { rule ->
                                activeRuleExplanation = rule.arabicTitle to rule.explanation
                            }
                        )
                    } else if (state.riwayah == QuranRiwayah.WARSH) {
                        WarshUsulInteractiveStrip(
                            isParchment = isParchment,
                            bodyFontFamily = bodyFontFamily,
                            onUsulClick = { rule ->
                                activeRuleExplanation = rule.arabicTitle to rule.explanation
                            }
                        )
                    }

                    // شريط أزرار الصفحات (متزامن مع HorizontalPager)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isParchment) Color(0xFFF0E9D8) else Color(0xFF071713))
                            .padding(horizontal = 12.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "< ${strings.prevPageLabel}",
                            color = if (state.currentReaderPage > 1) SakinaOrbitalColors.MutedGoldText else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = bodyFontFamily,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = state.currentReaderPage > 1) {
                                    scope.launch {
                                        pagerState.animateScrollToPage((state.currentReaderPage - 2).coerceAtLeast(0))
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        Text(
                            text = strings.pageCounterFormat.format(state.currentReaderPage, state.totalPages),
                            color = pageTextColor,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = bodyFontFamily,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )

                        Text(
                            text = "${strings.nextPageLabel} >",
                            color = if (state.currentReaderPage < state.totalPages) SakinaOrbitalColors.MutedGoldText else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = bodyFontFamily,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = state.currentReaderPage < state.totalPages) {
                                    scope.launch {
                                        pagerState.animateScrollToPage((state.currentReaderPage).coerceAtMost(state.totalPages - 1))
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // v51: removed VerticalSpineScrubber for 100% centered Mushaf
        }
    }
}

@Composable
private fun ReaderTopChip(
    label: String,
    isActive: Boolean,
    bodyFontFamily: FontFamily,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isActive) SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.90f)
                else SakinaLuxuryColors.MidnightNavy.copy(alpha = 0.78f)
            )
            .border(
                width = if (isActive) 1.1.dp else 0.8.dp,
                color = if (isActive) SakinaLuxuryColors.SoftGoldGlow else SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.35f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (isActive) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.80f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = bodyFontFamily
        )
    }
}

@Composable
private fun HafsTajweedLegendStrip(
    isParchment: Boolean,
    bodyFontFamily: FontFamily,
    onRuleClick: (TajweedRuleType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isParchment) Color(0xFFF5EFE0) else Color(0xFF0C2620))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TajweedRuleType.entries.forEach { rule ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onRuleClick(rule) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(rule.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = rule.arabicTitle,
                    color = if (isParchment) SakinaOrbitalColors.InkNavy.copy(alpha = 0.82f) else SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontFamily = bodyFontFamily
                )
            }
        }
    }
}

@Composable
private fun WarshUsulInteractiveStrip(
    isParchment: Boolean,
    bodyFontFamily: FontFamily,
    onUsulClick: (WarshUsulRule) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isParchment) Color(0xFFF5EFE0) else Color(0xFF0C2620))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        items(WarshUsulRule.entries) { rule ->
            Text(
                text = rule.arabicTitle,
                color = if (isParchment) SakinaOrbitalColors.DeepEmerald else SakinaOrbitalColors.SoftGoldGlow,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = bodyFontFamily,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.16f))
                    .clickable { onUsulClick(rule) }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun VerticalSpineScrubber(
    currentPage: Int,
    totalPages: Int,
    onPageChanged: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .width(18.dp)
            .fillMaxHeight(0.84f)
            .pointerInput(totalPages) {
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    val fraction = (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                    val target = (1 + (fraction * (totalPages - 1)).roundToInt()).coerceIn(1, totalPages)
                    onPageChanged(target)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val fraction = ((currentPage - 1).toFloat() / (totalPages - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width / 2f
            val topY = 8.dp.toPx()
            val botY = size.height - 8.dp.toPx()
            drawLine(
                color = SakinaLuxuryPalette.GoldRich.copy(alpha = 0.25f),
                start = Offset(x, topY),
                end = Offset(x, botY),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(SakinaLuxuryPalette.GoldDeep, SakinaLuxuryPalette.GoldSpecular, SakinaLuxuryPalette.GoldDeep)
                ),
                start = Offset(x, topY),
                end = Offset(x, botY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            val ticks = 28
            for (i in 0..ticks) {
                val ty = topY + (i.toFloat() / ticks) * (botY - topY)
                val tw = if (i % 4 == 0) 4.5.dp.toPx() else 2.2.dp.toPx()
                drawLine(
                    color = SakinaLuxuryPalette.GoldBright.copy(alpha = 0.45f),
                    start = Offset(x + 2.dp.toPx(), ty),
                    end = Offset(x + 2.dp.toPx() + tw, ty),
                    strokeWidth = 0.8.dp.toPx()
                )
            }
            val thumbY = topY + fraction * (botY - topY)
            drawGlowingLuxuryOrb(
                center = Offset(x, thumbY),
                coreRadiusPx = 5.dp.toPx(),
                coreColor = SakinaLuxuryPalette.GoldSpecular,
                haloColor = SakinaLuxuryPalette.GoldBright,
                haloScale = 2.6f,
                intensity = 1.0f
            )
        }
    }
}

// ============================================================================
// 5. المشغّل الصوتي الموجي الفاخر (Mini-Player + البطاقة الموسّعة التفاعلية)
// ============================================================================
@Composable
private fun KineticWaveMiniPlayer(
    player: QuranAudioPlayerState,
    strings: SakinaQuranStrings,
    bodyFontFamily: FontFamily,
    onExpandClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wavePhase"
    )

    val progressFraction = when {
        player.audioStatus == SurahAudioStatus.DOWNLOADING -> player.downloadProgress.coerceIn(0f, 1f)
        player.totalDurationSec > 0 -> (player.currentPositionSec.toFloat() / player.totalDurationSec).coerceIn(0f, 1f)
        else -> 0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(30.dp))
            .clickable(onClick = onExpandClick)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_luxury_audio_player_capsule),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(progressFraction)
                    .height(2.5.dp)
                    .background(SakinaOrbitalColors.SoftGoldGlow)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable {
                            when (player.audioStatus) {
                                SurahAudioStatus.DOWNLOADED -> onPlayPauseClick()
                                SurahAudioStatus.NOT_DOWNLOADED -> onDownloadClick()
                                SurahAudioStatus.DOWNLOADING -> Unit
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_gold_medallion_btn),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    when (player.audioStatus) {
                        SurahAudioStatus.DOWNLOADING -> {
                            CircularProgressIndicator(
                                progress = { player.downloadProgress.coerceIn(0f, 1f) },
                                color = SakinaLuxuryColors.SoftGoldGlow,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        SurahAudioStatus.NOT_DOWNLOADED -> {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                drawDownloadVector(SakinaLuxuryColors.SoftGoldGlow)
                            }
                        }
                        SurahAudioStatus.DOWNLOADED -> {
                            if (player.isPlaying) {
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    drawPauseVector(SakinaLuxuryColors.SoftGoldGlow)
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = SakinaLuxuryColors.SoftGoldGlow,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = player.selectedSurahName,
                        color = SakinaOrbitalColors.SoftGoldGlow,
                        fontSize = 13.5.sp,
                        lineHeight = 16.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subtitle = when (player.audioStatus) {
                        SurahAudioStatus.DOWNLOADED -> "${player.reciterName} · ${strings.offlineReadyLabel}"
                        SurahAudioStatus.DOWNLOADING -> "${player.reciterName} · ${(player.downloadProgress * 100).roundToInt()}%"
                        SurahAudioStatus.NOT_DOWNLOADED -> "${player.reciterName} · ${strings.downloadFormat.format(player.audioSizeMb)}"
                    }
                    Text(
                        text = subtitle,
                        color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontFamily = bodyFontFamily,
                        style = TextStyle(fontFeatureSettings = "tnum", platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Canvas(
                    modifier = Modifier
                        .width(42.dp)
                        .height(22.dp)
                ) {
                    val bars = 7
                    val barWidth = 2.5.dp.toPx()
                    val gap = (size.width - bars * barWidth) / (bars - 1)
                    val heights = listOf(0.35f, 0.75f, 0.50f, 1.0f, 0.60f, 0.85f, 0.40f)
                    for (i in 0 until bars) {
                        val animFactor = if (player.isPlaying) {
                            0.35f + 0.65f * ((heights[i] + wavePhase * (if (i % 2 == 0) 0.4f else -0.3f)).coerceIn(0.2f, 1f))
                        } else {
                            heights[i] * 0.45f
                        }
                        val barH = size.height * animFactor
                        val x = i * (barWidth + gap)
                        val y = (size.height - barH) / 2f
                        drawRoundRect(
                            color = SakinaOrbitalColors.SoftGoldGlow.copy(alpha = if (player.isPlaying) 0.95f else 0.45f),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedLuxuryAudioOverlay(
    player: QuranAudioPlayerState,
    bottomReserve: Dp = 0.dp,
    strings: SakinaQuranStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onDismiss: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onSeekToSec: (Int) -> Unit,
    onSkipBySec: (Int) -> Unit,
    onCycleSpeedClick: () -> Unit,
    onToggleRepeatClick: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onReciterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.58f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomReserve, start = 10.dp, end = 10.dp)
                .sakinaLuxuryGlassCard(
                    cornerRadius = 28.dp,
                    innerHairline = true,
                    emeraldFlare = true,
                    shimmer = true,
                    borderAlpha = 0.84f
                )
                .clip(RoundedCornerShape(28.dp))
                .clickable(enabled = false, onClick = {})
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .background(SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.7f), CircleShape)
                        .clickable(onClick = onDismiss)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "◆ ${player.selectedSurahName} ◆",
                    color = SakinaOrbitalColors.SoftGoldGlow,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = player.reciterName,
                    color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontFamily = bodyFontFamily,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onReciterClick)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // شريط التمرير الذهبي التفاعلي (اللمس + السحب)
                val progress = if (player.totalDurationSec > 0) {
                    (player.currentPositionSec.toFloat() / player.totalDurationSec).coerceIn(0f, 1f)
                } else 0f

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .pointerInput(player.totalDurationSec) {
                            detectTapGestures { offset ->
                                if (player.totalDurationSec > 0) {
                                    val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    onSeekToSec((fraction * player.totalDurationSec).roundToInt())
                                }
                            }
                        }
                        .pointerInput(player.totalDurationSec) {
                            detectHorizontalDragGestures { change, _ ->
                                change.consume()
                                if (player.totalDurationSec > 0) {
                                    val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    onSeekToSec((fraction * player.totalDurationSec).roundToInt())
                                }
                            }
                        }
                ) {
                    val y = size.height / 2f
                    drawLine(
                        color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.25f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    val activeX = size.width * progress
                    drawLine(
                        color = SakinaOrbitalColors.SoftGoldGlow,
                        start = Offset(0f, y),
                        end = Offset(activeX, y),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    val d = 6.5.dp.toPx()
                    val diamond = Path().apply {
                        moveTo(activeX, y - d)
                        lineTo(activeX + d, y)
                        lineTo(activeX, y + d)
                        lineTo(activeX - d, y)
                        close()
                    }
                    drawPath(diamond, color = SakinaOrbitalColors.SoftGoldGlow)
                }

                // عدادا الوقت بخط رقمي ثابت العرض (tnum)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDurationSec(player.currentPositionSec),
                        color = SakinaOrbitalColors.SoftGoldGlow,
                        fontSize = 11.sp,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                    Text(
                        text = formatDurationSec(player.totalDurationSec),
                        color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.72f),
                        fontSize = 11.sp,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // أزرار التحكم المحورية (-10s · Play/Pause · +10s)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    Text(
                        text = strings.skipBackwardLabel,
                        color = SakinaOrbitalColors.SoftGoldGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.45f), CircleShape)
                            .clickable { onSkipBySec(-10) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )

                    SakinaGoldMedallionButton(
                        medallionBgRes = R.drawable.ic_gold_medallion_btn,
                        onClick = {
                            when (player.audioStatus) {
                                SurahAudioStatus.DOWNLOADED -> onPlayPauseClick()
                                SurahAudioStatus.NOT_DOWNLOADED -> onDownloadClick()
                                SurahAudioStatus.DOWNLOADING -> Unit
                            }
                        },
                        size = 68.dp
                    ) {
                        when (player.audioStatus) {
                            SurahAudioStatus.DOWNLOADING -> CircularProgressIndicator(
                                progress = { player.downloadProgress.coerceIn(0f, 1f) },
                                color = SakinaLuxuryColors.SoftGoldGlow,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(30.dp)
                            )
                            SurahAudioStatus.NOT_DOWNLOADED -> Canvas(modifier = Modifier.size(24.dp)) {
                                drawDownloadVector(SakinaLuxuryColors.SoftGoldGlow)
                            }
                            SurahAudioStatus.DOWNLOADED -> {
                                if (player.isPlaying) {
                                    Canvas(modifier = Modifier.size(22.dp)) { drawPauseVector(SakinaLuxuryColors.SoftGoldGlow) }
                                } else {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = SakinaLuxuryColors.SoftGoldGlow, modifier = Modifier.size(34.dp))
                                }
                            }
                        }
                    }

                    Text(
                        text = strings.skipForwardLabel,
                        color = SakinaOrbitalColors.SoftGoldGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.45f), CircleShape)
                            .clickable { onSkipBySec(10) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.speedFormat.format(player.playbackSpeed),
                        color = SakinaOrbitalColors.SoftGoldGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onCycleSpeedClick)
                            .padding(8.dp)
                    )
                    Text(
                        text = if (player.isSleepTimerActive) "🌙 ON" else "🌙",
                        color = if (player.isSleepTimerActive) SakinaOrbitalColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onSleepTimerClick)
                            .padding(8.dp)
                    )
                    Text(
                        text = if (player.isRepeatEnabled) "↻ ON" else "↻",
                        color = if (player.isRepeatEnabled) SakinaOrbitalColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.6f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onToggleRepeatClick)
                            .padding(8.dp)
                    )
                    Text(
                        text = if (player.audioStatus == SurahAudioStatus.DOWNLOADED) strings.offlineReadyLabel else strings.mbFormat.format(player.audioSizeMb),
                        color = SakinaOrbitalColors.SoftGoldGlow,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
            }
        }
    }
}

private fun formatDurationSec(totalSec: Int): String {
    val safe = totalSec.coerceAtLeast(0)
    val mins = safe / 60
    val secs = safe % 60
    return "%02d:%02d".format(mins, secs)
}

// ============================================================================
// 6. أيقونات متجهة خفيفة مدمجة
// ============================================================================
private fun DrawScope.drawDownloadVector(tint: Color) {
    val stroke = 1.8.dp.toPx()
    val w = size.width
    val h = size.height
    drawLine(tint, Offset(w * 0.5f, h * 0.14f), Offset(w * 0.5f, h * 0.68f), strokeWidth = stroke, cap = StrokeCap.Round)
    drawLine(tint, Offset(w * 0.28f, h * 0.48f), Offset(w * 0.5f, h * 0.70f), strokeWidth = stroke, cap = StrokeCap.Round)
    drawLine(tint, Offset(w * 0.72f, h * 0.48f), Offset(w * 0.5f, h * 0.70f), strokeWidth = stroke, cap = StrokeCap.Round)
    drawLine(tint, Offset(w * 0.20f, h * 0.86f), Offset(w * 0.80f, h * 0.86f), strokeWidth = stroke, cap = StrokeCap.Round)
}

private fun DrawScope.drawPauseVector(tint: Color) {
    val barW = size.width * 0.26f
    drawRoundRect(tint, Offset(size.width * 0.18f, 0f), Size(barW, size.height), CornerRadius(2.dp.toPx()))
    drawRoundRect(tint, Offset(size.width * 0.56f, 0f), Size(barW, size.height), CornerRadius(2.dp.toPx()))
}
