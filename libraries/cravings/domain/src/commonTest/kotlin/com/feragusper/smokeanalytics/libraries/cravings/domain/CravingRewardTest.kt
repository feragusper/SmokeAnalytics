package com.feragusper.smokeanalytics.libraries.cravings.domain

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import kotlin.test.Test
import kotlin.test.assertEquals

class CravingRewardTest {

    @Test
    fun `resisting awards minutes plus bonus`() {
        // 40 / 5 + 10 = 18
        assertEquals(18, CravingReward.pointsFor(CravingOutcome.RESISTED, waitedMinutes = 40))
    }

    @Test
    fun `postponing awards minutes only`() {
        assertEquals(8, CravingReward.pointsFor(CravingOutcome.POSTPONED, waitedMinutes = 40))
    }

    @Test
    fun `giving in awards nothing`() {
        assertEquals(0, CravingReward.pointsFor(CravingOutcome.GAVE_IN, waitedMinutes = 40))
    }

    @Test
    fun `pending awards nothing`() {
        assertEquals(0, CravingReward.pointsFor(CravingOutcome.PENDING, waitedMinutes = 40))
    }

    @Test
    fun `negative waited minutes are clamped`() {
        assertEquals(CravingReward.RESIST_BONUS, CravingReward.pointsFor(CravingOutcome.RESISTED, waitedMinutes = -5))
    }
}
