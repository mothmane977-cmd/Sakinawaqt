package app.sakina

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.KeyEvent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.core.app.NotificationCompat
import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.util.*

object Offsets {
    const val MAX = 120
    private fun sp(c: Context) = c.getSharedPreferences("offsets", Context.MODE_PRIVATE)
    fun get(c: Context, name: String): Int = sp(c).getInt(name, 0)
    fun set(c: Context, name: String, m: Int) { sp(c).edit().putInt(name, m.coerceIn(-MAX, MAX)).apply() }
}

object Place {
    private const val DEF_LAT = 35.06
    private const val DEF_LON = 1.04
    private fun sp(c: Context) = c.getSharedPreferences("place", Context.MODE_PRIVATE)

    fun coords(c: Context): Coordinates {
        val p = sp(c)
        val lat = p.getString("lat", null)?.toDoubleOrNull() ?: DEF_LAT
        val lon = p.getString("lon", null)?.toDoubleOrNull() ?: DEF_LON
        return Coordinates(lat, lon)
    }

    fun name(c: Context): String = sp(c).getString("name", "فرندة") ?: "فرندة"

    fun set(c: Context, lat: Double, lon: Double, name: String) {
        sp(c).edit().putString("lat", lat.toString()).putString("lon", lon.toString())
            .putString("name", name).apply()
        DiagLog.add(c, "الموقع: $name")
    }

    fun reset(c: Context) {
        sp(c).edit().clear().apply()
        DiagLog.add(c, "الموقع: عاد إلى فرندة")
    }

    @SuppressLint("MissingPermission")
    fun fromDevice(c: Context): Location? {
        val lm = c.getSystemService(LocationManager::class.java) ?: return null
        return lm.getProviders(true).mapNotNull {
            try { lm.getLastKnownLocation(it) } catch (e: SecurityException) { null }
        }.maxByOrNull { it.time }
    }
}

val PRAYER_NAMES = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")

object Prefs {
    val METHODS = listOf(
        "زاويتا الجزائر 18°/17°",
        "رابطة العالم الإسلامي",
        "الهيئة المصرية",
        "أم القرى",
        "كراتشي",
        "وزارة الأوقاف المغربية 19°/17°",
        "الجمهورية التونسية 18°/18°",
        "أمريكا الشمالية (ISNA 15°)"
    )
    private fun sp(c: Context) = c.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    fun method(c: Context): Int = sp(c).getInt("method", 0).coerceIn(0, METHODS.size - 1)
    fun cycleMethod(c: Context) { sp(c).edit().putInt("method", (method(c) + 1) % METHODS.size).apply() }
    fun hanafi(c: Context): Boolean = sp(c).getBoolean("hanafi", false)
    fun setHanafi(c: Context, v: Boolean) { sp(c).edit().putBoolean("hanafi", v).apply() }
    fun hijriOffset(c: Context): Int = sp(c).getInt("hijri", 0)
    fun setHijriOffset(c: Context, v: Int) { sp(c).edit().putInt("hijri", v.coerceIn(-2, 2)).apply() }
    fun amoled(c: Context): Boolean = sp(c).getBoolean("amoled", false)
    fun fajrFade(c: Context): Boolean = sp(c).getBoolean("fajrfade", true)
    fun setFajrFade(c: Context, v: Boolean) { sp(c).edit().putBoolean("fajrfade", v).apply() }
    fun setAmoled(c: Context, v: Boolean) { sp(c).edit().putBoolean("amoled", v).apply() }

    fun methodLabel(c: Context): String =
        METHODS[method(c)] + " · " + (if (hanafi(c)) "العصر حنفي" else "العصر شافعي")

