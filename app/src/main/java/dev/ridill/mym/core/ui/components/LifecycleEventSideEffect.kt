package dev.ridill.mym.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun OnLifecycleEventEffect(
    lifecycleEvent: Lifecycle.Event,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    vararg keys: Any?,
    block: () -> Unit
) {
    DisposableEffect(lifecycleOwner, *keys) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == lifecycleEvent) {
                block()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun OnLifecycleStartEffect(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    vararg keys: Any?,
    block: () -> Unit
) = OnLifecycleEventEffect(
    lifecycleEvent = Lifecycle.Event.ON_START,
    keys = keys,
    lifecycleOwner = lifecycleOwner,
    block = block
)