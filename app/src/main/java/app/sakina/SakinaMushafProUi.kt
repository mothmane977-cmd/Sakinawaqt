package app.sakina

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * مكونات واجهة «مصحفي» الاحترافية (v64):
 * 1. لوحة إعدادات القراءة الشاملة (السطوع، الاصفرار، 5 ألوان ورق، 6 ألوان إطار، أفقي/رأسي، تلقائي، الدوران)
 * 2. نافذة الـ 10 علامات مرقّمة مستقلة (حفظ، تسمية، فرز، حذف، انتقال)
 * 3. نافذة «الانتقال إلى الصفحة» (1–604) مع معاينة السورة والجزء والحزب
 * 4. قائمة الضغط المطوّل على الآية (نسخ، حفظ علامة، إضافة إلى ملاحظاتي، مشاركة)
 * 5. جولة التركيز البؤري الحي (Spotlight Onboarding)
 * 6. فهرس الصفحات (604) + جدول الأحزاب رباعي الأرباع + شريط القفز السريع للأجزاء (1–30)
 */
@Composable
fun MushafDisplaySettingsDialog(
    settings: MushafDisplaySettings,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onUpdate: (MushafDisplaySettings) -> Unit,
    onReplaySpotlight: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071E1B),
        title = {
            Text(
                text = "إعدادات العرض وراحة العين",
                color = SakinaLuxuryColors.SoftGoldGlow,
                fontWeight = FontWeight.Bold,
                fontFamily = headingFontFamily,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // حجم الخط (متزامن مع Pinch-to-Zoom)
                Text("حجم الخط: ${(settings.fontScale * 100).toInt()}% (أو كبّر بإصبعين)", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontFamily = bodyFontFamily)
                Slider(
                    value = settings.fontScale,
                    onValueChange = { onUpdate(settings.fontScale.let { _ -> settings.copy(fontScale = it) }) },
                    valueRange = 0.75f..1.75f,
                    colors = SliderDefaults.colors(thumbColor = SakinaLuxuryColors.SoftGoldGlow, activeTrackColor = SakinaLuxuryColors.ChampagneGold)
                )

                // طول الصفحة (تباعد الأسطر)
                Text("طول الصفحة وتباعد الأسطر: ${(settings.lineSpacingScale * 100).toInt()}%", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontFamily = bodyFontFamily)
                Slider(
                    value = settings.lineSpacingScale,
                    onValueChange = { onUpdate(settings.copy(lineSpacingScale = it)) },
                    valueRange = 0.85f..1.45f,
                    colors = SliderDefaults.colors(thumbColor = SakinaLuxuryColors.SoftGoldGlow, activeTrackColor = SakinaLuxuryColors.ChampagneGold)
                )

                // السطوع المستقل
                val brVal = if (settings.brightness < 0f) 0.65f else settings.brightness
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("سطوع الشاشة المستقل", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontFamily = bodyFontFamily)
                    Text(
                        text = if (settings.brightness < 0f) "تلقائي" else "${(settings.brightness * 100).toInt()}%",
                        color = SakinaLuxuryColors.EmeraldGlow,
                        fontSize = 11.5.sp,
                        fontFamily = SakinaNumeralFont,
                        modifier = Modifier.clickable { onUpdate(settings.copy(brightness = -1f)) }
                    )
                }
                Slider(
                    value = brVal,
                    onValueChange = { onUpdate(settings.copy(brightness = it)) },
                    valueRange = 0.15f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = SakinaLuxuryColors.SoftGoldGlow, activeTrackColor = SakinaLuxuryColors.ChampagneGold)
                )

                // درجة الاصفرار لحماية العين
                Text("درجة الاصفرار (حماية العين): ${(settings.warmthYellow * 220).toInt()}%", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontFamily = bodyFontFamily)
                Slider(
                    value = settings.warmthYellow,
                    onValueChange = { onUpdate(settings.copy(warmthYellow = it)) },
                    valueRange = 0f..0.45f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFFCA6B), activeTrackColor = Color(0xFFE6A23C))
                )

                HorizontalDivider(color = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.3f))

                // 5 ألوان لورق القراءة
                Text("لون ورق القراءة (5 ألوان):", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SakinaMushafProPrefs.PAPER_COLORS.forEachIndexed { idx, (_, col) ->
                        val active = settings.paperColorIdx == idx
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (active) 2.5.dp else 1.dp,
                                    color = if (active) SakinaLuxuryColors.EmeraldGlow else SakinaLuxuryColors.ChampagneGold,
                                    shape = CircleShape
                                )
                                .clickable { onUpdate(settings.copy(paperColorIdx = idx)) }
                        )
                    }
                }

                // 6 ألوان لإطار وزخارف المصحف
                Text("لوّن مصحفك (6 ألوان للإطار):", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SakinaMushafProPrefs.FRAME_THEMES.forEachIndexed { idx, (_, col) ->
                        val active = settings.frameThemeIdx == idx
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (active) 2.5.dp else 1.dp,
                                    color = if (active) Color.White else SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                                .clickable { onUpdate(settings.copy(frameThemeIdx = idx)) }
                        )
                    }
                }

                HorizontalDivider(color = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.3f))

                // التصفح الأفقي / الرأسي
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("التصفح الرأسي المستمر", color = SakinaOrbitalColors.WarmCream, fontSize = 12.5.sp, fontFamily = bodyFontFamily)
                    SakinaJewelSwitch(checked = settings.isVerticalScroll, onCheckedChange = { onUpdate(settings.copy(isVerticalScroll = it)) })
                    )
                }

                // التصفح التلقائي
                Text("التصفح التلقائي (Auto-Scroll):", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontFamily = bodyFontFamily)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("إيقاف", "بطيء", "متوسط", "سريع").forEachIndexed { idx, lbl ->
                        val sel = settings.autoScrollSpeed == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (sel) SakinaLuxuryPalette.MetallicGoldButtonBrush else Brush.horizontalGradient(listOf(Color(0xFF041716), Color(0xFF041716))))
                                .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { onUpdate(settings.copy(autoScrollSpeed = idx)) }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(lbl, color = if (sel) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // اتجاه الشاشة الثلاثي
                Text("اتجاه الشاشة:", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontFamily = bodyFontFamily)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("تلقائي", "عمودي", "أفقي").forEachIndexed { idx, lbl ->
                        val sel = settings.orientationMode == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (sel) SakinaLuxuryPalette.MetallicGoldButtonBrush else Brush.horizontalGradient(listOf(Color(0xFF041716), Color(0xFF041716))))
                                .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { onUpdate(settings.copy(orientationMode = idx)) }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(lbl, color = if (sel) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // إبقاء الشاشة مضاءة
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("إبقاء الشاشة مضاءة أثناء التلاوة", color = SakinaOrbitalColors.WarmCream, fontSize = 12.5.sp, fontFamily = bodyFontFamily)
                    SakinaJewelSwitch(checked = settings.keepScreenAwake, onCheckedChange = { onUpdate(settings.copy(keepScreenAwake = it)) })
                    )
                }

                Text(
                    text = "✦ إعادة عرض الجولة الإرشادية للمصحف",
                    color = SakinaLuxuryColors.EmeraldGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onReplaySpotlight(); onDismiss() }.padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("تم", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun MushafBookmarks10Dialog(
    currentPage: Int,
    currentSurahName: String,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onJumpToPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    var sortByMushafOrder by remember { mutableStateOf(false) }
    var editingSlotId by remember { mutableStateOf<Int?>(null) }
    var editLabelText by remember { mutableStateOf("") }

    val savedMap = remember(tick) { SakinaMushafProPrefs.loadBookmarks10(ctx).associateBy { it.slotId } }
    val slotOrder = remember(tick, sortByMushafOrder) {
        if (!sortByMushafOrder) (1..10).toList()
        else {
            val filled = savedMap.values.sortedBy { it.page }.map { it.slotId }
            val empty = (1..10).filterNot { it in savedMap.keys }
            filled + empty
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071E1B),
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("العلامات المرجعية (10)", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily, fontSize = 16.sp)
                Text(
                    text = if (sortByMushafOrder) "فرز: المصحف" else "فرز: الرقم",
                    color = SakinaLuxuryColors.EmeraldGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { sortByMushafOrder = !sortByMushafOrder }.padding(4.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                if (editingSlotId != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = editLabelText,
                            onValueChange = { editLabelText = it },
                            label = { Text("اسم العلامة $editingSlotId", color = SakinaLuxuryColors.SoftGoldGlow) },
                            singleLine = true,
                            textStyle = TextStyle(color = SakinaOrbitalColors.WarmCream, fontFamily = bodyFontFamily),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            SakinaMushafProPrefs.renameBookmarkSlot(ctx, editingSlotId!!, editLabelText)
                            editingSlotId = null
                            tick++
                        }) {
                            Text("حفظ", color = SakinaLuxuryColors.EmeraldGlow, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                slotOrder.forEach { id ->
                    val b = savedMap[id]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (b != null) Color(0xFF0C362C) else Color(0xFF041515))
                            .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = if (b != null) 0.65f else 0.25f), RoundedCornerShape(12.dp))
                            .clickable {
                                if (b != null) {
                                    onJumpToPage(b.page)
                                    onDismiss()
                                } else {
                                    SakinaMushafProPrefs.setBookmarkSlot(ctx, id, currentPage, currentSurahName)
                                    tick++
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${id}. ${b?.label ?: "علامة فارغة (اضغط للحفظ هنا)"}",
                                color = if (b != null) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = bodyFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (b != null) {
                                Text(
                                    text = "${b.surahName} · صفحة ${b.page}",
                                    color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f),
                                    fontSize = 10.5.sp,
                                    fontFamily = SakinaNumeralFont
                                )
                            }
                        }
                        if (b != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("تحديث", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 10.5.sp, modifier = Modifier.clickable {
                                    SakinaMushafProPrefs.setBookmarkSlot(ctx, id, currentPage, currentSurahName)
                                    tick++
                                })
                                Text("تسمية", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 10.5.sp, modifier = Modifier.clickable {
                                    editingSlotId = id
                                    editLabelText = b.label
                                })
                                Text("×", color = Color(0xFFE58A7E), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                                    SakinaMushafProPrefs.deleteBookmarkSlot(ctx, id)
                                    tick++
                                })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun MushafJumpToPageDialog(
    currentPage: Int,
    surahNameForPage: (Int) -> String,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onJump: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(currentPage.toString()) }
    val parsed = input.trim().toIntOrNull()?.coerceIn(1, 604)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071E1B),
        title = {
            Text("الانتقال إلى الصفحة (1 – 604)", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { if (it.length <= 3) input = it.filter { ch -> ch.isDigit() } },
                    label = { Text("رقم الصفحة", color = SakinaLuxuryColors.SoftGoldGlow) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(color = SakinaOrbitalColors.WarmCream, fontFamily = SakinaNumeralFont, fontSize = 16.sp)
                )
                if (parsed != null) {
                    val sName = surahNameForPage(parsed)
                    val juz = SakinaMushafProPrefs.approxJuzForPage(parsed)
                    val hizb = SakinaMushafProPrefs.approxHizbForPage(parsed)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A2E26))
                            .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "معاينة الصفحة $parsed:\n$sName · الجزء $juz · الحزب $hizb",
                            color = SakinaOrbitalColors.WarmCream,
                            fontSize = 12.5.sp,
                            lineHeight = 20.sp,
                            fontFamily = bodyFontFamily
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (parsed != null) {
                    onJump(parsed)
                    onDismiss()
                }
            }) {
                Text("انتقال", color = SakinaLuxuryColors.EmeraldGlow, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
fun MushafVerseLongPressDialog(
    surahName: String,
    page: Int,
    verseText: String,
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onSaveToBookmarks: () -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    var feedback by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071E1B),
        title = {
            Text("خدمات الصفحة · $surahName (ص $page)", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily, fontSize = 15.5.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = verseText.take(180) + if (verseText.length > 180) "..." else "",
                    color = SakinaOrbitalColors.WarmCream,
                    fontSize = 13.5.sp,
                    lineHeight = 22.sp,
                    fontFamily = headingFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF041515))
                        .padding(10.dp)
                )

                listOf(
                    "📋 نسخ نص الصفحة" to {
                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Quran", "$verseText\n[$surahName · ص $page]"))
                        feedback = "تم النسخ إلى الحافظة ✓"
                    },
                    "🔖 حفظ في العلامات الـ 10" to {
                        onSaveToBookmarks()
                        onDismiss()
                    },
                    "✍ إضافة إلى دفتر «ملاحظاتي»" to {
                        val nSp = ctx.getSharedPreferences("nurqunoot_notes_v63", Context.MODE_PRIVATE)
                        val old = nSp.getString("personal_tadabbur_note", "") ?: ""
                        val appended = (if (old.isNotBlank()) "$old\n\n" else "") + "[$surahName · ص $page]: ${verseText.take(140)}"
                        nSp.edit().putString("personal_tadabbur_note", appended).apply()
                        feedback = "تمت الإضافة إلى دفتر ملاحظاتي ✓"
                    },
                    "↗ مشاركة المقطع" to {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "$verseText\n\n— $surahName · صفحة $page (نُورُ قُنُوت)")
                        }
                        ctx.startActivity(Intent.createChooser(shareIntent, "مشاركة"))
                    }
                ).forEach { (lbl, action) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A2E26))
                            .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .clickable(onClick = action)
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Text(lbl, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, fontFamily = bodyFontFamily)
                    }
                }

                if (feedback != null) {
                    Text(feedback!!, color = SakinaLuxuryColors.EmeraldGlow, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = SakinaLuxuryColors.SoftGoldGlow)
            }
        }
    )
}

