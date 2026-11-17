package app.sakina

import android.content.Context
import android.icu.util.IslamicCalendar
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private enum class ExtraSection { NONE, DUAS, RUQYAH, ALARM, SETTINGS }

private data class BrandPrayerRow(
    val keyNameAr: String,
    val displayTitle: String,
    val timeText: String,
    @DrawableRes val iconRes: Int,
    val isNext: Boolean,
    val mode: Int
)

@DrawableRes
private fun iconForPrayer(nameAr: String): Int = when (nameAr) {
    "الفجر" -> R.drawable.ic_fajr
    "الظهر" -> R.drawable.ic_dhuhr
    "العصر" -> R.drawable.ic_asr
    "المغرب" -> R.drawable.ic_maghrib
    else -> R.drawable.ic_isha
}

private val HOME_HIJRI_MONTHS = listOf(
    "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
    "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
)

private fun occasionWithGregorian(hijriOffset: Int): String {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, hijriOffset) }
    val ic = IslamicCalendar().apply { timeInMillis = cal.timeInMillis }
    val y = ic.get(IslamicCalendar.YEAR)
    val occasions = listOf(
        Triple(IslamicCalendar.RAJAB, 27, "الإسراء والمعراج"),
        Triple(IslamicCalendar.RAMADAN, 1, "بداية شهر رمضان"),
        Triple(IslamicCalendar.SHAWWAL, 1, "عيد الفطر المبارك"),
        Triple(IslamicCalendar.DHU_AL_HIJJAH, 9, "يوم عرفة"),
        Triple(IslamicCalendar.MUHARRAM, 1, "رأس السنة الهجرية")
    )
    var bestTitle = "رمضان المبارك"
    var bestDays = Int.MAX_VALUE
    var bestMs = cal.timeInMillis
    val nowMs = ic.timeInMillis
    for ((m, d, title) in occasions) {
        for (yr in y..y + 1) {
            val target = IslamicCalendar(yr, m, d)
            val diffDays = ((target.timeInMillis - nowMs) / 86_400_000L).toInt()
            if (diffDays in 0 until bestDays) {
                bestDays = diffDays; bestTitle = title; bestMs = target.timeInMillis
            }
        }
    }
    val gStr = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(bestMs))
    return "• $bestTitle: بعد $bestDays يوماً (الموافق $gStr م)".toWesternDigits()
}

