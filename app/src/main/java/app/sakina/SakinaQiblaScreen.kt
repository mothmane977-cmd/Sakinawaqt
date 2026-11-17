package app.sakina

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.view.HapticFeedbackConstants
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batoulapps.adhan.Qibla
import kotlin.math.*

private fun sakinaSunPosition(lat: Double, lon: Double, timeMs: Long): Pair<Double, Double> {
    val d = timeMs / 86400000.0 + 2440587.5 - 2451545.0
    val g = Math.toRadians((357.529 + 0.98560028 * d) % 360.0)
    val q = (280.459 + 0.98564736 * d) % 360.0
    val lambda = Math.toRadians(q + 1.915 * sin(g) + 0.020 * sin(2 * g))
    val eps = Math.toRadians(23.439 - 0.00000036 * d)
    val ra = atan2(cos(eps) * sin(lambda), cos(lambda))
    val dec = asin(sin(eps) * sin(lambda))
    val gmst = (280.46061837 + 360.98564736629 * d) % 360.0
    val h = Math.toRadians(gmst + lon) - ra
    val latR = Math.toRadians(lat)
    val az = atan2(sin(h), cos(h) * sin(latR) - tan(dec) * cos(latR)) + Math.PI
    val alt = asin(sin(latR) * sin(dec) + cos(latR) * cos(dec) * cos(h))
    return Pair((Math.toDegrees(az) + 360.0) % 360.0, Math.toDegrees(alt))
}

private fun sakinaCardinal(deg: Double): String {
    val names = listOf("شمال", "شمال شرق", "شرق", "جنوب شرق", "جنوب", "جنوب غرب", "غرب", "شمال غرب")
    return names[(((deg + 22.5) % 360.0) / 45.0).toInt() % 8]
}

@Composable
fun SakinaQiblaScreen(ctx: Context, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val co = Place.coords(ctx)
    val qibla = remember(co.latitude, co.longitude) { Qibla(co).direction }
    val distanceKm = remember(co.latitude, co.longitude) {
        val res = FloatArray(1)
        Location.distanceBetween(co.latitude, co.longitude, 21.4225, 39.8262, res)
        (res[0] / 1000f).roundToInt()
    }
    val declination = remember(co.latitude, co.longitude) {
        GeomagneticField(co.latitude.toFloat(), co.longitude.toFloat(), 0f, System.currentTimeMillis()).declination.toDouble()
    }

    var heading by remember { mutableStateOf(0f) }
    var cont by remember { mutableStateOf(0f) }
    var last by remember { mutableStateOf(Float.NaN) }
    var acc by remember { mutableStateOf(SensorManager.SENSOR_STATUS_UNRELIABLE) }
    var magUt by remember { mutableStateOf(42f) }
    var hasSensor by remember { mutableStateOf(true) }
    val view = LocalView.current

    DisposableEffect(Unit) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val magSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (rotSensor == null) {
            hasSensor = false; onDispose { }
        } else {
            val l = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent) {
                    if (e.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        val x = e.values[0]; val y = e.values[1]; val z = e.values[2]
                        magUt = sqrt(x * x + y * y + z * z); return
                    }
                    val r = FloatArray(9); val o = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(r, e.values)
                    SensorManager.getOrientation(r, o)
                    if (abs(o[1]) > 0.8f) {
                        val r2 = FloatArray(9)
                        SensorManager.remapCoordinateSystem(r, SensorManager.AXIS_X, SensorManager.AXIS_Z, r2)
                        SensorManager.getOrientation(r2, o)
                    }
                    val h = ((Math.toDegrees(o[0].toDouble()) + declination + 720.0) % 360.0).toFloat()
                    if (last.isNaN()) cont = h else cont += ((h - last + 540f) % 360f) - 180f
                    last = h; heading = h
                }
                override fun onAccuracyChanged(s: Sensor?, a: Int) {
                    if (s?.type == Sensor.TYPE_ROTATION_VECTOR) acc = a
                }
            }
            sm.registerListener(l, rotSensor, SensorManager.SENSOR_DELAY_UI)
            if (magSensor != null) sm.registerListener(l, magSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sm.unregisterListener(l) }
        }
    }

    val diff = (((qibla - heading + 540.0) % 360.0) - 180.0)
    val aligned = hasSensor && abs(diff) < 2.0
    LaunchedEffect(aligned) { if (aligned) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) }

    val sun = sakinaSunPosition(co.latitude, co.longitude, System.currentTimeMillis())
    val sunUp = sun.second > 0.0
    val fieldStable = magUt in 22f..68f

    val accLabel = when (acc) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "الدقة: عالية"
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "الدقة: جيدة"
        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "الدقة: ضعيفة · حرّك الهاتف على شكل 8"
        else -> "الدقة: غير مستقرة · أبعد الهاتف عن المعادن"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 74.dp, bottom = 78.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("اتجاه القبلة · ${Place.name(ctx)}", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = theme.accentBright, textAlign = TextAlign.Center)

        Box(modifier = Modifier.size(240.dp), contentAlignment = Alignment.Center) {
            SakinaAstrolabeDial(qibla.toFloat(), cont, sun.first.toFloat(), sunUp, aligned, Modifier.fillMaxSize())
            Box(
                modifier = Modifier.size(54.dp).clip(CircleShape).background(theme.bgDeep).border(0.9.dp, Sakina3DGoldBevelBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(id = R.drawable.ic_kaaba), contentDescription = null, modifier = Modifier.size(26.dp))
            }
        }

        SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${qibla.toInt()}° · ${if (aligned) "على القبلة" else sakinaCardinal(qibla)}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = if (aligned) SakinaBrandColors.StableEmerald else theme.accentBright)
                Text(
                    text = when {
                        !hasSensor -> "لا يوجد مستشعر اتجاه"
                        abs(diff) < 4.0 -> "أنت متجه نحو القبلة"
                        else -> "أدر الهاتف ${abs(diff).toInt()}° نحو ${if (diff > 0) "اليمين" else "اليسار"}".toWesternDigits()
                    },
                    fontFamily = SakinaBodyFont,
                    fontSize = 12.sp,
                    color = SakinaBrandColors.LuminousIvory
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            QiblaTelemetryPill(glow = false) {
                Image(painter = painterResource(id = R.drawable.ic_kaaba), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("$distanceKm كم عن مكة".toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 13.sp, color = theme.accentBright)
            }
            QiblaTelemetryPill(glow = fieldStable) {
                Text("${magUt.roundToInt()} μT · ${if (fieldStable) "مجال مستقر" else "تشويش مغناطيسي"}".toWesternDigits(), fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (fieldStable) SakinaBrandColors.StableEmerald else theme.accentBright)
            }
            QiblaTelemetryPill(glow = false) {
                Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(accLabel.toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 12.sp, color = theme.accentBright, maxLines = 1)
            }
        }
    }
}

