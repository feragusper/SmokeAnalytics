package com.feragusper.smokeanalytics.features.devtools.presentation.di

import com.feragusper.smokeanalytics.features.devtools.presentation.DevToolsViewModel
import com.feragusper.smokeanalytics.features.devtools.presentation.process.DevToolsProcessHolder
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val devToolsPresentationModule = module {
    factoryOf(::DevToolsProcessHolder)
    viewModelOf(::DevToolsViewModel)
}
