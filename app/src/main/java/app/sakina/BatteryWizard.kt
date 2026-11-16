package app.sakina

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private class Vendor(val label: String, val autostart: ComponentName?, val steps: List<String>)

private fun vendor(): Vendor {
    val m = (Build.MANUFACTURER ?: "").lowercase()
    return when {
        m.contains("xiaomi") || m.contains("redmi") || m.contains("poco") -> Vendor(
            "شاومي / ريدمي / بوكو",
            ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
            listOf(
                "اضغط «السماح بتجاوز البطارية» ووافق في النافذة.",
                "اضغط «التشغيل التلقائي» وفعّل سَكِينة.",
                "اضغط «معلومات التطبيق»، ثم «توفير الطاقة» ← «بلا قيود».",
                "افتح قائمة التطبيقات الأخيرة، واضغط بطاقة سَكِينة مطولاً واختر القفل، كي لا يُغلق عند تنظيف الذاكرة."
            )
        )
        m.contains("samsung") -> Vendor(
            "سامسونغ",
            ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"),
            listOf(
                "اضغط «السماح بتجاوز البطارية» ووافق في النافذة.",
                "في «البطارية» ← «حدود الاستخدام في الخلفية»: أزل سَكِينة من «التطبيقات النائمة» و«النائمة بعمق»، وأضفها إلى «التطبيقات التي لا تنام أبداً»."
            )
        )
        m.contains("huawei") || m.contains("honor") -> Vendor(
            "هواوي / هونر",
            ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
            listOf(
                "اضغط «السماح بتجاوز البطارية» ووافق في النافذة.",
                "في «تشغيل التطبيقات»: اختر سَكِينة، وعطّل «الإدارة التلقائية»، وفعّل التشغيل التلقائي والتشغيل الثانوي والتشغيل في الخلفية."
            )
        )
        m.contains("oppo") || m.contains("realme") || m.contains("oneplus") -> Vendor(
            "أوبو / ريلمي / ون بلس",
            ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
            listOf(
                "اضغط «السماح بتجاوز البطارية» ووافق في النافذة.",
                "فعّل «السماح بالتشغيل التلقائي» لسَكِينة، وفي «استخدام البطارية» اختر «السماح بالنشاط في الخلفية»."
            )
        )
        m.contains("vivo") || m.contains("iqoo") -> Vendor(
            "فيفو",
            ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
            listOf(
                "اضغط «السماح بتجاوز البطارية» ووافق في النافذة.",
                "فعّل سَكِينة في «التشغيل التلقائي»، وفي البطارية اسمح لها بالنشاط في الخلفية."
            )
        )
        m.contains("infinix") || m.contains("tecno") || m.contains("itel") -> Vendor(
            "إنفينكس / تكنو / آيتل",
            ComponentName("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity"),
            listOf(
                "اضغط «السماح بتجاوز البطارية» ووافق في النافذة.",
                "فعّل سَكِينة في «التشغيل التلقائي» داخل مدير الهاتف، وأبقها مقفلة في قائمة التطبيقات الأخيرة."
            )
        )
        else -> Vendor(
            Build.MANUFACTURER ?: "جهازك", null,
            listOf(
                "اضغط «السماح بتجاوز البطارية» ووافق في النافذة.",
                "في معلومات التطبيق ← البطارية: اختر «بلا قيود» أو «غير محسّن»، وفعّل التشغيل التلقائي إن وُجد."
            )
        )
    }
}

private fun open(ctx: Context, i: Intent, fallback: Intent? = null) {
    try {
        ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        try {
            ctx.startActivity((fallback ?: appDetails(ctx)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e2: Exception) {
            DiagLog.add(ctx, "تعذّر فتح الإعدادات: ${e2.javaClass.simpleName}")
        }
    }
}

private fun appDetails(ctx: Context) =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))

@Composable
fun BatteryWizard(ctx: Context, sky: Sky) {
    val v = vendor()
    GlassCard(sky) {
        Text("معالج البطارية", color = sky.ink, fontWeight = FontWeight.Bold)
        Text("الجهاز: ${v.label}", color = sky.ink, fontSize = 13.sp)
        v.steps.forEachIndexed { i, t -> Text("${i + 1}. $t", color = sky.ink, fontSize = 13.5.sp, lineHeight = 20.sp) }
        GoldButton("السماح بتجاوز البطارية") {
            open(
                ctx,
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}")),
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            )
        }
        if (v.autostart != null) {
            GlassButton(sky, "التشغيل التلقائي") { open(ctx, Intent().setComponent(v.autostart)) }
        }
        GlassButton(sky, "معلومات التطبيق") { open(ctx, appDetails(ctx)) }
    }
}
