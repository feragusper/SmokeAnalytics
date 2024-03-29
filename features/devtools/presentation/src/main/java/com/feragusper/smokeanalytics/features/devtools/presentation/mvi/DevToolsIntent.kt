package com.feragusper.smokeanalytics.features.devtools.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

/**
 * Represents the set of user intentions for interacting with the DevTools feature.
 */
sealed class DevToolsIntent : MVIIntent {

    /**
     * Intent to fetch the current user's session.
     */
    object FetchUser : DevToolsIntent()
}
