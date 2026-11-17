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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
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
val SakinaInkDark = Color(0xFF152230)

val Sakina3DGoldBevelBrush = Brush.linearGradient(
    listOf(Color(0xFF6B5224), Color(0xFFC5A059), Color(0xFFFFF1C2), Color(0xFFC5A059), Color(0xFF5C441B))
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
    val theme = LocalSakinaTheme.current
    val deep = theme.bgDeep
    Box(modifier = modifier.fillMaxSize().background(deep)) {
        Image(
            painter = painterResource(id = headerImageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.44f).align(Alignment.TopCenter)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.46f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        0.0f to deep.copy(alpha = 0.35f),
                        0.55f to deep.copy(alpha = 0.25f),
                        0.88f to deep.copy(alpha = 0.90f),
                        1.0f to deep
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
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                val w = size.width
                val h = size.height
                val archShoulderY = 54.dp.toPx()
                val apexY = 4.dp.toPx()
                val cornerR = 24.dp.toPx()

                fun buildArchPath(inset: Float): Path = Path().apply {
                    val left = inset
                    val right = w - inset
                    val top = apexY + inset
                    val shY = archShoulderY + inset * 0.45f
                    val bot = h - inset
                    val r = (cornerR - inset).coerceAtLeast(8f)

                    moveTo(w * 0.5f, top)
                    cubicTo(w * 0.62f, top + 16.dp.toPx(), w * 0.78f, shY - 12.dp.toPx(), right - 10.dp.toPx(), shY)
                    quadraticBezierTo(right, shY + 6.dp.toPx(), right, shY + 22.dp.toPx())
                    lineTo(right, bot - r)
                    quadraticBezierTo(right, bot, right - r, bot)
                    lineTo(left + r, bot)
                    quadraticBezierTo(left, bot, left, bot - r)
                    lineTo(left, shY + 22.dp.toPx())
                    quadraticBezierTo(left, shY + 6.dp.toPx(), left + 10.dp.toPx(), shY)
                    cubicTo(w * 0.22f, shY - 12.dp.toPx(), w * 0.38f, top + 16.dp.toPx(), w * 0.5f, top)
                    close()
                }

                val outerPath = buildArchPath(0f)
                val midPath = buildArchPath(3.5.dp.toPx())
                val innerPath = buildArchPath(6.5.dp.toPx())

                // 3D Engraved Gold Mihrab Arch (thicker & sculptural at top)
                drawPath(outerPath, color = Color(0x66000000), style = Stroke(width = 7.dp.toPx()))
                drawPath(outerPath, brush = Sakina3DGoldBevelBrush, style = Stroke(width = 3.8.dp.toPx()))
                drawPath(outerPath, color = Color(0x88FFF6D1), style = Stroke(width = 1.1.dp.toPx()))
                drawPath(midPath, brush = Sakina3DGoldBevelBrush, style = Stroke(width = 1.4.dp.toPx()))
                drawPath(innerPath, color = Color(0x77C5A059), style = Stroke(width = 0.8.dp.toPx()))

                // Ornamental 3D Gold Crown Finial at the top apex
                val d = 6.5.dp.toPx()
                val apex = Path().apply {
                    moveTo(w * 0.5f, apexY - d)
                    lineTo(w * 0.5f + d, apexY + 4.dp.toPx())
                    lineTo(w * 0.5f, apexY + d + 8.dp.toPx())
                    lineTo(w * 0.5f - d, apexY + 4.dp.toPx())
                    close()
                }
                drawPath(apex, brush = Sakina3DGoldBevelBrush)
                drawPath(apex, color = Color(0xAAFFF6D1), style = Stroke(width = 0.8.dp.toPx()))
            }
        }
    }
}

