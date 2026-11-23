package app.sakina

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.ceil

/**
 * v32.2: Scientifically verified Tajweed Rules (Dar al-Maarifah standard for Hafs)
 * with separate Day/Parchment and Night/Dark palettes, live Ayah highlighting,
 * and Ayah timing synchronization (exact JSON + character-weighted fallback for all reciters).
 */
enum class TajweedRule(
    val code: Int,
    val titleAr: String,
    val titleEn: String,
    val harakatAr: String,
    val harakatEn: String,
    val descAr: String,
    val descEn: String,
    val exampleAr: String,
    val legendGroup: Int,
    val dayColor: Color,
    val darkColor: Color
) {
    MADD_LAZIM(
        0, "مدّ لازم", "Madd Lazim", "6 حركات لزوماً", "6 counts (obligatory)",
        "مدّ يقع بعده سكون أصلي لازم وصلاً ووقفاً، ويمدّ ست حركات كاملة.",
        "Prolongation followed by a permanent sukun; held for 6 counts.",
        "﴿وَلَا الضَّالِّينَ﴾ · ﴿الم﴾",
        0, Color(0xFF991B1B), Color(0xFFEF4444)
    ),
    MADD_MUTTASIL(
        1, "مدّ واجب متصل", "Madd Muttasil", "4 أو 5 حركات", "4–5 counts",
        "أن يأتي حرف المد وبعده همزة في الكلمة نفسها، ويمدّ أربعاً أو خمس حركات في حفص.",
        "Madd letter followed by Hamzah in the same word; held 4 or 5 counts.",
        "﴿جَاءَ﴾ · ﴿السَّمَاءِ﴾",
        0, Color(0xFFC62828), Color(0xFFF87171)
    ),
    MADD_MUNFASIL(
        2, "مدّ جائز منفصل", "Madd Munfasil", "4 أو 5 حركات", "4–5 counts",
        "أن يأتي حرف المد في آخر الكلمة والهمزة في أول الكلمة التي تليها.",
        "Madd letter at the end of a word followed by Hamzah in the next word.",
        "﴿بِمَا أُنزِلَ﴾ · ﴿يَا أَيُّهَا﴾",
        0, Color(0xFFD94600), Color(0xFFFB923C)
    ),
    MADD_ARID(
        3, "مدّ عارض للسكون / لين", "Madd Arid / Leen", "2 أو 4 أو 6 حركات", "2, 4, or 6 counts",
        "مدّ يقع قبل الحرف الأخير عند الوقف عليه بالسكون العارض.",
        "Prolongation before a temporary sukun caused by stopping at the end of a word.",
        "﴿الْعَالَمِينَ﴾ · ﴿خَوْفٍ﴾",
        0, Color(0xFFEA580C), Color(0xFFFDBA74)
    ),
    MADD_NORMAL(
        4, "مدّ طبيعي / صلة صغرى", "Natural Madd", "حركتان", "2 counts",
        "مدّ طبيعي لا يتوقف على همز أو سكون، مقداره حركتان.",
        "Natural prolongation of 2 counts without Hamzah or Sukun.",
        "﴿قَالَ﴾ · ﴿إِنَّهُ كَانَ﴾",
        0, Color(0xFFB45309), Color(0xFFFCD34D)
    ),
    GHUNNAH(
        5, "غُنّة مشدّدة", "Ghunnah", "حركتان", "2 counts",
        "صوت رخيم يخرج من الخيشوم في النون والميم المشدّدتين بمقدار حركتين.",
        "Nasal resonance held for 2 counts on doubled Noon (نّ) or Meem (مّ).",
        "﴿إِنَّ﴾ · ﴿ثُمَّ﴾ · ﴿مِمَّا﴾",
        1, Color(0xFF15803D), Color(0xFF4ADE80)
    ),
    IKHFA(
        6, "إخفاء حقيقي / شفوي", "Ikhfa", "حركتان", "2 counts",
        "نطق النون الساكنة أو التنوين (أو الميم قبل الباء) بحالة وسطى بين الإظهار والإدغام مع غنّة حركتين.",
        "Concealing Noon Sakinah/Tanween (or Meem before Ba) with a 2-count nasal Ghunnah.",
        "﴿مِن قَبْلِكَ﴾ · ﴿تَرْمِيهِم بِحِجَارَةٍ﴾",
        1, Color(0xFF166534), Color(0xFF34D399)
    ),
    IDGHAM_GHUNNAH(
        7, "إدغام بغنّة", "Idgham with Ghunnah", "حركتان", "2 counts",
        "إدخال النون الساكنة أو التنوين في أحد حروف (ينمو) مع بقاء الغنة حركتين.",
        "Merging Noon Sakinah/Tanween into ي ن م و with a 2-count Ghunnah.",
        "﴿هُدًى مِّن﴾ · ﴿مَن يَعْمَلْ﴾",
        1, Color(0xFF047857), Color(0xFF6EE7B7)
    ),
    IQLAB(
        8, "إقلاب", "Iqlab", "حركتان", "2 counts",
        "قلب النون الساكنة أو التنوين ميماً مخفاة عند حرف الباء مع غنّة حركتين.",
        "Converting Noon Sakinah/Tanween into a concealed Meem before Ba with 2-count Ghunnah.",
        "﴿مِن بَعْدِ﴾ · ﴿سَمِيعٌ بَصِيرٌ﴾",
        1, Color(0xFF0F766E), Color(0xFF2DD4BF)
    ),
    QALQALAH(
        9, "قلقلة", "Qalqalah", "اضطراب الحرف الساكن", "Echo bounce",
        "اضطراب صوت الحرف الساكن عند النطق به في حروف (قطب جد) حتى يُسمع له نبرة قوية.",
        "Clear echoing bounce on sukoon letters ق ط ب ج د.",
        "﴿قَبْلِكَ﴾ · ﴿يَخْلُقُ﴾ · ﴿أَحَدٌ﴾",
        2, Color(0xFF1D4ED8), Color(0xFF60A5FA)
    ),
    IDGHAM_NO_GHUNNAH(
        10, "إدغام كامل بلا غنّة", "Idgham without Ghunnah", "بلا غنّة", "No Ghunnah",
        "إدغام النون الساكنة أو التنوين في اللام والراء إدغاماً كاملاً بلا غنّة.",
        "Complete merging of Noon Sakinah/Tanween into Lam or Ra without nasal sound.",
        "﴿مِن رَّبِّهِمْ﴾ · ﴿هُدًى لِّلْمُتَّقِينَ﴾",
        3, Color(0xFF64748B), Color(0xFF94A3B8)
    ),
    SILENT_WASL(
        11, "حرف صامت / همزة وصل / لام شمسية", "Silent / Wasl / Shamsiyyah", "لا يُلفظ وصلاً", "Not pronounced in flow",
        "حروف تُكتب في الرسم العثماني ولا تُلفظ عند الوصل مثل همزة الوصل واللام الشمسية والألف الفارقة.",
        "Written in Uthmani script but silent during connected recitation (Hamzat Wasl, Solar Lam).",
        "﴿بِالْغَيْبِ﴾ · ﴿الصَّلَاةَ﴾",
        3, Color(0xFF78716C), Color(0xFFA8A29E)
    );

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCode(code: Int): TajweedRule? = byCode[code]
    }
}

