package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Month

class SmokeStatsTest {

    // Helper function to create mock smoke events.
    private fun createSmokeEvents(): List<Smoke> {
        val smoke1 =
            Smoke("1", LocalDateTime.of(2023, Month.MARCH, 1, 12, 0, 0, 0), Pair(0L, 0L)) // Week 1
        val smoke2 =
            Smoke("2", LocalDateTime.of(2023, Month.MARCH, 2, 13, 0, 0, 0), Pair(0L, 0L)) // Week 1
        val smoke3 = Smoke(
            "3",
            LocalDateTime.of(2023, Month.MARCH, 8, 14, 30, 0, 0),
            Pair(1L, 30L)
        ) // Week 2
        val smoke4 = Smoke(
            "4",
            LocalDateTime.of(2023, Month.MARCH, 15, 16, 15, 0, 0),
            Pair(2L, 0L)
        ) // Week 3
        val smoke5 = Smoke(
            "5",
            LocalDateTime.of(2023, Month.MARCH, 22, 10, 0, 0, 0),
            Pair(2L, 30L)
        ) // Week 4
        val smoke6 =
            Smoke("6", LocalDateTime.of(2023, Month.MARCH, 29, 11, 0, 0, 0), Pair(3L, 0L)) // Week 5

        // Return the updated list of smokes
        return listOf(smoke1, smoke2, smoke3, smoke4, smoke5, smoke6)
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct daily statistics`() {
        // Arrange: Create mock smoke events
        val smokes = createSmokeEvents()

        // Act: Call the 'from' method to calculate the statistics
        val stats = SmokeStats.from(smokes, 2023, 3, null)

        // Assert: Verify the daily statistics
        stats.daily["1"] shouldBe 1  // March 1st has 2 smokes
        stats.daily["2"] shouldBe 1  // March 2nd has 1 smoke
        stats.daily["3"] shouldBe 0  // March 3rd has 1 smoke
        stats.daily["4"] shouldBe 0  // March 4th has 1 smoke
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct weekly statistics`() {
        val smokes =
            createSmokeEvents() // Assuming `createSmokeEvents` provides the necessary test data

        val stats = SmokeStats.from(smokes, 2023, 3, null)

        // Adjust the expected values based on the corrected week grouping
        stats.weekly["Wed"] shouldBeEqualTo 5 // March 1st, 2nd, 8th, 15th, 22nd, 29th are Wednesdays
        stats.weekly["Thu"] shouldBeEqualTo 1 // March 23rd is a Thursday
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct monthly statistics`() {
        // Arrange: Create mock smoke events
        val smokes = createSmokeEvents()

        // Act: Call the 'from' method to calculate the statistics
        val stats = SmokeStats.from(smokes, 2023, 3, null)

        // Assert: Verify the monthly statistics (should return the number of smokes per week)
        stats.monthly["W1"] shouldBe 2  // Week 1 (March 1st - 7th)
        stats.monthly["W2"] shouldBe 1  // Week 2 (March 8th - 14th)
        stats.monthly["W3"] shouldBe 1  // Week 3 (March 15th - 21st)
        stats.monthly["W4"] shouldBe 1  // Week 4 (March 22nd - 28th)
        stats.monthly["W5"] shouldBe 1  // Week 5 (March 29th - 31st)
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct yearly statistics`() {
        // Arrange: Create mock smoke events
        val smokes = createSmokeEvents()

        // Act: Call the 'from' method to calculate the statistics
        val stats = SmokeStats.from(smokes, 2023, 3, null)

        // Assert: Verify the yearly statistics (should return the number of smokes per month)
        stats.yearly["Jan"] shouldBe 0  // No smokes in January
        stats.yearly["Feb"] shouldBe 0  // No smokes in February
        stats.yearly["Mar"] shouldBe 6  // 5 smokes in March
        stats.yearly["Apr"] shouldBe 0  // No smokes in April
        stats.yearly["May"] shouldBe 0  // No smokes in May
        stats.yearly["Jun"] shouldBe 0  // No smokes in June
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct hourly statistics`() {
        // Arrange: Create mock smoke events
        val smokes = createSmokeEvents()

        // Act: Call the 'from' method to calculate the statistics for a specific day
        val stats = SmokeStats.from(smokes, 2023, 3, 1)

        // Assert: Verify the hourly statistics (should return the number of smokes per hour)
        stats.hourly["12:00"] shouldBe 1  // 2 smokes on March 1st at 12:00
        stats.hourly["13:00"] shouldBe 0  // 1 smoke on March 1st at 13:00
        stats.hourly["14:00"] shouldBe 0  // No smoke at 14:00
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct total month statistics`() {
        // Arrange: Create mock smoke events
        val smokes = createSmokeEvents()

        // Act: Call the 'from' method to calculate the total for the month
        val stats = SmokeStats.from(smokes, 2023, 3, null)

        // Assert: Verify the total number of smokes in the month
        stats.totalMonth shouldBe 6  // 5 total smokes in March
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct daily average statistics`() {
        // Arrange: Create mock smoke events
        val smokes = createSmokeEvents()

        // Act: Call the 'from' method to calculate the daily average for the month
        val stats = SmokeStats.from(smokes, 2023, 3, null)

        // Assert: Verify the daily average calculation
        stats.dailyAverage shouldBeEqualTo 6f / 31f // Total smokes in the month divided by the number of days
    }
}
