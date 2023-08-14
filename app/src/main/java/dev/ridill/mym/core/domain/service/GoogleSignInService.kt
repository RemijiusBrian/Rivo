package dev.ridill.mym.core.domain.service

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.tryOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

class GoogleSignInService(
    private val context: Context
) {
    suspend fun getSignInIntent(): Intent = withContext(Dispatchers.IO) {
        val signInOptions = GoogleSignInOptions.Builder()
            .requestEmail()
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
            .build()

        val client = GoogleSignIn.getClient(context, signInOptions)
        client.signOut().await()
        client.signInIntent
    }

    fun getSignedInAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    suspend fun getAccountFromIntent(intent: Intent?): GoogleSignInAccount? = tryOrNull {
        val account = GoogleSignIn.getSignedInAccountFromIntent(intent).await()
        account
    }

    @Throws(
        IOException::class,
        UserRecoverableAuthException::class,
        GoogleAuthException::class,
        Throwable::class
    )
    suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(context)?.account
            ?: throw Throwable("Failed to get access token")
        val token = GoogleAuthUtil.getToken(
            context,
            account,
            "oauth2:${Scopes.DRIVE_APPFOLDER}"
        )
        println("AppDebug: Access Token - $token")
        return@withContext "Bearer $token"
    }
}