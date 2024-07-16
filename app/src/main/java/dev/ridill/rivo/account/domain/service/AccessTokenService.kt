package dev.ridill.rivo.account.domain.service

interface AccessTokenService {
    suspend fun getAccessToken(): String?
    suspend fun updateAccessToken(token: String)
}