    fun params(c: Context): CalculationParameters {
        val p = when (method(c)) {
            1 -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            2 -> CalculationMethod.EGYPTIAN.parameters
            3 -> CalculationMethod.UMM_AL_QURA.parameters
            4 -> CalculationMethod.KARACHI.parameters
            5 -> CalculationMethod.OTHER.parameters.apply { fajrAngle = 19.0; ishaAngle = 17.0 }
            6 -> CalculationMethod.OTHER.parameters.apply { fajrAngle = 18.0; ishaAngle = 18.0 }
            7 -> CalculationMethod.NORTH_AMERICA.parameters
            else -> CalculationMethod.OTHER.parameters.apply { fajrAngle = 18.0; ishaAngle = 17.0 }
        }
        p.madhab = if (hanafi(c)) Madhab.HANAFI else Madhab.SHAFI
        p.highLatitudeRule = when (SakinaAdvancedPrefs.highLatRule(c)) {
            1 -> HighLatitudeRule.MIDDLE_OF_THE_NIGHT
            2 -> HighLatitudeRule.SEVENTH_OF_THE_NIGHT
            else -> HighLatitudeRule.TWILIGHT_ANGLE
        }
        return p
    }
}

object PrayerEngine {
    fun events(c: Context, offset: Int): List<Pair<Int, Date>> {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
        val t = PrayerTimes(Place.coords(c), DateComponents.from(cal.time), Prefs.params(c))
        val tz = TimeZone.getDefault()
        val inDst = tz.inDaylightTime(cal.time)
        val dstShiftMs = when (SakinaAdvancedPrefs.dstMode(c)) {
            1 -> if (!inDst) 3_600_000L else 0L
            2 -> if (inDst) -3_600_000L else 0L
            else -> 0L
        }
        fun sh(i: Int, d: Date) = Date(d.time + Offsets.get(c, PRAYER_NAMES[i]) * 60_000L + dstShiftMs)
        return listOf(
            0 to sh(0, t.fajr), 1 to t.sunrise, 2 to sh(2, t.dhuhr),
            3 to sh(3, t.asr), 4 to sh(4, t.maghrib), 5 to sh(5, t.isha)
        )
    }

    fun day(c: Context, offset: Int): List<Pair<String, Date>> =
        events(c, offset).filter { it.first != 1 }.map { PRAYER_NAMES[it.first] to it.second }
}

object Modes {
    val labels = listOf("أذان كامل", "تكبير قصير", "اهتزاز", "صامت")
    private fun sp(c: Context) = c.getSharedPreferences("modes", Context.MODE_PRIVATE)
    fun get(c: Context, p: String): Int = sp(c).getInt(p, 0)
    fun set(c: Context, p: String, m: Int) { sp(c).edit().putInt(p, m).apply() }
}

object DiagLog {
    private const val K = "log"
    private fun sp(c: Context) = c.getSharedPreferences("diag", Context.MODE_PRIVATE)
    fun add(c: Context, msg: String) {
        val t = SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(Date())
        val all = (listOf("$t $msg") + get(c)).take(50)
        sp(c).edit().putString(K, all.joinToString("\n")).apply()
    }
    fun get(c: Context): List<String> = sp(c).getString(K, "")!!.split("\n").filter { it.isNotBlank() }
}

object AlarmScheduler {
    private const val TEST = "تجربة"
    private const val WINDOW_DAYS = 14
    private const val BASE_CODE = 1000
    private const val MAX_CODES = WINDOW_DAYS * 5 + 10

