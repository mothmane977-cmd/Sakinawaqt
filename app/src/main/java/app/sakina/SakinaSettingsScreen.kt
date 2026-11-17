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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SakinaSettingsScreen(
    ctx: Context,
    selectedTheme: SakinaLuxuryTheme,
    onThemeChange: (SakinaLuxuryTheme) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }
    val theme = LocalSakinaTheme.current
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    var refreshTick by remember { mutableIntStateOf(0) }
    var langCode by remember { mutableStateOf(sp.getString("app_lang", "ar") ?: "ar") }
    var isHanafi by remember { mutableStateOf(Prefs.hanafi(ctx)) }
    var hijriOff by remember { mutableIntStateOf(Prefs.hijriOffset(ctx)) }
    val placeName = remember(refreshTick) { Place.name(ctx) }
    val methodShort = remember(refreshTick) { Prefs.METHODS[Prefs.method(ctx)] }

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
                Text("الإعدادات العامة", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = theme.accentBright)
            }
            Spacer(Modifier.width(44.dp))
        }

        // 5 Luxury Themes Selector
        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), glowGold = true) {
            Text("اختر الثيم الفاخر (5 ثيمات)", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = theme.accentBright)
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
                        if (sel) Text("مُفعّل", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = t.accentBright)
                    }
                }
            }
        }

        // Language Selector
        SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("لغة التطبيق", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ar" to "العربية", "fr" to "Français", "en" to "English").forEach { (code, label) ->
                    val sel = code == langCode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) theme.accentGold else Color(0x22FFFFFF))
                            .clickable { langCode = code; sp.edit().putString("app_lang", code).apply() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (sel) theme.bgDeep else SakinaBrandColors.LuminousIvory)
                    }
                }
            }
        }

        // Asr Madhab & Hijri Offset
        SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("المذهب الحنفي للعصر", fontFamily = SakinaBodyFont, fontSize = 13.5.sp, color = SakinaBrandColors.LuminousIvory)
                SakinaGoldSwitch(checked = isHanafi, onCheckedChange = {
                    isHanafi = it; Prefs.setHanafi(ctx, it); AlarmScheduler.scheduleNext(ctx)
                })
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("تعديل التاريخ الهجري: ${if (hijriOff > 0) "+$hijriOff" else "$hijriOff"} يوم".toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 13.sp, color = theme.accentBright)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(-1, 0, 1).forEach { step ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hijriOff == step) theme.accentGold else Color(0x22FFFFFF))
                                .clickable { hijriOff = step; Prefs.setHijriOffset(ctx, step) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("${if (step > 0) "+$step" else "$step"}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontSize = 12.sp, color = if (hijriOff == step) theme.bgDeep else SakinaBrandColors.LuminousIvory)
                        }
                    }
                }
            }
        }

        SakinaSettingsPill(R.drawable.ic_mosque, "الموقع", placeName) {
            val loc = Place.fromDevice(ctx)
            if (loc != null) Place.set(ctx, loc.latitude, loc.longitude, "موقعي الحالي") else Place.reset(ctx)
            AlarmScheduler.scheduleNext(ctx); refreshTick++
        }
        SakinaSettingsPill(R.drawable.ic_star_ornament, "طريقة الحساب", methodShort) {
            Prefs.cycleMethod(ctx); AlarmScheduler.scheduleNext(ctx); refreshTick++
        }
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
