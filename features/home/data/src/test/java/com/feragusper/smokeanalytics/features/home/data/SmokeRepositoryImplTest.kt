package com.feragusper.smokeanalytics.features.home.data

import com.feragusper.smokeanalytics.features.home.data.SmokeRepositoryImpl.FirestoreCollection.Companion.SMOKES
import com.feragusper.smokeanalytics.features.home.data.SmokeRepositoryImpl.FirestoreCollection.Companion.USERS
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SmokeRepositoryImplTest {

    private val firebaseAuth: FirebaseAuth = mockk()
    private val firebaseFirestore: FirebaseFirestore = mockk()
    private val authenticationRepository = SmokeRepositoryImpl(
        firebaseAuth = firebaseAuth,
        firebaseFirestore = firebaseFirestore
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `GIVEN the user is null WHEN add smoke is called THEN it should throw an illegal state exception`() {
        every { firebaseAuth.currentUser } returns null

        runTest {
            assertThrows<IllegalStateException> { authenticationRepository.addSmoke() }
        }
    }

    @Test
    fun `GIVEN the user is not null WHEN add smoke is called THEN it should finish`() {
        val uid = "uid"
        every { firebaseAuth.currentUser } returns mockk<FirebaseUser>().apply {
            every { this@apply.uid } returns uid
        }

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
