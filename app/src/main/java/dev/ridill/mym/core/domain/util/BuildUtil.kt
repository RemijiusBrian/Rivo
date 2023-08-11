package dev.ridill.mym.core.domain.util

import android.os.Build
import dev.ridill.mym.BuildConfig

object BuildUtil {

    val versionName: String get() = BuildConfig.VERSION_NAME

    val versionCode: Long get() = BuildConfig.VERSION_CODE.toLong()

    fun isDynamicColorsSupported(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun isNotificationRuntimePermissionNeeded(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun isBuildInternalRelease(): Boolean =
        BuildConfig.FLAVOR == Flavours.INTERNAL
                && !BuildConfig.DEBUG

    object Flavours {
        const val INTERNAL = "internal"
        const val PRODUCTION = "production"
    }
}