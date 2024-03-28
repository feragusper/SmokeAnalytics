package com.feragusper.smokeanalytics.libraries.smokes.domain

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class FetchSmokesUseCaseTest {

    private val repository: SmokeRepository = mockk()
    private val useCase = FetchSmokesUseCase(repository)

    @Test
    fun `GIVEN fetch smokes answers WHEN invoke is executed THEN it returns`() = runTest {
        val smoke: Smoke = mockk()

        coEvery { repository.fetchSmokes() } answers {
            listOf(smoke)
        }

        assertEquals(
            useCase.invoke(), listOf(smoke)
        )
    }

    @Test
    fun `GIVEN fetch smokes by date answers WHEN invoke with date is executed THEN it returns`() = runTest {
        val smoke: Smoke = mockk()

        val date: LocalDateTime = mockk()
        coEvery { repository.fetchSmokes(date) } answers {
            listOf(smoke)
        }

        assertEquals(
            useCase.invoke(date), listOf(smoke)
        )
    }
}
