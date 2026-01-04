package com.feragusper.smokeanalytics

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.presentation.web.process.HomeProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.data.AuthenticationRepositoryImpl
import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.externals.GoogleAuthProvider
import dev.gitlive.firebase.auth.externals.signInWithPopup
import kotlinx.coroutines.await

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
    val addSmokeUseCase: AddSmokeUseCase,
    val editSmokeUseCase: EditSmokeUseCase,
    val deleteSmokeUseCase: DeleteSmokeUseCase,
    val fetchSmokesUseCase: FetchSmokesUseCase,
    val fetchSmokeStatsUseCase: FetchSmokeStatsUseCase,
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

            val fetchSession = FetchSessionUseCase(authRepo)
            val signOut = SignOutUseCase(authRepo)

            val addSmoke = AddSmokeUseCase(smokeRepo)
            val editSmoke = EditSmokeUseCase(smokeRepo)
            val deleteSmoke = DeleteSmokeUseCase(smokeRepo)
            val fetchSmokes = FetchSmokesUseCase(smokeRepo)
            val fetchStats = FetchSmokeStatsUseCase(smokeRepo)
            val fetchSmokeCounts = FetchSmokeCountListUseCase(smokeRepo)

            val homeProcessHolder = HomeProcessHolder(
                addSmokeUseCase = addSmoke,
                editSmokeUseCase = editSmoke,
                deleteSmokeUseCase = deleteSmoke,
                fetchSmokeCountListUseCase = fetchSmokeCounts,
                fetchSessionUseCase = fetchSession,
            )

            val signInWithGoogleWeb: suspend () -> Unit = {
                val auth = Firebase.auth
                val provider = GoogleAuthProvider()
                signInWithPopup(auth.js, provider).await()
            }

            return WebAppGraph(
                homeProcessHolder = homeProcessHolder,
                fetchSessionUseCase = fetchSession,
                signOutUseCase = signOut,
                addSmokeUseCase = addSmoke,
                editSmokeUseCase = editSmoke,
                deleteSmokeUseCase = deleteSmoke,
                fetchSmokesUseCase = fetchSmokes,
                fetchSmokeStatsUseCase = fetchStats,
                signInWithGoogleWeb = signInWithGoogleWeb,
            )
        }
    }
}