package app.sakina

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NightSpan(val maghrib: Date, val fajr: Date) {
    val midnight: Date get() = Date(NightMath.midnight(maghrib.time, fajr.time))
    val lastThird: Date get() = Date(NightMath.lastThird(maghrib.time, fajr.time))
}

object NightWindow {
    fun current(c: Context, now: Date): NightSpan {
        val today = PrayerEngine.events(c, 0)
        val fajrToday = today[0].second
        return if (now.before(fajrToday)) {
            NightSpan(PrayerEngine.events(c, -1)[4].second, fajrToday)
        } else {
            NightSpan(today[4].second, PrayerEngine.events(c, 1)[0].second)
        }
    }
}

@Composable
fun NightCard(ctx: Context, sky: Sky, now: Date) {
    val span = remember(now.time / 60_000L) { NightWindow.current(ctx, now) }
    val fmt = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp).clip(shape).background(sky.glass)
            .border(0.75.dp, sky.hairline, shape).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NightCell("منتصف الليل", fmt.format(span.midnight), sky)
        NightCell("الثلث الأخير", fmt.format(span.lastThird), sky)
    }
}

@Composable
private fun NightCell(label: String, time: String, sky: Sky) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label:  ", color = sky.ink2, fontSize = 13.sp)
        TabularText(time, 15.sp, sky.ink, NotoSans, FontWeight.Medium)
    }
}
