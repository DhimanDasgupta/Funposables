package com.dhimandasgupta.funposables.statemachines

import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

interface CounterBaseState

data class CounterState(
    val enabled: Boolean = true,
    val counter: Int = 0
) : CounterBaseState

interface CounterBaseAction

object IncrementAction : CounterBaseAction
object DecrementAction : CounterBaseAction

@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
class CounterStateMachineFactory @Inject constructor()
    : FlowReduxStateMachineFactory<CounterBaseState, CounterBaseAction>() {
    init {
        initializeWith(reuseLastEmittedStateOnLaunch = true) { initialState() }

        spec {
            inState<CounterState> {
                on<IncrementAction> {
                    mutate { copy(enabled = false) }
                    delay(1000)
                    override { copy(counter = counter + 1) }
                }
                on<DecrementAction> {
                    mutate { copy(enabled = false) }
                    delay(1000)
                    override { copy(counter = counter - 1) }
                }
            }
        }
    }

    companion object {
        fun initialState(): CounterBaseState = CounterState()
    }
}
