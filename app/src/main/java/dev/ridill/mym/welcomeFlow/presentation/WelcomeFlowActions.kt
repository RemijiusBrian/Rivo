package dev.ridill.mym.welcomeFlow.presentation

interface WelcomeFlowActions {
    fun onWelcomeMessageContinue()
    fun onPermissionsContinue()
    fun onPermissionResponse()
    fun onGoogleSignInClick()
    fun onRestoreDataClick()
    fun onSkipSignInOrRestore()
    fun onBudgetInputChange(value: String)
    fun onSetBudgetContinue()
}