@Composable
fun SakinaOrnamentCartouche(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val theme = LocalSakinaTheme.current
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val tip = 14.dp.toPx()
            val path = Path().apply {
                moveTo(tip, 0f); lineTo(w - tip, 0f); lineTo(w, h * 0.5f)
                lineTo(w - tip, h); lineTo(tip, h); lineTo(0f, h * 0.5f); close()
            }
            drawPath(path = path, brush = Brush.verticalGradient(listOf(theme.bgCard.copy(alpha = 0.94f), theme.bgDeep.copy(alpha = 0.96f))))
            drawPath(path = path, brush = Sakina3DGoldBevelBrush, style = Stroke(width = 1.5.dp.toPx()))
            val d = 4.dp.toPx()
            listOf(Offset(tip * 0.45f, h * 0.5f), Offset(w - tip * 0.45f, h * 0.5f)).forEach { c ->
                val dpPath = Path().apply {
                    moveTo(c.x, c.y - d); lineTo(c.x + d, c.y); lineTo(c.x, c.y + d); lineTo(c.x - d, c.y); close()
                }
                drawPath(dpPath, brush = Sakina3DGoldBevelBrush)
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
    val theme = LocalSakinaTheme.current
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .shadow(10.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(theme.parchmentTop, theme.parchmentBottom)))
            .border(1.1.dp, Sakina3DGoldBevelBrush, shape)
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
    val theme = LocalSakinaTheme.current
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val borderBrush = if (glowGold) Sakina3DGoldBevelBrush
    else Brush.verticalGradient(listOf(theme.accentGold.copy(alpha = 0.5f), Color(0x22FFFFFF)))

    Column(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(theme.bgCard.copy(alpha = 0.92f), theme.bgDeep.copy(alpha = 0.96f))))
            .border(0.9.dp, borderBrush, shape)
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
    val theme = LocalSakinaTheme.current
    val pillShape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(pillShape)
            .background(Brush.verticalGradient(listOf(theme.bgCard, theme.bgDeep)))
            .border(0.85.dp, Sakina3DGoldBevelBrush, pillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SakinaBrandColors.LuminousIvory)
        }
        if (badge.isNotEmpty()) {
            Text(badge.toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 10.5.sp, color = theme.accentBright)
        }
    }
}

@Composable
fun SakinaBottomPillBar(
    currentTab: SakinaNavTab,
    onTabSelected: (SakinaNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    val barShape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .navigationBarsPadding()
            .height(62.dp)
            .clip(barShape)
            .background(Brush.verticalGradient(listOf(theme.bgCard.copy(alpha = 0.96f), theme.bgDeep)))
            .border(0.9.dp, Sakina3DGoldBevelBrush, barShape)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SakinaNavTab.entries.forEach { tab ->
            val selected = tab == currentTab
            val textColor by animateColorAsState(if (selected) theme.bgDeep else theme.accentBright, label = "tabColor")
            val tabShape = RoundedCornerShape(14.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(tabShape)
                    .background(
                        if (selected) Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold))
                        else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onTabSelected(tab) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(painter = painterResource(id = tab.iconRes), contentDescription = tab.labelAr, modifier = Modifier.size(19.dp))
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = tab.labelAr,
                    fontFamily = SakinaHeadingFont,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    color = textColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }
        }
    }
}

@Composable
fun SakinaGoldSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val thumbOffset by animateDpAsState(if (checked) 36.dp else 4.dp, label = "switchThumb")
    val borderBrush = if (checked) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color(0x55FFFFFF), Color(0x33FFFFFF)))

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .width(66.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(if (checked) theme.accentGold.copy(alpha = 0.24f) else Color(0x331B2531))
                .border(0.8.dp, borderBrush, CircleShape)
                .clickable { onCheckedChange(!checked) },
            contentAlignment = Alignment.CenterStart
        ) {
            if (checked) {
                Text("ON", fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = theme.accentBright, modifier = Modifier.padding(start = 10.dp))
            }
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(24.dp)
                    .shadow(8.dp, CircleShape, ambientColor = theme.accentGold, spotColor = theme.accentGold)
                    .clip(CircleShape)
                    .background(
                        if (checked) Brush.radialGradient(listOf(Color(0xFFFFF4C2), theme.accentBright, theme.accentGold))
                        else Brush.radialGradient(listOf(Color(0xFF8B949E), Color(0xFF4B5563)))
                    )
            )
        }
    }
}
