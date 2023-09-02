package dev.ridill.mym.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics

@Composable
fun LabelledRadioButton(
    @StringRes labelRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
) = LabelledRadioButton(
    label = stringResource(labelRes),
    selected = selected,
    onClick = onClick,
    modifier = modifier,
    shape = shape
)

@Composable
fun LabelledRadioButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
) {
    Row(
        modifier = Modifier
            .clip(shape)
            .toggleable(
                value = selected,
                role = Role.RadioButton,
                onValueChange = { onClick() }
            )
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = Modifier
                .clearAndSetSemantics {}
        )
        SpacerSmall()
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
    }
}