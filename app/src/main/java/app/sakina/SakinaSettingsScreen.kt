package app.sakina

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.TimeZone
import kotlin.math.roundToInt

// Uses SakinaCitiesDb.ALL_CITIES (45+ offline cities)

@Composable
fun SakinaSettingsScreen(
    ctx: Context,
    selectedTheme: SakinaLuxuryTheme,
    selectedLang: SakinaLang,
    onThemeChange: (SakinaLuxuryTheme) -> Unit,
    onLangChange: (SakinaLang) -> Unit,
    onBackClick: () -> Unit,
    appearanceOnly: Boolean = false,
    onAppearanceChanged: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        SakinaAdhanAudio.stop()
        onBackClick()
    }
    DisposableEffect(Unit) {
        onDispose { SakinaAdhanAudio.stop() }
    }

    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    val isArOrUr = lang == SakinaLang.AR || lang == SakinaLang.UR
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    // 4 Organized Sub-Tabs
    var activeSubTab by remember(appearanceOnly) { mutableIntStateOf(if (appearanceOnly) 0 else 1) }
    var appFontStyle by remember { mutableIntStateOf(SakinaAdvancedPrefs.appFontStyle(ctx)) }
    var appFontScale by remember { mutableFloatStateOf(SakinaAdvancedPrefs.readingFontScale(ctx)) }
    var appTextColorIdx by remember { mutableIntStateOf(SakinaAdvancedPrefs.appTextColorIndex(ctx)) }
    var mushafThemeIdx by remember { mutableIntStateOf(SakinaAdvancedPrefs.mushafTheme(ctx)) }
    val subTabLabels = remember(lang) {
        when (lang) {
            SakinaLang.AR -> listOf("المظهر والوقت", "حساب المواقيت", "الأذان والصامت", "المشاركة وحول")
            SakinaLang.FR -> listOf("Apparence", "Calcul", "Adhan & Silence", "Partage")
            SakinaLang.TR -> listOf("Görünüm", "Hesaplama", "Ezan & Sessiz", "Paylaş")
            SakinaLang.ID -> listOf("Tampilan", "Hisab", "Adzan & Senyap", "Bagikan")
            SakinaLang.UR -> listOf("ظاہری شکل", "اوقات حساب", "اذان و خاموش", "شیئر")
            else -> listOf("Appearance", "Calculation", "Adhan & Silent", "Share & About")
        }
    }

    var refreshTick by remember { mutableIntStateOf(0) }
    var showCityPicker by remember { mutableStateOf(false) }
    var showVoicePicker by remember { mutableStateOf(false) }
    var showManualOffsets by remember { mutableStateOf(false) }
    var showPerPrayerMuezzin by remember { mutableStateOf(false) }

    var autoLocation by remember { mutableStateOf(sp.getBoolean("auto_gps_location", true)) }
    var autoNight by remember { mutableStateOf(SakinaAdvancedPrefs.autoNightMode(ctx)) }
    var timeFmtMode by remember { mutableIntStateOf(SakinaAdvancedPrefs.timeFormatMode(ctx)) }
    var dstMode by remember { mutableIntStateOf(SakinaAdvancedPrefs.dstMode(ctx)) }
    var hijriOff by remember { mutableIntStateOf(Prefs.hijriOffset(ctx)) }

    var isHanafi by remember { mutableStateOf(Prefs.hanafi(ctx)) }
    var highLatRule by remember { mutableIntStateOf(SakinaAdvancedPrefs.highLatRule(ctx)) }

    var selectedVoice by remember { mutableStateOf(SakinaAdhanAudio.selectedVoice(ctx)) }
    var playingVoiceKey by remember { mutableStateOf<String?>(null) }
    var malikiAdhan by remember { mutableStateOf(SakinaAdvancedPrefs.malikiAdhan(ctx)) }
    var persistentOn by remember { mutableStateOf(SakinaAdvancedPrefs.persistentNotifEnabled(ctx)) }
    var persistentWin by remember { mutableIntStateOf(SakinaAdvancedPrefs.persistentNotifWindow(ctx)) }
    var volMode by remember { mutableIntStateOf(SakinaAdvancedPrefs.volumeControlMode(ctx)) }
    var vibType by remember { mutableIntStateOf(SakinaAdvancedPrefs.vibrationType(ctx)) }
    var vibWithAdhan by remember { mutableStateOf(SakinaAdvancedPrefs.vibrateWithAdhan(ctx)) }
    var wakeScreen by remember { mutableStateOf(SakinaAdvancedPrefs.wakeScreenOnAdhan(ctx)) }
    var flipSilence by remember { mutableStateOf(SakinaAdvancedPrefs.flipToSilence(ctx)) }
    var autoSilent by remember { mutableStateOf(SakinaAdvancedPrefs.autoSilentEnabled(ctx)) }
    var autoSilentVib by remember { mutableStateOf(SakinaAdvancedPrefs.autoSilentVibrate(ctx)) }
    var silentAlertMode by remember { mutableIntStateOf(SakinaAdvancedPrefs.autoSilentTransitionAlert(ctx)) }
    var silentDurMin by remember { mutableIntStateOf(SakinaAdvancedPrefs.autoSilentDurationMin(ctx)) }
    var fridaySilentMin by remember { mutableIntStateOf(SakinaAdvancedPrefs.fridaySilentDurationMin(ctx)) }
    var postPrayerNotif by remember { mutableStateOf(SakinaAdvancedPrefs.postPrayerAdhkarNotif(ctx)) }
    var customReminders by remember { mutableStateOf(SakinaAdvancedPrefs.customReminders(ctx)) }

    var calDays by remember { mutableIntStateOf(SakinaAdvancedPrefs.calendarSyncDays(ctx)) }
    var calBusy by remember { mutableStateOf(SakinaAdvancedPrefs.calendarSyncBusy(ctx)) }
    var calRem by remember { mutableStateOf(SakinaAdvancedPrefs.calendarSyncReminder(ctx)) }

    val placeName = remember(refreshTick, lang) { localizeCityName(Place.name(ctx), lang) }
    val methodShort = remember(refreshTick, lang) { localizeMethodName(Prefs.METHODS[Prefs.method(ctx)], lang) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 56.dp, bottom = 74.dp)
    ) {
        // Pinned Header below Mihrab arch (Row layout prevents Back button overlap in all languages)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SakinaBackIconButton(onClick = { SakinaAdhanAudio.stop(); onBackClick() })
            Spacer(Modifier.width(8.dp))
            SakinaOrnamentCartouche(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (appearanceOnly) sakinaTr(lang, "المظهر والخطوط", "Appearance & Fonts", "Apparence et polices", "Görünüm ve Yazı Tipi", "Tampilan & Font", "ظاہری شکل اور فونٹس") else sakinaTr(lang, "الإعدادات العامة", "General Settings", "Paramètres généraux", "Genel Ayarlar", "Pengaturan Umum", "عمومی ترتیبات"),
                    style = TextStyle(
                        fontFamily = SakinaEditorialFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        brush = SakinaCalligraphyBrush()
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // 4 Organized Sub-Tab Pills (hidden in dedicated Appearance & Fonts mode)
        if (!appearanceOnly) Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(theme.bgCard)
                .border(0.9.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(17.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            subTabLabels.forEachIndexed { idx, label ->
                val sel = activeSubTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (sel) Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold))
                            else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable { activeSubTab = idx },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = SakinaBodyFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Scrollable Sub-Tab Body (keeps top cartouche & sub-tab bar safely below Mihrab arch!)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            when (activeSubTab) {
                // =========================================================
                // TAB 0: Appearance, Themes, Location, Timezone & Hijri
                // =========================================================
                0 -> {
                        // v19 Point 8: Dedicated Font Style, Font Size, Text Color & Mushaf Theme Studio
                        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), glowGold = true) {
                            Text(
                                text = sakinaTr(lang, "تخصيص الخط وحجمه ولونه", "Typography, Size & Color", "Police, taille et couleur", "Yazı Tipi, Boyut ve Renk", "Font, Ukuran & Warna", "فونٹ، سائز اور رنگ"),
                                fontFamily = SakinaHeadingFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = theme.accentBright
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = sakinaTr(lang, "نوع الخط", "Font Family", "Style de police", "Yazı Tipi", "Jenis Font", "فونٹ کی قسم"),
                                fontFamily = SakinaBodyFont,
                                fontSize = 11.5.sp,
                                color = SakinaBrandColors.SoftSilver
                            )
                            Spacer(Modifier.height(4.dp))
                            SettingSegmentedRow(
                                labels = listOf(sakinaTr(lang, "الأميري النسخ", "Amiri", "Amiri", "Amiri", "Amiri", "امیری"), sakinaTr(lang, "رقعة مزخرف", "Ornate", "Calligraphie", "Hat", "Kaligrafi", "رقعہ"), sakinaTr(lang, "خط حديث", "Modern", "Moderne", "Modern", "Modern", "جدید")),
                                selectedIdx = appFontStyle,
                                onSelect = { idx ->
                                    appFontStyle = idx
                                    SakinaAdvancedPrefs.setAppFontStyle(ctx, idx)
                                    onAppearanceChanged()
                                }
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sakinaTr(lang, "حجم الخط العام", "App Font Size", "Taille de la police", "Yazı Boyutu", "Ukuran Font", "فونٹ سائز"),
                                    fontFamily = SakinaBodyFont,
                                    fontSize = 11.5.sp,
                                    color = SakinaBrandColors.SoftSilver
                                )
                                Text(
                                    text = "${(appFontScale * 100).toInt()}%".toWesternDigits(),
                                    fontFamily = SakinaNumeralFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = theme.accentBright
                                )
                            }
                            Slider(
                                value = appFontScale,
                                onValueChange = { v ->
                                    appFontScale = v
                                    SakinaAdvancedPrefs.setReadingFontScale(ctx, v)
                                    onAppearanceChanged()
                                },
                                valueRange = 0.85f..1.35f,
                                colors = SliderDefaults.colors(
                                    thumbColor = theme.accentBright,
                                    activeTrackColor = theme.accentGold,
                                    inactiveTrackColor = Color(0x33FFFFFF)
                                )
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = sakinaTr(lang, "لون النصوص", "Text Color", "Couleur du texte", "Metin Rengi", "Warna Teks", "متن کا رنگ"),
                                fontFamily = SakinaBodyFont,
                                fontSize = 11.5.sp,
                                color = SakinaBrandColors.SoftSilver
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SakinaTextColorPresets.forEachIndexed { idx, (label, swatchColor) ->
                                    val sel = appTextColorIdx == idx
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (sel) Color(0x33D4AF37) else Color(0x1AFFFFFF))
                                            .border(if (sel) 1.3.dp else 0.6.dp, if (sel) theme.accentBright else Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                                            .clickable {
                                                appTextColorIdx = idx
                                                SakinaAdvancedPrefs.setAppTextColorIndex(ctx, idx)
                                                onAppearanceChanged()
                                            }
                                            .padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(swatchColor)
                                                .border(1.dp, Color(0x88000000), CircleShape)
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            text = label.substringBefore(" "),
                                            fontFamily = SakinaBodyFont,
                                            fontSize = 9.sp,
                                            color = swatchColor,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = sakinaTr(lang, "ثيم قراءة المصحف", "Mushaf Reading Theme", "Thème de lecture du Coran", "Mushaf Okuma Teması", "Tema Baca Mushaf", "مصحف مطالعہ تھیم"),
                                fontFamily = SakinaBodyFont,
                                fontSize = 11.5.sp,
                                color = SakinaBrandColors.SoftSilver
                            )
                            Spacer(Modifier.height(4.dp))
                            SettingSegmentedRow(
                                labels = listOf(sakinaTr(lang, "ورقي دافئ", "Warm", "Papier", "Sıcak", "Hangat", "کاغذی"), sakinaTr(lang, "عادي", "White", "Clair", "Beyaz", "Terang", "سفید"), sakinaTr(lang, "ليلي", "Dark", "Sombre", "Gece", "Gelap", "تاریک")),
                                selectedIdx = mushafThemeIdx,
                                onSelect = { idx ->
                                    mushafThemeIdx = idx
                                    SakinaAdvancedPrefs.setMushafTheme(ctx, idx)
                                }
                            )
                        }

                    SakinaGlassCard(modifier = Modifier.fillMaxWidth(), glowGold = true) {
                        Text(s.themeTitle.toWesternDigits(), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
                        Spacer(Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            SakinaLuxuryTheme.entries.forEachIndexed { idx, t ->
                                val sel = t == selectedTheme
                                val localizedThemeName = s.themeNames.getOrElse(idx) { t.titleAr }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (sel) t.accentGold.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                                        .border(0.8.dp, if (sel) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)), RoundedCornerShape(10.dp))
                                        .clickable { onThemeChange(t) }
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(Modifier.size(13.dp).clip(RoundedCornerShape(4.dp)).background(t.bgDeep).border(1.dp, t.accentGold, RoundedCornerShape(4.dp)))
                                        Spacer(Modifier.width(8.dp))
                                        Text(localizedThemeName, fontFamily = SakinaBodyFont, fontSize = 12.sp, color = if (sel) t.accentBright else SakinaBrandColors.LuminousIvory, maxLines = 1)
                                    }
                                    if (sel) Text("✓ ${s.activeWord}", fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = t.accentBright)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        SettingSwitchRow(
                            title = sakinaTr(lang, "الوضع الليلي التلقائي (ليلاً)", "Auto Night Mode (AMOLED)", "Mode nuit automatique (AMOLED)", "Otomatik Gece Modu", "Mode Malam Otomatis", "خودکار نائٹ موڈ"),
                            checked = autoNight,
                            onCheckedChange = { autoNight = it; SakinaAdvancedPrefs.setAutoNightMode(ctx, it) }
                        )
                    }

                    // Location Card
                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        SettingSwitchRow(
                            title = "${s.locationLabel}: $placeName",
                            checked = autoLocation,
                            onCheckedChange = {
                                autoLocation = it
                                sp.edit().putBoolean("auto_gps_location", it).apply()
                                if (it) {
                                    val loc = Place.fromDevice(ctx)
                                    if (loc != null) {
                                        var resolved = "GPS"
                                        try {
                                            @Suppress("DEPRECATION")
                                            val list = Geocoder(ctx, lang.locale).getFromLocation(loc.latitude, loc.longitude, 1)
                                            val city = list?.firstOrNull()?.locality ?: list?.firstOrNull()?.subAdminArea
                                            if (!city.isNullOrBlank()) resolved = city
                                        } catch (_: Exception) {}
                                        Place.set(ctx, loc.latitude, loc.longitude, resolved)
                                        AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); refreshTick++
                                    }
                                }
                            }
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(10.dp)).background(theme.accentGold.copy(alpha = 0.22f)).border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(10.dp)).clickable {
                                    val loc = Place.fromDevice(ctx)
                                    if (loc != null) Place.set(ctx, loc.latitude, loc.longitude, "GPS") else Place.reset(ctx)
                                    AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); refreshTick++
                                },
                                contentAlignment = Alignment.Center
                            ) { Text(s.autoGps, fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = theme.accentBright) }
                            Box(
                                modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(10.dp)).background(Color(0x22FFFFFF)).border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(10.dp)).clickable { showCityPicker = true },
                                contentAlignment = Alignment.Center
                            ) { Text(s.manualPick, fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = SakinaBrandColors.LuminousIvory) }
                        }
                    }

                    // Timezone, 12h/24h & Hijri Adjustment (-2..+2)
                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        val tz = TimeZone.getDefault()
                        val offsetHrs = tz.rawOffset / 3_600_000
                        Text(
                            text = "${sakinaTr(lang, "المنطقة الزمنية", "Timezone", "Fuseau horaire", "Saat Dilimi", "Zona Waktu", "ٹائم زون")}: ${tz.id} (UTC${if (offsetHrs >= 0) "+$offsetHrs" else "$offsetHrs"})".toWesternDigits(),
                            fontFamily = SakinaBodyFont,
                            fontSize = 11.5.sp,
                            color = SakinaBrandColors.SoftSilver
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(sakinaTr(lang, "نظام عرض الأوقات", "Time Format", "Format de l'heure", "Saat Biçimi", "Format Waktu", "وقت کا فارمیٹ"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = theme.accentBright)
                        Spacer(Modifier.height(4.dp))
                        val fmtLabels = listOf(sakinaTr(lang, "تلقائي", "Auto", "Auto", "Oto", "Oto", "خودکار"), sakinaTr(lang, "12 ساعة", "12h", "12h", "12s", "12j", "12 گھنٹے"), sakinaTr(lang, "24 ساعة", "24h", "24h", "24s", "24j", "24 گھنٹے"))
                        SettingSegmentedRow(labels = fmtLabels, selectedIdx = timeFmtMode) {
                            timeFmtMode = it; SakinaAdvancedPrefs.setTimeFormatMode(ctx, it)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(sakinaTr(lang, "التوقيت الصيفي (DST)", "Daylight Saving", "Heure d'été (DST)", "Yaz Saati (DST)", "Waktu Musim Panas", "ڈے لائٹ سیونگ"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = theme.accentBright)
                        Spacer(Modifier.height(4.dp))
                        val dstLabels = listOf(sakinaTr(lang, "تلقائي", "Auto", "Auto", "Oto", "Oto", "خودکار"), sakinaTr(lang, "مفعّل", "On", "Activé", "Açık", "Aktif", "آن"), sakinaTr(lang, "معطّل", "Off", "Désactivé", "Kapalı", "Mati", "آف"))
                        SettingSegmentedRow(labels = dstLabels, selectedIdx = dstMode) {
                            dstMode = it; SakinaAdvancedPrefs.setDstMode(ctx, it)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("${s.hijriAdjLabel}: ${if (hijriOff > 0) "+$hijriOff" else "$hijriOff"}".toWesternDigits(), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = theme.accentBright)
                        Spacer(Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            listOf(-2, -1, 0, 1, 2).forEach { step ->
                                val sel = hijriOff == step
                                Box(
                                    modifier = Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(8.dp)).background(if (sel) theme.accentGold else Color(0x22FFFFFF)).clickable { hijriOff = step; Prefs.setHijriOffset(ctx, step) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${if (step > 0) "+$step" else "$step"}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory)
                                }
                            }
                        }
                    }
                }

                // =========================================================
                // TAB 1: Prayer Calculation, Madhhab, High Latitudes & Manual Offsets (-120..+120)
                // =========================================================
                1 -> {
                    SakinaSettingsPill(R.drawable.ic_star_ornament, s.calcMethod, methodShort) {
                        Prefs.cycleMethod(ctx); AlarmScheduler.scheduleNext(ctx); refreshTick++
                    }

                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(sakinaTr(lang, "المذهب الفقهي لحساب العصر", "Asr Juristic Method", "Méthode juridique (Asr)", "İkindi Mezhep Hesabı", "Mazhab Waktu Ashar", "عصر کا فقہی مسلک"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.accentBright)
                        Spacer(Modifier.height(6.dp))
                        val madhhabLabels = listOf(sakinaTr(lang, "الجمهور (مالكي / شافعي)", "Standard (Maliki/Shafi'i)", "Standard (Malikite/Chafiite)", "Standart (Şafii/Maliki)", "Standar (Syafi'i/Maliki)", "جمہور (مالکی/شافعی)"), sakinaTr(lang, "المذهب الحنفي", "Hanafi", "Hanafite", "Hanefi", "Hanafi", "حنفی"))
                        SettingSegmentedRow(labels = madhhabLabels, selectedIdx = if (isHanafi) 1 else 0) {
                            isHanafi = it == 1
                            Prefs.setHanafi(ctx, isHanafi)
                            AlarmScheduler.scheduleNext(ctx)
                        }
                    }

                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(sakinaTr(lang, "تعديل خطوط العرض العليا", "High Latitude Adjustment", "Ajustement hautes latitudes", "Yüksek Enlem Ayarı", "Penyesuaian Lintang Tinggi", "بلند عرض البلد ایڈجسٹمنٹ"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.accentBright)
                        Spacer(Modifier.height(6.dp))
                        val hlOptions = listOf(sakinaTr(lang, "بدون تعديل", "None", "Aucun", "Yok", "Tidak Ada", "کوئی نہیں"), sakinaTr(lang, "منتصف الليل", "Midnight", "Milieu de la nuit", "Gece Yarısı", "Tengah Malam", "نصف شب"), sakinaTr(lang, "سُبع الليل", "1/7 Night", "Septième de la nuit", "Gecenin 1/7'si", "Sepertujuh Malam", "رات کا ساتواں حصہ"), sakinaTr(lang, "القائمة على الزاوية", "Angle-Based", "Basé sur l'angle", "Açı Tabanlı", "Berbasis Sudut", "زاویہ کی بنیاد پر"))
                        hlOptions.forEachIndexed { idx, opt ->
                            val sel = highLatRule == idx
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (sel) theme.accentGold.copy(alpha = 0.22f) else Color(0x16FFFFFF))
                                    .clickable {
                                        highLatRule = idx
                                        SakinaAdvancedPrefs.setHighLatRule(ctx, idx)
                                        AlarmScheduler.scheduleNext(ctx)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(opt, fontFamily = SakinaBodyFont, fontSize = 12.sp, color = if (sel) theme.accentBright else SakinaBrandColors.LuminousIvory)
                                if (sel) Text("✓", fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, color = theme.accentBright)
                            }
                            if (idx < hlOptions.lastIndex) Spacer(Modifier.height(4.dp))
                        }
                    }

                    SakinaSettingsPill(
                        iconRes = R.drawable.ic_mosque,
                        title = sakinaTr(lang, "تعديل أوقات الصلاة يدوياً", "Manual Prayer Offsets", "Ajustement manuel des horaires", "Manuel Vakit Ayarı", "Penyesuaian Waktu Manual", "اوقات کی دستی تبدیلی"),
                        valueText = sakinaTr(lang, "±120 دقيقة", "±120 min", "±120 min", "±120 dk", "±120 mnt", "±120 منٹ")
                    ) { showManualOffsets = true }
                }

                // =========================================================
                // TAB 2: Adhan Voice, Maliki Adhan, Persistent Notif, Volume/Vib, Auto-Silent & Custom Reminders
                // =========================================================
                2 -> {
                    SakinaSettingsPill(R.drawable.ic_alarm, s.adhanVoiceLabel, selectedVoice.localizedTitle(lang)) {
                        showVoicePicker = true
                    }
                    SakinaSettingsPill(
                        R.drawable.ic_mosque,
                        sakinaTr(lang, "تخصيص مؤذن لكل صلاة", "Muezzin per Prayer", "Muezzin par prière", "Namaza Göre Müezzin", "Muadzin per Sholat", "ہر نماز کے لیے مؤذن"),
                        sakinaTr(lang, "الصلوات الخمس", "5 Prayers", "5 prières", "5 Vakit", "5 Sholat", "5 نمازیں")
                    ) {
                        showPerPrayerMuezzin = true
                    }

                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        SettingSwitchRow(
                            title = sakinaTr(lang, "أذان على المذهب المالكي (تكبيرتان)", "Maliki Adhan (2 Takbirs)", "Adhan malikite (2 Takbirs)", "Maliki Ezanı (2 Tekbir)", "Adzan Maliki (2 Takbir)", "مالکی اذان (2 تکبیریں)"),
                            checked = malikiAdhan,
                            onCheckedChange = { malikiAdhan = it; SakinaAdvancedPrefs.setMalikiAdhan(ctx, it) }
                        )
                        HorizontalDivider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 6.dp))
                        SettingSwitchRow(
                            title = sakinaTr(lang, "الإشعار المستمر في شريط الإشعارات", "Persistent Notification", "Notification permanente", "Kalıcı Bildirim", "Notifikasi Permanen", "مستقل نوٹیفکیشن"),
                            checked = persistentOn,
                            onCheckedChange = {
                                persistentOn = it
                                SakinaAdvancedPrefs.setPersistentNotifEnabled(ctx, it)
                                SakinaLiveNotifier.update(ctx)
                            }
                        )
                        if (persistentOn) {
                            Spacer(Modifier.height(5.dp))
                            val winLabels = listOf(sakinaTr(lang, "دائم", "Always", "Toujours", "Daima", "Selalu", "ہمیشہ"), "30m ±", "45m ±")
                            SettingSegmentedRow(labels = winLabels, selectedIdx = persistentWin) {
                                persistentWin = it
                                SakinaAdvancedPrefs.setPersistentNotifWindow(ctx, it)
                                SakinaLiveNotifier.update(ctx)
                            }
                        }
                    }

                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(sakinaTr(lang, "مصدر مستوى صوت الأذان", "Adhan Volume Source", "Source du volume de l'Adhan", "Ezan Ses Kaynağı", "Sumber Volume Adzan", "اذان کی آواز کا ذریعہ"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = theme.accentBright)
                        Spacer(Modifier.height(4.dp))
                        val volLabels = listOf(sakinaTr(lang, "صوت الرنين", "Ringtone", "Sonnerie", "Zil Sesi", "Nada Dering", "رنگ ٹون"), sakinaTr(lang, "صوت الميديا", "Media", "Média", "Medya", "Media", "میڈیا"), sakinaTr(lang, "مخصص", "Custom", "Personnalisé", "Özel", "Kustom", "مخصوص"))
                        SettingSegmentedRow(labels = volLabels, selectedIdx = volMode) {
                            volMode = it; SakinaAdvancedPrefs.setVolumeControlMode(ctx, it)
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(sakinaTr(lang, "نوع الاهتزاز", "Vibration Pattern", "Type de vibration", "Titreşim Deseni", "Pola Getaran", "وائبریشن انداز"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = theme.accentBright)
                        Spacer(Modifier.height(4.dp))
                        val vibLabels = listOf(sakinaTr(lang, "قصير", "Short", "Court", "Kısa", "Pendek", "مختصر"), sakinaTr(lang, "متوسط", "Medium", "Moyen", "Orta", "Sedang", "درمیانہ"), sakinaTr(lang, "طويل", "Long", "Long", "Uzun", "Panjang", "طویل"), sakinaTr(lang, "طويل جداً", "V. Long", "Très long", "Çok Uzun", "Sangat Lama", "بہت طویل"))
                        SettingSegmentedRow(labels = vibLabels, selectedIdx = vibType) {
                            vibType = it; SakinaAdvancedPrefs.setVibrationType(ctx, it)
                        }
                        Spacer(Modifier.height(6.dp))
                        SettingSwitchRow(sakinaTr(lang, "اهتزاز مصاحب للأذان", "Vibrate with Adhan", "Vibrer avec l'Adhan", "Ezanla Titreşim", "Getar Saat Adzan", "اذان کے ساتھ وائبریشن"), vibWithAdhan) {
                            vibWithAdhan = it; SakinaAdvancedPrefs.setVibrateWithAdhan(ctx, it)
                        }
                        Spacer(Modifier.height(5.dp))
                        SettingSwitchRow(sakinaTr(lang, "تشغيل الشاشة أثناء الأذان", "Wake Screen During Adhan", "Allumer l'écran pendant l'Adhan", "Ezan Sırasında Ekranı Aç", "Nyalakan Layar Saat Adzan", "اذان کے دوران اسکرین آن کریں"), wakeScreen) {
                            wakeScreen = it; SakinaAdvancedPrefs.setWakeScreenOnAdhan(ctx, it)
                        }
                        Spacer(Modifier.height(5.dp))
                        SettingSwitchRow(sakinaTr(lang, "إيقاف الأذان عند قلب الجهاز", "Flip Phone to Silence Adhan", "Retourner pour couper l'Adhan", "Ezanı Susturmak İçin Çevir", "Balik HP untuk Hentikan Adzan", "فون الٹ کر اذان بند کریں"), flipSilence) {
                            flipSilence = it; SakinaAdvancedPrefs.setFlipToSilence(ctx, it)
                        }
                    }

                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        SettingSwitchRow(sakinaTr(lang, "تفعيل الوضع الصامت أثناء الصلاة", "Auto Silent Mode During Prayer", "Mode silencieux auto en prière", "Namazda Otomatik Sessiz Mod", "Mode Senyap Otomatis Saat Sholat", "نماز کے دوران خودکار سائلنٹ"), autoSilent) {
                            autoSilent = it; SakinaAdvancedPrefs.setAutoSilentEnabled(ctx, it)
                        }
                        if (autoSilent) {
                            Spacer(Modifier.height(5.dp))
                            SettingSwitchRow(sakinaTr(lang, "اهتزاز أثناء الوضع الصامت", "Vibrate in Silent Mode", "Vibrer en mode silencieux", "Sessiz Modda Titreşim", "Getar di Mode Senyap", "سائلنٹ موڈ میں وائبریشن"), autoSilentVib) {
                                autoSilentVib = it; SakinaAdvancedPrefs.setAutoSilentVibrate(ctx, it)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(sakinaTr(lang, "التنبيه عند التحويل للوضع الصامت", "Alert on Silent Transition", "Alerte au passage en silencieux", "Sessize Geçiş Uyarısı", "Peringatan Mode Senyap", "سائلنٹ تبدیلی پر اطلاع"), fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = theme.accentBright)
                            Spacer(Modifier.height(4.dp))
                            val trLabels = listOf(sakinaTr(lang, "بدون", "None", "Aucun", "Yok", "Tidak", "کوئی نہیں"), sakinaTr(lang, "اهتزاز", "Vibrate", "Vibreur", "Titreşim", "Getar", "وائبریٹ"), sakinaTr(lang, "نغمة", "Tone", "Son", "Ton", "Nada", "ٹون"), sakinaTr(lang, "نغمة+اهتزاز", "Both", "Les deux", "İkisi", "Keduanya", "دونوں"))
                            SettingSegmentedRow(labels = trLabels, selectedIdx = silentAlertMode) {
                                silentAlertMode = it; SakinaAdvancedPrefs.setAutoSilentTransitionAlert(ctx, it)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(sakinaTr(lang, "مدة الصامت (الأيام العادية / الجمعة)", "Silent Duration (Regular / Friday)", "Durée silencieuse (Normale / Vendredi)", "Sessiz Süresi (Normal / Cuma)", "Durasi Senyap (Biasa / Jumat)", "سائلنٹ مدت (عام / جمعہ)"), fontFamily = SakinaBodyFont, fontSize = 11.sp, color = theme.accentBright)
                            Spacer(Modifier.height(4.dp))
                            val durOpts = listOf(15, 20, 30, 45)
                            SettingSegmentedRow(
                                labels = durOpts.map { "$it ${s.minuteShort}".toWesternDigits() },
                                selectedIdx = durOpts.indexOf(silentDurMin).coerceAtLeast(1)
                            ) {
                                silentDurMin = durOpts[it]; SakinaAdvancedPrefs.setAutoSilentDurationMin(ctx, silentDurMin)
                            }
                            Spacer(Modifier.height(4.dp))
                            val friOpts = listOf(30, 45, 60)
                            SettingSegmentedRow(
                                labels = friOpts.map { "${sakinaTr(lang, "الجمعة", "Fri", "Ven", "Cuma", "Jum", "جمعہ")} $it${s.minuteShort}".toWesternDigits() },
                                selectedIdx = friOpts.indexOf(fridaySilentMin).coerceAtLeast(1)
                            ) {
                                fridaySilentMin = friOpts[it]; SakinaAdvancedPrefs.setFridaySilentDurationMin(ctx, fridaySilentMin)
                            }
                        }
                    }
                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        SettingSwitchRow(
                            title = sakinaTr(lang, "تذكير أذكار ما بعد الصلاة (بعد الأذان بـ 12د)", "Post-Prayer Adhkar Reminder (+12m)", "Rappel Adhkar après la prière (+12m)", "Namaz Sonrası Zikir (+12 dk)", "Pengingat Dzikir (+12 mnt)", "نماز کے بعد اذکار (+12 منٹ)"),
                            checked = postPrayerNotif,
                            onCheckedChange = { postPrayerNotif = it; SakinaAdvancedPrefs.setPostPrayerAdhkarNotif(ctx, it) }
                        )
                    }

                    // Custom Prayer Reminders
                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(sakinaTr(lang, "تنبيهات مخصصة (التحضر / الإقامة / التهجد)", "Custom Prayer Reminders", "Rappels de prière personnalisés", "Özel Namaz Hatırlatıcıları", "Pengingat Sholat Kustom", "مخصوص نماز یاد دہانیاں"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = theme.accentBright)
                        Spacer(Modifier.height(5.dp))
                        customReminders.forEachIndexed { idx, rem ->
                            SettingSwitchRow("${rem.title} (${if (rem.offsetMinutes > 0) "+${rem.offsetMinutes}" else "${rem.offsetMinutes}"} د)".toWesternDigits(), rem.enabled) { checked ->
                                val updated = customReminders.toMutableList()
                                updated[idx] = rem.copy(enabled = checked)
                                customReminders = updated
                                SakinaAdvancedPrefs.saveCustomReminders(ctx, updated)
                            }
                            if (idx < customReminders.lastIndex) Spacer(Modifier.height(4.dp))
                        }
                    }
                }

                // =========================================================
                // TAB 3: Google Calendar Sync, Social Sharing & About/Contact
                // =========================================================
                3 -> {
                    SakinaGlassCard(modifier = Modifier.fillMaxWidth(), glowGold = true) {
                        Text(sakinaTr(lang, "حفظ أوقات الصلاة على تقويم جوجل", "Save Prayer Times to Google Calendar", "Enregistrer dans Google Agenda", "Google Takvim'e Kaydet", "Simpan ke Google Kalender", "گوگل کیلنڈر میں محفوظ کریں"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
                        Spacer(Modifier.height(6.dp))
                        Text("${sakinaTr(lang, "عدد الأيام", "Days to Sync", "Jours à synchroniser", "Senkronize Gün", "Jumlah Hari", "دنوں کی تعداد")}: $calDays".toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.LuminousIvory)
                        Spacer(Modifier.height(4.dp))
                        val dayOpts = listOf(7, 14, 30)
                        SettingSegmentedRow(
                            labels = dayOpts.map { "$it ${sakinaTr(lang, "أيام", "days", "jours", "gün", "hari", "دن")}".toWesternDigits() },
                            selectedIdx = dayOpts.indexOf(calDays).coerceAtLeast(0)
                        ) {
                            calDays = dayOpts[it]; SakinaAdvancedPrefs.setCalendarSyncDays(ctx, calDays)
                        }
                        Spacer(Modifier.height(6.dp))
                        SettingSwitchRow(sakinaTr(lang, "عرض الحالة (مشغول) أثناء الصلاة", "Show Status as Busy", "Afficher le statut « Occupé »", "Meşgul Olarak Göster", "Tampilkan Status Sibuk", "مصروف اسٹیٹس دکھائیں"), calBusy) {
                            calBusy = it; SakinaAdvancedPrefs.setCalendarSyncBusy(ctx, it)
                        }
                        Spacer(Modifier.height(4.dp))
                        SettingSwitchRow(sakinaTr(lang, "تفعيل تذكير التقويم", "Enable Calendar Reminders", "Activer les rappels d'agenda", "Takvim Hatırlatıcıları", "Aktifkan Pengingat Kalender", "کیلنڈر یاد دہانی فعال کریں"), calRem) {
                            calRem = it; SakinaAdvancedPrefs.setCalendarSyncReminder(ctx, it)
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(12.dp)).background(Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold))).clickable {
                                val next = PrayerEngine.day(ctx, 0).firstOrNull { it.second.time > System.currentTimeMillis() } ?: PrayerEngine.day(ctx, 1).first()
                                SakinaAdvancedPrefs.exportNextPrayerToCalendar(ctx, next.first, next.second.time)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(sakinaTr(lang, "حفظ الصلاة القادمة في التقويم", "Save Next Prayer to Calendar", "Ajouter la prochaine prière à l'agenda", "Sonraki Namazı Takvime Ekle", "Simpan Sholat Berikutnya", "اگلی نماز کیلنڈر میں محفوظ کریں"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.bgDeep)
                        }
                    }

                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(sakinaTr(lang, "المشاركة الاجتماعية", "Social Sharing", "Partage social", "Sosyal Paylaşım", "Berbagi Sosial", "سوشل شیئرنگ"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = sakinaTr(lang, "شارك وقت الصلاة مع الأصدقاء لموقعك الحالي ($placeName).", "Share prayer time with your current city ($placeName).", "Partagez l'horaire de prière pour votre ville ($placeName).", "Bulunduğunuz şehir ($placeName) için vakti paylaşın.", "Bagikan waktu sholat untuk kota Anda ($placeName).", "اپنے شہر ($placeName) کا نماز وقت شیئر کریں۔"),
                            fontFamily = SakinaBodyFont,
                            fontSize = 11.5.sp,
                            color = SakinaBrandColors.SoftSilver
                        )
                        Spacer(Modifier.height(7.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(34.dp).clip(RoundedCornerShape(12.dp)).background(theme.accentGold.copy(alpha = 0.25f)).border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp)).clickable {
                                val next = PrayerEngine.day(ctx, 0).firstOrNull { it.second.time > System.currentTimeMillis() } ?: PrayerEngine.day(ctx, 1).first()
                                SakinaAdvancedPrefs.sharePrayerTime(ctx, next.first, SakinaAdvancedPrefs.formatPrayerTime(ctx, next.second))
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(sakinaTr(lang, "شارك وقت الصلاة الآن", "Share Prayer Time Now", "Partager l'horaire maintenant", "Namaz Vaktini Paylaş", "Bagikan Waktu Sholat", "نماز کا وقت شیئر کریں"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.accentBright)
                        }
                    }

                    // === v18 New High-Value Settings (16-أ..و) ===
                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        var keepOn by remember { mutableStateOf(SakinaAdvancedPrefs.keepScreenOn(ctx)) }
                        var fontSc by remember { mutableFloatStateOf(SakinaAdvancedPrefs.readingFontScale(ctx)) }
                        var fastNotif by remember { mutableStateOf(SakinaAdvancedPrefs.voluntaryFastingNotif(ctx)) }
                        var flipOff by remember { mutableStateOf(SakinaAdvancedPrefs.flipToSilence(ctx)) }
                        var readTheme by remember { mutableIntStateOf(SakinaAdvancedPrefs.mushafReadTheme(ctx)) }

                        Text(sakinaTr(lang, "القراءة والراحة والنسخ الاحتياطي", "Reading, Comfort & Backup", "Lecture, confort et sauvegarde", "Okuma, Konfor ve Yedekleme", "Bacaan, Kenyamanan & Cadangan", "مطالعہ، آرام اور بیک اپ"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
                        Spacer(Modifier.height(6.dp))
                        SettingSwitchRow(sakinaTr(lang, "إبقاء الشاشة مضاءة أثناء القراءة", "Keep Screen On While Reading", "Garder l'écran allumé en lecture", "Okurken Ekranı Açık Tut", "Layar Tetap Menyala Saat Membaca", "مطالعہ کے دوران اسکرین آن رکھیں"), keepOn) {
                            keepOn = it; SakinaAdvancedPrefs.setKeepScreenOn(ctx, it)
                        }
                        Spacer(Modifier.height(4.dp))
                        SettingSwitchRow(sakinaTr(lang, "قلب الهاتف لإيقاف صوت الأذان", "Flip Phone to Silence Adhan", "Retourner pour couper l'Adhan", "Ezanı Susturmak İçin Çevir", "Balik HP untuk Hentikan Adzan", "فون الٹ کر اذان بند کریں"), flipOff) {
                            flipOff = it; SakinaAdvancedPrefs.setFlipToSilence(ctx, it)
                        }
                        Spacer(Modifier.height(4.dp))
                        SettingSwitchRow(sakinaTr(lang, "تذكير ليلي بصيام التطوع والأيام البيض", "Voluntary Fasting Reminder", "Rappel du jeûne surérogatoire", "Nafile Oruç Hatırlatıcısı", "Pengingat Puasa Sunnah", "نفلی روزوں کی یاد دہانی"), fastNotif) {
                            fastNotif = it; SakinaAdvancedPrefs.setVoluntaryFastingNotif(ctx, it); try { AlarmScheduler.scheduleNext(ctx) } catch (_: Exception) {}
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(sakinaTr(lang, "نمط قراءة المصحف الليلي الدافئ", "Mushaf Reading Theme", "Thème de lecture du Coran", "Mushaf Okuma Teması", "Tema Baca Mushaf", "مصحف مطالعہ تھیم"), fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.SoftSilver)
                        Spacer(Modifier.height(4.dp))
                        SettingSegmentedRow(listOf("عادي", "ورقي دافئ", "ليلي داكن"), readTheme) {
                            readTheme = it; SakinaAdvancedPrefs.setMushafReadTheme(ctx, it)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("${sakinaTr(lang, "حجم خط القراءة والأذكار", "Reading Font Scale", "Taille du texte de lecture", "Okuma Yazı Boyutu", "Ukuran Font Bacaan", "مطالعہ فونٹ سائز")}: ${(fontSc * 100).toInt()}%".toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.SoftSilver)
                        Slider(
                            value = fontSc,
                            onValueChange = { fontSc = it; SakinaAdvancedPrefs.setReadingFontScale(ctx, it) },
                            valueRange = 0.85f..1.45f,
                            colors = SliderDefaults.colors(thumbColor = theme.accentBright, activeTrackColor = theme.accentGold)
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(theme.bgCard).border(0.9.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                                    .clickable {
                                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        cm.setPrimaryClip(android.content.ClipData.newPlainText("SakinaBackup", SakinaAdvancedPrefs.exportSettingsJson(ctx)))
                                        Toast.makeText(ctx, "تم نسخ إعداداتك للحافظة", Toast.LENGTH_SHORT).show()
                                    }.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) { Text("نسخ الإعدادات", fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = theme.accentBright) }
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(theme.bgCard).border(0.9.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                                    .clickable {
                                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val txt = cm.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                        val ok = SakinaAdvancedPrefs.importSettingsJson(ctx, txt)
                                        Toast.makeText(ctx, if (ok) "تمت استعادة الإعدادات" else "انسخ نص النسخة أولاً", Toast.LENGTH_SHORT).show()
                                    }.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) { Text("استعادة الإعدادات", fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = theme.accentBright) }
                        }
                    }

                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(sakinaTr(lang, "تواصل معنا وحول التطبيق", "Contact & About Sakina", "Contact et à propos de Sakina", "İletişim ve Hakkında", "Kontak & Tentang Sakina", "رابطہ اور سکینہ کے بارے میں"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
                        Spacer(Modifier.height(6.dp))
                        SakinaSettingsPill(R.drawable.ic_duas, sakinaTr(lang, "شارك تطبيق سَكِينة", "Share Sakina App", "Partager l'application Sakina", "Sakina Uygulamasını Paylaş", "Bagikan Aplikasi Sakina", "سکینہ ایپ شیئر کریں"), "") {
                            try {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "تطبيق سَكِينة لمواقيت الصلاة والقرآن الكريم والأذكار (بدون إعلانات)")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                ctx.startActivity(Intent.createChooser(sendIntent, s.appName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: Exception) {}
                        }
                        Spacer(Modifier.height(6.dp))
                        SakinaSettingsPill(R.drawable.ic_star_ornament, sakinaTr(lang, "اقترح ميزة أو بلّغ عن خطأ", "Feedback & Bug Report", "Suggérer ou signaler un bug", "Geri Bildirim ve Hata Bildir", "Saran & Lapor Bug", "تجویز یا خرابی رپورٹ کریں"), "Email") {
                            try {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:islam.app.dev@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Sakina App Feedback")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                ctx.startActivity(emailIntent)
                            } catch (_: Exception) {
                                Toast.makeText(ctx, "islam.app.dev@gmail.com", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        SakinaSettingsPill(R.drawable.ic_settings, sakinaTr(lang, "سياسة الخصوصية", "Privacy Policy", "Politique de confidentialité", "Gizlilik Politikası", "Kebijakan Privasi", "پرائیویسی پالیسی"), "HTTPS") {
                            try {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://mothmane977-cmd.github.io/Sakinawaqt/")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: Exception) {}
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = sakinaTr(lang, "الإصدار الحالي: 2.0 · سَكِينة (أوفلاين وبدون إعلانات)", "Version 2.0 · Sakina (Offline & Ad-Free)", "Version 2.0 · Sakina (Hors ligne et sans pub)", "Sürüm 2.0 · Sakina (Çevrimdışı ve Reklamsız)", "Versi 2.0 · Sakina (Offline & Bebas Iklan)", "ورژن 2.0 · سکینہ (آف لائن، بلا اشتہار)"),
                            fontFamily = SakinaBodyFont,
                            fontSize = 11.sp,
                            color = SakinaBrandColors.SoftSilver,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Manual Prayer Time Offsets Dialog (-120..+120 min)
    if (showPerPrayerMuezzin) {
        val prayerKeys = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
        var tick by remember { mutableIntStateOf(0) }
        AlertDialog(
            onDismissRequest = { showPerPrayerMuezzin = false },
            containerColor = theme.bgCard,
            title = {
                Text(
                    text = sakinaTr(lang, "تخصيص مؤذن لكل صلاة", "Muezzin per Prayer", "Muezzin par prière", "Namaza Göre Müezzin", "Muadzin per Sholat", "ہر نماز کے لیے مؤذن"),
                    fontFamily = SakinaHeadingFont,
                    color = theme.accentBright
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    prayerKeys.forEachIndexed { idx, pKey ->
                        val curVoice = remember(tick) { SakinaAdhanAudio.voiceForPrayer(ctx, pKey) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x22FFFFFF))
                                .clickable {
                                    val nextVoice = AdhanVoice.entries[(curVoice.ordinal + 1) % AdhanVoice.entries.size]
                                    SakinaAdhanAudio.setVoiceForPrayer(ctx, pKey, nextVoice)
                                    tick++
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(s.prayers.getOrElse(idx) { pKey }, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, color = theme.accentBright)
                            Text(curVoice.localizedTitle(lang), fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.LuminousIvory, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPerPrayerMuezzin = false }) { Text("×", color = theme.accentBright, fontSize = 18.sp) }
            }
        )
    }

    if (showManualOffsets) {
        AlertDialog(
            onDismissRequest = { showManualOffsets = false },
            containerColor = theme.bgCard,
            title = { Text(sakinaTr(lang, "تعديل أوقات الصلاة يدوياً", "Manual Prayer Offsets", "Ajustement manuel des horaires", "Manuel Vakit Ayarı", "Penyesuaian Manual", "دستی اوقات تبدیلی"), fontFamily = SakinaHeadingFont, color = theme.accentBright) },
            text = {
                Column(modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PRAYER_NAMES.forEachIndexed { idx, pKey ->
                        var valMin by remember { mutableIntStateOf(Offsets.get(ctx, pKey)) }
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(pKey, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, color = SakinaBrandColors.LuminousIvory)
                                Text("${if (valMin > 0) "+$valMin" else "$valMin"} ${s.minuteShort}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, color = theme.accentBright)
                            }
                            Slider(
                                value = valMin.toFloat(),
                                onValueChange = {
                                    valMin = it.roundToInt()
                                    Offsets.set(ctx, pKey, valMin)
                                },
                                valueRange = -120f..120f,
                                colors = SliderDefaults.colors(thumbColor = theme.accentBright, activeTrackColor = theme.accentGold, inactiveTrackColor = Color(0x33FFFFFF))
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(theme.accentGold).clickable {
                        AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx)
                        showManualOffsets = false
                    }.padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(sakinaTr(lang, "حفظ", "Save", "Enregistrer", "Kaydet", "Simpan", "محفوظ کریں"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, color = theme.bgDeep)
                }
            }
        )
    }

    if (showVoicePicker) {
        AlertDialog(
            onDismissRequest = {
                SakinaAdhanAudio.stop(); playingVoiceKey = null; showVoicePicker = false
            },
            containerColor = theme.bgCard,
            title = { Text(s.adhanVoiceLabel, fontFamily = SakinaHeadingFont, color = theme.accentBright) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdhanVoice.entries.forEach { voice ->
                        val isSel = voice == selectedVoice
                        val isPlaying = playingVoiceKey == voice.key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) theme.accentGold.copy(alpha = 0.25f) else Color(0x22FFFFFF))
                                .border(0.8.dp, if (isSel) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)), RoundedCornerShape(12.dp))
                                .clickable { selectedVoice = voice; SakinaAdhanAudio.setSelectedVoice(ctx, voice) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (if (isSel) "✓ " else "• ") + voice.localizedTitle(lang),
                                fontFamily = SakinaBodyFont,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.5.sp,
                                color = if (isSel) theme.accentBright else SakinaBrandColors.LuminousIvory,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(if (isPlaying) theme.accentGold else Color(0x28FFFFFF)).clickable {
                                    if (isPlaying) { SakinaAdhanAudio.stop(); playingVoiceKey = null }
                                    else {
                                        selectedVoice = voice; SakinaAdhanAudio.setSelectedVoice(ctx, voice)
                                        playingVoiceKey = voice.key
                                        SakinaAdhanAudio.startPreview(ctx, voice) { playingVoiceKey = null }
                                    }
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow, contentDescription = null, tint = if (isPlaying) theme.bgDeep else theme.accentBright, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { SakinaAdhanAudio.stop(); playingVoiceKey = null; showVoicePicker = false }) { Text("×", color = theme.accentBright, fontSize = 18.sp) }
            }
        )
    }

    if (showCityPicker) {
        AlertDialog(
            onDismissRequest = { showCityPicker = false },
            containerColor = theme.bgCard,
            title = { Text(s.manualPick, fontFamily = SakinaHeadingFont, color = theme.accentBright) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SakinaCitiesDb.ALL_CITIES.forEach { city ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0x22FFFFFF)).clickable {
                                    Place.set(ctx, city.lat, city.lon, city.nameAr)
                                    AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); SakinaWidgetUpdater.updateAll(ctx)
                                    refreshTick++; showCityPicker = false
                                }.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isArOrUr) "${city.nameAr} (${city.countryAr})" else city.nameLatin, fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory)
                                Text("${city.lat}, ${city.lon}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontSize = 11.sp, color = theme.accentBright)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityPicker = false }) { Text("×", color = theme.accentBright, fontSize = 18.sp) }
            }
        )
    }
}

@Composable
private fun SettingSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = SakinaBodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = SakinaBrandColors.LuminousIvory,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        SakinaGoldSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingSegmentedRow(labels: List<String>, selectedIdx: Int, onSelect: (Int) -> Unit) {
    val theme = LocalSakinaTheme.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        labels.forEachIndexed { idx, label ->
            val sel = idx == selectedIdx
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(29.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (sel) theme.accentGold else Color(0x22FFFFFF))
                    .clickable { onSelect(idx) }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontFamily = SakinaBodyFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SakinaSettingsPill(@DrawableRes iconRes: Int, title: String, valueText: String, onClick: () -> Unit) {
    val theme = LocalSakinaTheme.current
    val pillShape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(pillShape)
            .background(Brush.verticalGradient(listOf(theme.bgCard, theme.bgDeep)))
            .border(0.9.dp, Sakina3DGoldBevelBrush, pillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 6.dp)) {
            Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (valueText.isNotBlank()) {
            Text(valueText.toWesternDigits(), fontFamily = SakinaBodyFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = theme.accentBright, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
