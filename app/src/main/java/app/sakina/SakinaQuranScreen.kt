package app.sakina

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.ceil

enum class QuranRiwaya(val assetFile: String) {
    HAFS("quran_hafs_full.json"),
    WARSH("quran_warsh_full.json");

    fun displayTitle(s: SakinaStrings): String = when (this) {
        HAFS -> s.riwayaHafs
        WARSH -> s.riwayaWarsh
    }
}

data class SurahEntry(
    val number: Int,
    val nameAr: String,
    val nameLatin: String,
    val isMakki: Boolean,
    val ayahCount: Int,
    val startPage: Int,
    val ayahs: List<String>
) {
    fun localizedName(lang: SakinaLang): String =
        if (lang == SakinaLang.AR || lang == SakinaLang.UR) nameAr else nameLatin

    fun localizedType(s: SakinaStrings): String =
        if (isMakki) s.makkiWord else s.madaniWord
}

private val SURAH_NAMES_AR = listOf(
    "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال",
    "التوبة", "يونس", "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل",
    "الإسراء", "الكهف", "مريم", "طه", "الأنبياء", "الحج", "المؤمنون", "النور",
    "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم", "لقمان", "السجدة",
    "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر",
    "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح",
    "الحجرات", "ق", "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة",
    "الحديد", "المجادلة", "الحشر", "الممتحنة", "الصف", "الجمعة", "المنافقون", "التغابن",
    "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج", "نوح", "الجن",
    "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس",
    "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية",
    "الفجر", "البلد", "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق",
    "القدر", "البينة", "الزلزلة", "العاديات", "القارعة", "التكاثر", "العصر", "الهمزة",
    "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر", "المسد", "الإخلاص",
    "الفلق", "الناس"
)

private val SURAH_AYAH_COUNTS = intArrayOf(
    7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111, 43, 52, 99, 128,
    111, 110, 98, 135, 112, 78, 118, 64, 77, 227, 93, 88, 69, 60, 34, 30,
    73, 54, 45, 83, 182, 88, 75, 85, 54, 53, 89, 59, 37, 35, 38, 29,
    18, 45, 60, 49, 62, 55, 78, 96, 29, 22, 24, 13, 14, 11, 11, 18,
    12, 12, 30, 52, 52, 44, 28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
    29, 19, 36, 25, 22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19,
    5, 8, 8, 11, 11, 8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6
)

private val SURAH_START_PAGES = intArrayOf(
    1, 2, 50, 77, 106, 128, 151, 177, 187, 208, 221, 235, 249, 255, 262, 267,
    282, 293, 305, 312, 322, 332, 342, 350, 359, 367, 377, 385, 396, 404, 411, 415,
    418, 428, 434, 440, 446, 453, 458, 467, 477, 483, 489, 496, 499, 502, 507, 511,
    515, 518, 520, 523, 526, 528, 531, 534, 537, 542, 545, 549, 551, 553, 554, 556,
    558, 560, 562, 564, 566, 568, 570, 572, 574, 575, 577, 578, 580, 582, 583, 585,
    586, 587, 587, 589, 590, 591, 591, 592, 593, 594, 595, 595, 596, 596, 597, 597,
    598, 598, 599, 599, 600, 600, 601, 601, 601, 602, 602, 602, 603, 603, 603, 604,
    604, 604
)

private val MADANI_SET = setOf(
    2, 3, 4, 5, 8, 9, 22, 24, 33, 47, 48, 49, 55, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 76, 98, 99, 110
)

private val JUZ_START_PAGES = intArrayOf(
    1, 22, 42, 62, 82, 102, 121, 142, 162, 182,
    201, 222, 242, 262, 282, 302, 322, 342, 362, 382,
    402, 422, 442, 462, 482, 502, 522, 542, 562, 582
)

