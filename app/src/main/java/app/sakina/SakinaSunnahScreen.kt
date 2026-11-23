package app.sakina

import android.content.Context
import androidx.activity.compose.BackHandler
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

private data class SunnahItem(
    val id: String,
    val titleAr: String, val titleEn: String, val titleFr: String,
    val subAr: String, val subEn: String, val subFr: String
)

private val SAKINA_SUNNAH_LIST = listOf(
    SunnahItem("fajr_sunnah", "ركعتا سنة الفجر", "2 Rak'ahs Sunnah of Fajr", "2 Rak'ahs Sunna du Fajr", "خيرٌ من الدنيا وما فيها (رواه مسلم)", "Better than the world and all it contains (Muslim)", "Meilleures que ce bas monde et ce qu'il contient (Mouslim)"),
    SunnahItem("duha", "صلاة الضحى", "Duha Prayer", "Prière de Duha", "من ركعتين إلى ثمان ركعات بعد الشروق بـ 15 د", "2 to 8 Rak'ahs, 15 min after sunrise", "2 à 8 Rak'ahs, 15 min après le lever du soleil"),
    SunnahItem("rawatib", "السنن الرواتب (12 ركعة)", "12 Daily Sunnah Rak'ahs", "12 Rak'ahs Sunna quotidiennes", "من صلاها بُني له بيت في الجنة", "A house in Paradise is built for the one who prays them", "Une demeure au Paradis est bâtie pour qui les accomplit"),
    SunnahItem("qiyam_witr", "قيام الليل وصلاة الوتر", "Night Prayer & Witr", "Prière de nuit (Qiyam) & Witr", "في الثلث الأخير من الليل", "Best performed in the last third of the night", "Recommandée dans le dernier tiers de la nuit"),
    SunnahItem("daily_wird", "الورد اليومي من القرآن", "Daily Quran Recitation", "Lecture quotidienne du Coran", "تلاوة حزب أو جزء أو ما تيسر بتدبّر", "Recite a daily portion with reflection", "Lire une portion quotidienne avec méditation"),
    SunnahItem("mulk_sleep", "سورة الملك قبل النوم", "Surah Al-Mulk Before Sleep", "Sourate Al-Mulk avant de dormir", "تنجّي صاحبها وتشفع له بإذن الله", "Protects and intercedes for its reciter", "Protège et intercède pour son lecteur"),
    SunnahItem("friday_kahf", "سورة الكهف يوم الجمعة", "Surah Al-Kahf on Friday", "Sourate Al-Kahf le vendredi", "نور ما بين الجمعتين والصلاة على النبي ﷺ", "Light between the two Fridays & Salawat", "Lumière entre deux vendredis et prières sur le Prophète ﷺ"),
    SunnahItem("fast_mon_thu", "صيام الإثنين والخميس", "Monday & Thursday Fasting", "Jeûne du lundi et jeudi", "تُعرض الأعمال فيهما على الله", "Deeds are presented to Allah on these days", "Les œuvres sont présentées à Allah ces jours-là"),
    SunnahItem("fast_white", "صيام الأيام البيض (13–14–15)", "White Days Fasting (13-14-15)", "Jeûne des jours blancs (13-14-15)", "كصيام الدهر كله في الأجر", "Reward like fasting the entire year", "Récompense équivalente au jeûne de toute l'année"),
    SunnahItem("sadaqah", "الصدقة اليومية", "Daily Charity (Sadaqah)", "Aumône quotidienne (Sadaqah)", "ما نقص مال من صدقة ولو بالقليل", "Charity never decreases wealth", "L'aumône ne diminue jamais la richesse"),
    SunnahItem("silat_rahim", "صلة الرحم", "Maintaining Family Ties", "Liens de parenté", "اتصال أو زيارة أو رسالة طيبة للأهل", "A kind call, visit, or message to family", "Un appel, une visite ou un message bienveillant"),
    SunnahItem("birr_parents", "برّ الوالدين والدعاء لهما", "Honoring & Praying for Parents", "Bienfaisance envers les parents", "ربّ ارحمهما كما ربياني صغيراً", "My Lord, have mercy upon them both", "Seigneur, fais-leur miséricorde à tous deux")
)

