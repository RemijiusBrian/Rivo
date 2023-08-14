package dev.ridill.mym.welcomeFlow.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.work.WorkInfo
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.PermissionRationaleDialog
import dev.ridill.mym.core.ui.components.SnackbarController
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.welcomeFlow.domain.model.WelcomeFlowStop
import dev.ridill.mym.welcomeFlow.presentation.components.IncomeInputContent
import dev.ridill.mym.welcomeFlow.presentation.components.RestoreDataContent
import dev.ridill.mym.welcomeFlow.presentation.components.WelcomeMessageContent

@Composable
fun WelcomeFlowScreen(
    snackbarController: SnackbarController,
    flowStop: WelcomeFlowStop,
    incomeInput: () -> String,
    showPermissionRationale: Boolean,
    showNextButton: Boolean,
    restoreState: WorkInfo.State,
    actions: WelcomeFlowActions
) {
    MYMScaffold(
        modifier = Modifier
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        snackbarController = snackbarController,
        floatingActionButton = {
            val isLimitInputEmpty by remember {
                derivedStateOf { incomeInput().isEmpty() }
            }
            if (showNextButton) {
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
                    val imageVector = when (flowStop) {
                        WelcomeFlowStop.WELCOME -> Icons.Default.KeyboardArrowRight
                        WelcomeFlowStop.RESTORE_DATA -> Icons.Default.KeyboardArrowRight
                        WelcomeFlowStop.INCOME_SET -> {
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
            AnimatedContent(
                targetState = flowStop,
                label = "WelcomeFlowStops"
            ) { stop ->
                when (stop) {
                    WelcomeFlowStop.WELCOME -> {
                        WelcomeMessageContent(
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }

                    WelcomeFlowStop.RESTORE_DATA -> {
                        RestoreDataContent(
                            onCheckForBackupClick = actions::onCheckForBackupClick,
                            onSkipClick = actions::onSkipDataRestore,
                            modifier = Modifier
                                .fillMaxSize(),
                            restoreState = restoreState
                        )
                    }

                    WelcomeFlowStop.INCOME_SET -> {
                        IncomeInputContent(
                            input = incomeInput,
                            onInputChange = actions::onIncomeInputChange,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
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