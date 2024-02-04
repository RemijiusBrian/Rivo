package dev.ridill.rivo.settings.presentation.security

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.ManualLottieAnim
import dev.ridill.rivo.core.ui.theme.SpacingMedium

@Composable
fun AppLockScreen(
    animProgress: () -> Float,
    onBack: () -> Unit,
    launchAuthentication: () -> Unit
) {
    BackHandler(
        enabled = true,
        onBack = onBack
    )

    Surface {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingMedium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            onClick = launchAuthentication
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    ManualLottieAnim(
                        resId = R.raw.lottie_fingerprint_success,
                        progress = animProgress,
                        modifier = Modifier
                            .size(FingerprintIconSize)
                    )
                }

                Text(
                    text = stringResource(R.string.tap_to_unlock),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private val FingerprintIconSize = 120.dp