package com.feragusper.smokeanalytics.features.settings.presentation.process

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsProcessHolderTest {

    private lateinit var processHolder: SettingsProcessHolder

    private val fetchSessionUseCase: FetchSessionUseCase = mockk()
    private val signOutUseCase: SignOutUseCase = mockk()
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase = mockk()
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase = mockk()
    private val fetchSmokesUseCase: FetchSmokesUseCase = mockk()

    /**
     * Sets up the test environment by initializing the process holder and configuring mock behaviors.
     */
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        processHolder = SettingsProcessHolder(
            fetchSessionUseCase = fetchSessionUseCase,
            signOutUseCase = signOutUseCase,
            fetchUserPreferencesUseCase = fetchUserPreferencesUseCase,
            updateUserPreferencesUseCase = updateUserPreferencesUseCase,
            fetchSmokesUseCase = fetchSmokesUseCase,
        )
    }

    /**
     * Ensures that when the session is anonymous, fetching the user results in a logged-out state.
     */
    @Test
    fun `GIVEN session is anonymous WHEN FetchUser intent is processed THEN emit loading and UserLoggedOut`() =
        runTest {
            coEvery { fetchSessionUseCase() } returns Session.Anonymous

            processHolder.processIntent(SettingsIntent.FetchUser).test {
                awaitItem() shouldBeEqualTo SettingsResult.Loading
                awaitItem() shouldBeEqualTo SettingsResult.UserLoggedOut
                awaitComplete()
            }
        }

    /**
     * Ensures that when the session is logged in, fetching the user results in a logged-in state.
     */
    @Test
    fun `GIVEN session is logged in WHEN FetchUser intent is processed THEN emit loading and UserLoggedIn`() =
        runTest {
            val email = "fernancho@gmail.com"
            val displayName = "Fer"
            coEvery { fetchSessionUseCase() } returns Session.LoggedIn(
                Session.User(id = "123", email = email, displayName = displayName)
            )
            coEvery { fetchUserPreferencesUseCase() } returns UserPreferences()
            coEvery { fetchSmokesUseCase(any(), any()) } returns emptyList()

            processHolder.processIntent(SettingsIntent.FetchUser).test {
                awaitItem() shouldBeEqualTo SettingsResult.Loading
                awaitItem() shouldBeEqualTo SettingsResult.UserLoggedIn(
                    email = email,
                    displayName = displayName,
                    preferences = UserPreferences(),
                    goalProgress = null,
                )
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN session is logged in WHEN goal smokes fail THEN emit loading and load error`() =
        runTest {
            coEvery { fetchSessionUseCase() } returns Session.LoggedIn(
                Session.User(id = "123", email = "fernancho@gmail.com", displayName = "Fer")
            )
            coEvery { fetchUserPreferencesUseCase() } returns UserPreferences(activeGoal = SmokingGoal.DailyCap(10))
            coEvery { fetchSmokesUseCase(any(), any()) } throws IllegalStateException("Quota exceeded")

            processHolder.processIntent(SettingsIntent.FetchUser).test {
                awaitItem() shouldBeEqualTo SettingsResult.Loading
                awaitItem() shouldBeEqualTo SettingsResult.Error(
                    "Could not load your settings. IllegalStateException: Quota exceeded"
                )
                awaitComplete()
            }
        }

    /**
     * Ensures that when the sign-out intent is processed, it results in a logged-out state.
     */
    @Test
    fun `WHEN SignOut intent is processed THEN emit loading and UserLoggedOut`() =
        runTest {
            coEvery { signOutUseCase() } just Runs

            processHolder.processIntent(SettingsIntent.SignOut).test {
                awaitItem() shouldBeEqualTo SettingsResult.Loading
                awaitItem() shouldBeEqualTo SettingsResult.UserLoggedOut
                awaitComplete()
            }

            // Verify that signOutUseCase() was actually called
            coVerify(exactly = 1) { signOutUseCase() }
        }

    @Test
    fun `WHEN UpdatePreferences saves goal THEN emits persisted active goal`() =
        runTest {
            val email = "fernancho@gmail.com"
            val displayName = "Fer"
            val preferences = UserPreferences(activeGoal = SmokingGoal.DailyCap(15))
            coEvery { updateUserPreferencesUseCase(preferences) } just Runs
            coEvery { fetchUserPreferencesUseCase() } returns preferences
            coEvery { fetchSessionUseCase() } returns Session.LoggedIn(
                Session.User(id = "123", email = email, displayName = displayName)
            )
            coEvery { fetchSmokesUseCase(any(), any()) } returns emptyList()

            processHolder.processIntent(SettingsIntent.UpdatePreferences(preferences)).test {
                awaitItem() shouldBeEqualTo SettingsResult.Loading
                val loggedIn = awaitItem() as SettingsResult.UserLoggedIn
                loggedIn.email shouldBeEqualTo email
                loggedIn.displayName shouldBeEqualTo displayName
                loggedIn.preferences shouldBeEqualTo preferences
                loggedIn.goalProgress shouldNotBe null
                awaitItem() shouldBeEqualTo SettingsResult.PreferencesSaved
                awaitComplete()
            }
        }

    @Test
    fun `WHEN UpdatePreferences cannot refresh goal progress THEN emits save error`() =
        runTest {
            val preferences = UserPreferences(activeGoal = SmokingGoal.DailyCap(15))
            coEvery { updateUserPreferencesUseCase(preferences) } just Runs
            coEvery { fetchUserPreferencesUseCase() } returns preferences
            coEvery { fetchSmokesUseCase(any(), any()) } throws IllegalStateException("Quota exceeded")

            processHolder.processIntent(SettingsIntent.UpdatePreferences(preferences)).test {
                awaitItem() shouldBeEqualTo SettingsResult.Loading
                awaitItem() shouldBeEqualTo SettingsResult.Error(
                    "Could not save your settings. IllegalStateException: Quota exceeded"
                )
                awaitComplete()
            }
        }

    @Test
    fun `WHEN UpdatePreferences write fails THEN emits diagnostic save error`() =
        runTest {
            val preferences = UserPreferences(activeGoal = SmokingGoal.DailyCap(15))
            coEvery { updateUserPreferencesUseCase(preferences) } throws IllegalStateException("Firestore update preferences timed out")

            processHolder.processIntent(SettingsIntent.UpdatePreferences(preferences)).test {
                awaitItem() shouldBeEqualTo SettingsResult.Loading
                awaitItem() shouldBeEqualTo SettingsResult.Error(
                    "Could not save your settings. IllegalStateException: Firestore update preferences timed out"
                )
                awaitComplete()
            }
        }
}
