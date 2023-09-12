package dev.ridill.mym.settings.domain.modal

import androidx.annotation.StringRes

interface BaseRadioOption {
    @get:StringRes
    val labelRes: Int
}