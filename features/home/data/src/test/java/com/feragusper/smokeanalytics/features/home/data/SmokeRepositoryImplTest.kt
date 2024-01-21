package com.feragusper.smokeanalytics.features.home.data

import com.feragusper.smokeanalytics.features.home.data.SmokeRepositoryImpl.FirestoreCollection.Companion.SMOKES
import com.feragusper.smokeanalytics.features.home.data.SmokeRepositoryImpl.FirestoreCollection.Companion.USERS
import com.feragusper.smokeanalytics.features.home.domain.Smoke
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.timeAfter
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
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

class SmokeRepositoryImplTest {

    private val firebaseAuth: FirebaseAuth = mockk()
    private val firebaseFirestore: FirebaseFirestore = mockk()
    private val authenticationRepository = SmokeRepositoryImpl(
        firebaseAuth = firebaseAuth,
        firebaseFirestore = firebaseFirestore
    )

    @Test
    fun `GIVEN the user is null WHEN add smoke is called THEN it should throw an illegal state exception`() {
        every { firebaseAuth.currentUser } returns null

        runTest {
            assertThrows<IllegalStateException> { authenticationRepository.addSmoke() }
        }
    }

    @Nested
    inner class UserLoggedIn {

        private val uid = "uid"

        @BeforeEach
        fun setUp() {
            every { firebaseAuth.currentUser } returns mockk<FirebaseUser>().apply {
                every { this@apply.uid } returns this@UserLoggedIn.uid
            }
        }

        @Test
        fun `GIVEN user is logged in WHEN fetch smokes is called THEN it should finish`() {
            val date1: Date = mockk<Date>()
            val date2: Date = mockk<Date>()
            val timeAfterNothing: Pair<Long, Long> = mockk()
            val timeAfter2: Pair<Long, Long> = mockk()
            mockkStatic(Date::timeAfter).apply {
                every { date2.timeAfter(null) } answers { timeAfterNothing }
                every { date1.timeAfter(date2) } answers { timeAfter2 }
            }
            every {
                firebaseFirestore.collection("$USERS/$uid/$SMOKES")
                    .orderBy(Smoke::date.name, Query.Direction.DESCENDING)
            } answers {
                mockk<CollectionReference>().apply {
                    every { get() } answers {
                        mockk<Task<QuerySnapshot>>().apply {
                            every { isComplete } answers { true }
                            every { exception } answers { nothing }
                            every { isCanceled } answers { false }
                            every { isSuccessful } answers { true }
                            every { result } answers {
                                mockk<QuerySnapshot>().apply {
                                    every { documents } answers {
                                        listOf(
                                            mockk<DocumentSnapshot>().apply {
                                                every { getDate(Smoke::date.name) } answers { date1 }
                                            },
                                            mockk<DocumentSnapshot>().apply {
                                                every { getDate(Smoke::date.name) } answers { date2 }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            runTest {
                assertEquals(
                    authenticationRepository.fetchSmokes(), listOf(
                        Smoke(
                            date = date1,
                            timeElapsedSincePreviousSmoke = timeAfter2
                        ),
                        Smoke(
                            date = date2,
                            timeElapsedSincePreviousSmoke = timeAfterNothing
                        )
                    )
                )
            }
        }

        @Test
        fun `GIVEN user is logged in WHEN add smoke is called THEN it should finish`() {
            every { firebaseFirestore.collection("$USERS/$uid/$SMOKES") } answers {
                mockk<CollectionReference>().apply {
                    every { add(any<Smoke>()) } answers {
                        mockk<Task<DocumentReference>>().apply {
                            every { isComplete } answers { true }
                            every { exception } answers { nothing }
                            every { isCanceled } answers { false }
                            every { isSuccessful } answers { true }
                            every { result } answers { mockk() }
                        }
                    }
                }
            }

            runTest {
                authenticationRepository.addSmoke()
            }
        }
    }

}
