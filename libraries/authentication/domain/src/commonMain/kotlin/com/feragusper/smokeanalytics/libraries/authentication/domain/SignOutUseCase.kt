package com.feragusper.smokeanalytics.libraries.authentication.domain

class SignOutUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke() = authenticationRepository.signOut()
}