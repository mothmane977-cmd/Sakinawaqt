package app.sakina

import android.content.Context
import android.location.Geocoder
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class CityPreset(val nameAr: String, val lat: Double, val lon: Double)

private val PRESET_CITIES = listOf(
    CityPreset("فرندة", 35.06, 1.04),
    CityPreset("الجزائر العاصمة", 36.75, 3.05),
    CityPreset("وهران", 35.70, -0.63),
    CityPreset("قسنطينة", 36.36, 6.61),
    CityPreset("تيارت", 35.37, 1.32),
    CityPreset("مكة المكرمة", 21.42, 39.82)
)

@Composable
fun SakinaSettingsScreen(
    ctx: Context,
    selectedTheme: SakinaLuxuryTheme,
    selectedLang: SakinaLang,
    onThemeChange: (SakinaLuxuryTheme) -> Unit,
    onLangChange: (SakinaLang) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current

    var refreshTick by remember { mutableIntStateOf(0) }
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }
    var autoLocation by remember { mutableStateOf(sp.getBoolean("auto_gps_location", true)) }
    var hijriOff by remember { mutableIntStateOf(Prefs.hijriOffset(ctx)) }
    var showCityPicker by remember { mutableStateOf(false) }
    var showVoicePicker by remember { mutableStateOf(false) }
    var selectedVoice by remember { mutableStateOf(SakinaAdhanAudio.selectedVoice(ctx)) }
    var playingVoiceKey by remember { mutableStateOf<String?>(null) }
    val placeName = remember(refreshTick, lang) { localizeCityName(Place.name(ctx), lang) }
    val methodShort = remember(refreshTick, lang) { localizeMethodName(Prefs.METHODS[Prefs.method(ctx)], lang) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 54.dp, bottom = 74.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SakinaBackIconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart))
            SakinaOrnamentCartouche(
                modifier = Modifier.align(Alignment.Center),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 7.dp)
            ) {
                Text(
                    text = s.settingsTitle,
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

        // 1. 5 Luxury Themes Selector
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowGold = true,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = s.themeTitle.toWesternDigits(),
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = theme.accentBright
            )
            Spacer(Modifier.height(7.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SakinaLuxuryTheme.entries.forEachIndexed { idx, t ->
                    val sel = t == selectedTheme
                    val localizedThemeName = s.themeNames.getOrElse(idx) { t.titleAr }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(33.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) t.accentGold.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                            .border(
                                0.8.dp,
                                if (sel) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onThemeChange(t) }
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(t.bgDeep)
                                    .border(1.dp, t.accentGold, RoundedCornerShape(4.dp))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = localizedThemeName,
                                fontFamily = SakinaBodyFont,
                                fontSize = 12.5.sp,
                                color = if (sel) t.accentBright else SakinaBrandColors.LuminousIvory,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (sel) {
                            Text(
                                text = "✓ ${s.activeWord}",
                                fontFamily = SakinaBodyFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = t.accentBright
                            )
                        }
                    }
                }
            }
        }

        // 2. Location: Auto GPS on change + Manual City (#7)
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Image(painter = painterResource(id = R.drawable.ic_location), contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "${s.locationLabel}: $placeName",
                        fontFamily = SakinaHeadingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = theme.accentBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                SakinaGoldSwitch(
                    checked = autoLocation,
                    onCheckedChange = {
                        autoLocation = it
                        sp.edit().putBoolean("auto_gps_location", it).apply()
                        if (it) {
                            val loc = Place.fromDevice(ctx)
                            if (loc != null) {
                                var resolved = "GPS"
                                try {
                                    @Suppress("DEPRECATION")
                                    val list = Geocoder(ctx, lang.locale).getFromLocation(loc.latitude, loc.longitude, 1)
                                    val city = list?.firstOrNull()?.locality ?: list?.firstOrNull()?.subAdminArea
                                    if (!city.isNullOrBlank()) resolved = city
                                } catch (_: Exception) {}
                                Place.set(ctx, loc.latitude, loc.longitude, resolved)
                                AlarmScheduler.scheduleNext(ctx)
                                SakinaLiveNotifier.update(ctx)
                                refreshTick++
                            }
                        }
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.accentGold.copy(alpha = 0.22f))
                        .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(10.dp))
                        .clickable {
                            val loc = Place.fromDevice(ctx)
                            if (loc != null) Place.set(ctx, loc.latitude, loc.longitude, "GPS")
                            else Place.reset(ctx)
                            AlarmScheduler.scheduleNext(ctx); refreshTick++
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.autoGps, fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.accentBright, maxLines = 1)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x22FFFFFF))
                        .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(10.dp))
                        .clickable { showCityPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.manualPick, fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SakinaBrandColors.LuminousIvory, maxLines = 1)
                }
            }
        }

        // 3. Hijri Date Adjustment ONLY (#17: Madhhab removed, label corrected to "تعديل التاريخ الهجري")
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${s.hijriAdjLabel}: ${if (hijriOff > 0) "+$hijriOff" else "$hijriOff"}".toWesternDigits(),
                    fontFamily = SakinaBodyFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.8.sp,
                    color = theme.accentBright,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(-1, 0, 1).forEach { step ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hijriOff == step) theme.accentGold else Color(0x22FFFFFF))
                                .clickable { hijriOff = step; Prefs.setHijriOffset(ctx, step) }
                                .padding(horizontal = 11.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${if (step > 0) "+$step" else "$step"}".toWesternDigits(),
                                fontFamily = SakinaNumeralFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (hijriOff == step) theme.bgDeep else SakinaBrandColors.LuminousIvory
                            )
                        }
                    }
                }
            }
        }

        SakinaSettingsPill(R.drawable.ic_alarm, s.adhanVoiceLabel, selectedVoice.localizedTitle(lang)) {
            showVoicePicker = true
        }

        SakinaSettingsPill(R.drawable.ic_star_ornament, s.calcMethod, methodShort) {
            Prefs.cycleMethod(ctx); AlarmScheduler.scheduleNext(ctx); refreshTick++
        }
    }

    if (showVoicePicker) {
        AlertDialog(
            onDismissRequest = {
                SakinaAdhanAudio.stop()
                playingVoiceKey = null
                showVoicePicker = false
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
                    showVoicePicker = false
                }) { Text("×", color = theme.accentBright, fontSize = 18.sp) }
            }
        )
    }

    if (showCityPicker) {
        AlertDialog(
            onDismissRequest = { showCityPicker = false },
            containerColor = theme.bgCard,
            title = { Text(s.manualPick, fontFamily = SakinaHeadingFont, color = theme.accentBright) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PRESET_CITIES.forEach { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x22FFFFFF))
                                .clickable {
                                    Place.set(ctx, city.lat, city.lon, city.nameAr)
                                    AlarmScheduler.scheduleNext(ctx)
                                    refreshTick++
                                    showCityPicker = false
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(localizeCityName(city.nameAr, lang), fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory)
                            Text("${city.lat}, ${city.lon}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontSize = 11.sp, color = theme.accentBright)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityPicker = false }) { Text("×", color = theme.accentBright, fontSize = 18.sp) }
            }
        )
    }
}

@Composable
private fun SakinaSettingsPill(@DrawableRes iconRes: Int, title: String, valueText: String, onClick: () -> Unit) {
    val theme = LocalSakinaTheme.current
    val pillShape = RoundedCornerShape(21.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(pillShape)
            .background(Brush.verticalGradient(listOf(theme.bgCard, theme.bgDeep)))
            .border(0.9.dp, Sakina3DGoldBevelBrush, pillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 6.dp)) {
            Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SakinaBrandColors.LuminousIvory, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(valueText.toWesternDigits(), fontFamily = SakinaBodyFont, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp, color = theme.accentBright, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
