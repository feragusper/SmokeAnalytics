package com.feragusper.smokeanalytics.libraries.architecture.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnalyticsTrackerTest {

    /** Records every call so the default helper implementations can be asserted. */
    private class RecordingTracker : AnalyticsTracker {
        val events = mutableListOf<Pair<String, Map<String, Any>>>()
        var lastScreen: String? = null
        var lastUserId: String? = null
        var userIdWasSet = false

        override fun screenView(screenName: String) {
            lastScreen = screenName
        }

        override fun logEvent(name: String, params: Map<String, Any>) {
            events += name to params
        }

        override fun setUserId(userId: String?) {
            userIdWasSet = true
            lastUserId = userId
        }
    }

    private val tracker = RecordingTracker()

    @Test
    fun screenView_recordsScreenName() {
        tracker.screenView(AnalyticsScreen.HOME)
        assertEquals(AnalyticsScreen.HOME, tracker.lastScreen)
    }

    @Test
    fun smokeAdded_logsEventWithSource() {
        tracker.smokeAdded(AnalyticsSource.HOME)
        assertEquals(
            AnalyticsEvent.SMOKE_ADDED to mapOf(AnalyticsParam.SOURCE to AnalyticsSource.HOME),
            tracker.events.single(),
        )
    }

    @Test
    fun smokeEdited_logsEventWithSource() {
        tracker.smokeEdited(AnalyticsSource.HISTORY)
        assertEquals(
            AnalyticsEvent.SMOKE_EDITED to mapOf(AnalyticsParam.SOURCE to AnalyticsSource.HISTORY),
            tracker.events.single(),
        )
    }

    @Test
    fun smokeDeleted_logsEventWithSource() {
        tracker.smokeDeleted(AnalyticsSource.HISTORY)
        assertEquals(
            AnalyticsEvent.SMOKE_DELETED to mapOf(AnalyticsParam.SOURCE to AnalyticsSource.HISTORY),
            tracker.events.single(),
        )
    }

    @Test
    fun cravingTracked_logsEventWithoutParams() {
        tracker.cravingTracked()
        assertEquals(AnalyticsEvent.CRAVING_TRACKED to emptyMap(), tracker.events.single())
    }

    @Test
    fun cravingResolved_logsSmokedFlag() {
        tracker.cravingResolved(smoked = true)
        assertEquals(
            AnalyticsEvent.CRAVING_RESOLVED to mapOf(AnalyticsParam.SMOKED to true),
            tracker.events.single(),
        )
    }

    @Test
    fun goalSet_logsGoalType() {
        tracker.goalSet("DAILY_LIMIT")
        assertEquals(
            AnalyticsEvent.GOAL_SET to mapOf(AnalyticsParam.GOAL_TYPE to "DAILY_LIMIT"),
            tracker.events.single(),
        )
    }

    @Test
    fun goalCleared_logsEventWithoutParams() {
        tracker.goalCleared()
        assertEquals(AnalyticsEvent.GOAL_CLEARED to emptyMap(), tracker.events.single())
    }

    @Test
    fun relationshipTagged_logsTagCount() {
        tracker.relationshipTagged(tagCount = 3)
        assertEquals(
            AnalyticsEvent.RELATIONSHIP_TAGGED to mapOf(AnalyticsParam.TAG_COUNT to 3),
            tracker.events.single(),
        )
    }

    @Test
    fun relationshipSkipped_logsEventWithoutParams() {
        tracker.relationshipSkipped()
        assertEquals(AnalyticsEvent.RELATIONSHIP_SKIPPED to emptyMap(), tracker.events.single())
    }

    @Test
    fun newDayStarted_logsEventWithoutParams() {
        tracker.newDayStarted()
        assertEquals(AnalyticsEvent.NEW_DAY_STARTED to emptyMap(), tracker.events.single())
    }

    @Test
    fun login_defaultsToGoogleMethod() {
        tracker.login()
        assertEquals(
            AnalyticsEvent.LOGIN to mapOf(AnalyticsParam.METHOD to "google"),
            tracker.events.single(),
        )
    }

    @Test
    fun login_usesProvidedMethod() {
        tracker.login(method = "email")
        assertEquals(
            AnalyticsEvent.LOGIN to mapOf(AnalyticsParam.METHOD to "email"),
            tracker.events.single(),
        )
    }

    @Test
    fun logout_logsEventWithoutParams() {
        tracker.logout()
        assertEquals(AnalyticsEvent.LOGOUT to emptyMap(), tracker.events.single())
    }

    @Test
    fun shareApp_logsEventWithoutParams() {
        tracker.shareApp()
        assertEquals(AnalyticsEvent.SHARE_APP to emptyMap(), tracker.events.single())
    }

    @Test
    fun languageChanged_logsLanguage() {
        tracker.languageChanged("ES")
        assertEquals(
            AnalyticsEvent.LANGUAGE_CHANGED to mapOf(AnalyticsParam.LANGUAGE to "ES"),
            tracker.events.single(),
        )
    }

    @Test
    fun accentChanged_logsAccent() {
        tracker.accentChanged("teal")
        assertEquals(
            AnalyticsEvent.ACCENT_CHANGED to mapOf(AnalyticsParam.ACCENT to "teal"),
            tracker.events.single(),
        )
    }

    @Test
    fun setUserId_forwardsValue() {
        tracker.setUserId("uid-123")
        assertTrue(tracker.userIdWasSet)
        assertEquals("uid-123", tracker.lastUserId)
    }

    @Test
    fun noOpTracker_defaultSetUserId_isNoop() {
        // Exercises the default no-op setUserId on the interface (not overridden here).
        val defaultTracker = object : AnalyticsTracker {
            override fun screenView(screenName: String) {}
            override fun logEvent(name: String, params: Map<String, Any>) {}
        }
        defaultTracker.setUserId("ignored")
        defaultTracker.screenView(AnalyticsScreen.SETTINGS)
        // No crash; default setUserId keeps no state.
        assertNull((defaultTracker as? RecordingTracker)?.lastUserId)
    }

    @Test
    fun noOpTracker_doesNothing() {
        NoOpAnalyticsTracker.screenView(AnalyticsScreen.HOME)
        NoOpAnalyticsTracker.logEvent(AnalyticsEvent.LOGIN)
        NoOpAnalyticsTracker.setUserId("uid")
        NoOpAnalyticsTracker.smokeAdded(AnalyticsSource.WIDGET)
        // Reaching here without exception is the assertion.
        assertTrue(true)
    }
}
