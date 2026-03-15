package com.feragusper.smokeanalytics.platform

import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetRefreshService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformServicesModule {

    @Binds
    @Singleton
    abstract fun bindLocationCaptureService(
        impl: AndroidLocationCaptureService,
    ): LocationCaptureService

    @Binds
    @Singleton
    abstract fun bindWidgetRefreshService(
        impl: AndroidWidgetRefreshService,
    ): WidgetRefreshService
}
