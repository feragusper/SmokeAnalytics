package com.feragusper.smokeanalytics.libraries.design.i18n

/**
 * Every user-facing web string. English is the base; [SpanishStrings] overrides each value.
 * Any override that's missing simply falls back to English, so a partial translation never
 * breaks the UI. Parametrised strings are functions.
 */
open class AppStrings internal constructor() {

    // Nav / scaffold
    open val navHome: String = "Home"
    open val navAnalytics: String = "Analytics & Map"
    open val navHistory: String = "History"
    open val navGoals: String = "Goals"
    open val navYou: String = "You"
    open val brandName: String = "Smoke Analytics"
    open val sidebarSectionWeb: String = "Web"
    open val language: String = "Language"
    open val accent: String = "Accent"
    open val personalization: String = "Personalization"
    open val personalizationSubtitle: String = "Make the app yours: a nickname for the greeting and a reason to keep in view."
    open val nicknameLabel: String = "Nickname"
    open val nicknamePlaceholder: String = "How should we greet you?"
    open val quitReasonLabel: String = "Your reason"
    open val quitReasonPlaceholder: String = "Why are you cutting down?"

    // Document titles
    open val titleHome: String = "Smoke Analytics | Home"
    open val titleAnalytics: String = "Smoke Analytics | Analytics & Map"
    open val titleHistory: String = "Smoke Analytics | History"
    open val titleGoals: String = "Smoke Analytics | Goals"
    open val titleYou: String = "Smoke Analytics | You"
    open val titleSignIn: String = "Smoke Analytics | Sign in"

    // Common actions
    open val cancel: String = "Cancel"
    open val ok: String = "OK"
    open val save: String = "Save"
    open val add: String = "Add"
    open val apply: String = "Apply"
    open val rename: String = "Rename"
    open val remove: String = "Remove"
    open val reset: String = "Reset"
    open val clear: String = "Clear"
    open val tryAgain: String = "Try again"
    open val shareApp: String = "Share app"

    // Home
    open val feelingUrge: String = "Feeling the urge?"
    open val feelingUrgeBody: String =
        "Track the craving before lighting up. If it isn't time yet, we'll help you wait it out and reward the win."
    open val cravingGoodTime: String = "It's already a good time — go ahead when you want."
    open val cravingWaitOver: String =
        "The wait is over. Log the cigarette if you still want it, or let the urge pass for the full reward."
    open val cravingHoldOn: String = "Until your next cigarette fits the goal. You've got this."
    open fun rememberYourReason(reason: String): String = "Remember why: $reason"
    open val vsLastMonth: String = "Vs last month"
    open val startNewDayBody: String =
        "If the day started earlier than usual, reset the reflection window now and keep Home aligned with the day you are actually living."
    open val iFeelLikeSmoking: String = "I feel like smoking"
    open val track: String = "Track"
    open val dismiss: String = "Dismiss"
    open val imGood: String = "I'm good"
    open val logTheCigarette: String = "Log the cigarette"
    open val iSmokedAnyway: String = "I smoked anyway"
    open val nice: String = "Nice"
    open val resisted: String = "Resisted"
    open val postponed: String = "Postponed"
    open val waited: String = "Waited"
    open val points: String = "Points"
    open val startNewDay: String = "Start New Day"
    open val timeSince: String = "Time since"
    open val atLabel: String = "At"
    open val homeTitle: String = "Home"
    open val needsAttention: String = "Needs attention"
    open val sessionRequired: String = "Session required"
    open val couldNotRefreshHome: String = "Could not refresh home"
    open val homeNeedsSession: String =
        "Home needs an active session to line up your goal, last cigarette, and latest gap."
    open val homeGenericError: String = "The goal-first home could not be refreshed. Try again in a moment."
    open val openArchive: String = "Open archive"
    open val retry: String = "Retry"
    open val resetDay: String = "Reset day"
    open val lastCigarette: String = "Last cigarette"
    open val goal: String = "Goal"
    open val consistency: String = "Consistency"
    open val craving: String = "Craving"
    open val cravings: String = "Cravings"
    open val holdOnTitle: String = "Hold on 💪"
    open val youMadeItTitle: String = "You made it! 🎉"
    open val sameCountSoFar: String = "Same count so far"
    open val samePaceLastMonth: String = "Same pace as last month"
    open val smokingLessThanLastMonth: String = "Smoking less than last month"
    open val smokingMoreThanLastMonth: String = "Smoking more than last month"
    open fun cravingPassedPoints(points: Int): String =
        "You let the craving pass without smoking. +$points points earned."
    open fun cravingWaitedPoints(points: Int): String =
        "You waited it out before smoking. +$points points earned."

