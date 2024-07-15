package dev.ridill.rivo.core.ui.authentication

import android.app.PendingIntent
import android.content.Intent
import dev.ridill.rivo.core.domain.model.Error

interface AuthorizationService {
    suspend fun getIntentSenderForAuthorization(): AuthorizationResult
    fun decodeAccessTokenFromIntent(intent: Intent?): String

    enum class AuthorizationError : Error {
        NEEDS_RESOLUTION,
        AUTHORIZATION_FAILED
    }
}

data class AuthorizationResult(
    val accessToken: String?,
    val pendingIntent: PendingIntent?
)