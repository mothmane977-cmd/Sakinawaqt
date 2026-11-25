package app.sakina

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.util.IslamicCalendar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sin

// -----------------------------------------------------------------------------
// Sakina Al-Qalb ("سكينة القلب") — Living 3D Parallax Glass Card Studio (v47)
// Package: app.sakina (100% Compose 1.6.x compatible, zero GraphicsLayer/FileProvider crashes)
// -----------------------------------------------------------------------------

object SakinaHeartColors {
    val DeepSpaceNavy = Color(0xFF06101F)
    val MidnightNavy = Color(0xFF0A192F)
    val DeepEmerald = Color(0xFF0D3B2E)
    val EmeraldGlow = Color(0xFF1EE3A8)
    val ChampagneGold = Color(0xFFD4AF37)
    val SoftGoldGlow = Color(0xFFF3DFA2)
    val WarmCream = Color(0xFFFAF6EC)
}

enum class HeartMood(
    val labelAr: String,
    val labelEn: String,
    val seedOffset: Int
) {
    SUKOON("سكون", "Stillness", 11),
    TUMAANINAH("طمأنينة", "Tranquility", 28),
    SHUKR("شكر", "Gratitude", 47),
    RAJAA("رجاء", "Hope", 63),
    JABR("جبر خاطر", "Solace", 84),
    TAHAJJUD("ثلث الليل", "Night Vigil", 99)
}

enum class PrayerLightPhase(
    val labelAr: String,
    val primaryGlow: Color,
    val secondaryGlow: Color,
    val primaryGlowArgb: Int
) {
    FAJR_DEW("ندى الفجر", Color(0xFF176B53), Color(0xFF28587B), 0xFF176B53.toInt()),
    DUHA_GOLD("ذهب الضحى", Color(0xFFD4AF37), Color(0xFF1B4D3E), 0xFF7D6217.toInt()),
    SUNSET_WARMTH("دفء الغروب", Color(0xFFB57C3C), Color(0xFF1A2F4B), 0xFF6B441C.toInt()),
    NIGHT_SANCTUARY("سكون الليل", Color(0xFF0D3B2E), Color(0xFF091A32), 0xFF0D3B2E.toInt())
}

data class HeartCardVerse(
    val mood: HeartMood,
    val arabicText: String,
    val translationEn: String,
    val sourceCitation: String,
    val reflectionNote: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val hasOfflineAudio: Boolean = true
)

object SakinaHeartCatalog {
    val curatedVerses: List<HeartCardVerse> = listOf(
        HeartCardVerse(
            mood = HeartMood.SUKOON,
            arabicText = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            translationEn = "Unquestionably, by the remembrance of Allah hearts are assured.",
            sourceCitation = "سورة الرعد · الآية 28",
            reflectionNote = "السكينة لا تُشترى من الخارج، بل تنزل على القلب حين يأنس بربه.",
            surahNumber = 13,
            ayahNumber = 28
        ),
        HeartCardVerse(
            mood = HeartMood.TUMAANINAH,
            arabicText = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            translationEn = "For indeed, with hardship will be ease. Indeed, with hardship will be ease.",
            sourceCitation = "سورة الشرح · الآيتان 5–6",
            reflectionNote = "اليسر لا يأتي بعد العسر فقط، بل يرافقه في قلب اللحظة.",
            surahNumber = 94,
            ayahNumber = 5
        ),
        HeartCardVerse(
            mood = HeartMood.SHUKR,
            arabicText = "لَئِن شَكَرْتُمْ لَأَزِيدَنَّكُمْ",
            translationEn = "If you are grateful, I will surely increase you.",
            sourceCitation = "سورة إبراهيم · الآية 7",
            reflectionNote = "الشكر قيدُ النعمة الموجودة، ومفتاحُ النعمة المنتظرة.",
            surahNumber = 14,
            ayahNumber = 7
        ),
        HeartCardVerse(
            mood = HeartMood.RAJAA,
            arabicText = "وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ",
            translationEn = "And your Lord is going to give you, and you will be satisfied.",
            sourceCitation = "سورة الضحى · الآية 5",
            reflectionNote = "عطاء الله لا يقتصر على قدر سؤالك، بل يمتد حتى يرضى قلبك.",
            surahNumber = 93,
            ayahNumber = 5
        ),
        HeartCardVerse(
            mood = HeartMood.JABR,
            arabicText = "وَاصْبِرْ لِحُكْمِ رَبِّكَ فَإِنَّكَ بِأَعْيُنِنَا",
            translationEn = "And be patient for the decision of your Lord, for indeed you are in Our eyes.",
            sourceCitation = "سورة الطور · الآية 48",
            reflectionNote = "أعظم جبرٍ للقلب أن تعلم أنك في رعاية الله وحفظه في كل خطوة.",
            surahNumber = 52,
            ayahNumber = 48
        ),
        HeartCardVerse(
            mood = HeartMood.TAHAJJUD,
            arabicText = "تَتَجَافَىٰ جُنُوبُهُمْ عَنِ الْمَضَاجِعِ يَدْعُونَ رَبَّهُمْ خَوْفًا وَطَمَعًا",
            translationEn = "Their sides part from their beds, supplicating their Lord in fear and aspiration.",
            sourceCitation = "سورة السجدة · الآية 16",
            reflectionNote = "ساعات السَّحَر هي أصدق خلوة بين العبد وخالقه.",
            surahNumber = 32,
            ayahNumber = 16
        )
    )
}

