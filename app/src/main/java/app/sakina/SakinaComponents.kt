package app.sakina

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SakinaNavTab(val labelAr: String, val icon: ImageVector) {
    HOME("الرئيسية", Icons.Rounded.Home),
    QIBLA("القبلة", Icons.Rounded.Explore),
    SETTINGS("الإعدادات", Icons.Rounded.Settings)
}

@Composable
fun SakinaBackgroundLayer(
    modifier: Modifier = Modifier,
    showMihrabFrame: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakinaBrandColors.MidnightMihrab)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_sky),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.85f,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xAA0A0E12),
                        0.45f to Color(0x660A0E12),
                        1.0f to Color(0xDD0A0E12)
                    )
                )
        )
        if (showMihrabFrame) {
            Image(
                painter = painterResource(id = R.drawable.bg_mihrab),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                alpha = 0.55f,
                modifier = Modifier.fillMaxSize()
            )
        }
        content()
    }
}

@Composable
fun SakinaGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    glowGold: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val borderBrush = if (glowGold) {
        Brush.verticalGradient(
            listOf(SakinaBrandColors.GoldBright.copy(alpha = 0.75f), SakinaBrandColors.ManuscriptGold.copy(alpha = 0.25f))
        )
    } else {
        Brush.verticalGradient(
            listOf(SakinaBrandColors.ManuscriptGold.copy(alpha = 0.42f), Color(0x22FFFFFF))
        )
    }

    Column(
        modifier = modifier
            .then(
                if (glowGold) Modifier.shadow(16.dp, shape, ambientColor = SakinaBrandColors.ManuscriptGold, spotColor = SakinaBrandColors.ManuscriptGold)
                else Modifier
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(SakinaBrandColors.GlassTop, SakinaBrandColors.GlassBottom)
                )
            )
            .border(0.9.dp, borderBrush, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun SakinaBottomPillBar(
    currentTab: SakinaNavTab,
    onTabSelected: (SakinaNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .navigationBarsPadding()
            .clip(pillShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xD919222C), Color(0xF00B0F15))
                )
            )
            .border(
                0.9.dp,
                Brush.horizontalGradient(
                    listOf(
                        SakinaBrandColors.ManuscriptGold.copy(alpha = 0.25f),
                        SakinaBrandColors.ManuscriptGold.copy(alpha = 0.65f),
                        SakinaBrandColors.ManuscriptGold.copy(alpha = 0.25f)
                    )
                ),
                pillShape
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SakinaNavTab.entries.forEach { tab ->
                val selected = tab == currentTab
                val textColor by animateColorAsState(
                    if (selected) SakinaBrandColors.GoldBright else SakinaBrandColors.SoftSilver,
                    label = "tabColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .then(
                            if (selected) {
                                Modifier.background(
                                    Brush.radialGradient(
                                        listOf(SakinaBrandColors.ManuscriptGold.copy(alpha = 0.24f), Color.Transparent)
                                    )
                                )
                            } else Modifier
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.labelAr,
                            tint = textColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.labelAr,
                            fontFamily = SakinaHeadingFont,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            color = textColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SakinaGoldSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(if (checked) 32.dp else 4.dp, label = "switchThumb")
    val borderBrush = if (checked) {
        Brush.horizontalGradient(listOf(SakinaBrandColors.ManuscriptGold, SakinaBrandColors.GoldBright))
    } else {
        Brush.horizontalGradient(listOf(Color(0x55FFFFFF), Color(0x33FFFFFF)))
    }

    Box(
        modifier = modifier
            .width(64.dp)
            .height(32.dp)
            .clip(CircleShape)
            .background(if (checked) Color(0x44D4AF37) else Color(0x331B2531))
            .border(1.dp, borderBrush, CircleShape)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        if (checked) {
            Text(
                text = "ON",
                fontFamily = SakinaNumeralFont,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = SakinaBrandColors.GoldBright,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .shadow(8.dp, CircleShape, ambientColor = SakinaBrandColors.GoldBright, spotColor = SakinaBrandColors.GoldBright)
                .clip(CircleShape)
                .background(
                    if (checked) Brush.radialGradient(listOf(SakinaBrandColors.GoldBright, SakinaBrandColors.ManuscriptGold))
                    else Brush.radialGradient(listOf(Color(0xFF8B949E), Color(0xFF4B5563)))
                )
        )
    }
}
