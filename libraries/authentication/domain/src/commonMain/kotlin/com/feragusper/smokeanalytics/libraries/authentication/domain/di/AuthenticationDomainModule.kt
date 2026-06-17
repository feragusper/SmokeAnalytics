package com.feragusper.smokeanalytics.libraries.authentication.domain.di

import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Koin module exposing the authentication use cases (shared across mobile and web). */
val authenticationDomainModule = module {
    factoryOf(::FetchSessionUseCase)
    factoryOf(::SignOutUseCase)
}
