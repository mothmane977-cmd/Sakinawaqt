package app.sakina

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.AbsoluteAlignment

private const val KAABA_LAT = 21.422487
private const val KAABA_LON = 39.826206

fun calculateGreatCircleQibla(userLat: Double, userLon: Double): Float {
    val phi1 = Math.toRadians(userLat)
    val phi2 = Math.toRadians(KAABA_LAT)
    val deltaLambda = Math.toRadians(KAABA_LON - userLon)
    val y = sin(deltaLambda)
    val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)
    val theta = Math.toDegrees(atan2(y, x))
    return ((theta + 360.0) % 360.0).toFloat()
}

// Offline Astronomical Sun Azimuth (degrees clockwise from North, null if sun below horizon)
fun calculateSunAzimuthDeg(latDeg: Double, lonDeg: Double): Float? {
    return try {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        val dayOfYear = cal.get(java.util.Calendar.DAY_OF_YEAR)
        val utcHours = cal.get(java.util.Calendar.HOUR_OF_DAY) +
            cal.get(java.util.Calendar.MINUTE) / 60.0 +
            cal.get(java.util.Calendar.SECOND) / 3600.0
        val gamma = 2.0 * Math.PI / 365.0 * (dayOfYear - 1 + (utcHours - 12.0) / 24.0)
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) -
            0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
            0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma) -
            0.002697 * cos(3 * gamma) + 0.00148 * sin(3 * gamma)
        val trueSolarMin = (utcHours * 60.0 + eqTime + 4.0 * lonDeg + 1440.0) % 1440.0
        val hourAngleRad = Math.toRadians(trueSolarMin / 4.0 - 180.0)
        val latRad = Math.toRadians(latDeg)
        val sinAlt = sin(latRad) * sin(decl) + cos(latRad) * cos(decl) * cos(hourAngleRad)
        val altRad = asin(sinAlt.coerceIn(-1.0, 1.0))
        if (Math.toDegrees(altRad) < -2.0) return null
        val cosAz = ((sin(decl) - sin(latRad) * sinAlt) / (cos(latRad) * cos(altRad))).coerceIn(-1.0, 1.0)
        var azDeg = Math.toDegrees(acos(cosAz))
        if (hourAngleRad > 0.0) azDeg = 360.0 - azDeg
        ((azDeg + 360.0) % 360.0).toFloat()
    } catch (_: Exception) {
        null
    }
}

fun calculateHaversineDistanceKm(userLat: Double, userLon: Double): Int {
    val r = 6371.0
    val dLat = Math.toRadians(KAABA_LAT - userLat)
    val dLon = Math.toRadians(KAABA_LON - userLon)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(userLat)) * cos(Math.toRadians(KAABA_LAT)) * sin(dLon / 2).pow(2)
    return (r * 2 * atan2(sqrt(a), sqrt(1 - a))).roundToInt()
}

// ============================================================================
// 1. نماذج بيانات القبلة ثلاثية الأبعاد (Spatial Qibla State Models - v34)
// ============================================================================
enum class QiblaSensorMode {
    MAGNETOMETER_ONLY,
    FUSED_GYRO_MAG,
    SUN_FALLBACK;

    fun next(): QiblaSensorMode = when (this) {
        MAGNETOMETER_ONLY -> FUSED_GYRO_MAG
        FUSED_GYRO_MAG -> SUN_FALLBACK
        SUN_FALLBACK -> MAGNETOMETER_ONLY
    }
}

enum class MagneticStability {
    STABLE,       // 20..65 µT
    INTERFERENCE  // < 20 أو > 65 µT
}

data class SakinaQiblaStrings(
    val screenTitle: String = "اتجاه القبلة",
    val turnLeftFormat: String = "%d° يساراً",
    val turnRightFormat: String = "%d° يميناً",
    val alignedText: String = "أنت تواجه القبلة ✓",
    val distanceFormat: String = "%s · %d كم إلى مكة",
    val qiblaBearingFormat: String = "القبلة %d° · اتجاهك %d°",
    val sensorFusedLabel: String = "جيروسكوب مدمج",
    val sensorMagLabel: String = "مغناطيس مباشر",
    val sensorSunLabel: String = "مرجع الشمس",
    val accuracyHighLabel: String = "دقة عالية ✓",
    val accuracyCalibrateLabel: String = "يُرجى المعايرة ⚠",
    val microTeslaFormat: String = "%d µT",
    val refreshLocationDesc: String = "تحديث الموقع",
    val switchSensorDesc: String = "تبديل وضع البوصلة",
    val flatLevelHint: String = "اجعل الهاتف مستوياً"
)

