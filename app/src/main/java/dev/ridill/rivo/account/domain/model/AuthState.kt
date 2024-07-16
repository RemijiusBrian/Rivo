package dev.ridill.rivo.account.domain.model

sealed interface AuthState {
    data object UnAuthenticated : AuthState
    data class Authenticated(val account: UserAccount) : AuthState
}