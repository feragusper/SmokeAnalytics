package com.feragusper.smokeanalytics.libraries.smokes.domain.model

/**
 * The built-in seed catalog of smoke triggers. Tags are persisted and selected as plain
 * string keys (see [SmokeRelationship.Tagged]); this enum only provides the default keys
 * and their display labels, on top of which users add their own custom tags.
 *
 * [key] is the stable string stored in Firestore — never rename a key without a migration.
 */
enum class SmokeTrigger(val key: String, val defaultLabel: String) {
    COFFEE("coffee", "Coffee"),
    ALCOHOL("alcohol", "Alcohol"),
    BOREDOM("boredom", "Boredom"),
    ANXIETY("anxiety", "Anxiety"),
    STRESS("stress", "Stress"),
    AFTER_MEAL("after_meal", "After a meal"),
    SOCIAL("social", "Social"),
    BREAK("break", "Break"),
    DRIVING("driving", "Driving"),
    PHONE("phone", "Phone");

    companion object {
        /** Default trigger options, in declaration order. */
        fun defaultOptions(): List<TriggerOption> =
            entries.map { TriggerOption(key = it.key, label = it.defaultLabel, isCustom = false) }

        /** Display label for a persisted tag key: a default label if known, else the key itself. */
        fun labelFor(key: String): String =
            entries.firstOrNull { it.key == key }?.defaultLabel ?: key

        /**
         * The selectable trigger catalog: visible defaults (minus [hiddenDefaultKeys]) plus the
         * user's [customTriggers]. Pure function so it can be built from preferences without
         * coupling the preferences module to this one.
         */
        fun catalog(
            customTriggers: List<String>,
            hiddenDefaultKeys: Set<String> = emptySet(),
        ): List<TriggerOption> {
            val defaults = entries
                .filter { it.key !in hiddenDefaultKeys }
                .map { TriggerOption(key = it.key, label = it.defaultLabel, isCustom = false) }
            val custom = customTriggers
                .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
                .distinct()
                .map { TriggerOption(key = it, label = it, isCustom = true) }
            return defaults + custom
        }
    }
}

/**
 * A selectable trigger in the prompt / management catalog.
 *
 * @property key The persisted tag key (a [SmokeTrigger.key] for defaults, or the custom string).
 * @property label Human-readable label.
 * @property isCustom True for user-created tags (deletable), false for built-in defaults.
 */
data class TriggerOption(
    val key: String,
    val label: String,
    val isCustom: Boolean,
)
