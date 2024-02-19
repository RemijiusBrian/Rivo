package dev.ridill.rivo.onboarding.presentation

interface OnboardingActions {
    fun onGiveNotificationPermissionClick()
    fun onSkipNotificationPermission()
    fun onNotificationPermissionResponse(granted: Boolean)
    fun onGoogleSignInClick()
    fun onSkipGoogleSignInClick()
    fun onRestoreDataClick()
    fun onSkipDataRestore()
    fun onEncryptionPasswordInputDismiss()
    fun onEncryptionPasswordSubmit(password: String)
    fun onBudgetInputChange(value: String)
    fun onSetBudgetContinue()
}