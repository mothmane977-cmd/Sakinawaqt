package app.sakina

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * المرحلة السادسة (v68) — الأربعون النووية + الرقية الشرعية الموثّقة (ارقِ نفسك / ارقِ غيرك):
 * - 100% أوفلاين بدون إنترنت
 * - تخريج علمي موثّق + شرح غريب الألفاظ + عدّاد تكرار تفاعلي + شريط تقدّم مئوي + تكبير خط + نسخ ومشاركة
 */
data class NawawiHadith(
    val number: Int,
    val title: String,
    val text: String,
    val source: String
)

data class RuqyahProEntry(
    val id: String,
    val section: Int, // 0=إرشادات، 1=ارقِ نفسك، 2=ارقِ غيرك
    val title: String,
    val text: String,
    val targetCount: Int,
    val takhrij: String,
    val gharib: String
)

object SakinaNawawiRuqyahRepo {
    val NAWAWI_HADITHS: List<NawawiHadith> = listOf(
        NawawiHadith(1, "إنما الأعمال بالنيات", "عن أمير المؤمنين أبي حفص عمر بن الخطاب رضي الله عنه قال: سمعت رسول الله ﷺ يقول: «إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى، فمن كانت هجرته إلى الله ورسوله فهجرته إلى الله ورسوله، ومن كانت هجرته لدنيا يصيبها أو امرأة ينكحها فهجرته إلى ما هاجر إليه».", "رواه البخاري ومسلم"),
        NawawiHadith(2, "مراتب الدين (الإسلام والإيمان والإحسان)", "حديث جبريل المشهور حين سأل النبي ﷺ عن الإسلام والإيمان والإحسان وعلامات الساعة، فقال ﷺ في الإحسان: «أن تعبد الله كأنك تراه، فإن لم تكن تراه فإنه يراك».", "رواه مسلم"),
        NawawiHadith(3, "أركان الإسلام الخمسة", "عن ابن عمر رضي الله عنهما قال: سمعت رسول الله ﷺ يقول: «بُني الإسلام على خمس: شهادة أن لا إله إلا الله وأن محمداً رسول الله، وإقام الصلاة، وإيتاء الزكاة، وحج البيت، وصوم رمضان».", "رواه البخاري ومسلم"),
        NawawiHadith(4, "مراحل خلق الإنسان والخاتمة", "عن ابن مسعود رضي الله عنه قال: حدثنا رسول الله ﷺ وهو الصادق المصدوق: «إن أحدكم يُجمع خلقه في بطن أمه أربعين يوماً نطفة، ثم يكون علقة مثل ذلك، ثم يكون مضغة مثل ذلك، ثم يُرسل إليه الملك فينفخ فيه الروح ويُؤمر بأربع كلمات: بكتب رزقه وأجله وعمله وشقي أو سعيد...».", "رواه البخاري ومسلم"),
        NawawiHadith(5, "ردّ البدع والمحدثات", "عن أم المؤمنين عائشة رضي الله عنها قالت: قال رسول الله ﷺ: «من أحدث في أمرنا هذا ما ليس منه فهو ردّ».", "رواه البخاري ومسلم"),
        NawawiHadith(6, "الحلال بيّن والحرام بيّن", "عن النعمان بن بشير رضي الله عنهما قال: سمعت رسول الله ﷺ يقول: «إن الحلال بيّن وإن الحرام بيّن، وبينهما أمور مشتبهات لا يعلمهن كثير من الناس... ألا وإن في الجسد مضغة إذا صلحت صلح الجسد كله، وإذا فسدت فسد الجسد كله، ألا وهي القلب».", "رواه البخاري ومسلم"),
        NawawiHadith(7, "الدين النصيحة", "عن تميم الداري رضي الله عنه أن النبي ﷺ قال: «الدين النصيحة». قلنا: لمن؟ قال: «لله، ولكتابه، ولرسوله، ولأئمة المسلمين وعامتهم».", "رواه مسلم"),
        NawawiHadith(8, "حرمة دم المسلم وماله", "عن ابن عمر رضي الله عنهما أن رسول الله ﷺ قال: «أُمرت أن أقاتل الناس حتى يشهدوا أن لا إله إلا الله وأن محمداً رسول الله، ويقيموا الصلاة، ويؤتوا الزكاة، فإذا فعلوا ذلك عصموا مني دماءهم وأموالهم إلا بحق الإسلام وحسابهم على الله تعالى».", "رواه البخاري ومسلم"),
        NawawiHadith(9, "التكليف بحسب الاستطاعة", "عن أبي هريرة رضي الله عنه قال: سمعت رسول الله ﷺ يقول: «ما نهيتكم عنه فاجتنبوه، وما أمرتكم به فأتوا منه ما استطعتم، فإنما أهلك الذين من قبلكم كثرة مسائلهم واختلافهم على أنبيائهم».", "رواه البخاري ومسلم"),
        NawawiHadith(10, "إن الله طيب لا يقبل إلا طيباً", "عن أبي هريرة رضي الله عنه قال: قال رسول الله ﷺ: «إن الله تعالى طيب لا يقبل إلا طيباً، وإن الله أمر المؤمنين بما أمر به المرسلين...».", "رواه مسلم"),
        NawawiHadith(11, "دع ما يريبك إلى ما لا يريبك", "عن الحسن بن علي رضي الله عنهما قال: حفظت من رسول الله ﷺ: «دع ما يريبك إلى ما لا يريبك».", "رواه الترمذي والنسائي — حديث حسن صحيح"),
        NawawiHadith(12, "ترك ما لا يعنيه", "عن أبي هريرة رضي الله عنه قال: قال رسول الله ﷺ: «من حسن إسلام المرء تركه ما لا يعنيه».", "رواه الترمذي وغيره — حسن"),
        NawawiHadith(13, "كمال الإيمان بمحبة الخير للناس", "عن أنس بن مالك رضي الله عنه عن النبي ﷺ قال: «لا يؤمن أحدكم حتى يحب لأخيه ما يحب لنفسه».", "رواه البخاري ومسلم"),
        NawawiHadith(14, "حفظ اللسان وإكرام الضيف والجار", "عن أبي هريرة رضي الله عنه أن رسول الله ﷺ قال: «من كان يؤمن بالله واليوم الآخر فليقل خيراً أو ليصمت، ومن كان يؤمن بالله واليوم الآخر فليكرم جاره، ومن كان يؤمن بالله واليوم الآخر فليكرم ضيفه».", "رواه البخاري ومسلم"),
        NawawiHadith(15, "الوصية بترك الغضب", "عن أبي هريرة رضي الله عنه أن رجلاً قال للنبي ﷺ: أوصني. قال: «لا تغضب». فردّد مراراً، قال: «لا تغضب».", "رواه البخاري"),
        NawawiHadith(16, "الإحسان في كل شيء", "عن شداد بن أوس رضي الله عنه عن رسول الله ﷺ قال: «إن الله كتب الإحسان على كل شيء...».", "رواه مسلم"),
        NawawiHadith(17, "التقوى وحسن الخلق", "عن أبي ذر ومعاذ بن جبل رضي الله عنهما عن رسول الله ﷺ قال: «اتق الله حيثما كنت، وأتبع السيئة الحسنة تمحها، وخالق الناس بخلق حسن».", "رواه الترمذي — حسن صحيح"),
        NawawiHadith(18, "احفظ الله يحفظك", "عن ابن عباس رضي الله عنهما قال: كنت خلف النبي ﷺ يوماً فقال: «يا غلام إني أعلمك كلمات: احفظ الله يحفظك، احفظ الله تجده تجاهك، إذا سألت فاسأل الله، وإذا استعنت فاستعن بالله...».", "رواه الترمذي — حسن صحيح"),
        NawawiHadith(19, "الحياء من الإيمان", "عن أبي مسعود البدري رضي الله عنه قال: قال رسول الله ﷺ: «إن مما أدرك الناس من كلام النبوة الأولى: إذا لم تستح فاصنع ما شئت».", "رواه البخاري"),
        NawawiHadith(20, "الاستقامة على الإيمان", "عن سفيان بن عبد الله الثقفي رضي الله عنه قال: قلت يا رسول الله قل لي في الإسلام قولاً لا أسأل عنه أحداً غيرك. قال: «قل آمنت بالله ثم استقم».", "رواه مسلم"),
        NawawiHadith(21, "الطهور شطر الإيمان", "عن أبي مالك الأشعري رضي الله عنه قال: قال رسول الله ﷺ: «الطهور شطر الإيمان، والحمد لله تملأ الميزان، وسبحان الله والحمد لله تملآن ما بين السماء والأرض، والصلاة نور، والصدقة برهان، والصبر ضياء، والقرآن حجة لك أو عليك».", "رواه مسلم"),
        NawawiHadith(22, "تحريم الظلم", "عن أبي ذر رضي الله عنه عن النبي ﷺ فيما يرويه عن ربه عز وجل: «يا عبادي إني حرمت الظلم على نفسي وجعلته بينكم محرماً فلا تظالموا...».", "رواه مسلم"),
        NawawiHadith(23, "البرّ حسن الخلق", "عن النواس بن سمعان رضي الله عنه عن النبي ﷺ قال: «البرّ حسن الخلق، والإثم ما حاك في نفسك وكرهت أن يطلع عليه الناس».", "رواه مسلم"),
        NawawiHadith(24, "الزهد في الدنيا", "عن سهل بن سعد الساعدي رضي الله عنه قال: جاء رجل إلى النبي ﷺ فقال: يا رسول الله دلّني على عمل إذا عملته أحبني الله وأحبني الناس. فقال: «ازهد في الدنيا يحبك الله، وازهد فيما عند الناس يحبك الناس».", "رواه ابن ماجه — حسن"),
        NawawiHadith(25, "لا ضرر ولا ضرار", "عن أبي سعيد الخدري رضي الله عنه أن رسول الله ﷺ قال: «لا ضرر ولا ضرار».", "رواه ابن ماجه والدارقطني — حسن"),
        NawawiHadith(26, "التنفيس عن المكروب وطلب العلم", "عن أبي هريرة رضي الله عنه عن النبي ﷺ قال: «من نفّس عن مؤمن كربة من كرب الدنيا نفّس الله عنه كربة من كرب يوم القيامة... ومن سلك طريقاً يلتمس فيه علماً سهّل الله له به طريقاً إلى الجنة».", "رواه مسلم"),
        NawawiHadith(27, "مضاعفة الحسنات وسعة رحمة الله", "عن ابن عباس رضي الله عنهما عن رسول الله ﷺ فيما يرويه عن ربه: «إن الله كتب الحسنات والسيئات ثم بيّن ذلك: فمن همّ بحسنة فلم يعملها كتبها الله عنده حسنة كاملة، وإن همّ بها فعملها كتبها الله عشر حسنات إلى سبعمائة ضعف...».", "رواه البخاري ومسلم"),
        NawawiHadith(28, "التقرب إلى الله بالنوافل", "عن أبي هريرة رضي الله عنه قال: قال رسول الله ﷺ: «إن الله تعالى قال: من عادى لي ولياً فقد آذنته بالحرب، وما تقرب إليّ عبدي بشيء أحب إليّ مما افترضت عليه، ولا يزال عبدي يتقرب إليّ بالنوافل حتى أحبه...».", "رواه البخاري"),
        NawawiHadith(29, "كن في الدنيا كأنك غريب", "عن ابن عمر رضي الله عنهما قال: أخذ رسول الله ﷺ بمنكبي فقال: «كن في الدنيا كأنك غريب أو عابر سبيل».", "رواه البخاري"),
        NawawiHadith(30, "سعة مغفرة الله بالتوبة والتوحيد", "عن أنس رضي الله عنه قال: سمعت رسول الله ﷺ يقول: «قال الله تعالى: يا ابن آدم إنك ما دعوتني ورجوتني غفرت لك على ما كان منك ولا أبالي... يا ابن آدم إنك لو أتيتني بقراب الأرض خطايا ثم لقيتني لا تشرك بي شيئاً لأتيتك بقرابها مغفرة».", "رواه الترمذي — حسن صحيح")
    )