    // Relationship prompt / reminder
    open val whatWereTheseAbout: String = "What were these about?"
    open val whatWasItRelatedTo: String = "What was it related to?"
    open val tagWhatTriggered: String =
        "Tag what triggered this cigarette, or skip if it was nothing in particular."
    open val loadingYourTags: String = "Loading your tags…"
    open val addTrigger: String = "Add trigger"
    open val noRelation: String = "No relation"
    open val addATagPlaceholder: String = "Add a tag…"
    open fun loggedAt(dateLabel: String): String = "Logged $dateLabel"
    open fun moreCount(n: Int): String = "+$n more"
    open fun pendingNeedsTrigger(n: Int): String =
        if (n == 1) "1 cigarette still needs a trigger — tag it below."
        else "$n cigarettes still need a trigger — tag them one at a time."

    // Edit smoke
    open val editSmoke: String = "Edit smoke"
    open val date: String = "Date"
    open val time: String = "Time"

    // Goals
    open val noActiveGoalYet: String = "No active goal yet"
    open val noActiveGoalBody: String = "Set one target to keep your progress front and center on Home."
    open val activeGoal: String = "Active goal"
    open val goalsNeedAccount: String = "Goals need an account"
    open val goalsNeedAccountBody: String = "Sign in to save one active goal and sync it across platforms."
    open val goalSetup: String = "Goal setup"
    open val goalsTitle: String = "Goals"
    open val backToProgress: String = "← Back to progress"
    open val setAGoal: String = "Set a goal"
    open val goalsUnavailable: String = "Goals are unavailable"
    open val configureGoal: String = "Configure goal"
    open val chooseOneTarget: String = "Choose one active target and keep its progress visible from Home."
    open val completed: String = "Completed"
    open val noGoalYet: String = "No goal yet"
    open val offTrack: String = "Off track"
    open val onTrack: String = "On track"
    open val trackHowGoing: String = "Track how your active goal is going."
    open val yourGoal: String = "Your goal"
    // Goal editor
    open val dailyCap: String = "Daily cap"
    open val mindfulGap: String = "Mindful gap"
    open val reduceVsPrevMonth: String = "Reduce vs previous month"
    open val reduceVsPrevWeek: String = "Reduce vs previous week"
    open val saveGoal: String = "Save goal"
    open val updateGoal: String = "Update goal"
    open val usePositiveMinutes: String = "Use a positive number of minutes."
    open val usePositiveWhole: String = "Use a positive whole number."
    open val useValue1to90: String = "Use a value between 1 and 90."
    open fun dailyCapDesc(max: Int): String = "Daily cap: at most $max cigarettes."
    open fun reduceMonthDesc(pct: Int): String = "Reduce the current month by $pct%."
    open fun reduceWeekDesc(pct: Int): String = "Reduce the current week by $pct%."
    open fun waitAtLeastDesc(min: Int): String = "Wait at least $min minutes between cigarettes."
    // History
    open val archiveNeedsSession: String =
        "Archive access needs an active session so edits, dates, and older smoke entries stay tied to the same account."
    open val goToSignIn: String = "Go to sign in"
    open val historyCouldNotLoad: String = "History could not be loaded"
    open val signInRequired: String = "Sign in required"
    open val dayHistoryCouldNotLoad: String = "The selected day's history could not be loaded. Try refreshing the day."
    open fun afterHm(h: Int, m: Int): String = "After ${h}h ${m}m"
    open fun afterM(m: Int): String = "After ${m}m"
    // Auth extras
    open val checkingSession: String = "Checking session"
    open val secureSignIn: String = "Secure sign-in"
    open val keepEditsBody: String = "Keep edits, date buckets, and older entries tied to the same account."
    open val keepReductionBody: String = "Keep reduction targets and product preferences tied to the same account."
    open val restorePreferencesBody: String = "Restore preferences like day-start hour, pack price, and location tracking."

    // History
    open val historyRefreshFailed: String = "Latest refresh failed. Showing the last available archive state."
    open val shiftMonthHint: String = "Shift the month or tap a day to inspect it in the list below."
    open val dailyArchive: String = "Daily archive"
    open val theArchive: String = "The Archive"
    open val theArchiveSubtitle: String =
        "Browse the calendar, inspect a single day, and edit the full smoking log without leaving the main shell."
    open val quietDay: String = "Quiet day in the archive"
    open val quietDayBody: String =
        "This date has no smoke entries yet. Shift the archive window or add one for the selected day."
    open val addSmoke: String = "Add smoke"
    open val addForDate: String = "Add for Date"
    open fun entriesCount(n: Int): String = "$n entries"
    open fun dailyAverageUnits(avg: String): String = "Daily average $avg units"

