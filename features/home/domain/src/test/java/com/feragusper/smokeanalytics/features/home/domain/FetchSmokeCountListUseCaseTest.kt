package com.feragusper.smokeanalytics.features.home.domain

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isThisWeek
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isToday
import io.mockk.mockkStatic
import java.util.Date

class FetchSmokeCountListUseCaseTest {

    private val repository: SmokeRepository = mockk()
    private val useCase = FetchSmokeCountListUseCase(repository)

    @Test
    fun `GIVEN fetch smokes answers WHEN invoke is executed THEN it return `() {
        val today: Date = mockk()
        val thisWeek: Date = mockk()
        val thisMonth: Date = mockk()

        mockkStatic(Date::isToday)
        mockkStatic(Date::isThisWeek)
        mockkStatic(Date::isThisMonth)

        every { today.isToday() } answers { true }
        every { today.isThisWeek() } answers { true }
        every { today.isThisMonth() } answers { true }

        every { thisWeek.isToday() } answers { false }
        every { thisWeek.isThisWeek() } answers { true }
        every { thisWeek.isThisMonth() } answers { true }

        every { thisMonth.isToday() } answers { false }
        every { thisMonth.isThisWeek() } answers { false }
        every { thisMonth.isThisMonth() } answers { true }

        coEvery { repository.fetchSmokes() } answers {
            listOf(
                Smoke(today),
                Smoke(thisWeek),
                Smoke(thisMonth),
            )
        }

        runBlocking {
            assertEquals(
                useCase.invoke(), SmokeCountListResult(
                    byToday = 1,
                    byWeek = 2,
                    byMonth = 3,
                )
            )
        }
    }
}
