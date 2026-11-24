package app.sakina

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CelestialPortalItem(
    val title: String,
    @DrawableRes val iconRes: Int,
    val onClick: () -> Unit
)

enum class CelestialHubMode { NONE, WORSHIP, SETTINGS }

/**
 * Sakina v44 — Celestial Command Dome (بوابة القبة الفلكية العائمة)
 * Revolutionary radial-constellation navigation hub replacing ordinary side drawers:
 * opens with elastic spring bloom, luminous golden mandala core, 2-mode switcher,
 * and floating emerald-gold constellation portal tiles.
 */
@Composable
fun SakinaCelestialCommandHub(
    mode: CelestialHubMode,
    lang: SakinaLang,
    worshipPortals: List<CelestialPortalItem>,
    settingsPortals: List<CelestialPortalItem>,
    onSwitchMode: (CelestialHubMode) -> Unit,
    onDismiss: () -> Unit
) {
    val visible = mode != CelestialHubMode.NONE
    val bloomScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.86f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "celestialBloom"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val activeItems = if (mode == CelestialHubMode.SETTINGS) settingsPortals else worshipPortals
        val hubTitle = if (mode == CelestialHubMode.SETTINGS) {
            sakinaTr(lang, "التحكم والإعدادات", "Control & Settings", "Paramètres", "Kontrol ve Ayarlar", "Pengaturan", "کنٹرول اور ترتیبات")
        } else {
            sakinaTr(lang, "الأذكار والعبادات", "Adhkar & Worship", "Adorations", "Zikir ve İbadetler", "Ibadah & Dzikir", "اذکار اور عبادات")
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xEE0B2A24),
                            Color(0xF4061221),
                            Color(0xFA030912)
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // قوس محراب إسلامي رقيق ومضيء من الذهب الخالص يحيط بالرأس والبطاقات بدل الدائرة
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width * 0.5f
                val archHalfW = size.minDimension * 0.34f
                val topY = size.height * 0.105f
                val shoulderY = topY + archHalfW * 0.86f
                val baseY = size.height * 0.94f

                fun buildMihrabPath(inset: Float): Path {
                    val hw = (archHalfW - inset).coerceAtLeast(24f)
                    val ty = topY + inset * 1.15f
                    val sy = shoulderY + inset * 0.4f
                    val by = baseY - inset * 0.5f
                    return Path().apply {
                        // العمود الأيسر صعوداً نحو كتف المحراب
                        moveTo(cx - hw, by)
                        lineTo(cx - hw, sy)
                        // قوس إسلامي مدبّب رشيق (Ogee / Mihrab Arch) من اليسار إلى القمة
                        cubicTo(
                            cx - hw, sy - hw * 0.54f,
                            cx - hw * 0.36f, ty + hw * 0.22f,
                            cx, ty
                        )
                        // القوس الأيمن نزولاً من القمة إلى الكتف الأيمن
                        cubicTo(
                            cx + hw * 0.36f, ty + hw * 0.22f,
                            cx + hw, sy - hw * 0.54f,
                            cx + hw, sy
                        )
                        // العمود الأيمن نزولاً
                        lineTo(cx + hw, by)
                    }
                }

                val bandW = 11.dp.toPx()
                val outerMihrab = buildMihrabPath(0f)
                val innerMihrab = buildMihrabPath(bandW)
                val haloMihrab = buildMihrabPath(-14.dp.toPx())

                // دالة حساب النقاط بدقة على طول منحنى المحراب لتوزيع الزخارف الإسلامية بين الخطين
                fun sampleSide(inset: Float, isRight: Boolean, u: Float): Offset {
                    val hw = (archHalfW - inset).coerceAtLeast(24f)
                    val ty = topY + inset * 1.15f
                    val sy = shoulderY + inset * 0.4f
                    val by = baseY - inset * 0.5f
                    val sign = if (isRight) 1f else -1f
                    return if (u <= 0.68f) {
                        val t = (u / 0.68f).coerceIn(0f, 1f)
                        Offset(cx + sign * hw, by + (sy - by) * t)
                    } else {
                        val t = ((u - 0.68f) / 0.32f).coerceIn(0f, 1f)
                        val omt = 1f - t
                        val p0x = cx + sign * hw
                        val p0y = sy
                        val p1x = cx + sign * hw
                        val p1y = sy - hw * 0.54f
                        val p2x = cx + sign * hw * 0.36f
                        val p2y = ty + hw * 0.22f
                        val p3x = cx
                        val p3y = ty
                        val x = omt * omt * omt * p0x + 3f * omt * omt * t * p1x + 3f * omt * t * t * p2x + t * t * t * p3x
                        val y = omt * omt * omt * p0y + 3f * omt * omt * t * p1y + 3f * omt * t * t * p2y + t * t * t * p3y
                        Offset(x, y)
                    }
                }

                val pureGoldBrush = Brush.linearGradient(
                    colors = listOf(
                        SakinaLuxuryPalette.GoldDeep.copy(alpha = 0.30f),
                        SakinaLuxuryPalette.GoldRich.copy(alpha = 0.88f),
                        SakinaLuxuryPalette.GoldSpecular,
                        SakinaLuxuryPalette.GoldBright,
                        SakinaLuxuryPalette.GoldRich.copy(alpha = 0.88f),
                        SakinaLuxuryPalette.GoldDeep.copy(alpha = 0.30f)
                    ),
                    start = Offset(cx - archHalfW, baseY),
                    end = Offset(cx + archHalfW, topY)
                )

                val softFadeBrush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to SakinaLuxuryPalette.GoldSpecular.copy(alpha = 0.95f),
                        0.28f to SakinaLuxuryPalette.GoldBright.copy(alpha = 0.85f),
                        0.82f to SakinaLuxuryPalette.GoldRich.copy(alpha = 0.68f),
                        0.96f to SakinaLuxuryPalette.GoldDeep.copy(alpha = 0.28f),
                        1.00f to Color.Transparent
                    ),
                    startY = topY,
                    endY = baseY
                )

                // هالة ذهبية ناعمة خلف شريط المحراب المزخرف
                drawPath(
                    path = outerMihrab,
                    brush = softFadeBrush,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    alpha = 0.14f
                )

                // زخارف هندسية إسلامية متشابكة (معيّنات مزدوجة + حبيبات ذهب خالص) تملأ الشريط بين الخطين
                val ornamentLattice = Path()
                val segCount = 56
                for (isRight in listOf(false, true)) {
                    for (i in 0 until segCount) {
                        val u0 = i.toFloat() / segCount
                        val uMid = (i + 0.5f) / segCount
                        val u1 = (i + 1f) / segCount

                        val o0 = sampleSide(1.5.dp.toPx(), isRight, u0)
                        val oMid = sampleSide(1.5.dp.toPx(), isRight, uMid)
                        val o1 = sampleSide(1.5.dp.toPx(), isRight, u1)
                        val in0 = sampleSide(bandW - 1.5.dp.toPx(), isRight, u0)
                        val inMid = sampleSide(bandW - 1.5.dp.toPx(), isRight, uMid)
                        val in1 = sampleSide(bandW - 1.5.dp.toPx(), isRight, u1)

                        // شبكة معيّنات متشابكة (Zigzag Diamond Filigree) بين الحافتين
                        ornamentLattice.moveTo(o0.x, o0.y)
                        ornamentLattice.lineTo(inMid.x, inMid.y)
                        ornamentLattice.lineTo(o1.x, o1.y)

                        ornamentLattice.moveTo(in0.x, in0.y)
                        ornamentLattice.lineTo(oMid.x, oMid.y)
                        ornamentLattice.lineTo(in1.x, in1.y)

                        // لؤلؤة ذهبية مصغّرة في قلب كل معيّن زخرفي
                        val beadCenter = Offset((oMid.x + inMid.x) * 0.5f, (oMid.y + inMid.y) * 0.5f)
                        val fadeAlpha = ((baseY - beadCenter.y) / (baseY - topY)).coerceIn(0f, 1f)
                        if (fadeAlpha > 0.08f) {
                            drawCircle(
                                color = SakinaLuxuryPalette.GoldSpecular.copy(alpha = fadeAlpha * 0.85f),
                                radius = 1.35.dp.toPx(),
                                center = beadCenter
                            )
                        }
                    }
                }
                drawPath(
                    path = ornamentLattice,
                    brush = softFadeBrush,
                    style = Stroke(width = 0.85.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    alpha = 0.82f
                )

                // خط المحراب الخارجي الذهبي الخالص
                drawPath(
                    path = outerMihrab,
                    brush = pureGoldBrush,
                    style = Stroke(width = 1.65.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // خط المحراب الداخلي الذهبي الخالص
                drawPath(
                    path = innerMihrab,
                    brush = pureGoldBrush,
                    style = Stroke(width = 1.25.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    alpha = 0.88f
                )
                // قوس خارجي أثيري خافت جداً لإبراز العمق
                drawPath(
                    path = haloMihrab,
                    brush = softFadeBrush,
                    style = Stroke(width = 0.75.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    alpha = 0.16f
                )

                // معيّن ذهبي صغير متوهج عند قمة قوس المحراب
                val d = 4.5.dp.toPx()
                val finial = Path().apply {
                    moveTo(cx, topY - d * 1.4f)
                    lineTo(cx + d, topY)
                    lineTo(cx, topY + d * 1.4f)
                    lineTo(cx - d, topY)
                    close()
                }
                drawPath(path = finial, color = SakinaLuxuryPalette.GoldSpecular)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = bloomScale
                        scaleY = bloomScale
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top glowing mode switcher between Worship & Settings Domes
                SakinaLuxuryTabs(
                    tabs = listOf(
                        sakinaTr(lang, "الأذكار والعبادات", "Worship", "Culte", "İbadetler", "Ibadah", "عبادات"),
                        sakinaTr(lang, "التحكم والإعدادات", "Settings", "Réglages", "Ayarlar", "Pengaturan", "ترتیبات")
                    ),
                    selectedIndex = if (mode == CelestialHubMode.SETTINGS) 1 else 0,
                    onSelect = { idx ->
                        onSwitchMode(if (idx == 1) CelestialHubMode.SETTINGS else CelestialHubMode.WORSHIP)
                    },
                    modifier = Modifier.fillMaxWidth(0.88f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // النجمة المثمّنة الذهبية داخل قوس المحراب الرقيق
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_star_medallion_gold),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                    Icon(
                        painter = painterResource(
                            id = if (mode == CelestialHubMode.SETTINGS) R.drawable.ic_sakina_settings else R.drawable.ic_sakina_tasbih
                        ),
                        contentDescription = null,
                        tint = SakinaLuxuryColors.SoftGoldGlow,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = hubTitle,
                    style = TextStyle(
                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SakinaHeadingFont,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Constellation 2-column floating glass portal grid
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val rows = activeItems.chunked(2)
                    rows.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { item ->
                                SakinaGlowingGlassCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(86.dp),
                                    cornerRadius = 20.dp,
                                    emeraldIntensity = 0.36f,
                                    onClick = {
                                        onDismiss()
                                        item.onClick()
                                    }
                                ) {
                                    SakinaLuxuryCornerOverlay(cornerSize = 18.dp, alpha = 0.65f)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.72f))
                                                .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.65f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = item.iconRes),
                                                contentDescription = null,
                                                tint = SakinaLuxuryColors.SoftGoldGlow,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.title,
                                            color = SakinaOrbitalColors.WarmCream,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = SakinaBodyFont,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Floating 3D gold medallion close trigger
                SakinaGoldMedallionButton(
                    medallionBgRes = R.drawable.ic_gold_medallion_btn,
                    onClick = onDismiss,
                    size = 54.dp
                ) {
                    Text(
                        text = "✕",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
