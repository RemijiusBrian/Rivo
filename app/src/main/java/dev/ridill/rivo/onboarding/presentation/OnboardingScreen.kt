package dev.ridill.rivo.onboarding.presentation

import android.app.Activity
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import dev.ridill.rivo.core.domain.model.AuthState
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.MultiplePermissionsState
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.theme.BorderWidthStandard
import dev.ridill.rivo.core.ui.theme.PrimaryBrandColor
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.theme.contentColor
import dev.ridill.rivo.onboarding.domain.model.DataRestoreState
import dev.ridill.rivo.onboarding.domain.model.OnboardingPage
import dev.ridill.rivo.onboarding.presentation.components.AccountSignInPage
import dev.ridill.rivo.onboarding.presentation.components.DataRestorePage
import dev.ridill.rivo.onboarding.presentation.components.PermissionsPage
import dev.ridill.rivo.onboarding.presentation.components.SetBudgetPage
import dev.ridill.rivo.onboarding.presentation.components.WelcomeMessagePage
import java.util.Currency

@Composable
fun OnboardingScreen(
    snackbarController: SnackbarController,
    pagerState: PagerState,
    permissionsState: MultiplePermissionsState,
    authState: AuthState,
    restoreState: DataRestoreState,
    showEncryptionPasswordInput: Boolean,
    currency: Currency,
    budgetInput: () -> String,
    onSignInClick: () -> Unit,
    actions: OnboardingActions
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        // Set isAppearanceLightStatusBars = true for Onboarding Screen only
//      // and revert it back to original value when screen disposes
        DisposableEffect(view) {
            val window = (view.context as Activity).window
            val originalLightStatusBarValue = WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            onDispose {
                WindowCompat.getInsetsController(window, view)
                    .isAppearanceLightStatusBars = originalLightStatusBarValue
            }
        }
    }
    CompositionLocalProvider(
        LocalContentColor provides PrimaryBrandColor.contentColor()
    ) {
        RivoScaffold(
            modifier = Modifier
                .imePadding(),
            containerColor = PrimaryBrandColor,
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
                        OnboardingPage.WELCOME.ordinal -> {
                            WelcomeMessagePage()
                        }

                        OnboardingPage.APP_PERMISSIONS.ordinal -> {
                            PermissionsPage(
                                permissionsState = permissionsState,
                                onGivePermissionClick = actions::onGivePermissionsClick,
                                onSkipClick = actions::onSkipPermissionsClick
                            )
                        }

                        OnboardingPage.ACCOUNT_SIGN_IN.ordinal -> {
                            AccountSignInPage(
                                authState = authState,
                                onSignInClick = onSignInClick,
                                onSignInSkip = actions::onSkipSignInClick,
                            )
                        }

                        OnboardingPage.DATA_RESTORE.ordinal -> {
                            DataRestorePage(
                                restoreState = restoreState,
                                onCheckForBackupClick = actions::onCheckOrRestoreClick,
                                onSkipClick = actions::onDataRestoreSkip,
                                showEncryptionPasswordInput = showEncryptionPasswordInput,
                                onEncryptionPasswordInputDismiss = actions::onEncryptionPasswordInputDismiss,
                                onEncryptionPasswordSubmit = actions::onEncryptionPasswordSubmit
                            )
                        }

                        OnboardingPage.SET_BUDGET.ordinal -> {
                            SetBudgetPage(
                                currency = currency,
                                input = budgetInput,
                                onInputChange = actions::onBudgetInputChange,
                                onStartBudgetingClick = actions::onStartBudgetingClick
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
                targetValue = if (isCurrentOrPrevious) LocalContentColor.current
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