package app.sakina

import android.content.Context
import android.icu.util.IslamicCalendar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

private val CAL_HIJRI_MONTHS = listOf(
    "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
    "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
)

private val FULL_WEEKDAYS_AR = listOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

private data class VerifiedHadith(
    val occasionTag: String,
    val matn: String,
    val isnadAndGrade: String
)

private val DAILY_VERIFIED_HADITHS = listOf(
    VerifiedHadith(
        "فضل الصلوات الخمس",
        "قال رسول الله ﷺ: «أرأيتم لو أن نهراً بباب أحدكم يغتسل منه كل يوم خمس مرات، هل يبقى من درنه شيء؟» قالوا: لا يبقى من درنه شيء، قال: «فذلك مثل الصلوات الخمس، يمحو الله بهن الخطايا».",
        "الراوي: أبو هريرة رضي الله عنه | المصدر: متفق عليه (صحيح البخاري 528، وصحيح مسلم 667) | الدرجة: صحيح قطعي الصحة"
    ),
    VerifiedHadith(
        "أحب الأعمال إلى الله",
        "سُئل النبي ﷺ: أي العمل أحب إلى الله؟ قال: «الصلاة على وقتها»، قال: ثم أي؟ قال: «ثم بر الوالدين»، قال: ثم أي؟ قال: «الجهاد في سبيل الله».",
        "الراوي: عبد الله بن مسعود رضي الله عنه | المصدر: متفق عليه (صحيح البخاري 527، وصحيح مسلم 85) | الدرجة: صحيح"
    ),
    VerifiedHadith(
        "فضل البردين (الفجر والعصر)",
        "قال رسول الله ﷺ: «من صلى البردين دخل الجنة».",
        "الراوي: أبو موسى الأشعري رضي الله عنه | المصدر: متفق عليه (صحيح البخاري 574، وصحيح مسلم 635) | الدرجة: صحيح"
    ),
    VerifiedHadith(
        "فضل صوم الإثنين والخميس",
        "سُئل رسول الله ﷺ عن صوم يوم الإثنين فقال: «ذاك يوم وُلدت فيه، ويوم بُعثت أو أُنزل عليّ فيه».",
        "الراوي: أبو قتادة الأنصاري رضي الله عنه | المصدر: صحيح مسلم (1162) | الدرجة: صحيح"
    ),
    VerifiedHadith(
        "وصية النبي ﷺ الثلاثية",
        "قال أبو هريرة رضي الله عنه: «أوصاني خليلي ﷺ بثلاث لا أدعهن حتى أموت: صوم ثلاثة أيام من كل شهر، وصلاة الضحى، ونوم على وتر».",
        "الراوي: أبو هريرة رضي الله عنه | المصدر: متفق عليه (صحيح البخاري 1178، وصحيح مسلم 721) | الدرجة: صحيح"
    ),
    VerifiedHadith(
        "فضل يوم الجمعة وساعة الإجابة",
        "قال رسول الله ﷺ: «خير يوم طلعت عليه الشمس يوم الجمعة؛ فيه خُلق آدم، وفيه أُدخل الجنة، وفيه أُخرج منها».",
        "الراوي: أبو هريرة رضي الله عنه | المصدر: صحيح مسلم (854) | الدرجة: صحيح"
    ),
    VerifiedHadith(
        "فضل الذكر والقرآن",
        "قال رسول الله ﷺ: «كلمتان خفيفتان على اللسان، ثقيلتان في الميزان، حبيبتان إلى الرحمن: سبحان الله وبحمده، سبحان الله العظيم».",
        "الراوي: أبو هريرة رضي الله عنه | المصدر: متفق عليه (صحيح البخاري 6406، وصحيح مسلم 2694) | الدرجة: صحيح"
    )
)

private fun selectHadithForDate(cal: Calendar, hijri: IslamicCalendar): VerifiedHadith {
    val hDay = hijri.get(IslamicCalendar.DAY_OF_MONTH)
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    return when {
        hDay in 13..15 -> DAILY_VERIFIED_HADITHS[4] // الأيام البيض
        dow == Calendar.FRIDAY -> DAILY_VERIFIED_HADITHS[5]
        dow == Calendar.MONDAY || dow == Calendar.THURSDAY -> DAILY_VERIFIED_HADITHS[3]
        else -> DAILY_VERIFIED_HADITHS[(cal.get(Calendar.DAY_OF_YEAR)) % DAILY_VERIFIED_HADITHS.size]
    }
}

