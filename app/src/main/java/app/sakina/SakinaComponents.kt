package app.sakina

import androidx.annotation.DrawableRes
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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SakinaNavTab(val labelAr: String, val icon: ImageVector) {
    HOME("الرئيسية", Icons.Rounded.Home),
    QIBLA("القبلة", Icons.Rounded.Explore),
    SETTINGS("الإعدادات", Icons.Rounded.Settings)
}

val Sakina3DGoldBevelBrush = Brush.linearGradient(
    listOf(
        Color(0xFF5C4720),
        Color(0xFFC5A059),
        Color(0xFFE6CF94),
        Color(0xFF9E7B38),
        Color(0xFF4D3B1A)
    )
)

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
                alpha = 0.78f,
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
    val borderBrush = if (glowGold) Sakina3DGoldBevelBrush
    else Brush.verticalGradient(listOf(Color(0x66C5A059), Color(0x22FFFFFF)))

    Column(
        modifier = modifier
            .then(
                if (glowGold) Modifier.shadow(14.dp, shape, ambientColor = Color(0xFFC5A059), spotColor = Color(0xFFC5A059))
                else Modifier
            )
            .clip(shape)
            .background(Brush.verticalGradient(listOf(SakinaBrandColors.GlassTop, SakinaBrandColors.GlassBottom)))
            .border(0.65.dp, borderBrush, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun SakinaDrawerPillItem(
    @DrawableRes iconRes: Int,
    title: String,
    badge: String = "",
    onClick: () -> Unit
) {
    val pillShape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(pillShape)
            .background(Brush.verticalGradient(listOf(Color(0xE01A2430), Color(0xF50B0F15))))
            .border(0.65.dp, Sakina3DGoldBevelBrush, pillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0x33C5A059), Color(0x18131B22))))
                    .border(0.65.dp, Sakina3DGoldBevelBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SakinaBrandColors.LuminousIvory,
                maxLines = 1
            )
        }
        if (badge.isNotEmpty()) {
            Text(
                text = badge,
                fontFamily = SakinaBodyFont,
                fontSize = 11.sp,
                color = Color(0xFFE6CF94),
                maxLines = 1
            )
        }
    }
}

@Composable
fun SakinaBottomPillBar(
    currentTab: SakinaNavTab,
    onTabSelected: (SakinaNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .height(46.dp)
            .clip(pillShape)
            .background(Brush.verticalGradient(listOf(Color(0xEC141C25), Color(0xF8080B10))))
            .border(0.65.dp, Sakina3DGoldBevelBrush, pillShape)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SakinaNavTab.entries.forEach { tab ->
            val selected = tab == currentTab
            val tint by animateColorAsState(
                if (selected) Color(0xFFE6CF94) else SakinaBrandColors.SoftSilver,
                label = "tabColor"
            )
            val tabShape = RoundedCornerShape(18.dp)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(tabShape)
                    .background(
                        if (selected) Brush.verticalGradient(listOf(Color(0x30C5A059), Color(0x120A0E12)))
                        else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .border(
                        0.65.dp,
                        if (selected) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)),
                        tabShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(tab) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.labelAr,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = tab.labelAr,
                    fontFamily = SakinaHeadingFont,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp,
                    color = tint,
                    maxLines = 1,
                    softWrap = false
                )
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
    val thumbOffset by animateDpAsState(if (checked) 36.dp else 4.dp, label = "switchThumb")
    val borderBrush = if (checked) Sakina3DGoldBevelBrush
    else Brush.horizontalGradient(listOf(Color(0x55FFFFFF), Color(0x33FFFFFF)))

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .width(66.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(if (checked) Color(0x38C5A059) else Color(0x331B2531))
                .border(0.8.dp, borderBrush, CircleShape)
                .clickable { onCheckedChange(!checked) },
            contentAlignment = Alignment.CenterStart
        ) {
            if (checked) {
                Text(
                    text = "ON",
                    fontFamily = SakinaNumeralFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFFE6CF94),
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(24.dp)
                    .shadow(8.dp, CircleShape, ambientColor = Color(0xFFC5A059), spotColor = Color(0xFFC5A059))
                    .clip(CircleShape)
                    .background(
                        if (checked) Brush.radialGradient(listOf(Color(0xFFFFF4C2), Color(0xFFE6CF94), Color(0xFFC5A059)))
                        else Brush.radialGradient(listOf(Color(0xFF8B949E), Color(0xFF4B5563)))
                    )
            )
        }
    }
}
