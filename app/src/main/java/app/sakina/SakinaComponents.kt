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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class SakinaNavTab(@DrawableRes val iconRes: Int) {
    HOME(R.drawable.ic_mosque),
    QIBLA(R.drawable.ic_kaaba),
    QURAN(R.drawable.ic_adhkar),
    ADHKAR(R.drawable.ic_tasbih),
    CALENDAR(R.drawable.ic_calendar);

    fun label(s: SakinaStrings): String = when (this) {
        HOME -> s.tabHome
        QIBLA -> s.tabQibla
        QURAN -> s.tabQuran
        ADHKAR -> s.tabAdhkar
        CALENDAR -> s.tabCalendar
    }
}

val SakinaInkDark = Color(0xFF152230)

val Sakina3DGoldBevelBrush = Brush.linearGradient(
    listOf(Color(0xFF6B5224), Color(0xFFC5A059), Color(0xFFFFF2C2), Color(0xFFC5A059), Color(0xFF5C441B))
)

fun String.toWesternDigits(): String {
    val hindi = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val western = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    var out = this
    for (i in hindi.indices) out = out.replace(hindi[i], western[i])
    return out
}

@Composable
fun SakinaTitle3D(
    text: String,
    fontSize: TextUnit = 21.sp,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontFamily = SakinaEditorialFont,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFFF7D6), Color(0xFFE6CF94), Color(0xFFC5A059), Color(0xFF8C6B28))
            ),
            shadow = Shadow(color = Color(0xDD000000), offset = Offset(0f, 3f), blurRadius = 6f),
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun SakinaAnalogClockDial(
    timeMs: Long,
    size: Dp = 46.dp,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r = this.size.minDimension / 2f - 2.dp.toPx()

        drawCircle(color = theme.bgDeep, radius = r, center = Offset(cx, cy))
        drawCircle(brush = Sakina3DGoldBevelBrush, radius = r, center = Offset(cx, cy), style = Stroke(width = 1.8.dp.toPx()))

        for (i in 0 until 12) {
            val ang = (i * 30.0 - 90.0) * PI / 180.0
            val innerR = if (i % 3 == 0) r * 0.72f else r * 0.83f
            drawLine(
                color = theme.accentBright,
                start = Offset(cx + (innerR * cos(ang)).toFloat(), cy + (innerR * sin(ang)).toFloat()),
                end = Offset(cx + (r * 0.92f * cos(ang)).toFloat(), cy + (r * 0.92f * sin(ang)).toFloat()),
                strokeWidth = if (i % 3 == 0) 1.5.dp.toPx() else 0.9.dp.toPx()
            )
        }

        val cal = Calendar.getInstance().apply { timeInMillis = timeMs }
        val sec = cal.get(Calendar.SECOND)
        val min = cal.get(Calendar.MINUTE) + sec / 60f
        val hr = (cal.get(Calendar.HOUR) % 12) + min / 60f

        val hrAng = (hr * 30.0 - 90.0) * PI / 180.0
        val minAng = (min * 6.0 - 90.0) * PI / 180.0
        val secAng = (sec * 6.0 - 90.0) * PI / 180.0

        drawLine(
            color = theme.accentBright,
            start = Offset(cx, cy),
            end = Offset(cx + (r * 0.48f * cos(hrAng)).toFloat(), cy + (r * 0.48f * sin(hrAng)).toFloat()),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = SakinaBrandColors.LuminousIvory,
            start = Offset(cx, cy),
            end = Offset(cx + (r * 0.68f * cos(minAng)).toFloat(), cy + (r * 0.68f * sin(minAng)).toFloat()),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = theme.accentGold,
            start = Offset(cx, cy),
            end = Offset(cx + (r * 0.76f * cos(secAng)).toFloat(), cy + (r * 0.76f * sin(secAng)).toFloat()),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(color = theme.accentBright, radius = 2.4.dp.toPx(), center = Offset(cx, cy))
    }
}

