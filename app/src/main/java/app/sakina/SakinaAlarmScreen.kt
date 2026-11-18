package app.sakina

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SakinaAlarmScreen(ctx: Context, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler { onBackClick() }
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
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
            .padding(start = 16.dp, end = 16.dp, top = 54.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SakinaBackIconButton(onClick = onBackClick)
            SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 32.dp, vertical = 6.dp)) {
                Text(s.alarmTitle, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = theme.accentBright, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.size(32.dp))
        }

        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), glowGold = suhoorEnabled) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(s.suhoorTitle, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = theme.accentBright, maxLines = 1)
                    Text("$suhoorOffset ${s.minsBeforeFajr}".toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.SoftSilver, maxLines = 1)
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
                        modifier = Modifier.weight(1f).height(30.dp).clip(RoundedCornerShape(10.dp)).background(if (sel) theme.accentGold else Color(0x22FFFFFF)).clickable { suhoorOffset = mins; sp.edit().putInt("suhoor_offset", mins).apply() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$mins ${s.minuteShort}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory)
                    }
                }
            }
        }

        SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("${s.volumeLabel}: ${(volumePct * 100).roundToInt()}%".toWesternDigits(), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.accentBright)
            Slider(
                value = volumePct,
                onValueChange = { volumePct = it; sp.edit().putFloat("alarm_volume", it).apply() },
                valueRange = 0.2f..1f,
                colors = SliderDefaults.colors(thumbColor = theme.accentBright, activeTrackColor = theme.accentGold)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(s.fajrFadeLabel, fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                SakinaGoldSwitch(checked = fajrFade, onCheckedChange = {
                    fajrFade = it; Prefs.setFajrFade(ctx, it)
                })
            }
        }

        SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(s.defaultSoundLabel, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.accentBright)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                s.soundModes.forEachIndexed { idx, label ->
                    val sel = idx == selectedSound
                    Box(
                        modifier = Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(10.dp)).background(if (sel) theme.accentGold else Color(0x22FFFFFF)).clickable {
                            selectedSound = idx
                            PRAYER_NAMES.filter { it != "الشروق" }.forEach { Modes.set(ctx, it, idx) }
                            AlarmScheduler.scheduleNext(ctx)
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontFamily = SakinaBodyFont, fontSize = 10.sp, color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(21.dp)).background(Brush.horizontalGradient(listOf(theme.accentGold, theme.accentBright))).clickable {
                AlarmScheduler.scheduleTest(ctx)
                statusMsg = s.testAlarmScheduled.toWesternDigits()
            },
            contentAlignment = Alignment.Center
        ) {
            Text(if (statusMsg.isEmpty()) s.testAlarmBtn.toWesternDigits() else statusMsg, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = theme.bgDeep, maxLines = 1)
        }
    }
}
