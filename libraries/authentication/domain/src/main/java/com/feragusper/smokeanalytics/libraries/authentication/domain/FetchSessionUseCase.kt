package com.feragusper.smokeanalytics.libraries.authentication.domain

import javax.inject.Inject

class FetchSessionUseCase @Inject constructor(private val authenticationRepository: AuthenticationRepository) {
    operator fun invoke() = authenticationRepository.fetchSession()
}