data class SakinaSpatialQiblaState(
    val cityName: String = "فرندة",
    val distanceKm: Int = 4059,
    val qiblaBearingDeg: Float = 101f,
    val deviceHeadingDeg: Float = 120f,
    val sunAzimuthDeg: Float? = null,
    val pitchDeg: Float = 0f,
    val rollDeg: Float = 0f,
    val magneticFieldMicroTesla: Int = 45,
    val sensorMode: QiblaSensorMode = QiblaSensorMode.MAGNETOMETER_ONLY
) {
    val signedDeltaDeg: Float
        get() {
            var diff = (qiblaBearingDeg - deviceHeadingDeg) % 360f
            if (diff > 180f) diff -= 360f
            if (diff < -180f) diff += 360f
            return diff
        }

    val isAligned: Boolean
        get() = abs(signedDeltaDeg) <= 3f

    val isPhoneFlat: Boolean
        get() = abs(pitchDeg) <= 14f && abs(rollDeg) <= 14f

    val magneticStability: MagneticStability
        get() = if (magneticFieldMicroTesla in 20..65) {
            MagneticStability.STABLE
        } else {
            MagneticStability.INTERFERENCE
        }
}

// ============================================================================
// 2. نقطة الربط مع مستشعرات الهاتف الحقيقية (SakinaQiblaScreen)
// ============================================================================
@Composable
fun SakinaQiblaScreen(ctx: Context, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current

    var locTick by remember { mutableIntStateOf(0) }
    var showLocDialog by remember { mutableStateOf(false) }
    var showCalibDialog by remember { mutableStateOf(false) }

    val co = remember(locTick) { Place.coords(ctx) }
    val lat = co.latitude
    val lon = co.longitude
    val placeName = remember(lang, locTick) { localizeCityName(Place.name(ctx), lang) }
    val qiblaBearing = remember(lat, lon) { calculateGreatCircleQibla(lat, lon) }
    val sunAzimuth = remember(lat, lon) { calculateSunAzimuthDeg(lat, lon) }
    val distanceKm = remember(lat, lon) { calculateHaversineDistanceKm(lat, lon) }

    var normalizedHeading by remember { mutableFloatStateOf(0f) }
    var pitchDeg by remember { mutableFloatStateOf(0f) }
    var rollDeg by remember { mutableFloatStateOf(0f) }
    var fieldStrengthUt by remember { mutableFloatStateOf(45f) }
    var sensorMode by remember {
        mutableStateOf(
            when (SakinaAdvancedPrefs.qiblaSensorMode(ctx)) {
                1 -> QiblaSensorMode.FUSED_GYRO_MAG
                else -> QiblaSensorMode.MAGNETOMETER_ONLY
            }
        )
    }

    DisposableEffect(ctx, lat, lon, sensorMode) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotVector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val geoRotVector = sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        val gravitySensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val accelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasMag = false
        var smoothSin = 0f
        var smoothCos = 1f
        var initialized = false

        val declination = try {
            GeomagneticField(lat.toFloat(), lon.toFloat(), 0f, System.currentTimeMillis()).declination
        } catch (_: Exception) {
            0f
        }

        fun pushHeadingFromMatrix(r: FloatArray) {
            val flatness = r[8].coerceIn(-1f, 1f)
            val east: Double
            val north: Double
            if (flatness >= 0.35f) {
                east = r[1].toDouble()
                north = r[4].toDouble()
            } else {
                east = -r[2].toDouble()
                north = -r[5].toDouble()
            }
            if (east * east + north * north < 1e-6) return
            val magAzimuthDeg = Math.toDegrees(atan2(east, north)).toFloat()
            val trueAzimuthDeg = ((magAzimuthDeg + declination) + 360f) % 360f

            val rad = Math.toRadians(trueAzimuthDeg.toDouble())
            val sVal = sin(rad).toFloat()
            val cVal = cos(rad).toFloat()

            if (!initialized) {
                smoothSin = sVal
                smoothCos = cVal
                initialized = true
            } else {
                val alpha = 0.16f
                smoothSin = smoothSin * (1f - alpha) + sVal * alpha
                smoothCos = smoothCos * (1f - alpha) + cVal * alpha
            }

            normalizedHeading = ((Math.toDegrees(atan2(smoothSin.toDouble(), smoothCos.toDouble())).toFloat() + 360f) % 360f)
        }

        val useFused = (sensorMode == QiblaSensorMode.FUSED_GYRO_MAG) && (rotVector != null || geoRotVector != null)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                        if (useFused) {
                            val rotMatrix = FloatArray(9)
                            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                            pushHeadingFromMatrix(rotMatrix)
                        }
                    }
                    Sensor.TYPE_GRAVITY -> {
                        for (i in 0..2) gravity[i] = event.values[i]
                        hasGravity = true
                        rollDeg = Math.toDegrees(atan2(gravity[0].toDouble(), gravity[2].toDouble())).toFloat().coerceIn(-90f, 90f)
                        pitchDeg = Math.toDegrees(atan2(gravity[1].toDouble(), gravity[2].toDouble())).toFloat().coerceIn(-90f, 90f)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        if (gravitySensor == null) {
                            for (i in 0..2) {
                                gravity[i] = if (!hasGravity) event.values[i] else gravity[i] * 0.88f + event.values[i] * 0.12f
                            }
                            hasGravity = true
                            rollDeg = Math.toDegrees(atan2(gravity[0].toDouble(), gravity[2].toDouble())).toFloat().coerceIn(-90f, 90f)
                            pitchDeg = Math.toDegrees(atan2(gravity[1].toDouble(), gravity[2].toDouble())).toFloat().coerceIn(-90f, 90f)
                        }
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        for (i in 0..2) {
                            geomagnetic[i] = if (!hasMag) event.values[i] else geomagnetic[i] * 0.82f + event.values[i] * 0.18f
                        }
                        hasMag = true
                        val bx = event.values[0]; val by = event.values[1]; val bz = event.values[2]
                        fieldStrengthUt = sqrt(bx * bx + by * by + bz * bz)
                        if (!useFused && hasGravity) {
                            val r = FloatArray(9)
                            val iMat = FloatArray(9)
                            if (SensorManager.getRotationMatrix(r, iMat, gravity, geomagnetic)) {
                                pushHeadingFromMatrix(r)
                            }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (useFused) {
            val activeRot = rotVector ?: geoRotVector
            if (activeRot != null) sm.registerListener(listener, activeRot, SensorManager.SENSOR_DELAY_GAME)
        }
        if (gravitySensor != null) sm.registerListener(listener, gravitySensor, SensorManager.SENSOR_DELAY_GAME)
        else if (accelSensor != null) sm.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_GAME)
        if (magSensor != null) sm.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_GAME)

        onDispose { sm.unregisterListener(listener) }
    }

    val qiblaStrings = SakinaQiblaStrings(
        screenTitle = sakinaTr(lang, "اتجاه القبلة", "Qibla Direction", "Direction de la Qibla", "Kıble Yönü", "Arah Kiblat", "سمتِ قبلہ"),
        turnLeftFormat = sakinaTr(lang, "%d° يساراً", "%d° Left", "%d° Gauche", "%d° Sola", "%d° Kiri", "%d° بائیں"),
        turnRightFormat = sakinaTr(lang, "%d° يميناً", "%d° Right", "%d° Droite", "%d° Sağa", "%d° Kanan", "%d° دائیں"),
        alignedText = sakinaTr(lang, "أنت تواجه القبلة ✓", "Facing Qibla ✓", "Face à la Qibla ✓", "Kıbleye Dönüksünüz ✓", "Menghadap Kiblat ✓", "آپ قبلہ رخ ہیں ✓"),
        distanceFormat = sakinaTr(lang, "%s · %d كم إلى مكة", "%s · %d km to Makkah", "%s · %d km de La Mecque", "%s · Mekke'ye %d km", "%s · %d km ke Makkah", "%s · مکہ سے %d کلومیٹر"),
        qiblaBearingFormat = sakinaTr(lang, "القبلة %d° · اتجاهك %d°", "Qibla %d° · Heading %d°", "Qibla %d° · Cap %d°", "Kıble %d° · Yön %d°", "Kiblat %d° · Arah %d°", "قبلہ %d° · رخ %d°"),
        sensorFusedLabel = sakinaTr(lang, "جيروسكوب مدمج", "Fused Gyro", "Gyro Fusionné", "Birleşik Jiro", "Giro Fusi", "جائرو فیوزڈ"),
        sensorMagLabel = sakinaTr(lang, "مغناطيس مباشر", "Magnetometer", "Magnétomètre", "Manyetik", "Magnetometer", "مقناطیسی"),
        sensorSunLabel = sakinaTr(lang, "مرجع الشمس", "Sun Reference", "Repère Solaire", "Güneş Referansı", "Acuan Matahari", "سورج حوالہ"),
        accuracyHighLabel = sakinaTr(lang, "دقة عالية ✓", "High Accuracy ✓", "Haute précision ✓", "Yüksek Doğruluk ✓", "Akurasi Tinggi ✓", "اعلیٰ درستگی ✓"),
        accuracyCalibrateLabel = sakinaTr(lang, "يُرجى المعايرة ⚠", "Calibrate ⚠", "Calibrer ⚠", "Kalibre Et ⚠", "Kalibrasi ⚠", "کیلیبریٹ کریں ⚠"),
        flatLevelHint = sakinaTr(lang, "اجعل الهاتف مستوياً", "Hold phone flat", "Tenez le téléphone à plat", "Telefonu düz tutun", "Pegang ponsel datar", "فون کو سیدھا رکھیں")
    )

    val spatialState = SakinaSpatialQiblaState(
        cityName = placeName,
        distanceKm = distanceKm,
        qiblaBearingDeg = qiblaBearing,
        deviceHeadingDeg = normalizedHeading,
        sunAzimuthDeg = sunAzimuth,
        pitchDeg = pitchDeg,
        rollDeg = rollDeg,
        magneticFieldMicroTesla = fieldStrengthUt.roundToInt(),
        sensorMode = sensorMode
    )

    SakinaSpatialQiblaScreen(
        state = spatialState,
        strings = qiblaStrings,
        headingFontFamily = SakinaHeadingFont,
        bodyFontFamily = SakinaBodyFont,
        showBottomDock = false,
        onRefreshLocationClick = { showLocDialog = true },
        onCycleSensorMode = {
            val next = sensorMode.next()
            sensorMode = next
            val prefVal = if (next == QiblaSensorMode.FUSED_GYRO_MAG) 1 else 0
            SakinaAdvancedPrefs.setQiblaSensorMode(ctx, prefVal)
        },
        onCalibrationHelpClick = { showCalibDialog = true }
    )

    if (showCalibDialog) {
        AlertDialog(
            onDismissRequest = { showCalibDialog = false },
            containerColor = theme.bgCard,
            title = {
                Text(
                    text = sakinaTr(lang, "معايرة البوصلة المغناطيسية", "Compass Calibration", "Calibration de la boussole", "Pusula Kalibrasyonu", "Kalibrasi Kompas", "کمپاس کیلیبریشن"),
                    fontFamily = SakinaHeadingFont,
                    color = theme.accentBright
                )
            },
            text = {
                Text(
                    text = sakinaTr(
                        lang,
                        "1. ابتعد عن المعادن والأغطية المغناطيسية.\n2. حرّك الهاتف في الهواء على شكل الرقم (8) ثلاث مرات.\n3. اجعل الهاتف مستوياً حتى تتحوّل فقاعة المركز إلى الأخضر.\n4. في النهار يمكنك مقارنة جرم الشمس الذهبي على الأفق مع موقع الشمس الحقيقي.",
                        "1. Move away from metal objects and magnetic cases.\n2. Wave your phone in a figure-8 motion 3 times.\n3. Hold the phone flat until the center bubble turns emerald.\n4. During daytime, match the golden Sun orb on the horizon with the real Sun.",
                        "1. Éloignez-vous des métaux et coques magnétiques.\n2. Tracez un 8 dans l'air 3 fois avec votre téléphone.\n3. Maintenez le téléphone à plat jusqu'à ce que la bulle devienne verte.",
                        "1. Metal ve mıknatıslı kılıflardan uzaklaşın.\n2. Telefonu havada 3 kez 8 şeklinde hareket ettirin.\n3. Merkez baloncuk yeşil olana kadar düz tutun.",
                        "1. Jauhkan dari logam dan casing magnet.\n2. Gerakkan ponsel membentuk angka 8 tiga kali.\n3. Pegang datar hingga gelembung tengah hijau.",
                        "1. دھات اور مقناطیسی کور سے دور رہیں۔\n2. فون کو ہوا میں 3 بار (8) کی شکل میں گھمائیں۔\n3. فون کو سیدھا رکھیں۔"
                    ),
                    fontFamily = SakinaBodyFont,
                    fontSize = 13.sp,
                    lineHeight = 21.sp,
                    color = SakinaBrandColors.LuminousIvory
                )
            },
            confirmButton = { SakinaBackIconButton(onClick = { showCalibDialog = false }) }
        )
    }

    if (showLocDialog) {
        val cities = listOf(
            Triple("فرندة", 35.0653, 1.0494),
            Triple("الجزائر العاصمة", 36.75, 3.05),
            Triple("وهران", 35.70, -0.63),
            Triple("قسنطينة", 36.36, 6.61),
            Triple("تيارت", 35.37, 1.32),
            Triple("مكة المكرمة", 21.42, 39.82)
        )
        AlertDialog(
            onDismissRequest = { showLocDialog = false },
            containerColor = theme.bgCard,
            title = { Text(s.locationLabel, fontFamily = SakinaHeadingFont, color = theme.accentBright) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.accentGold.copy(alpha = 0.25f))
                            .clickable {
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
                                    locTick++
                                }
                                showLocDialog = false
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(s.autoGps, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, color = theme.accentBright)
                    }
                    cities.forEach { (cName, cLat, cLon) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x22FFFFFF))
                                .clickable {
                                    Place.set(ctx, cLat, cLon, cName)
                                    AlarmScheduler.scheduleNext(ctx)
                                    SakinaLiveNotifier.update(ctx)
                                    locTick++
                                    showLocDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(localizeCityName(cName, lang), fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory)
                            Text("$cLat, $cLon".toWesternDigits(), fontFamily = SakinaNumeralFont, fontSize = 10.5.sp, color = theme.accentBright)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocDialog = false }) { Text("×", color = theme.accentBright, fontSize = 18.sp) }
            }
        )
    }
}

