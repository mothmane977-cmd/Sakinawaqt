package app.sakina

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

/**
 * محرّك حفظ وإدارة «ختمتي» الشامل (v63) — 100% أوفلاين:
 * - دعم 3 ختمات متزامنة مستقلة (ختمة 1، 2، 3) بالاسم والمقصد ونقطة البداية والمدة
 * - خريطة الـ 604 صفحة المقروءة (604-bit String)
 * - ورد الميقات المرن (توزيع يومي تلقائي على الصلوات الخمس)
 * - المعوّض الصامت + وضع اليوم المزدحم + الختمة الهجينة + مؤقّت المصحف الورقي
 * - سجل القراءة لآخر 7 أيام + تخصيص ساعة بداية اليوم (12 ص – 4 ص)
 */
data class KhatmahSlotState(
    val slotIndex: Int,
    val name: String,
    val intentTag: String,
    val startJuz: Int,
    val startPage: Int,
    val currentPage: Int,
    val targetDays: Int,
    val dailyPagesGoal: Int,
    val completedCount: Int,
    val isBusyDay: Boolean,
    val isZeroGuilt: Boolean,
    val isHybridAudio: Boolean,
    val reminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
    val smartHabitEnabled: Boolean,
    val readPagesMask: String
) {
    val totalReadCount: Int
        get() = readPagesMask.count { it == '1' }.coerceAtLeast((currentPage - startPage).coerceAtLeast(0))

    val remainingPages: Int
        get() = (604 - totalReadCount).coerceIn(0, 604)

    val progressFraction: Float
        get() = (totalReadCount / 604f).coerceIn(0f, 1f)

    val percent: Int
        get() = (progressFraction * 100f).toInt().coerceIn(0, 100)

    val effectiveDailyGoal: Int
        get() = if (isBusyDay) 1 else dailyPagesGoal.coerceIn(1, 202)
}

data class DailyReadingEntry(
    val dateKey: String,
    val dayLabel: String,
    val pages: Int,
    val minutes: Int
)

object SakinaKhatmahStore {
    private const val PREF_NAME = "nurqunoot_khatmah_v63"
    private fun sp(ctx: Context) = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    val INTENT_TAGS = listOf("قراءة", "حفظ", "مراجعة", "تدبر", "أخرى")

    val PRESET_TEMPLATES: List<Pair<String, Int>> = listOf(
        "3 أيام (10 أجزاء)" to 3,
        "6 أيام (5 أجزاء)" to 6,
        "10 أيام (3 أجزاء)" to 10,
        "15 يوماً (جزآن)" to 15,
        "20 يوماً (جزء ونصف)" to 20,
        "30 يوماً (جزء يومياً)" to 30,
        "60 يوماً (حزب يومياً)" to 60,
        "120 يوماً (ربعان)" to 120,
        "240 يوماً (ربع يومياً)" to 240
    )

    val PRAYER_NAMES_AR = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")

    fun juzStartPage(juz: Int): Int {
        val j = juz.coerceIn(1, 30)
        return if (j == 1) 1 else ((j - 1) * 20 + 2).coerceIn(1, 604)
    }

    fun activeSlot(ctx: Context): Int = sp(ctx).getInt("active_slot", 0).coerceIn(0, 2)

    fun setActiveSlot(ctx: Context, slot: Int) {
        sp(ctx).edit().putInt("active_slot", slot.coerceIn(0, 2)).apply()
    }

    fun dayStartHour(ctx: Context): Int = sp(ctx).getInt("day_start_hour", 0).coerceIn(0, 4)

    fun setDayStartHour(ctx: Context, hour: Int) {
        sp(ctx).edit().putInt("day_start_hour", hour.coerceIn(0, 4)).apply()
    }

