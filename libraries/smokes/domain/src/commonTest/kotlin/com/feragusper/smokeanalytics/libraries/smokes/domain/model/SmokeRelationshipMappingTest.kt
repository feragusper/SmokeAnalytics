package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SmokeRelationshipMappingTest {

    @Test
    fun `GIVEN no fields THEN it maps to Untracked`() {
        assertEquals(
            SmokeRelationship.Untracked,
            smokeRelationshipFromFields(triggers = null, note = null, skipped = null),
        )
    }

    @Test
    fun `GIVEN empty triggers and blank note THEN it maps to Untracked`() {
        assertEquals(
            SmokeRelationship.Untracked,
            smokeRelationshipFromFields(triggers = emptyList(), note = "  ", skipped = false),
        )
    }

    @Test
    fun `GIVEN skipped flag THEN it maps to Skipped regardless of triggers`() {
        assertEquals(
            SmokeRelationship.Skipped,
            smokeRelationshipFromFields(triggers = listOf("coffee"), note = "x", skipped = true),
        )
    }

    @Test
    fun `GIVEN known triggers and note THEN it maps to Tagged ignoring unknown keys`() {
        val relationship = smokeRelationshipFromFields(
            triggers = listOf("coffee", "alcohol", "not_a_real_key"),
            note = " after lunch ",
            skipped = false,
        )
        assertEquals(
            SmokeRelationship.Tagged(
                triggers = setOf(SmokeTrigger.COFFEE, SmokeTrigger.ALCOHOL),
                note = "after lunch",
            ),
            relationship,
        )
    }

    @Test
    fun `GIVEN a Tagged relationship THEN the persisted fields round-trip`() {
        val original = SmokeRelationship.Tagged(
            triggers = setOf(SmokeTrigger.BOREDOM, SmokeTrigger.PHONE),
            note = "scrolling",
        )
        val restored = smokeRelationshipFromFields(
            triggers = original.triggerKeys(),
            note = original.noteOrNull(),
            skipped = original.skippedFlag(),
        )
        assertEquals(original, restored)
    }

    @Test
    fun `GIVEN Skipped THEN it exposes the skipped flag and no triggers`() {
        assertEquals(true, SmokeRelationship.Skipped.skippedFlag())
        assertEquals(emptyList(), SmokeRelationship.Skipped.triggerKeys())
        assertEquals(null, SmokeRelationship.Skipped.noteOrNull())
    }
}
