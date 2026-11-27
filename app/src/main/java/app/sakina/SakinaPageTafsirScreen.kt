package app.sakina

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * شاشة «تفسير الصفحة والمكتبة الموحّدة» الصوتية التفاعلية (v67):
 * - تبديل فوري على نفس الآيات بين: الهدايات · التفاسير الـ 8 · الإعراب · أسباب النزول · الترجمة
 * - شريط تحكم علوي: السابق/التالي للصفحة، تشغيل الصفحة، إخفاء/إظهار، A-/A+، وإدارة الحزم أوفلاين
 * - لكل آية: زر نسخ الآية وتفسيرها + إضافة إلى «ملاحظاتي» + زر عائم للعودة السريعة للأعلى (↑)
 */
@Composable
fun SakinaPageTafsirContent(
    surahNumber: Int,
    surahName: String,
    pageNumber: Int,
    pageAyahs: List<MushafAyahUi>,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onPlayPageAudio: (() -> Unit)? = null,
    onPrevPage: (() -> Unit)? = null,
    onNextPage: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var activePack by remember { mutableStateOf(TafsirSourcePack.HIDAYAT) }
    var packData by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var fontSp by remember { mutableIntStateOf(14) }
    var hideAyahs by remember { mutableStateOf(false) }
    var showPacksManager by remember { mutableStateOf(false) }
    var feedbackToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(activePack, surahNumber) {
        isLoading = true
        packData = SakinaTafsirPacksManager.fetchOrLoadSurahPack(ctx, activePack, surahNumber)
        isLoading = false
    }

    val velvetBg = remember { Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.55f)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. شريط التبديل الفوري بين الهدايات والتفاسير الـ 8 والإعراب وأسباب النزول والترجمة
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(TafsirSourcePack.values().toList()) { pk ->
                    val sel = activePack == pk
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (sel) SakinaLuxuryPalette.MetallicGoldButtonBrush else SolidColor(Color(0xFF041716)))
                            .border(0.8.dp, if (sel) SakinaLuxuryColors.SoftGoldGlow else goldBorder.copy(alpha = 0.45f), RoundedCornerShape(11.dp))
                            .clickable { activePack = pk }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = pk.shortAr,
                            color = if (sel) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = bodyFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            // 2. بطاقة التحكم العلوية للصفحة (السابق/التالي · تشغيل · عين · خط · الحزم)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE6051C19))
                    .border(0.85.dp, goldBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (onPrevPage != null) {
                        Text("<", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onPrevPage).padding(4.dp))
                    }
                    Text(
                        text = "$surahName · ص $pageNumber",
                        color = SakinaLuxuryColors.SoftGoldGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = headingFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (onNextPage != null) {
                        Text(">", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onNextPage).padding(4.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (onPlayPageAudio != null) {
                        Text("▶", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onPlayPageAudio).padding(3.dp))
                    }
                    Text("👁", fontSize = 12.sp, modifier = Modifier.clickable { hideAyahs = !hideAyahs }.padding(3.dp))
                    Text("A-", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp - 1).coerceAtLeast(12) }.padding(2.dp))
                    Text("A+", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp + 1).coerceAtMost(24) }.padding(2.dp))
                    Text("⬇", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { showPacksManager = true }.padding(3.dp))
                }
            }

            if (feedbackToast != null) {
                Text(
                    text = feedbackToast!!,
                    color = SakinaLuxuryColors.EmeraldGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SakinaLuxuryColors.SoftGoldGlow, modifier = Modifier.size(30.dp))
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 44.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    if (pageAyahs.isEmpty()) {
                        item {
                            Text(
                                text = "افتح أي سورة من «مصحفي» ثم اضغط زر «تفسير» لاستعراض ${activePack.titleAr} آيةً بآية.",
                                color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f),
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center,
                                fontFamily = bodyFontFamily,
                                modifier = Modifier.fillMaxWidth().padding(20.dp)
                            )
                        }
                    } else {
                        items(pageAyahs, key = { it.ayahNumber }) { ayah ->
                            val commentary = packData[ayah.ayahNumber]
                                ?: "تُعرض بيانات ${activePack.titleAr} الكاملة لهذه السورة فور تحميل الحزمة أوفلاين من أيقونة (⬇) بالأعلى."
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(velvetBg)
                                    .border(0.9.dp, goldBorder, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$surahName · آية ${ayah.ayahNumber}",
                                        color = SakinaLuxuryColors.EmeraldGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SakinaNumeralFont
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "📋 نسخ",
                                            color = SakinaLuxuryColors.SoftGoldGlow,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                cm.setPrimaryClip(ClipData.newPlainText("Tafsir", "﴿${ayah.styledText.text}﴾\n[${activePack.titleAr}]: $commentary"))
                                                feedbackToast = "تم نسخ الآية مع ${activePack.shortAr} ✓"
                                            }
                                        )
                                        Text(
                                            text = "✍ ملاحظتي",
                                            color = SakinaLuxuryColors.SoftGoldGlow,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                val nSp = ctx.getSharedPreferences("nurqunoot_notes_v63", Context.MODE_PRIVATE)
                                                val old = nSp.getString("personal_tadabbur_note", "") ?: ""
                                                val appended = (if (old.isNotBlank()) "$old\n\n" else "") + "[$surahName:${ayah.ayahNumber}] $commentary"
                                                nSp.edit().putString("personal_tadabbur_note", appended).apply()
                                                feedbackToast = "أُضيف إلى دفتر ملاحظاتي ✓"
                                            }
                                        )
                                    }
                                }
                                Text(
                                    text = "﴿ ${ayah.styledText.text} ﴾",
                                    color = SakinaLuxuryColors.SoftGoldGlow,
                                    fontSize = (fontSp + 2).sp,
                                    lineHeight = (fontSp + 14).sp,
                                    fontFamily = headingFontFamily,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().alpha(if (hideAyahs) 0.08f else 1f)
                                )
                                HorizontalDivider(color = goldBorder.copy(alpha = 0.3f))
                                Text(
                                    text = commentary,
                                    color = SakinaOrbitalColors.WarmCream,
                                    fontSize = fontSp.sp,
                                    lineHeight = (fontSp + 10).sp,
                                    fontFamily = bodyFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }

        // زر عائم للعودة السريعة لأعلى الصفحة (↑)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                .clickable { scope.launch { listState.animateScrollToItem(0) } },
            contentAlignment = Alignment.Center
        ) {
            Text("↑", color = Color(0xFF071A18), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }

    // نافذة إدارة وتحميل وحذف حزم التفاسير الـ 8 أوفلاين عند الطلب
    if (showPacksManager) {
        var mgrTick by remember { mutableIntStateOf(0) }
        AlertDialog(
            onDismissRequest = { showPacksManager = false },
            containerColor = Color(0xFF071E1B),
            title = { Text("إدارة حزم التفاسير أوفلاين", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    TafsirSourcePack.values().forEach { pk ->
                        val ready = remember(mgrTick) { SakinaTafsirPacksManager.isPackDownloaded(ctx, pk) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF041515))
                                .border(0.75.dp, goldBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pk.titleAr, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = bodyFontFamily)
                                Text(pk.approxMb, color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = SakinaNumeralFont)
                            }
                            if (ready && pk != TafsirSourcePack.HIDAYAT) {
                                Text("حذف", color = Color(0xFFE58A7E), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                                    SakinaTafsirPacksManager.deletePack(ctx, pk)
                                    mgrTick++
                                }.padding(4.dp))
                            } else if (ready) {
                                Text("✓ مدمج", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("⬇ تحميل", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                                    scope.launch {
                                        SakinaTafsirPacksManager.fetchOrLoadSurahPack(ctx, pk, surahNumber)
                                        mgrTick++
                                    }
                                }.padding(4.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPacksManager = false }) { Text("إغلاق", color = SakinaLuxuryColors.SoftGoldGlow) } }
        )
    }
}
