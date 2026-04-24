package com.feragusper.smokeanalytics.libraries.preferences.data

import android.content.Context
import com.feragusper.smokeanalytics.libraries.preferences.domain.AccountTier
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserPreferencesRepositoryImplTest {

    private val firestore: FirebaseFirestore = mockk()
    private val auth: FirebaseAuth = mockk()
    private val appContext: Context = mockk(relaxed = true)
    private val usersCollection: CollectionReference = mockk()
    private val userDocument: DocumentReference = mockk()
    private val profileCollection: CollectionReference = mockk()
    private val preferencesDocument: DocumentReference = mockk()

    private val repository = UserPreferencesRepositoryImpl(
        firestore = firestore,
        auth = auth,
        appContext = appContext,
    )

    @BeforeEach
    fun setUp() {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns UID
        every { auth.currentUser } returns user

        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document(UID) } returns userDocument
        every { userDocument.collection("profile") } returns profileCollection
        every { profileCollection.document(UserPreferencesEntity.DOCUMENT) } returns preferencesDocument
        every { preferencesDocument.path } returns "users/$UID/profile/${UserPreferencesEntity.DOCUMENT}"
    }

    @Test
    fun `GIVEN preferences WHEN update is called THEN it writes explicit Firestore field names`() = runTest {
        val preferences = UserPreferences(
            packPrice = 12.5,
            cigarettesPerPack = 25,
            dayStartHour = 7,
            bedtimeHour = 23,
            manualDayStartEpochMillis = 1_714_000_000_000,
            locationTrackingEnabled = true,
            currencySymbol = "$",
            accountTier = AccountTier.Free,
            activeGoal = SmokingGoal.MindfulGap(targetMinutes = 120),
        )
        val payloadSlot = slot<Map<String, Any?>>()

        every { preferencesDocument.set(capture(payloadSlot)) } returns voidTask()
        every { preferencesDocument.get(Source.SERVER) } returns taskOf(preferencesSnapshot(preferences))

        repository.update(preferences)

        val payload = payloadSlot.captured
        assertEquals(preferences.packPrice, payload[UserPreferencesEntity.PACK_PRICE])
        assertEquals(preferences.cigarettesPerPack.toLong(), payload[UserPreferencesEntity.CIGARETTES_PER_PACK])
        assertEquals(preferences.dayStartHour.toLong(), payload[UserPreferencesEntity.DAY_START_HOUR])
        assertEquals(preferences.bedtimeHour.toLong(), payload[UserPreferencesEntity.BEDTIME_HOUR])
        assertEquals(preferences.manualDayStartEpochMillis, payload[UserPreferencesEntity.MANUAL_DAY_START_EPOCH_MILLIS])
        assertEquals(preferences.locationTrackingEnabled, payload[UserPreferencesEntity.LOCATION_TRACKING_ENABLED])
        assertEquals(preferences.currencySymbol, payload[UserPreferencesEntity.CURRENCY_SYMBOL])
        assertEquals(preferences.accountTier.name, payload[UserPreferencesEntity.ACCOUNT_TIER])
        assertEquals(preferences.activeGoal?.type?.name, payload[UserPreferencesEntity.ACTIVE_GOAL_TYPE])
        assertEquals(preferences.activeGoal?.metricValue, payload[UserPreferencesEntity.ACTIVE_GOAL_METRIC_VALUE])
    }

    @Test
    fun `GIVEN server verification misses active goal WHEN update completes THEN it throws`() = runTest {
        val preferences = UserPreferences(activeGoal = SmokingGoal.DailyCap(maxCigarettesPerDay = 8))

        every { preferencesDocument.set(any<Map<String, Any?>>()) } returns voidTask()
        every { preferencesDocument.get(Source.SERVER) } returns taskOf(
            preferencesSnapshot(preferences, activeGoalType = null)
        )

        val error = assertThrows<IllegalStateException> {
            repository.update(preferences)
        }

        assertTrue(error.message.orEmpty().contains(UserPreferencesEntity.ACTIVE_GOAL_TYPE))
    }

    @Test
    fun `GIVEN Play Store release wrote obfuscated preferences fields WHEN fetch is called THEN it restores them`() =
        runTest {
            val preferences = UserPreferences(
                packPrice = 11.0,
                cigarettesPerPack = 24,
                dayStartHour = 8,
                bedtimeHour = 1,
                manualDayStartEpochMillis = 1_714_000_000_000,
                locationTrackingEnabled = true,
                currencySymbol = "$",
                accountTier = AccountTier.Free,
                activeGoal = SmokingGoal.ReductionVsPreviousWeek(reductionPercent = 20.0),
            )

            every { preferencesDocument.get(Source.SERVER) } returns taskOf(
                preferencesSnapshot(
                    preferences = preferences,
                    canonicalFields = false,
                    legacyFields = true,
                )
            )

            val result = repository.fetch()

            assertEquals(preferences, result)
        }

    private fun preferencesSnapshot(
        preferences: UserPreferences,
        activeGoalType: String? = preferences.activeGoal?.type?.name,
        canonicalFields: Boolean = true,
        legacyFields: Boolean = false,
    ): DocumentSnapshot = mockk<DocumentSnapshot>().apply {
        every { exists() } returns true
        val doubleFields = buildMap {
            if (canonicalFields) {
                put(UserPreferencesEntity.PACK_PRICE, preferences.packPrice)
                put(UserPreferencesEntity.ACTIVE_GOAL_METRIC_VALUE, preferences.activeGoal?.metricValue)
            }
            if (legacyFields) {
                put("a", preferences.packPrice)
                put("j", preferences.activeGoal?.metricValue)
            }
        }
        val longFields = buildMap {
            if (canonicalFields) {
                put(UserPreferencesEntity.CIGARETTES_PER_PACK, preferences.cigarettesPerPack.toLong())
                put(UserPreferencesEntity.DAY_START_HOUR, preferences.dayStartHour.toLong())
                put(UserPreferencesEntity.BEDTIME_HOUR, preferences.bedtimeHour.toLong())
                put(UserPreferencesEntity.MANUAL_DAY_START_EPOCH_MILLIS, preferences.manualDayStartEpochMillis)
            }
            if (legacyFields) {
                put("b", preferences.cigarettesPerPack.toLong())
                put("c", preferences.dayStartHour.toLong())
                put("d", preferences.bedtimeHour.toLong())
                put("e", preferences.manualDayStartEpochMillis)
            }
        }
        val booleanFields = buildMap {
            if (canonicalFields) put(UserPreferencesEntity.LOCATION_TRACKING_ENABLED, preferences.locationTrackingEnabled)
            if (legacyFields) put("f", preferences.locationTrackingEnabled)
        }
        val stringFields = buildMap {
            if (canonicalFields) {
                put(UserPreferencesEntity.CURRENCY_SYMBOL, preferences.currencySymbol)
                put(UserPreferencesEntity.ACCOUNT_TIER, preferences.accountTier.name)
                put(UserPreferencesEntity.ACTIVE_GOAL_TYPE, activeGoalType)
            }
            if (legacyFields) {
                put("g", preferences.currencySymbol)
                put("h", preferences.accountTier.name)
                put("i", activeGoalType)
            }
        }
        every { getDouble(any<String>()) } answers { doubleFields[firstArg()] }
        every { getLong(any<String>()) } answers { longFields[firstArg()] }
        every { getBoolean(any<String>()) } answers { booleanFields[firstArg()] }
        every { getString(any<String>()) } answers { stringFields[firstArg()] }
    }

    private fun voidTask(): Task<Void> =
        mockk<Task<Void>>().apply {
            every { isComplete } returns true
            every { isSuccessful } returns true
            every { isCanceled } returns false
            every { exception } returns null
            every { result } returns null
        }

    private fun <T> taskOf(value: T): Task<T> =
        mockk<Task<T>>().apply {
            every { isComplete } returns true
            every { isSuccessful } returns true
            every { isCanceled } returns false
            every { exception } returns null
            every { result } returns value
        }

    private companion object {
        const val UID = "uid"
    }
}
