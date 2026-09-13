package com.dhimandasgupta.funposables.di

import androidx.compose.runtime.staticCompositionLocalOf
import com.dhimandasgupta.funposables.statemachines.CounterStateMachineFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(AppScope::class)
interface AppGraph {
  val counterStateMachineFactory: CounterStateMachineFactory
}

val LocalFunposablesGraph =
  staticCompositionLocalOf<AppGraph> {
    error("No NoteMarkGraph provided")
  }
