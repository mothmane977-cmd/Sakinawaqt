package app.sakina

import android.content.Context
import android.icu.util.IslamicCalendar
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

/**
 * Sakina v45 — Phase 3 Luxury Worship Progress Hub:
 * 1. 3 Statistical Rings for Prayer Tracker + Editable Hijri Monthly Goal (#28)
 * 2. Fasting Tracker (Ramadan, Mon/Thu, White Days 13-14-15) with Imsak/Iftar (#20)
 * 3. Quran Khatmah Daily Progress, Streak & Saved Bookmarks (#19)
 */
@Composable
fun SakinaWorshipStatsScreen(
    ctx: Context,
    onBack: () -> Unit,
    onOpenQuranPage: (Int) -> Unit = {}
) {
    BackHandler { onBack() }
    val lang = LocalSakinaLang.current
    var tick by remember { mutableIntStateOf(0) }

    val todayKey = remember { SakinaAdvancedPrefs.dateKeyOffset(0) }
    val todayPrayed = remember(tick) {
        (0..4).count { SakinaAdvancedPrefs.isPrayerLogged(ctx, todayKey, it) }
    }
    val (monthlyLogged, monthlyTotal, perfectDays) = remember(tick) {
        SakinaAdvancedPrefs.monthlyPrayerStats(ctx)
    }
    val monthlyGoal = remember(tick) { SakinaV45StatsPrefs.monthlyPrayerGoal(ctx) }
    val isFastedToday = remember(tick) { SakinaV45StatsPrefs.isFastedToday(ctx) }
    val totalFasted = remember(tick) { SakinaV45StatsPrefs.totalFastedDays(ctx) }
    val pagesToday = remember(tick) { SakinaV45StatsPrefs.pagesReadToday(ctx) }
    val khatmahPages = remember(tick) { SakinaV45StatsPrefs.khatmahCurrentPages(ctx) }
    val completedKhatmahs = remember(tick) { SakinaV45StatsPrefs.completedKhatmahCount(ctx) }
    val streakDays = remember(tick) { SakinaV45StatsPrefs.readingStreakDays(ctx) }
    val bookmarks = remember(tick) { SakinaV45StatsPrefs.bookmarks(ctx) }
    val deedsDone = remember(tick) { SakinaV45StatsPrefs.goodDeedsDoneToday(ctx) }
    var showKhatmDua by remember { mutableStateOf(false) }

    // Detect today's fasting occasion (Mon/Thu or White Days 13-14-15 or Ramadan)
    val fastingBadge = remember {
        val cal = Calendar.getInstance()
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val ic = IslamicCalendar().apply {
            add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
        }
        val hDay = ic.get(IslamicCalendar.DAY_OF_MONTH)
        val hMonth = ic.get(IslamicCalendar.MONTH)
        when {
            hMonth == IslamicCalendar.RAMADAN -> sakinaTr(lang, "شهر رمضان المبارك", "Blessed Ramadan", "Mois de Ramadan", "Mübarek Ramazan", "Bulan Ramadan", "ماہِ رمضان")
            hDay in 13..15 -> sakinaTr(lang, "الأيام البيض ($hDay هـ)", "White Days (Hijri $hDay)", "Jours Blancs ($hDay)", "Eyyam-ı Biyd ($hDay)", "Ayyamul Bidh ($hDay)", "ایامِ بیض ($hDay)")
            dow == Calendar.MONDAY || dow == Calendar.THURSDAY -> sakinaTr(lang, "صيام سنّة الإثنين والخميس", "Mon/Thu Sunnah Fasting", "Jeûne Sunna Lun/Jeu", "Pzt/Per Sünnet Orucu", "Puasa Sunnah Sen/Kam", "پیر و جمعرات کا روزہ")
            else -> sakinaTr(lang, "صيام التطوّع", "Voluntary Fasting", "Jeûne surérogatoire", "Nafile Oruç", "Puasa Sunnah", "نفلی روزہ")
        }
    }

    val (imsakTime, iftarTime) = remember {
        val todayTimes = PrayerEngine.day(ctx, 0)
        val fajrMs = todayTimes.firstOrNull()?.second?.time ?: System.currentTimeMillis()
        val imsakDate = java.util.Date(fajrMs - 10 * 60_000L)
        val maghribDate = todayTimes.getOrNull(3)?.second ?: java.util.Date()
        SakinaAdvancedPrefs.formatPrayerTime(ctx, imsakDate) to SakinaAdvancedPrefs.formatPrayerTime(ctx, maghribDate)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SakinaLuxuryPalette.ObsidianNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SakinaAmbientLuxuryBackdrop(showGeometricPattern = true, showStarDust = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            SakinaUnboxedHeader(
                title = sakinaTr(lang, "إنجازي وعباداتي", "My Worship Progress", "Mes Progrès", "İbadet İlerlemem", "Progres Ibadahku", "میری عبادات"),
                backIconRes = R.drawable.ic_sakina_back,
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. الحلقات الإحصائية الثلاث لمتتبع الصلاة + هدفي للشهر الهجري (#28)
                SakinaGlowingGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    emeraldIntensity = 0.36f
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = sakinaTr(lang, "متتبع الصلوات الخمس · هدفي الشهري", "5-Prayer Tracker · Monthly Goal", "Suivi des 5 Prières · Objectif", "5 Vakit Takibi · Aylık Hedef", "Pelacak 5 Waktu · Target Bulanan", "پانچ نمازوں کا ریکارڈ"),
                            style = TextStyle(
                                brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaHeadingFont
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                SakinaCircularProgressRing(
                                    progress = todayPrayed / 5f,
                                    centerLabel = "$todayPrayed/5",
                                    size = 66.dp,
                                    strokeWidth = 5.dp
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(sakinaTr(lang, "اليوم", "Today", "Aujourd'hui", "Bugün", "Hari Ini", "آج"), color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f), fontSize = 11.sp, fontFamily = SakinaBodyFont)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                SakinaCircularProgressRing(
                                    progress = (perfectDays / 30f).coerceIn(0f, 1f),
                                    centerLabel = "${perfectDays}d",
                                    size = 66.dp,
                                    strokeWidth = 5.dp
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(sakinaTr(lang, "أيام كاملة", "Perfect Days", "Jours parfaits", "Tam Gün", "Hari Penuh", "مکمل دن"), color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.8f), fontSize = 11.sp, fontFamily = SakinaBodyFont)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                SakinaCircularProgressRing(
                                    progress = (monthlyLogged.toFloat() / monthlyGoal.coerceAtLeast(1)).coerceIn(0f, 1f),
                                    centerLabel = "$monthlyLogged",
                                    size = 66.dp,
                                    strokeWidth = 5.dp
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(sakinaTr(lang, "الهدف: $monthlyGoal", "Goal: $monthlyGoal", "But: $monthlyGoal", "Hedef: $monthlyGoal", "Target: $monthlyGoal", "ہدف: $monthlyGoal"), color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.sp, fontFamily = SakinaBodyFont)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // تعديل الهدف الشهري بلمسة واحدة
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(SakinaLuxuryColors.MidnightNavy)
                                    .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.6f), CircleShape)
                                    .clickable {
                                        SakinaV45StatsPrefs.setMonthlyPrayerGoal(ctx, monthlyGoal - 10)
                                        tick++
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("−", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = sakinaTr(lang, "تعديل هدف الشهر ($monthlyGoal صلاة)", "Monthly Target ($monthlyGoal)", "Objectif ($monthlyGoal)", "Aylık Hedef ($monthlyGoal)", "Target ($monthlyGoal)", "ماہانہ ہدف ($monthlyGoal)"),
                                color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f),
                                fontSize = 11.5.sp,
                                fontFamily = SakinaBodyFont
                            )
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(SakinaLuxuryColors.MidnightNavy)
                                    .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.6f), CircleShape)
                                    .clickable {
                                        SakinaV45StatsPrefs.setMonthlyPrayerGoal(ctx, monthlyGoal + 10)
                                        tick++
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 2. متتبع الصيام (رمضان + الإثنين والخميس + الأيام البيض) (#20)
                SakinaGlowingGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    emeraldIntensity = 0.42f
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = sakinaTr(lang, "متتبع الصيام", "Fasting Tracker", "Suivi du Jeûne", "Oruç Takibi", "Pelacak Puasa", "روزہ ٹریکر"),
                                    style = TextStyle(
                                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SakinaHeadingFont
                                    )
                                )
                                Text(
                                    text = fastingBadge,
                                    color = SakinaLuxuryColors.EmeraldGlow,
                                    fontSize = 11.5.sp,
                                    fontFamily = SakinaBodyFont
                                )
                            }
                            Text(
                                text = sakinaTr(lang, "$totalFasted يوم", "$totalFasted days", "$totalFasted j", "$totalFasted gün", "$totalFasted hari", "$totalFasted دن"),
                                color = SakinaLuxuryColors.SoftGoldGlow,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaBodyFont,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = sakinaTr(lang, "الإمساك: $imsakTime", "Imsak: $imsakTime", "Imsak : $imsakTime", "İmsak: $imsakTime", "Imsak: $imsakTime", "امساک: $imsakTime"),
                                color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontFamily = SakinaBodyFont,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                            Text(
                                text = sakinaTr(lang, "الإفطار (المغرب): $iftarTime", "Iftar (Maghrib): $iftarTime", "Iftar : $iftarTime", "İftar: $iftarTime", "Buka: $iftarTime", "افطار: $iftarTime"),
                                color = SakinaLuxuryColors.SoftGoldGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaBodyFont,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SakinaBrushedGoldButton(
                            text = if (isFastedToday) {
                                sakinaTr(lang, "تم تسجيل صيام اليوم ✓ (تراجع)", "Today's Fast Logged ✓", "Jeûne enregistré ✓", "Bugünkü Oruç Kaydedildi ✓", "Puasa Hari Ini Tercatat ✓", "آج کا روزہ درج ✓")
                            } else {
                                sakinaTr(lang, "سجّل صيام اليوم ✓", "Log Today's Fast ✓", "Enregistrer le jeûne d'aujourd'hui", "Bugünkü Orucu Kaydet", "Catat Puasa Hari Ini", "آج کا روزہ درج کریں")
                            },
                            pillBgRes = R.drawable.btn_gold_cta_pill,
                            onClick = {
                                SakinaV45StatsPrefs.toggleFastedToday(ctx)
                                tick++
                            },
                            height = 44.dp
                        )
                    }
                }

                // 3. ختمة القرآن اليومية والمرجعيات المحفوظة (#19)
                SakinaGlowingGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    emeraldIntensity = 0.30f
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sakinaTr(lang, "ختمتي اليومية للقرآن", "Daily Quran Khatmah", "Ma Khatma Quotidienne", "Günlük Hatim Hedefi", "Target Khatam Harian", "روزانہ تلاوتِ قرآن"),
                                    style = TextStyle(
                                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SakinaHeadingFont
                                    )
                                )
                                Text(
                                    text = "اليوم: $pagesToday ص · السلسلة: $streakDays يوم · الختمات: $completedKhatmahs",
                                    color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f),
                                    fontSize = 11.sp,
                                    fontFamily = SakinaBodyFont,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                            SakinaCircularProgressRing(
                                progress = (khatmahPages / 604f).coerceIn(0f, 1f),
                                centerLabel = "$khatmahPages/604",
                                size = 62.dp,
                                strokeWidth = 4.5.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1, 5, 10, 20).forEach { delta ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(50))
                                        .background(SakinaLuxuryPalette.MetallicGoldButtonBrush)
                                        .clickable {
                                            SakinaV45StatsPrefs.logKhatmahPages(ctx, delta)
                                            tick++
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+$delta ص",
                                        color = SakinaOrbitalColors.InkNavy,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SakinaBodyFont
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.85f))
                                .border(0.85.dp, SakinaLuxuryPalette.MetallicGoldButtonBrush, RoundedCornerShape(50))
                                .clickable { showKhatmDua = !showKhatmDua }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (showKhatmDua) "إخفاء دعاء ختم القرآن ▴" else "✦ دعاء ختم القرآن الكريم",
                                color = SakinaLuxuryColors.SoftGoldGlow,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaBodyFont
                            )
                        }

                        AnimatedVisibility(visible = showKhatmDua) {
                            Text(
                                text = "اللهم ارحمني بالقرآن واجعله لي إماماً ونوراً وهدىً ورحمة، اللهم ذكّرني منه ما نُسّيت وعلّمني منه ما جهلت، وارزقني تلاوته آناء الليل وأطراف النهار، واجعله لي حجةً يا رب العالمين.",
                                color = SakinaOrbitalColors.WarmCream,
                                fontSize = 12.sp,
                                lineHeight = 21.sp,
                                fontFamily = SakinaBodyFont,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x7706101F))
                                    .padding(10.dp)
                            )
                        }

                        if (bookmarks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = sakinaTr(lang, "المرجعيات المحفوظة 🔖", "Saved Bookmarks 🔖", "Signets enregistrés 🔖", "Kayıtlı Yer İmleri 🔖", "Penanda Tersimpan 🔖", "محفوظ نشانات 🔖"),
                                color = SakinaLuxuryColors.SoftGoldGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaBodyFont
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                bookmarks.take(4).forEach { bm ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.85f))
                                            .border(0.8.dp, SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                                            .clickable { onOpenQuranPage(bm.page) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${bm.surahName} · ص ${bm.page}",
                                            color = SakinaLuxuryColors.SoftGoldGlow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = SakinaBodyFont,
                                            style = TextStyle(fontFeatureSettings = "tnum")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                // 4. حصيلة يومي للأعمال الصالحة (12 عملاً صالحاً نبوياً)
                val dailyDeedsList = remember {
                    listOf(
                        "duha" to "صلاة الضحى",
                        "rawatib" to "السنن الرواتب",
                        "qiyam" to "قيام الليل والوتر",
                        "sadaqah" to "صدقة اليوم ولو بالقليل",
                        "kind_word" to "الكلمة الطيبة والتبسّم",
                        "harm_road" to "إماطة الأذى عن الطريق",
                        "neighbor" to "الإحسان إلى الجار",
                        "kinship" to "برّ الوالدين وصلة الرحم",
                        "ease_debt" to "التجاوز عن معسر أو تفريج كربة",
                        "orphan_sick" to "مسح رأس يتيم أو عيادة مريض",
                        "salawat" to "الصلاة على النبي ﷺ (100×)",
                        "istighfar" to "الاستغفار والتوبة (100×)"
                    )
                }
                SakinaGlowingGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    emeraldIntensity = 0.36f
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "حصيلة يومي للأعمال الصالحة",
                                    style = TextStyle(
                                        brush = SakinaLuxuryPalette.MetallicGoldTextBrush,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SakinaHeadingFont
                                    )
                                )
                                Text(
                                    text = "سجّل ما وفّقك الله إليه من أبواب الخير اليوم",
                                    color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.72f),
                                    fontSize = 11.sp,
                                    fontFamily = SakinaBodyFont
                                )
                            }
                            Text(
                                text = "${deedsDone.size} / ${dailyDeedsList.size}",
                                color = SakinaLuxuryColors.EmeraldGlow,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SakinaBodyFont,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        dailyDeedsList.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { (id, label) ->
                                    val done = deedsDone.contains(id)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (done) SakinaLuxuryColors.DeepEmerald.copy(alpha = 0.92f)
                                                else Color(0x5506101F)
                                            )
                                            .border(
                                                width = if (done) 1.1.dp else 0.7.dp,
                                                color = if (done) SakinaLuxuryColors.EmeraldGlow else SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.42f),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                SakinaV45StatsPrefs.toggleGoodDeedToday(ctx, id)
                                                tick++
                                            }
                                            .padding(horizontal = 9.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = if (done) "✓ $label" else "○ $label",
                                            color = if (done) SakinaLuxuryColors.SoftGoldGlow else SakinaOrbitalColors.WarmCream.copy(alpha = 0.85f),
                                            fontSize = 11.sp,
                                            fontWeight = if (done) FontWeight.Bold else FontWeight.Medium,
                                            fontFamily = SakinaBodyFont,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
