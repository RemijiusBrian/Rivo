package dev.ridill.mym.welcomeFlow.presentation

interface WelcomeFlowActions {
    fun onPermissionResponse()
    fun onGoogleSignInClick()
    fun onSkipGoogleSignInClick()
    fun onRestoreDataClick()
    fun onSkipDataRestore()
    fun onBudgetInputChange(value: String)
    fun onSetBudgetContinue()
}