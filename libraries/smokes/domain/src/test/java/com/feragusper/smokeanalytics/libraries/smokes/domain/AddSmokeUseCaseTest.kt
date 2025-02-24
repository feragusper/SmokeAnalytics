package com.feragusper.smokeanalytics.libraries.smokes.domain

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AddSmokeUseCaseTest {

    private val smokeRepository: SmokeRepository = mockk()
    private val addSmokeUseCase = AddSmokeUseCase(smokeRepository)

    @Test
    fun `GIVEN add smoke answers WHEN invoke is executed THEN it should finish`() = runTest {
        coEvery { smokeRepository.addSmoke(any()) } just Runs

        addSmokeUseCase.invoke()
    }
}
