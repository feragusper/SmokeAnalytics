package com.feragusper.smokeanalytics.features.devtools.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

/**
 * Represents the set of user intentions for interacting with the DevTools feature.
 *
 * This sealed class defines all possible actions a user can perform within the DevTools module,
 * allowing the ViewModel to handle them in a structured manner.
 */
sealed class DevToolsIntent : MVIIntent {

    /**
     * Intent to fetch the current user's session information.
     */
    object FetchUser : DevToolsIntent()
}
