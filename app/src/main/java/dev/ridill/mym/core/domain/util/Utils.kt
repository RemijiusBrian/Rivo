package dev.ridill.mym.core.domain.util

import dev.ridill.mym.R
import dev.ridill.mym.core.domain.model.Resource
import dev.ridill.mym.core.ui.util.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

inline fun <T> tryOrNull(
    tryBlock: () -> T
): T? = try {
    tryBlock()
} catch (t: Throwable) {
    t.printStackTrace()
    null
}

suspend inline fun <T> tryForResource(
    crossinline block: suspend () -> Resource<T>
): Resource<T> = withContext(Dispatchers.IO) {
    try {
        block()
    } catch (t: Throwable) {
        t.printStackTrace()
        Resource.Error(
            t.localizedMessage?.let {
                UiText.DynamicString(it)
            } ?: UiText.StringResource(R.string.error_unknown)
        )
    }
}

fun <T> Flow<T>.asStateFlow(
    scope: CoroutineScope,
    initialValue: T,
    sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5_000L)
): StateFlow<T> = this.stateIn(scope, sharingStarted, initialValue)

inline fun log(crossinline message: () -> String) {
    println("AppDebug: ${message()}")
}