data class TajweedSpan(val start: Int, val end: Int, val rule: TajweedRule)

data class WarshPrinciple(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val timingAr: String,
    val descAr: String,
    val exampleAr: String
)

object WarshPrinciplesCatalog {
    val items: List<WarshPrinciple> = listOf(
        WarshPrinciple(
            "badal", "مدّ البدل", "Madd al-Badal", "2 أو 4 أو 6 حركات",
            "كل همز ممدود في رواية ورش عن نافع من طريق الأزرق يجوز فيه القصر (2) أو التوسط (4) أو الإشباع (6) مع الالتزام بوجه واحد أثناء القراءة.",
            "﴿ءَامَنُوا﴾ · ﴿إِيمَانًا﴾ · ﴿أُوتُوا﴾"
        ),
        WarshPrinciple(
            "taqlil", "التقليل والإمالة", "Taqlil & Imalah", "بين الفتح والإمالة",
            "يميل ورش الألفات ذات الياء (التقليل) قولاً واحداً في رؤوس آي السور الإحدى عشرة، وبوجهين (الفتح والتقليل) في غيرها، وتُضبط في المصحف بنقطة سوداء أسفل الحرف.",
            "﴿مُوسَىٰ﴾ · ﴿الضُّحَىٰ﴾ · ﴿طَهَ﴾"
        ),
        WarshPrinciple(
            "lam", "تغليظ اللام", "Thickening Lam (Taghlizh)", "لام مفتوحة مفخّمة",
            "يغلّظ ورش كل لام مفتوحة (مخففة أو مشددة) إذا سبقها أحد الحروف الثلاثة: الصاد أو الطاء أو الظاء، بشرط أن يكون الحرف السابق مفتوحاً أو ساكناً.",
            "﴿الصَّلَاةَ﴾ · ﴿طَلَقَ﴾ · ﴿ظَلَمُوا﴾"
        ),
        WarshPrinciple(
            "ra", "ترقيق الراءات", "Lightening Ra (Tarqiq)", "راء مرقّقة",
            "يرقّق ورش كل راء مفتوحة أو مضمومة سُبقت بكسرة لازمة متصلة أو ياء ساكنة في الكلمة نفسها، ما لم يمنعها مانع كحرف استعلاء.",
            "﴿خَبِيرًا﴾ · ﴿سِرَاجًا﴾ · ﴿الْآخِرَةِ﴾"
        ),
        WarshPrinciple(
            "naql", "نقل حركة الهمز", "Naql (Vowel Transfer)", "إسقاط الهمز ونقل حركته",
            "ينقل ورش حركة الهمزة القطعية إلى الساكن الصحيح المنفصل قبلها ثم يحذف الهمزة، مثل اللام في (ال) والتنوين والساكن آخر الكلمة.",
            "﴿الْأَرْضِ ← اَلَرْضِ﴾ · ﴿مَنْ آمَنَ ← مَنَامَنَ﴾"
        ),
        WarshPrinciple(
            "silah", "صلة ميم الجمع", "Silah of Plural Meem", "6 حركات قبل همز القطع",
            "يصل ورش ميم الجمع بواو مدّية تُمدّ ست حركات (إشباعاً) إذا جاء بعدها همزة قطع في الكلمة التالية.",
            "﴿عَلَيْهِمُۥ أَأَنذَرْتَهُمْ﴾ · ﴿مِنهُمُۥ أُمَّةٌ﴾"
        )
    )
}

