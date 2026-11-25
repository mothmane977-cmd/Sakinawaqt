package app.sakina

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.IslamicCalendar
import android.os.Build
import java.util.Calendar

/**
 * Sakina v53 — Live Prayer Horizon Notifier
 * Connects PrayerEngine + SakinaStatusBarIconFactory (hollow dome minutes < 60m)
 * + SakinaLuxuryNotifications (لوحة الأفق الحيّ + 3-stage lifecycle + live interactive buttons).
 */
object SakinaLiveNotifier {
    private const val NOTIF_ID = SakinaLuxuryNotifications.LIVE_NOTIF_ID
    private const val ACTION_REFRESH = "app.sakina.ACTION_REFRESH_LIVE_NOTIF"
    private var receiverRegistered = false

    private fun ensureTickAndActionReceiver(ctx: Context) {
        if (receiverRegistered) return
        try {
            val appCtx = ctx.applicationContext
            val filter = IntentFilter().apply {
                addAction(ACTION_REFRESH)
                addAction(SakinaLuxuryNotifications.ACTION_MARK_PRAYED)
                addAction(SakinaLuxuryNotifications.ACTION_CYCLE_ALERT)
                addAction(SakinaLuxuryNotifications.ACTION_TASBIH_TAP)
                addAction(SakinaLuxuryNotifications.ACTION_PREPARED)
                addAction(SakinaLuxuryNotifications.ACTION_SNOOZE_5M)
                addAction(SakinaLuxuryNotifications.ACTION_STOP_ADHAN)
                addAction(SakinaLuxuryNotifications.ACTION_IQAMA_COUNTDOWN)
                addAction(SakinaLuxuryNotifications.ACTION_POST_DHIKR_TAP)
                addAction(SakinaLuxuryNotifications.ACTION_POST_DHIKR_DONE)
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val c = context ?: return
                    val sp = c.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    when (intent?.action) {
                        SakinaLuxuryNotifications.ACTION_MARK_PRAYED -> {
                            val todayKey = SakinaAdvancedPrefs.dateKeyOffset(0)
                            val idx = sp.getInt("live_active_prayer_idx", 0).coerceIn(0, 4)
                            if (!SakinaAdvancedPrefs.isPrayerLogged(c, todayKey, idx)) {
                                SakinaAdvancedPrefs.togglePrayerLogged(c, todayKey, idx)
                            }
                            SakinaLuxuryNotifications.dismissStageNotification(c)
                        }
                        SakinaLuxuryNotifications.ACTION_CYCLE_ALERT -> {
                            val cur = sp.getInt("live_quick_alert_mode", 0)
                            sp.edit().putInt("live_quick_alert_mode", (cur + 1) % 3).apply()
                        }
                        SakinaLuxuryNotifications.ACTION_TASBIH_TAP -> {
                            val cnt = (sp.getInt("live_notif_tasbih", 0) + 1) % 34
                            sp.edit().putInt("live_notif_tasbih", cnt).apply()
                        }
                        SakinaLuxuryNotifications.ACTION_PREPARED,
                        SakinaLuxuryNotifications.ACTION_SNOOZE_5M,
                        SakinaLuxuryNotifications.ACTION_POST_DHIKR_DONE -> {
                            SakinaLuxuryNotifications.dismissStageNotification(c)
                        }
                        SakinaLuxuryNotifications.ACTION_STOP_ADHAN -> {
                            try {
                                c.stopService(Intent(c, SakinaAdhanForegroundService::class.java))
                            } catch (_: Exception) {}
                            SakinaLuxuryNotifications.dismissStageNotification(c)
                        }
                        SakinaLuxuryNotifications.ACTION_POST_DHIKR_TAP -> {
                            val cnt = (sp.getInt("live_post_tasbih", 0) + 1) % 34
                            sp.edit().putInt("live_post_tasbih", cnt).apply()
                            val openPi = buildOpenMainPi(c)
                            val dhikr = when {
                                cnt < 11 -> "سُبْحَانَ اللَّهِ · أَسْتَغْفِرُ اللَّهَ (3×)"
                                cnt < 22 -> "الْحَمْدُ لِلَّهِ · اللَّهُمَّ أَنْتَ السَّلَامُ"
                                else -> "اللَّهُ أَكْبَرُ · لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ"
                            }
                            SakinaLuxuryNotifications.showPostPrayerNotification(
                                context = c,
                                prayerName = sp.getString("live_active_prayer_name", "الصلاة") ?: "الصلاة",
                                dhikrText = dhikr,
                                tasbihCount = cnt,
                                contentPendingIntent = openPi
                            )
                        }
                    }
                    update(c)
                    if (intent?.action != Intent.ACTION_TIME_TICK) {
                        try { SakinaWidgetUpdater.updateAll(c) } catch (_: Exception) {}
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= 33) {
                appCtx.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                appCtx.registerReceiver(receiver, filter)
            }
            receiverRegistered = true
        } catch (_: Exception) {}
    }

    private fun activeLang(ctx: Context): SakinaLang {
        val code = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("app_lang", "ar") ?: "ar"
        return SakinaLang.entries.firstOrNull { it.code == code } ?: SakinaLang.AR
    }

    private fun buildOpenMainPi(ctx: Context): PendingIntent {
        val openIntent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            ctx, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun update(ctx: Context) {
        try {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!SakinaAdvancedPrefs.persistentNotifEnabled(ctx)) {
                nm.cancel(NOTIF_ID)
                return
            }

            val winCode = SakinaAdvancedPrefs.persistentNotifWindow(ctx)
            if (winCode > 0) {
                val limitMs = (if (winCode == 1) 30L else 45L) * 60_000L
                val nowCheck = System.currentTimeMillis()
                val nearAny = PrayerEngine.day(ctx, 0).any { kotlin.math.abs(it.second.time - nowCheck) <= limitMs }
                if (!nearAny) {
                    nm.cancel(NOTIF_ID)
                    return
                }
            }

            ensureTickAndActionReceiver(ctx)

            val lang = activeLang(ctx)
            val s = stringsFor(lang)
            val now = System.currentTimeMillis()
            val today = PrayerEngine.day(ctx, 0)
            val tomorrow = PrayerEngine.day(ctx, 1)

            val nextIdx = today.indexOfFirst { it.second.time > now }
            val isTomorrow = nextIdx < 0
            val resolvedNextIdx = if (nextIdx >= 0) nextIdx else 0
            val (_, nextDate) = if (nextIdx >= 0) today[nextIdx] else tomorrow.first()

            val calNow = Calendar.getInstance().apply { timeInMillis = now }
            val isFriday = calNow.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
            val nextDisplayName = SakinaAdvancedPrefs.displayPrayerName(ctx, lang, resolvedNextIdx, isFriday && !isTomorrow)
            val nextTimeFmt = SakinaAdvancedPrefs.formatPrayerTime(ctx, nextDate)
            val remainingMillis = (nextDate.time - now).coerceAtLeast(0L)

            val ic = IslamicCalendar().apply {
                timeInMillis = now
                add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
            }
            val hijriMonth = s.hijriMonths.getOrElse(ic.get(IslamicCalendar.MONTH).coerceIn(0, 11)) { "" }
            val hijriShort = SakinaAdvancedPrefs.formatDigits(ctx, "${ic.get(IslamicCalendar.DAY_OF_MONTH)} $hijriMonth")
            val place = localizeCityName(Place.name(ctx), lang)

            val fajrMs = today.first().second.time
            val ishaMs = today.last().second.time
            val span = (ishaMs - fajrMs).coerceAtLeast(1L).toFloat()
            val dayProgress = ((now - fajrMs).toFloat() / span).coerceIn(0f, 1f)

            val stripNames = List(5) { idx ->
                val nmStr = SakinaAdvancedPrefs.displayPrayerName(ctx, lang, idx, isFriday && !isTomorrow && idx == 1)
                if (idx == resolvedNextIdx) "✦ $nmStr" else nmStr
            }.joinToString(" · ")

            val sp = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            sp.edit()
                .putInt("live_active_prayer_idx", resolvedNextIdx)
                .putString("live_active_prayer_name", nextDisplayName)
                .apply()

            val quickAlertMode = sp.getInt("live_quick_alert_mode", 0)
            val alertModeLabel = when (quickAlertMode) {
                0 -> sakinaTr(lang, "🔊 أذان", "🔊 Adhan", "🔊 Adhan", "🔊 Ezan", "🔊 Adzan", "🔊 اذان")
                1 -> sakinaTr(lang, "🔔 إشعار", "🔔 Alert", "🔔 Alerte", "🔔 Bildirim", "🔔 Notif", "🔔 اطلاع")
                else -> sakinaTr(lang, "🔇 صامت", "🔇 Silent", "🔇 Muet", "🔇 Sessiz", "🔇 Senyap", "🔇 خاموش")
            }
            val tasbihCount = sp.getInt("live_notif_tasbih", 0)

            val hr = calNow.get(Calendar.HOUR_OF_DAY)
            val skyPhase = when (hr) {
                in 4..7 -> SakinaLuxuryNotifications.SkyPhase.FAJR_DAWN
                in 8..16 -> SakinaLuxuryNotifications.SkyPhase.DHUHR_GOLD
                in 17..19 -> SakinaLuxuryNotifications.SkyPhase.MAGHRIB_TWILIGHT
                else -> SakinaLuxuryNotifications.SkyPhase.NIGHT_VIGIL
            }

            val openPi = buildOpenMainPi(ctx)
            val refreshPi = PendingIntent.getBroadcast(
                ctx, 777, Intent(ACTION_REFRESH).setPackage(ctx.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            SakinaLuxuryNotifications.showLiveHorizonNotification(
                context = ctx,
                prayerName = nextDisplayName,
                prayerTimeFormatted = nextTimeFmt,
                remainingMillis = remainingMillis,
                cityAndHijri = "$place · $hijriShort",
                fivePrayersStrip = stripNames,
                alertModeLabel = alertModeLabel,
                tasbihCount = tasbihCount,
                skyPhase = skyPhase,
                dayProgressFraction = dayProgress,
                contentPendingIntent = openPi,
                deletePendingIntent = refreshPi
            )
        } catch (_: Exception) {}
    }
}
