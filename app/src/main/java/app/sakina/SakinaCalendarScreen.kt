package app.sakina

import android.content.Context
import android.icu.util.IslamicCalendar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

private data class VerifiedHadith(
    val occasionAr: String,
    val occasionEn: String,
    val matnAr: String,
    val matnEn: String,
    val isnadAr: String,
    val isnadEn: String
)

private val DAILY_VERIFIED_HADITHS = listOf(
    VerifiedHadith(
        "فضل الصلوات الخمس", "Virtue of the Five Daily Prayers",
        "قال رسول الله ﷺ: «أرأيتم لو أن نهراً بباب أحدكم يغتسل منه كل يوم خمس مرات، هل يبقى من درنه شيء؟» قالوا: لا يبقى من درنه شيء، قال: «فذلك مثل الصلوات الخمس، يمحو الله بهن الخطايا».",
        "The Prophet ﷺ said: 'If there were a river at the door of one of you in which he washed five times daily, would any dirt remain on him?' They said: 'None.' He said: 'That is the likeness of the five prayers by which Allah erases sins.'",
        "أبو هريرة رضي الله عنه | متفق عليه (البخاري 528، مسلم 667) | صحيح", "Abu Hurayrah | Bukhari (528) & Muslim (667) | Sahih"
    ),
    VerifiedHadith(
        "أحب الأعمال إلى الله", "Deeds Most Beloved to Allah",
        "سُئل النبي ﷺ: أي العمل أحب إلى الله؟ قال: «الصلاة على وقتها»، قال: ثم أي؟ قال: «ثم بر الوالدين»، قال: ثم أي؟ قال: «الجهاد في سبيل الله».",
        "The Prophet ﷺ was asked: 'Which deed is most beloved to Allah?' He said: 'Prayer offered on time, then kindness to parents, then striving in the cause of Allah.'",
        "ابن مسعود رضي الله عنه | متفق عليه (البخاري 527، مسلم 85) | صحيح", "Ibn Mas'ud | Bukhari (527) & Muslim (85) | Sahih"
    ),
    VerifiedHadith(
        "فضل البردين", "Virtue of Fajr and Asr",
        "قال رسول الله ﷺ: «من صلى البردين دخل الجنة».",
        "The Messenger of Allah ﷺ said: 'Whoever prays the two cool prayers (Fajr and Asr) will enter Paradise.'",
        "أبو موسى الأشعري رضي الله عنه | متفق عليه (البخاري 574، مسلم 635) | صحيح", "Abu Musa | Bukhari (574) & Muslim (635) | Sahih"
    ),
    VerifiedHadith(
        "فضل الذكر والتسبيح", "Virtue of Dhikr and Tasbih",
        "قال رسول الله ﷺ: «كلمتان خفيفتان على اللسان، ثقيلتان في الميزان، حبيبتان إلى الرحمن: سبحان الله وبحمده، سبحان الله العظيم».",
        "The Prophet ﷺ said: 'Two expressions are light on the tongue, heavy on the Scale, and beloved to the Most Merciful: SubhanAllahi wa bihamdihi, SubhanAllahil-Adheem.'",
        "أبو هريرة رضي الله عنه | متفق عليه (البخاري 6406، مسلم 2694) | صحيح", "Abu Hurayrah | Bukhari (6406) & Muslim (2694) | Sahih"
    ),
    VerifiedHadith(
        "الحياء والإيمان", "Modesty and Faith",
        "قال رسول الله ﷺ: «الإيمان بضع وستون شعبة، والحياء شعبة من الإيمان».",
        "The Messenger of Allah ﷺ said: 'Faith has over sixty branches, and modesty is a branch of faith.'",
        "أبو هريرة رضي الله عنه | متفق عليه (البخاري 9، مسلم 35) | صحيح", "Abu Hurayrah | Bukhari (9) & Muslim (35) | Sahih"
    ),
    VerifiedHadith(
        "الأعمال بالنيات", "Actions Are by Intentions",
        "قال رسول الله ﷺ: «إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى».",
        "The Messenger of Allah ﷺ said: 'Actions are judged only by intentions, and every person shall have only that which he intended.'",
        "عمر بن الخطاب رضي الله عنه | متفق عليه (البخاري 1، مسلم 1907) | صحيح", "Umar ibn al-Khattab | Bukhari (1) & Muslim (1907) | Sahih"
    ),
    VerifiedHadith(
        "حسن الخلق والمحبة", "Loving for Your Brother",
        "قال رسول الله ﷺ: «لا يؤمن أحدكم حتى يحب لأخيه ما يحب لنفسه».",
        "The Messenger of Allah ﷺ said: 'None of you truly believes until he loves for his brother what he loves for himself.'",
        "أنس بن مالك رضي الله عنه | متفق عليه (البخاري 13، مسلم 45) | صحيح", "Anas ibn Malik | Bukhari (13) & Muslim (45) | Sahih"
    ),
    VerifiedHadith(
        "فضل قراءة القرآن", "Virtue of Reciting the Quran",
        "قال رسول الله ﷺ: «اقرؤوا القرآن فإنه يأتي يوم القيامة شفيعاً لأصحابه».",
        "The Messenger of Allah ﷺ said: 'Recite the Quran, for on the Day of Resurrection it will come as an intercessor for its companions.'",
        "أبو أمامة الباهلي رضي الله عنه | صحيح مسلم (804) | صحيح", "Abu Umamah | Sahih Muslim (804) | Sahih"
    ),
    VerifiedHadith(
        "فضل الدعاء للمسلم بظهر الغيب", "Supplicating for Others",
        "قال رسول الله ﷺ: «دعوة المرء المسلم لأخيه بظهر الغيب مستجابة، عند رأسه ملك موكل كلما دعا لأخيه بخير قال الملك الموكل به: آمين ولك بمثل».",
        "The Prophet ﷺ said: 'The supplication of a Muslim for his brother in his absence is answered; an angel says: Ameen, and for you the same.'",
        "أبو الدرداء رضي الله عنه | صحيح مسلم (2733) | صحيح", "Abu Darda | Sahih Muslim (2733) | Sahih"
    ),
    VerifiedHadith(
        "فضل المشي إلى المساجد", "Walking to the Mosque",
        "قال رسول الله ﷺ: «من غدا إلى المسجد أو راح أعد الله له في الجنة نزلاً كلما غدا أو راح».",
        "The Prophet ﷺ said: 'Whoever goes to the mosque in the morning or evening, Allah prepares for him a welcoming abode in Paradise.'",
        "أبو هريرة رضي الله عنه | متفق عليه (البخاري 662، مسلم 669) | صحيح", "Abu Hurayrah | Bukhari (662) & Muslim (669) | Sahih"
    ),
    VerifiedHadith(
        "الرفق في الأمور", "Gentleness in All Affairs",
        "قال رسول الله ﷺ: «إن الرفق لا يكون في شيء إلا زانه، ولا ينزع من شيء إلا شانه».",
        "The Messenger of Allah ﷺ said: 'Gentleness is not found in anything except that it adorns it, and it is not removed from anything except that it mars it.'",
        "عائشة رضي الله عنها | صحيح مسلم (2594) | صحيح", "Aishah | Sahih Muslim (2594) | Sahih"
    ),
    VerifiedHadith(
        "الكلمة الطيبة صدقة", "A Good Word Is Charity",
        "قال رسول الله ﷺ: «اتقوا النار ولو بشق تمرة، فإن لم تجدوا فبكلمة طيبة».",
        "The Messenger of Allah ﷺ said: 'Guard yourselves from the Fire even with half a date, and if you do not find it, then with a kind word.'",
        "عدي بن حاتم رضي الله عنه | متفق عليه (البخاري 6023، مسلم 1016) | صحيح", "Adi ibn Hatim | Bukhari (6023) & Muslim (1016) | Sahih"
    ),
    VerifiedHadith(
        "الصدقة لا تنقص المال", "Charity Never Decreases Wealth",
        "قال رسول الله ﷺ: «ما نقصت صدقة من مال، وما زاد الله عبداً بعفو إلا عزاً، وما تواضع أحد لله إلا رفعه الله».",
        "The Messenger of Allah ﷺ said: 'Charity does not decrease wealth, Allah only increases a forgiving servant in honor, and whoever humbles himself for Allah, Allah elevates him.'",
        "أبو هريرة رضي الله عنه | صحيح مسلم (2588) | صحيح", "Abu Hurayrah | Sahih Muslim (2588) | Sahih"
    ),
    VerifiedHadith(
        "صلة الرحم وبركة العمر", "Maintaining Family Ties",
        "قال رسول الله ﷺ: «من أحب أن يبسط له في رزقه، وينسأ له في أثره، فليصل رحمه».",
        "The Messenger of Allah ﷺ said: 'Whoever loves to have his provision expanded and his lifespan blessed, let him maintain his ties of kinship.'",
        "أنس بن مالك رضي الله عنه | متفق عليه (البخاري 5986، مسلم 2557) | صحيح", "Anas ibn Malik | Bukhari (5986) & Muslim (2557) | Sahih"
    ),
    VerifiedHadith(
        "الماهر بالقرآن", "Mastery of the Quran",
        "قال رسول الله ﷺ: «الماهر بالقرآن مع السفرة الكرام البررة، والذي يقرأ القرآن ويتتعتع فيه وهو عليه شاق له أجران».",
        "The Prophet ﷺ said: 'The one who is proficient in reciting the Quran is with the noble, obedient angels, and the one who struggles with it gets two rewards.'",
        "عائشة رضي الله عنها | متفق عليه (البخاري 4937، مسلم 798) | صحيح", "Aishah | Bukhari (4937) & Muslim (798) | Sahih"
    ),
    VerifiedHadith(
        "حفظ اللسان", "Guarding the Tongue",
        "قال رسول الله ﷺ: «من كان يؤمن بالله واليوم الآخر فليقل خيراً أو ليصمت».",
        "The Messenger of Allah ﷺ said: 'Whoever believes in Allah and the Last Day, let him speak good or remain silent.'",
        "أبو هريرة رضي الله عنه | متفق عليه (البخاري 6018، مسلم 47) | صحيح", "Abu Hurayrah | Bukhari (6018) & Muslim (47) | Sahih"
    ),
    VerifiedHadith(
        "تيسير العسير", "Relieving Hardship",
        "قال رسول الله ﷺ: «من نفس عن مؤمن كربة من كرب الدنيا نفس الله عنه كربة من كرب يوم القيامة، والله في عون العبد ما كان العبد في عون أخيه».",
        "The Prophet ﷺ said: 'Whoever relieves a believer of a worldly hardship, Allah will relieve him of a hardship on the Day of Resurrection.'",
        "أبو هريرة رضي الله عنه | صحيح مسلم (2699) | صحيح", "Abu Hurayrah | Sahih Muslim (2699) | Sahih"
    ),
    VerifiedHadith(
        "الذكر وحياة القلب", "Remembrance of Allah",
        "قال رسول الله ﷺ: «مثل الذي يذكر ربه والذي لا يذكر ربه مثل الحي والميت».",
        "The Messenger of Allah ﷺ said: 'The likeness of the one who remembers his Lord and the one who does not is that of the living and the dead.'",
        "أبو موسى الأشعري رضي الله عنه | متفق عليه (البخاري 6407، مسلم 779) | صحيح", "Abu Musa | Bukhari (6407) & Muslim (779) | Sahih"
    ),
    VerifiedHadith(
        "أفضل الصدقة وإفشاء السلام", "Spreading Peace",
        "قال رسول الله ﷺ: «لا تدخلون الجنة حتى تؤمنوا، ولا تؤمنوا حتى تحابوا، أولا أدلكم على شيء إذا فعلتموه تحاببتم؟ أفشوا السلام بينكم».",
        "The Prophet ﷺ said: 'You will not enter Paradise until you believe, and you will not believe until you love one another. Spread Salam among yourselves.'",
        "أبو هريرة رضي الله عنه | صحيح مسلم (54) | صحيح", "Abu Hurayrah | Sahih Muslim (54) | Sahih"
    ),
    VerifiedHadith(
        "القوة في ضبط النفس", "True Strength",
        "قال رسول الله ﷺ: «ليس الشديد بالصرعة، إنما الشديد الذي يملك نفسه عند الغضب».",
        "The Messenger of Allah ﷺ said: 'The strong person is not the one who throws others down, but the one who controls himself when angry.'",
        "أبو هريرة رضي الله عنه | متفق عليه (البخاري 6114، مسلم 2609) | صحيح", "Abu Hurayrah | Bukhari (6114) & Muslim (2609) | Sahih"
    ),
    VerifiedHadith(
        "نعمة الصحة والفراغ", "Two Neglected Blessings",
        "قال رسول الله ﷺ: «نعمتان مغبون فيهما كثير من الناس: الصحة والفراغ».",
        "The Messenger of Allah ﷺ said: 'There are two blessings which many people lose: health and free time.'",
        "ابن عباس رضي الله عنهما | صحيح البخاري (6412) | صحيح", "Ibn Abbas | Sahih al-Bukhari (6412) | Sahih"
    ),
    VerifiedHadith(
        "الاستغفار والتوبة", "Seeking Forgiveness",
        "قال رسول الله ﷺ: «والله إني لأستغفر الله وأتوب إليه في اليوم أكثر من سبعين مرة».",
        "The Messenger of Allah ﷺ said: 'By Allah, I seek Allah's forgiveness and turn to Him in repentance more than seventy times a day.'",
        "أبو هريرة رضي الله عنه | صحيح البخاري (6307) | صحيح", "Abu Hurayrah | Sahih al-Bukhari (6307) | Sahih"
    ),
    VerifiedHadith(
        "الصلاة نور والصدقة برهان", "Purity and Prayer",
        "قال رسول الله ﷺ: «الطهور شطر الإيمان، والحمد لله تملأ الميزان، والصلاة نور، والصدقة برهان، والصبر ضياء».",
        "The Prophet ﷺ said: 'Purity is half of faith, Alhamdulillah fills the scale, prayer is light, charity is proof, and patience is illumination.'",
        "أبو مالك الأشعري رضي الله عنه | صحيح مسلم (223) | صحيح", "Abu Malik | Sahih Muslim (223) | Sahih"
    ),
    VerifiedHadith(
        "خيركم من تعلم القرآن وعلمه", "Best Among You",
        "قال رسول الله ﷺ: «خيركم من تعلم القرآن وعلمه».",
        "The Messenger of Allah ﷺ said: 'The best among you are those who learn the Quran and teach it.'",
        "عثمان بن عفان رضي الله عنه | صحيح البخاري (5027) | صحيح", "Uthman ibn Affan | Sahih al-Bukhari (5027) | Sahih"
    )
)

