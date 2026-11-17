package app.sakina

import android.content.Context
import android.graphics.BlurMaskFilter
import android.icu.util.IslamicCalendar
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class BrandPrayerRow(
    val nameAr: String,
    val timeText: String,
    @DrawableRes val iconRes: Int,
    val isNext: Boolean
)

@DrawableRes
private fun iconForPrayer(nameAr: String): Int = when (nameAr) {
    "الفجر" -> R.drawable.ic_fajr
    "الظهر" -> R.drawable.ic_dhuhr
    "العصر" -> R.drawable.ic_asr
    "المغرب" -> R.drawable.ic_maghrib
    else -> R.drawable.ic_isha
}

private fun nextIslamicOccasionText(hijriOffset: Int): String {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, hijriOffset) }
    val ic = IslamicCalendar().apply { timeInMillis = cal.timeInMillis }
    val y = ic.get(IslamicCalendar.YEAR)
    val occasions = listOf(
        Triple(IslamicCalendar.RAJAB, 27, "الإسراء والمعراج"),
        Triple(IslamicCalendar.RAMADAN, 1, "بداية شهر رمضان"),
        Triple(IslamicCalendar.SHAWWAL, 1, "عيد الفطر المبارك"),
        Triple(IslamicCalendar.DHU_AL_HIJJAH, 9, "يوم عرفة"),
        Triple(IslamicCalendar.MUHARRAM, 1, "رأس السنة الهجرية"),
        Triple(IslamicCalendar.MUHARRAM, 10, "يوم عاشوراء")
    )
    var bestName = "رمضان المبارك"
    var bestDays = Int.MAX_VALUE
    val nowMs = ic.timeInMillis
    for ((m, d, title) in occasions) {
        for (yr in y..y + 1) {
            val target = IslamicCalendar(yr, m, d)
            val diffDays = ((target.timeInMillis - nowMs) / 86_400_000L).toInt()
            if (diffDays in 0 until bestDays) {
                bestDays = diffDays
                bestName = title
            }
        }
    }
    return if (bestDays == 0) "المناسبة اليوم: $bestName"
    else "المناسبة القادمة: $bestName · بعد $bestDays يوماً"
}

