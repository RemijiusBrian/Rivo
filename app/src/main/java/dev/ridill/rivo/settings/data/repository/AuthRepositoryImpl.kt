package dev.ridill.rivo.settings.data.repository

import dev.ridill.rivo.core.data.util.tryNetworkCall
import dev.ridill.rivo.core.domain.model.AuthState
import dev.ridill.rivo.core.domain.model.DataError
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.core.domain.model.UserAccount
import dev.ridill.rivo.core.domain.service.AuthService
import dev.ridill.rivo.core.ui.authentication.CredentialService
import dev.ridill.rivo.settings.domain.repositoty.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val credentialService: CredentialService,
    private val authService: AuthService
) : AuthRepository {
    override fun getSignedInAccount(): UserAccount? =
        authService.getSignedInAccount()

    override fun getAuthState(): Flow<AuthState> =
        authService.getAuthStateFlow()

    override suspend fun signUserInWithToken(
        idToken: String
    ): Result<Unit, DataError> = tryNetworkCall {
        authService.signUserWithIdToken(idToken)
    }

    override suspend fun signUserOut() = withContext(Dispatchers.IO) {
        authService.signUserOut()
        credentialService.clearCredentials()
    }
}