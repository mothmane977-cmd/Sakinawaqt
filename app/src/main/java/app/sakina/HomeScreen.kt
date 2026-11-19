package app.sakina

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.location.Geocoder
import kotlin.math.abs
import android.icu.util.IslamicCalendar
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
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

private enum class ExtraSection { NONE, ADHKAR, CALENDAR, DUAS, RUQYAH, ALARM, SETTINGS }

private data class BrandPrayerRow(
    val keyNameAr: String,
    val displayTitle: String,
    val timeText: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val bgRes: Int,
    val isNext: Boolean,
    val mode: Int
)

private data class IslamicOccasionDetail(
    val hijriMonth: Int,
    val hijriDay: Int,
    val titleAr: String,
    val titleEn: String,
    val hijriDateTextAr: String,
    val hijriDateTextEn: String,
    val detailsAr: String,
    val detailsEn: String
)

private val ISLAMIC_OCCASIONS = listOf(
    IslamicOccasionDetail(
        IslamicCalendar.MUHARRAM, 1,
        "رأس السنة الهجرية", "Islamic New Year (1 Muharram)",
        "1 محرم", "1 Muharram",
        "بداية العام الهجري الجديد، وشهر المحرم هو شهر الله المحرم ومن الأشهر الحرم التي يعظم فيها الأجر، وقال النبي ﷺ: «أفضل الصيام بعد رمضان شهر الله المحرم» (رواه مسلم 1163).",
        "Marks the beginning of the Hijri year and the sacred month of Allah. The Prophet ﷺ said: 'The best fasting after Ramadan is the month of Allah, Muharram.' (Sahih Muslim 1163)."
    ),
    IslamicOccasionDetail(
        IslamicCalendar.MUHARRAM, 10,
        "يوم عاشوراء (مع تاسوعاء)", "Day of Ashura (10 Muharram)",
        "9 و 10 محرم", "9 & 10 Muharram",
        "اليوم الذي نجّى الله فيه موسى عليه السلام وقومه، ويُستحب صيامه مع يوم تاسوعاء (9 محرم)؛ قال النبي ﷺ عن صيام يوم عاشوراء: «أحتسب على الله أن يكفر السنة التي قبله» (رواه مسلم 1162).",
        "The day Allah saved Musa (peace be upon him). Fasting the 9th and 10th of Muharram expiates the sins of the preceding year (Sahih Muslim 1162)."
    ),
    IslamicOccasionDetail(
        IslamicCalendar.RABI_1, 12,
        "ذكرى المولد النبوي الشريف", "Birth of the Prophet ﷺ (Rabi' al-Awwal)",
        "12 ربيع الأول", "12 Rabi' al-Awwal",
        "ذكرى مولد سيد الخلق محمد ﷺ في عام الفيل بمكة المكرمة، ويتأكد فيها وفي كل وقت الإكثار من الصلاة والسلام عليه ﷺ واتباع سنته وسيرته العطرة.",
        "Commemorates the birth of Prophet Muhammad ﷺ in Makkah, inspiring abundant blessings (Salawat) upon him and adherence to his Sunnah."
    ),
    IslamicOccasionDetail(
        IslamicCalendar.RAJAB, 27,
        "ذكرى الإسراء والمعراج", "Isra and Mi'raj",
        "27 رجب", "27 Rajab",
        "المعجزة العظيمة التي أسري فيها بالنبي ﷺ من المسجد الحرام إلى المسجد الأقصى ثم عُرج به إلى السماوات العلا، وفيها فُرضت الصلوات الخمس كل يوم وليلة لتكون معراج المؤمن وصلته بربه.",
        "The miraculous night journey of the Prophet ﷺ from Masjid al-Haram to Masjid al-Aqsa and ascension to the heavens, during which the five daily prayers were enjoined."
    ),
    IslamicOccasionDetail(
        IslamicCalendar.RAMADAN, 1,
        "حلول شهر رمضان المبارك", "Start of Blessed Ramadan",
        "1 رمضان", "1 Ramadan",
        "شهر الصيام والقيام وتلاوة القرآن، تفتح فيه أبواب الجنة وتغلق أبواب النار وتصفد الشياطين، وفيه ليلة القدر التي هي خير من ألف شهر. قال ﷺ: «من صام رمضان إيماناً واحتساباً غفر له ما تقدم من ذنبه» (متفق عليه).",
        "The month of fasting, Qiyam, and the revelation of the Qur'an, containing Laylat al-Qadr (better than a thousand months)."
    ),
    IslamicOccasionDetail(
        IslamicCalendar.SHAWWAL, 1,
        "عيد الفطر المبارك", "Eid al-Fitr",
        "1 شوال", "1 Shawwal",
        "يوم الجائزة والفرح بإتمام صيام رمضان، يجب فيه إخراج زكاة الفطر قبل صلاة العيد، ويُسن فيه التكبير والاغتسال والتجمل وأكل تمرات وتراً قبل الخروج للمصلى وصلة الأرحام، ويحرم صيام يوم العيد.",
        "The festival of breaking the fast after Ramadan. Includes Zakat al-Fitr before the Eid prayer, Takbir, eating an odd number of dates before prayer, and maintaining family ties."
    ),
    IslamicOccasionDetail(
        IslamicCalendar.DHU_AL_HIJJAH, 9,
        "يوم عرفة (والعشر الأوائل من ذي الحجة)", "Day of Arafah (9 Dhu al-Hijjah)",
        "9 ذو الحجة", "9 Dhu al-Hijjah",
        "أعظم أيام السنة، والعمل الصالح في العشر الأوائل من ذي الحجة أحب إلى الله من سائر الأيام. ويُستحب لغير الحاج صيام يوم عرفة؛ قال ﷺ: «صيام يوم عرفة أحتسب على الله أن يكفر السنة التي قبله والسنة التي بعده» (رواه مسلم 1162)، وخير الدعاء دعاء يوم عرفة.",
        "The greatest day of the year. For non-pilgrims, fasting the Day of Arafah expiates the sins of the previous year and the coming year (Sahih Muslim 1162)."
    ),
    IslamicOccasionDetail(
        IslamicCalendar.DHU_AL_HIJJAH, 10,
        "عيد الأضحى المبارك وأيام التشريق", "Eid al-Adha (10 Dhu al-Hijjah)",
        "10 ذو الحجة", "10 Dhu al-Hijjah",
        "يوم النحر وأعظم الأيام عند الله، يُشرع فيه صلاة العيد وذبح الأضاحي تقرباً إلى الله والتكبير المقيد والمطلق، ويليه أيام التشريق الثلاثة (11، 12، 13 ذو الحجة) وهي أيام أكل وشرب وذكر لله تعالى.",
        "The Festival of Sacrifice and the greatest day in the sight of Allah, followed by the three days of Tashriq (11th, 12th, 13th Dhu al-Hijjah) of remembrance and gratitude."
    )
)