    val RUQYAH_ENTRIES: List<RuqyahProEntry> = listOf(
        // قسم 0: إرشادات وآداب الرقية
        RuqyahProEntry("g1", 0, "اليقين بأن الشافي هو الله وحده", "الرقية سبب شرعي نافع بإذن الله، مع اليقين التام أن النفع والضر والشفاء بيد الله وحده لا شريك له.", 1, "متفق عليه من أصول التوحيد", "الرقية: العوذة التي يُرقى بها المريض بالقرآن والدعاء المأثور."),
        RuqyahProEntry("g2", 0, "الطهارة والنفث الخفيف", "يُستحب الوضوء واستقبال القبلة وقراءة الرقية بتدبر وحضور قلب، مع جمع الكفين والنفث الخفيف فيهما ومسح ما تيسر من الجسد.", 1, "ثبت عن النبي ﷺ في الصحيحين", "النفث: نفخ لطيف بلا ريق أو بريق يسير جداً."),
        // قسم 1: ارقِ نفسك
        RuqyahProEntry("s1", 1, "سورة الفاتحة (الشافية)", "﴿بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ۝ الرَّحْمَٰنِ الرَّحِيمِ ۝ مَالِكِ يَوْمِ الدِّينِ ۝ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ۝ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ۝ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ﴾.", 7, "متفق عليه — «وما يدريك أنها رقية»", "الصراط المستقيم: الطريق الواضح القويم وهو الإسلام."),
        RuqyahProEntry("s2", 1, "آية الكرسي (البقرة: 255)", "﴿اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ... وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ﴾.", 3, "رواه البخاري — حرز من الشيطان حتى يصبح", "سِنَة: النعاس الخفيف. لا يئوده: لا يثقله ولا يشق عليه."),
        RuqyahProEntry("s3", 1, "الإخلاص والمعوذتان", "قراءة سورة الإخلاص، وسورة الفلق، وسورة الناس ثلاث مرات مع النفث في الكفين ومسح الوجه والصدر وسائر الجسد.", 3, "رواه البخاري ومسلم عن عائشة رضي الله عنها", "غاسق إذا وقب: الليل إذا أظلم واشتد سواده. الوسواس الخناس: الشيطان المتواري عند ذكر الله."),
        RuqyahProEntry("s4", 1, "دعاء موضع الألم", "ضع يدك على الذي تألّم من جسدك وقل: «بِسْمِ اللَّهِ» (3 مرات)، ثم قل: «أَعُوذُ بِاللَّهِ وَقُدْرَتِهِ مِنْ شَرِّ مَا أَجِدُ وَأُحَاذِرُ».", 7, "رواه مسلم (2202)", "أحاذر: أخاف وأحذر من وقوعه أو استمراره."),
        RuqyahProEntry("s5", 1, "التحصين بكلمات الله التامات", "«أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ».", 3, "رواه مسلم (2708)", "التامات: الكاملات السالمات من أي نقص أو عيب."),
        // قسم 2: ارقِ غيرك
        RuqyahProEntry("o1", 2, "دعاء الشفاء العظيم للغير", "«أَسْأَلُ اللَّهَ الْعَظِيمَ رَبَّ الْعَرْشِ الْعَظِيمِ أَنْ يَشْفِيَكَ» (أو: أن يشفيكِ / أن يشفيه).", 7, "رواه أبو داود والترمذي — صححه الألباني", "العرش العظيم: أعظم مخلوقات الله تعالى وسقفها."),
        RuqyahProEntry("o2", 2, "رقية جبريل عليه السلام للغير", "«بِسْمِ اللَّهِ أَرْقِيكَ، مِنْ كُلِّ شَيْءٍ يُؤْذِيكَ، مِنْ شَرِّ كُلِّ نَفْسٍ أَوْ عَيْنِ حَاسِدٍ، اللَّهُ يَشْفِيكَ، بِسْمِ اللَّهِ أَرْقِيكَ».", 3, "رواه مسلم (2186)", "أرقيك: أعوّذك وأدعو الله لك بالشفاء والعافية."),
        RuqyahProEntry("o3", 2, "دعاء إذهاب البأس للغير", "«اللَّهُمَّ رَبَّ النَّاسِ، أَذْهِبِ الْبَاسَ، اشْفِهِ وَأَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَماً».", 3, "رواه البخاري ومسلم", "الباس: الشدة والألم والمرض. لا يغادر سقماً: لا يترك أثراً للمرض."),
        RuqyahProEntry("o4", 2, "تعويذ الأبناء والأهل", "«أُعِيذُكَ بِكَلِمَاتِ اللَّهِ التَّامَّةِ، مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ، وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ».", 3, "رواه البخاري (3371)", "هامة: كل ذي سمّ قاتل. عين لامة: عين مصيبة بسوء.")
    )
}

