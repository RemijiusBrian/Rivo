package dev.ridill.rivo.core.domain.util

import timber.log.Timber

inline fun logI(crossinline message: () -> String) {
    Timber.i("AppDebug: ${message()}")
}

inline fun logE(throwable: Throwable, crossinline message: () -> String = { String.Empty }) {
    Timber.e(throwable, "AppDebug: ${message()}")
}

inline fun logD(crossinline message: () -> String) {
    Timber.d("AppDebug: ${message()}")
}