private fun buildReligiousOccasionsReport(hijriOffset: Int, isArOrUr: Boolean): String {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, hijriOffset) }
    val ic = IslamicCalendar().apply { timeInMillis = cal.timeInMillis }
    val y = ic.get(IslamicCalendar.YEAR)
    val nowMs = ic.timeInMillis
    val gFmt = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    data class RankedOccasion(val detail: IslamicOccasionDetail, val diffDays: Int, val gregDate: String, val hijriYear: Int)
    val ranked = mutableListOf<RankedOccasion>()

    for (occ in ISLAMIC_OCCASIONS) {
        var bestDays = Int.MAX_VALUE
        var bestMs = nowMs
        var bestYr = y
        for (yr in y..y + 1) {
            val target = IslamicCalendar(yr, occ.hijriMonth, occ.hijriDay)
            val diff = ((target.timeInMillis - nowMs) / 86_400_000L).toInt()
            if (diff in 0 until bestDays) {
                bestDays = diff
                bestMs = target.timeInMillis
                bestYr = yr
            }
        }
        ranked.add(RankedOccasion(occ, bestDays, gFmt.format(Date(bestMs)), bestYr))
    }
    ranked.sortBy { it.diffDays }
    val nearest = ranked.first()

    return if (isArOrUr) {
        buildString {
            appendLine("❖ أقرب مناسبة دينية لليوم ❖")
            appendLine("• المناسبة: ${nearest.detail.titleAr}")
            appendLine("• التاريخ الهجري: ${nearest.detail.hijriDateTextAr} ${nearest.hijriYear} هـ")
            appendLine("• التاريخ الميلادي الموافق: ${nearest.gregDate} م")
            appendLine("• المدة المتبقية: ${if (nearest.diffDays == 0) "اليوم!" else "بعد ${nearest.diffDays} يوماً"}")
            appendLine()
            appendLine("❖ أهم ما يتعلق بهذه المناسبة ❖")
            appendLine(nearest.detail.detailsAr)
            appendLine()
            appendLine("❖ جدول المناسبات الدينية القادمة ❖")
            ranked.drop(1).forEach { r ->
                appendLine("• ${r.detail.titleAr} (${r.detail.hijriDateTextAr}): بعد ${r.diffDays} يوماً — الموافق ${r.gregDate} م")
            }
        }.toWesternDigits()
    } else {
        buildString {
            appendLine("❖ Nearest Upcoming Islamic Occasion ❖")
            appendLine("• Occasion: ${nearest.detail.titleEn}")
            appendLine("• Hijri Date: ${nearest.detail.hijriDateTextEn} ${nearest.hijriYear} AH")
            appendLine("• Gregorian Date: ${nearest.gregDate}")
            appendLine("• Countdown: ${if (nearest.diffDays == 0) "Today!" else "in ${nearest.diffDays} days"}")
            appendLine()
            appendLine("❖ Significance & Recommended Acts ❖")
            appendLine(nearest.detail.detailsEn)
            appendLine()
            appendLine("❖ Upcoming Islamic Occasions Schedule ❖")
            ranked.drop(1).forEach { r ->
                appendLine("• ${r.detail.titleEn}: in ${r.diffDays} days (${r.gregDate})")
            }
        }.toWesternDigits()
    }
}

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

