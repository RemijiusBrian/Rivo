package dev.ridill.mym.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

@Composable
fun DestinationResultEffect(
    key: String,
    navBackStackEntry: NavBackStackEntry,
    vararg keys: Any? = emptyArray(),
    onResult: (String?) -> Unit
) {
    val result = navBackStackEntry
        .savedStateHandle
        .get<String>(key)

    LaunchedEffect(result, *keys) {
        onResult(result)
        navBackStackEntry.savedStateHandle
            .remove<String>(key)
    }
}

fun NavHostController.navigateUpWithResult(key: String, result: String) {
    previousBackStackEntry
        ?.savedStateHandle
        ?.set(key, result)
    navigateUp()
}