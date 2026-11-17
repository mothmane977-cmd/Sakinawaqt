package app.sakina

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.PowerManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private val NAMES = PRAYER_NAMES
private val HIJRI = android.icu.text.DateFormat.getDateInstance(
    android.icu.text.DateFormat.LONG, Locale.forLanguageTag("ar-u-ca-islamic-umalqura-nu-latn")
)

private fun events(ctx: Context, offset: Int) = PrayerEngine.events(ctx, offset)

@Composable
private fun HomeTab(
    modifier: Modifier, ctx: Context, sky: Sky, now: Date, nxt: Pair<Int, Date>, left: Long, hijri: String
) {
    val ink = sky.ink
    val hm = remember { SimpleDateFormat("HH:mm", Locale.US) }
    
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 6.dp)) {
        
        // 1. صورة الخلفية (Mihrab) + الساعة
        Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp))) {
            Image(
                painter = painterResource(id = R.drawable.bg_sky),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, sky.top))))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                TabularText(hm.format(now), 56.sp, ink, InterFont, FontWeight.Bold)
                Text("${Place.name(ctx)}  ·  $hijri", color = sky.accent, fontSize = 14.sp, fontFamily = NotoKufi)
            }
        }

        // 2. بطاقة الصلاة القادمة
        GlassCard(sky, Modifier.weight(0.4f).padding(vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("الصلاة القادمة", color = sky.ink2, fontSize = 13.sp, fontFamily = NotoKufi)
                Text(NAMES[nxt.first], color = sky.accent, fontSize = 16.sp, fontFamily = NotoKufi, fontWeight = FontWeight.Bold)
            }
            TabularText(
                String.format(Locale.US, "%02d:%02d:%02d", left / 3600, (left % 3600) / 60, left % 60),
                36.sp, sky.accent, InterFont, FontWeight.Bold
            )
        }

        // 3. قائمة المواقيت (بخلفية عاجية)
        val ivoryCard = RoundedCornerShape(20.dp)
        Column(
            Modifier.weight(2.5f).fillMaxWidth().padding(bottom = 8.dp).clip(ivoryCard).background(Color(0xFFF7F2E8)).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            events(ctx, 0).forEach { (i, t) ->
                val isNext = i == nxt.first
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (isNext) sky.accent.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconRes = when(i) {
                        0 -> R.drawable.ic_fajr
                        2 -> R.drawable.ic_dhuhr
                        3 -> R.drawable.ic_asr
                        4 -> R.drawable.ic_maghrib
                        5 -> R.drawable.ic_isha
                        else -> R.drawable.ic_fajr
                    }
                    Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(NAMES[i], color = Color(0xFF0B1630), fontSize = 16.sp, fontFamily = NotoKufi, fontWeight = if(isNext) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                    TabularText(hm.format(t), 16.sp, Color(0xFF0B1630), InterFont, FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(ctx: Context) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        var now by remember { mutableStateOf(Date()) }
        LaunchedEffect(Unit) { while (true) { delay(1000); now = Date() } }
        var tab by remember { mutableStateOf(0) }
        var amoledOn by remember { mutableStateOf(Prefs.amoled(ctx)) }
        
        val all = events(ctx, -1) + events(ctx, 0) + events(ctx, 1)
        val idx = all.indexOfLast { !it.second.after(now) }.coerceAtLeast(0)
        val cur = all[idx]
        val nxt = all[idx + 1]
        val sky = if (cur.first == 5 && amoledOn) Brand.AMOLED else Brand.SKIES[cur.first]
        val left = ((nxt.second.time - now.time) / 1000).coerceAtLeast(0)
        val hijri = remember(now.time / 3600000) { HIJRI.format(Date(now.time)) }

        SideEffect {
            val w = (ctx as? Activity)?.window
            if (w != null) {
                val ic = WindowCompat.getInsetsController(w, w.decorView)
                ic.isAppearanceLightStatusBars = false
                ic.isAppearanceLightNavigationBars = false
            }
        }

        SakinaTheme(sky) {
            Box(Modifier.fillMaxSize()) {
                SkyBackdrop(sky, Modifier.fillMaxSize())
                Column(Modifier.fillMaxSize().systemBarsPadding()) {
                    
                    // شريط التنقل السفلي الجديد
                    if (tab == 0) HomeTab(Modifier.weight(1f), ctx, sky, now, nxt, left, hijri)
                    else if (tab == 1) QiblaScreen(ctx, sky)
                    else if (tab == 2) TasbihScreen(sky)
                    
                    BrandNav(sky, listOf("الرئيسية", "المواقيت", "القبلة", "القرآن", "الأذكار", "التقويم"), tab) { if (tab != it) { tab = it } }
                }
            }
        }
    }
}
