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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
    val co = Place.coords(ctx)
    val qibla = remember(co.latitude, co.longitude) { Qibla(co).direction }
    val distanceKm = remember(co.latitude, co.longitude) {
        val res = FloatArray(1)
        Location.distanceBetween(co.latitude, co.longitude, 21.4225, 39.8262, res)
        (res[0] / 1000f).roundToInt()
    }
    val declination = remember(co.latitude, co.longitude) {
        GeomagneticField(co.latitude.toFloat(), co.longitude.toFloat(), 0f, System.currentTimeMillis())
            .declination.toDouble()
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
            hasSensor = false
            onDispose { }
        } else {
            val l = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent) {
                    if (e.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        val x = e.values[0]
                        val y = e.values[1]
                        val z = e.values[2]
                        magUt = sqrt(x * x + y * y + z * z)
                        return
                    }
                    val r = FloatArray(9)
                    val o = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(r, e.values)
                    SensorManager.getOrientation(r, o)
                    if (abs(o[1]) > 0.8f) {
                        val r2 = FloatArray(9)
                        SensorManager.remapCoordinateSystem(r, SensorManager.AXIS_X, SensorManager.AXIS_Z, r2)
                        SensorManager.getOrientation(r2, o)
                    }
                    val h = ((Math.toDegrees(o[0].toDouble()) + declination + 720.0) % 360.0).toFloat()
                    if (last.isNaN()) cont = h else cont += ((h - last + 540f) % 360f) - 180f
                    last = h
                    heading = h
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
    LaunchedEffect(aligned) {
        if (aligned) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    val sun = sakinaSunPosition(co.latitude, co.longitude, System.currentTimeMillis())
    val sunUp = sun.second > 0.0
    val fieldStable = magUt in 22f..68f

    val accLabel = when (acc) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "الدقة: عالية"
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "الدقة: جيدة"
        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "الدقة: ضعيفة · حرّك الهاتف على شكل ٨"
        else -> "الدقة: غير مستقرة · أبعد الهاتف عن المعادن"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "اتجاه القبلة الحقيقي في ${Place.name(ctx)}",
            fontFamily = SakinaBodyFont,
            fontSize = 13.sp,
            color = SakinaBrandColors.SoftSilver,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier.size(290.dp),
            contentAlignment = Alignment.Center
        ) {
            SakinaAstrolabeDial(
                qibla = qibla.toFloat(),
                headingCont = cont,
                sunAz = sun.first.toFloat(),
                sunUp = sunUp,
                aligned = aligned,
                modifier = Modifier.fillMaxSize()
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.ic_kaaba),
                    contentDescription = "الكعبة المشرفة",
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${qibla.toInt()}°",
                    fontFamily = SakinaNumeralFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp,
                    color = SakinaBrandColors.GoldBright
                )
                Text(
                    text = if (aligned) "على القبلة" else sakinaCardinal(qibla),
                    fontFamily = SakinaHeadingFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (aligned) SakinaBrandColors.StableEmerald else SakinaBrandColors.GoldBright
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = when {
                !hasSensor -> "لا يوجد مستشعر اتجاه في هذا الهاتف"
                abs(diff) < 4.0 -> "أنت متجه نحو القبلة"
                else -> "أدر الهاتف ${abs(diff).toInt()}° نحو ${if (diff > 0) "اليمين" else "اليسار"}"
            },
            fontFamily = SakinaHeadingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (aligned) SakinaBrandColors.StableEmerald else SakinaBrandColors.LuminousIvory,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_kaaba),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$distanceKm كم عن مكة",
                    fontFamily = SakinaBodyFont,
                    fontSize = 14.sp,
                    color = SakinaBrandColors.GoldBright
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            glowGold = fieldStable,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${magUt.roundToInt()} μT · ${if (fieldStable) "مجال مستقر" else "تشويش مغناطيسي"}",
                    fontFamily = SakinaBodyFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (fieldStable) SakinaBrandColors.StableEmerald else SakinaBrandColors.GoldBright
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = SakinaBrandColors.GoldBright,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = accLabel,
                    fontFamily = SakinaBodyFont,
                    fontSize = 13.sp,
                    color = SakinaBrandColors.GoldBright
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SakinaAstrolabeDial(
    qibla: Float,
    headingCont: Float,
    sunAz: Float,
    sunUp: Boolean,
    aligned: Boolean,
    modifier: Modifier = Modifier
) {
    val ringRotation by animateFloatAsState(
        targetValue = -headingCont,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "astrolabeRing"
    )
    val ringColor = if (aligned) SakinaBrandColors.StableEmerald else SakinaBrandColors.GoldBright

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val center = Offset(cx, cy)
        val outerR = size.minDimension / 2f - 12.dp.toPx()

        drawCircle(
            brush = Brush.radialGradient(
                listOf(SakinaBrandColors.ManuscriptGold.copy(alpha = 0.16f), Color.Transparent),
                center = center,
                radius = outerR * 1.05f
            ),
            radius = outerR * 1.05f,
            center = center
        )

        drawCircle(
            brush = Brush.sweepGradient(
                listOf(SakinaBrandColors.ManuscriptGold, ringColor, SakinaBrandColors.MutedGold, SakinaBrandColors.ManuscriptGold)
            ),
            radius = outerR,
            center = center,
            style = Stroke(width = 3.5.dp.toPx())
        )
        drawCircle(
            color = SakinaBrandColors.ManuscriptGold.copy(alpha = 0.45f),
            radius = outerR * 0.72f,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        rotate(degrees = ringRotation, pivot = center) {
            val side = outerR * 1.02f
            listOf(0f, 45f).forEach { angle ->
                rotate(angle, pivot = center) {
                    drawRect(
                        color = SakinaBrandColors.ManuscriptGold.copy(alpha = 0.22f),
                        topLeft = Offset(cx - side / 2f, cy - side / 2f),
                        size = Size(side, side),
                        style = Stroke(1.2.dp.toPx())
                    )
                }
            }
            for (deg in 0 until 360 step 5) {
                val major = deg % 30 == 0
                val len = if (major) 14.dp.toPx() else 7.dp.toPx()
                val rad = Math.toRadians((deg - 90).toDouble())
                drawLine(
                    color = SakinaBrandColors.GoldBright.copy(alpha = if (major) 0.95f else 0.35f),
                    start = Offset(cx + (outerR - len) * cos(rad).toFloat(), cy + (outerR - len) * sin(rad).toFloat()),
                    end = Offset(cx + outerR * cos(rad).toFloat(), cy + outerR * sin(rad).toFloat()),
                    strokeWidth = if (major) 2.dp.toPx() else 1.dp.toPx()
                )
            }
            val qRad = Math.toRadians((qibla - 90f).toDouble())
            val qPos = Offset(cx + (outerR - 22.dp.toPx()) * cos(qRad).toFloat(), cy + (outerR - 22.dp.toPx()) * sin(qRad).toFloat())
            drawCircle(ringColor.copy(alpha = 0.28f), 14.dp.toPx(), qPos)
            drawCircle(ringColor, 6.dp.toPx(), qPos)

            if (sunUp) {
                val sRad = Math.toRadians((sunAz - 90f).toDouble())
                val sPos = Offset(cx + (outerR - 10.dp.toPx()) * cos(sRad).toFloat(), cy + (outerR - 10.dp.toPx()) * sin(sRad).toFloat())
                drawCircle(Color(0xFFFFB74D), 6.dp.toPx(), sPos)
            }
        }

        val tip = Path().apply {
            moveTo(cx, cy - outerR - 4.dp.toPx())
            lineTo(cx - 7.dp.toPx(), cy - outerR + 12.dp.toPx())
            lineTo(cx + 7.dp.toPx(), cy - outerR + 12.dp.toPx())
            close()
        }
        drawPath(path = tip, color = ringColor)
    }
}
