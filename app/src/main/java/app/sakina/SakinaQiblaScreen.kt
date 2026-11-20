package app.sakina

import android.content.Context
import android.location.Geocoder
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

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

fun calculateHaversineDistanceKm(userLat: Double, userLon: Double): Int {
    val r = 6371.0
    val dLat = Math.toRadians(KAABA_LAT - userLat)
    val dLon = Math.toRadians(KAABA_LON - userLon)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(userLat)) * cos(Math.toRadians(KAABA_LAT)) * sin(dLon / 2).pow(2)
    return (r * 2 * atan2(sqrt(a), sqrt(1 - a))).roundToInt()
}

@Composable
fun SakinaQiblaScreen(ctx: Context, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    var locTick by remember { mutableIntStateOf(0) }
    var showLocDialog by remember { mutableStateOf(false) }
    val co = remember(locTick) { Place.coords(ctx) }
    val lat = co.latitude
    val lon = co.longitude
    val placeName = remember(lang, locTick) { localizeCityName(Place.name(ctx), lang) }
    val qiblaBearing = remember(lat, lon) { calculateGreatCircleQibla(lat, lon) }
    val distanceKm = remember(lat, lon) { calculateHaversineDistanceKm(lat, lon) }

    var continuousHeading by remember { mutableFloatStateOf(0f) }
    var normalizedHeading by remember { mutableFloatStateOf(0f) }
    var fieldStrengthUt by remember { mutableFloatStateOf(48f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var hasSensor by remember { mutableStateOf(true) }

    DisposableEffect(ctx, lat, lon) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotVector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val geoRotVector = sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        val accelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotVector == null && geoRotVector == null && (accelSensor == null || magSensor == null)) {
            hasSensor = false
        }

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasMag = false
        var rotVectorActive = false
        var smoothSin = 0f
        var smoothCos = 1f
        var initialized = false

        val declination = try {
            GeomagneticField(lat.toFloat(), lon.toFloat(), 0f, System.currentTimeMillis()).declination
        } catch (_: Exception) { 0f }

        fun pushHeadingFromMatrix(rotMatrix: FloatArray) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotMatrix, orientation)
            val pitchDeg = Math.toDegrees(orientation[1].toDouble())
            // v18 Point 22ج: Tilt-compensated axis remap when phone is held upright (>45 deg)
            val effectiveMatrix = FloatArray(9)
            if (kotlin.math.abs(pitchDeg) > 45.0) {
                SensorManager.remapCoordinateSystem(rotMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, effectiveMatrix)
                SensorManager.getOrientation(effectiveMatrix, orientation)
            }
            val rawDeg = ((Math.toDegrees(orientation[0].toDouble()).toFloat() + declination) + 360f) % 360f
            val rad = Math.toRadians(rawDeg.toDouble())
            val sVal = sin(rad).toFloat()
            val cVal = cos(rad).toFloat()

            if (!initialized) {
                smoothSin = sVal
                smoothCos = cVal
                initialized = true
            } else {
                smoothSin = smoothSin * 0.80f + sVal * 0.20f
                smoothCos = smoothCos * 0.80f + cVal * 0.20f
            }

            val filteredDeg = ((Math.toDegrees(atan2(smoothSin.toDouble(), smoothCos.toDouble())).toFloat() + 360f) % 360f)
            normalizedHeading = filteredDeg

            val currentMod = ((continuousHeading % 360f) + 360f) % 360f
            var delta = filteredDeg - currentMod
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f
            continuousHeading += delta
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                        rotVectorActive = true
                        val rotMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                        pushHeadingFromMatrix(rotMatrix)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        for (i in 0..2) {
                            gravity[i] = if (!hasGravity) event.values[i] else gravity[i] * 0.85f + event.values[i] * 0.15f
                        }
                        hasGravity = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        for (i in 0..2) {
                            geomagnetic[i] = if (!hasMag) event.values[i] else geomagnetic[i] * 0.85f + event.values[i] * 0.15f
                        }
                        hasMag = true
                        val bx = event.values[0]; val by = event.values[1]; val bz = event.values[2]
                        fieldStrengthUt = sqrt(bx * bx + by * by + bz * bz)
                        if (!rotVectorActive && hasGravity) {
                            val r = FloatArray(9)
                            val iMat = FloatArray(9)
                            if (SensorManager.getRotationMatrix(r, iMat, gravity, geomagnetic)) {
                                pushHeadingFromMatrix(r)
                            }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                    sensorAccuracy = accuracy
                }
            }
        }

        // Register fused Rotation Vector first (works reliably on Xiaomi/HyperOS/Samsung/Pixel) (#6)
        if (rotVector != null) {
            sm.registerListener(listener, rotVector, SensorManager.SENSOR_DELAY_GAME)
        } else if (geoRotVector != null) {
            sm.registerListener(listener, geoRotVector, SensorManager.SENSOR_DELAY_GAME)
        }
        // Always listen to Magnetometer (for µT) + Accelerometer (for fallback)
        if (magSensor != null) sm.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
        if (accelSensor != null) sm.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)

        onDispose { sm.unregisterListener(listener) }
    }

    val animatedHeading by animateFloatAsState(
        targetValue = continuousHeading,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "qiblaDialHeading"
    )

    val deltaToQibla = ((qiblaBearing - normalizedHeading + 540f) % 360f) - 180f
    val isAligned = abs(deltaToQibla) <= 4f
    val isFieldNormal = fieldStrengthUt in 22f..68f

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 66.dp, bottom = 74.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Title Cartouche centered cleanly in the open space between Mihrab arch & Compass dial (#7)
        SakinaOrnamentCartouche(
            modifier = Modifier.clickable { showLocDialog = true },
            contentPadding = PaddingValues(horizontal = 36.dp, vertical = 7.dp)
        ) {
            Text(
                text = "${s.qiblaTitle} · $placeName ↻",
                style = TextStyle(
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    brush = SakinaCalligraphyBrush()
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        // 2. Astrolabe Dial with Dedicated 3D Gold Qibla Needle (210.dp so top cartouche has breathing room #7)
        Box(modifier = Modifier.size(210.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.ic_astrolabe_dial),
                contentDescription = "Astrolabe Compass Dial",
                modifier = Modifier.fillMaxSize().rotate(-animatedHeading)
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.minDimension / 2f

                if (isAligned) {
                    drawCircle(
                        color = SakinaBrandColors.StableEmerald.copy(alpha = 0.25f),
                        radius = radius * 0.94f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                val pointerPath = Path().apply {
                    moveTo(cx, 2.dp.toPx())
                    lineTo(cx - 8.dp.toPx(), 16.dp.toPx())
                    lineTo(cx + 8.dp.toPx(), 16.dp.toPx())
                    close()
                }
                drawPath(
                    pointerPath,
                    color = if (isAligned) SakinaBrandColors.StableEmerald else theme.accentBright
                )

                val qiblaAngleRad = Math.toRadians((qiblaBearing - animatedHeading - 90f).toDouble())
                val needleTipR = radius - 14.dp.toPx()
                val needleBaseR = 26.dp.toPx()
                val tipX = cx + (needleTipR * cos(qiblaAngleRad)).toFloat()
                val tipY = cy + (needleTipR * sin(qiblaAngleRad)).toFloat()
                val baseX = cx + (needleBaseR * cos(qiblaAngleRad)).toFloat()
                val baseY = cy + (needleBaseR * sin(qiblaAngleRad)).toFloat()

                val needleColor = if (isAligned) SakinaBrandColors.StableEmerald else Color(0xFFFFD700)
                drawLine(
                    color = needleColor,
                    start = Offset(baseX, baseY),
                    end = Offset(tipX, tipY),
                    strokeWidth = 3.2.dp.toPx()
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFF7D6), needleColor, Color.Transparent),
                        center = Offset(tipX, tipY),
                        radius = 15.dp.toPx()
                    ),
                    radius = 15.dp.toPx(),
                    center = Offset(tipX, tipY)
                )
                drawCircle(color = theme.bgDeep, radius = 6.dp.toPx(), center = Offset(tipX, tipY))
                drawCircle(color = needleColor, radius = 4.dp.toPx(), center = Offset(tipX, tipY))
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(theme.bgCard, theme.bgDeep)))
                    .border(
                        1.6.dp,
                        if (isAligned) Brush.linearGradient(listOf(SakinaBrandColors.StableEmerald, theme.accentBright))
                        else Sakina3DGoldBevelBrush,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_kaaba),
                    contentDescription = "Kaaba",
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // 3. Degree & Live Guidance Cartouche
        val qiblaDeg = qiblaBearing.roundToInt()
        val headingDeg = normalizedHeading.roundToInt() % 360
        val cardinal = s.cardinals[((headingDeg + 22) / 45) % 8]
        val guidanceText = when {
            !hasSensor -> s.noSensor
            isAligned -> "✓ ${s.onQibla}"
            deltaToQibla > 0 -> "${s.turnPhone} ${abs(deltaToQibla).roundToInt()}° ${s.rightDir}".toWesternDigits()
            else -> "${s.turnPhone} ${abs(deltaToQibla).roundToInt()}° ${s.leftDir}".toWesternDigits()
        }

        SakinaOrnamentCartouche(
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 9.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${s.qiblaTargetLabel} $qiblaDeg° · ${s.yourHeadingLabel} $headingDeg° ($cardinal)".toWesternDigits(),
                    fontFamily = SakinaNumeralFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.8.sp,
                    lineHeight = 16.sp,
                    color = if (isAligned) SakinaBrandColors.StableEmerald else theme.accentBright,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = guidanceText,
                    fontFamily = SakinaBodyFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    lineHeight = 14.sp,
                    color = if (isAligned) SakinaBrandColors.StableEmerald else SakinaBrandColors.LuminousIvory,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }
        }

        // 4. Bottom Status Pills + Manual Location Update (#6)
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(theme.bgCard.copy(alpha = 0.90f))
                    .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(15.dp))
                    .clickable { showLocDialog = true }
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(id = R.drawable.ic_location), contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (lang == SakinaLang.AR || lang == SakinaLang.UR) "تحديث الموقع ($placeName)" else "Update Location ($placeName)",
                    fontFamily = SakinaBodyFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = theme.accentBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(theme.bgCard.copy(alpha = 0.90f))
                    .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(17.dp))
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(id = R.drawable.ic_kaaba), contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$distanceKm ${s.kmFromMakkah}".toWesternDigits(),
                    fontFamily = SakinaBodyFont,
                    fontSize = 12.5.sp,
                    color = theme.accentBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val fieldColor = if (isFieldNormal) SakinaBrandColors.StableEmerald else Color(0xFFF59E0B)
            val fieldLabel = if (isFieldNormal) s.stableField else s.magInterference
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(theme.bgCard.copy(alpha = 0.85f))
                    .border(0.7.dp, fieldColor.copy(alpha = 0.6f), RoundedCornerShape(15.dp))
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${fieldStrengthUt.roundToInt()} µT · $fieldLabel".toWesternDigits(),
                    fontFamily = SakinaBodyFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = fieldColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val accLabel = when (sensorAccuracy) {
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "✓ ${s.accHigh}"
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "• ${s.accMed}"
                else -> "⚠ ${s.accLow}"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(theme.bgCard.copy(alpha = 0.85f))
                    .border(0.7.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = accLabel,
                    fontFamily = SakinaBodyFont,
                    fontSize = 11.sp,
                    color = theme.accentBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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
