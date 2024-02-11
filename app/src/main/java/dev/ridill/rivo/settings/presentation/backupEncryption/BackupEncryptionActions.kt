package dev.ridill.rivo.settings.presentation.backupEncryption

interface BackupEncryptionActions {
    fun onUpdatePasswordClick()
    fun onCurrentPasswordChange(value: String)
    fun onForgotCurrentPasswordClick()
    fun onNewPasswordChange(value: String)
    fun onConfirmNewPasswordChange(value: String)
    fun onPasswordInputDismiss()
    fun onPasswordUpdateConfirm()
}