private val HIZB_START_PAGES = intArrayOf(
    1, 11, 22, 32, 42, 51, 62, 72, 82, 92,
    102, 111, 121, 132, 142, 151, 162, 173, 182, 192,
    201, 212, 222, 231, 242, 252, 262, 272, 282, 292,
    302, 312, 322, 332, 342, 352, 362, 371, 382, 392,
    402, 413, 422, 431, 442, 451, 462, 472, 482, 491,
    502, 513, 522, 531, 542, 553, 562, 572, 582, 591
)

private val quranCache = mutableMapOf<QuranRiwaya, List<SurahEntry>>()

private fun defaultSurahIndex(): List<SurahEntry> = (1..114).map { num ->
    SurahEntry(
        number = num,
        nameAr = SURAH_NAMES_AR[num - 1],
        nameLatin = SURAH_NAMES_LATIN.getOrElse(num - 1) { "Surah $num" },
        isMakki = num !in MADANI_SET,
        ayahCount = SURAH_AYAH_COUNTS[num - 1],
        startPage = SURAH_START_PAGES[num - 1],
        ayahs = emptyList()
    )
}

private fun surahForMushafPage(pageNum: Int): Int {
    for (i in SURAH_START_PAGES.indices.reversed()) {
        if (pageNum >= SURAH_START_PAGES[i]) return i + 1
    }
    return 1
}

private val TRAILING_AYAH_NUM_REGEX = Regex("\\s*(?:\\([0-9٠-٩]+\\)|﴿[0-9٠-٩]+﴾|\\[[0-9٠-٩]+\\])\\s*$")

private fun cleanAyahText(raw: String): String =
    raw.replace(TRAILING_AYAH_NUM_REGEX, "").trim()

private fun extractAyahText(arr: JSONArray, index: Int): String {
    val item = arr.opt(index)
    val raw = when (item) {
        is String -> item
        is JSONObject -> item.optString("text", item.optString("ayah", ""))
        else -> arr.optString(index, "")
    }
    return cleanAyahText(raw)
}

private fun openQuranJsonText(context: Context, riwaya: QuranRiwaya): String? {
    val primary = if (riwaya == QuranRiwaya.WARSH) "quran_warsh.json" else "quran_hafs.json"
    val candidates = mutableListOf(primary, riwaya.assetFile)
    try {
        val allAssets = context.assets.list("")?.toList().orEmpty()
            .filter {
                it.endsWith(".json", ignoreCase = true) &&
                    !it.contains("tajweed", ignoreCase = true) &&
                    !it.contains("reciter", ignoreCase = true) &&
                    !it.contains("timing", ignoreCase = true)
            }
        val key = if (riwaya == QuranRiwaya.WARSH) "warsh" else "hafs"
        allAssets.filter { it.contains(key, ignoreCase = true) }
            .forEach { if (it !in candidates) candidates.add(it) }
        allAssets.filter { it.contains("quran", ignoreCase = true) }
            .forEach { if (it !in candidates) candidates.add(it) }
    } catch (_: Exception) {}

    for (name in candidates) {
        try {
            val txt = context.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }.trim()
            if (txt.length > 100) return txt
        } catch (_: Exception) {}
    }
    return null
}

