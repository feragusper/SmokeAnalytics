package com.feragusper.smokeanalytics

import org.koin.core.module.Module

/**
 * DevTools ships only in debug builds. Release builds register no DevTools Koin module,
 * mirroring the absence of the `:features:devtools:presentation` dependency from the
 * release classpath. See the `src/debug` counterpart for the debug implementation.
 */
fun devToolsKoinModules(): List<Module> = emptyList()
