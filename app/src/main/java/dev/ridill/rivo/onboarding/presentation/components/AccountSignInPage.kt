package dev.ridill.rivo.onboarding.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.common.SignInButton
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.MediumDisplayText
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.SpacingLarge
import dev.ridill.rivo.core.ui.theme.SpacingMedium

@Composable
fun AccountSignInPage(
    authState: AuthState,
    onSignInClick: () -> Unit,
    onSignInSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingLarge)
    ) {
        MediumDisplayText(
            title = stringResource(R.string.onboarding_page_sign_in_title),
            modifier = Modifier
                .padding(vertical = SpacingMedium)
        )
        Text(
            text = stringResource(R.string.onboarding_page_sign_in_message),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(weight = Float.One)

        GoogleSignInActions(
            authState = authState,
            onSignInClick = onSignInClick,
            onSkipClick = onSignInSkip
        )
    }
}

@Composable
private fun GoogleSignInActions(
    authState: AuthState,
    onSignInClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showSignInOption by remember {
        derivedStateOf { authState is AuthState.UnAuthenticated }
    }
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(visible = showSignInOption) {
            GoogleSignInButton(
                onClick = onSignInClick
            )
        }

        TextButton(
            onClick = onSkipClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = LocalContentColor.current
            )
        ) {
            Text(
                stringResource(
                    id = when (authState) {
                        is AuthState.Authenticated -> R.string.action_next
                        AuthState.UnAuthenticated -> R.string.action_skip
                    }
                )
            )
        }
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            SignInButton(it).apply {
                setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_AUTO)

                setOnClickListener { onClick() }
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewRestoreScreen() {
    RivoTheme {
        AccountSignInPage(
            authState = AuthState.UnAuthenticated,
            onSignInClick = {},
            onSignInSkip = {}
        )
    }
}