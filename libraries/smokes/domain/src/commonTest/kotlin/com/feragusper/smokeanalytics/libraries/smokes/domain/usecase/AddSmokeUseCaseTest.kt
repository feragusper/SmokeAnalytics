package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AddSmokeUseCaseTest {

    private lateinit var repository: FakeSmokeRepository
    private lateinit var useCase: AddSmokeUseCase

    @BeforeTest
    fun setUp() {
        repository = FakeSmokeRepository()
        useCase = AddSmokeUseCase(repository)
    }

    @Test
    fun `GIVEN a smoke event WHEN invoke is executed THEN it should call addSmoke`() =
        runTest {
            // Act
            useCase.invoke()

            // Assert
            assertEquals(1, repository.addSmokeCalls)
            assertNotNull(repository.lastAddedSmoke)
        }

    @Test
    fun `GIVEN a specific date WHEN invoke is executed THEN it should call addSmoke with that date`() =
        runTest {
            // Arrange
            val specificInstant = Instant.parse("2023-03-01T12:00:00Z")

            // Act
            useCase.invoke(specificInstant)

            // Assert
            assertEquals(1, repository.addSmokeCalls)
            assertEquals(specificInstant, repository.lastAddedSmoke)
        }

    private class FakeSmokeRepository : SmokeRepository {

        var addSmokeCalls = 0
            private set

        var lastAddedSmoke: Instant? = null
            private set

        override suspend fun addSmoke(date: Instant) {
            addSmokeCalls++
            lastAddedSmoke = date
        }

        override suspend fun editSmoke(id: String, date: Instant) = Unit
        override suspend fun deleteSmoke(id: String) = Unit
        override suspend fun fetchSmokes(
            startDate: Instant?,
            endDate: Instant?
        ): List<Smoke> = emptyList()

        override suspend fun fetchSmokeCount() =
            error("Not needed for this test")
    }
}