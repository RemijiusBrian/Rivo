package dev.ridill.mym.welcomeFlow.presentation

interface WelcomeFlowActions {
    fun onNextClick()
    fun onLimitAmountChange(value: String)
    fun onNotificationRationaleDismiss()
    fun onNotificationRationaleAgree()
    fun onPermissionResponse()
}