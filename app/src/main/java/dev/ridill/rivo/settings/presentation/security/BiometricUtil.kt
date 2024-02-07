package dev.ridill.rivo.settings.presentation.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

@Composable
fun rememberBiometricManager(
    context: Context = LocalContext.current
): BiometricManager = remember(context) { BiometricManager.from(context) }

@Composable
fun rememberBiometricPrompt(
    context: Context = LocalContext.current,
    activity: FragmentActivity = LocalContext.current as FragmentActivity,
    executor: Executor = remember { ContextCompat.getMainExecutor(context) },
    onAuthError: (String) -> Unit = {},
    onAuthSucceeded: (BiometricPrompt.AuthenticationResult) -> Unit = {},
    onAuthFailed: () -> Unit = {}
): BiometricPrompt {
    val callback = remember(onAuthError, onAuthSucceeded, onAuthFailed) {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onAuthError(errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSucceeded(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onAuthFailed()
            }
        }
    }

    return remember(activity, executor, callback) {
        BiometricPrompt(activity, executor, callback)
    }
}

@Composable
fun rememberPromptInfo(
    title: String,
    subTitle: String? = null,
    description: String? = null,
    allowedAuthenticators: Int = BiometricUtil.DefaultBiometricAuthenticators
): BiometricPrompt.PromptInfo {

    return remember(title, subTitle, description) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subTitle)
            .setDescription(description)
            .setAllowedAuthenticators(allowedAuthenticators)
            .build()
    }
}

object BiometricUtil {
    val DefaultBiometricAuthenticators: Int
        get() = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
}
