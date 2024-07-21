package dev.ridill.rivo.account.presentation.accountDetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.BodyMediumText
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.GoogleSignInButton
import dev.ridill.rivo.core.ui.components.RivoImage
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SmallDisplayText
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerLarge
import dev.ridill.rivo.core.ui.components.SpacerMedium
import dev.ridill.rivo.core.ui.components.icons.Google
import dev.ridill.rivo.core.ui.navigation.destinations.AccountDetailsScreenSpec
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.settings.presentation.components.BasicPreference
import dev.ridill.rivo.settings.presentation.components.PreferenceIconSize
import dev.ridill.rivo.settings.presentation.components.SimpleSettingsPreference

@Composable
fun AccountDetailsScreen(
    snackbarController: SnackbarController,
    state: AccountDetailsState,
    actions: AccountDetailsActions,
    navigateUp: () -> Unit
) {
    RivoScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(AccountDetailsScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) }
            )
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(visible = state.isAccountAuthenticated) {
                AccountDetails(
                    photoUrl = state.photoUrl,
                    displayName = state.displayName,
                    email = state.email,
                    modifier = Modifier
                        .padding(horizontal = SpacingMedium)
                )
            }

            AnimatedVisibility(visible = !state.isAccountAuthenticated) {
                AccountInfoText()
            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = SpacingMedium)
            )

            SpacerLarge()

            AnimatedVisibility(
                visible = !state.isAccountAuthenticated,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                GoogleSignInButton(
                    onClick = actions::onSignInClick
                )
            }

            AnimatedVisibility(visible = state.isAccountAuthenticated) {
                AccountActions(
                    onDeleteClick = actions::onDeleteAccountClick,
                    onLogoutClick = actions::onLogoutClick
                )
            }
        }

        if (state.showAccountDeleteConfirmation) {
            ConfirmationDialog(
                titleRes = R.string.account_delete_confirmation_title,
                contentRes = R.string.account_delete_confirmation_message,
                onConfirm = actions::onDeleteAccountConfirm,
                onDismiss = actions::onDeleteAccountDismiss
            )
        }

        if (state.showLogoutConfirmation) {
            ConfirmationDialog(
                titleRes = R.string.account_logout_confirmation_title,
                contentRes = R.string.account_logout_confirmation_message,
                onConfirm = actions::onLogoutConfirm,
                onDismiss = actions::onLogoutDismiss
            )
        }
    }
}

@Composable
private fun AccountInfoText(modifier: Modifier = Modifier) {
    BasicPreference(
        titleContent = { Text(stringResource(R.string.preference_title_google_sign_in)) },
        summaryContent = {
            Text(
                text = stringResource(
                    R.string.preference_google_sign_in_message,
                    stringResource(R.string.app_name)
                )
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Google,
                contentDescription = null,
                modifier = Modifier
                    .size(PreferenceIconSize)
            )
        },
        titleTextStyle = MaterialTheme.typography.titleMedium,
        summaryTextStyle = MaterialTheme.typography.bodyLarge,
        modifier = modifier,
        verticalAlignment = Alignment.Top
    )
}

@Composable
private fun AccountDetails(
    photoUrl: String?,
    displayName: String,
    email: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RivoImage(
            url = photoUrl.orEmpty(),
            contentDescription = stringResource(R.string.cd_profile_picture),
            size = ProfilePictureSize,
            modifier = Modifier,
            placeholderRes = R.drawable.ic_rounded_person,
        )

        SpacerMedium()

        SmallDisplayText(displayName)
        BodyMediumText(email)
    }
}

private val ProfilePictureSize = 80.dp

@Composable
private fun AccountActions(
    onDeleteClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        /*SimpleSettingsPreference(
            titleRes = R.string.preference_delete_account,
            summary = stringResource(R.string.preference_delete_account_summary),
            leadingIcon = Icons.Rounded.DeleteForever,
            onClick = onDeleteClick
        )*/

        SimpleSettingsPreference(
            titleRes = R.string.preference_logout,
            summary = stringResource(R.string.preference_logout_summary),
            leadingIcon = Icons.AutoMirrored.Rounded.Logout,
            onClick = onLogoutClick,
            contentColor = MaterialTheme.colorScheme.error
        )
    }
}