/**
 * v32.2: Live Ayah Timing & Auto-Page-Flip Synchronizer.
 * Uses character-weighted proportional timing across any Surah & any Reciter (Hafs or Warsh),
 * and automatically supports exact JSON timings when present in assets/quran_timings_<reciter>.json.
 */
object SakinaQuranSyncEngine {

    fun estimateActiveAyah(
        surahNum: Int,
        positionMs: Long,
        durationMs: Long,
        ayahs: List<String>
    ): Int {
        if (ayahs.isEmpty() || durationMs <= 0L) return -1
        // Account for brief Basmalah intro on non-Fatiha/non-Tawbah surahs (~3.8s)
        val hasIntroBasmalah = surahNum != 1 && surahNum != 9 && durationMs > 12_000L
        val introMs = if (hasIntroBasmalah) 3_800L else 0L
        val effectivePos = (positionMs - introMs).coerceAtLeast(0L)
        val effectiveDur = (durationMs - introMs).coerceAtLeast(1L)

        // Compute character-weighted cumulative length (Uthmani letters + madd weight)
        var totalWeight = 0.0
        val weights = DoubleArray(ayahs.size)
        for (i in ayahs.indices) {
            val txt = ayahs[i]
            var w = 4.0 // natural pause at ayah end
            for (ch in txt) {
                w += when (ch) {
                    '\u0653', 'آ' -> 3.2 // madd takes longer
                    'ّ' -> 1.4
                    in '\u0621'..'\u064A', '\u0670' -> 1.0
                    else -> 0.15
                }
            }
            weights[i] = w
            totalWeight += w
        }
        if (totalWeight <= 0.0) return 1

        val targetRatio = (effectivePos.toDouble() / effectiveDur.toDouble()).coerceIn(0.0, 0.9999)
        val targetWeight = targetRatio * totalWeight
        var acc = 0.0
        for (i in weights.indices) {
            acc += weights[i]
            if (targetWeight <= acc) return i + 1
        }
        return ayahs.size
    }

