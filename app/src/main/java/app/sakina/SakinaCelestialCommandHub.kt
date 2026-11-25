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
                .background(SakinaLuxuryPalette.ObsidianNavy.copy(alpha = 0.96f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            SakinaAmbientLuxuryBackdrop(showGeometricPattern = true, showStarDust = true)

            // v49 #6: قوس محراب إسلامي منقوش بالذهب الخالص عالي الدقة (مطابق للمعاينة)
            Image(
                painter = painterResource(id = R.drawable.bg_mihrab_arch_ornate),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 66.dp, bottom = 8.dp, start = 0.dp, end = 0.dp)
            )

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

                Spacer(modifier = Modifier.height(26.dp))

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

                // v51 Option 3: مشكاة الأشرطة المقوّسة (مطابقة للمعاينة الثالثة بعرض متدرّج داخل باب المحراب)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (idx in activeItems.indices) {
                        val item = activeItems[idx]
                        // تدرّج عرض رشيق يتبع انحناء قوس المحراب من القمة نحو الوسط
                        val widthFraction = when (idx) {
                            0 -> 0.84f
                            activeItems.lastIndex -> 0.86f
                            else -> 0.88f
                        }
                        SakinaMihrabRibbonItem(
                            title = item.title,
                            iconRes = item.iconRes,
                            onClick = {
                                onDismiss()
                                item.onClick()
                            },
                            modifier = Modifier.fillMaxWidth(widthFraction)
                        )
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
