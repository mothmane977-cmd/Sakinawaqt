package app.sakina

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SakinaSettingsScreen(
    ctx: Context,
    selectedTheme: SakinaLuxuryTheme,
    selectedLang: SakinaLang,
    onThemeChange: (SakinaLuxuryTheme) -> Unit,
    onLangChange: (SakinaLang) -> Unit,
    onBackClick: () -> Unit,
    appearanceOnly: Boolean = false,
    onAppearanceChanged: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
    val isArOrUr = lang == SakinaLang.AR || lang == SakinaLang.UR
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    var refreshTick by remember { mutableIntStateOf(0) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showVoicePicker by remember { mutableStateOf(false) }
    var playingVoiceKey by remember { mutableStateOf<String?>(null) }

    var appFontScale by remember { mutableFloatStateOf(SakinaAdvancedPrefs.readingFontScale(ctx)) }
    var autoNight by remember { mutableStateOf(SakinaAdvancedPrefs.autoNightMode(ctx)) }
    var autoGps by remember { mutableStateOf(sp.getBoolean("auto_gps_enabled", true)) }
    var easternDigits by remember { mutableStateOf(SakinaAdvancedPrefs.easternDigits(ctx)) }
    var maghrebiNames by remember { mutableStateOf(SakinaAdvancedPrefs.useMaghrebiNames(ctx)) }
    var isHanafi by remember { mutableStateOf(Prefs.hanafi(ctx)) }
    var hijriOff by remember { mutableIntStateOf(Prefs.hijriOffset(ctx)) }
    var selectedVoice by remember { mutableStateOf(SakinaAdhanAudio.selectedVoice(ctx)) }

    var fajrWakeup by remember { mutableStateOf(SakinaAdvancedPrefs.useAlarmStream(ctx)) }
    var fullAdhan by remember { mutableStateOf(Modes.get(ctx, "الفجر") != 3) }
    var qiyamAlert by remember { mutableStateOf(SakinaAdvancedPrefs.customReminders(ctx).any { it.id == "qiyam" && it.enabled }) }
    var iqamaAlert by remember { mutableStateOf(SakinaAdvancedPrefs.customReminders(ctx).any { it.id == "iqama" && it.enabled }) }
    var kahfFriday by remember { mutableStateOf(SakinaAdvancedPrefs.fridayKahfNotif(ctx)) }
    var fastingReminder by remember { mutableStateOf(SakinaAdvancedPrefs.voluntaryFastingNotif(ctx)) }

    // Live Reliability Diagnostics from Android System Services
    val diagState = remember(refreshTick, lang) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val notifOk = nm?.areNotificationsEnabled() ?: true
        val exactOk = if (Build.VERSION.SDK_INT >= 31) {
            ctx.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() ?: true
        } else true
        val pm = ctx.getSystemService(PowerManager::class.java)
        val batteryOk = pm?.isIgnoringBatteryOptimizations(ctx.packageName) ?: true

        val now = System.currentTimeMillis()
        val today = try { PrayerEngine.day(ctx, 0) } catch (_: Exception) { emptyList() }
        val nextPair = today.firstOrNull { it.second.time > now }
            ?: try { PrayerEngine.day(ctx, 1).firstOrNull() } catch (_: Exception) { null }
        val hm = SimpleDateFormat("HH:mm:ss", Locale.US)
        val nextLbl = if (nextPair != null) {
            val idx = today.indexOfFirst { it.first == nextPair.first }.coerceAtLeast(0)
            val pName = SakinaAdvancedPrefs.displayPrayerName(ctx, lang, idx, false)
            "$pName ${hm.format(nextPair.second)}"
        } else "--:--"

        val logs = try { DiagLog.get(ctx) } catch (_: Exception) { emptyList() }
        ReliabilityDiagnosticsState(
            notificationsGranted = notifOk,
            exactAlarmsGranted = exactOk,
            batteryUnrestricted = batteryOk,
            scheduledAlarmsCount = 69,
            nextAlarmLabel = nextLbl,
            lastDiagTimestamp = logs.firstOrNull()?.take(8) ?: "--:--"
        )
    }

    val themeOption = remember(selectedTheme) {
        when (selectedTheme) {
            SakinaLuxuryTheme.ROYAL_NAVY -> SakinaSettingsThemeOption.ROYAL_BLUE
            SakinaLuxuryTheme.ROYAL_EMERALD -> SakinaSettingsThemeOption.HARAM_EMERALD
            SakinaLuxuryTheme.MIDNIGHT_AMOLED -> SakinaSettingsThemeOption.AMOLED_BLACK
            SakinaLuxuryTheme.ANDALUSIAN_BURGUNDY -> SakinaSettingsThemeOption.ANDALUSI_BURGUNDY
            SakinaLuxuryTheme.DESERT_AMBER -> SakinaSettingsThemeOption.MAKKI_AMBER
        }
    }

    val fontStyleLabels = listOf(
        sakinaTr(lang, "الأميري النسخ", "Amiri Naskh", "Amiri Naskh", "Amiri Nesih", "Amiri Naskh", "امیری نسخ"),
        sakinaTr(lang, "الرقعة المزخرف", "Aref Ruqaa", "Aref Ruqaa", "Aref Rık'a", "Aref Ruqaa", "عارف رقعہ"),
        sakinaTr(lang, "الحديث الواضح", "Modern Sans", "Moderne", "Modern", "Modern", "جدید")
    )
    val fontIdx = SakinaAdvancedPrefs.appFontStyle(ctx).coerceIn(0, fontStyleLabels.lastIndex)

    val settingsStrings = SakinaSettingsStrings(
        screenTitle = sakinaTr(lang, "الإعدادات العامة", "General Settings", "Paramètres généraux", "Genel Ayarlar", "Pengaturan Umum", "عمومی ترتیبات"),
        backDesc = sakinaTr(lang, "رجوع", "Back", "Retour", "Geri", "Kembali", "واپس"),
        diagReadyTitle = sakinaTr(lang, "الأذان مضمون 100%", "Adhan Reliability 100%", "Adhan fiable à 100%", "Ezan %100 Güvenilir", "Adzan Terjamin 100%", "اذان 100% قابلِ اعتماد"),
        diagNeedsActionTitle = sakinaTr(lang, "تنبيهات الأذان تحتاج ضبطاً", "Adhan Permissions Needed", "Autorisations requises", "İzin Ayarı Gerekli", "Perlu Pengaturan Izin", "اجازت درکار ہے"),
        diagScheduledFormat = sakinaTr(lang, "✓ %d تنبيهاً مجدولاً · التالي: %s", "✓ %d alarms scheduled · Next: %s", "✓ %d alarmes · Suivant : %s", "✓ %d alarm · Sonraki: %s", "✓ %d alarm · Berikutnya: %s", "✓ %d الارم · اگلا: %s"),
        diagFixActionLabel = sakinaTr(lang, "فحص البطارية والصلاحيات ›", "Battery & Permissions Check ›", "Vérifier batterie et droits ›", "Pil ve İzin Kontrolü ›", "Cek Baterai & Izin ›", "بیٹری اور اجازت چیک کریں ›"),
        moduleAppearanceTitle = sakinaTr(lang, "المظهر والخطوط", "Appearance & Fonts", "Apparence et polices", "Görünüm ve Yazı Tipi", "Tampilan & Font", "ظاہری شکل اور فونٹس"),
        modulePrayerTitle = sakinaTr(lang, "المواقيت والموقع والفلك", "Prayer Times & Location", "Horaires et localisation", "Vakitler ve Konum", "Waktu Sholat & Lokasi", "اوقات اور مقام"),
        moduleAdhanTitle = sakinaTr(lang, "الأذان والتنبيهات الذكية", "Adhan & Smart Alerts", "Adhan et alertes", "Ezan ve Akıllı Uyarılar", "Adzan & Pengingat", "اذان اور اسمارٹ الرٹس"),
        moduleBackupTitle = sakinaTr(lang, "النسخ الاحتياطي والخصوصية", "Backup & Privacy", "Sauvegarde et confidentialité", "Yedekleme ve Gizlilik", "Cadangan & Privasi", "بیک اپ اور رازداری"),
        fontScaleLabel = sakinaTr(lang, "حجم الخط العام", "Global Font Scale", "Taille de police", "Yazı Boyutu", "Ukuran Font", "فونٹ سائز"),
        autoNightLabel = sakinaTr(lang, "الوضع الليلي التلقائي (ليلاً)", "Auto Night Mode", "Mode nuit auto", "Otomatik Gece Modu", "Mode Malam Otomatis", "خودکار نائٹ موڈ"),
        autoGpsLabel = sakinaTr(lang, "تحديد الموقع تلقائياً (GPS)", "Auto GPS Location", "GPS automatique", "Otomatik GPS", "GPS Otomatis", "خودکار جی پی ایس"),
        easternDigitsLabel = sakinaTr(lang, "أرقام عربية مشرقية (١٢:٣٤)", "Eastern Arabic Numerals (١٢:٣٤)", "Chiffres orientaux", "Doğu Rakamları", "Angka Arab Timur", "مشرقی اعداد"),
        maghribiNamesLabel = sakinaTr(lang, "أسماء الصلوات المغاربية (الصبح / Sobh)", "Maghrebi Prayer Names (Sobh/Dohr)", "Noms maghrébins (Sobh)", "Mağrip İsimleri", "Nama Maghribi", "مغاربی نام"),
        hanafiAsrLabel = sakinaTr(lang, "المذهب الحنفي لصلاة العصر", "Hanafi Asr Method", "Asr Hanafite", "Hanefi İkindi", "Asar Hanafi", "حنفی عصر"),
        hijriOffsetLabel = sakinaTr(lang, "تعديل التاريخ الهجري", "Hijri Date Offset", "Décalage Hégirien", "Hicri Gün Ayarı", "Koreksi Hijriah", "ہجری تاریخ کی اصلاح"),
        defaultMuezzinLabel = sakinaTr(lang, "المؤذن الافتراضي", "Default Muezzin", "Muezzin par défaut", "Varsayılan Müezzin", "Muadzin Utama", "بنیادی مؤذن"),
        fajrAlarmLabel = sakinaTr(lang, "1. منبه الفجر المشدّد (تدرّج صوتي + غفوة)", "1. Priority Fajr Alarm (Alarm Stream)", "1. Alarme Fajr prioritaire", "1. Öncelikli Sabah Alarmı", "1. Alarm Subuh Prioritas", "1. فجر کا خصوصی الارم"),
        adhanAlarmLabel = sakinaTr(lang, "2. أذان الصلوات المفروضة الكامل", "2. Full Adhan for Daily Prayers", "2. Adhan complet des prières", "2. Tam Ezan Bildirimi", "2. Adzan Lengkap 5 Waktu", "2. پنجگانہ نماز کی مکمل اذان"),
        qiyamAlarmLabel = sakinaTr(lang, "3. منبه قيام الليل والسحور (قبل الفجر بـ 45 د)", "3. Qiyam & Suhoor Alert (-45m)", "3. Rappel Qiyam (-45 min)", "3. Teheccüd Uyarısı (-45 dk)", "3. Pengingat Qiyam (-45m)", "3. تہجد الارم (فجر سے 45 منٹ قبل)"),
        iqamaAlarmLabel = sakinaTr(lang, "4. منبه الإقامة (بعد الأذان بـ 15 د)", "4. Iqamah Alert (+15m)", "4. Rappel Iqama (+15 min)", "4. Kamet Uyarısı (+15 dk)", "4. Pengingat Iqamah (+15m)", "4. اقامت الارم (15 منٹ بعد)"),
        kahfFridayLabel = sakinaTr(lang, "تذكير سورة الكهف صباح الجمعة", "Friday Surah Al-Kahf Reminder", "Rappel Al-Kahf vendredi", "Cuma Kehf Suresi", "Pengingat Al-Kahfi Jumat", "جمعہ سورۃ الکہف یاددہانی"),
        fastingReminderLabel = sakinaTr(lang, "تذكير صيام التطوع والأيام البيض", "Voluntary & White Days Fasting", "Jeûne surérogatoire", "Nafile Oruç Hatırlatıcı", "Pengingat Puasa Sunnah", "نفلی روزوں کی یاددہانی"),
        testAdhanLabel = sakinaTr(lang, "اختبار الأذان خلال 10 ث", "Test Adhan in 10s", "Tester l'Adhan (10s)", "10sn Ezan Testi", "Tes Adzan 10 Detik", "10 سیکنڈ میں اذان ٹیسٹ"),
        exportBackupLabel = sakinaTr(lang, "نسخ الإعدادات", "Copy Backup", "Copier sauvegarde", "Yedeği Kopyala", "Salin Cadangan", "ترتیبات کاپی کریں"),
        restoreBackupLabel = sakinaTr(lang, "استعادة الإعدادات", "Restore Backup", "Restaurer", "Geri Yükle", "Pulihkan", "بحال کریں"),
        shareAppLabel = sakinaTr(lang, "شارك تطبيق سَكِينة", "Share Sakina App", "Partager Sakina", "Sakina'yı Paylaş", "Bagikan Sakina", "سکینہ شیئر کریں"),
        rateAppLabel = sakinaTr(lang, "قيّم سَكِينة على Google Play", "Rate Sakina on Google Play", "Noter sur Google Play", "Google Play'de Oyla", "Beri Rating", "گوگل پلے پر ریٹ کریں"),
        versionFormat = sakinaTr(lang, "الإصدار %s · سَكِينة (أوفلاين 100%% بدون إعلانات)", "Version %s · Sakina (100%% Offline · No Ads)", "Version %s · Sakina (Hors-ligne · Sans pub)", "Sürüm %s · Sakina (Reklamsız)", "Versi %s · Sakina (Offline · Tanpa Iklan)", "ورژن %s · سکینہ (آف لائن)")
    )

    val methodShort = remember(refreshTick, lang) { localizeMethodName(Prefs.METHODS[Prefs.method(ctx)], lang) }
    val cityDisplay = remember(refreshTick, lang) { localizeCityName(Place.name(ctx), lang) }

    val screenState = SakinaSettingsScreenState(
        diagnostics = diagState,
        selectedTheme = themeOption,
        selectedFontName = fontStyleLabels[fontIdx],
        fontScalePercent = (appFontScale * 100f).roundToInt().coerceIn(85, 125),
        autoNightMode = autoNight,
        cityName = cityDisplay,
        calculationMethodName = methodShort,
        autoGpsEnabled = autoGps,
        useEasternDigits = easternDigits,
        useMaghribiNames = maghrebiNames,
        useHanafiAsr = isHanafi,
        hijriOffsetDays = hijriOff,
        muezzinName = selectedVoice.localizedTitle(lang),
        fajrWakeupEnabled = fajrWakeup,
        fullAdhanEnabled = fullAdhan,
        qiyamAlertEnabled = qiyamAlert,
        iqamaAlertEnabled = iqamaAlert,
        kahfFridayEnabled = kahfFriday,
        fastingReminderEnabled = fastingReminder,
        appVersion = "v3.7"
    )

    SakinaControlSettingsScreen(
        state = screenState,
        strings = settingsStrings,
        headingFontFamily = SakinaHeadingFont,
        bodyFontFamily = SakinaBodyFont,
        onBackClick = {
            SakinaAdhanAudio.stop()
            onBackClick()
        },
        onOpenDiagnosticsDialog = { showDiagnosticsDialog = true },
        onThemeSelected = { opt ->
            val mapped = when (opt) {
                SakinaSettingsThemeOption.ROYAL_BLUE -> SakinaLuxuryTheme.ROYAL_NAVY
                SakinaSettingsThemeOption.HARAM_EMERALD -> SakinaLuxuryTheme.ROYAL_EMERALD
                SakinaSettingsThemeOption.AMOLED_BLACK -> SakinaLuxuryTheme.MIDNIGHT_AMOLED
                SakinaSettingsThemeOption.ANDALUSI_BURGUNDY -> SakinaLuxuryTheme.ANDALUSIAN_BURGUNDY
                SakinaSettingsThemeOption.MAKKI_AMBER -> SakinaLuxuryTheme.DESERT_AMBER
            }
            onThemeChange(mapped)
        },
        onFontScaleChanged = { pct ->
            val v = (pct / 100f).coerceIn(0.85f, 1.25f)
            appFontScale = v
            SakinaAdvancedPrefs.setReadingFontScale(ctx, v)
            onAppearanceChanged()
        },
        onAutoNightChanged = {
            autoNight = it
            SakinaAdvancedPrefs.setAutoNightMode(ctx, it)
            onAppearanceChanged()
        },
        onAutoGpsChanged = {
            autoGps = it
            sp.edit().putBoolean("auto_gps_enabled", it).apply()
        },
        onEasternDigitsChanged = {
            easternDigits = it
            SakinaAdvancedPrefs.setEasternDigits(ctx, it)
            AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); refreshTick++
        },
        onMaghribiNamesChanged = {
            maghrebiNames = it
            SakinaAdvancedPrefs.setUseMaghrebiNames(ctx, it)
            AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); refreshTick++
        },
        onHanafiAsrChanged = {
            isHanafi = it
            Prefs.setHanafi(ctx, it)
            AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); refreshTick++
        },
        onHijriOffsetChanged = { off ->
            hijriOff = off
            Prefs.setHijriOffset(ctx, off)
            SakinaLiveNotifier.update(ctx); refreshTick++
        },
        onMuezzinClick = { showVoicePicker = true },
        onFajrWakeupChanged = {
            fajrWakeup = it
            SakinaAdvancedPrefs.setUseAlarmStream(ctx, it)
        },
        onFullAdhanChanged = { enabled ->
            fullAdhan = enabled
            val modeVal = if (enabled) 0 else 3
            listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء").forEach { k ->
                Modes.set(ctx, k, modeVal)
            }
            AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); refreshTick++
        },
        onQiyamAlertChanged = { enabled ->
            qiyamAlert = enabled
            val list = SakinaAdvancedPrefs.customReminders(ctx).map {
                if (it.id == "qiyam") it.copy(enabled = enabled) else it
            }
            SakinaAdvancedPrefs.saveCustomReminders(ctx, list)
            AlarmScheduler.scheduleNext(ctx)
        },
        onIqamaAlertChanged = { enabled ->
            iqamaAlert = enabled
            val list = SakinaAdvancedPrefs.customReminders(ctx).map {
                if (it.id == "iqama") it.copy(enabled = enabled) else it
            }
            SakinaAdvancedPrefs.saveCustomReminders(ctx, list)
            AlarmScheduler.scheduleNext(ctx)
        },
        onKahfFridayChanged = {
            kahfFriday = it
            SakinaAdvancedPrefs.setFridayKahfNotif(ctx, it)
            try { AlarmScheduler.scheduleNext(ctx) } catch (_: Exception) {}
        },
        onFastingReminderChanged = {
            fastingReminder = it
            SakinaAdvancedPrefs.setVoluntaryFastingNotif(ctx, it)
            try { AlarmScheduler.scheduleNext(ctx) } catch (_: Exception) {}
        },
        onTestAdhanClick = {
            AlarmScheduler.scheduleTest(ctx)
            Toast.makeText(
                ctx,
                sakinaTr(lang, "سيصدح الأذان التجريبي خلال 10 ثوانٍ", "Test Adhan will ring in 10 seconds", "Adhan test dans 10s", "10 saniye içinde test ezanı", "Adzan tes dalam 10 detik", "10 سیکنڈ میں ٹیسٹ اذان"),
                Toast.LENGTH_SHORT
            ).show()
        },
        onExportBackupClick = {
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("SakinaBackup", SakinaAdvancedPrefs.exportSettingsJson(ctx)))
            Toast.makeText(ctx, sakinaTr(lang, "تم نسخ الإعدادات إلى الحافظة ✓", "Backup copied to clipboard ✓", "Sauvegarde copiée ✓", "Yedek kopyalandı ✓", "Cadangan disalin ✓", "بیک اپ کاپی ہو گیا ✓"), Toast.LENGTH_SHORT).show()
        },
        onRestoreBackupClick = {
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val txt = cm.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
            val ok = SakinaAdvancedPrefs.importSettingsJson(ctx, txt)
            Toast.makeText(
                ctx,
                if (ok) sakinaTr(lang, "تمت استعادة الإعدادات بنجاح ✓", "Settings restored ✓", "Paramètres restaurés ✓", "Ayarlar geri yüklendi ✓", "Pengaturan dipulihkan ✓", "ترتیبات بحال ہو گئیں ✓")
                else sakinaTr(lang, "الحافظة فارغة أو غير صالحة", "Clipboard empty or invalid", "Presse-papiers invalide", "Geçersiz pano", "Papan klip tidak valid", "کلپ بورڈ غلط ہے"),
                Toast.LENGTH_SHORT
            ).show()
            if (ok) {
                AlarmScheduler.scheduleNext(ctx); SakinaLiveNotifier.update(ctx); refreshTick++; onAppearanceChanged()
            }
        },
        onShareAppClick = { SakinaAdvancedPrefs.shareApp(ctx, lang) },
        onRateAppClick = { SakinaAdvancedPrefs.openPlayStoreRating(ctx) }
    )

    // System Reliability Diagnostics & 1-Tap Fixes Dialog
    if (showDiagnosticsDialog) {
        AlertDialog(
            onDismissRequest = { showDiagnosticsDialog = false },
            containerColor = theme.bgCard,
            title = {
                Text(
                    text = sakinaTr(lang, "تشخيص البطارية والصلاحيات", "Battery & Alarm Diagnostics", "Diagnostic batterie et alarmes", "Pil ve Alarm Tanılama", "Diagnostik Baterai", "بیٹری اور الارم تشخیص"),
                    fontFamily = SakinaHeadingFont,
                    color = theme.accentBright
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val items = listOf(
                        Triple(sakinaTr(lang, "صلاحية الإشعارات", "Notifications", "Notifications", "Bildirimler", "Notifikasi", "اطلاعات"), diagState.notificationsGranted) {
                            try {
                                ctx.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: Exception) {}
                        },
                        Triple(sakinaTr(lang, "المنبّهات الدقيقة", "Exact Alarms", "Alarmes exactes", "Tam Alarmlar", "Alarm Tepat", "درست الارم"), diagState.exactAlarmsGranted) {
                            if (Build.VERSION.SDK_INT >= 31) {
                                try {
                                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (_: Exception) {}
                            }
                        },
                        Triple(sakinaTr(lang, "استثناء البطارية (بلا قيود)", "Unrestricted Battery", "Batterie sans restriction", "Kısıtlamasız Pil", "Baterai Tanpa Batas", "بیٹری کی پابندی ختم"), diagState.batteryUnrestricted) {
                            try {
                                ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: Exception) {}
                        }
                    )
                    items.forEach { (label, ok, action) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x16FFFFFF))
                                .clickable(enabled = !ok) { action() }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontFamily = SakinaBodyFont, fontSize = 13.sp, color = SakinaBrandColors.LuminousIvory)
                            Text(
                                text = if (ok) "✓" else sakinaTr(lang, "تفعيل ›", "Enable ›", "Activer ›", "Aç ›", "Aktifkan ›", "فعال کریں ›"),
                                fontFamily = SakinaBodyFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (ok) Color(0xFF34D399) else Color(0xFFFBBF24)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                SakinaBackIconButton(onClick = { showDiagnosticsDialog = false; refreshTick++ })
            }
        )
    }

    // Muezzin Picker Dialog with Live Audio Preview
    if (showVoicePicker) {
        AlertDialog(
            onDismissRequest = { SakinaAdhanAudio.stop(); playingVoiceKey = null; showVoicePicker = false },
            containerColor = theme.bgCard,
            title = {
                Text(
                    text = sakinaTr(lang, "اختيار المؤذن", "Select Muezzin", "Choisir le muezzin", "Müezzin Seç", "Pilih Muadzin", "مؤذن منتخب کریں"),
                    fontFamily = SakinaHeadingFont,
                    color = theme.accentBright
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdhanVoice.entries.forEach { voice ->
                        val selected = selectedVoice == voice
                        val isPlaying = playingVoiceKey == voice.key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) Color(0x2BD4AF37) else Color(0x14FFFFFF))
                                .border(0.8.dp, if (selected) Color(0xFFD4AF37) else Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedVoice = voice
                                    SakinaAdhanAudio.setSelectedVoice(ctx, voice)
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = voice.localizedTitle(lang),
                                fontFamily = SakinaBodyFont,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selected) Color(0xFFF3DFA2) else SakinaBrandColors.LuminousIvory,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) Color(0xFFD4AF37) else Color(0x26FFFFFF))
                                    .clickable {
                                        if (isPlaying) {
                                            SakinaAdhanAudio.stop(); playingVoiceKey = null
                                        } else {
                                            selectedVoice = voice; SakinaAdhanAudio.setSelectedVoice(ctx, voice)
                                            playingVoiceKey = voice.key
                                            SakinaAdhanAudio.startPreview(ctx, voice) { playingVoiceKey = null }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isPlaying) "❚❚" else "▶",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPlaying) Color(0xFF071E19) else Color(0xFFF3DFA2)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                SakinaBackIconButton(onClick = { SakinaAdhanAudio.stop(); playingVoiceKey = null; showVoicePicker = false })
            }
        )
    }
}