private fun resolveLiveHijriDateAr(ctx: Context): String {
    return try {
        val ic = IslamicCalendar().apply {
            timeInMillis = System.currentTimeMillis()
            add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
        }
        val months = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        val day = ic.get(IslamicCalendar.DAY_OF_MONTH)
        val month = months.getOrElse(ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)) { "جمادى الآخرة" }
        val year = ic.get(IslamicCalendar.YEAR)
        "$day $month $year هـ"
    } catch (_: Exception) {
        "سَكِينة"
    }
}

private fun detectCurrentLightPhase(): PrayerLightPhase {
    val hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hr) {
        in 4..6 -> PrayerLightPhase.FAJR_DEW
        in 7..15 -> PrayerLightPhase.DUHA_GOLD
        in 16..18 -> PrayerLightPhase.SUNSET_WARMTH
        else -> PrayerLightPhase.NIGHT_SANCTUARY
    }
}

@Composable
fun SakinaHeartCardScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hijriDateLabel = remember { resolveLiveHijriDateAr(context) }

    var selectedMood by remember { mutableStateOf(HeartMood.SUKOON) }
    var activePhase by remember { mutableStateOf(detectCurrentLightPhase()) }
    var showBilingualMirror by remember { mutableStateOf(false) }
    var attachAudioPulse by remember { mutableStateOf(true) }
    var isExportingPng by remember { mutableStateOf(false) }
    var isExportingMp4 by remember { mutableStateOf(false) }
    var mp4Progress by remember { mutableFloatStateOf(0f) }
    var exportFeedback by remember { mutableStateOf<String?>(null) }
    var showHeartGiftDialog by remember { mutableStateOf(false) }

    val activeVerse = remember(selectedMood) {
        SakinaHeartCatalog.curatedVerses.firstOrNull { it.mood == selectedMood }
            ?: SakinaHeartCatalog.curatedVerses.first()
    }

    // Auto-calibrated Gyroscope / Rotation-Vector 3D Parallax
    val (tiltX, tiltY) = rememberCalibratedParallaxTilt()
    val smoothTiltX by animateFloatAsState(
        targetValue = tiltX.coerceIn(-12f, 12f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "tiltX"
    )
    val smoothTiltY by animateFloatAsState(
        targetValue = tiltY.coerceIn(-12f, 12f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "tiltY"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "sakinaCardPulse")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SakinaHeartColors.DeepSpaceNavy,
                            SakinaHeartColors.MidnightNavy,
                            SakinaHeartColors.DeepSpaceNavy
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SakinaHeartColors.MidnightNavy.copy(alpha = 0.85f))
                            .border(0.9.dp, SakinaHeartColors.ChampagneGold.copy(alpha = 0.55f), CircleShape)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "→", color = SakinaHeartColors.SoftGoldGlow, fontSize = 18.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "سكينة القلب",
                            color = SakinaHeartColors.SoftGoldGlow,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SakinaHeadingFont
                        )
                        Text(
                            text = "بطاقة الزجاج الحيّ",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            fontFamily = SakinaBodyFont
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (showBilingualMirror) SakinaHeartColors.DeepEmerald.copy(alpha = 0.88f)
                                else SakinaHeartColors.MidnightNavy.copy(alpha = 0.82f)
                            )
                            .border(
                                0.9.dp,
                                if (showBilingualMirror) SakinaHeartColors.ChampagneGold else SakinaHeartColors.ChampagneGold.copy(alpha = 0.38f),
                                RoundedCornerShape(50)
                            )
                            .clickable { showBilingualMirror = !showBilingualMirror }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "AR/EN",
                            color = SakinaHeartColors.SoftGoldGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = SakinaBodyFont
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3-Layer Spatial Parallax Glass Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.76f)
                        .graphicsLayer {
                            rotationX = smoothTiltX
                            rotationY = smoothTiltY
                            cameraDistance = 16f * density
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Back glass layer 2
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.91f)
                            .graphicsLayer {
                                translationX = -smoothTiltY * 1.4f
                                translationY = smoothTiltX * 1.4f
                                rotationZ = -4.5f
                            }
                            .clip(RoundedCornerShape(26.dp))
                            .background(activePhase.secondaryGlow.copy(alpha = 0.28f))
                            .border(0.8.dp, SakinaHeartColors.ChampagneGold.copy(alpha = 0.24f), RoundedCornerShape(26.dp))
                    )

                    // Middle glass layer 1
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.95f)
                            .graphicsLayer {
                                translationX = -smoothTiltY * 0.7f
                                translationY = smoothTiltX * 0.7f
                                rotationZ = 3.0f
                            }
                            .clip(RoundedCornerShape(26.dp))
                            .background(activePhase.primaryGlow.copy(alpha = 0.34f))
                            .border(0.8.dp, SakinaHeartColors.ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(26.dp))
                    )

                    // Front Hero Card
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        activePhase.primaryGlow.copy(alpha = 0.72f),
                                        SakinaHeartColors.MidnightNavy.copy(alpha = 0.94f),
                                        SakinaHeartColors.DeepSpaceNavy.copy(alpha = 0.98f)
                                    ),
                                    center = Offset(
                                        x = 400f + smoothTiltY * 18f,
                                        y = 350f + smoothTiltX * 18f
                                    ),
                                    radius = 950f
                                )
                            )
                            .border(
                                width = 1.25.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        SakinaHeartColors.SoftGoldGlow.copy(alpha = 0.88f),
                                        SakinaHeartColors.ChampagneGold.copy(alpha = 0.30f),
                                        SakinaHeartColors.SoftGoldGlow.copy(alpha = 0.78f)
                                    )
                                ),
                                shape = RoundedCornerShape(26.dp)
                            )
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = breathScale
                                    scaleY = breathScale
                                    translationX = smoothTiltY * 0.8f
                                    translationY = smoothTiltX * 0.8f
                                }
                        ) {
                            drawGenerativeSacredGeometry(
                                seed = activeVerse.surahNumber * 1000 + activeVerse.ayahNumber + activeVerse.mood.seedOffset,
                                goldColor = SakinaHeartColors.ChampagneGold,
                                glowColor = SakinaHeartColors.SoftGoldGlow
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 22.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✦ ${activeVerse.mood.labelAr}",
                                    color = SakinaHeartColors.SoftGoldGlow.copy(alpha = 0.88f),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = SakinaBodyFont
                                )
                                Text(
                                    text = activePhase.labelAr,
                                    color = Color.White.copy(alpha = 0.60f),
                                    fontSize = 11.sp,
                                    fontFamily = SakinaBodyFont
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = activeVerse.arabicText,
                                    color = SakinaHeartColors.SoftGoldGlow,
                                    fontSize = 25.sp,
                                    lineHeight = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SakinaOrnateArabicFont,
                                    textAlign = TextAlign.Center
                                )
                                AnimatedVisibility(visible = showBilingualMirror) {
                                    Text(
                                        text = activeVerse.translationEn,
                                        color = Color.White.copy(alpha = 0.82f),
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = activeVerse.reflectionNote,
                                    color = SakinaHeartColors.WarmCream.copy(alpha = 0.82f),
                                    fontSize = 12.5.sp,
                                    lineHeight = 20.sp,
                                    fontFamily = SakinaBodyFont,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (attachAudioPulse) {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth(0.65f)
                                            .height(22.dp)
                                    ) {
                                        val bars = 27
                                        val step = size.width / bars
                                        val midY = size.height / 2f
                                        for (i in 0 until bars) {
                                            val distCenter = 1f - abs(i - bars / 2f) / (bars / 2f)
                                            val amp = (0.28f + 0.72f * distCenter) *
                                                (0.45f + 0.55f * sin(wavePhase + i * 0.55f).absoluteValue)
                                            val barH = size.height * amp
                                            drawLine(
                                                color = SakinaHeartColors.ChampagneGold.copy(alpha = 0.78f),
                                                start = Offset(i * step + step / 2f, midY - barH / 2f),
                                                end = Offset(i * step + step / 2f, midY + barH / 2f),
                                                strokeWidth = 2.dp.toPx(),
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activeVerse.sourceCitation,
                                        color = SakinaHeartColors.ChampagneGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = SakinaBodyFont
                                    )
                                    Text(
                                        text = "سَكِينة · $hijriDateLabel",
                                        color = Color.White.copy(alpha = 0.55f),
                                        fontSize = 10.sp,
                                        fontFamily = SakinaBodyFont
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Curved Orbital Mood Dial ("بوصلة الحال") — Zero text clipping!
                HeartMoodArcDial(
                    moods = HeartMood.entries,
                    selectedMood = selectedMood,
                    onMoodSelected = { selectedMood = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Prayer-Time Lighting Sync Row (standard for loop)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val phases = PrayerLightPhase.entries
                    for (idx in phases.indices) {
                        val phase = phases[idx]
                        val active = phase == activePhase
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (active) phase.primaryGlow.copy(alpha = 0.78f)
                                    else SakinaHeartColors.MidnightNavy.copy(alpha = 0.72f)
                                )
                                .border(
                                    0.8.dp,
                                    if (active) SakinaHeartColors.SoftGoldGlow else SakinaHeartColors.ChampagneGold.copy(alpha = 0.24f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { activePhase = phase }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = phase.labelAr,
                                color = if (active) Color.White else Color.White.copy(alpha = 0.65f),
                                fontSize = 10.5.sp,
                                fontFamily = SakinaBodyFont,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Export Action Bar: HD PNG | Living MP4 | Heart Gift ("هدية أثر")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. HD PNG Export & Share (MediaStore — zero FileProvider crash)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SakinaHeartColors.ChampagneGold, SakinaHeartColors.SoftGoldGlow)
                                )
                            )
                            .clickable(enabled = !isExportingPng && !isExportingMp4) {
                                isExportingPng = true
                                exportFeedback = null
                                scope.launch {
                                    try {
                                        val ok = SakinaLivingCardVideoExporter.exportAndShareHdPng(
                                            context = context,
                                            verse = activeVerse,
                                            phase = activePhase,
                                            hijriDateLabel = hijriDateLabel,
                                            showBilingualMirror = showBilingualMirror,
                                            attachAudioPulse = attachAudioPulse
                                        )
                                        exportFeedback = if (ok) "تم حفظ البطاقة في المعرض وتجهيز المشاركة ✓" else "تعذر تصدير الصورة"
                                    } finally {
                                        isExportingPng = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isExportingPng) "جاري الحفظ..." else "صورة HD",
                            color = SakinaHeartColors.DeepSpaceNavy,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SakinaBodyFont
                        )
                    }

                    // 2. Real Living MP4 Export via MediaCodec
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SakinaHeartColors.DeepEmerald.copy(alpha = 0.88f))
                            .border(1.dp, SakinaHeartColors.ChampagneGold.copy(alpha = 0.72f), RoundedCornerShape(50))
                            .clickable(enabled = !isExportingPng && !isExportingMp4) {
                                attachAudioPulse = true
                                isExportingMp4 = true
                                mp4Progress = 0f
                                exportFeedback = "جاري تصيير الفيديو الحيّ (8 ثوانٍ)..."
                                scope.launch {
                                    try {
                                        val ok = SakinaLivingCardVideoExporter.exportAndShareLivingMp4(
                                            context = context,
                                            verse = activeVerse,
                                            phase = activePhase,
                                            hijriDateLabel = hijriDateLabel,
                                            showBilingualMirror = showBilingualMirror,
                                            attachAudioPulse = attachAudioPulse,
                                            onProgress = { p -> mp4Progress = p }
                                        )
                                        exportFeedback = if (ok) "تم حفظ الفيديو الحيّ في المعرض ✓" else "تعذر تصدير الفيديو على هذا الجهاز"
                                    } finally {
                                        isExportingMp4 = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val mp4Label = if (isExportingMp4) {
                            "${(mp4Progress * 100).toInt()}%"
                        } else {
                            "تصدير حيّ MP4"
                        }
                        Text(
                            text = mp4Label,
                            color = SakinaHeartColors.SoftGoldGlow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = SakinaBodyFont
                        )
                    }

                    // 3. Scheduled Heart Gift ("هدية أثر")
                    Box(
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SakinaHeartColors.MidnightNavy.copy(alpha = 0.88f))
                            .border(0.9.dp, SakinaHeartColors.ChampagneGold.copy(alpha = 0.45f), RoundedCornerShape(50))
                            .clickable { showHeartGiftDialog = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هدية أثر",
                            color = SakinaHeartColors.ChampagneGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = SakinaBodyFont
                        )
                    }
                }

                exportFeedback?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = SakinaHeartColors.EmeraldGlow,
                        fontSize = 11.5.sp,
                        fontFamily = SakinaBodyFont,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (showHeartGiftDialog) {
            val windows = listOf("قبل الفجر بـ 10 دقائق", "وقت الضحى", "عند الغروب", "ثلث الليل الأخير")
            AlertDialog(
                onDismissRequest = { showHeartGiftDialog = false },
                containerColor = SakinaHeartColors.MidnightNavy,
                title = {
                    Text(
                        text = "جدولة «هدية أثر»",
                        color = SakinaHeartColors.SoftGoldGlow,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SakinaHeadingFont
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "اختر الوقت الروحاني لحفظ هذه البطاقة كأثر وتذكير لطيف:",
                            color = SakinaHeartColors.WarmCream.copy(alpha = 0.85f),
                            fontSize = 12.5.sp,
                            fontFamily = SakinaBodyFont
                        )
                        for (idx in windows.indices) {
                            val label = windows[idx]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SakinaHeartColors.DeepEmerald.copy(alpha = 0.55f))
                                    .border(0.8.dp, SakinaHeartColors.ChampagneGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit()
                                            .putString("heart_gift_verse", activeVerse.arabicText)
                                            .putString("heart_gift_window", label)
                                            .apply()
                                        exportFeedback = "تم حفظ «هدية أثر» لموعد: $label ✓"
                                        showHeartGiftDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "✦ $label",
                                    color = SakinaHeartColors.SoftGoldGlow,
                                    fontSize = 12.5.sp,
                                    fontFamily = SakinaBodyFont
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHeartGiftDialog = false }) {
                        Text("إغلاق", color = SakinaHeartColors.ChampagneGold, fontFamily = SakinaBodyFont)
                    }
                }
            )
        }
    }
}

@Composable
private fun HeartMoodArcDial(
    moods: List<HeartMood>,
    selectedMood: HeartMood,
    onMoodSelected: (HeartMood) -> Unit
) {
    var dragAccumulated by remember { mutableFloatStateOf(0f) }
    val selectedIdx = moods.indexOf(selectedMood).coerceAtLeast(0)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(selectedMood) {
                detectHorizontalDragGestures { _, dragAmount ->
                    dragAccumulated += dragAmount
                    if (abs(dragAccumulated) > 70f) {
                        val idx = moods.indexOf(selectedMood)
                        val nextIdx = if (dragAccumulated > 0) (idx + 1) % moods.size
                        else (idx - 1 + moods.size) % moods.size
                        onMoodSelected(moods[nextIdx])
                        dragAccumulated = 0f
                    }
                }
            }
    ) {
        Text(
            text = "بوصلة الحال",
            color = SakinaHeartColors.SoftGoldGlow,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = SakinaHeadingFont
        )

        // Glowing golden orbital arc with active jewel indicator
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .height(34.dp)
        ) {
            val w = size.width
            val h = size.height
            val arcPath = Path().apply {
                moveTo(w * 0.04f, h * 0.18f)
                quadraticBezierTo(w * 0.5f, h * 1.15f, w * 0.96f, h * 0.18f)
            }
            drawPath(
                path = arcPath,
                brush = Brush.horizontalGradient(
                    listOf(
                        SakinaHeartColors.ChampagneGold.copy(alpha = 0.25f),
                        SakinaHeartColors.SoftGoldGlow.copy(alpha = 0.90f),
                        SakinaHeartColors.ChampagneGold.copy(alpha = 0.25f)
                    )
                ),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )
            // Nodes along the golden arc
            val count = moods.size.coerceAtLeast(1)
            for (i in 0 until count) {
                val t = if (count == 1) 0.5f else i.toFloat() / (count - 1).toFloat()
                val omt = 1f - t
                val px = omt * omt * (w * 0.04f) + 2f * omt * t * (w * 0.5f) + t * t * (w * 0.96f)
                val py = omt * omt * (h * 0.18f) + 2f * omt * t * (h * 1.15f) + t * t * (h * 0.18f)
                val isSel = i == selectedIdx
                if (isSel) {
                    drawCircle(SakinaHeartColors.SoftGoldGlow.copy(alpha = 0.30f), radius = 9.dp.toPx(), center = Offset(px, py))
                    drawCircle(SakinaHeartColors.SoftGoldGlow, radius = 4.5.dp.toPx(), center = Offset(px, py))
                } else {
                    drawCircle(SakinaHeartColors.ChampagneGold.copy(alpha = 0.55f), radius = 2.6.dp.toPx(), center = Offset(px, py))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Full-width scrollable pills so Arabic mood names NEVER truncate
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(moods) { _, mood ->
                val selected = mood == selectedMood
                val bg by animateColorAsState(
                    targetValue = if (selected) SakinaHeartColors.DeepEmerald.copy(alpha = 0.92f)
                    else SakinaHeartColors.MidnightNavy.copy(alpha = 0.68f),
                    label = "moodBg"
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(bg)
                        .border(
                            width = if (selected) 1.1.dp else 0.7.dp,
                            color = if (selected) SakinaHeartColors.SoftGoldGlow else SakinaHeartColors.ChampagneGold.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { onMoodSelected(mood) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mood.labelAr,
                        color = if (selected) SakinaHeartColors.SoftGoldGlow else Color.White.copy(alpha = 0.70f),
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = SakinaBodyFont,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawGenerativeSacredGeometry(
    seed: Int,
    goldColor: Color,
    glowColor: Color
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val baseRadius = size.minDimension * 0.40f
    val folds = 8 + (seed % 3) * 4

    for (ring in 1..3) {
        val r = baseRadius * (0.48f + ring * 0.18f)
        drawCircle(
            color = goldColor.copy(alpha = 0.14f / ring),
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = 1.dp.toPx())
        )
    }

    val angleStep = 360f / folds
    val innerR = baseRadius * (0.42f + (seed % 5) * 0.04f)
    val outerR = baseRadius * 0.96f

    for (i in 0 until folds) {
        val deg = i * angleStep + (seed % 15)
        rotate(degrees = deg, pivot = Offset(cx, cy)) {
            val path = Path().apply {
                moveTo(cx, cy - outerR)
                lineTo(cx + innerR * 0.28f, cy - innerR)
                lineTo(cx, cy - innerR * 0.55f)
                lineTo(cx - innerR * 0.28f, cy - innerR)
                close()
            }
            drawPath(
                path = path,
                color = glowColor.copy(alpha = 0.16f),
                style = Stroke(width = 0.9.dp.toPx())
            )
        }
    }
}

/**
 * Calibrated 3D Parallax tilt: captures the user's natural hand-holding angle as zero baseline
 * so the glass card sits balanced and tilts smoothly around the natural reading posture.
 */
@Composable
private fun rememberCalibratedParallaxTilt(): Pair<Float, Float> {
    val context = LocalContext.current
    var tilt by remember { mutableStateOf(0f to 0f) }

    DisposableEffect(context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (sm == null || sensor == null) {
            onDispose {}
        } else {
            val listener = object : SensorEventListener {
                private val rotMatrix = FloatArray(9)
                private val orientation = FloatArray(3)
                private var basePitch: Float? = null
                private var baseRoll: Float? = null

                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                        SensorManager.getOrientation(rotMatrix, orientation)
                        val pitchDeg = Math.toDegrees(orientation[1].toDouble()).toFloat()
                        val rollDeg = Math.toDegrees(orientation[2].toDouble()).toFloat()
                        if (basePitch == null) {
                            basePitch = pitchDeg
                            baseRoll = rollDeg
                        }
                        val dpitch = pitchDeg - (basePitch ?: 0f)
                        val droll = rollDeg - (baseRoll ?: 0f)
                        tilt = (dpitch * 0.35f) to (droll * 0.35f)
                    } else {
                        val ax = event.values[0]
                        val ay = event.values[1]
                        if (basePitch == null) {
                            basePitch = ay
                            baseRoll = ax
                        }
                        tilt = ((ay - (basePitch ?: 0f)) * 1.1f) to (-(ax - (baseRoll ?: 0f)) * 1.1f)
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sm.unregisterListener(listener) }
        }
    }
    return tilt
}
