package com.feragusper.smokeanalytics.libraries.architecture.common.coroutines

import kotlinx.coroutines.Dispatchers
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test

class DispatcherProviderImplTest {

    private val dispatcherProvider = DispatcherProviderImpl()

    @Test
    fun `WHEN default is called THEN it returns Dispatchers Default`() {
        dispatcherProvider.default() shouldBe Dispatchers.Default
    }

    @Test
    fun `WHEN io is called THEN it returns Dispatchers IO`() {
        dispatcherProvider.io() shouldBe Dispatchers.IO
    }

    @Test
    fun `WHEN main is called THEN it returns Dispatchers Main`() {
        dispatcherProvider.main() shouldBe Dispatchers.Main
    }
}
