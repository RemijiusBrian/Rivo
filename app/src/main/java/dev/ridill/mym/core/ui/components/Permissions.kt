package dev.ridill.mym.core.ui.components

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import dev.ridill.mym.core.ui.util.findActivity

data class PermissionsState(
    private val permissionString: String,
    private val context: Context,
    private val launcher: ManagedActivityResultLauncher<String, Boolean>
) {
    val isGranted: Boolean
        get() = isPermissionGranted(context, permissionString)

    val shouldShowRationale: Boolean
        get() = context.findActivity()
            .shouldShowRequestPermissionRationale(permissionString)

    val isPermanentlyDenied: Boolean
        get() = !isGranted && !shouldShowRationale

    fun requestPermission() = launcher.launch(permissionString)
}

@Composable
fun rememberPermissionsState(
    permissionString: String,
    context: Context = LocalContext.current,
    launcher: ManagedActivityResultLauncher<String, Boolean> = rememberPermissionLauncher(),
): PermissionsState = remember(context, launcher) {
    PermissionsState(
        permissionString = permissionString,
        context = context,
        launcher = launcher
    )
}

@Composable
fun rememberPermissionLauncher(
    onResult: (Boolean) -> Unit = {}
): ManagedActivityResultLauncher<String, Boolean> = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = onResult
)

private fun isPermissionGranted(
    context: Context,
    permissionString: String
): Boolean = ContextCompat.checkSelfPermission(context, permissionString) ==
        PackageManager.PERMISSION_GRANTED