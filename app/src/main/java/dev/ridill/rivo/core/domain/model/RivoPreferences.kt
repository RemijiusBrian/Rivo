package dev.ridill.rivo.core.domain.model

import dev.ridill.rivo.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.rivo.settings.domain.modal.AppTheme
import java.time.LocalDateTime

data class RivoPreferences(
    val showOnboarding: Boolean,
    val appTheme: AppTheme,
    val dynamicColorsEnabled: Boolean,
    val lastBackupDateTime: LocalDateTime?,
    val needsConfigRestore: Boolean,
    val autoAddTransactionEnabled: Boolean,
    val showExcludedTransactions: Boolean,
    val showBalancedFolders: Boolean,
    val appLockEnabled: Boolean,
    val appAutoLockInterval: AppAutoLockInterval,
    val isAppLocked: Boolean,
    val encryptionPasswordHash: String?
)