@Composable
fun HomeScreen(ctx: Context, openAfterPrayer: Boolean = false) {
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }
    val autoNight = SakinaAdvancedPrefs.autoNightMode(ctx)
    var currentTheme by remember {
        mutableStateOf(SakinaLuxuryTheme.entries.getOrElse(sp.getInt("theme_index", 0)) { SakinaLuxuryTheme.ROYAL_NAVY })
    }
    var currentLang by remember {
        val code = sp.getString("app_lang", "ar") ?: "ar"
        mutableStateOf(SakinaLang.entries.firstOrNull { it.code == code } ?: SakinaLang.AR)
    }
    var currentTab by remember { mutableStateOf(SakinaNavTab.HOME) }
    var extraSection by remember { mutableStateOf(if (openAfterPrayer) ExtraSection.ADHKAR else ExtraSection.NONE) }
    var adhkarInitCat by remember { mutableStateOf<Int?>(if (openAfterPrayer) 2 else null) }
    LaunchedEffect(openAfterPrayer) {
        if (openAfterPrayer) {
            adhkarInitCat = null; extraSection = ExtraSection.ADHKAR
            adhkarInitCat = 2
        }
    }
    var openedFromRightDrawer by remember { mutableStateOf(false) }
    var openedFromLeftDrawer by remember { mutableStateOf(false) }
    var hadithTitle by remember { mutableStateOf<String?>(null) }
    var hadithBody by remember { mutableStateOf<String?>(null) }
    var showDiagnostics by remember { mutableStateOf(false) }
    val rightDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = rightDrawerState.isOpen || leftDrawerState.isOpen) {
        scope.launch {
            rightDrawerState.close()
            leftDrawerState.close()
        }
    }

    fun returnFromExtra() {
        extraSection = ExtraSection.NONE
        when {
            openedFromRightDrawer -> {
                openedFromRightDrawer = false
                scope.launch { rightDrawerState.open() }
            }
            openedFromLeftDrawer -> {
                openedFromLeftDrawer = false
                scope.launch { leftDrawerState.open() }
            }
        }
    }

    val headerImg = when (extraSection) {
        ExtraSection.ADHKAR, ExtraSection.DUAS, ExtraSection.RUQYAH -> R.drawable.bg_header_adhkar
        ExtraSection.CALENDAR -> R.drawable.bg_header_madinah
        ExtraSection.ALARM, ExtraSection.SETTINGS -> R.drawable.bg_header_mosque
        ExtraSection.NONE -> when (currentTab) {
            SakinaNavTab.HOME -> {
                val nowHr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                when (nowHr) {
                    in 4..6 -> R.drawable.bg_header_mosque
                    in 7..14 -> R.drawable.bg_header_kaaba
                    in 15..16 -> R.drawable.bg_header_madinah
                    in 17..18 -> R.drawable.bg_header_adhkar
                    else -> R.drawable.bg_header_quran
                }
            }
            SakinaNavTab.QIBLA -> R.drawable.bg_header_kaaba
            SakinaNavTab.QURAN -> R.drawable.bg_header_quran
        }
    }

    val isNightHours = remember {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        h >= 19 || h < 5
    }
    val effectiveTheme = if (autoNight && isNightHours) SakinaLuxuryTheme.MIDNIGHT_AMOLED else currentTheme
    SakinaBrandTheme(theme = effectiveTheme, lang = currentLang) {
        val s = LocalSakinaStrings.current
        val isArOrUr = currentLang == SakinaLang.AR || currentLang == SakinaLang.UR

        val appDir = if (currentLang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

        // Outer Drawer: Physical RIGHT edge (Settings, Alarms, Duas, Ruqyah, Diagnostics)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
                drawerState = rightDrawerState,
                drawerContent = {
                    CompositionLocalProvider(LocalLayoutDirection provides appDir) {
                        ModalDrawerSheet(drawerContainerColor = effectiveTheme.bgDeep, modifier = Modifier.width(292.dp)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    SakinaTitle3D(text = s.appName, fontSize = 21.sp, modifier = Modifier.weight(1f))
                                    SakinaBackIconButton(onClick = { scope.launch { rightDrawerState.close() } })
                                }
                                Text(
                                    text = if (isArOrUr) "العبادات والإعدادات" else "Worship & Settings",
                                    fontFamily = SakinaBodyFont,
                                    fontSize = 11.sp,
                                    color = effectiveTheme.accentGold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                SakinaDrawerPillItem(R.drawable.ic_duas, s.duasTitle) {
                                    scope.launch { rightDrawerState.close() }; openedFromRightDrawer = true; openedFromLeftDrawer = false; extraSection = ExtraSection.DUAS
                                }
                                SakinaDrawerPillItem(R.drawable.ic_ruqyah, s.ruqyahTitle) {
                                    scope.launch { rightDrawerState.close() }; openedFromRightDrawer = true; openedFromLeftDrawer = false; extraSection = ExtraSection.RUQYAH
                                }
                                SakinaDrawerPillItem(R.drawable.ic_alarm, s.alarmTitle) {
                                    scope.launch { rightDrawerState.close() }; openedFromRightDrawer = true; openedFromLeftDrawer = false; extraSection = ExtraSection.ALARM
                                }
                                SakinaDrawerPillItem(R.drawable.ic_settings, s.settingsTitle) {
                                    scope.launch { rightDrawerState.close() }; openedFromRightDrawer = true; openedFromLeftDrawer = false; extraSection = ExtraSection.SETTINGS
                                }
                                SakinaDrawerPillItem(
                                    R.drawable.ic_star_ornament,
                                    if (isArOrUr) "تشخيص البطارية والصلاحيات" else "Battery & Alarm Diagnostics"
                                ) {
                                    scope.launch { rightDrawerState.close() }
                                    showDiagnostics = true
                                }
                            }
                        }
                    }
                }
            ) {
                // Inner Drawer: Physical LEFT edge (Adhkar & Tasbih, Hijri Calendar, Sunan, Duha, Qiyam, Fasting, Occasions + Language)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalNavigationDrawer(
                        drawerState = leftDrawerState,
                        drawerContent = {
                            CompositionLocalProvider(LocalLayoutDirection provides appDir) {
                                ModalDrawerSheet(drawerContainerColor = effectiveTheme.bgDeep, modifier = Modifier.width(296.dp)) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .verticalScroll(rememberScrollState())
                                            .padding(horizontal = 16.dp, vertical = 20.dp),
                                        verticalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            SakinaTitle3D(text = if (isArOrUr) "الأذكار والتقويم" else "Adhkar & Calendar", fontSize = 18.sp, modifier = Modifier.weight(1f))
                                            SakinaBackIconButton(onClick = { scope.launch { leftDrawerState.close() } })
                                        }
                                        Spacer(Modifier.height(2.dp))

                                        SakinaDrawerPillItem(R.drawable.ic_tasbih, s.adhkarTitle) {
                                            scope.launch { leftDrawerState.close() }; openedFromLeftDrawer = true; openedFromRightDrawer = false; adhkarInitCat = null; extraSection = ExtraSection.ADHKAR
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_calendar, s.calendarTitle) {
                                            scope.launch { leftDrawerState.close() }; openedFromLeftDrawer = true; openedFromRightDrawer = false; extraSection = ExtraSection.CALENDAR
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_sunan, s.sunanTitle) {
                                            scope.launch { leftDrawerState.close() }
                                            hadithTitle = s.sunanTitle
                                            hadithBody = if (isArOrUr) {
                                                """
                                                ❖ تفصيل النوافل القبلية والبعدية لكل صلاة ❖

                                                1. صلاة الفجر:
                                                • القبلية: ركعتان مؤكدتان قبل الفريضة (قال ﷺ: «ركعتا الفجر خير من الدنيا وما فيها» رواه مسلم 725).
                                                • البعدية: لا نافلة بعدها حتى تشرق الشمس وترتفع.

                                                2. صلاة الظهر:
                                                • القبلية: 4 ركعات مؤكدة قبلها (بتسليمتين).
                                                • البعدية: ركعتان مؤكدتان بعدها (ويُستحب زيادة ركعتين لتكون 4 قبلها و4 بعدها؛ رواه الترمذي 428).

                                                3. صلاة العصر:
                                                • القبلية: 4 ركعات مستحبة قبلها («رحم الله امرأً صلى قبل العصر أربعاً» أبو داود 1271).
                                                • البعدية: لا نافلة بعدها حتى تغرب الشمس.

                                                4. صلاة المغرب:
                                                • القبلية: ركعتان مستحبتان (البخاري 1183).
                                                • البعدية: ركعتان مؤكدتان بعدها.

                                                5. صلاة العشاء:
                                                • القبلية: ركعتان مستحبتان.
                                                • البعدية: ركعتان مؤكدتان بعدها، ثم صلاة الوتر.
                                                """.trimIndent().toWesternDigits()
                                            } else {
                                                "1. Fajr: 2 before\n2. Dhuhr: 4 before + 2 after\n3. Asr: 4 recommended before\n4. Maghrib: 2 after\n5. Isha: 2 after + Witr (Sahih Muslim 728)."
                                            }
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_dhuhr, s.duhaTitle) {
                                            scope.launch { leftDrawerState.close() }
                                            val events = PrayerEngine.events(ctx, 0)
                                            val hm = SimpleDateFormat("HH:mm", Locale.US)
                                            val duhaStart = hm.format(Date(events.first { it.first == 1 }.second.time + 15 * 60_000L))
                                            val duhaEnd = hm.format(Date(events.first { it.first == 2 }.second.time - 10 * 60_000L))
                                            hadithTitle = "${s.duhaTitle} ($duhaStart – $duhaEnd)"
                                            hadithBody = if (isArOrUr) {
                                                "وقت صلاة الضحى اليوم: $duhaStart – $duhaEnd\n• أقلها: ركعتان (صحيح مسلم 720).\n• أوسطها: 4 ركعات (صحيح مسلم 719).\n• أفضلها: 8 ركعات مثنى مثنى (متفق عليه).".toWesternDigits()
                                            } else {
                                                "Today's Duha Window: $duhaStart – $duhaEnd\n• Minimum: 2 Rak'ahs (Muslim 720)\n• Regular: 4 Rak'ahs (Muslim 719)\n• Best: 8 Rak'ahs (Bukhari 1176 & Muslim 336)."
                                            }
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_isha, s.qiyamTitle) {
                                            scope.launch { leftDrawerState.close() }
                                            hadithTitle = s.qiyamTitle
                                            hadithBody = if (isArOrUr) {
                                                "• هدي النبي ﷺ: 11 أو 13 ركعة (متفق عليه: البخاري 1147 ومسلم 736).\n• تصلى مثنى مثنى ويُوتر بواحدة أو ثلاث (البخاري 990).\n• ينزل ربنا في الثلث الأخير من الليل فيستجيب للداعين (متفق عليه).".toWesternDigits()
                                            } else {
                                                "• Prophetic practice: 11 or 13 Rak'ahs (Bukhari 1147 & Muslim 736), prayed two by two + odd Witr (Bukhari 990)."
                                            }
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_calendar, s.fastingTitle) {
                                            scope.launch { leftDrawerState.close() }
                                            hadithTitle = s.fastingTitle
                                            hadithBody = if (isArOrUr) {
                                                "• ست من شوال (مسلم 1164)\n• يوم عرفة لغير الحاج (مسلم 1162)\n• عاشوراء وتاسوعاء (مسلم 1162)\n• الأيام البيض 13-14-15 (البخاري 1981)\n• الإثنين والخميس (مسلم 1162)\n• شهر الله المحرم وشعبان (مسلم 1163)".toWesternDigits()
                                            } else {
                                                "• 6 of Shawwal (Muslim 1164)\n• Day of Arafah (Muslim 1162)\n• Ashura 9-10 Muharram\n• White Days 13-15\n• Mondays & Thursdays"
                                            }
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_star_ornament, s.occasionsTitle) {
                                            scope.launch { leftDrawerState.close() }
                                            hadithTitle = s.occasionsTitle
                                            hadithBody = buildReligiousOccasionsReport(Prefs.hijriOffset(ctx), isArOrUr)
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        // Restore app's natural RTL/LTR LayoutDirection for main screen content
                        CompositionLocalProvider(LocalLayoutDirection provides appDir) {
                            SakinaBackgroundLayer(headerImageRes = headerImg, showMihrabFrame = true) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    when (extraSection) {
                                        ExtraSection.ADHKAR -> SakinaAdhkarScreen(ctx = ctx, initialCategoryIndex = adhkarInitCat)
                                        ExtraSection.CALENDAR -> SakinaCalendarScreen(ctx = ctx)
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
                                            Crossfade(targetState = currentTab, label = "sakina3Tabs") { tab ->
                                                when (tab) {
                                                    SakinaNavTab.HOME -> SakinaHomeContent(ctx = ctx)
                                                    SakinaNavTab.QIBLA -> SakinaQiblaScreen(ctx = ctx)
                                                    SakinaNavTab.QURAN -> SakinaQuranScreen(ctx = ctx)
                                                }
                                            }
                                        }
                                    }

                                    if (extraSection == ExtraSection.NONE) {
                                        // Top-Right Menu Button (34.dp): Opens Worship & Settings Drawer
                                        Box(
                                            modifier = Modifier
                                                .align(AbsoluteAlignment.TopRight)
                                                .statusBarsPadding()
                                                .padding(end = 15.dp, top = 14.dp)
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(effectiveTheme.bgDeep.copy(alpha = 0.88f))
                                                .border(1.dp, Sakina3DGoldBevelBrush, CircleShape)
                                                .clickable { scope.launch { rightDrawerState.open() } },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(painter = painterResource(id = R.drawable.ic_menu), contentDescription = "Menu", modifier = Modifier.size(19.dp))
                                        }

                                        // Top-Left Group: Left Drawer Button (Adhkar & Calendar) + Language Switcher Pill beside it (#1)
                                        Row(
                                            modifier = Modifier
                                                .align(AbsoluteAlignment.TopLeft)
                                                .statusBarsPadding()
                                                .padding(start = 15.dp, top = 14.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(effectiveTheme.bgDeep.copy(alpha = 0.88f))
                                                    .border(1.dp, Sakina3DGoldBevelBrush, CircleShape)
                                                    .clickable { scope.launch { leftDrawerState.open() } },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(painter = painterResource(id = R.drawable.ic_tasbih), contentDescription = "Adhkar & Calendar", modifier = Modifier.size(20.dp))
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(effectiveTheme.bgDeep.copy(alpha = 0.88f))
                                                    .border(1.dp, Sakina3DGoldBevelBrush, CircleShape)
                                                    .clickable {
                                                        val next = SakinaLang.entries[(currentLang.ordinal + 1) % SakinaLang.entries.size]
                                                        currentLang = next
                                                        sp.edit().putString("app_lang", next.code).apply()
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(painter = painterResource(id = R.drawable.ic_language), contentDescription = "Language", alpha = 0.35f, modifier = Modifier.size(22.dp))
                                                Text(
                                                    text = currentLang.code.uppercase(),
                                                    fontFamily = SakinaNumeralFont,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = effectiveTheme.accentBright,
                                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                                )
                                            }
                                        }
                                    } else if (extraSection == ExtraSection.ADHKAR || extraSection == ExtraSection.CALENDAR) {
                                        // Back button on top-left when viewing Adhkar or Calendar from Left Drawer
                                        SakinaBackIconButton(
                                            onClick = { returnFromExtra() },
                                            modifier = Modifier
                                                .align(AbsoluteAlignment.TopLeft)
                                                .statusBarsPadding()
                                                .padding(start = 16.dp, top = 54.dp)
                                        )
                                    }

                                    SakinaBottomPillBar(
                                        currentTab = currentTab,
                                        onTabSelected = { extraSection = ExtraSection.NONE; openedFromRightDrawer = false; openedFromLeftDrawer = false; currentTab = it },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        if (showDiagnostics) {
            val pm = remember { ctx.getSystemService(Context.POWER_SERVICE) as PowerManager }
            val batteryExempt = remember(showDiagnostics) { pm.isIgnoringBatteryOptimizations(ctx.packageName) }
            val exactAlarmOk = remember(showDiagnostics) {
                if (Build.VERSION.SDK_INT >= 31) ctx.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() ?: true
                else true
            }
            val nm = remember { ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
            val notifOk = remember(showDiagnostics) { nm.areNotificationsEnabled() }

            AlertDialog(
                onDismissRequest = { showDiagnostics = false },
                containerColor = effectiveTheme.bgCard,
                title = {
                    Text(
                        text = if (isArOrUr) "تشخيص الموثوقية والبطارية" else "Battery & Alarm Diagnostics",
                        fontFamily = SakinaHeadingFont,
                        fontWeight = FontWeight.Bold,
                        color = effectiveTheme.accentBright
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${if (notifOk) "✓" else "⚠"} ${if (isArOrUr) "صلاحية الإشعارات" else "Notifications"}: ${if (notifOk) (if (isArOrUr) "مفعّلة" else "Enabled") else (if (isArOrUr) "معطّلة" else "Disabled")}",
                            fontFamily = SakinaBodyFont,
                            color = if (notifOk) SakinaBrandColors.StableEmerald else Color(0xFFF59E0B)
                        )
                        Text(
                            text = "${if (exactAlarmOk) "✓" else "⚠"} ${if (isArOrUr) "التنبيهات الدقيقة (Exact Alarm)" else "Exact Alarms"}: ${if (exactAlarmOk) (if (isArOrUr) "مفعّلة" else "Allowed") else (if (isArOrUr) "تحتاج إذن" else "Needs Permission")}",
                            fontFamily = SakinaBodyFont,
                            color = if (exactAlarmOk) SakinaBrandColors.StableEmerald else Color(0xFFF59E0B)
                        )
                        Text(
                            text = "${if (batteryExempt) "✓" else "⚠"} ${if (isArOrUr) "استثناء توفير البطارية" else "Battery Optimization Exempt"}: ${if (batteryExempt) (if (isArOrUr) "مستثنى" else "Exempt") else (if (isArOrUr) "مقيّد (قد يتأخر الأذان)" else "Restricted")}",
                            fontFamily = SakinaBodyFont,
                            color = if (batteryExempt) SakinaBrandColors.StableEmerald else Color(0xFFF59E0B)
                        )
                        if (!notifOk) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x33F59E0B))
                                    .clickable {
                                        try {
                                            ctx.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        } catch (_: Exception) {}
                                    }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isArOrUr) "تفعيل صلاحية الإشعارات" else "Enable Notifications", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = effectiveTheme.accentBright)
                            }
                        }
                        if (!exactAlarmOk && Build.VERSION.SDK_INT >= 31) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x33F59E0B))
                                    .clickable {
                                        try {
                                            ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                data = Uri.parse("package:${ctx.packageName}")
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        } catch (_: Exception) {}
                                    }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isArOrUr) "منح إذن التنبيهات الدقيقة" else "Grant Exact Alarm Permission", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = effectiveTheme.accentBright)
                            }
                        }
                        Text(
                            text = if (isArOrUr) "ملاحظة لهواتف Xiaomi / Samsung: فعّل (التشغيل التلقائي Autostart) واختر (بلا قيود على البطارية) لضمان انطلاق الأذان دائماً." else "Xiaomi/Samsung tip: Enable Autostart and set Battery to Unrestricted.",
                            fontFamily = SakinaBodyFont,
                            fontSize = 11.sp,
                            color = SakinaBrandColors.SoftSilver
                        )
                        if (!batteryExempt) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Brush.verticalGradient(listOf(effectiveTheme.accentBright, effectiveTheme.accentGold)))
                                    .clickable {
                                        try {
                                            ctx.startActivity(
                                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                    data = Uri.parse("package:${ctx.packageName}")
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                            )
                                        } catch (_: Exception) {}
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isArOrUr) "استثناء التطبيق من توفير البطارية الآن" else "Disable Battery Optimization",
                                    fontFamily = SakinaHeadingFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = effectiveTheme.bgDeep
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    SakinaBackIconButton(onClick = { showDiagnostics = false })
                }
            )
        }

        if (hadithTitle != null && hadithBody != null) {
            AlertDialog(
                onDismissRequest = { hadithTitle = null },
                containerColor = currentTheme.bgCard,
                title = {
                    Text(
                        text = hadithTitle!!,
                        fontFamily = SakinaHeadingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = currentTheme.accentBright
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = hadithBody!!,
                            fontFamily = SakinaBodyFont,
                            fontSize = 13.5.sp,
                            color = SakinaBrandColors.LuminousIvory,
                            lineHeight = 22.sp
                        )
                    }
                },
                confirmButton = {
                    SakinaBackIconButton(onClick = { hadithTitle = null; scope.launch { leftDrawerState.open() } })
                }
            )
        }
    }
}

