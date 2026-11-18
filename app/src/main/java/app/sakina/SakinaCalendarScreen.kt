package app.sakina

import android.content.Context
import android.icu.util.IslamicCalendar
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

private data class VerifiedHadith(
    val occasionAr: String,
    val occasionEn: String,
    val matnAr: String,
    val matnEn: String,
    val isnadAr: String,
    val isnadEn: String
)

private val DAILY_VERIFIED_HADITHS = listOf(
    VerifiedHadith(
        "فضل الصلوات الخمس",
        "Virtue of the Five Daily Prayers",
        "قال رسول الله ﷺ: «أرأيتم لو أن نهراً بباب أحدكم يغتسل منه كل يوم خمس مرات، هل يبقى من درنه شيء؟» قالوا: لا يبقى من درنه شيء، قال: «فذلك مثل الصلوات الخمس، يمحو الله بهن الخطايا».",
        "The Prophet ﷺ said: 'If there were a river at the door of one of you in which he washed five times daily, would any dirt remain on him?' They said: 'None.' He said: 'That is the likeness of the five prayers by which Allah erases sins.'",
        "الراوي: أبو هريرة رضي الله عنه | المصدر: متفق عليه (البخاري 528، مسلم 667) | الدرجة: صحيح",
        "Narrated by Abu Hurayrah | Sahih al-Bukhari (528) & Sahih Muslim (667) | Grade: Authentic (Sahih)"
    ),
    VerifiedHadith(
        "أحب الأعمال إلى الله",
        "Deeds Most Beloved to Allah",
        "سُئل النبي ﷺ: أي العمل أحب إلى الله؟ قال: «الصلاة على وقتها»، قال: ثم أي؟ قال: «ثم بر الوالدين»، قال: ثم أي؟ قال: «الجهاد في سبيل الله».",
        "The Prophet ﷺ was asked: 'Which deed is most beloved to Allah?' He said: 'Prayer offered on time, then kindness to parents, then striving in the cause of Allah.'",
        "الراوي: عبد الله بن مسعود رضي الله عنه | المصدر: متفق عليه (البخاري 527، مسلم 85) | الدرجة: صحيح",
        "Narrated by Abdullah ibn Mas'ud | Sahih al-Bukhari (527) & Sahih Muslim (85) | Grade: Authentic (Sahih)"
    ),
    VerifiedHadith(
        "فضل البردين (الفجر والعصر)",
        "Virtue of Fajr and Asr",
        "قال رسول الله ﷺ: «من صلى البردين دخل الجنة».",
        "The Messenger of Allah ﷺ said: 'Whoever prays the two cool prayers (Fajr and Asr) will enter Paradise.'",
        "الراوي: أبو موسى الأشعري رضي الله عنه | المصدر: متفق عليه (البخاري 574، مسلم 635) | الدرجة: صحيح",
        "Narrated by Abu Musa al-Ash'ari | Sahih al-Bukhari (574) & Sahih Muslim (635) | Grade: Authentic (Sahih)"
    ),
    VerifiedHadith(
        "فضل صوم الإثنين والخميس",
        "Fasting Mondays and Thursdays",
        "سُئل رسول الله ﷺ عن صوم يوم الإثنين فقال: «ذاك يوم وُلدت فيه، ويوم بُعثت أو أُنزل عليّ فيه».",
        "The Messenger of Allah ﷺ was asked about fasting on Mondays, and he said: 'That is the day on which I was born, and the day on which I received revelation.'",
        "الراوي: أبو قتادة الأنصاري رضي الله عنه | المصدر: صحيح مسلم (1162) | الدرجة: صحيح",
        "Narrated by Abu Qatadah al-Ansari | Sahih Muslim (1162) | Grade: Authentic (Sahih)"
    ),
    VerifiedHadith(
        "وصية النبي ﷺ الثلاثية",
        "Three Steadfast Sunnahs",
        "قال أبو هريرة رضي الله عنه: «أوصاني خليلي ﷺ بثلاث لا أدعهن حتى أموت: صوم ثلاثة أيام من كل شهر، وصلاة الضحى، ونوم على وتر».",
        "Abu Hurayrah said: 'My beloved friend ﷺ advised me to do three things which I will never abandon: fasting three days each month, praying Duha, and praying Witr before sleeping.'",
        "الراوي: أبو هريرة رضي الله عنه | المصدر: متفق عليه (البخاري 1178، مسلم 721) | الدرجة: صحيح",
        "Narrated by Abu Hurayrah | Sahih al-Bukhari (1178) & Sahih Muslim (721) | Grade: Authentic (Sahih)"
    ),
    VerifiedHadith(
        "فضل يوم الجمعة وساعة الإجابة",
        "Virtue of Friday (Jumu'ah)",
        "قال رسول الله ﷺ: «خير يوم طلعت عليه الشمس يوم الجمعة؛ فيه خُلق آدم، وفيه أُدخل الجنة، وفيه أُخرج منها».",
        "The Messenger of Allah ﷺ said: 'The best day on which the sun rises is Friday; on it Adam was created, on it he entered Paradise, and on it he left it.'",
        "الراوي: أبو هريرة رضي الله عنه | المصدر: صحيح مسلم (854) | الدرجة: صحيح",
        "Narrated by Abu Hurayrah | Sahih Muslim (854) | Grade: Authentic (Sahih)"
    ),
    VerifiedHadith(
        "فضل الذكر والتسبيح",
        "Virtue of Dhikr and Tasbih",
        "قال رسول الله ﷺ: «كلمتان خفيفتان على اللسان، ثقيلتان في الميزان، حبيبتان إلى الرحمن: سبحان الله وبحمده، سبحان الله العظيم».",
        "The Prophet ﷺ said: 'Two expressions are light on the tongue, heavy on the Scale, and beloved to the Most Merciful: SubhanAllahi wa bihamdihi, SubhanAllahil-Adheem.'",
        "الراوي: أبو هريرة رضي الله عنه | المصدر: متفق عليه (البخاري 6406، مسلم 2694) | الدرجة: صحيح",
        "Narrated by Abu Hurayrah | Sahih al-Bukhari (6406) & Sahih Muslim (2694) | Grade: Authentic (Sahih)"
    )
)

