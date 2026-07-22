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

    // --- UI interactions ------------------------------------------------------------------

    /**
     * A meaningful control was tapped/clicked. [screen] is one of [AnalyticsScreen] and
     * [target] one of [AnalyticsTarget] — together they identify which button on which
     * screen the user pressed. Decorative or purely cosmetic controls are not tracked.
     */
    fun buttonTap(screen: String, target: String) = logEvent(
        AnalyticsEvent.BUTTON_TAP,
        mapOf(AnalyticsParam.SCREEN to screen, AnalyticsParam.TARGET to target),
    )

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
    const val BUTTON_TAP = "button_tap"
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
    const val SCREEN = "screen"
    const val TARGET = "target"
}

/**
 * Canonical [AnalyticsParam.TARGET] ids for [AnalyticsTracker.buttonTap], grouped by screen.
 * Values are GA4 snake_case and stable — renaming one breaks historical funnels, so append
 * rather than repurpose. Ids are screen-local; the event also carries the screen name.
 */
object AnalyticsTarget {
    // Bottom navigation (from any tab).
    const val NAV_HOME = "nav_home"
    const val NAV_ANALYTICS = "nav_analytics"
    const val NAV_HISTORY = "nav_history"
    const val NAV_GOALS = "nav_goals"
    const val NAV_SETTINGS = "nav_settings"

    // Home.
    const val TRACK_SMOKE = "track_smoke"
    const val TRACK_CRAVING = "track_craving"
    const val CRAVING_DISMISS = "craving_dismiss"
    const val CRAVING_I_SMOKED = "craving_i_smoked"
    const val CRAVING_IM_GOOD = "craving_im_good"
    const val CRAVING_LOG_CIGARETTE = "craving_log_cigarette"
    const val CRAVING_HINT_DISMISS = "craving_hint_dismiss"
    const val START_NEW_DAY = "start_new_day"
    const val OPEN_HISTORY = "open_history"
    const val OPEN_GOALS = "open_goals"
    const val RELATIONSHIP_OPEN = "relationship_open"
    const val RELATIONSHIP_SAVE = "relationship_save"
    const val RELATIONSHIP_SKIP = "relationship_skip"
    const val RETRY = "retry"

    // History.
    const val ADD_SMOKE = "add_smoke"
    const val EDIT_SMOKE = "edit_smoke"
    const val DELETE_SMOKE = "delete_smoke"
    const val CONFIRM = "confirm"
    const val CANCEL = "cancel"
    const val PREV = "prev"
    const val NEXT = "next"
    const val PICK_DATE = "pick_date"
    const val SHIFT_MONTH_PREV = "shift_month_prev"
    const val SHIFT_MONTH_NEXT = "shift_month_next"

    // Stats / Analytics.
    const val PERIOD_TAB = "period_tab"

    // Goals.
    const val CONFIGURE_GOAL = "configure_goal"
    const val SELECT_GOAL_TYPE = "select_goal_type"
    const val SAVE_GOAL = "save_goal"
    const val CLEAR_GOAL = "clear_goal"
    const val BACK = "back"

    // Settings.
    const val EDIT_PROFILE = "edit_profile"
    const val SAVE = "save"
    const val TOGGLE_LOCATION = "toggle_location"
    const val CHANGE_LANGUAGE = "change_language"
    const val CHANGE_ACCENT = "change_accent"
    const val CHANGE_DAY_START = "change_day_start"
    const val CHANGE_BEDTIME = "change_bedtime"
    const val CHANGE_CURRENCY = "change_currency"
    const val CHANGE_PACK_PRICE = "change_pack_price"
    const val CHANGE_CIGS_PER_PACK = "change_cigs_per_pack"
    const val OPEN_ABOUT = "open_about"
    const val SHARE_APP = "share_app"
    const val RATE_APP = "rate_app"
    const val REPORT_ISSUE = "report_issue"
    const val CONTACT = "contact"
    const val LOGOUT = "logout"
    const val OPEN_LINK = "open_link"

    // Authentication.
    const val SIGN_IN = "sign_in"
}

/** Common values for [AnalyticsParam.SOURCE]. */
object AnalyticsSource {
    const val HOME = "home"
    const val HISTORY = "history"
    const val WIDGET = "widget"
    const val WEAR = "wear"
    const val QUICK_ADD = "quick_add"
}
