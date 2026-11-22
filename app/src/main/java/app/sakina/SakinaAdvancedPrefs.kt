package app.sakina

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

data class CustomPrayerReminder(
    val id: String,
    val title: String,
    val prayerIndex: Int, // 0=Fajr,1=Dhuhr,2=Asr,3=Maghrib,4=Isha
    val offsetMinutes: Int, // negative = before, positive = after
    val enabled: Boolean
)

data class NawafilTimes(
    val imsak: Date,
    val duha: Date,
    val midnight: Date,
    val lastThird: Date
)

object SakinaAdvancedPrefs {
    private fun sp(c: Context) = c.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    // Appearance & Time
    fun autoNightMode(c: Context): Boolean = sp(c).getBoolean("auto_night_mode", false)
    fun setAutoNightMode(c: Context, v: Boolean) = sp(c).edit().putBoolean("auto_night_mode", v).apply()

    // 0 = Auto (device), 1 = 12h, 2 = 24h
    fun timeFormatMode(c: Context): Int = sp(c).getInt("time_format_mode", 2)
    fun setTimeFormatMode(c: Context, v: Int) = sp(c).edit().putInt("time_format_mode", v.coerceIn(0, 2)).apply()

    // 0 = Auto, 1 = On (+1h), 2 = Off
    fun dstMode(c: Context): Int = sp(c).getInt("dst_mode", 0)
    fun setDstMode(c: Context, v: Int) = sp(c).edit().putInt("dst_mode", v.coerceIn(0, 2)).apply()

    // 0 = None, 1 = Midnight, 2 = Seventh of Night, 3 = Angle-based
    fun highLatRule(c: Context): Int = sp(c).getInt("high_lat_rule", 3)
    fun setHighLatRule(c: Context, v: Int) = sp(c).edit().putInt("high_lat_rule", v.coerceIn(0, 3)).apply()

    // Adhan & Sound Behavior
    fun malikiAdhan(c: Context): Boolean = sp(c).getBoolean("maliki_adhan", false)
    fun setMalikiAdhan(c: Context, v: Boolean) = sp(c).edit().putBoolean("maliki_adhan", v).apply()

    // 0 = Always, 1 = 30m before/after, 2 = 45m before/after
    fun persistentNotifEnabled(c: Context): Boolean = sp(c).getBoolean("persistent_notif_on", true)
    fun setPersistentNotifEnabled(c: Context, v: Boolean) = sp(c).edit().putBoolean("persistent_notif_on", v).apply()

    fun persistentNotifWindow(c: Context): Int = sp(c).getInt("persistent_notif_window", 0)
    fun setPersistentNotifWindow(c: Context, v: Int) = sp(c).edit().putInt("persistent_notif_window", v.coerceIn(0, 2)).apply()

    // 0 = Ringtone volume, 1 = Media volume, 2 = Custom slider
    fun volumeControlMode(c: Context): Int = sp(c).getInt("vol_control_mode", 2)
    fun setVolumeControlMode(c: Context, v: Int) = sp(c).edit().putInt("vol_control_mode", v.coerceIn(0, 2)).apply()

    // 0 = Short, 1 = Medium, 2 = Long, 3 = Very Long
    fun vibrationType(c: Context): Int = sp(c).getInt("vib_type", 1)
    fun setVibrationType(c: Context, v: Int) = sp(c).edit().putInt("vib_type", v.coerceIn(0, 3)).apply()

    fun vibrateWithAdhan(c: Context): Boolean = sp(c).getBoolean("vib_with_adhan", true)
    fun setVibrateWithAdhan(c: Context, v: Boolean) = sp(c).edit().putBoolean("vib_with_adhan", v).apply()

    fun wakeScreenOnAdhan(c: Context): Boolean = sp(c).getBoolean("wake_screen_adhan", false)
    fun setWakeScreenOnAdhan(c: Context, v: Boolean) = sp(c).edit().putBoolean("wake_screen_adhan", v).apply()

    fun flipToSilence(c: Context): Boolean = sp(c).getBoolean("flip_to_silence", true)
    fun setFlipToSilence(c: Context, v: Boolean) = sp(c).edit().putBoolean("flip_to_silence", v).apply()

    fun alarmBellFallback(c: Context): Boolean = sp(c).getBoolean("alarm_bell_fallback", true)
    fun setAlarmBellFallback(c: Context, v: Boolean) = sp(c).edit().putBoolean("alarm_bell_fallback", v).apply()

