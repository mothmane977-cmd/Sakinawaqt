package app.sakina

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
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
import java.text.SimpleDateFormat
import java.util.*

private val NAMES = PRAYER_NAMES

private val HIJRI = android.icu.text.DateFormat.getDateInstance(
    android.icu.text.DateFormat.LONG, Locale.forLanguageTag("ar-u-ca-islamic-umalqura-nu-latn")
)

private fun events(ctx: Context, offset: Int) = PrayerEngine.events(ctx, offset)

@Composable
private fun HealthCard(ctx: Context, sky: Sky) {
    val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
    val saver = pm.isPowerSaveMode
    val ignoring = pm.isIgnoringBatteryOptimizations(ctx.packageName)
    val notif = NotificationManagerCompat.from(ctx).areNotificationsEnabled()
    GlassCard(sky) {
        Text("فحص الموثوقية", color = sky.ink, fontWeight = FontWeight.Bold)
        Text(if (notif) "الإشعارات: مفعّلة ✓" else "الإشعارات: معطّلة ⚠", color = sky.ink)
        Text(if (saver) "توفير البطارية: مفعّل ⚠ قد يمنع التنبيه" else "توفير البطارية: مطفأ ✓", color = sky.ink)
        Text(
            if (ignoring) "استثناء تحسين البطارية: ممنوح ✓" else "استثناء تحسين البطارية: غير ممنوح ⚠",
            color = sky.ink
        )
        GlassButton(sky, "إعدادات الإشعارات") {
            ctx.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
            )
        }
    }
}

