package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SmokeCountListResultTest {

    private lateinit var now: LocalDateTime
    private lateinit var lastSmokeDate: LocalDateTime
    private lateinit var lastSmoke: Smoke

    @BeforeEach
    fun setUp() {
        now = LocalDateTime.of(2023, 1, 1, 12, 0)
        lastSmokeDate = LocalDateTime.of(2023, 1, 1, 10, 30)

        mockkStatic(LocalDateTime::timeElapsedSinceNow)

        every { lastSmokeDate.timeElapsedSinceNow() } returns (1L to 30L)

        lastSmoke = Smoke(
            id = "1",
            date = lastSmokeDate,
            timeElapsedSincePreviousSmoke = (2L to 0L)
        )
    }

    @Test
    fun `GIVEN a list of smokes WHEN initializing SmokeCountListResult THEN it correctly calculates properties`() {
        val todaysSmokes = listOf(
            Smoke(id = "2", date = now, timeElapsedSincePreviousSmoke = (0L to 45L)),
            Smoke(id = "3", date = now.minusHours(1), timeElapsedSincePreviousSmoke = (1L to 15L))
        )

        val result = SmokeCountListResult(
            todaysSmokes = todaysSmokes,
            countByWeek = 5,
            countByMonth = 20,
            lastSmoke = lastSmoke
        )

        assertEquals(2, result.countByToday) // Two smokes today
        assertEquals(5, result.countByWeek)
        assertEquals(20, result.countByMonth)
        assertEquals((1L to 30L), result.timeSinceLastCigarette) // Last smoke was 1h 30m ago
    }

    @Test
    fun `GIVEN no last smoke WHEN initializing SmokeCountListResult THEN timeSinceLastCigarette is zero`() {
        val result = SmokeCountListResult(
            todaysSmokes = emptyList(),
            countByWeek = 3,
            countByMonth = 10,
            lastSmoke = null
        )

        assertEquals(0, result.countByToday)
        assertEquals(3, result.countByWeek)
        assertEquals(10, result.countByMonth)
        assertEquals((0L to 0L), result.timeSinceLastCigarette) // No last smoke, should be (0, 0)
    }
}
