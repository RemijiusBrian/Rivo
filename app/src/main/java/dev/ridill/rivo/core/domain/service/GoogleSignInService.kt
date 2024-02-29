package dev.ridill.rivo.core.domain.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.core.os.BundleCompat
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.Status
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.core.ui.util.UiText
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
            .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE))
            .build()

        val client = GoogleSignIn.getClient(context, signInOptions)
        client.signOut().await()
        client.signInIntent
    }

    fun getSignedInAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    suspend fun getAccountFromSignInResult(result: ActivityResult): Resource<GoogleSignInAccount> =
        try {
            if (result.resultCode != Activity.RESULT_OK) {
                logI { "SignIn Failed" }
                logD {
                    "SignIn Status - ${
                        result.data?.extras?.let {
                            BundleCompat.getParcelable(
                                it,
                                KEY_GOOGLE_SIGN_IN_STATUS,
                                Status::class.java
                            )
                        }
                    }"
                }
                throw GoogleSignInFailedException()
            }

            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).await()
            if (account == null) {
                logI { "SignIn Failed" }
                throw GoogleSignInFailedException()
            }

            logD { "SignIn Success - ${account.email}" }
            Resource.Success(account)
        } catch (t: Throwable) {
            Resource.Error(
                UiText.StringResource(
                    R.string.error_sign_in_failed,
                    true
                )
            )
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
            "oauth2:${Scopes.DRIVE_APPFOLDER} ${Scopes.DRIVE_FILE}"
        )

        token
    }
}

private const val KEY_GOOGLE_SIGN_IN_STATUS = "googleSignInStatus"

class GoogleSignInFailedException : Throwable("SignIn Failed")