// Occasion-specific verified hadiths (#4)
private val OCCASION_RAMADAN = VerifiedHadith(
    "شهر رمضان المبارك", "Blessed Month of Ramadan",
    "قال رسول الله ﷺ: «من صام رمضان إيماناً واحتساباً غفر له ما تقدم من ذنبه، ومن قام ليلة القدر إيماناً واحتساباً غفر له ما تقدم من ذنبه».",
    "The Prophet ﷺ said: 'Whoever fasts Ramadan out of faith and seeking reward will be forgiven his past sins.'",
    "أبو هريرة رضي الله عنه | متفق عليه (البخاري 1901، مسلم 760) | صحيح", "Abu Hurayrah | Bukhari (1901) & Muslim (760) | Sahih"
)
private val OCCASION_SHAWWAL = VerifiedHadith(
    "صيام ست من شوال", "Six Days of Shawwal",
    "قال رسول الله ﷺ: «من صام رمضان ثم أتبعه ستاً من شوال كان كصيام الدهر».",
    "The Prophet ﷺ said: 'Whoever fasts Ramadan and follows it with six days of Shawwal, it is as if he fasted a lifetime.'",
    "أبو أيوب الأنصاري رضي الله عنه | صحيح مسلم (1164) | صحيح", "Abu Ayyub | Sahih Muslim (1164) | Sahih"
)
private val OCCASION_TEN_DHUL_HIJJAH = VerifiedHadith(
    "العشر الأوائل من ذي الحجة", "First Ten Days of Dhu al-Hijjah",
    "قال رسول الله ﷺ: «ما من أيام العمل الصالح فيهن أحب إلى الله من هذه الأيام العشر» قالوا: ولا الجهاد في سبيل الله؟ قال: «ولا الجهاد في سبيل الله إلا رجل خرج بنفسه وماله فلم يرجع من ذلك بشيء».",
    "The Prophet ﷺ said: 'There are no days in which righteous deeds are more beloved to Allah than these ten days.'",
    "ابن عباس رضي الله عنهما | صحيح البخاري (969) | صحيح", "Ibn Abbas | Sahih al-Bukhari (969) | Sahih"
)
private val OCCASION_ARAFAH = VerifiedHadith(
    "فضل صيام يوم عرفة", "Day of Arafah (9 Dhu al-Hijjah)",
    "قال رسول الله ﷺ: «صيام يوم عرفة أحتسب على الله أن يكفر السنة التي قبله والسنة التي بعده».",
    "The Prophet ﷺ said: 'Fasting the Day of Arafah, I hope from Allah that it expiates the sins of the preceding year and the coming year.'",
    "أبو قتادة الأنصاري رضي الله عنه | صحيح مسلم (1162) | صحيح", "Abu Qatadah | Sahih Muslim (1162) | Sahih"
)
private val OCCASION_ASHURA = VerifiedHadith(
    "يوم عاشوراء وتاسوعاء", "Day of Ashura (Muharram)",
    "قال رسول الله ﷺ عن صيام يوم عاشوراء: «أحتسب على الله أن يكفر السنة التي قبله»، وقال: «لئن بقيت إلى قابل لأصومن التاسع».",
    "The Prophet ﷺ said: 'Fasting the Day of Ashura, I hope from Allah that it expiates the sins of the preceding year.'",
    "أبو قتادة وابن عباس رضي الله عنهم | صحيح مسلم (1162، 1134) | صحيح", "Abu Qatadah & Ibn Abbas | Sahih Muslim (1162, 1134) | Sahih"
)
private val OCCASION_WHITE_DAYS = VerifiedHadith(
    "صيام الأيام البيض", "The Three White Days (13–15)",
    "قال أبو هريرة رضي الله عنه: «أوصاني خليلي ﷺ بثلاث لا أدعهن حتى أموت: صوم ثلاثة أيام من كل شهر، وصلاة الضحى، ونوم على وتر».",
    "Abu Hurayrah said: 'My beloved friend ﷺ advised me to fast three days every month, pray Duha, and pray Witr before sleep.'",
    "أبو هريرة رضي الله عنه | متفق عليه (البخاري 1178، مسلم 721) | صحيح", "Abu Hurayrah | Bukhari (1178) & Muslim (721) | Sahih"
)
private val OCCASION_FRIDAY = VerifiedHadith(
    "فضل يوم الجمعة", "Virtue of Friday (Jumu'ah)",
    "قال رسول الله ﷺ: «خير يوم طلعت عليه الشمس يوم الجمعة؛ فيه خُلق آدم، وفيه أُدخل الجنة، وفيه أُخرج منها».",
    "The Prophet ﷺ said: 'The best day on which the sun rises is Friday; on it Adam was created, entered Paradise, and left it.'",
    "أبو هريرة رضي الله عنه | صحيح مسلم (854) | صحيح", "Abu Hurayrah | Sahih Muslim (854) | Sahih"
)
private val OCCASION_MON_THU = VerifiedHadith(
    "صيام الإثنين والخميس", "Mondays and Thursdays",
    "سُئل رسول الله ﷺ عن صوم يوم الإثنين فقال: «ذاك يوم وُلدت فيه، ويوم بُعثت أو أُنزل عليّ فيه».",
    "The Prophet ﷺ was asked about fasting Mondays and said: 'That is the day I was born and the day revelation began.'",
    "أبو قتادة الأنصاري رضي الله عنه | صحيح مسلم (1162) | صحيح", "Abu Qatadah | Sahih Muslim (1162) | Sahih"
)

