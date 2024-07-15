package dev.ridill.rivo.settings.domain.repositoty

import android.app.PendingIntent
import android.content.Intent
import dev.ridill.rivo.core.domain.model.AuthState
import dev.ridill.rivo.core.domain.model.DataError
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.core.domain.model.UserAccount
import dev.ridill.rivo.core.ui.authentication.AuthorizationService
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getSignedInAccount(): UserAccount?
    fun getAuthState(): Flow<AuthState>
    suspend fun signUserInWithToken(idToken: String): Result<Unit, DataError>
    suspend fun signUserOut()
    suspend fun authorizeUserAccount(): Result<PendingIntent?, AuthorizationService.AuthorizationError>
    suspend fun decodeAuthorizationResult(intent: Intent): Result<Unit, AuthorizationService.AuthorizationError>
}