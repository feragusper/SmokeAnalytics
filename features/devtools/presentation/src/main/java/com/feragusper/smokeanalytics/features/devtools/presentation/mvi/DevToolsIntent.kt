package com.feragusper.smokeanalytics.features.devtools.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

sealed class DevToolsIntent : MVIIntent {
    object FetchUser : DevToolsIntent()
}
