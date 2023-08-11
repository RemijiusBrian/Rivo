package dev.ridill.mym.settings.domain.service

import android.content.Context
import android.content.Intent
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

    fun getCurrentSignedInAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    suspend fun onSignInResult(intent: Intent?): GoogleSignInAccount? = tryOrNull {
        GoogleSignIn.getSignedInAccountFromIntent(intent).await()
    }
}