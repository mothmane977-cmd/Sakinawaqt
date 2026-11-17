package app.sakina

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class CityPreset(val name: String, val lat: Double, val lon: Double)

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

    var refreshTick by remember { mutableIntStateOf(0) }
    var isHanafi by remember { mutableStateOf(Prefs.hanafi(ctx)) }
    var hijriOff by remember { mutableIntStateOf(Prefs.hijriOffset(ctx)) }
    var showCityPicker by remember { mutableStateOf(false) }
    val placeName = remember(refreshTick) { Place.name(ctx) }
    val methodShort = remember(refreshTick) { Prefs.METHODS[Prefs.method(ctx)] }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 74.dp, bottom = 86.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SakinaBackIconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart))
            SakinaTitle3D(text = s.settingsTitle, fontSize = 22.sp, modifier = Modifier.align(Alignment.Center))
        }

        // 5 Luxury Themes Selector
        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), glowGold = true) {
            Text(s.themeTitle.toWesternDigits(), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = theme.accentBright)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SakinaLuxuryTheme.entries.forEach { t ->
                    val sel = t == selectedTheme
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) t.accentGold.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                            .border(0.8.dp, if (sel) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)), RoundedCornerShape(12.dp))
                            .clickable { onThemeChange(t) }
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(t.bgDeep).border(1.dp, t.accentGold, RoundedCornerShape(4.dp)))
                            Spacer(Modifier.width(8.dp))
                            Text(t.titleAr, fontFamily = SakinaBodyFont, fontSize = 13.sp, color = if (sel) t.accentBright else SakinaBrandColors.LuminousIvory)
                        }
                        if (sel) Text("✓", fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = t.accentBright)
                    }
                }
            }
        }

        // 6 Languages Selector
        SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(s.languageLabel, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (r in 0 until 2) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (c in 0 until 3) {
                            val lang = SakinaLang.entries[r * 3 + c]
                            val sel = lang == selectedLang
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (sel) theme.accentGold else Color(0x22FFFFFF))
                                    .clickable { onLangChange(lang) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${lang.flag} ${lang.nativeName}", fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Location: Auto GPS or Manual City
        SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_location), contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("${s.locationLabel}: $placeName", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = theme.accentBright)
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.accentGold.copy(alpha = 0.22f))
                        .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp))
                        .clickable {
                            val loc = Place.fromDevice(ctx)
                            if (loc != null) Place.set(ctx, loc.latitude, loc.longitude, "GPS")
                            else Place.reset(ctx)
                            AlarmScheduler.scheduleNext(ctx); refreshTick++
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.autoGps, fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.accentBright)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp))
                        .clickable { showCityPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.manualPick, fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SakinaBrandColors.LuminousIvory)
                }
            }
        }

        // Asr & Hijri
        SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("المذهب الحنفي للعصر", fontFamily = SakinaBodyFont, fontSize = 13.5.sp, color = SakinaBrandColors.LuminousIvory)
                SakinaGoldSwitch(checked = isHanafi, onCheckedChange = {
                    isHanafi = it; Prefs.setHanafi(ctx, it); AlarmScheduler.scheduleNext(ctx)
                })
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("تعديل الهجري: ${if (hijriOff > 0) "+$hijriOff" else "$hijriOff"}".toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 13.sp, color = theme.accentBright)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(-1, 0, 1).forEach { step ->
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (hijriOff == step) theme.accentGold else Color(0x22FFFFFF)).clickable { hijriOff = step; Prefs.setHijriOffset(ctx, step) }.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("${if (step > 0) "+$step" else "$step"}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontSize = 12.sp, color = if (hijriOff == step) theme.bgDeep else SakinaBrandColors.LuminousIvory)
                        }
                    }
                }
            }
        }

        SakinaSettingsPill(R.drawable.ic_star_ornament, s.calcMethod, methodShort) {
            Prefs.cycleMethod(ctx); AlarmScheduler.scheduleNext(ctx); refreshTick++
        }
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
                                    Place.set(ctx, city.lat, city.lon, city.name)
                                    AlarmScheduler.scheduleNext(ctx)
                                    refreshTick++
                                    showCityPicker = false
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(city.name, fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory)
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
    val pillShape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(pillShape)
            .background(Brush.verticalGradient(listOf(theme.bgCard, theme.bgDeep)))
            .border(0.8.dp, Sakina3DGoldBevelBrush, pillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SakinaBrandColors.LuminousIvory)
        }
        Text(valueText.toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 12.sp, color = theme.accentBright, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
