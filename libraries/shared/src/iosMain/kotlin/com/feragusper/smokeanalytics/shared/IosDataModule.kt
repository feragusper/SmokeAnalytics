package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.NoOpAnalyticsTracker
import com.feragusper.smokeanalytics.libraries.authentication.data.AuthenticationRepositoryImpl
import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.cravings.data.CravingRepositoryImpl
import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository
import com.feragusper.smokeanalytics.libraries.preferences.data.UserPreferencesRepositoryImpl
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import org.koin.dsl.module

/**
 * Koin module for the iOS app. Binds the GitLive-Firebase repositories (shared with web) and the
 * platform services. Use cases come from the shared domain Koin modules — see [iosKoinModules].
 * Analytics is a no-op until Firebase Analytics is wired on the Swift side (Phase 2).
 */
val iosDataModule = module {
    single<SmokeRepository> { SmokeRepositoryImpl() }
    single<CravingRepository> { CravingRepositoryImpl() }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl() }
    single<AuthenticationRepository> { AuthenticationRepositoryImpl() }
    single<LocationCaptureService> { IosLocationCaptureService() }
    single<AnalyticsTracker> { NoOpAnalyticsTracker }
}