private fun selectHadithForDate(cal: Calendar, hijri: IslamicCalendar): VerifiedHadith {
    val hMonth = hijri.get(IslamicCalendar.MONTH)
    val hDay = hijri.get(IslamicCalendar.DAY_OF_MONTH)
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    return when {
        hMonth == IslamicCalendar.RAMADAN -> OCCASION_RAMADAN
        hMonth == IslamicCalendar.SHAWWAL && hDay in 2..10 -> OCCASION_SHAWWAL
        hMonth == IslamicCalendar.DHU_AL_HIJJAH && hDay == 9 -> OCCASION_ARAFAH
        hMonth == IslamicCalendar.DHU_AL_HIJJAH && hDay in 1..8 -> OCCASION_TEN_DHUL_HIJJAH
        hMonth == IslamicCalendar.MUHARRAM && hDay in 9..10 -> OCCASION_ASHURA
        hDay in 13..15 -> OCCASION_WHITE_DAYS
        dow == Calendar.FRIDAY -> OCCASION_FRIDAY
        dow == Calendar.MONDAY || dow == Calendar.THURSDAY -> OCCASION_MON_THU
        else -> {
            val seed = cal.get(Calendar.YEAR) * 366 + cal.get(Calendar.DAY_OF_YEAR)
            DAILY_VERIFIED_HADITHS[seed % DAILY_VERIFIED_HADITHS.size]
        }
    }
}

