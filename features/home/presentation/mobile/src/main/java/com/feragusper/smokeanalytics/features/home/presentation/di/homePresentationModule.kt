package com.feragusper.smokeanalytics.features.home.presentation.di

import com.feragusper.smokeanalytics.features.home.presentation.HomeViewModel
import com.feragusper.smokeanalytics.features.home.presentation.process.HomeProcessHolder
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val homePresentationModule = module {
    factoryOf(::HomeProcessHolder)
    viewModelOf(::HomeViewModel)
}
