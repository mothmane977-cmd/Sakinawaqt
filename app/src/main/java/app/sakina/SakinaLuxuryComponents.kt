package app.sakina

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object SakinaLuxuryColors {
    val DeepSpaceNavy = Color(0xFF06101F)
    val MidnightNavy = Color(0xFF0A192F)
    val DeepEmerald = Color(0xFF0D3B2E)
    val EmeraldGlow = Color(0xFF1EE3A8)
    val ChampagneGold = Color(0xFFD4AF37)
    val SoftGoldGlow = Color(0xFFF3DFA2)
    val WarmCream = Color(0xFFFAF6EC)
    val InkNavy = Color(0xFF0F1E36)
    val MutedGoldText = Color(0xFF7A6320)
    val GlassNavy = Color(0xB8102442)
}

/**
 * Clean unboxed header with floating gold title and circular glass back button.
 */
@Composable
fun SakinaUnboxedHeader(
    title: String,
    @DrawableRes backIconRes: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    leadingAction: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SakinaLuxuryColors.MidnightNavy.copy(alpha = 0.78f))
                .border(0.9.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.45f), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = backIconRes),
                contentDescription = null,
                tint = SakinaLuxuryColors.SoftGoldGlow,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = title,
            color = SakinaLuxuryColors.SoftGoldGlow,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
        )
        if (leadingAction != null) {
            leadingAction()
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

/**
 * Razor-thin luxury glass card with crisp 1dp gold border and tiny pinpoint diamond
 * corner accents (eliminates blurry horizontal white-green streaks between stacked cards).
 */
@Composable
fun SakinaGlowingGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    emeraldIntensity: Float = 0.34f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardMod = modifier
        .clip(shape)
        .background(
            Brush.linearGradient(
                colors = listOf(
                    SakinaLuxuryColors.DeepEmerald.copy(alpha = emeraldIntensity),
                    SakinaLuxuryColors.MidnightNavy.copy(alpha = 0.88f),
                    SakinaLuxuryColors.DeepSpaceNavy.copy(alpha = 0.94f)
                )
            )
        )
        .drawBehind {
            val r = cornerRadius.toPx()
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.62f),
                        SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.22f),
                        SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.42f)
                    )
                ),
                cornerRadius = CornerRadius(r, r),
                style = Stroke(width = 0.95.dp.toPx())
            )
            // Subtle pinpoint specular dot at top-center (10dp radius, not a wide streak)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.70f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, 0f),
                    radius = 12.dp.toPx()
                ),
                radius = 12.dp.toPx(),
                center = Offset(size.width * 0.5f, 0f)
            )
        }
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    Box(modifier = cardMod) {
        Image(
            painter = painterResource(id = R.drawable.bg_glass_gold_card),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        content()
    }
}

/**
 * Jewel-like metallic gold/emerald toggle switch.
 */
@Composable
fun SakinaJewelSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(66.dp)
            .height(31.dp)
            .clip(RoundedCornerShape(50))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = if (checked) R.drawable.switch_gold_on_bg else R.drawable.switch_gold_off_bg
            ),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Frosted glass segmented tabs with glowing emerald-gold active rim (replaces heavy flat yellow blocks).
 */
@Composable
fun SakinaLuxuryTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = SakinaLuxuryColors.MidnightNavy.copy(alpha = 0.78f),
        border = BorderStroke(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = index == selectedIndex
                val bgColor by animateColorAsState(
                    targetValue = if (selected) SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.88f) else Color.Transparent,
                    label = "tabBg"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (selected) SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.85f) else Color.Transparent,
                    label = "tabBorder"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(50))
                        .clickable { onSelect(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selected) SakinaLuxuryColors.SoftGoldGlow else Color.White.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Ornate 8-pointed Islamic star gold number medallion backed by ic_star_medallion_gold.webp.
 */
@Composable
fun SakinaStarMedallionBadge(
    numberText: String,
    @DrawableRes starBgRes: Int,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = starBgRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        val scaledSp = when {
            numberText.length <= 1 -> 13.sp
            numberText.length == 2 -> 10.5.sp
            else -> 8.5.sp
        }
        Text(
            text = numberText,
            color = SakinaLuxuryColors.SoftGoldGlow,
            fontSize = scaledSp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
    }
}

/**
 * Luminous emerald-gold circular progress ring (for 0/12 worship tracker & Dhikr cards).
 */
@Composable
fun SakinaCircularProgressRing(
    progress: Float,
    centerLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    strokeWidth: Dp = 3.5.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(450),
        label = "ringProgress"
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = Color.White.copy(alpha = 0.12f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            SakinaLuxuryColors.ChampagneGold,
                            SakinaLuxuryColors.EmeraldGlow,
                            SakinaLuxuryColors.SoftGoldGlow
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = stroke
                )
            }
        }
        Text(
            text = centerLabel,
            color = SakinaLuxuryColors.SoftGoldGlow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 3D brushed gold CTA pill button backed by btn_gold_cta_pill.webp.
 */
@Composable
fun SakinaBrushedGoldButton(
    text: String,
    @DrawableRes pillBgRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 58.dp
) {
    val safeHeight = if (height < 54.dp) 54.dp else height
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(safeHeight)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = pillBgRes),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = text,
            color = SakinaLuxuryColors.InkNavy,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 11.dp)
        )
    }
}

/**
 * Beveled 3D gold medallion play/check button backed by ic_gold_medallion_btn.webp.
 */
