package dev.ridill.rivo.account.presentation.accountDetails

interface AccountDetailsActions {
    fun onSignInClick()
    fun onDeleteAccountClick()
    fun onDeleteAccountDismiss()
    fun onDeleteAccountConfirm()
    fun onLogoutClick()
    fun onLogoutDismiss()
    fun onLogoutConfirm()
}