private fun loadMushafAsset(context: Context, riwaya: QuranRiwaya): List<SurahEntry> {
    quranCache[riwaya]?.let { cached ->
        if (cached.any { it.ayahs.isNotEmpty() }) return cached
    }
    return try {
        val raw = openQuranJsonText(context, riwaya) ?: return defaultSurahIndex()
        val parsed: List<SurahEntry> = if (raw.startsWith("{")) {
            val root = JSONObject(raw)
            when {
                root.has("1") -> (1..114).map { num ->
                    val arr = root.optJSONArray(num.toString())
                    val list = if (arr != null) List(arr.length()) { i -> extractAyahText(arr, i) } else emptyList()
                    SurahEntry(
                        number = num,
                        nameAr = SURAH_NAMES_AR[num - 1],
                        nameLatin = SURAH_NAMES_LATIN.getOrElse(num - 1) { "Surah $num" },
                        isMakki = num !in MADANI_SET,
                        ayahCount = if (list.isNotEmpty()) list.size else SURAH_AYAH_COUNTS[num - 1],
                        startPage = SURAH_START_PAGES[num - 1],
                        ayahs = list
                    )
                }
                root.has("quran") -> {
                    val verses = root.getJSONArray("quran")
                    val byChapter = Array(115) { mutableListOf<String>() }
                    for (i in 0 until verses.length()) {
                        val v = verses.getJSONObject(i)
                        val ch = v.optInt("chapter", 0)
                        if (ch in 1..114) byChapter[ch].add(cleanAyahText(v.optString("text", "")))
                    }
                    (1..114).map { num ->
                        val list = byChapter[num]
                        SurahEntry(
                            number = num,
                            nameAr = SURAH_NAMES_AR[num - 1],
                            nameLatin = SURAH_NAMES_LATIN.getOrElse(num - 1) { "Surah $num" },
                            isMakki = num !in MADANI_SET,
                            ayahCount = if (list.isNotEmpty()) list.size else SURAH_AYAH_COUNTS[num - 1],
                            startPage = SURAH_START_PAGES[num - 1],
                            ayahs = list
                        )
                    }
                }
                else -> defaultSurahIndex()
            }
        } else defaultSurahIndex()
        quranCache[riwaya] = parsed
        parsed
    } catch (_: Exception) {
        defaultSurahIndex()
    }
}

