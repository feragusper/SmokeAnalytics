package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeRelationship
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class SetSmokeRelationshipUseCaseTest {

    @Test
    fun `GIVEN an id and relationship WHEN invoked THEN it forwards them to the repository`() = runTest {
        val repository = FakeSmokeRepository()
        val useCase = SetSmokeRelationshipUseCase(repository)
        val relationship = SmokeRelationship.Tagged(tags = setOf("coffee", "morning"))

        useCase("smoke-1", relationship)

        assertEquals("smoke-1", repository.lastId)
        assertEquals(relationship, repository.lastRelationship)
    }

    private class FakeSmokeRepository : SmokeRepository {
        var lastId: String? = null
            private set
        var lastRelationship: SmokeRelationship? = null
            private set

        override suspend fun addSmoke(timestamp: Instant, location: GeoPoint?): String = "id"

        override suspend fun setSmokeRelationship(id: String, relationship: SmokeRelationship) {
            lastId = id
            lastRelationship = relationship
        }

        override suspend fun editSmoke(id: String, timestamp: Instant, location: GeoPoint?) = Unit
        override suspend fun deleteSmoke(id: String) = Unit
        override suspend fun fetchSmokes(start: Instant?, end: Instant?): List<Smoke> = emptyList()
        override suspend fun fetchSmokeCount(dayStartHour: Int, manualDayStartEpochMillis: Long?) =
            error("Not needed for this test")
    }
}
