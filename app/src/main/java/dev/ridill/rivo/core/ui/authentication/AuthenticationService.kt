package dev.ridill.rivo.core.ui.authentication

import android.content.Context
import android.content.IntentSender

interface AuthenticationService {
    suspend fun signInWithGoogle(activityContext: Context)
    suspend fun getSignedInAccount()
    suspend fun signOut()
}

data class UserAccount(
    val email: String,
    val displayName: String
)