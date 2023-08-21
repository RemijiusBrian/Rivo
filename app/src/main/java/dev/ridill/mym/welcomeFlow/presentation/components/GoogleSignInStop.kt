package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.common.SignInButton
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.SpacingLarge

@Composable
fun GoogleSignInStop(
    onSignInClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingLarge)
    ) {
        Text(
            text = stringResource(R.string.welcome_flow_stop_google_sign_in_message),
            style = MaterialTheme.typography.headlineSmall
        )

        VerticalSpacer(weight = Float.One)

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GoogleSignInButton(onClick = onSignInClick)

            TextButton(onClick = onSkipClick) {
                Text(stringResource(R.string.action_skip))
            }
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