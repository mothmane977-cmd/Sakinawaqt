package app.sakina

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
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

    var suhoorEnabled by remember { mutableStateOf(sp.getBoolean("suhoor_alarm_on", true)) }
    var suhoorMinutesBeforeFajr by remember { mutableIntStateOf(sp.getInt("suhoor_mins", 40)) }
    var adhanVolume by remember { mutableFloatStateOf(sp.getFloat("adhan_vol", 0.85f)) }
    var fajrGentleFade by remember { mutableStateOf(Prefs.fajrFade(ctx)) }
    var defaultAdhanMode by remember { mutableIntStateOf(sp.getInt("default_adhan_mode", 0)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 50.dp, bottom = 74.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SakinaBackIconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart))
            SakinaOrnamentCartouche(
                modifier = Modifier.align(Alignment.Center),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 7.dp)
            ) {
                Text(
                    text = s.alarmTitle,
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = theme.accentBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 1. Suhoor & Qiyam Alarm
        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), glowGold = suhoorEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = s.suhoorTitle,
                        fontFamily = SakinaHeadingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = theme.accentBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$suhoorMinutesBeforeFajr ${s.minsBeforeFajr}".toWesternDigits(),
                        fontFamily = SakinaBodyFont,
                        fontSize = 11.5.sp,
                        color = SakinaBrandColors.SoftSilver,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                SakinaGoldSwitch(
                    checked = suhoorEnabled,
                    onCheckedChange = {
                        suhoorEnabled = it
                        sp.edit().putBoolean("suhoor_alarm_on", it).apply()
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(20, 30, 40, 60).forEach { mins ->
                    val sel = suhoorMinutesBeforeFajr == mins
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (sel) theme.accentGold else Color(0x22FFFFFF))
                            .clickable {
                                suhoorMinutesBeforeFajr = mins
                                sp.edit().putInt("suhoor_mins", mins).apply()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$mins ${s.minuteShort}".toWesternDigits(),
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 2. Volume & Fajr Fade
        SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${s.volumeLabel}: ${(adhanVolume * 100).roundToInt()}%".toWesternDigits(),
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = theme.accentBright,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Slider(
                value = adhanVolume,
                onValueChange = {
                    adhanVolume = it
                    sp.edit().putFloat("adhan_vol", it).apply()
                },
                colors = SliderDefaults.colors(
                    thumbColor = theme.accentBright,
                    activeTrackColor = theme.accentGold,
                    inactiveTrackColor = Color(0x33FFFFFF)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = s.fajrFadeLabel,
                    fontFamily = SakinaBodyFont,
                    fontSize = 12.5.sp,
                    color = SakinaBrandColors.LuminousIvory,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SakinaGoldSwitch(
                    checked = fajrGentleFade,
                    onCheckedChange = {
                        fajrGentleFade = it
                        Prefs.setFajrFade(ctx, it)
                    }
                )
            }
        }

        // 3. Default Sound Mode
        SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = s.defaultSoundLabel,
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = theme.accentBright,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                s.soundModes.forEachIndexed { idx, label ->
                    val sel = defaultAdhanMode == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (sel) theme.accentGold else Color(0x22FFFFFF))
                            .clickable {
                                defaultAdhanMode = idx
                                sp.edit().putInt("default_adhan_mode", idx).apply()
                                listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء").forEach { p ->
                                    Modes.set(ctx, p, idx)
                                }
                                AlarmScheduler.scheduleNext(ctx)
                            }
                            .padding(horizontal = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = SakinaBodyFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 4. Test Alarm Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold)))
                .border(1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(21.dp))
                .clickable {
                    AlarmScheduler.scheduleTest(ctx)
                    Toast.makeText(ctx, s.testAlarmScheduled.toWesternDigits(), Toast.LENGTH_SHORT).show()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = s.testAlarmBtn.toWesternDigits(),
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = theme.bgDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
