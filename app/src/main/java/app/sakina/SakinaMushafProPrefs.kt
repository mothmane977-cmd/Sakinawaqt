package app.sakina

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

/**
 * تخزين وإدارة إعدادات «مصحفي» الاحترافية (v64) — 100% أوفلاين:
 * - 5 ألوان ورق القراءة + 6 ثيمات لإطار المصحف
 * - تكبير الخط بالإيماء (Pinch-to-Zoom) + طول الصفحة + السطوع المستقل + درجة الاصفرار
 * - التصفح الأفقي/الرأسي + التصفح التلقائي + إبقاء الشاشة مضاءة + قفل الدوران
 * - تثبيت السور (Pin) + سجل البحث الموحّد + 10 علامات مرقّمة مسمّاة + جولة Spotlight
 */
data class MushafDisplaySettings(
    val fontScale: Float = 1.0f,
    val lineSpacingScale: Float = 1.0f,
    val brightness: Float = -1f, // -1f = سطوع النظام
    val warmthYellow: Float = 0f,
    val paperColorIdx: Int = 0,  // 0=بيج دافئ، 1=أبيض، 2=رمادي، 3=سماوي، 4=نعناعي
    val frameThemeIdx: Int = 0,  // 0=ذهبي، 1=أخضر، 2=أزرق، 3=بني، 4=تركواز، 5=قرمزي
    val isVerticalScroll: Boolean = false,
    val autoScrollSpeed: Int = 0, // 0=إيقاف، 1=بطيء، 2=متوسط، 3=سريع
    val keepScreenAwake: Boolean = true,
    val orientationMode: Int = 0  // 0=تلقائي، 1=عمودي، 2=أفقي
)

data class MushafBookmarkSlot(
    val slotId: Int, // 1..10
    val label: String,
    val page: Int,
    val surahName: String,
    val ayahNumber: Int,
    val timestampMs: Long
)

object SakinaMushafProPrefs {
    private const val PREF_NAME = "nurqunoot_mushaf_pro_v64"
    private fun sp(ctx: Context) = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    val PAPER_COLORS: List<Pair<String, Color>> = listOf(
        "بيج دافئ" to Color(0xFFFAF4E6),
        "أبيض نقي" to Color(0xFFFFFFFF),
        "رمادي هادئ" to Color(0xFFEEF1F3),
        "سماوي" to Color(0xFFEAF4FA),
        "نعناعي" to Color(0xFFE9F6EF)
    )

    val FRAME_THEMES: List<Pair<String, Color>> = listOf(
        "ذهبي" to Color(0xFFD8B87A),
        "أخضر" to Color(0xFF1A7F5E),
        "أزرق" to Color(0xFF2872B3),
        "بني" to Color(0xFF8C5B2C),
        "تركواز" to Color(0xFF199191),
        "قرمزي" to Color(0xFFA62F4D)
    )

    fun loadDisplay(ctx: Context): MushafDisplaySettings {
        val p = sp(ctx)
        return MushafDisplaySettings(
            fontScale = p.getFloat("font_scale", 1.0f).coerceIn(0.75f, 1.75f),
            lineSpacingScale = p.getFloat("line_scale", 1.0f).coerceIn(0.85f, 1.45f),
            brightness = p.getFloat("brightness", -1f),
            warmthYellow = p.getFloat("warmth", 0f).coerceIn(0f, 0.45f),
            paperColorIdx = p.getInt("paper_idx", 0).coerceIn(0, PAPER_COLORS.lastIndex),
            frameThemeIdx = p.getInt("frame_idx", 0).coerceIn(0, FRAME_THEMES.lastIndex),
            isVerticalScroll = p.getBoolean("vertical_scroll", false),
            autoScrollSpeed = p.getInt("auto_scroll", 0).coerceIn(0, 3),
            keepScreenAwake = p.getBoolean("keep_awake", true),
            orientationMode = p.getInt("orientation", 0).coerceIn(0, 2)
        )
    }

    fun saveDisplay(ctx: Context, s: MushafDisplaySettings) {
        sp(ctx).edit()
            .putFloat("font_scale", s.fontScale.coerceIn(0.75f, 1.75f))
            .putFloat("line_scale", s.lineSpacingScale.coerceIn(0.85f, 1.45f))
            .putFloat("brightness", s.brightness)
            .putFloat("warmth", s.warmthYellow.coerceIn(0f, 0.45f))
            .putInt("paper_idx", s.paperColorIdx.coerceIn(0, PAPER_COLORS.lastIndex))
            .putInt("frame_idx", s.frameThemeIdx.coerceIn(0, FRAME_THEMES.lastIndex))
            .putBoolean("vertical_scroll", s.isVerticalScroll)
            .putInt("auto_scroll", s.autoScrollSpeed.coerceIn(0, 3))
            .putBoolean("keep_awake", s.keepScreenAwake)
            .putInt("orientation", s.orientationMode.coerceIn(0, 2))
            .apply()
    }

