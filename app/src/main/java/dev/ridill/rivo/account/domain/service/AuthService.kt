package dev.ridill.rivo.account.domain.service

import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.account.domain.model.UserAccount
import kotlinx.coroutines.flow.Flow

interface AuthService {
    fun getSignedInAccount(): UserAccount?
    fun getAuthStateFlow(): Flow<AuthState>
    suspend fun signinUserWithIdToken(idToken: String)
    suspend fun signUserOut()
    suspend fun deleteAccount()
}