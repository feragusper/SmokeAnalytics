package com.feragusper.smokeanalytics.libraries.smokes.domain

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
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
