package com.feragusper.smokeanalytics.features.home.domain

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class DeleteSmokeUseCaseTest {

    private val smokeRepository: SmokeRepository = mockk()
    private val deleteSmokeUseCase = DeleteSmokeUseCase(smokeRepository)

    @Test
    fun `GIVEN delete smoke just runs WHEN invoke is executed THEN it should finish`() {
        val id = "id"
        coEvery { smokeRepository.deleteSmoke(id) } just Runs

        runBlocking {
            deleteSmokeUseCase.invoke(id)
        }
    }
}
