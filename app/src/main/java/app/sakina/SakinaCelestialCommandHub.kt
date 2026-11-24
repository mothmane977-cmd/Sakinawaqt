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
            // Luminous celestial mandala geometry behind the constellation
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width * 0.5f, size.height * 0.24f)
                val r1 = size.minDimension * 0.28f
                val r2 = size.minDimension * 0.44f
                drawGlowingGoldOval(
                    topLeft = Offset(center.x - r1, center.y - r1),
                    size = Size(r1 * 2f, r1 * 2f),
                    coreStrokePx = 1.4.dp.toPx(),
                    alphaScale = 0.55f
                )
                drawCircle(
                    color = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.14f),
                    radius = r2,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.9.dp.toPx())
                )
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

                // شعار القبة الإسلامية ثلاثية الأبعاد المرصّعة بالذهب (بنفس خامة النجمة المثمنة)
                Image(
                    painter = painterResource(id = R.drawable.ic_golden_dome_3d),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(86.dp)
                )

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
