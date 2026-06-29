package com.feragusper.smokeanalytics.libraries.smokes.domain.model

/**
 * What a smoke was related to.
 *
 * A smoke starts as [Untracked]: smokes logged from the watch, or smokes whose
 * prompt the user dismissed, have no relationship yet and are surfaced in the
 * home reminder card. The user can then either tag it ([Tagged]) or declare it
 * had no particular trigger ([Skipped]); both states stop the reminder.
 */
sealed interface SmokeRelationship {

    /** Never answered — shown in the home reminder card. */
    data object Untracked : SmokeRelationship

    /** The user explicitly said this smoke had no particular trigger. */
    data object Skipped : SmokeRelationship

    /**
     * The user attached one or more triggers, and optionally a free-text [note]
     * (the "Other" option).
     */
    data class Tagged(
        val triggers: Set<SmokeTrigger> = emptySet(),
        val note: String? = null,
    ) : SmokeRelationship

    /** True while the smoke still needs the user's attention in the reminder card. */
    val isPending: Boolean get() = this is Untracked
}
