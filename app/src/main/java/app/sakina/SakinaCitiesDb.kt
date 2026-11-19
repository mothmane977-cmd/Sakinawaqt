package app.sakina

data class SakinaCity(
    val nameAr: String,
    val nameLatin: String,
    val countryAr: String,
    val lat: Double,
    val lon: Double
)

object SakinaCitiesDb {
    val ALL_CITIES = listOf(
        // Algeria (Major Cities & Wilayas)
        SakinaCity("فرندة", "Frenda", "الجزائر", 35.06, 1.04),
        SakinaCity("تيارت", "Tiaret", "الجزائر", 35.37, 1.32),
        SakinaCity("الجزائر العاصمة", "Algiers", "الجزائر", 36.75, 3.05),
        SakinaCity("وهران", "Oran", "الجزائر", 35.70, -0.63),
        SakinaCity("قسنطينة", "Constantine", "الجزائر", 36.36, 6.61),
        SakinaCity("عنابة", "Annaba", "الجزائر", 36.90, 7.77),
        SakinaCity("سطيف", "Setif", "الجزائر", 36.19, 5.41),
        SakinaCity("تلمسان", "Tlemcen", "الجزائر", 34.88, -1.32),
        SakinaCity("بجاية", "Bejaia", "الجزائر", 36.75, 5.06),
        SakinaCity("بسكرة", "Biskra", "الجزائر", 34.85, 5.73),
        SakinaCity("باتنة", "Batna", "الجزائر", 35.56, 6.17),
        SakinaCity("سيدي بلعباس", "Sidi Bel Abbes", "الجزائر", 35.19, -0.63),
        SakinaCity("البليدة", "Blida", "الجزائر", 36.47, 2.83),
        SakinaCity("الشلف", "Chlef", "الجزائر", 36.16, 1.33),
        SakinaCity("تيزي وزو", "Tizi Ouzou", "الجزائر", 36.71, 4.05),
        SakinaCity("غرداية", "Ghardaia", "الجزائر", 32.49, 3.67),
        SakinaCity("ورقلة", "Ouargla", "الجزائر", 31.95, 5.32),
        SakinaCity("الوادي", "El Oued", "الجزائر", 33.37, 6.85),
        SakinaCity("بشار", "Bechar", "الجزائر", 31.62, -2.22),
        SakinaCity("تمنراست", "Tamanrasset", "الجزائر", 22.78, 5.52),
        SakinaCity("الجلفة", "Djelfa", "الجزائر", 34.67, 3.25),
        SakinaCity("المسيلة", "M'Sila", "الجزائر", 35.70, 4.54),
        SakinaCity("معسكر", "Mascara", "الجزائر", 35.40, 0.14),
        SakinaCity("سعيدة", "Saida", "الجزائر", 34.83, 0.15),
        SakinaCity("الأغواط", "Laghouat", "الجزائر", 33.80, 2.86),
        SakinaCity("سكيكدة", "Skikda", "الجزائر", 36.88, 6.91),
        SakinaCity("جيجل", "Jijel", "الجزائر", 36.82, 5.77),
        SakinaCity("مستغانم", "Mostaganem", "الجزائر", 35.93, 0.09),
        // Maghreb, Arab & World Cities
        SakinaCity("مكة المكرمة", "Makkah", "السعودية", 21.42, 39.82),
        SakinaCity("المدينة المنورة", "Madinah", "السعودية", 24.47, 39.61),
        SakinaCity("الرياض", "Riyadh", "السعودية", 24.71, 46.68),
        SakinaCity("القدس الشريف", "Al-Quds", "فلسطين", 31.78, 35.23),
        SakinaCity("تونس", "Tunis", "تونس", 36.81, 10.18),
        SakinaCity("الرباط", "Rabat", "المغرب", 34.02, -6.84),
        SakinaCity("الدار البيضاء", "Casablanca", "المغرب", 33.57, -7.59),
        SakinaCity("القاهرة", "Cairo", "مصر", 30.04, 31.24),
        SakinaCity("عمّان", "Amman", "الأردن", 31.95, 35.93),
        SakinaCity("الدوحة", "Doha", "قطر", 25.29, 51.53),
        SakinaCity("دبي", "Dubai", "الإمارات", 25.20, 55.27),
        SakinaCity("إسطنبول", "Istanbul", "تركيا", 41.01, 28.98),
        SakinaCity("باريس", "Paris", "فرنسا", 48.86, 2.35),
        SakinaCity("ليون", "Lyon", "فرنسا", 45.76, 4.84),
        SakinaCity("مارسيليا", "Marseille", "فرنسا", 43.30, 5.37),
        SakinaCity("لندن", "London", "بريطانيا", 51.51, -0.13),
        SakinaCity("جاكرتا", "Jakarta", "إندونيسيا", -6.21, 106.85),
        SakinaCity("كوالالمبور", "Kuala Lumpur", "ماليزيا", 3.14, 101.69)
    )
}
