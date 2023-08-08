package dev.ridill.mym.core.domain.service

import android.content.Context
import com.google.firebase.appdistribution.FirebaseAppDistributionException
import com.google.firebase.appdistribution.ktx.appDistribution
import com.google.firebase.ktx.Firebase
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.util.UiText
import kotlinx.coroutines.tasks.await

class AppDistributionService(
    private val context: Context
) {
    private val appDistribution = Firebase.appDistribution

    val isTesterSignedIn: Boolean
        get() = appDistribution.isTesterSignedIn

    suspend fun enableTestingFeatures(): UiText = try {
        appDistribution.signInTester().await()
        UiText.StringResource(R.string.testing_features_enabled)
    } catch (e: FirebaseAppDistributionException) {
        e.localizedMessage?.let { UiText.DynamicString(it, true) }
            ?: UiText.StringResource(R.string.error_invalid_amount, true)
    } catch (t: Throwable) {
        t.message?.let { UiText.DynamicString(it, true) }
            ?: UiText.StringResource(R.string.error_invalid_amount, true)
    }

    fun startFeedback() {
        appDistribution.startFeedback(context.getString(R.string.feedback_message))
    }
}