    // Auto Silent Mode during prayer
    fun autoSilentEnabled(c: Context): Boolean = sp(c).getBoolean("auto_silent_on", false)
    fun setAutoSilentEnabled(c: Context, v: Boolean) = sp(c).edit().putBoolean("auto_silent_on", v).apply()

    fun autoSilentVibrate(c: Context): Boolean = sp(c).getBoolean("auto_silent_vib", true)
    fun setAutoSilentVibrate(c: Context, v: Boolean) = sp(c).edit().putBoolean("auto_silent_vib", v).apply()

    // 0 = None, 1 = Vibrate only, 2 = Tone no vib, 3 = Tone + vib
    fun autoSilentTransitionAlert(c: Context): Int = sp(c).getInt("auto_silent_alert", 1)
    fun setAutoSilentTransitionAlert(c: Context, v: Int) = sp(c).edit().putInt("auto_silent_alert", v.coerceIn(0, 3)).apply()

    fun autoSilentDurationMin(c: Context): Int = sp(c).getInt("auto_silent_dur_min", 20)
    fun setAutoSilentDurationMin(c: Context, v: Int) = sp(c).edit().putInt("auto_silent_dur_min", v).apply()

    fun fridaySilentDurationMin(c: Context): Int = sp(c).getInt("friday_silent_dur_min", 45)
    fun setFridaySilentDurationMin(c: Context, v: Int) = sp(c).edit().putInt("friday_silent_dur_min", v).apply()

    fun postPrayerAdhkarNotif(c: Context): Boolean = sp(c).getBoolean("post_prayer_adhkar_notif", true)
    fun setPostPrayerAdhkarNotif(c: Context, v: Boolean) = sp(c).edit().putBoolean("post_prayer_adhkar_notif", v).apply()

    fun silentBypassFor(c: Context, prayerKeyAr: String): Boolean =
        sp(c).getBoolean("silent_bypass_$prayerKeyAr", false)

    fun setSilentBypassFor(c: Context, prayerKeyAr: String, v: Boolean) =
        sp(c).edit().putBoolean("silent_bypass_$prayerKeyAr", v).apply()

    // Custom Reminders (stored as semicolon-separated entries)
    fun customReminders(c: Context): List<CustomPrayerReminder> {
        val raw = sp(c).getString("custom_reminders_v1", null)
        if (raw.isNullOrBlank()) {
            return listOf(
                CustomPrayerReminder("prep", "التذكير بالتحضر للصلاة", 0, -10, false),
                CustomPrayerReminder("iqama", "تذكير إقامة الصلاة", 1, 15, false),
                CustomPrayerReminder("tahajjud", "تذكير وقت التهجد", 0, -45, false)
            )
        }
        return raw.split(";;").mapNotNull { entry ->
            val p = entry.split("|")
            if (p.size >= 5) {
                CustomPrayerReminder(
                    id = p[0],
                    title = p[1],
                    prayerIndex = p[2].toIntOrNull() ?: 0,
                    offsetMinutes = p[3].toIntOrNull() ?: 0,
                    enabled = p[4].toBooleanStrictOrNull() ?: false
                )
            } else null
        }
    }

    fun saveCustomReminders(c: Context, list: List<CustomPrayerReminder>) {
        val encoded = list.joinToString(";;") { "${it.id}|${it.title}|${it.prayerIndex}|${it.offsetMinutes}|${it.enabled}" }
        sp(c).edit().putString("custom_reminders_v1", encoded).apply()
    }

    // Google Calendar Sync Settings
    fun calendarSyncDays(c: Context): Int = sp(c).getInt("cal_sync_days", 7)
    fun setCalendarSyncDays(c: Context, v: Int) = sp(c).edit().putInt("cal_sync_days", v).apply()

    fun calendarSyncBusy(c: Context): Boolean = sp(c).getBoolean("cal_sync_busy", true)
    fun setCalendarSyncBusy(c: Context, v: Boolean) = sp(c).edit().putBoolean("cal_sync_busy", v).apply()

    fun calendarSyncReminder(c: Context): Boolean = sp(c).getBoolean("cal_sync_rem", true)
    fun setCalendarSyncReminder(c: Context, v: Boolean) = sp(c).edit().putBoolean("cal_sync_rem", v).apply()

