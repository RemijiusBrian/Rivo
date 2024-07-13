package dev.ridill.rivo.settings.domain.repositoty

import dev.ridill.rivo.core.domain.model.AuthState
import dev.ridill.rivo.core.domain.model.DataError
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.core.domain.model.UserAccount
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getSignedInAccount(): UserAccount?
    fun getAuthState(): Flow<AuthState>
    suspend fun signUserInWithToken(idToken: String): Result<Unit, DataError>
    suspend fun signUserOut()
}