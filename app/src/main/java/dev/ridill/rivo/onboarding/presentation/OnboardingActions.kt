package dev.ridill.rivo.onboarding.presentation

interface OnboardingActions {
    fun onGivePermissionsClick()
    fun onSkipPermissionsClick()
    fun onSkipGoogleSignInClick()
    fun onBackupClick()
    fun onDataRestoreSkip()
    fun onEncryptionPasswordInputDismiss()
    fun onEncryptionPasswordSubmit(password: String)
    fun onBudgetInputChange(value: String)
    fun onStartBudgetingClick()
}