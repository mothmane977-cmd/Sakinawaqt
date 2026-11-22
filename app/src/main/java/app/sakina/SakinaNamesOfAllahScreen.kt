package app.sakina

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DivineNameEntry(
    val number: Int,
    val arabic: String,
    val translit: String,
    val meaningAr: String,
    val meaningEn: String,
    val meaningFr: String
)

private val SAKINA_99_NAMES = listOf(
    DivineNameEntry(1, "الرَّحْمَنُ", "Ar-Rahman", "ذو الرحمة الواسعة الشاملة لجميع الخلائق", "The Most Merciful", "Le Tout-Miséricordieux"),
    DivineNameEntry(2, "الرَّحِيمُ", "Ar-Rahim", "الرحيم بعباده المؤمنين في الدنيا والآخرة", "The Especially Merciful", "Le Très-Miséricordieux"),
    DivineNameEntry(3, "المَلِكُ", "Al-Malik", "المالك لكل شيء المتصرف في ملكه بلا منازع", "The Sovereign", "Le Souverain"),
    DivineNameEntry(4, "القُدُّوسُ", "Al-Quddus", "المنزّه عن كل نقص وعيب", "The Pure & Holy", "Le Saint"),
    DivineNameEntry(5, "السَّلَامُ", "As-Salam", "السالم من كل نقص، وواهب السلام والأمان", "The Source of Peace", "La Paix"),
    DivineNameEntry(6, "المُؤْمِنُ", "Al-Mu'min", "المصدّق لرسله والواهب للأمن والطمأنينة", "The Giver of Security", "Le Rassurant"),
    DivineNameEntry(7, "المُهَيْمِنُ", "Al-Muhaymin", "الرقيب الحافظ الشاهد على كل شيء", "The Guardian", "Le Gardien"),
    DivineNameEntry(8, "العَزِيزُ", "Al-Aziz", "القوي الغالب الذي لا يُقهر", "The Almighty", "Le Tout-Puissant"),
    DivineNameEntry(9, "الجَبَّارُ", "Al-Jabbar", "جابر القلوب المنكسرة والقاهر بقدرته", "The Compeller & Restorer", "Le Contraignant"),
    DivineNameEntry(10, "المُتَكَبِّرُ", "Al-Mutakabbir", "المتعالي عن صفات الخلق ذو الكبرياء", "The Supreme", "Le Superbe"),
    DivineNameEntry(11, "الخَالِقُ", "Al-Khaliq", "المبدع للخلق من العدم على غير مثال سابق", "The Creator", "Le Créateur"),
    DivineNameEntry(12, "البَارِئُ", "Al-Bari'", "الموجد للخلائق المميّز بعضها عن بعض", "The Evolver", "Le Producteur"),
    DivineNameEntry(13, "المُصَوِّرُ", "Al-Musawwir", "الذي صوّر المخلوقات في أحسن صورة", "The Fashioner", "Le Façonneur"),
    DivineNameEntry(14, "الغَفَّارُ", "Al-Ghaffar", "كثير المغفرة والستر لذنوب عباده", "The Repeatedly Forgiving", "Le Grand Pardonneur"),
    DivineNameEntry(15, "القَهَّارُ", "Al-Qahhar", "الغالب الذي خضع له كل شيء", "The Subduer", "Le Dominateur"),
    DivineNameEntry(16, "الوَهَّابُ", "Al-Wahhab", "كثير العطاء بلا عوض ولا منّة", "The Bestower", "Le Donateur"),
    DivineNameEntry(17, "الرَّزَّاقُ", "Ar-Razzaq", "المتكفل بأرزاق جميع المخلوقات", "The Provider", "Le Pourvoyeur"),
    DivineNameEntry(18, "الفَتَّاحُ", "Al-Fattah", "الذي يفتح أبواب الرحمة والرزق والنصر", "The Opener", "Celui qui ouvre"),
    DivineNameEntry(19, "العَلِيمُ", "Al-Alim", "المحيط علمه بكل ظاهر وباطن", "The All-Knowing", "L'Omniscient"),
    DivineNameEntry(20, "القَابِضُ", "Al-Qabid", "الذي يقبض الرزق والأرواح بحكمته", "The Withholder", "Celui qui retient"),
    DivineNameEntry(21, "البَاسِطُ", "Al-Basit", "الذي يوسع الرزق والرحمة لمن يشاء", "The Expander", "Celui qui étend"),
    DivineNameEntry(22, "الخَافِضُ", "Al-Khafid", "الذي يخفض الجبارين والطغاة", "The Abaser", "Celui qui abaisse"),
    DivineNameEntry(23, "الرَّافِعُ", "Ar-Rafi'", "الذي يرفع أولياءه بالطاعة والعلم", "The Exalter", "Celui qui élève"),
    DivineNameEntry(24, "المُعِزُّ", "Al-Mu'izz", "واهب العزة والرفعة لمن يشاء", "The Honorer", "Celui qui honore"),
    DivineNameEntry(25, "المُذِلُّ", "Al-Mudhill", "الذي يذل من استكبر وعاند الحق", "The Humiliator", "Celui qui humilie"),
    DivineNameEntry(26, "السَّمِيعُ", "As-Sami'", "الذي يسمع كل صوت مهما خفي", "The All-Hearing", "L'Audient"),
    DivineNameEntry(27, "البَصِيرُ", "Al-Basir", "الذي يرى كل شيء لا يخفى عليه خافية", "The All-Seeing", "Le Clairvoyant"),
    DivineNameEntry(28, "الحَكَمُ", "Al-Hakam", "الذي يحكم بين عباده بالعدل المطلق", "The Judge", "Le Juge"),
    DivineNameEntry(29, "العَدْلُ", "Al-Adl", "المنزّه عن الظلم، العادل في قضائه", "The Utterly Just", "Le Juste"),
    DivineNameEntry(30, "اللَّطِيفُ", "Al-Latif", "الرفيق بعباده، المدبّر لأمورهم بلطف خفي", "The Subtle & Kind", "Le Subtil"),
    DivineNameEntry(31, "الخَبِيرُ", "Al-Khabir", "العالم ببواطن الأمور وخفايا القلوب", "The All-Aware", "Le Bien-Informé"),
    DivineNameEntry(32, "الحَلِيمُ", "Al-Halim", "الذي لا يعاجل عباده بالعقوبة مع قدرته", "The Forbearing", "L'Indulgent"),
    DivineNameEntry(33, "العَظِيمُ", "Al-Azim", "ذو العظمة والجلال الذي لا يُحدّ", "The Magnificent", "L'Immense"),
    DivineNameEntry(34, "الغَفُورُ", "Al-Ghafur", "واسع المغفرة والتجاوز عن السيئات", "The All-Forgiving", "Le Pardonneur"),
    DivineNameEntry(35, "الشَّكُورُ", "Ash-Shakur", "الذي يجازي على العمل القليل بالثواب الجزيل", "The Appreciative", "Le Reconnaissant"),
    DivineNameEntry(36, "العَلِيُّ", "Al-Aliyy", "الأعلى قدراً وذاتاً وقهراً فوق خلقه", "The Most High", "Le Très-Haut"),
    DivineNameEntry(37, "الكَبِيرُ", "Al-Kabir", "الأكبر من كل شيء في عظمته وكبريائه", "The Greatest", "Le Grand"),
    DivineNameEntry(38, "الحَفِيظُ", "Al-Hafiz", "الحافظ للكون ولعباده وأعمالهم", "The Preserver", "Le Préservateur"),
    DivineNameEntry(39, "المُقِيتُ", "Al-Muqit", "خالق الأقوات وموصلها إلى الأبدان والأرواح", "The Sustainer", "Le Nourricier"),
    DivineNameEntry(40, "الحَسِيبُ", "Al-Hasib", "الكافي لعباده والمحاسب لهم على أعمالهم", "The Reckoner", "Celui qui suffit"),
    DivineNameEntry(41, "الجَلِيلُ", "Al-Jalil", "المتصف بصفات الجلال والعظمة والكمال", "The Majestic", "Le Majestueux"),
    DivineNameEntry(42, "الكَرِيمُ", "Al-Karim", "كثير الخير والجود والعفو والتفضل", "The Generous", "Le Généreux"),
    DivineNameEntry(43, "الرَّقِيبُ", "Ar-Raqib", "المطّلع على كل حركة وسكون وخواطر الصدور", "The Watchful", "L'Observateur"),
    DivineNameEntry(44, "المُجِيبُ", "Al-Mujib", "الذي يجيب دعوة الداعي إذا دعاه", "The Responsive", "Celui qui exauce"),
    DivineNameEntry(45, "الوَاسِعُ", "Al-Wasi'", "واسع الرحمة والعلم والغنى والفضل", "The All-Encompassing", "L'Immense"),
    DivineNameEntry(46, "الحَكِيمُ", "Al-Hakim", "الذي يضع كل شيء في موضعه بحكمة بالغة", "The All-Wise", "Le Sage"),
    DivineNameEntry(47, "الوَدُودُ", "Al-Wadud", "المحبّ لعباده الصالحين والمحبوب في قلوبهم", "The Most Loving", "Le Bien-Aimant"),
    DivineNameEntry(48, "المَجِيدُ", "Al-Majid", "ذو المجد والشرف والكرم الواسع", "The Glorious", "Le Glorieux"),
    DivineNameEntry(49, "البَاعِثُ", "Al-Ba'ith", "الذي يبعث الخلق يوم القيامة للحساب", "The Resurrector", "Celui qui ressuscite"),
    DivineNameEntry(50, "الشَّهِيدُ", "Ash-Shahid", "الحاضر المطّلع الذي لا يغيب عنه شيء", "The Witness", "Le Témoin"),
    DivineNameEntry(51, "الحَقُّ", "Al-Haqq", "الثابت وجوده، الصادق وعده وقوله", "The Absolute Truth", "La Vérité"),
    DivineNameEntry(52, "الوَكِيلُ", "Al-Wakil", "المتكفل بأمور من توكل عليه وفوّض أمره إليه", "The Trustee", "Le Garant"),
    DivineNameEntry(53, "القَوِيُّ", "Al-Qawiyy", "ذو القوة التامة التي لا يعتريها ضعف", "The All-Strong", "Le Fort"),
    DivineNameEntry(54, "المَتِينُ", "Al-Matin", "الشديد القوة الذي لا يلحقه مشقة ولا تعب", "The Firm", "Le Ferme"),
    DivineNameEntry(55, "الوَلِيُّ", "Al-Waliyy", "الناصر والمعين والمحب لعباده المؤمنين", "The Protecting Friend", "Le Protecteur"),
    DivineNameEntry(56, "الحَمِيدُ", "Al-Hamid", "المستحق للحمد والثناء في كل حال", "The Praiseworthy", "Le Digne de louange"),
    DivineNameEntry(57, "المُحْصِي", "Al-Muhsi", "الذي أحصى كل شيء عدداً وعلماً", "The Accounter", "Celui qui dénombre"),
    DivineNameEntry(58, "المُبْدِئُ", "Al-Mubdi'", "الذي أنشأ الخلق وبدأه من العدم", "The Originator", "Celui qui crée en premier"),
    DivineNameEntry(59, "المُعِيدُ", "Al-Mu'id", "الذي يعيد الخلق بعد الموت كما بدأهم", "The Restorer", "Celui qui ramène"),
    DivineNameEntry(60, "المُحْيِي", "Al-Muhyi", "واهب الحياة للأجساد والقلوب", "The Giver of Life", "Celui qui donne la vie"),
    DivineNameEntry(61, "المُمِيتُ", "Al-Mumit", "الذي يقدّر الموت على كل حيّ", "The Bringer of Death", "Celui qui donne la mort"),
    DivineNameEntry(62, "الحَيُّ", "Al-Hayy", "الدائم الحياة الذي لا يموت أبداً", "The Ever-Living", "Le Vivant"),
    DivineNameEntry(63, "القَيُّومُ", "Al-Qayyum", "القائم بنفسه والمقيم لخلقه لا تأخذه سنة ولا نوم", "The Sustainer of All", "L'Immuable"),
    DivineNameEntry(64, "الوَاجِدُ", "Al-Wajid", "الغني الذي لا يفتقر إلى شيء ويجد ما يريد", "The Perceiver", "Celui qui existe"),
    DivineNameEntry(65, "المَاجِدُ", "Al-Majid", "العظيم الشأن كثير الإحسان والكرم", "The Noble", "Le Noble"),
    DivineNameEntry(66, "الوَاحِدُ", "Al-Wahid", "المتفرد في ذاته وصفاته وأفعاله بلا شريك", "The One", "L'Unique"),
    DivineNameEntry(67, "الأَحَدُ", "Al-Ahad", "المتوحّد بالكمال الذي لا نظير له ولا مثيل", "The Only One", "L'Un"),
    DivineNameEntry(68, "الصَّمَدُ", "As-Samad", "السيد المقصود في جميع الحوائج المستغني عن كل أحد", "The Eternal Refuge", "L'Absolu"),
    DivineNameEntry(69, "القَادِرُ", "Al-Qadir", "الذي لا يعجزه شيء في الأرض ولا في السماء", "The Capable", "Le Capable"),
    DivineNameEntry(70, "المُقْتَدِرُ", "Al-Muqtadir", "التام القدرة الذي ينفّذ ما يشاء بلا مانع", "The All-Powerful", "Le Tout-Puissant"),
    DivineNameEntry(71, "المُقَدِّمُ", "Al-Muqaddim", "الذي يقدّم الأشياء والأشخاص بحكمته", "The Expediter", "Celui qui avance"),
    DivineNameEntry(72, "المُؤَخِّرُ", "Al-Mu'akhkhir", "الذي يؤخر ما يشاء بحكمته وعدله", "The Delayer", "Celui qui retarde"),
    DivineNameEntry(73, "الأَوَّلُ", "Al-Awwal", "السابق لكل شيء فليس قبله شيء", "The First", "Le Premier"),
    DivineNameEntry(74, "الآخِرُ", "Al-Akhir", "الباقي بعد فناء كل شيء فليس بعده شيء", "The Last", "Le Dernier"),
    DivineNameEntry(75, "الظَّاهِرُ", "Az-Zahir", "العالي فوق كل شيء، الظاهر بآياته ودلائله", "The Manifest", "L'Apparent"),
    DivineNameEntry(76, "البَاطِنُ", "Al-Batin", "القريب المحيط ببواطن الأمور، ليس دونه شيء", "The Hidden", "Le Caché"),
    DivineNameEntry(77, "الوَالِي", "Al-Wali", "المالك المتصرف المدبّر لجميع شؤون الخلق", "The Governor", "Le Maître"),
    DivineNameEntry(78, "المُتَعَالِي", "Al-Muta'ali", "المنزّه عن صفات المخلوقين بعلوّ شأنه", "The Most Exalted", "Le Très-Élevé"),
    DivineNameEntry(79, "البَرُّ", "Al-Barr", "العطوف المحسن واسع البرّ بعباده", "The Source of Goodness", "Le Bienfaisant"),
    DivineNameEntry(80, "التَّوَّابُ", "At-Tawwab", "الذي يوفق عباده للتوبة ويقبلها منهم", "The Acceptor of Repentance", "Celui qui accepte le repentir"),
    DivineNameEntry(81, "المُنْتَقِمُ", "Al-Muntaqim", "المعاقب للظالمين والعصاة بعد إقامة الحجة", "The Avenger", "Le Vengeur"),
    DivineNameEntry(82, "العَفُوُّ", "Al-Afuww", "الذي يمحو الذنوب ويتجاوز عنها كأن لم تكن", "The Pardoner", "L'Indulgent"),
    DivineNameEntry(83, "الرَّؤُوفُ", "Ar-Ra'uf", "شديد الرحمة والرأفة بعباده", "The Most Compassionate", "Le Compatissant"),
    DivineNameEntry(84, "مَالِكُ المُلْكِ", "Malik-ul-Mulk", "المتصرف في الملك يؤتيه من يشاء وينزعه ممن يشاء", "Owner of All Sovereignty", "Possesseur de la Royauté"),
    DivineNameEntry(85, "ذُو الجَلَالِ وَالإِكْرَامِ", "Dhul-Jalali wal-Ikram", "المستحق للتعظيم والإجلال وصاحب الإكرام لأوليائه", "Lord of Majesty & Generosity", "Détenteur de Majesté et de Générosité"),
    DivineNameEntry(86, "المُقْسِطُ", "Al-Muqsit", "العادل المنصف الذي ينتصف للمظلوم من الظالم", "The Equitable", "L'Équitable"),
    DivineNameEntry(87, "الجَامِعُ", "Al-Jami'", "الجامع للخلق يوم القيامة والجامع للكمالات", "The Gatherer", "Le Rassembleur"),
    DivineNameEntry(88, "الغَنِيُّ", "Al-Ghaniyy", "المستغني عن كل خلقه، وكل خلقه مفتقر إليه", "The Self-Sufficient", "Le Riche"),
    DivineNameEntry(89, "المُغْنِي", "Al-Mughni", "الذي يغني من يشاء من عباده بفضله وكرمه", "The Enricher", "Celui qui enrichit"),
    DivineNameEntry(90, "المَانِعُ", "Al-Mani'", "الذي يمنع الأذى والضرر ويمنع العطاء بحكمته", "The Preventer", "Celui qui empêche"),
    DivineNameEntry(91, "الضَّارُّ", "Ad-Darr", "المقدّر للضرر ابتلاءً أو عقوبة بحكمته ولا يُذكر إلا مع النافع", "The Distresser (with An-Nafi')", "Celui qui éprouve"),
    DivineNameEntry(92, "النَّافِعُ", "An-Nafi'", "الموصل للنفع والخير لمن يشاء من عباده", "The Propitious", "Celui qui accorde le bien"),
    DivineNameEntry(93, "النُّورُ", "An-Nur", "نور السماوات والأرض، الهادي لقلوب عباده", "The Light", "La Lumière"),
    DivineNameEntry(94, "الهَادِي", "Al-Hadi", "الذي يهدي الخلق إلى مصالحهم وإلى الصراط المستقيم", "The Guide", "Le Guide"),
    DivineNameEntry(95, "البَدِيعُ", "Al-Badi'", "المبدع للكون على غير مثال سابق", "The Incomparable", "L'Inventeur"),
    DivineNameEntry(96, "البَاقِي", "Al-Baqi", "الدائم وجوده الذي لا يزول ولا يفنى", "The Everlasting", "Le Permanent"),
    DivineNameEntry(97, "الوَارِثُ", "Al-Warith", "الباقي بعد فناء الخلق وإليه يرجع كل شيء", "The Inheritor", "L'Héritier"),
    DivineNameEntry(98, "الرَّشِيدُ", "Ar-Rashid", "المرشد لعباده إلى الحق والصواب", "The Guide to the Right Path", "Le Guide vers la droiture"),
    DivineNameEntry(99, "الصَّبُورُ", "As-Sabur", "الذي لا يعجّل بالعقوبة على العصاة مع قدرته", "The Most Patient", "Le Patient")
)

