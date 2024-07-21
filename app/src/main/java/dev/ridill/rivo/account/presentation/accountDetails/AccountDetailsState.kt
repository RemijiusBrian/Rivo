package dev.ridill.rivo.account.presentation.accountDetails

import dev.ridill.rivo.core.domain.util.Empty

data class AccountDetailsState(
    val isAccountAuthenticated: Boolean = false,
    val photoUrl: String? = null,
    val email: String = String.Empty,
    val displayName: String = String.Empty,
    val showAccountDeleteConfirmation: Boolean = false,
    val showLogoutConfirmation: Boolean = false
)