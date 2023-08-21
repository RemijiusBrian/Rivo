package dev.ridill.mym.welcomeFlow.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.work.WorkInfo
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.LargeTitle
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.SnackbarController
import dev.ridill.mym.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.mym.core.ui.components.slideInVerticallyWithFadeIn
import dev.ridill.mym.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.mym.core.ui.components.slideOutVerticallyWithFadeOut
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.settings.domain.modal.BackupDetails
import dev.ridill.mym.welcomeFlow.domain.model.WelcomeFlowStop
import dev.ridill.mym.welcomeFlow.presentation.components.GoogleSignInStop
import dev.ridill.mym.welcomeFlow.presentation.components.RestoreDataStop
import dev.ridill.mym.welcomeFlow.presentation.components.SetBudgetStop
import dev.ridill.mym.welcomeFlow.presentation.components.WelcomeMessageStop

@Composable
fun WelcomeFlowScreen(
    snackbarController: SnackbarController,
    flowStop: WelcomeFlowStop,
    budgetInput: () -> String,
    restoreState: WorkInfo.State?,
    availableBackup: BackupDetails?,
    actions: WelcomeFlowActions
) {
    MYMScaffold(
        modifier = Modifier
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        snackbarController = snackbarController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = flowStop,
                label = "WelcomeFlowTitle",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingLarge),
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontallyWithFadeIn { it }
                            .togetherWith(
                                slideOutVerticallyWithFadeOut { -it }
                            )
                    } else {
                        slideInVerticallyWithFadeIn { -it }
                            .togetherWith(
                                slideOutHorizontallyWithFadeOut { it }
                            )
                    }
                        .using(SizeTransform(false))
                }
            ) { stop ->
                val title = when (stop) {
                    WelcomeFlowStop.WELCOME -> stringResource(
                        R.string.welcome_flow_stop_welcome_title,
                        stringResource(R.string.app_name)
                    )

                    WelcomeFlowStop.GOOGLE_SIGN_IN -> stringResource(R.string.welcome_flow_stop_google_sign_in_title)
                    WelcomeFlowStop.RESTORE_DATA -> stringResource(R.string.welcome_flow_stop_restore_data_title)
                    WelcomeFlowStop.SET_BUDGET -> stringResource(R.string.welcome_flow_stop_set_budget_title)
                }
                LargeTitle(title)
            }
            AnimatedContent(
                targetState = flowStop,
                label = "WelcomeFlowStops",
                modifier = Modifier
                    .fillMaxSize(),
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontallyWithFadeIn { it }
                            .togetherWith(
                                slideOutHorizontallyWithFadeOut { -it }
                            )
                    } else {
                        slideInHorizontallyWithFadeIn { -it }
                            .togetherWith(
                                slideOutHorizontallyWithFadeOut { it }
                            )
                    }
                        .using(SizeTransform(false))
                }
            ) { stop ->
                when (stop) {
                    WelcomeFlowStop.WELCOME -> {
                        WelcomeMessageStop(
                            onContinueClick = actions::onWelcomeMessageContinue
                        )
                    }

                    WelcomeFlowStop.GOOGLE_SIGN_IN -> {
                        GoogleSignInStop(
                            onSignInClick = actions::onGoogleSignInClick,
                            onSkipClick = actions::onSkipGoogleSignInClick,
                        )
                    }

                    WelcomeFlowStop.RESTORE_DATA -> {
                        RestoreDataStop(
                            restoreState = restoreState,
                            onSkipClick = actions::onSkipDataRestore,
                            availableBackup = availableBackup,
                            onRestoreClick = actions::onRestoreDataClick
                        )
                    }

                    WelcomeFlowStop.SET_BUDGET -> {
                        SetBudgetStop(
                            input = budgetInput,
                            onInputChange = actions::onBudgetInputChange,
                            onContinueClick = actions::onSetBudgetContinue
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContinueAction(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(
                role = Role.Button,
                onClick = onClick,
                onClickLabel = stringResource(R.string.cd_action_continue)
            )
            .padding(SpacingMedium),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(FloatingActionButtonDefaults.LargeIconSize),
            tint = LocalContentColor.current
        )
    }
}