package dev.ridill.rivo.core.domain.model

data class UserAccount(
    val email: String,
    val displayName: String
)

sealed interface AuthState {
    data object UnAuthenticated : AuthState
    data class Authenticated(val account: UserAccount) : AuthState
}