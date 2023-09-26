package dev.ridill.rivo.transactionGroups.presentation.groupDetails

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.LabelledSwitch
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.TransactionGroupDetailsScreenSpec
import dev.ridill.rivo.core.ui.theme.SpacingMedium

@Composable
fun TxGroupDetailsScreen(
    snackbarController: SnackbarController,
    state: TxGroupDetailsState,
    groupName: () -> String,
    actions: TxGroupDetailsActions,
    navigateUp: () -> Unit
) {
    BackHandler(
        enabled = state.editModeActive,
        onBack = actions::onEditDismiss
    )

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    RivoScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(TransactionGroupDetailsScreenSpec.labelRes)) },
                navigationIcon = {
                    if (state.editModeActive) {
                        IconButton(onClick = actions::onEditDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.action_cancel)
                            )
                        }
                    } else {
                        BackArrowButton(onClick = navigateUp)
                    }
                },
                actions = {
                    if (state.editModeActive) {
                        IconButton(onClick = actions::onEditConfirm) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(R.string.cd_save_transaction_group)
                            )
                        }
                    } else {
                        IconButton(onClick = actions::onEditClick) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.cd_edit_transaction_group)
                            )
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarController = snackbarController,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .imePadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(SpacingMedium)
        ) {
            NameField(
                name = groupName,
                onNameChange = actions::onNameChange,
                editModeActive = state.editModeActive
            )
            LabelledSwitch(
                labelRes = R.string.mark_excluded_question,
                checked = state.isExcluded,
                onCheckedChange = actions::onExclusionToggle,
                enabled = state.editModeActive,
                modifier = Modifier
                    .align(Alignment.End)
            )
        }
    }
}

@Composable
private fun NameField(
    name: () -> String,
    onNameChange: (String) -> Unit,
    editModeActive: Boolean,
    modifier: Modifier = Modifier
) {
    TextField(
        value = name(),
        onValueChange = onNameChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        readOnly = !editModeActive,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth(),
        label = { Text(stringResource(R.string.transaction_group_name)) }
    )
}