@Composable
fun MushafSpotlightOverlay(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val steps = listOf(
        "اللمس الفردي للمصحف" to "المس وسط الصفحة في أي وقت لإظهار أو إخفاء الشريط العلوي والمشغّل الصوتي الماسي لخشوع كامل ملء الشاشة.",
        "تكبير الخط بإصبعين (Pinch-to-Zoom)" to "اسحب بإصبعين على صفحة المصحف لتكبير الخط أو تصغيره فوراً، أو اضبط السطوع والاصفرار من زر الإعدادات ⚙.",
        "زر العين والتنقل السريع (±5 صفحات)" to "اضغط أيقونة العين لإخفاء الآيات ومراجعة حفظك، واستخدم أزرار القفز ±5 صفحات أو المس رقم الصفحة للانتقال المباشر.",
        "الضغط المطوّل والعلامات الـ 10" to "اضغط مطوّلاً على نص الصفحة للنسخ أو الحفظ في دفتر «ملاحظاتي»، واستخدم زر المرجعية لإدارة 10 علامات مستقلة."
    )
    val (title, desc) = steps[step]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC030D0E))
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF0A3229), Color(0xFF051818))))
                .border(1.3.dp, SakinaLuxuryPalette.MetallicGoldTextBrush, RoundedCornerShape(22.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✦ دليل المصحف السريع (${step + 1}/${steps.size})",
                color = SakinaLuxuryColors.EmeraldGlow,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = bodyFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = TextStyle(
                    brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = desc,
                color = SakinaOrbitalColors.WarmCream,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                fontFamily = bodyFontFamily,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onFinish) {
                    Text("تخطي", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.7f))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                        .clickable {
                            if (step < steps.lastIndex) step++ else onFinish()
                        }
                        .padding(horizontal = 20.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = if (step < steps.lastIndex) "التالي" else "ابدأ القراءة",
                        color = Color(0xFF071A18),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bodyFontFamily
                    )
                }
            }
        }
    }
}

/** فهرس الصفحات الـ 604 + جدول الأحزاب رباعي الأرباع */
@Composable
fun MushafPages604Grid(
    surahNameForPage: (Int) -> String,
    bodyFontFamily: FontFamily,
    onPageSelected: (Int) -> Unit
) {
    val velvetBg = remember { Brush.horizontalGradient(listOf(Color(0xFF06231D), Color(0xFF082E26), Color(0xFF051819))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.45f)

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        (1..604).chunked(4).forEach { rowPages ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                rowPages.forEach { p ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(velvetBg)
                            .border(0.75.dp, goldBorder, RoundedCornerShape(12.dp))
                            .clickable { onPageSelected(p) }
                            .padding(vertical = 7.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ص $p",
                            color = SakinaLuxuryColors.SoftGoldGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SakinaNumeralFont
                        )
                        Text(
                            text = surahNameForPage(p).replace("سورة ", ""),
                            color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.72f),
                            fontSize = 9.5.sp,
                            fontFamily = bodyFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                repeat(4 - rowPages.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}
