package app.sakina

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SakinaAlarmScreen(ctx: Context, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler { onBackClick() }
    val theme = LocalSakinaTheme.current
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    var suhoorEnabled by remember { mutableStateOf(sp.getBoolean("suhoor_enabled", true)) }
    var suhoorOffset by remember { mutableIntStateOf(sp.getInt("suhoor_offset", 40)) }
    var volumePct by remember { mutableFloatStateOf(sp.getFloat("alarm_volume", 0.85f)) }
    var fajrFade by remember { mutableStateOf(Prefs.fajrFade(ctx)) }
    var selectedSound by remember { mutableIntStateOf(Modes.get(ctx, "الفجر")) }
    var statusMsg by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 66.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.bgCard)
                    .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(16.dp))
                    .clickable { onBackClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("رجوع", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = theme.accentBright)
            }
            SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 5.dp)) {
                Text("المنبه والتنبيهات", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = theme.accentBright)
            }
            Spacer(Modifier.width(44.dp))
        }

        // Suhoor time selector
        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), glowGold = suhoorEnabled) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("منبه السحور والتهجد", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = theme.accentBright)
                    Text("$suhoorOffset دقيقة قبل الفجر".toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 12.sp, color = SakinaBrandColors.SoftSilver)
                }
                SakinaGoldSwitch(checked = suhoorEnabled, onCheckedChange = {
                    suhoorEnabled = it; sp.edit().putBoolean("suhoor_enabled", it).apply()
                })
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(20, 30, 40, 60).forEach { mins ->
                    val sel = mins == suhoorOffset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) theme.accentGold else Color(0x22FFFFFF))
                            .clickable { suhoorOffset = mins; sp.edit().putInt("suhoor_offset", mins).apply() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$mins د".toWesternDigits(), fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory)
                    }
                }
            }
        }

        // Volume Slider & Fade
        SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("درجة صوت الأذان والمنبه: ${(volumePct * 100).roundToInt()}%".toWesternDigits(), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
            Slider(
                value = volumePct,
                onValueChange = { volumePct = it; sp.edit().putFloat("alarm_volume", it).apply() },
                valueRange = 0.2f..1f,
                colors = SliderDefaults.colors(thumbColor = theme.accentBright, activeTrackColor = theme.accentGold)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("تدرّج صوت الفجر الهادئ", fontFamily = SakinaBodyFont, fontSize = 13.sp, color = SakinaBrandColors.LuminousIvory)
                SakinaGoldSwitch(checked = fajrFade, onCheckedChange = {
                    fajrFade = it; Prefs.setFajrFade(ctx, it)
                })
            }
        }

        // Sound Type Selector
        SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("اختيار صوت المنبه الافتراضي", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Modes.labels.forEachIndexed { idx, label ->
                    val sel = idx == selectedSound
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) theme.accentGold else Color(0x22FFFFFF))
                            .clickable {
                                selectedSound = idx
                                PRAYER_NAMES.filter { it != "الشروق" }.forEach { Modes.set(ctx, it, idx) }
                                AlarmScheduler.scheduleNext(ctx)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontFamily = SakinaBodyFont, fontSize = 10.5.sp, color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory, maxLines = 1)
                    }
                }
            }
        }

        // Test Alarm Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.horizontalGradient(listOf(theme.accentGold, theme.accentBright)))
                .clickable {
                    AlarmScheduler.scheduleTest(ctx)
                    statusMsg = "تمت جدولة تجربة المنبه بعد 10 ثوانٍ".toWesternDigits()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(if (statusMsg.isEmpty()) "اختبار رنين المنبه الآن (بعد 10 ثوانٍ)".toWesternDigits() else statusMsg, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.bgDeep)
        }
    }
}
