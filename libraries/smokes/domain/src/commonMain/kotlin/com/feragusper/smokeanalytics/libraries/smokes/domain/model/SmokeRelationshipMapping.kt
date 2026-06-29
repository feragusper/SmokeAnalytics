package com.feragusper.smokeanalytics.libraries.smokes.domain.model

/**
 * Shared encode/decode between the persisted Firestore fields and [SmokeRelationship],
 * so the mobile and web data modules stay in sync.
 *
 * Persisted fields:
 *  - triggers: list of [SmokeTrigger.key] values
 *  - note: free-text "Other"
 *  - skipped: true when the user declared no relation
 *
 * Precedence on read: skipped → tagged (any trigger or note) → untracked.
 */
fun smokeRelationshipFromFields(
    triggers: List<String>?,
    note: String?,
    skipped: Boolean?,
): SmokeRelationship {
    if (skipped == true) return SmokeRelationship.Skipped
    val parsedTriggers = triggers.orEmpty().mapNotNull(SmokeTrigger::fromKey).toSet()
    val cleanNote = note?.trim()?.takeIf { it.isNotEmpty() }
    return if (parsedTriggers.isNotEmpty() || cleanNote != null) {
        SmokeRelationship.Tagged(triggers = parsedTriggers, note = cleanNote)
    } else {
        SmokeRelationship.Untracked
    }
}

/** The persisted `triggers` value for this relationship (empty unless [SmokeRelationship.Tagged]). */
fun SmokeRelationship.triggerKeys(): List<String> = when (this) {
    is SmokeRelationship.Tagged -> triggers.map { it.key }
    else -> emptyList()
}

/** The persisted `triggerNote` value for this relationship. */
fun SmokeRelationship.noteOrNull(): String? = (this as? SmokeRelationship.Tagged)
    ?.note?.trim()?.takeIf { it.isNotEmpty() }

/** The persisted `relationshipSkipped` flag for this relationship. */
fun SmokeRelationship.skippedFlag(): Boolean = this is SmokeRelationship.Skipped