private fun selectHadithForDate(cal: Calendar, hijri: IslamicCalendar): VerifiedHadith {
    val hDay = hijri.get(IslamicCalendar.DAY_OF_MONTH)
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    return when {
        hDay in 13..15 -> DAILY_VERIFIED_HADITHS[4]
        dow == Calendar.FRIDAY -> DAILY_VERIFIED_HADITHS[5]
        dow == Calendar.MONDAY || dow == Calendar.THURSDAY -> DAILY_VERIFIED_HADITHS[3]
        else -> DAILY_VERIFIED_HADITHS[(cal.get(Calendar.DAY_OF_YEAR)) % DAILY_VERIFIED_HADITHS.size]
    }
}

@Composable
fun SakinaCalendarScreen(ctx: Context, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    var monthOffset by remember { mutableIntStateOf(0) }
    val todayDayNum = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }
    var selectedDay by remember { mutableIntStateOf(todayDayNum) }

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
    val gregTitle = remember(baseCal, lang) {
        SimpleDateFormat("MMMM yyyy", lang.locale).format(baseCal.time).toWesternDigits()
    }
    val hijriMonthName = s.hijriMonths.getOrElse(hijriCal.get(IslamicCalendar.MONTH).coerceIn(0, 11)) { "" }
    val hijriSuffix = if (lang == SakinaLang.AR || lang == SakinaLang.UR) "هـ" else "AH"
    val hijriSubtitle = "${hijriCal.get(IslamicCalendar.DAY_OF_MONTH)} $hijriMonthName ${hijriCal.get(IslamicCalendar.YEAR)} $hijriSuffix".toWesternDigits()

    val dayOffsetFromToday = ((selectedCal.timeInMillis - Calendar.getInstance().timeInMillis) / 86_400_000L).toInt()
    val dayPrayers = remember(dayOffsetFromToday) { PrayerEngine.day(ctx, dayOffsetFromToday) }
    val hm = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val dailyHadith = remember(selectedCal, hijriCal) { selectHadithForDate(selectedCal, hijriCal) }
    val isArabicOrUrdu = lang == SakinaLang.AR || lang == SakinaLang.UR

    // #5: Shifted down (top = 56.dp) so the top cartouche sits well below the Mihrab arch,
    // and added breathing room between the cartouche and the Calendar card while keeping the bottom anchored cleanly above the nav bar!
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 56.dp, bottom = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 36.dp, vertical = 6.dp)) {
            Text(
                text = s.calendarTitle,
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = theme.accentBright,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        // #5: Space moved to the top between the Hijri Calendar cartouche and the Calendar grid!
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Luxury Ornate Monthly Calendar Card (#7)
        val calShape = RoundedCornerShape(18.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, calShape)
                .clip(calShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            theme.bgCard.copy(alpha = 0.96f),
                            Color(0xFF071F24),
                            theme.bgDeep.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(1.4.dp, Sakina3DGoldBevelBrush, calShape)
        ) {
            // 4-Corner Gold Arabesque Filigree Ornaments inside the Calendar (#7)
            val cSize = 18.dp
            Image(
                painter = painterResource(id = R.drawable.ic_corner_arabesque),
                contentDescription = null,
                alpha = 0.72f,
                modifier = Modifier.size(cSize).align(AbsoluteAlignment.TopLeft).padding(2.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_corner_arabesque),
                contentDescription = null,
                alpha = 0.72f,
                modifier = Modifier.size(cSize).align(AbsoluteAlignment.TopRight).padding(2.dp).graphicsLayer { rotationZ = 90f }
            )
            Image(
                painter = painterResource(id = R.drawable.ic_corner_arabesque),
                contentDescription = null,
                alpha = 0.72f,
                modifier = Modifier.size(cSize).align(AbsoluteAlignment.BottomRight).padding(2.dp).graphicsLayer { rotationZ = 180f }
            )
            Image(
                painter = painterResource(id = R.drawable.ic_corner_arabesque),
                contentDescription = null,
                alpha = 0.72f,
                modifier = Modifier.size(cSize).align(AbsoluteAlignment.BottomLeft).padding(2.dp).graphicsLayer { rotationZ = 270f }
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // Luxury Month Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33D4AF37))
                        .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(theme.bgDeep)
                            .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                            .clickable { monthOffset-- },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = hijriSubtitle,
                            fontFamily = SakinaEditorialFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = theme.accentBright,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                        Text(
                            text = gregTitle,
                            fontFamily = SakinaBodyFont,
                            fontSize = 10.sp,
                            color = SakinaBrandColors.LuminousIvory,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(theme.bgDeep)
                            .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                            .clickable { monthOffset++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Gold-Tinted Weekdays Ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x1AD4AF37))
                        .padding(vertical = 2.5.dp)
                ) {
                    s.weekdays.forEach { d ->
                        Text(
                            text = d,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontFamily = SakinaBodyFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.8.sp,
                            color = theme.accentBright,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                }

                Spacer(Modifier.height(3.dp))

                val totalCells = firstDayOfWeek + daysInMonth
                val rows = (totalCells + 6) / 7
                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                        for (c in 0 until 7) {
                            val idx = r * 7 + c
                            val dayNum = idx - firstDayOfWeek + 1
                            val isSelected = dayNum == clampedSelected
                            val isToday = monthOffset == 0 && dayNum == todayDayNum
                            val isFriday = c == 6
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(21.dp)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .then(
                                        when {
                                            isSelected -> Modifier
                                                .background(Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold)))
                                                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(7.dp))
                                            isToday -> Modifier
                                                .background(Color(0xFF0F3D3E))
                                                .border(0.9.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(7.dp))
                                            else -> Modifier
                                        }
                                    )
                                    .clickable(enabled = dayNum in 1..daysInMonth) { selectedDay = dayNum },
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNum in 1..daysInMonth) {
                                    Text(
                                        text = "$dayNum".toWesternDigits(),
                                        fontFamily = SakinaNumeralFont,
                                        fontWeight = if (isSelected || isToday || isFriday) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.8.sp,
                                        color = when {
                                            isSelected -> theme.bgDeep
                                            isToday || isFriday -> theme.accentBright
                                            else -> SakinaBrandColors.LuminousIvory
                                        },
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Spacious 5-Column Prayer Times Card ("توقيت صلوات هذا اليوم") (#6)
        // Shows ALL 5 prayers and their times in 5 columns with zero truncation!
        SakinaParchmentPanel(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 15.dp,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Text(
                text = "❖ ${s.dayPrayersCardTitle} ❖",
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = Color(0xFF0F3D3E),
                textAlign = TextAlign.Center,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x140F3D3E))
                    .border(0.8.dp, Color(0x44C5A059), RoundedCornerShape(10.dp))
                    .padding(vertical = 5.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dayPrayers.forEachIndexed { idx, (_, d) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = s.prayers.getOrElse(idx) { "" },
                            fontFamily = SakinaEditorialFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = Color(0xFF0F3D3E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = hm.format(d).toWesternDigits(),
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.2.sp,
                            color = SakinaInkDark,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontFeatureSettings = "tnum",
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                    if (idx < dayPrayers.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(0.8.dp)
                                .height(24.dp)
                                .background(Color(0x33C5A059))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Verified Hadith of the Day Card with Isnad & Grade
        val occasionText = if (isArabicOrUrdu) dailyHadith.occasionAr else dailyHadith.occasionEn
        val matnText = if (isArabicOrUrdu) dailyHadith.matnAr else dailyHadith.matnEn
        val isnadText = if (isArabicOrUrdu) dailyHadith.isnadAr else dailyHadith.isnadEn
        val matnFontSize = if (matnText.length > 145) 10.2.sp else 11.2.sp

        SakinaParchmentPanel(
            modifier = Modifier.fillMaxWidth().weight(1f),
            cornerRadius = 15.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "❖ ${s.dailyHadithTitle} ($occasionText) ❖",
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF0F3D3E),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = matnText.toWesternDigits(),
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = matnFontSize,
                    lineHeight = 15.5.sp,
                    color = SakinaInkDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = isnadText.toWesternDigits(),
                    fontFamily = SakinaBodyFont,
                    fontSize = 9.sp,
                    lineHeight = 12.5.sp,
                    color = Color(0xFF6B5224),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
