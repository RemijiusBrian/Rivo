package dev.ridill.rivo.tags.presentation.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import dev.ridill.rivo.R

@Composable
fun TagSearchField(
    query: () -> String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query(),
        onValueChange = onSearchQueryChange,
        modifier = modifier,
        shape = CircleShape,
        placeholder = { Text(stringResource(R.string.search_tags)) },
        colors = TextFieldDefaults.colors(
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        )
    )
}