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
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    @DrawableRes val bgRes: Int,
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

@DrawableRes
private fun bgForPrayer(nameAr: String): Int = when (nameAr) {
    "الفجر" -> R.drawable.bg_header_mosque
    "الظهر" -> R.drawable.bg_header_kaaba
    "العصر" -> R.drawable.bg_header_madinah
    "المغرب" -> R.drawable.bg_header_adhkar
    else -> R.drawable.bg_header_quran
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
                        SakinaDrawerPillItem(R.drawable.ic_sunan, s.sunanTitle) {
                            scope.launch { drawerState.close() }
                            hadithTitle = s.sunanTitle
                            hadithBody = "• الفجر (2 قبلها) | الظهر (4 قبلها + 2 بعدها) | المغرب (2 بعدها) | العشاء (2 بعدها)\n\nالحديث: عن أم حبيبة رضي الله عنها قالت: سمعت رسول الله ﷺ يقول: «ما من عبد مسلم يصلي لله كل يوم ثنتي عشرة ركعة تطوعاً غير الفريضة إلا بنى الله له بيتاً في الجنة».\nالإسناد: رواه مسلم في صحيحه (رقم 728).".toWesternDigits()
                        }
                        SakinaDrawerPillItem(R.drawable.ic_dhuhr, s.duhaTitle) {
                            scope.launch { drawerState.close() }
                            val events = PrayerEngine.events(ctx, 0)
                            val hm = SimpleDateFormat("HH:mm", Locale.US)
                            val duhaStart = hm.format(Date(events.first { it.first == 1 }.second.time + 15 * 60_000L))
                            val duhaEnd = hm.format(Date(events.first { it.first == 2 }.second.time - 10 * 60_000L))
                            hadithTitle = "${s.duhaTitle} ($duhaStart – $duhaEnd)"
                            hadithBody = "الحديث: عن أبي ذر رضي الله عنه عن النبي ﷺ أنه قال: «يصبح على كل سلامى من أحدكم صدقة... ويجزئ من ذلك ركعتان يركعهما من الضحى».\nالإسناد: رواه مسلم في صحيحه (رقم 720).".toWesternDigits()
                        }
                        SakinaDrawerPillItem(R.drawable.ic_isha, s.qiyamTitle) {
                            scope.launch { drawerState.close() }
                            hadithTitle = s.qiyamTitle
                            hadithBody = "الحديث: عن أبي هريرة رضي الله عنه أن رسول الله ﷺ قال: «ينزل ربنا تبارك وتعالى كل ليلة إلى السماء الدنيا حين يبقى ثلث الليل الآخر يقول: من يدعوني فأستجيب له، من يسألني فأعطيه، من يستغفرني فأغفر له».\nالإسناد: متفق عليه؛ رواه البخاري (1145) ومسلم (758).".toWesternDigits()
                        }
                        SakinaDrawerPillItem(R.drawable.ic_calendar, s.fastingTitle) {
                            scope.launch { drawerState.close() }
                            hadithTitle = s.fastingTitle
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
                        // FIXED Physical Right Corner (AbsoluteAlignment.TopRight): Menu Button
                        Box(
                            modifier = Modifier
                                .align(AbsoluteAlignment.TopRight)
                                .statusBarsPadding()
                                .padding(end = 18.dp, top = 16.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(currentTheme.bgDeep.copy(alpha = 0.85f))
                                .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                                .clickable { scope.launch { drawerState.open() } },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(painter = painterResource(id = R.drawable.ic_menu), contentDescription = "Menu", modifier = Modifier.size(15.dp))
                        }

                        // FIXED Physical Left Corner (AbsoluteAlignment.TopLeft): Language Switcher NEVER moves when switching RTL/LTR!
                        Row(
                            modifier = Modifier
                                .align(AbsoluteAlignment.TopLeft)
                                .statusBarsPadding()
                                .padding(start = 18.dp, top = 16.dp)
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
                            Image(painter = painterResource(id = R.drawable.ic_language), contentDescription = "Language", modifier = Modifier.size(14.dp))
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
        val highlightIdx = if (nextIndexToday >= 0) nextIndexToday else 0
        todayPrayers.mapIndexed { idx, (keyAr, date) ->
            BrandPrayerRow(keyAr, s.prayers[idx], hmFmt.format(date).toWesternDigits(), iconForPrayer(keyAr), bgForPrayer(keyAr), idx == highlightIdx, Modes.get(ctx, keyAr))
        }
    }

    val viewedCal = remember(dayOffset, nowMs / 3_600_000L) {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
    }
    val datePillText = remember(viewedCal) {
        val g = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(viewedCal.time).toWesternDigits()
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
            .padding(start = 18.dp, end = 18.dp, top = 52.dp, bottom = 76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SakinaTitle3D(text = s.appName, fontSize = 23.sp)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier.size(22.dp).clip(CircleShape).background(theme.bgCard).border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape).clickable { dayOffset-- },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(15.dp))
                }
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(theme.bgCard.copy(alpha = 0.88f)).border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(datePillText, fontFamily = SakinaEditorialFont, fontSize = 10.5.sp, color = SakinaBrandColors.LuminousIvory, maxLines = 1)
                }
                Box(
                    modifier = Modifier.size(22.dp).clip(CircleShape).background(theme.bgCard).border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape).clickable { dayOffset++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(15.dp))
                }
            }
            if (dayOffset != 0) {
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFF0F3D3E)).border(0.7.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp)).clickable { dayOffset = 0 }.padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(s.todayLabel, fontFamily = SakinaEditorialFont, fontSize = 10.sp, color = theme.accentBright)
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("❖ ${s.nextPrayer}: $nextDisplayName · ${Place.name(ctx)} ❖", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = theme.accentBright, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(id = R.drawable.ic_fajr), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(countdownText, fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 25.sp, color = theme.accentBright, style = TextStyle(fontFeatureSettings = "tnum"))
                }
            }
        }

        // Prayer rows: Next prayer is separated, larger, and has a full-bleed high-res image background (#8)!
        val nextRow = rows.firstOrNull { it.isNext } ?: rows.first()
        val otherRows = rows.filter { it != nextRow }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SakinaFeaturedNextPrayerCard(
                prayer = nextRow,
                nextLabel = s.nextPrayer,
                onCycleMode = {
                    val nextMode = when (nextRow.mode) { 0, 1 -> 2; 2 -> 3; else -> 0 }
                    Modes.set(ctx, nextRow.keyNameAr, nextMode)
                    AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); modeTick++
                }
            )

            SakinaParchmentPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                otherRows.forEachIndexed { index, prayer ->
                    SakinaPrayerRow(
                        prayer = prayer,
                        onCycleMode = {
                            val nextMode = when (prayer.mode) { 0, 1 -> 2; 2 -> 3; else -> 0 }
                            Modes.set(ctx, prayer.keyNameAr, nextMode)
                            AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); modeTick++
                        }
                    )
                    if (index < otherRows.lastIndex) HorizontalDivider(color = Color(0x22C5A059))
                }
            }
        }
    }
}

