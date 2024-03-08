package dev.ridill.rivo.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
fun RivoScaffold(
    modifier: Modifier = Modifier,
    snackbarController: SnackbarController = rememberSnackbarController(),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = { RivoSnackbarHost(snackbarController.snackbarHostState) },
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit
) = Scaffold(
    modifier = modifier,
    topBar = topBar,
    bottomBar = bottomBar,
    snackbarHost = snackbarHost,
    floatingActionButton = floatingActionButton,
    floatingActionButtonPosition = floatingActionButtonPosition,
    containerColor = containerColor,
    contentColor = contentColor,
    contentWindowInsets = contentWindowInsets,
    content = content
)

@Composable
fun RivoBottomSheetScaffold(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    snackbarController: SnackbarController = rememberSnackbarController(),
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(snackbarHostState = snackbarController.snackbarHostState),
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    sheetShape: Shape = BottomSheetDefaults.ExpandedShape,
    sheetContainerColor: Color = BottomSheetDefaults.ContainerColor,
    sheetContentColor: Color = contentColorFor(sheetContainerColor),
    sheetTonalElevation: Dp = BottomSheetDefaults.Elevation,
    sheetShadowElevation: Dp = BottomSheetDefaults.Elevation,
    sheetDragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    sheetSwipeEnabled: Boolean = true,
    topBar: @Composable () -> Unit = {},
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { RivoSnackbarHost(it) },
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable (PaddingValues) -> Unit
) = BottomSheetScaffold(
    sheetContent = sheetContent,
    modifier = modifier,
    scaffoldState = scaffoldState,
    sheetPeekHeight = sheetPeekHeight,
    sheetMaxWidth = sheetMaxWidth,
    sheetShape = sheetShape,
    sheetContainerColor = sheetContainerColor,
    sheetContentColor = sheetContentColor,
    sheetTonalElevation = sheetTonalElevation,
    sheetShadowElevation = sheetShadowElevation,
    sheetDragHandle = sheetDragHandle,
    sheetSwipeEnabled = sheetSwipeEnabled,
    topBar = topBar,
    snackbarHost = snackbarHost,
    containerColor = containerColor,
    contentColor = contentColor,
    content = content
)