@Composable
fun SakinaCalendarScreen(ctx: Context, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val s = LocalSakinaStrings.current
    val lang = LocalSakinaLang.current
    var monthOffset by remember { mutableIntStateOf(0) }
    val todayDayNum = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }
    var selectedDay by remember { mutableIntStateOf(todayDayNum) }

    val baseCal = remember(monthOffset) {
        Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset); set(Calendar.DAY_OF_MONTH, 1) }
    }
    val daysInMonth = baseCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (baseCal.get(Calendar.DAY_OF_WEEK) - Calendar.SATURDAY + 7) % 7
    val clampedSelected = selectedDay.coerceIn(1, daysInMonth)

    val selectedCal = remember(monthOffset, clampedSelected) {
        (baseCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, clampedSelected) }
    }
    val hijriCal = remember(selectedCal) {
        IslamicCalendar().apply {
            timeInMillis = selectedCal.timeInMillis
            add(IslamicCalendar.DAY_OF_MONTH, Prefs.hijriOffset(ctx))
        }
    }
    val gregTitle = remember(baseCal, lang) {
        SimpleDateFormat("MMMM yyyy", lang.locale).format(baseCal.time).toWesternDigits()
    }
    val hijriMonthName = s.hijriMonths.getOrElse(hijriCal.get(IslamicCalendar.MONTH).coerceIn(0, 11)) { "" }
    val hijriSuffix = sakinaTr(lang, "هـ", "AH", "H", "H", "H", "ھ")
    val hijriSubtitle = "${hijriCal.get(IslamicCalendar.DAY_OF_MONTH)} $hijriMonthName ${hijriCal.get(IslamicCalendar.YEAR)} $hijriSuffix".toWesternDigits()

    val dayOffsetFromToday = ((selectedCal.timeInMillis - Calendar.getInstance().timeInMillis) / 86_400_000L).toInt()
    val dayPrayers = remember(dayOffsetFromToday) { PrayerEngine.day(ctx, dayOffsetFromToday) }
    val hm = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val dailyHadith = remember(selectedCal, hijriCal) { selectHadithForDate(selectedCal, hijriCal) }
    val isArabicOrUrdu = lang == SakinaLang.AR || lang == SakinaLang.UR

    // #5: Shifted down (top = 56.dp) so the top cartouche sits well below the Mihrab arch,
    // and added breathing room between the cartouche and the Calendar card while keeping the bottom anchored cleanly above the nav bar!
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 56.dp, bottom = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 36.dp, vertical = 6.dp)) {
            Text(
                text = s.calendarTitle,
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = theme.accentBright,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        // #5: Space moved to the top between the Hijri Calendar cartouche and the Calendar grid!
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Luxury Ornate Monthly Calendar Card (#7)
        val calShape = RoundedCornerShape(18.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, calShape)
                .clip(calShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            theme.bgCard.copy(alpha = 0.96f),
                            Color(0xFF071F24),
                            theme.bgDeep.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(1.4.dp, Sakina3DGoldBevelBrush, calShape)
        ) {
            // 4-Corner Gold Arabesque Filigree Ornaments inside the Calendar (#7)
            val cSize = 18.dp
            Image(
                painter = painterResource(id = R.drawable.ic_corner_arabesque),
                contentDescription = null,
                alpha = 0.72f,
                modifier = Modifier.size(cSize).align(AbsoluteAlignment.TopLeft).padding(2.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_corner_arabesque),
                contentDescription = null,
                alpha = 0.72f,
                modifier = Modifier.size(cSize).align(AbsoluteAlignment.TopRight).padding(2.dp).graphicsLayer { rotationZ = 90f }
            )
            Image(
                painter = painterResource(id = R.drawable.ic_corner_arabesque),
                contentDescription = null,
                alpha = 0.72f,
                modifier = Modifier.size(cSize).align(AbsoluteAlignment.BottomRight).padding(2.dp).graphicsLayer { rotationZ = 180f }
            )
            Image(
                painter = painterResource(id = R.drawable.ic_corner_arabesque),
                contentDescription = null,
                alpha = 0.72f,
                modifier = Modifier.size(cSize).align(AbsoluteAlignment.BottomLeft).padding(2.dp).graphicsLayer { rotationZ = 270f }
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // Luxury Month Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33D4AF37))
                        .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(theme.bgDeep)
                            .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                            .clickable { monthOffset-- },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = hijriSubtitle,
                            fontFamily = SakinaEditorialFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = theme.accentBright,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                        Text(
                            text = gregTitle,
                            fontFamily = SakinaBodyFont,
                            fontSize = 10.sp,
                            color = SakinaBrandColors.LuminousIvory,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(theme.bgDeep)
                            .border(0.8.dp, Sakina3DGoldBevelBrush, CircleShape)
                            .clickable { monthOffset++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = null, tint = theme.accentBright, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Gold-Tinted Weekdays Ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x1AD4AF37))
                        .padding(vertical = 2.5.dp)
                ) {
                    s.weekdays.forEach { d ->
                        Text(
                            text = d,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontFamily = SakinaBodyFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.8.sp,
                            color = theme.accentBright,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                }

                Spacer(Modifier.height(3.dp))

                val totalCells = firstDayOfWeek + daysInMonth
                val rows = (totalCells + 6) / 7
                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                        for (c in 0 until 7) {
                            val idx = r * 7 + c
                            val dayNum = idx - firstDayOfWeek + 1
                            val isSelected = dayNum == clampedSelected
                            val isToday = monthOffset == 0 && dayNum == todayDayNum
                            val isFriday = c == 6
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(21.dp)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .then(
                                        when {
                                            isSelected -> Modifier
                                                .background(Brush.verticalGradient(listOf(theme.accentBright, theme.accentGold)))
                                                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(7.dp))
                                            isToday -> Modifier
                                                .background(Color(0xFF0F3D3E))
                                                .border(0.9.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(7.dp))
                                            else -> Modifier
                                        }
                                    )
                                    .clickable(enabled = dayNum in 1..daysInMonth) { selectedDay = dayNum },
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNum in 1..daysInMonth) {
                                    Text(
                                        text = "$dayNum".toWesternDigits(),
                                        fontFamily = SakinaNumeralFont,
                                        fontWeight = if (isSelected || isToday || isFriday) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.8.sp,
                                        color = when {
                                            isSelected -> theme.bgDeep
                                            isToday || isFriday -> theme.accentBright
                                            else -> SakinaBrandColors.LuminousIvory
                                        },
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Spacious 5-Column Prayer Times Card ("توقيت صلوات هذا اليوم") (#6)
        // Shows ALL 5 prayers and their times in 5 columns with zero truncation!
        SakinaParchmentPanel(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 15.dp,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Text(
                text = "❖ ${s.dayPrayersCardTitle} ❖",
                fontFamily = SakinaEditorialFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = Color(0xFF0F3D3E),
                textAlign = TextAlign.Center,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x140F3D3E))
                    .border(0.8.dp, Color(0x44C5A059), RoundedCornerShape(10.dp))
                    .padding(vertical = 5.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dayPrayers.forEachIndexed { idx, (_, d) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = s.prayers.getOrElse(idx) { "" },
                            fontFamily = SakinaEditorialFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = Color(0xFF0F3D3E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = hm.format(d).toWesternDigits(),
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.2.sp,
                            color = SakinaInkDark,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontFeatureSettings = "tnum",
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                    if (idx < dayPrayers.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(0.8.dp)
                                .height(24.dp)
                                .background(Color(0x33C5A059))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Verified Hadith of the Day Card with Isnad & Grade
        val frMap = mapOf(
            "Deeds Most Beloved to Allah" to Triple("Les œuvres les plus aimées d'Allah", "On demanda au Prophète ﷺ : « Quelle est l'œuvre la plus aimée d'Allah ? » Il répondit : « La prière accomplie à son heure, puis la bienfaisance envers les parents, puis l'effort dans le sentier d'Allah. »", "Ibn Mas'oud | Boukhari (527) & Mouslim (85) | Sahih"),
            "Virtue of Friday" to Triple("Mérite du vendredi", "Le meilleur jour sur lequel le soleil s'est levé est le vendredi : c'est en ce jour qu'Adam fut créé.", "Abou Hourayra | Sahih Mouslim (854)"),
            "Monday & Thursday Fasting" to Triple("Jeûne du lundi et jeudi", "Les œuvres sont présentées le lundi et le jeudi, et j'aime que mes œuvres soient présentées alors que je jeûne.", "Abou Hourayra | Tirmidhi (747) | Sahih"),
            "The White Days" to Triple("Les jours blancs", "Jeûner trois jours chaque mois équivaut au jeûne de toute l'année.", "Abou Dharr | Boukhari (1979) & Mouslim (1159)")
        )
        val frHit = if (lang == SakinaLang.FR) frMap[dailyHadith.occasionEn] else null
        val occasionText = when {
            isArabicOrUrdu -> dailyHadith.occasionAr
            frHit != null -> frHit.first
            lang == SakinaLang.FR -> "Parole prophétique"
            else -> dailyHadith.occasionEn
        }
        val matnText = when {
            isArabicOrUrdu -> dailyHadith.matnAr
            frHit != null -> frHit.second
            else -> dailyHadith.matnEn
        }
        val isnadText = when {
            isArabicOrUrdu -> dailyHadith.isnadAr
            frHit != null -> frHit.third
            else -> dailyHadith.isnadEn
        }
        val matnFontSize = if (matnText.length > 145) 10.2.sp else 11.2.sp

        SakinaParchmentPanel(
            modifier = Modifier.fillMaxWidth().weight(1f),
            cornerRadius = 15.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "❖ ${s.dailyHadithTitle} ($occasionText) ❖",
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF0F3D3E),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = matnText.toWesternDigits(),
                    fontFamily = SakinaEditorialFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = matnFontSize,
                    lineHeight = 15.5.sp,
                    color = SakinaInkDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = isnadText.toWesternDigits(),
                    fontFamily = SakinaBodyFont,
                    fontSize = 9.sp,
                    lineHeight = 12.5.sp,
                    color = Color(0xFF6B5224),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