@Composable
fun SakinaNamesOfAllahScreen(ctx: Context, onBack: () -> Unit) {
    val theme = LocalSakinaTheme.current
    val lang = LocalSakinaLang.current
    var query by remember { mutableStateOf("") }
    var gridMode by remember { mutableStateOf(false) }

    val filtered = remember(query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) SAKINA_99_NAMES
        else SAKINA_99_NAMES.filter {
            it.arabic.contains(q) ||
                it.meaningAr.contains(q) ||
                it.translit.lowercase().contains(q) ||
                it.meaningEn.lowercase().contains(q) ||
                it.meaningFr.lowercase().contains(q) ||
                it.number.toString() == q
        }
    }

    fun shareName(entry: DivineNameEntry) {
        val meaning = when (lang) {
            SakinaLang.FR -> entry.meaningFr
            SakinaLang.EN, SakinaLang.TR, SakinaLang.ID -> entry.meaningEn
            else -> entry.meaningAr
        }
        val text = "✨ ${entry.arabic} (${entry.translit})\n$meaning\n\n— تطبيق سَكِينة"
        try {
            val i = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(Intent.createChooser(i, entry.arabic).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = sakinaTr(lang, "← رجوع", "← Back", "← Retour", "← Geri", "← Kembali", "← واپس"),
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = theme.accentBright,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x22FFFFFF))
                    .clickable { onBack() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
            Text(
                text = sakinaTr(lang, "أسماء الله الحسنى", "99 Names of Allah", "Les 99 Noms d'Allah", "Esmaül Hüsna", "Asmaul Husna", "اسماء الحسنیٰ"),
                fontFamily = SakinaHeadingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = theme.accentBright
            )
            Text(
                text = if (gridMode) sakinaTr(lang, "قائمة", "List", "Liste", "Liste", "Daftar", "فہرست")
                else sakinaTr(lang, "شبكة", "Grid", "Grille", "Izgara", "Kisi", "گرڈ"),
                fontFamily = SakinaBodyFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = theme.bgDeep,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.accentGold)
                    .clickable { gridMode = !gridMode }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = {
                Text(
                    sakinaTr(lang, "ابحث باسم أو معنى...", "Search name or meaning...", "Rechercher un nom ou un sens...", "İsim veya anlam ara...", "Cari nama atau arti...", "نام یا معنی تلاش کریں..."),
                    fontFamily = SakinaBodyFont,
                    fontSize = 13.sp,
                    color = SakinaBrandColors.SoftSilver
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SakinaBrandColors.LuminousIvory,
                unfocusedTextColor = SakinaBrandColors.LuminousIvory,
                focusedBorderColor = theme.accentGold,
                unfocusedBorderColor = Color(0x44FFFFFF)
            )
        )

        Spacer(Modifier.height(10.dp))

        if (!gridMode) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered, key = { it.number }) { entry ->
                    val meaning = when (lang) {
                        SakinaLang.FR -> entry.meaningFr
                        SakinaLang.EN, SakinaLang.TR, SakinaLang.ID -> entry.meaningEn
                        else -> entry.meaningAr
                    }
                    SakinaGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.accentGold.copy(alpha = 0.22f))
                                    .border(1.dp, theme.accentGold.copy(alpha = 0.55f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    SakinaAdvancedPrefs.formatDigits(ctx, "${entry.number}"),
                                    fontFamily = SakinaNumeralFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = theme.accentBright
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    entry.arabic,
                                    fontFamily = SakinaHeadingFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp,
                                    color = theme.accentBright
                                )
                                if (lang != SakinaLang.AR) {
                                    Text(
                                        entry.translit,
                                        fontFamily = SakinaBodyFont,
                                        fontSize = 12.sp,
                                        color = SakinaBrandColors.SoftSilver
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    meaning,
                                    fontFamily = SakinaBodyFont,
                                    fontSize = 13.sp,
                                    color = SakinaBrandColors.LuminousIvory
                                )
                            }
                            Text(
                                text = sakinaTr(lang, "مشاركة", "Share", "Partager", "Paylaş", "Bagikan", "شیئر"),
                                fontFamily = SakinaBodyFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = theme.accentBright,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22FFFFFF))
                                    .clickable { shareName(entry) }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        } else {
            val rows = remember(filtered) { filtered.chunked(2) }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(rows) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { entry ->
                            val meaning = when (lang) {
                                SakinaLang.FR -> entry.meaningFr
                                SakinaLang.EN, SakinaLang.TR, SakinaLang.ID -> entry.meaningEn
                                else -> entry.meaningAr
                            }
                            SakinaGlassCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { shareName(entry) }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        SakinaAdvancedPrefs.formatDigits(ctx, "${entry.number}"),
                                        fontFamily = SakinaNumeralFont,
                                        fontSize = 11.sp,
                                        color = SakinaBrandColors.SoftSilver
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        entry.arabic,
                                        fontFamily = SakinaHeadingFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = theme.accentBright,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        meaning,
                                        fontFamily = SakinaBodyFont,
                                        fontSize = 11.5.sp,
                                        color = SakinaBrandColors.LuminousIvory,
                                        textAlign = TextAlign.Center,
                                        maxLines = 3
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
