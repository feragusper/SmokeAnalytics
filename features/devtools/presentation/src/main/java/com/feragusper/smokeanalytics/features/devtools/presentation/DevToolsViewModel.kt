package com.feragusper.smokeanalytics.features.devtools.presentation

import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsIntent
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsResult
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsViewState
import com.feragusper.smokeanalytics.features.devtools.presentation.navigation.DevToolsNavigator
import com.feragusper.smokeanalytics.features.devtools.presentation.process.DevToolsProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DevToolsViewModel @Inject constructor(
    private val processHolder: DevToolsProcessHolder,
) : MVIViewModel<DevToolsIntent, DevToolsViewState, DevToolsResult, DevToolsNavigator>(initialState = DevToolsViewState()) {

    override lateinit var navigator: DevToolsNavigator

    init {
        intents().trySend(DevToolsIntent.FetchUser)
    }

    override suspend fun transformer(intent: DevToolsIntent) = processHolder.processIntent(intent)

    override suspend fun reducer(
        previous: DevToolsViewState,
        result: DevToolsResult,
    ): DevToolsViewState = when (result) {
        is DevToolsResult.UserLoggedIn -> previous.copy(
            displayLoading = false,
            currentUser = result.user?.let {
                DevToolsViewState.User(
                    id = it.id,
                    email = it.email,
                )
            }
        )

        DevToolsResult.UserLoggedOut -> previous.copy(
            displayLoading = false,
            currentUser = null,
        )

        DevToolsResult.Loading -> previous.copy(displayLoading = true)
    }
}
