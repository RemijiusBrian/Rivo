package dev.ridill.rivo.core.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.paging.compose.LazyPagingItems

fun <T : Any> LazyPagingItems<T>.isEmpty(): Boolean = this.itemCount == 0
fun <T : Any> LazyPagingItems<T>.isNotEmpty(): Boolean = !this.isEmpty()

fun Modifier.mergedContentDescription(
    contentDescription: String?
): Modifier = this
    .semantics(mergeDescendants = true) {}
    .clearAndSetSemantics {
        contentDescription?.let {
            this.contentDescription = it
        }
    }

fun TextDecoration.Companion.exclusion(excluded: Boolean): TextDecoration? =
    if (excluded) LineThrough else null