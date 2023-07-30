package dev.ridill.mym.core.domain.model

data class MYMPreferences(
    val isAppFirstLaunch: Boolean,
    val monthlyLimit: Long
)