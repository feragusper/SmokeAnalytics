package com.feragusper.smokeanalytics.libraries.architecture.domain.extensions

import org.amshove.kluent.internal.assertEquals
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Month

class LocalDateTimeExtensionsTest {

    /**
     * Verifies that isToday returns true when the date is today.
     */
    @Test
    fun `GIVEN the date is today WHEN isToday is called THEN it should return true`() {
        val today = LocalDateTime.now()
        today.isToday() shouldBe true
    }

    /**
     * Verifies that isToday returns false when the date is not today.
     */
    @Test
    fun `GIVEN the date is not today WHEN isToday is called THEN it should return false`() {
        val yesterday = LocalDateTime.now().minusDays(1)
        yesterday.isToday() shouldBe false
    }

    /**
     * Verifies that isThisWeek returns true when the date is within this week.
     */
    @Test
    fun `GIVEN the date is this week WHEN isThisWeek is called THEN it should return true`() {
        val thisWeek = LocalDateTime.now()
        thisWeek.isThisWeek() shouldBe true
    }

    /**
     * Verifies that isThisWeek returns false when the date is not within this week.
     */
    @Test
    fun `GIVEN the date is not this week WHEN isThisWeek is called THEN it should return false`() {
        val lastWeek = LocalDateTime.now().minusWeeks(1)
        lastWeek.isThisWeek() shouldBe false
    }

    /**
     * Verifies that timeElapsedSinceNow correctly calculates the time difference since now.
     */
    @Test
    fun `GIVEN a date in the past WHEN timeElapsedSinceNow is called THEN it should return the correct time difference`() {
        val pastDate = LocalDateTime.now().minusHours(2).minusMinutes(30)
        val (hours, minutes) = pastDate.timeElapsedSinceNow()

        hours shouldBe 2
        minutes shouldBe 30
    }

    /**
     * Verifies that timeFormatted returns the correct format "HH:mm".
     */
    @Test
    fun `GIVEN a LocalDateTime WHEN timeFormatted is called THEN it should return the correct format`() {
        val date = LocalDateTime.of(2025, Month.MARCH, 1, 14, 30, 0, 0)
        val formattedTime = date.timeFormatted()

        // Compare the actual string values
        assertEquals("14:30", formattedTime)
    }

    /**
     * Verifies that dateFormatted returns the correct format "EEEE, MMMM dd".
     */
    @Test
    fun `GIVEN a LocalDateTime WHEN dateFormatted is called THEN it should return the correct date format`() {
        val date = LocalDateTime.of(2025, Month.MARCH, 1, 14, 30, 0, 0)
        val formattedDate = date.dateFormatted()

        assertEquals(formattedDate, "Saturday, March 01")
    }

}
