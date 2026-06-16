package com.feragusper.smokeanalytics.libraries.cravings.domain.model

/**
 * Aggregate view over a list of cravings, for the home stats card and history.
 *
 * @property total Number of resolved cravings (pending ones are excluded).
 * @property resisted How many urges were fully resisted.
 * @property postponed How many cigarettes were delayed after waiting.
 * @property gaveIn How many urges were given in to without waiting.
 * @property minutesWaited Total minutes the user spent waiting out resolved cravings.
 * @property points Total reward points earned from cravings.
 */
data class CravingStats(
    val total: Int = 0,
    val resisted: Int = 0,
    val postponed: Int = 0,
    val gaveIn: Int = 0,
    val minutesWaited: Long = 0,
    val points: Int = 0,
) {
    /** Share of resolved urges that did not end in an immediate cigarette. */
    val winRate: Float
        get() = if (total == 0) 0f else (resisted + postponed).toFloat() / total.toFloat()
}

/**
 * Builds [CravingStats] from a list of cravings. Pending cravings are ignored.
 */
fun List<Craving>.toCravingStats(): CravingStats {
    val resolved = filter { !it.isPending }
    if (resolved.isEmpty()) return CravingStats()

    var resisted = 0
    var postponed = 0
    var gaveIn = 0
    var minutesWaited = 0L
    var points = 0

    for (craving in resolved) {
        when (craving.outcome) {
            CravingOutcome.RESISTED -> resisted++
            CravingOutcome.POSTPONED -> postponed++
            CravingOutcome.GAVE_IN -> gaveIn++
            CravingOutcome.PENDING -> Unit
        }
        points += craving.pointsAwarded
        val resolvedAt = craving.resolvedAt
        if (resolvedAt != null && craving.outcome != CravingOutcome.GAVE_IN) {
            minutesWaited += (resolvedAt - craving.createdAt).inWholeMinutes.coerceAtLeast(0)
        }
    }

    return CravingStats(
        total = resolved.size,
        resisted = resisted,
        postponed = postponed,
        gaveIn = gaveIn,
        minutesWaited = minutesWaited,
        points = points,
    )
}
