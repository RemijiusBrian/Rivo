package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.LargeTitle
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.theme.SpacingExtraLarge
import dev.ridill.mym.core.ui.theme.SpacingSmall

@Composable
fun IncomeInputContent(
    input: () -> String,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        VerticalSpacer(spacing = SpacingExtraLarge)
        LargeTitle(
            title = stringResource(
                R.string.welcome_flow_stop_set_income_title,
                stringResource(R.string.app_name)
            ),
            modifier = Modifier
        )

        VerticalSpacer(spacing = SpacingExtraLarge)

        LimitInput(
            input = input,
            onValueChange = onInputChange
        )
    }
}

@Composable
private fun LimitInput(
    input: () -> String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.welcome_flow_stop_set_income_message),
            style = MaterialTheme.typography.titleMedium
        )
        VerticalSpacer(spacing = SpacingSmall)
        TextField(
            value = input(),
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = MaterialTheme.shapes.medium,
            placeholder = { Text(stringResource(R.string.enter_income)) },
            supportingText = { Text(stringResource(R.string.you_can_change_income_later_in_settings)) }
        )
    }
}