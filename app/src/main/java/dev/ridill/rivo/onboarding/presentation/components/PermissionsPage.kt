package dev.ridill.rivo.onboarding.presentation.components

import android.Manifest
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.MediumDisplayText
import dev.ridill.rivo.core.ui.components.MultiplePermissionsState
import dev.ridill.rivo.core.ui.components.SpacerMedium
import dev.ridill.rivo.core.ui.components.icons.Message
import dev.ridill.rivo.core.ui.theme.PrimaryBrandColor
import dev.ridill.rivo.core.ui.theme.contentColor
import dev.ridill.rivo.core.ui.theme.spacing

@Composable
fun PermissionsPage(
    permissionsState: MultiplePermissionsState,
    onGivePermissionClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val areAllPermissionsGranted by remember {
        derivedStateOf { permissionsState.areAllPermissionsGranted }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large),
    ) {
        MediumDisplayText(
            text = stringResource(R.string.onboarding_page_permissions_title),
            modifier = Modifier
                .padding(vertical = MaterialTheme.spacing.medium)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(Float.One)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            permissionsState.permissions.forEach { permission ->
                val icon = when (permission) {
                    Manifest.permission.POST_NOTIFICATIONS -> Icons.Rounded.Notifications
                    Manifest.permission.RECEIVE_SMS -> Icons.Rounded.Message
                    else -> null
                }
                val titleRes = when (permission) {
                    Manifest.permission.POST_NOTIFICATIONS -> R.string.permission_notification
                    Manifest.permission.RECEIVE_SMS -> R.string.permission_sms
                    else -> null
                }
                val messageRes = when (permission) {
                    Manifest.permission.POST_NOTIFICATIONS -> R.string.permission_rationale_notification
                    Manifest.permission.RECEIVE_SMS -> R.string.permission_rationale_read_sms
                    else -> null
                }
                if (icon != null && titleRes != null && messageRes != null) {
                    PermissionDetails(
                        icon = icon,
                        titleRes = titleRes,
                        message = stringResource(messageRes, stringResource(R.string.app_name)),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }

        if (!areAllPermissionsGranted) {
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
            Button(
                onClick = onGivePermissionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                Text(stringResource(R.string.i_understand))
            }
        }
    }
}

@Composable
private fun PermissionDetails(
    icon: ImageVector,
    @StringRes titleRes: Int,
    message: String,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalContentColor provides PrimaryBrandColor.contentColor()
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
            )

            SpacerMedium()

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}