    fun effectiveDateKey(ctx: Context, daysOffset: Int = 0): String {
        val cal = Calendar.getInstance()
        val cutHour = dayStartHour(ctx)
        if (cal.get(Calendar.HOUR_OF_DAY) < cutHour) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        if (daysOffset != 0) cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    private fun defaultMask(readUpTo: Int, startPage: Int): String {
        val arr = CharArray(604) { '0' }
        val from = (startPage - 1).coerceIn(0, 603)
        val to = (readUpTo - 1).coerceIn(0, 603)
        if (readUpTo >= startPage && readUpTo > 0) {
            for (i in from..to) arr[i] = '1'
        }
        return String(arr)
    }

    fun loadSlot(ctx: Context, slot: Int = activeSlot(ctx)): KhatmahSlotState {
        val idx = slot.coerceIn(0, 2)
        val p = sp(ctx)
        val prefix = "slot_${idx}_"
        val defaultName = when (idx) {
            0 -> "الختمة الرئيسية"
            1 -> "ختمة الحفظ والمراجعة"
            else -> "ختمة التدبر والورد"
        }
        val legacyPages = if (idx == 0) SakinaV45StatsPrefs.khatmahCurrentPages(ctx) else 0
        val startJuz = p.getInt("${prefix}start_juz", 1).coerceIn(1, 30)
        val startPage = juzStartPage(startJuz)
        val currentPage = p.getInt("${prefix}current_page", legacyPages.coerceAtLeast(startPage)).coerceIn(1, 604)
        val targetDays = p.getInt("${prefix}target_days", 30).coerceIn(3, 240)
        val dailyGoal = p.getInt("${prefix}daily_goal", ceil(604.0 / targetDays).toInt().coerceAtLeast(1)).coerceIn(1, 202)
        val rawMask = p.getString("${prefix}mask", null)
        val mask = if (rawMask != null && rawMask.length == 604) rawMask else defaultMask(legacyPages, startPage)
        return KhatmahSlotState(
            slotIndex = idx,
            name = p.getString("${prefix}name", defaultName) ?: defaultName,
            intentTag = p.getString("${prefix}intent", "قراءة") ?: "قراءة",
            startJuz = startJuz,
            startPage = startPage,
            currentPage = currentPage,
            targetDays = targetDays,
            dailyPagesGoal = dailyGoal,
            completedCount = p.getInt("${prefix}completed", if (idx == 0) SakinaV45StatsPrefs.completedKhatmahCount(ctx) else 0),
            isBusyDay = p.getBoolean("${prefix}busy_day", false),
            isZeroGuilt = p.getBoolean("${prefix}zero_guilt", true),
            isHybridAudio = p.getBoolean("${prefix}hybrid_audio", false),
            reminderEnabled = p.getBoolean("${prefix}rem_on", false),
            reminderHour = p.getInt("${prefix}rem_h", 18).coerceIn(0, 23),
            reminderMinute = p.getInt("${prefix}rem_m", 0).coerceIn(0, 59),
            smartHabitEnabled = p.getBoolean("${prefix}smart_habit", true),
            readPagesMask = mask
        )
    }

    fun savePlan(
        ctx: Context,
        slot: Int,
        name: String,
        intentTag: String,
        startJuz: Int,
        targetDays: Int,
        dailyGoal: Int
    ) {
        val idx = slot.coerceIn(0, 2)
        val prefix = "slot_${idx}_"
        val sPage = juzStartPage(startJuz)
        sp(ctx).edit()
            .putString("${prefix}name", name.ifBlank { "ختمة ${idx + 1}" })
            .putString("${prefix}intent", intentTag)
            .putInt("${prefix}start_juz", startJuz.coerceIn(1, 30))
            .putInt("${prefix}target_days", targetDays.coerceIn(3, 240))
            .putInt("${prefix}daily_goal", dailyGoal.coerceIn(1, 202))
            .putInt("${prefix}current_page", loadSlot(ctx, idx).currentPage.coerceAtLeast(sPage))
            .apply()
    }

    fun toggleBusyDay(ctx: Context, slot: Int = activeSlot(ctx)) {
        val s = loadSlot(ctx, slot)
        sp(ctx).edit().putBoolean("slot_${s.slotIndex}_busy_day", !s.isBusyDay).apply()
    }

    fun toggleZeroGuilt(ctx: Context, slot: Int = activeSlot(ctx)) {
        val s = loadSlot(ctx, slot)
        sp(ctx).edit().putBoolean("slot_${s.slotIndex}_zero_guilt", !s.isZeroGuilt).apply()
    }

    fun toggleHybridAudio(ctx: Context, slot: Int = activeSlot(ctx)) {
        val s = loadSlot(ctx, slot)
        sp(ctx).edit().putBoolean("slot_${s.slotIndex}_hybrid_audio", !s.isHybridAudio).apply()
    }

    fun saveReminder(
        ctx: Context,
        slot: Int,
        enabled: Boolean,
        hour: Int,
        minute: Int,
        smartHabit: Boolean
    ) {
        val prefix = "slot_${slot.coerceIn(0, 2)}_"
        sp(ctx).edit()
            .putBoolean("${prefix}rem_on", enabled)
            .putInt("${prefix}rem_h", hour.coerceIn(0, 23))
            .putInt("${prefix}rem_m", minute.coerceIn(0, 59))
            .putBoolean("${prefix}smart_habit", smartHabit)
            .apply()
    }

    /** توزيع الورد اليومي على الصلوات الخمس (ختمة الميقات المرن) */
    fun prayerWirdSplit(dailyGoal: Int): List<Int> {
        val g = dailyGoal.coerceAtLeast(1)
        val base = g / 5
        val rem = g % 5
        return List(5) { idx -> base + if (idx < rem) 1 else 0 }
    }

    fun isPrayerWirdDoneToday(ctx: Context, slot: Int, prayerIndex: Int): Boolean {
        val key = "pw_${slot}_${effectiveDateKey(ctx)}_$prayerIndex"
        return sp(ctx).getBoolean(key, false)
    }

    fun togglePrayerWirdDoneToday(ctx: Context, slot: Int, prayerIndex: Int, pagesForPrayer: Int) {
        val key = "pw_${slot}_${effectiveDateKey(ctx)}_$prayerIndex"
        val cur = sp(ctx).getBoolean(key, false)
        sp(ctx).edit().putBoolean(key, !cur).apply()
        if (!cur && pagesForPrayer > 0) {
            logPages(ctx, slot, pagesForPrayer, minutesSpent = pagesForPrayer * 2)
        }
    }

    /** تسجيل صفحات مقروءة مع تحديث خريطة الـ 604 نقطة وسجل آخر 7 أيام والختمات */
    fun logPages(ctx: Context, slot: Int = activeSlot(ctx), deltaPages: Int, minutesSpent: Int = 0) {
        if (deltaPages <= 0 && minutesSpent <= 0) return
        val s = loadSlot(ctx, slot)
        val prefix = "slot_${s.slotIndex}_"
        val maskChars = s.readPagesMask.toCharArray()
        var newPage = s.currentPage
        var completed = s.completedCount

        if (deltaPages > 0) {
            for (step in 0 until deltaPages) {
                val pIdx = (newPage - 1).coerceIn(0, 603)
                maskChars[pIdx] = '1'
                if (newPage < 604) {
                    newPage++
                } else {
                    completed++
                    for (i in maskChars.indices) maskChars[i] = '0'
                    newPage = 1
                }
            }
        }

        val todayK = effectiveDateKey(ctx)
        val p = sp(ctx)
        val todayP = p.getInt("log_p_$todayK", 0) + deltaPages.coerceAtLeast(0)
        val todayM = p.getInt("log_m_$todayK", 0) + minutesSpent.coerceAtLeast(0)
        val totalAll = p.getInt("log_total_pages", 0) + deltaPages.coerceAtLeast(0)

        p.edit()
            .putInt("${prefix}current_page", newPage)
            .putInt("${prefix}completed", completed)
            .putString("${prefix}mask", String(maskChars))
            .putInt("log_p_$todayK", todayP)
            .putInt("log_m_$todayK", todayM)
            .putInt("log_total_pages", totalAll)
            .apply()

        if (s.slotIndex == 0 && deltaPages > 0) {
            SakinaV45StatsPrefs.logKhatmahPages(ctx, deltaPages)
        }
    }

    fun jumpToPage(ctx: Context, slot: Int = activeSlot(ctx), page: Int) {
        val target = page.coerceIn(1, 604)
        val s = loadSlot(ctx, slot)
        val mask = defaultMask(target, s.startPage)
        sp(ctx).edit()
            .putInt("slot_${s.slotIndex}_current_page", target)
            .putString("slot_${s.slotIndex}_mask", mask)
            .apply()
    }

    fun pagesToday(ctx: Context): Int = sp(ctx).getInt("log_p_${effectiveDateKey(ctx)}", 0)
    fun pagesYesterday(ctx: Context): Int = sp(ctx).getInt("log_p_${effectiveDateKey(ctx, -1)}", 0)
    fun minutesToday(ctx: Context): Int = sp(ctx).getInt("log_m_${effectiveDateKey(ctx)}", 0)
    fun totalLoggedPages(ctx: Context): Int = sp(ctx).getInt("log_total_pages", SakinaV45StatsPrefs.khatmahCurrentPages(ctx))

    fun last7DaysLog(ctx: Context): List<DailyReadingEntry> {
        val p = sp(ctx)
        val arDayFmt = SimpleDateFormat("EEEE · d/M", Locale("ar"))
        val keyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cutHour = dayStartHour(ctx)
        return (0..6).map { back ->
            val cal = Calendar.getInstance()
            if (cal.get(Calendar.HOUR_OF_DAY) < cutHour) cal.add(Calendar.DAY_OF_YEAR, -1)
            cal.add(Calendar.DAY_OF_YEAR, -back)
            val k = keyFmt.format(cal.time)
            DailyReadingEntry(
                dateKey = k,
                dayLabel = if (back == 0) "اليوم" else if (back == 1) "الأمس" else arDayFmt.format(cal.time),
                pages = p.getInt("log_p_$k", 0),
                minutes = p.getInt("log_m_$k", 0)
            )
        }
    }

    fun clearReadingLog(ctx: Context) {
        val p = sp(ctx)
        val ed = p.edit()
        p.all.keys.filter { it.startsWith("log_") }.forEach { ed.remove(it) }
        ed.apply()
    }

    fun expectedCompletionDateAr(state: KhatmahSlotState): String {
        val daily = state.effectiveDailyGoal.coerceAtLeast(1)
        val daysLeft = ceil(state.remainingPages.toDouble() / daily.toDouble()).toInt().coerceAtLeast(0)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysLeft)
        return SimpleDateFormat("d MMMM yyyy", Locale("ar")).format(cal.time)
    }