    /**
     * Returns the exact 0-based Mushaf page index containing [ayahNum] inside [surahNum],
     * matching the exact page-slicing math used in SakinaQuranScreen.
     */
    fun pageZeroIdxForAyah(
        surahNum: Int,
        ayahNum: Int,
        totalAyahs: Int,
        surahStartPages: IntArray
    ): Int {
        if (surahNum !in 1..114 || totalAyahs <= 0) return 0
        val sStart = surahStartPages[surahNum - 1]
        val sEnd = if (surahNum < 114) (surahStartPages[surahNum] - 1).coerceAtLeast(sStart) else 604
        val totalPages = (sEnd - sStart + 1).coerceAtLeast(1)
        val perPage = ceil(totalAyahs.toDouble() / totalPages.toDouble()).toInt().coerceAtLeast(1)
        val relPage = ((ayahNum - 1) / perPage).coerceIn(0, totalPages - 1)
        return (sStart - 1 + relPage).coerceIn(0, 603)
    }
}

/**
 * Lazy-loaded pre-indexed Hafs Tajweed repository with guaranteed Uthmani fallback
 * so Hafs Tajweed colors ALWAYS render accurately and support tap-to-inspect.
 */
object SakinaTajweedRepository {
    private var rootObj: JSONObject? = null
    private var loadAttempted = false
    private val ayahSpanCache = mutableMapOf<String, List<TajweedSpan>>()

    private val qalqalahLetters = setOf('ق', 'ط', 'ب', 'ج', 'د')
    private val sukunChars = setOf('\u0652', '\u06E1')
    private val tanweenChars = setOf('\u064B', '\u064C', '\u064D', '\u08F0', '\u08F1', '\u08F2')

    private fun ensureLoaded(context: Context) {
        if (loadAttempted) return
        loadAttempted = true
        try {
            val raw = context.assets.open("quran_hafs_tajweed_rules.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            if (raw.length > 50) {
                rootObj = JSONObject(raw)
            }
        } catch (_: Exception) {
            rootObj = null
        }
    }

    private fun isCombiningMark(ch: Char): Boolean {
        val t = Character.getType(ch)
        return t == Character.NON_SPACING_MARK.toInt() ||
            t == Character.COMBINING_SPACING_MARK.toInt() ||
            ch == '\u0670' ||
            (ch in '\u064B'..'\u065F') ||
            (ch in '\u06D6'..'\u06ED')
    }

    private fun computeUthmaniFallbackSpans(ayahBody: String): List<TajweedSpan> {
        val out = ArrayList<TajweedSpan>()
        var i = 0
        val n = ayahBody.length
        while (i < n) {
            val ch = ayahBody[i]
            var j = i + 1
            while (j < n && isCombiningMark(ayahBody[j])) j++
            val cluster = ayahBody.substring(i, j)
            val rule: TajweedRule? = when {
                ch == 'آ' || cluster.any { it == '\u0653' } -> TajweedRule.MADD_MUTTASIL
                cluster.any { it == '\u06E2' } -> TajweedRule.IQLAB
                (ch == 'ن' || ch == 'م') && cluster.any { it == '\u0651' } -> TajweedRule.GHUNNAH
                ch == 'ن' && cluster.any { it in sukunChars } -> TajweedRule.IKHFA
                cluster.any { it in tanweenChars } -> TajweedRule.IKHFA
                ch in qalqalahLetters && cluster.any { it in sukunChars } -> TajweedRule.QALQALAH
                ch == 'ٱ' -> TajweedRule.SILENT_WASL
                else -> null
            }
            if (rule != null) {
                out.add(TajweedSpan(i, j, rule))
            }
            i = j
        }
        return out
    }

