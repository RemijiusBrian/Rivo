package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.ridill.mym.core.ui.components.LargeTitle
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.SpacingExtraLarge

@Composable
fun EnableTestingFeaturesContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        VerticalSpacer(spacing = SpacingExtraLarge)
        LargeTitle(
            title = stringResource(
                com.google.firebase.appdistribution.impl.R.string.signin_dialog_title
            ),
            modifier = Modifier
                .fillMaxWidth(0.80f)
        )

        VerticalSpacer(spacing = SpacingExtraLarge)

        Text(
            text = stringResource(
                com.google.firebase.appdistribution.impl.R.string.singin_dialog_message
            ),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}