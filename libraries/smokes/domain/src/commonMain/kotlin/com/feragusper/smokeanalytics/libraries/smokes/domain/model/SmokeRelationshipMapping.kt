package com.feragusper.smokeanalytics.libraries.smokes.domain.model

/**
 * Shared encode/decode between the persisted Firestore fields and [SmokeRelationship],
 * so the mobile and web data modules stay in sync.
 *
 * Persisted fields:
 *  - triggers: list of tag keys (built-in [SmokeTrigger.key] values and/or custom strings)
 *  - note: legacy free-text "Other" (older docs); folded into the tag set on read
 *  - skipped: true when the user declared no relation
 *
 * Precedence on read: skipped → tagged (any tag) → untracked.
 */
fun smokeRelationshipFromFields(
    triggers: List<String>?,
    note: String?,
    skipped: Boolean?,
): SmokeRelationship {
    if (skipped == true) return SmokeRelationship.Skipped
    val tags = buildSet {
        triggers.orEmpty().forEach { tag -> tag.normalizedTag()?.let(::add) }
        // Legacy docs stored the "Other" free text separately; treat it as a tag.
        note.normalizedTag()?.let(::add)
    }
    return if (tags.isNotEmpty()) SmokeRelationship.Tagged(tags = tags) else SmokeRelationship.Untracked
}

/** The persisted `triggers` value for this relationship (empty unless [SmokeRelationship.Tagged]). */
fun SmokeRelationship.triggerKeys(): List<String> = when (this) {
    is SmokeRelationship.Tagged -> tags.toList()
    else -> emptyList()
}

/** The persisted `relationshipSkipped` flag for this relationship. */
fun SmokeRelationship.skippedFlag(): Boolean = this is SmokeRelationship.Skipped

/** Trims a tag and discards blanks; tags are stored as-is (keys for defaults, labels for custom). */
fun String?.normalizedTag(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
