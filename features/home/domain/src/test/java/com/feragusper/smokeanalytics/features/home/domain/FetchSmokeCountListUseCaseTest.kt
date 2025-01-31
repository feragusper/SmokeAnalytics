package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isThisWeek
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isToday
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeAfter
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.SmokeCount
import com.feragusper.smokeanalytics.libraries.smokes.domain.SmokeRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class FetchSmokeCountListUseCaseTest {

    private val repository: SmokeRepository = mockk()
    private val useCase = FetchSmokeCountListUseCase(repository)

    @Test
    fun `GIVEN fetch smokes answers WHEN invoke is executed THEN it return `() = runTest {
        val today: LocalDateTime = mockk()
        val thisWeek: LocalDateTime = mockk()

        mockkStatic(LocalDateTime::isToday)
        mockkStatic(LocalDateTime::isThisWeek)
        mockkStatic(LocalDateTime::isThisMonth)
        mockkStatic(LocalDateTime::timeAfter)

        val todayTimeAfter: Pair<Long, Long> = mockk()
        every { today.isToday() } answers { true }
        every { today.isThisWeek() } answers { true }
        every { today.isThisMonth() } answers { true }
        every { today.timeAfter(thisWeek) } answers { todayTimeAfter }

        val todaySmoke = Smoke(
            id = "1",
            date = today,
            timeElapsedSincePreviousSmoke = todayTimeAfter
        )
        coEvery { repository.fetchSmokeCount() } answers {
            SmokeCount(
                week = 2,
                month = 3,
                today = listOf(todaySmoke),
                lastSmoke = todaySmoke,
            )
        }

        assertEquals(
            useCase.invoke(), SmokeCountListResult(
                countByWeek = 2,
                countByMonth = 3,
                todaysSmokes = listOf(todaySmoke),
                lastSmoke = todaySmoke,
            )
        )
    }
}
