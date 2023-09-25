package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.MediumDisplayText
import dev.ridill.mym.core.ui.components.SpacerExtraLarge
import dev.ridill.mym.core.ui.components.SpacerSmall
import dev.ridill.mym.core.ui.theme.SpacingLarge
import dev.ridill.mym.core.ui.theme.SpacingMedium

@Composable
fun SetBudgetPage(
    input: () -> String,
    onInputChange: (String) -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInputNotEmpty by remember {
        derivedStateOf { input().isNotEmpty() }
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingLarge)
            .verticalScroll(rememberScrollState())
    ) {
        MediumDisplayText(
            title = stringResource(R.string.welcome_flow_stop_set_budget_title),
            modifier = Modifier
                .padding(vertical = SpacingMedium)
        )
        SpacerExtraLarge()
        LimitInput(
            input = input,
            onValueChange = onInputChange
        )
        SpacerSmall()
        AnimatedVisibility(
            visible = isInputNotEmpty,
            modifier = Modifier
                .align(Alignment.End)
        ) {
            Button(
                onClick = {
                    keyboardController?.hide()
                    onContinueClick()
                }
            ) {
                Text(stringResource(R.string.start_budgeting))
            }
        }
    }
}

@Composable
private fun LimitInput(
    input: () -> String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = LocalContentColor.current
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.welcome_flow_stop_set_budget_message),
            style = MaterialTheme.typography.titleMedium
        )
        SpacerSmall()
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
                focusedTextColor = contentColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = contentColor,
                unfocusedIndicatorColor = contentColor,
                unfocusedPlaceholderColor = contentColor,
                focusedPlaceholderColor = contentColor
            ),
            shape = MaterialTheme.shapes.medium,
            placeholder = { Text(stringResource(R.string.enter_budget)) },
            supportingText = { Text(stringResource(R.string.you_can_change_budget_later_in_settings)) }
        )
    }
}