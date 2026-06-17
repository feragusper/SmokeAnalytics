package com.feragusper.smokeanalytics

import com.feragusper.smokeanalytics.map.MapMobileViewModel
import com.feragusper.smokeanalytics.platform.AndroidLocationCaptureService
import com.feragusper.smokeanalytics.platform.AndroidWidgetRefreshService
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetRefreshService
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.SyncWithWearUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * App-level Koin module: platform services, the mobile-only wear sync use case,
 * and screen-level ViewModels that live in the app module.
 */
val appModule = module {
    single<LocationCaptureService> { AndroidLocationCaptureService(androidContext()) }
    single<WidgetRefreshService> { AndroidWidgetRefreshService(androidContext()) }
    factoryOf(::SyncWithWearUseCase)
    viewModelOf(::MapMobileViewModel)
}
