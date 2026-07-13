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

    @Test
    fun `GIVEN icon overrides THEN they replace defaults and decorate custom tags`() {
        val catalog = SmokeTrigger.catalog(
            customTriggers = listOf("Gaming"),
            iconOverrides = mapOf("coffee" to "🔥", "Gaming" to "🎮", "boredom" to "  "),
        )
        // Override replaces the built-in default icon.
        assertEquals("🔥", catalog.first { it.key == "coffee" }.icon)
        // Blank override falls back to the default icon.
        assertEquals(SmokeTrigger.BOREDOM.defaultIcon, catalog.first { it.key == "boredom" }.icon)
        // Untouched built-ins keep their default; custom tags get theirs from the override.
        assertEquals(SmokeTrigger.ALCOHOL.defaultIcon, catalog.first { it.key == "alcohol" }.icon)
        assertEquals("🎮", catalog.first { it.key == "Gaming" }.icon)
    }

    @Test
    fun `GIVEN label overrides THEN they rename built-ins and custom tags without changing keys`() {
        val catalog = SmokeTrigger.catalog(
            customTriggers = listOf("Gaming"),
            labelOverrides = mapOf("coffee" to "Mate", "Gaming" to "Play", "boredom" to "  "),
        )
        assertEquals("Mate", catalog.first { it.key == "coffee" }.label)
        assertEquals("Play", catalog.first { it.key == "Gaming" }.label)
        // Blank override falls back to the original name; keys never change.
        assertEquals(SmokeTrigger.BOREDOM.defaultLabel, catalog.first { it.key == "boredom" }.label)
        assertEquals(true, catalog.any { it.key == "Gaming" })
    }

    @Test
    fun `GIVEN the emoji palette THEN it is distinct and covers every default icon`() {
        assertEquals(TriggerEmojiPalette.size, TriggerEmojiPalette.distinct().size)
        SmokeTrigger.entries.forEach { trigger ->
            assertEquals(true, trigger.defaultIcon in TriggerEmojiPalette)
        }
    }

    @Test
    fun `GIVEN a blank query THEN searchEmojis returns the whole catalog`() {
        assertEquals(EmojiCatalog, searchEmojis("   "))
    }

    @Test
    fun `GIVEN a keyword THEN searchEmojis filters by it and matches the glyph`() {
        val coffee = searchEmojis("coffee")
        assertEquals(true, coffee.any { it.emoji == "☕" })
        assertEquals(true, coffee.all { it.keywords.any { k -> k.contains("coffee") } })
        // The raw glyph resolves to its own entry.
        assertEquals(true, searchEmojis("🍺").any { it.emoji == "🍺" })
        assertEquals(true, searchEmojis("zzzzzz").isEmpty())
    }

    @Test
    fun `GIVEN an option THEN display prefixes the icon when present`() {
        assertEquals(
            "🎮 Gaming",
            TriggerOption(key = "Gaming", label = "Gaming", isCustom = true, icon = "🎮").display,
        )
        assertEquals(
            "Gaming",
            TriggerOption(key = "Gaming", label = "Gaming", isCustom = true, icon = null).display,
        )
    }
}
