package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EditSmokeUseCaseTest {

    private lateinit var repository: FakeSmokeRepository
    private lateinit var useCase: EditSmokeUseCase

    @BeforeTest
    fun setUp() {
        repository = FakeSmokeRepository()
        useCase = EditSmokeUseCase(repository)
    }

    @Test
    fun `GIVEN a smoke event ID and date WHEN invoke is executed THEN it should call editSmoke`() =
        runTest {
            val smokeId = "id"
            val newDate = Instant.parse("2023-03-01T12:00:00Z")

            useCase.invoke(smokeId, newDate)

            assertEquals(1, repository.editSmokeCalls)
            assertEquals(smokeId, repository.lastEditedSmokeId)
            assertEquals(newDate, repository.lastEditedSmokeDate)
        }

    private class FakeSmokeRepository : SmokeRepository {

        var editSmokeCalls = 0
            private set

        var lastEditedSmokeId: String? = null
            private set

        var lastEditedSmokeDate: Instant? = null
            private set

        override suspend fun editSmoke(id: String, date: Instant) {
            editSmokeCalls++
            lastEditedSmokeId = id
            lastEditedSmokeDate = date
        }

        override suspend fun addSmoke(date: Instant) = Unit
        override suspend fun deleteSmoke(id: String) = Unit
        override suspend fun fetchSmokes(startDate: Instant?, endDate: Instant?): List<Smoke> =
            emptyList()

        override suspend fun fetchSmokeCount() = error("Not needed for this test")
    }
}