package com.feragusper.smokeanalytics.libraries.smokes.domain.model

/**
 * A predefined context/trigger a smoke can be related to (coffee, boredom, etc.).
 *
 * [key] is the stable string persisted in Firestore — never rename a key without a
 * migration, because existing documents store these values. The display label is
 * resolved in the presentation layer so it can be localized.
 */
enum class SmokeTrigger(val key: String) {
    COFFEE("coffee"),
    ALCOHOL("alcohol"),
    BOREDOM("boredom"),
    ANXIETY("anxiety"),
    STRESS("stress"),
    AFTER_MEAL("after_meal"),
    SOCIAL("social"),
    BREAK("break"),
    DRIVING("driving"),
    PHONE("phone");

    companion object {
        /** Resolves a persisted [key] back to a [SmokeTrigger], ignoring unknown values. */
        fun fromKey(key: String): SmokeTrigger? = entries.firstOrNull { it.key == key }
    }
}
