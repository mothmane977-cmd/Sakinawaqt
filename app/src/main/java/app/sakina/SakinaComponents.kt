package app.sakina

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SakinaNavTab(val labelAr: String, @DrawableRes val iconRes: Int) {
    HOME("الرئيسية", R.drawable.ic_mosque),
    QIBLA("القبلة", R.drawable.ic_kaaba),
    QURAN("القرآن", R.drawable.ic_adhkar),
    ADHKAR("الأذكار", R.drawable.ic_tasbih),
    CALENDAR("التقويم", R.drawable.ic_calendar)
}

val SakinaRoyalNavy = Color(0xFF071422)
val SakinaRoyalNavyCard = Color(0xFF0B1C2E)
val SakinaParchmentTop = Color(0xFFF9F4E8)
val SakinaParchmentBottom = Color(0xFFEDE3CC)
val SakinaInkDark = Color(0xFF152230)

val Sakina3DGoldBevelBrush = Brush.linearGradient(
    listOf(
        Color(0xFF6B5224),
        Color(0xFFC5A059),
        Color(0xFFF0DAA0),
        Color(0xFFC5A059),
        Color(0xFF5C441B)
    )
)

fun String.toWesternDigits(): String {
    val hindi = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val western = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    var out = this
    for (i in hindi.indices) out = out.replace(hindi[i], western[i])
    return out
}

@Composable
fun SakinaBackgroundLayer(
    modifier: Modifier = Modifier,
    @DrawableRes headerImageRes: Int = R.drawable.bg_header_mosque,
    showMihrabFrame: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakinaRoyalNavy)
    ) {
        Image(
            painter = painterResource(id = headerImageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.46f)
                .align(Alignment.TopCenter)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.48f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x55071422),
                        0.55f to Color(0x44071422),
                        0.88f to Color(0xE0071422),
                        1.0f to SakinaRoyalNavy
                    )
                )
        )

        content()

        if (showMihrabFrame) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val w = size.width
                val h = size.height
                val archShoulderY = 92.dp.toPx()
                val apexY = 6.dp.toPx()
                val cornerR = 26.dp.toPx()

                val framePath = Path().apply {
                    moveTo(w * 0.5f, apexY)
                    cubicTo(w * 0.65f, apexY + 18.dp.toPx(), w * 0.72f, archShoulderY - 12.dp.toPx(), w * 0.96f, archShoulderY)
                    quadraticBezierTo(w, archShoulderY + 8.dp.toPx(), w, archShoulderY + 26.dp.toPx())
                    lineTo(w, h - cornerR)
                    quadraticBezierTo(w, h, w - cornerR, h)
                    lineTo(cornerR, h)
                    quadraticBezierTo(0f, h, 0f, h - cornerR)
                    lineTo(0f, archShoulderY + 26.dp.toPx())
                    quadraticBezierTo(0f, archShoulderY + 8.dp.toPx(), w * 0.04f, archShoulderY)
                    cubicTo(w * 0.28f, archShoulderY - 12.dp.toPx(), w * 0.35f, apexY + 18.dp.toPx(), w * 0.5f, apexY)
                    close()
                }
                drawPath(path = framePath, color = Color(0x33E6CF94), style = Stroke(width = 3.5.dp.toPx()))
                drawPath(path = framePath, brush = Sakina3DGoldBevelBrush, style = Stroke(width = 1.4.dp.toPx()))
            }
        }
    }
}

@Composable
fun SakinaOrnamentCartouche(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val tip = 12.dp.toPx()
            val path = Path().apply {
                moveTo(tip, 0f); lineTo(w - tip, 0f); lineTo(w, h * 0.5f)
                lineTo(w - tip, h); lineTo(tip, h); lineTo(0f, h * 0.5f); close()
            }
            drawPath(path = path, brush = Brush.verticalGradient(listOf(Color(0xEB0E2238), Color(0xF5071422))))
            drawPath(path = path, brush = Sakina3DGoldBevelBrush, style = Stroke(width = 1.dp.toPx()))
            val d = 3.5.dp.toPx()
            listOf(Offset(tip * 0.45f, h * 0.5f), Offset(w - tip * 0.45f, h * 0.5f)).forEach { c ->
                val dpPath = Path().apply {
                    moveTo(c.x, c.y - d); lineTo(c.x + d, c.y); lineTo(c.x, c.y + d); lineTo(c.x - d, c.y); close()
                }
                drawPath(dpPath, color = Color(0xFFE6CF94))
            }
        }
        Box(modifier = Modifier.padding(contentPadding), contentAlignment = Alignment.Center, content = content)
    }
}

@Composable
fun SakinaParchmentPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .shadow(10.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(SakinaParchmentTop, SakinaParchmentBottom)))
            .border(1.dp, Sakina3DGoldBevelBrush, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun SakinaGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    glowGold: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val borderBrush = if (glowGold) Sakina3DGoldBevelBrush
    else Brush.verticalGradient(listOf(Color(0x66C5A059), Color(0x22FFFFFF)))

    Column(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xE80E2238), Color(0xF5071422))))
            .border(0.85.dp, borderBrush, shape)
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
            .background(Brush.verticalGradient(listOf(Color(0xE80E2238), Color(0xF5071422))))
            .border(0.8.dp, Sakina3DGoldBevelBrush, pillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SakinaBrandColors.LuminousIvory)
        }
        if (badge.isNotEmpty()) {
            Text(badge.toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 11.sp, color = Color(0xFFE6CF94))
        }
    }
}

@Composable
fun SakinaBottomPillBar(
    currentTab: SakinaNavTab,
    onTabSelected: (SakinaNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .navigationBarsPadding()
            .height(54.dp)
            .clip(barShape)
            .background(Brush.verticalGradient(listOf(Color(0xF20C1E33), Color(0xFA06101B))))
            .border(0.85.dp, Sakina3DGoldBevelBrush, barShape)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SakinaNavTab.entries.forEach { tab ->
            val selected = tab == currentTab
            val textColor by animateColorAsState(if (selected) SakinaRoyalNavy else Color(0xFFD8C288), label = "tabColor")
            val tabShape = RoundedCornerShape(14.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(tabShape)
                    .background(
                        if (selected) Brush.verticalGradient(listOf(Color(0xFFF0DAA0), Color(0xFFC5A059)))
                        else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onTabSelected(tab) }
                    .padding(vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(painter = painterResource(id = tab.iconRes), contentDescription = tab.labelAr, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tab.labelAr,
                    fontFamily = SakinaHeadingFont,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp,
                    color = textColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SakinaGoldSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val thumbOffset by animateDpAsState(if (checked) 36.dp else 4.dp, label = "switchThumb")
    val borderBrush = if (checked) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color(0x55FFFFFF), Color(0x33FFFFFF)))

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
                Text("ON", fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFE6CF94), modifier = Modifier.padding(start = 10.dp))
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
