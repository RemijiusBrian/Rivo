package dev.ridill.mym.core.ui.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText(
    open val isErrorText: Boolean
) {
    data class StringResource(
        @StringRes val resId: Int,
        override val isErrorText: Boolean = false,
        val args: List<Any> = emptyList()
    ) : UiText(isErrorText = isErrorText)

    data class DynamicString(
        val message: String,
        override val isErrorText: Boolean = false
    ) : UiText(isErrorText = isErrorText)

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> message
        is StringResource -> stringResource(resId, *(args.toTypedArray()))
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> message
        is StringResource -> context.getString(resId, *(args.toTypedArray()))
    }
}
