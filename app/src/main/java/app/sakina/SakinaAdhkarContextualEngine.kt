package app.sakina

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * المرحلة الأولى من ركن الأذكار الجامع (v70):
 * - المحرّك السياقي الذكي («ذِكْرُك الآن») المتكامل مع مواقيت الصلاة
 * - مستويات الورد الثلاثة لأذكار الصباح والمساء (الكامل · المختار · المختصر للمستعجل والمسافر)
 * - بطاقة الذكر الموثّقة (التخريج + غريب الألفاظ + فضل الذكر)
 * - متابعة المواظبة الهادئة للوردين (سلسلة الاستمرارية + آخر 14 يوماً)
 */
enum class AdhkarSubSection(val labelAr: String) {
    NOW("ذِكْرُك الآن"),
    HISN("حصن المسلم"),
    TASBIH("المسبحة"),
    MY_DUAS("أدعيتي")
}

enum class WirdLevel(val labelAr: String, val descAr: String) {
    FULL("الكامل", "الورد التام"),
    SELECTED("المختار", "أهم الأذكار"),
    SHORT("المختصر", "للمستعجل والمسافر")
}

data class ContextualDhikrBanner(
    val badge: String,
    val title: String,
    val subtitle: String,
    val isMorning: Boolean,
    val isNight: Boolean
)

data class VerifiedWirdItem(
    val id: String,
    val title: String,
    val text: String,
    val targetCount: Int,
    val takhrij: String,
    val gharib: String,
    val fadl: String,
    val minLevel: WirdLevel // SHORT يظهر في الكل، SELECTED في المختار والكامل، FULL في الكامل
)

object SakinaAdhkarContextualEngine {
    private const val PREF_NAME = "nurqunoot_adhkar_v70"
    private fun sp(ctx: Context) = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private fun dateKey(offsetDays: Int = 0): String {
        val c = Calendar.getInstance()
        if (offsetDays != 0) c.add(Calendar.DAY_OF_YEAR, offsetDays)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
    }

    fun resolveCurrentMoment(ctx: Context): ContextualDhikrBanner {
        val now = System.currentTimeMillis()
        val prayers = try { PrayerEngine.day(ctx, 0) } catch (_: Exception) { emptyList() }
        val cal = Calendar.getInstance()
        val hr = cal.get(Calendar.HOUR_OF_DAY)

        // 1. خلال 15 دقيقة بعد أي صلاة مفروضة -> أذكار ما بعد السلام
        prayers.forEachIndexed { idx, (_, dt) ->
            val diffMin = (now - dt.time) / 60_000L
            if (diffMin in 0..15) {
                val pName = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء").getOrElse(idx) { "الصلاة" }
                return ContextualDhikrBanner(
                    badge = "بعد السلام الآن",
                    title = "أذكار ما بعد صلاة $pName",
                    subtitle = "مضى $diffMin د على الأذان · أستغفر الله (3×) وآية الكرسي والتسبيح",
                    isMorning = idx == 0,
                    isNight = idx == 4
                )
            }
        }

        val fajrMs = prayers.getOrNull(0)?.second?.time
        val dhuhrMs = prayers.getOrNull(1)?.second?.time
        val asrMs = prayers.getOrNull(2)?.second?.time
        val maghribMs = prayers.getOrNull(3)?.second?.time
        val ishaMs = prayers.getOrNull(4)?.second?.time

        // 2. بعد الفجر حتى الشروق/الضحى -> أذكار الصباح
        if (fajrMs != null && dhuhrMs != null && now in fajrMs until (fajrMs + 3 * 3600_000L)) {
            val minsLeft = (((fajrMs + 95 * 60_000L) - now) / 60_000L).coerceAtLeast(0L)
            return ContextualDhikrBanner(
                badge = "ورد الصباح",
                title = "أذكار الصباح المباركة",
                subtitle = if (minsLeft > 0) "متبقي نحو $minsLeft دقيقة على الشروق" else "وقت الضحى المبارك · حصّن يومك بالأذكار",
                isMorning = true,
                isNight = false
            )
        }

        // 3. بعد العصر حتى الغروب -> أذكار المساء
        if (asrMs != null && maghribMs != null && now in asrMs until (maghribMs + 45 * 60_000L)) {
            val minsLeft = ((maghribMs - now) / 60_000L).coerceAtLeast(0L)
            return ContextualDhikrBanner(
                badge = "ورد المساء",
                title = "أذكار المساء والتحصين",
                subtitle = if (minsLeft > 0) "متبقي $minsLeft دقيقة حتى غروب الشمس" else "أمسينا وأمسى الملك لله",
                isMorning = false,
                isNight = false
            )
        }

        // 4. بعد العشاء أو في السحر -> أذكار النوم والاستغفار بالأسحار
        if ((ishaMs != null && now >= ishaMs) || hr >= 21 || hr < 5) {
            return ContextualDhikrBanner(
                badge = if (hr in 2..4) "وقت السَّحَر" else "قبل النوم",
                title = if (hr in 2..4) "الاستغفار بالأسحار وقنوت الوتر" else "أذكار النوم والسكينة الليلية",
                subtitle = if (hr in 2..4) "﴿وَبِالْأَسْحَارِ هُمْ يَسْتَغْفِرُونَ﴾" else "اختم يومك بآية الكرسي والمعوذات وباسمك ربي وضعت جنبي",
                isMorning = false,
                isNight = true
            )
        }

        return ContextualDhikrBanner(
            badge = "ذِكْر الساعة",
            title = "أذكار اليوم والتحصين",
            subtitle = "سبحان الله وبحمده · أستغفر الله · اللهم صلّ وسلم على نبينا محمد",
            isMorning = hr < 12,
            isNight = false
        )
    }