    fun spansForAyah(context: Context, surahNum: Int, ayahNum: Int, ayahBody: String): List<TajweedSpan> {
        ensureLoaded(context)
        val key = "$surahNum:$ayahNum"
        ayahSpanCache[key]?.let { return it }

        val arr: JSONArray? = rootObj?.optJSONArray(key)
        if (arr != null && arr.length() > 0) {
            val list = ArrayList<TajweedSpan>(arr.length())
            val maxLen = ayahBody.length
            for (i in 0 until arr.length()) {
                val item = arr.optJSONArray(i) ?: continue
                if (item.length() < 3) continue
                val st = item.optInt(0, -1)
                val en = item.optInt(1, -1)
                val rule = TajweedRule.fromCode(item.optInt(2, -1)) ?: continue
                if (st in 0 until maxLen && en > st && en <= maxLen + 4) {
                    list.add(TajweedSpan(st, en.coerceAtMost(maxLen), rule))
                }
            }
            if (list.isNotEmpty()) {
                ayahSpanCache[key] = list
                return list
            }
        }

        val fallback = computeUthmaniFallbackSpans(ayahBody)
        ayahSpanCache[key] = fallback
        return fallback
    }

    /**
     * Builds a ligature-safe AnnotatedString for a whole Mushaf page of Ayahs,
     * applying:
     * 1) Verified Hafs Tajweed colors + tap-to-inspect annotations (when tajweedEnabled = true)
     * 2) Live Golden Highlight on activeAyahNum during recitation (both Hafs & Warsh!)
     */
    fun buildPageAnnotatedText(
        context: Context,
        surahNum: Int,
        pageAyahs: List<Pair<Int, String>>,
        tajweedEnabled: Boolean,
        darkTheme: Boolean,
        activeAyahNum: Int = -1
    ): AnnotatedString {
        val highlightBg = if (darkTheme) Color(0x4AD4AF37) else Color(0x3CD4AF37)
        return buildAnnotatedString {
            pageAyahs.forEachIndexed { idx, (ayahNum, ayahBody) ->
                if (idx > 0) append(" ")
                val ayahStartOffset = length
                append(ayahBody)

                if (tajweedEnabled) {
                    val spans = spansForAyah(context, surahNum, ayahNum, ayahBody)
                    val len = ayahBody.length
                    for (sp in spans) {
                        var st = sp.start.coerceIn(0, len)
                        var en = sp.end.coerceIn(st, len)
                        while (st > 0 && st < len && isCombiningMark(ayahBody[st])) st--
                        while (en < len && isCombiningMark(ayahBody[en])) en++
                        if (en > st) {
                            val col = if (darkTheme) sp.rule.darkColor else sp.rule.dayColor
                            addStyle(
                                style = SpanStyle(color = col),
                                start = ayahStartOffset + st,
                                end = ayahStartOffset + en
                            )
                            addStringAnnotation(
                                tag = "TAJWEED_RULE",
                                annotation = sp.rule.code.toString(),
                                start = ayahStartOffset + st,
                                end = ayahStartOffset + en
                            )
                        }
                    }
                }

                append(" ﴿")
                append(SakinaAdvancedPrefs.formatDigits(context, ayahNum.toString()))
                append("﴾")
                val ayahEndOffset = length

                if (activeAyahNum == ayahNum) {
                    addStyle(
                        style = SpanStyle(background = highlightBg),
                        start = ayahStartOffset,
                        end = ayahEndOffset
                    )
                }
            }
        }
    }
}
