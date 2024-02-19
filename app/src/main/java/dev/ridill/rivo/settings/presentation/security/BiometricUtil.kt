package dev.ridill.rivo.settings.presentation.security

import androidx.biometric.BiometricManager

object BiometricUtil {
    val DefaultBiometricAuthenticators: Int
        get() = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
}
