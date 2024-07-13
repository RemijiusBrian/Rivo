package dev.ridill.rivo.core.ui.authentication

import android.app.PendingIntent
import android.content.Intent
import dev.ridill.rivo.core.domain.model.Resource

interface AuthorizationService {
    suspend fun getIntentSenderForAuthorization(): Resource<PendingIntent?>
    fun getResultFromAuthorization(intent: Intent?)
    suspend fun getAccessToken(): String?
}