@Composable
fun HomeScreen(ctx: Context) {
    var currentTab by remember { mutableStateOf(SakinaNavTab.HOME) }

    SakinaBrandTheme(isRtl = true) {
        SakinaBackgroundLayer(showMihrabFrame = currentTab == SakinaNavTab.HOME) {
            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(targetState = currentTab, label = "sakinaMainTabs") { tab ->
                    when (tab) {
                        SakinaNavTab.HOME -> SakinaHomeContent(ctx = ctx)
                        SakinaNavTab.QIBLA -> SakinaQiblaScreen(ctx = ctx)
                        SakinaNavTab.SETTINGS -> SakinaSettingsScreen(
                            ctx = ctx,
                            onBackClick = { currentTab = SakinaNavTab.HOME }
                        )
                    }
                }
                SakinaBottomPillBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun SakinaHomeContent(ctx: Context) {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val hmFmt = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val todayPrayers = remember(nowMs / 60_000L) { PrayerEngine.day(ctx, 0) }
    val tomorrowPrayers = remember(nowMs / 60_000L) { PrayerEngine.day(ctx, 1) }

    val nextIndexToday = todayPrayers.indexOfFirst { it.second.time > nowMs }
    val (nextName, nextTimeMs, prevTimeMs) = if (nextIndexToday >= 0) {
        val prev = if (nextIndexToday > 0) todayPrayers[nextIndexToday - 1].second.time
        else PrayerEngine.day(ctx, -1).last().second.time
        Triple(todayPrayers[nextIndexToday].first, todayPrayers[nextIndexToday].second.time, prev)
    } else {
        Triple(tomorrowPrayers.first().first, tomorrowPrayers.first().second.time, todayPrayers.last().second.time)
    }

    val remSec = ((nextTimeMs - nowMs).coerceAtLeast(0L) / 1000L)
    val countdownText = String.format(
        Locale.US, "%02d:%02d:%02d",
        remSec / 3600, (remSec % 3600) / 60, remSec % 60
    )
    val arcProgress = ((nowMs - prevTimeMs).toFloat() / (nextTimeMs - prevTimeMs).coerceAtLeast(1L)).coerceIn(0.04f, 0.96f)

    val rows = todayPrayers.mapIndexed { idx, (name, date) ->
        BrandPrayerRow(
            nameAr = name,
            timeText = hmFmt.format(date),
            iconRes = iconForPrayer(name),
            isNext = idx == nextIndexToday
        )
    }

    val maghribMs = todayPrayers.first { it.first == "المغرب" }.second.time
    val nextFajrMs = tomorrowPrayers.first { it.first == "الفجر" }.second.time
    val midnightText = hmFmt.format(Date(NightMath.midnight(maghribMs, nextFajrMs)))
    val lastThirdText = hmFmt.format(Date(NightMath.lastThird(maghribMs, nextFajrMs)))
    val occasionText = remember(nowMs / 3_600_000L) { nextIslamicOccasionText(Prefs.hijriOffset(ctx)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "سَكِينَة",
            fontFamily = SakinaEditorialFont,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = SakinaBrandColors.GoldBright
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp),
            contentAlignment = Alignment.Center
        ) {
            SakinaSolarArc(
                progress = arcProgress,
                modifier = Modifier
                    .width(270.dp)
                    .height(155.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 34.dp)
            ) {
                Text(
                    text = nextName,
                    fontFamily = SakinaHeadingFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SakinaBrandColors.LuminousIvory
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = countdownText,
                    fontFamily = SakinaNumeralFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    color = SakinaBrandColors.GoldBright,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }

        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp,
            contentPadding = PaddingValues(12.dp)
        ) {
            rows.forEachIndexed { index, prayer ->
                SakinaPrayerRow(prayer = prayer)
                if (index < rows.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_fajr),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "منتصف الليل: $midnightText",
                        fontFamily = SakinaBodyFont,
                        fontSize = 12.sp,
                        color = SakinaBrandColors.GoldBright
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(SakinaBrandColors.DividerGold)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_isha),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الثلث الأخير: $lastThirdText",
                        fontFamily = SakinaBodyFont,
                        fontSize = 12.sp,
                        color = SakinaBrandColors.LuminousIvory
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_star_ornament),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = occasionText,
                    fontFamily = SakinaBodyFont,
                    fontSize = 12.sp,
                    color = SakinaBrandColors.GoldBright
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SakinaPrayerRow(prayer: BrandPrayerRow) {
    val rowShape = RoundedCornerShape(14.dp)
    val bgBrush = if (prayer.isNext) {
        Brush.horizontalGradient(
            listOf(
                Color(0x55D4AF37),
                SakinaBrandColors.ActiveRowMidnight.copy(alpha = 0.85f),
                Color(0xCC0D131A)
            )
        )
    } else {
        Brush.horizontalGradient(listOf(Color(0x38202B38), Color(0x4D131A22)))
    }
    val borderColor = if (prayer.isNext) SakinaBrandColors.ManuscriptGold.copy(alpha = 0.78f) else Color(0x18FFFFFF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(bgBrush)
            .border(0.8.dp, borderColor, rowShape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = prayer.iconRes),
                contentDescription = prayer.nameAr,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = prayer.nameAr,
                fontFamily = SakinaHeadingFont,
                fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp,
                color = if (prayer.isNext) SakinaBrandColors.GoldBright else SakinaBrandColors.LuminousIvory
            )
        }
        Text(
            text = prayer.timeText,
            fontFamily = SakinaNumeralFont,
            fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp,
            color = if (prayer.isNext) SakinaBrandColors.GoldBright else SakinaBrandColors.SoftSilver,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}

@Composable
private fun SakinaSolarArc(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val clamped = progress.coerceIn(0.02f, 0.98f)
    Canvas(modifier = modifier) {
        val strokePx = 6.dp.toPx()
        val arcWidth = size.width - 28.dp.toPx()
        val arcHeight = arcWidth
        val topLeft = Offset((size.width - arcWidth) / 2f, 16.dp.toPx())
        val arcSize = Size(arcWidth, arcHeight)

        drawArc(
            color = Color(0x33FFFFFF),
            startAngle = 195f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        val activeSweep = 150f * clamped
        drawIntoCanvas { canvas ->
            val paint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = strokePx * 1.8f
                strokeCap = android.graphics.Paint.Cap.ROUND
                color = android.graphics.Color.argb(135, 243, 220, 139)
                maskFilter = BlurMaskFilter(18f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawArc(
                topLeft.x, topLeft.y,
                topLeft.x + arcSize.width, topLeft.y + arcSize.height,
                195f, activeSweep, false, paint
            )
        }

        drawArc(
            brush = Brush.horizontalGradient(listOf(SakinaBrandColors.ManuscriptGold, SakinaBrandColors.GoldBright)),
            startAngle = 195f,
            sweepAngle = activeSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        val angleRad = (195.0 + activeSweep) * PI / 180.0
        val center = Offset(topLeft.x + arcSize.width / 2f, topLeft.y + arcSize.height / 2f)
        val radius = arcSize.width / 2f
        val orbPos = Offset(
            center.x + (radius * cos(angleRad)).toFloat(),
            center.y + (radius * sin(angleRad)).toFloat()
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(SakinaBrandColors.GoldBright, Color.Transparent), center = orbPos, radius = 16.dp.toPx()),
            radius = 16.dp.toPx(),
            center = orbPos
        )
        drawCircle(
            color = SakinaBrandColors.GoldBright,
            radius = 5.5.dp.toPx(),
            center = orbPos
        )
    }
}
