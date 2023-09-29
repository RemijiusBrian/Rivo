package dev.ridill.rivo.core.ui.util

import androidx.paging.compose.LazyPagingItems

fun <T : Any> LazyPagingItems<T>.isEmpty(): Boolean = this.itemCount == 0
fun <T : Any> LazyPagingItems<T>.isNotEmpty(): Boolean = !this.isEmpty()