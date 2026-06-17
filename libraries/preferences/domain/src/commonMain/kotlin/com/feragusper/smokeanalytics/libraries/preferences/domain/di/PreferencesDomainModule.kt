package com.feragusper.smokeanalytics.libraries.preferences.domain.di

import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Koin module exposing the preferences use cases (shared across mobile and web). */
val preferencesDomainModule = module {
    factoryOf(::FetchUserPreferencesUseCase)
    factoryOf(::UpdateUserPreferencesUseCase)
}
