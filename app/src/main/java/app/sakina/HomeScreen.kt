package app.sakina

import android.content.Context
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
                ModalDrawerSheet(drawerContainerColor = currentTheme.bgDeep, modifier = Modifier.width(296.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            SakinaTitle3D(text = s.appName, fontSize = 21.sp, modifier = Modifier.weight(1f))
                            SakinaBackIconButton(onClick = { scope.launch { drawerState.close() } })
                        }
                        Spacer(Modifier.height(2.dp))

                        SakinaDrawerPillItem(R.drawable.ic_duas, s.duasTitle) {
                            scope.launch { drawerState.close() }; openedFromDrawer = true; extraSection = ExtraSection.DUAS
                        }
                        SakinaDrawerPillItem(R.drawable.ic_ruqyah, s.ruqyahTitle) {
                            scope.launch { drawerState.close() }; openedFromDrawer = true; extraSection = ExtraSection.RUQYAH
                        }
                        SakinaDrawerPillItem(R.drawable.ic_alarm, s.alarmTitle) {
                            scope.launch { drawerState.close() }; openedFromDrawer = true; extraSection = ExtraSection.ALARM
                        }

                        // #10: Sunan & Rawatib specifying Qabliyyah (Before) & Ba'diyyah (After) for every prayer
                        SakinaDrawerPillItem(R.drawable.ic_sunan, s.sunanTitle) {
                            scope.launch { drawerState.close() }
                            hadithTitle = s.sunanTitle
                            hadithBody = if (isArOrUr) {
                                """
                                ❖ تفصيل النوافل القبلية والبعدية لكل صلاة ❖

                                1. صلاة الفجر:
                                • القبلية: ركعتان مؤكدتان قبل الفريضة (وهما أوكد السنن الرواتب؛ قال ﷺ: «ركعتا الفجر خير من الدنيا وما فيها» رواه مسلم 725).
                                • البعدية: لا نافلة بعدها حتى تشرق الشمس وترتفع.

                                2. صلاة الظهر:
                                • القبلية: 4 ركعات مؤكدة قبلها (بتسليمتين).
                                • البعدية: ركعتان مؤكدتان بعدها (ويُستحب زيادة ركعتين لتكون 4 قبلها و4 بعدها؛ لحديث: «من حافظ على أربع ركعات قبل الظهر وأربع بعدها حرّمه الله على النار» رواه أبو داود والترمذي 428).

                                3. صلاة العصر:
                                • القبلية: 4 ركعات مستحبة (غير راتبة مؤكدة) قبلها؛ لقوله ﷺ: «رحم الله امرأً صلى قبل العصر أربعاً» (رواه أبو داود 1271 والترمذي 430).
                                • البعدية: لا نافلة بعد صلاة العصر حتى تغرب الشمس.

                                4. صلاة المغرب:
                                • القبلية: ركعتان مستحبتان (غير مؤكدة)؛ لقوله ﷺ: «صلوا قبل صلاة المغرب» ثلاثاً (رواه البخاري 1183).
                                • البعدية: ركعتان مؤكدتان بعدها.

                                5. صلاة العشاء:
                                • القبلية: ركعتان مستحبتان (غير مؤكدة)؛ لحديث: «بين كل أذانين صلاة» (متفق عليه).
                                • البعدية: ركعتان مؤكدتان بعدها، ثم صلاة الوتر.

                                ❖ الحديث الجامع للرواتب المؤكدة (12 ركعة) ❖
                                عن أم حبيبة رضي الله عنها قالت: سمعت رسول الله ﷺ يقول: «ما من عبد مسلم يصلي لله كل يوم ثنتي عشرة ركعة تطوعاً غير الفريضة إلا بنى الله له بيتاً في الجنة» (رواه مسلم في صحيحه 728).
                                """.trimIndent().toWesternDigits()
                            } else {
                                """
                                ❖ Complete Qabliyyah (Before) & Ba'diyyah (After) Breakdown ❖

                                1. Fajr Prayer:
                                • Before (Qabliyyah): 2 confirmed (Mu'akkadah) rak'ahs — the most emphasized of all Sunnah prayers ('Two rak'ahs of Fajr are better than the world and all it contains' — Sahih Muslim 725).
                                • After (Ba'diyyah): None until after sunrise.

                                2. Dhuhr Prayer:
                                • Before (Qabliyyah): 4 confirmed rak'ahs (2+2).
                                • After (Ba'diyyah): 2 confirmed rak'ahs (and 4 after is recommended: 'Whoever maintains 4 before Dhuhr and 4 after, Allah forbids him to the Fire' — Tirmidhi 428).

                                3. Asr Prayer:
                                • Before (Qabliyyah): 4 recommended (Ghayr Mu'akkadah) rak'ahs ('May Allah have mercy on a person who prays 4 rak'ahs before Asr' — Abu Dawud 1271).
                                • After (Ba'diyyah): None until after sunset.

                                4. Maghrib Prayer:
                                • Before (Qabliyyah): 2 recommended rak'ahs ('Pray before Maghrib' — Sahih al-Bukhari 1183).
                                • After (Ba'diyyah): 2 confirmed rak'ahs.

                                5. Isha Prayer:
                                • Before (Qabliyyah): 2 recommended rak'ahs ('Between every two calls is a prayer' — Bukhari & Muslim).
                                • After (Ba'diyyah): 2 confirmed rak'ahs, followed by Witr.

                                ❖ Hadith of the 12 Confirmed Rawatib ❖
                                Umm Habibah narrated that the Messenger of Allah ﷺ said: 'Whoever prays twelve voluntary rak'ahs every day, Allah will build a house for him in Paradise.' (Sahih Muslim 728).
                                """.trimIndent().toWesternDigits()
                            }
                        }

                        // #11: Duha Prayer detailing recommended & prophetic Rak'ah counts
                        SakinaDrawerPillItem(R.drawable.ic_dhuhr, s.duhaTitle) {
                            scope.launch { drawerState.close() }
                            val events = PrayerEngine.events(ctx, 0)
                            val hm = SimpleDateFormat("HH:mm", Locale.US)
                            val duhaStart = hm.format(Date(events.first { it.first == 1 }.second.time + 15 * 60_000L))
                            val duhaEnd = hm.format(Date(events.first { it.first == 2 }.second.time - 10 * 60_000L))
                            hadithTitle = "${s.duhaTitle} ($duhaStart – $duhaEnd)"
                            hadithBody = if (isArOrUr) {
                                """
                                ❖ وقت صلاة الضحى اليوم: من $duhaStart إلى $duhaEnd ❖
                                (وأفضل وقتها حين يشتد الضحى؛ لقوله ﷺ: «صلاة الأوابين حين ترمض الفصال» رواه مسلم 748).

                                ❖ عدد ركعات صلاة الضحى المستحبة والمأثورة عن النبي ﷺ ❖

                                1. أقلها (ركعتان):
                                عن أبي ذر رضي الله عنه عن النبي ﷺ أنه قال: «يصبح على كل سلامى من أحدكم صدقة... ويجزئ من ذلك ركعتان يركعهما من الضحى» (رواه مسلم 720)، وأوصى بها النبي ﷺ أبا هريرة وأبا الدرداء رضي الله عنهما.

                                2. أوسطها والمستحب المواظبة عليه (4 ركعات فصاعداً):
                                • عن عائشة رضي الله عنها قالت: «كان رسول الله ﷺ يصلي الضحى أربعاً، ويزيد ما شاء الله» (رواه مسلم 719).
                                • وعن نعيم بن همار رضي الله عنه أن رسول الله ﷺ قال عن الله عز وجل: «ابن آدم، اركع لي من أول النهار أربع ركعات أكفك آخره» (رواه الترمذي 475 وأبو داود 1289 وصححه الألباني).

                                3. أفضلها وأكثر ما ثبت من فعله ﷺ (8 ركعات):
                                عن أم هانئ بنت أبي طالب رضي الله عنها: «أن النبي ﷺ دخل بيتها يوم فتح مكة فاغتسل وصلى ثماني ركعات سبحة الضحى، يسلم من كل ركعتين» (متفق عليه: البخاري 1176، ومسلم 336).
                                • وقال جمع من المحققين: لا حدّ لأكثرها لمن شاء الزيادة مثنى مثنى.
                                """.trimIndent().toWesternDigits()
                            } else {
                                """
                                ❖ Today's Duha Window: $duhaStart – $duhaEnd ❖
                                (Best performed mid-morning when the sun grows warm — Sahih Muslim 748).

                                ❖ Authentic Rak'ah Counts of Duha from the Prophet ﷺ ❖

                                1. Minimum (2 Rak'ahs):
                                Abu Dharr narrated that the Prophet ﷺ said: 'Charity is due upon every joint of a person every morning... and two rak'ahs of Duha suffice for that.' (Sahih Muslim 720).

                                2. Recommended Regular Count (4 Rak'ahs or more):
                                • Aishah narrated: 'The Messenger of Allah ﷺ used to pray four rak'ahs of Duha, and would add whatever Allah willed.' (Sahih Muslim 719).
                                • Hadith Qudsi: 'O son of Adam, bow for Me four rak'ahs at the start of the day and I will suffice you for the rest of it.' (Tirmidhi 475, Abu Dawud 1289).

                                3. Most Complete Prophetic Practice (8 Rak'ahs, two by two):
                                Umm Hani' narrated that on the Day of the Conquest of Makkah, the Prophet ﷺ prayed eight rak'ahs of Duha, offering Salam after every two rak'ahs. (Sahih al-Bukhari 1176 & Sahih Muslim 336).
                                """.trimIndent().toWesternDigits()
                            }
                        }

                        // #14: Qiyam al-Layl detailing recommended & prophetic Rak'ah counts
                        SakinaDrawerPillItem(R.drawable.ic_isha, s.qiyamTitle) {
                            scope.launch { drawerState.close() }
                            hadithTitle = s.qiyamTitle
                            hadithBody = if (isArOrUr) {
                                """
                                ❖ عدد ركعات قيام الليل المستحبة والمأثورة عن النبي ﷺ ❖

                                1. هدي النبي ﷺ الغالب والمختار (11 ركعة أو 13 ركعة):
                                • عن عائشة رضي الله عنها قالت: «ما كان رسول الله ﷺ يزيد في رمضان ولا في غيره على إحدى عشرة ركعة: يصلي أربعاً فلا تسأل عن حسنهن وطولهن، ثم يصلي أربعاً فلا تسأل عن حسنهن وطولهن، ثم يصلي ثلاثاً» (متفق عليه: البخاري 1147 ومسلم 736).
                                • وثبت عنها وعن ابن عباس رضي الله عنهم في الصحيحين: «كانت صلاة النبي ﷺ من الليل ثلاث عشرة ركعة» (البخاري 1138 ومسلم 737) بإضافة ركعتين خفيفتين في افتتاح القيام.

                                2. صفة صلاة الليل وعدد ركعات الوتر:
                                • قال رسول الله ﷺ: «صلاة الليل مثنى مثنى، فإذا خشي أحدكم الصبح صلى ركعة واحدة توتر له ما قد صلى» (متفق عليه: البخاري 990 ومسلم 749).
                                • ويجوز الوتر بركعة واحدة، أو بثلاث ركعات، أو بخمس، أو بسبع، أو بتسع؛ لقوله ﷺ: «الوتر حق على كل مسلم، فمن أحب أن يوتر بخمس فليفعل، ومن أحب أن يوتر بثلاث فليفعل، ومن أحب أن يوتر بواحدة فليفعل» (رواه أبو داود 1422 والنسائي).

                                3. فضل الثلث الأخير من الليل:
                                عن أبي هريرة رضي الله عنه أن رسول الله ﷺ قال: «ينزل ربنا تبارك وتعالى كل ليلة إلى السماء الدنيا حين يبقى ثلث الليل الآخر يقول: من يدعوني فأستجيب له، من يسألني فأعطيه، من يستغفرني فأغفر له» (متفق عليه: البخاري 1145 ومسلم 758).
                                """.trimIndent().toWesternDigits()
                            } else {
                                """
                                ❖ Authentic Rak'ah Counts of Qiyam al-Layl from the Prophet ﷺ ❖

                                1. The Prophet's ﷺ Regular Practice (11 or 13 Rak'ahs):
                                • Aishah narrated: 'The Messenger of Allah ﷺ never exceeded eleven rak'ahs in Ramadan or outside it: he would pray four—do not ask about their beauty and length—then pray four—do not ask about their beauty and length—then pray three.' (Sahih al-Bukhari 1147 & Sahih Muslim 736).
                                • Ibn Abbas and Aishah also reported 13 rak'ahs when including the 2 light opening rak'ahs (Sahih al-Bukhari 1138 & Sahih Muslim 737).

                                2. Method of Night Prayer & Witr (Two by Two + Odd Witr):
                                • The Prophet ﷺ said: 'The night prayer is two by two (Mathna Mathna), and if one of you fears the break of dawn, let him pray a single rak'ah to make what he has prayed Witr (odd-numbered).' (Sahih al-Bukhari 990 & Sahih Muslim 749).
                                • Witr may be prayed as 1, 3, 5, 7, or 9 rak'ahs (Abu Dawud 1422).

                                3. Virtue of the Last Third of the Night:
                                'Our Lord descends every night to the lowest heaven in the last third of the night, saying: Who calls upon Me that I may answer him? Who asks of Me that I may give him? Who seeks My forgiveness that I may forgive him?' (Sahih al-Bukhari 1145 & Sahih Muslim 758).
                                """.trimIndent().toWesternDigits()
                            }
                        }

                        // #15: Separate Voluntary Fasting Tab (تبويب صيام التطوع)
                        SakinaDrawerPillItem(R.drawable.ic_calendar, s.fastingTitle) {
                            scope.launch { drawerState.close() }
                            hadithTitle = s.fastingTitle
                            hadithBody = if (isArOrUr) {
                                """
                                ❖ ما سُنّ وما يُستحب صيامه من الأيام عن النبي ﷺ ❖

                                1. صيام ستة أيام من شوال:
                                قال رسول الله ﷺ: «من صام رمضان ثم أتبعه ستاً من شوال كان كصيام الدهر» (رواه مسلم 1164).

                                2. صيام يوم عرفة (9 ذو الحجة لغير الحاج):
                                قال ﷺ: «صيام يوم عرفة أحتسب على الله أن يكفر السنة التي قبله والسنة التي بعده» (رواه مسلم 1162).

                                3. صيام يوم عاشوراء (10 محرم) مع يوم تاسوعاء (9 محرم):
                                قال ﷺ عن صيام يوم عاشوراء: «أحتسب على الله أن يكفر السنة التي قبله» (رواه مسلم 1162)، وقال: «لئن بقيت إلى قابل لأصومن التاسع» (رواه مسلم 1134).

                                4. صيام الأيام البيض من كل شهر هجري (13 و 14 و 15):
                                قال أبو هريرة رضي الله عنه: «أوصاني خليلي ﷺ بثلاث: صوم ثلاثة أيام من كل شهر...» (متفق عليه: البخاري 1981 ومسلم 721)، وقال ﷺ لأبي ذر: «إذا صمت من الشهر ثلاثاً فصم ثلاث عشرة وأربع عشرة وخمس عشرة» (رواه الترمذي 761).

                                5. صيام يومي الإثنين والخميس من كل أسبوع:
                                سُئل ﷺ عن صوم يوم الإثنين فقال: «ذاك يوم وُلدت فيه، ويوم بُعثت أو أُنزل عليّ فيه» (رواه مسلم 1162)، وقال: «تُعرض الأعمال يوم الإثنين والخميس، فأحب أن يُعرض عملي وأنا صائم» (رواه الترمذي 747).

                                6. الإكثار من الصيام في شهر الله المحرم وشهر شعبان والتسع الأوائل من ذي الحجة:
                                • قال ﷺ: «أفضل الصيام بعد رمضان شهر الله المحرم» (رواه مسلم 1163).
                                • وقالت عائشة رضي الله عنها: «ما رأيت رسول الله ﷺ أكثر صياماً منه في شعبان» (متفق عليه: البخاري 1969 ومسلم 1156).

                                7. أفضل الصيام لمن أطاقه (صيام داود عليه السلام):
                                قال ﷺ: «أحب الصيام إلى الله صيام داود؛ كان يصوم يوماً ويفطر يوماً» (متفق عليه: البخاري 1131 ومسلم 1159).
                                """.trimIndent().toWesternDigits()
                            } else {
                                """
                                ❖ Prophetic & Recommended Voluntary Fasts from the Sunnah ❖

                                1. Six Days of Shawwal:
                                'Whoever fasts Ramadan and follows it with six days of Shawwal, it is as if he fasted for a lifetime.' (Sahih Muslim 1164).

                                2. Day of Arafah (9th Dhu al-Hijjah for non-pilgrims):
                                'Fasting the Day of Arafah expiates the sins of the preceding year and the coming year.' (Sahih Muslim 1162).

                                3. Day of Ashura (10th Muharram) with Tasu'a (9th Muharram):
                                'Fasting the Day of Ashura expiates the sins of the preceding year.' (Sahih Muslim 1162 & 1134).

                                4. The Three White Days (13th, 14th, and 15th of every Hijri month):
                                Recommended by the Prophet ﷺ to Abu Hurayrah and Abu Dharr as equal to fasting the whole month (Sahih al-Bukhari 1981 & Tirmidhi 761).

                                5. Mondays and Thursdays Every Week:
                                'Deeds are presented on Mondays and Thursdays, and I love for my deeds to be presented while I am fasting.' (Sahih Muslim 1162 & Tirmidhi 747).

                                6. Fasting in Muharram, Sha'ban, and the First Nine Days of Dhu al-Hijjah:
                                'The best fasting after Ramadan is the month of Allah, Muharram.' (Sahih Muslim 1163), and the Prophet ﷺ fasted abundantly in Sha'ban (Bukhari 1969).

                                7. The Fast of Prophet Dawud (Alternate Days):
                                'The most beloved fasting to Allah is the fasting of Dawud: he used to fast one day and break his fast one day.' (Bukhari 1131 & Muslim 1159).
                                """.trimIndent().toWesternDigits()
                            }
                        }

                        // #15 & #16: Separate Islamic Occasions Tab (تبويب المناسبات الدينية)
                        SakinaDrawerPillItem(R.drawable.ic_star_ornament, s.occasionsTitle) {
                            scope.launch { drawerState.close() }
                            hadithTitle = s.occasionsTitle
                            hadithBody = buildReligiousOccasionsReport(Prefs.hijriOffset(ctx), isArOrUr)
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
                        // Symmetrical 34.dp Top-Right Menu Button with larger 19.dp icon (#19)
                        Box(
                            modifier = Modifier
                                .align(AbsoluteAlignment.TopRight)
                                .statusBarsPadding()
                                .padding(end = 15.dp, top = 14.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(currentTheme.bgDeep.copy(alpha = 0.88f))
                                .border(1.dp, Sakina3DGoldBevelBrush, CircleShape)
                                .clickable { scope.launch { drawerState.open() } },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(painter = painterResource(id = R.drawable.ic_menu), contentDescription = "Menu", modifier = Modifier.size(19.dp))
                        }

                        // Symmetrical 34.dp Top-Left Language Switcher (#10, #19)
                        Box(
                            modifier = Modifier
                                .align(AbsoluteAlignment.TopLeft)
                                .statusBarsPadding()
                                .padding(start = 15.dp, top = 14.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(currentTheme.bgDeep.copy(alpha = 0.88f))
                                .border(1.dp, Sakina3DGoldBevelBrush, CircleShape)
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
                                alpha = 0.35f,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = currentLang.code.uppercase(),
                                fontFamily = SakinaNumeralFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
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
