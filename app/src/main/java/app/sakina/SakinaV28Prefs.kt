package app.sakina

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*

data class NawafilTimes(
    val imsak: Date,
    val duha: Date,
    val midnight: Date,
    val lastThird: Date
)

private fun v28Sp(c: Context) = c.getSharedPreferences("prefs", Context.MODE_PRIVATE)
private fun v28TrackerSp(c: Context) = c.getSharedPreferences("sakina_prayer_tracker", Context.MODE_PRIVATE)

// #02 Alarm Audio Stream (true = USAGE_ALARM bypasses Silent/DND; false = USAGE_NOTIFICATION)
fun SakinaAdvancedPrefs.useAlarmStream(c: Context): Boolean = v28Sp(c).getBoolean("v28_use_alarm_stream", true)
fun SakinaAdvancedPrefs.setUseAlarmStream(c: Context, v: Boolean) = v28Sp(c).edit().putBoolean("v28_use_alarm_stream", v).apply()

// #03 Force AlarmClock (setAlarmClock shows status-bar alarm icon, max OEM reliability)
fun SakinaAdvancedPrefs.forceAlarmClock(c: Context): Boolean = v28Sp(c).getBoolean("v28_force_alarm_clock", true)
fun SakinaAdvancedPrefs.setForceAlarmClock(c: Context, v: Boolean) = v28Sp(c).edit().putBoolean("v28_force_alarm_clock", v).apply()

// #07 Pre-Prayer & Pre-Sunrise Reminder Minutes (0 = off)
fun SakinaAdvancedPrefs.prePrayerReminderMin(c: Context): Int = v28Sp(c).getInt("v28_pre_prayer_min", 0).coerceIn(0, 30)
fun SakinaAdvancedPrefs.setPrePrayerReminderMin(c: Context, v: Int) = v28Sp(c).edit().putInt("v28_pre_prayer_min", v.coerceIn(0, 30)).apply()

fun SakinaAdvancedPrefs.preSunriseReminderMin(c: Context): Int = v28Sp(c).getInt("v28_pre_sunrise_min", 0).coerceIn(0, 30)
fun SakinaAdvancedPrefs.setPreSunriseReminderMin(c: Context, v: Int) = v28Sp(c).edit().putInt("v28_pre_sunrise_min", v.coerceIn(0, 30)).apply()

// #11 Imsak & Nawafil (Duha, Midnight, Last Third / Tahajjud)
fun SakinaAdvancedPrefs.imsakMinutesBefore(c: Context): Int = v28Sp(c).getInt("v28_imsak_min", 10).coerceIn(5, 25)
fun SakinaAdvancedPrefs.setImsakMinutesBefore(c: Context, v: Int) = v28Sp(c).edit().putInt("v28_imsak_min", v.coerceIn(5, 25)).apply()

fun SakinaAdvancedPrefs.showNawafilCard(c: Context): Boolean = v28Sp(c).getBoolean("v28_show_nawafil", true)
fun SakinaAdvancedPrefs.setShowNawafilCard(c: Context, v: Boolean) = v28Sp(c).edit().putBoolean("v28_show_nawafil", v).apply()

