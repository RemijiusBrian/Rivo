package dev.ridill.mym.welcomeFlow.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.VerticalSpacer
import dev.ridill.mym.core.ui.components.simpleFadeIn
import dev.ridill.mym.core.ui.components.simpleFadeOut
import dev.ridill.mym.core.ui.theme.SpacingExtraLarge
import dev.ridill.mym.core.ui.theme.SpacingMedium
import dev.ridill.mym.core.ui.theme.SpacingSmall
import dev.ridill.mym.welcomeFlow.presentation.ContinueAction

@Composable
fun SetBudgetStop(
    input: () -> String,
    onInputChange: (String) -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInputNotEmpty by remember {
        derivedStateOf { input().isNotEmpty() }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingMedium)
    ) {
        Column(
            modifier = modifier
                .matchParentSize()
                .padding(SpacingMedium)
                .verticalScroll(rememberScrollState())
        ) {
            VerticalSpacer(spacing = SpacingExtraLarge)
            LimitInput(
                input = input,
                onValueChange = onInputChange
            )
        }

        AnimatedVisibility(
            visible = isInputNotEmpty,
            modifier = Modifier
                .fillMaxWidth(0.50f)
                .align(Alignment.BottomEnd),
            enter = simpleFadeIn(),
            exit = simpleFadeOut()
        ) {
            ContinueAction(
                icon = Icons.Default.Check,
                onClick = onContinueClick
            )
        }
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
            text = stringResource(R.string.welcome_flow_stop_set_budget_message),
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
            placeholder = { Text(stringResource(R.string.enter_budget)) },
            supportingText = { Text(stringResource(R.string.you_can_change_budget_later_in_settings)) }
        )
    }
}