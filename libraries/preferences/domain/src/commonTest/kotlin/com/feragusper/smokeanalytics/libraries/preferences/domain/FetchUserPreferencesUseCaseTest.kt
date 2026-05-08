package com.feragusper.smokeanalytics.libraries.preferences.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchUserPreferencesUseCaseTest {

    private val fakeRepository = object : UserPreferencesRepository {
        var stored = UserPreferences(packPrice = 8.5, cigarettesPerPack = 20)
        override suspend fun fetch(): UserPreferences = stored
        override suspend fun update(preferences: UserPreferences) { stored = preferences }
    }

    @Test
    fun invoke_delegatesToRepository() = runTest {
        val useCase = FetchUserPreferencesUseCase(fakeRepository)
        val result = useCase()
        assertEquals(8.5, result.packPrice)
        assertEquals(20, result.cigarettesPerPack)
    }
}

