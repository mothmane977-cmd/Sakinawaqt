package app.sakina

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// ============================================================================
// 1. نماذج بيانات الإعدادات والتشخيص الحي (Settings & Diagnostics Models)
// ============================================================================
enum class SettingsModuleId {
    APPEARANCE,
    PRAYER_CALC,
    ADHAN_ALERTS,
    BACKUP_PRIVACY
}

enum class SakinaSettingsThemeOption(val arabicName: String) {
    ROYAL_BLUE("الأزرق الملكي"),
    HARAM_EMERALD("الزمردي الحرمي"),
    AMOLED_BLACK("الأسود AMOLED"),
    ANDALUSI_BURGUNDY("العنّابي الأندلسي"),
    MAKKI_AMBER("الكهرماني المكي")
}

data class ReliabilityDiagnosticsState(
    val notificationsGranted: Boolean = true,
    val exactAlarmsGranted: Boolean = true,
    val batteryUnrestricted: Boolean = true,
    val scheduledAlarmsCount: Int = 69,
    val nextAlarmLabel: String = "الظهر 12:43:00",
    val lastDiagTimestamp: String = "09:56:35"
) {
    val readinessPercent: Int
        get() {
            val score = (if (notificationsGranted) 34 else 0) +
                (if (exactAlarmsGranted) 33 else 0) +
                (if (batteryUnrestricted) 33 else 0)
            return score
        }

    val isFullyReady: Boolean
        get() = readinessPercent == 100
}

data class SakinaSettingsStrings(
    val screenTitle: String = "الإعدادات العامة",
    val backDesc: String = "رجوع",
    val diagReadyTitle: String = "الأذان مضمون 100%",
    val diagNeedsActionTitle: String = "تنبيهات الأذان تحتاج ضبطاً",
    val diagScheduledFormat: String = "✓ %d تنبيهاً مجدولاً · التالي: %s",
    val diagFixActionLabel: String = "فحص البطارية والصلاحيات ›",
    val moduleAppearanceTitle: String = "المظهر والخطوط",
    val modulePrayerTitle: String = "المواقيت والموقع والفلك",
    val moduleAdhanTitle: String = "الأذان والتنبيهات الذكية",
    val moduleBackupTitle: String = "النسخ الاحتياطي والخصوصية",
    val fontScaleLabel: String = "حجم الخط العام",
    val autoNightLabel: String = "الوضع الليلي التلقائي (ليلاً)",
    val autoGpsLabel: String = "تحديد الموقع تلقائياً (GPS)",
    val easternDigitsLabel: String = "أرقام عربية مشرقية (١٢:٣٤)",
    val maghribiNamesLabel: String = "أسماء الصلوات المغاربية (الصبح / Sobh)",
    val hanafiAsrLabel: String = "المذهب الحنفي لصلاة العصر",
    val fajrAlarmLabel: String = "1. منبه الفجر المشدّد (تدرّج صوتي + غفوة)",
    val adhanAlarmLabel: String = "2. أذان الصلوات المفروضة الكامل",
    val qiyamAlarmLabel: String = "3. منبه قيام الليل والسحور (قبل الفجر بـ 45 د)",
    val iqamaAlarmLabel: String = "4. منبه الإقامة (بعد الأذان بـ 15 د)",
    val kahfFridayLabel: String = "تذكير سورة الكهف صباح الجمعة",
    val fastingReminderLabel: String = "تذكير صيام التطوع والأيام البيض",
    val testAdhanLabel: String = "اختبار الأذان خلال 10 ث",
    val exportBackupLabel: String = "نسخ الإعدادات",
    val restoreBackupLabel: String = "استعادة الإعدادات",
    val shareAppLabel: String = "شارك تطبيق سَكِينة",
    val rateAppLabel: String = "قيّم سَكِينة على Google Play",
    val hijriOffsetLabel: String = "تعديل التاريخ الهجري",
    val defaultMuezzinLabel: String = "المؤذن الافتراضي",
    val versionFormat: String = "الإصدار %s · سَكِينة (أوفلاين 100%% بدون إعلانات)"
)

