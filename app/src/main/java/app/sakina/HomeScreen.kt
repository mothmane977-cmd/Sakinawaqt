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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeQuickSheet by remember { mutableStateOf<String?>(null) }

    SakinaBrandTheme(isRtl = true) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color(0xF50B1016),
                    modifier = Modifier.width(285.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp, vertical = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "سَكِينَة · القائمة",
                            fontFamily = SakinaEditorialFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFFE6CF94)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SakinaDrawerPillItem(R.drawable.ic_star_ornament, "المسبحة الإلكترونية", "v1.1") {
                            scope.launch { drawerState.close() }
                            activeQuickSheet = "المسبحة الإلكترونية: سيتم تفعيل مسبحة الذكر الدائرية (33 / 99 / 100) في التحديث القادم."
                        }
                        SakinaDrawerPillItem(R.drawable.ic_mosque, "السنن الرواتب", "12 ركعة") {
                            scope.launch { drawerState.close() }
                            activeQuickSheet = "جدول السنن الرواتب (قبلية وبعدية لكل صلاة) جاهز للإدراج في التحديث القادم."
                        }
                        SakinaDrawerPillItem(R.drawable.ic_fajr, "الأذكار اليومية", "حصن المسلم") {
                            scope.launch { drawerState.close() }
                            activeQuickSheet = "أذكار الصباح والمساء والنوم بالتخريج الصحيح جاهزة للإدراج في التحديث القادم."
                        }
                        SakinaDrawerPillItem(R.drawable.ic_kaaba, "التقويم الهجري", "تفاعلي") {
                            scope.launch { drawerState.close() }
                            currentTab = SakinaNavTab.SETTINGS
                        }
                        SakinaDrawerPillItem(R.drawable.ic_dhuhr, "الإعدادات والتنبيهات") {
                            scope.launch { drawerState.close() }
                            currentTab = SakinaNavTab.SETTINGS
                        }
                    }
                }
            }
        ) {
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

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .padding(start = 18.dp, top = 10.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC131B22))
                            .border(0.65.dp, Sakina3DGoldBevelBrush, CircleShape)
                            .clickable { scope.launch { drawerState.open() } },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_star_ornament),
                            contentDescription = "القائمة",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    SakinaBottomPillBar(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        if (activeQuickSheet != null) {
            AlertDialog(
                onDismissRequest = { activeQuickSheet = null },
                containerColor = SakinaBrandColors.SakinaSurface,
                title = {
                    Text("سَكِينَة", fontFamily = SakinaHeadingFont, color = Color(0xFFE6CF94))
                },
                text = {
                    Text(activeQuickSheet!!, fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory)
                },
                confirmButton = {
                    TextButton(onClick = { activeQuickSheet = null }) {
                        Text("حسناً", fontFamily = SakinaHeadingFont, color = Color(0xFFE6CF94))
                    }
                }
            )
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
        BrandPrayerRow(name, hmFmt.format(date), iconForPrayer(name), idx == nextIndexToday)
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
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 48.dp, bottom = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. اسم التطبيق مستقر أسفل قوس المحراب بدون تداخل
        Text(
            text = "سَكِينَة",
            fontFamily = SakinaEditorialFont,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFFE6CF94)
        )

        // 2. قوس الشمس المدمج
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            contentAlignment = Alignment.Center
        ) {
            SakinaSolarArc(
                progress = arcProgress,
                modifier = Modifier
                    .width(210.dp)
                    .height(104.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 18.dp)
            ) {
                Text(
                    text = nextName,
                    fontFamily = SakinaHeadingFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SakinaBrandColors.LuminousIvory
                )
                Text(
                    text = countdownText,
                    fontFamily = SakinaNumeralFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = Color(0xFFE6CF94),
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }

        // 3. خانات الصلوات الخمس الرقيقة كأزرار الإعدادات
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEach { prayer -> SakinaPrayerRow(prayer = prayer) }
        }

        // 4. منتصف الليل والثلث الأخير + المناسبة القادمة
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xCC161E28), Color(0xEB0B0F15))))
                    .border(0.65.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(17.dp))
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("منتصف الليل: $midnightText", fontFamily = SakinaBodyFont, fontSize = 10.5.sp, color = Color(0xFFE6CF94), maxLines = 1)
                Box(Modifier.width(1.dp).height(12.dp).background(SakinaBrandColors.DividerGold))
                Text("الثلث الأخير: $lastThirdText", fontFamily = SakinaBodyFont, fontSize = 10.5.sp, color = SakinaBrandColors.LuminousIvory, maxLines = 1)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xCC161E28), Color(0xEB0B0F15))))
                    .border(0.65.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(id = R.drawable.ic_star_ornament), contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(occasionText, fontFamily = SakinaBodyFont, fontSize = 10.5.sp, color = Color(0xFFE6CF94), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SakinaPrayerRow(prayer: BrandPrayerRow) {
    val pillShape = RoundedCornerShape(19.dp)
    val bgBrush = if (prayer.isNext) {
        Brush.horizontalGradient(listOf(Color(0x45C5A059), Color(0xCC11223B), Color(0xEB0B0F15)))
    } else {
        Brush.verticalGradient(listOf(Color(0xCC161E28), Color(0xE60B0F15)))
    }
    val borderBrush = if (prayer.isNext) Sakina3DGoldBevelBrush
    else Brush.horizontalGradient(listOf(Color(0x446E5629), Color(0x88C5A059), Color(0x446E5629)))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(pillShape)
            .background(bgBrush)
            .border(0.65.dp, borderBrush, pillShape)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = prayer.iconRes),
                contentDescription = prayer.nameAr,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = prayer.nameAr,
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (prayer.isNext) Color(0xFFE6CF94) else SakinaBrandColors.LuminousIvory
            )
        }
        Text(
            text = prayer.timeText,
            fontFamily = SakinaNumeralFont,
            fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.5.sp,
            color = if (prayer.isNext) Color(0xFFE6CF94) else SakinaBrandColors.SoftSilver,
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
        val strokePx = 5.dp.toPx()
        val arcWidth = size.width - 20.dp.toPx()
        val arcHeight = arcWidth
        val topLeft = Offset((size.width - arcWidth) / 2f, 10.dp.toPx())
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
                color = android.graphics.Color.argb(125, 230, 207, 148)
                maskFilter = BlurMaskFilter(14f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawArc(
                topLeft.x, topLeft.y,
                topLeft.x + arcSize.width, topLeft.y + arcSize.height,
                195f, activeSweep, false, paint
            )
        }

        drawArc(
            brush = Brush.horizontalGradient(listOf(Color(0xFFC5A059), Color(0xFFE6CF94))),
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
            brush = Brush.radialGradient(listOf(Color(0xFFE6CF94), Color.Transparent), center = orbPos, radius = 12.dp.toPx()),
            radius = 12.dp.toPx(),
            center = orbPos
        )
        drawCircle(
            color = Color(0xFFE6CF94),
            radius = 4.5.dp.toPx(),
            center = orbPos
        )
    }
}
