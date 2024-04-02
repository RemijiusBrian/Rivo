package dev.ridill.rivo.core.domain.model

import dev.ridill.rivo.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.rivo.settings.domain.modal.AppTheme
import dev.ridill.rivo.settings.domain.repositoty.FatalBackupError
import java.time.LocalDateTime

data class RivoPreferences(
    val showOnboarding: Boolean,
    val appTheme: AppTheme,
    val dynamicColorsEnabled: Boolean,
    val lastBackupDateTime: LocalDateTime?,
    val autoAddTransactionEnabled: Boolean,
    val allTransactionsShowExcludedOption: Boolean,
    val appLockEnabled: Boolean,
    val appAutoLockInterval: AppAutoLockInterval,
    val isAppLocked: Boolean,
    val screenSecurityEnabled: Boolean,
    val encryptionPasswordHash: String?,
    val fatalBackupError: FatalBackupError?
)