data class SakinaSettingsScreenState(
    val diagnostics: ReliabilityDiagnosticsState = ReliabilityDiagnosticsState(),
    val selectedTheme: SakinaSettingsThemeOption = SakinaSettingsThemeOption.ROYAL_BLUE,
    val selectedFontName: String = "الأميري النسخ",
    val fontScalePercent: Int = 99,
    val autoNightMode: Boolean = false,
    val cityName: String = "فرندة",
    val calculationMethodName: String = "الجزائر الرسمية 18°/17°",
    val autoGpsEnabled: Boolean = true,
    val useEasternDigits: Boolean = false,
    val useMaghribiNames: Boolean = false,
    val useHanafiAsr: Boolean = false,
    val hijriOffsetDays: Int = 0,
    val muezzinName: String = "الشيخ عبد المجيد السريحي",
    val fajrWakeupEnabled: Boolean = true,
    val fullAdhanEnabled: Boolean = true,
    val qiyamAlertEnabled: Boolean = false,
    val iqamaAlertEnabled: Boolean = false,
    val kahfFridayEnabled: Boolean = true,
    val fastingReminderEnabled: Boolean = false,
    val appVersion: String = "v2.0"
)

// ============================================================================
// 2. شاشة الإعدادات العامة والتشخيص الحي (شاشة كاملة بدون شريط تنقل سفلي)
// ============================================================================
@Composable
fun SakinaControlSettingsScreen(
    state: SakinaSettingsScreenState,
    strings: SakinaSettingsStrings = SakinaSettingsStrings(),
    headingFontFamily: FontFamily = FontFamily.Default,
    bodyFontFamily: FontFamily = FontFamily.Default,
    onBackClick: () -> Unit = {},
    onOpenDiagnosticsDialog: () -> Unit = {},
    onThemeSelected: (SakinaSettingsThemeOption) -> Unit = {},
    onFontScaleChanged: (Int) -> Unit = {},
    onAutoNightChanged: (Boolean) -> Unit = {},
    onAutoGpsChanged: (Boolean) -> Unit = {},
    onEasternDigitsChanged: (Boolean) -> Unit = {},
    onMaghribiNamesChanged: (Boolean) -> Unit = {},
    onHanafiAsrChanged: (Boolean) -> Unit = {},
    onHijriOffsetChanged: (Int) -> Unit = {},
    onMuezzinClick: () -> Unit = {},
    onFajrWakeupChanged: (Boolean) -> Unit = {},
    onFullAdhanChanged: (Boolean) -> Unit = {},
    onQiyamAlertChanged: (Boolean) -> Unit = {},
    onIqamaAlertChanged: (Boolean) -> Unit = {},
    onKahfFridayChanged: (Boolean) -> Unit = {},
    onFastingReminderChanged: (Boolean) -> Unit = {},
    onTestAdhanClick: () -> Unit = {},
    onExportBackupClick: () -> Unit = {},
    onRestoreBackupClick: () -> Unit = {},
    onShareAppClick: () -> Unit = {},
    onRateAppClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var expandedModule by remember { mutableStateOf<SettingsModuleId?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SakinaLuxuryPalette.ObsidianNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SakinaAmbientLuxuryBackdrop(showGeometricPattern = true, showStarDust = false)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // 1. ترويسة نظيفة بزر رجوع واحد فقط (بدون شريط تنقل سفلي)
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = strings.backDesc,
                        tint = SakinaOrbitalColors.ChampagneGold
                    )
                }

                Text(
                    text = strings.screenTitle,
                    color = SakinaOrbitalColors.WarmCream,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 2. بطاقة نبض الموثوقية الحيّة
                LiveReliabilityPulseCard(
                    diagnostics = state.diagnostics,
                    strings = strings,
                    headingFontFamily = headingFontFamily,
                    bodyFontFamily = bodyFontFamily,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenDiagnosticsDialog()
                    }
                )

                // 3. وحدة المظهر والخطوط
                ExpandableControlModule(
                    title = strings.moduleAppearanceTitle,
                    summary = "${state.selectedTheme.arabicName} · ${state.selectedFontName} · ${state.fontScalePercent}%",
                    isExpanded = expandedModule == SettingsModuleId.APPEARANCE,
                    headingFontFamily = headingFontFamily,
                    bodyFontFamily = bodyFontFamily,
                    iconDraw = { drawPaletteVector(SakinaOrbitalColors.ChampagneGold) },
                    onHeaderClick = {
                        expandedModule = if (expandedModule == SettingsModuleId.APPEARANCE) null else SettingsModuleId.APPEARANCE
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // اختيار الثيمات الفاخرة
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SakinaSettingsThemeOption.entries.forEach { theme ->
                                val active = theme == state.selectedTheme
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (active) SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.18f) else Color.Transparent)
                                        .clickable { onThemeSelected(theme) }
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = theme.arabicName,
                                        color = if (active) SakinaOrbitalColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream,
                                        fontSize = 12.5.sp,
                                        fontFamily = bodyFontFamily
                                    )
                                    if (active) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = SakinaOrbitalColors.SoftGoldGlow,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.18f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.fontScaleLabel,
                                color = SakinaOrbitalColors.WarmCream,
                                fontSize = 12.5.sp,
                                fontFamily = bodyFontFamily
                            )
                            Text(
                                text = "${state.fontScalePercent}%",
                                color = SakinaOrbitalColors.SoftGoldGlow,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                        Slider(
                            value = state.fontScalePercent.toFloat(),
                            onValueChange = { onFontScaleChanged(it.roundToInt()) },
                            valueRange = 85f..125f,
                            colors = SliderDefaults.colors(
                                thumbColor = SakinaOrbitalColors.SoftGoldGlow,
                                activeTrackColor = SakinaOrbitalColors.ChampagneGold,
                                inactiveTrackColor = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.25f)
                            )
                        )

                        SettingsSwitchRow(
                            label = strings.autoNightLabel,
                            checked = state.autoNightMode,
                            bodyFontFamily = bodyFontFamily,
                            onCheckedChange = onAutoNightChanged
                        )
                    }
                }

                // 4. وحدة المواقيت والفلك
                ExpandableControlModule(
                    title = strings.modulePrayerTitle,
                    summary = "${state.cityName} · ${state.calculationMethodName}",
                    isExpanded = expandedModule == SettingsModuleId.PRAYER_CALC,
                    headingFontFamily = headingFontFamily,
                    bodyFontFamily = bodyFontFamily,
                    iconDraw = { drawCompassSettingsVector(SakinaOrbitalColors.ChampagneGold) },
                    onHeaderClick = {
                        expandedModule = if (expandedModule == SettingsModuleId.PRAYER_CALC) null else SettingsModuleId.PRAYER_CALC
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsSwitchRow(strings.autoGpsLabel, state.autoGpsEnabled, bodyFontFamily, onAutoGpsChanged)
                        SettingsSwitchRow(strings.easternDigitsLabel, state.useEasternDigits, bodyFontFamily, onEasternDigitsChanged)
                        SettingsSwitchRow(strings.maghribiNamesLabel, state.useMaghribiNames, bodyFontFamily, onMaghribiNamesChanged)
                        SettingsSwitchRow(strings.hanafiAsrLabel, state.useHanafiAsr, bodyFontFamily, onHanafiAsrChanged)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.hijriOffsetLabel,
                                color = SakinaOrbitalColors.WarmCream,
                                fontSize = 12.5.sp,
                                fontFamily = bodyFontFamily
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(-2, -1, 0, 1, 2).forEach { offset ->
                                    val active = state.hijriOffsetDays == offset
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (active) SakinaOrbitalColors.ChampagneGold else SakinaOrbitalColors.DeepSpaceNavy)
                                            .clickable { onHijriOffsetChanged(offset) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (offset > 0) "+$offset" else offset.toString(),
                                            color = if (active) SakinaOrbitalColors.InkNavy else SakinaOrbitalColors.WarmCream,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            style = TextStyle(fontFeatureSettings = "tnum")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. وحدة الأذان والتنبيهات الذكية (ترقيم متسلسل 1، 2، 3، 4 دون قصّ)
                ExpandableControlModule(
                    title = strings.moduleAdhanTitle,
                    summary = "${state.muezzinName} · الفجر ${if (state.fajrWakeupEnabled) "ON" else "OFF"}",
                    isExpanded = expandedModule == SettingsModuleId.ADHAN_ALERTS,
                    headingFontFamily = headingFontFamily,
                    bodyFontFamily = bodyFontFamily,
                    iconDraw = { drawBellWaveVector(SakinaOrbitalColors.ChampagneGold) },
                    onHeaderClick = {
                        expandedModule = if (expandedModule == SettingsModuleId.ADHAN_ALERTS) null else SettingsModuleId.ADHAN_ALERTS
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SakinaOrbitalColors.DeepSpaceNavy.copy(alpha = 0.6f))
                                .clickable(onClick = onMuezzinClick)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(strings.defaultMuezzinLabel, color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.7f), fontSize = 12.sp, fontFamily = bodyFontFamily)
                            Text(state.muezzinName, color = SakinaOrbitalColors.SoftGoldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = bodyFontFamily)
                        }

                        SettingsSwitchRow(strings.fajrAlarmLabel, state.fajrWakeupEnabled, bodyFontFamily, onFajrWakeupChanged)
                        SettingsSwitchRow(strings.adhanAlarmLabel, state.fullAdhanEnabled, bodyFontFamily, onFullAdhanChanged)
                        SettingsSwitchRow(strings.qiyamAlarmLabel, state.qiyamAlertEnabled, bodyFontFamily, onQiyamAlertChanged)
                        SettingsSwitchRow(strings.iqamaAlarmLabel, state.iqamaAlertEnabled, bodyFontFamily, onIqamaAlertChanged)

                        HorizontalDivider(color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.18f))

                        SettingsSwitchRow(strings.kahfFridayLabel, state.kahfFridayEnabled, bodyFontFamily, onKahfFridayChanged)
                        SettingsSwitchRow(strings.fastingReminderLabel, state.fastingReminderEnabled, bodyFontFamily, onFastingReminderChanged)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SakinaOrbitalColors.ChampagneGold)
                                .clickable(onClick = onTestAdhanClick)
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.testAdhanLabel,
                                color = SakinaOrbitalColors.InkNavy,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = bodyFontFamily
                            )
                        }
                    }
                }

                // 6. وحدة النسخ الاحتياطي والخصوصية
                ExpandableControlModule(
                    title = strings.moduleBackupTitle,
                    summary = strings.versionFormat.format(state.appVersion),
                    isExpanded = expandedModule == SettingsModuleId.BACKUP_PRIVACY,
                    headingFontFamily = headingFontFamily,
                    bodyFontFamily = bodyFontFamily,
                    iconDraw = { drawShieldCheckVector(SakinaOrbitalColors.ChampagneGold) },
                    onHeaderClick = {
                        expandedModule = if (expandedModule == SettingsModuleId.BACKUP_PRIVACY) null else SettingsModuleId.BACKUP_PRIVACY
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable(onClick = onExportBackupClick)
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(strings.exportBackupLabel, color = SakinaOrbitalColors.SoftGoldGlow, fontSize = 12.sp, fontFamily = bodyFontFamily)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable(onClick = onRestoreBackupClick)
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(strings.restoreBackupLabel, color = SakinaOrbitalColors.SoftGoldGlow, fontSize = 12.sp, fontFamily = bodyFontFamily)
                            }
                        }

                        ActionRowItem(strings.shareAppLabel, bodyFontFamily, onShareAppClick)
                        ActionRowItem(strings.rateAppLabel, bodyFontFamily, onRateAppClick)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

