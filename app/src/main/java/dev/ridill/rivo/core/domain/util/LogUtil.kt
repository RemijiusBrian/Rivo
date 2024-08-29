package dev.ridill.rivo.core.domain.util

import timber.log.Timber

inline fun logI(tag: String? = null, crossinline message: () -> String) {
    Timber.i("${tag ?: "AppDebug"}: ${message()}")
}

inline fun logE(
    throwable: Throwable,
    tag: String? = null,
    crossinline message: () -> String = { String.Empty }
) {
    Timber.e(throwable, "${tag ?: "AppDebug"}: ${message().ifEmpty { throwable.message }}")
}

inline fun logD(tag: String? = null, crossinline message: () -> String) {
    Timber.d("${tag ?: "AppDebug"}: ${message()}")
}