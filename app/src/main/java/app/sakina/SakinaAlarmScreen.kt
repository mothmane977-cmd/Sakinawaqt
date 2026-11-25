package app.sakina

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SakinaAlarmScreen(ctx: Context, initialSection: Int = 0, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler {
        SakinaAdhanAudio.stop()
        onBackClick()
    }
    DisposableEffect(Unit) {
        onDispose { SakinaAdhanAudio.stop() }
    }

    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    var suhoorEnabled by remember { mutableStateOf(sp.getBoolean("suhoor_alarm_on", true)) }
    var suhoorMinutesBeforeFajr by remember { mutableIntStateOf(sp.getInt("suhoor_mins", 40)) }
    var adhanVolume by remember { mutableFloatStateOf(sp.getFloat("adhan_vol", 0.85f)) }
    var fajrGentleFade by remember { mutableStateOf(Prefs.fajrFade(ctx)) }
    var defaultAdhanMode by remember { mutableIntStateOf(sp.getInt("default_adhan_mode", 0)) }
    var selectedVoice by remember { mutableStateOf(SakinaAdhanAudio.selectedVoice(ctx)) }
    var playingVoiceKey by remember { mutableStateOf<String?>(null) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var alarmTab by remember(initialSection) { mutableIntStateOf(initialSection) } // 0 = المنبهات الصوتية, 1 = التنبيهات الهادئة
    var alarmSubTab by remember { mutableIntStateOf(0) } // 0 = أنواع المنبهات, 1 = المؤذنون والصوت
    var fajrSnooze by remember { mutableStateOf(SakinaAdvancedPrefs.fajrSnoozeEnabled(ctx)) }
    var qiyamOn by remember { mutableStateOf(SakinaAdvancedPrefs.qiyamAlarmEnabled(ctx)) }
    var qiyamMin by remember { mutableIntStateOf(SakinaAdvancedPrefs.qiyamAlarmMinutesBefore(ctx)) }
    var iqamaOn by remember { mutableStateOf(SakinaAdvancedPrefs.iqamaAlarmEnabled(ctx)) }
    var iqamaMin by remember { mutableIntStateOf(SakinaAdvancedPrefs.iqamaMinutesAfter(ctx)) }
    var postAdhkarOn by remember { mutableStateOf(SakinaAdvancedPrefs.postPrayerAdhkarNotif(ctx)) }
    var fastingOn by remember { mutableStateOf(SakinaAdvancedPrefs.voluntaryFastingNotif(ctx)) }
    var kahfOn by remember { mutableStateOf(SakinaAdvancedPrefs.fridayKahfNotif(ctx)) }
    var liveNotifOn by remember { mutableStateOf(SakinaAdvancedPrefs.persistentNotifEnabled(ctx)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 76.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SakinaBackIconButton(
                onClick = {
                    SakinaAdhanAudio.stop()
                    onBackClick()
                }
            )
            Spacer(Modifier.width(8.dp))
            SakinaOrnamentCartouche(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = if (alarmTab == 1) sakinaTr(lang, "التنبيهات الهادئة", "Quiet Notifications", "Notifications discrètes", "Sessiz Bildirimler", "Notifikasi Tenang", "خاموش اطلاعات") else sakinaTr(lang, "المنبهات الصوتية", "Sound Alarms", "Alarmes sonores", "Sesli Alarmlar", "Alarm Suara", "صوتی الارم"),
                    style = TextStyle(
                        fontFamily = SakinaEditorialFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        brush = SakinaCalligraphyBrush()
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        // v18 Point 13: Clear separation between Active Alarms (المنبهات) and Quiet Notifications (التنبيهات)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(theme.bgCard.copy(alpha = 0.88f))
                .border(1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(18.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(sakinaTr(lang, "المنبهات الصوتية", "Sound Alarms", "Alarmes sonores", "Sesli Alarmlar", "Alarm Suara", "صوتی الارم"), sakinaTr(lang, "التنبيهات الهادئة", "Quiet Notifications", "Notifications", "Bildirimler", "Notifikasi", "اطلاعات")).forEachIndexed { idx, label ->
                val sel = alarmTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (sel) SakinaLuxuryColors.DeepEmerald else Color.Transparent)
                            .border(if (sel) 1.dp else 0.dp, if (sel) SakinaLuxuryColors.SoftGoldGlow else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { alarmTab = idx }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = SakinaEditorialFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = if (sel) SakinaLuxuryColors.SoftGoldGlow else SakinaBrandColors.LuminousIvory.copy(alpha = 0.72f)
                    )
                }
            }
        }

        if (alarmTab == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xEE05141C))
                    .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(14.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(sakinaTr(lang, "أنواع المنبهات", "Alarm Modes", "Types d'alarmes", "Alarm Türleri", "Jenis Alarm", "الارم اقسام"), sakinaTr(lang, "المؤذنون والصوت", "Muezzins & Audio", "Muezzins & Audio", "Müezzinler & Ses", "Muadzin & Suara", "مؤذن اور آواز")).forEachIndexed { idx, label ->
                    val sel = alarmSubTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (sel) SakinaLuxuryColors.DeepEmerald else Color.Transparent)
                            .border(if (sel) 1.dp else 0.dp, if (sel) SakinaLuxuryColors.SoftGoldGlow else Color.Transparent, RoundedCornerShape(11.dp))
                            .clickable { alarmSubTab = idx }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = if (sel) SakinaLuxuryColors.SoftGoldGlow else SakinaBrandColors.LuminousIvory.copy(alpha = 0.72f)
                        )
                    }
                }
            }
        }

        if (alarmTab == 1) {
            var expandLiveNotif by remember { mutableStateOf(true) }
            var expandSpiritualReminders by remember { mutableStateOf(true) }

            // 1. بطاقة الإشعارات الصامتة المنسدلة للأسفل
            SakinaGlowingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                onClick = { expandLiveNotif = !expandLiveNotif }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(sakinaTr(lang, "الإشعارات الصامتة وشريط الصلاة الحي", "Silent & Live Notifications", "Notifications silencieuses", "Sessiz Bildirimler", "Notifikasi Senyap", "خاموش اطلاعات"), fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SakinaLuxuryColors.SoftGoldGlow)
                        Text(if (expandLiveNotif) "▴" else "▾", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 15.sp)
                    }
                    androidx.compose.animation.AnimatedVisibility(visible = expandLiveNotif) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(sakinaTr(lang, "الإشعار الحي الدائم لمواقيت الصلاة", "Persistent Live Prayer Bar", "Barre de prière permanente", "Kalıcı Namaz Bildirimi", "Notifikasi Jadwal Sholat Aktif", "مستقل لائیو نماز نوٹیفکیشن"), fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                                SakinaGoldSwitch(checked = liveNotifOn, onCheckedChange = {
                                    liveNotifOn = it; SakinaAdvancedPrefs.setPersistentNotifEnabled(ctx, it); SakinaLiveNotifier.update(ctx)
                                })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(sakinaTr(lang, "إشعار أذكار ما بعد الصلاة (بعد 12 د)", "Post-Prayer Adhkar Alert (12m)", "Alerte Adhkar après prière (12 min)", "Namaz Sonrası Zikir (12 dk)", "Notifikasi Dzikir Ba'da Sholat", "نماز کے بعد اذکار"), fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                                SakinaGoldSwitch(checked = postAdhkarOn, onCheckedChange = {
                                    postAdhkarOn = it; SakinaAdvancedPrefs.setPostPrayerAdhkarNotif(ctx, it)
                                })
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            // 2. بطاقة التذكيرات الروحانية المنسدلة للأسفل
            SakinaGlowingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                onClick = { expandSpiritualReminders = !expandSpiritualReminders }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(sakinaTr(lang, "التذكيرات الروحانية والمناسبات", "Spiritual Reminders", "Rappels spirituels", "Manevi Hatırlatıcılar", "Pengingat Ibadah", "روحانی یاد دہانیاں"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SakinaLuxuryColors.SoftGoldGlow)
                        Text(if (expandSpiritualReminders) "▴" else "▾", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 15.sp)
                    }
                    androidx.compose.animation.AnimatedVisibility(visible = expandSpiritualReminders) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(sakinaTr(lang, "تذكير ليلي بصيام التطوع والأيام البيض", "Voluntary & White Days Fasting", "Rappel du jeûne surérogatoire", "Nafile Oruç Hatırlatıcısı", "Pengingat Puasa Sunnah", "نفلی روزوں کی یاد دہانی"), fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                                SakinaGoldSwitch(checked = fastingOn, onCheckedChange = {
                                    fastingOn = it; SakinaAdvancedPrefs.setVoluntaryFastingNotif(ctx, it); try { AlarmScheduler.scheduleNext(ctx) } catch (_: Exception) {}
                                })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(sakinaTr(lang, "تذكير سورة الكهف صباح الجمعة", "Friday Surah Al-Kahf Reminder", "Rappel Sourate Al-Kahf (vendredi)", "Cuma Kehf Suresi Hatırlatıcısı", "Pengingat Surah Al-Kahf Jumat", "جمعہ سورۃ الکہف یاد دہانی"), fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                                SakinaGoldSwitch(checked = kahfOn, onCheckedChange = {
                                    kahfOn = it; SakinaAdvancedPrefs.setFridayKahfNotif(ctx, it)
                                })
                            }
                        }
                    }
                }
            }
            if (false) { // suppress old flat block
                Text(sakinaTr(lang, "الإشعارات والتذكيرات الروحانية (مفصولة حسب الوظيفة)", "Quiet Notifications & Alerts", "Notifications et alertes discrètes", "Sessiz Bildirimler", "Notifikasi Tenang", "خاموش اطلاعات"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = theme.accentBright)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(sakinaTr(lang, "الإشعار الحي الدائم لمواقيت الصلاة", "Persistent Live Prayer Bar", "Barre de prière permanente", "Kalıcı Namaz Bildirimi", "Notifikasi Jadwal Sholat Aktif", "مستقل لائیو نماز نوٹیفکیشن"), fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                    SakinaGoldSwitch(checked = liveNotifOn, onCheckedChange = {
                        liveNotifOn = it; SakinaAdvancedPrefs.setPersistentNotifEnabled(ctx, it); SakinaLiveNotifier.update(ctx)
                    })
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(sakinaTr(lang, "تذكير أذكار ما بعد الصلاة (بعد 12 د)", "Post-Prayer Adhkar Reminder (12m)", "Rappel Adhkar après la prière (12 min)", "Namaz Sonrası Zikir Hatırlatıcı (12 dk)", "Pengingat Dzikir Setelah Sholat (12 mnt)", "نماز کے بعد اذکار یاد دہانی (12 منٹ)"), fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                    SakinaGoldSwitch(checked = postAdhkarOn, onCheckedChange = {
                        postAdhkarOn = it; SakinaAdvancedPrefs.setPostPrayerAdhkarNotif(ctx, it)
                    })
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(sakinaTr(lang, "تذكير ليلي بصيام التطوع والأيام البيض", "Evening Voluntary Fasting Reminder", "Rappel du jeûne surérogatoire", "Nafile Oruç Akşam Hatırlatıcısı", "Pengingat Puasa Sunnah", "نفلی روزوں کی یاد دہانی"), fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                    SakinaGoldSwitch(checked = fastingOn, onCheckedChange = {
                        fastingOn = it; SakinaAdvancedPrefs.setVoluntaryFastingNotif(ctx, it); try { AlarmScheduler.scheduleNext(ctx) } catch (_: Exception) {}
                    })
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(sakinaTr(lang, "تذكير سورة الكهف صباح الجمعة", "Friday Surah Al-Kahf Reminder", "Rappel Sourate Al-Kahf (vendredi)", "Cuma Kehf Suresi Hatırlatıcısı", "Pengingat Surah Al-Kahf Jumat", "جمعہ سورۃ الکہف یاد دہانی"), fontFamily = SakinaBodyFont, fontSize = 12.5.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                    SakinaGoldSwitch(checked = kahfOn, onCheckedChange = {
                        kahfOn = it; SakinaAdvancedPrefs.setFridayKahfNotif(ctx, it)
                    })
                }
            }
        } else if (alarmSubTab == 0) {
            // 13-1, 13-3, 13-4 Categorized Alarm Modes Card
            SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(sakinaTr(lang, "تصنيف المنبهات حسب الوظيفة", "Alarms by Function", "Alarmes par fonction", "İşleve Göre Alarmlar", "Alarm Berdasarkan Fungsi", "الارم کی اقسام"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = theme.accentBright)
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(sakinaTr(lang, "1. منبه الفجر المشدّد (تدرّج صوتي + غفوة)", "1. Fajr Wake-Up Alarm (Fade + Snooze)", "1. Alarme Fajr (fondu + rappel)", "1. Sabah Namazı Alarmı (Kademeli)", "1. Alarm Subuh (Halus + Tunda)", "1. فجر الارم (تدریجی آواز)"), fontFamily = SakinaBodyFont, fontSize = 12.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                    SakinaGoldSwitch(checked = fajrSnooze, onCheckedChange = {
                        fajrSnooze = it; SakinaAdvancedPrefs.setFajrSnoozeEnabled(ctx, it); Prefs.setFajrFade(ctx, it)
                    })
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(sakinaTr(lang, "2. منبه قيام الليل والسحور (قبل الفجر بـ $qiyamMin د)", "2. Qiyam & Suhoor ($qiyamMin min before Fajr)", "2. Qiyam & Suhoor ($qiyamMin min avant Fajr)", "3. Teheccüd & Sahur (Fecrden $qiyamMin dk önce)", "3. Tahajud & Sahur ($qiyamMin mnt sebelum Subuh)", "3. قیام اللیل اور سحری (فجر سے $qiyamMin منٹ پہلے)").toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 12.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                    SakinaGoldSwitch(checked = qiyamOn, onCheckedChange = {
                        qiyamOn = it; SakinaAdvancedPrefs.setQiyamAlarmEnabled(ctx, it); try { AlarmScheduler.scheduleNext(ctx) } catch (_: Exception) {}
                    })
                }
                if (qiyamOn) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(30, 45, 60).forEach { m ->
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                    .background(if (qiyamMin == m) theme.accentGold else Color(0x22FFFFFF))
                                    .clickable { qiyamMin = m; SakinaAdvancedPrefs.setQiyamAlarmMinutesBefore(ctx, m); try { AlarmScheduler.scheduleNext(ctx) } catch (_: Exception) {} }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) { Text("${m} د".toWesternDigits(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (qiyamMin == m) theme.bgDeep else SakinaBrandColors.LuminousIvory) }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(sakinaTr(lang, "3. منبه الإقامة (بعد الأذان بـ $iqamaMin د)", "3. Iqama Alarm ($iqamaMin min after Adhan)", "4. Alarme Iqama ($iqamaMin min après l'Adhan)", "4. Kamet Alarmı (Ezandan $iqamaMin dk sonra)", "4. Alarm Iqamah ($iqamaMin mnt setelah Adzan)", "4. اقامت الارم (اذان کے $iqamaMin منٹ بعد)").toWesternDigits(), fontFamily = SakinaBodyFont, fontSize = 12.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f))
                    SakinaGoldSwitch(checked = iqamaOn, onCheckedChange = {
                        iqamaOn = it; SakinaAdvancedPrefs.setIqamaAlarmEnabled(ctx, it)
                    })
                }
                if (iqamaOn) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(10, 15, 20).forEach { m ->
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                    .background(if (iqamaMin == m) theme.accentGold else Color(0x22FFFFFF))
                                    .clickable { iqamaMin = m; SakinaAdvancedPrefs.setIqamaMinutesAfter(ctx, m) }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) { Text("${m} د".toWesternDigits(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (iqamaMin == m) theme.bgDeep else SakinaBrandColors.LuminousIvory) }
                        }
                    }
                }
            }
        }

        if (alarmTab == 0 && alarmSubTab == 1) {
        // 1. Compact Adhan Voice Selector Card with Preview + Change Dialog
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowGold = true,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = s.adhanVoiceLabel,
                        fontFamily = SakinaHeadingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = theme.accentBright
                    )
                    Text(
                        text = selectedVoice.localizedTitle(lang),
                        fontFamily = SakinaBodyFont,
                        fontSize = 12.sp,
                        color = SakinaBrandColors.LuminousIvory,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    val isPlaying = playingVoiceKey == selectedVoice.key
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) theme.accentGold else Color(0x28FFFFFF))
                            .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                            .clickable {
                                if (isPlaying) {
                                    SakinaAdhanAudio.stop()
                                    playingVoiceKey = null
                                } else {
                                    playingVoiceKey = selectedVoice.key
                                    SakinaAdhanAudio.startPreview(ctx, selectedVoice) { playingVoiceKey = null }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = if (isPlaying) theme.bgDeep else theme.accentBright,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold)))
                            .clickable { showVoiceDialog = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (lang == SakinaLang.AR || lang == SakinaLang.UR) "تغيير" else "Change",
                            fontFamily = SakinaHeadingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = theme.bgDeep
                        )
                    }
                }
            }
        }

        // 2. Suhoor & Qiyam Alarm
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowGold = suhoorEnabled,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = s.suhoorTitle,
                        fontFamily = SakinaHeadingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = theme.accentBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$suhoorMinutesBeforeFajr ${s.minsBeforeFajr}".toWesternDigits(),
                        fontFamily = SakinaBodyFont,
                        fontSize = 11.sp,
                        color = SakinaBrandColors.SoftSilver,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                SakinaGoldSwitch(
                    checked = suhoorEnabled,
                    onCheckedChange = {
                        suhoorEnabled = it
                        sp.edit().putBoolean("suhoor_alarm_on", it).apply()
                    }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(20, 30, 40, 60).forEach { mins ->
                    val sel = suhoorMinutesBeforeFajr == mins
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(27.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (sel) theme.accentGold else Color(0x22FFFFFF))
                            .clickable {
                                suhoorMinutesBeforeFajr = mins
                                sp.edit().putInt("suhoor_mins", mins).apply()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$mins ${s.minuteShort}".toWesternDigits(),
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (sel) SakinaLuxuryColors.SoftGoldGlow else SakinaBrandColors.LuminousIvory.copy(alpha = 0.72f),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 3. Volume & Fajr Fade
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Text(
                text = "${s.volumeLabel}: ${(adhanVolume * 100).roundToInt()}%".toWesternDigits(),
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = theme.accentBright,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Slider(
                value = adhanVolume,
                onValueChange = {
                    adhanVolume = it
                    sp.edit().putFloat("adhan_vol", it).apply()
                },
                colors = SliderDefaults.colors(
                    thumbColor = theme.accentBright,
                    activeTrackColor = theme.accentGold,
                    inactiveTrackColor = Color(0x33FFFFFF)
                ),
                modifier = Modifier.height(32.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = s.fajrFadeLabel,
                    fontFamily = SakinaBodyFont,
                    fontSize = 12.sp,
                    color = SakinaBrandColors.LuminousIvory,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SakinaGoldSwitch(
                    checked = fajrGentleFade,
                    onCheckedChange = {
                        fajrGentleFade = it
                        Prefs.setFajrFade(ctx, it)
                    }
                )
            }
        }

        // 4. Default Sound Mode
        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Text(
                text = s.defaultSoundLabel,
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = theme.accentBright,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                s.soundModes.forEachIndexed { idx, label ->
                    val sel = defaultAdhanMode == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(29.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (sel) theme.accentGold else Color(0x22FFFFFF))
                            .clickable {
                                defaultAdhanMode = idx
                                sp.edit().putInt("default_adhan_mode", idx).apply()
                                listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء").forEach { p ->
                                    Modes.set(ctx, p, idx)
                                }
                                AlarmScheduler.scheduleNext(ctx)
                            }
                            .padding(horizontal = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = SakinaBodyFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (sel) SakinaLuxuryColors.SoftGoldGlow else SakinaBrandColors.LuminousIvory.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 5. Test Alarm Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold)))
                .border(1.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(20.dp))
                .clickable {
                    AlarmScheduler.scheduleTest(ctx)
                    Toast.makeText(ctx, s.testAlarmScheduled.toWesternDigits(), Toast.LENGTH_SHORT).show()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = s.testAlarmBtn.toWesternDigits(),
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = theme.bgDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        }
    }

    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = {
                SakinaAdhanAudio.stop()
                playingVoiceKey = null
                showVoiceDialog = false
            },
            containerColor = theme.bgCard,
            title = { Text(s.adhanVoiceLabel, fontFamily = SakinaHeadingFont, color = theme.accentBright) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdhanVoice.entries.forEach { voice ->
                        val isSel = voice == selectedVoice
                        val isPlaying = playingVoiceKey == voice.key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) theme.accentGold.copy(alpha = 0.25f) else Color(0x22FFFFFF))
                                .border(0.8.dp, if (isSel) Sakina3DGoldBevelBrush else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)), RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedVoice = voice
                                    SakinaAdhanAudio.setSelectedVoice(ctx, voice)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (if (isSel) "✓ " else "• ") + voice.localizedTitle(lang),
                                fontFamily = SakinaBodyFont,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.5.sp,
                                color = if (isSel) theme.accentBright else SakinaBrandColors.LuminousIvory,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) theme.accentGold else Color(0x28FFFFFF))
                                    .clickable {
                                        if (isPlaying) {
                                            SakinaAdhanAudio.stop()
                                            playingVoiceKey = null
                                        } else {
                                            selectedVoice = voice
                                            SakinaAdhanAudio.setSelectedVoice(ctx, voice)
                                            playingVoiceKey = voice.key
                                            SakinaAdhanAudio.startPreview(ctx, voice) { playingVoiceKey = null }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isPlaying) theme.bgDeep else theme.accentBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    SakinaAdhanAudio.stop()
                    playingVoiceKey = null
                    showVoiceDialog = false
                }) { Text("×", color = theme.accentBright, fontSize = 18.sp) }
            }
        )
    }
}