@Composable
fun Nawawi40OverlayDialog(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    var selectedIdx by remember { mutableIntStateOf(-1) }
    var fontSp by remember { mutableIntStateOf(15) }
    val list = SakinaNawawiRuqyahRepo.NAWAWI_HADITHS
    val velvetBg = remember { Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.5f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF061B18),
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (selectedIdx < 0) "الأربعون النووية" else "حديث ${list[selectedIdx].number}",
                    color = SakinaLuxuryColors.SoftGoldGlow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = headingFontFamily
                )
                if (selectedIdx >= 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("A-", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp - 2).coerceAtLeast(12) }.padding(3.dp))
                        Text("A+", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp + 2).coerceAtMost(24) }.padding(3.dp))
                        Text("الفهرس", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { selectedIdx = -1 }.padding(3.dp))
                    }
                }
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(430.dp)) {
                if (selectedIdx < 0) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
                        items(list, key = { it.number }) { h ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(velvetBg)
                                    .border(0.8.dp, goldBorder, RoundedCornerShape(14.dp))
                                    .clickable { selectedIdx = list.indexOf(h) }
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${h.number}. ${h.title}", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily, modifier = Modifier.weight(1f))
                                Text("←", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    val h = list[selectedIdx]
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(h.title, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Text(h.text, color = SakinaOrbitalColors.WarmCream, fontSize = fontSp.sp, lineHeight = (fontSp + 12).sp, fontFamily = bodyFontFamily, textAlign = TextAlign.Justify)
                        Text("التخريج: ${h.source}", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = bodyFontFamily)
                        HorizontalDivider(color = goldBorder.copy(alpha = 0.35f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text("📋 نسخ", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Hadith", "${h.title}\n${h.text}\n(${h.source})"))
                            }.padding(6.dp))
                            Text("↗ مشاركة", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                                val sh = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${h.title}\n\n${h.text}\n\n(${h.source}) — نور قنوت")
                                }
                                ctx.startActivity(Intent.createChooser(sh, "مشاركة الحديث"))
                            }.padding(6.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("< السابق", color = if (selectedIdx > 0) SakinaLuxuryColors.SoftGoldGlow else Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(enabled = selectedIdx > 0) { selectedIdx-- }.padding(6.dp))
                            Text("التالي >", color = if (selectedIdx < list.lastIndex) SakinaLuxuryColors.SoftGoldGlow else Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(enabled = selectedIdx < list.lastIndex) { selectedIdx++ }.padding(6.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold) } }
    )
}

@Composable
fun RuqyahProOverlayDialog(
    headingFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var secTab by remember { mutableIntStateOf(1) } // 0=إرشادات، 1=ارقِ نفسك، 2=ارقِ غيرك
    var fontSp by remember { mutableIntStateOf(14) }
    val counts = remember { mutableStateMapOf<String, Int>() }
    val entries = remember(secTab) { SakinaNawawiRuqyahRepo.RUQYAH_ENTRIES.filter { it.section == secTab } }
    val totalTarget = entries.sumOf { it.targetCount }.coerceAtLeast(1)
    val totalDone = entries.sumOf { (counts[it.id] ?: 0).coerceAtMost(it.targetCount) }
    val progress = (totalDone.toFloat() / totalTarget.toFloat()).coerceIn(0f, 1f)
    val velvetBg = remember { Brush.verticalGradient(listOf(Color(0xFF072821), Color(0xFF051A18))) }
    val goldBorder = SakinaLuxuryColors.ChampagneGold.copy(alpha = 0.5f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF061B18),
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("الرقية الشرعية الموثّقة", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("A-", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp - 1).coerceAtLeast(12) }.padding(3.dp))
                    Text("A+", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { fontSp = (fontSp + 1).coerceAtMost(22) }.padding(3.dp))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(440.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("إرشادات", "ارقِ نفسك", "ارقِ غيرك").forEachIndexed { idx, lbl ->
                        val sel = secTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (sel) SakinaLuxuryPalette.MetallicGoldButtonBrush else SolidColor(Color(0xFF041716)))
                                .border(0.75.dp, goldBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { secTab = idx }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(lbl, color = if (sel) Color(0xFF071A18) else SakinaOrbitalColors.WarmCream, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("التقدم في القسم", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.75f), fontSize = 10.5.sp)
                    Text("${(progress * 100).toInt()}%", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaNumeralFont)
                }
                LinearProgressIndicator(
                    progress = { progress },
                    color = SakinaLuxuryColors.SoftGoldGlow,
                    trackColor = Color(0xFF10382F),
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
                    items(entries, key = { it.id }) { e ->
                        val cur = counts[e.id] ?: 0
                        val done = cur >= e.targetCount
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(velvetBg)
                                .border(if (done) 1.2.dp else 0.8.dp, if (done) SakinaLuxuryColors.EmeraldGlow else goldBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    counts[e.id] = (cur + 1).coerceAtMost(e.targetCount)
                                }
                                .padding(11.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(e.title, color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, fontFamily = headingFontFamily, modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (done) Color(0xFF14644F) else Color(0xFF041716))
                                        .border(0.7.dp, SakinaLuxuryColors.SoftGoldGlow, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(if (done) "✓ تم" else "$cur / ${e.targetCount}", color = SakinaLuxuryColors.SoftGoldGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = SakinaNumeralFont)
                                }
                            }
                            Text(e.text, color = SakinaOrbitalColors.WarmCream, fontSize = fontSp.sp, lineHeight = (fontSp + 10).sp, fontFamily = bodyFontFamily)
                            Text("التخريج: ${e.takhrij}", color = SakinaLuxuryColors.EmeraldGlow, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Text("غريب الألفاظ: ${e.gharib}", color = SakinaOrbitalColors.WarmCream.copy(alpha = 0.78f), fontSize = 10.5.sp, fontFamily = bodyFontFamily)
                            Text(
                                text = "↗ مشاركة",
                                color = SakinaLuxuryColors.SoftGoldGlow,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.End).clickable {
                                    val sh = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "${e.title}\n${e.text}\n(${e.takhrij}) — نور قنوت")
                                    }
                                    ctx.startActivity(Intent.createChooser(sh, "مشاركة الرقية"))
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق", color = SakinaLuxuryColors.SoftGoldGlow, fontWeight = FontWeight.Bold) } }
    )
}
