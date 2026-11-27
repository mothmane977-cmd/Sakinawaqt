package app.sakina

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * محرّك بيانات «التلاوة والحفظ والمختارات» (v65) — 100% أوفلاين:
 * - إعدادات جلسة التحفيظ الثلاثية («اسمع وكرّر»): النطاق (7 مستويات)، تكرار الآية، تكرار المقطع، وطول السكتة بقدر الآية
 * - «مختارات المصحف» المأثورة (14 مختارة نبوية ويومية)
 * - «مختاراتك» المخصصة (سور كاملة أو نطاقات آيات محددة)
 */
enum class HifzScopeLevel(val labelAr: String) {
    AYAH("آية"),
    RANGE("نطاق آيات"),
    PAGE("صفحة"),
    SURAH("سورة"),
    THUMN("ثمن"),
    RUB("ربع"),
    NISF("نصف"),
    HIZB("حزب")
}

data class HifzSessionConfig(
    val isActive: Boolean = false,
    val scope: HifzScopeLevel = HifzScopeLevel.RANGE,
    val surahNumber: Int = 1,
    val surahName: String = "الفاتحة",
    val startPage: Int = 1,
    val fromAyah: Int = 1,
    val toAyah: Int = 7,
    val ayahRepeat: Int = 3,
    val rangeRepeat: Int = 3,
    val pauseMultiplier: Int = 1, // 0=بدون سكتة، 1=بقدر الآية، 2=ضعف الآية، 3=3 أضعاف
    val postSessionTest: Boolean = true
)

data class MukhtarItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val surahNumber: Int,
    val page: Int,
    val fromAyah: Int = 1,
    val toAyah: Int = 0,
    val isCustom: Boolean = false
)

object SakinaHifzMukhtaratStore {
    private const val PREF_NAME = "nurqunoot_hifz_v65"
    private fun sp(ctx: Context) = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    val CURATED_MUKHTARAT: List<MukhtarItem> = listOf(
        MukhtarItem("c_kursi", "آية الكرسي", "سورة البقرة · آية 255", 2, 42, 255, 255),
        MukhtarItem("c_baq_end", "أواخر سورة البقرة", "سورة البقرة · الآيتان 285–286", 2, 49, 285, 286),
        MukhtarItem("c_kahf_all", "سورة الكهف كاملة", "نور ما بين الجمعتين · ص 293", 18, 293, 1, 110),
        MukhtarItem("c_kahf_10a", "أول 10 آيات من الكهف", "العصمة من الدجال · ص 293", 18, 293, 1, 10),
        MukhtarItem("c_kahf_10b", "آخر 10 آيات من الكهف", "سورة الكهف · 101–110", 18, 304, 101, 110),
        MukhtarItem("c_isra", "سورة الإسراء", "من أوراد قبل النوم · ص 282", 17, 282, 1, 111),
        MukhtarItem("c_sajdah", "سورة السجدة", "قبل النوم وفجر الجمعة · ص 415", 32, 415, 1, 30),
        MukhtarItem("c_yasin", "سورة يس", "قلب القرآن الكريم · ص 440", 36, 440, 1, 83),
        MukhtarItem("c_zumar", "سورة الزمر", "من أوراد قبل النوم · ص 458", 39, 458, 1, 75),
        MukhtarItem("c_fath", "سورة الفتح", "بشارة ونصر وسكينة · ص 511", 48, 511, 1, 29),
        MukhtarItem("c_rahman", "سورة الرحمن", "عروس القرآن · ص 531", 55, 531, 1, 78),
        MukhtarItem("c_waqiah", "سورة الواقعة", "ورد المساء · ص 534", 56, 534, 1, 96),
        MukhtarItem("c_mulk", "سورة الملك", "المنجية قبل النوم · ص 562", 67, 562, 1, 30),
        MukhtarItem("c_muawwidhat", "الإخلاص والمعوذتان", "التحصين النبوي الشامل · ص 604", 112, 604, 1, 6)
    )

