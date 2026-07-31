package com.feragusper.smokeanalytics

import com.feragusper.smokeanalytics.features.devtools.presentation.di.devToolsPresentationModule
import org.koin.core.module.Module

/**
 * DevTools is a debug-only feature, surfaced through app shortcuts in debug builds.
 * In debug builds its Koin module is registered so the tooling can resolve its dependencies.
 *
 * The release counterpart in `src/release` returns an empty list, keeping DevTools out of
 * production builds entirely.
 */
fun devToolsKoinModules(): List<Module> = listOf(devToolsPresentationModule)
