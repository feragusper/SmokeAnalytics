package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.AddCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.FetchActiveCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.ResolveCravingUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** The pending craving the user is currently resisting, flattened for SwiftUI. */
data class CravingDTO(val id: String, val createdAtEpochMillis: Long)

/**
 * Swift-facing entry point for the craving flow. A craving is started when the user feels the urge;
 * resolving it "resisted" (urge passed) or "gave in" (smoked) awards points and, on gave-in, also
 * logs a cigarette so Home/History stay accurate.
 */
class CravingFacade : KoinComponent {

    private val addCraving: AddCravingUseCase by inject()
    private val fetchActive: FetchActiveCravingUseCase by inject()
    private val resolveCraving: ResolveCravingUseCase by inject()
    private val addSmoke: AddSmokeUseCase by inject()

    /** The currently pending craving, or null. */
    @Throws(Throwable::class)
    suspend fun active(): CravingDTO? = fetchActive()?.let {
        CravingDTO(id = it.id, createdAtEpochMillis = it.createdAt.toEpochMilliseconds())
    }

    /** Starts tracking a craving. */
    @Throws(Throwable::class)
    suspend fun start() {
        addCraving()
    }

    /** The urge passed — resolve as resisted. */
    @Throws(Throwable::class)
    suspend fun resolveResisted() {
        fetchActive()?.let { resolveCraving(it, CravingOutcome.RESISTED) }
    }

    /** The user smoked — resolve as gave-in and log the cigarette. */
    @Throws(Throwable::class)
    suspend fun resolveGaveIn() {
        fetchActive()?.let { resolveCraving(it, CravingOutcome.GAVE_IN) }
        addSmoke()
    }
}
