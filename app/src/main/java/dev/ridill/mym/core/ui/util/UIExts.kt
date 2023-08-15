package dev.ridill.mym.core.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.system.exitProcess

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}

fun Context.launchAppNotificationSettings() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    }
    startActivity(intent)
}

fun Context.launchAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}

fun Context.launchUrlExternally(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}

fun Context.restartApplication() {
    val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }
    startActivity(intent)
    exitProcess(0)
}

fun Color.contentColor(
    onLight: Color = Color.Black,
    onDark: Color = Color.White
): Color = if (luminance() >= 0.4f) onLight
else onDark