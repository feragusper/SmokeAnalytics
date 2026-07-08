package com.feragusper.smokeanalytics.libraries.smokes.domain.model

/**
 * The built-in seed catalog of smoke triggers. Tags are persisted and selected as plain
 * string keys (see [SmokeRelationship.Tagged]); this enum only provides the default keys,
 * display labels and icons, on top of which users add their own custom tags.
 *
 * Icons are emoji strings: they render identically on Android and web, persist as plain
 * text, and stay user-editable (see `UserPreferences.triggerIcons`).
 *
 * [key] is the stable string stored in Firestore — never rename a key without a migration.
 */
enum class SmokeTrigger(val key: String, val defaultLabel: String, val defaultIcon: String) {
    COFFEE("coffee", "Coffee", "☕"),
    ALCOHOL("alcohol", "Alcohol", "🍺"),
    BOREDOM("boredom", "Boredom", "🥱"),
    ANXIETY("anxiety", "Anxiety", "😰"),
    STRESS("stress", "Stress", "😖"),
    AFTER_MEAL("after_meal", "After a meal", "🍽️"),
    SOCIAL("social", "Social", "👥"),
    BREAK("break", "Break", "⏸️"),
    DRIVING("driving", "Driving", "🚗"),
    PHONE("phone", "Phone", "📱");

    companion object {
        /** Default trigger options, in declaration order. */
        fun defaultOptions(): List<TriggerOption> =
            entries.map {
                TriggerOption(key = it.key, label = it.defaultLabel, isCustom = false, icon = it.defaultIcon)
            }

        /** Display label for a persisted tag key: a default label if known, else the key itself. */
        fun labelFor(key: String): String =
            entries.firstOrNull { it.key == key }?.defaultLabel ?: key

        /**
         * The selectable trigger catalog: visible defaults (minus [hiddenDefaultKeys]) plus the
         * user's [customTriggers]. [iconOverrides] (tag key → emoji) replaces the default icon
         * of built-ins and gives custom tags one; [labelOverrides] (tag key → name) renames a
         * tag without touching the key persisted on smokes, so history and analytics keep
         * counting under the same key. Pure function so it can be built from preferences
         * without coupling the preferences module to this one.
         */
        fun catalog(
            customTriggers: List<String>,
            hiddenDefaultKeys: Set<String> = emptySet(),
            iconOverrides: Map<String, String> = emptyMap(),
            labelOverrides: Map<String, String> = emptyMap(),
        ): List<TriggerOption> {
            fun iconFor(key: String, default: String?): String? =
                iconOverrides[key]?.trim()?.takeIf { it.isNotEmpty() } ?: default

            fun labelFor(key: String, default: String): String =
                labelOverrides[key]?.trim()?.takeIf { it.isNotEmpty() } ?: default

            val defaults = entries
                .filter { it.key !in hiddenDefaultKeys }
                .map {
                    TriggerOption(
                        key = it.key,
                        label = labelFor(it.key, it.defaultLabel),
                        isCustom = false,
                        icon = iconFor(it.key, it.defaultIcon),
                    )
                }
            val custom = customTriggers
                .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
                .distinct()
                .map {
                    TriggerOption(
                        key = it,
                        label = labelFor(it, it),
                        isCustom = true,
                        icon = iconFor(it, null),
                    )
                }
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
 * @property icon Emoji shown alongside the label, or null when the tag has none.
 */
data class TriggerOption(
    val key: String,
    val label: String,
    val isCustom: Boolean,
    val icon: String? = null,
) {
    /** Chip/list text: "☕ Coffee" when an icon is set, plain label otherwise. */
    val display: String get() = icon?.let { "$it $label" } ?: label
}
