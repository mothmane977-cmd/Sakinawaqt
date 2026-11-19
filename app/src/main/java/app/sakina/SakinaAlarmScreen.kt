package app.sakina

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
    var showVoiceDialog by remember { mutableStateOf(false) }

    // #5: Single non-scrolling page positioned cleanly below the Mihrab arch (top = 56.dp, bottom = 74.dp)
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 74.dp),
        verticalArrangement = Arrangement.SpaceBetween
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
                    style = TextStyle(
                        fontFamily = SakinaEditorialFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        brush = SakinaCalligraphyBrush()
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 1. Compact Adhan Voice Selector Card with Preview + Change Dialog
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowGold = true,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = s.adhanVoiceLabel,
                        fontFamily = SakinaHeadingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = theme.accentBright
                    )
                    Text(
                        text = selectedVoice.localizedTitle(lang),
                        fontFamily = SakinaBodyFont,
                        fontSize = 12.sp,
                        color = SakinaBrandColors.LuminousIvory,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    val isPlaying = playingVoiceKey == selectedVoice.key
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) theme.accentGold else Color(0x28FFFFFF))
                            .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                            .clickable {
                                if (isPlaying) {
                                    SakinaAdhanAudio.stop()
                                    playingVoiceKey = null
                                } else {
                                    playingVoiceKey = selectedVoice.key
                                    SakinaAdhanAudio.startPreview(ctx, selectedVoice) { playingVoiceKey = null }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = if (isPlaying) theme.bgDeep else theme.accentBright,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold)))
                            .clickable { showVoiceDialog = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (lang == SakinaLang.AR || lang == SakinaLang.UR) "تغيير" else "Change",
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = theme.bgDeep
                        )
                    }
                }
            }
        }

        // 2. Suhoor & Qiyam Alarm
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowGold = suhoorEnabled,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
        ) {
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
                        fontSize = 13.5.sp,
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
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(20, 30, 40, 60).forEach { mins ->
                    val sel = suhoorMinutesBeforeFajr == mins
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(27.dp)
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
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Text(
                text = "${s.volumeLabel}: ${(adhanVolume * 100).roundToInt()}%".toWesternDigits(),
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
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
                ),
                modifier = Modifier.height(32.dp)
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
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Text(
                text = s.defaultSoundLabel,
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
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
                            .height(29.dp)
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
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold)))
                .border(1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(20.dp))
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

    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = {
                SakinaAdhanAudio.stop()
                playingVoiceKey = null
                showVoiceDialog = false
            },
            containerColor = theme.bgCard,
            title = { Text(s.adhanVoiceLabel, fontFamily = SakinaHeadingFont, color = theme.accentBright) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdhanVoice.entries.forEach { voice ->
                        val isSel = voice == selectedVoice
                        val isPlaying = playingVoiceKey == voice.key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) theme.accentGold.copy(alpha = 0.25f) else Color(0x22FFFFFF))
                                .border(0.8.dp, if (isSel) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)), RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedVoice = voice
                                    SakinaAdhanAudio.setSelectedVoice(ctx, voice)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (if (isSel) "✓ " else "• ") + voice.localizedTitle(lang),
                                fontFamily = SakinaBodyFont,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.5.sp,
                                color = if (isSel) theme.accentBright else SakinaBrandColors.LuminousIvory,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) theme.accentGold else Color(0x28FFFFFF))
                                    .clickable {
                                        if (isPlaying) {
                                            SakinaAdhanAudio.stop()
                                            playingVoiceKey = null
                                        } else {
                                            selectedVoice = voice
                                            SakinaAdhanAudio.setSelectedVoice(ctx, voice)
                                            playingVoiceKey = voice.key
                                            SakinaAdhanAudio.startPreview(ctx, voice) { playingVoiceKey = null }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isPlaying) theme.bgDeep else theme.accentBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    SakinaAdhanAudio.stop()
                    playingVoiceKey = null
                    showVoiceDialog = false
                }) { Text("×", color = theme.accentBright, fontSize = 18.sp) }
            }
        )
    }
}
