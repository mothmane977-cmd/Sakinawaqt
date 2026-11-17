package app.sakina

import androidx.compose.runtime.compositionLocalOf

enum class SakinaLang(
    val code: String,
    val flag: String,
    val nativeName: String,
    val isRtl: Boolean
) {
    AR("ar", "🇩🇿", "العربية", true),
    EN("en", "🇬🇧", "English", false),
    FR("fr", "🇫🇷", "Français", false),
    TR("tr", "🇹🇷", "Türkçe", false),
    ID("id", "🇮🇩", "Indonesia", false),
    UR("ur", "🇵🇰", "اردو", true)
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
    val qiblaTitle: String,
    val quranTitle: String,
    val adhkarTitle: String,
    val calendarTitle: String,
    val settingsTitle: String,
    val alarmTitle: String,
    val duasTitle: String,
    val ruqyahTitle: String,
    val themeTitle: String,
    val languageLabel: String,
    val locationLabel: String,
    val autoGps: String,
    val manualPick: String,
    val calcMethod: String,
    val prayers: List<String>
)

val LocalSakinaStrings = compositionLocalOf { stringsFor(SakinaLang.AR) }
val LocalSakinaLang = compositionLocalOf { SakinaLang.AR }

fun stringsFor(lang: SakinaLang): SakinaStrings = when (lang) {
    SakinaLang.AR -> SakinaStrings(
        appName = "سَكِينَة", tabHome = "الرئيسية", tabQibla = "القبلة", tabQuran = "القرآن",
        tabAdhkar = "الأذكار", tabCalendar = "التقويم", nextPrayer = "الصلاة القادمة",
        midnight = "منتصف الليل", lastThird = "الثلث الأخير", qiblaTitle = "القبلة",
        quranTitle = "القرآن الكريم", adhkarTitle = "الأذكار والمسبحة", calendarTitle = "التقويم الهجري",
        settingsTitle = "الإعدادات", alarmTitle = "المنبه والتنبيهات", duasTitle = "الأدعية المصنفة",
        ruqyahTitle = "الرقية الشرعية", themeTitle = "اختر الثيم الفاخر (5 ثيمات)",
        languageLabel = "لغة التطبيق", locationLabel = "الموقع", autoGps = "تلقائي (GPS)",
        manualPick = "اختيار يدوي", calcMethod = "طريقة الحساب",
        prayers = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
    )
    SakinaLang.EN -> SakinaStrings(
        appName = "Sakina", tabHome = "Home", tabQibla = "Qibla", tabQuran = "Quran",
        tabAdhkar = "Adhkar", tabCalendar = "Calendar", nextPrayer = "Next Prayer",
        midnight = "Midnight", lastThird = "Last Third", qiblaTitle = "Qibla",
        quranTitle = "Noble Quran", adhkarTitle = "Adhkar & Tasbih", calendarTitle = "Hijri Calendar",
        settingsTitle = "Settings", alarmTitle = "Alarms & Alerts", duasTitle = "Duas",
        ruqyahTitle = "Ruqyah", themeTitle = "Luxury Themes (5)",
        languageLabel = "App Language", locationLabel = "Location", autoGps = "Auto (GPS)",
        manualPick = "Manual City", calcMethod = "Calculation Method",
        prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    )
    SakinaLang.FR -> SakinaStrings(
        appName = "Sakina", tabHome = "Accueil", tabQibla = "Qibla", tabQuran = "Coran",
        tabAdhkar = "Adhkar", tabCalendar = "Calendrier", nextPrayer = "Prochaine prière",
        midnight = "Minuit", lastThird = "Dernier tiers", qiblaTitle = "Qibla",
        quranTitle = "Saint Coran", adhkarTitle = "Adhkar & Tasbih", calendarTitle = "Calendrier Hégirien",
        settingsTitle = "Paramètres", alarmTitle = "Alarmes", duasTitle = "Invocations",
        ruqyahTitle = "Roqya", themeTitle = "Thèmes de luxe (5)",
        languageLabel = "Langue", locationLabel = "Position", autoGps = "Auto (GPS)",
        manualPick = "Manuel", calcMethod = "Méthode de calcul",
        prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    )
    SakinaLang.TR -> SakinaStrings(
        appName = "Sekîne", tabHome = "Ana Sayfa", tabQibla = "Kıble", tabQuran = "Kur'an",
        tabAdhkar = "Zikir", tabCalendar = "Takvim", nextPrayer = "Sonraki Vakit",
        midnight = "Gece Yarısı", lastThird = "Son Üçte Bir", qiblaTitle = "Kıble",
        quranTitle = "Kur'an-ı Kerim", adhkarTitle = "Zikir & Tesbih", calendarTitle = "Hicri Takvim",
        settingsTitle = "Ayarlar", alarmTitle = "Alarmlar", duasTitle = "Dualar",
        ruqyahTitle = "Rukye", themeTitle = "Lüks Temalar (5)",
        languageLabel = "Uygulama Dili", locationLabel = "Konum", autoGps = "Otomatik",
        manualPick = "Manuel", calcMethod = "Hesaplama Yöntemi",
        prayers = listOf("İmsak", "Öğle", "İkindi", "Akşam", "Yatsı")
    )
    SakinaLang.ID -> SakinaStrings(
        appName = "Sakina", tabHome = "Beranda", tabQibla = "Kiblat", tabQuran = "Al-Qur'an",
        tabAdhkar = "Dzikir", tabCalendar = "Kalender", nextPrayer = "Shalat Berikutnya",
        midnight = "Tengah Malam", lastThird = "Sepertiga Akhir", qiblaTitle = "Kiblat",
        quranTitle = "Al-Qur'an", adhkarTitle = "Dzikir & Tasbih", calendarTitle = "Kalender Hijriah",
        settingsTitle = "Pengaturan", alarmTitle = "Alarm", duasTitle = "Doa-Doa",
        ruqyahTitle = "Ruqyah", themeTitle = "Tema Mewah (5)",
        languageLabel = "Bahasa", locationLabel = "Lokasi", autoGps = "Otomatis",
        manualPick = "Manual", calcMethod = "Metode Hisab",
        prayers = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
    )
    SakinaLang.UR -> SakinaStrings(
        appName = "سَكِينَة", tabHome = "صفحہ اول", tabQibla = "قبلہ", tabQuran = "قرآن",
        tabAdhkar = "اذکار", tabCalendar = "تقویم", nextPrayer = "اگلی نماز",
        midnight = "نصف شب", lastThird = "آخری تہائی", qiblaTitle = "قبلہ",
        quranTitle = "قرآن کریم", adhkarTitle = "اذکار و تسبیح", calendarTitle = "ہجری تقویم",
        settingsTitle = "ترتیبات", alarmTitle = "الارم", duasTitle = "دعائیں",
        ruqyahTitle = "رقیہ شرعیہ", themeTitle = "تھیمز (5)",
        languageLabel = "زبان", locationLabel = "مقام", autoGps = "خودکار (GPS)",
        manualPick = "دستی انتخاب", calcMethod = "طریقہ حساب",
        prayers = listOf("فجر", "ظہر", "عصر", "مغرب", "عشاء")
    )
}
