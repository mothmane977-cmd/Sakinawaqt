package app.sakina

/** حساب الليل الإسلامي بالمللي ثانية: من المغرب إلى الفجر التالي. */
object NightMath {
    fun midnight(maghrib: Long, nextFajr: Long): Long = maghrib + (nextFajr - maghrib) / 2
    fun lastThird(maghrib: Long, nextFajr: Long): Long = nextFajr - (nextFajr - maghrib) / 3
}
