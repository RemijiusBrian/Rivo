package dev.ridill.rivo.core.ui.components

import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.ridill.rivo.core.ui.util.isPermissionGranted

data class MultiplePermissionsState(
    private val permissions: List<String>,
    private val context: Context,
    private val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    val isAllPermissionsGranted: Boolean
        get() = permissions.all { context.isPermissionGranted(it) }

    fun launchRequest() = launcher.launch(permissions.toTypedArray())
}

@Composable
fun rememberMultiplePermissionsState(
    permissions: List<String>,
    context: Context = LocalContext.current,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> = rememberMultiplePermissionsLauncher(),
): MultiplePermissionsState = remember(context, launcher) {
    MultiplePermissionsState(
        permissions = permissions,
        context = context,
        launcher = launcher
    )
}

@Composable
fun rememberMultiplePermissionsLauncher(
    onResult: (Map<String, Boolean>) -> Unit = {}
): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = onResult
    )