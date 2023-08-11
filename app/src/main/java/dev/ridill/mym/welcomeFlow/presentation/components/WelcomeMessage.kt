package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.One
import dev.ridill.mym.core.ui.components.LargeTitle
import dev.ridill.mym.core.ui.components.LottieAnim
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.SpacingExtraLarge

@Composable
fun WelcomeMessageContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        VerticalSpacer(spacing = SpacingExtraLarge)
        LargeTitle(
            title = stringResource(
                R.string.welcome_flow_stop_welcome_title,
                stringResource(R.string.app_name)
            ),
            modifier = Modifier
                .fillMaxWidth(0.80f)
        )

        VerticalSpacer(weight = Float.One)

        LottieAnim(
            resId = R.raw.lottie_wallet,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}