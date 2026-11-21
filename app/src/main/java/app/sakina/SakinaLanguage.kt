package app.sakina

import androidx.compose.runtime.compositionLocalOf
import java.util.Locale

enum class SakinaLang(
    val code: String,
    val flag: String,
    val nativeName: String,
    val isRtl: Boolean,
    val locale: Locale
) {
    AR("ar", "🇩🇿", "العربية", true, Locale("ar")),
    EN("en", "🇬🇧", "English", false, Locale.ENGLISH),
    FR("fr", "🇫🇷", "Français", false, Locale.FRENCH),
    TR("tr", "🇹🇷", "Türkçe", false, Locale("tr")),
    ID("id", "🇮🇩", "Indonesia", false, Locale("id")),
    UR("ur", "🇵🇰", "اردو", true, Locale("ur"))
}

data class SakinaStrings(
    val appName: String,
    val tabHome: String,
    val tabQibla: String,
    val tabQuran: String,
    val tabAdhkar: String,
    val tabCalendar: String,
    val nextPrayer: String,
    val midnight: String,
    val lastThird: String,
    val todayLabel: String,
    val qiblaTitle: String,
    val qiblaTargetLabel: String,
    val yourHeadingLabel: String,
    val onQibla: String,
    val turnPhone: String,
    val rightDir: String,
    val leftDir: String,
    val noSensor: String,
    val kmFromMakkah: String,
    val stableField: String,
    val magInterference: String,
    val accHigh: String,
    val accMed: String,
    val accLow: String,
    val cardinals: List<String>,
    val quranTitle: String,
    val riwayaHafs: String,
    val riwayaWarsh: String,
    val surahsBtn: String,
    val searchSurahHint: String,
    val lastReadLabel: String,
    val riwayaLabel: String,
    val resumeLabel: String,
    val ayahWord: String,
    val makkiWord: String,
    val madaniWord: String,
    val adhkarTitle: String,
    val tasbihPrompt: String,
    val adhkarCatTitles: List<String>,
    val asmaTitle: String,
    val calendarTitle: String,
    val weekdays: List<String>,
    val hijriMonths: List<String>,
    val dayPrayersCardTitle: String,
    val dailyHadithTitle: String,
    val settingsTitle: String,
    val themeTitle: String,
    val themeNames: List<String>,
    val activeWord: String,
    val languageLabel: String,
    val locationLabel: String,
    val autoGps: String,
    val manualPick: String,
    val hijriAdjLabel: String,
    val calcMethod: String,
    val alarmTitle: String,
    val suhoorTitle: String,
    val minsBeforeFajr: String,
    val minuteShort: String,
    val volumeLabel: String,
    val fajrFadeLabel: String,
    val defaultSoundLabel: String,
    val adhanVoiceLabel: String,
    val soundModes: List<String>,
    val testAlarmBtn: String,
    val testAlarmScheduled: String,
    val duasTitle: String,
    val duasMainTab: String,
    val hisnMuslimTab: String,
    val duaSectionsBtn: String,
    val duaCatTitles: List<String>,
    val duaCatSubs: List<String>,
    val ruqyahTitle: String,
    val ruqyahTypesBtn: String,
    val ruqyahCatTitles: List<String>,
    val ruqyahCatSubs: List<String>,
    val sunanTitle: String,
    val duhaTitle: String,
    val qiyamTitle: String,
    val fastingTitle: String,
    val occasionsTitle: String,
    val backLabel: String,
    val prayers: List<String>
)

val LocalSakinaStrings = compositionLocalOf { stringsFor(SakinaLang.AR) }
val LocalSakinaLang = compositionLocalOf { SakinaLang.AR }

fun localizeCityName(raw: String, lang: SakinaLang): String {
    if (lang == SakinaLang.AR || lang == SakinaLang.UR) return raw
    return when (raw.trim()) {
        "فرندة" -> "Frenda"
        "الجزائر العاصمة" -> if (lang == SakinaLang.FR) "Alger" else "Algiers"
        "وهران" -> "Oran"
        "قسنطينة" -> "Constantine"
        "تيارت" -> "Tiaret"
        "مكة المكرمة" -> if (lang == SakinaLang.FR) "La Mecque" else "Makkah"
        else -> raw
    }
}

fun localizeMethodName(raw: String, lang: SakinaLang): String {
    if (lang == SakinaLang.AR || lang == SakinaLang.UR) return raw
    return when {
        raw.contains("الجزائر") -> when (lang) {
            SakinaLang.FR -> "Algérie (18°/17°)"
            SakinaLang.TR -> "Cezayir (18°/17°)"
            SakinaLang.ID -> "Aljazair (18°/17°)"
            else -> "Algeria (18°/17°)"
        }
        raw.contains("رابطة") || raw.contains("MWL") -> "Muslim World League"
        raw.contains("أم القرى") -> "Umm al-Qura (Makkah)"
        raw.contains("مصر") -> "Egyptian General Authority"
        raw.contains("كراتشي") -> "Karachi (Hanafi)"
        raw.contains("المغرب") -> if (lang == SakinaLang.FR) "Maroc (Habous 19°/17°)" else "Morocco (Awqaf 19°/17°)"
        raw.contains("تونس") -> if (lang == SakinaLang.FR) "Tunisie (18°/18°)" else "Tunisia (18°/18°)"
        raw.contains("ISNA") -> "ISNA (North America 15°)"
        else -> raw
    }
}

