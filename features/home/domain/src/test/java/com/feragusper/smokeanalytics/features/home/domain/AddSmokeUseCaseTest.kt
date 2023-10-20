package com.feragusper.smokeanalytics.features.home.domain

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class AddSmokeUseCaseTest {

    private val smokeRepository: SmokeRepository = mockk()
    private val addSmokeUseCase = AddSmokeUseCase(smokeRepository)

    @Test
    fun `GIVEN the session is anonymous WHEN invoke is executed THEN it should return anonymous session`() {
        coEvery { smokeRepository.addSmoke() } answers { }

        runBlocking {
            addSmokeUseCase.invoke()
        }
    }
}
