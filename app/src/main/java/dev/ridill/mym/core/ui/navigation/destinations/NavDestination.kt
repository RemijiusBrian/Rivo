package dev.ridill.mym.core.ui.navigation.destinations

import androidx.annotation.StringRes
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink

sealed interface NavDestination {

    val route: String

    @get:StringRes
    val labelRes: Int

    val arguments: List<NamedNavArgument>
        get() = emptyList()

    val deepLinks: List<NavDeepLink>
        get() = emptyList()
}

const val ARG_INVALID_ID_LONG = -1L