// ============================================================================
// 3. المكونات الفرعية
// ============================================================================

@Composable
private fun LiveReliabilityPulseCard(
    diagnostics: ReliabilityDiagnosticsState,
    strings: SakinaSettingsStrings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onClick: () -> Unit
) {
    val progressAnim by animateFloatAsState(
        targetValue = diagnostics.readinessPercent / 100f,
        animationSpec = tween(700),
        label = "diagProgress"
    )
    val accent = if (diagnostics.isFullyReady) Color(0xFF34D399) else Color(0xFFFBBF24)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .sakinaLuxuryGlassCard(
                cornerRadius = 22.dp,
                innerHairline = false,
                emeraldFlare = true,
                shimmer = true,
                borderAlpha = 0.72f
            )
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (diagnostics.isFullyReady) strings.diagReadyTitle else strings.diagNeedsActionTitle,
                    color = SakinaOrbitalColors.WarmCream,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = strings.diagScheduledFormat.format(
                        diagnostics.scheduledAlarmsCount,
                        diagnostics.nextAlarmLabel
                    ),
                    color = SakinaOrbitalColors.SoftGoldGlow,
                    fontSize = 11.5.sp,
                    fontFamily = bodyFontFamily,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.diagFixActionLabel,
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = bodyFontFamily
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 5.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.28f), Color.Transparent)
                        ),
                        radius = size.minDimension * 0.56f
                    )
                    drawCircle(
                        color = accent.copy(alpha = 0.22f),
                        style = Stroke(stroke)
                    )
                    drawArc(
                        color = accent.copy(alpha = 0.30f),
                        startAngle = -90f,
                        sweepAngle = 360f * progressAnim,
                        useCenter = false,
                        style = Stroke(stroke * 2.4f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = accent,
                        startAngle = -90f,
                        sweepAngle = 360f * progressAnim,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${diagnostics.readinessPercent}%",
                    color = accent,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }
    }
}

