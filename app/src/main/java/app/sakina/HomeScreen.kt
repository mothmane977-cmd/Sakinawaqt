package app.sakina

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
private fun ExpandableSection(title: String, sky: Sky, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(sky.glass).border(0.75.dp, sky.hairline, RoundedCornerShape(16.dp))) {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = sky.ink, fontSize = 16.sp, fontFamily = NotoNaskh, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, contentDescription = null, tint = sky.accent)
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
        }
    }
}

@Composable
private fun HomeTab(
    modifier: Modifier, ctx: Context, sky: Sky, now: Date, nxt: Pair<Int, Date>, left: Long, hijri: String
) {
    val ink = sky.ink
    val hm = remember { SimpleDateFormat("HH:mm", Locale.US) }
    
    // استخدام Column مع padding لتقسيم الشاشة بالتساوي
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 6.dp)) {
        
        // 1. الخلفية والساعة (الجزء العلوي)
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
                Text("${Place.name(ctx)}  ·  $hijri", color = sky.accent, fontSize = 14.sp, fontFamily = NotoNaskh)
            }
        }

        // 2. بطاقة الصلاة القادمة (الجزء الأوسط)
        GlassCard(sky, Modifier.weight(0.4f).padding(vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("الصلاة القادمة", color = sky.ink2, fontSize = 13.sp, fontFamily = NotoNaskh)
                Text(NAMES[nxt.first], color = sky.accent, fontSize = 16.sp, fontFamily = NotoNaskh, fontWeight = FontWeight.Bold)
            }
            TabularText(
                String.format(Locale.US, "%02d:%02d:%02d", left / 3600, (left % 3600) / 60, left % 60),
                36.sp, sky.accent, InterFont, FontWeight.Bold
            )
        }

        // 3. قائمة المواقيت (الجزء السفلي)
        val ivoryCard = RoundedCornerShape(20.dp)
        Column(
            Modifier.weight(2.5f).fillMaxWidth().padding(bottom = 8.dp).clip(ivoryCard).background(Color(0xFFFAF8F5)).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly // توزيع متساوٍ
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
                    Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    // النص هنا أصبح بخط النسخ
                    Text(NAMES[i], color = Color(0xFF1C1613), fontSize = 16.sp, fontFamily = NotoNaskh, fontWeight = if(isNext) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                    TabularText(hm.format(t), 16.sp, Color(0xFF1C1613), InterFont, FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(ctx: Context) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        var now by remember { mutableStateOf(Date()) }
        LaunchedEffect(Unit) { while (true) { delay(1000); now = Date() } }
        var tab by remember { mutableStateOf(0) }
        var cfgVer by remember { mutableStateOf(0) }
        var hOff by remember { mutableStateOf(Prefs.hijriOffset(ctx)) }
        var amoledOn by remember { mutableStateOf(Prefs.amoled(ctx)) }
        var fajrFadeOn by remember { mutableStateOf(Prefs.fajrFade(ctx)) }
        var log by remember { mutableStateOf(DiagLog.get(ctx)) }
        
        val modes = remember {
            mutableStateMapOf<String, Int>().apply { NAMES.forEach { put(it, Modes.get(ctx, it)) } }
        }
        
        val all = events(ctx, -1) + events(ctx, 0) + events(ctx, 1)
        val idx = all.indexOfLast { !it.second.after(now) }.coerceAtLeast(0)
        val cur = all[idx]
        val nxt = all[idx + 1]
        val sky = if (cur.first == 5 && amoledOn) Brand.AMOLED else Brand.SKIES[cur.first]
        val ink = sky.ink
        val left = ((nxt.second.time - now.time) / 1000).coerceAtLeast(0)
        val hijri = remember(now.time / 3600000, hOff) { HIJRI.format(Date(now.time + hOff * 86_400_000L)) }

        SideEffect {
            val w = (ctx as? Activity)?.window
            if (w != null) {
                val ic = WindowCompat.getInsetsController(w, w.decorView)
                ic.isAppearanceLightStatusBars = false
                ic.isAppearanceLightNavigationBars = false
            }
        }

        val cycleMode: (String) -> Unit = { n ->
            val m = ((modes[n] ?: 0) + 1) % 4
            modes[n] = m
            Modes.set(ctx, n, m)
            Toast.makeText(ctx, "$n: ${Modes.labels[m]}", Toast.LENGTH_SHORT).show()
        }

        SakinaTheme(sky) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(drawerContainerColor = Color(0xFF0A0E12), drawerContentColor = sky.ink) {
                        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("سكينة", color = sky.accent, fontSize = 28.sp, fontFamily = NotoNaskh, fontWeight = FontWeight.Bold)
                            
                            // الأزرار هنا أصبحت متوسطة وأنيقة
                            GoldButton("المسبحة الإلكترونية") {
                                scope.launch { drawerState.close() }
                                tab = 2
                            }
                            
                            ExpandableSection("الموقع", sky) {
                                Text("${Place.name(ctx)}", color = sky.ink2, fontFamily = NotoNaskh)
                                GoldButton("تحديث الموقع") { /* locate() */ }
                            }
                            
                            ExpandableSection("الإشعارات", sky) {
                                Text("نوع التنبيه لكل صلاة", color = sky.ink2, fontFamily = NotoNaskh)
                            }
                            
                            ExpandableSection("الحساب والتاريخ", sky) {
                                Text("الطريقة: ${Prefs.methodLabel(ctx)}", color = sky.ink2, fontFamily = NotoNaskh)
                                GlassButton(sky, "تغيير طريقة الحساب") { Prefs.cycleMethod(ctx); AlarmScheduler.scheduleNext(ctx); cfgVer++ }
                            }
                            
                            ExpandableSection("صحة المنبه", sky) {
                                Text("فحص الموثوقية", color = sky.ink2, fontFamily = NotoNaskh)
                            }
                            
                            GlassButton(sky, "سجل التشخيص") { }
                        }
                    }
                }
            ) {
                Box(Modifier.fillMaxSize()) {
                    SkyBackdrop(sky, Modifier.fillMaxSize())
                    Column(Modifier.fillMaxSize().systemBarsPadding()) {
                        
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Rounded.Menu, contentDescription = "القائمة", tint = sky.ink)
                            }
                        }

                        AnimatedContent(
                            targetState = tab,
                            transitionSpec = { (fadeIn(tween(400)) + slideInHorizontally(initialOffsetX = { 50 })).togetherWith(fadeOut(tween(400))) },
                            label = "TabTransition", modifier = Modifier.weight(1f)
                        ) { currentTab ->
                            // هنا نستخدم وزن 1 لكي تملأ الشاشة
                            if (currentTab == 0) HomeTab(Modifier.fillMaxSize(), ctx, sky, now, nxt, left, hijri)
                            else if (currentTab == 1) QiblaScreen(ctx, sky)
                            else if (currentTab == 2) TasbihScreen(sky)
                        }
                        
                        BrandNav(sky, listOf("الرئيسية", "القبلة"), tab) { if (tab != it) { tab = it; log = DiagLog.get(ctx) } }
                    }
                }
            }
        }
    }
}