    /** نص دعاء ختم القرآن الكريم المنسّق */
    const val KHATM_DUA_TEXT = """اللَّهُمَّ ارْحَمْنِي بِالْقُرْآنِ، وَاجْعَلْهُ لِي إِمَاماً وَنُوراً وَهُدًى وَرَحْمَةً.

اللَّهُمَّ ذَكِّرْنِي مِنْهُ مَا نُسِّيتُ، وَعَلِّمْنِي مِنْهُ مَا جَهِلْتُ، وَارْزُقْنِي تِلاَوَتَهُ آنَاءَ اللَّيْلِ وَأَطْرَافَ النَّهَارِ، وَاجْعَلْهُ لِي حُجَّةً يَا رَبَّ الْعَالَمِينَ.

اللَّهُمَّ أَصْلِحْ لِي دِينِي الَّذِي هُوَ عِصْمَةُ أَمْرِي، وَأَصْلِحْ لِي دُنْيَايَ الَّتِي فِيهَا مَعَاشِي، وَأَصْلِحْ لِي آخِرَتِي الَّتِي فِيهَا مَعَادِي، وَاجْعَلِ الْحَيَاةَ زِيَادَةً لِي فِي كُلِّ خَيْرٍ، وَاجْعَلِ الْمَوْتَ رَاحَةً لِي مِنْ كُلِّ شَرٍّ.

اللَّهُمَّ اجْعَلْ خَيْرَ عُمْرِي آخِرَهُ، وَخَيْرَ عَمَلِي خَوَاتِمَهُ، وَخَيْرَ أَيَّامِي يَوْمَ أَلْقَاكَ فِيهِ.

اللَّهُمَّ إِنِّي أَسْأَلُكَ عِيشَةً هَنِيَّةً، وَمِيتَةً سَوِيَّةً، وَمَرَدّاً غَيْرَ مُخْزٍ وَلاَ فَاضِحٍ. وَصَلَّى اللهُ عَلَى نَبِيِّنَا مُحَمَّدٍ وَعَلَى آلِهِ وَصَحْبِهِ أَجْمَعِينَ."""
}
