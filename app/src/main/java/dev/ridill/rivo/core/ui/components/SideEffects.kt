package dev.ridill.rivo.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
    val observer = remember(lifecycleOwner, *keys) {
        LifecycleEventObserver { _, event ->
            if (event == lifecycleEvent) {
                block()
            }
        }
    }
    val lifecycle = lifecycleOwner.lifecycle
    DisposableEffect(lifecycle, *keys) {
        lifecycle.addObserver(observer)

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

@Composable
fun OnLifecycleResumeEffect(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    vararg keys: Any?,
    block: () -> Unit
) = OnLifecycleEventEffect(
    lifecycleEvent = Lifecycle.Event.ON_RESUME,
    keys = keys,
    lifecycleOwner = lifecycleOwner,
    block = block
)