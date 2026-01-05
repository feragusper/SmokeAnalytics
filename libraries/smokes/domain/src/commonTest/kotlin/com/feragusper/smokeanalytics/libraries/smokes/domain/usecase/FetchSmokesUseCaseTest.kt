package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchSmokesUseCaseTest {

    @Test
    fun `GIVEN fetch smokes by date answers WHEN invoke with date is executed THEN it should return the correct data`() =
        runTest {
            val startDate = Instant.parse("2023-03-01T00:00:00Z")
            val endDate = Instant.parse("2023-03-31T23:59:59Z")

            val smokeList = listOf(
                Smoke(
                    id = "1",
                    date = Instant.parse("2023-03-01T12:00:00Z"),
                    timeElapsedSincePreviousSmoke = 0L to 0L
                ),
                Smoke(
                    id = "2",
                    date = Instant.parse("2023-03-05T13:00:00Z"),
                    timeElapsedSincePreviousSmoke = 0L to 0L
                ),
            )

            val repository = FakeSmokeRepository(fetchSmokesResult = smokeList)
            val useCase = FetchSmokesUseCase(repository)

            val result = useCase.invoke(startDate, endDate)

            assertEquals(1, repository.fetchSmokesCalls)
            assertEquals(startDate, repository.lastFetchStart)
            assertEquals(endDate, repository.lastFetchEnd)
            assertEquals(smokeList, result)
        }

    private class FakeSmokeRepository(
        private val fetchSmokesResult: List<Smoke>
    ) : SmokeRepository {

        var fetchSmokesCalls = 0
            private set

        var lastFetchStart: Instant? = null
            private set

        var lastFetchEnd: Instant? = null
            private set

        override suspend fun fetchSmokes(startDate: Instant?, endDate: Instant?): List<Smoke> {
            fetchSmokesCalls++
            lastFetchStart = startDate
            lastFetchEnd = endDate
            return fetchSmokesResult
        }

        override suspend fun addSmoke(date: Instant) = Unit
        override suspend fun editSmoke(id: String, date: Instant) = Unit
        override suspend fun deleteSmoke(id: String) = Unit
        override suspend fun fetchSmokeCount() = error("Not needed for this test")
    }
}