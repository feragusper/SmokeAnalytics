package com.feragusper.smokeanalytics.features.history.presentation.process

import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class HistoryProcessHolder(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val editSmokeUseCase: EditSmokeUseCase,
    private val deleteSmokeUseCase: DeleteSmokeUseCase,
    private val fetchSmokesUseCase: FetchSmokesUseCase,
    private val fetchSessionUseCase: FetchSessionUseCase,
) {

    fun processIntent(intent: HistoryIntent): Flow<HistoryResult> = when (intent) {
        is HistoryIntent.FetchSmokes -> processFetchSmokes(intent)
        is HistoryIntent.AddSmoke -> processAddSmoke(intent)
        is HistoryIntent.EditSmoke -> processEditSmoke(intent)
        is HistoryIntent.DeleteSmoke -> processDeleteSmoke(intent)
        HistoryIntent.NavigateUp -> flow { emit(HistoryResult.NavigateUp) }
    }

    private fun processFetchSmokes(intent: HistoryIntent.FetchSmokes) = flow {
        val tz = TimeZone.Companion.currentSystemDefault()

        val dayStart = intent.date.toLocalDateTime(tz).date.atStartOfDayIn(tz)
        val nextDayStart = dayStart.plus(1, DateTimeUnit.Companion.DAY, tz)

        when (fetchSessionUseCase()) {
            is Session.Anonymous -> emit(HistoryResult.NotLoggedIn(dayStart))

            is Session.LoggedIn -> {
                emit(HistoryResult.Loading)

                val raw = fetchSmokesUseCase(
                    start = dayStart,
                    end = nextDayStart,
                )

                val filtered = raw.filter { smoke ->
                    val d = smoke.date.toLocalDateTime(tz).date
                    d == dayStart.toLocalDateTime(tz).date
                }

                emit(
                    HistoryResult.FetchSmokesSuccess(
                        selectedDate = dayStart,
                        smokes = filtered,
                    )
                )
            }
        }
    }.catch { emit(HistoryResult.FetchSmokesError) }

    private fun processAddSmoke(intent: HistoryIntent.AddSmoke) = flow {
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> {
                emit(HistoryResult.Error.NotLoggedIn)
                emit(HistoryResult.GoToAuthentication)
            }

            is Session.LoggedIn -> {
                emit(HistoryResult.Loading)
                addSmokeUseCase(intent.date)
                emit(HistoryResult.AddSmokeSuccess)
            }
        }
    }.catch { emit(HistoryResult.Error.Generic) }

    private fun processEditSmoke(intent: HistoryIntent.EditSmoke) = flow {
        emit(HistoryResult.Loading)
        editSmokeUseCase(intent.id, intent.date)
        emit(HistoryResult.EditSmokeSuccess)
    }.catch { emit(HistoryResult.Error.Generic) }

    private fun processDeleteSmoke(intent: HistoryIntent.DeleteSmoke) = flow {
        emit(HistoryResult.Loading)
        deleteSmokeUseCase(intent.id)
        emit(HistoryResult.DeleteSmokeSuccess)
    }.catch { emit(HistoryResult.Error.Generic) }
}