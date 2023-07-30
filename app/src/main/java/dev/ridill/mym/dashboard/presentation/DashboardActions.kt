package dev.ridill.mym.dashboard.presentation

interface DashboardActions {
    fun onSetLimitClick()
    fun onSetLimitDismiss()
    fun onSetLimitConfirm(value: String)
}