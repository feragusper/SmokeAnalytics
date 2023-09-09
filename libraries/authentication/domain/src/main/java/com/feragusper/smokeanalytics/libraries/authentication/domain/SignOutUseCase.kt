package com.feragusper.smokeanalytics.libraries.authentication.domain

import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val authenticationRepository: AuthenticationRepository) {
    operator fun invoke() = authenticationRepository.signOut()
}