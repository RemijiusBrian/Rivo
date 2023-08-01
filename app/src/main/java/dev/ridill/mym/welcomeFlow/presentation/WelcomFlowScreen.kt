package dev.ridill.mym.welcomeFlow.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.PermissionRationaleDialog
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.SpacingExtraLarge
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.welcomeFlow.domain.model.WelcomeFlowStop

@Composable
fun WelcomeFlowScreen(
    snackbarHostState: SnackbarHostState,
    flowStop: WelcomeFlowStop,
    limitInput: () -> String,
    showPermissionRationale: Boolean,
    actions: WelcomeFlowActions
) {
    MYMScaffold(
        modifier = Modifier
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            val isLimitInputEmpty by remember {
                derivedStateOf { limitInput().isEmpty() }
            }
            LargeFloatingActionButton(
                onClick = actions::onNextClick,
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = Dp.Zero,
                    pressedElevation = Dp.Zero,
                    focusedElevation = Dp.Zero,
                    hoveredElevation = Dp.Zero
                )
            ) {
                AnimatedContent(targetState = flowStop, label = "WelcomeFlowNextButton") { stop ->
                    val imageVector = when (stop) {
                        WelcomeFlowStop.WELCOME -> Icons.Default.KeyboardArrowRight
                        WelcomeFlowStop.LIMIT_SET -> {
                            if (isLimitInputEmpty) Icons.Default.KeyboardArrowRight
                            else Icons.Default.Check
                        }
                    }

                    Icon(
                        imageVector = imageVector,
                        contentDescription = stringResource(R.string.cd_action_continue),
                        modifier = Modifier
                            .size(FloatingActionButtonDefaults.LargeIconSize)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(SpacingLarge)
        ) {
            Crossfade(
                targetState = flowStop,
                label = "FlowStopTitleText",
                modifier = Modifier
                    .padding(top = SpacingExtraLarge)
            ) { stop ->
                val title = when (stop) {
                    WelcomeFlowStop.WELCOME -> stringResource(
                        R.string.welcome_flow_stop_welcome_title,
                        stringResource(R.string.app_name)
                    )

                    WelcomeFlowStop.LIMIT_SET -> stringResource(R.string.set_a_monthly_limit)
                }
                FlowTitle(
                    title = title,
                    modifier = Modifier
                        .fillMaxHeight(TITLE_WIDTH_FRACTION)
                )
            }

            VerticalSpacer(spacing = SpacingMedium)

            AnimatedVisibility(
                visible = flowStop == WelcomeFlowStop.LIMIT_SET,
                enter = slideInVertically { it / 2 } + fadeIn(),
                exit = slideOutVertically { it / 2 } + fadeOut()
            ) {
                LimitInput(
                    input = limitInput,
                    onValueChange = actions::onLimitAmountChange
                )
            }
        }

        if (showPermissionRationale) {
            PermissionRationaleDialog(
                icon = Icons.Rounded.Notifications,
                textRes = R.string.permission_rationale_notification,
                onDismiss = actions::onNotificationRationaleDismiss,
                onAgree = actions::onNotificationRationaleAgree
            )
        }
    }
}

@Composable
private fun FlowTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.displayMedium,
        modifier = modifier
    )
}

private const val TITLE_WIDTH_FRACTION = 0.40f

@Composable
private fun LimitInput(
    input: () -> String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.welcome_flow_stop_limit_set_message),
            style = MaterialTheme.typography.titleMedium
        )
        VerticalSpacer(spacing = SpacingSmall)
        TextField(
            value = input(),
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = MaterialTheme.shapes.medium,
            placeholder = { Text(stringResource(R.string.enter_monthly_limit)) },
            supportingText = { Text(stringResource(R.string.you_can_change_limit_later_in_settings)) }
        )
    }
}