package app.sakina

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

/**
 * Sakina v43 — 3-Slide Luxury First-Run Onboarding (15-second zero-friction setup).
 * Slide 1: Identity, Ad-free/Offline promise & Instant 6-Language Switcher
 * Slide 2: Location & Official Country Calculation Method (GPS or Offline Manual)
 * Slide 3: Live 0%->100% Adhan Reliability Ring (Notifications + Exact Alarms + Battery)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SakinaOnboardingScreen(
    ctx: Context,
    currentLang: SakinaLang,
    onLangSelected: (SakinaLang) -> Unit,
    onRequestGpsDetect: () -> Unit,
    onOpenCityPicker: () -> Unit,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    var permTick by remember { mutableIntStateOf(0) }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permTick++ }
    val locLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            onRequestGpsDetect()
        }
        permTick++
    }

    val notifGranted = remember(permTick) {
        if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }
    val exactAlarmGranted = remember(permTick) {
        if (Build.VERSION.SDK_INT >= 31) {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.canScheduleExactAlarms()
        } else true
    }
    val batteryUnrestricted = remember(permTick) {
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        pm.isIgnoringBatteryOptimizations(ctx.packageName)
    }
    val readinessPct = remember(notifGranted, exactAlarmGranted, batteryUnrestricted) {
        var score = 0
        if (notifGranted) score += 34
        if (exactAlarmGranted) score += 33
        if (batteryUnrestricted) score += 33
        score
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SakinaLuxuryPalette.ObsidianNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SakinaAmbientLuxuryBackdrop(showGeometricPattern = true, showStarDust = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar: progress dots + Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { idx ->
                        val active = pagerState.currentPage == idx
                        Box(
                            modifier = Modifier
                                .width(if (active) 26.dp else 8.dp)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) SakinaLuxuryPalette.MetallicGoldButtonBrush
                                    else androidx.compose.ui.graphics.SolidColor(SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.28f))
                                )
                        )
                    }
                }

                Text(
                    text = sakinaTr(currentLang, "تخطّي الآن", "Skip", "Passer", "Atla", "Lewati", "ابھی چھوڑیں"),
                    color = SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontFamily = SakinaBodyFont,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onFinish() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingSlideIdentity(
                        lang = currentLang,
                        onLangSelected = onLangSelected
                    )
                    1 -> OnboardingSlideLocation(
                        ctx = ctx,
                        lang = currentLang,
                        onGpsClick = {
                            locLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        },
                        onManualCityClick = onOpenCityPicker
                    )
                    else -> OnboardingSlideReliability(
                        lang = currentLang,
                        readinessPct = readinessPct,
                        notifGranted = notifGranted,
                        exactAlarmGranted = exactAlarmGranted,
                        batteryUnrestricted = batteryUnrestricted,
                        onRequestNotif = {
                            if (Build.VERSION.SDK_INT >= 33) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onRequestExactAlarm = {
                            if (Build.VERSION.SDK_INT >= 31) {
                                try {
                                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.parse("package:${ctx.packageName}")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                } catch (_: Exception) {}
                                permTick++
                            }
                        },
                        onRequestBattery = {
                            try {
                                ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${ctx.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            } catch (_: Exception) {}
                            permTick++
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary brushed-gold CTA button
            val ctaLabel = if (pagerState.currentPage < 2) {
                sakinaTr(currentLang, "التالي", "Continue", "Continuer", "Devam", "Lanjut", "آگے")
            } else {
                sakinaTr(currentLang, "ابدأ مع سَكِينة", "Start with Sakina", "Commencer avec Sakina", "Sakina ile Başla", "Mulai dengan Sakina", "سکینہ کے ساتھ شروع کریں")
            }
            SakinaBrushedGoldButton(
                text = ctaLabel,
                pillBgRes = R.drawable.btn_gold_cta_pill,
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onFinish()
                    }
                },
                height = 50.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OnboardingSlideIdentity(
    lang: SakinaLang,
    onLangSelected: (SakinaLang) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(168.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pad = 16.dp.toPx()
                drawGlowingGoldOval(
                    topLeft = Offset(pad, pad),
                    size = Size(size.width - pad * 2f, size.height - pad * 2f),
                    coreStrokePx = 2.6.dp.toPx(),
                    alphaScale = 0.95f
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_kaaba_3d),
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = sakinaTr(lang, "سَكِينة… وقتك مع الله بلا ضجيج", "Sakina… Your Prayer Time in Peace", "Sakina… Vos prières en sérénité", "Sakina… Huzurla Namaz Vakitleri", "Sakina… Waktu Sholat Penuh Ketenangan", "سکینہ… شور کے بغیر نماز کا وقت"),
            style = TextStyle(
                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SakinaHeadingFont,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                sakinaTr(lang, "بلا إعلانات", "Zero Ads", "Sans pub", "Reklamsız", "Tanpa Iklan", "بغیر اشتہارات"),
                sakinaTr(lang, "100% أوفلاين", "100% Offline", "100% Hors-ligne", "100% Çevrimdışı", "100% Offline", "100% آف لائن"),
                sakinaTr(lang, "بلا تتبّع", "Zero Tracking", "Zéro suivi", "İzlemesiz", "Tanpa Pelacakan", "بغیر ٹریکنگ")
            ).forEach { badge ->
                Text(
                    text = badge,
                    color = SakinaLuxuryColors.SoftGoldGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = SakinaBodyFont,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.65f))
                        .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = sakinaTr(lang, "اختر لغتك المفضّلة", "Choose your language", "Choisissez votre langue", "Dilinizi seçin", "Pilih bahasa Anda", "اپنی زبان منتخب کریں"),
            color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f),
            fontSize = 12.5.sp,
            fontFamily = SakinaBodyFont
        )

        Spacer(modifier = Modifier.height(10.dp))

        val rows = SakinaLang.entries.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { rowLangs ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowLangs.forEach { item ->
                        val selected = item == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (selected) SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.92f)
                                    else SakinaLuxuryColors.MidnightNavy.copy(alpha = 0.75f)
                                )
                                .border(
                                    width = if (selected) 1.2.dp else 0.8.dp,
                                    color = if (selected) SakinaLuxuryColors.SoftGoldGlow else SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.30f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onLangSelected(item) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.nativeName,
                                color = if (selected) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.80f),
                                fontSize = 12.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = SakinaBodyFont
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingSlideLocation(
    ctx: Context,
    lang: SakinaLang,
    onGpsClick: () -> Unit,
    onManualCityClick: () -> Unit
) {
    val city = remember { localizeCityName(Place.name(ctx), lang) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .sakinaLuxuryGlassCard(cornerRadius = 22.dp, innerHairline = true, emeraldFlare = true)
                .clip(RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_qibla_horizon_grid),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.85f,
                modifier = Modifier.fillMaxSize()
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = city,
                    style = TextStyle(
                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SakinaHeadingFont
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sakinaTr(lang, "دقة فلكية أوفلاين حسب بلدك", "Official Astronomical Precision Offline", "Précision astronomique hors-ligne", "Resmi Çevrimdışı Astronomik Hassasiyet", "Akurasi Astronomi Resmi Offline", "ملک کے مطابق درست اوقات"),
                    color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.88f),
                    fontSize = 12.sp,
                    fontFamily = SakinaBodyFont
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = sakinaTr(lang, "مواقيتك بدقة بلدك", "Prayer Times for Your City", "Horaires précis de votre ville", "Şehrinize Özel Vakitler", "Jadwal Sholat Akurat", "آپ کے شہر کے درست اوقات"),
            style = TextStyle(
                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SakinaHeadingFont,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = sakinaTr(lang, "يمكنك تحديد موقعك تلقائياً بضغطة واحدة، أو اختيار مدينتك يدوياً بدون إنترنت أو GPS.", "Detect your city automatically with one tap, or pick manually offline without GPS.", "Détectez votre ville automatiquement ou choisissez-la manuellement hors-ligne.", "Konumunuzu tek dokunuşla algılayın veya çevrimdışı seçin.", "Deteksi otomatis atau pilih kota secara manual offline.", "اپنا مقام خودکار منتخب کریں یا آف لائن شہر چنیں۔"),
            color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f),
            fontSize = 13.sp,
            fontFamily = SakinaBodyFont,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        SakinaGlowingGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            onClick = onGpsClick
        ) {
            Text(
                text = sakinaTr(lang, "تحديد موقعي تلقائياً (GPS)", "Detect My Location Automatically", "Détecter ma position (GPS)", "Konumumu Otomatik Algıla", "Deteksi Lokasi Otomatis", "خودکار مقام منتخب کریں"),
                color = SakinaLuxuryColors.SoftGoldGlow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SakinaBodyFont,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        SakinaGlowingGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            emeraldIntensity = 0.16f,
            onClick = onManualCityClick
        ) {
            Text(
                text = sakinaTr(lang, "اختيار المدينة يدوياً أوفلاين", "Choose City Manually Offline", "Choisir la ville manuellement", "Şehri Manuel Seç", "Pilih Kota Manual Offline", "دستی طور پر شہر منتخب کریں"),
                color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.90f),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = SakinaBodyFont,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 13.dp)
            )
        }
    }
}

@Composable
private fun OnboardingSlideReliability(
    lang: SakinaLang,
    readinessPct: Int,
    notifGranted: Boolean,
    exactAlarmGranted: Boolean,
    batteryUnrestricted: Boolean,
    onRequestNotif: () -> Unit,
    onRequestExactAlarm: () -> Unit,
    onRequestBattery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SakinaCircularProgressRing(
            progress = readinessPct / 100f,
            centerLabel = "$readinessPct%",
            size = 96.dp,
            strokeWidth = 6.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = sakinaTr(lang, "أذان لا يفوتك أبداً", "Never Miss an Adhan", "Ne manquez jamais l'Adhan", "Ezanı Asla Kaçırmayın", "Jangan Lewatkan Adzan", "اذان کبھی نہ چھوٹنے دیں"),
            style = TextStyle(
                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SakinaHeadingFont,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = sakinaTr(lang, "فعّل هذه الصلاحيات الثلاث ليصل الأذان في وقته الدقيق حتى عند إغلاق الشاشة.", "Enable these 3 permissions so Adhan rings on the exact second even when the screen is off.", "Activez ces 3 autorisations pour un Adhan ponctuel même écran éteint.", "Ekran kapalıyken bile tam vaktinde ezan için 3 izni etkinleştirin.", "Aktifkan 3 izin ini agar adzan berbunyi tepat waktu saat layar mati.", "اسکرین بند ہونے پر بھی درست وقت پر اذان کے لیے یہ 3 اجازتیں فعال کریں۔"),
            color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f),
            fontSize = 12.5.sp,
            fontFamily = SakinaBodyFont,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ReliabilityPermRow(
                title = sakinaTr(lang, "إشعارات الأذان والتنبيهات", "Adhan Notifications", "Notifications d'Adhan", "Ezan Bildirimleri", "Notifikasi Adzan", "اذان کی اطلاعات"),
                checked = notifGranted,
                onToggle = { onRequestNotif() }
            )
            ReliabilityPermRow(
                title = sakinaTr(lang, "المنبّهات الدقيقة في وقتها", "Exact Prayer Alarms", "Alarmes exactes", "Tam Zamanlı Alarmlar", "Alarm Sholat Tepat Waktu", "درست وقت کے الارم"),
                checked = exactAlarmGranted,
                onToggle = { onRequestExactAlarm() }
            )
            ReliabilityPermRow(
                title = sakinaTr(lang, "استثناء توفير البطارية", "Unrestricted Battery", "Batterie sans restriction", "Pil Kısıtlaması Yok", "Baterai Tanpa Batasan", "بیٹری کی پابندی ہٹائیں"),
                checked = batteryUnrestricted,
                onToggle = { onRequestBattery() }
            )
        }
    }
}

@Composable
private fun ReliabilityPermRow(
    title: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    SakinaGlowingGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        emeraldIntensity = if (checked) 0.42f else 0.18f,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = SakinaOrbitalColors.WarmCream,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = SakinaBodyFont,
                modifier = Modifier.weight(1f)
            )
            SakinaJewelSwitch(
                checked = checked,
                onCheckedChange = { onToggle() }
            )
        }
    }
}