// ============================================================================
// 3. شاشة القبلة ثلاثية الأبعاد (Spatial 3D Geodesic Horizon UI)
// ============================================================================
@Composable
fun SakinaSpatialQiblaScreen(
    state: SakinaSpatialQiblaState,
    strings: SakinaQiblaStrings = SakinaQiblaStrings(),
    dockStrings: SakinaHomeStrings = SakinaHomeStrings(),
    headingFontFamily: FontFamily = FontFamily.Default,
    bodyFontFamily: FontFamily = FontFamily.Default,
    selectedTabIndex: Int = 1,
    showBottomDock: Boolean = false,
    onRefreshLocationClick: () -> Unit = {},
    onCycleSensorMode: () -> Unit = {},
    onCalibrationHelpClick: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(state.isAligned) {
        if (state.isAligned && !wasAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasAligned = state.isAligned
    }

    var continuousDelta by remember { mutableFloatStateOf(state.signedDeltaDeg) }
    LaunchedEffect(state.signedDeltaDeg) {
        var step = (state.signedDeltaDeg - continuousDelta) % 360f
        if (step > 180f) step -= 360f
        if (step < -180f) step += 360f
        continuousDelta += step
    }
    val animatedDelta by animateFloatAsState(
        targetValue = continuousDelta,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "qiblaDelta"
    )

    val rawSunDelta = remember(state.sunAzimuthDeg, state.deviceHeadingDeg) {
        val sun = state.sunAzimuthDeg ?: return@remember null
        var d = (sun - state.deviceHeadingDeg) % 360f
        if (d > 180f) d -= 360f
        if (d < -180f) d += 360f
        d
    }
    val animatedSunDelta by animateFloatAsState(
        targetValue = rawSunDelta ?: 999f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "sunDelta"
    )

    val guidanceColor by animateColorAsState(
        targetValue = if (state.isAligned) Color(0xFF34D399) else SakinaOrbitalColors.SoftGoldGlow,
        label = "guidanceColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SakinaLuxuryPalette.ObsidianNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_qibla_horizon),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.95f,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 54.dp,
                    bottom = if (showBottomDock) 10.dp else 84.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SpatialQiblaHeader(
                state = state,
                strings = strings,
                guidanceColor = guidanceColor,
                headingFontFamily = headingFontFamily,
                bodyFontFamily = bodyFontFamily,
                onRefreshLocationClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onRefreshLocationClick()
                }
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                SpatialHorizonCanvas(
                    deltaDeg = animatedDelta,
                    sunDeltaDeg = if (rawSunDelta != null) animatedSunDelta else null,
                    pitchDeg = state.pitchDeg,
                    rollDeg = state.rollDeg,
                    isAligned = state.isAligned,
                    isPhoneFlat = state.isPhoneFlat
                )

                if (!state.isPhoneFlat) {
                    Text(
                        text = strings.flatLevelHint,
                        color = SakinaOrbitalColors.SoftGoldGlow,
                        fontSize = 11.sp,
                        fontFamily = bodyFontFamily,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .background(
                                SakinaOrbitalColors.GlassNavy,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                0.8.dp,
                                SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            UnifiedTelemetryPill(
                state = state,
                strings = strings,
                bodyFontFamily = bodyFontFamily,
                onCycleSensorMode = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCycleSensorMode()
                },
                onCalibrationHelpClick = onCalibrationHelpClick
            )

            if (showBottomDock) {
                Spacer(modifier = Modifier.height(10.dp))
                LiquidDynamicDock(
                    selectedIndex = selectedTabIndex,
                    strings = dockStrings,
                    bodyFontFamily = bodyFontFamily,
                    onTabSelected = onTabSelected
                )
            }
        }
    }
}

@Composable
private fun SpatialQiblaHeader(
    state: SakinaSpatialQiblaState,
    strings: SakinaQiblaStrings,
    guidanceColor: Color,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onRefreshLocationClick: () -> Unit
) {
    val absDelta = abs(state.signedDeltaDeg).roundToInt()
    val guidanceText = when {
        state.isAligned -> strings.alignedText
        state.signedDeltaDeg < 0f -> strings.turnLeftFormat.format(absDelta)
        else -> strings.turnRightFormat.format(absDelta)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = strings.screenTitle,
            color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = bodyFontFamily
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = guidanceText,
            color = guidanceColor,
            fontSize = if (state.isAligned) 28.sp else 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = headingFontFamily,
            style = TextStyle(fontFeatureSettings = "tnum"),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(SakinaOrbitalColors.GlassNavy.copy(alpha = 0.65f))
                .border(0.8.dp, SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .clickable(onClick = onRefreshLocationClick)
                .padding(horizontal = 12.dp, vertical = 5.dp)
                .semantics { contentDescription = strings.refreshLocationDesc }
        ) {
            Text(
                text = strings.distanceFormat.format(state.cityName, state.distanceKm),
                color = SakinaOrbitalColors.WarmCream,
                fontSize = 12.sp,
                fontFamily = bodyFontFamily,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
            Spacer(modifier = Modifier.size(6.dp))
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                tint = SakinaOrbitalColors.ChampagneGold,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = strings.qiblaBearingFormat.format(
                state.qiblaBearingDeg.roundToInt(),
                state.deviceHeadingDeg.roundToInt()
            ),
            color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontFamily = bodyFontFamily,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}

@Composable
private fun SpatialHorizonCanvas(
    deltaDeg: Float,
    sunDeltaDeg: Float?,
    pitchDeg: Float,
    rollDeg: Float,
    isAligned: Boolean,
    isPhoneFlat: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconPulse"
    )

    val beamColor = if (isAligned) SakinaLuxuryPalette.EmeraldGlow else SakinaLuxuryPalette.GoldBright

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = AbsoluteAlignment.TopLeft
    ) {
        val density = LocalDensity.current
        val boxW = with(density) { maxWidth.toPx() }
        val boxH = with(density) { maxHeight.toPx() }
        // الكعبة ثابتة دائماً في منتصف الأفق (تعمل في جميع اللغات RTL و LTR بدون اختفاء)
        val kaabaXUi = boxW * 0.5f
        val kaabaYUi = boxH * 0.24f - with(density) { 4.dp.toPx() }
        val kaabaImgPx = with(density) { 74.dp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val horizonY = h * 0.24f
        val reticleCenter = Offset(w * 0.5f, h * 0.66f)
        val reticleRx = w * 0.36f
        val reticleRy = h * 0.16f

        val horizonPath = Path().apply {
            moveTo(0f, horizonY + 26.dp.toPx())
            quadraticBezierTo(w * 0.5f, horizonY - 22.dp.toPx(), w, horizonY + 26.dp.toPx())
        }
        drawPath(
            path = horizonPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    SakinaLuxuryPalette.EmeraldGlow.copy(alpha = 0.25f),
                    beamColor.copy(alpha = 0.45f),
                    SakinaLuxuryPalette.EmeraldGlow.copy(alpha = 0.25f),
                    Color.Transparent
                )
            ),
            style = Stroke(width = 9.dp.toPx())
        )
        drawPath(
            path = horizonPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    SakinaLuxuryPalette.GoldRich.copy(alpha = 0.75f),
                    SakinaLuxuryPalette.GoldSpecular,
                    SakinaLuxuryPalette.GoldRich.copy(alpha = 0.75f),
                    Color.Transparent
                )
            ),
            style = Stroke(width = 1.8.dp.toPx())
        )

        val gridCount = 7
        for (i in -gridCount / 2..gridCount / 2) {
            val spreadTop = i * (w * 0.11f)
            val spreadBottom = i * (w * 0.24f)
            drawLine(
                color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.14f),
                start = Offset(w * 0.5f + spreadTop, horizonY),
                end = Offset(w * 0.5f + spreadBottom, h * 0.96f),
                strokeWidth = 0.8.dp.toPx()
            )
        }
        listOf(0.36f, 0.50f, 0.66f, 0.84f).forEach { yFactor ->
            val y = h * yFactor
            drawLine(
                color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.10f),
                start = Offset(w * 0.06f, y),
                end = Offset(w * 0.94f, y),
                strokeWidth = 0.8.dp.toPx()
            )
        }

        if (sunDeltaDeg != null && abs(sunDeltaDeg) <= 85f) {
            val sunNorm = (sunDeltaDeg / 85f).coerceIn(-1f, 1f)
            val sunX = w * 0.5f + sunNorm * (w * 0.42f)
            val sunY = horizonY - 24.dp.toPx() + (sunNorm * sunNorm) * 16.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SakinaOrbitalColors.SoftGoldGlow.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(sunX, sunY),
                    radius = 18.dp.toPx()
                ),
                radius = 18.dp.toPx(),
                center = Offset(sunX, sunY)
            )
            drawCircle(
                color = SakinaOrbitalColors.SoftGoldGlow,
                radius = 6.dp.toPx(),
                center = Offset(sunX, sunY)
            )
        }

        // الكعبة ثابتة في منتصف الأفق بينما يدور مؤشر البوصلة والشعاع مع اتجاه الهاتف
        val kaabaX = w * 0.5f
        val kaabaY = horizonY - 4.dp.toPx()
        val kaabaCenter = Offset(kaabaX, kaabaY)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(beamColor.copy(alpha = 0.45f), Color.Transparent),
                center = kaabaCenter,
                radius = (34f * beaconPulse).dp.toPx()
            ),
            radius = (34f * beaconPulse).dp.toPx(),
            center = kaabaCenter
        )

        val dxBeam = kaabaCenter.x - reticleCenter.x
        val dyBeam = kaabaCenter.y - reticleCenter.y
        val beamLen = kotlin.math.hypot(dxBeam.toDouble(), dyBeam.toDouble()).toFloat().coerceAtLeast(1f)
        val perpX = -dyBeam / beamLen
        val perpY = dxBeam / beamLen
        val baseSpread = 16.dp.toPx()
        val beamCone = Path().apply {
            moveTo(reticleCenter.x + perpX * baseSpread, reticleCenter.y + perpY * baseSpread)
            lineTo(kaabaCenter.x, kaabaCenter.y)
            lineTo(reticleCenter.x - perpX * baseSpread, reticleCenter.y - perpY * baseSpread)
            close()
        }
        drawPath(
            path = beamCone,
            brush = Brush.linearGradient(
                colors = listOf(
                    beamColor.copy(alpha = 0.22f),
                    SakinaLuxuryPalette.GoldBright.copy(alpha = 0.36f),
                    SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.60f)
                ),
                start = reticleCenter,
                end = kaabaCenter
            )
        )
        val steps = 6
        for (s in 1 until steps) {
            val t = s.toFloat() / steps
            val cx = reticleCenter.x + dxBeam * t
            val cy = reticleCenter.y + dyBeam * t
            val halfW = baseSpread * (1f - t)
            drawLine(
                color = SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.45f),
                start = Offset(cx + perpX * halfW, cy + perpY * halfW),
                end = Offset(cx - perpX * halfW, cy - perpY * halfW),
                strokeWidth = 0.9.dp.toPx()
            )
            drawGlowingLuxuryOrb(
                center = Offset(cx, cy),
                coreRadiusPx = (1.6f + (1f - t) * 1.2f).dp.toPx(),
                coreColor = SakinaLuxuryPalette.GoldSpecular,
                haloColor = beamColor,
                haloScale = 2.6f,
                intensity = 0.85f
            )
        }
        drawGlowingGoldLine(
            start = reticleCenter,
            end = kaabaCenter,
            coreStrokePx = if (isAligned) 3.4.dp.toPx() else 2.4.dp.toPx(),
            glowColor = beamColor
        )

        drawLuxuryKaabaEmblem(
            center = kaabaCenter,
            sizePx = 32.dp.toPx(),
            withLightPillar = true,
            pillarHeightPx = 98.dp.toPx(),
            glowColor = beamColor
        )

        drawGlowingGoldOval(
            topLeft = Offset(reticleCenter.x - reticleRx, reticleCenter.y - reticleRy),
            size = Size(reticleRx * 2f, reticleRy * 2f),
            coreStrokePx = 2.4.dp.toPx(),
            glowColor = beamColor,
            alphaScale = 1.0f
        )
        drawOval(
            color = SakinaLuxuryPalette.EmeraldGlow.copy(alpha = 0.48f),
            topLeft = Offset(reticleCenter.x - reticleRx * 0.84f, reticleCenter.y - reticleRy * 0.84f),
            size = Size(reticleRx * 1.68f, reticleRy * 1.68f),
            style = Stroke(width = 1.6.dp.toPx())
        )
        drawOval(
            color = SakinaLuxuryPalette.GoldBright.copy(alpha = 0.36f),
            topLeft = Offset(reticleCenter.x - reticleRx * 0.64f, reticleCenter.y - reticleRy * 0.64f),
            size = Size(reticleRx * 1.28f, reticleRy * 1.28f),
            style = Stroke(
                width = 1.1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )
        )
        for (deg in 0 until 360 step 15) {
            val a = deg * (PI / 180.0)
            val isMajor = deg % 90 == 0
            val innerScale = if (isMajor) 0.88f else 0.94f
            val p1 = Offset(
                reticleCenter.x + (cos(a) * reticleRx * innerScale).toFloat(),
                reticleCenter.y + (sin(a) * reticleRy * innerScale).toFloat()
            )
            val p2 = Offset(
                reticleCenter.x + (cos(a) * reticleRx).toFloat(),
                reticleCenter.y + (sin(a) * reticleRy).toFloat()
            )
            drawLine(
                color = if (isMajor) SakinaLuxuryPalette.GoldSpecular else SakinaLuxuryPalette.GoldBright.copy(alpha = 0.55f),
                start = p1,
                end = p2,
                strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
            )
        }

        val rad = (deltaDeg - 90f) * (PI / 180.0)
        val dotOnReticle = Offset(
            x = reticleCenter.x + (cos(rad) * reticleRx).toFloat(),
            y = reticleCenter.y + (sin(rad) * reticleRy).toFloat()
        )
        drawCircle(
            color = beamColor,
            radius = 5.5.dp.toPx(),
            center = dotOnReticle
        )

        val maxShiftPx = 20.dp.toPx()
        val bubbleOffset = Offset(
            x = reticleCenter.x + (rollDeg / 45f).coerceIn(-1f, 1f) * maxShiftPx,
            y = reticleCenter.y + (pitchDeg / 45f).coerceIn(-1f, 1f) * maxShiftPx * 0.6f
        )
        drawCircle(
            color = SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.35f),
            radius = 16.dp.toPx(),
            center = reticleCenter,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = if (isPhoneFlat) Color(0xFF34D399) else SakinaOrbitalColors.SoftGoldGlow,
            radius = 5.dp.toPx(),
            center = bubbleOffset
        )
    }

        // مجسّم الكعبة ثلاثي الأبعاد ثابت دائماً في منتصف الأفق
        Image(
            painter = painterResource(id = R.drawable.ic_kaaba_3d),
            contentDescription = null,
            modifier = Modifier
                .size(74.dp)
                .absoluteOffset {
                    IntOffset(
                        x = (kaabaXUi - kaabaImgPx / 2f).roundToInt(),
                        y = (kaabaYUi - kaabaImgPx * 0.56f).roundToInt()
                    )
                }
        )
    }
}

