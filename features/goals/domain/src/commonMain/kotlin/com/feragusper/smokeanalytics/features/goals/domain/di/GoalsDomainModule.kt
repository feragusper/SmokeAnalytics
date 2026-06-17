package com.feragusper.smokeanalytics.features.goals.domain.di

import com.feragusper.smokeanalytics.features.goals.domain.EvaluateGoalProgressUseCase
import org.koin.dsl.module

/** Koin module exposing the goals domain use cases (shared across mobile and web). */
val goalsDomainModule = module {
    factory { EvaluateGoalProgressUseCase() }
}
