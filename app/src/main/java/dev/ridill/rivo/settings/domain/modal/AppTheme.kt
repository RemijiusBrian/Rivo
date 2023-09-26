package dev.ridill.rivo.settings.domain.modal

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class AppTheme(@StringRes override val labelRes: Int) : BaseRadioOption {
    SYSTEM_DEFAULT(R.string.app_theme_system_default),
    LIGHT(R.string.app_theme_light),
    DARK(R.string.app_theme_dark)
}