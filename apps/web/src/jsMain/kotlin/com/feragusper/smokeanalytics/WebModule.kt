package com.feragusper.smokeanalytics

import com.feragusper.smokeanalytics.apps.web.MapWebStateHolder
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationWebDependencies
import com.feragusper.smokeanalytics.features.authentication.presentation.process.AuthenticationProcessHolder
import com.feragusper.smokeanalytics.features.goals.presentation.web.GoalsWebDependencies
import com.feragusper.smokeanalytics.features.goals.presentation.web.process.GoalsProcessHolder
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryWebStore
import com.feragusper.smokeanalytics.features.history.presentation.process.HistoryProcessHolder
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeWebStore
import com.feragusper.smokeanalytics.features.home.presentation.web.process.HomeProcessHolder
import com.feragusper.smokeanalytics.features.settings.presentation.web.SettingsWebDependencies
import com.feragusper.smokeanalytics.features.settings.presentation.web.process.SettingsProcessHolder
import com.feragusper.smokeanalytics.features.stats.presentation.web.StatsWebDependencies
import com.feragusper.smokeanalytics.features.stats.presentation.web.mvi.StatsWebStore
import com.feragusper.smokeanalytics.features.stats.presentation.web.process.StatsProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.domain.Coordinate
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationTrackingAvailability
import com.feragusper.smokeanalytics.libraries.authentication.data.AuthenticationRepositoryImpl
import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.cravings.data.CravingRepositoryImpl
import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository
import com.feragusper.smokeanalytics.libraries.preferences.data.UserPreferencesRepositoryImpl
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import dev.gitlive.firebase.auth.externals.GoogleAuthProvider
import dev.gitlive.firebase.auth.externals.getAuth
import dev.gitlive.firebase.auth.externals.signInWithPopup
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import kotlin.coroutines.resume

/**
 * Koin module for the web app. Provides the GitLive-Firebase repositories, the
 * browser-backed location service, and the per-screen process holders and stores.
 * Use cases come from the shared (commonMain) domain Koin modules.
 */
val webModule = module {
    single<SmokeRepository> { SmokeRepositoryImpl() }
    single<CravingRepository> { CravingRepositoryImpl() }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl() }
    single<AuthenticationRepository> { AuthenticationRepositoryImpl() }
    single<LocationCaptureService> { WebLocationCaptureService() }

    factoryOf(::HomeProcessHolder)
    factoryOf(::HistoryProcessHolder)
    factoryOf(::GoalsProcessHolder)
    factoryOf(::SettingsProcessHolder)
    factoryOf(::StatsProcessHolder)
    factory { AuthenticationProcessHolder(get(), get(), ::signInWithGoogleWeb) }

    factory { HomeWebStore(processHolder = get()) }
    factory { HistoryWebStore(processHolder = get()) }
    factory { StatsWebStore(processHolder = get()) }
    factory { MapWebStateHolder(fetchSmokesUseCase = get(), fetchUserPreferencesUseCase = get()) }
    factory { SettingsWebDependencies(processHolder = get()) }
    factory { GoalsWebDependencies(processHolder = get()) }
    factory { StatsWebDependencies(processHolder = get()) }
    factory { AuthenticationWebDependencies(processHolder = get()) }
}

/** Signs in with Google via the Firebase web popup flow. */
suspend fun signInWithGoogleWeb() {
    val provider = GoogleAuthProvider()
    signInWithPopup(getAuth(), provider).await()
}

/**
 * Browser-backed [LocationCaptureService] using the Geolocation and Permissions APIs.
 */
private class WebLocationCaptureService : LocationCaptureService {

    override suspend fun locationTrackingAvailability(
        preferenceEnabled: Boolean,
    ): LocationTrackingAvailability = suspendCancellableCoroutine { continuation ->
        val navigator = js("window.navigator")
        val geolocation = navigator?.geolocation
        if (geolocation == null) {
            continuation.resume(
                LocationTrackingAvailability(
                    preferenceEnabled = preferenceEnabled,
                    permissionGranted = false,
                    providerEnabled = false,
                )
            )
            return@suspendCancellableCoroutine
        }

        val permissions = navigator.permissions
        if (permissions == null) {
            continuation.resume(
                LocationTrackingAvailability(
                    preferenceEnabled = preferenceEnabled,
                    permissionGranted = preferenceEnabled,
                    providerEnabled = true,
                )
            )
            return@suspendCancellableCoroutine
        }

        permissions.query(js("{ name: 'geolocation' }")).then(
            { status: dynamic ->
                continuation.resume(
                    LocationTrackingAvailability(
                        preferenceEnabled = preferenceEnabled,
                        permissionGranted = status.state == "granted",
                        providerEnabled = true,
                    )
                )
            },
            { _: dynamic ->
                continuation.resume(
                    LocationTrackingAvailability(
                        preferenceEnabled = preferenceEnabled,
                        permissionGranted = false,
                        providerEnabled = true,
                    )
                )
            },
        )
    }

    override suspend fun captureCurrentLocation(): Coordinate? = suspendCancellableCoroutine { continuation ->
        val navigator = js("window.navigator")
        val geolocation = navigator?.geolocation
        if (geolocation == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        geolocation.getCurrentPosition(
            { position: dynamic ->
                continuation.resume(
                    Coordinate(
                        latitude = position.coords.latitude as Double,
                        longitude = position.coords.longitude as Double,
                    )
                )
            },
            { _: dynamic ->
                continuation.resume(null)
            },
            js("{ enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }"),
        )
    }
}
