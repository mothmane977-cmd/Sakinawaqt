package app.sakina

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TasbihScreen(sky: Sky) {
    var count by remember { mutableIntStateOf(0) }
    val targetCount = 33 // يمكنك تغييره إلى 99 أو 100
    val view = LocalView.current
    
    // تأثير حركة عند الضغط
    val scale by animateFloatAsState(
        targetValue = if (count > 0 && count % targetCount == 0) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f),
        label = "pulse"
    )

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "المسبحة الإلكترونية",
            color = sky.ink,
            fontSize = 24.sp,
            fontFamily = NotoKufi,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // الدائرة التفاعلية
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    count++
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    // اهتزاز أقوى عند إكمال الدورة
                    if (count % targetCount == 0) {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                }
        ) {
            // رسم الحلقة الخارجية
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f - 10.dp.toPx()
                val center = Offset(size.width / 2f, size.height / 2f)
                
                // الخلفية الباهتة
                drawCircle(
                    color = sky.accent.copy(alpha = 0.15f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 8.dp.toPx())
                )
                
                // التقدم (نسبة العدد الحالي إلى العدد المستهدف)
                val progress = (count % targetCount).toFloat() / targetCount
                drawArc(
                    color = sky.accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx())
                )
            }

            // النص داخل الدائرة
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$count",
                    color = sky.ink,
                    fontSize = 72.sp,
                    fontFamily = NotoSans,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اضغط للتسبيح",
                    color = sky.ink2,
                    fontSize = 14.sp,
                    fontFamily = NotoSans
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        // زر إعادة التعيين
        IconButton(
            onClick = { count = 0 },
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        ) {
            Icon(
                Icons.Rounded.Refresh,
                contentDescription = "إعادة تعيين",
                tint = sky.accent,
                modifier = Modifier.size(32.dp)
            )
        }
        Text("إعادة تعيين", color = sky.ink2, fontSize = 12.sp)
    }
}
