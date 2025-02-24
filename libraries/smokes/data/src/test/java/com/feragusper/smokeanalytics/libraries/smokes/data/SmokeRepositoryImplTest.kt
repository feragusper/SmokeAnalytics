package com.feragusper.smokeanalytics.libraries.smokes.data

class SmokeRepositoryImplTest {

//    private val firebaseAuth: FirebaseAuth = mockk()
//    private val firebaseFirestore: FirebaseFirestore = mockk()
//    private val smokeRepository = SmokeRepositoryImpl(
//        firebaseAuth = firebaseAuth,
//        firebaseFirestore = firebaseFirestore,
//        context = mockk(relaxed = true),
//    )
//
//    @Test
//    fun `GIVEN the user is null WHEN add smoke is called THEN it should throw an illegal state exception`() =
//        runTest {
//            every { firebaseAuth.currentUser } returns null
//
//            assertThrows<IllegalStateException> { smokeRepository.addSmoke(LocalDateTime.now()) }
//        }
//
//    @Nested
//    inner class UserLoggedIn {
//
//        private val uid = "uid"
//
//        @BeforeEach
//        fun setUp() {
//            every { firebaseAuth.currentUser } returns mockk<FirebaseUser>().apply {
//                every { this@apply.uid } returns this@UserLoggedIn.uid
//            }
//        }
//
//        @Disabled("TODO FIX THIS.")
//        @Test
//        fun `GIVEN user is logged in WHEN fetch smokes is called THEN it should finish`() =
//            runTest {
//                val id1 = "id1"
//                val id2 = "id2"
//                val date1: Date = mockk()
//                val date2: Date = mockk()
//                val localDateTime1: LocalDateTime = mockk()
//                val localDateTime2: LocalDateTime = mockk()
//                val timeAfterNothing: Pair<Long, Long> = mockk()
//                val timeAfter2: Pair<Long, Long> = mockk()
//                mockkStatic(LocalDateTime::timeAfter).apply {
//                    every { localDateTime2.timeAfter(null) } answers { timeAfterNothing }
//                    every { localDateTime1.timeAfter(localDateTime2) } answers { timeAfter2 }
//                }
//                mockkStatic(Date::toLocalDateTime).apply {
//                    every { date1.toLocalDateTime() } answers { localDateTime1 }
//                    every { date2.toLocalDateTime() } answers { localDateTime2 }
//                }
//                every {
//                    firebaseFirestore.collection("$USERS/$uid/$SMOKES")
//                        .orderBy(Smoke::date.name, Query.Direction.DESCENDING)
//                } answers {
//                    mockk<CollectionReference>().apply {
//                        every { get() } answers {
//                            mockk<Task<QuerySnapshot>>().apply {
//                                every { isComplete } answers { true }
//                                every { exception } answers { nothing }
//                                every { isCanceled } answers { false }
//                                every { isSuccessful } answers { true }
//                                every { result } answers {
//                                    mockk<QuerySnapshot>().apply {
//                                        every { documents } answers {
//                                            listOf(
//                                                mockk<DocumentSnapshot>().apply {
//                                                    every { id } answers { id1 }
//                                                    every { getDate(Smoke::date.name) } answers { date1 }
//                                                },
//                                                mockk<DocumentSnapshot>().apply {
//                                                    every { id } answers { id2 }
//                                                    every { getDate(Smoke::date.name) } answers { date2 }
//                                                },
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                assertEquals(
//                    smokeRepository.fetchSmokes(), listOf(
//                        Smoke(
//                            id = id1,
//                            date = localDateTime1,
//                            timeElapsedSincePreviousSmoke = timeAfter2
//                        ),
//                        Smoke(
//                            id = id2,
//                            date = localDateTime2,
//                            timeElapsedSincePreviousSmoke = timeAfterNothing
//                        ),
//                    )
//                )
//            }
//
//        @Disabled("TODO FIX THIS.")
//        @Test
//        fun `GIVEN user is logged in WHEN add smoke is called THEN it should finish`() = runTest {
//            every { firebaseFirestore.collection("$USERS/$uid/$SMOKES") } answers {
//                mockk<CollectionReference>().apply {
//                    every { add(any<Smoke>()) } answers {
//                        mockk<Task<DocumentReference>>().apply {
//                            every { isComplete } answers { true }
//                            every { exception } answers { nothing }
//                            every { isCanceled } answers { false }
//                            every { isSuccessful } answers { true }
//                            every { result } answers { mockk() }
//                        }
//                    }
//                }
//            }
//
//
//            smokeRepository.addSmoke(LocalDateTime.now())
//        }
//
//        @Disabled("TODO FIX THIS.")
//        @Test
//        fun `GIVEN user is logged in WHEN edit smoke is called THEN it should finish`() = runTest {
//
//            val id = "id"
//            val localDateTime: LocalDateTime = mockk()
//            val date: Date = mockk()
//
//            mockkStatic(LocalDateTime::toDate).apply {
//                every { localDateTime.toDate() } answers { date }
//            }
//
//            every {
//                firebaseFirestore
//                    .collection("$USERS/$uid/$SMOKES")
//                    .document(id)
//                    .set(SmokeEntity(date))
//            } answers {
//                mockk<Task<Void>>().apply {
//                    every { isComplete } answers { true }
//                    every { exception } answers { nothing }
//                    every { isCanceled } answers { false }
//                    every { isSuccessful } answers { true }
//                    every { result } answers { mockk() }
//                }
//            }
//
//            smokeRepository.editSmoke(
//                id = id,
//                date = localDateTime
//            )
//        }
//
//        @Disabled("TODO FIX THIS.")
//        @Test
//        fun `GIVEN user is logged in WHEN delete smoke is called THEN it should finish`() =
//            runTest {
//                val id = "id"
//
//                every {
//                    firebaseFirestore
//                        .collection("$USERS/$uid/$SMOKES")
//                        .document(id)
//                        .delete()
//                } answers {
//                    mockk<Task<Void>>().apply {
//                        every { isComplete } answers { true }
//                        every { exception } answers { nothing }
//                        every { isCanceled } answers { false }
//                        every { isSuccessful } answers { true }
//                        every { result } answers { mockk() }
//                    }
//                }
//
//                smokeRepository.deleteSmoke(id)
//            }
//    }

}
