package com.feragusper.smokeanalytics.libraries.smokes.domain

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.Date

class EditSmokeUseCaseTest {

    private val smokeRepository: SmokeRepository = mockk()
    private val editSmokeUseCase = EditSmokeUseCase(smokeRepository)

    @Test
    fun `GIVEN edit smoke just runs WHEN invoke is executed THEN it should finish`() {
        val id = "id"
        val date: Date = mockk()
        coEvery { smokeRepository.editSmoke(id, date) } just Runs

        runBlocking {
            editSmokeUseCase.invoke(id, date)
        }
    }
}