    // Auth
    open val restoreShell: String = "Restore the product shell"
    open val restoreShellBody: String =
        "Sign in with Google to recover synced history, settings, and the full multi-device product flow."
    open val beforeYouContinue: String = "Before you continue"
    open val beforeYouContinueBody: String =
        "The redesign keeps the same product capabilities: fast smoke logging, analytics and map insights, archive editing, goals, and settings sync."
    open val signInToContinue: String = "Sign in to continue"
    open val authFailed: String = "Authentication failed"
    open val authFailedBody: String = "The session could not be restored. Try signing in again."
    open val retrySessionCheck: String = "Retry session check"
    open val refreshSession: String = "Refresh session"
    open val clearSession: String = "Clear session"
    open val archiveSync: String = "Archive sync"
    open val routine: String = "Routine"
    open val stableDayModel: String = "Stable day model"
    open val contextAware: String = "Context-aware"

    // Stats
    open val statsRefreshFailed: String = "Latest refresh failed. Showing the last available analytics snapshot."
    open val peakWindow: String = "Peak Window"
    open val peakWindowBody: String = "Highest activity bucket for the selected range."
    open val smokingFrequency: String = "Smoking Frequency"
    open val byTrigger: String = "By trigger"
    open val byTriggerEmpty: String =
        "No tagged cigarettes in this period yet. Tag what each one was related to and the breakdown shows up here."
    open val eyebrowTrends: String = "Trends"
    open val loading: String = "Loading"
    open val errorBadge: String = "Error"
    open val refreshingFrequency: String = "Refreshing frequency in background."
    open val freqRefreshFailed: String = "Latest frequency refresh failed. Showing the last snapshot."
    open val patternsInMotion: String = "Patterns in motion"
    open val patternViewUnavailable: String = "Pattern view unavailable"
    open val patternViewUnavailableBody: String =
        "The selected range could not be assembled right now. Keep the period and date, then refresh to try this view again."
    open val periodToday: String = "Today"
    open val periodWeek: String = "Week"
    open val periodMonth: String = "Month"
    open val periodYear: String = "Year"
    open val chartTitleDay: String = "Today (hourly)"
    open val chartTitleWeek: String = "This week"
    open val chartTitleMonth: String = "This month"
    open val chartTitleYear: String = "This year"

    // Map
    open val refreshingClusters: String = "Refreshing clusters in background."
    open val mapRefreshFailed: String = "Latest refresh failed. Showing the last available clusters."
    open val mostFrequentArea: String = "Most frequent area for the selected period"
    open val topClusters: String = "Top Clusters"
    open val pickAreaToInspect: String = "Pick an area to inspect on Google Maps."
    open val observation: String = "Observation"
    open val observationBody: String =
        "Repeated clusters usually point to routines worth protecting or interrupting, especially around commute, breaks, or end-of-day transitions."
    open val geographicClusters: String = "Geographic Clusters"
    open val geographicClustersSubtitle: String =
        "Inspect repeated smoking areas and the places that dominate the current map period."
    open val locationOff: String = "Location tracking is off"
    open val locationOffBody: String =
        "Enable location tracking in You to unlock map insights, repeated-area detection, and the geographic side of Analytics."
    open val noMappedSmokes: String = "No mapped smokes yet"
    open val noMappedSmokesBody: String =
        "There is not enough location-linked history for this period yet. Add more smoke entries with location tracking enabled to build clusters."
    open val refreshing: String = "Refreshing"
    open val view: String = "View"
    open val viewing: String = "Viewing"
    open fun smokesCount(n: Int): String = "$n smokes"
    open fun mappedSmokesInPeriod(count: Int, period: String): String =
        "$count mapped smokes in the selected $period."
    open fun smokesInArea(n: Int): String = "$n smokes in this area"
    open fun clusterSummary(count: Int, radius: Int): String =
        "$count smokes grouped in an approximate $radius m area."
    open val analyticsAndMap: String = "Analytics & Map"
    open val analyticsAndMapSubtitle: String =
        "Review smoking frequency and repeated smoking areas from one destination."
    open val eyebrowPatterns: String = "Patterns"
    open val eyebrowLocations: String = "Locations"
    open val eyebrowPersonalSpace: String = "Personal space"
    open val tabFrequency: String = "Frequency"
    open val tabClusters: String = "Clusters"
    open val periodDay: String = "Day"
    open val youSubtitle: String =
        "Keep account, routine preferences, goals, and product details in one calmer destination."
    open fun weekOf(date: String): String = "Week of $date"
    open val selectedDay: String = "Selected day"
    open val monthOverview: String = "Month overview"
    open val yearToDate: String = "Year to date"

