package dev.ridill.rivo.core.domain.model

import dev.ridill.rivo.core.ui.util.UiText

typealias RootError = Error

sealed interface Result<out D, out E : RootError> {
    data class Success<out D, out E : RootError>(val data: D) : Result<D, E>
    data class Error<out D, out E : RootError>(
        val error: E,
        val message: UiText,
        val data: D? = null
    ) : Result<D, E>
}