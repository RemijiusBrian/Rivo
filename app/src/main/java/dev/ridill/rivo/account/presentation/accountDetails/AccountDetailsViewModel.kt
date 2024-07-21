package dev.ridill.rivo.account.presentation.accountDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.account.domain.service.AccessTokenService
import dev.ridill.rivo.account.presentation.util.CredentialService
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val accessTokenService: AccessTokenService,
    private val repo: AuthRepository,
    private val eventBus: EventBus<AccountDetailsEvent>
) : ViewModel(), AccountDetailsActions {

    private val authState = repo.getAuthState()
    private val isAccountAuthenticated = authState.mapLatest { state ->
        state is AuthState.Authenticated
    }.distinctUntilChanged()
    private val accountDetails = authState.mapLatest { state ->
        when (state) {
            is AuthState.Authenticated -> state.account
            AuthState.UnAuthenticated -> null
        }
    }.distinctUntilChanged()

    private val showDeleteAccountConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_ACCOUNT_CONFIRMATION, false)

    private val showLogoutConfirmation = savedStateHandle
        .getStateFlow(SHOW_LOGOUT_CONFIRMATION, false)

    val state = combineTuple(
        isAccountAuthenticated,
        accountDetails,
        showDeleteAccountConfirmation,
        showLogoutConfirmation
    ).mapLatest { (
                      isAccountAuthenticated,
                      accountDetails,
                      showDeleteAccountConfirmation,
                      showLogoutConfirmation
                  ) ->
        AccountDetailsState(
            isAccountAuthenticated = isAccountAuthenticated,
            photoUrl = accountDetails?.photoUrl,
            displayName = accountDetails?.displayName.orEmpty(),
            email = accountDetails?.email.orEmpty(),
            showAccountDeleteConfirmation = showDeleteAccountConfirmation,
            showLogoutConfirmation = showLogoutConfirmation
        )
    }.asStateFlow(viewModelScope, AccountDetailsState())

    val events = eventBus.eventFlow

    override fun onSignInClick() {
        viewModelScope.launch {
            eventBus.send(AccountDetailsEvent.StartManualSignInFlow)
        }
    }

    fun onCredentialResult(
        result: Result<String, CredentialService.CredentialError>
    ) = viewModelScope.launch {
        when (result) {
            is Result.Error -> {
                when (result.error) {
                    CredentialService.CredentialError.NO_AUTHORIZED_CREDENTIAL -> {
                        eventBus.send(AccountDetailsEvent.StartManualSignInFlow)
                    }

                    CredentialService.CredentialError.CREDENTIAL_PROCESS_FAILED -> eventBus.send(
                        AccountDetailsEvent.ShowUiMessage(result.message)
                    )
                }
            }

            is Result.Success -> {
                signInUserWithIdToken(result.data)
            }
        }
    }

    private suspend fun signInUserWithIdToken(idToken: String) {
        when (val result = repo.signUserInWithToken(idToken)) {
            is Result.Error -> {
                eventBus.send(AccountDetailsEvent.ShowUiMessage(result.message))
            }

            is Result.Success -> {
                eventBus.send(AccountDetailsEvent.ShowUiMessage(UiText.StringResource(R.string.sign_in_success)))
            }
        }
    }

    override fun onDeleteAccountClick() {
        savedStateHandle[SHOW_DELETE_ACCOUNT_CONFIRMATION] = true
    }

    override fun onDeleteAccountDismiss() {
        savedStateHandle[SHOW_DELETE_ACCOUNT_CONFIRMATION] = false
    }

    override fun onDeleteAccountConfirm() {
        viewModelScope.launch {
            savedStateHandle[SHOW_DELETE_ACCOUNT_CONFIRMATION] = false
            when (val result = repo.deleteAccount()) {
                is Result.Error -> {
                    eventBus.send(AccountDetailsEvent.ShowUiMessage(result.message))
                }

                is Result.Success -> {
                    eventBus.send(AccountDetailsEvent.AccountDeleted)
                }
            }
        }
    }

    override fun onLogoutClick() {
        savedStateHandle[SHOW_LOGOUT_CONFIRMATION] = true
    }

    override fun onLogoutDismiss() {
        savedStateHandle[SHOW_LOGOUT_CONFIRMATION] = false
    }

    override fun onLogoutConfirm() {
        viewModelScope.launch {
            savedStateHandle[SHOW_LOGOUT_CONFIRMATION] = false
            when (repo.signUserOut()) {
                is Result.Error -> {
                    eventBus.send(AccountDetailsEvent.ShowUiMessage(UiText.StringResource(R.string.error_sign_out_failed)))
                }

                is Result.Success -> {
                    accessTokenService.updateAccessToken(null)
                }
            }
        }
    }

    sealed interface AccountDetailsEvent {
        data class ShowUiMessage(val uiText: UiText) : AccountDetailsEvent
        data object StartManualSignInFlow : AccountDetailsEvent
        data object AccountDeleted : AccountDetailsEvent
    }
}

private const val SHOW_DELETE_ACCOUNT_CONFIRMATION = "SHOW_DELETE_ACCOUNT_CONFIRMATION"
private const val SHOW_LOGOUT_CONFIRMATION = "SHOW_LOGOUT_CONFIRMATION"