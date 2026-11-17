package app.sakina

import android.content.Context
import android.view.HapticFeedbackConstants
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class DhikrCategory(val title: String, @DrawableRes val iconRes: Int, val items: List<String>)

private val ADHKAR_CATEGORIES = listOf(
    DhikrCategory("أذكار الصباح", R.drawable.ic_dhuhr, listOf(
        "أصبحنا وأصبح الملك لله، والحمد لله، لا إله إلا الله وحده لا شريك له.",
        "اللهم بك أصبحنا، وبك أمسينا، وبك نحيا، وبك نموت، وإليك النشور.",
        "سبحان الله وبحمده (100 مرة)."
    )),
    DhikrCategory("أذكار المساء", R.drawable.ic_isha, listOf(
        "أمسينا وأمسى الملك لله، والحمد لله، لا إله إلا الله وحده لا شريك له.",
        "أعوذ بكلمات الله التامات من شر ما خلق (3 مرات).",
        "حسبي الله لا إله إلا هو عليه توكلت وهو رب العرش العظيم (7 مرات)."
    )),
    DhikrCategory("بعد الصلاة", R.drawable.ic_mosque, listOf(
        "أستغفر الله (3 مرات)، اللهم أنت السلام ومنك السلام تباركت يا ذا الجلال والإكرام.",
        "سبحان الله (33)، الحمد لله (33)، الله أكبر (33)، تمام المائة: لا إله إلا الله وحده لا شريك له.",
        "آية الكرسي دبر كل صلاة مكتوبة."
    )),
    DhikrCategory("قبل النوم", R.drawable.ic_fajr, listOf(
        "باسمك ربي وضعت جنبي وبك أرفعه.",
        "اللهم قني عذابك يوم تبعث عبادك (3 مرات).",
        "قراءة المعوذات والنفث في الكفين (3 مرات)."
    ))
)

@Composable
fun SakinaAdhkarScreen(ctx: Context, modifier: Modifier = Modifier) {
    val sp = remember { ctx.getSharedPreferences("tasbih_prefs", Context.MODE_PRIVATE) }
    var count by remember { mutableIntStateOf(sp.getInt("count", 0)) }
    var selectedCategory by remember { mutableStateOf<DhikrCategory?>(null) }
    val view = LocalView.current

    if (selectedCategory != null) {
        val cat = selectedCategory!!
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 74.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "رجوع",
                    tint = Color(0xFFE6CF94),
                    modifier = Modifier.size(24.dp).clickable { selectedCategory = null }
                )
                Text(cat.title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFE6CF94))
                Spacer(Modifier.size(24.dp))
            }
            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                cat.items.forEach { text ->
                    SakinaParchmentPanel(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                        Text(
                            text = text.toWesternDigits(),
                            fontFamily = SakinaEditorialFont,
                            fontSize = 16.sp,
                            lineHeight = 28.sp,
                            color = SakinaInkDark
                        )
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
            .padding(start = 22.dp, end = 22.dp, top = 28.dp, bottom = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("الأذكار", fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFE6CF94))
            Text("طمأنينة القلب بذكر الله", fontFamily = SakinaBodyFont, fontSize = 12.sp, color = SakinaBrandColors.SoftSilver)
        }

        // 2x2 Parchment Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdhkarTile(ADHKAR_CATEGORIES[0], Modifier.weight(1f)) { selectedCategory = ADHKAR_CATEGORIES[0] }
                AdhkarTile(ADHKAR_CATEGORIES[1], Modifier.weight(1f)) { selectedCategory = ADHKAR_CATEGORIES[1] }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdhkarTile(ADHKAR_CATEGORIES[2], Modifier.weight(1f)) { selectedCategory = ADHKAR_CATEGORIES[2] }
                AdhkarTile(ADHKAR_CATEGORIES[3], Modifier.weight(1f)) { selectedCategory = ADHKAR_CATEGORIES[3] }
            }
        }

        // Daily Wird / Tasbih Counter Card
        SakinaGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    count++
                    sp.edit().putInt("count", count).apply()
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                },
            cornerRadius = 20.dp,
            glowGold = true,
            contentPadding = PaddingValues(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(id = R.drawable.ic_tasbih), contentDescription = null, modifier = Modifier.size(54.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ابدأ وردك اليومي (اضغط للتسبيح)", fontFamily = SakinaBodyFont, fontSize = 11.5.sp, color = SakinaBrandColors.LuminousIvory)
                    Spacer(Modifier.height(6.dp))
                    SakinaOrnamentCartouche(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp)) {
                        Text(
                            text = "$count".toWesternDigits(),
                            fontFamily = SakinaNumeralFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = Color(0xFFE6CF94)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "تصفير",
                    tint = Color(0xFFC5A059),
                    modifier = Modifier.size(22.dp).clickable {
                        count = 0
                        sp.edit().putInt("count", 0).apply()
                    }
                )
            }
        }
    }
}

@Composable
private fun AdhkarTile(cat: DhikrCategory, modifier: Modifier = Modifier, onClick: () -> Unit) {
    SakinaParchmentPanel(
        modifier = modifier.height(92.dp).clickable { onClick() },
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = cat.iconRes), contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(cat.title, fontFamily = SakinaEditorialFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SakinaInkDark, textAlign = TextAlign.Center)
        }
    }
}
