package com.feragusper.smokeanalytics.features.authentication.presentation.di

import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationViewModel
import com.feragusper.smokeanalytics.features.authentication.presentation.process.AuthenticationProcessHolder
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val authenticationPresentationModule = module {
    factoryOf(::AuthenticationProcessHolder)
    viewModelOf(::AuthenticationViewModel)
}