@Composable
private fun SakinaFeaturedNextPrayerCard(
    prayer: BrandPrayerRow,
    nextLabel: String,
    onCycleMode: () -> Unit
) {
    val theme = LocalSakinaTheme.current
    val shape = RoundedCornerShape(18.dp)
    val modeIcon = when (prayer.mode) {
        2 -> Icons.Rounded.Vibration
        3 -> Icons.Rounded.NotificationsOff
        else -> Icons.Rounded.NotificationsActive
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(10.dp, shape, ambientColor = theme.accentGold, spotColor = theme.accentGold)
            .clip(shape)
            .border(1.5.dp, Sakina3DGoldBevelBrush, shape)
    ) {
        Image(
            painter = painterResource(id = prayer.bgRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(Color(0xCC071422), Color(0x99071422), Color(0xCC071422))))
        )
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = modeIcon,
                    contentDescription = null,
                    tint = if (prayer.mode == 3) SakinaBrandColors.SoftSilver else theme.accentBright,
                    modifier = Modifier.size(20.dp).clickable { onCycleMode() }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(prayer.displayTitle, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = theme.accentBright)
                    Text(nextLabel, fontFamily = SakinaBodyFont, fontSize = 10.sp, color = SakinaBrandColors.LuminousIvory)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = prayer.timeText.toWesternDigits(),
                    fontFamily = SakinaNumeralFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = theme.accentBright,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                Spacer(modifier = Modifier.width(10.dp))
                SakinaStarPolygonBadge(iconRes = prayer.iconRes, size = 36.dp)
            }
        }
    }
}

@Composable
private fun SakinaPrayerRow(prayer: BrandPrayerRow, onCycleMode: () -> Unit) {
    val modeIcon = when (prayer.mode) {
        2 -> Icons.Rounded.Vibration
        3 -> Icons.Rounded.NotificationsOff
        else -> Icons.Rounded.NotificationsActive
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = modeIcon,
                contentDescription = null,
                tint = if (prayer.mode == 3) Color(0xFF9E8C6C) else Color(0xFFC5A059),
                modifier = Modifier.size(17.dp).clickable { onCycleMode() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(prayer.displayTitle, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = SakinaInkDark)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = prayer.timeText.toWesternDigits(),
                fontFamily = SakinaNumeralFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = SakinaInkDark,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
            Spacer(modifier = Modifier.width(10.dp))
            SakinaStarPolygonBadge(iconRes = prayer.iconRes, size = 30.dp)
        }
    }
}
