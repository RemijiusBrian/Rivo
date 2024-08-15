package dev.ridill.rivo.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

@Composable
fun <T> NavigationResultEffect(
    key: String,
    navBackStackEntry: NavBackStackEntry,
    vararg keys: Any,
    onResult: (T?) -> Unit
) {
    val result = navBackStackEntry
        .savedStateHandle
        .get<T>(key)

    LaunchedEffect(result, navBackStackEntry, *keys) {
        onResult(result)
//        navBackStackEntry.savedStateHandle
//            .remove<T>(key)
    }
}

fun <T> NavHostController.navigateUpWithResult(
    key: String,
    result: T,
    backStackEntry: NavBackStackEntry? = this.previousBackStackEntry
) {
    backStackEntry
        ?.savedStateHandle
        ?.set(key, result)
    navigateUp()
}