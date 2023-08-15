package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.LottieAnim
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.SpacingExtraLarge
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.welcomeFlow.presentation.ContinueAction

@Composable
fun WelcomeMessageStop(
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingMedium),
        verticalArrangement = Arrangement.Bottom
    ) {
        LottieAnim(
            resId = R.raw.lottie_wallet,
            modifier = Modifier
                .fillMaxWidth()
                .height(WelcomeAnimHeight)
        )
        VerticalSpacer(spacing = SpacingExtraLarge)
        ContinueAction(
            icon = Icons.Default.KeyboardArrowRight,
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth(0.50f)
                .align(Alignment.End)
        )
    }
}

private val WelcomeAnimHeight = 200.dp