@Composable
fun SakinaSunnahScreen(
    ctx: Context,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }
    val theme = LocalSakinaTheme.current
    val lang = LocalSakinaLang.current
    val textColor = LocalSakinaTextColor.current
    var tick by remember { mutableIntStateOf(0) }
    var fastingNotif by remember { mutableStateOf(SakinaAdvancedPrefs.voluntaryFastingNotif(ctx)) }
    var kahfNotif by remember { mutableStateOf(SakinaAdvancedPrefs.fridayKahfNotif(ctx)) }

    val doneCount = remember(tick) { SAKINA_SUNNAH_LIST.count { SakinaAdvancedPrefs.isSunnahDoneToday(ctx, it.id) } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 54.dp, bottom = 78.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SakinaBackIconButton(onClick = onBackClick)
            Spacer(Modifier.width(8.dp))
            SakinaTitle3D(
                text = sakinaTr(lang, "تذكير بالعبادات وصالح الأعمال", "Worship & Good Deeds", "Adorations & bonnes œuvres", "İbadet ve Salih Ameller", "Ibadah & Amal Saleh", "عبادات اور نیک اعمال"),
                fontSize = 14.5.sp,
                modifier = Modifier.weight(1f)
            )
        }

        SakinaGlassCard(modifier = Modifier.fillMaxWidth(), glowGold = true) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sakinaTr(lang, "إنجازك اليومي", "Daily Progress", "Progrès quotidien", "Günlük İlerleme", "Capaian Harian", "روزانہ پیش رفت"),
                    fontFamily = SakinaHeadingFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = theme.accentBright
                )
                Text(
                    text = "$doneCount / ${SAKINA_SUNNAH_LIST.size}".toWesternDigits(),
                    fontFamily = SakinaNumeralFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SakinaBrandColors.StableEmerald
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sakinaTr(lang, "تذكير صيام التطوع والأيام البيض", "Voluntary Fasting Reminder", "Rappel du jeûne surérogatoire", "Nafile Oruç Hatırlatıcı", "Pengingat Puasa Sunnah", "نفلی روزوں کی یاد دہانی"),
                    fontFamily = SakinaBodyFont, fontSize = 12.sp, color = textColor, modifier = Modifier.weight(1f)
                )
                SakinaGoldSwitch(checked = fastingNotif, onCheckedChange = {
                    fastingNotif = it; SakinaAdvancedPrefs.setVoluntaryFastingNotif(ctx, it)
                })
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sakinaTr(lang, "تذكير سورة الكهف يوم الجمعة", "Friday Surah Al-Kahf Reminder", "Rappel Sourate Al-Kahf (vendredi)", "Cuma Kehf Suresi Hatırlatıcı", "Pengingat Al-Kahf Jumat", "جمعہ سورۃ الکہف یاد دہانی"),
                    fontFamily = SakinaBodyFont, fontSize = 12.sp, color = textColor, modifier = Modifier.weight(1f)
                )
                SakinaGoldSwitch(checked = kahfNotif, onCheckedChange = {
                    kahfNotif = it; SakinaAdvancedPrefs.setFridayKahfNotif(ctx, it)
                })
            }
        }

        SAKINA_SUNNAH_LIST.forEach { item ->
            val done = remember(tick) { SakinaAdvancedPrefs.isSunnahDoneToday(ctx, item.id) }
            val shape = RoundedCornerShape(14.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sakinaLuxuryGlassCard(
                        cornerRadius = 16.dp,
                        innerHairline = done,
                        emeraldFlare = done,
                        shimmer = false,
                        borderAlpha = if (done) 0.85f else 0.58f
                    )
                    .clip(shape)
                    .clickable {
                        SakinaAdvancedPrefs.toggleSunnahToday(ctx, item.id)
                        tick++
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (done) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (done) SakinaBrandColors.StableEmerald else theme.accentGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sakinaTr(lang, item.titleAr, item.titleEn, item.titleFr),
                        fontFamily = SakinaHeadingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (done) SakinaBrandColors.StableEmerald else theme.accentBright
                    )
                    Text(
                        text = sakinaTr(lang, item.subAr, item.subEn, item.subFr),
                        fontFamily = SakinaBodyFont,
                        fontSize = 11.sp,
                        color = SakinaBrandColors.SoftSilver
                    )
                }
            }
        }
    }
}
