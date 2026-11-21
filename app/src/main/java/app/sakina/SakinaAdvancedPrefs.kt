package app.sakina

import android.content.Context
import android.content.Intent
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
        return SimpleDateFormat(pattern, Locale.US).format(date).toWesternDigits()
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

}
