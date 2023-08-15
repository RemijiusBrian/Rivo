package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.ui.theme.ContentAlpha
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.welcomeFlow.presentation.ContinueAction

@Composable
fun PermissionsRationaleStop(
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingMedium)
    ) {
        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(SpacingMedium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            if (BuildUtil.isNotificationRuntimePermissionNeeded()) {
                PermissionDetails(
                    titleRes = R.string.permission_notification,
                    rationale = stringResource(
                        R.string.permission_rationale_notification,
                        stringResource(R.string.app_name)
                    )
                )
            }
            PermissionDetails(
                titleRes = R.string.permission_sms,
                rationale = stringResource(
                    R.string.permission_rationale_sms_for_expense,
                    stringResource(R.string.app_name)
                )
            )
        }
        ContinueAction(
            icon = Icons.Default.KeyboardArrowRight,
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth(0.50f)
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun PermissionDetails(
    @StringRes titleRes: Int,
    rationale: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = rationale,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.PERCENT_90)
        )
    }
}