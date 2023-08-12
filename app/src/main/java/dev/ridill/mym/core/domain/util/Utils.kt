package dev.ridill.mym.core.domain.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

inline fun <T> tryOrNull(
    tryBlock: () -> T
): T? = try {
    tryBlock()
} catch (t: Throwable) {
    t.printStackTrace()
    null
}

fun <T> Flow<T>.asStateFlow(
    scope: CoroutineScope,
    initialValue: T,
    sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5_000L)
): StateFlow<T> = this.stateIn(scope, sharingStarted, initialValue)