package com.feragusper.smokeanalytics.libraries.cravings.data

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CravingRepositoryImplTest {

    private val firebaseAuth: FirebaseAuth = mockk()
    private val firebaseFirestore: FirebaseFirestore = mockk()
    private val collectionReference = mockk<CollectionReference>()
    private val uid = "uid"

    private val repository = CravingRepositoryImpl(
        firebaseFirestore = firebaseFirestore,
        firebaseAuth = firebaseAuth,
    )

    @BeforeEach
    fun setUp() {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns uid
        every { firebaseAuth.currentUser } returns user
        every { firebaseFirestore.collection("users/$uid/cravings") } returns collectionReference
    }

    @Test
    fun `GIVEN no user WHEN add craving is called THEN it throws`() = runTest {
        every { firebaseAuth.currentUser } returns null

        assertThrows<IllegalStateException> {
            repository.addCraving(createdAt = Instant.fromEpochMilliseconds(0))
        }
    }

    @Test
    fun `GIVEN user is logged in WHEN add craving THEN it writes the payload and returns the new craving`() = runTest {
        val createdAt = Instant.fromEpochMilliseconds(1_000_000)
        val targetAt = Instant.fromEpochMilliseconds(2_000_000)
        val documentRef = mockk<DocumentReference>()
        val payloadSlot = slot<Map<String, Any?>>()

        every { collectionReference.document() } returns documentRef
        every { documentRef.id } returns "c1"
        every { documentRef.set(capture(payloadSlot)) } returns mockk(relaxed = true)

        val result = repository.addCraving(createdAt = createdAt, targetAt = targetAt)

        result.id shouldBeEqualTo "c1"
        result.outcome shouldBeEqualTo CravingOutcome.PENDING
        payloadSlot.captured[CravingEntity.Fields.CREATED_AT_MILLIS] shouldBeEqualTo createdAt.toEpochMilliseconds().toDouble()
        payloadSlot.captured[CravingEntity.Fields.TARGET_AT_MILLIS] shouldBeEqualTo targetAt.toEpochMilliseconds().toDouble()
        payloadSlot.captured[CravingEntity.Fields.OUTCOME] shouldBeEqualTo CravingOutcome.PENDING.name
    }

    @Test
    fun `GIVEN cravings exist WHEN fetch cravings THEN it maps them`() = runTest {
        val query = mockk<Query>()
        every {
            collectionReference.orderBy(CravingEntity.Fields.CREATED_AT_MILLIS, Direction.DESCENDING)
        } returns query
        every { query.get() } returns taskOf(querySnapshotOf(cravingDoc("c1", createdAtMillis = 5_000.0, outcome = "RESISTED", points = 18.0)))

        val result = repository.fetchCravings()

        result.size shouldBeEqualTo 1
        result.first().id shouldBeEqualTo "c1"
        result.first().outcome shouldBeEqualTo CravingOutcome.RESISTED
        result.first().pointsAwarded shouldBeEqualTo 18
    }

    @Test
    fun `GIVEN a pending craving WHEN fetch active craving THEN it returns the most recent one`() = runTest {
        val query = mockk<Query>()
        every {
            collectionReference.whereEqualTo(CravingEntity.Fields.OUTCOME, CravingOutcome.PENDING.name)
        } returns query
        every { query.get() } returns taskOf(
            querySnapshotOf(
                cravingDoc("old", createdAtMillis = 1_000.0, outcome = "PENDING"),
                cravingDoc("new", createdAtMillis = 9_000.0, outcome = "PENDING"),
            )
        )

        val result = repository.fetchActiveCraving()

        result?.id shouldBeEqualTo "new"
    }

    @Test
    fun `GIVEN a craving WHEN resolve craving THEN it updates the outcome fields`() = runTest {
        val documentRef = mockk<DocumentReference>()
        val updateSlot = slot<Map<String, Any?>>()
        every { collectionReference.document("c1") } returns documentRef
        every { documentRef.update(capture(updateSlot)) } returns mockk(relaxed = true)

        repository.resolveCraving(
            id = "c1",
            outcome = CravingOutcome.POSTPONED,
            resolvedAt = Instant.fromEpochMilliseconds(7_000),
            pointsAwarded = 9,
        )

        updateSlot.captured[CravingEntity.Fields.OUTCOME] shouldBeEqualTo CravingOutcome.POSTPONED.name
        updateSlot.captured[CravingEntity.Fields.POINTS_AWARDED] shouldBeEqualTo 9.0
    }

    @Test
    fun `GIVEN a craving WHEN delete craving THEN it deletes the document`() = runTest {
        val documentRef = mockk<DocumentReference>()
        every { collectionReference.document("c1") } returns documentRef
        every { documentRef.delete() } returns mockk(relaxed = true)

        repository.deleteCraving("c1")
    }

    private fun cravingDoc(
        id: String,
        createdAtMillis: Double,
        outcome: String,
        targetAtMillis: Double? = null,
        resolvedAtMillis: Double? = null,
        points: Double = 0.0,
    ): DocumentSnapshot = mockk<DocumentSnapshot>().apply {
        every { this@apply.id } returns id
        every { getDouble(CravingEntity.Fields.CREATED_AT_MILLIS) } returns createdAtMillis
        every { getDouble(CravingEntity.Fields.TARGET_AT_MILLIS) } returns targetAtMillis
        every { getDouble(CravingEntity.Fields.RESOLVED_AT_MILLIS) } returns resolvedAtMillis
        every { getDouble(CravingEntity.Fields.POINTS_AWARDED) } returns points
        every { getString(CravingEntity.Fields.OUTCOME) } returns outcome
    }

    private fun querySnapshotOf(vararg documents: DocumentSnapshot): QuerySnapshot =
        mockk<QuerySnapshot>().apply {
            every { this@apply.documents } returns documents.toList()
        }

    private fun <T> taskOf(value: T): Task<T> =
        mockk<Task<T>>().apply {
            every { isComplete } returns true
            every { isSuccessful } returns true
            every { isCanceled } returns false
            every { exception } returns null
            every { result } returns value
        }
}