@Composable
fun SakinaQuranScreen(ctx: Context, modifier: Modifier = Modifier, onReaderActiveChanged: (Boolean) -> Unit = {}) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    val isArOrUr = lang == SakinaLang.AR || lang == SakinaLang.UR
    val sp = remember { ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    var riwaya by remember {
        val saved = sp.getString("quran_riwaya", QuranRiwaya.HAFS.name)
        mutableStateOf(if (saved == QuranRiwaya.WARSH.name) QuranRiwaya.WARSH else QuranRiwaya.HAFS)
    }
    var indexTab by remember { mutableStateOf(QuranIndexTab.SURAHS) }
    var searchQuery by remember { mutableStateOf("") }
    var isReaderOpen by remember { mutableStateOf(false) }
    var bookmarkTick by remember { mutableIntStateOf(0) }
    var currentPage by remember { mutableIntStateOf((sp.getInt("quran_last_page", 0) + 1).coerceIn(1, 604)) }
    var tajweedEnabled by remember { mutableStateOf(sp.getBoolean("v29_tajweed_on", true)) }
    var readerTheme by remember {
        mutableStateOf(
            if (SakinaAdvancedPrefs.mushafTheme(ctx) == 2) QuranReaderTheme.NIGHT_EMERALD else QuranReaderTheme.WARM_PARCHMENT
        )
    }
    var selectedSurahForPlayer by remember { mutableIntStateOf(sp.getInt("quran_last_surah", 1).coerceIn(1, 114)) }
    var showReciterDialog by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var deleteTargetSurahNum by remember { mutableStateOf<Int?>(null) }

    var surahs by remember { mutableStateOf(defaultSurahIndex()) }
    LaunchedEffect(riwaya) {
        surahs = withContext(Dispatchers.IO) { loadMushafAsset(ctx, riwaya) }
    }
    LaunchedEffect(Unit) {
        SakinaQuranAudioController.connect(ctx)
    }

    BackHandler(enabled = isReaderOpen) { isReaderOpen = false }

    val audioState by SakinaQuranAudioController.uiState.collectAsState()
    val activeDownloads by SakinaQuranDownloadManager.downloads.collectAsState()
    val storageVersion by SakinaQuranDownloadManager.storageVersion.collectAsState()
    val selectedReciter = remember(riwaya, storageVersion) {
        SakinaQuranDownloadManager.getSelectedReciter(ctx, riwaya)
    }

    val surahUiList = remember(surahs, selectedReciter, activeDownloads, storageVersion, lang) {
        surahs.map { sr ->
            val dlKey = SakinaQuranDownloadManager.downloadKey(selectedReciter.id, sr.number)
            val dlProg = activeDownloads[dlKey]
            val ready = SakinaQuranDownloadManager.isSurahReadyOffline(ctx, selectedReciter.id, sr.number)
            val status = when {
                dlProg != null -> SurahAudioStatus.DOWNLOADING
                ready -> SurahAudioStatus.DOWNLOADED
                else -> SurahAudioStatus.NOT_DOWNLOADED
            }
            SurahAudioItem(
                number = sr.number,
                arabicName = sr.localizedName(lang),
                localizedName = sr.nameLatin,
                revelationType = sr.localizedType(s),
                ayahCount = sr.ayahCount,
                startPage = sr.startPage,
                audioSizeMb = SakinaQuranDownloadManager.getSurahSizeMb(selectedReciter, sr.number, sr.ayahCount).toFloat(),
                audioStatus = status,
                downloadProgress = dlProg ?: 0f
            )
        }
    }

    val quickSurahs = remember(surahUiList) {
        listOf(18, 36, 55, 56).mapNotNull { num -> surahUiList.firstOrNull { it.number == num } }
    }

    val juzList = remember(lang) {
        (1..30).map { j ->
            val spg = JUZ_START_PAGES[j - 1]
            val sName = SURAH_NAMES_AR.getOrElse(surahForMushafPage(spg) - 1) { "" }
            QuranDivisionItem(
                number = j,
                title = sakinaTr(lang, "الجزء $j", "Juz $j", "Juz $j", "Cüz $j", "Juz $j", "پارہ $j"),
                subtitle = if (isArOrUr) "سورة $sName" else sName,
                startPage = spg
            )
        }
    }

    val hizbList = remember(lang) {
        (1..60).map { h ->
            val spg = HIZB_START_PAGES[h - 1]
            val sName = SURAH_NAMES_AR.getOrElse(surahForMushafPage(spg) - 1) { "" }
            QuranDivisionItem(
                number = h,
                title = sakinaTr(lang, "الحزب $h", "Hizb $h", "Hizb $h", "Hizib $h", "Hizb $h", "حزب $h"),
                subtitle = "${sakinaTr(lang, "الجزء", "Juz", "Juz", "Cüz", "Juz", "پارہ")} ${((h - 1) / 2) + 1} · $sName",
                startPage = spg
            )
        }
    }

    val activeReaderSurahNum = surahForMushafPage(currentPage)
    val playerSurahNum = when {
        audioState.isPlaying && audioState.currentSurahNum > 0 -> audioState.currentSurahNum
        isReaderOpen -> activeReaderSurahNum
        else -> selectedSurahForPlayer
    }
    val playerSurahItem = surahUiList.firstOrNull { it.number == playerSurahNum } ?: surahUiList.first()
    val isThisTrackPlaying = audioState.isPlaying && audioState.currentSurahNum == playerSurahNum && audioState.currentReciterId == selectedReciter.id
    val isThisTrackLoaded = audioState.currentSurahNum == playerSurahNum && audioState.currentReciterId == selectedReciter.id

    val playerState = QuranAudioPlayerState(
        selectedSurahNumber = playerSurahNum,
        selectedSurahName = if (isArOrUr) "سورة ${SURAH_NAMES_AR.getOrElse(playerSurahNum - 1) { "" }}" else "Surah ${playerSurahItem.localizedName}",
        reciterName = selectedReciter.displayName(lang),
        audioStatus = playerSurahItem.audioStatus,
        audioSizeMb = playerSurahItem.audioSizeMb,
        downloadProgress = playerSurahItem.downloadProgress,
        isPlaying = isThisTrackPlaying,
        currentPositionSec = if (isThisTrackLoaded) (audioState.positionMs / 1000L).toInt() else 0,
        totalDurationSec = if (isThisTrackLoaded) (audioState.durationMs / 1000L).toInt() else 0,
        playbackSpeed = audioState.playbackSpeed,
        isRepeatEnabled = audioState.repeatSurah,
        isSleepTimerActive = audioState.sleepTimerMinutes != 0
    )

    val buildAyahsForPage: (Int) -> List<MushafAyahUi> = { pNum ->
        val sNum = surahForMushafPage(pNum)
        val sObj = surahs.firstOrNull { it.number == sNum }
        val all = sObj?.ayahs.orEmpty()
        if (all.isEmpty()) emptyList()
        else {
            val sStart = SURAH_START_PAGES.getOrElse(sNum - 1) { 1 }
            val sEnd = if (sNum < 114) (SURAH_START_PAGES[sNum] - 1).coerceAtLeast(sStart) else 604
            val totalPages = (sEnd - sStart + 1).coerceAtLeast(1)
            val relPage = (pNum - sStart).coerceIn(0, totalPages - 1)
            val perPage = ceil(all.size.toDouble() / totalPages.toDouble()).toInt().coerceAtLeast(1)
            val fromIdx = (relPage * perPage).coerceIn(0, all.size)
            val toIdx = if (relPage == totalPages - 1) all.size else ((relPage + 1) * perPage).coerceIn(fromIdx, all.size)
            val isDark = readerTheme == QuranReaderTheme.NIGHT_EMERALD
            val applyHafsTajweed = tajweedEnabled && riwaya == QuranRiwaya.HAFS
            (fromIdx until toIdx).map { i ->
                val ayNum = i + 1
                val body = all[i]
                val styled = SakinaTajweedRepository.buildPageAnnotatedText(
                    context = ctx,
                    surahNum = sNum,
                    pageAyahs = listOf(ayNum to body),
                    tajweedEnabled = applyHafsTajweed,
                    darkTheme = isDark,
                    activeAyahNum = -1
                )
                val cutIdx = styled.text.lastIndexOf(" ﴿")
                val cleanStyled = if (cutIdx > 0) styled.subSequence(0, cutIdx) else styled
                MushafAyahUi(ayahNumber = ayNum, styledText = cleanStyled)
            }
        }
    }

    val lastReadSurahNum = sp.getInt("quran_last_surah", 1).coerceIn(1, 114)
    val lastReadPageNum = (sp.getInt("quran_last_page", 0) + 1).coerceIn(1, 604)

    val quranStrings = SakinaQuranStrings(
        screenTitle = sakinaTr(lang, "القرآن الكريم", "The Holy Quran", "Le Saint Coran", "Kur'an-ı Kerim", "Al-Qur'an", "قرآن کریم"),
        hafsLabel = s.riwayaHafs,
        warshLabel = s.riwayaWarsh,
        hafsShort = if (isArOrUr) "حفص" else "Hafs",
        warshShort = if (isArOrUr) "ورش" else "Warsh",
        searchPlaceholder = sakinaTr(lang, "ابحث عن اسم السورة أو رقمها...", "Search Surah name or number...", "Rechercher une sourate...", "Sure ara...", "Cari surah...", "سورۃ تلاش کریں..."),
        lastReadFormat = sakinaTr(lang, "آخر قراءة: %s · ص %d / %d", "Last read: %s · p. %d / %d", "Dernière lecture : %s · p. %d / %d", "Son okuma: %s · s. %d / %d", "Terakhir dibaca: %s · h. %d / %d", "آخری تلاوت: %s · ص %d / %d"),
        resumeLabel = s.resumeLabel,
        tabSurahs = sakinaTr(lang, "السور (114)", "Surahs (114)", "Sourates (114)", "Sureler (114)", "Surah (114)", "سورتیں (114)"),
        tabJuzs = sakinaTr(lang, "الأجزاء (30)", "Juz (30)", "Juz (30)", "Cüz (30)", "Juz (30)", "پارے (30)"),
        tabHizbs = sakinaTr(lang, "الأحزاب (60)", "Hizb (60)", "Hizb (60)", "Hizib (60)", "Hizb (60)", "احزاب (60)"),
        offlineReadyLabel = if (isArOrUr) "أوفلاين ✓" else "Offline ✓",
        downloadFormat = if (isArOrUr) "تحميل · %.1f MB" else "Download · %.1f MB",
        tajweedLabel = sakinaTr(lang, "التجويد", "Tajweed", "Tajwid", "Tecvid", "Tajwid", "تجوید"),
        warshUsulLabel = sakinaTr(lang, "أصول ورش", "Warsh Usul", "Règles Warch", "Verş Usulü", "Usul Warsy", "اصول ورش"),
        prevPageLabel = sakinaTr(lang, "السابقة", "Prev", "Préc.", "Önceki", "Sblm", "پچھلا"),
        nextPageLabel = sakinaTr(lang, "التالية", "Next", "Suiv.", "Sonraki", "Brikut", "اگلا")
    )

    val screenState = SakinaQuranScreenState(
        riwayah = if (riwaya == QuranRiwaya.WARSH) QuranRiwayah.WARSH else QuranRiwayah.HAFS,
        indexTab = indexTab,
        searchQuery = searchQuery,
        lastReadSurahName = if (isArOrUr) "سورة ${SURAH_NAMES_AR[lastReadSurahNum - 1]}" else SURAH_NAMES_LATIN.getOrElse(lastReadSurahNum - 1) { "Al-Fatihah" },
        lastReadPage = lastReadPageNum,
        totalPages = 604,
        quickSurahs = quickSurahs,
        surahs = surahUiList,
        juzs = juzList,
        hizbs = hizbList,
        isReaderOpen = isReaderOpen,
        currentReaderSurah = if (isArOrUr) "سورة ${SURAH_NAMES_AR[activeReaderSurahNum - 1]}" else SURAH_NAMES_LATIN.getOrElse(activeReaderSurahNum - 1) { "Al-Fatihah" },
        currentReaderPage = currentPage,
        currentPageAyahs = emptyList(),
        isTajweedEnabled = tajweedEnabled,
        readerTheme = readerTheme,
        player = playerState
    )

    SakinaLuminousQuranScreen(
        state = screenState,
        strings = quranStrings,
        headingFontFamily = SakinaHeadingFont,
        bodyFontFamily = SakinaBodyFont,
        quranFontFamily = SakinaNaskhFontFamily,
        showBottomDock = false,
        hostBottomBarReserve = 82.dp,
        onReaderActiveChanged = onReaderActiveChanged,
        pageAyahsProvider = buildAyahsForPage,
        onRiwayahChange = { newR ->
            riwaya = if (newR == QuranRiwayah.WARSH) QuranRiwaya.WARSH else QuranRiwaya.HAFS
            sp.edit().putString("quran_riwaya", riwaya.name).apply()
        },
        onIndexTabChange = { indexTab = it },
        onSearchQueryChange = { searchQuery = it },
        onResumeClick = {
            currentPage = lastReadPageNum
            selectedSurahForPlayer = lastReadSurahNum
            isReaderOpen = true
        },
        onSurahSelected = { sr ->
            selectedSurahForPlayer = sr.number
            currentPage = sr.startPage.coerceIn(1, 604)
            sp.edit().putInt("quran_last_page", currentPage - 1).putInt("quran_last_surah", sr.number).apply()
            isReaderOpen = true
        },
        onDivisionSelected = { div ->
            currentPage = div.startPage.coerceIn(1, 604)
            val sNum = surahForMushafPage(currentPage)
            selectedSurahForPlayer = sNum
            sp.edit().putInt("quran_last_page", currentPage - 1).putInt("quran_last_surah", sNum).apply()
            isReaderOpen = true
        },
        onCloseReader = { isReaderOpen = false },
        onToggleTajweed = {
            tajweedEnabled = !tajweedEnabled
            sp.edit().putBoolean("v29_tajweed_on", tajweedEnabled).apply()
        },
        onToggleReaderTheme = {
            readerTheme = if (readerTheme == QuranReaderTheme.WARM_PARCHMENT) QuranReaderTheme.NIGHT_EMERALD else QuranReaderTheme.WARM_PARCHMENT
        },
        isCurrentPageBookmarked = remember(currentPage, bookmarkTick) {
            SakinaV45StatsPrefs.isPageBookmarked(ctx, currentPage)
        },
        onToggleBookmark = {
            val sNum = surahForMushafPage(currentPage)
            val sName = SURAH_NAMES_AR.getOrElse(sNum - 1) { "ص $currentPage" }
            SakinaV45StatsPrefs.toggleBookmark(ctx, currentPage, sName)
            bookmarkTick++
        },
        onPageChanged = { newPage ->
            if (newPage != currentPage) {
                SakinaV45StatsPrefs.logPageRead(ctx)
            }
            val clamped = newPage.coerceIn(1, 604)
            currentPage = clamped
            val sNum = surahForMushafPage(clamped)
            selectedSurahForPlayer = sNum
            sp.edit().putInt("quran_last_page", clamped - 1).putInt("quran_last_surah", sNum).apply()
        },
        onPlayPauseClick = {
            val nameAr = SURAH_NAMES_AR.getOrElse(playerSurahNum - 1) { "الفاتحة" }
            SakinaQuranAudioController.togglePlayPause(
                context = ctx,
                targetSurahNum = playerSurahNum,
                targetSurahNameAr = nameAr,
                reciterId = selectedReciter.id,
                reciterNameAr = selectedReciter.nameAr
            )
        },
        onDownloadSurahClick = { sNum ->
            val nameAr = SURAH_NAMES_AR.getOrElse(sNum - 1) { "الفاتحة" }
            SakinaQuranDownloadManager.enqueueDownload(ctx, selectedReciter, sNum, nameAr)
        },
        onDeleteDownloadedSurah = { sNum ->
            if (SakinaQuranDownloadManager.isDownloadedOnDisk(ctx, selectedReciter.id, sNum)) {
                deleteTargetSurahNum = sNum
            }
        },
        onSeekToSec = { sec -> SakinaQuranAudioController.seekTo(sec * 1000L) },
        onSkipBySec = { deltaSec -> SakinaQuranAudioController.seekBy(deltaSec * 1000L) },
        onCycleSpeedClick = { SakinaQuranAudioController.cycleSpeed() },
        onToggleRepeatClick = { SakinaQuranAudioController.toggleRepeat() },
        onSleepTimerClick = { showSleepDialog = true },
        onReciterClick = { showReciterDialog = true }
    )

    deleteTargetSurahNum?.let { delNum ->
        val mb = SakinaQuranDownloadManager.downloadedFileSizeMb(ctx, selectedReciter.id, delNum)
        val sName = SURAH_NAMES_AR.getOrElse(delNum - 1) { "" }
        AlertDialog(
            onDismissRequest = { deleteTargetSurahNum = null },
            containerColor = theme.bgCard,
            title = {
                Text(
                    text = if (isArOrUr) "حذف تلاوة سورة $sName؟" else "Delete Surah ${SURAH_NAMES_LATIN.getOrElse(delNum - 1) { "" }}?",
                    fontFamily = SakinaHeadingFont,
                    color = theme.accentBright
                )
            },
            text = {
                Text(
                    text = if (isArOrUr) "سيتم حذف الملف المحفوظ (${SakinaQuranDownloadManager.formatSizeMb(mb)}) وتحرير المساحة من جهازك."
                    else "Remove offline audio (${SakinaQuranDownloadManager.formatSizeMb(mb)}) to free device storage.",
                    fontFamily = SakinaBodyFont,
                    color = SakinaBrandColors.LuminousIvory
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFC62828))
                        .clickable {
                            SakinaQuranDownloadManager.deleteDownloadedSurah(ctx, selectedReciter.id, delNum)
                            deleteTargetSurahNum = null
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(if (isArOrUr) "حذف" else "Delete", fontFamily = SakinaBodyFont, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = { SakinaBackIconButton(onClick = { deleteTargetSurahNum = null }) }
        )
    }

    if (showReciterDialog) {
        val reciters = remember(riwaya) { SakinaQuranDownloadManager.recitersForRiwaya(ctx, riwaya) }
        AlertDialog(
            onDismissRequest = { showReciterDialog = false },
            containerColor = theme.bgCard,
            title = {
                Text(
                    text = if (isArOrUr) "اختر القارئ (${if (riwaya == QuranRiwaya.WARSH) "ورش" else "حفص"})" else "Select Reciter",
                    fontFamily = SakinaHeadingFont,
                    color = theme.accentBright
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reciters.forEach { rec ->
                        val isSel = rec.id == selectedReciter.id
                        val readyHere = SakinaQuranDownloadManager.isSurahReadyOffline(ctx, rec.id, playerSurahNum)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) Color(0x2AD4AF37) else Color(0x12FFFFFF))
                                .border(if (isSel) 1.dp else 0.5.dp, if (isSel) Color(0xFFD4AF37) else Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                                .clickable {
                                    SakinaQuranDownloadManager.setSelectedReciter(ctx, rec)
                                    showReciterDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rec.displayName(lang),
                                fontFamily = SakinaBodyFont,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.5.sp,
                                color = if (isSel) Color(0xFFF3DFA2) else SakinaBrandColors.LuminousIvory
                            )
                            Text(
                                text = if (readyHere) (if (isArOrUr) "أوفلاين ✓" else "Offline ✓") else "☁",
                                fontFamily = SakinaBodyFont,
                                fontSize = 11.sp,
                                color = if (readyHere) Color(0xFFA7F3D0) else Color(0x99D4AF37)
                            )
                        }
                    }
                }
            },
            confirmButton = { SakinaBackIconButton(onClick = { showReciterDialog = false }) }
        )
    }

    if (showSleepDialog) {
        val options = listOf(
            0 to (if (isArOrUr) "إيقاف المؤقت" else "Timer Off"),
            15 to (if (isArOrUr) "15 دقيقة" else "15 minutes"),
            30 to (if (isArOrUr) "30 دقيقة" else "30 minutes"),
            60 to (if (isArOrUr) "60 دقيقة" else "60 minutes"),
            -1 to (if (isArOrUr) "إيقاف عند نهاية السورة الحالية" else "Stop at end of Surah")
        )
        AlertDialog(
            onDismissRequest = { showSleepDialog = false },
            containerColor = theme.bgCard,
            title = { Text(if (isArOrUr) "مؤقت النوم 🌙" else "Sleep Timer 🌙", fontFamily = SakinaHeadingFont, color = theme.accentBright) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { (mins, lbl) ->
                        val active = audioState.sleepTimerMinutes == mins
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) Color(0x2AD4AF37) else Color(0x12FFFFFF))
                                .clickable {
                                    SakinaQuranAudioController.setSleepTimerMinutes(mins)
                                    showSleepDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = lbl,
                                fontFamily = SakinaBodyFont,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) Color(0xFFF3DFA2) else SakinaBrandColors.LuminousIvory
                            )
                            if (active) Text("✓", color = Color(0xFFA7F3D0), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = { SakinaBackIconButton(onClick = { showSleepDialog = false }) }
        )
    }
}
