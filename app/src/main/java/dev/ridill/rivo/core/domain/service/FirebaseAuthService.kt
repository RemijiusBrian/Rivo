package dev.ridill.rivo.core.domain.service

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import dev.ridill.rivo.core.domain.model.AuthState
import dev.ridill.rivo.core.domain.model.UserAccount
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthService : AuthService {
    private val auth = Firebase.auth

    override fun getSignedInAccount(): UserAccount? =
        auth.currentUser?.let(FirebaseUser::toUserAccount)

    override fun getAuthStateFlow(): Flow<AuthState> = callbackFlow {
        val listener = AuthStateListener { auth ->
            val authState = auth.currentUser?.let {
                AuthState.Authenticated(it.toUserAccount())
            } ?: AuthState.UnAuthenticated
            trySendBlocking(authState)
        }

        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signUserWithIdToken(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    override suspend fun signUserOut() {
        auth.signOut()
    }
}

fun FirebaseUser.toUserAccount(): UserAccount = UserAccount(
    email = email.orEmpty(),
    displayName = displayName.orEmpty(),
    photoUrl = photoUrl?.toString().orEmpty()
)