private fun DrawScope.drawKaabaBeacon(center: Offset, sizePx: Float, glowColor: Color) {
    val topLeft = Offset(center.x - sizePx / 2f, center.y - sizePx / 2f)
    drawRoundRect(
        color = SakinaOrbitalColors.DeepSpaceNavy,
        topLeft = topLeft,
        size = Size(sizePx, sizePx),
        cornerRadius = CornerRadius(3.dp.toPx())
    )
    drawRoundRect(
        color = glowColor,
        topLeft = topLeft,
        size = Size(sizePx, sizePx),
        cornerRadius = CornerRadius(3.dp.toPx()),
        style = Stroke(width = 1.6.dp.toPx())
    )
    drawLine(
        color = SakinaOrbitalColors.SoftGoldGlow,
        start = Offset(topLeft.x + 2.dp.toPx(), topLeft.y + sizePx * 0.34f),
        end = Offset(topLeft.x + sizePx - 2.dp.toPx(), topLeft.y + sizePx * 0.34f),
        strokeWidth = 2.dp.toPx()
    )
}

@Composable
private fun UnifiedTelemetryPill(
    state: SakinaSpatialQiblaState,
    strings: SakinaQiblaStrings,
    bodyFontFamily: FontFamily,
    onCycleSensorMode: () -> Unit,
    onCalibrationHelpClick: () -> Unit
) {
    val isStable = state.magneticStability == MagneticStability.STABLE
    val sensorName = when (state.sensorMode) {
        QiblaSensorMode.FUSED_GYRO_MAG -> strings.sensorFusedLabel
        QiblaSensorMode.MAGNETOMETER_ONLY -> strings.sensorMagLabel
        QiblaSensorMode.SUN_FALLBACK -> strings.sensorSunLabel
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .sakinaLuxuryGlassCard(
                cornerRadius = 22.dp,
                innerHairline = false,
                emeraldFlare = isStable,
                shimmer = true,
                borderAlpha = 0.72f
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sensorName,
                color = SakinaOrbitalColors.SoftGoldGlow,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = bodyFontFamily,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SakinaOrbitalColors.ChampagneGold.copy(alpha = 0.14f))
                    .clickable(onClick = onCycleSensorMode)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .semantics { contentDescription = strings.switchSensorDesc }
            )

            Text(
                text = strings.microTeslaFormat.format(state.magneticFieldMicroTesla),
                color = SakinaOrbitalColors.WarmCream,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = bodyFontFamily,
                style = TextStyle(fontFeatureSettings = "tnum")
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onCalibrationHelpClick)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                if (isStable) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = if (isStable) strings.accuracyHighLabel else strings.accuracyCalibrateLabel,
                    color = if (isStable) Color(0xFF34D399) else Color(0xFFFBBF24),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = bodyFontFamily
                )
            }
        }
    }
}