@Composable
private fun QiblaTelemetryPill(glow: Boolean, content: @Composable RowScope.() -> Unit) {
    val theme = LocalSakinaTheme.current
    val pillShape = RoundedCornerShape(19.dp)
    val borderBrush = if (glow) Brush.horizontalGradient(listOf(Color(0x664ADE80), theme.accentBright, Color(0x664ADE80))) else Sakina3DGoldBevelBrush

    Row(
        modifier = Modifier.fillMaxWidth().height(36.dp).clip(pillShape).background(Brush.verticalGradient(listOf(theme.bgCard, theme.bgDeep))).border(0.65.dp, borderBrush, pillShape).padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun SakinaAstrolabeDial(qibla: Float, headingCont: Float, sunAz: Float, sunUp: Boolean, aligned: Boolean, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val ringRotation by animateFloatAsState(targetValue = -headingCont, animationSpec = spring(stiffness = Spring.StiffnessLow), label = "astrolabe3DRing")
    val accentGlow = if (aligned) SakinaBrandColors.StableEmerald else theme.accentBright

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(painter = painterResource(id = R.drawable.ic_astrolabe_dial), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = ringRotation })
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f; val cy = size.height / 2f; val outerR = size.minDimension / 2f - 12.dp.toPx()
            rotate(degrees = ringRotation, pivot = Offset(cx, cy)) {
                val qRad = Math.toRadians((qibla - 90f).toDouble())
                val qPos = Offset(cx + (outerR - 15.dp.toPx()) * cos(qRad).toFloat(), cy + (outerR - 15.dp.toPx()) * sin(qRad).toFloat())
                drawCircle(accentGlow.copy(alpha = 0.35f), 10.dp.toPx(), qPos)
                drawCircle(accentGlow, 5.dp.toPx(), qPos)
                if (sunUp) {
                    val sRad = Math.toRadians((sunAz - 90f).toDouble())
                    val sPos = Offset(cx + outerR * cos(sRad).toFloat(), cy + outerR * sin(sRad).toFloat())
                    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF3B0), Color(0xFFFF9800), Color.Transparent), center = sPos, radius = 10.dp.toPx()), 10.dp.toPx(), sPos)
                    drawCircle(Color(0xFFFFE082), 4.dp.toPx(), sPos)
                }
            }
            val pointer = Path().apply {
                moveTo(cx, cy - outerR - 6.dp.toPx()); lineTo(cx - 7.dp.toPx(), cy - outerR + 10.dp.toPx()); lineTo(cx + 7.dp.toPx(), cy - outerR + 10.dp.toPx()); close()
            }
            drawPath(pointer, color = accentGlow)
        }
    }
}
