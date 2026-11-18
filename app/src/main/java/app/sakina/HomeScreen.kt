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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

private fun occasionWithGregorian(hijriOffset: Int, isArOrUr: Boolean): String {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, hijriOffset) }
    val ic = IslamicCalendar().apply { timeInMillis = cal.timeInMillis }
    val y = ic.get(IslamicCalendar.YEAR)
    val occasions = listOf(
        Triple(IslamicCalendar.RAJAB, 27, if (isArOrUr) "الإسراء والمعراج" else "Isra & Mi'raj"),
        Triple(IslamicCalendar.RAMADAN, 1, if (isArOrUr) "بداية شهر رمضان" else "Start of Ramadan"),
        Triple(IslamicCalendar.SHAWWAL, 1, if (isArOrUr) "عيد الفطر المبارك" else "Eid al-Fitr"),
        Triple(IslamicCalendar.DHU_AL_HIJJAH, 9, if (isArOrUr) "يوم عرفة" else "Day of Arafah"),
        Triple(IslamicCalendar.MUHARRAM, 1, if (isArOrUr) "رأس السنة الهجرية" else "Islamic New Year")
    )
    var bestTitle = occasions[1].third
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
    return if (isArOrUr) "• $bestTitle: بعد $bestDays يوماً (الموافق $gStr م)".toWesternDigits()
    else "• $bestTitle: in $bestDays days ($gStr)".toWesternDigits()
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
        val isArOrUr = currentLang == SakinaLang.AR || currentLang == SakinaLang.UR

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = currentTheme.bgDeep, modifier = Modifier.width(288.dp)) {
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
                            hadithBody = if (isArOrUr) {
                                "• ${s.prayers[0]} (2) | ${s.prayers[1]} (4+2) | ${s.prayers[3]} (2) | ${s.prayers[4]} (2)\n\nالحديث: عن أم حبيبة رضي الله عنها قالت: سمعت رسول الله ﷺ يقول: «ما من عبد مسلم يصلي لله كل يوم ثنتي عشرة ركعة تطوعاً غير الفريضة إلا بنى الله له بيتاً في الجنة».\nالإسناد: رواه مسلم في صحيحه (رقم 728).".toWesternDigits()
                            } else {
                                "• ${s.prayers[0]} (2 before) | ${s.prayers[1]} (4 before + 2 after) | ${s.prayers[3]} (2 after) | ${s.prayers[4]} (2 after)\n\nHadith: Umm Habibah narrated that the Messenger of Allah ﷺ said: 'Whoever prays twelve voluntary rak'ahs every day, Allah will build a house for him in Paradise.'\nSource: Sahih Muslim (728).".toWesternDigits()
                            }
                        }
                        SakinaDrawerPillItem(R.drawable.ic_dhuhr, s.duhaTitle) {
                            scope.launch { drawerState.close() }
                            val events = PrayerEngine.events(ctx, 0)
                            val hm = SimpleDateFormat("HH:mm", Locale.US)
                            val duhaStart = hm.format(Date(events.first { it.first == 1 }.second.time + 15 * 60_000L))
                            val duhaEnd = hm.format(Date(events.first { it.first == 2 }.second.time - 10 * 60_000L))
                            hadithTitle = "${s.duhaTitle} ($duhaStart – $duhaEnd)"
                            hadithBody = if (isArOrUr) {
                                "الحديث: عن أبي ذر رضي الله عنه عن النبي ﷺ أنه قال: «يصبح على كل سلامى من أحدكم صدقة... ويجزئ من ذلك ركعتان يركعهما من الضحى».\nالإسناد: رواه مسلم في صحيحه (رقم 720).".toWesternDigits()
                            } else {
                                "Hadith: Abu Dharr narrated that the Prophet ﷺ said: 'Charity is due upon every joint of a person every morning... and two rak'ahs of Duha suffice for that.'\nSource: Sahih Muslim (720).".toWesternDigits()
                            }
                        }
                        SakinaDrawerPillItem(R.drawable.ic_isha, s.qiyamTitle) {
                            scope.launch { drawerState.close() }
                            hadithTitle = s.qiyamTitle
                            hadithBody = if (isArOrUr) {
                                "الحديث: عن أبي هريرة رضي الله عنه أن رسول الله ﷺ قال: «ينزل ربنا تبارك وتعالى كل ليلة إلى السماء الدنيا حين يبقى ثلث الليل الآخر يقول: من يدعوني فأستجيب له، من يسألني فأعطيه، من يستغفرني فأغفر له».\nالإسناد: متفق عليه؛ رواه البخاري (1145) ومسلم (758).".toWesternDigits()
                            } else {
                                "Hadith: Abu Hurayrah narrated that the Messenger of Allah ﷺ said: 'Our Lord descends every night to the lowest heaven in the last third of the night, saying: Who calls upon Me that I may answer him? Who asks of Me that I may give him? Who seeks My forgiveness that I may forgive him?'\nSource: Sahih al-Bukhari (1145) & Sahih Muslim (758).".toWesternDigits()
                            }
                        }
                        SakinaDrawerPillItem(R.drawable.ic_calendar, s.fastingTitle) {
                            scope.launch { drawerState.close() }
                            hadithTitle = s.fastingTitle
                            hadithBody = if (isArOrUr) {
                                "${occasionWithGregorian(Prefs.hijriOffset(ctx), true)}\n\nالحديث: عن أبي قتادة الأنصاري رضي الله عنه أن رسول الله ﷺ سُئل عن صوم يوم الإثنين فقال: «ذاك يوم ولدت فيه، ويوم بعثت أو أنزل علي فيه».\nالإسناد: رواه مسلم (رقم 1162)، وروى البخاري (1981) وصية النبي ﷺ بصيام ثلاثة أيام من كل شهر (الأيام البيض: 13، 14، 15).".toWesternDigits()
                            } else {
                                "${occasionWithGregorian(Prefs.hijriOffset(ctx), false)}\n\nHadith: Fast Mondays & Thursdays (Sahih Muslim 1162) and the Three White Days (13th, 14th, 15th of every Hijri month — Sahih al-Bukhari 1981).".toWesternDigits()
                            }
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
                        // Top-Right Menu Button (30.dp circle)
                        Box(
                            modifier = Modifier
                                .align(AbsoluteAlignment.TopRight)
                                .statusBarsPadding()
                                .padding(end = 16.dp, top = 14.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(currentTheme.bgDeep.copy(alpha = 0.88f))
                                .border(0.9.dp, Sakina3DGoldBevelBrush, CircleShape)
                                .clickable { scope.launch { drawerState.open() } },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(painter = painterResource(id = R.drawable.ic_menu), contentDescription = "Menu", modifier = Modifier.size(15.dp))
                        }

                        // Top-Left Language Switcher (#10): Exact same 30.dp circular size as Menu button for 100% symmetry!
                        // Combines globe icon + 2-letter language code without the flag.
                        Box(
                            modifier = Modifier
                                .align(AbsoluteAlignment.TopLeft)
                                .statusBarsPadding()
                                .padding(start = 16.dp, top = 14.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(currentTheme.bgDeep.copy(alpha = 0.88f))
                                .border(0.9.dp, Sakina3DGoldBevelBrush, CircleShape)
                                .clickable {
                                    val next = SakinaLang.entries[(currentLang.ordinal + 1) % SakinaLang.entries.size]
                                    currentLang = next
                                    sp.edit().putString("app_lang", next.code).apply()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_language),
                                contentDescription = "Language",
                                alpha = 0.30f,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = currentLang.code.uppercase(),
                                fontFamily = SakinaNumeralFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                color = currentTheme.accentBright,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
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
                text = { Text(hadithBody!!, fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory, lineHeight = 22.sp) },
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
    var analogPrimary by remember { mutableStateOf(false) }

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

    // Keep all 5 prayers in their natural chronological order (#6): الفجر، الظهر، العصر، المغرب، العشاء
    val rows = remember(todayPrayers, nextIndexToday, dayOffset, modeTick, s) {
        val highlightIdx = if (nextIndexToday >= 0) nextIndexToday else 0
        todayPrayers.mapIndexed { idx, (keyAr, date) ->
            BrandPrayerRow(
                keyNameAr = keyAr,
                displayTitle = s.prayers.getOrElse(idx) { keyAr },
                timeText = hmFmt.format(date).toWesternDigits(),
                iconRes = iconForPrayer(keyAr),
                bgRes = bgForPrayer(keyAr),
                isNext = idx == highlightIdx,
                mode = Modes.get(ctx, keyAr)
            )
        }
    }

    val viewedCal = remember(dayOffset, nowMs / 3_600_000L) {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
    }
    val datePillText = remember(viewedCal, s) {
        val g = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(viewedCal.time).toWesternDigits()
        val ic = IslamicCalendar().apply {
            timeInMillis = viewedCal.timeInMillis
            add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
        }
        val hMonth = s.hijriMonths.getOrElse(ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)) { "" }
        val h = "${ic.get(IslamicCalendar.DAY_OF_MONTH)} $hMonth ${ic.get(IslamicCalendar.YEAR)}".toWesternDigits()
        "$g · $h"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 46.dp, bottom = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header: App Title + Date Pill + Location with GPS Icon directly underneath (#12)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SakinaTitle3D(text = s.appName, fontSize = 21.sp)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .size(21.dp)
                        .clip(CircleShape)
                        .background(theme.bgCard)
                        .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                        .clickable { dayOffset-- },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(14.dp))
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.bgCard.copy(alpha = 0.88f))
                        .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                        .padding(horizontal = 9.dp, vertical = 2.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(datePillText, fontFamily = SakinaEditorialFont, fontSize = 10.sp, color = SakinaBrandColors.LuminousIvory, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(
                    modifier = Modifier
                        .size(21.dp)
                        .clip(CircleShape)
                        .background(theme.bgCard)
                        .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                        .clickable { dayOffset++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(2.dp))
            // Location + GPS Icon right under the Date (#12)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = Place.name(ctx),
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = theme.accentBright
                )
                if (dayOffset != 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F3D3E))
                            .border(0.7.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(10.dp))
                            .clickable { dayOffset = 0 }
                            .padding(horizontal = 8.dp, vertical = 1.dp)
                    ) {
                        Text(s.todayLabel, fontFamily = SakinaEditorialFont, fontSize = 9.5.sp, color = theme.accentBright)
                    }
                }
            }
        }

        // Next Prayer Countdown Cartouche (well-spaced numerals) + Luxury Analog Clock right beside it (#11)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "❖ ${s.nextPrayer}: $nextDisplayName ❖",
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = theme.accentBright,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                SakinaOrnamentCartouche(
                    contentPadding = PaddingValues(horizontal = 36.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.ic_fajr), contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (analogPrimary) hmFmt.format(Date(nowMs)).toWesternDigits() else countdownText,
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.5.sp,
                            color = theme.accentBright,
                            maxLines = 1,
                            style = TextStyle(
                                fontFeatureSettings = "tnum",
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                // Analog Clock beside the cartouche (#11) — tap to toggle live time / countdown focus
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { analogPrimary = !analogPrimary },
                    contentAlignment = Alignment.Center
                ) {
                    SakinaAnalogClockDial(
                        timeMs = nowMs,
                        size = if (analogPrimary) 48.dp else 42.dp
                    )
                }
            }
        }

        // Chronological 5 Prayers List (#6):
        // Keeps الفجر -> الظهر -> العصر -> المغرب -> العشاء in exact order!
        // Whichever prayer is next is rendered in-place as the separated, larger photo-backed card.
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            val nextIdx = rows.indexOfFirst { it.isNext }.coerceAtLeast(0)
            val beforeRows = rows.subList(0, nextIdx)
            val featuredRow = rows[nextIdx]
            val afterRows = rows.subList(nextIdx + 1, rows.size)

            if (beforeRows.isNotEmpty()) {
                SakinaParchmentPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    beforeRows.forEachIndexed { i, p ->
                        SakinaPrayerRow(
                            prayer = p,
                            onCycleMode = {
                                val nextMode = when (p.mode) { 0, 1 -> 2; 2 -> 3; else -> 0 }
                                Modes.set(ctx, p.keyNameAr, nextMode)
                                AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); modeTick++
                            }
                        )
                        if (i < beforeRows.lastIndex) HorizontalDivider(color = Color(0x22C5A059))
                    }
                }
            }

            SakinaFeaturedNextPrayerCard(
                prayer = featuredRow,
                nextLabel = s.nextPrayer,
                onCycleMode = {
                    val nextMode = when (featuredRow.mode) { 0, 1 -> 2; 2 -> 3; else -> 0 }
                    Modes.set(ctx, featuredRow.keyNameAr, nextMode)
                    AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); modeTick++
                }
            )

            if (afterRows.isNotEmpty()) {
                SakinaParchmentPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    afterRows.forEachIndexed { i, p ->
                        SakinaPrayerRow(
                            prayer = p,
                            onCycleMode = {
                                val nextMode = when (p.mode) { 0, 1 -> 2; 2 -> 3; else -> 0 }
                                Modes.set(ctx, p.keyNameAr, nextMode)
                                AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); modeTick++
                            }
                        )
                        if (i < afterRows.lastIndex) HorizontalDivider(color = Color(0x22C5A059))
                    }
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
    val shape = RoundedCornerShape(16.dp)
    val modeIcon = when (prayer.mode) {
        2 -> Icons.Rounded.Vibration
        3 -> Icons.Rounded.NotificationsOff
        else -> Icons.Rounded.NotificationsActive
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
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
                .background(Brush.horizontalGradient(listOf(Color(0xD8071422), Color(0x99071422), Color(0xD8071422))))
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = modeIcon,
                    contentDescription = null,
                    tint = if (prayer.mode == 3) SakinaBrandColors.SoftSilver else theme.accentBright,
                    modifier = Modifier.size(19.dp).clickable { onCycleMode() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = prayer.displayTitle,
                        fontFamily = SakinaEditorialFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        lineHeight = 17.sp,
                        color = theme.accentBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = nextLabel,
                        fontFamily = SakinaBodyFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.5.sp,
                        lineHeight = 11.sp,
                        color = SakinaBrandColors.LuminousIvory,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = prayer.timeText.toWesternDigits(),
                    fontFamily = SakinaNumeralFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = theme.accentBright,
                    style = TextStyle(
                        fontFeatureSettings = "tnum",
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                SakinaStarPolygonBadge(iconRes = prayer.iconRes, size = 34.dp)
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = modeIcon,
                contentDescription = null,
                tint = if (prayer.mode == 3) Color(0xFF9E8C6C) else Color(0xFFC5A059),
                modifier = Modifier.size(16.dp).clickable { onCycleMode() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = prayer.displayTitle,
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SakinaInkDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = prayer.timeText.toWesternDigits(),
                fontFamily = SakinaNumeralFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = SakinaInkDark,
                style = TextStyle(
                    fontFeatureSettings = "tnum",
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            SakinaStarPolygonBadge(iconRes = prayer.iconRes, size = 28.dp)
        }
    }
}
