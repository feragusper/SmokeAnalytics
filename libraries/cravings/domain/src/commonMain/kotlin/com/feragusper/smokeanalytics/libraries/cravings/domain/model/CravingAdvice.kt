package com.feragusper.smokeanalytics.libraries.cravings.domain.model

import kotlinx.datetime.Instant

/**
 * The recommendation produced when the user tracks an urge, based on the active
 * goal and the time of the last cigarette.
 *
 * @property recommendedGapMinutes The minimum gap the active goal implies between
 *   cigarettes, or null when the goal does not pace individual cigarettes.
 * @property nextAllowedAt The earliest moment the user should smoke again, or null
 *   when there is no pacing (no goal that defines a gap, or no previous smoke).
 * @property canSmokeNow True when no wait is required.
 * @property waitMinutes Whole minutes remaining until [nextAllowedAt] (0 when
 *   [canSmokeNow]).
 */
data class CravingAdvice(
    val recommendedGapMinutes: Int?,
    val nextAllowedAt: Instant?,
    val canSmokeNow: Boolean,
    val waitMinutes: Int,
)
