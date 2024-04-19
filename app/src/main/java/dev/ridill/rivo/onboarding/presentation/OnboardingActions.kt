package dev.ridill.rivo.onboarding.presentation

interface OnboardingActions {
    fun onGivePermissionsClick()
    fun onSkipPermissionsClick()
    fun onGoogleSignInClick()
    fun onSkipGoogleSignInClick()
    fun onRestoreDataClick()
    fun onSkipDataRestore()
    fun onEncryptionPasswordInputDismiss()
    fun onEncryptionPasswordSubmit(password: String)
    fun onBudgetInputChange(value: String)
    fun onStartBudgetingClick()
}