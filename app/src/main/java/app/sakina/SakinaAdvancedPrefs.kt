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
}
