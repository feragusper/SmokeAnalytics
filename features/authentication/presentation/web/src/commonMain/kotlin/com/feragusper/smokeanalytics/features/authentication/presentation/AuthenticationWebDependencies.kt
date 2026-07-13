package com.feragusper.smokeanalytics.features.authentication.presentation

import com.feragusper.smokeanalytics.features.authentication.presentation.process.AuthenticationProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase

class AuthenticationWebDependencies(
    val processHolder: AuthenticationProcessHolder,
)

fun createAuthenticationWebDependencies(
    fetchSessionUseCase: FetchSessionUseCase,
    signOutUseCase: SignOutUseCase,
    signInWithGoogle: suspend () -> Unit,
    analyticsTracker: AnalyticsTracker,
): AuthenticationWebDependencies {
    return AuthenticationWebDependencies(
        processHolder = AuthenticationProcessHolder(
            fetchSessionUseCase = fetchSessionUseCase,
            signOutUseCase = signOutUseCase,
            signInWithGoogle = signInWithGoogle,
            analyticsTracker = analyticsTracker,
        )
    )
}