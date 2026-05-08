package com.feragusper.smokeanalytics.libraries.preferences.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateUserPreferencesUseCaseTest {

    private val fakeRepository = object : UserPreferencesRepository {
        var stored = UserPreferences()
        override suspend fun fetch(): UserPreferences = stored
        override suspend fun update(preferences: UserPreferences) { stored = preferences }
    }

    @Test
    fun invoke_updatesRepository() = runTest {
        val useCase = UpdateUserPreferencesUseCase(fakeRepository)
        val updated = UserPreferences(packPrice = 12.0, cigarettesPerPack = 25)
        useCase(updated)
        assertEquals(12.0, fakeRepository.stored.packPrice)
        assertEquals(25, fakeRepository.stored.cigarettesPerPack)
    }
}

