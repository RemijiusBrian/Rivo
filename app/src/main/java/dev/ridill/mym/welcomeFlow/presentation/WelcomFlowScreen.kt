package dev.ridill.mym.welcomeFlow.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.ui.components.MYMScaffold
import dev.ridill.mym.core.ui.components.SnackbarController
import dev.ridill.mym.core.ui.theme.BorderWidthStandard
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.settings.domain.modal.BackupDetails
import dev.ridill.mym.welcomeFlow.domain.model.WelcomeFlowPage
import dev.ridill.mym.welcomeFlow.presentation.components.GoogleSignInPage
import dev.ridill.mym.welcomeFlow.presentation.components.NotificationPermissionPage
import dev.ridill.mym.welcomeFlow.presentation.components.SetBudgetPage
import dev.ridill.mym.welcomeFlow.presentation.components.WelcomeMessagePage

@Composable
fun WelcomeFlowScreen(
    snackbarController: SnackbarController,
    pagerState: PagerState,
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(Float.One)
            ) { page ->
                when (page) {
                    WelcomeFlowPage.WELCOME.ordinal -> {
                        WelcomeMessagePage()
                    }

                    WelcomeFlowPage.NOTIFICATION_PERMISSION.ordinal -> {
                        NotificationPermissionPage(
                            onGivePermissionClick = actions::onGiveNotificationPermissionClick,
                            onSkipClick = actions::onSkipNotificationPermission
                        )
                    }

                    WelcomeFlowPage.GOOGLE_SIGN_IN.ordinal -> {
                        GoogleSignInPage(
                            onSignInClick = actions::onGoogleSignInClick,
                            onSkipSignInClick = actions::onSkipGoogleSignInClick,
                            restoreWorkerState = restoreState,
                            onRestoreClick = actions::onRestoreDataClick,
                            onSkipRestoreClick = actions::onSkipDataRestore,
                            availableBackupDetails = availableBackup
                        )
                    }

                    WelcomeFlowPage.SET_BUDGET.ordinal -> {
                        SetBudgetPage(
                            input = budgetInput,
                            onInputChange = actions::onBudgetInputChange,
                            onContinueClick = actions::onSetBudgetContinue
                        )
                    }
                }
            }

            WelcomeFlowProgress(
                pageCount = pagerState.pageCount,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WelcomeFlowProgress(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingMedium, Alignment.CenterHorizontally)
    ) {
        repeat(pageCount) { page ->
            val isCurrentOrPrevious = remember(currentPage) {
                page <= currentPage
            }
            val backgroundColor by animateColorAsState(
                targetValue = if (isCurrentOrPrevious) MaterialTheme.colorScheme.onPrimaryContainer
                else Color.Transparent,
                label = "PageIndicatorBackgroundColor"
            )
            Box(
                modifier = Modifier
                    .size(WelcomeFlowProgressIndicatorSize)
                    .clip(CircleShape)
                    .border(
                        width = BorderWidthStandard,
                        color = LocalContentColor.current,
                        shape = CircleShape
                    )
                    .drawBehind {
                        drawCircle(color = backgroundColor)
                    }
            )
        }
    }
}

private val WelcomeFlowProgressIndicatorSize = 12.dp