    // ---- تثبيت السور (Pin) ----
    fun pinnedSurahs(ctx: Context): Set<Int> {
        val raw = sp(ctx).getString("pinned_surahs", "") ?: ""
        if (raw.isBlank()) return emptySet()
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    fun togglePinnedSurah(ctx: Context, surahNumber: Int): Set<Int> {
        val cur = pinnedSurahs(ctx).toMutableSet()
        if (!cur.add(surahNumber)) cur.remove(surahNumber)
        sp(ctx).edit().putString("pinned_surahs", cur.sorted().joinToString(",")).apply()
        return cur
    }

    // ---- سجل آخر عمليات البحث الموحّد ----
    fun searchHistory(ctx: Context): List<String> {
        val raw = sp(ctx).getString("search_history", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("||").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun addSearchHistory(ctx: Context, query: String) {
        val q = query.trim()
        if (q.length < 2) return
        val list = searchHistory(ctx).filterNot { it == q }.toMutableList()
        list.add(0, q)
        sp(ctx).edit().putString("search_history", list.take(8).joinToString("||")).apply()
    }

    fun clearSearchHistory(ctx: Context) {
        sp(ctx).edit().remove("search_history").apply()
    }

    // ---- 10 علامات مرقّمة مستقلة قابلة للتسمية والفرز ----
    fun loadBookmarks10(ctx: Context): List<MushafBookmarkSlot> {
        val raw = sp(ctx).getString("bookmarks_10_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                MushafBookmarkSlot(
                    slotId = o.optInt("id", i + 1).coerceIn(1, 10),
                    label = o.optString("label", "علامة ${i + 1}"),
                    page = o.optInt("page", 1).coerceIn(1, 604),
                    surahName = o.optString("surah", ""),
                    ayahNumber = o.optInt("ayah", 1).coerceAtLeast(1),
                    timestampMs = o.optLong("ts", 0L)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveBookmarksList(ctx: Context, list: List<MushafBookmarkSlot>) {
        val arr = JSONArray()
        list.forEach { b ->
            val o = JSONObject()
            o.put("id", b.slotId)
            o.put("label", b.label)
            o.put("page", b.page)
            o.put("surah", b.surahName)
            o.put("ayah", b.ayahNumber)
            o.put("ts", b.timestampMs)
            arr.put(o)
        }
        sp(ctx).edit().putString("bookmarks_10_json", arr.toString()).apply()
    }

    fun setBookmarkSlot(
        ctx: Context,
        slotId: Int,
        page: Int,
        surahName: String,
        ayahNumber: Int = 1,
        customLabel: String? = null
    ) {
        val id = slotId.coerceIn(1, 10)
        val cur = loadBookmarks10(ctx).filterNot { it.slotId == id }.toMutableList()
        val existingLabel = loadBookmarks10(ctx).firstOrNull { it.slotId == id }?.label
        cur.add(
            MushafBookmarkSlot(
                slotId = id,
                label = customLabel?.ifBlank { "علامة $id" } ?: existingLabel ?: "علامة $id",
                page = page.coerceIn(1, 604),
                surahName = surahName,
                ayahNumber = ayahNumber.coerceAtLeast(1),
                timestampMs = System.currentTimeMillis()
            )
        )
        saveBookmarksList(ctx, cur.sortedBy { it.slotId })
    }

    fun renameBookmarkSlot(ctx: Context, slotId: Int, newLabel: String) {
        val cur = loadBookmarks10(ctx).map {
            if (it.slotId == slotId) it.copy(label = newLabel.ifBlank { "علامة $slotId" }) else it
        }
        saveBookmarksList(ctx, cur)
    }

    fun deleteBookmarkSlot(ctx: Context, slotId: Int) {
        saveBookmarksList(ctx, loadBookmarks10(ctx).filterNot { it.slotId == slotId })
    }

    // ---- جولة التركيز البؤري الحي (Spotlight) ----
    fun hasSeenSpotlight(ctx: Context): Boolean = sp(ctx).getBoolean("seen_spotlight_v64", false)
    fun markSpotlightSeen(ctx: Context, seen: Boolean = true) {
        sp(ctx).edit().putBoolean("seen_spotlight_v64", seen).apply()
    }

    // ---- helper: تقريب الجزء والحزب لرقم صفحة (1..604) ----
    fun approxJuzForPage(page: Int): Int {
        val p = page.coerceIn(1, 604)
        return if (p <= 21) 1 else (((p - 2) / 20) + 1).coerceIn(1, 30)
    }

    fun approxHizbForPage(page: Int): Int {
        val p = page.coerceIn(1, 604)
        return (((p - 1) / 10) + 1).coerceIn(1, 60)
    }
}
