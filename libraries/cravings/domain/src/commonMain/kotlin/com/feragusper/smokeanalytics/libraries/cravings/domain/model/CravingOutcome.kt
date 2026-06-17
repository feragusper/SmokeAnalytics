package com.feragusper.smokeanalytics.libraries.cravings.domain.model

/**
 * How a tracked craving ended.
 */
enum class CravingOutcome {
    /** Still waiting / unresolved. */
    PENDING,

    /** The user waited and let the urge pass without smoking. The best outcome. */
    RESISTED,

    /** The user waited the suggested time and then smoked. The cigarette was delayed. */
    POSTPONED,

    /** The user smoked without waiting. No reward. */
    GAVE_IN,
}