@Composable
private fun ExpandableControlModule(
    title: String,
    summary: String,
    isExpanded: Boolean,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    iconDraw: DrawScope.() -> Unit,
    onHeaderClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .sakinaLuxuryGlassCard(
                cornerRadius = 18.dp,
                innerHairline = true,
                emeraldFlare = true,
                shimmer = isExpanded,
                borderAlpha = if (isExpanded) 0.85f else 0.58f
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHeaderClick)
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        iconDraw()
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            color = SakinaOrbitalColors.WarmCream,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = headingFontFamily
                        )
                        Text(
                            text = summary,
                            color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.82f),
                            fontSize = 11.sp,
                            fontFamily = bodyFontFamily,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }

                Text(
                    text = if (isExpanded) "⌃" else "⌄",
                    color = SakinaOrbitalColors.SoftGoldGlow,
                    fontSize = 18.sp
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                ) {
                    HorizontalDivider(color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    bodyFontFamily: FontFamily,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = SakinaOrbitalColors.WarmCream,
            fontSize = 12.5.sp,
            fontFamily = bodyFontFamily,
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SakinaOrbitalColors.InkNavy,
                checkedTrackColor = SakinaOrbitalColors.ChampagneGold,
                uncheckedThumbColor = SakinaOrbitalColors.WarmCream.copy(alpha = 0.7f),
                uncheckedTrackColor = SakinaOrbitalColors.DeepSpaceNavy
            )
        )
    }
}

