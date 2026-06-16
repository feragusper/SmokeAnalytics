package com.feragusper.smokeanalytics.features.goals.presentation.di

import com.feragusper.smokeanalytics.features.goals.presentation.GoalsViewModel
import com.feragusper.smokeanalytics.features.goals.presentation.process.GoalsProcessHolder
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val goalsPresentationModule = module {
    factoryOf(::GoalsProcessHolder)
    viewModelOf(::GoalsViewModel)
}
