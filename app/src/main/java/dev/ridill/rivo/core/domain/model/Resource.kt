package dev.ridill.rivo.core.domain.model

import dev.ridill.rivo.core.ui.util.UiText

typealias SimpleResource = Resource<Unit>

sealed class Resource<T>(
    val data: T? = null,
    val message: UiText? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: UiText) : Resource<T>(null, message)
}