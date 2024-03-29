package com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi

import androidx.compose.runtime.Composable

/**
 * A data class implementing this interface represents a specific state of the UI.
 */
interface MVIViewState<I : MVIIntent> {

    /**
     * Defines how the UI should be rendered based on the current state and allows for intentions to be emitted based on user actions.
     */
    @Composable
    fun Compose(intent: (I) -> Unit)
}
