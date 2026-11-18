package app.sakina

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SakinaAlarmScreen(ctx: Context, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler {
        SakinaAdhanAudio.stop()
        onBackClick()
    }
    DisposableEffect(Unit) {
        onDispose { SakinaAdhanAudio.stop() }
    }

    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    var suhoorEnabled by remember { mutableStateOf(sp.getBoolean("suhoor_alarm_on", true)) }
    var suhoorMinutesBeforeFajr by remember { mutableIntStateOf(sp.getInt("suhoor_mins", 40)) }
    var adhanVolume by remember { mutableFloatStateOf(sp.getFloat("adhan_vol", 0.85f)) }
    var fajrGentleFade by remember { mutableStateOf(Prefs.fajrFade(ctx)) }
    var defaultAdhanMode by remember { mutableIntStateOf(sp.getInt("default_adhan_mode", 0)) }
    var selectedVoice by remember { mutableStateOf(SakinaAdhanAudio.selectedVoice(ctx)) }
    var playingVoiceKey by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 50.dp, bottom = 78.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SakinaBackIconButton(
                onClick = {
                    SakinaAdhanAudio.stop()
                    onBackClick()
                },
                modifier = Modifier.align(Alignment.CenterStart)
            )
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

        // 1. Adhan Voice Selector with Live Play/Stop Preview (5 uploaded res/raw voices)
        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), glowGold = true) {
            Text(
                text = s.adhanVoiceLabel,
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = theme.accentBright
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                AdhanVoice.entries.forEach { voice ->
                    val isSel = voice == selectedVoice
                    val isPlaying = playingVoiceKey == voice.key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) theme.accentGold.copy(alpha = 0.24f) else Color(0x18FFFFFF))
                            .border(
                                0.8.dp,
                                if (isSel) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedVoice = voice
                                SakinaAdhanAudio.setSelectedVoice(ctx, voice)
                            }
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSel) "✓ " else "• ",
                                fontFamily = SakinaNumeralFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSel) theme.accentBright else SakinaBrandColors.SoftSilver
                            )
                            Text(
                                text = voice.localizedTitle(lang),
                                fontFamily = SakinaBodyFont,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (isSel) theme.accentBright else SakinaBrandColors.LuminousIvory,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) theme.accentGold else Color(0x28FFFFFF))
                                .border(0.7.dp, Sakina3DGoldBevelBrush, CircleShape)
                                .clickable {
                                    if (isPlaying) {
                                        SakinaAdhanAudio.stop()
                                        playingVoiceKey = null
                                    } else {
                                        selectedVoice = voice
                                        SakinaAdhanAudio.setSelectedVoice(ctx, voice)
                                        playingVoiceKey = voice.key
                                        SakinaAdhanAudio.startPreview(ctx, voice) {
                                            playingVoiceKey = null
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = if (isPlaying) theme.bgDeep else theme.accentBright,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Suhoor & Qiyam Alarm
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
                        fontSize = 14.sp,
                        color = theme.accentBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$suhoorMinutesBeforeFajr ${s.minsBeforeFajr}".toWesternDigits(),
                        fontFamily = SakinaBodyFont,
                        fontSize = 11.sp,
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
            Spacer(modifier = Modifier.height(7.dp))
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

        // 3. Volume & Fajr Fade
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
                    fontSize = 12.sp,
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

        // 4. Default Sound Mode
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

        // 5. Test Alarm Button
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
