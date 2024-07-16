package dev.ridill.rivo.account.domain.repository

import android.app.PendingIntent
import android.content.Intent
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.core.domain.model.DataError
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.account.domain.model.UserAccount
import dev.ridill.rivo.account.presentation.AuthorizationService
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getSignedInAccount(): UserAccount?
    fun getAuthState(): Flow<AuthState>
    suspend fun signUserInWithToken(idToken: String): Result<Unit, DataError>
    suspend fun signUserOut()
    suspend fun authorizeUserAccount(): Result<PendingIntent?, AuthorizationService.AuthorizationError>
    suspend fun decodeAuthorizationResult(intent: Intent): Result<Unit, AuthorizationService.AuthorizationError>
}