    private fun hm(t: Long) = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(t))
    private fun sched(c: Context) = c.getSharedPreferences("sched2", Context.MODE_PRIVATE)

    private fun firedList(c: Context): List<Long> =
        (sched(c).getString("fired", "") ?: "").split(",").mapNotNull { it.toLongOrNull() }

    fun isFired(c: Context, at: Long): Boolean = at in firedList(c)

    fun markFired(c: Context, at: Long) {
        val keep = (firedList(c) + at).distinct().sortedDescending().take(40)
        sched(c).edit().putString("fired", keep.joinToString(",")).apply()
    }

    private fun fireIntent(c: Context, code: Int, name: String, at: Long, backup: Boolean): PendingIntent =
        PendingIntent.getBroadcast(
            c, code,
            Intent(c, AlarmReceiver::class.java)
                .putExtra("n", name).putExtra("at", at).putExtra("bk", backup),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun setClock(c: Context, code: Int, name: String, at: Long) {
        val am = c.getSystemService(AlarmManager::class.java)
        val show = PendingIntent.getActivity(
            c, 2, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        am.setAlarmClock(AlarmManager.AlarmClockInfo(at, show), fireIntent(c, code, name, at, false))
    }

    private fun cancelWindow(c: Context) {
        val am = c.getSystemService(AlarmManager::class.java)
        val codes = (BASE_CODE until BASE_CODE + MAX_CODES) + listOf(1, 4)
        for (code in codes) {
            val pi = PendingIntent.getBroadcast(
                c, code, Intent(c, AlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pi != null) { am.cancel(pi); pi.cancel() }
        }
    }

    private fun checkMissed(c: Context) {
        val sp = sched(c)
        val now = System.currentTimeMillis()
        val last = sp.getLong("checked", 0L)
        if (last > 0L) {
            val cutoff = now - 60_000L
            for (d in -1..0) {
                for ((name, t) in PrayerEngine.day(c, d)) {
                    if (t.time > last && t.time <= cutoff && !isFired(c, t.time)) {
                        DiagLog.add(c, "⚠ فاتنا $name المقرر ${hm(t.time)}: لم يصل التنبيه ولا الاحتياطي")
                    }
                }
            }
        }
        sp.edit().putLong("checked", now - 60_000L).apply()
    }

    fun scheduleNext(c: Context, after: Long = System.currentTimeMillis()) {
        // Background travel check: auto-update coordinates if user moved > 10 km
        try {
            if (c.getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean("auto_gps_location", true)) {
                val loc = Place.fromDevice(c)
                if (loc != null) {
                    val cur = Place.coords(c)
                    if (kotlin.math.abs(loc.latitude - cur.latitude) > 0.1 || kotlin.math.abs(loc.longitude - cur.longitude) > 0.1) {
                        Place.set(c, loc.latitude, loc.longitude, "GPS")
                    }
                }
            }
        } catch (_: Exception) {}
        checkMissed(c)
        cancelWindow(c)

        var code = BASE_CODE
        var firstName: String? = null
        var firstAt = 0L
        for (d in 0 until WINDOW_DAYS) {
            for ((name, t) in PrayerEngine.day(c, d)) {
                if (t.time > after && code < BASE_CODE + MAX_CODES) {
                    setClock(c, code++, name, t.time)
                    if (firstName == null) { firstName = name; firstAt = t.time }
                }
            }
        }
        val count = code - BASE_CODE

        if (firstName != null) {
            try {
                val am = c.getSystemService(AlarmManager::class.java)
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, firstAt + 90_000, fireIntent(c, 4, firstName, firstAt, true)
                )
            } catch (e: SecurityException) {
                DiagLog.add(c, "تعذّر الاحتياطي: ${e.javaClass.simpleName}")
            }
        }

        val pm = c.getSystemService(Context.POWER_SERVICE) as PowerManager
        val saver = if (pm.isPowerSaveMode) "نعم" else "لا"
        val exempt = if (pm.isIgnoringBatteryOptimizations(c.packageName)) "نعم" else "لا"
        // Schedule enabled Custom Reminders (Pre-Adhan / Iqama / Tahajjud) for today/tomorrow
        try {
            val amCustom = c.getSystemService(AlarmManager::class.java)
            val dayPrayers = PrayerEngine.day(c, 0) + PrayerEngine.day(c, 1)
            SakinaAdvancedPrefs.customReminders(c).filter { it.enabled }.forEachIndexed { rIdx, rem ->
                val targetName = PRAYER_NAMES.filter { it != "الشروق" }.getOrElse(rem.prayerIndex) { "الفجر" }
                val nextMatch = dayPrayers.firstOrNull { (pName, dt) ->
                    pName == targetName && (dt.time + rem.offsetMinutes * 60_000L) > after
                }
                if (nextMatch != null) {
                    val fireAt = nextMatch.second.time + rem.offsetMinutes * 60_000L
                    val pi = PendingIntent.getBroadcast(
                        c, 900 + rIdx,
                        Intent(c, PostPrayerAdhkarReceiver::class.java).putExtra("p", rem.title).putExtra("custom", true),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    amCustom.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pi)
                }
            }
        } catch (_: Exception) {}
        SakinaWidgetUpdater.updateAll(c)
        DiagLog.add(c, "جُدول $count تنبيهاً (14 يوماً)، التالي $firstName ${hm(firstAt)} | توفير: $saver | استثناء: $exempt")
    }

    fun scheduleTest(c: Context) {
        val am = c.getSystemService(AlarmManager::class.java)
        val show = PendingIntent.getActivity(
            c, 2, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val at = System.currentTimeMillis() + 10_000
        am.setAlarmClock(AlarmManager.AlarmClockInfo(at, show), fireIntent(c, 3, TEST, at, false))
        DiagLog.add(c, "جُدولت تجربة بعد 10 ثوانٍ")
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val name = i.getStringExtra("n") ?: return
        val at = i.getLongExtra("at", 0L)
        val backup = i.getBooleanExtra("bk", false)
        val delay = (System.currentTimeMillis() - at) / 1000

        if (name == "تجربة") {
            val m = Modes.get(c, "الفجر")
            DiagLog.add(c, "رنّت التجربة بتأخر $delay ث (${Modes.labels[m]})")
            ring(c, name, m)
            return
        }

        if (AlarmScheduler.isFired(c, at)) {
            DiagLog.add(c, "${if (backup) "الاحتياطي" else "الأساسي"} لـ$name وصل بعد أن رنّ؛ تجاهلته")
            return
        }
        AlarmScheduler.markFired(c, at)

        val mode = Modes.get(c, name)
        val kind = if (backup) "رنّ احتياطياً" else "رنّ"
        DiagLog.add(c, "$kind $name بتأخر $delay ث (${Modes.labels[mode]})")
        ring(c, name, mode)
        AlarmScheduler.scheduleNext(c, at + 1000)
    }

    private fun ring(c: Context, name: String, mode: Int) {
        if (mode == 3) return
        if (mode == 0) {
            SakinaAdhanAudio.playAlarmAdhan(c, name)
            Notifier.show(c, name, mode, playSoundInChannel = false)
            SilentModeScheduler.scheduleIfEnabled(c, name)
            PostPrayerAdhkarScheduler.scheduleIfEnabled(c, name)
            return
        }
        if (mode == 1) {
            try {
                AdhanService.start(c, name, mode)
                return
            } catch (e: Exception) {
                DiagLog.add(c, "تعذّر بدء الخدمة (${e.javaClass.simpleName}): إشعار عادي")
            }
        }
        Notifier.show(c, name, mode, playSoundInChannel = true)
    }
}

object Notifier {
    fun show(c: Context, name: String, mode: Int, playSoundInChannel: Boolean = true) {
        val nm = c.getSystemService(NotificationManager::class.java)
        val rid = if (mode == 0) {
            SakinaAdhanAudio.rawResForPrayer(c, name)
        } else {
            c.resources.getIdentifier("takbir", "raw", c.packageName)
        }
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val chId = "m${mode}_${rid}_${if (playSoundInChannel) 1 else 0}"
        val ch = NotificationChannel(chId, Modes.labels[mode], NotificationManager.IMPORTANCE_HIGH).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            vibrationPattern = SakinaAdvancedPrefs.vibrationPatternFor(c)
            if (mode == 2 || !playSoundInChannel) {
                setSound(null, null)
            } else {
                val uri = if (rid != 0) Uri.parse("android.resource://${c.packageName}/$rid")
                else Settings.System.DEFAULT_ALARM_ALERT_URI
                setSound(uri, attrs)
            }
        }
        nm.createNotificationChannel(ch)
        val openPi = PendingIntent.getActivity(
            c, 10, Intent(c, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopPi = PendingIntent.getBroadcast(
            c, 778, Intent(c, StopAdhanReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val b = NotificationCompat.Builder(c, chId)
            .setSmallIcon(R.drawable.ic_stat_sakina)
            .setColor(0xFFC69C53.toInt())
            .setContentTitle("حان وقت $name")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPi)
            .setDeleteIntent(stopPi)
            .setAutoCancel(true)
        if (mode == 0) {
            b.addAction(0, "إيقاف الأذان", stopPi)
        }
        nm.notify(name.hashCode(), b.build())
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        DiagLog.add(c, "إعادة جدولة بعد ${i.action?.substringAfterLast('.')}")
        AlarmScheduler.scheduleNext(c)
    }
}

object PostPrayerAdhkarScheduler {
    fun scheduleIfEnabled(c: Context, prayerName: String) {
        if (!SakinaAdvancedPrefs.postPrayerAdhkarNotif(c)) return
        try {
            val am = c.getSystemService(AlarmManager::class.java)
            val fireAt = System.currentTimeMillis() + 12 * 60_000L
            val pi = PendingIntent.getBroadcast(
                c, 820, Intent(c, PostPrayerAdhkarReceiver::class.java).putExtra("p", prayerName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pi)
        } catch (_: Exception) {}
    }
}

class PostPrayerAdhkarReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        try {
            val nm = c.getSystemService(NotificationManager::class.java)
            val chId = "sakina_post_prayer_adhkar"
            if (Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(
                    NotificationChannel(chId, "أذكار ما بعد الصلاة", NotificationManager.IMPORTANCE_DEFAULT)
                )
            }
            val openIntent = Intent(c, MainActivity::class.java).apply {
                putExtra("open_after_prayer", true)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pi = PendingIntent.getActivity(c, 821, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val pName = i.getStringExtra("p") ?: "الصلاة"
            nm.notify(
                820,
                NotificationCompat.Builder(c, chId)
                    .setSmallIcon(R.drawable.ic_stat_sakina)
                    .setColor(0xFFC69C53.toInt())
                    .setContentTitle("أذكار بعد صلاة $pName")
                    .setContentText("سبحان الله (33) · الحمد لله (33) · الله أكبر (33) · آية الكرسي")
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build()
            )
        } catch (_: Exception) {}
    }
}

class MainActivity : ComponentActivity() {
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) && SakinaAdhanAudio.isPlaying()) {
            SakinaAdhanAudio.stop()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        AlarmScheduler.scheduleNext(this)
        setContent { HomeScreen(this@MainActivity) }
    }
}

class StopAdhanReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        SakinaAdhanAudio.stop()
    }
}

object SilentModeScheduler {
    fun scheduleIfEnabled(c: Context, prayerName: String) {
        if (!SakinaAdvancedPrefs.autoSilentEnabled(c)) return
        try {
            val nm = c.getSystemService(NotificationManager::class.java)
            if (Build.VERSION.SDK_INT >= 23 && !nm.isNotificationPolicyAccessGranted) return
            val am = c.getSystemService(AlarmManager::class.java)
            val now = System.currentTimeMillis()
            val startAt = now + 10 * 60_000L
            val isFridayDhuhr = prayerName == "الظهر" && Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
            val durMin = if (isFridayDhuhr) SakinaAdvancedPrefs.fridaySilentDurationMin(c).toLong() else SakinaAdvancedPrefs.autoSilentDurationMin(c).toLong()
            val endAt = startAt + durMin * 60_000L

            val startPi = PendingIntent.getBroadcast(
                c, 810, Intent(c, AutoSilentReceiver::class.java).putExtra("silent", true),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val endPi = PendingIntent.getBroadcast(
                c, 811, Intent(c, AutoSilentReceiver::class.java).putExtra("silent", false),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startAt, startPi)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAt, endPi)
        } catch (_: Exception) {}
    }
}

class AutoSilentReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        try {
            val audio = c.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val goSilent = i.getBooleanExtra("silent", false)
            if (goSilent) {
                val target = if (SakinaAdvancedPrefs.autoSilentVibrate(c)) AudioManager.RINGER_MODE_VIBRATE else AudioManager.RINGER_MODE_SILENT
                audio.ringerMode = target
            } else {
                audio.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        } catch (_: Exception) {}
    }
}
