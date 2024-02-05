package com.feragusper.smokeanalytics.features.devtools.presentation.process

import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsIntent
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DevToolsProcessHolder @Inject constructor(
    private val fetchSessionUseCase: FetchSessionUseCase,
) : MVIProcessHolder<DevToolsIntent, DevToolsResult> {

    override fun processIntent(intent: DevToolsIntent): Flow<DevToolsResult> = when (intent) {
        DevToolsIntent.FetchUser -> processFetchUser()
    }

    private fun processFetchUser(): Flow<DevToolsResult> = flow {
        emit(DevToolsResult.Loading)
        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> emit(DevToolsResult.UserLoggedOut)
            is Session.LoggedIn -> emit(DevToolsResult.UserLoggedIn(session.user))
        }
    }

}
