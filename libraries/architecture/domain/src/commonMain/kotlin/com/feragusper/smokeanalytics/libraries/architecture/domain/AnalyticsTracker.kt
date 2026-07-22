package com.feragusper.smokeanalytics.libraries.architecture.domain

/**
 * Platform-agnostic analytics façade. Implementations forward to Firebase Analytics (mobile) or
 * gtag/GA4 (web). Only [screenView] and [logEvent] need a real implementation; the typed helpers
 * below build on them so call sites stay readable and event names/params stay consistent.
 *
 * Event and parameter names follow the GA4 snake_case convention and stay ≤ 40 chars.
 */
interface AnalyticsTracker {

    /** Records a screen view. [screenName] is one of [AnalyticsScreen]. */
    fun screenView(screenName: String)

    /** Records a custom event with optional string/number/bool params. */
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())

    /** Sets the signed-in user id (or null on sign-out) so events can be attributed. */
    fun setUserId(userId: String?) {}

    // --- Transactions ---------------------------------------------------------------------

    /** A cigarette was logged. [source] is where from (home, history, widget, wear, quick_add). */
    fun smokeAdded(source: String) = logEvent(AnalyticsEvent.SMOKE_ADDED, mapOf(AnalyticsParam.SOURCE to source))

    fun smokeEdited(source: String) = logEvent(AnalyticsEvent.SMOKE_EDITED, mapOf(AnalyticsParam.SOURCE to source))

    fun smokeDeleted(source: String) = logEvent(AnalyticsEvent.SMOKE_DELETED, mapOf(AnalyticsParam.SOURCE to source))

    /** The user started tracking a craving (before deciding to smoke). */
    fun cravingTracked() = logEvent(AnalyticsEvent.CRAVING_TRACKED)

    /** A craving resolved: [smoked] true if they gave in, false if the urge passed. */
    fun cravingResolved(smoked: Boolean) =
        logEvent(AnalyticsEvent.CRAVING_RESOLVED, mapOf(AnalyticsParam.SMOKED to smoked))

    /** A craving was dismissed (tracked by mistake) without recording an outcome. */
    fun cravingDismissed() = logEvent(AnalyticsEvent.CRAVING_DISMISSED)

    /** The user set/updated their active goal. [goalType] is the goal kind. */
    fun goalSet(goalType: String) = logEvent(AnalyticsEvent.GOAL_SET, mapOf(AnalyticsParam.GOAL_TYPE to goalType))

    fun goalCleared() = logEvent(AnalyticsEvent.GOAL_CLEARED)

    /** A smoke was tagged with a relationship/trigger. [tagCount] = how many tags. */
    fun relationshipTagged(tagCount: Int) =
        logEvent(AnalyticsEvent.RELATIONSHIP_TAGGED, mapOf(AnalyticsParam.TAG_COUNT to tagCount))

    fun relationshipSkipped() = logEvent(AnalyticsEvent.RELATIONSHIP_SKIPPED)

    /** A new reflection day was started from Home. */
    fun newDayStarted() = logEvent(AnalyticsEvent.NEW_DAY_STARTED)

    // --- Account & preferences ------------------------------------------------------------

    fun login(method: String = "google") = logEvent(AnalyticsEvent.LOGIN, mapOf(AnalyticsParam.METHOD to method))

    fun logout() = logEvent(AnalyticsEvent.LOGOUT)

    fun shareApp() = logEvent(AnalyticsEvent.SHARE_APP)

    fun languageChanged(language: String) =
        logEvent(AnalyticsEvent.LANGUAGE_CHANGED, mapOf(AnalyticsParam.LANGUAGE to language))

    fun accentChanged(accent: String) =
        logEvent(AnalyticsEvent.ACCENT_CHANGED, mapOf(AnalyticsParam.ACCENT to accent))
}

/** A no-op tracker for previews, tests, and platforms without analytics configured. */
object NoOpAnalyticsTracker : AnalyticsTracker {
    override fun screenView(screenName: String) {}
    override fun logEvent(name: String, params: Map<String, Any>) {}
}

/** Canonical screen names for [AnalyticsTracker.screenView]. */
object AnalyticsScreen {
    const val HOME = "home"
    const val HISTORY = "history"
    const val ANALYTICS = "analytics"
    const val MAP = "map"
    const val GOALS = "goals"
    const val GOALS_CONFIGURE = "goals_configure"
    const val SETTINGS = "settings"
    const val AUTHENTICATION = "authentication"
}

/** Canonical event names (GA4 snake_case, ≤ 40 chars). */
object AnalyticsEvent {
    const val SMOKE_ADDED = "smoke_added"
    const val SMOKE_EDITED = "smoke_edited"
    const val SMOKE_DELETED = "smoke_deleted"
    const val CRAVING_TRACKED = "craving_tracked"
    const val CRAVING_RESOLVED = "craving_resolved"
    const val CRAVING_DISMISSED = "craving_dismissed"
    const val GOAL_SET = "goal_set"
    const val GOAL_CLEARED = "goal_cleared"
    const val RELATIONSHIP_TAGGED = "relationship_tagged"
    const val RELATIONSHIP_SKIPPED = "relationship_skipped"
    const val NEW_DAY_STARTED = "new_day_started"
    const val LOGIN = "login"
    const val LOGOUT = "logout"
    const val SHARE_APP = "share_app"
    const val LANGUAGE_CHANGED = "language_changed"
    const val ACCENT_CHANGED = "accent_changed"
}

/** Canonical parameter keys. */
object AnalyticsParam {
    const val SOURCE = "source"
    const val SMOKED = "smoked"
    const val GOAL_TYPE = "goal_type"
    const val TAG_COUNT = "tag_count"
    const val METHOD = "method"
    const val LANGUAGE = "language"
    const val ACCENT = "accent"
}

/** Common values for [AnalyticsParam.SOURCE]. */
object AnalyticsSource {
    const val HOME = "home"
    const val HISTORY = "history"
    const val WIDGET = "widget"
    const val WEAR = "wear"
    const val QUICK_ADD = "quick_add"
}
