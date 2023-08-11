package dev.ridill.mym.welcomeFlow.presentation

interface WelcomeFlowActions {
    fun onNextClick()
    fun onCheckForBackupClick()
    fun onSkipDataRestore()
    fun onIncomeInputChange(value: String)
    fun onNotificationRationaleDismiss()
    fun onNotificationRationaleAgree()
    fun onPermissionResponse()
}