    // Settings / You
    open val account: String = "Account"
    open val accountSubtitle: String =
        "Session state and core product context stay together here instead of splitting You into old Settings/About leftovers."
    open val session: String = "Session"
    open val sessionGuestBody: String =
        "Guest mode keeps the shell readable, but sign-in restores synced preferences, a stable archive, and goals across devices."
    open val plan: String = "Plan"
    open val pointsCard: String = "Points"
    open val recovery: String = "Recovery"
    open val planPremiumBody: String = "Premium stays framed as a future upgrade with richer insights and no ads."
    open val pointsBody: String =
        "Progress is tied to smoke-free gaps, not perfection. Longer gaps keep the score moving."
    open val yourSpaceUnavailable: String = "Your space is unavailable"
    open val preferences: String = "Preferences"
    open val preferencesSubtitle: String =
        "Routine and cost settings shape how the rest of the product interprets your day."
    open val preferencesSignInHint: String =
        "Sign in to edit routine preferences and keep them synced across mobile and web."
    open val triggers: String = "Triggers"
    open val triggersSubtitle: String =
        "Choose which built-in triggers appear when tagging a cigarette, and add your own."
    open val appSection: String = "App"
    open val appSectionSubtitle: String =
        "Support, sharing, and product metadata stay visible inside You instead of a detached About route."
    open val builtIn: String = "Built-in"
    open val yourTags: String = "Your tags"
    open val leaveEmptyToRestore: String = "Leave empty to restore the original name"
    open fun restoreRemovedDefaults(n: Int): String = "Restore removed defaults ($n)"
    open val dayModel: String = "Day model"
    open val firstHourOfDay: String = "First hour of the day"
    open val sleepStarts: String = "Sleep starts"
    open val bedtime: String = "Bedtime"
    open val location: String = "Location"
    open val trackLocationWithSmokes: String = "Track location with smokes"
    open val currency: String = "Currency"
    open val packPrice: String = "Pack price"
    open val cigarettesPerPack: String = "Cigarettes per pack"
    open val actions: String = "Actions"
    open val actionsSubtitle: String = "Share the app, report bugs, and reach support from the same personal destination."
    open val signOut: String = "Sign out"
    open val working: String = "Working..."
    open val off: String = "Off"
    open val pickTriggerIcon: String = "Pick trigger icon"
    open val searchEmoji: String = "Search emoji…"
    open val changesSavedAutomatically: String = "Changes are saved automatically."
    open val currencyOptional: String = "Optional. Used for map insights."
    open val dayModelBody: String = "Wake-up hour used as the main bucket boundary across Home, History, and Analytics."
    open val firstHourBody: String = "Sleep hours are excluded from the mindful gap target and the daily hourly average."
    open val locationOnBody: String = "Location tracking is enabled, so map insights can learn from repeated areas."
    open val locationOffShortBody: String = "Location tracking is off, so map insights stay unavailable until the setting changes."
    open val packPriceBody: String = "The cost base for spend estimates and progress summaries."
    open val cigarettesPerPackBody: String = "Keeps cost metrics and pack-level estimates aligned."
    open val currencyBody: String = "Used for pack-price and cost calculations across the product."
    open val bedtimeBody: String = "Sleep hours are excluded from the mindful gap target and the daily hourly average."
    open val historyCardBody: String = "Keep edits and older smoke entries connected to the same account."
    open val goalsCardBody: String = "Keep reduction targets and product preferences connected to the same account."
    open val preferencesCardBody: String = "Carry pack price, day-start hour, and location settings across devices."
    open val webCardBody: String = "The browser surface stays aligned with the same product direction as mobile."
    open val routineSync: String = "Routine sync"
    open val stableArchive: String = "Stable archive"
    open val stableTargets: String = "Stable targets"
    open val version: String = "Version"

    // About
    open val about: String = "About"
    open val aboutTagline: String = "Track less, notice more."
    open val aboutBody: String =
        "Smoke Analytics is a personal smoking journal built to make patterns visible without turning the app into noise. It helps you review the day, follow longer trends, understand cost, and notice where or when smoking tends to cluster across mobile and web."
    open val contact: String = "Contact"
    open val contactBody: String =
        "Built by Fernando Perez. If something feels off, the best path today is GitHub: check the repo, open a bug report, or start a discussion."
    open val github: String = "GitHub"
    open val reportBug: String = "Report bug"
    open val contactUs: String = "Contact us"
    open val free: String = "Free"
    open val planAboutBody: String = "Premium is defined as a future upgrade with richer insights and no ads."
    open val copyright: String = "Copyright"
    open val copyrightBody: String = "© Fernando Perez. All rights reserved."
    open val shareAppArrow: String = "↗ Share app"

    companion object {
        fun forLanguage(language: AppLanguage): AppStrings = when (language) {
            AppLanguage.EN -> EnglishStrings
            AppLanguage.ES -> SpanishStrings
        }
    }
}

/** Base English catalog. */
object EnglishStrings : AppStrings()