    val MORNING_EVENING_WIRD: List<VerifiedWirdItem> = listOf(
        VerifiedWirdItem(
            "w_kursi", "آية الكرسي",
            "﴿اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ... وَهُوَ الْعَلِيُّ الْعَظِيمُ﴾.",
            1, "سورة البقرة: 255 · صحيح البخاري", "سِنة: النعاس الخفيف. لا يئوده: لا يثقله.", "من قرأها حين يصبح أُجير من الجن حتى يمسي.", WirdLevel.SHORT
        ),
        VerifiedWirdItem(
            "w_muawwidhat", "الإخلاص والفلق والناس",
            "قراءة سورة الإخلاص، وسورة الفلق، وسورة الناس ثلاث مرات في الصباح والمساء.",
            3, "رواه أبو داود والترمذي — حسن صحيح", "الصمد: السيد المقصود في الحوائج كلها.", "تكفيك من كل شيء بإذن الله.", WirdLevel.SHORT
        ),
        VerifiedWirdItem(
            "w_sayyid", "سيّد الاستغفار",
            "«اللَّهُمَّ أَنْتَ رَبِّي لا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ».",
            1, "رواه البخاري (6306)", "أبوء لك: أقرّ وأعترف.", "من قالها موقناً بها فمات من يومه دخل الجنة.", WirdLevel.SHORT
        ),
        VerifiedWirdItem(
            "w_bismillah", "بسم الله الذي لا يضر مع اسمه شيء",
            "«بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ».",
            3, "رواه أبو داود والترمذي — صحيح", "لا يضر مع اسمه شيء: حفظ شامل من كل أذى.", "لم يضره شيء في يومه أو ليلته.", WirdLevel.SELECTED
        ),
        VerifiedWirdItem(
            "w_raditu", "رضيت بالله رباً",
            "«رَضِيتُ بِاللَّهِ رَبّاً، وَبِالْإِسْلَامِ دِيناً، وَبِمُحَمَّدٍ ﷺ نَبِيّاً».",
            3, "رواه أحمد وأبو داود — حسن", "الرضا: طمأنينة القلب وتسليمه لأمر الله.", "كان حقاً على الله أن يرضيه يوم القيامة.", WirdLevel.SELECTED
        ),
        VerifiedWirdItem(
            "w_hayy", "يا حي يا قيوم برحمتك أستغيث",
            "«يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ».",
            3, "رواه الحاكم والنسائي — حسن", "طرفة عين: أقل لحظة من الزمن.", "تفويض كامل لله واستجلاب التوفيق في كل أمر.", WirdLevel.SELECTED
        ),
        VerifiedWirdItem(
            "w_afiyah", "سؤال العفو والعافية",
            "«اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي وَآمِنْ رَوْعَاتِي...».",
            1, "رواه أبو داود وابن ماجه — صحيح", "روعاتي: ما يُفزعني ويُخيفني.", "لم يكن النبي ﷺ يدع هؤلاء الدعوات حين يمسي وحين يصبح.", WirdLevel.FULL
        ),
        VerifiedWirdItem(
            "w_subhan", "سبحان الله وبحمده",
            "«سُبْحَانَ اللَّهِ وَبِحَمْدِهِ».",
            100, "رواه مسلم (2692)", "التسبيح: تنزيه الله عن كل نقص مع الثناء عليه.", "حُطّت خطاياه وإن كانت مثل زبد البحر.", WirdLevel.FULL
        )
    )

    fun itemsForLevel(level: WirdLevel): List<VerifiedWirdItem> = when (level) {
        WirdLevel.SHORT -> MORNING_EVENING_WIRD.filter { it.minLevel == WirdLevel.SHORT }
        WirdLevel.SELECTED -> MORNING_EVENING_WIRD.filter { it.minLevel != WirdLevel.FULL }
        WirdLevel.FULL -> MORNING_EVENING_WIRD
    }

    fun isMorningDoneToday(ctx: Context): Boolean = sp(ctx).getBoolean("m_${dateKey()}", false)
    fun isEveningDoneToday(ctx: Context): Boolean = sp(ctx).getBoolean("e_${dateKey()}", false)

    fun markWirdCompleted(ctx: Context, isMorning: Boolean) {
        val k = (if (isMorning) "m_" else "e_") + dateKey()
        sp(ctx).edit().putBoolean(k, true).apply()
    }

    fun quietStreakDays(ctx: Context): Int {
        val p = sp(ctx)
        var streak = 0
        for (i in 0..60) {
            val dk = dateKey(-i)
            val done = p.getBoolean("m_$dk", false) || p.getBoolean("e_$dk", false)
            if (done) streak++ else if (i > 0) break
        }
        return streak
    }

    fun last14DaysStatus(ctx: Context): List<Boolean> {
        val p = sp(ctx)
        return (13 downTo 0).map { back ->
            val dk = dateKey(-back)
            p.getBoolean("m_$dk", false) || p.getBoolean("e_$dk", false)
        }
    }
}
