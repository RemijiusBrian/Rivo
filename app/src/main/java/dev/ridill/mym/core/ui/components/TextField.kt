package dev.ridill.mym.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MinWidthTextField(
    value: () -> String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    iconPadding: Dp = DefaultIconPadding,
    shape: CornerBasedShape = MaterialTheme.shapes.medium,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val textStyleWithContentColor = remember(textStyle, contentColor) {
        textStyle.copy(color = contentColor)
    }
    BasicTextField(
        value = value(),
        onValueChange = onValueChange,
        modifier = modifier
            .width(IntrinsicSize.Min),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyleWithContentColor,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(LocalContentColor.current),
        decorationBox = { innerTextField ->
            val showPlaceholder by remember {
                derivedStateOf { value().isEmpty() }
            }
            CompositionLocalProvider(
                LocalTextStyle provides textStyleWithContentColor,
                LocalContentColor provides contentColor
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .clip(shape)
                        .background(containerColor)
                        .padding(TextFieldDefaults.contentPaddingWithoutLabel())
                ) {
                    leadingIcon?.let { icon ->
                        icon()
                        HorizontalSpacer(spacing = iconPadding)
                    }
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        placeholder?.let {
                            CompositionLocalProvider(
                                LocalContentColor provides contentColor
                                    .copy(alpha = 0.40f)
                            ) {
                                this@Row.AnimatedVisibility(visible = showPlaceholder) {
                                    it.invoke()
                                }
                            }
                        }
                        innerTextField()
                    }
                    trailingIcon?.let { icon ->
                        HorizontalSpacer(spacing = iconPadding)
                        icon()
                    }
                }
            }
        }
    )
}

private val DefaultIconPadding = 12.dp