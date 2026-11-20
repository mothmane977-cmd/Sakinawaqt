package app.sakina

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler

private data class SunnahItem(
    val id: String,
    val categoryAr: String,
    val titleAr: String,
    val subtitleAr: String
)

private val SAKINA_SUNNAH_LIST = listOf(
    SunnahItem("fajr_sunnah", "صلوات التطوع", "ركعتا سنة الفجر", "خيرٌ من الدنيا وما فيها (رواه مسلم)"),
    SunnahItem("duha", "صلوات التطوع", "صلاة الضحى", "من ركعتين إلى ثمان ركعات بعد الشروق بـ 15 دقيقة"),
    SunnahItem("rawatib", "صلوات التطوع", "السنن الرواتب (12 ركعة)", "من صلاها بُني له بيت في الجنة"),
    SunnahItem("qiyam_witr", "صلوات التطوع", "قيام الليل وصلاة الوتر", "أقرب ما يكون العبد من ربه في جوف الليل الأخير"),
    SunnahItem("daily_wird", "القرآن والذكر", "الورد اليومي من القرآن", "تلاوة حزب أو جزء أو ما تيسر بتدبّر"),
    SunnahItem("mulk_sleep", "القرآن والذكر", "سورة الملك قبل النوم", "تنجّي صاحبها وتشفع له بإذن الله"),
    SunnahItem("friday_kahf", "القرآن والذكر", "سورة الكهف والصلاة على النبي ﷺ", "نور ما بين الجمعتين يوم الجمعة"),
    SunnahItem("fast_mon_thu", "الصيام والبر", "صيام التطوع (الإثنين والخميس)", "تُعرض الأعمال فيهما على الله"),
    SunnahItem("fast_white", "الصيام والبر", "صيام الأيام البيض (13–14–15)", "كصيام الدهر كله في الأجر"),
    SunnahItem("sadaqah", "الصيام والبر", "الصدقة اليومية", "ما نقص مال من صدقة ولو بالقليل"),
    SunnahItem("silat_rahim", "الصيام والبر", "صلة الرحم", "اتصال أو زيارة أو رسالة طيبة للأهل والأقارب"),
    SunnahItem("birr_parents", "الصيام والبر", "برّ الوالدين والدعاء لهما", "ربّ ارحمهما كما ربياني صغيراً")
)

@Composable
fun SakinaSunnahScreen(
    ctx: Context,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }
    val theme = LocalSakinaTheme.current
    var tick by remember { mutableIntStateOf(0) }
    var fastingNotif by remember { mutableStateOf(SakinaAdvancedPrefs.voluntaryFastingNotif(ctx)) }
    var kahfNotif by remember { mutableStateOf(SakinaAdvancedPrefs.fridayKahfNotif(ctx)) }

    val doneCount = remember(tick) { SAKINA_SUNNAH_LIST.count { SakinaAdvancedPrefs.isSunnahDoneToday(ctx, it.id) } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 78.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SakinaBackIconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart))
            SakinaTitle3D(text = "السنن والنوافل", fontSize = 20.sp, modifier = Modifier.align(Alignment.Center))
        }

        // Calm daily progress summary
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(theme.bgCard.copy(alpha = 0.92f), theme.bgDeep.copy(alpha = 0.92f))))
                .border(1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("إنجاز اليوم الهادئ", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
                Text("$doneCount / ${SAKINA_SUNNAH_LIST.size}".toWesternDigits(), fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SakinaBrandColors.StableEmerald)
            }
        }

        // Quick reminder switches
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(theme.bgCard.copy(alpha = 0.85f))
                .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تذكير ليلي بصيام التطوع والأيام البيض", fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
            SakinaGoldSwitch(checked = fastingNotif, onCheckedChange = {
                fastingNotif = it
                SakinaAdvancedPrefs.setVoluntaryFastingNotif(ctx, it)
                try { AlarmScheduler.scheduleNext(ctx) } catch (_: Exception) {}
            })
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(theme.bgCard.copy(alpha = 0.85f))
                .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تذكير سورة الكهف يوم الجمعة", fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
            SakinaGoldSwitch(checked = kahfNotif, onCheckedChange = {
                kahfNotif = it
                SakinaAdvancedPrefs.setFridayKahfNotif(ctx, it)
            })
        }

        val grouped = SAKINA_SUNNAH_LIST.groupBy { it.categoryAr }
        grouped.forEach { (catTitle, items) ->
            Text(
                text = "❖ $catTitle ❖",
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = theme.accentGold,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
            items.forEach { item ->
                val checked = remember(tick) { SakinaAdvancedPrefs.isSunnahDoneToday(ctx, item.id) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (checked) Color(0xFF0E3B2E).copy(alpha = 0.88f) else theme.bgCard.copy(alpha = 0.86f))
                        .border(0.9.dp, if (checked) SakinaBrandColors.StableEmerald else theme.accentGold.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                        .clickable {
                            SakinaAdvancedPrefs.toggleSunnahToday(ctx, item.id)
                            tick++
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                        Text(item.titleAr, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = SakinaBrandColors.LuminousIvory)
                        Text(item.subtitleAr, fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.SoftSilver)
                    }
                    Icon(
                        imageVector = if (checked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (checked) SakinaBrandColors.StableEmerald else theme.accentGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