// Ornate Islamic Hexagon Badge for Surah Numbers (#3)
@Composable
fun SakinaHexagonNumberBadge(
    number: Int,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val rOuter = this.size.minDimension / 2f - 1.5.dp.toPx()
            val rInner = rOuter * 0.80f

            val outerHex = Path().apply {
                for (i in 0 until 6) {
                    val ang = (i * 60.0 - 90.0) * PI / 180.0
                    val x = cx + (rOuter * cos(ang)).toFloat()
                    val y = cy + (rOuter * sin(ang)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            val innerHex = Path().apply {
                for (i in 0 until 6) {
                    val ang = (i * 60.0 - 90.0) * PI / 180.0
                    val x = cx + (rInner * cos(ang)).toFloat()
                    val y = cy + (rInner * sin(ang)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            drawPath(outerHex, color = theme.bgDeep)
            drawPath(outerHex, brush = Sakina3DGoldBevelBrush, style = Stroke(width = 1.6.dp.toPx()))
            drawPath(innerHex, color = Color(0x55C5A059), style = Stroke(width = 0.8.dp.toPx()))

            // Ornate gold pearls at the 6 hexagon vertices
            for (i in 0 until 6) {
                val ang = (i * 60.0 - 90.0) * PI / 180.0
                val vx = cx + (rOuter * cos(ang)).toFloat()
                val vy = cy + (rOuter * sin(ang)).toFloat()
                drawCircle(color = theme.accentBright, radius = 1.3.dp.toPx(), center = Offset(vx, vy))
            }
        }
        Text(
            text = "$number".toWesternDigits(),
            fontFamily = SakinaNumeralFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            color = theme.accentBright,
            textAlign = TextAlign.Center,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
    }
}

@Composable
fun SakinaBackIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    Box(
        modifier = modifier
            .size(34.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(theme.bgCard, theme.bgDeep)))
            .border(1.dp, Sakina3DGoldBevelBrush, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = null,
            tint = theme.accentBright,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SakinaStarPolygonBadge(
    @DrawableRes iconRes: Int,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val rOuter = this.size.minDimension / 2f
            val rInner = rOuter * 0.80f
            val starPath = Path().apply {
                for (i in 0 until 16) {
                    val ang = (i * 22.5 - 90.0) * PI / 180.0
                    val r = if (i % 2 == 0) rOuter else rInner
                    val x = cx + (r * cos(ang)).toFloat()
                    val y = cy + (r * sin(ang)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(starPath, color = theme.bgDeep)
            drawPath(starPath, brush = Sakina3DGoldBevelBrush, style = Stroke(width = 1.3.dp.toPx()))
        }
        // Enlarged inner icon ratio (0.68f) so prayer icons are clearly visible (#19)
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(size * 0.68f)
        )
    }
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
            Image(
                painter = painterResource(id = R.drawable.bg_mihrab),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                alpha = 0.86f,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun SakinaOrnamentCartouche(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 36.dp, vertical = 10.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .widthIn(min = 140.dp, max = 268.dp)
            .defaultMinSize(minHeight = 46.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_cartouche),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
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
    Box(
        modifier = modifier
            .shadow(10.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(theme.parchmentTop, theme.parchmentBottom)))
            .drawBehind {
                val step = 20.dp.toPx()
                var x = -size.height
                while (x < size.width + size.height) {
                    drawLine(Color(0x0FA67C2E), Offset(x, 0f), Offset(x + size.height, size.height), strokeWidth = 0.7.dp.toPx())
                    drawLine(Color(0x0FA67C2E), Offset(x + size.height, 0f), Offset(x, size.height), strokeWidth = 0.7.dp.toPx())
                    x += step
                }
            }
            .border(1.4.dp, Sakina3DGoldBevelBrush, shape)
    ) {
        val cSize = 22.dp
        Image(
            painter = painterResource(id = R.drawable.ic_corner_arabesque),
            contentDescription = null,
            alpha = 0.88f,
            modifier = Modifier.size(cSize).align(AbsoluteAlignment.TopLeft).padding(2.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_corner_arabesque),
            contentDescription = null,
            alpha = 0.88f,
            modifier = Modifier.size(cSize).align(AbsoluteAlignment.TopRight).padding(2.dp).graphicsLayer { rotationZ = 90f }
        )
        Image(
            painter = painterResource(id = R.drawable.ic_corner_arabesque),
            contentDescription = null,
            alpha = 0.88f,
            modifier = Modifier.size(cSize).align(AbsoluteAlignment.BottomRight).padding(2.dp).graphicsLayer { rotationZ = 180f }
        )
        Image(
            painter = painterResource(id = R.drawable.ic_corner_arabesque),
            contentDescription = null,
            alpha = 0.88f,
            modifier = Modifier.size(cSize).align(AbsoluteAlignment.BottomLeft).padding(2.dp).graphicsLayer { rotationZ = 270f }
        )

        Column(modifier = Modifier.fillMaxWidth().padding(contentPadding), content = content)
    }
}

@Composable
fun SakinaGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    glowGold: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalSakinaTheme.current
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val borderBrush = if (glowGold) Sakina3DGoldBevelBrush
    else Brush.verticalGradient(listOf(theme.accentGold.copy(alpha = 0.55f), Color(0x22FFFFFF)))

    Column(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(theme.bgCard.copy(alpha = 0.92f), theme.bgDeep.copy(alpha = 0.96f))))
            .border(1.dp, borderBrush, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun SakinaDrawerPillItem(
    @DrawableRes iconRes: Int,
    title: String,
    onClick: () -> Unit
) {
    val theme = LocalSakinaTheme.current
    val pillShape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(pillShape)
            .background(Brush.verticalGradient(listOf(theme.bgCard, theme.bgDeep)))
            .border(0.9.dp, Sakina3DGoldBevelBrush, pillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Larger icon (26.dp) for clear visibility (#19)
        Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontFamily = SakinaEditorialFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = SakinaBrandColors.LuminousIvory,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SakinaBottomPillBar(
    currentTab: SakinaNavTab,
    onTabSelected: (SakinaNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val barShape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .height(62.dp)
            .clip(barShape)
            .background(Brush.verticalGradient(listOf(theme.bgCard.copy(alpha = 0.96f), theme.bgDeep)))
            .border(1.dp, Sakina3DGoldBevelBrush, barShape)
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
                    .padding(vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Enlarged tab bar icon (24.dp) (#19)
                Image(painter = painterResource(id = tab.iconRes), contentDescription = tab.label(s), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tab.label(s),
                    fontFamily = SakinaHeadingFont,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp,
                    lineHeight = 11.5.sp,
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
                .height(30.dp)
                .clip(CircleShape)
                .background(if (checked) theme.accentGold.copy(alpha = 0.24f) else Color(0x331B2531))
                .border(0.8.dp, borderBrush, CircleShape)
                .clickable { onCheckedChange(!checked) },
            contentAlignment = Alignment.CenterStart
        ) {
            if (checked) {
                Text("ON", fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = theme.accentBright, modifier = Modifier.padding(start = 10.dp))
            }
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(22.dp)
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
