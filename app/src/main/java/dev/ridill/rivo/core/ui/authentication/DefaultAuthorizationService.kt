package dev.ridill.rivo.core.ui.authentication

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.ui.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DefaultAuthorizationService(
    private val context: Context
) : AuthorizationService {
    private val authorizationClient = Identity.getAuthorizationClient(context)

    private val scopes
        get() = listOf(Scopes.DRIVE_APPFOLDER)
            .map { Scope(it) }

    override suspend fun getIntentSenderForAuthorization(): Resource<PendingIntent?> =
        withContext(Dispatchers.IO) {
            try {
                val request = AuthorizationRequest.Builder()
                    .setRequestedScopes(scopes)
                    .build()

                val result = authorizationClient.authorize(request).await()
                logD { "Authorize Result: Token = ${result.accessToken}, PendingIntent = ${result.pendingIntent}, hasResolution = ${result.hasResolution()}" }
                if (result.hasResolution())
                    Resource.Success(result.pendingIntent)
                else Resource.Success(result.pendingIntent)
            } catch (t: Throwable) {
                Resource.Error(UiText.StringResource(R.string.error_unknown))
            }
        }

    override fun getResultFromAuthorization(intent: Intent?) {
        val result = authorizationClient.getAuthorizationResultFromIntent(intent)

        logD { "Authorize Intent Result: Token = ${result.accessToken}, PendingIntent = ${result.pendingIntent}, hasResolution = ${result.hasResolution()}" }

    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val request = AuthorizationRequest.Builder()
            .setRequestedScopes(scopes)
            .build()

        val result = authorizationClient.authorize(request).await()
        result.accessToken
    }

    /*companion object {
        const val OAUTH_SCOPE_STRING = "oauth2:${Scopes.DRIVE_APPFOLDER} ${Scopes.DRIVE_FILE}"
    }*/
}