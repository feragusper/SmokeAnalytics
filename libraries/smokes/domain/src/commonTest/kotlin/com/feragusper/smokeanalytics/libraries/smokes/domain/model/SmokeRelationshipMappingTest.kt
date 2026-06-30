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
    fun `GIVEN tags and a legacy note THEN all are folded into the tag set`() {
        val relationship = smokeRelationshipFromFields(
            triggers = listOf("coffee", "my custom tag"),
            note = " after lunch ",
            skipped = false,
        )
        assertEquals(
            SmokeRelationship.Tagged(tags = setOf("coffee", "my custom tag", "after lunch")),
            relationship,
        )
    }

    @Test
    fun `GIVEN a Tagged relationship THEN the persisted fields round-trip`() {
        val original = SmokeRelationship.Tagged(tags = setOf("boredom", "Phone scroll"))
        val restored = smokeRelationshipFromFields(
            triggers = original.triggerKeys(),
            note = null,
            skipped = original.skippedFlag(),
        )
        assertEquals(original, restored)
    }

    @Test
    fun `GIVEN Skipped THEN it exposes the skipped flag and no tags`() {
        assertEquals(true, SmokeRelationship.Skipped.skippedFlag())
        assertEquals(emptyList(), SmokeRelationship.Skipped.triggerKeys())
    }

    @Test
    fun `GIVEN relationships THEN only Untracked is pending`() {
        assertEquals(true, SmokeRelationship.Untracked.isPending)
        assertEquals(false, SmokeRelationship.Skipped.isPending)
        assertEquals(false, SmokeRelationship.Tagged(setOf("coffee")).isPending)
    }

    @Test
    fun `GIVEN a key THEN labelFor resolves defaults and passes through unknown`() {
        assertEquals("Coffee", SmokeTrigger.labelFor("coffee"))
        assertEquals("my custom tag", SmokeTrigger.labelFor("my custom tag"))
    }

    @Test
    fun `GIVEN the catalog with custom triggers THEN defaults and custom are combined`() {
        val catalog = SmokeTrigger.catalog(
            customTriggers = listOf("Gaming", "coffee"),
            hiddenDefaultKeys = setOf("phone"),
        )
        // Defaults minus hidden 'phone' (9), plus 2 custom.
        assertEquals(9 + 2, catalog.size)
        assertEquals(false, catalog.any { it.key == "phone" })
        assertEquals(true, catalog.any { it.key == "Gaming" && it.isCustom })
    }
}
