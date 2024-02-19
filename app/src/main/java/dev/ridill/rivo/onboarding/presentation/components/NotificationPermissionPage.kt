package dev.ridill.rivo.onboarding.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.LottieAnim
import dev.ridill.rivo.core.ui.components.MediumDisplayText
import dev.ridill.rivo.core.ui.components.Spacer
import dev.ridill.rivo.core.ui.components.SpacerLarge
import dev.ridill.rivo.core.ui.theme.SpacingLarge
import dev.ridill.rivo.core.ui.theme.SpacingMedium

@Composable
fun NotificationPermissionPage(
    onGivePermissionClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingLarge),
    ) {
        MediumDisplayText(
            title = stringResource(R.string.permission_notification),
            modifier = Modifier
                .padding(vertical = SpacingMedium)
        )

        Text(
            text = stringResource(
                R.string.permission_rationale_notification,
                stringResource(R.string.app_name)
            ),
            style = MaterialTheme.typography.titleLarge
        )

        SpacerLarge()

        LottieAnim(
            resId = R.raw.lottie_notification_bell,
            modifier = Modifier
                .size(NotificationBellAnimSize)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(weight = Float.One)

        if (BuildUtil.isNotificationRuntimePermissionNeeded()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onSkipClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = LocalContentColor.current
                    )
                ) {
                    Text(stringResource(R.string.action_skip))
                }

                Button(
                    onClick = onGivePermissionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(stringResource(R.string.give_permission))
                }
            }
        } else {
            Button(onClick = onGivePermissionClick) {
                Text(stringResource(R.string.i_understand))
            }
        }
    }
}

private val NotificationBellAnimSize = 200.dp