package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isThisWeek
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isToday
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeAfter
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class FetchSmokeCountListUseCaseTest {

    private val repository: SmokeRepository = mockk()
    private val useCase = FetchSmokeCountListUseCase(repository)

    private lateinit var today: LocalDateTime
    private lateinit var thisWeek: LocalDateTime
    private lateinit var todayTimeAfter: Pair<Long, Long>

    @BeforeEach
    fun setUp() {
        today = LocalDateTime.of(2023, 1, 1, 12, 0)
        thisWeek = LocalDateTime.of(2023, 1, 1, 8, 0)
        todayTimeAfter = Pair(2, 30)

        mockkStatic(LocalDateTime::isToday)
        mockkStatic(LocalDateTime::isThisWeek)
        mockkStatic(LocalDateTime::isThisMonth)
        mockkStatic(LocalDateTime::timeAfter)

        every { today.isToday() } returns true
        every { today.isThisWeek() } returns true
        every { today.isThisMonth() } returns true
        every { today.timeAfter(thisWeek) } returns todayTimeAfter
    }

    @Test
    fun `GIVEN fetchSmokeCount answers WHEN invoke is executed THEN it returns correct SmokeCountListResult`() =
        runTest {
            val todaySmoke = Smoke(
                id = "1",
                date = today,
                timeElapsedSincePreviousSmoke = todayTimeAfter
            )

            coEvery { repository.fetchSmokeCount() } returns SmokeCount(
                week = 2,
                month = 3,
                today = listOf(todaySmoke),
                lastSmoke = todaySmoke
            )

            val expected = SmokeCountListResult(
                countByWeek = 2,
                countByMonth = 3,
                todaysSmokes = listOf(todaySmoke),
                lastSmoke = todaySmoke
            )

            assertEquals(expected, useCase.invoke())
        }
}