@Composable
private fun ActionRowItem(label: String, bodyFontFamily: FontFamily, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SakinaOrbitalColors.DeepSpaceNavy.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = SakinaOrbitalColors.WarmCream, fontSize = 12.5.sp, fontFamily = bodyFontFamily)
        Text("‹", color = SakinaOrbitalColors.ChampagneGold, fontSize = 16.sp)
    }
}

// ============================================================================
// 4. أيقونات متجهة خفيفة مدمجة
// ============================================================================
private fun DrawScope.drawPaletteVector(tint: Color) {
    val s = 1.6.dp.toPx()
    drawCircle(tint, radius = size.minDimension * 0.42f, style = Stroke(s))
    drawCircle(tint, radius = 2.dp.toPx(), center = Offset(size.width * 0.36f, size.height * 0.4f))
    drawCircle(tint, radius = 2.dp.toPx(), center = Offset(size.width * 0.62f, size.height * 0.38f))
    drawCircle(tint, radius = 2.dp.toPx(), center = Offset(size.width * 0.52f, size.height * 0.64f))
}

private fun DrawScope.drawCompassSettingsVector(tint: Color) {
    val s = 1.6.dp.toPx()
    drawCircle(tint, radius = size.minDimension * 0.42f, style = Stroke(s))
    drawLine(tint, Offset(size.width * 0.5f, size.height * 0.22f), Offset(size.width * 0.5f, size.height * 0.52f), s, StrokeCap.Round)
    drawLine(tint, Offset(size.width * 0.5f, size.height * 0.52f), Offset(size.width * 0.72f, size.height * 0.66f), s, StrokeCap.Round)
}

private fun DrawScope.drawBellWaveVector(tint: Color) {
    val s = 1.6.dp.toPx()
    drawArc(tint, 180f, 180f, false, Offset(size.width * 0.22f, size.height * 0.18f), Size(size.width * 0.56f, size.height * 0.65f), style = Stroke(s))
    drawLine(tint, Offset(size.width * 0.15f, size.height * 0.72f), Offset(size.width * 0.85f, size.height * 0.72f), s, StrokeCap.Round)
    drawCircle(tint, radius = 2.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.84f))
}

private fun DrawScope.drawShieldCheckVector(tint: Color) {
    val s = 1.6.dp.toPx()
    drawCircle(tint, radius = size.minDimension * 0.42f, style = Stroke(s))
    drawLine(tint, Offset(size.width * 0.32f, size.height * 0.52f), Offset(size.width * 0.46f, size.height * 0.66f), s, StrokeCap.Round)
    drawLine(tint, Offset(size.width * 0.46f, size.height * 0.66f), Offset(size.width * 0.70f, size.height * 0.36f), s, StrokeCap.Round)
}