@Composable
private fun HomeTab(
    modifier: Modifier,
    ctx: Context,
    sky: Sky,
    now: Date,
    nxt: Pair<Int, Date>,
    frac: Float,
    left: Long,
    hijri: String,
    saver: Boolean,
    modes: Map<String, Int>,
    onCycleMode: (String) -> Unit
) {
    val ink = sky.ink
    val hm = remember { SimpleDateFormat("HH:mm", Locale.US) }
    Column(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(
            Modifier.weight(0.95f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TabularText(hm.format(now), 60.sp, ink, NotoSans)
            val pill = RoundedCornerShape(50)
            Text(
                "${Place.name(ctx)}  ·  $hijri",
                color = sky.ink2, fontSize = 14.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp).clip(pill).background(sky.glass)
                    .border(0.75.dp, sky.hairline, pill)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
            if (saver) {
                Text(
                    "توفير البطارية مفعّل: قد لا يصل التنبيه في وقته",
                    color = ink, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Box(Modifier.weight(1.6f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
                val strokePx = 2.5.dp.toPx()
                val arcSize = Size(size.width, size.width * 0.92f)
                val topLeft = Offset(0f, 16.dp.toPx())
                val startAngle = 195f
                val sweepTotal = 150f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(sky.halo, Color.Transparent),
                        center = Offset(size.width / 2f, topLeft.y + arcSize.height * 0.45f),
                        radius = size.width * 0.52f
                    ),
                    radius = size.width * 0.52f,
                    center = Offset(size.width / 2f, topLeft.y + arcSize.height * 0.45f)
                )
                drawArc(
                    color = sky.accent.copy(alpha = 0.22f), startAngle = startAngle, sweepAngle = sweepTotal,
                    useCenter = false, topLeft = topLeft, size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                val activeSweep = sweepTotal * frac.coerceIn(0f, 1f)
                drawArc(
                    brush = Brush.horizontalGradient(listOf(sky.accent.copy(alpha = 0.45f), sky.accent)),
                    startAngle = startAngle, sweepAngle = activeSweep, useCenter = false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(width = strokePx * 1.4f, cap = StrokeCap.Round)
                )
                val rad = Math.toRadians((startAngle + activeSweep).toDouble())
                val cx = topLeft.x + arcSize.width / 2f + (arcSize.width / 2f) * Math.cos(rad).toFloat()
                val cy = topLeft.y + arcSize.height / 2f + (arcSize.height / 2f) * Math.sin(rad).toFloat()
                drawCircle(sky.accent.copy(alpha = 0.18f), 12.dp.toPx(), Offset(cx, cy))
                drawCircle(sky.accent.copy(alpha = 0.45f), 6.dp.toPx(), Offset(cx, cy))
                drawCircle(sky.accent, 3.5.dp.toPx(), Offset(cx, cy))
            }
            Column(
                Modifier.padding(top = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (nxt.first == 1) "الوقت القادم" else "الصلاة القادمة",
                    color = sky.ink2, fontSize = 13.sp
                )
                Text(
                    NAMES[nxt.first], color = ink, fontSize = 34.sp,
                    fontFamily = NotoKufi, fontWeight = FontWeight.Bold
                )
                TabularText(
                    String.format(Locale.US, "%02d:%02d:%02d", left / 3600, (left % 3600) / 60, left % 60),
                    30.sp, ink, NotoSans, FontWeight.Medium
                )
                Text("عند ${hm.format(nxt.second)}", color = sky.ink2, fontSize = 15.sp)
            }
        }

        Column(Modifier.weight(2.4f).fillMaxWidth()) {
            val shape = RoundedCornerShape(24.dp)
            Column(
                Modifier.weight(1f).fillMaxWidth().clip(shape).background(sky.glass)
                    .border(0.75.dp, sky.hairline, shape).padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                events(ctx, 0).forEach { (i, t) ->
                    val isNext = i == nxt.first
                    val rowShape = RoundedCornerShape(16.dp)
                    Row(
                        Modifier.weight(1f).fillMaxWidth().clip(rowShape)
                            .let {
                                if (isNext) it.background(sky.accent.copy(alpha = 0.14f))
                                    .border(0.75.dp, sky.accent, rowShape)
                                else it
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            NAMES[i], fontSize = 17.sp,
                            color = if (i == 1) sky.ink2 else ink,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium
                        )
                        val o = Offsets.get(ctx, NAMES[i])
                        if (i != 1 && o != 0) {
                            Text(
                                "  معدّل \u200E${if (o > 0) "+$o" else "$o"} د",
                                color = sky.ink2, fontSize = 11.sp
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        TabularText(hm.format(t), 17.sp, ink, NotoSans, FontWeight.Medium)
                        if (i != 1) {
                            val m = modes[NAMES[i]] ?: 0
                            ModeIconButton(sky, m, "نوع التنبيه: ${Modes.labels[m]}") { onCycleMode(NAMES[i]) }
                        } else {
                            Spacer(Modifier.width(36.dp))
                        }
                    }
                }
            }
            Text(
                "محسوبة · ${Prefs.methodLabel(ctx)}",
                color = sky.ink2, fontSize = 11.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp), textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HomeScreen(ctx: Context) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        var now by remember { mutableStateOf(Date()) }
        LaunchedEffect(Unit) { while (true) { delay(1000); now = Date() } }
        var tab by remember { mutableStateOf(0) }
        var cfgVer by remember { mutableStateOf(0) }
        val cfgRead = cfgVer
        var hOff by remember { mutableStateOf(Prefs.hijriOffset(ctx)) }
        var amoledOn by remember { mutableStateOf(Prefs.amoled(ctx)) }
        var fajrFadeOn by remember { mutableStateOf(Prefs.fajrFade(ctx)) }
        var log by remember { mutableStateOf(DiagLog.get(ctx)) }
        val offs = remember {
            mutableStateMapOf<String, Int>().apply {
                NAMES.forEach { put(it, Offsets.get(ctx, it)) }
            }
        }
        val modes = remember {
            mutableStateMapOf<String, Int>().apply {
                NAMES.forEach { put(it, Modes.get(ctx, it)) }
            }
        }
        val all = events(ctx, -1) + events(ctx, 0) + events(ctx, 1)
        val idx = all.indexOfLast { !it.second.after(now) }.coerceAtLeast(0)
        val cur = all[idx]
        val nxt = all[idx + 1]
        val sky = if (cur.first == 5 && amoledOn) Brand.AMOLED else Brand.SKIES[cur.first]
        val ink = sky.ink
        val left = ((nxt.second.time - now.time) / 1000).coerceAtLeast(0)
        val span = (nxt.second.time - cur.second.time).toFloat().coerceAtLeast(1f)
        val frac = ((now.time - cur.second.time) / span).coerceIn(0f, 1f)
        val fmt = remember { SimpleDateFormat("HH:mm", Locale.US) }
        val hijri = remember(now.time / 3600000, hOff) {
            HIJRI.format(Date(now.time + hOff * 86_400_000L))
        }
        val saver = (ctx.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode

        SideEffect {
            val w = (ctx as? Activity)?.window
            if (w != null) {
                val ic = WindowCompat.getInsetsController(w, w.decorView)
                ic.isAppearanceLightStatusBars = !sky.dark
                ic.isAppearanceLightNavigationBars = !sky.dark
            }
        }

        val locMsg = remember { mutableStateOf("") }
        val locate: () -> Unit = {
            val loc = Place.fromDevice(ctx)
            if (loc == null) {
                locMsg.value = "تعذّر الحصول على موقع. افتحوا خرائط جوجل لحظة ثم أعيدوا المحاولة."
            } else {
                Place.set(ctx, loc.latitude, loc.longitude, "موقع الجهاز")
                AlarmScheduler.scheduleNext(ctx)
                locMsg.value = "تم تحديث الموقع وإعادة جدولة التنبيه."
            }
        }
        val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            if (ok) locate() else locMsg.value = "لم يُمنح إذن الموقع."
        }
        val cycleMode: (String) -> Unit = { n ->
            val m = ((modes[n] ?: 0) + 1) % 4
            modes[n] = m
            Modes.set(ctx, n, m)
            Toast.makeText(ctx, "$n: ${Modes.labels[m]}", Toast.LENGTH_SHORT).show()
        }

        SakinaTheme(sky) {
            Box(Modifier.fillMaxSize()) {
                SkyBackdrop(sky, Modifier.fillMaxSize())
                Column(Modifier.fillMaxSize().systemBarsPadding()) {
                    if (tab == 0) {
                        HomeTab(Modifier.weight(1f), ctx, sky, now, nxt, frac, left, hijri, saver, modes, cycleMode)
                    } else {
                        Column(
                            Modifier.weight(1f).fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (tab == 1) {
                                SectionTitle(sky, "القبلة")
                                QiblaScreen(ctx, sky)
                            } else {
                                SectionTitle(sky, "الموقع")
                                GlassCard(sky) {
                                    val co = Place.coords(ctx)
                                    Text(
                                        "${Place.name(ctx)}\nخط العرض ${String.format(Locale.US, "%.2f", co.latitude)}° · خط الطول ${String.format(Locale.US, "%.2f", co.longitude)}°",
                                        color = ink
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        GoldButton("حدّد موقعي", Modifier.weight(1f)) {
                                            val ok = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                                PackageManager.PERMISSION_GRANTED
                                            if (ok) locate() else permLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                        }
                                        GlassButton(sky, "فرندة", Modifier.weight(1f)) {
                                            Place.reset(ctx); AlarmScheduler.scheduleNext(ctx)
                                            locMsg.value = "عاد الموقع إلى فرندة."
                                        }
                                    }
                                    if (locMsg.value.isNotEmpty()) Text(locMsg.value, color = ink, fontSize = 13.sp)
                                }

                                SectionTitle(sky, "الحساب والتاريخ")
                                GlassCard(sky) {
                                    Text("الطريقة: ${Prefs.methodLabel(ctx)}", color = ink)
                                    GlassButton(sky, "تغيير طريقة الحساب", Modifier.fillMaxWidth()) {
                                        Prefs.cycleMethod(ctx); AlarmScheduler.scheduleNext(ctx); cfgVer++
                                    }
                                    GlassButton(
                                        sky,
                                        if (Prefs.hanafi(ctx)) "العصر: حنفي (اضغط للتبديل)" else "العصر: شافعي (اضغط للتبديل)",
                                        Modifier.fillMaxWidth()
                                    ) {
                                        Prefs.setHanafi(ctx, !Prefs.hanafi(ctx)); AlarmScheduler.scheduleNext(ctx); cfgVer++
                                    }
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("تعديل التاريخ الهجري (أيام)", color = ink, modifier = Modifier.weight(1f))
                                        StepButton(sky, "−") {
                                            hOff = (hOff - 1).coerceIn(-2, 2); Prefs.setHijriOffset(ctx, hOff)
                                        }
                                        Text(
                                            if (hOff > 0) "+$hOff" else "$hOff", color = ink,
                                            modifier = Modifier.width(40.dp), textAlign = TextAlign.Center
                                        )
                                        StepButton(sky, "+") {
                                            hOff = (hOff + 1).coerceIn(-2, 2); Prefs.setHijriOffset(ctx, hOff)
                                        }
                                    }
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("وضع الليل الأسود (AMOLED)", color = ink, modifier = Modifier.weight(1f))
                                        Switch(
                                            checked = amoledOn,
                                            onCheckedChange = { amoledOn = it; Prefs.setAmoled(ctx, it) },
                                            colors = switchColors(sky)
                                        )
                                    }
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("تدرّج صوت أذان الفجر (12 ث)", color = ink, modifier = Modifier.weight(1f))
                                        Switch(
                                            checked = fajrFadeOn,
                                            onCheckedChange = { fajrFadeOn = it; Prefs.setFajrFade(ctx, it) },
                                            colors = switchColors(sky)
                                        )
                                    }
                                }

                                SectionTitle(sky, "نوع التنبيه لكل صلاة")
                                GlassCard(sky) {
                                    Text(
                                        "اضغط الشريحة للتبديل: أذان كامل، تكبير قصير، اهتزاز، صامت. وهي نفسها أيقونات القائمة الرئيسية.",
                                        color = sky.ink2, fontSize = 13.sp
                                    )
                                    events(ctx, 0).filter { it.first != 1 }.forEach { (i, _) ->
                                        val n = NAMES[i]
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text(n, color = ink, fontSize = 17.sp, modifier = Modifier.weight(1f))
                                            ModeChip(sky, Modes.labels[modes[n] ?: 0]) { cycleMode(n) }
                                        }
                                    }
                                }

                                SectionTitle(sky, "تعديل المواقيت بالدقائق")
                                GlassCard(sky) {
                                    Text(
                                        "يُضاف الفرق إلى الحساب، ويسري على العدّاد وعلى وقت التنبيه (من -15 إلى +15).",
                                        color = sky.ink2, fontSize = 13.sp
                                    )
                                    events(ctx, 0).filter { it.first != 1 }.forEach { (i, _) ->
                                        val n = NAMES[i]
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text(n, color = ink, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                            StepButton(sky, "−") {
                                                val v = (offs[n] ?: 0) - 1
                                                offs[n] = v; Offsets.set(ctx, n, v); AlarmScheduler.scheduleNext(ctx)
                                            }
                                            Text(
                                                "${offs[n] ?: 0}", color = ink, modifier = Modifier.width(50.dp),
                                                textAlign = TextAlign.Center, fontSize = 16.sp
                                            )
                                            StepButton(sky, "+") {
                                                val v = (offs[n] ?: 0) + 1
                                                offs[n] = v; Offsets.set(ctx, n, v); AlarmScheduler.scheduleNext(ctx)
                                            }
                                        }
                                    }
                                    GlassButton(sky, "إرجاع كل التعديلات إلى صفر", Modifier.fillMaxWidth()) {
                                        NAMES.forEach { offs[it] = 0; Offsets.set(ctx, it, 0) }
                                        AlarmScheduler.scheduleNext(ctx)
                                    }
                                }

                                SectionTitle(sky, "صحة المنبه")
                                HealthCard(ctx, sky)
                                BatteryWizard(ctx, sky)

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    GoldButton("أعد الجدولة", Modifier.weight(1f)) {
                                        AlarmScheduler.scheduleNext(ctx)
                                        log = DiagLog.get(ctx)
                                        Toast.makeText(ctx, "تمت إعادة الجدولة", Toast.LENGTH_SHORT).show()
                                    }
                                    GlassButton(sky, "اختبر الآن (10 ث)", Modifier.weight(1f)) {
                                        AlarmScheduler.scheduleTest(ctx)
                                        Toast.makeText(ctx, "سيظهر التنبيه بعد 10 ثوان", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                Text(
                                    "الاختبار يشغّل الأذان بنوع الفجر الحالي؛ أوقفه بزر «إيقاف» في الإشعار أو بقلب الهاتف على وجهه.",
                                    color = sky.ink2, fontSize = 12.sp
                                )

                                SectionTitle(sky, "سجل التشخيص")
                                GlassCard(sky) {
                                    val clip = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    GlassButton(sky, "نسخ السجل", Modifier.fillMaxWidth()) {
                                        clip.setPrimaryClip(ClipData.newPlainText("Sakina log", log.joinToString("\n")))
                                        Toast.makeText(ctx, "نُسخ السجل", Toast.LENGTH_SHORT).show()
                                    }
                                    log.forEach { Text(it, color = ink, fontSize = 12.sp, fontFamily = NotoSans) }
                                }
                            }
                        }
                    }
                    BrandNav(
                        sky,
                        listOf("الرئيسية", "القبلة", "الإعدادات"),
                        tab
                    ) { if (tab != it) { tab = it; log = DiagLog.get(ctx) } }
                }
            }
        }
    }
}
               
