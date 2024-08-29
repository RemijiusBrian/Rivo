package dev.ridill.rivo.core.ui.util

import androidx.compose.runtime.compositionLocalOf
import dev.ridill.rivo.core.domain.util.LocaleUtil

val LocalCurrencyPreference = compositionLocalOf { LocaleUtil.defaultCurrency }