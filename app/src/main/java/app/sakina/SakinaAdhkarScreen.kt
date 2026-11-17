package app.sakina

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class DhikrCategory(val title: String, @DrawableRes val iconRes: Int, val items: List<String>)

private val ADHKAR_CATEGORIES = listOf(
    DhikrCategory("أذكار الصباح", R.drawable.ic_dhuhr, listOf(
        "أصبحنا وأصبح الملك لله، والحمد لله، لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير.",
        "اللهم بك أصبحنا، وبك أمسينا، وبك نحيا، وبك نموت، وإليك النشور.",
        "اللهم أنت ربي لا إله إلا أنت، خلقتني وأنا عبدك، وأنا على عهدك ووعدك ما استطعت (سيد الاستغفار).",
        "رضيت بالله رباً، وبالإسلام ديناً، وبمحمد ﷺ نبياً (3 مرات).",
        "بسم الله الذي لا يضر مع اسمه شيء في الأرض ولا في السماء وهو السميع العليم (3 مرات).",
        "حسبي الله لا إله إلا هو عليه توكلت وهو رب العرش العظيم (7 مرات).",
        "يا حي يا قيوم برحمتك أستغيث أصلح لي شأني كله ولا تكلني إلى نفسي طرفة عين.",
        "سبحان الله وبحمده (100 مرة)."
    )),
    DhikrCategory("أذكار المساء", R.drawable.ic_isha, listOf(
        "أمسينا وأمسى الملك لله، والحمد لله، لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير.",
        "اللهم بك أمسينا، وبك أصبحنا، وبك نحيا، وبك نموت، وإليك المصير.",
        "أعوذ بكلمات الله التامات من شر ما خلق (3 مرات).",
        "اللهم إني أسألك العفو والعافية في الدنيا والآخرة.",
        "بسم الله الذي لا يضر مع اسمه شيء في الأرض ولا في السماء وهو السميع العليم (3 مرات).",
        "حسبي الله لا إله إلا هو عليه توكلت وهو رب العرش العظيم (7 مرات).",
        "اللهم عافني في بدني، اللهم عافني في سمعي، اللهم عافني في بصري (3 مرات).",
        "أستغفر الله العظيم وأتوب إليه (100 مرة)."
    )),
    DhikrCategory("دخول وخروج المسجد", R.drawable.ic_mosque, listOf(
        "عند الذهاب: اللهم اجعل في قلبي نوراً، وفي لساني نوراً، واجعل في سمعي نوراً، واجعل في بصري نوراً.",
        "عند الدخول (بالرجل اليمنى): أعوذ بالله العظيم وبوجهه الكريم وسلطانه القديم من الشيطان الرجيم.",
        "عند الدخول: بسم الله، والصلاة والسلام على رسول الله، اللهم افتح لي أبواب رحمتك.",
        "عند الخروج (بالرجل اليسرى): بسم الله، والصلاة والسلام على رسول الله، اللهم إني أسألك من فضلك، اللهم اعصمني من الشيطان الرجيم."
    )),
    DhikrCategory("دخول وخروج البيت", R.drawable.ic_star_ornament, listOf(
        "عند الدخول: بسم الله ولجنا، وبسم الله خرجنا، وعلى الله ربنا توكلنا، ثم ليسلّم على أهله.",
        "فضل التسمية عند الدخول: إذا ذكر الله عند دخوله وعند طعامه قال الشيطان: لا مبيت لكم ولا عشاء (رواه مسلم).",
        "عند الخروج: بسم الله، توكلت على الله، ولا حول ولا قوة إلا بالله.",
        "عند الخروج: اللهم إني أعوذ بك أن أضل أو أُضل، أو أزل أو أُزل، أو أظلم أو أُظلم، أو أجهل أو يُجهل عليّ."
    )),
    DhikrCategory("بعد الصلاة", R.drawable.ic_asr, listOf(
        "أستغفر الله (3 مرات)، اللهم أنت السلام ومنك السلام تباركت يا ذا الجلال والإكرام.",
        "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير، اللهم لا مانع لما أعطيت.",
        "سبحان الله (33)، الحمد لله (33)، الله أكبر (33)، وتمام المائة: لا إله إلا الله وحده لا شريك له.",
        "قراءة آية الكرسي دبر كل صلاة مكتوبة.",
        "قراءة سورة الإخلاص والفلق والناس بعد كل صلاة.",
        "اللهم أعني على ذكرك وشكرك وحسن عبادتك."
    )),
    DhikrCategory("أذكار النوم", R.drawable.ic_fajr, listOf(
        "الجمع بين الكفين وقراءة الإخلاص والفلق والناس والنفث فيهما ومسح الجسد (3 مرات).",
        "قراءة آية الكرسي عند أخذ المضجع.",
        "باسمك ربي وضعت جنبي وبك أرفعه، فإن أمسكت نفسي فارحمها، وإن أرسلتها فاحفظها.",
        "اللهم قني عذابك يوم تبعث عبادك (3 مرات).",
        "سبحان الله (33)، والحمد لله (33)، والله أكبر (34).",
        "اللهم أسلمت نفسي إليك، وفوضت أمري إليك، ووجهت وجهي إليك، وألجأت ظهري إليك."
    ))
)

