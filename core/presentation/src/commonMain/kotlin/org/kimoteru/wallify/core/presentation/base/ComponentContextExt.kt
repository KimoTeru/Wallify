package org.kimoteru.wallify.core.presentation.base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.Lifecycle as DecomposeLifecycle
import com.arkivanov.essenty.lifecycle.subscribe

fun DecomposeLifecycle.lifecycleOwner(): LifecycleOwner = object : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this).apply {
        this@lifecycleOwner.subscribe(
            onCreate = { currentState = Lifecycle.State.CREATED },
            onStart = { currentState = Lifecycle.State.STARTED },
            onResume = { currentState = Lifecycle.State.RESUMED },
            onPause = { currentState = Lifecycle.State.STARTED },
            onStop = { currentState = Lifecycle.State.CREATED },
            onDestroy = { currentState = Lifecycle.State.DESTROYED }
        )
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
}