@Composable
fun HomeScreen(ctx: Context) {
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }
    var currentTheme by remember {
        mutableStateOf(SakinaLuxuryTheme.entries.getOrElse(sp.getInt("theme_index", 0)) { SakinaLuxuryTheme.ROYAL_NAVY })
    }
    var currentLang by remember {
        val code = sp.getString("app_lang", "ar") ?: "ar"
        mutableStateOf(SakinaLang.entries.firstOrNull { it.code == code } ?: SakinaLang.AR)
    }
    var currentTab by remember { mutableStateOf(SakinaNavTab.HOME) }
    var extraSection by remember { mutableStateOf(ExtraSection.NONE) }
    var openedFromDrawer by remember { mutableStateOf(false) }
    var hadithTitle by remember { mutableStateOf<String?>(null) }
    var hadithBody by remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

    fun returnFromExtra() {
        extraSection = ExtraSection.NONE
        if (openedFromDrawer) {
            openedFromDrawer = false
            scope.launch { drawerState.open() }
        }
    }

    val headerImg = when (extraSection) {
        ExtraSection.DUAS, ExtraSection.RUQYAH -> R.drawable.bg_header_adhkar
        ExtraSection.ALARM, ExtraSection.SETTINGS -> R.drawable.bg_header_mosque
        ExtraSection.NONE -> when (currentTab) {
            SakinaNavTab.HOME -> R.drawable.bg_header_mosque
            SakinaNavTab.QIBLA -> R.drawable.bg_header_kaaba
            SakinaNavTab.QURAN -> R.drawable.bg_header_quran
            SakinaNavTab.ADHKAR -> R.drawable.bg_header_adhkar
            SakinaNavTab.CALENDAR -> R.drawable.bg_header_madinah
        }
    }

    SakinaBrandTheme(theme = currentTheme, lang = currentLang) {
        val s = LocalSakinaStrings.current
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = currentTheme.bgDeep, modifier = Modifier.width(292.dp)) {
                    Column(
                        modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp, vertical = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            SakinaTitle3D(text = s.appName, fontSize = 20.sp, modifier = Modifier.weight(1f))
                            SakinaBackIconButton(onClick = { scope.launch { drawerState.close() } })
                        }
                        Spacer(Modifier.height(4.dp))

                        SakinaDrawerPillItem(R.drawable.ic_duas, s.duasTitle) {
                            scope.launch { drawerState.close() }; openedFromDrawer = true; extraSection = ExtraSection.DUAS
                        }
                        SakinaDrawerPillItem(R.drawable.ic_ruqyah, s.ruqyahTitle) {
                            scope.launch { drawerState.close() }; openedFromDrawer = true; extraSection = ExtraSection.RUQYAH
                        }
                        SakinaDrawerPillItem(R.drawable.ic_alarm, s.alarmTitle) {
                            scope.launch { drawerState.close() }; openedFromDrawer = true; extraSection = ExtraSection.ALARM
                        }
                        SakinaDrawerPillItem(R.drawable.ic_sunan, "السنن الرواتب") {
                            scope.launch { drawerState.close() }
                            hadithTitle = "السنن الرواتب (12 ركعة)"
                            hadithBody = "• الفجر (2 قبلها) | الظهر (4 قبلها + 2 بعدها) | المغرب (2 بعدها) | العشاء (2 بعدها)\n\nالحديث: عن أم حبيبة رضي الله عنها قالت: سمعت رسول الله ﷺ يقول: «ما من عبد مسلم يصلي لله كل يوم ثنتي عشرة ركعة تطوعاً غير الفريضة إلا بنى الله له بيتاً في الجنة».\nالإسناد: رواه مسلم في صحيحه (رقم 728).".toWesternDigits()
                        }
                        SakinaDrawerPillItem(R.drawable.ic_dhuhr, "صلاة الضحى") {
                            scope.launch { drawerState.close() }
                            val events = PrayerEngine.events(ctx, 0)
                            val hm = SimpleDateFormat("HH:mm", Locale.US)
                            val duhaStart = hm.format(Date(events.first { it.first == 1 }.second.time + 15 * 60_000L))
                            val duhaEnd = hm.format(Date(events.first { it.first == 2 }.second.time - 10 * 60_000L))
                            hadithTitle = "صلاة الضحى ($duhaStart – $duhaEnd)"
                            hadithBody = "الحديث: عن أبي ذر رضي الله عنه عن النبي ﷺ أنه قال: «يصبح على كل سلامى من أحدكم صدقة... ويجزئ من ذلك ركعتان يركعهما من الضحى».\nالإسناد: رواه مسلم في صحيحه (رقم 720).".toWesternDigits()
                        }
                        SakinaDrawerPillItem(R.drawable.ic_isha, "قيام الليل") {
                            scope.launch { drawerState.close() }
                            hadithTitle = "قيام الليل والثلث الأخير"
                            hadithBody = "الحديث: عن أبي هريرة رضي الله عنه أن رسول الله ﷺ قال: «ينزل ربنا تبارك وتعالى كل ليلة إلى السماء الدنيا حين يبقى ثلث الليل الآخر يقول: من يدعوني فأستجيب له، من يسألني فأعطيه، من يستغفرني فأغفر له».\nالإسناد: متفق عليه؛ رواه البخاري (1145) ومسلم (758).".toWesternDigits()
                        }
                        SakinaDrawerPillItem(R.drawable.ic_calendar, "صيام التطوع والمناسبات") {
                            scope.launch { drawerState.close() }
                            hadithTitle = "صيام التطوع والمناسبات"
                            hadithBody = "${occasionWithGregorian(Prefs.hijriOffset(ctx))}\n\nالحديث: عن أبي قتادة الأنصاري رضي الله عنه أن رسول الله ﷺ سُئل عن صوم يوم الإثنين فقال: «ذاك يوم ولدت فيه، ويوم بعثت أو أنزل علي فيه».\nالإسناد: رواه مسلم (رقم 1162)، وروى البخاري (1981) وصية النبي ﷺ بصيام ثلاثة أيام من كل شهر (الأيام البيض: 13، 14، 15).".toWesternDigits()
                        }
                        SakinaDrawerPillItem(R.drawable.ic_settings, s.settingsTitle) {
                            scope.launch { drawerState.close() }; openedFromDrawer = true; extraSection = ExtraSection.SETTINGS
                        }
                    }
                }
            }
        ) {
            SakinaBackgroundLayer(headerImageRes = headerImg, showMihrabFrame = true) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (extraSection) {
                        ExtraSection.DUAS -> SakinaDuasScreen(onBackToHome = { returnFromExtra() })
                        ExtraSection.RUQYAH -> SakinaRuqyahScreen(onBackToHome = { returnFromExtra() })
                        ExtraSection.ALARM -> SakinaAlarmScreen(ctx = ctx, onBackClick = { returnFromExtra() })
                        ExtraSection.SETTINGS -> SakinaSettingsScreen(
                            ctx = ctx,
                            selectedTheme = currentTheme,
                            selectedLang = currentLang,
                            onThemeChange = { t -> currentTheme = t; sp.edit().putInt("theme_index", t.ordinal).apply() },
                            onLangChange = { l -> currentLang = l; sp.edit().putString("app_lang", l.code).apply() },
                            onBackClick = { returnFromExtra() }
                        )
                        ExtraSection.NONE -> {
                            Crossfade(targetState = currentTab, label = "sakina5Tabs") { tab ->
                                when (tab) {
                                    SakinaNavTab.HOME -> SakinaHomeContent(ctx = ctx)
                                    SakinaNavTab.QIBLA -> SakinaQiblaScreen(ctx = ctx)
                                    SakinaNavTab.QURAN -> SakinaQuranScreen(ctx = ctx)
                                    SakinaNavTab.ADHKAR -> SakinaAdhkarScreen(ctx = ctx)
                                    SakinaNavTab.CALENDAR -> SakinaCalendarScreen(ctx = ctx)
                                }
                            }
                        }
                    }

                    if (extraSection == ExtraSection.NONE) {
                        // Right corner: Menu Button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .statusBarsPadding()
                                .padding(start = 24.dp, top = 32.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(currentTheme.bgDeep.copy(alpha = 0.82f))
                                .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                                .clickable { scope.launch { drawerState.open() } },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(painter = painterResource(id = R.drawable.ic_menu), contentDescription = "Menu", modifier = Modifier.size(15.dp))
                        }

                        // Left corner (#5): Petite Language Switcher with custom globe icon & flag (replaces old Settings icon!)
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(end = 24.dp, top = 32.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(currentTheme.bgDeep.copy(alpha = 0.85f))
                                .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                                .clickable {
                                    val next = SakinaLang.entries[(currentLang.ordinal + 1) % SakinaLang.entries.size]
                                    currentLang = next
                                    sp.edit().putString("app_lang", next.code).apply()
                                }
                                .padding(horizontal = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_language),
                                contentDescription = "Language",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${currentLang.flag} ${currentLang.code.uppercase()}",
                                fontFamily = SakinaNumeralFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                color = currentTheme.accentBright
                            )
                        }
                    }

                    SakinaBottomPillBar(
                        currentTab = currentTab,
                        onTabSelected = { extraSection = ExtraSection.NONE; openedFromDrawer = false; currentTab = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        if (hadithTitle != null && hadithBody != null) {
            AlertDialog(
                onDismissRequest = { hadithTitle = null },
                containerColor = currentTheme.bgCard,
                title = { Text(hadithTitle!!, fontFamily = SakinaHeadingFont, color = currentTheme.accentBright) },
                text = { Text(hadithBody!!, fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory, lineHeight = 24.sp) },
                confirmButton = {
                    SakinaBackIconButton(onClick = { hadithTitle = null; scope.launch { drawerState.open() } })
                }
            )
        }
    }
}

