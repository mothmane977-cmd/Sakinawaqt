package app.sakina

import android.content.Context
import android.icu.util.IslamicCalendar
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

private val HOME_HIJRI_MONTHS = listOf(
    "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
    "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
)

@Composable
fun HomeScreen(ctx: Context) {
    var currentTab by remember { mutableStateOf(SakinaNavTab.HOME) }
    var showSettings by remember { mutableStateOf(false) }
    var serviceDialogTitle by remember { mutableStateOf<String?>(null) }
    var serviceDialogBody by remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val headerImg = when {
        showSettings -> R.drawable.bg_header_mosque
        currentTab == SakinaNavTab.QIBLA -> R.drawable.bg_header_kaaba
        else -> R.drawable.bg_header_mosque
    }

    SakinaBrandTheme(isRtl = true) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = SakinaRoyalNavy, modifier = Modifier.width(285.dp)) {
                    Column(
                        modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("سَكِينَة · خدمات إضافية", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFE6CF94))
                        Spacer(Modifier.height(4.dp))

                        SakinaDrawerPillItem(R.drawable.ic_sunan, "السنن الرواتب", "12 ركعة") {
                            scope.launch { drawerState.close() }
                            serviceDialogTitle = "السنن الرواتب (12 ركعة)"
                            serviceDialogBody = "• الفجر: 2 ركعتان قبلها\n• الظهر: 4 قبلها + 2 بعدها\n• المغرب: 2 ركعتان بعدها\n• العشاء: 2 ركعتان بعدها"
                        }
                        SakinaDrawerPillItem(R.drawable.ic_dhuhr, "وقت الضحى وقيام الليل") {
                            scope.launch { drawerState.close() }
                            val events = PrayerEngine.events(ctx, 0)
                            val sunrise = events.first { it.first == 1 }.second.time
                            val dhuhr = events.first { it.first == 2 }.second.time
                            val hm = SimpleDateFormat("HH:mm", Locale.US)
                            val duhaStart = hm.format(Date(sunrise + 15 * 60_000L))
                            val duhaEnd = hm.format(Date(dhuhr - 10 * 60_000L))
                            serviceDialogTitle = "صلاة الضحى والقيام"
                            serviceDialogBody = "• وقت صلاة الضحى اليوم: من $duhaStart إلى $duhaEnd\n• أفضل وقتها حين ترمض الفصال (قبل الظهر بساعتين).".toWesternDigits()
                        }
                        SakinaDrawerPillItem(R.drawable.ic_fajr, "صيام التطوع والأيام البيض") {
                            scope.launch { drawerState.close() }
                            val ic = IslamicCalendar()
                            val hDay = ic.get(IslamicCalendar.DAY_OF_MONTH)
                            serviceDialogTitle = "صيام التطوع"
                            serviceDialogBody = "• اليوم الهجري: $hDay\n• الأيام البيض: 13 و14 و15 من كل شهر هجري\n• سنن الأسبوع: الإثنين والخميس".toWesternDigits()
                        }
                        SakinaDrawerPillItem(R.drawable.ic_star_ornament, "المناسبات الإسلامية القادمة") {
                            scope.launch { drawerState.close() }
                            serviceDialogTitle = "المناسبات القادمة"
                            serviceDialogBody = "• ليلة الإسراء والمعراج (27 رجب)\n• بداية شهر رمضان (1 رمضان)\n• عيد الفطر (1 شوال)\n• يوم عرفة (9 ذو الحجة)".toWesternDigits()
                        }
                        SakinaDrawerPillItem(R.drawable.ic_settings, "الإعدادات والتنبيهات") {
                            scope.launch { drawerState.close() }
                            showSettings = true
                        }
                    }
                }
            }
        ) {
            SakinaBackgroundLayer(headerImageRes = headerImg, showMihrabFrame = true) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (showSettings) {
                        SakinaSettingsScreen(ctx = ctx, onBackClick = { showSettings = false })
                    } else {
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

                    if (!showSettings) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .statusBarsPadding()
                                .padding(start = 24.dp, top = 22.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xC0071422))
                                .border(0.7.dp, Sakina3DGoldBevelBrush, CircleShape)
                                .clickable { scope.launch { drawerState.open() } },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(painter = painterResource(id = R.drawable.ic_menu), contentDescription = "القائمة", modifier = Modifier.size(16.dp))
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(end = 24.dp, top = 22.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xC0071422))
                                .border(0.7.dp, Sakina3DGoldBevelBrush, CircleShape)
                                .clickable { showSettings = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(painter = painterResource(id = R.drawable.ic_settings), contentDescription = "الإعدادات", modifier = Modifier.size(18.dp))
                        }
                    }

                    SakinaBottomPillBar(
                        currentTab = currentTab,
                        onTabSelected = { showSettings = false; currentTab = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        if (serviceDialogTitle != null && serviceDialogBody != null) {
            AlertDialog(
                onDismissRequest = { serviceDialogTitle = null },
                containerColor = SakinaRoyalNavyCard,
                title = { Text(serviceDialogTitle!!, fontFamily = SakinaHeadingFont, color = Color(0xFFE6CF94)) },
                text = { Text(serviceDialogBody!!, fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory, lineHeight = 24.sp) },
                confirmButton = {
                    TextButton(onClick = { serviceDialogTitle = null }) {
                        Text("إغلاق", fontFamily = SakinaHeadingFont, color = Color(0xFFE6CF94))
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
        SakinaLiveNotifier.update(ctx)
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val hmFmt = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val todayPrayers = remember(nowMs / 60_000L) { PrayerEngine.day(ctx, 0) }
    val tomorrowPrayers = remember(nowMs / 60_000L) { PrayerEngine.day(ctx, 1) }

    val nextIndexToday = todayPrayers.indexOfFirst { it.second.time > nowMs }
    val (nextName, nextTimeMs) = if (nextIndexToday >= 0) {
        todayPrayers[nextIndexToday].first to todayPrayers[nextIndexToday].second.time
    } else {
        tomorrowPrayers.first().first to tomorrowPrayers.first().second.time
    }

    val remSec = ((nextTimeMs - nowMs).coerceAtLeast(0L) / 1000L)
    val countdownText = String.format(Locale.US, "%02d:%02d:%02d", remSec / 3600, (remSec % 3600) / 60, remSec % 60).toWesternDigits()

    val rows = todayPrayers.mapIndexed { idx, (name, date) ->
        BrandPrayerRow(name, hmFmt.format(date).toWesternDigits(), iconForPrayer(name), idx == nextIndexToday)
    }

    val maghribMs = todayPrayers.first { it.first == "المغرب" }.second.time
    val nextFajrMs = tomorrowPrayers.first { it.first == "الفجر" }.second.time
    val midnightText = hmFmt.format(Date(NightMath.midnight(maghribMs, nextFajrMs))).toWesternDigits()
    val lastThirdText = hmFmt.format(Date(NightMath.lastThird(maghribMs, nextFajrMs))).toWesternDigits()

    val (gregText, hijriText) = remember(nowMs / 3_600_000L) {
        val g = SimpleDateFormat("dd MMMM yyyy", Locale("ar")).format(Date()).toWesternDigits()
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, Prefs.hijriOffset(ctx)) }
        val ic = IslamicCalendar().apply { timeInMillis = cal.timeInMillis }
        val h = "${ic.get(IslamicCalendar.DAY_OF_MONTH)} ${HOME_HIJRI_MONTHS[ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)]} ${ic.get(IslamicCalendar.YEAR)} هـ".toWesternDigits()
        g to h
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("سَكِينَة", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFE6CF94))
            Spacer(Modifier.height(2.dp))
            Text("${Place.name(ctx)} · $gregText", fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.LuminousIvory)
            Text(hijriText, fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = Color(0xFFE6CF94))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("الصلاة القادمة: $nextName", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SakinaBrandColors.LuminousIvory)
            Spacer(Modifier.height(6.dp))
            SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 28.dp, vertical = 8.dp)) {
                Text(
                    text = countdownText,
                    fontFamily = SakinaNumeralFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = Color(0xFFE6CF94),
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }

        SakinaParchmentPanel(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
            rows.forEachIndexed { index, prayer ->
                SakinaPrayerRow(prayer = prayer)
                if (index < rows.lastIndex) HorizontalDivider(color = Color(0x22C5A059))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(Color(0xDC0B1C2E))
                .border(0.7.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(17.dp))
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("منتصف الليل: $midnightText", fontFamily = SakinaBodyFont, fontSize = 11.sp, color = Color(0xFFE6CF94))
            Box(Modifier.width(1.dp).height(12.dp).background(SakinaBrandColors.DividerGold))
            Text("الثلث الأخير: $lastThirdText", fontFamily = SakinaBodyFont, fontSize = 11.sp, color = SakinaBrandColors.LuminousIvory)
        }
    }
}

@Composable
private fun SakinaPrayerRow(prayer: BrandPrayerRow) {
    val bgColor = if (prayer.isNext) Color(0x26C5A059) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = prayer.iconRes), contentDescription = prayer.nameAr, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = prayer.nameAr,
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (prayer.isNext) Color(0xFF0F3D3E) else SakinaInkDark
            )
        }
        Text(
            text = prayer.timeText.toWesternDigits(),
            fontFamily = SakinaNumeralFont,
            fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.5.sp,
            color = if (prayer.isNext) Color(0xFF0F3D3E) else Color(0xFF4A3D26),
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}
