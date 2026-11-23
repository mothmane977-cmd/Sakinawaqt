package app.sakina
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.PrayerTimes
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import android.widget.Toast
import android.provider.MediaStore
import android.os.Environment
import android.os.Build
import android.graphics.Typeface
import android.graphics.Shader
import android.graphics.RectF
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Canvas
import android.graphics.Bitmap
import android.content.Intent
import android.content.ContentValues

import android.content.Context
import android.icu.util.IslamicCalendar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.*
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
    // v27 Point 2: Hijri Calendar is the FIXED default; user can toggle to Gregorian Calendar anytime
    var isHijriMode by remember { mutableStateOf(true) }
    var showImsakiyah by remember { mutableStateOf(false) }
    var monthOffset by remember { mutableIntStateOf(0) }
    val hijriOffsetDays = remember { Prefs.hijriOffset(ctx) }

    val todayHijriDay = remember(hijriOffsetDays) {
        IslamicCalendar().apply { add(IslamicCalendar.DAY_OF_MONTH, hijriOffsetDays) }.get(IslamicCalendar.DAY_OF_MONTH)
    }
    val todayGregDay = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }
    val todayDayNum = if (isHijriMode) todayHijriDay else todayGregDay
    var selectedDay by remember(isHijriMode) { mutableIntStateOf(if (isHijriMode) todayHijriDay else todayGregDay) }

    // Compute month start, length, and selected Calendar accurately for BOTH Hijri & Gregorian modes
    val baseHijri = remember(monthOffset, hijriOffsetDays) {
        IslamicCalendar().apply {
            add(IslamicCalendar.DAY_OF_MONTH, hijriOffsetDays)
            set(IslamicCalendar.DAY_OF_MONTH, 1)
            add(IslamicCalendar.MONTH, monthOffset)
        }
    }
    val baseGreg = remember(monthOffset) {
        Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset); set(Calendar.DAY_OF_MONTH, 1) }
    }

    val daysInMonth = if (isHijriMode) baseHijri.getActualMaximum(IslamicCalendar.DAY_OF_MONTH) else baseGreg.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = if (isHijriMode) {
        val gStart = Calendar.getInstance().apply { timeInMillis = baseHijri.timeInMillis; add(Calendar.DAY_OF_YEAR, -hijriOffsetDays) }
        (gStart.get(Calendar.DAY_OF_WEEK) - Calendar.SATURDAY + 7) % 7
    } else {
        (baseGreg.get(Calendar.DAY_OF_WEEK) - Calendar.SATURDAY + 7) % 7
    }
    val clampedSelected = selectedDay.coerceIn(1, daysInMonth)

    val selectedCal = remember(isHijriMode, monthOffset, clampedSelected, hijriOffsetDays) {
        if (isHijriMode) {
            val icSel = (baseHijri.clone() as IslamicCalendar).apply { set(IslamicCalendar.DAY_OF_MONTH, clampedSelected) }
            Calendar.getInstance().apply { timeInMillis = icSel.timeInMillis; add(Calendar.DAY_OF_YEAR, -hijriOffsetDays) }
        } else {
            (baseGreg.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, clampedSelected) }
        }
    }
    val hijriCal = remember(selectedCal, hijriOffsetDays) {
        IslamicCalendar().apply {
            timeInMillis = selectedCal.timeInMillis
            add(IslamicCalendar.DAY_OF_MONTH, hijriOffsetDays)
        }
    }
    val gregTitle = remember(selectedCal, lang) {
        SimpleDateFormat("d MMMM yyyy", lang.locale).format(selectedCal.time).toWesternDigits()
    }
    val hijriMonthName = s.hijriMonths.getOrElse(hijriCal.get(IslamicCalendar.MONTH).coerceIn(0, 11)) { "" }
    val hijriSuffix = sakinaTr(lang, "هـ", "AH", "H", "H", "H", "ھ")
    val hijriSubtitle = "${hijriCal.get(IslamicCalendar.DAY_OF_MONTH)} $hijriMonthName ${hijriCal.get(IslamicCalendar.YEAR)} $hijriSuffix".toWesternDigits()
    val dynamicCalTitle = if (isHijriMode) {
        sakinaTr(lang, "التقويم الهجري", "Hijri Calendar", "Calendrier Hégirien", "Hicri Takvim", "Kalender Hijriah", "ہجری کیلنڈر")
    } else {
        sakinaTr(lang, "التقويم الميلادي", "Gregorian Calendar", "Calendrier Grégorien", "Miladi Takvim", "Kalender Masehi", "عیسوی کیلنڈر")
    }

    val dayOffsetFromToday = ((selectedCal.timeInMillis - Calendar.getInstance().timeInMillis) / 86_400_000L).toInt()
    val dayPrayers = remember(dayOffsetFromToday) { PrayerEngine.day(ctx, dayOffsetFromToday) }
    val hm = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val dailyHadith = remember(selectedCal, hijriCal) { selectHadithForDate(selectedCal, hijriCal) }
    val isArabicOrUrdu = lang == SakinaLang.AR || lang == SakinaLang.UR

    // v36: Wire Astronomical Moon-Phase & Glassmorphic Lunar Calendar UI
    val monthCells = remember(isHijriMode, monthOffset, daysInMonth, firstDayOfWeek, todayDayNum, hijriOffsetDays, lang) {
        val cells = mutableListOf<CalendarDayCell>()
        repeat(firstDayOfWeek) {
            cells.add(CalendarDayCell(dayNumber = 0, isCurrentMonth = false))
        }
        for (d in 1..daysInMonth) {
            val cellCal = if (isHijriMode) {
                val ic = (baseHijri.clone() as IslamicCalendar).apply { set(IslamicCalendar.DAY_OF_MONTH, d) }
                Calendar.getInstance().apply { timeInMillis = ic.timeInMillis; add(Calendar.DAY_OF_YEAR, -hijriOffsetDays) }
            } else {
                (baseGreg.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, d) }
            }
            val cellHijri = IslamicCalendar().apply {
                timeInMillis = cellCal.timeInMillis
                add(IslamicCalendar.DAY_OF_MONTH, hijriOffsetDays)
            }
            val hM = cellHijri.get(IslamicCalendar.MONTH)
            val hD = cellHijri.get(IslamicCalendar.DAY_OF_MONTH)
            val isFri = cellCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
            val isWhite = hD in 13..15
            val occ: String? = when {
                hM == IslamicCalendar.MUHARRAM && hD == 1 -> sakinaTr(lang, "رأس السنة الهجرية", "Islamic New Year", "Nouvel An Hégirien", "Hicri Yılbaşı", "Tahun Baru Hijriah", "نیا ہجری سال")
                hM == IslamicCalendar.MUHARRAM && hD == 10 -> sakinaTr(lang, "يوم عاشوراء", "Day of Ashura", "Jour d'Achoura", "Aşure Günü", "Hari Asyura", "یوم عاشورہ")
                hM == IslamicCalendar.RAMADAN && hD == 1 -> sakinaTr(lang, "بداية شهر رمضان", "Start of Ramadan", "Début du Ramadan", "Ramazan Başlangıcı", "Awal Ramadan", "آغازِ رمضان")
                hM == IslamicCalendar.RAMADAN && hD == 27 -> sakinaTr(lang, "ليلة 27 رمضان", "27th of Ramadan", "27e nuit de Ramadan", "Kadir Gecesi", "Malam 27 Ramadan", "27 رمضان")
                hM == IslamicCalendar.SHAWWAL && hD == 1 -> sakinaTr(lang, "عيد الفطر المبارك", "Eid al-Fitr", "Aïd al-Fitr", "Ramazan Bayramı", "Idul Fitri", "عید الفطر")
                hM == IslamicCalendar.DHU_AL_HIJJAH && hD == 9 -> sakinaTr(lang, "يوم عرفة", "Day of Arafah", "Jour d'Arafat", "Arefe Günü", "Hari Arafah", "یوم عرفہ")
                hM == IslamicCalendar.DHU_AL_HIJJAH && hD == 10 -> sakinaTr(lang, "عيد الأضحى المبارك", "Eid al-Adha", "Aïd al-Adha", "Kurban Bayramı", "Idul Adha", "عید الاضحیٰ")
                else -> null
            }
            cells.add(
                CalendarDayCell(
                    dayNumber = d,
                    isCurrentMonth = true,
                    isToday = monthOffset == 0 && d == todayDayNum,
                    isFriday = isFri,
                    isWhiteDay = isWhite,
                    hasOccasion = occ != null,
                    occasionName = occ
                )
            )
        }
        cells
    }

    val monthHeaderLabel = remember(isHijriMode, baseHijri, baseGreg, s, lang) {
        if (isHijriMode) {
            val mName = s.hijriMonths.getOrElse(baseHijri.get(IslamicCalendar.MONTH).coerceIn(0, 11)) { "" }
            "$mName ${baseHijri.get(IslamicCalendar.YEAR)} $hijriSuffix".toWesternDigits()
        } else {
            SimpleDateFormat("MMMM yyyy", lang.locale).format(baseGreg.time).toWesternDigits()
        }
    }

    val prayerSummary = remember(dayPrayers) {
        fun fmt(idx: Int): String = dayPrayers.getOrNull(idx)?.second?.let { SakinaAdvancedPrefs.formatPrayerTime(ctx, it) } ?: "--:--"
        DayPrayerSummary(
            fajr = fmt(0),
            dhuhr = fmt(1),
            asr = fmt(2),
            maghrib = fmt(3),
            isha = fmt(4)
        )
    }

    val weekdayHdrs = when (lang) {
        SakinaLang.AR -> listOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
        SakinaLang.FR -> listOf("Sam", "Dim", "Lun", "Mar", "Mer", "Jeu", "Ven")
        SakinaLang.TR -> listOf("Cmt", "Paz", "Pzt", "Sal", "Çar", "Per", "Cum")
        SakinaLang.ID -> listOf("Sab", "Min", "Sen", "Sel", "Rab", "Kam", "Jum")
        SakinaLang.UR -> listOf("ہفتہ", "اتوار", "پیر", "منگل", "بدھ", "جمعرات", "جمعہ")
        else -> listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
    }

    val calStrings = SakinaCalendarStrings(
        screenTitle = dynamicCalTitle,
        hijriModeLabel = sakinaTr(lang, "هجري", "Hijri", "Hégirien", "Hicri", "Hijriah", "ہجری"),
        gregorianModeLabel = sakinaTr(lang, "ميلادي", "Gregorian", "Grégorien", "Miladi", "Masehi", "عیسوی"),
        sharePngLabel = "PNG",
        weekdayHeaders = weekdayHdrs,
        fajrLabel = SakinaAdvancedPrefs.displayPrayerName(ctx, lang, 0, false),
        dhuhrLabel = SakinaAdvancedPrefs.displayPrayerName(ctx, lang, 1, false),
        asrLabel = SakinaAdvancedPrefs.displayPrayerName(ctx, lang, 2, false),
        maghribLabel = SakinaAdvancedPrefs.displayPrayerName(ctx, lang, 3, false),
        ishaLabel = SakinaAdvancedPrefs.displayPrayerName(ctx, lang, 4, false),
        whiteDayBadge = sakinaTr(lang, "من الأيام البيض", "White Days (Fasting)", "Jours Blancs", "Eyyam-ı Biyd", "Puasa Ayyamul Bidh", "ایام بیض"),
        hadithCardTitle = sakinaTr(lang, "حديث اليوم", "Hadith of the Day", "Hadith du jour", "Günün Hadisi", "Hadits Hari Ini", "آج کی حدیث"),
        moonPhaseWaxingCrescent = sakinaTr(lang, "هلال متزايد", "Waxing Crescent", "Premier croissant", "Hilal", "Sabit Awal", "بڑھتا ہلال"),
        moonPhaseFirstQuarter = sakinaTr(lang, "تربيع أول", "First Quarter", "Premier quartier", "İlk Dördün", "Perbani Awal", "تربیع اول"),
        moonPhaseWaxingGibbous = sakinaTr(lang, "أحدب متزايد", "Waxing Gibbous", "Gibbeuse croissante", "Şişkin Ay", "Cembung Awal", "بڑھتا چاند"),
        moonPhaseFullMoon = sakinaTr(lang, "بدر مكتمل", "Full Moon", "Pleine lune", "Dolunay", "Purnama", "پورا چاند"),
        moonPhaseWaningGibbous = sakinaTr(lang, "أحدب متناقص", "Waning Gibbous", "Gibbeuse décroissante", "Azalan Şişkin", "Cembung Akhir", "گھٹتا چاند"),
        moonPhaseLastQuarter = sakinaTr(lang, "تربيع أخير", "Last Quarter", "Dernier quartier", "Son Dördün", "Perbani Akhir", "تربیع آخر"),
        moonPhaseWaningCrescent = sakinaTr(lang, "هلال متناقص", "Waning Crescent", "Dernier croissant", "Son Hilal", "Sabit Akhir", "گھٹتا ہلال"),
        moonPhaseNewMoon = sakinaTr(lang, "محاق", "New Moon", "Nouvelle lune", "Yeni Ay", "Bulan Baru", "محاق")
    )

    val lunarState = SakinaLunarCalendarState(
        displayMode = if (isHijriMode) CalendarDisplayMode.HIJRI else CalendarDisplayMode.GREGORIAN,
        hijriDayOfMonth = hijriCal.get(IslamicCalendar.DAY_OF_MONTH),
        hijriDateTitle = hijriSubtitle,
        monthHeaderTitle = monthHeaderLabel,
        gregorianSubtitle = gregTitle,
        monthCells = monthCells,
        selectedDayNumber = clampedSelected,
        selectedDayPrayers = prayerSummary,
        hadithTopic = if (isArabicOrUrdu) dailyHadith.topicAr else dailyHadith.topicEn,
        hadithText = if (isArabicOrUrdu) dailyHadith.textAr else dailyHadith.textEn,
        hadithSource = if (isArabicOrUrdu) dailyHadith.sourceAr else dailyHadith.sourceEn
    )

    SakinaLunarCalendarScreen(
        state = lunarState,
        strings = calStrings,
        headingFontFamily = SakinaHeadingFont,
        bodyFontFamily = SakinaBodyFont,
        showBottomDock = false,
        hostBottomBarReserve = 82.dp,
        onDisplayModeChange = { mode ->
            val wantHijri = (mode == CalendarDisplayMode.HIJRI)
            if (wantHijri != isHijriMode) {
                isHijriMode = wantHijri
                monthOffset = 0
                selectedDay = if (wantHijri) todayHijriDay else todayGregDay
            }
        },
        onShareMonthlyPngClick = { showImsakiyah = true },
        onPrevMonthClick = { monthOffset-- },
        onNextMonthClick = { monthOffset++ },
        onDayClick = { cell -> selectedDay = cell.dayNumber }
    )

        if (showImsakiyah) {
            val monthRows = remember(isHijriMode, monthOffset, daysInMonth, hijriOffsetDays) {
                buildMonthlyImsakiyah(ctx, isHijriMode, monthOffset, daysInMonth, hijriOffsetDays)
            }
            val titleLabel = if (isHijriMode) hijriSubtitle else gregTitle
            AlertDialog(
                onDismissRequest = { showImsakiyah = false },
                containerColor = theme.bgCard,
                title = {
                    Text(
                        text = sakinaTr(lang, "إمساكية $titleLabel", "$titleLabel Timetable", "Horaires $titleLabel", "$titleLabel İmsakiye", "Jadwal $titleLabel", "امساکیہ $titleLabel"),
                        fontFamily = SakinaHeadingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = theme.accentBright
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 390.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0x28FFFFFF)).padding(vertical = 4.dp, horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                sakinaTr(lang, "اليوم", "D", "J", "G", "H", "دن"),
                                sakinaTr(lang, "فجر", "Fajr", "Fajr", "İms", "Sub", "فجر"),
                                sakinaTr(lang, "ظهر", "Dhr", "Doh", "Öğl", "Dzh", "ظہر"),
                                sakinaTr(lang, "عصر", "Asr", "Asr", "İkn", "Ash", "عصر"),
                                sakinaTr(lang, "مغرب", "Mgh", "Mgh", "Akş", "Mgh", "مغرب"),
                                sakinaTr(lang, "عشاء", "Ish", "Ich", "Yat", "Isy", "عشاء")
                            ).forEach { hLbl ->
                                Text(hLbl, fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = theme.accentBright, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }
                        }
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            items(monthRows) { r ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp, horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(SakinaAdvancedPrefs.formatDigits(ctx, "${r.dayNum}"), fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = theme.accentBright, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text(r.fajr, fontFamily = SakinaNumeralFont, fontSize = 10.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text(r.dhuhr, fontFamily = SakinaNumeralFont, fontSize = 10.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text(r.asr, fontFamily = SakinaNumeralFont, fontSize = 10.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text(r.maghrib, fontFamily = SakinaNumeralFont, fontSize = 10.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text(r.isha, fontFamily = SakinaNumeralFont, fontSize = 10.sp, color = SakinaBrandColors.LuminousIvory, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                }
                                HorizontalDivider(color = Color(0x18FFFFFF))
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { shareImsakiyahAsPng(ctx, lang, titleLabel, monthRows) },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentGold)
                    ) {
                        Text(sakinaTr(lang, "مشاركة PNG", "Share PNG", "Partager PNG", "PNG Paylaş", "Bagikan PNG", "PNG شیئر"), fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, color = theme.bgDeep)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImsakiyah = false }) {
                        Text(sakinaTr(lang, "إغلاق", "Close", "Fermer", "Kapat", "Tutup", "بند کریں"), fontFamily = SakinaBodyFont, color = SakinaBrandColors.LuminousIvory)
                    }
                }
            )
        }

        // 1. Luxury Ornate Monthly Calendar Card (#7)

}

// v28 #04: Monthly Imsakiyah Data & High-DPI PNG Generator + Share
private data class ImsakiyahRow(
    val dayNum: Int,
    val gregShort: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

private fun buildMonthlyImsakiyah(
    ctx: Context,
    isHijriMode: Boolean,
    monthOffset: Int,
    daysInMonth: Int,
    hijriOffsetDays: Int
): List<ImsakiyahRow> {
    val result = mutableListOf<ImsakiyahRow>()
    val shortGreg = SimpleDateFormat("dd/MM", Locale.US)
    val coords = Place.coords(ctx)
    val params = Prefs.params(ctx)
    val baseGreg = Calendar.getInstance().apply {
        add(Calendar.MONTH, monthOffset)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val baseHijri = IslamicCalendar().apply {
        add(IslamicCalendar.DAY_OF_MONTH, hijriOffsetDays)
        add(IslamicCalendar.MONTH, monthOffset)
        set(IslamicCalendar.DAY_OF_MONTH, 1)
    }
    for (d in 1..daysInMonth) {
        val cal = Calendar.getInstance()
        if (isHijriMode) {
            val h = IslamicCalendar().apply {
                set(baseHijri.get(IslamicCalendar.YEAR), baseHijri.get(IslamicCalendar.MONTH), d)
                add(IslamicCalendar.DAY_OF_MONTH, -hijriOffsetDays)
            }
            cal.timeInMillis = h.timeInMillis
        } else {
            cal.set(baseGreg.get(Calendar.YEAR), baseGreg.get(Calendar.MONTH), d)
        }
        val pt = PrayerTimes(coords, DateComponents.from(cal.time), params)
        fun fmt(base: Date, keyAr: String?): String {
            val off = if (keyAr != null) Offsets.get(ctx, keyAr) * 60_000L else 0L
            return SakinaAdvancedPrefs.formatPrayerTime(ctx, Date(base.time + off))
        }
        result.add(
            ImsakiyahRow(
                dayNum = d,
                gregShort = SakinaAdvancedPrefs.formatDigits(ctx, shortGreg.format(cal.time)),
                fajr = fmt(pt.fajr, "الفجر"),
                sunrise = fmt(pt.sunrise, null),
                dhuhr = fmt(pt.dhuhr, "الظهر"),
                asr = fmt(pt.asr, "العصر"),
                maghrib = fmt(pt.maghrib, "المغرب"),
                isha = fmt(pt.isha, "العشاء")
            )
        )
    }
    return result
}

private fun shareImsakiyahAsPng(
    ctx: Context,
    lang: SakinaLang,
    monthTitle: String,
    rows: List<ImsakiyahRow>
) {
    try {
        val w = 1080
        val rowH = 44
        val headerH = 220
        val footerH = 120
        val h = headerH + (rows.size + 1) * rowH + footerH
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // Deep emerald gradient background
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(0xFF05241C.toInt(), 0xFF0B3B2E.toInt(), 0xFF05241C.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)

        // Gold outer frame
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = 0xAAD4AF37.toInt()
        }
        c.drawRoundRect(RectF(20f, 20f, w - 20f, h - 20f), 28f, 28f, framePaint)

        // Title & Subtitle
        val titleP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE5C158.toInt()
            textSize = 46f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val subP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD1FAE5.toInt()
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        val city = localizeCityName(Place.name(ctx), lang)
        val method = localizeMethodName(Prefs.methodLabel(ctx), lang)
        c.drawText(
            sakinaTr(lang, "إمساكية $monthTitle", "$monthTitle Timetable", "Horaires $monthTitle", "$monthTitle İmsakiye", "Jadwal $monthTitle", "امساکیہ $monthTitle"),
            w / 2f, 92f, titleP
        )
        c.drawText("$city · $method", w / 2f, 144f, subP)

        // Table Header
        val colsX = floatArrayOf(960f, 820f, 680f, 540f, 400f, 260f, 120f)
        val headers = listOf(
            sakinaTr(lang, "اليوم", "Day", "Jour", "Gün", "Hari", "دن"),
            sakinaTr(lang, "الفجر", "Fajr", "Fajr", "İmsak", "Subuh", "فجر"),
            sakinaTr(lang, "الشروق", "Sun", "Lev.", "Güneş", "Syuruq", "طلوع"),
            sakinaTr(lang, "الظهر", "Dhuhr", "Dohr", "Öğle", "Dzuhur", "ظہر"),
            sakinaTr(lang, "العصر", "Asr", "Asr", "İkindi", "Ashar", "عصر"),
            sakinaTr(lang, "المغرب", "Magh.", "Magh.", "Akşam", "Magh.", "مغرب"),
            sakinaTr(lang, "العشاء", "Isha", "Icha", "Yatsı", "Isya", "عشاء")
        )
        val hdrBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF145343.toInt() }
        c.drawRect(36f, headerH - 36f, w - 36f, headerH + 10f, hdrBg)

        val hdrTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE5C158.toInt()
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        headers.forEachIndexed { idx, lbl -> c.drawText(lbl, colsX[idx], headerH - 6f, hdrTxt) }

        val rowAlt = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x18FFFFFF }
        val cellTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF9FAFB.toInt()
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        val dayTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFA7F3D0.toInt()
            textSize = 21f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        rows.forEachIndexed { i, r ->
            val yTop = headerH + 12f + i * rowH
            if (i % 2 == 1) c.drawRect(36f, yTop, w - 36f, yTop + rowH, rowAlt)
            val yBase = yTop + 30f
            c.drawText("${SakinaAdvancedPrefs.formatDigits(ctx, r.dayNum.toString())} (${r.gregShort})", colsX[0], yBase, dayTxt)
            c.drawText(r.fajr, colsX[1], yBase, cellTxt)
            c.drawText(r.sunrise, colsX[2], yBase, cellTxt)
            c.drawText(r.dhuhr, colsX[3], yBase, cellTxt)
            c.drawText(r.asr, colsX[4], yBase, cellTxt)
            c.drawText(r.maghrib, colsX[5], yBase, cellTxt)
            c.drawText(r.isha, colsX[6], yBase, cellTxt)
        }

        // Footer branding
        val footP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE5C158.toInt()
            textSize = 26f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        c.drawText(
            sakinaTr(lang, "تطبيق سَكِينة · أوفلاين ومجاني 100% بدون إعلانات", "Sakina App · 100% Offline & Ad-Free", "Application Sakina · 100% Hors-ligne & Sans Pub", "Sakina · %100 Çevrimdışı ve Reklamsız", "Sakina · 100% Offline & Tanpa Iklan", "سکینہ ایپ · 100% آف لائن، بلا اشتہار"),
            w / 2f, h - 52f, footP
        )

        // Save via MediaStore (zero permissions on Android 10+) and share
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "sakina_imsakiyah_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Sakina")
            }
        }
        val uri = ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            ctx.contentResolver.openOutputStream(uri)?.use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(Intent.createChooser(share, monthTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            Toast.makeText(ctx, sakinaTr(lang, "تعذّر حفظ الصورة", "Could not save image", "Impossible d'enregistrer l'image", "Görsel kaydedilemedi", "Gagal menyimpan gambar", "تصویر محفوظ نہ ہو سکی"), Toast.LENGTH_SHORT).show()
        }
    } catch (_: Exception) {
        Toast.makeText(ctx, sakinaTr(lang, "تعذّر مشاركة الصورة", "Could not share image", "Impossible de partager", "Paylaşılamadı", "Gagal membagikan", "شیئر نہیں ہو سکا"), Toast.LENGTH_SHORT).show()
    }
}
