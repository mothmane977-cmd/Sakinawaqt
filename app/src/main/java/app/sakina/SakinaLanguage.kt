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
    val todayLabel: String,
    val qiblaTitle: String,
    val quranTitle: String,
    val adhkarTitle: String,
    val calendarTitle: String,
    val settingsTitle: String,
    val alarmTitle: String,
    val duasTitle: String,
    val ruqyahTitle: String,
    val sunanTitle: String,
    val duhaTitle: String,
    val qiyamTitle: String,
    val fastingTitle: String,
    val themeTitle: String,
    val languageLabel: String,
    val locationLabel: String,
    val autoGps: String,
    val manualPick: String,
    val calcMethod: String,
    val dayPrayersCardTitle: String,
    val dailyHadithTitle: String,
    val searchSurahHint: String,
    val lastReadLabel: String,
    val resumeLabel: String,
    val tasbihPrompt: String,
    val adhkarCatTitles: List<String>,
    val prayers: List<String>
)

val LocalSakinaStrings = compositionLocalOf { stringsFor(SakinaLang.AR) }
val LocalSakinaLang = compositionLocalOf { SakinaLang.AR }

fun stringsFor(lang: SakinaLang): SakinaStrings = when (lang) {
    SakinaLang.AR -> SakinaStrings(
        appName = "سَكِينَة", tabHome = "الرئيسية", tabQibla = "القبلة", tabQuran = "القرآن",
        tabAdhkar = "الأذكار", tabCalendar = "التقويم", nextPrayer = "الصلاة القادمة",
        midnight = "منتصف الليل", lastThird = "الثلث الأخير", todayLabel = "اليوم",
        qiblaTitle = "اتجاه القبلة", quranTitle = "القرآن الكريم", adhkarTitle = "الأذكار والمسبحة",
        calendarTitle = "التقويم الهجري", settingsTitle = "الإعدادات العامة", alarmTitle = "المنبه والتنبيهات",
        duasTitle = "الأدعية المصنفة", ruqyahTitle = "الرقية الشرعية", sunanTitle = "السنن الرواتب",
        duhaTitle = "صلاة الضحى", qiyamTitle = "قيام الليل", fastingTitle = "صيام التطوع والمناسبات",
        themeTitle = "اختر الثيم الفاخر (5 ثيمات)", languageLabel = "لغة التطبيق", locationLabel = "الموقع",
        autoGps = "تلقائي (GPS)", manualPick = "اختيار يدوي", calcMethod = "طريقة الحساب",
        dayPrayersCardTitle = "توقيت صلوات هذا اليوم", dailyHadithTitle = "حديث اليوم والمناسبة",
        searchSurahHint = "ابحث باسم السورة أو رقمها...", lastReadLabel = "آخر قراءة", resumeLabel = "متابعة",
        tasbihPrompt = "ورد التسبيح (اضغط للعد)",
        adhkarCatTitles = listOf("أذكار الصباح", "أذكار المساء", "أذكار الدخول والخروج من المسجد", "أذكار الدخول والخروج من البيت", "بعد الصلاة", "أذكار النوم"),
        prayers = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
    )
    SakinaLang.EN -> SakinaStrings(
        appName = "Sakina", tabHome = "Home", tabQibla = "Qibla", tabQuran = "Quran",
        tabAdhkar = "Adhkar", tabCalendar = "Calendar", nextPrayer = "Next Prayer",
        midnight = "Midnight", lastThird = "Last Third", todayLabel = "Today",
        qiblaTitle = "Qibla Direction", quranTitle = "Noble Quran", adhkarTitle = "Adhkar & Tasbih",
        calendarTitle = "Hijri Calendar", settingsTitle = "Settings", alarmTitle = "Alarms & Alerts",
        duasTitle = "Categorized Duas", ruqyahTitle = "Islamic Ruqyah", sunanTitle = "Sunan Rawatib",
        duhaTitle = "Duha Prayer", qiyamTitle = "Qiyam al-Layl", fastingTitle = "Sunnah Fasting & Events",
        themeTitle = "Luxury Themes (5)", languageLabel = "App Language", locationLabel = "Location",
        autoGps = "Auto (GPS)", manualPick = "Manual City", calcMethod = "Calculation Method",
        dayPrayersCardTitle = "Prayer Times for This Day", dailyHadithTitle = "Hadith of the Day",
        searchSurahHint = "Search Surah name or number...", lastReadLabel = "Last Read", resumeLabel = "Resume",
        tasbihPrompt = "Daily Tasbih (Tap to count)",
        adhkarCatTitles = listOf("Morning Adhkar", "Evening Adhkar", "Entering & Leaving Mosque", "Entering & Leaving Home", "After Prayer", "Sleeping Adhkar"),
        prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    )
    SakinaLang.FR -> SakinaStrings(
        appName = "Sakina", tabHome = "Accueil", tabQibla = "Qibla", tabQuran = "Coran",
        tabAdhkar = "Adhkar", tabCalendar = "Calendrier", nextPrayer = "Prochaine prière",
        midnight = "Minuit", lastThird = "Dernier tiers", todayLabel = "Aujourd'hui",
        qiblaTitle = "Direction Qibla", quranTitle = "Saint Coran", adhkarTitle = "Adhkar & Tasbih",
        calendarTitle = "Calendrier Hégirien", settingsTitle = "Paramètres", alarmTitle = "Alarmes & Alertes",
        duasTitle = "Invocations (Duas)", ruqyahTitle = "Roqya Légitime", sunanTitle = "Sunan Rawatib",
        duhaTitle = "Prière de Duha", qiyamTitle = "Qiyam al-Layl", fastingTitle = "Jeûne & Événements",
        themeTitle = "Thèmes de luxe (5)", languageLabel = "Langue de l'application", locationLabel = "Position",
        autoGps = "Auto (GPS)", manualPick = "Manuel", calcMethod = "Méthode de calcul",
        dayPrayersCardTitle = "Horaires des prières de ce jour", dailyHadithTitle = "Hadith du Jour",
        searchSurahHint = "Rechercher une sourate...", lastReadLabel = "Dernière lecture", resumeLabel = "Continuer",
        tasbihPrompt = "Tasbih quotidien (Toucher)",
        adhkarCatTitles = listOf("Adhkar du Matin", "Adhkar du Soir", "Entrée & Sortie de la Mosquée", "Entrée & Sortie de la Maison", "Après la Prière", "Avant de Dormir"),
        prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    )
    SakinaLang.TR -> SakinaStrings(
        appName = "Sekîne", tabHome = "Ana Sayfa", tabQibla = "Kıble", tabQuran = "Kur'an",
        tabAdhkar = "Zikir", tabCalendar = "Takvim", nextPrayer = "Sonraki Vakit",
        midnight = "Gece Yarısı", lastThird = "Son Üçte Bir", todayLabel = "Bugün",
        qiblaTitle = "Kıble Yönü", quranTitle = "Kur'an-ı Kerim", adhkarTitle = "Zikir & Tesbih",
        calendarTitle = "Hicri Takvim", settingsTitle = "Ayarlar", alarmTitle = "Alarmlar",
        duasTitle = "Dualar", ruqyahTitle = "Şer'i Rukye", sunanTitle = "Revâtib Sünnetler",
        duhaTitle = "Kuşluk Namazı", qiyamTitle = "Teheccüd", fastingTitle = "Nafile Oruç & Günler",
        themeTitle = "Lüks Temalar (5)", languageLabel = "Uygulama Dili", locationLabel = "Konum",
        autoGps = "Otomatik (GPS)", manualPick = "Manuel", calcMethod = "Hesaplama Yöntemi",
        dayPrayersCardTitle = "Bu Günün Namaz Vakitleri", dailyHadithTitle = "Günün Hadisi",
        searchSurahHint = "Sure adı veya numarası ara...", lastReadLabel = "Son Okunan", resumeLabel = "Devam Et",
        tasbihPrompt = "Günlük Tesbih (Dokun)",
        adhkarCatTitles = listOf("Sabah Zikirleri", "Akşam Zikirleri", "Camiye Giriş ve Çıkış", "Eve Giriş ve Çıkış", "Namaz Sonrası", "Uyku Zikirleri"),
        prayers = listOf("İmsak", "Öğle", "İkindi", "Akşam", "Yatsı")
    )
    SakinaLang.ID -> SakinaStrings(
        appName = "Sakina", tabHome = "Beranda", tabQibla = "Kiblat", tabQuran = "Al-Qur'an",
        tabAdhkar = "Dzikir", tabCalendar = "Kalender", nextPrayer = "Shalat Berikutnya",
        midnight = "Tengah Malam", lastThird = "Sepertiga Akhir", todayLabel = "Hari Ini",
        qiblaTitle = "Arah Kiblat", quranTitle = "Al-Qur'an", adhkarTitle = "Dzikir & Tasbih",
        calendarTitle = "Kalender Hijriah", settingsTitle = "Pengaturan", alarmTitle = "Alarm & Peringatan",
        duasTitle = "Kumpulan Doa", ruqyahTitle = "Ruqyah Syar'iyyah", sunanTitle = "Sunnah Rawatib",
        duhaTitle = "Shalat Dhuha", qiyamTitle = "Qiyamul Lail", fastingTitle = "Puasa Sunnah & Hari Besar",
        themeTitle = "Tema Mewah (5)", languageLabel = "Bahasa Aplikasi", locationLabel = "Lokasi",
        autoGps = "Otomatis (GPS)", manualPick = "Manual", calcMethod = "Metode Hisab",
        dayPrayersCardTitle = "Waktu Shalat Hari Ini", dailyHadithTitle = "Hadits Hari Ini",
        searchSurahHint = "Cari nama atau nomor surah...", lastReadLabel = "Terakhir Dibaca", resumeLabel = "Lanjut",
        tasbihPrompt = "Tasbih Harian (Ketuk)",
        adhkarCatTitles = listOf("Dzikir Pagi", "Dzikir Petang", "Masuk & Keluar Masjid", "Masuk & Keluar Rumah", "Setelah Shalat", "Dzikir Tidur"),
        prayers = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
    )
    SakinaLang.UR -> SakinaStrings(
        appName = "سَكِينَة", tabHome = "صفحہ اول", tabQibla = "قبلہ", tabQuran = "قرآن",
        tabAdhkar = "اذکار", tabCalendar = "تقویم", nextPrayer = "اگلی نماز",
        midnight = "نصف شب", lastThird = "آخری تہائی", todayLabel = "آج",
        qiblaTitle = "سمت قبلہ", quranTitle = "قرآن کریم", adhkarTitle = "اذکار و تسبیح",
        calendarTitle = "ہجری تقویم", settingsTitle = "ترتیبات", alarmTitle = "الارم اور تنبیہات",
        duasTitle = "مسنون دعائیں", ruqyahTitle = "رقیہ شرعیہ", sunanTitle = "سنن رواتب",
        duhaTitle = "نماز چاشت", qiyamTitle = "قیام اللیل", fastingTitle = "نفلی روزے اور مواقع",
        themeTitle = "تھیمز (5)", languageLabel = "زبان", locationLabel = "مقام",
        autoGps = "خودکار (GPS)", manualPick = "دستی انتخاب", calcMethod = "طریقہ حساب",
        dayPrayersCardTitle = "آج کے اوقات نماز", dailyHadithTitle = "آج کی حدیث",
        searchSurahHint = "سورت کا نام یا نمبر تلاش کریں...", lastReadLabel = "آخری قراءت", resumeLabel = "جاری رکھیں",
        tasbihPrompt = "روزانہ تسبیح (گننے کے لیے دبائیں)",
        adhkarCatTitles = listOf("صبح کے اذکار", "شام کے اذکار", "مسجد میں داخل ہونے اور نکلنے کی دعا", "گھر میں داخل ہونے اور نکلنے کی دعا", "نماز کے بعد کے اذکار", "سونے کے اذکار"),
        prayers = listOf("فجر", "ظہر", "عصر", "مغرب", "عشاء")
    )
}
