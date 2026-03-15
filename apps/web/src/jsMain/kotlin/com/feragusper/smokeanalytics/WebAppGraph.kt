package com.feragusper.smokeanalytics

import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotRepository
import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotUseCase
import com.feragusper.smokeanalytics.features.chatbot.domain.CoachContext
import com.feragusper.smokeanalytics.features.chatbot.domain.fallbackCoachReply
import com.feragusper.smokeanalytics.features.chatbot.domain.fallbackInitialCoachMessage
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.presentation.web.process.HomeProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.domain.Coordinate
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.authentication.data.AuthenticationRepositoryImpl
import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import com.feragusper.smokeanalytics.libraries.preferences.data.UserPreferencesRepositoryImpl
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import dev.gitlive.firebase.auth.externals.GoogleAuthProvider
import dev.gitlive.firebase.auth.externals.getAuth
import dev.gitlive.firebase.auth.externals.signInWithPopup
import kotlin.coroutines.resume
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * The dependency graph for the web application.
 *
 * @property homeProcessHolder The process holder for the home screen.
 * @property fetchSessionUseCase The use case for fetching the session.
 * @property signOutUseCase The use case for signing out.
 * @property addSmokeUseCase The use case for adding a smoke.
 * @property editSmokeUseCase The use case for editing a smoke.
 * @property deleteSmokeUseCase The use case for deleting a smoke.
 * @property fetchSmokesUseCase The use case for fetching smokes.
 * @property fetchSmokeStatsUseCase The use case for fetching smoke stats.
 * @property signInWithGoogleWeb The function for signing in with Google.
 */
data class WebAppGraph(
    val homeProcessHolder: HomeProcessHolder,
    val fetchSessionUseCase: FetchSessionUseCase,
    val signOutUseCase: SignOutUseCase,
    val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
    val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    val locationCaptureService: LocationCaptureService,
    val addSmokeUseCase: AddSmokeUseCase,
    val editSmokeUseCase: EditSmokeUseCase,
    val deleteSmokeUseCase: DeleteSmokeUseCase,
    val fetchSmokesUseCase: FetchSmokesUseCase,
    val fetchSmokeStatsUseCase: FetchSmokeStatsUseCase,
    val chatbotUseCase: ChatbotUseCase,
    val signInWithGoogleWeb: suspend () -> Unit,
) {
    companion object {

        /**
         * Creates a new instance of the dependency graph for the web application.
         *
         * @return The new instance of the dependency graph.
         */
        fun create(): WebAppGraph {
            val authRepo: AuthenticationRepository = AuthenticationRepositoryImpl()
            val smokeRepo: SmokeRepository = SmokeRepositoryImpl()
            val prefsRepo: UserPreferencesRepository = UserPreferencesRepositoryImpl()

            val fetchSession = FetchSessionUseCase(authRepo)
            val signOut = SignOutUseCase(authRepo)
            val fetchPreferences = FetchUserPreferencesUseCase(prefsRepo)
            val updatePreferences = UpdateUserPreferencesUseCase(prefsRepo)
            val locationCaptureService = object : LocationCaptureService {
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
                        js("{ enableHighAccuracy: false, timeout: 2500, maximumAge: 60000 }"),
                    )
                }
            }

            val addSmoke = AddSmokeUseCase(smokeRepo)
            val editSmoke = EditSmokeUseCase(smokeRepo)
            val deleteSmoke = DeleteSmokeUseCase(smokeRepo)
            val fetchSmokes = FetchSmokesUseCase(smokeRepo)
            val fetchStats = FetchSmokeStatsUseCase(smokeRepo)
            val fetchSmokeCounts = FetchSmokeCountListUseCase(smokeRepo)
            val chatbotRepository = object : ChatbotRepository {
                override suspend fun sendMessage(message: String, context: CoachContext): String =
                    fallbackCoachReply(message, context)

                override suspend fun sendInitialMessage(context: CoachContext): String =
                    fallbackInitialCoachMessage(context)
            }
            val chatbotUseCase = ChatbotUseCase(
                smokeRepository = smokeRepo,
                authRepository = authRepo,
                chatbotRepository = chatbotRepository,
            )

            val homeProcessHolder = HomeProcessHolder(
                addSmokeUseCase = addSmoke,
                editSmokeUseCase = editSmoke,
                deleteSmokeUseCase = deleteSmoke,
                fetchSmokeCountListUseCase = fetchSmokeCounts,
                fetchSessionUseCase = fetchSession,
                fetchUserPreferencesUseCase = fetchPreferences,
                locationCaptureService = locationCaptureService,
            )

            val signInWithGoogleWeb: suspend () -> Unit = {
                val provider = GoogleAuthProvider()
                signInWithPopup(getAuth(), provider).await()
            }

            return WebAppGraph(
                homeProcessHolder = homeProcessHolder,
                fetchSessionUseCase = fetchSession,
                signOutUseCase = signOut,
                fetchUserPreferencesUseCase = fetchPreferences,
                updateUserPreferencesUseCase = updatePreferences,
                locationCaptureService = locationCaptureService,
                addSmokeUseCase = addSmoke,
                editSmokeUseCase = editSmoke,
                deleteSmokeUseCase = deleteSmoke,
                fetchSmokesUseCase = fetchSmokes,
                fetchSmokeStatsUseCase = fetchStats,
                chatbotUseCase = chatbotUseCase,
                signInWithGoogleWeb = signInWithGoogleWeb,
            )
        }
    }
}
