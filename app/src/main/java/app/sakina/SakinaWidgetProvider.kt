package app.sakina

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.icu.util.IslamicCalendar
import android.widget.RemoteViews
import java.util.*

class SakinaCompactWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        SakinaWidgetUpdater.updateAll(context)
    }
}

class SakinaFullWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        SakinaWidgetUpdater.updateAll(context)
    }
}

object SakinaWidgetUpdater {
    private val HIJRI_MONTHS_AR = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    fun updateAll(ctx: Context) {
        try {
            val mgr = AppWidgetManager.getInstance(ctx)
            val now = System.currentTimeMillis()
            val today = PrayerEngine.day(ctx, 0)
            val tomorrow = PrayerEngine.day(ctx, 1)
            val next = today.firstOrNull { it.second.time > now } ?: tomorrow.first()

            val ic = IslamicCalendar().apply {
                timeInMillis = now
                add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
            }
            val hijriStr = "${ic.get(IslamicCalendar.DAY_OF_MONTH)} ${HIJRI_MONTHS_AR[ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)]} ${ic.get(IslamicCalendar.YEAR)} هـ".toWesternDigits()
            val place = Place.name(ctx)

            val clickPi = PendingIntent.getActivity(
                ctx, 50, Intent(ctx, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 1. Compact 4x1 Widget
            val compactIds = mgr.getAppWidgetIds(ComponentName(ctx, SakinaCompactWidgetProvider::class.java))
            if (compactIds.isNotEmpty()) {
                val rv = RemoteViews(ctx.packageName, R.layout.sakina_widget_compact)
                rv.setTextViewText(R.id.w_next_prayer, "الصلاة القادمة: ${next.first}")
                rv.setTextViewText(R.id.w_place_date, "$place · $hijriStr")
                rv.setTextViewText(R.id.w_next_time, SakinaAdvancedPrefs.formatPrayerTime(ctx, next.second))
                rv.setOnClickPendingIntent(R.id.widget_root, clickPi)
                mgr.updateAppWidget(compactIds, rv)
            }

            // 2. Full 4x2 Daily 5 Prayers Widget
            val fullIds = mgr.getAppWidgetIds(ComponentName(ctx, SakinaFullWidgetProvider::class.java))
            if (fullIds.isNotEmpty()) {
                val rv = RemoteViews(ctx.packageName, R.layout.sakina_widget_full)
                rv.setTextViewText(R.id.wf_header, "سَكِينة · $place")
                rv.setTextViewText(R.id.wf_hijri, hijriStr)
                val tIds = listOf(R.id.wf_t1, R.id.wf_t2, R.id.wf_t3, R.id.wf_t4, R.id.wf_t5)
                today.take(5).forEachIndexed { idx, (_, date) ->
                    rv.setTextViewText(tIds[idx], SakinaAdvancedPrefs.formatPrayerTime(ctx, date))
                }
                rv.setOnClickPendingIntent(R.id.widget_full_root, clickPi)
                mgr.updateAppWidget(fullIds, rv)
            }
        } catch (_: Exception) {}
    }
}
