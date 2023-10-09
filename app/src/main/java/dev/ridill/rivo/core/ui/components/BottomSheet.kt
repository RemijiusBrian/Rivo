package dev.ridill.rivo.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.ui.theme.ElevationLevel0
import dev.ridill.rivo.core.ui.theme.SpacingMedium
import dev.ridill.rivo.core.ui.util.UiText

@Composable
fun ValueInputSheet(
    @StringRes titleRes: Int,
    inputValue: () -> String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    singleLine: Boolean = true,
    placeholder: String? = null,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    @StringRes actionLabel: Int = R.string.action_confirm,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) = ValueInputSheet(
    title = {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(horizontal = SpacingMedium)
        )
    },
    inputValue = inputValue,
    onValueChange = onValueChange,
    onDismiss = onDismiss,
    actionButton = {
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingMedium)
        ) {
            Text(stringResource(actionLabel))
        }
    },
    modifier = modifier,
    text = text,
    focusRequester = focusRequester,
    errorMessage = errorMessage,
    singleLine = singleLine,
    placeholder = placeholder,
    label = label,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    visualTransformation = visualTransformation,
    prefix = prefix,
    suffix = suffix,
    contentAfterTextField = contentAfterTextField
)

@Composable
fun ValueInputSheet(
    title: @Composable () -> Unit,
    inputValue: () -> String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    actionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    singleLine: Boolean = true,
    placeholder: String? = null,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) {
    val isInputEmpty by remember {
        derivedStateOf { inputValue().isEmpty() }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingMedium),
            modifier = Modifier
                .padding(vertical = SpacingMedium)
        ) {
            title()

            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = SpacingMedium)
                )
            }

            OutlinedTextField(
                value = inputValue(),
                onValueChange = onValueChange,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
                    .focusRequester(focusRequester),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                label = label?.let { { Text(it) } },
                supportingText = { errorMessage?.let { Text(it.asString()) } },
                isError = errorMessage != null,
                placeholder = placeholder?.let { { Text(it) } },
                singleLine = singleLine,
                trailingIcon = {
                    if (!isInputEmpty) {
                        IconButton(onClick = { onValueChange(String.Empty) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_clear)
                            )
                        }
                    }
                },
                visualTransformation = visualTransformation,
                prefix = prefix,
                suffix = suffix
            )

            contentAfterTextField?.invoke(this)

            actionButton()
        }
    }
}

@Composable
fun <T> ListSearchSheet(
    searchQuery: () -> String,
    onSearchQueryChange: (String) -> Unit,
    itemsList: List<T>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    onSearch: (String) -> Unit = {},
    active: Boolean = true,
    onActiveChange: (Boolean) -> Unit = {},
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    val isSearchQueryEmpty by remember {
        derivedStateOf { searchQuery().isEmpty() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        SearchBar(
            query = searchQuery(),
            onQueryChange = onSearchQueryChange,
            onSearch = onSearch,
            active = active,
            onActiveChange = onActiveChange,
            trailingIcon = {
                if (!isSearchQueryEmpty) {
                    IconButton(onClick = { onSearchQueryChange(String.Empty) }) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = stringResource(R.string.cd_clear)
                        )
                    }
                }
            },
            placeholder = { placeholder?.let { Text(it) } },
            tonalElevation = ElevationLevel0
        ) {
            LazyColumn {
                items(items = itemsList, key = itemKey) { item ->
                    itemContent(item)
                }
            }
        }
    }
}

@Composable
fun ListSearchSheet(
    searchQuery: () -> String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    onSearch: (String) -> Unit = {},
    active: Boolean = true,
    onActiveChange: (Boolean) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    val isSearchQueryEmpty by remember {
        derivedStateOf { searchQuery().isEmpty() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        SearchBar(
            query = searchQuery(),
            onQueryChange = onSearchQueryChange,
            onSearch = onSearch,
            active = active,
            onActiveChange = onActiveChange,
            trailingIcon = {
                if (!isSearchQueryEmpty) {
                    IconButton(onClick = { onSearchQueryChange(String.Empty) }) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = stringResource(R.string.cd_clear)
                        )
                    }
                }
            },
            placeholder = { placeholder?.let { Text(it) } },
            tonalElevation = ElevationLevel0
        ) {
            LazyColumn(
                state = listState,
                contentPadding = contentPadding,
                reverseLayout = reverseLayout,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                flingBehavior = flingBehavior,
                userScrollEnabled = userScrollEnabled,
                content = content
            )
        }
    }
}