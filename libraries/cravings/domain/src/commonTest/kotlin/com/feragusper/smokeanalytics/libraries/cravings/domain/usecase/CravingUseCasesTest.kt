package com.feragusper.smokeanalytics.libraries.cravings.domain.usecase

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

private class FakeCravingRepository : CravingRepository {
    var added: Pair<Instant, Instant?>? = null
    var fetchRange: Pair<Instant?, Instant?>? = null
    var resolved: ResolvedCall? = null
    var deletedId: String? = null
    var activeCraving: Craving? = null
    var cravings: List<Craving> = emptyList()

    data class ResolvedCall(
        val id: String,
        val outcome: CravingOutcome,
        val resolvedAt: Instant,
        val points: Int,
    )

    override suspend fun addCraving(createdAt: Instant, targetAt: Instant?): Craving {
        added = createdAt to targetAt
        return Craving(id = "new", createdAt = createdAt, targetAt = targetAt)
    }

    override suspend fun fetchCravings(start: Instant?, end: Instant?): List<Craving> {
        fetchRange = start to end
        return cravings
    }

    override suspend fun fetchActiveCraving(): Craving? = activeCraving

    override suspend fun resolveCraving(
        id: String,
        outcome: CravingOutcome,
        resolvedAt: Instant,
        pointsAwarded: Int,
    ) {
        resolved = ResolvedCall(id, outcome, resolvedAt, pointsAwarded)
    }

    override suspend fun deleteCraving(id: String) {
        deletedId = id
    }
}

class CravingUseCasesTest {

    private val repository = FakeCravingRepository()
    private val base = Instant.parse("2026-06-16T12:00:00Z")

    @Test
    fun `add craving delegates to the repository`() = runTest {
        val result = AddCravingUseCase(repository)(createdAt = base, targetAt = base + 60.minutes)

        assertEquals(base to (base + 60.minutes), repository.added)
        assertEquals("new", result.id)
        assertEquals(CravingOutcome.PENDING, result.outcome)
    }

    @Test
    fun `fetch cravings delegates the range`() = runTest {
        repository.cravings = listOf(Craving(id = "c1", createdAt = base))

        val result = FetchCravingsUseCase(repository)(start = base, end = null)

        assertEquals(base to null, repository.fetchRange)
        assertEquals(1, result.size)
    }

    @Test
    fun `fetch active craving returns the repository value`() = runTest {
        repository.activeCraving = Craving(id = "active", createdAt = base)

        val result = FetchActiveCravingUseCase(repository)()

        assertEquals("active", result?.id)
    }

    @Test
    fun `fetch active craving returns null when none pending`() = runTest {
        val result = FetchActiveCravingUseCase(repository)()
        assertNull(result)
    }

    @Test
    fun `resolving a resisted craving awards minutes waited plus the bonus`() = runTest {
        val craving = Craving(id = "c1", createdAt = base)

        // Waited 40 minutes -> 40/5 + 10 = 18.
        val points = ResolveCravingUseCase(repository)(
            craving = craving,
            outcome = CravingOutcome.RESISTED,
            resolvedAt = base + 40.minutes,
        )

        assertEquals(18, points)
        val call = repository.resolved!!
        assertEquals("c1", call.id)
        assertEquals(CravingOutcome.RESISTED, call.outcome)
        assertEquals(18, call.points)
    }

    @Test
    fun `resolving a craving given in awards no points`() = runTest {
        val craving = Craving(id = "c1", createdAt = base)

        val points = ResolveCravingUseCase(repository)(
            craving = craving,
            outcome = CravingOutcome.GAVE_IN,
            resolvedAt = base + 5.minutes,
        )

        assertEquals(0, points)
        assertEquals(0, repository.resolved!!.points)
    }

    @Test
    fun `negative wait is clamped to zero minutes`() = runTest {
        val craving = Craving(id = "c1", createdAt = base + 10.minutes)

        // resolvedAt before createdAt -> waited minutes clamps to 0 -> postponed = 0 points.
        val points = ResolveCravingUseCase(repository)(
            craving = craving,
            outcome = CravingOutcome.POSTPONED,
            resolvedAt = base,
        )

        assertEquals(0, points)
        assertTrue(repository.resolved!!.points == 0)
    }
}
