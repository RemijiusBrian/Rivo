package dev.ridill.rivo.settings.domain.repositoty

interface AppInitRepository {
    suspend fun initCurrenciesList()
}