@Composable
fun SakinaAdhkarScreen(ctx: Context, modifier: Modifier = Modifier) {
    val theme = LocalSakinaTheme.current
    val sp = remember { ctx.getSharedPreferences("tasbih_prefs", Context.MODE_PRIVATE) }
    var count by remember { mutableIntStateOf(sp.getInt("count", 0)) }
    var selectedCategory by remember { mutableStateOf<DhikrCategory?>(null) }
    val view = LocalView.current

    BackHandler(enabled = selectedCategory != null) { selectedCategory = null }

    if (selectedCategory != null) {
        val cat = selectedCategory!!
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, top = 74.dp, bottom = 82.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.bgCard)
                        .border(0.8.dp, Sakina3DGoldBevelBrush, RoundedCornerShape(16.dp))
                        .clickable { selectedCategory = null }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "الأذكار", tint = theme.accentBright, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("الأذكار", fontFamily = SakinaHeadingFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.accentBright)
                }
                Text(cat.title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = theme.accentBright)
                Spacer(Modifier.width(56.dp))
            }
            Spacer(Modifier.height(10.dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                cat.items.forEach { text ->
                    SakinaParchmentPanel(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                        Text(text.toWesternDigits(), fontFamily = SakinaEditorialFont, fontSize = 15.5.sp, lineHeight = 27.sp, color = SakinaInkDark)
                    }
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 74.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 5.dp)) {
            Text("الأذكار والمسبحة", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = theme.accentBright)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (r in 0 until 3) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdhkarTile(ADHKAR_CATEGORIES[r * 2], Modifier.weight(1f)) { selectedCategory = ADHKAR_CATEGORIES[r * 2] }
                    AdhkarTile(ADHKAR_CATEGORIES[r * 2 + 1], Modifier.weight(1f)) { selectedCategory = ADHKAR_CATEGORIES[r * 2 + 1] }
                }
            }
        }

        SakinaGlassCard(
            modifier = Modifier.fillMaxWidth().clickable {
                count++; sp.edit().putInt("count", count).apply()
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            },
            cornerRadius = 20.dp,
            glowGold = true,
            contentPadding = PaddingValues(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_tasbih), contentDescription = null, modifier = Modifier.size(46.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ورد التسبيح (اضغط للعد)", fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.LuminousIvory)
                    Spacer(Modifier.height(4.dp))
                    SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 22.dp, vertical = 5.dp)) {
                        Text("$count".toWesternDigits(), fontFamily = SakinaNumeralFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = theme.accentBright)
                    }
                }
                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "تصفير", tint = theme.accentGold, modifier = Modifier.size(22.dp).clickable { count = 0; sp.edit().putInt("count", 0).apply() })
            }
        }
    }
}

@Composable
private fun AdhkarTile(cat: DhikrCategory, modifier: Modifier = Modifier, onClick: () -> Unit) {
    SakinaParchmentPanel(modifier = modifier.height(74.dp).clickable { onClick() }, cornerRadius = 14.dp, contentPadding = PaddingValues(8.dp)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Image(painter = painterResource(id = cat.iconRes), contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(cat.title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = SakinaInkDark, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}
