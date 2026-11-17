package app.sakina

import android.content.Context
import android.icu.util.IslamicCalendar
import androidx.activity.compose.BackHandler
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
fun SakinaSettingsScreen(ctx: Context, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler { onBackClick() }

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
        val gFmt = SimpleDateFormat("dd MMM yyyy", Locale("ar")).format(Date()).toWesternDigits()
        "$hDay $hMonth $hYear".toWesternDigits() to gFmt
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 92.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("الإعدادات والتنبيهات", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFE6CF94))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0x26C5A059))
                    .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "رجوع", tint = Color(0xFFE6CF94), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp, contentPadding = PaddingValues(5.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GlobalAudioMode.entries.forEach { mode ->
                    val selected = mode == selectedAudioMode
                    val shape = RoundedCornerShape(12.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(shape)
                            .background(
                                if (selected) Brush.verticalGradient(listOf(Color(0x4AC5A059), Color(0x1FC5A059)))
                                else Brush.verticalGradient(listOf(Color(0x1CFFFFFF), Color(0x0AFFFFFF)))
                            )
                            .border(0.8.dp, if (selected) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color(0x22FFFFFF), Color(0x11FFFFFF))), shape)
                            .clickable {
                                PRAYER_NAMES.filter { it != "الشروق" }.forEach { p -> Modes.set(ctx, p, mode.modeCode) }
                                AlarmScheduler.scheduleNext(ctx)
                                refreshTick++
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(mode.labelAr, fontFamily = SakinaHeadingFont, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp, color = if (selected) Color(0xFFE6CF94) else SakinaBrandColors.SoftSilver, maxLines = 1)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, glowGold = suhoorEnabled, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(id = R.drawable.ic_fajr), contentDescription = null, modifier = Modifier.size(30.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("منبه السحور", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SakinaBrandColors.LuminousIvory)
                        Text("40 دقيقة قبل الفجر", fontFamily = SakinaBodyFont, fontSize = 11.sp, color = SakinaBrandColors.SoftSilver)
                    }
                }
                SakinaGoldSwitch(checked = suhoorEnabled, onCheckedChange = {
                    suhoorEnabled = it; suhoorPrefs.edit().putBoolean("suhoor_enabled", it).apply()
                })
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 13.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(hijriText, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFFE6CF94), textAlign = TextAlign.Center, maxLines = 1, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.width(1.dp).height(20.dp).background(SakinaBrandColors.DividerGold))
                Text(gregorianText, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, textAlign = TextAlign.Center, maxLines = 1, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            SakinaLuxuryPillButton(R.drawable.ic_mosque, "الموقع", placeName) {
                val loc = Place.fromDevice(ctx)
                if (loc != null) Place.set(ctx, loc.latitude, loc.longitude, "موقعي الحالي") else Place.reset(ctx)
                AlarmScheduler.scheduleNext(ctx); refreshTick++
            }
            SakinaLuxuryPillButton(R.drawable.ic_star_ornament, "طريقة الحساب", methodShort) {
                Prefs.cycleMethod(ctx); AlarmScheduler.scheduleNext(ctx); refreshTick++
            }
            SakinaLuxuryPillButton(R.drawable.ic_fajr, "اختبار المنبه", testStatus) {
                AlarmScheduler.scheduleTest(ctx); refreshTick++
            }
        }
    }
}

@Composable
private fun SakinaLuxuryPillButton(@DrawableRes iconRes: Int, title: String, valueText: String, onClick: () -> Unit) {
    val pillShape = RoundedCornerShape(23.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(pillShape)
            .background(Brush.verticalGradient(listOf(Color(0xD618222D), Color(0xEB0B0F15))))
            .border(0.75.dp, Sakina3DGoldBevelBrush, pillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SakinaBrandColors.LuminousIvory, maxLines = 1)
        }
        Text(valueText.toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 12.sp, color = Color(0xFFE6CF94), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