@Composable
private fun SakinaHomeContent(ctx: Context) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var dayOffset by remember { mutableIntStateOf(0) }
    var modeTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        SakinaLiveNotifier.update(ctx)
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val hmFmt = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val todayPrayers = remember(nowMs / 60_000L, dayOffset) { PrayerEngine.day(ctx, dayOffset) }
    val tomorrowPrayers = remember(nowMs / 60_000L) { PrayerEngine.day(ctx, 1) }

    val nextIndexToday = PrayerEngine.day(ctx, 0).indexOfFirst { it.second.time > nowMs }
    val (nextDisplayName, nextTimeMs) = if (nextIndexToday >= 0) {
        s.prayers[nextIndexToday] to PrayerEngine.day(ctx, 0)[nextIndexToday].second.time
    } else {
        s.prayers[0] to tomorrowPrayers.first().second.time
    }

    val remSec = ((nextTimeMs - nowMs).coerceAtLeast(0L) / 1000L)
    val countdownText = String.format(Locale.US, "%02d:%02d:%02d", remSec / 3600, (remSec % 3600) / 60, remSec % 60).toWesternDigits()

    val rows = remember(todayPrayers, nextIndexToday, dayOffset, modeTick, s) {
        todayPrayers.mapIndexed { idx, (keyAr, date) ->
            BrandPrayerRow(keyAr, s.prayers[idx], hmFmt.format(date).toWesternDigits(), iconForPrayer(keyAr), dayOffset == 0 && idx == nextIndexToday, Modes.get(ctx, keyAr))
        }
    }

    val viewedCal = remember(dayOffset, nowMs / 3_600_000L) {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
    }
    val datePillText = remember(viewedCal) {
        val g = SimpleDateFormat("EEEE dd MMMM yyyy", Locale("ar")).format(viewedCal.time).toWesternDigits()
        val ic = IslamicCalendar().apply {
            timeInMillis = viewedCal.timeInMillis
            add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
        }
        val h = "${ic.get(IslamicCalendar.DAY_OF_MONTH)} ${HOME_HIJRI_MONTHS[ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)]} ${ic.get(IslamicCalendar.YEAR)}".toWesternDigits()
        "$g · $h"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 76.dp, bottom = 78.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Header organized like 19829.jpg: 3D Title + Ornamental Divider + Date Pill with arrows + Today badge
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SakinaTitle3D(text = s.appName, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(theme.bgCard).border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape).clickable { dayOffset-- },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.bgCard.copy(alpha = 0.88f))
                        .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(datePillText, fontFamily = SakinaEditorialFont, fontSize = 11.sp, color = SakinaBrandColors.LuminousIvory, maxLines = 1)
                }
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(theme.bgCard).border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape).clickable { dayOffset++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                }
            }
            if (dayOffset != 0) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F3D3E))
                        .border(0.7.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp))
                        .clickable { dayOffset = 0 }
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text("اليوم", fontFamily = SakinaEditorialFont, fontSize = 10.5.sp, color = theme.accentBright)
                }
            }
        }

        // 2. Unified Card Container like 19829.jpg: Dark Navy Top Header with Cartouche + Parchment Rows Below
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp,
            glowGold = true,
            contentPadding = PaddingValues(0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp, start = 14.dp, end = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "❖ ${s.nextPrayer}: $nextDisplayName · ${Place.name(ctx)} ❖",
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = theme.accentBright,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 26.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.ic_fajr), contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(countdownText, fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = theme.accentBright, style = TextStyle(fontFeatureSettings = "tnum"))
                    }
                }
            }

            SakinaParchmentPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                rows.forEachIndexed { index, prayer ->
                    SakinaPrayerRow(
                        prayer = prayer,
                        onCycleMode = {
                            val nextMode = when (prayer.mode) { 0, 1 -> 2; 2 -> 3; else -> 0 }
                            Modes.set(ctx, prayer.keyNameAr, nextMode)
                            AlarmScheduler.scheduleNext(ctx)
                            SakinaLiveNotifier.update(ctx)
                            modeTick++
                        }
                    )
                    if (index < rows.lastIndex) HorizontalDivider(color = Color(0x22C5A059))
                }
            }
        }
    }
}