@Composable
private fun SakinaHomeContent(ctx: Context) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var dayOffset by remember { mutableIntStateOf(0) }
    var modeTick by remember { mutableIntStateOf(0) }
    var analogPrimary by remember { mutableStateOf(false) }
    var showCurrentTimeInCartouche by remember { mutableStateOf(false) }
    var locRefreshTick by remember { mutableIntStateOf(0) }

    // Auto-update Location via GPS when enabled in Settings (#7) + Live Notification Watchdog
    LaunchedEffect(Unit) {
        val spPrefs = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        if (spPrefs.getBoolean("auto_gps_location", true)) {
            val loc = Place.fromDevice(ctx)
            if (loc != null) {
                val cur = Place.coords(ctx)
                if (abs(loc.latitude - cur.latitude) > 0.03 || abs(loc.longitude - cur.longitude) > 0.03) {
                    var resolved = Place.name(ctx)
                    try {
                        @Suppress("DEPRECATION")
                        val list = Geocoder(ctx, lang.locale).getFromLocation(loc.latitude, loc.longitude, 1)
                        val city = list?.firstOrNull()?.locality ?: list?.firstOrNull()?.subAdminArea
                        if (!city.isNullOrBlank()) resolved = city
                    } catch (_: Exception) {}
                    Place.set(ctx, loc.latitude, loc.longitude, resolved)
                    AlarmScheduler.scheduleNext(ctx)
                    locRefreshTick++
                }
            }
        }
        SakinaLiveNotifier.update(ctx)
        var secTick = 0
        while (true) {
            nowMs = System.currentTimeMillis()
            secTick++
            if (secTick % 15 == 0) {
                SakinaLiveNotifier.update(ctx)
            }
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
            BrandPrayerRow(
                keyNameAr = keyAr,
                displayTitle = s.prayers.getOrElse(idx) { keyAr },
                timeText = SakinaAdvancedPrefs.formatPrayerTime(ctx, date),
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
    val localizedCity = remember(lang, locRefreshTick) { localizeCityName(Place.name(ctx), lang) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 46.dp, bottom = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header with larger icons (#19)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SakinaTitle3D(text = s.appName, fontSize = 22.sp)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(theme.bgCard)
                        .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                        .clickable { dayOffset-- },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.bgCard.copy(alpha = 0.88f))
                        .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 3.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(datePillText, fontFamily = SakinaEditorialFont, fontSize = 10.5.sp, color = SakinaBrandColors.LuminousIvory, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(theme.bgCard)
                        .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                        .clickable { dayOffset++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = localizedCity,
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
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

        // #8: User chooses either Luxury Digital Countdown Cartouche OR Luxury Analog Clock with a single tap!
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "❖ ${s.nextPrayer}: $nextDisplayName ❖",
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = theme.accentBright,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val hmsFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
                if (!analogPrimary) {
                    SakinaOrnamentCartouche(
                        modifier = Modifier.clickable { showCurrentTimeInCartouche = !showCurrentTimeInCartouche },
                        contentPadding = PaddingValues(horizontal = 38.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(painter = painterResource(id = R.drawable.ic_fajr), contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (showCurrentTimeInCartouche) hmsFmt.format(Date(nowMs)).toWesternDigits() else countdownText,
                                fontFamily = SakinaNumeralFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = theme.accentBright,
                                maxLines = 1,
                                style = TextStyle(
                                    fontFeatureSettings = "tnum",
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                    }
                } else {
                    SakinaAnalogClockDial(
                        timeMs = nowMs,
                        size = 84.dp
                    )
                }

                // Sleek 1-tap toggle button on the side to switch between Digital & Analog Clock (#8)
                Box(
                    modifier = Modifier
                        .align(AbsoluteAlignment.CenterRight)
                        .padding(end = 6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(theme.bgCard.copy(alpha = 0.90f))
                        .border(0.9.dp, Sakina3DGoldBevelBrush, CircleShape)
                        .clickable { analogPrimary = !analogPrimary },
                    contentAlignment = Alignment.Center
                ) {
                    if (analogPrimary) {
                        Text(
                            text = "00",
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = theme.accentBright,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    } else {
                        SakinaAnalogClockDial(timeMs = nowMs, size = 24.dp)
                    }
                }
            }
        }

        // #9: Every prayer row is its own fixed, standalone, gold-beveled button in natural chronological order!
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            rows.forEach { prayer ->
                val cycleAction: () -> Unit = {
                    val nextMode = when (prayer.mode) { 0, 1 -> 2; 2 -> 3; else -> 0 }
                    Modes.set(ctx, prayer.keyNameAr, nextMode)
                    AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); modeTick++
                }
                if (prayer.isNext) {
                    SakinaFeaturedNextPrayerCard(
                        prayer = prayer,
                        nextLabel = s.nextPrayer,
                        onCycleMode = cycleAction
                    )
                } else {
                    SakinaStandalonePrayerButton(
                        prayer = prayer,
                        onCycleMode = cycleAction
                    )
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
            .height(56.dp)
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
                    modifier = Modifier.size(21.dp).clickable { onCycleMode() }
                )
                Spacer(modifier = Modifier.width(9.dp))
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
                SakinaStarPolygonBadge(iconRes = prayer.iconRes, size = 38.dp)
            }
        }
    }
}

@Composable
private fun SakinaStandalonePrayerButton(prayer: BrandPrayerRow, onCycleMode: () -> Unit) {
    val theme = LocalSakinaTheme.current
    val btnShape = RoundedCornerShape(15.dp)
    val modeIcon = when (prayer.mode) {
        2 -> Icons.Rounded.Vibration
        3 -> Icons.Rounded.NotificationsOff
        else -> Icons.Rounded.NotificationsActive
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .shadow(4.dp, btnShape)
            .clip(btnShape)
            .background(Brush.verticalGradient(listOf(theme.parchmentTop, theme.parchmentBottom)))
            .border(1.1.dp, Sakina3DGoldBevelBrush, btnShape)
            .clickable { onCycleMode() }
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = modeIcon,
                contentDescription = null,
                tint = if (prayer.mode == 3) Color(0xFF9E8C6C) else Color(0xFFC5A059),
                modifier = Modifier.size(18.dp).clickable { onCycleMode() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = prayer.displayTitle,
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.2.sp,
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
                fontSize = 13.8.sp,
                color = SakinaInkDark,
                style = TextStyle(
                    fontFeatureSettings = "tnum",
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            SakinaStarPolygonBadge(iconRes = prayer.iconRes, size = 32.dp)
        }
    }
}
