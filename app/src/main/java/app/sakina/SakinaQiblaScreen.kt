package app.sakina

import android.content.Context
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
    val (lat, lon) = remember { Place.coords(ctx) }
    val placeName = remember(lang) { localizeCityName(Place.name(ctx), lang) }
    val qiblaBearing = remember(lat, lon) { calculateGreatCircleQibla(lat, lon) }
    val distanceKm = remember(lat, lon) { calculateHaversineDistanceKm(lat, lon) }

    // Continuous unwrapped heading so animateFloatAsState NEVER spins 360° backwards across North (0°/360°) (#1)
    var continuousHeading by remember { mutableFloatStateOf(0f) }
    var normalizedHeading by remember { mutableFloatStateOf(0f) }
    var fieldStrengthUt by remember { mutableFloatStateOf(48f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var hasSensor by remember { mutableStateOf(true) }

    DisposableEffect(ctx, lat, lon) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotSensor = sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
            ?: sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotSensor == null && (accelSensor == null || magSensor == null)) {
            hasSensor = false
        }

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasMag = false
        var smoothSin = 0f
        var smoothCos = 1f
        var initialized = false

        val declination = GeomagneticField(
            lat.toFloat(), lon.toFloat(), 0f, System.currentTimeMillis()
        ).declination

        fun updateHeadingFromMatrix(rotMatrix: FloatArray) {
            // Remap if phone is tilted upwards > 45 deg, otherwise standard flat table orientation
            val outMatrix = FloatArray(9)
            val pitchSlope = abs(rotMatrix[7])
            val finalMatrix = if (pitchSlope > 0.72f) {
                SensorManager.remapCoordinateSystem(rotMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, outMatrix)
                outMatrix
            } else {
                rotMatrix
            }

            val orientation = FloatArray(3)
            SensorManager.getOrientation(finalMatrix, orientation)
            val rawDeg = (Math.toDegrees(orientation[0].toDouble()).toFloat() + declination + 360f) % 360f
            val rad = Math.toRadians(rawDeg.toDouble())
            val sVal = sin(rad).toFloat()
            val cVal = cos(rad).toFloat()

            // Circular vector low-pass filter eliminates compass jitter (#1)
            if (!initialized) {
                smoothSin = sVal
                smoothCos = cVal
                initialized = true
            } else {
                smoothSin = smoothSin * 0.84f + sVal * 0.16f
                smoothCos = smoothCos * 0.84f + cVal * 0.16f
            }

            val filteredDeg = ((Math.toDegrees(atan2(smoothSin.toDouble(), smoothCos.toDouble())).toFloat() + 360f) % 360f)
            normalizedHeading = filteredDeg

            // Unwrap delta relative to current continuousHeading
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
                        val rotMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                        updateHeadingFromMatrix(rotMatrix)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        for (i in 0..2) gravity[i] = if (!hasGravity) event.values[i] else gravity[i] * 0.85f + event.values[i] * 0.15f
                        hasGravity = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        for (i in 0..2) geomagnetic[i] = if (!hasMag) event.values[i] else geomagnetic[i] * 0.85f + event.values[i] * 0.15f
                        hasMag = true
                        val bx = event.values[0]; val by = event.values[1]; val bz = event.values[2]
                        fieldStrengthUt = sqrt(bx * bx + by * by + bz * bz)
                        if (rotSensor == null && hasGravity && hasMag) {
                            val r = FloatArray(9)
                            val iMat = FloatArray(9)
                            if (SensorManager.getRotationMatrix(r, iMat, gravity, geomagnetic)) {
                                updateHeadingFromMatrix(r)
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

        // Prefer Accelerometer + Magnetic Field fusion when available for direct magnetic North + true declination,
        // or Rotation Vector sensor
        if (accelSensor != null && magSensor != null) {
            sm.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (rotSensor != null) {
            sm.registerListener(listener, rotSensor, SensorManager.SENSOR_DELAY_UI)
        }

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
            .padding(start = 16.dp, end = 16.dp, top = 50.dp, bottom = 74.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Title in Ornate Cartouche (#2)
        SakinaOrnamentCartouche(
            contentPadding = PaddingValues(horizontal = 36.dp, vertical = 7.dp)
        ) {
            Text(
                text = "${s.qiblaTitle} · $placeName",
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = theme.accentBright,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        // 2. Astrolabe Dial with Dedicated 3D Gold Qibla Needle (#1, #19)
        Box(modifier = Modifier.size(226.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.ic_astrolabe_dial),
                contentDescription = "Astrolabe Compass Dial",
                modifier = Modifier.fillMaxSize().rotate(-animatedHeading)
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.minDimension / 2f

                // Alignment emerald halo when facing Qibla
                if (isAligned) {
                    drawCircle(
                        color = SakinaBrandColors.StableEmerald.copy(alpha = 0.25f),
                        radius = radius * 0.94f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Top Fixed Lubber Line (Phone Front Pointer)
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

                // Dedicated Qibla Pointer Needle pointing from center toward Qibla bearing (#1)
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

            // Enlarged Central 3D Kaaba Medallion (58.dp box, 38.dp Kaaba icon) (#19)
            Box(
                modifier = Modifier
                    .size(58.dp)
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
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // 3. Degree & Live Guidance Cartouche (#1):
        // Shows BOTH Qibla Target Degree AND Live Phone Heading Degree so user sees live response!
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
            contentPadding = PaddingValues(horizontal = 36.dp, vertical = 10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${s.qiblaTargetLabel}: $qiblaDeg° · ${s.yourHeadingLabel}: $headingDeg° ($cardinal)".toWesternDigits(),
                    fontFamily = SakinaNumeralFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
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

        // 4. Bottom Status Pills with larger 24.dp Kaaba icon (#19)
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
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
}