@Composable
private fun SakinaPrayerRow(prayer: BrandPrayerRow, onCycleMode: () -> Unit) {
    val bgColor = if (prayer.isNext) Color(0x28C5A059) else Color.Transparent
    val modeIcon = when (prayer.mode) {
        2 -> Icons.Rounded.Vibration
        3 -> Icons.Rounded.NotificationsOff
        else -> Icons.Rounded.NotificationsActive
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Right side (in RTL): Diamond ornament + Prayer Name
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = modeIcon,
                contentDescription = null,
                tint = if (prayer.mode == 3) Color(0xFF9E8C6C) else Color(0xFFC5A059),
                modifier = Modifier.size(17.dp).clickable { onCycleMode() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = prayer.displayTitle,
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (prayer.isNext) Color(0xFF0F3D3E) else SakinaInkDark
            )
        }
        // Left side (in RTL): Prayer Time + Star/Hexagon Polygon Badge (like 19829.jpg!)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = prayer.timeText.toWesternDigits(),
                fontFamily = SakinaNumeralFont,
                fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 14.5.sp,
                color = if (prayer.isNext) Color(0xFF0F3D3E) else SakinaInkDark,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
            Spacer(modifier = Modifier.width(10.dp))
            SakinaStarPolygonBadge(iconRes = prayer.iconRes, size = 32.dp)
        }
    }
}
