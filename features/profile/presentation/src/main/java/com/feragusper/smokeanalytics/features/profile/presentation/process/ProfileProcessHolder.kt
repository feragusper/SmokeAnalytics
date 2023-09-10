package com.feragusper.smokeanalytics.features.profile.presentation.process

import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileIntent
import com.feragusper.smokeanalytics.features.profile.presentation.mvi.ProfileResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProfileProcessHolder @Inject constructor(
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val signOutUseCase: SignOutUseCase,
) : MVIProcessHolder<ProfileIntent, ProfileResult> {

    override fun processIntent(intent: ProfileIntent): Flow<ProfileResult> = when (intent) {
        ProfileIntent.FetchUser -> processFetchUser()
        ProfileIntent.SignOut -> processSignOut()
    }

    private fun processFetchUser(): Flow<ProfileResult> = flow {
        emit(ProfileResult.Loading)
        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> emit(ProfileResult.UserLoggedOut)
            is Session.LoggedIn -> emit(ProfileResult.UserLoggedIn(session.user.displayName))
        }
    }

    private fun processSignOut(): Flow<ProfileResult> = flow {
        emit(ProfileResult.Loading)
        signOutUseCase()
        emit(ProfileResult.UserLoggedOut)
    }
}
