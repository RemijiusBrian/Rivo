package dev.ridill.rivo.onboarding.domain.model

enum class DataRestoreState {
    IDLE,
    CHECKING_FOR_BACKUP,
    DOWNLOADING_DATA,
    PASSWORD_VERIFICATION,
    RESTORE_IN_PROGRESS,
    COMPLETED,
    FAILED
}