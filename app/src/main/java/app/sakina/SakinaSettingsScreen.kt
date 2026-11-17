package app.sakina

import android.content.Context
import android.icu.util.IslamicCalendar
import androidx.annotation.DrawableRes
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

private enum class GlobalAudioMode(val labelAr: String, val modeCode: Int) {
    ALL_SOUND("كلها بصوت", 0),
    ALL_VIBRATE("كلها اهتزاز", 2),
    MUTE_ALL("إيقاف الكل", 3)
}

private val HIJRI_MONTHS_AR = listOf(
    "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
    "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
    "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
)

@Composable
fun SakinaSettingsScreen(
    ctx: Context,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var refreshTick by remember { mutableIntStateOf(0) }
    val currentFajrMode = remember(refreshTick) { Modes.get(ctx, "الفجر") }
    val selectedAudioMode = when (currentFajrMode) {
        2 -> GlobalAudioMode.ALL_VIBRATE
        3 -> GlobalAudioMode.MUTE_ALL
        else -> GlobalAudioMode.ALL_SOUND
    }

    val suhoorPrefs = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }
    var suhoorEnabled by remember { mutableStateOf(suhoorPrefs.getBoolean("suhoor_enabled", true)) }
    val placeName = remember(refreshTick) { Place.name(ctx) }
    val methodShort = remember(refreshTick) { Prefs.METHODS[Prefs.method(ctx)] }
    val testStatus = remember(refreshTick) {
        val last = DiagLog.get(ctx).firstOrNull() ?: ""
        if (last.contains("تجربة")) "مُجدوَلة" else "تشغيل 10ث"
    }

    val (hijriText, gregorianText) = remember(refreshTick) {
        val off = Prefs.hijriOffset(ctx)
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, off) }
        val ic = IslamicCalendar().apply { timeInMillis = cal.timeInMillis }
        val hDay = ic.get(IslamicCalendar.DAY_OF_MONTH)
        val hMonth = HIJRI_MONTHS_AR[ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)]
        val hYear = ic.get(IslamicCalendar.YEAR)
        val gFmt = SimpleDateFormat("dd MMMM yyyy", Locale("ar")).format(Date())
        "$hDay $hMonth $hYear" to gFmt
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // 1. الترويسة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الإعدادات والتنبيهات",
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = SakinaBrandColors.GoldBright
            )
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0x26D4AF37))
                    .border(0.8.dp, SakinaBrandColors.ManuscriptGold.copy(alpha = 0.6f), CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "رجوع",
                    tint = SakinaBrandColors.GoldBright,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. شريط أوضاع الصوت الثلاثي
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            contentPadding = PaddingValues(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlobalAudioMode.entries.forEach { mode ->
                    val selected = mode == selectedAudioMode
                    val shape = RoundedCornerShape(12.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(shape)
                            .background(
                                if (selected) Brush.verticalGradient(listOf(Color(0x4AD4AF37), Color(0x1FD4AF37)))
                                else Brush.verticalGradient(listOf(Color(0x1CFFFFFF), Color(0x0AFFFFFF)))
                            )
                            .border(
                                0.9.dp,
                                if (selected) Brush.horizontalGradient(
                                    listOf(SakinaBrandColors.ManuscriptGold, SakinaBrandColors.GoldBright, SakinaBrandColors.ManuscriptGold)
                                ) else Brush.horizontalGradient(listOf(Color(0x22FFFFFF), Color(0x11FFFFFF))),
                                shape
                            )
                            .clickable {
                                PRAYER_NAMES.filter { it != "الشروق" }.forEach { p ->
                                    Modes.set(ctx, p, mode.modeCode)
                                }
                                AlarmScheduler.scheduleNext(ctx)
                                refreshTick++
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.labelAr,
                            fontFamily = SakinaHeadingFont,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selected) SakinaBrandColors.GoldBright else SakinaBrandColors.SoftSilver,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. بطاقة منبه السحور
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            glowGold = suhoorEnabled,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
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
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "منبه السحور",
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SakinaBrandColors.LuminousIvory
                        )
                        Text(
                            text = "40 دقيقة قبل الفجر",
                            fontFamily = SakinaBodyFont,
                            fontSize = 11.sp,
                            color = SakinaBrandColors.SoftSilver
                        )
                    }
                }
                SakinaGoldSwitch(
                    checked = suhoorEnabled,
                    onCheckedChange = { enabled ->
                        suhoorEnabled = enabled
                        suhoorPrefs.edit().putBoolean("suhoor_enabled", enabled).apply()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. بطاقة التاريخ الهجري والميلادي المزدوجة
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hijriText,
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SakinaBrandColors.GoldBright,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(22.dp)
                        .background(SakinaBrandColors.DividerGold)
                )
                Text(
                    text = gregorianText,
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SakinaBrandColors.LuminousIvory,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. أزرار ذهبية رشيقة مدمجة الأيقونات (46dp فقط لكل زر)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SakinaLuxuryPillButton(
                iconRes = R.drawable.ic_mosque,
                title = "الموقع",
                valueText = placeName,
                onClick = {
                    val loc = Place.fromDevice(ctx)
                    if (loc != null) Place.set(ctx, loc.latitude, loc.longitude, "موقعي الحالي")
                    else Place.reset(ctx)
                    AlarmScheduler.scheduleNext(ctx)
                    refreshTick++
                }
            )
            SakinaLuxuryPillButton(
                iconRes = R.drawable.ic_star_ornament,
                title = "طريقة الحساب",
                valueText = methodShort,
                onClick = {
                    Prefs.cycleMethod(ctx)
                    AlarmScheduler.scheduleNext(ctx)
                    refreshTick++
                }
            )
            SakinaLuxuryPillButton(
                iconRes = R.drawable.ic_fajr,
                title = "اختبار المنبه",
                valueText = testStatus,
                onClick = {
                    AlarmScheduler.scheduleTest(ctx)
                    refreshTick++
                }
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun SakinaLuxuryPillButton(
    @DrawableRes iconRes: Int,
    title: String,
    valueText: String,
    onClick: () -> Unit
) {
    val pillShape = RoundedCornerShape(24.dp)
    val goldBevelBorder = Brush.horizontalGradient(
        listOf(
            Color(0x668C6D2E),
            SakinaBrandColors.GoldBright,
            SakinaBrandColors.ManuscriptGold,
            Color(0x668C6D2E)
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(pillShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xD618222D), Color(0xEB0B0F15))
                )
            )
            .border(0.9.dp, goldBevelBorder, pillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0x24D4AF37))
                    .border(0.7.dp, SakinaBrandColors.ManuscriptGold.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SakinaBrandColors.LuminousIvory,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = valueText,
            fontFamily = SakinaBodyFont,
            fontSize = 12.sp,
            color = SakinaBrandColors.GoldBright,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
