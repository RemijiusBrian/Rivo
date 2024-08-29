package dev.ridill.rivo.settings.domain.repositoty

import kotlinx.coroutines.flow.Flow
import java.util.Currency

interface SettingsRepository {
    fun getCurrentBudget(): Flow<Long>
    fun getCurrencyPreference(): Flow<Currency>
}