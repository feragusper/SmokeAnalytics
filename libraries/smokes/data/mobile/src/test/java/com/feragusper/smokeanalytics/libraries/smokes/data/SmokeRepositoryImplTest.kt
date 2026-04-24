package com.feragusper.smokeanalytics.libraries.smokes.data

import android.content.Context
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeAfter
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.SMOKES
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.USERS
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SmokeRepositoryImplTest {

    private val firebaseAuth: FirebaseAuth = mockk()
    private val firebaseFirestore: FirebaseFirestore = mockk()
    private val appContext: Context = mockk(relaxed = true)
    private val smokeRepository = SmokeRepositoryImpl(
        firebaseAuth = firebaseAuth,
        firebaseFirestore = firebaseFirestore,
        appContext = appContext,
    )

    @Test
    fun `GIVEN the user is null WHEN add smoke is called THEN it should throw an illegal state exception`() =
        runTest {
            every { firebaseAuth.currentUser } returns null
            assertThrows<IllegalStateException> {
                smokeRepository.addSmoke(
                    Instant.fromEpochMilliseconds(
                        0
                    )
                )
            }
        }

    @Nested
    inner class UserLoggedIn {

        private val id1 = "id1"
        private val id2 = "id2"

        private val instant1 =
            Instant.fromEpochMilliseconds(1_672_574_400_000) // 2023-01-01T12:00:00Z-ish depending TZ, but ok for test
        private val instant2 = Instant.fromEpochMilliseconds(1_672_567_200_000) // earlier

        private val timeAfterNothing = 0L to 0L
        private val timeAfter2 = instant1.timeAfter(instant2)

        private val uid = "uid"

        private val collectionReference = mockk<CollectionReference>()

        @BeforeEach
        fun setUp() {
            val mockUser = mockk<FirebaseUser>()
            every { mockUser.uid } returns uid
            every { firebaseAuth.currentUser } returns mockUser

            every { firebaseFirestore.collection("$USERS/$uid/$SMOKES") } returns collectionReference

            every {
                collectionReference.orderBy(
                    SmokeEntity.Fields.TIMESTAMP_MILLIS,
                    Query.Direction.DESCENDING
                )
            } returns collectionReference
        }

        @Test
        fun `GIVEN user is logged in WHEN fetch smokes is called THEN it should return a list of smokes`() =
            runTest {
                mockFetchSmokes()

                val result = smokeRepository.fetchSmokes(instant1, instant2)

                assertEquals(
                    listOf(
                        Smoke(
                            id = id1,
                            date = instant1,
                            timeElapsedSincePreviousSmoke = timeAfter2
                        ),
                        Smoke(
                            id = id2,
                            date = instant2,
                            timeElapsedSincePreviousSmoke = timeAfterNothing
                        )
                    ),
                    result
                )
            }

        @Test
        fun `GIVEN fetch starts after an older smoke WHEN fetch smokes is called THEN first row keeps the real previous gap`() =
            runTest {
                mockFetchSmokes(
                    resultDocuments = listOf(mockDocumentSnapshot(id1, instant1)),
                    previousDocument = mockDocumentSnapshot(id2, instant2),
                )

                val result = smokeRepository.fetchSmokes(instant1, Instant.fromEpochMilliseconds(1_672_580_000_000))

                assertEquals(
                    listOf(
                        Smoke(
                            id = id1,
                            date = instant1,
                            timeElapsedSincePreviousSmoke = timeAfter2
                        )
                    ),
                    result
                )
            }

        @Test
        fun `GIVEN Play Store release wrote obfuscated smoke fields WHEN fetch smokes is called THEN it restores them`() =
            runTest {
                mockFetchSmokes(
                    resultDocuments = emptyList(),
                    legacyResultDocuments = listOf(
                        mockDocumentSnapshot(
                            id = id1,
                            date = instant1,
                            timestampMillis = null,
                            legacyTimestampMillis = instant1.toEpochMilliseconds().toDouble(),
                        )
                    ),
                )

                val result = smokeRepository.fetchSmokes(instant1, Instant.fromEpochMilliseconds(1_672_580_000_000))

                assertEquals(
                    listOf(
                        Smoke(
                            id = id1,
                            date = instant1,
                            timeElapsedSincePreviousSmoke = timeAfterNothing,
                        )
                    ),
                    result,
                )
            }

        @Test
        fun `GIVEN user is logged in WHEN add smoke is called THEN it should finish`() = runTest {
            val date = Instant.fromEpochMilliseconds(1_672_574_400_000)
            val smokePayloadSlot = slot<Map<String, Any?>>()
            val documentRef = mockk<DocumentReference>()

            every { collectionReference.document() } returns documentRef
            every { documentRef.path } returns "$USERS/$uid/$SMOKES/generated"
            every { documentRef.set(capture(smokePayloadSlot)) } answers {
                mockk<Task<Void>>().apply {
                    every { isComplete } returns true
                    every { isSuccessful } returns true
                    every { result } returns null
                    every { exception } returns null
                    every { isCanceled } returns false
                }
            }
            every { documentRef.get(Source.SERVER) } answers {
                taskOf(mockDocumentSnapshot("generated", date))
            }

            smokeRepository.addSmoke(date)

            assertTrue(smokePayloadSlot.isCaptured)
            assertEquals(
                date.toEpochMilliseconds().toDouble(),
                smokePayloadSlot.captured[SmokeEntity.Fields.TIMESTAMP_MILLIS]
            )
            assertTrue(smokePayloadSlot.captured.containsKey(SmokeEntity.Fields.LATITUDE))
            assertTrue(smokePayloadSlot.captured.containsKey(SmokeEntity.Fields.LONGITUDE))
        }

        @Test
        fun `GIVEN user is logged in WHEN edit smoke is called THEN it should finish`() = runTest {
            val id = "id"
            val date = instant1

            val documentRef = mockk<DocumentReference>()
            every { collectionReference.document(id) } returns documentRef
            every { documentRef.path } returns "$USERS/$uid/$SMOKES/$id"
            every { documentRef.get(Source.SERVER) } answers { taskOf(mockDocumentSnapshot(id, date)) }

            every { documentRef.set(any<Map<String, Any?>>()) } answers {
                mockk<Task<Void>>().apply {
                    every { isComplete } returns true
                    every { isSuccessful } returns true
                    every { exception } returns null
                    every { isCanceled } returns false
                    every { result } returns mockk()
                }
            }

            smokeRepository.editSmoke(id, date)
        }

        @Test
        fun `GIVEN server write omits timestamp WHEN add smoke verifies server state THEN it should throw`() =
            runTest {
                val date = Instant.fromEpochMilliseconds(1_672_574_400_000)
                val documentRef = mockk<DocumentReference>()

                every { collectionReference.document() } returns documentRef
                every { documentRef.path } returns "$USERS/$uid/$SMOKES/generated"
                every { documentRef.set(any<Map<String, Any?>>()) } answers {
                    mockk<Task<Void>>().apply {
                        every { isComplete } returns true
                        every { isSuccessful } returns true
                        every { result } returns null
                        every { exception } returns null
                        every { isCanceled } returns false
                    }
                }
                every { documentRef.get(Source.SERVER) } answers {
                    taskOf(mockDocumentSnapshot("generated", date, timestampMillis = null))
                }

                val error = assertThrows<IllegalStateException> {
                    smokeRepository.addSmoke(date)
                }

                assertTrue(error.message.orEmpty().contains("timestampMillis"))
            }

        @Test
        fun `GIVEN user is logged in WHEN delete smoke is called THEN it should finish`() =
            runTest {
                val id = "id"

                val documentRef = mockk<DocumentReference>()
                every { collectionReference.document(id) } returns documentRef
                every { documentRef.path } returns "$USERS/$uid/$SMOKES/$id"

                every { documentRef.delete() } answers {
                    mockk<Task<Void>>().apply {
                        every { isComplete } returns true
                        every { isSuccessful } returns true
                        every { exception } returns null
                        every { isCanceled } returns false
                        every { result } returns mockk()
                    }
                }
                every { documentRef.get(Source.SERVER) } answers {
                    taskOf(mockDocumentSnapshot(id, instant1, exists = false))
                }

                smokeRepository.deleteSmoke(id)
            }

        private fun mockFetchSmokes(
            resultDocuments: List<DocumentSnapshot> = listOf(
                mockDocumentSnapshot(id1, instant1),
                mockDocumentSnapshot(id2, instant2),
            ),
            previousDocument: DocumentSnapshot? = null,
            legacyResultDocuments: List<DocumentSnapshot> = emptyList(),
            legacyPreviousDocument: DocumentSnapshot? = null,
        ) {
            mockSmokeQuery(
                field = SmokeEntity.Fields.TIMESTAMP_MILLIS,
                resultDocuments = resultDocuments,
                previousDocument = previousDocument,
            )
            mockSmokeQuery(
                field = "a",
                resultDocuments = legacyResultDocuments,
                previousDocument = legacyPreviousDocument,
            )
        }

        private fun mockSmokeQuery(
            field: String,
            resultDocuments: List<DocumentSnapshot>,
            previousDocument: DocumentSnapshot?,
        ) {
            val query = mockk<Query>(relaxed = true)
            val finalQuery = mockk<Query>(relaxed = true)
            val previousQuery = mockk<Query>(relaxed = true)
            val previousLimitedQuery = mockk<Query>(relaxed = true)

            every {
                collectionReference.orderBy(
                    field,
                    Query.Direction.DESCENDING
                )
            } returns query

            every {
                query.whereGreaterThanOrEqualTo(field, any())
            } returns finalQuery

            every {
                finalQuery.whereLessThan(field, any())
            } returns finalQuery
            every {
                query.whereLessThan(field, any())
            } returns previousQuery
            every {
                previousQuery.limit(1)
            } returns previousLimitedQuery

            every { finalQuery.get(Source.SERVER) } answers {
                mockk<Task<QuerySnapshot>>().apply {
                    every { isComplete } returns true
                    every { isSuccessful } returns true
                    every { isCanceled } returns false
                    every { result } answers {
                        mockk<QuerySnapshot>().apply {
                            every { documents } returns resultDocuments
                        }
                    }
                    every { exception } returns null
                }
            }
            every { previousLimitedQuery.get(Source.SERVER) } answers {
                mockk<Task<QuerySnapshot>>().apply {
                    every { isComplete } returns true
                    every { isSuccessful } returns true
                    every { isCanceled } returns false
                    every { result } answers {
                        mockk<QuerySnapshot>().apply {
                            every { documents } returns listOfNotNull(previousDocument)
                        }
                    }
                    every { exception } returns null
                }
            }
        }

        private fun mockDocumentSnapshot(
            id: String,
            date: Instant,
            exists: Boolean = true,
            timestampMillis: Double? = date.toEpochMilliseconds().toDouble(),
            legacyTimestampMillis: Double? = null,
        ): DocumentSnapshot {
            return mockk<DocumentSnapshot>().apply {
                every { this@apply.id } returns id
                every { exists() } returns exists
                every { getDouble(SmokeEntity.Fields.TIMESTAMP_MILLIS) } returns timestampMillis
                every { getDouble(SmokeEntity.Fields.LATITUDE) } returns null
                every { getDouble(SmokeEntity.Fields.LONGITUDE) } returns null
                every { getDouble("a") } returns legacyTimestampMillis
                every { getDouble("b") } returns null
                every { getDouble("c") } returns null
            }
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
}