    fun formatPrayerTime(c: Context, date: Date): String {
        val mode = timeFormatMode(c)
        val use24 = when (mode) {
            1 -> false
            2 -> true
            else -> DateFormat.is24HourFormat(c)
        }
        val pattern = if (use24) "HH:mm" else "hh:mm a"
        return formatDigits(c, SimpleDateFormat(pattern, Locale.US).format(date))
    }

    fun vibrationPatternFor(c: Context): LongArray = when (vibrationType(c)) {
        0 -> longArrayOf(0, 220, 150, 220)
        1 -> longArrayOf(0, 500, 250, 500)
        2 -> longArrayOf(0, 900, 300, 900)
        else -> longArrayOf(0, 1400, 350, 1400)
    }

    fun exportNextPrayerToCalendar(ctx: Context, prayerName: String, startMs: Long) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "صلاة $prayerName - سكينة")
                putExtra(CalendarContract.Events.DESCRIPTION, "وقت صلاة $prayerName (${Place.name(ctx)})")
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMs)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startMs + 20 * 60_000L)
                putExtra(
                    CalendarContract.Events.AVAILABILITY,
                    if (calendarSyncBusy(ctx)) CalendarContract.Events.AVAILABILITY_BUSY else CalendarContract.Events.AVAILABILITY_FREE
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun sharePrayerTime(ctx: Context, prayerName: String, timeStr: String) {
        try {
            val city = Place.name(ctx)
            val msg = "$timeStr : أنا في $city، حان الآن وقت #صلاة_${prayerName.replace(" ", "_")} — تطبيق سَكِينة".toWesternDigits()
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, msg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(Intent.createChooser(sendIntent, "مشاركة وقت الصلاة").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {}
    }

    // === v18 New High-Value Settings (16-أ..و) ===
    fun keepScreenOn(c: Context): Boolean = sp(c).getBoolean("keep_screen_on", false)
    fun setKeepScreenOn(c: Context, v: Boolean) = sp(c).edit().putBoolean("keep_screen_on", v).apply()

    fun readingFontScale(c: Context): Float = sp(c).getFloat("reading_font_scale", 1.0f).coerceIn(0.85f, 1.50f)
    fun setReadingFontScale(c: Context, v: Float) = sp(c).edit().putFloat("reading_font_scale", v.coerceIn(0.85f, 1.50f)).apply()

    fun voluntaryFastingNotif(c: Context): Boolean = sp(c).getBoolean("voluntary_fasting_notif", true)
    fun setVoluntaryFastingNotif(c: Context, v: Boolean) = sp(c).edit().putBoolean("voluntary_fasting_notif", v).apply()

    // 0 = Normal, 1 = Warm Paper, 2 = Dark Night
    fun mushafReadTheme(c: Context): Int = sp(c).getInt("mushaf_theme", 0).coerceIn(0, 2)
    fun setMushafReadTheme(c: Context, v: Int) = sp(c).edit().putInt("mushaf_theme", v.coerceIn(0, 2)).apply()

    // === v18 Categorized Alarms (13-1..4) ===
    fun fajrSnoozeEnabled(c: Context): Boolean = sp(c).getBoolean("fajr_snooze_on", true)
    fun setFajrSnoozeEnabled(c: Context, v: Boolean) = sp(c).edit().putBoolean("fajr_snooze_on", v).apply()

    fun qiyamAlarmEnabled(c: Context): Boolean = sp(c).getBoolean("qiyam_alarm_on", false)
    fun setQiyamAlarmEnabled(c: Context, v: Boolean) = sp(c).edit().putBoolean("qiyam_alarm_on", v).apply()

    fun qiyamAlarmMinutesBefore(c: Context): Int = sp(c).getInt("qiyam_alarm_min", 45).coerceIn(15, 120)
    fun setQiyamAlarmMinutesBefore(c: Context, v: Int) = sp(c).edit().putInt("qiyam_alarm_min", v.coerceIn(15, 120)).apply()

    fun iqamaAlarmEnabled(c: Context): Boolean = sp(c).getBoolean("iqama_alarm_on", false)
    fun setIqamaAlarmEnabled(c: Context, v: Boolean) = sp(c).edit().putBoolean("iqama_alarm_on", v).apply()

    fun iqamaMinutesAfter(c: Context): Int = sp(c).getInt("iqama_min_after", 15).coerceIn(5, 45)
    fun setIqamaMinutesAfter(c: Context, v: Int) = sp(c).edit().putInt("iqama_min_after", v.coerceIn(5, 45)).apply()

    fun fridayKahfNotif(c: Context): Boolean = sp(c).getBoolean("friday_kahf_notif", true)
    fun setFridayKahfNotif(c: Context, v: Boolean) = sp(c).edit().putBoolean("friday_kahf_notif", v).apply()

    // === v18 Daily Sunnah Checklist (Point 12) ===
    private fun todayKey(): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    fun isSunnahDoneToday(c: Context, id: String): Boolean =
        c.getSharedPreferences("sunnah_daily", Context.MODE_PRIVATE).getBoolean("${todayKey()}_$id", false)
    fun toggleSunnahToday(c: Context, id: String): Boolean {
        val ssp = c.getSharedPreferences("sunnah_daily", Context.MODE_PRIVATE)
        val k = "${todayKey()}_$id"
        val next = !ssp.getBoolean(k, false)
        ssp.edit().putBoolean(k, next).apply()
        return next
    }

    // === v18 Local Settings JSON Backup & Restore (16-و) ===
    fun exportSettingsJson(c: Context): String {
        val obj = org.json.JSONObject()
        val co = Place.coords(c)
        obj.put("city", Place.name(c))
        obj.put("lat", co.latitude)
        obj.put("lon", co.longitude)
        obj.put("method", Prefs.method(c))
        obj.put("hanafi", Prefs.hanafi(c))
        obj.put("hijri", Prefs.hijriOffset(c))
        obj.put("timeFormat", timeFormatMode(c))
        obj.put("autoNight", autoNightMode(c))
        obj.put("keepScreenOn", keepScreenOn(c))
        obj.put("fontStyle", appFontStyle(c))
        obj.put("fontScale", readingFontScale(c).toDouble())
        obj.put("textColorIdx", appTextColorIndex(c))
        obj.put("qiblaSensorMode", qiblaSensorMode(c))
        obj.put("fastingNotif", voluntaryFastingNotif(c))
        obj.put("flipSilence", flipToSilence(c))
        obj.put("mushafTheme", mushafTheme(c))
        obj.put("voice", SakinaAdhanAudio.selectedVoice(c).key)
        return obj.toString(2)
    }

    fun importSettingsJson(c: Context, raw: String): Boolean = try {
        val obj = org.json.JSONObject(raw.trim())
        if (obj.has("lat") && obj.has("lon") && obj.has("city")) {
            val lat = obj.optDouble("lat", Double.NaN)
            val lon = obj.optDouble("lon", Double.NaN)
            val city = obj.optString("city", "").trim()
            if (!lat.isNaN() && !lon.isNaN() && lat in -90.0..90.0 && lon in -180.0..180.0 && city.isNotEmpty()) {
                Place.set(c, lat, lon, city)
            }
        }
        if (obj.has("method")) sp(c).edit().putInt("method", obj.getInt("method").coerceIn(0, Prefs.METHODS.lastIndex)).apply()
        if (obj.has("hanafi")) Prefs.setHanafi(c, obj.getBoolean("hanafi"))
        if (obj.has("hijri")) Prefs.setHijriOffset(c, obj.getInt("hijri").coerceIn(-2, 2))
        if (obj.has("timeFormat")) setTimeFormatMode(c, obj.getInt("timeFormat"))
        if (obj.has("autoNight")) setAutoNightMode(c, obj.getBoolean("autoNight"))
        if (obj.has("keepScreenOn")) setKeepScreenOn(c, obj.getBoolean("keepScreenOn"))
        if (obj.has("fontStyle")) setAppFontStyle(c, obj.getInt("fontStyle"))
        if (obj.has("fontScale")) setReadingFontScale(c, obj.getDouble("fontScale").toFloat())
        if (obj.has("textColorIdx")) setAppTextColorIndex(c, obj.getInt("textColorIdx"))
        if (obj.has("qiblaSensorMode")) setQiblaSensorMode(c, obj.getInt("qiblaSensorMode"))
        if (obj.has("fastingNotif")) setVoluntaryFastingNotif(c, obj.getBoolean("fastingNotif"))
        if (obj.has("flipSilence")) setFlipToSilence(c, obj.getBoolean("flipSilence"))
        if (obj.has("mushafTheme")) setMushafTheme(c, obj.getInt("mushafTheme"))
        if (obj.has("voice")) {
            val vk = obj.optString("voice", "")
            AdhanVoice.entries.firstOrNull { it.key == vk }?.let { SakinaAdhanAudio.setSelectedVoice(c, it) }
        }
        try { AlarmScheduler.scheduleNext(c) } catch (_: Exception) {}
        try { SakinaLiveNotifier.update(c) } catch (_: Exception) {}
        try { SakinaWidgetUpdater.updateAll(c) } catch (_: Exception) {}
        true
    } catch (_: Exception) { false }

    // v19 Appearance: Font Style (0=Amiri Naskh, 1=Ornate Ruqaa, 2=Modern), Text Color Index (0..4), Qibla Sensor Mode (0=Fused, 1=Direct Mag)
    fun appFontStyle(c: Context): Int = sp(c).getInt("app_font_style", 0)
    fun setAppFontStyle(c: Context, v: Int) = sp(c).edit().putInt("app_font_style", v.coerceIn(0, 2)).apply()

    fun appTextColorIndex(c: Context): Int = sp(c).getInt("app_text_color_idx", 0)
    fun setAppTextColorIndex(c: Context, v: Int) = sp(c).edit().putInt("app_text_color_idx", v.coerceIn(0, 4)).apply()

    fun qiblaSensorMode(c: Context): Int = sp(c).getInt("qibla_sensor_mode", 0)
    fun setQiblaSensorMode(c: Context, v: Int) = sp(c).edit().putInt("qibla_sensor_mode", v.coerceIn(0, 1)).apply()


    fun mushafTheme(c: Context): Int = sp(c).getInt("mushaf_theme", 0)
    fun setMushafTheme(c: Context, v: Int) = sp(c).edit().putInt("mushaf_theme", v.coerceIn(0, 2)).apply()


    // === v28 Essential 14 Features ===

    // #02 Alarm Audio Stream (true = USAGE_ALARM bypasses Silent/DND; false = USAGE_NOTIFICATION)
    fun useAlarmStream(c: Context): Boolean = sp(c).getBoolean("v28_use_alarm_stream", true)
    fun setUseAlarmStream(c: Context, v: Boolean) = sp(c).edit().putBoolean("v28_use_alarm_stream", v).apply()

    // #03 Force AlarmClock (setAlarmClock shows status-bar alarm icon, max OEM reliability)
    fun forceAlarmClock(c: Context): Boolean = sp(c).getBoolean("v28_force_alarm_clock", true)
    fun setForceAlarmClock(c: Context, v: Boolean) = sp(c).edit().putBoolean("v28_force_alarm_clock", v).apply()

    // #07 Pre-Prayer & Pre-Sunrise Reminder Minutes (0 = off)
    fun prePrayerReminderMin(c: Context): Int = sp(c).getInt("v28_pre_prayer_min", 0).coerceIn(0, 30)
    fun setPrePrayerReminderMin(c: Context, v: Int) = sp(c).edit().putInt("v28_pre_prayer_min", v.coerceIn(0, 30)).apply()

    fun preSunriseReminderMin(c: Context): Int = sp(c).getInt("v28_pre_sunrise_min", 0).coerceIn(0, 30)
    fun setPreSunriseReminderMin(c: Context, v: Int) = sp(c).edit().putInt("v28_pre_sunrise_min", v.coerceIn(0, 30)).apply()

    // #11 Imsak & Nawafil (Duha, Midnight, Last Third / Tahajjud)
    fun imsakMinutesBefore(c: Context): Int = sp(c).getInt("v28_imsak_min", 10).coerceIn(5, 25)
    fun setImsakMinutesBefore(c: Context, v: Int) = sp(c).edit().putInt("v28_imsak_min", v.coerceIn(5, 25)).apply()

    fun showNawafilCard(c: Context): Boolean = sp(c).getBoolean("v28_show_nawafil", true)
    fun setShowNawafilCard(c: Context, v: Boolean) = sp(c).edit().putBoolean("v28_show_nawafil", v).apply()

    fun computeNawafil(c: Context, dayOffset: Int = 0): NawafilTimes? {
        return try {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
            val dc = com.batoulapps.adhan.data.DateComponents.from(cal.time)
            val pt = com.batoulapps.adhan.PrayerTimes(Place.coords(c), dc, Prefs.params(c))
            val calNext = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset + 1) }
            val ptNext = com.batoulapps.adhan.PrayerTimes(Place.coords(c), com.batoulapps.adhan.data.DateComponents.from(calNext.time), Prefs.params(c))

            val fajrMs = pt.fajr.time + Offsets.get(c, "الفجر") * 60_000L
            val sunriseMs = pt.sunrise.time
            val maghribMs = pt.maghrib.time + Offsets.get(c, "المغرب") * 60_000L
            val nextFajrMs = ptNext.fajr.time + Offsets.get(c, "الفجر") * 60_000L
            val nightMs = (nextFajrMs - maghribMs).coerceAtLeast(3_600_000L)

            NawafilTimes(
                imsak = Date(fajrMs - imsakMinutesBefore(c) * 60_000L),
                duha = Date(sunriseMs + 15 * 60_000L),
                midnight = Date(maghribMs + nightMs / 2L),
                lastThird = Date(maghribMs + (nightMs * 2L) / 3L)
            )
        } catch (_: Exception) {
            null
        }
    }

    // #12 Eastern vs Western Digits + Maghrebi Prayer Names Preset
    fun easternDigits(c: Context): Boolean = sp(c).getBoolean("v28_eastern_digits", false)
    fun setEasternDigits(c: Context, v: Boolean) = sp(c).edit().putBoolean("v28_eastern_digits", v).apply()

    fun formatDigits(c: Context, raw: String): String {
        val west = raw.toWesternDigits()
        if (!easternDigits(c)) return west
        val map = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val sb = StringBuilder(west.length)
        for (ch in west) {
            if (ch in '0'..'9') sb.append(map[ch - '0']) else sb.append(ch)
        }
        return sb.toString()
    }

    fun useMaghrebiNames(c: Context): Boolean = sp(c).getBoolean("v28_maghrebi_names", false)
    fun setUseMaghrebiNames(c: Context, v: Boolean) = sp(c).edit().putBoolean("v28_maghrebi_names", v).apply()

    fun displayPrayerName(c: Context, lang: SakinaLang, prayerIdx: Int, isFriday: Boolean = false): String {
        if (isFriday && prayerIdx == 1) {
            return sakinaTr(lang, "الجمعة", "Jumu'ah", "Jumu'ah", "Cuma", "Jumat", "جمعہ")
        }
        if (useMaghrebiNames(c)) {
            val maghrebi = when (lang) {
                SakinaLang.AR -> listOf("الصبح", "الظهر", "العصر", "المغرب", "العشاء")
                SakinaLang.FR -> listOf("Sobh", "Dohr", "Asr", "Maghreb", "Icha")
                SakinaLang.EN -> listOf("Sobh", "Dohr", "Asr", "Maghreb", "Icha")
                else -> stringsFor(lang).prayers
            }
            return maghrebi.getOrElse(prayerIdx) { stringsFor(lang).prayers.getOrElse(prayerIdx) { "" } }
        }
        return stringsFor(lang).prayers.getOrElse(prayerIdx) { "" }
    }

    // #05 Daily 5-Prayer Tracker + Monthly Stats (bitmask 0..31 per yyyyMMdd)
    fun dateKeyFor(cal: Calendar): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.time)
    fun dateKeyOffset(dayOffset: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
        return dateKeyFor(cal)
    }

    private fun trackerSp(c: Context) = c.getSharedPreferences("sakina_prayer_tracker", Context.MODE_PRIVATE)

    fun isPrayerLogged(c: Context, dateKey: String, prayerIdx: Int): Boolean {
        if (prayerIdx !in 0..4) return false
        val mask = trackerSp(c).getInt("d_$dateKey", 0)
        return (mask and (1 shl prayerIdx)) != 0
    }

    fun togglePrayerLogged(c: Context, dateKey: String, prayerIdx: Int): Boolean {
        if (prayerIdx !in 0..4) return false
        val tsp = trackerSp(c)
        val mask = tsp.getInt("d_$dateKey", 0)
        val bit = 1 shl prayerIdx
        val nowLogged = (mask and bit) == 0
        val newMask = if (nowLogged) (mask or bit) else (mask and bit.inv())
        tsp.edit().putInt("d_$dateKey", newMask).apply()
        return nowLogged
    }

    fun loggedCountForDay(c: Context, dateKey: String): Int {
        val mask = trackerSp(c).getInt("d_$dateKey", 0)
        var cnt = 0
        for (i in 0..4) if ((mask and (1 shl i)) != 0) cnt++
        return cnt
    }

    // Returns Triple(loggedPrayersInMonth, totalExpectedSoFar, perfectFiveOfFiveDays)
    fun monthlyPrayerStats(c: Context): Triple<Int, Int, Int> {
        val now = Calendar.getInstance()
        val dayOfMonth = now.get(Calendar.DAY_OF_MONTH)
        val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
        var totalLogged = 0
        var perfectDays = 0
        for (d in 1..dayOfMonth) {
            cal.set(Calendar.DAY_OF_MONTH, d)
            val cnt = loggedCountForDay(c, dateKeyFor(cal))
            totalLogged += cnt
            if (cnt == 5) perfectDays++
        }
        return Triple(totalLogged, dayOfMonth * 5, perfectDays)
    }

    // #14 Nearby Mosques (0 INTERNET permission), Share App, Polite 7-Day Rating Prompt
    fun openNearbyMosques(ctx: Context, lang: SakinaLang) {
        val q = when (lang) {
            SakinaLang.AR, SakinaLang.UR -> "مسجد"
            SakinaLang.FR -> "mosquée"
            SakinaLang.TR -> "cami"
            SakinaLang.ID -> "masjid"
            else -> "mosque"
        }
        try {
            val co = Place.coords(ctx)
            val uri = Uri.parse("geo:${co.latitude},${co.longitude}?q=${Uri.encode(q)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        } catch (_: Exception) {
            try {
                val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(q)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(web)
            } catch (_: Exception) {}
        }
    }

    fun shareApp(ctx: Context, lang: SakinaLang) {
        val msg = sakinaTr(
            lang,
            "جرّب تطبيق «سَكِينة» لمواقيت الصلاة والأذان والقرآن الكريم (رواية ورش وحفص) — مجاني 100% وبدون إعلانات أو إنترنت:
https://play.google.com/store/apps/details?id=${ctx.packageName}",
            "Try Sakina — 100% offline, ad-free Prayer Times, Adhan & Mushaf app:
https://play.google.com/store/apps/details?id=${ctx.packageName}",
            "Découvrez Sakina — Horaires de prière, Adhan et Coran (Warsh & Hafs) 100% hors-ligne et sans publicité :
https://play.google.com/store/apps/details?id=${ctx.packageName}",
            "Sakina — %100 çevrimdışı, reklamsız Namaz Vakitleri, Ezan ve Kur'an uygulaması:
https://play.google.com/store/apps/details?id=${ctx.packageName}",
            "Coba Sakina — Jadwal Sholat, Adzan & Al-Qur'an 100% offline dan tanpa iklan:
https://play.google.com/store/apps/details?id=${ctx.packageName}",
            "سکینہ ایپ آزمائیں — 100% آف لائن، بغیر اشتہارات نماز کے اوقات، اذان اور قرآن:
https://play.google.com/store/apps/details?id=${ctx.packageName}"
        )
        try {
            val i = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, msg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(Intent.createChooser(i, sakinaTr(lang, "مشاركة تطبيق سَكِينة", "Share Sakina", "Partager Sakina", "Sakina'yı Paylaş", "Bagikan Sakina", "سکینہ شیئر کریں")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {}
    }

    fun openPlayStoreRating(ctx: Context) {
        try {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            try {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: Exception) {}
        }
    }

    fun recordFirstLaunchIfNeeded(c: Context) {
        val prefs = sp(c)
        if (prefs.getLong("v28_first_launch_ms", 0L) == 0L) {
            prefs.edit().putLong("v28_first_launch_ms", System.currentTimeMillis()).apply()
        }
    }

    fun shouldShowRatingPrompt(c: Context): Boolean {
        val prefs = sp(c)
        if (prefs.getBoolean("v28_rating_prompted", false)) return false
        val first = prefs.getLong("v28_first_launch_ms", 0L)
        if (first == 0L) return false
        return (System.currentTimeMillis() - first) >= 7L * 24L * 3_600_000L
    }

    fun markRatingPrompted(c: Context) {
        sp(c).edit().putBoolean("v28_rating_prompted", true).apply()
    }

}
