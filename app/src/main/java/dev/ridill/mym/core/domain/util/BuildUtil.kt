package dev.ridill.mym.core.domain.util

import android.os.Build
import dev.ridill.mym.BuildConfig

object BuildUtil {

    val versionName: String get() = BuildConfig.VERSION_NAME

    fun isDynamicColorsSupported(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun isNotificationRuntimePermissionNeeded(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}