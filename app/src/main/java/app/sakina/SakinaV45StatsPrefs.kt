package app.sakina

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class QuranBookmarkEntry(
    val page: Int,
    val surahName: String
)

/**
 * Sakina v45 — Local Offline Persistence for Phase 3 (Category 3) Features:
 * - Multiple Quran Bookmarks (🔖)
 * - Daily Khatmah Pages Read & Streak
 * - Fasting Tracker (Ramadan, Mon/Thu, White Days)
 * - Monthly Prayer Goal Target
 */
object SakinaV45StatsPrefs {
    private fun sp(ctx: Context) = ctx.getSharedPreferences("sakina_v45_stats", Context.MODE_PRIVATE)

    fun todayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
    }

    // ---- Multiple Quran Bookmarks (🔖) ----
    fun bookmarks(ctx: Context): List<QuranBookmarkEntry> {
        val raw = sp(ctx).getString("quran_bookmarks", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";;").mapNotNull { entry ->
            val parts = entry.split("|")
            val page = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val name = parts.getOrNull(1) ?: "ص $page"
            QuranBookmarkEntry(page, name)
        }.sortedBy { it.page }
    }

    fun isPageBookmarked(ctx: Context, page: Int): Boolean =
        bookmarks(ctx).any { it.page == page }

    fun toggleBookmark(ctx: Context, page: Int, surahName: String): Boolean {
        val current = bookmarks(ctx).toMutableList()
        val existingIdx = current.indexOfFirst { it.page == page }
        val added = if (existingIdx >= 0) {
            current.removeAt(existingIdx)
            false
        } else {
            current.add(QuranBookmarkEntry(page, surahName))
            true
        }
        val encoded = current.joinToString(";;") { "${it.page}|${it.surahName.replace("|", " ")}" }
        sp(ctx).edit().putString("quran_bookmarks", encoded).apply()
        return added
    }

    // ---- Quran Khatmah Daily Progress & Streak ----
    fun dailyPageGoal(ctx: Context): Int = sp(ctx).getInt("khatmah_daily_goal", 4).coerceIn(1, 30)
    fun setDailyPageGoal(ctx: Context, pages: Int) =
        sp(ctx).edit().putInt("khatmah_daily_goal", pages.coerceIn(1, 30)).apply()

    fun pagesReadToday(ctx: Context): Int {
        val p = sp(ctx)
        val savedDay = p.getString("khatmah_last_day", "")
        return if (savedDay == todayKey()) p.getInt("khatmah_pages_today", 0) else 0
    }

    fun readingStreakDays(ctx: Context): Int = sp(ctx).getInt("khatmah_streak", 0)

    fun logPageRead(ctx: Context) {
        val p = sp(ctx)
        val today = todayKey()
        val lastDay = p.getString("khatmah_last_day", "")
        val newPages = if (lastDay == today) p.getInt("khatmah_pages_today", 0) + 1 else 1
        var streak = p.getInt("khatmah_streak", 0)
        if (lastDay != today) {
            val yestCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yestKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(yestCal.time)
            streak = if (lastDay == yestKey) streak + 1 else 1
        }
        p.edit()
            .putString("khatmah_last_day", today)
            .putInt("khatmah_pages_today", newPages)
            .putInt("khatmah_streak", streak)
            .apply()
    }

    // ---- Fasting Tracker (Ramadan, Mon/Thu, White Days) ----
    fun fastedDates(ctx: Context): Set<String> {
        val raw = sp(ctx).getString("fasted_dates_csv", "") ?: ""
        if (raw.isBlank()) return emptySet()
        return raw.split(",").filter { it.isNotBlank() }.toSet()
    }

    fun isFastedToday(ctx: Context): Boolean = fastedDates(ctx).contains(todayKey())

    fun toggleFastedToday(ctx: Context): Boolean {
        val set = fastedDates(ctx).toMutableSet()
        val today = todayKey()
        val nowFasted = if (set.contains(today)) {
            set.remove(today); false
        } else {
            set.add(today); true
        }
        sp(ctx).edit().putString("fasted_dates_csv", set.joinToString(",")).apply()
        return nowFasted
    }

    fun totalFastedDays(ctx: Context): Int = fastedDates(ctx).size

    // ---- Monthly Prayer Goal ----
    fun monthlyPrayerGoal(ctx: Context): Int = sp(ctx).getInt("monthly_prayer_goal", 150).coerceIn(30, 155)
    fun setMonthlyPrayerGoal(ctx: Context, target: Int) =
        sp(ctx).edit().putInt("monthly_prayer_goal", target.coerceIn(30, 155)).apply()
}
