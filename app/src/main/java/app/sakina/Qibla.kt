package app.sakina

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batoulapps.adhan.Qibla
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/** موضع الشمس: (السمت من الشمال الحقيقي، الارتفاع فوق الأفق) بالدرجات. */
private fun sunPosition(lat: Double, lon: Double, timeMs: Long): Pair<Double, Double> {
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

private fun cardinal(deg: Double): String {
    val names = listOf("شمال", "شمال شرق", "شرق", "جنوب شرق", "جنوب", "جنوب غرب", "غرب", "شمال غرب")
    return names[(((deg + 22.5) % 360.0) / 45.0).toInt() % 8]
}

/** قرص أسطرلابي فاخر: حلقة ذهبية، علامات دقيقة، نجمة ثمانية، ومنارة القبلة. */
@Composable
private fun QiblaDial(
    sky: Sky,
    qibla: Float,
    headingCont: Float,
    sunAz: Float,
    sunUp: Boolean,
    aligned: Boolean,
    centerLabel: String
) {
    val accent = sky.accent
    val ringRotation by animateFloatAsState(
        targetValue = -headingCont,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ring"
    )
    
    Box(Modifier.size(300.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f - 20.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer Golden Ring
            drawCircle(color = accent.copy(alpha = 0.8f), radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))
            drawCircle(color = accent.copy(alpha = 0.3f), radius = radius - 4.dp.toPx(), center = center, style = Stroke(width = 0.5.dp.toPx()))

            rotate(degrees = ringRotation, pivot = center) {
                // Degree ticks
                for (deg in 0 until 360 step 5) {
                    val major = deg % 30 == 0
                    val len = if (major) 16.dp.toPx() else 8.dp.toPx()
                    val rad = Math.toRadians((deg - 90).toDouble())
                    drawLine(
                        color = accent.copy(alpha = if (major) 1f else 0.4f),
                        start = Offset(center.x + (radius - len) * cos(rad).toFloat(), center.y + (radius - len) * sin(rad).toFloat()),
                        end = Offset(center.x + radius * cos(rad).toFloat(), center.y + radius * sin(rad).toFloat()),
                        strokeWidth = if (major) 2.dp.toPx() else 1.dp.toPx()
                    )
                }

                // Inner Astrolabe Geometric Pattern (Octagram)
                val side = radius * 1.1f
                listOf(0f, 45f).forEach { angle ->
                    rotate(angle, pivot = center) {
                        drawRect(color = accent.copy(alpha = 0.15f), topLeft = Offset(center.x - side / 2f, center.y - side / 2f), size = Size(side, side), style = Stroke(1.dp.toPx()))
                    }
                }
                
                // Kaaba Marker
                val qRad = Math.toRadians((qibla - 90f).toDouble())
                val qPos = Offset(center.x + (radius - 24.dp.toPx()) * cos(qRad).toFloat(), center.y + (radius - 24.dp.toPx()) * sin(qRad).toFloat())
                drawCircle(accent.copy(alpha = 0.2f), 20.dp.toPx(), qPos)
                drawCircle(accent, 6.dp.toPx(), qPos)

                // Sun Marker
                if (sunUp) {
                    val sRad = Math.toRadians((sunAz - 90f).toDouble())
                    val sPos = Offset(center.x + (radius - 12.dp.toPx()) * cos(sRad).toFloat(), center.y + (radius - 12.dp.toPx()) * sin(sRad).toFloat())
                    drawCircle(Color(0xFFE6A23C), 6.dp.toPx(), sPos)
                }
            }
            // Fixed Heading Pointer
            drawLine(accent, Offset(center.x, 0f), Offset(center.x, 30.dp.toPx()), 4.dp.toPx())
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${qibla.toInt()}°", fontFamily = NotoKufi, fontWeight = FontWeight.Bold, fontSize = 48.sp, color = sky.ink)
            Text(centerLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accent, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun QiblaScreen(ctx: Context, sky: Sky) {
    val ink = sky.ink
    val co = Place.coords(ctx)
    val qibla = remember(co.latitude, co.longitude) { Qibla(co).direction }
    val declination = remember(co.latitude, co.longitude) {
        GeomagneticField(co.latitude.toFloat(), co.longitude.toFloat(), 0f, System.currentTimeMillis())
            .declination.toDouble()
    }
    var heading by remember { mutableStateOf(0f) }
    var cont by remember { mutableStateOf(0f) }
    var last by remember { mutableStateOf(Float.NaN) }
    var acc by remember { mutableStateOf(SensorManager.SENSOR_STATUS_UNRELIABLE) }
    var has by remember { mutableStateOf(true) }
    val view = LocalView.current

    DisposableEffect(Unit) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensor == null) {
            has = false
            onDispose { }
        } else {
            val l = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent) {
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
                override fun onAccuracyChanged(s: Sensor?, a: Int) { acc = a }
            }
            sm.registerListener(l, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sm.unregisterListener(l) }
        }
    }

    val diff = (((qibla - heading + 540.0) % 360.0) - 180.0)
    val aligned = has && abs(diff) < 2.0
    LaunchedEffect(aligned) {
        if (aligned) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    val sun = sunPosition(co.latitude, co.longitude, System.currentTimeMillis())
    val sunUp = sun.second > 0.0
    val sunRel = ((qibla - sun.first + 540.0) % 360.0) - 180.0

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "اتجاه القبلة الحقيقي في ${Place.name(ctx)}",
            color = sky.ink2, fontSize = 14.sp, textAlign = TextAlign.Center
        )
        if (has) {
            QiblaDial(
                sky, qibla.toFloat(), cont, sun.first.toFloat(), sunUp, aligned,
                if (aligned) "على القبلة" else cardinal(qibla)
            )
            Text(
                if (abs(diff) < 4.0) "أنت متجه نحو القبلة"
                else "أدر الهاتف ${abs(diff).toInt()}° نحو ${if (diff > 0) "اليمين" else "اليسار"}",
                color = ink, fontSize = 20.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
            Text(
                when (acc) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH, SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "دقة المستشعر: جيدة"
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "دقة المستشعر: ضعيفة، حرّك الهاتف على شكل ٨"
                    else -> "دقة المستشعر: غير موثوقة، حرّك الهاتف على شكل ٨"
                },
                color = sky.ink2, fontSize = 13.sp
            )
            Text("امسك الهاتف أفقياً مسطحاً وبعيداً عن المعادن والمغناطيس.", color = sky.ink2, fontSize = 13.sp)
        } else {
            Text("${qibla.toInt()}° (${cardinal(qibla)})", color = ink, fontSize = 34.sp, fontFamily = NotoKufi, fontWeight = FontWeight.Bold)
            Text("لا يوجد مستشعر اتجاه في هذا الهاتف. استعمل الدرجات أعلاه.", color = ink)
        }
        Text(
            if (sunUp) "الشمس الآن عند ${sun.first.toInt()}° (${cardinal(sun.first)}): القبلة على ${if (sunRel > 0) "يمين" else "يسار"} الشمس بنحو ${abs(sunRel).toInt()}°."
            else "الشمس تحت الأفق الآن، فلا يمكن التحقق منها بالعين.",
            color = sky.ink2, fontSize = 13.sp, textAlign = TextAlign.Center
        )
    }
}
