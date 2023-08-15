package dev.ridill.mym.welcomeFlow.presentation

interface WelcomeFlowActions {
    fun onWelcomeMessageContinue()
    fun onPermissionsContinue()
    fun onPermissionResponse()
    fun onCheckForBackupClick()
    fun onSkipDataRestore()
    fun onBudgetInputChange(value: String)
    fun onSetBudgetContinue()
}