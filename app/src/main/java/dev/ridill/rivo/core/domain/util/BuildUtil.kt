package dev.ridill.rivo.core.domain.util

import android.os.Build
import dev.ridill.rivo.BuildConfig

object BuildUtil {
    val versionName: String get() = BuildConfig.VERSION_NAME

    val isDebug: Boolean get() = BuildConfig.DEBUG

    val isApiLevelAtLeast30: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun isDynamicColorsSupported(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun isNotificationRuntimePermissionNeeded(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}