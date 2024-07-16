package dev.ridill.rivo.account.domain.model

data class UserAccount(
    val email: String,
    val displayName: String,
    val photoUrl: String
)