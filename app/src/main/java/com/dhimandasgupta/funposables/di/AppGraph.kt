package com.dhimandasgupta.funposables.di

import com.dhimandasgupta.funposables.statemachines.CounterStateMachineFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(AppScope::class)
interface AppGraph {
    val counterStateMachineFactory: CounterStateMachineFactory
}
