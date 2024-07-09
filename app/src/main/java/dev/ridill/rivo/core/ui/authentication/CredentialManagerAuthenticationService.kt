package dev.ridill.rivo.core.ui.authentication

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.domain.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CredentialManagerAuthenticationService(
    private val context: Context
) : AuthenticationService {
    private val credentialManager = CredentialManager.create(context)

    override suspend fun signInWithGoogle(activityContext: Context) {
        withContext(Dispatchers.IO) {
            try {
                getSignedInAccount()
                signOut()
                val request = buildGetCredentialRequest()
                val response = credentialManager.getCredential(activityContext, request)
                when (val credential = response.credential) {
                    is CustomCredential -> {
                        if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
                            throw UnexpectedCredentialTypeThrowable()

                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        logD { "GoogleIdTokenCredentials: Token = ${googleIdTokenCredential.idToken}, DisplayName = ${googleIdTokenCredential.displayName}" }
//                        Firebase.auth.signInWithCustomToken(googleIdTokenCredential.idToken).await()
                    }

                    else -> throw UnexpectedCredentialTypeThrowable()
                }
            } catch (e: GetCredentialException) {
                logE(e) { "Type - ${e.type}" }
                logE(e)
            } catch (e: GoogleIdTokenParsingException) {
                logE(e)
            } catch (t: UnexpectedCredentialTypeThrowable) {
                logE(t)
            } catch (t: Throwable) {
                logE(t)
            }
        }
    }

    override suspend fun getSignedInAccount() {
        logD { "Firebase user - ${Firebase.auth.currentUser}" }
    }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }

    private fun buildGetCredentialRequest(): GetCredentialRequest {
        val option = GetSignInWithGoogleOption
            .Builder("10451787819-oiiasg8jt6rnu1vk4gv0airrk0oee2t8.apps.googleusercontent.com")
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
    }
}

class UnexpectedCredentialTypeThrowable : Throwable()