@Composable
fun SakinaCalendarScreen(ctx: Context, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    var monthOffset by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    val baseCal = remember(monthOffset) {
        Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset); set(Calendar.DAY_OF_MONTH, 1) }
    }
    val daysInMonth = baseCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (baseCal.get(Calendar.DAY_OF_WEEK) - Calendar.SATURDAY + 7) % 7
    val clampedSelected = selectedDay.coerceIn(1, daysInMonth)

    val selectedCal = remember(monthOffset, clampedSelected) {
        (baseCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, clampedSelected) }
    }
    val hijriCal = remember(selectedCal) {
        IslamicCalendar().apply {
            timeInMillis = selectedCal.timeInMillis
            add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
        }
    }
    val gregTitle = SimpleDateFormat("MMMM yyyy", Locale("ar")).format(baseCal.time).toWesternDigits()
    val hijriSubtitle = "${hijriCal.get(IslamicCalendar.DAY_OF_MONTH)} ${CAL_HIJRI_MONTHS[hijriCal.get(IslamicCalendar.MONTH).coerceIn(0, 11)]} ${hijriCal.get(IslamicCalendar.YEAR)} هـ".toWesternDigits()

    val dayOffsetFromToday = ((selectedCal.timeInMillis - Calendar.getInstance().timeInMillis) / 86_400_000L).toInt()
    val dayPrayers = remember(dayOffsetFromToday) { PrayerEngine.day(ctx, dayOffsetFromToday) }
    val hm = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val dailyHadith = remember(selectedCal, hijriCal) { selectHadithForDate(selectedCal, hijriCal) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 5.dp)) {
            Text(s.calendarTitle, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = theme.accentBright)
        }

        // 1. Compact Monthly Grid
        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp, contentPadding = PaddingValues(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(22.dp).clickable { monthOffset-- })
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(gregTitle, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
                    Text(hijriSubtitle, fontFamily = SakinaBodyFont, fontSize = 10.5.sp, color = SakinaBrandColors.LuminousIvory)
                }
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(22.dp).clickable { monthOffset++ })
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                FULL_WEEKDAYS_AR.forEach { d ->
                    Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = theme.accentBright, maxLines = 1, softWrap = false)
                }
            }
            Spacer(Modifier.height(2.dp))
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                    for (c in 0 until 7) {
                        val idx = r * 7 + c
                        val dayNum = idx - firstDayOfWeek + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp)
                                .clip(CircleShape)
                                .then(if (dayNum == clampedSelected) Modifier.background(Color(0xFF0F3D3E)).border(1.dp, Sakina3DGoldBevelBrush, CircleShape) else Modifier)
                                .clickable(enabled = dayNum in 1..daysInMonth) { selectedDay = dayNum },
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNum in 1..daysInMonth) {
                                Text("$dayNum".toWesternDigits(), fontFamily = SakinaNumeralFont, fontWeight = if (dayNum == clampedSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp, color = if (dayNum == clampedSelected) theme.accentBright else SakinaBrandColors.LuminousIvory)
                            }
                        }
                    }
                }
            }
        }

        // 2. Compact Prayer Times Card ("توقيت صلوات هذا اليوم")
        SakinaParchmentPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)) {
            Text(s.dayPrayersCardTitle, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F3D3E), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(2.dp))
            Text(
                text = dayPrayers.joinToString(" · ") { "${it.first} ${hm.format(it.second)}" }.toWesternDigits(),
                fontFamily = SakinaBodyFont,
                fontSize = 10.sp,
                color = SakinaInkDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. Verified Hadith of the Day Card with Isnad & Grade (Dynamic font size)
        val matnFontSize = if (dailyHadith.matn.length > 150) 11.sp else 12.sp
        SakinaParchmentPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)) {
            Text(
                text = "❖ ${s.dailyHadithTitle} (${dailyHadith.occasionTag}) ❖",
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = Color(0xFF0F3D3E),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dailyHadith.matn.toWesternDigits(),
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = matnFontSize,
                lineHeight = 18.sp,
                color = SakinaInkDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dailyHadith.isnadAndGrade.toWesternDigits(),
                fontFamily = SakinaBodyFont,
                fontSize = 9.5.sp,
                lineHeight = 13.5.sp,
                color = Color(0xFF6B5224),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
