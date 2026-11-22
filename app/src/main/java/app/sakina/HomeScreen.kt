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
import androidx.compose.material.icons.rounded.Settings
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

private enum class ExtraSection { NONE, ADHKAR, DUAS, RUQYAH, ALARM, NOTIFICATIONS, APPEARANCE, SETTINGS, SUNNAH, NAMES_OF_ALLAH }

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
    var showRatingDialog by remember { mutableStateOf(SakinaAdvancedPrefs.shouldShowRatingPrompt(ctx)) }
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
        ExtraSection.ADHKAR, ExtraSection.DUAS, ExtraSection.RUQYAH, ExtraSection.SUNNAH, ExtraSection.NAMES_OF_ALLAH -> R.drawable.bg_header_adhkar
        ExtraSection.ALARM, ExtraSection.NOTIFICATIONS, ExtraSection.APPEARANCE, ExtraSection.SETTINGS -> R.drawable.bg_header_mosque
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
            SakinaNavTab.CALENDAR -> R.drawable.bg_header_madinah
        }
    }

    val isNightHours = remember {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        h >= 19 || h < 5
    }
    var appearanceTick by remember { mutableIntStateOf(0) }
    val appFontStyle = remember(appearanceTick) { SakinaAdvancedPrefs.appFontStyle(ctx) }
    val appFontScale = remember(appearanceTick) { SakinaAdvancedPrefs.readingFontScale(ctx) }
    val appTextColorIdx = remember(appearanceTick) { SakinaAdvancedPrefs.appTextColorIndex(ctx) }
    val effectiveTheme = if (autoNight && isNightHours) SakinaLuxuryTheme.MIDNIGHT_AMOLED else currentTheme
    SakinaBrandTheme(
        theme = effectiveTheme,
        lang = currentLang,
        fontStyle = appFontStyle,
        fontScale = appFontScale,
        textColorIdx = appTextColorIdx
    ) {
        val s = LocalSakinaStrings.current
        val isArOrUr = currentLang == SakinaLang.AR || currentLang == SakinaLang.UR

        val appDir = if (currentLang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

        // Outer Drawer: Physical RIGHT edge (Settings, Alarms, Duas, Ruqyah, Diagnostics)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
                drawerState = rightDrawerState,
                gesturesEnabled = rightDrawerState.isOpen,
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
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    SakinaTitle3D(
                                        text = sakinaTr(currentLang, "الإعدادات العامة", "General Settings", "Paramètres", "Genel Ayarlar", "Pengaturan", "عمومی ترتیبات"),
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    SakinaBackIconButton(onClick = { scope.launch { rightDrawerState.close() } })
                                }
                                Text(
                                    text = sakinaTr(currentLang, "التنبيهات والمنبهات والضبط", "Alarms, Notifications & Settings", "Alarmes, notifications et réglages", "Alarmlar, Bildirimler ve Ayarlar", "Alarm, Notifikasi & Pengaturan", "الارم، اطلاعات اور ترتیبات"),
                                    fontFamily = SakinaBodyFont,
                                    fontSize = 11.sp,
                                    color = effectiveTheme.accentGold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                SakinaDrawerPillItem(R.drawable.ic_alarm, sakinaTr(currentLang, "المنبهات الصوتية", "Sound Alarms", "Alarmes sonores", "Sesli Alarmlar", "Alarm Suara", "صوتی الارم")) {
                                    scope.launch { rightDrawerState.close() }; openedFromRightDrawer = true; openedFromLeftDrawer = false; extraSection = ExtraSection.ALARM
                                }
                                SakinaDrawerPillItem(R.drawable.ic_after_prayer, sakinaTr(currentLang, "التنبيهات الهادئة", "Quiet Notifications", "Notifications discrètes", "Sessiz Bildirimler", "Notifikasi Tenang", "خاموش اطلاعات")) {
                                    scope.launch { rightDrawerState.close() }; openedFromRightDrawer = true; openedFromLeftDrawer = false; extraSection = ExtraSection.NOTIFICATIONS
                                }
                                SakinaDrawerPillItem(R.drawable.ic_star_ornament, sakinaTr(currentLang, "المظهر والخطوط", "Appearance & Fonts", "Apparence et polices", "Görünüm ve Yazı Tipi", "Tampilan & Font", "ظاہری شکل اور فونٹس")) {
                                    scope.launch { rightDrawerState.close() }; openedFromRightDrawer = true; openedFromLeftDrawer = false; extraSection = ExtraSection.APPEARANCE
                                }
                                SakinaDrawerPillItem(R.drawable.ic_settings, sakinaTr(currentLang, "الإعدادات العامة", "General Settings", "Paramètres généraux", "Genel Ayarlar", "Pengaturan Umum", "عمومی ترتیبات")) {
                                    scope.launch { rightDrawerState.close() }; openedFromRightDrawer = true; openedFromLeftDrawer = false; extraSection = ExtraSection.SETTINGS
                                }
                                SakinaDrawerPillItem(
                                    R.drawable.ic_star_ornament,
                                    sakinaTr(currentLang, "تشخيص البطارية والصلاحيات", "Battery & Alarm Diagnostics", "Diagnostic batterie et alarmes", "Pil ve Alarm Tanılama", "Diagnostik Baterai & Alarm", "بیٹری اور الارم تشخیص")
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
                        gesturesEnabled = leftDrawerState.isOpen,
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
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            SakinaTitle3D(
                                                text = sakinaTr(currentLang, "الأذكار والعبادات", "Adhkar & Worship", "Invocations & Culte", "Zikir ve İbadetler", "Dzikir & Ibadah", "اذکار اور عبادات"),
                                                fontSize = 14.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            SakinaBackIconButton(onClick = { scope.launch { leftDrawerState.close() } })
                                        }
                                        Spacer(Modifier.height(2.dp))

                                        SakinaDrawerPillItem(R.drawable.ic_tasbih, s.adhkarTitle) {
                                            scope.launch { leftDrawerState.close() }; openedFromLeftDrawer = true; openedFromRightDrawer = false; adhkarInitCat = null; extraSection = ExtraSection.ADHKAR
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_duas, sakinaTr(currentLang, "أدعية من القرآن والسنة", "Duas from Quran & Sunnah", "Invocations du Coran et de la Sunna", "Kur'an ve Sünnetten Dualar", "Doa Al-Qur'an & Sunnah", "قرآن و سنت کی دعائیں")) {
                                            scope.launch { leftDrawerState.close() }; openedFromLeftDrawer = true; openedFromRightDrawer = false; extraSection = ExtraSection.DUAS
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_ruqyah, s.ruqyahTitle) {
                                            scope.launch { leftDrawerState.close() }; openedFromLeftDrawer = true; openedFromRightDrawer = false; extraSection = ExtraSection.RUQYAH
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_sunan, sakinaTr(currentLang, "تذكير بالعبادات وصالح الأعمال", "Worship & Good Deeds", "Rappel d'adorations et bonnes œuvres", "İbadet ve Salih Amel Hatırlatıcısı", "Pengingat Ibadah & Amal Saleh", "عبادات اور نیک اعمال کی یاد دہانی")) {
                                            scope.launch { leftDrawerState.close() }; openedFromLeftDrawer = true; openedFromRightDrawer = false; extraSection = ExtraSection.SUNNAH
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_star_ornament, sakinaTr(currentLang, "أسماء الله الحسنى (99)", "99 Names of Allah", "Les 99 Noms d'Allah", "Esmaül Hüsna (99)", "Asmaul Husna (99)", "اسماء الحسنیٰ (99)")) {
                                            scope.launch { leftDrawerState.close() }; openedFromLeftDrawer = true; openedFromRightDrawer = false; extraSection = ExtraSection.NAMES_OF_ALLAH
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_mosque, sakinaTr(currentLang, "أقرب المساجد حولي", "Nearby Mosques", "Mosquées à proximité", "Yakındaki Camiler", "Masjid Terdekat", "قریبی مساجد")) {
                                            scope.launch { leftDrawerState.close() }
                                            SakinaAdvancedPrefs.openNearbyMosques(ctx, currentLang)
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
                                                if (currentLang == SakinaLang.FR) "1. Fajr : 2 avant\n2. Dhuhr : 4 avant + 2 après\n3. Asr : 4 recommandées avant\n4. Maghrib : 2 après\n5. Isha : 2 après + Witr (Sahih Mouslim 728)." else "1. Fajr: 2 before\n2. Dhuhr: 4 before + 2 after\n3. Asr: 4 recommended before\n4. Maghrib: 2 after\n5. Isha: 2 after + Witr (Sahih Muslim 728)."
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
                                                if (currentLang == SakinaLang.FR) "Horaire de Duha aujourd'hui : $duhaStart – $duhaEnd\n• Minimum : 2 Rak'ahs (Mouslim 720)\n• Habituel : 4 Rak'ahs (Mouslim 719)\n• Meilleur : 8 Rak'ahs (Boukhari 1176 & Mouslim 336)." else "Today's Duha Window: $duhaStart – $duhaEnd\n• Minimum: 2 Rak'ahs (Muslim 720)\n• Regular: 4 Rak'ahs (Muslim 719)\n• Best: 8 Rak'ahs (Bukhari 1176 & Muslim 336)."
                                            }
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_isha, s.qiyamTitle) {
                                            scope.launch { leftDrawerState.close() }
                                            hadithTitle = s.qiyamTitle
                                            hadithBody = if (isArOrUr) {
                                                "• هدي النبي ﷺ: 11 أو 13 ركعة (متفق عليه: البخاري 1147 ومسلم 736).\n• تصلى مثنى مثنى ويُوتر بواحدة أو ثلاث (البخاري 990).\n• ينزل ربنا في الثلث الأخير من الليل فيستجيب للداعين (متفق عليه).".toWesternDigits()
                                            } else {
                                                if (currentLang == SakinaLang.FR) "• Pratique prophétique : 11 ou 13 Rak'ahs (Boukhari 1147 & Mouslim 736), priées deux par deux + le Witr impair (Boukhari 990)." else "• Prophetic practice: 11 or 13 Rak'ahs (Bukhari 1147 & Muslim 736), prayed two by two + odd Witr (Bukhari 990)."
                                            }
                                        }
                                        SakinaDrawerPillItem(R.drawable.ic_calendar, s.fastingTitle) {
                                            scope.launch { leftDrawerState.close() }
                                            hadithTitle = s.fastingTitle
                                            hadithBody = if (isArOrUr) {
                                                "• ست من شوال (مسلم 1164)\n• يوم عرفة لغير الحاج (مسلم 1162)\n• عاشوراء وتاسوعاء (مسلم 1162)\n• الأيام البيض 13-14-15 (البخاري 1981)\n• الإثنين والخميس (مسلم 1162)\n• شهر الله المحرم وشعبان (مسلم 1163)".toWesternDigits()
                                            } else {
                                                if (currentLang == SakinaLang.FR) "• 6 jours de Chawwal (Mouslim 1164)\n• Jour d'Arafat (Mouslim 1162)\n• Achoura 9-10 Mouharram\n• Jours blancs 13-14-15\n• Lundis et jeudis" else "• 6 of Shawwal (Muslim 1164)\n• Day of Arafah (Muslim 1162)\n• Ashura 9-10 Muharram\n• White Days 13-15\n• Mondays & Thursdays"
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
                                        ExtraSection.DUAS -> SakinaDuasScreen(onBackToHome = { returnFromExtra() })
                                        ExtraSection.RUQYAH -> SakinaRuqyahScreen(onBackToHome = { returnFromExtra() })
                                        ExtraSection.SUNNAH -> SakinaSunnahScreen(ctx = ctx, onBackClick = { returnFromExtra() })
                                        ExtraSection.NAMES_OF_ALLAH -> SakinaNamesOfAllahScreen(ctx = ctx, onBack = { returnFromExtra() })
                                        ExtraSection.ALARM -> SakinaAlarmScreen(ctx = ctx, initialSection = 0, onBackClick = { returnFromExtra() })
                                        ExtraSection.NOTIFICATIONS -> SakinaAlarmScreen(ctx = ctx, initialSection = 1, onBackClick = { returnFromExtra() })
                                        ExtraSection.APPEARANCE -> SakinaSettingsScreen(
                                            ctx = ctx,
                                            selectedTheme = currentTheme,
                                            selectedLang = currentLang,
                                            onThemeChange = { t -> currentTheme = t; sp.edit().putInt("theme_index", t.ordinal).apply() },
                                            onLangChange = { l -> currentLang = l; sp.edit().putString("app_lang", l.code).apply() },
                                            onBackClick = { returnFromExtra() },
                                            appearanceOnly = true,
                                            onAppearanceChanged = { appearanceTick++ }
                                        )
                                        ExtraSection.SETTINGS -> SakinaSettingsScreen(
                                            ctx = ctx,
                                            selectedTheme = currentTheme,
                                            selectedLang = currentLang,
                                            onThemeChange = { t -> currentTheme = t; sp.edit().putInt("theme_index", t.ordinal).apply() },
                                            onLangChange = { l -> currentLang = l; sp.edit().putString("app_lang", l.code).apply() },
                                            onBackClick = { returnFromExtra() },
                                            appearanceOnly = false,
                                            onAppearanceChanged = { appearanceTick++ }
                                        )
                                        ExtraSection.NONE -> {
                                            Crossfade(targetState = currentTab, label = "sakina4Tabs") { tab ->
                                                when (tab) {
                                                    SakinaNavTab.HOME -> SakinaHomeContent(ctx = ctx)
                                                    SakinaNavTab.QIBLA -> SakinaQiblaScreen(ctx = ctx)
                                                    SakinaNavTab.QURAN -> SakinaQuranScreen(ctx = ctx)
                                                    SakinaNavTab.CALENDAR -> SakinaCalendarScreen(ctx = ctx)
                                                }
                                            }
                                        }
                                    }

                                    if (extraSection == ExtraSection.NONE) {
                                        // v29 #5: Top-Right Dedicated Golden Settings Gear Icon -> General Settings Drawer
                                        Box(
                                            modifier = Modifier
                                                .align(AbsoluteAlignment.TopRight)
                                                .statusBarsPadding()
                                                .padding(end = 16.dp, top = 14.dp)
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(effectiveTheme.bgDeep.copy(alpha = 0.88f))
                                                .border(1.1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp))
                                                .clickable { scope.launch { rightDrawerState.open() } },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Settings,
                                                contentDescription = sakinaTr(currentLang, "الإعدادات", "Settings", "Paramètres", "Ayarlar", "Pengaturan", "ترتیبات"),
                                                tint = effectiveTheme.accentBright,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        // Top-Left Group pinned in LTR so Language Switcher NEVER jumps across AR/UR/EN/FR (Point 5)
                                        Box(
                                            modifier = Modifier
                                                .align(AbsoluteAlignment.TopLeft)
                                                .statusBarsPadding()
                                                .padding(start = 16.dp, top = 14.dp)
                                        ) {
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SakinaGraduatedMenuIcon(
                                                mirror = true,
                                                onClick = { scope.launch { leftDrawerState.open() } }
                                            )

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
                                        }
                                        }
                                    } else if (extraSection == ExtraSection.ADHKAR) {
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


        // v26: Sleek 1-Time First-Launch Setup Card (10-second guided setup, adapts to OEM brand, never repeats)
        var showOnboarding by remember { mutableStateOf(!sp.getBoolean("onboarding_done_v1", false)) }
        if (showOnboarding) {
            val pmOb = remember { ctx.getSystemService(Context.POWER_SERVICE) as PowerManager }
            val nmOb = remember { ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
            val exactOkOb = remember(showOnboarding) {
                if (Build.VERSION.SDK_INT >= 31) ctx.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() ?: true
                else true
            }
            val notifOkOb = remember(showOnboarding) { nmOb.areNotificationsEnabled() }
            val battOkOb = remember(showOnboarding) { pmOb.isIgnoringBatteryOptimizations(ctx.packageName) }
            val brandRaw = remember { Build.MANUFACTURER.orEmpty() }
            val brandLow = remember { brandRaw.lowercase(Locale.US) }
            val isAggressiveOem = remember {
                listOf("xiaomi", "redmi", "poco", "huawei", "honor", "oppo", "realme", "oneplus", "vivo", "tecno", "infinix").any { brandLow.contains(it) }
            }

            AlertDialog(
                onDismissRequest = {
                    sp.edit().putBoolean("onboarding_done_v1", true).apply()
                    showOnboarding = false
                },
                containerColor = effectiveTheme.bgCard,
                title = {
                    Text(
                        text = sakinaTr(
                            currentLang,
                            "أهلاً بك في سَكِينة 🕌",
                            "Welcome to Sakina 🕌",
                            "Bienvenue sur Sakina 🕌",
                            "Sakina'ya Hoş Geldiniz 🕌",
                            "Selamat Datang di Sakina 🕌",
                            "سکینہ میں خوش آمدید 🕌"
                        ),
                        fontFamily = SakinaHeadingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = effectiveTheme.accentBright
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = sakinaTr(
                                currentLang,
                                "خطوة سريعة لمرة واحدة (10 ثوانٍ) لضمان انطلاق الأذان في وقته بالثانية حتى عند إغلاق الشاشة:",
                                "A quick one-time step (10 seconds) so Adhan rings on time even when your screen is locked:",
                                "Une étape rapide (10 secondes) pour garantir l'Adhan à l'heure même écran verrouillé :",
                                "Ekran kapalıyken bile ezanın tam vaktinde çalması için 10 saniyelik tek seferlik adım:",
                                "Langkah cepat (10 detik) agar Adzan berkumandang tepat waktu meski layar terkunci:",
                                "اسکرین لاک ہونے پر بھی وقت پر اذان کے لیے صرف 10 سیکنڈ کا ایک قدم:"
                            ),
                            fontFamily = SakinaBodyFont,
                            fontSize = 12.5.sp,
                            color = SakinaBrandColors.LuminousIvory
                        )

                        if (!notifOkOb || !exactOkOb) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.verticalGradient(listOf(effectiveTheme.accentBright, effectiveTheme.accentGold)))
                                    .clickable {
                                        try {
                                            if (!notifOkOb) {
                                                ctx.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                    putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                })
                                            } else if (Build.VERSION.SDK_INT >= 31) {
                                                ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                    data = Uri.parse("package:${ctx.packageName}")
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                })
                                            }
                                        } catch (_: Exception) {}
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sakinaTr(
                                        currentLang,
                                        "1. تفعيل الأذان والتنبيهات الدقيقة",
                                        "1. Enable Adhan & Exact Alarms",
                                        "1. Activer l'Adhan et les alarmes exactes",
                                        "1. Ezan ve Tam Alarmları Etkinleştir",
                                        "1. Aktifkan Adzan & Alarm Tepat",
                                        "1. اذان اور درست الارم فعال کریں"
                                    ),
                                    fontFamily = SakinaHeadingFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = effectiveTheme.bgDeep,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        if (isAggressiveOem && !battOkOb) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x28D4AF37))
                                    .border(1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp))
                                    .clickable {
                                        val oemIntents = listOf(
                                            android.content.ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
                                            android.content.ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                                            android.content.ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
                                            android.content.ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
                                        )
                                        var ok = false
                                        for (cn in oemIntents) {
                                            try {
                                                ctx.startActivity(Intent().setComponent(cn).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                                ok = true; break
                                            } catch (_: Exception) {}
                                        }
                                        if (!ok) {
                                            try {
                                                ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = Uri.parse("package:${ctx.packageName}")
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                })
                                            } catch (_: Exception) {}
                                        }
                                    }
                                    .padding(vertical = 9.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sakinaTr(
                                        currentLang,
                                        "لهواتف $brandRaw: اضغط هنا واختر «بلا قيود» للبطارية",
                                        "For $brandRaw phones: tap here & set Battery to Unrestricted",
                                        "Pour $brandRaw : touchez ici et choisissez « Aucune restriction »",
                                        "$brandRaw telefonlar: pil kısıtlamasını kaldırın",
                                        "Untuk HP $brandRaw: atur baterai Tanpa Batasan",
                                        "$brandRaw فون کے لیے: بیٹری کو بلا پابندی سیٹ کریں"
                                    ),
                                    fontFamily = SakinaHeadingFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = effectiveTheme.accentBright,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        if (notifOkOb && exactOkOb && (!isAggressiveOem || battOkOb)) {
                            Text(
                                text = sakinaTr(
                                    currentLang,
                                    "✓ جهازك جاهز تماماً! الأذان سيعمل بدقة بإذن الله.",
                                    "✓ Your device is all set! Adhan will ring accurately.",
                                    "✓ Votre appareil est prêt ! L'Adhan sonnera à l'heure.",
                                    "✓ Cihazınız hazır! Ezan tam vaktinde çalacaktır.",
                                    "✓ Perangkat Anda siap! Adzan akan berbunyi tepat waktu.",
                                    "✓ آپ کا فون بالکل تیار ہے! اذان وقت پر چلے گی۔"
                                ),
                                fontFamily = SakinaHeadingFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = SakinaBrandColors.StableEmerald
                            )
                        }
                    }
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.verticalGradient(listOf(effectiveTheme.accentBright, effectiveTheme.accentGold)))
                            .clickable {
                                sp.edit().putBoolean("onboarding_done_v1", true).apply()
                                try { AlarmScheduler.scheduleNext(ctx) } catch (_: Exception) {}
                                showOnboarding = false
                            }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = sakinaTr(currentLang, "ابدأ الاستخدام", "Get Started", "Commencer", "Başla", "Mulai", "شروع کریں"),
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = effectiveTheme.bgDeep
                        )
                    }
                }
            )
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
                        text = sakinaTr(currentLang, "تشخيص الموثوقية والبطارية", "Battery & Alarm Diagnostics", "Diagnostic batterie et alarmes", "Pil ve Alarm Tanılama", "Diagnostik Baterai & Alarm", "بیٹری اور الارم تشخیص"),
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x28D4AF37))
                                .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(10.dp))
                                .clickable {
                                    val oemIntents = listOf(
                                        android.content.ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
                                        android.content.ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"),
                                        android.content.ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                                        android.content.ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
                                        android.content.ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
                                    )
                                    var launched = false
                                    for (cn in oemIntents) {
                                        try {
                                            ctx.startActivity(Intent().setComponent(cn).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                            launched = true
                                            break
                                        } catch (_: Exception) {}
                                    }
                                    if (!launched) {
                                        try {
                                            ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${ctx.packageName}")
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                        } catch (_: Exception) {}
                                    }
                                }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sakinaTr(currentLang, "فتح إعدادات التشغيل التلقائي والبطارية (Autostart)", "Open Autostart & Battery Settings", "Ouvrir démarrage auto et batterie", "Otomatik Başlatma ve Pil Ayarları", "Buka Pengaturan Autostart & Baterai", "آٹو اسٹارٹ اور بیٹری ترتیبات کھولیں"),
                                fontFamily = SakinaHeadingFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = effectiveTheme.accentBright
                            )
                        }
                        val recentLogs = remember(showDiagnostics) { DiagLog.get(ctx).take(6) }
                        if (recentLogs.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xAA040D14))
                                    .border(0.6.dp, Color(0x44C5A059), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = sakinaTr(currentLang, "سجل آخر التنبيهات (DiagLog):", "Recent Alarm Log:", "Journal des dernières alarmes :", "Son Alarm Kayıtları:", "Log Alarm Terbaru:", "حالیہ الارم لاگ:"),
                                    fontFamily = SakinaHeadingFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = effectiveTheme.accentGold
                                )
                                recentLogs.forEach { entry ->
                                    Text(
                                        text = "• $entry".toWesternDigits(),
                                        fontFamily = SakinaBodyFont,
                                        fontSize = 9.5.sp,
                                        color = SakinaBrandColors.SoftSilver,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
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
    val minuteBucket = nowMs / 60_000L
    var trackerTick by remember { mutableIntStateOf(0) }
    var showMonthlyStats by remember { mutableStateOf(false) }
    val currentDayPrayers = remember(minuteBucket, locRefreshTick) { PrayerEngine.day(ctx, 0) }
    val todayPrayers = remember(minuteBucket, dayOffset, locRefreshTick) {
        if (dayOffset == 0) currentDayPrayers else PrayerEngine.day(ctx, dayOffset)
    }
    val tomorrowPrayers = remember(minuteBucket, locRefreshTick) { PrayerEngine.day(ctx, 1) }

    val nextIndexToday = remember(currentDayPrayers, minuteBucket) {
        currentDayPrayers.indexOfFirst { it.second.time > nowMs }
    }
    val (nextDisplayName, nextTimeMs) = remember(nextIndexToday, currentDayPrayers, tomorrowPrayers, s) {
        if (nextIndexToday >= 0) {
            s.prayers[nextIndexToday] to currentDayPrayers[nextIndexToday].second.time
        } else {
            s.prayers[0] to tomorrowPrayers.first().second.time
        }
    }

    val remSec = ((nextTimeMs - nowMs).coerceAtLeast(0L) / 1000L)
    val countdownText = String.format(Locale.US, "%02d:%02d:%02d", remSec / 3600, (remSec % 3600) / 60, remSec % 60).toWesternDigits()

    val rows = remember(todayPrayers, nextIndexToday, dayOffset, modeTick, s) {
        val highlightIdx = if (nextIndexToday >= 0) nextIndexToday else 0
        todayPrayers.mapIndexed { idx, (keyAr, date) ->
            BrandPrayerRow(
                keyNameAr = keyAr,
                displayTitle = SakinaAdvancedPrefs.displayPrayerName(ctx, lang, idx, Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY),
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
    val datePillText = remember(viewedCal, s, lang) {
        val dayName = SimpleDateFormat("EEEE", lang.locale).format(viewedCal.time)
        val gregWords = SimpleDateFormat("d MMMM yyyy", lang.locale).format(viewedCal.time).toWesternDigits()
        val ic = IslamicCalendar().apply {
            timeInMillis = viewedCal.timeInMillis
            add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
        }
        val hMonth = s.hijriMonths.getOrElse(ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)) { "" }
        val h = "${ic.get(IslamicCalendar.DAY_OF_MONTH)} $hMonth ${ic.get(IslamicCalendar.YEAR)}".toWesternDigits()
        "$dayName، $h · $gregWords"
    }
    val localizedCity = remember(lang, locRefreshTick) { localizeCityName(Place.name(ctx), lang) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 46.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Exact Alarm 1-tap warning banner (only visible on Android 12+ when Exact Alarm permission is still OFF)
        val exactAllowed = remember(nowMs / 5_000L) {
            if (Build.VERSION.SDK_INT >= 31) ctx.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() ?: true
            else true
        }
        if (!exactAllowed && Build.VERSION.SDK_INT >= 31) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xEE7A1F1F))
                    .border(1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp))
                    .clickable {
                        try {
                            ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${ctx.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        } catch (_: Exception) {}
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sakinaTr(
                        lang,
                        "⚠ اضغط هنا لتفعيل «التنبيهات الدقيقة» حتى يؤذن التطبيق في وقته بالثانية",
                        "⚠ Tap here to enable Exact Alarms so Adhan rings on time",
                        "⚠ Touchez ici pour activer les alarmes exactes (Adhan à l'heure)",
                        "⚠ Ezanın vaktinde çalması için Tam Alarmları etkinleştirin",
                        "⚠ Ketuk untuk mengaktifkan Alarm Tepat agar Adzan tepat waktu",
                        "⚠ وقت پر اذان کے لیے درست الارم کی اجازت دینے کے لیے یہاں ٹیپ کریں"
                    ),
                    fontFamily = SakinaHeadingFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFFFFF4C2),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Top Header with larger icons (#19)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SakinaTitle3D(text = s.appName, fontSize = 24.sp, useAmiri = true)
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
                    Image(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(20.dp))
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
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xDE041118))
                    .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = localizedCity,
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
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

        // v29 #2: Streamlined Clock Cartouche (Next Prayer merged directly under countdown to reduce clutter)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val hmsFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
                    if (!analogPrimary) {
                        SakinaOrnamentCartouche(
                            modifier = Modifier.clickable { showCurrentTimeInCartouche = !showCurrentTimeInCartouche },
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (showCurrentTimeInCartouche) hmsFmt.format(Date(nowMs)).toWesternDigits() else countdownText,
                                    fontFamily = SakinaNumeralFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp,
                                    color = theme.accentBright,
                                    maxLines = 1,
                                    style = TextStyle(
                                        fontFeatureSettings = "tnum",
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    )
                                )
                                Text(
                                    text = if (showCurrentTimeInCartouche) sakinaTr(lang, "الوقت الحالي", "Current Time", "Heure actuelle", "Şu Anki Saat", "Waktu Saat Ini", "موجودہ وقت") else sakinaTr(lang, "المتبقي لصلاة $nextDisplayName", "Until $nextDisplayName", "Avant $nextDisplayName", "$nextDisplayName vaktine", "Menuju $nextDisplayName", "$nextDisplayName میں باقی"),
                                    fontFamily = SakinaBodyFont,
                                    fontSize = 9.5.sp,
                                    color = SakinaBrandColors.SoftSilver,
                                    maxLines = 1,
                                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                )
                            }
                        }
                    } else {
                        SakinaAnalogClockDial(
                            timeMs = nowMs,
                            size = 176.dp
                        )
                    }

                    // Left Symmetric 3D Gold Button (Point 15): Toggle Current Time <-> Remaining Countdown
                    SakinaClockActionButton(
                        isActive = showCurrentTimeInCartouche,
                        iconType = 1,
                        onClick = {
                            if (analogPrimary) analogPrimary = false
                            showCurrentTimeInCartouche = !showCurrentTimeInCartouche
                        },
                        modifier = Modifier
                            .align(AbsoluteAlignment.CenterLeft)
                            .padding(start = 6.dp)
                    )

                    // Right Symmetric 3D Gold Button (Point 14): Toggle Digital <-> Luxury Analog Clock
                    SakinaClockActionButton(
                        isActive = analogPrimary,
                        iconType = 0,
                        onClick = { analogPrimary = !analogPrimary },
                        modifier = Modifier
                            .align(AbsoluteAlignment.CenterRight)
                            .padding(end = 6.dp)
                    )
                }
            }
        }

        // v29 #1: Clear Weekly Day Preview Strip + Helper Hint for Tracker Circles (○ -> ✓)
        val activeDateKey = remember(dayOffset, minuteBucket) { SakinaAdvancedPrefs.dateKeyOffset(dayOffset) }
        val dayLoggedCount = remember(activeDateKey, trackerTick) { SakinaAdvancedPrefs.loggedCountForDay(ctx, activeDateKey) }
        val (mLogged, mTotal, mPerfect) = remember(trackerTick) { SakinaAdvancedPrefs.monthlyPrayerStats(ctx) }
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sakinaTr(
                        lang,
                        "اختر يوماً لمعاينة مواقيته · اضغط ○ لتسجيل صلاتك",
                        "Tap a day to preview · Tap ○ to log prayer",
                        "Choisir un jour · Toucher ○ pour cocher la prière",
                        "Gün seçin · Namazı işaretlemek için ○ dokunun",
                        "Pilih hari · Ketuk ○ untuk catat sholat",
                        "دن منتخب کریں · نماز ریکارڈ کے لیے ○ دبائیں"
                    ),
                    fontFamily = SakinaBodyFont,
                    fontSize = 9.5.sp,
                    color = SakinaBrandColors.SoftSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC0A3D30))
                        .border(0.8.dp, SakinaBrandColors.StableEmerald, RoundedCornerShape(8.dp))
                        .clickable { showMonthlyStats = true }
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = SakinaAdvancedPrefs.formatDigits(ctx, "✓ $dayLoggedCount/5"),
                        fontFamily = SakinaNumeralFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        color = SakinaBrandColors.StableEmerald
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val arWeekdays = listOf("", "أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
                (-1..4).forEach { dOff ->
                    val dCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dOff) }
                    val dow = dCal.get(Calendar.DAY_OF_WEEK)
                    val dayNameClean = when (lang) {
                        SakinaLang.AR -> arWeekdays.getOrElse(dow) { "" }
                        else -> SimpleDateFormat("EEE", lang.locale).format(dCal.time).replace(".", "").take(3)
                    }
                    val dayNumStr = SakinaAdvancedPrefs.formatDigits(ctx, "${dCal.get(Calendar.DAY_OF_MONTH)}")
                    val sel = dayOffset == dOff
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (sel) theme.accentGold else Color(0xBB041218))
                            .border(0.7.dp, if (sel) theme.accentBright else Color(0x44C5A059), RoundedCornerShape(9.dp))
                            .clickable { dayOffset = dOff }
                            .padding(vertical = 2.5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayNameClean,
                            fontFamily = SakinaBodyFont,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 9.sp,
                            color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory,
                            maxLines = 1,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                        Text(
                            text = dayNumStr,
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            color = if (sel) theme.bgDeep else theme.accentBright,
                            maxLines = 1,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                }
            }
        }

        if (showMonthlyStats) {
            AlertDialog(
                onDismissRequest = { showMonthlyStats = false },
                containerColor = theme.bgCard,
                title = {
                    Text(
                        sakinaTr(lang, "إحصائية الصلوات لهذا الشهر", "Monthly Prayer Progress", "Suivi mensuel des prières", "Aylık Namaz Takibi", "Progres Sholat Bulanan", "ماہانہ نماز کی کارکردگی"),
                        fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = theme.accentBright
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            SakinaAdvancedPrefs.formatDigits(ctx, sakinaTr(lang, "الصلوات المسجّلة هذا الشهر: $mLogged من $mTotal", "Logged this month: $mLogged / $mTotal", "Prières accomplies ce mois : $mLogged / $mTotal", "Bu ay kılınan: $mLogged / $mTotal", "Sholat bulan ini: $mLogged / $mTotal", "اس ماہ کی نمازیں: $mLogged / $mTotal")),
                            fontFamily = SakinaBodyFont, fontSize = 14.sp, color = SakinaBrandColors.LuminousIvory
                        )
                        Text(
                            SakinaAdvancedPrefs.formatDigits(ctx, sakinaTr(lang, "الأيام المكتملة (5/5 صلوات): $mPerfect يوم", "Complete 5/5 days: $mPerfect days", "Jours complets (5/5) : $mPerfect jours", "Tam gün (5/5): $mPerfect gün", "Hari lengkap (5/5): $mPerfect hari", "مکمل دن (5/5): $mPerfect دن")),
                            fontFamily = SakinaBodyFont, fontSize = 14.sp, color = SakinaBrandColors.StableEmerald
                        )
                        Text(
                            sakinaTr(lang, "اضغط على الدائرة بجانب أي صلاة في الشاشة الرئيسية لتسجيل أدائها يومياً.", "Tap the circle beside any prayer on the home screen to log it.", "Appuyez sur le cercle à côté de chaque prière pour la cocher.", "Ana ekranda her namazın yanındaki daireye dokunarak işaretleyin.", "Ketuk lingkaran di samping tiap sholat untuk mencatatnya.", "روزانہ ریکارڈ کے لیے ہر نماز کے ساتھ دائرے پر ٹیپ کریں۔"),
                            fontFamily = SakinaBodyFont, fontSize = 12.sp, color = SakinaBrandColors.SoftSilver
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMonthlyStats = false }) {
                        Text(sakinaTr(lang, "حسناً", "OK", "OK", "Tamam", "OK", "ٹھیک ہے"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, color = theme.accentBright)
                    }
                }
            )
        }

        // #9 + v28 #05: Every prayer row with inline 1-tap Prayer Tracker circle (✓ / ○) + alert mode toggle
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            rows.forEachIndexed { idx, prayer ->
                val cycleAction: () -> Unit = {
                    val nextMode = when (prayer.mode) { 0, 1 -> 2; 2 -> 3; else -> 0 }
                    Modes.set(ctx, prayer.keyNameAr, nextMode)
                    AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); modeTick++
                }
                val isLogged = remember(activeDateKey, idx, trackerTick) {
                    SakinaAdvancedPrefs.isPrayerLogged(ctx, activeDateKey, idx)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isLogged) SakinaBrandColors.StableEmerald else Color(0x99041218))
                            .border(1.3.dp, if (isLogged) SakinaBrandColors.StableEmerald else theme.accentGold, CircleShape)
                            .clickable {
                                SakinaAdvancedPrefs.togglePrayerLogged(ctx, activeDateKey, idx)
                                trackerTick++
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLogged) "✓" else "",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
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

        // v28 #11: Compact Imsak & Nawafil (Duha, Midnight, Tahajjud) Bar
        val showNawafil = remember(minuteBucket) { SakinaAdvancedPrefs.showNawafilCard(ctx) }
        val nawafil = remember(dayOffset, locRefreshTick, showNawafil) {
            if (showNawafil) SakinaAdvancedPrefs.computeNawafil(ctx, dayOffset) else null
        }
        if (nawafil != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC041218))
                    .border(0.8.dp, Color(0x55C5A059), RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    sakinaTr(lang, "الإمساك", "Imsak", "Imsak", "İmsak", "Imsak", "امساک") to SakinaAdvancedPrefs.formatPrayerTime(ctx, nawafil.imsak),
                    sakinaTr(lang, "الضحى", "Duha", "Doha", "Kuşluk", "Dhuha", "چاشت") to SakinaAdvancedPrefs.formatPrayerTime(ctx, nawafil.duha),
                    sakinaTr(lang, "منتصف الليل", "Midnight", "Minuit", "Gece Yarısı", "Tengah Malam", "نصف شب") to SakinaAdvancedPrefs.formatPrayerTime(ctx, nawafil.midnight),
                    sakinaTr(lang, "التهجد", "Tahajjud", "Tahajjud", "Teheccüd", "Tahajud", "تہجد") to SakinaAdvancedPrefs.formatPrayerTime(ctx, nawafil.lastThird)
                ).forEach { (nLbl, nTime) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = nLbl,
                            fontFamily = SakinaBodyFont,
                            fontSize = 9.5.sp,
                            color = SakinaBrandColors.SoftSilver,
                            maxLines = 1,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                        Text(
                            text = nTime,
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = theme.accentBright,
                            maxLines = 1,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
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
            .defaultMinSize(minHeight = 74.dp)
            .shadow(10.dp, shape, ambientColor = theme.accentGold, spotColor = theme.accentGold)
            .clip(shape)
            .border(1.5.dp, Sakina3DGoldBevelBrush, shape)
    ) {
        Image(
            painter = painterResource(id = prayer.bgRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.horizontalGradient(listOf(Color(0xDD071422), Color(0xA6071422), Color(0xDD071422))))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
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
                    modifier = Modifier.size(24.dp).clickable { onCycleMode() }
                )
                Spacer(modifier = Modifier.width(9.dp))
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = prayer.displayTitle,
                        fontFamily = SakinaEditorialFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        color = theme.accentBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = nextLabel,
                        fontFamily = SakinaBodyFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        lineHeight = 19.sp,
                        color = LocalSakinaTextColor.current,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 2.dp)
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
                SakinaStarPolygonBadge(iconRes = prayer.iconRes, size = 44.dp)
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
            .height(46.dp)
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
                modifier = Modifier.size(22.dp).clickable { onCycleMode() }
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
            SakinaStarPolygonBadge(iconRes = prayer.iconRes, size = 36.dp)
        }
    }
}
