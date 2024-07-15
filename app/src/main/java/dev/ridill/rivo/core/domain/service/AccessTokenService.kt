package dev.ridill.rivo.core.domain.service

interface AccessTokenService {
    suspend fun getAccessToken(): String?
    suspend fun updateAccessToken(token: String)
}