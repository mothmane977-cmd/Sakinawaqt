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

@Composable
fun SakinaCalendarScreen(ctx: Context, modifier: Modifier = Modifier) {
    var monthOffset by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    val baseCal = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 28.dp, bottom = 76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("التقويم", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 23.sp, color = Color(0xFFE6CF94))

        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, contentPadding = PaddingValues(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFE6CF94), modifier = Modifier.size(26.dp).clickable { monthOffset-- })
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(gregTitle, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFE6CF94))
                    Text(hijriSubtitle, fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.LuminousIvory)
                }
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = Color(0xFFE6CF94), modifier = Modifier.size(26.dp).clickable { monthOffset++ })
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                FULL_WEEKDAYS_AR.forEach { d ->
                    Text(
                        text = d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontFamily = SakinaBodyFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        color = Color(0xFFE6CF94),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    for (c in 0 until 7) {
                        val idx = r * 7 + c
                        val dayNum = idx - firstDayOfWeek + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(27.dp)
                                .clip(CircleShape)
                                .then(
                                    if (dayNum == clampedSelected) Modifier.background(Color(0xFF0F3D3E)).border(1.dp, Sakina3DGoldBevelBrush, CircleShape)
                                    else Modifier
                                )
                                .clickable(enabled = dayNum in 1..daysInMonth) { selectedDay = dayNum },
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNum in 1..daysInMonth) {
                                Text(
                                    text = "$dayNum".toWesternDigits(),
                                    fontFamily = SakinaNumeralFont,
                                    fontWeight = if (dayNum == clampedSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (dayNum == clampedSelected) Color(0xFFE6CF94) else SakinaBrandColors.LuminousIvory
                                )
                            }
                        }
                    }
                }
            }
        }

        SakinaParchmentPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp, contentPadding = PaddingValues(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_mosque), contentDescription = null, modifier = Modifier.size(34.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("مواقيت اليوم المختار", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F3D3E))
                    Text(
                        dayPrayers.joinToString(" · ") { "${it.first} ${hm.format(it.second)}" }.toWesternDigits(),
                        fontFamily = SakinaBodyFont,
                        fontSize = 10.5.sp,
                        color = SakinaInkDark,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
