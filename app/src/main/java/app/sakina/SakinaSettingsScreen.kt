package app.sakina

import android.content.Context
import android.icu.util.IslamicCalendar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
    val methodLabel = remember(refreshTick) { Prefs.methodLabel(ctx) }
    val lastDiag = remember(refreshTick) { DiagLog.get(ctx).firstOrNull() ?: "جاهز · لا توجد أخطاء" }

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

        // 1. Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الإعدادات والتنبيهات",
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = SakinaBrandColors.GoldBright
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "رجوع",
                tint = SakinaBrandColors.GoldBright,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. 3-Segment Audio Mode Pill
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            contentPadding = PaddingValues(6.dp)
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
                            .clip(shape)
                            .background(
                                if (selected) Brush.verticalGradient(listOf(Color(0x44D4AF37), Color(0x22D4AF37)))
                                else Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x11FFFFFF)))
                            )
                            .border(
                                1.dp,
                                if (selected) SakinaBrandColors.GoldBright else Color.Transparent,
                                shape
                            )
                            .clickable {
                                PRAYER_NAMES.filter { it != "الشروق" }.forEach { p ->
                                    Modes.set(ctx, p, mode.modeCode)
                                }
                                AlarmScheduler.scheduleNext(ctx)
                                refreshTick++
                            }
                            .padding(vertical = 10.dp),
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

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Glowing Suhoor Card
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            glowGold = suhoorEnabled,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
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
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "منبه السحور",
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SakinaBrandColors.LuminousIvory
                        )
                        Text(
                            text = "40 دقيقة قبل الفجر",
                            fontFamily = SakinaBodyFont,
                            fontSize = 12.sp,
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

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Dual Hijri / Gregorian Calendar Card
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hijriText,
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SakinaBrandColors.GoldBright
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(SakinaBrandColors.DividerGold)
                )
                Text(
                    text = gregorianText,
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SakinaBrandColors.LuminousIvory
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Location, Calculation Method & Alarm Test
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            SettingCompactRow("الموقع الحالي (اضغط للتحديث)", placeName) {
                val loc = Place.fromDevice(ctx)
                if (loc != null) Place.set(ctx, loc.latitude, loc.longitude, "موقعي الحالي")
                else Place.reset(ctx)
                AlarmScheduler.scheduleNext(ctx)
                refreshTick++
            }
            Spacer(modifier = Modifier.height(10.dp))
            SettingCompactRow("طريقة الحساب (اضغط للتبديل)", methodLabel) {
                Prefs.cycleMethod(ctx)
                AlarmScheduler.scheduleNext(ctx)
                refreshTick++
            }
            Spacer(modifier = Modifier.height(10.dp))
            SettingCompactRow("تجربة المنبه الفورية (بعد 10 ثوانٍ)", lastDiag) {
                AlarmScheduler.scheduleTest(ctx)
                refreshTick++
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SettingCompactRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x28FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = SakinaBrandColors.LuminousIvory
            )
            Text(
                text = subtitle,
                fontFamily = SakinaBodyFont,
                fontSize = 11.sp,
                color = SakinaBrandColors.SoftSilver
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_star_ornament),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}
