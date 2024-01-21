package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isThisWeek
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isToday
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.timeAfter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

class FetchSmokeCountListUseCaseTest {

    private val repository: SmokeRepository = mockk()
    private val useCase = FetchSmokeCountListUseCase(repository)

    @Test
    fun `GIVEN fetch smokes answers WHEN invoke is executed THEN it return `() = runTest {
        val today: Date = mockk()
        val thisWeek: Date = mockk()
        val thisMonth: Date = mockk()

        mockkStatic(Date::isToday)
        mockkStatic(Date::isThisWeek)
        mockkStatic(Date::isThisMonth)
        mockkStatic(Date::timeAfter)

        val todayTimeAfter: Pair<Long, Long> = mockk()
        every { today.isToday() } answers { true }
        every { today.isThisWeek() } answers { true }
        every { today.isThisMonth() } answers { true }
        every { today.timeAfter(thisWeek) } answers { todayTimeAfter }

        every { thisWeek.isToday() } answers { false }
        every { thisWeek.isThisWeek() } answers { true }
        every { thisWeek.isThisMonth() } answers { true }
        every { thisWeek.timeAfter(thisMonth) } answers { mockk() }

        every { thisMonth.isToday() } answers { false }
        every { thisMonth.isThisWeek() } answers { false }
        every { thisMonth.isThisMonth() } answers { true }
        every { thisMonth.timeAfter(null) } answers { mockk() }

        val todaySmoke = Smoke(today, todayTimeAfter)
        coEvery { repository.fetchSmokes() } answers {
            listOf(
                todaySmoke,
                Smoke(thisWeek, mockk()),
                Smoke(thisMonth, mockk()),
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
