package com.feragusper.smokeanalytics.libraries.smokes.domain

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FetchSmokesUseCaseTest {

    private val repository: SmokeRepository = mockk()
    private val useCase = FetchSmokesUseCase(repository)

    @Test
    fun `GIVEN fetch smokes answers WHEN invoke is executed THEN it return `() = runTest {
        val smoke: Smoke = mockk()

        coEvery { repository.fetchSmokes() } answers {
            listOf(smoke)
        }

        assertEquals(
            useCase.invoke(), listOf(smoke)
        )
    }
}
