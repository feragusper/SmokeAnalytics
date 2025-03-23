package com.feragusper.smokeanalytics.libraries.smokes.data

import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeAfter
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.toLocalDateTime
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class SmokeRepositoryImplTest {

    private val firebaseAuth: FirebaseAuth = mockk()
    private val firebaseFirestore: FirebaseFirestore = mockk()
    private val smokeRepository = SmokeRepositoryImpl(
        firebaseAuth = firebaseAuth,
        firebaseFirestore = firebaseFirestore
    )

    @Test
    fun `GIVEN the user is null WHEN add smoke is called THEN it should throw an illegal state exception`() =
        runTest {
            every { firebaseAuth.currentUser } returns null

            assertThrows<IllegalStateException> { smokeRepository.addSmoke(LocalDateTime.now()) }
        }

    @Nested
    inner class UserLoggedIn {

        private val id1 = "id1"
        private val id2 = "id2"

        private val localDateTime1 = LocalDateTime.of(2023, 1, 1, 12, 0)
        private val localDateTime2 = LocalDateTime.of(2023, 1, 1, 10, 0)

        private val timeAfterNothing = 0L to 0L
        private val timeAfter2 = localDateTime1.timeAfter(localDateTime2)

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
                    "date",
                    Query.Direction.DESCENDING
                )
            } returns collectionReference
        }

        @Test
        fun `GIVEN user is logged in WHEN fetch smokes is called THEN it should return a list of smokes`() =
            runTest {
                mockFetchSmokes()

                val result = smokeRepository.fetchSmokes(localDateTime1, localDateTime2)

                assertEquals(
                    listOf(
                        Smoke(
                            id = id1,
                            date = localDateTime1,
                            timeElapsedSincePreviousSmoke = timeAfter2
                        ),
                        Smoke(
                            id = id2,
                            date = localDateTime2,
                            timeElapsedSincePreviousSmoke = timeAfterNothing
                        )
                    ),
                    result
                )
            }

        @Test
        fun `GIVEN user is logged in WHEN add smoke is called THEN it should finish`() = runTest {
            val date = LocalDateTime.of(2023, 1, 1, 12, 0)
            val smokeEntitySlot = slot<SmokeEntity>()

            every { collectionReference.add(capture(smokeEntitySlot)) } answers {
                mockk<Task<DocumentReference>>().apply {
                    every { isComplete } returns true
                    every { isSuccessful } returns true
                    every { result } returns mockk()
                    every { exception } returns null
                    every { isCanceled } returns false
                }
            }

            smokeRepository.addSmoke(date)

            assertTrue(smokeEntitySlot.isCaptured)
            assertEquals(date, smokeEntitySlot.captured.date.toLocalDateTime())
        }

        @Test
        fun `GIVEN user is logged in WHEN edit smoke is called THEN it should finish`() = runTest {
            val id = "id"
            val date = Date.from(localDateTime1.atZone(ZoneId.systemDefault()).toInstant())

            every { collectionReference.document(id).set(SmokeEntity(date)) } answers {
                mockk<Task<Void>>().apply {
                    every { isComplete } returns true
                    every { isSuccessful } returns true
                    every { exception } returns null
                    every { isCanceled } returns false
                    every { result } returns mockk()
                }
            }

            smokeRepository.editSmoke(id, localDateTime1)
        }

        @Test
        fun `GIVEN user is logged in WHEN delete smoke is called THEN it should finish`() =
            runTest {
                val id = "id"

                every { collectionReference.document(id).delete() } answers {
                    mockk<Task<Void>>().apply {
                        every { isComplete } returns true
                        every { isSuccessful } returns true
                        every { exception } returns null
                        every { isCanceled } returns false
                        every { result } returns mockk()
                    }
                }

                smokeRepository.deleteSmoke(id)
            }

        private fun mockFetchSmokes() {
            val query = mockk<Query>(relaxed = true)
            val finalQuery = mockk<Query>(relaxed = true)

            every {
                collectionReference.orderBy("date", Query.Direction.DESCENDING)
            } returns query

            every {
                query.whereGreaterThanOrEqualTo(any<String>(), any())
            } returns finalQuery

            every {
                finalQuery.whereLessThan(any<String>(), any())
            } returns finalQuery

            every { finalQuery.get() } answers {
                mockk<Task<QuerySnapshot>>().apply {
                    every { isComplete } returns true
                    every { isSuccessful } returns true
                    every { isCanceled } returns false
                    every { result } answers {
                        mockk<QuerySnapshot>().apply {
                            every { documents } returns listOf(
                                mockDocumentSnapshot(id1, localDateTime1),
                                mockDocumentSnapshot(id2, localDateTime2)
                            )
                        }
                    }
                    every { exception } returns null
                }
            }
        }

        private fun mockDocumentSnapshot(id: String, date: LocalDateTime): DocumentSnapshot {
            return mockk<DocumentSnapshot>().apply {
                every { this@apply.id } returns id
                every { getDate("date") } answers {
                    Date.from(
                        date.atZone(ZoneId.systemDefault()).toInstant()
                    )
                }
            }
        }
    }
}
