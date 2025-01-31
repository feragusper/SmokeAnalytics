package com.feragusper.smokeanalytics.libraries.smokes.domain

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FetchSmokesUseCaseTest {

    private val repository: SmokeRepository = mockk()
    private val useCase = FetchSmokeCountUseCase(repository)

    @Test
    fun `GIVEN fetch smokes by date answers WHEN invoke with date is executed THEN it returns`() =
        runTest {
            val smokeCount: SmokeCount = mockk()

            coEvery { repository.fetchSmokeCount() } answers { smokeCount }

            assertEquals(
                useCase.invoke(), smokeCount
            )
        }
}
