package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteSmokeUseCaseTest {

    private lateinit var repository: FakeSmokeRepository
    private lateinit var useCase: DeleteSmokeUseCase

    @BeforeTest
    fun setUp() {
        repository = FakeSmokeRepository()
        useCase = DeleteSmokeUseCase(repository)
    }

    @Test
    fun `GIVEN a smoke event id WHEN invoke is executed THEN it should call deleteSmoke`() =
        runTest {
            val smokeId = "id"

            useCase.invoke(smokeId)

            assertEquals(1, repository.deleteSmokeCalls)
            assertEquals(smokeId, repository.lastDeletedSmokeId)
        }
}

private class FakeSmokeRepository : SmokeRepository {

    var deleteSmokeCalls = 0
        private set

    var lastDeletedSmokeId: String? = null
        private set

    override suspend fun deleteSmoke(id: String) {
        deleteSmokeCalls++
        lastDeletedSmokeId = id
    }

    override suspend fun addSmoke(date: Instant) = Unit
    override suspend fun editSmoke(id: String, date: Instant) = Unit
    override suspend fun fetchSmokes(startDate: Instant?, endDate: Instant?): List<Smoke> =
        emptyList()

    override suspend fun fetchSmokeCount() = error("Not needed for this test")
}