    fun loadHifzConfig(ctx: Context): HifzSessionConfig {
        val p = sp(ctx)
        val scopeName = p.getString("h_scope", HifzScopeLevel.RANGE.name) ?: HifzScopeLevel.RANGE.name
        val scope = HifzScopeLevel.values().firstOrNull { it.name == scopeName } ?: HifzScopeLevel.RANGE
        return HifzSessionConfig(
            isActive = false,
            scope = scope,
            surahNumber = p.getInt("h_surah", 1).coerceIn(1, 114),
            surahName = p.getString("h_surah_name", "الفاتحة") ?: "الفاتحة",
            startPage = p.getInt("h_page", 1).coerceIn(1, 604),
            fromAyah = p.getInt("h_from", 1).coerceAtLeast(1),
            toAyah = p.getInt("h_to", 7).coerceAtLeast(1),
            ayahRepeat = p.getInt("h_ayah_rep", 3).coerceIn(1, 20),
            rangeRepeat = p.getInt("h_range_rep", 3).coerceIn(1, 20),
            pauseMultiplier = p.getInt("h_pause_mul", 1).coerceIn(0, 3),
            postSessionTest = p.getBoolean("h_post_test", true)
        )
    }

    fun saveHifzConfig(ctx: Context, cfg: HifzSessionConfig) {
        sp(ctx).edit()
            .putString("h_scope", cfg.scope.name)
            .putInt("h_surah", cfg.surahNumber.coerceIn(1, 114))
            .putString("h_surah_name", cfg.surahName)
            .putInt("h_page", cfg.startPage.coerceIn(1, 604))
            .putInt("h_from", cfg.fromAyah.coerceAtLeast(1))
            .putInt("h_to", cfg.toAyah.coerceAtLeast(cfg.fromAyah))
            .putInt("h_ayah_rep", cfg.ayahRepeat.coerceIn(1, 20))
            .putInt("h_range_rep", cfg.rangeRepeat.coerceIn(1, 20))
            .putInt("h_pause_mul", cfg.pauseMultiplier.coerceIn(0, 3))
            .putBoolean("h_post_test", cfg.postSessionTest)
            .apply()
    }

    // ---- مختاراتك المخصصة ----
    fun loadCustomMukhtarat(ctx: Context): List<MukhtarItem> {
        val raw = sp(ctx).getString("custom_mukhtarat_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                MukhtarItem(
                    id = o.optString("id", "u_$i"),
                    title = o.optString("title", ""),
                    subtitle = o.optString("sub", ""),
                    surahNumber = o.optInt("surah", 1).coerceIn(1, 114),
                    page = o.optInt("page", 1).coerceIn(1, 604),
                    fromAyah = o.optInt("from", 1).coerceAtLeast(1),
                    toAyah = o.optInt("to", 0),
                    isCustom = true
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveCustomMukhtarat(ctx: Context, list: List<MukhtarItem>) {
        val arr = JSONArray()
        list.forEach { m ->
            val o = JSONObject()
            o.put("id", m.id)
            o.put("title", m.title)
            o.put("sub", m.subtitle)
            o.put("surah", m.surahNumber)
            o.put("page", m.page)
            o.put("from", m.fromAyah)
            o.put("to", m.toAyah)
            arr.put(o)
        }
        sp(ctx).edit().putString("custom_mukhtarat_json", arr.toString()).apply()
    }

    fun addCustomMukhtar(
        ctx: Context,
        title: String,
        surahNumber: Int,
        surahName: String,
        page: Int,
        fromAyah: Int,
        toAyah: Int
    ) {
        val cur = loadCustomMukhtarat(ctx).toMutableList()
        val sub = if (toAyah > 0) "سورة $surahName · آية $fromAyah–$toAyah · ص $page" else "سورة $surahName كاملة · ص $page"
        cur.add(
            0,
            MukhtarItem(
                id = "u_${System.currentTimeMillis()}",
                title = title.ifBlank { "سورة $surahName" },
                subtitle = sub,
                surahNumber = surahNumber,
                page = page,
                fromAyah = fromAyah,
                toAyah = toAyah,
                isCustom = true
            )
        )
        saveCustomMukhtarat(ctx, cur)
    }

    fun deleteCustomMukhtar(ctx: Context, id: String) {
        saveCustomMukhtarat(ctx, loadCustomMukhtarat(ctx).filterNot { it.id == id })
    }
}
