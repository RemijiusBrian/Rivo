package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.MediumDisplayText
import dev.ridill.mym.core.ui.components.LottieAnim
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingMedium

@Composable
fun WelcomeMessagePage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingLarge),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        MediumDisplayText(
            title = stringResource(
                R.string.welcome_flow_page_welcome_title,
                stringResource(R.string.app_name)
            ),
            modifier = Modifier
                .padding(vertical = SpacingMedium)
        )

        LottieAnim(
            resId = R.raw.lottie_wallet,
            modifier = Modifier
                .fillMaxWidth()
                .height(WelcomeAnimHeight)
        )
    }
}

private val WelcomeAnimHeight = 200.dp