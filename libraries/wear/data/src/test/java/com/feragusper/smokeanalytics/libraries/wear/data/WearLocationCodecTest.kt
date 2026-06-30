package com.feragusper.smokeanalytics.libraries.wear.data

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class WearLocationCodecTest {

    @Test
    fun `GIVEN coordinates WHEN encoded and decoded THEN they round-trip`() {
        val encoded = WearLocationCodec.encode(latitude = 12.34, longitude = -56.78)
        assertEquals(GeoPoint(latitude = 12.34, longitude = -56.78), WearLocationCodec.decode(encoded))
    }

    @Test
    fun `GIVEN null data WHEN decoded THEN it returns null`() {
        assertNull(WearLocationCodec.decode(null))
    }

    @Test
    fun `GIVEN blank data WHEN decoded THEN it returns null`() {
        assertNull(WearLocationCodec.decode("   ".encodeToByteArray()))
    }

    @Test
    fun `GIVEN malformed data WHEN decoded THEN it returns null`() {
        assertNull(WearLocationCodec.decode("not-a-coordinate".encodeToByteArray()))
        assertNull(WearLocationCodec.decode("12.34".encodeToByteArray()))
        assertNull(WearLocationCodec.decode("12.34;abc".encodeToByteArray()))
    }

    @Test
    fun `GIVEN coordinates WHEN encoded THEN the format is lat semicolon lng`() {
        assertEquals("1.5;2.5", WearLocationCodec.encode(latitude = 1.5, longitude = 2.5).decodeToString())
    }
}
