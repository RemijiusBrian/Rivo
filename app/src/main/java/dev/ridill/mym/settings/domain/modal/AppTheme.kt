package dev.ridill.mym.settings.domain.modal

import androidx.annotation.StringRes
import dev.ridill.mym.R

enum class AppTheme(@StringRes val labelRes: Int) {
    SYSTEM_DEFAULT(R.string.app_theme_system_default),
    LIGHT(R.string.app_theme_light),
    DARK(R.string.app_theme_dark)
}