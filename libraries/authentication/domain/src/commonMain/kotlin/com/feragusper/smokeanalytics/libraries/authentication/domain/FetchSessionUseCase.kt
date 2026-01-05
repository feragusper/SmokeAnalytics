package com.feragusper.smokeanalytics.libraries.authentication.domain

class FetchSessionUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    operator fun invoke(): Session = authenticationRepository.fetchSession()
}