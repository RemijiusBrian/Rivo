package dev.ridill.rivo.account.data.repository

import android.app.PendingIntent
import android.content.Intent
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.account.domain.model.UserAccount
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.account.domain.service.AccessTokenService
import dev.ridill.rivo.account.domain.service.AuthService
import dev.ridill.rivo.account.presentation.util.AuthorizationFailedThrowable
import dev.ridill.rivo.account.presentation.util.AuthorizationNeedsResolutionThrowable
import dev.ridill.rivo.account.presentation.util.AuthorizationService
import dev.ridill.rivo.account.presentation.util.CredentialService
import dev.ridill.rivo.core.data.util.tryNetworkCall
import dev.ridill.rivo.core.domain.model.DataError
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.core.ui.util.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val credentialService: CredentialService,
    private val authService: AuthService,
    private val authorizationService: AuthorizationService,
    private val accessTokenService: AccessTokenService
) : AuthRepository {
    override fun getSignedInAccount(): UserAccount? =
        authService.getSignedInAccount()

    override fun getAuthState(): Flow<AuthState> =
        authService.getAuthStateFlow()

    override suspend fun signUserInWithToken(
        idToken: String
    ): Result<Unit, DataError> = tryNetworkCall {
        authService.signUserWithIdToken(idToken)
        Result.Success(Unit)
    }

    override suspend fun signUserOut(): Result<Unit, DataError> = tryNetworkCall {
        authService.signUserOut()
        credentialService.clearCredentials()
        Result.Success(Unit)
    }

    override suspend fun authorizeUserAccount(): Result<PendingIntent?, AuthorizationService.AuthorizationError> =
        try {
            val result = authorizationService.getIntentSenderForAuthorization()
            accessTokenService.updateAccessToken(result.accessToken.orEmpty())
            Result.Success(result.pendingIntent)
        } catch (t: AuthorizationNeedsResolutionThrowable) {
            Result.Error(
                error = AuthorizationService.AuthorizationError.NEEDS_RESOLUTION,
                message = UiText.StringResource(R.string.error_authorization_required, true),
                data = t.pendingIntent
            )
        } catch (t: AuthorizationFailedThrowable) {
            Result.Error(
                error = AuthorizationService.AuthorizationError.AUTHORIZATION_FAILED,
                message = UiText.StringResource(R.string.error_authorization_failed, true)
            )
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            Result.Error(
                error = AuthorizationService.AuthorizationError.AUTHORIZATION_FAILED,
                message = UiText.StringResource(R.string.error_authorization_failed, true)
            )
        }

    override suspend fun decodeAuthorizationResult(intent: Intent): Result<Unit, AuthorizationService.AuthorizationError> =
        try {
            val accessToken = authorizationService.decodeAccessTokenFromIntent(intent)
            accessTokenService.updateAccessToken(accessToken)
            Result.Success(Unit)
        } catch (t: AuthorizationNeedsResolutionThrowable) {
            Result.Error(
                error = AuthorizationService.AuthorizationError.NEEDS_RESOLUTION,
                message = UiText.StringResource(R.string.error_authorization_required, true)
            )
        } catch (t: AuthorizationFailedThrowable) {
            Result.Error(
                error = AuthorizationService.AuthorizationError.AUTHORIZATION_FAILED,
                message = UiText.StringResource(R.string.error_authorization_failed, true)
            )
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            Result.Error(
                error = AuthorizationService.AuthorizationError.AUTHORIZATION_FAILED,
                message = UiText.StringResource(R.string.error_authorization_failed, true)
            )
        }

    override suspend fun deleteAccount(): Result<Unit, DataError> = tryNetworkCall {
        authService.deleteAccount()
        Result.Success(Unit)
    }
}