val SURAH_NAMES_LATIN = listOf(
    "Al-Fatihah", "Al-Baqarah", "Ali 'Imran", "An-Nisa", "Al-Ma'idah", "Al-An'am", "Al-A'raf", "Al-Anfal",
    "At-Tawbah", "Yunus", "Hud", "Yusuf", "Ar-Ra'd", "Ibrahim", "Al-Hijr", "An-Nahl",
    "Al-Isra", "Al-Kahf", "Maryam", "Ta-Ha", "Al-Anbiya", "Al-Hajj", "Al-Mu'minun", "An-Nur",
    "Al-Furqan", "Ash-Shu'ara", "An-Naml", "Al-Qasas", "Al-'Ankabut", "Ar-Rum", "Luqman", "As-Sajdah",
    "Al-Ahzab", "Saba", "Fatir", "Ya-Sin", "As-Saffat", "Sad", "Az-Zumar", "Ghafir",
    "Fussilat", "Ash-Shura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jathiyah", "Al-Ahqaf", "Muhammad", "Al-Fath",
    "Al-Hujurat", "Qaf", "Adh-Dhariyat", "At-Tur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqi'ah",
    "Al-Hadid", "Al-Mujadila", "Al-Hashr", "Al-Mumtahanah", "As-Saff", "Al-Jumu'ah", "Al-Munafiqun", "At-Taghabun",
    "At-Talaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Ma'arij", "Nuh", "Al-Jinn",
    "Al-Muzzammil", "Al-Muddaththir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba", "An-Nazi'at", "'Abasa",
    "At-Takwir", "Al-Infitar", "Al-Mutaffifin", "Al-Inshiqaq", "Al-Buruj", "At-Tariq", "Al-A'la", "Al-Ghashiyah",
    "Al-Fajr", "Al-Balad", "Ash-Shams", "Al-Layl", "Ad-Duha", "Ash-Sharh", "At-Tin", "Al-'Alaq",
    "Al-Qadr", "Al-Bayyinah", "Az-Zalzalah", "Al-'Adiyat", "Al-Qari'ah", "At-Takathur", "Al-'Asr", "Al-Humazah",
    "Al-Fil", "Quraysh", "Al-Ma'un", "Al-Kawthar", "Al-Kafirun", "An-Nasr", "Al-Masad", "Al-Ikhlas",
    "Al-Falaq", "An-Nas"
)