@Composable
fun SakinaGoldMedallionButton(
    @DrawableRes medallionBgRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = medallionBgRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        content()
    }
}

data class SakinaDockTab(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
)

/**
 * Floating luxury glass bottom navigation dock with animated golden spotlight arc.
 */
@Composable
fun SakinaLuxuryFloatingDock(
    tabs: List<SakinaDockTab>,
    selectedRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(116.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_luxury_floating_dock),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 26.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = tab.route == selectedRoute
                val medSize by animateDpAsState(
                    targetValue = if (selected) 42.dp else 39.dp,
                    label = "dockMedSize"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab.route) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.28f),
                                            SakinaLuxuryColors.EmeraldGlow.copy(alpha = 0.14f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = tab.iconRes),
                            contentDescription = tab.label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(medSize)
                        )
                        Text(
                            text = tab.label,
                            style = TextStyle(
                                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                shadow = SakinaLuxuryPalette.GoldEngravedShadow,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaHeadingFont,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.offset(y = (-2).dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sakina v51 — Universal Curved Mihrab Sanctuary Ribbon (مشكاة الأشرطة المقوّسة)
 * Uses clean bg_mihrab_ribbon_pill + dedicated ic_gold_medallion_btn semantic icon + zero text clipping.
 */
@Composable
fun SakinaMihrabRibbonItem(
    title: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingSymbol: String = "‹"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_mihrab_ribbon_pill),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // v54-2: الميدالية الذهبية ثلاثية الأبعاد الحقيقية الخاصة بكل وظيفة
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(1.0.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.72f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = SakinaLuxuryColors.SoftGoldGlow,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SakinaBodyFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = true))
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = SakinaLuxuryColors.WarmCream.copy(alpha = 0.72f),
                        fontSize = 11.sp,
                        fontFamily = SakinaBodyFont,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = true))
                    )
                }
            }

            Text(
                text = trailingSymbol,
                color = SakinaLuxuryColors.SoftGoldGlow.copy(alpha = 0.88f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}


/**
 * زر ذهبي معدني دائري بارز حقيقي (v69):
 * ينخفض فعلياً إلى الداخل عند الضغط ثم يعود بنابض ناعم — موحّد لأزرار الرجوع والإعدادات واللغة في كامل التطبيق.
 */
@Composable
fun SakinaTactileGoldCoinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    drawableRes: Int? = R.drawable.ic_quran_back_gold_3d,
    contentDescription: String? = null,
    content: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.87f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.55f),
        label = "goldCoinScale"
    )
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) 2.2f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "goldCoinOffset"
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = pressOffsetY * density
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (content != null) {
            content()
        }
    }
}

/**
 * شريط تمرير/تقدم ذهبي معدني لامع ثلاثي الأبعاد (v69 — مطابق للقطة 22686.jpg):
 * مسار زجاجي غائر + خط تقدم ذهبي متوهج + كرة ذهبية معدنية ثلاثية الأبعاد بلمعة مضيئة.
 */
@Composable
fun SakinaGold3DSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val clamped = value.coerceIn(0f, 1f)
    Canvas(
        modifier = modifier
            .height(26.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onValueChange(frac)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val frac = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onValueChange(frac)
                }
            }
    ) {
        val cy = size.height / 2f
        val trackH = 6.dp.toPx()
        val thumbR = 9.5.dp.toPx()
        val leftX = thumbR
        val rightX = (size.width - thumbR).coerceAtLeast(leftX + 1f)
        val usableW = (rightX - leftX).coerceAtLeast(1f)
        val activeX = leftX + usableW * clamped

        // 1. المسار الخلفي الزجاجي الغائر بحافة خفيفة
        drawRoundRect(
            color = Color(0x661C3434),
            topLeft = Offset(leftX, cy - trackH / 2f),
            size = Size(usableW, trackH),
            cornerRadius = CornerRadius(trackH / 2f, trackH / 2f)
        )

        // 2. توهج ذهبي ناعم حول شريط التقدم النشط
        if (clamped > 0.005f) {
            drawRoundRect(
                color = Color(0x44F5DC8E),
                topLeft = Offset(leftX, cy - trackH * 0.9f),
                size = Size((activeX - leftX).coerceAtLeast(trackH), trackH * 1.8f),
                cornerRadius = CornerRadius(trackH, trackH)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF0B8), Color(0xFFE7C25D), Color(0xFFB88A2E)),
                    startY = cy - trackH / 2f,
                    endY = cy + trackH / 2f
                ),
                topLeft = Offset(leftX, cy - trackH / 2f),
                size = Size((activeX - leftX).coerceAtLeast(trackH), trackH),
                cornerRadius = CornerRadius(trackH / 2f, trackH / 2f)
            )
        }

        // 3. الكرة الذهبية المعدنية ثلاثية الأبعاد (Thumb)
        drawCircle(
            color = Color(0x55F7DE8F),
            radius = thumbR * 1.38f,
            center = Offset(activeX, cy)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF6CE), Color(0xFFE4BC58), Color(0xFF9E701E)),
                center = Offset(activeX - thumbR * 0.3f, cy - thumbR * 0.3f),
                radius = thumbR * 1.25f
            ),
            radius = thumbR,
            center = Offset(activeX, cy)
        )
        drawCircle(
            color = Color(0xCCFFF8DC),
            radius = thumbR * 0.32f,
            center = Offset(activeX - thumbR * 0.32f, cy - thumbR * 0.32f)
        )
    }
}
