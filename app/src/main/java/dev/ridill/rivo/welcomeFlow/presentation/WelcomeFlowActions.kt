package dev.ridill.rivo.welcomeFlow.presentation

interface WelcomeFlowActions {
    fun onGiveNotificationPermissionClick()
    fun onSkipNotificationPermission()
    fun onNotificationPermissionResponse(granted: Boolean)
    fun onGoogleSignInClick()
    fun onSkipGoogleSignInClick()
    fun onRestoreDataClick()
    fun onSkipDataRestore()
    fun onBudgetInputChange(value: String)
    fun onSetBudgetContinue()
}