fun stringsFor(lang: SakinaLang): SakinaStrings = when (lang) {
    SakinaLang.AR -> SakinaStrings(
        appName = "سَكِينة",
        tabHome = "الرئيسية", tabQibla = "القبلة", tabQuran = "القرآن الكريم", tabAdhkar = "الأذكار", tabCalendar = "التقويم",
        nextPrayer = "الصلاة القادمة", midnight = "منتصف الليل", lastThird = "الثلث الأخير", todayLabel = "اليوم",
        qiblaTitle = "اتِّجَاهُ القِبْلَة", qiblaTargetLabel = "القبلة", yourHeadingLabel = "اتجاهك",
        onQibla = "أنت باتجاه القبلة تماماً", turnPhone = "أدر الهاتف", rightDir = "يميناً", leftDir = "يساراً",
        noSensor = "الحساس المغناطيسي غير متوفر", kmFromMakkah = "كم عن مكة المكرمة",
        stableField = "مجال مستقر", magInterference = "تشويش مغناطيسي · حرّك الهاتف ∞",
        accHigh = "دقة البوصلة: عالية", accMed = "دقة البوصلة: متوسطة", accLow = "دقة البوصلة: تحتاج معايرة ∞",
        cardinals = listOf("شمال", "ش.شرق", "شرق", "ج.شرق", "جنوب", "ج.غرب", "غرب", "ش.غرب"),
        quranTitle = "القُرْآنُ الكَرِيم", riwayaHafs = "حفص عن عاصم", riwayaWarsh = "ورش عن نافع",
        surahsBtn = "السور", searchSurahHint = "ابحث عن اسم السورة أو رقمها...",
        lastReadLabel = "آخر قراءة", riwayaLabel = "الرواية", resumeLabel = "متابعة",
        ayahWord = "آية", makkiWord = "مكية", madaniWord = "مدنية",
        adhkarTitle = "الأَذْكَارُ وَالتَّسْبِيح", tasbihPrompt = "المس للتسبيح",
        adhkarCatTitles = listOf("أذكار الصباح", "أذكار المساء", "أذكار بعد الصلاة", "أذكار النوم", "أذكار الدخول والخروج من المسجد", "أذكار الدخول والخروج من البيت"),
        asmaTitle = "أسماء الله الحسنى · اسم اليوم",
        calendarTitle = "التَّقْوِيمُ الهِجْرِيّ",
        weekdays = listOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة"),
        hijriMonths = listOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"),
        dayPrayersCardTitle = "توقيت صلوات هذا اليوم", dailyHadithTitle = "حديث اليوم",
        settingsTitle = "الإِعْدَادَاتُ العَامَّة", themeTitle = "الثيمات الفاخرة (5)",
        themeNames = listOf("الأزرق الملكي", "الزمردي الحرمي", "الأسود AMOLED", "العنّابي الأندلسي", "الكهرماني المكي"),
        activeWord = "مفعّل",
        languageLabel = "اللغة / Language", locationLabel = "الموقع", autoGps = "تلقائي (GPS)", manualPick = "اختيار مدينة",
        hijriAdjLabel = "تعديل التاريخ الهجري", calcMethod = "طريقة الحساب",
        alarmTitle = "المُنَبِّهَاتُ وَالتَّنْبِيهَات", suhoorTitle = "منبه السحور والقيام", minsBeforeFajr = "دقيقة قبل أذان الفجر",
        minuteShort = "د", volumeLabel = "مستوى صوت الأذان", fajrFadeLabel = "تدرج لطيف لصوت الفجر",
        defaultSoundLabel = "نمط التنبيه الافتراضي", adhanVoiceLabel = "صوت المؤذن", soundModes = listOf("أذان كامل", "تكبير", "اهتزاز", "صامت"),
        testAlarmBtn = "تجربة المنبه الآن (بعد 10 ثوانٍ)", testAlarmScheduled = "تم جدولة منبه تجريبي بعد 10 ثوانٍ",
        duasTitle = "الأَدْعِيَة", duasMainTab = "الأدعية المأثورة", hisnMuslimTab = "حصن المسلم",
        duaSectionsBtn = "الأقسام",
        duaCatTitles = listOf("الأدعية القرآنية الجامعة", "الأدعية النبوية المأثورة", "أدعية تفريج الكرب والهمّ والدَّين", "أدعية الوالدين والأهل والذرية", "دعاء السفر والركوب"),
        duaCatSubs = listOf("ربنا آتنا في الدنيا حسنة وفي الآخرة حسنة", "جوامع الدعاء الثابتة في الصحيحين", "دعاء ذي النون وأدعية قضاء الدين", "رب ارحمهما كما ربياني صغيراً", "سبحان الذي سخر لنا هذا وما كنا له مقرنين"),
        ruqyahTitle = "الرُّقْيَةُ الشَّرْعِيَّة", ruqyahTypesBtn = "الأقسام",
        ruqyahCatTitles = listOf("الرقية الشرعية الشاملة", "رقية السكينة والطمأنينة", "رقية التحصين من العين والحسد", "رقية الشفاء"),
        ruqyahCatSubs = listOf("الفاتحة وآية الكرسي والمعوذات", "لراحة القلب والسكينة والرقاد", "التحصين النبوي المأثور في الصحيحين", "توضع اليد على موضع الألم مع الدعاء المأثور"),
        sunanTitle = "السنن والرواتب (12 ركعة)", duhaTitle = "صلاة الضحى (صلاة الأوابين)",
        qiyamTitle = "قيام الليل والتهجد", fastingTitle = "صيام التطوع", occasionsTitle = "المناسبات الدينية",
        backLabel = "رجوع",
        prayers = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
    )
    SakinaLang.EN -> SakinaStrings(
        appName = "Sakina",
        tabHome = "Home", tabQibla = "Qibla", tabQuran = "Quran", tabAdhkar = "Adhkar", tabCalendar = "Calendar",
        nextPrayer = "Next Prayer", midnight = "Midnight", lastThird = "Last Third", todayLabel = "Today",
        qiblaTitle = "Qibla", qiblaTargetLabel = "Qibla", yourHeadingLabel = "Heading",
        onQibla = "Aligned with Qibla", turnPhone = "Turn phone", rightDir = "right", leftDir = "left",
        noSensor = "Magnetometer unavailable", kmFromMakkah = "km to Makkah",
        stableField = "Stable Field", magInterference = "Magnetic interference · Move in ∞",
        accHigh = "Accuracy: High", accMed = "Accuracy: Medium", accLow = "Accuracy: Calibrate ∞",
        cardinals = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW"),
        quranTitle = "Noble Quran", riwayaHafs = "Hafs 'an 'Asim", riwayaWarsh = "Warsh 'an Nafi'",
        surahsBtn = "Surahs", searchSurahHint = "Search Surah name or number...",
        lastReadLabel = "Last Read", riwayaLabel = "Narration", resumeLabel = "Resume",
        ayahWord = "Ayahs", makkiWord = "Makki", madaniWord = "Madani",
        adhkarTitle = "Adhkar & Tasbih", tasbihPrompt = "Tap to count",
        adhkarCatTitles = listOf("Morning Adhkar", "Evening Adhkar", "After Prayer Adhkar", "Sleep Adhkar", "Entering & Leaving Mosque", "Entering & Leaving Home"),
        asmaTitle = "99 Names of Allah · Name of the Day",
        calendarTitle = "Hijri Calendar",
        weekdays = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri"),
        hijriMonths = listOf("Muharram", "Safar", "Rabi' I", "Rabi' II", "Jumada I", "Jumada II", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"),
        dayPrayersCardTitle = "Prayer Times for This Day", dailyHadithTitle = "Hadith of the Day",
        settingsTitle = "General Settings", themeTitle = "Luxury Themes (5)",
        themeNames = listOf("Royal Navy", "Sacred Emerald", "Midnight AMOLED", "Andalusian Burgundy", "Makkan Amber"),
        activeWord = "Active",
        languageLabel = "Language", locationLabel = "Location", autoGps = "Auto (GPS)", manualPick = "Manual City",
        hijriAdjLabel = "Hijri Date Adjustment", calcMethod = "Calculation Method",
        alarmTitle = "Alarms & Alerts", suhoorTitle = "Suhoor & Tahajjud", minsBeforeFajr = "mins before Fajr",
        minuteShort = "m", volumeLabel = "Adhan & Alarm Volume", fajrFadeLabel = "Gentle Fajr Fade-In",
        defaultSoundLabel = "Default Sound Mode", adhanVoiceLabel = "Adhan Voice", soundModes = listOf("Adhan", "Takbir", "Vibrate", "Silent"),
        testAlarmBtn = "Test Alarm Now (in 10s)", testAlarmScheduled = "Test alarm scheduled in 10s",
        duasTitle = "Duas", duasMainTab = "Prophetic Duas", hisnMuslimTab = "Hisn al-Muslim",
        duaSectionsBtn = "Categories",
        duaCatTitles = listOf("Comprehensive Quranic Duas", "Authentic Prophetic Duas", "Relief from Distress & Debt", "Duas for Parents & Family", "Travel Supplication (Safar)"),
        duaCatSubs = listOf("Rabbana atina fid-dunya hasanah", "Authentic supplications from Sahihayn", "Prophetic prayers for anxiety and debt", "My Lord, have mercy upon them", "Authentic supplication when embarking on a journey"),
        ruqyahTitle = "Islamic Ruqyah", ruqyahTypesBtn = "Categories",
        ruqyahCatTitles = listOf("Comprehensive Ruqyah", "Ruqyah for Tranquility", "Protection from Evil Eye", "Ruqyah for Healing"),
        ruqyahCatSubs = listOf("Complete shield from Quran & Sunnah", "For peace of heart & restful sleep", "Prophetic protection from envy & harm", "Recited while placing hand on pain"),
        sunanTitle = "Sunan Rawatib (12 Rak'ahs)", duhaTitle = "Duha Prayer",
        qiyamTitle = "Qiyam al-Layl", fastingTitle = "Voluntary Fasting", occasionsTitle = "Islamic Occasions",
        backLabel = "Back",
        prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    )
    SakinaLang.FR -> SakinaStrings(
        appName = "Sakina",
        tabHome = "Accueil", tabQibla = "Qibla", tabQuran = "Coran", tabAdhkar = "Adhkar", tabCalendar = "Calendrier",
        nextPrayer = "Prochaine Prière", midnight = "Minuit", lastThird = "Dernier Tiers", todayLabel = "Aujourd'hui",
        qiblaTitle = "Qibla", qiblaTargetLabel = "Qibla", yourHeadingLabel = "Cap",
        onQibla = "Aligné avec la Qibla", turnPhone = "Tournez", rightDir = "à droite", leftDir = "à gauche",
        noSensor = "Capteur magnétique indisponible", kmFromMakkah = "km de La Mecque",
        stableField = "Champ Stable", magInterference = "Interférence · Mouvement ∞",
        accHigh = "Précision : Élevée", accMed = "Précision : Moyenne", accLow = "Précision : Calibrer ∞",
        cardinals = listOf("N", "NE", "E", "SE", "S", "SO", "O", "NO"),
        quranTitle = "Saint Coran", riwayaHafs = "Hafs d'après 'Asim", riwayaWarsh = "Warsh d'après Nafi'",
        surahsBtn = "Sourates", searchSurahHint = "Rechercher une sourate...",
        lastReadLabel = "Dernière lecture", riwayaLabel = "Lecture", resumeLabel = "Reprendre",
        ayahWord = "Versets", makkiWord = "Mecquoise", madaniWord = "Médinoise",
        adhkarTitle = "Adhkar & Tasbih", tasbihPrompt = "Toucher",
        adhkarCatTitles = listOf("Adhkar du Matin", "Adhkar du Soir", "Après la Prière", "Adhkar du Sommeil", "Entrée & Sortie de la Mosquée", "Entrée & Sortie de la Maison"),
        asmaTitle = "99 Noms d'Allah · Nom du Jour",
        calendarTitle = "Calendrier Hégirien",
        weekdays = listOf("Sam", "Dim", "Lun", "Mar", "Mer", "Jeu", "Ven"),
        hijriMonths = listOf("Mouharram", "Safar", "Rabi' I", "Rabi' II", "Joumada I", "Joumada II", "Rajab", "Cha'ban", "Ramadan", "Chawwal", "Dhou al-Qi'da", "Dhou al-Hijja"),
        dayPrayersCardTitle = "Horaires des prières de ce jour", dailyHadithTitle = "Hadith du Jour",
        settingsTitle = "Paramètres Généraux", themeTitle = "Thèmes de Luxe (5)",
        themeNames = listOf("Bleu Royal", "Émeraude Sacré", "Noir AMOLED", "Bordeaux Andalou", "Ambre Mecquois"),
        activeWord = "Actif",
        languageLabel = "Langue", locationLabel = "Position", autoGps = "Auto (GPS)", manualPick = "Choisir Ville",
        hijriAdjLabel = "Ajustement de la date hégirienne", calcMethod = "Méthode de Calcul",
        alarmTitle = "Alarmes & Alertes", suhoorTitle = "Alarme Souhour", minsBeforeFajr = "min avant Fajr",
        minuteShort = "m", volumeLabel = "Volume de l'Adhan", fajrFadeLabel = "Fondu doux pour Fajr",
        defaultSoundLabel = "Mode Sonore", adhanVoiceLabel = "Voix du Muezzin", soundModes = listOf("Adhan", "Takbir", "Vibreur", "Silencieux"),
        testAlarmBtn = "Tester l'alarme (dans 10s)", testAlarmScheduled = "Alarme programmée dans 10s",
        duasTitle = "Invocations", duasMainTab = "Invocations", hisnMuslimTab = "Citadelle du Musulman",
        duaSectionsBtn = "Catégories",
        duaCatTitles = listOf("Invocations Coraniques", "Invocations Prophétiques", "Soulagement des Soucis & Dettes", "Pour les Parents & la Famille", "Invocation du Voyage"),
        duaCatSubs = listOf("Seigneur, accorde-nous belle part ici-bas", "Invocations authentiques des Sahihayn", "Prières pour dissiper l'angoisse", "Seigneur, fais-leur miséricorde", "Gloire à Celui qui nous a soumis cela"),
        ruqyahTitle = "Roqya Légitime", ruqyahTypesBtn = "Catégories",
        ruqyahCatTitles = listOf("Roqya Complète", "Roqya d'Apaisement", "Protection Mauvais Œil", "Roqya de Guérison"),
        ruqyahCatSubs = listOf("Protection par le Coran et la Sunna", "Pour la sérénité du cœur", "Contre l'envie et le mauvais œil", "En posant la main sur la douleur"),
        sunanTitle = "Sunan Rawatib (12 Rak'ahs)", duhaTitle = "Prière de Duha",
        qiyamTitle = "Qiyam al-Layl", fastingTitle = "Jeûne Surérogatoire", occasionsTitle = "Événements Islamiques",
        backLabel = "Retour",
        prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    )
    SakinaLang.TR -> SakinaStrings(
        appName = "Sakina",
        tabHome = "Ana Sayfa", tabQibla = "Kıble", tabQuran = "Kur'an", tabAdhkar = "Zikir", tabCalendar = "Takvim",
        nextPrayer = "Sonraki Vakit", midnight = "Gece Yarısı", lastThird = "Son Üçte Bir", todayLabel = "Bugün",
        qiblaTitle = "Kıble", qiblaTargetLabel = "Kıble", yourHeadingLabel = "Yön",
        onQibla = "Kıble Yönündesiniz", turnPhone = "Telefonu çevirin", rightDir = "sağa", leftDir = "sola",
        noSensor = "Manyetik sensör bulunamadı", kmFromMakkah = "km Mekke'ye uzaklık",
        stableField = "Kararlı Alan", magInterference = "Manyetik Parazit · ∞ Çizin",
        accHigh = "Doğruluk: Yüksek", accMed = "Doğruluk: Orta", accLow = "Doğruluk: Kalibre Edin",
        cardinals = listOf("K", "KD", "D", "GD", "G", "GB", "B", "KB"),
        quranTitle = "Kur'ân-ı Kerîm", riwayaHafs = "Hafs Rivayeti", riwayaWarsh = "Verş Rivayeti",
        surahsBtn = "Sureler", searchSurahHint = "Sure adı veya numarası ara...",
        lastReadLabel = "Son Okunan", riwayaLabel = "Rivayet", resumeLabel = "Devam",
        ayahWord = "Ayet", makkiWord = "Mekkî", madaniWord = "Medenî",
        adhkarTitle = "Zikir & Tesbih", tasbihPrompt = "Zikir için dokun",
        adhkarCatTitles = listOf("Sabah Zikirleri", "Akşam Zikirleri", "Namaz Tesbihatı", "Uyku Zikirleri", "Camiye Giriş ve Çıkış", "Eve Giriş ve Çıkış"),
        asmaTitle = "Esmaü'l-Hüsna · Günün İsmi",
        calendarTitle = "Hicri Takvim",
        weekdays = listOf("Cmt", "Paz", "Pzt", "Sal", "Çar", "Per", "Cum"),
        hijriMonths = listOf("Muharrem", "Safer", "Rebiülevvel", "Rebiülahir", "Cemaziyelevvel", "Cemaziyelahir", "Recep", "Şaban", "Ramazan", "Şevval", "Zilkade", "Zilhicce"),
        dayPrayersCardTitle = "Bu Günün Namaz Vakitleri", dailyHadithTitle = "Günün Hadisi",
        settingsTitle = "Genel Ayarlar", themeTitle = "Lüks Temalar (5)",
        themeNames = listOf("Kraliyet Laciverti", "Harem Zümrüdü", "Gece AMOLED", "Endülüs Bordosu", "Mekke Kehribarı"),
        activeWord = "Aktif",
        languageLabel = "Dil", locationLabel = "Konum", autoGps = "Otomatik (GPS)", manualPick = "Şehir Seç",
        hijriAdjLabel = "Hicri Tarih Ayarı", calcMethod = "Hesaplama Yöntemi",
        alarmTitle = "Alarmlar & Bildirimler", suhoorTitle = "Sahur & Teheccüd", minsBeforeFajr = "dk İmsaktan önce",
        minuteShort = "dk", volumeLabel = "Ezan Ses Seviyesi", fajrFadeLabel = "Sabah Yumuşak Başlangıç",
        defaultSoundLabel = "Varsayılan Ses Modu", adhanVoiceLabel = "Ezan Sesi", soundModes = listOf("Ezan", "Tekbir", "Titreşim", "Sessiz"),
        testAlarmBtn = "Alarmı Şimdi Test Et (10 sn)", testAlarmScheduled = "Test alarmı 10 sn sonraya kuruldu",
        duasTitle = "Dualar", duasMainTab = "Seçme Dualar", hisnMuslimTab = "Hısnü'l-Müslim",
        duaSectionsBtn = "Kategoriler",
        duaCatTitles = listOf("Kur'an-ı Kerim Duaları", "Peygamber Efendimizin Duaları", "Sıkıntı ve Borç Duaları", "Anne, Baba ve Aile Duaları", "Yolculuk (Sefer) Duası"),
        duaCatSubs = listOf("Rabbena atina fid-dünya haseneh", "Sahih hadislerdeki kapsamlı dualar", "Hüzün ve borçtan kurtuluş duaları", "Rabbim! Onlara merhamet eyle", "Bunu hizmetimize veren Allah'ı tesbih ederiz"),
        ruqyahTitle = "Şer'î Rukye", ruqyahTypesBtn = "Kategoriler",
        ruqyahCatTitles = listOf("Kapsamlı Şer'î Rukye", "Huzur ve Sekinet Rukyesi", "Nazar ve Hasetten Korunma", "Şifa Rukyesi"),
        ruqyahCatSubs = listOf("Kur'an ve Sünnetten tam koruma", "Kalp huzuru ve rahat uyku için", "Nazar ve kötülüklerden korunma", "Ağrıyan yere el konularak okunur"),
        sunanTitle = "Revâtib Sünnetler (12 Rekât)", duhaTitle = "Kuşluk (Duha) Namazı",
        qiyamTitle = "Gece Namazı (Teheccüd)", fastingTitle = "Nafile Oruçlar", occasionsTitle = "Dini Günler",
        backLabel = "Geri",
        prayers = listOf("İmsak", "Öğle", "İkindi", "Akşam", "Yatsı")
    )
    SakinaLang.ID -> SakinaStrings(
        appName = "Sakina",
        tabHome = "Beranda", tabQibla = "Kiblat", tabQuran = "Al-Qur'an", tabAdhkar = "Dzikir", tabCalendar = "Kalender",
        nextPrayer = "Shalat Berikutnya", midnight = "Tengah Malam", lastThird = "Sepertiga Akhir", todayLabel = "Hari Ini",
        qiblaTitle = "Kiblat", qiblaTargetLabel = "Kiblat", yourHeadingLabel = "Arah",
        onQibla = "Tepat Menghadap Kiblat", turnPhone = "Putar ponsel", rightDir = "ke kanan", leftDir = "ke kiri",
        noSensor = "Sensor kompas tidak tersedia", kmFromMakkah = "km dari Makkah",
        stableField = "Medan Stabil", magInterference = "Gangguan Magnetik · Gerakkan ∞",
        accHigh = "Akurasi: Tinggi", accMed = "Akurasi: Sedang", accLow = "Akurasi: Kalibrasi ∞",
        cardinals = listOf("U", "TL", "T", "TG", "S", "BD", "B", "BL"),
        quranTitle = "Al-Qur'an Al-Karim", riwayaHafs = "Riwayat Hafs", riwayaWarsh = "Riwayat Warsy",
        surahsBtn = "Surah", searchSurahHint = "Cari nama atau nomor surah...",
        lastReadLabel = "Terakhir Dibaca", riwayaLabel = "Riwayat", resumeLabel = "Lanjut",
        ayahWord = "Ayat", makkiWord = "Makkiyah", madaniWord = "Madaniyah",
        adhkarTitle = "Dzikir & Tasbih", tasbihPrompt = "Ketuk",
        adhkarCatTitles = listOf("Dzikir Pagi", "Dzikir Petang", "Dzikir Setelah Shalat", "Dzikir Sebelum Tidur", "Dzikir Masuk & Keluar Masjid", "Dzikir Masuk & Keluar Rumah"),
        asmaTitle = "Asmaul Husna · Nama Hari Ini",
        calendarTitle = "Kalender Hijriah",
        weekdays = listOf("Sab", "Min", "Sen", "Sel", "Rab", "Kam", "Jum"),
        hijriMonths = listOf("Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban", "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"),
        dayPrayersCardTitle = "Jadwal Shalat Hari Ini", dailyHadithTitle = "Hadits Hari Ini",
        settingsTitle = "Pengaturan Umum", themeTitle = "Tema Mewah (5)",
        themeNames = listOf("Biru Kerajaan", "Zamrud Haramain", "Hitam AMOLED", "Burgundi Andalusia", "Amber Makkah"),
        activeWord = "Aktif",
        languageLabel = "Bahasa", locationLabel = "Lokasi", autoGps = "Otomatis (GPS)", manualPick = "Pilih Kota",
        hijriAdjLabel = "Penyesuaian Tanggal Hijriah", calcMethod = "Metode Perhitungan",
        alarmTitle = "Alarm & Pengingat", suhoorTitle = "Alarm Sahur & Tahajud", minsBeforeFajr = "mnt sebelum Subuh",
        minuteShort = "m", volumeLabel = "Volume Adzan", fajrFadeLabel = "Suara Subuh Bertahap",
        defaultSoundLabel = "Mode Suara", adhanVoiceLabel = "Suara Muadzin", soundModes = listOf("Adzan", "Takbir", "Getar", "Senyap"),
        testAlarmBtn = "Uji Alarm Sekarang (10 dtk)", testAlarmScheduled = "Alarm uji dijadwalkan dalam 10 dtk",
        duasTitle = "Doa-Doa", duasMainTab = "Doa Pilihan", hisnMuslimTab = "Hisnul Muslim",
        duaSectionsBtn = "Kategori",
        duaCatTitles = listOf("Doa-Doa Al-Qur'an", "Doa-Doa Nabi ﷺ", "Pelipur Gundah & Pelunas Hutang", "Doa untuk Orang Tua & Keluarga", "Doa Perjalanan (Safar)"),
        duaCatSubs = listOf("Rabbana atina fid-dunya hasanah", "Doa shahih dari Bukhari & Muslim", "Doa pelepas kesempitan dan hutang", "Ya Rabb, sayangilah keduanya", "Maha Suci Allah yang menundukkan kendaraan ini"),
        ruqyahTitle = "Ruqyah Syar'iyyah", ruqyahTypesBtn = "Kategori",
        ruqyahCatTitles = listOf("Ruqyah Syar'iyyah Lengkap", "Ruqyah Ketenangan Hati", "Perlindungan 'Ain & Hasad", "Ruqyah Penyembuhan"),
        ruqyahCatSubs = listOf("Benteng lengkap dari Al-Qur'an & Sunnah", "Untuk ketenteraman hati & tidur", "Perlindungan dari dengki & 'ain", "Dibaca sambil memegang bagian yang sakit"),
        sunanTitle = "Sunnah Rawatib (12 Rakaat)", duhaTitle = "Shalat Dhuha",
        qiyamTitle = "Qiyamul Lail", fastingTitle = "Puasa Sunnah", occasionsTitle = "Hari Besar Islam",
        backLabel = "Kembali",
        prayers = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
    )
    SakinaLang.UR -> SakinaStrings(
        appName = "سکینہ",
        tabHome = "اوقات", tabQibla = "قبلہ", tabQuran = "قرآن", tabAdhkar = "اذکار", tabCalendar = "تقویم",
        nextPrayer = "اگلی نماز", midnight = "نصف شب", lastThird = "آخری تہائی", todayLabel = "آج",
        qiblaTitle = "سمت قبلہ", qiblaTargetLabel = "قبلہ", yourHeadingLabel = "آپ کا رخ",
        onQibla = "آپ عین قبلہ رخ پر ہیں", turnPhone = "فون موڑیں", rightDir = "دائیں", leftDir = "بائیں",
        noSensor = "مقناطیسی سینسر دستیاب نہیں", kmFromMakkah = "کلومیٹر مکہ مکرمہ سے",
        stableField = "مستحکم مقناطیسی میدان", magInterference = "مقناطیسی مداخلت · فون کو ∞ گھمائیں",
        accHigh = "درستگی: بہترین", accMed = "درستگی: درمیانی", accLow = "درستگی: کیلیبریٹ کریں",
        cardinals = listOf("شمال", "ش.مشرق", "مشرق", "ج.مشرق", "جنوب", "ج.مغرب", "مغرب", "ش.مغرب"),
        quranTitle = "قرآن کریم", riwayaHafs = "حفص عن عاصم", riwayaWarsh = "ورش عن نافع",
        surahsBtn = "سورتیں", searchSurahHint = "سورت کا نام یا نمبر تلاش کریں...",
        lastReadLabel = "آخری تلاوت", riwayaLabel = "روایت", resumeLabel = "جاری رکھیں",
        ayahWord = "آیات", makkiWord = "مکی", madaniWord = "مدنی",
        adhkarTitle = "اذکار و تسبیح", tasbihPrompt = "تسبیح کے لیے چھوئیں",
        adhkarCatTitles = listOf("صبح کے اذکار", "شام کے اذکار", "نماز کے بعد کے اذکار", "سونے کے اذکار", "مسجد میں داخل ہونے اور نکلنے کی دعائیں", "گھر میں داخل ہونے اور نکلنے کی دعائیں"),
        asmaTitle = "اسمائے حسنیٰ · آج کا اسم",
        calendarTitle = "ہجری تقویم",
        weekdays = listOf("ہفتہ", "اتوار", "پیر", "منگل", "بدھ", "جمعرات", "جمعہ"),
        hijriMonths = listOf("محرم", "صفر", "ربیع الاول", "ربیع الثانی", "جمادی الاولیٰ", "جمادی الاخریٰ", "رجب", "شعبان", "رمضان", "شوال", "ذوالقعدہ", "ذوالحجہ"),
        dayPrayersCardTitle = "اس دن کی نمازوں کے اوقات", dailyHadithTitle = "آج کی حدیث",
        settingsTitle = "عمومی ترتیبات", themeTitle = "شاہی تھیمز (5)",
        themeNames = listOf("شاہی نیلا", "زمرد حرمی", "سیاہ AMOLED", "اندلسی عنابی", "مکی عنبر"),
        activeWord = "فعال",
        languageLabel = "زبان / Language", locationLabel = "مقام", autoGps = "خودکار (GPS)", manualPick = "شہر منتخب کریں",
        hijriAdjLabel = "ہجری تاریخ کی تصحیح", calcMethod = "طریقہ حساب",
        alarmTitle = "الارم اور اطلاعات", suhoorTitle = "سحری اور تہجد الارم", minsBeforeFajr = "منٹ فجر سے پہلے",
        minuteShort = "م", volumeLabel = "اذان کی آواز", fajrFadeLabel = "فجر کے لیے دھیمی بتدریج آواز",
        defaultSoundLabel = "پہلے سے طے شدہ آواز", adhanVoiceLabel = "مؤذن کی آواز", soundModes = listOf("مکمل اذان", "تکبیر", "تھرتھراہٹ", "خاموش"),
        testAlarmBtn = "الارم کا تجربہ کریں (10 سیکنڈ بعد)", testAlarmScheduled = "تجرباتی الارم 10 سیکنڈ بعد سیٹ ہو گیا",
        duasTitle = "دعائیں", duasMainTab = "ماثور دعائیں", hisnMuslimTab = "حصن المسلم",
        duaSectionsBtn = "اقسام",
        duaCatTitles = listOf("جامع قرآنی دعائیں", "مسنون نبوی دعائیں", "پریشانی اور قرض سے نجات کی دعائیں", "والدین اور اہل خانہ کی دعائیں", "سفر کی دعا"),
        duaCatSubs = listOf("ربنا آتنا في الدنيا حسنة", "صحیحین کی جامع مسنون دعائیں", "غم اور قرض کی ادائیگی کی دعائیں", "رب ارحمهما كما ربياني صغيراً", "سبحان الذي سخر لنا هذا"),
        ruqyahTitle = "شرعی رقیہ", ruqyahTypesBtn = "اقسام",
        ruqyahCatTitles = listOf("مکمل شرعی رقیہ", "سکونِ قلب کا رقیہ", "نظرِ بد اور حسد سے حفاظت", "رقية الشفاء"),
        ruqyahCatSubs = listOf("قرآن و سنت سے مکمل حفاظت", "دل کے اطمینان اور پر سکون نیند کے لیے", "نظر بد اور حسد سے نبوی حفاظت", "درد کی جگہ پر ہاتھ رکھ کر پڑھیں"),
        sunanTitle = "سنن رواتب (12 رکعات)", duhaTitle = "نمازِ چاشت (ضحیٰ)",
        qiyamTitle = "قیام اللیل و تہجد", fastingTitle = "نفلی روزے", occasionsTitle = "اسلامی مواقع",
        backLabel = "واپس",
        prayers = listOf("فجر", "ظہر", "عصر", "مغرب", "عشاء")
    )
}


fun sakinaTr(
    lang: SakinaLang,
    ar: String,
    en: String,
    fr: String,
    tr: String = en,
    id: String = en,
    ur: String = ar
): String = when (lang) {
    SakinaLang.AR -> ar
    SakinaLang.EN -> en
    SakinaLang.FR -> fr
    SakinaLang.TR -> tr
    SakinaLang.ID -> id
    SakinaLang.UR -> ur
}