fun SakinaAdvancedPrefs.computeNawafil(c: Context, dayOffset: Int = 0): NawafilTimes? {
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
fun SakinaAdvancedPrefs.easternDigits(c: Context): Boolean = v28Sp(c).getBoolean("v28_eastern_digits", false)
fun SakinaAdvancedPrefs.setEasternDigits(c: Context, v: Boolean) = v28Sp(c).edit().putBoolean("v28_eastern_digits", v).apply()

fun SakinaAdvancedPrefs.formatDigits(c: Context, raw: String): String {
    val west = raw.toWesternDigits()
    if (!easternDigits(c)) return west
    val map = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val sb = StringBuilder(west.length)
    for (ch in west) {
        if (ch in '0'..'9') sb.append(map[ch - '0']) else sb.append(ch)
    }
    return sb.toString()
}

fun SakinaAdvancedPrefs.useMaghrebiNames(c: Context): Boolean = v28Sp(c).getBoolean("v28_maghrebi_names", false)
fun SakinaAdvancedPrefs.setUseMaghrebiNames(c: Context, v: Boolean) = v28Sp(c).edit().putBoolean("v28_maghrebi_names", v).apply()

fun SakinaAdvancedPrefs.displayPrayerName(c: Context, lang: SakinaLang, prayerIdx: Int, isFriday: Boolean = false): String {
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
fun SakinaAdvancedPrefs.dateKeyFor(cal: Calendar): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.time)
fun SakinaAdvancedPrefs.dateKeyOffset(dayOffset: Int): String {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
    return dateKeyFor(cal)
}

fun SakinaAdvancedPrefs.isPrayerLogged(c: Context, dateKey: String, prayerIdx: Int): Boolean {
    if (prayerIdx !in 0..4) return false
    val mask = v28TrackerSp(c).getInt("d_$dateKey", 0)
    return (mask and (1 shl prayerIdx)) != 0
}

fun SakinaAdvancedPrefs.togglePrayerLogged(c: Context, dateKey: String, prayerIdx: Int): Boolean {
    if (prayerIdx !in 0..4) return false
    val tsp = v28TrackerSp(c)
    val mask = tsp.getInt("d_$dateKey", 0)
    val bit = 1 shl prayerIdx
    val nowLogged = (mask and bit) == 0
    val newMask = if (nowLogged) (mask or bit) else (mask and bit.inv())
    tsp.edit().putInt("d_$dateKey", newMask).apply()
    return nowLogged
}

fun SakinaAdvancedPrefs.loggedCountForDay(c: Context, dateKey: String): Int {
    val mask = v28TrackerSp(c).getInt("d_$dateKey", 0)
    var cnt = 0
    for (i in 0..4) if ((mask and (1 shl i)) != 0) cnt++
    return cnt
}

fun SakinaAdvancedPrefs.monthlyPrayerStats(c: Context): Triple<Int, Int, Int> {
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
fun SakinaAdvancedPrefs.openNearbyMosques(ctx: Context, lang: SakinaLang) {
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

fun SakinaAdvancedPrefs.shareApp(ctx: Context, lang: SakinaLang) {
    val msg = sakinaTr(
        lang,
        "جرّب تطبيق «سَكِينة» لمواقيت الصلاة والأذان والقرآن الكريم (رواية ورش وحفص) — مجاني 100% وبدون إعلانات أو إنترنت:\nhttps://play.google.com/store/apps/details?id=${ctx.packageName}",
        "Try Sakina — 100% offline, ad-free Prayer Times, Adhan & Mushaf app:\nhttps://play.google.com/store/apps/details?id=${ctx.packageName}",
        "Découvrez Sakina — Horaires de prière, Adhan et Coran (Warsh & Hafs) 100% hors-ligne et sans publicité :\nhttps://play.google.com/store/apps/details?id=${ctx.packageName}",
        "Sakina — %100 çevrimdışı, reklamsız Namaz Vakitleri, Ezan ve Kur'an uygulaması:\nhttps://play.google.com/store/apps/details?id=${ctx.packageName}",
        "Coba Sakina — Jadwal Sholat, Adzan & Al-Qur'an 100% offline dan tanpa iklan:\nhttps://play.google.com/store/apps/details?id=${ctx.packageName}",
        "سکینہ ایپ آزمائیں — 100% آف لائن، بغیر اشتہارات نماز کے اوقات، اذان اور قرآن:\nhttps://play.google.com/store/apps/details?id=${ctx.packageName}"
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

fun SakinaAdvancedPrefs.openPlayStoreRating(ctx: Context) {
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        try {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {}
    }
}

fun SakinaAdvancedPrefs.recordFirstLaunchIfNeeded(c: Context) {
    val prefs = v28Sp(c)
    if (prefs.getLong("v28_first_launch_ms", 0L) == 0L) {
        prefs.edit().putLong("v28_first_launch_ms", System.currentTimeMillis()).apply()
    }
}

fun SakinaAdvancedPrefs.shouldShowRatingPrompt(c: Context): Boolean {
    val prefs = v28Sp(c)
    if (prefs.getBoolean("v28_rating_prompted", false)) return false
    val first = prefs.getLong("v28_first_launch_ms", 0L)
    if (first == 0L) return false
    return (System.currentTimeMillis() - first) >= 7L * 24L * 3_600_000L
}

fun SakinaAdvancedPrefs.markRatingPrompted(c: Context) {
    v28Sp(c).edit().putBoolean("v28_rating_prompted", true).apply()
}
