package dev.ridill.rivo.settings.presentation.security

import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.components.ManualLottieAnim
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.settings.presentation.settings.rememberBiometricPrompt
import dev.ridill.rivo.settings.presentation.settings.rememberPromptInfo
import kotlinx.coroutines.launch

@Composable
fun AppLockScreen(
    onBack: () -> Unit,
    onAuthSucceeded: (BiometricPrompt.AuthenticationResult) -> Unit,
    onAuthError: (String) -> Unit = {},
    onAuthFailed: () -> Unit = {}
) {
    BackHandler(
        enabled = true,
        onBack = onBack
    )

    val progressAnimatable = remember {
        Animatable(Float.Zero)
    }
    val coroutineScope = rememberCoroutineScope()
    val biometricPrompt = rememberBiometricPrompt(
        onAuthSucceeded = {
            coroutineScope.launch {
                progressAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = ANIM_DURATION)
                )
                onAuthSucceeded(it)
            }
        },
        onAuthError = onAuthError,
        onAuthFailed = onAuthFailed
    )
    val promptInfo = rememberPromptInfo(
        title = stringResource(R.string.fingerprint_title),
        subTitle = stringResource(R.string.fingerprint_subtitle)
    )

    LaunchedEffect(Unit) {
        biometricPrompt.authenticate(promptInfo)
    }

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
                            onClick = { biometricPrompt.authenticate(promptInfo) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    ManualLottieAnim(
                        resId = R.raw.lottie_fingerprint_success,
                        progress = { progressAnimatable.value },
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
private const val ANIM_DURATION = 2000